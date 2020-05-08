 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 
 import java.awt.Desktop;
 import java.net.URL;
 
 /**
  * Author: Mat Schaffer <matschaffer@netflix.com>
  * Created: 8/10/13 11:53 PM
  */
 public class SlideServer {
     public static void main(String[] args) throws Exception {
         Server server = new Server(8080);
 
         RevealHandler revealHandler = new RevealHandler();
 
         ResourceHandler pwdHandler = new ResourceHandler();
         pwdHandler.setResourceBase(".");
 
         ResourceHandler revealResourceHandler = new ResourceHandler();
         URL revealAssets = SlideServer.class.getClassLoader().getResource("reveal");
 
         if (revealAssets != null) {
             revealResourceHandler.setResourceBase(revealAssets.toExternalForm());
         } else {
            throw new Exception("Couldn't find reveal assets, this is probably a bad build.");
         }
 
         HandlerList handlers = new HandlerList();
         handlers.setHandlers(new Handler[] { revealHandler, pwdHandler, revealResourceHandler });
         server.setHandler(handlers);
 
         server.start();
         Desktop.getDesktop().browse(server.getURI());
 
         server.join();
     }
 }
