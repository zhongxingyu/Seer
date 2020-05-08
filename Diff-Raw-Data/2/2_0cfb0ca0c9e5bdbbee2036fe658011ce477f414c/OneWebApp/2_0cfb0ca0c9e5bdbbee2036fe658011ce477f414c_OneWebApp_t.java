 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 import java.io.File;
 
 /**
  * Created with IntelliJ IDEA.
  * User: stevelowenthal
  * Date: 9/21/13
  * Time: 10:12 AM
  *
  */
 public class OneWebApp {
 
  public static final String WAR_FILE_LOCATION = "warmodule/target/webmodule-1.0-SNAPSHOT.war".replace('/', File.separatorChar);
   public static final String DEPLOYMENT_CONTEXT_PATH = "/warmodule";
 
   public static void main(String[] args) throws Exception
   {
     String jetty_home = System.getProperty("jetty.home",".");
 
     Server server = new Server(8080);
 
     WebAppContext webapp = new WebAppContext();
     webapp.setContextPath(DEPLOYMENT_CONTEXT_PATH);
     webapp.setWar(jetty_home + File.separator + WAR_FILE_LOCATION);
     server.setHandler(webapp);
 
     server.start();
     server.join();
   }
 }
