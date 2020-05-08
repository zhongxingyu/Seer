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
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.handler.codec.http.HttpClientCodec;
 import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
 import org.jboss.netty.handler.ssl.SslHandler;
 
 /**
  * HttpClientPipelineFactory
  */
 class HttpClientPipelineFactory {
     private Config config;
     
     public HttpClientPipelineFactory(Config config) {
         this.config = config;
 
     }
 
     public ChannelPipeline getPipeline(Channel client, boolean useSSL,
         boolean keepAlive)
         throws Exception {
         ChannelPipeline pipeline = pipeline();
         
         if (useSSL) {
             SSLEngine engine = SslContextFactory.getClientContext(
                config.getProperty("ssl.keystore"),
                config.getProperty("ssl.keystore.password")).createSSLEngine();
             engine.setUseClientMode(true);
             pipeline.addLast("ssl", new SslHandler(engine));
         }
         
         pipeline.addLast("codec", new HttpClientCodec());
 
         // Remove the following line if you don't want automatic content
         // decompression.
         pipeline.addLast("inflater", new HttpContentDecompressor());
 
         // Uncomment the following line if you don't want to handle HttpChunks.
         // pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
 
         pipeline.addLast("handler", new HttpRelayingResponseHandler(client, keepAlive));
         return pipeline;
     }
 
 }
