 package no.lau.servo;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class SimpleJettyServer extends AbstractHandler {
     public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         response.setContentType("text/html;charset=utf-8");
         response.setStatus(HttpServletResponse.SC_OK);
         baseRequest.setHandled(true);
         response.getWriter().println("<h1>Hello Serv-O!</h1>");
         response.getWriter().println("<body>It's alive !! " + request.toString() + "</body>");
     }
 
     public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
         server.setHandler(new SimpleJettyServer());
         server.start();
         server.join();
     }
 }
