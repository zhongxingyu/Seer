 package com.swijaya.coursera.posa.netty;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
 import static org.jboss.netty.channel.Channels.pipeline;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.concurrent.Executors;
 
 public class EchoServer {
 
     public static final int PORT = 8080;    // default port to bind our echo server to
 
     // create a global channel group to hold all currently open channels so that we
     // can close them all during application shutdown
     static final ChannelGroup allChannels = new DefaultChannelGroup("echo-server");
 
     public static void main(String[] args) throws Exception {
         int port = PORT;
 
         // The factory sets up a "reactor" configured with two thread pools to handle network events.
         // The first argument configures a "boss" thread pool (by default containing only one boss thread)
         // that accepts incoming connections, creates the channel abstractions for them, and pass them
         // along to the thread pool of workers, configured in the second argument.
         final ChannelFactory factory = new NioServerSocketChannelFactory(
                 Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
 
         ServerBootstrap bootstrap = new ServerBootstrap(factory);
 
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             @Override
             public ChannelPipeline getPipeline() throws Exception {
                 return pipeline(
                         new EchoServerHandler());
             }
         });
 
         // The bind() call triggers the creation of a server channel that acts as an "acceptor".
         // The actual OS bind() call (and others) is done by a "boss" thread configured with the bootstrap's factory.
         Channel c = bootstrap.bind(new InetSocketAddress(port));
         allChannels.add(c);
 
         // handle user Ctrl-C event to clean up after ourselves
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 ChannelGroupFuture f = allChannels.close();
                 f.awaitUninterruptibly();
                 factory.releaseExternalResources();
                 System.out.println("Shutdown.");
             }
         });
     }
 
 }
 
 class EchoServerHandler extends SimpleChannelUpstreamHandler {
 
     // dynamic buffer is used to store currently read bytes so far while we wait for an EOL marker
    private final ChannelBuffer buf = dynamicBuffer();
 
     @Override
     public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
         EchoServer.allChannels.add(e.getChannel());
     }
 
     @Override
     public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
         EchoServer.allChannels.remove(e.getChannel());
     }
 
     @Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
         SocketAddress remote = ctx.getChannel().getRemoteAddress();
         System.out.println("Connected to client: " + remote.toString());
     }
 
     @Override
     public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
         SocketAddress remote = ctx.getChannel().getRemoteAddress();
         System.out.println("Disconnected from client: " + remote.toString());
     }
 
     @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
         Channel ch = e.getChannel();
         ChannelBuffer m = (ChannelBuffer) e.getMessage();
 
         // read bytes from the event's channel buffer until we run out of bytes to read,
         // or we reach an EOL
         boolean eolFound = false;
        for (int i = 0; i < m.capacity(); i++) {
            byte b = m.getByte(i);
             buf.writeByte(b);
             if ((char) b == '\n') {
                 // we've read a line
                 eolFound = true;
                 break;
             }
         }
 
         if (!eolFound) {
             // there are still more bytes to read from the client for the current "line"
             return;
         }
 
         // we have a full line in the buffer; echo it to the client and reset buffers
        ChannelFuture f = ch.write(m);
         f.addListener(new ChannelFutureListener() {
             @Override
             public void operationComplete(ChannelFuture future) throws Exception {
                // clear the dynamic buffer we allocate for this handler so that we do not
                // run out of buffer space to write echoed lines to
                buf.clear();
             }
         });
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
         e.getCause().printStackTrace();
         e.getChannel().close();
     }
 
 }
