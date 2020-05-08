 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.*;
 import java.util.*;
 
 
 
 public class HelloWorld extends HttpServlet {
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
 		String title = "Heroku on Java: JVM properties";
 		resp.getWriter().print("<html>");
 		resp.getWriter().print("<head>");
 		resp.getWriter().print("<title>"+title+"</title><body>");
 		resp.getWriter().print("<style type=\"text/css\">"
 			+"body { color: #FF6C00; }"
 			+"#tquilalink { position: absolute; top: 10px; right: 10px; }"
 			+"#linkme { color: #FF6C00; }"
 			+"</style>");
 		resp.getWriter().print("</head>");
 		resp.getWriter().print("<body>");
 		
         resp.getWriter().print("<h1>"+title+"</h1>");
 		resp.getWriter().print("<h4><a class=\"linkme\" href=\"mailto:nicola@tquila.com\">Write us for any comment or request!</a></h4>");
 		
		resp.getWriter().print("<a id=\"tquilalink\" href=\"www.tquila.com\" target=\"_blank\"><img id=\"tquilaimg\" src=\"http://www.tquila.com/tquila-logo.png\"/></a>");
 		
 		resp.getWriter().print("<br/><table>");
 		Properties properties = System.getProperties();
 		for(String parameterName : Collections.list((Enumeration<String>)properties.propertyNames())) {
 			resp.getWriter().print("<tr><td>"+parameterName+"</td><td>"+properties.getProperty(parameterName)+"</td></tr>");
 		}
 		resp.getWriter().print("</table>");
 		resp.getWriter().print("</body>");
 		resp.getWriter().print("</html>");
 		
         resp.getWriter().print("");
     }
 
     public static void main(String[] args) throws Exception{
         Server server = new Server(Integer.valueOf(System.getenv("PORT")));
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         server.setHandler(context);
         context.addServlet(new ServletHolder(new HelloWorld()),"/*");
         server.start();
         server.join();   
     }
 }
