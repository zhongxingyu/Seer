 /**
  * 
  */
 package org.nexyu.nexyuAndroid.client;
 
 import java.net.SocketTimeoutException;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 
 import android.util.Log;
 
 import com.google.gson.JsonObject;
 
 /**
  * Handler that trigger the different actions of the app depending of the
  * received data.
  * 
  * @author Paul Ecoffet
  * @see org.jboss.netty.channel.SimpleChannelHandler
  */
 public class MessageClientHandler extends SimpleChannelHandler
 {
 
 	/**
 	 * @author Paul Ecoffet
 	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
 	 *      org.jboss.netty.channel.ChannelStateEvent)
 	 */
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
 	{
 		Log.d("NEX", "Connected");
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
 			Channel ch = e.getChannel();
 			ch.close();
 		}
 
 	}
 
 	/**
 	 * Trigger different actions depending of the type of request written in the
 	 * received data.
 	 * 
 	 * @param data
 	 *            The data received from the computer application of Nexyu in
 	 *            JSON
 	 * @param ch
 	 *            The channel from where the data were received
 	 * @author Paul Ecoffet
 	 */
 	private void manageReceivedData(JsonObject data, Channel ch)
 	{
 
 		String type = data.get("type").getAsString();
 		if (type.equals("pong"))
 			Log.d("NEX", "Pong received");
 		else if(type.equals("ok"));
 		else
 			Log.d("NEX", "Unknown type");		
 	}
 
 	/**
 	 * messageReceived is the callback triggered when data are received. They
 	 * are cast in a JsonObject, they must have been handled beforehand with
 	 * {@link JSONDecoder}. Once messageReceived has checked the received data
 	 * are in JSON and has cast this into a JsonObject, it call the function
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
		if (e.getMessage() instanceof JsonObject)
 		{
 			Channel ch = e.getChannel();
 			JsonObject data = (JsonObject) e.getMessage();
 			manageReceivedData(data, ch);
 		}
 		else
 			Log.e("NEX", "Message received is not a JsonObject");
 	}
 }
