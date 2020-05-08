 package com.develogical;
 
 import com.develogical.web.ApiResponse;
 import com.develogical.web.IndexPage;
 import com.develogical.web.ResultsPage;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class WebServer {
 
     static class Website extends HttpServlet {
         @Override
         protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
             if (req.getParameter("q") == null) {
                 new IndexPage().writeTo(resp);
             } else {
                 new ResultsPage(req.getParameter("q")).writeTo(resp);
             }
         }
     }
 
     static class Api extends HttpServlet {
         @Override
         protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
             new ApiResponse(req.getParameter("q")).writeTo(resp);
         }
     }
 
     public static void main(String[] args) throws Exception {
        Server server = new Server(Integer.valueOf(System.getenv("	)));
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         server.setHandler(context);
         context.addServlet(new ServletHolder(new Api()), "/api/*");
         context.addServlet(new ServletHolder(new Website()), "/*");
         server.start();
         server.join();
     }
 }
 
