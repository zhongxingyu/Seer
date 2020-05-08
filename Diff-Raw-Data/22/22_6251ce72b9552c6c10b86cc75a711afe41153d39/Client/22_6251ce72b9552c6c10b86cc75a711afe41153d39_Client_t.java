 package me.normanmaurer.netty4bench;
 
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.ChannelOption;
 import io.netty.channel.ChannelPipeline;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.SimpleChannelInboundHandler;
 import io.netty.channel.group.DefaultChannelGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.handler.codec.http.DefaultFullHttpRequest;
 import io.netty.handler.codec.http.FullHttpRequest;
 import io.netty.handler.codec.http.HttpClientCodec;
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpMethod;
 import io.netty.handler.codec.http.HttpObject;
 import io.netty.handler.codec.http.HttpVersion;
 import io.netty.handler.codec.http.LastHttpContent;
 import io.netty.handler.ssl.SslHandler;
 
import javax.net.ssl.SSLEngine;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 
 public class Client {
 
     public static void main(String[] args) throws Exception {
        //args = new String[] {"localhost", "8080", "3", "100", "true", "60000"};
         if (args.length < 6) {
             System.err.println("Args must be: <host(String)> <port(int)> <requestsPerConnection(int)> <concurrentConnections(int)> <useSsl(boolean)> <timeToRun(long)>");
             System.exit(1);
             return;
         }
         final String host = args[0];
         final int port = Integer.parseInt(args[1]);
         final int requestsPerConnection = Integer.parseInt(args[2]);
         int concurrentConnections = Integer.parseInt(args[3]);
         final boolean useSsl = Boolean.valueOf(args[4]);
         long timeToRun = Long.parseLong(args[5]);
         final AtomicBoolean done = new AtomicBoolean();
         EventLoopGroup group = new NioEventLoopGroup();
         final AtomicLong requests = new AtomicLong();
         final AtomicLong responses = new AtomicLong();
         try {
             final DefaultChannelGroup channelGroup = new DefaultChannelGroup(group.next());
 
             final Bootstrap bootstrap = new Bootstrap();
             bootstrap.group(group).channel(NioSocketChannel.class);
             bootstrap.option(ChannelOption.SO_REUSEADDR, true);
             bootstrap.handler(new ChannelInitializer<Channel>() {
                 @Override
                 protected void initChannel(Channel ch) throws Exception {
                     ChannelPipeline pipeline = ch.pipeline();
                     if (useSsl) {
                        SSLEngine engine = BogusSslContextFactory.getClientContext().createSSLEngine();
                        engine.setUseClientMode(true);
                        pipeline.addLast(new SslHandler(engine));
                     }
                     pipeline.addLast(new HttpClientCodec());
                     pipeline.addLast(new SimpleChannelInboundHandler<HttpObject>() {
                         private int count;
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws Exception {
                             channelGroup.add(ctx.channel());
                             writeRequest(ctx, true);
                         }
 
                         @Override
                         protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                             if (msg instanceof LastHttpContent) {
                                 responses.incrementAndGet();
                                 if (count < requestsPerConnection) {
                                     writeRequest(ctx, count + 1 != requestsPerConnection);
                                 } else {
                                     ctx.close();
                                 }
                             }
                         }
 
                         @Override
                         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                             cause.printStackTrace();
                             ctx.close();
                         }
 
                         private void writeRequest(ChannelHandlerContext ctx, boolean keepAlive) {
                             FullHttpRequest request = new DefaultFullHttpRequest(
                                     HttpVersion.HTTP_1_1, HttpMethod.GET, "http://" + host + ":" + port + "/");
                             if (keepAlive) {
                                 request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                             }
                             ctx.write(request);
                             requests.incrementAndGet();
                             count++;
                         }
                     });
                 }
             });
             bootstrap.remoteAddress(host, port);
             for (int i = 0; i < concurrentConnections; i++) {
                 ChannelFuture future = bootstrap.connect();
                 future.channel().closeFuture().addListener(new ChannelFutureListener() {
                     @Override
                     public void operationComplete(ChannelFuture future) throws Exception {
                         if (!done.get()) {
                             bootstrap.connect().channel().closeFuture().addListener(this);
                         }
                     }
                 });
             }
 
             Thread.sleep(timeToRun);
             done.set(true);
             int connected;
             while ((connected = channelGroup.size()) > 0) {
                 System.out.println("Connected clients " + connected + "/" + concurrentConnections + ", waiting for gracefully shutdown");
                 Thread.sleep(1000);
             }
             System.out.println("Num requests sent:              " + requests.get());
             System.out.println("Num responses received:         " + responses.get());
             System.out.println("Num requests per connection:    " + requestsPerConnection);
             System.out.println("Concurrent connections:         " + concurrentConnections);
             System.out.println("Run time:                       " + timeToRun);
 
         } finally {
             group.shutdownGracefully().syncUninterruptibly();
         }
     }
 }
