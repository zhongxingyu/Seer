 /*
  * This file is part of aion-unique <aion-unique.org>.
  *
  *  aion-unique is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-unique is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aionemu.commons.netty.handler;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 
 import org.apache.log4j.Logger;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 
 import com.aionemu.commons.netty.State;
 import com.aionemu.commons.netty.packet.BaseServerPacket;
 
 /**
  * @author ATracer
  */
 public abstract class AbstractChannelHandler extends SimpleChannelUpstreamHandler
 {
 	private static final Logger	log	= Logger.getLogger(AbstractChannelHandler.class);
 
 	/**
 	 * IP address of channel client
 	 */
 	protected InetAddress		inetAddress;
 	/**
 	 * Associated channel
 	 */
 	protected Channel			channel;
 
 	/**
 	 * Current state of this connection
 	 */
 	protected State			state;
 
 	/**
 	 * Creates a new instance.
 	 */
 	public AbstractChannelHandler()
 	{
 		super();
 	}
 
 	/**
 	 * @return Current state of this connection.
 	 */
 	public State getState()
 	{
 		return state;
 	}
 
 	/**
 	 * @param state
 	 *            Set current state of this connection.
 	 */
 	public void setState(State state)
 	{
 		this.state = state;
 	}
 
 	/**
 	 * Invoked when a Channel was disconnected from its remote peer
 	 */
 	@Override
 	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
 	{
 		log.debug("Channel disconnected IP: " + getIP());
 	}
 
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
 	{
 		super.channelConnected(ctx, e);
 		state = State.CONNECTED;
 		inetAddress = ((InetSocketAddress) e.getChannel().getRemoteAddress()).getAddress();
 		channel = ctx.getChannel();
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
 	{
 		e.getChannel().close();
 	}
 
 	/**
 	 * Closes the channel but ensures that packet is send before close
 	 * 
 	 * @param packet
 	 */
 	public void close(BaseServerPacket packet)
 	{
		packet.write(this);
		channel.write(packet.getBuf()).addListener(ChannelFutureListener.CLOSE);
 	}
 
 	/**
 	 * Closes the channel
 	 */
 	public void close()
 	{
 		channel.close();
 	}
 
 	/**
 	 * @return the IP address string
 	 */
 	public String getIP()
 	{
 		return inetAddress.getHostAddress();
 	}
 
 	/**
 	 * @param ctx
 	 * @param e
 	 * @throws Exception
 	 */
 	@Override
 	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception
 	{
 		super.handleUpstream(ctx, e);
 
 	}
 	
 	/**
 	 * 
 	 * @param packet
 	 */
 	public void sendPacket(BaseServerPacket packet)
 	{
 		packet.write(this);
 		channel.write(packet.getBuf());
 	}
 }
