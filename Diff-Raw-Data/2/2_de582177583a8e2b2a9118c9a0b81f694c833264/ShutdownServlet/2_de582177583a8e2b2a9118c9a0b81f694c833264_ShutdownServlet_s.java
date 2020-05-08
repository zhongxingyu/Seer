 package fedora.server.management;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ShutdownException;
 import fedora.server.Server;
 
 public class ShutdownServlet 
         extends HttpServlet {
         
     private static Server s_server;
 
     static {
         try {
             s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
         } catch (InitializationException ie) {
             System.err.println(ie.getMessage());
         }
     }
 
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         PrintWriter out = response.getWriter();
         response.setContentType("text/html");
         out.write("<html><body>");
         System.out.println("ShutdownServlet: Got shutdown request.");
         if (request.getParameter("password")!=null 
                 && request.getParameter("password").equals(
                s_server.getParameter("shutdownPassword")) {
             System.out.println("Password correct...shutting down.");
             try {
                 s_server.shutdown();
             } catch (Exception e) {
                 System.out.println("Error shutting down: " + e.getMessage());
             }
         } else {
             System.out.println("Correct password not given...ignoring request");
         }
         out.write("</body></html>");
     }
     
     public void init() {
     }
     
     public void destroy() {
     }
 }
