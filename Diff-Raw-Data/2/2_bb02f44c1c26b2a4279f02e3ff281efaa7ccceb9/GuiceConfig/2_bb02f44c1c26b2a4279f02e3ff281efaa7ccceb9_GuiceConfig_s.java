 package org.guiceae.util;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.persist.PersistFilter;
 import com.google.inject.persist.jpa.JpaPersistModule;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.google.inject.servlet.ServletModule;
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 import org.guiceae.main.ioc.GuiceModule;
 import org.guiceae.main.web.UploadServlet;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 public class GuiceConfig extends GuiceServletContextListener {
     Logger log = Logger.getLogger(GuiceConfig.class.getName());
 
     protected Injector getInjector() {
 
         final Map<String, String> params = new HashMap<String, String>();
        params.put("javax.ws.rs.Application", "org.guiceae.util.JaxrsControllersConfig");
         params.put(JSONConfiguration.FEATURE_POJO_MAPPING,"true");
         params.put("com.sun.jersey.config.property.JSPTemplatesBasePath","/WEB-INF/jsp");
 
         Injector injector = Guice.createInjector(
                 new JpaPersistModule("transactions-optional"),
                 new UtilModule(),
                 new ServletModule() {
                     @Override
                     protected void configureServlets() {
                         bind(UserPrincipalHolder.class);
 
                         serve("/app/upload").with(UploadServlet.class);
                         serve("/*").with(GuiceContainer.class, params);
                         filter("/*").through(InjectorFilter.class);
                         filter("/*").through(EncodingFilter.class);
                         filter("/*").through(PersistFilter.class);
                         filter("/*").through(SecurityFilter.class);
                     }
                 },
                 new GuiceModule());
 
         return injector;
     }
 }
