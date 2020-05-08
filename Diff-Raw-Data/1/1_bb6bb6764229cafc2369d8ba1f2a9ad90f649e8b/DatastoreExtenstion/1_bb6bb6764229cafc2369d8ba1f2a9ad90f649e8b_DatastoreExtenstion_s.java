 package com.rhcloud.mongo.spi;
 
 import com.rhcloud.mongo.annotation.Document;
 
 import java.util.logging.Logger;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.spi.AfterBeanDiscovery;
 import javax.enterprise.inject.spi.BeforeBeanDiscovery;
 import javax.enterprise.inject.spi.Extension;
 import javax.enterprise.inject.spi.ProcessAnnotatedType;
 
 public class DatastoreExtenstion implements Extension {
 	
 	/**
      * 
      */
     
     private static final Logger LOG = Logger.getLogger(DatastoreExtenstion.class.getName());
     
     public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
     	LOG.info("beginning the scanning process");
     }
     
     public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
     	if (pat.getAnnotatedType().isAnnotationPresent(Document.class)) {
     		Document document = pat.getClass().getAnnotation(Document.class);
     		LOG.info(document.collection());        	   
         }
      } 
     
     public void afterBeanDiscover(@Observes AfterBeanDiscovery abd) {
     	LOG.info("finished the scanning process");
     }
 }
