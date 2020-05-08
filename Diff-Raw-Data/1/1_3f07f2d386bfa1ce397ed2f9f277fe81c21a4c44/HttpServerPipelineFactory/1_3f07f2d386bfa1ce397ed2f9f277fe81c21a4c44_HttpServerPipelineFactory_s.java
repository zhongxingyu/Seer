 /**
  * Copyright (c) 2011 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package org.candlepin.thumbslug;
 
 import static org.jboss.netty.channel.Channels.*;
 
 import javax.net.ssl.SSLEngine;
 
 import org.candlepin.thumbslug.ssl.SslContextFactory;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.handler.codec.http.HttpContentCompressor;
 import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
 import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
 import org.jboss.netty.handler.ssl.SslHandler;
 
 /**
  * HttpServerPipelineFactory
  */
 public class HttpServerPipelineFactory implements ChannelPipelineFactory {
     
     private Config config;
     
     public HttpServerPipelineFactory(Config config) {
         this.config = config;
     }
         
     @Override
     public ChannelPipeline getPipeline() throws Exception {
         // Create a default pipeline implementation.
         ChannelPipeline pipeline = pipeline();
 
         if (config.getBoolean("ssl")) {
             SSLEngine engine =
                 SslContextFactory.getServerContext(config.getProperty("ssl.keystore"),
                     config.getProperty("ssl.keystore.password")).createSSLEngine();
             engine.setUseClientMode(false);
             pipeline.addLast("ssl", new SslHandler(engine));
         }
         
         pipeline.addLast("decoder", new HttpRequestDecoder());
         // Uncomment the following line if you don't want to handle HttpChunks.
         // pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
         pipeline.addLast("encoder", new HttpResponseEncoder());
         // Remove the following line if you don't want automatic content
         // compression.
         pipeline.addLast("deflater", new HttpContentCompressor());
         
         pipeline.addLast("logger", new HttpRequestLogger(config.getProperty("log.access")));
         
         pipeline.addLast("handler", new HttpRequestHandler(config));
         return pipeline;
     }
 }
