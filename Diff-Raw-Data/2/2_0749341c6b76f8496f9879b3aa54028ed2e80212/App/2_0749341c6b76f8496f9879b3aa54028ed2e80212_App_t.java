 package az.his;
 
 import org.apache.catalina.Context;
 import org.apache.catalina.LifecycleException;
 import org.apache.catalina.startup.Tomcat;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.net.MalformedURLException;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args ) throws ServletException, LifecycleException, MalformedURLException {
         String webappDirLocation = "../webapp/";
         Tomcat tomcat = new Tomcat();
 
         String webPort = System.getenv("PORT");
         if(webPort == null || webPort.isEmpty()) {
             webPort = "8080";
         }
 
         tomcat.setPort(Integer.valueOf(webPort));
         tomcat.enableNaming();
         tomcat.setBaseDir("tomcat");
 
         Context context = tomcat.addWebapp("/his", new File(webappDirLocation).getAbsolutePath());
        File configFile = new File(webappDirLocation + "META-INF/context.xml");
         context.setConfigFile(configFile.toURI().toURL());
         System.out.println("configuring app with basedir: " + new File("./" + webappDirLocation).getAbsolutePath());
 
         tomcat.start();
         tomcat.getServer().await();
     }
 }
