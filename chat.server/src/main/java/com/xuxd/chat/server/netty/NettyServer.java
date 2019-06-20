package com.xuxd.chat.server.netty;

import com.xuxd.chat.common.netty.NettyRemoting;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @Auther: 许晓东
 * @Date: 19-6-14 16:30
 * @Description: 基于netty实现服务端通信
 */
public class NettyServer implements NettyRemoting {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private NettyServerConfig config;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup, workerGroup;
    private Channel channel;

    public NettyServer(NettyServerConfig config) {
        this.config = config;
    }

    public void start() {

        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(config.getBossThreads(), new DefaultThreadFactory("ChatServerBoss", true));
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads(), new DefaultThreadFactory("ChatServerWorker", true));
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddr())
                .localAddress(config.getIp(), config.getPort())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new FixedLengthFrameDecoder(1024)
                                , new StringDecoder(Charset.defaultCharset())
                                , new NettyServerHandler()
                        );
                    }
                });
        ChannelFuture future = bootstrap.bind().syncUninterruptibly();
        LOGGER.info("netty server started, bind ip:{},port:{}", config.getIp(), config.getPort());
        channel = future.channel();

    }

    public void close() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void send(Object message) {

    }

    public Object receive() {
        return null;
    }

    public Channel getChannel() {
        return channel;
    }
}
