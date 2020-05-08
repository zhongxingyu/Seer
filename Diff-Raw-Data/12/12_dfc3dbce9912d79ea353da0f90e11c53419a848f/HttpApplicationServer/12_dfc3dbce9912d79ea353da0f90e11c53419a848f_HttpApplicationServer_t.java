 package com.example.stocks.infrastructure.rest;
 
import com.example.stocks.core.Valuation;
 import com.example.stocks.infrastructure.server.PortfolioBuilder;
 import com.example.stocks.infrastructure.server.Server;
 import com.googlecode.utterlyidle.Application;
 import com.googlecode.utterlyidle.ApplicationBuilder;
 import com.googlecode.utterlyidle.Resources;
 import com.googlecode.utterlyidle.ServerConfiguration;
 import com.googlecode.utterlyidle.httpserver.RestServer;
 import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
 import com.googlecode.utterlyidle.modules.ResourcesModule;
 import com.googlecode.yadic.Container;
 
 import java.io.IOException;
 
 import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
 import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
 
 public class HttpApplicationServer implements Server {
 
     private RestServer server;
     private PortfolioBuilder portfolio;
 
     public HttpApplicationServer(PortfolioBuilder portfolio) {
         this.portfolio = portfolio;
     }
 
     @Override
     public void start() {
         Application application = ApplicationBuilder.application().add(new PortfolioFeature(portfolio)).addAnnotated(Version.class).build();
         ServerConfiguration configuration = defaultConfiguration().port(8000);
         try {
             server = new RestServer(application, configuration);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void stop() {
         try {
             server.close();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static class PortfolioFeature implements ResourcesModule, ApplicationScopedModule {
 
         private final PortfolioBuilder portfolio;
 
         private PortfolioFeature(PortfolioBuilder portfolio) {
             this.portfolio = portfolio;
         }
 
         @Override
         public Resources addResources(Resources resources) throws Exception {
             return resources.add(annotatedClass(PortfolioResource.class));
         }
 
         @Override
         public Container addPerApplicationObjects(Container container) throws Exception {
            return container.addInstance(Valuation.class, portfolio.build());
         }
     }
 }
