 package lv.k2611a;
 
 
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.DefaultHandler;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.servlet.ServletHandler;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class App {
 
     public static final ClassPathXmlApplicationContext springContext;
     public static final AutowireCapableBeanFactory autowireCapableBeanFactory;
 
     static {
         springContext = new ClassPathXmlApplicationContext("application-context.xml");
         autowireCapableBeanFactory = springContext.getAutowireCapableBeanFactory();
         springContext.registerShutdownHook();
     }
 
     public static void main(String[] arg) throws Exception {
         int port = arg.length > 1 ? Integer.parseInt(arg[1]) : 8080;
         Server server = new Server(port);
 
         ServletHandler servletHandler = new ServletHandler();
         servletHandler.addServletWithMapping(GameServlet.class, "/chat/*");
 
 
         ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("./dune-client/resources/");
 
         DefaultHandler defaultHandler = new DefaultHandler();
 
         HandlerList handlers = new HandlerList();
         handlers.setHandlers(new Handler[]{servletHandler, resourceHandler, defaultHandler});
         server.setHandler(handlers);
 
         server.start();
         server.join();
     }
 }
