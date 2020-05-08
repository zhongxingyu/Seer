 package it.pronetics.madstore.server.spring;
 
 import it.pronetics.madstore.common.configuration.spring.AbstractMadStoreConfigurationFactoryBean;
 import it.pronetics.madstore.common.configuration.spring.MadStoreConfigurationBean;
 
 public class HttpCacheMaxAgeFactoryBean extends AbstractMadStoreConfigurationFactoryBean {
 
     public Object getObject() throws Exception {
         MadStoreConfigurationBean madstoreConfiguration = getMadStoreConfiguration();
         return madstoreConfiguration.getHttpCacheEnabled();
     }
 
     public Class getObjectType() {
         return Integer.class;
     }

 }
