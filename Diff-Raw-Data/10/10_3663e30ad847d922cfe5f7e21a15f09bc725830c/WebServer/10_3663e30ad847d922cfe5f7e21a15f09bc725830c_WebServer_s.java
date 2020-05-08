 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ar.com.sourcerain.labs.ide;
 
 import org.eclipse.jetty.security.HashLoginService;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 /**
  *
  * @author sebasjm (at) SourceRain.com.ar
  */
 public class WebServer {
 
     public static void main(String[] args) throws Exception {
        Server http = new Server(8080);
         Server comet = new Server(27306);
         
         {
             WebAppContext webapp = new WebAppContext();
             webapp.setContextPath("/");
             webapp.setWar(args[0]);
             
             ContextHandlerCollection contexts = new ContextHandlerCollection();
             contexts.setHandlers(new Handler[] { webapp });
             http.setHandler(contexts);
             
             HashLoginService hls = new HashLoginService("Security","etc/realm.properties");
             http.addBean(hls);
         }
 
 		{
             WebAppContext webapp = new WebAppContext();
             webapp.setContextPath("/");
             webapp.setWar(args[1]);
             ContextHandlerCollection contexts = new ContextHandlerCollection();
             contexts.setHandlers(new Handler[] { webapp });
             comet.setHandler(contexts);
         }
         
         
         comet.start();
         http.start();
         
         comet.join();
         http.join();
     }
 }
