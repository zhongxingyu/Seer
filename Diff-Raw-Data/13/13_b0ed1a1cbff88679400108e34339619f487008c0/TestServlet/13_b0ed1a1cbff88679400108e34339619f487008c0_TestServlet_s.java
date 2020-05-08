 package ai.svl;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class TestServlet extends HttpServlet
 {
     private static final long serialVersionUID = -5860886627046557680L;
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
     {
         String name = request.getParameter("name");
         try
         {
             response.setStatus(HttpServletResponse.SC_OK);
             PrintWriter writer = response.getWriter();
             if (name == null || name.length() < 1)
                 writer.write("<h1>Welcome to Hell</h1>");
             else
                 writer.write("<h1>" + name + " welcome to Hell</h1>");
             writer.flush();
             writer.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 }
