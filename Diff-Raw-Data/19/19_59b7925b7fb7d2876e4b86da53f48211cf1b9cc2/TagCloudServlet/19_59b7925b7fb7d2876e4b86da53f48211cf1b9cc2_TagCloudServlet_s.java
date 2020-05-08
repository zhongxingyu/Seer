 package edu.tuhh.tagcloud.servlet;
 
 import edu.tuhh.tagcloud.cloud.TagCloud;
 import org.mcavallo.opencloud.Cloud;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author avelinsk
  */
 public class TagCloudServlet extends HttpServlet {
 
   public void doGet(HttpServletRequest requeset, HttpServletResponse response)
           throws IOException, ServletException {
   }
 
   public void doPost(HttpServletRequest request, HttpServletResponse response)
           throws IOException, ServletException {
 
     String command = request.getParameter("command");
     String text = request.getParameter("text");
 
    Cloud cloud = TagCloud.getTagCloud(command, text);
 
     request.setAttribute("tagcloud", cloud);
    RequestDispatcher view = request.getRequestDispatcher("index.jsp");
     view.forward(request, response);
   }
 }
