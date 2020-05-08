 package jagex.runescape.net;
 
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 
 /**
  * PipelineFactory.java
  * @author Ryley M. Kimmel <ryley.kimmel@live.com>
  * @version 1.0
  * Aug 14, 2012
  */
 public final class PipelineFactory implements ChannelPipelineFactory {
 
     /** The instance for this class */
     private static PipelineFactory instance;
 
     @Override
     public ChannelPipeline getPipeline() {
 	final ChannelPipeline pipeline = Channels.pipeline();
 	pipeline.addFirst("handler", ChannelHandler.getInstance());
 	return pipeline;
     }
 
     /**
      * Gets and creates the instance for this class
      */
     public static PipelineFactory getInstance() {
 	if (instance == null) {
 	    instance = new PipelineFactory();
 	}
 	return instance;
     }
 
     /**
      * Blank constructor to prevent this class from being created
      */
     private PipelineFactory() { }
 
 }
