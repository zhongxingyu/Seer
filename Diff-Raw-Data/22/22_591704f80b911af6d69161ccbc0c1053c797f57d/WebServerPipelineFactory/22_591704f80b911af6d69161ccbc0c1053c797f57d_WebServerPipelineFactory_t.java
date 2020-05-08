 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
 package sirius.web.http;
 
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.handler.codec.http.HttpContentCompressor;
 import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
 import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
 import org.jboss.netty.handler.timeout.IdleStateHandler;
 import org.jboss.netty.util.Timer;
 import org.joda.time.Duration;
 import sirius.kernel.commons.PriorityCollector;
 import sirius.kernel.di.PartCollection;
 import sirius.kernel.di.std.ConfigValue;
 import sirius.kernel.di.std.Parts;
 
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Setup of the netty pipeline used by the {@link WebServer}
  *
  * @author Andreas Haufler (aha@scireum.de)
  * @since 2013/08
  */
 class WebServerPipelineFactory implements ChannelPipelineFactory {
 
     @Parts(WebDispatcher.class)
     private PartCollection<WebDispatcher> dispatchers;
 
     @ConfigValue("http.idleTimeout")
     private Duration idleTimeout;
 
     private List<WebDispatcher> sortedDispatchers;
     private Timer timer;
 
     protected WebServerPipelineFactory(Timer timer) {
         this.timer = timer;
     }
 
     @Override
     public ChannelPipeline getPipeline() throws Exception {
         ChannelPipeline pipeline = Channels.pipeline();
         pipeline.addFirst("lowlevel", LowLevelHandler.INSTANCE);
         pipeline.addLast("decoder", new HttpRequestDecoder());
         pipeline.addLast("encoder", new HttpResponseEncoder());
         if (idleTimeout != null && idleTimeout.getMillis() > 0) {
             pipeline.addLast("idler",
                              new IdleStateHandler(timer,
                                                  0,
                                                  0,
                                                   idleTimeout.getMillis(),
                                                   TimeUnit.MILLISECONDS));
         }
         pipeline.addLast("deflater", new HttpContentCompressor());
         pipeline.addLast("handler", new WebServerHandler(getSortedDispatchers()));
 
         return pipeline;
     }
 
     /*
      * Sorts all available dispatchers by their priority ascending
      */
     private List<WebDispatcher> getSortedDispatchers() {
         if (sortedDispatchers == null) {
             PriorityCollector<WebDispatcher> collector = PriorityCollector.create();
             for (WebDispatcher wd : dispatchers.getParts()) {
                 collector.add(wd.getPriority(), wd);
             }
             sortedDispatchers = collector.getData();
         }
         return sortedDispatchers;
     }
 }
