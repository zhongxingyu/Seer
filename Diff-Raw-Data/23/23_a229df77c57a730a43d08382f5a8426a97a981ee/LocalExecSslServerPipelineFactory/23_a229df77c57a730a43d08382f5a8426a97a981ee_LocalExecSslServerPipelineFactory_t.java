 /**
  * Copyright 2009, Frederic Bregier, and individual contributors
  * by the @author tags. See the COPYRIGHT.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3.0 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package goldengate.commandexec.ssl.server;
 
 import static org.jboss.netty.channel.Channels.pipeline;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
 import org.jboss.netty.handler.codec.frame.Delimiters;
 import org.jboss.netty.handler.codec.string.StringDecoder;
 import org.jboss.netty.handler.codec.string.StringEncoder;
 
 import goldengate.commandexec.server.LocalExecServerHandler;
 import goldengate.commandexec.server.LocalExecServerPipelineFactory;
 import goldengate.commandexec.utils.LocalExecDefaultResult;
 import goldengate.common.crypto.ssl.GgSslContextFactory;
 
 /**
  * Version with SSL support
  *
  * @author Frederic Bregier
  *
  */
 public class LocalExecSslServerPipelineFactory extends LocalExecServerPipelineFactory {
 
     private GgSslContextFactory ggSslContextFactory;
     private ExecutorService executor = Executors.newCachedThreadPool();
 
     private long delay = LocalExecDefaultResult.MAXWAITPROCESS;
 
     /**
      * Constructor with default delay
      * @param newdelay
      */
     public LocalExecSslServerPipelineFactory(GgSslContextFactory ggSslContextFactory) {
         // Default delay
         this.ggSslContextFactory = ggSslContextFactory;
     }
 
     /**
      * Constructor with a specific default delay
      * @param newdelay
      */
     public LocalExecSslServerPipelineFactory(GgSslContextFactory ggSslContextFactory, long newdelay) {
         delay = newdelay;
         this.ggSslContextFactory = ggSslContextFactory;
     }
 
     public ChannelPipeline getPipeline() throws Exception {
         // Create a default pipeline implementation.
         ChannelPipeline pipeline = pipeline();
 
         // Add SSL as first element in the pipeline
         pipeline.addLast("ssl",
                 ggSslContextFactory.initPipelineFactory(true,
                        ggSslContextFactory.needClientAuthentication(), executor));
         // Add the text line codec combination first,
         pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,
                 Delimiters.lineDelimiter()));
         pipeline.addLast("decoder", new StringDecoder());
         pipeline.addLast("encoder", new StringEncoder());
 
         // and then business logic.
         // Could change it with a new fixed delay if necessary at construction
         pipeline.addLast("handler", new LocalExecServerHandler(this, delay));
 
         return pipeline;
     }
 
     /**
      * Release internal resources
      */
     public void releaseResources() {
         super.releaseResources();
         this.executor.shutdownNow();
     }
 
 }
