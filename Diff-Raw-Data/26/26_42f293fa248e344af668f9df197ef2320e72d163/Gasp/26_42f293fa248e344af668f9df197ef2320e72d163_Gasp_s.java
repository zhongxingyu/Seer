 package com.cloudbees.gasp;
 
 import com.cloudbees.gasp.resources.RestaurantResource;
 import com.cloudbees.gasp.resources.ReviewResource;
 import com.cloudbees.gasp.resources.UserResource;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.persist.PersistFilter;
 import com.google.inject.persist.jpa.JpaPersistModule;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.sun.jersey.guice.JerseyServletModule;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Entry point to the app.
  *
  * @author Kohsuke Kawaguchi
  */
 public class Gasp extends GuiceServletContextListener {
 
     @Override
     protected Injector getInjector() {
         return Guice.createInjector(
             new JpaPersistModule("gasp"),
             new JerseyServletModule() {
                 @Override
                 protected void configureServlets() {
                     bind(RestaurantResource.class);
                     bind(UserResource.class);
                     bind(ReviewResource.class);
 
                     filter("/*").through(PersistFilter.class);
                    serveRegex("/.+").with(GuiceContainer.class,POJO_JSON_MAPPING);
                 }
             }
         );
     }
 
     private static final Map<String,String> POJO_JSON_MAPPING = Collections.singletonMap(
             "com.sun.jersey.api.json.POJOMappingFeature", "true"
     );
 
 }
