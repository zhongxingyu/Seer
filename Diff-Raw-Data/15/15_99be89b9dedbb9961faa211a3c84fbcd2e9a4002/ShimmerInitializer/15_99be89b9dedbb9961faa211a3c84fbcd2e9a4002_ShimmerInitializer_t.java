 package com.sproutinc.shimmer.config;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.web.context.ContextLoaderListener;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 import org.springframework.web.servlet.DispatcherServlet;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRegistration;
 import javax.servlet.annotation.WebListener;
 
 @WebListener
 public class ShimmerInitializer implements WebApplicationInitializer {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ShimmerInitializer.class);
 
     @Override
     public void onStartup(ServletContext servletContext) throws ServletException {
         LOGGER.info("onStartup called!!");
         AnnotationConfigWebApplicationContext context =
                 new AnnotationConfigWebApplicationContext();
         context.register(ShimmerConfiguration.class);
         context.scan("com.sproutinc.shimmer");
         servletContext.addListener(new ContextLoaderListener(context));
         ServletRegistration.Dynamic dispatcher =
                 servletContext.addServlet("dispatcher", new DispatcherServlet(context));
         dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/api/*");
     }
 }
