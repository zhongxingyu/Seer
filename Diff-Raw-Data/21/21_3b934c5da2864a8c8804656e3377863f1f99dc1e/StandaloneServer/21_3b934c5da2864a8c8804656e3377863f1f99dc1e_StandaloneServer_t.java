 package introsde.crud.rest.resources;
 
 import java.io.IOException;
 import java.net.InetAddress;
 
 import javax.ws.rs.Path;
 
 import com.sun.jersey.api.container.httpserver.HttpServerFactory;
 
 /**
  * http://stackoverflow.com/questions/9579970/can-not-use-the-com-sun-net-httpserver-httpserver
  * Problem:
  * "Access restriction: The type HttpServer is not accessible due to restriction 
  * on required library C:\jdk1.7.0\jre\lib\rt.jar"
  * 
  * Answer: 
  * "You shouldn't use Sun's internal packages since they can be not present in other VM's. 
  * If you're sure you won't use other VM then Sun's/Oracle's, you can disable this warning in 
  * Project properties 
  * -> Java Compiler 
  * -> Errors/Warnings 
  * -> Enable Project Specific settings
  * -> Deprecated and restricted API
  * -> Forbidden reference (access rules): -> change to warning"
  * 
  * Other options to solve the problem: 
  * - download httpserver from the url below and manually add it to the classpath 
  * http://download.java.net/maven/2/com/sun/net/httpserver/http/20070405/http-20070405.jar
  * 
  * - use a non-sun httpserver like jetty: 
  * http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty#Creating_a_Server
  */
 import com.sun.net.httpserver.HttpServer;
 
 // the whole resource will be available at baseUrl/rest
@SuppressWarnings("restriction")
 @Path("/rest")
 public class StandaloneServer
 {
     public static void main(String[] args) throws IllegalArgumentException, IOException
     {
     	String protocol = "http://";
         String port = ":5900/";
         String hostname = InetAddress.getLocalHost().getHostAddress();
        if (hostname.equals("127.0.0.1")) {
             hostname = "localhost";
         }
         String baseUrl = protocol + hostname + port;
         final HttpServer server = HttpServerFactory.create(baseUrl);
         server.start();
        System.out.println("Server is listening on: " + baseUrl + "\n [kill the process to exit]");
     }
 }
