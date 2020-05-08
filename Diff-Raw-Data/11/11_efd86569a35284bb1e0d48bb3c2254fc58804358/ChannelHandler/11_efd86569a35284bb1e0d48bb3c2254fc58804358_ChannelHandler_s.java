 package jagex.runescape.net;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 
 /**
  * ChannelHandler.java
  * @author Ryley M. Kimmel <ryley.kimmel@live.com>
  * @version 1.0
  * Aug 14, 2012
  */
 public final class ChannelHandler extends SimpleChannelHandler {
 
     /**	This singleton logger instance can be used for logging various component messages to the console */
     private static final Logger logger = Logger.getLogger(ChannelHandler.class.getSimpleName());
 
     /** The instance for this class */
     private static ChannelHandler instance;
 
     @Override
     public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
 	logger.log(Level.INFO, "Connection received, host: " + ctx.getChannel().getRemoteAddress());
     }
 
     @Override
     public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
 	logger.log(Level.INFO, "Connection terminated, host: " + ctx.getChannel().getRemoteAddress());
     }
 
     @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
	if (!(e.getMessage() instanceof Boolean)) {
	    throw new RuntimeException();
	}
	final boolean validMessage = (Boolean) e.getMessage();
	if (validMessage) {
	    return;
	}
	ctx.getChannel().close();
    }
 
     /**
      * Gets and creates the instance for this class
      */
     public static ChannelHandler getInstance() {
 	if (instance == null) {
 	    instance = new ChannelHandler();
 	}
 	return instance;
     }
 
     /**
      * Blank constructor to prevent this class from being created
      */
     private ChannelHandler() { }
 
 }
