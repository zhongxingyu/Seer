 package wandledi.jetty;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.bio.SocketConnector;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.webapp.WebAppContext;
 import wandledi.java.WandlediFilter;
 
 /**
  * @author Markus Kahl
  */
 public class Application {
 
     private int port = 8080;
    private static String descriptor = "src/conf/web.xml";
    private static String webDirectory = "web";
 
     public Application() {
 
     }
 
     public Application(int port) {
 
         this.port = port;
     }
 
     public static void main(String[] args) {
 
         boolean nio = true;
         int port = 8080;
         String nios = getArgument("nio", args);
         String ports = getArgument("port", args);
         if (nios != null) {
             nio = Boolean.valueOf(nios);
         }
         if (ports != null) {
             port = Integer.valueOf(ports);
             System.out.println("Jetty: Listen to port " + port);
         }
         new Application(port).start(nio);
     }
 
     public void start(boolean nio) {
 
         Server server = new Server(port);
 
         WebAppContext context = new WebAppContext();
         context.addFilter(WandlediFilter.class, "/*", 1);
         context.setResourceBase(getWebDirectory());
         context.setContextPath("/");
         context.setParentLoaderPriority(true);
 
         server.setHandler(context);
         Connector connector = null;
         if (!nio) {
             System.out.println("Jetty: Using blocking IO instead of NIO");
             connector = (Connector) new SocketConnector();
         } else {
             connector = (Connector) new SelectChannelConnector();
         }
         connector.setPort(port);
         server.setConnectors(new Connector[] { connector });
         try {
             server.start();
             server.join();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static String getArgument(String name, String[] args) {
 
         for (String arg: args) {
             if (arg.startsWith(name)) {
                 if (arg.contains("=")) {
                     return arg.substring(arg.indexOf('=') + 1);
                 } else {
                     return "";
                 }
             }
         }
         return null;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public static String getWebDirectory() {
         return webDirectory;
     }
 
     public static void setWebDirectory(String webDirectory) {
         Application.webDirectory = webDirectory;
     }
 
     public static String getDescriptor() {
         return descriptor;
     }
 
     public static void setDescriptor(String descriptor) {
         Application.descriptor = descriptor;
     }
 }
