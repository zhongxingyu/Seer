 /*
  * Copyright 2012 Nex yu Android authors
  * 
  * This file is part of Nex yu Android.
  * 
  * Nex yu Android is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Nex yu Android is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Nex yu Android. If not, see <http://www.gnu.org/licenses/>
  */
 package org.nexyu.nexyuAndroid.client;
 
 import java.net.SocketTimeoutException;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.nexyu.nexyuAndroid.client.protocol.NetworkMessage;
 import org.nexyu.nexyuAndroid.client.protocol.SMSToCell;
 import org.nexyu.nexyuAndroid.service.NexyuService;
 
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 import com.google.gson.JsonObject;
 
 /**
  * Handler that triggers the different actions of the application depending of
  * the received data.
  * 
  * @author Paul Ecoffet
  * @see org.jboss.netty.channel.SimpleChannelHandler
  */
 public class MessageClientHandler extends SimpleChannelHandler
 {
 	/**
 	 *
 	 */
 	private static final String	TAG	= "MessageClientHandler";
 	private Messenger			mService;
 
 	/**
 	 * Default constructor of MessageClientHandler, which requires a messenger
 	 * to communicate with the Nex yu core service.
 	 * 
 	 * @param service
 	 *            The Messenger of the Nex yu core service.
 	 */
 	public MessageClientHandler(Messenger service)
 	{
 		mService = service;
 	}
 
 	/**
 	 * Callback called when the connection between Nex yu Android is connected
 	 * to Nex yu Comp.
 	 * 
 	 * @author Paul Ecoffet
 	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
 	 *      org.jboss.netty.channel.ChannelStateEvent)
 	 */
 
 	// TODO Really Useful? Could be done with a ChannelFutureListener
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
 	{
 		Log.d(TAG, "Connected");
 		Message connected = Message.obtain(null, NexyuService.What.MSG_CONNECTED.ordinal());
 		try
 		{
 			mService.send(connected);
 			// TODO Send the contactsList only when asked.
 			// mService.send(askContactsList);
 		}
 		catch (RemoteException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Callback triggered when a exception is caught during the process of
 	 * gathering data. it displays the error in LogCat and then prints the Stack
 	 * Trace. Then it closes the channel.
 	 * 
 	 * @author Paul Ecoffet
 	 * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext,
 	 *      org.jboss.netty.channel.ExceptionEvent)
 	 */
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 	{
 		// TODO Handle correctly this exception (see
 		// http://stackoverflow.com/questions/3726696/setting-socket-timeout-on-netty-channel
 		// )
 		if (e.getCause() instanceof SocketTimeoutException)
 		{
 			Message message = Message.obtain(null,
 					NexyuService.What.MSG_IMPOSSIBLE_CONNECT.ordinal());
 			try
 			{
 				mService.send(message);
 				Log.w(TAG, "Impossible de se connecter");
 			}
 			catch (RemoteException e1)
 			{
 				e1.printStackTrace();
 			}
 			Channel ch = e.getChannel();
 			ch.close();
 		}
 	}
 
 	/**
 	 * Triggers different actions depending of the type of request written in
 	 * the received data.
 	 * 
 	 * @param message
 	 *            The data received from the computer application of Nex yu in
 	 *            JSON
 	 * @param ch
 	 *            The channel from where the data were received
 	 * @author Paul Ecoffet
 	 */
 	private void manageReceivedData(NetworkMessage message, Channel ch)
 	{
 		String type = message.getType();
		Message toService = null;
 		if (type.equals("messageToCell"))
 		{
 			toService = Message.obtain(null, NexyuService.What.MSG_SEND_SMS.ordinal());
 			try
 			{
 				toService.obj = new SMSToCell(message);
 			}
 			catch (ClassCastException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		else if (type.equals("askVerifCode"))
 		{
 			toService = Message.obtain(null, NexyuService.What.MSG_SEND_VERIF.ordinal());
 		}
 		else if (type.equals("askContacts"))
 		{
 			toService = Message.obtain(null, NexyuService.What.MSG_SEND_CONTACT_LIST.ordinal());
 		}
 		else if (type.equals("ok"))
 			Log.i(TAG, "ok");
 		else
 			Log.d(TAG, "Unknown type: " + type);
 
 		if (toService != null)
 		{
 			try
 			{
 				mService.send(toService);
 			}
 			catch (RemoteException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * messageReceived is the callback triggered when data are received. They
 	 * are cast in a JsonObject, they must have been handled beforehand with
 	 * {@link StringJSONtoNetMessageDecoder}. Once messageReceived has checked
 	 * the received data are in JSON and has cast this into a JsonObject, it
 	 * call the function
 	 * {@link MessageClientHandler#manageReceivedData(JsonObject, Channel)} that
 	 * trigger the action requested by the received data
 	 * 
 	 * @author Paul Ecoffet
 	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,
 	 *      org.jboss.netty.channel.MessageEvent)
 	 */
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 	{
 		if (e.getMessage() instanceof NetworkMessage)
 		{
 			Channel ch = e.getChannel();
 			NetworkMessage data = (NetworkMessage) e.getMessage();
 			manageReceivedData(data, ch);
 		}
 		else
 			Log.e(TAG, "Message received is not a NetworkMessage");
 	}
 
 	/**
 	 * Callback called when a write is requested. It checked if the data that
 	 * must be sent is a NetworkMessage, if it is not, it aborts the operation.
 	 * 
 	 * @author Paul Ecoffet
 	 * @see org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext,
 	 *      org.jboss.netty.channel.MessageEvent)
 	 */
 	@Override
 	public void writeRequested(ChannelHandlerContext ctx, MessageEvent evt)
 	{
 		if (evt.getMessage() instanceof NetworkMessage)
 			ctx.sendDownstream(evt);
 		else
 			Log.e(TAG, "Message to send is not a NetworkMessage object.");
 	}
 }
