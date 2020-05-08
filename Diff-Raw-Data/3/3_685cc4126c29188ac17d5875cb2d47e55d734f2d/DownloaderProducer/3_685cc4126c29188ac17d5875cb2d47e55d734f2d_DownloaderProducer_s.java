 package org.esquivo.weather.cdi;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.enterprise.inject.Disposes;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.deltaspike.core.api.config.ConfigProperty;
 import org.esquivo.downloader.Downloader;
 import org.esquivo.downloader.HCDownloader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DownloaderProducer {
     private static final Logger LOG = LoggerFactory.getLogger(DownloaderProducer.class);
 
     @Inject
     @ConfigProperty(name = "downloader.connetion.timeout")
     private Integer connectionTimeout;
 
     @Inject
     @ConfigProperty(name = "downloader.read.timeout")
     private Integer readTimeout;
 
     @Produces
     public Downloader createDownloader() {
         final Map<String,Object> params = new HashMap<String,Object>();
         
         params.put(HCDownloader.CONNECTION_TIMEOUT, connectionTimeout);
         params.put(HCDownloader.READ_TIMEOUT, readTimeout);
         
         LOG.debug("Creatting logger with properties : {}", ToStringBuilder.reflectionToString(params));
         
         return new HCDownloader(params);
     }
     
     public void disposeDownloader(@Disposes Downloader downloader) {
         downloader.dispose();
     }
 
 }
