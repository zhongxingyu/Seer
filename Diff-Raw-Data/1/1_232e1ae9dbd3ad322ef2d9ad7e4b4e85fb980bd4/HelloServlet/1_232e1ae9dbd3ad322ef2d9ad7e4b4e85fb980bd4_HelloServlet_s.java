 package org.inuua.executable_war.sample;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class HelloServlet extends HttpServlet {
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet HelloServlet</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Hello World!</h1>");
             out.println("</body>");
             out.println("</html>");
         } finally {
             out.close();
         }

     }
 }
