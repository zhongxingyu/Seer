 package netty.rpc.client;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import netty.rpc.coder.Transport;
 
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 客户端消息处理
  * @author steven.qiu
  *
  */
 public class ClientHandler extends SimpleChannelUpstreamHandler{
 	
 	private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
 	
 	private ConcurrentHashMap<String, ResultHandler> callbackHandlerMap;
 	
 	public ClientHandler(ConcurrentHashMap<String, ResultHandler> callbackHandlerMap){
 		this.callbackHandlerMap = callbackHandlerMap;
 	}
 	
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e){
 		Transport transport = (Transport)e.getMessage();
 		String keyString = new String(transport.getKey());
 		ResultHandler handler = callbackHandlerMap.remove(keyString);
 		if(handler!=null){
 			handler.processor(transport.getValue());
 		}else{
			logger.warn("Can not find the handle with the key {}", keyString);
 		}
 	}
 	
 	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception{
 		super.channelClosed(ctx, e);
 	}
 	
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception{
 		logger.error("Client Catch a Exception", e.getCause());
 		e.getChannel().close();
 	}
 
 }
