 package com.hopper.verb.handler;
 
 import com.hopper.GlobalConfiguration;
 import com.hopper.server.ComponentManager;
 import com.hopper.server.ComponentManagerFactory;
 import com.hopper.server.Endpoint;
 import com.hopper.session.IncomingSession;
 import com.hopper.session.Message;
 import com.hopper.session.SessionManager;
 import com.hopper.thrift.ChannelBound;
 import com.hopper.verb.Verb;
 import org.jboss.netty.channel.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.SocketAddress;
 
 public class ServerMessageHandler extends SimpleChannelHandler {
 
     /**
      * Logger
      */
     private static final Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);
 
     private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
 
     /**
      * The unique {@link SessionManager} instance
      */
     private final GlobalConfiguration config = componentManager.getGlobalConfiguration();
 
     @Override
     public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
 
         final Channel channel = ctx.getChannel();
         SocketAddress address = channel.getRemoteAddress();
         Endpoint endpoint = config.getEndpoint(address);
 
         // Only the allowed endpoints can connect to this port
         if (endpoint == null) {
             channel.close();
            logger.debug("Reject the invalidate connection from :" + address);
             return;
         }
 
        logger.debug("Accepted the connection from {},and creating incoming session for this connection...",
                endpoint.address);
 
         IncomingSession incomingSession = componentManager.getSessionManager().getIncomingSession(endpoint);
 
         if (incomingSession == null) {
             incomingSession = componentManager.getSessionManager().createIncomingSession(channel);
         }
 
         // The session has created for other channel from endpoint
         if (incomingSession.getConnection().getChannel() != channel) {
            logger.debug("The session for {} has created, only one connection can be allowed.", address);
             channel.close();
             return;
         }
 
         // create a outgoing session for the endpoint
        logger.debug("Creating outgoing session for {} ...", endpoint.address);
 
        try {
            componentManager.getSessionManager().createOutgoingSession(endpoint);
        } catch (Exception ex) {
            logger.debug("Failed to create outgoing session for {}", endpoint.address, ex);
        }
         // forward to others handlers
         super.channelOpen(ctx, e);
     }
 
     @Override
     public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
         final Channel channel = ctx.getChannel();
         Endpoint endpoint = config.getEndpoint(channel.getRemoteAddress());
         componentManager.getSessionManager().closeServerSession(endpoint);
         super.childChannelClosed(ctx, e);
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         try {
             // bound channel to thread local
             ChannelBound.bound(ctx.getChannel());
 
             if (e.getMessage() instanceof Message) {
                 Message message = (Message) e.getMessage();
 
                 // mutation the command
                 processReceivedMessage(message, e.getChannel());
 
             } else {
                 ctx.sendUpstream(e);
             }
         } finally {
             ChannelBound.unbound();
         }
     }
 
     private void processReceivedMessage(Message message, Channel channel) {
 
         Verb verb = message.getVerb();
 
         // register multiplexer session
         if (verb == Verb.BOUND_MULTIPLEXER_SESSION) {
             IncomingSession session = componentManager.getSessionManager().getIncomingSession(channel);
 
             BatchSessionCreator batchCreator = (BatchSessionCreator) message.getBody();
 
             for (String multiplexerSessionId : batchCreator.getSessions()) {
                 session.boundMultiplexerSession(multiplexerSessionId);
             }
 
             Message reply = new Message();
             reply.setId(message.getId());
             reply.setVerb(Verb.RES_BOUND_MULTIPLEXER_SESSION);
             reply.setBody(new byte[]{0});
             // send reply
             session.sendOneway(reply);
         } else {
             IncomingSession session = componentManager.getSessionManager().getIncomingSession(ChannelBound.get());
             // delegates the message processing to bound IncomingSession
             session.receive(message);
         }
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
         e.getCause().printStackTrace();
     }
 }
