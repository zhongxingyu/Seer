 package com.camunda.fox.showcase.jobannouncement.service.camel;
 
 import org.apache.camel.CamelContext;
import org.apache.camel.component.cdi.CdiCamelContext;
 import org.apache.camel.component.twitter.TwitterComponent;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.inject.Inject;
 import java.util.logging.Logger;
 
 /**
  * BootStrap the Apache Camel context on a CDI environment.
  * See http://camel.apache.org/cdi.html
  */
 @Singleton
 @Startup
 public class ContextBootStrap {
 
     Logger logger = Logger.getLogger(getClass().getName());
 
     /*
      * These are The Job Announcer's Twitter API configuration settings
      * PLEASE! Do not use them on your own aps! Thanks!
      */
     private static String consumerKey = "B05xfeYmoEJjrikPo0Nv3Q";
     private static String consumerSecret = "vUqywyCeh2Z97rOCOymX6fpkqntAEhnISeN6KjGZ3Pk";
     private static String accessToken = "620425776-Hd9mhhDlCtrqGNADne9w3yv6CaTnaCXyXSex0I5j";
     private static String accessTokenSecret = "Gw0aiW3VYYpUazTRjjLaaUw5o1a0ivvlaslW0t7s40s";
 
     @Inject
     CdiCamelContext camelCtx;
 
     @Inject
     TwitterPostingCamelRoute tweetRoute;
 
     @PostConstruct
     public void init() throws Exception {
         logger.info(">> Starting Apache Camel's context: ...");
 
         /*
          *  Setup the Twitter component
          */
         TwitterComponent tc = camelCtx.getComponent("twitter", TwitterComponent.class);
         tc.setAccessToken(accessToken);
         tc.setAccessTokenSecret(accessTokenSecret);
         tc.setConsumerKey(consumerKey);
         tc.setConsumerSecret(consumerSecret);
 
         /*
          * Add the Camel routes
          */
         camelCtx.addRoutes(tweetRoute);
 
         /*
          * Start Camel context
          */
         camelCtx.start();
 
         logger.info(">> Camel context started and routes started.");
     }
 
     @PreDestroy
     public void stop() throws Exception {
        camelCtx.stop();
     }
 
     public CamelContext getCamelContext() {
         return this.camelCtx;
     }
 }
