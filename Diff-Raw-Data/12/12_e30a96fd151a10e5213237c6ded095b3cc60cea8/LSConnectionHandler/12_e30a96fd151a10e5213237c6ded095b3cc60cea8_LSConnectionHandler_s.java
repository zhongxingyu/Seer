 package org.darkquest.ls.net;
 
 import org.darkquest.ls.LoginEngine;
 import org.darkquest.ls.Server;
 import org.darkquest.ls.model.World;
 import org.jboss.netty.channel.*;
 
 //import org.darkquest.ls.codec.LSCodecFactory;
 
 
 /**
  * Handles the protocol events fired from .
  */
 public class LSConnectionHandler extends SimpleChannelHandler {
 	/**
 	 * A reference to the login engine
 	 */
 	private LoginEngine engine;
 
 	/**
 	 * Creates a new connection handler for the given login engine.
 	 *
 	 * @param engine The engine in use
 	 */
 	public LSConnectionHandler(LoginEngine engine) {
 		this.engine = engine;
 	}
 
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
 		Channel session = ctx.getChannel();
 		session.setAttachment(session);
 	}
 
 	/**
 	 * Invoked whenever a packet is ready to be added to the queue.
 	 *
 	 * @param ctx The channel chandler context
 	 * @param e   The message event
 	 */
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
 		Channel ch = ctx.getChannel();
 		if(e.getMessage() instanceof LSPacket) {
 			if (ch.isConnected()) {
 				engine.getLSPacketQueue().add((LSPacket) e.getMessage());
 				return;
 			}
 
 		}
 	}
 
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
 		e.getCause().printStackTrace();
 		System.out.println("LS EXCEPTION: " + e.getCause().getMessage());
 
 	}
 
 	/**
 	 * Invoked whenever a a channel is closed. This must handle unregistering
 	 * the disconnecting world from the engine.
 	 *
 	 * @param ctx The channel chandler context
 	 * @param e   The state event
 	 */
 	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
 		World world = (World) ctx.getAttachment();
 		if (world != null) {
 			Server.getServer().setIdle(world, true);
 			world.clearPlayers();
 			Server.error("Connection to world " + world.getID() + " lost!");
 		}
 	}
 
 	/**
 	 * Invoked whenever a channel is opened
 	 *
 	 * @param ctx The channel chandler context
 	 * @param e   The state event
 	 */
 	public void channelOpened(ChannelHandlerContext ctx, ChannelStateEvent e) {
 		//TODO: Add filter the specific protocol
 		//session.getFilterChain().addFirst("protocolFilter", new ProtocolCodecFilter(new LSCodecFactory()));
 	}
 
 	/**
 	 * Invoked whenever a channel is unbinded
 	 *
 	 * @param ctx The channel chandler context
 	 * @param e   The state event
 	 */
 	public void unbindRequested(ChannelHandlerContext ctx, ChannelStateEvent e) {
 		ctx.getChannel().disconnect();
 	}
 }
