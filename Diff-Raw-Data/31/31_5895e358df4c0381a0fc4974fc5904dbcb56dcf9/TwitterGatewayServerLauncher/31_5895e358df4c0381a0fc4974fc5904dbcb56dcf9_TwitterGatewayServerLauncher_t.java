 package net.nitram509.launcher;
 
 import net.nitram509.config.EnvironmentConfig;
 import net.nitram509.logger.ConsoleLogger;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
 import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
 
 public class TwitterGatewayServerLauncher {
 
   public static void main(String[] args) throws Exception {
     printConfig();
 
     Server server = new Server(getPort());
 
     HandlerList handlerList = new HandlerList();
    handlerList.addHandler(createStaticResourceHandler());
     handlerList.addHandler(createServletContextHandler());
     server.setHandler(handlerList);
 
     server.start();
     server.join();
   }
 
   private static ServletContextHandler createServletContextHandler() {
     ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
     servletContextHandler.setContextPath("/");
    servletContextHandler.addFilter(createRestJerseyFilterHolder(), "/*", EnumSet.allOf(DispatcherType.class));
     return servletContextHandler;
   }
 
  private static FilterHolder createRestJerseyFilterHolder() {
    FilterHolder filterHolder = new FilterHolder();
    filterHolder.setName("Jersey REST");
    filterHolder.setClassName("com.sun.jersey.spi.container.servlet.ServletContainer");
    filterHolder.getInitParameters().put("com.sun.jersey.config.property.packages", "net.nitram509");
    filterHolder.getInitParameters().put("com.sun.jersey.config.feature.FilterForwardOn404", "true"); // allow static resources
    return filterHolder;
   }
 
  private static Handler createStaticResourceHandler() {
     ResourceHandler resourceHandler = new ResourceHandler();
     resourceHandler.setResourceBase("web");
     resourceHandler.setWelcomeFiles(new String[]{"index.html"});
     resourceHandler.setDirectoriesListed(false);
     return resourceHandler;
   }
 
   private static int getPort() {
     String port = System.getenv("PORT") == null ? "5000" : System.getenv("PORT");
     return Integer.parseInt(port);
   }
 
   private static void printConfig() {
     EnvironmentConfig config = new EnvironmentConfig();
     ConsoleLogger logger = new ConsoleLogger();
     logger.info("consumerKey >>> " + config.consumerKey());
     logger.info("defaultHashTag >>> " + config.defaultHashTag());
   }
 }
