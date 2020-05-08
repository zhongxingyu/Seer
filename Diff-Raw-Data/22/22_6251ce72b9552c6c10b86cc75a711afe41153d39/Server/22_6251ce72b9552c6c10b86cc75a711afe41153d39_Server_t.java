 package me.normanmaurer.netty4bench;
 
 
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.Unpooled;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.ChannelOption;
 import io.netty.channel.ChannelPipeline;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.MessageList;
 import io.netty.channel.SimpleChannelInboundHandler;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.handler.codec.MessageToMessageDecoder;
 import io.netty.handler.codec.MessageToMessageEncoder;
 import io.netty.handler.codec.http.DefaultFullHttpResponse;
 import io.netty.handler.codec.http.FullHttpResponse;
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpObject;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.HttpServerCodec;
 import io.netty.handler.ssl.SslHandler;
 import io.netty.util.CharsetUtil;
 import io.netty.util.ReferenceCountUtil;
 
 import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
 
 import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
 import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
 import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
 import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
 import static io.netty.handler.codec.http.HttpResponseStatus.OK;
 import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
 
 public class Server {
     private static final ByteBuf CONTENT =
             Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.US_ASCII));
 
     public static void main(String args[]) {
        //args = new String[] {"localhost", "8080", "16", "true"};
         if (args.length < 4) {
             System.err.println("Args must be: <host(String)> <port(int)> <numHandler(int)> <useSsl(boolean)>");
             System.exit(1);
             return;
         }
         String host = args[0];
         int port = Integer.parseInt(args[1]);
         final int numHandlers = Integer.parseInt(args[2]);
         final boolean useSsl = Boolean.valueOf(args[3]);
 
         EventLoopGroup group = new NioEventLoopGroup();
         try {
             ServerBootstrap bootstrap = new ServerBootstrap();
             bootstrap.option(ChannelOption.SO_REUSEADDR, true);
             bootstrap.channel(NioServerSocketChannel.class).group(group).localAddress(host, port);
             bootstrap.childHandler(new ChannelInitializer<Channel>() {
                 @Override
                 protected void initChannel(Channel ch) throws Exception {
                     int extraHandlers = 2;
                     ChannelPipeline pipeline = ch.pipeline();
                     if (useSsl) {
                         extraHandlers += 1;
                         SSLContext context = BogusSslContextFactory.getServerContext();
                        SSLEngine engine = context.createSSLEngine();
                        engine.setUseClientMode(false);
                        pipeline.addLast(new SslHandler(engine));
                     }
 
                     int num = numHandlers - extraHandlers;
                     for (int i = 0; i < num; i++) {
                         if (i % 2  == 0) {
                             pipeline.addLast(new MessageToMessageDecoder<Object>() {
                                 @Override
                                 protected void decode(ChannelHandlerContext ctx, Object msg, MessageList<Object> out) throws Exception {
                                     out.add(ReferenceCountUtil.retain(msg));
                                 }
                             });
                         } else {
                             pipeline.addLast(new MessageToMessageEncoder<Object>() {
                                 @Override
                                 protected void encode(ChannelHandlerContext ctx, Object msg, MessageList<Object> out) throws Exception {
                                     out.add(ReferenceCountUtil.retain(msg));
                                 }
                             });
                         }
                     }
 
                     pipeline.addLast(new HttpServerCodec());
                     pipeline.addLast(new SimpleChannelInboundHandler<HttpObject>() {
 
                         @Override
                         public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                             if (msg instanceof HttpRequest) {
                                 HttpRequest req = (HttpRequest) msg;
                                 boolean keepAlive = isKeepAlive(req);
                                 FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, CONTENT.duplicate());
                                 response.headers().set(CONTENT_TYPE, "text/plain");
                                 response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
 
                                 if (!keepAlive) {
                                     ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                                 } else {
                                     response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                                     ctx.write(response);
                                 }
                             }
                         }
 
                         @Override
                         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                             cause.printStackTrace();
                             ctx.close();
                         }
                     });
                 }
             });
             bootstrap.bind().syncUninterruptibly().channel().closeFuture().syncUninterruptibly();
         } finally {
             group.shutdownGracefully().syncUninterruptibly();
         }
     }
 
 }
