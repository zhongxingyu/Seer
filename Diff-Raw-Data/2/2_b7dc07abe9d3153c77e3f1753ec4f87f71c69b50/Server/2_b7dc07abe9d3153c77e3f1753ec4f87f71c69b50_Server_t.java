 package com.cgbystrom;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.handler.codec.http.*;
 import org.jboss.netty.util.CharsetUtil;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;
 
 import static org.jboss.netty.channel.Channels.pipeline;
 
 public class Server {
     public static class HttpHandler extends SimpleChannelUpstreamHandler {
         private static final ChannelBuffer HELLO_WORLD = ChannelBuffers.copiedBuffer("Hello world!", CharsetUtil.UTF_8);
 
         @Override
         public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
             HttpRequest request = (HttpRequest) e.getMessage();
 
             HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
             response.setContent(HELLO_WORLD);
             response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
 
             if (HttpHeaders.isKeepAlive(request)) {
                 if (request.getProtocolVersion() == HttpVersion.HTTP_1_0) {
                     response.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                 }
                 e.getChannel().write(response);
             } else {
                 e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
             }
         }
     }
 
     public static void main(String[] args) {
         final HttpHandler handler = new HttpHandler();
         int numThreads = 2 * Runtime.getRuntime().availableProcessors();
         int backlog = 128;
         int port = 8080;
 
         if (System.getProperty("threads") != null)
             numThreads = Integer.valueOf(System.getProperty("threads"));
 
         if (System.getProperty("backlog") != null)
            backlog = Integer.valueOf(System.getProperty("backlog"));
 
         if (System.getProperty("port") != null)
             port = Integer.valueOf(System.getProperty("port"));
 
 
         System.out.println("Threads: " + numThreads + " (set with -Dthreads=8)");
         System.out.println("Backlog: " + backlog + " (set with -Dbacklog=128)");
         System.out.println("   Port: " + port + " (set with -Dport=8080)");
 
         ServerBootstrap bootstrap = new ServerBootstrap(
                 new NioServerSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool(), numThreads));
 
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             @Override
             public ChannelPipeline getPipeline() throws Exception {
                 ChannelPipeline pipeline = pipeline();
                 pipeline.addLast("decoder", new HttpRequestDecoder());
                 pipeline.addLast("encoder", new HttpResponseEncoder());
                 pipeline.addLast("handler", handler);
                 return pipeline;
             }
         });
         bootstrap.bind(new InetSocketAddress(8080));
         System.out.println("Server running..");
     }
 }
