 package ddth.dasp.hetty.front;
 
 import java.util.concurrent.TimeUnit;
 
 import org.jboss.netty.channel.ChannelHandler;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
 import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
 import org.jboss.netty.handler.timeout.IdleStateHandler;
 import org.jboss.netty.util.Timer;
 
 import ddth.dasp.hetty.qnt.IQueueWriter;
 
 public class HettyPipelineFactory implements ChannelPipelineFactory {
 
     private final ChannelHandler idleStateHandler;
 
     private IQueueWriter queueWriter;
 
     public HettyPipelineFactory(IQueueWriter queueWriter, Timer timer, long readTimeoutMillisecs,
             long writeTimeoutMillisecs) {
         this.queueWriter = queueWriter;
         this.idleStateHandler = new IdleStateHandler(timer, readTimeoutMillisecs,
                writeTimeoutMillisecs, 0, TimeUnit.MILLISECONDS);
     }
 
     @Override
     public ChannelPipeline getPipeline() throws Exception {
         ChannelPipeline pipeline = Channels.pipeline();
         pipeline.addLast("decoder", new HttpRequestDecoder());
         // pipeline.addLast("aggregator", new HttpChunkAggregator(128 * 1024));
         pipeline.addLast("encoder", new HttpResponseEncoder());
         pipeline.addLast("timeout", idleStateHandler);
         pipeline.addLast("handler", new HettyHttpHandler(this.queueWriter));
         return pipeline;
     }
 }
