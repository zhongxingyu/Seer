 import java.net.URL;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 /**
  * 
  * This class launches the web application in an embedded Jetty container.
  * This is the entry point to your application. The Java command that is used for
  * launching should fire this main method.
  *
  * @author John Simone
  */
 public class JettyLauncher {
 
     private URL getWarLocation() {
         return this.getClass().getProtectionDomain().getCodeSource().getLocation();
     }
     
     /**
      * @param args
      */
     public static void main(String[] args) throws Exception{
         Server server = new Server(8080);
         WebAppContext root = new WebAppContext();
 
         //new URL("jar:file:/home/jsimone/.m2/repository/com/force/sample/springDebugTest/1.0-SNAPSHOT/springDebugTest-1.0-SNAPSHOT.war!/WEB-INF/web.xml");
         //The location of your web.xml
         //root.setDescriptor(
         //        "jar:file:/home/jsimone/.m2/repository/com/force/sample/springDebugTest/1.0-SNAPSHOT/springDebugTest-1.0-SNAPSHOT.war!/WEB-INF/web.xml");
         //The location of your resource ("webapp") directory
         //root.setResourceBase("jar:file:/home/jsimone/.m2/repository/com/force/sample/springDebugTest/1.0-SNAPSHOT/springDebugTest-1.0-SNAPSHOT.war!/");
         
         //Want to do this with the setDescriptor and setResourceBase methods, but was having some issues setting that up. Jesper's jar approach would work around this issue.
        root.setWar(new JettyLauncher().getWarLocation().getPath());
         //Hack to get the taglibs onto the webapp classpath for now. Could also do this in the pom, but would just be a different version of the same hack
        root.setExtraClasspath(System.getProperty("user.home") + "/.m2/repository/javax/servlet/jstl/1.2/jstl-1.2.jar" + ",;" + System.getProperty("user.home") + "/.m2/repository/org/springframework/spring-webmvc/3.0.5.RELEASE/spring-webmvc-3.0.5.RELEASE.jar");
         //Your context root
         root.setContextPath("/");
         
         //root.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*/[^/]*\\.jar");
         
         //Parent loader priority is a class loader setting that Jetty accepts.
         //By default Jetty will behave like most web containers in that it will
         //allow your application to replace libraries even if they are part of the
         //container. Setting parent loader priority to true changes this behavior.
         //Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
         root.setParentLoaderPriority(true);
         
         server.setHandler(root);
  
         server.start();
         server.join();
     }
 
 }
