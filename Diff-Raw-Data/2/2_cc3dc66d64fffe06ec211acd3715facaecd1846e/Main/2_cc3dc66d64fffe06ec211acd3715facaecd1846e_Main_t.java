 package ru.lighttms.tms;
 
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: azee
  * Date: 7/12/12
  * Time: 7:07 PM
  */
 public class Main {
     public static void main(String[] args) throws Exception {
         Server server = new Server(9001);
         Context root = new Context(server, "/", Context.SESSIONS);
        root.addServlet(new ServletHolder(new ServletContainer(new PackagesResourceConfig("ru.lighttms.tms.api"))), "/tms-api/*");
         server.start();
     }
 
 }
