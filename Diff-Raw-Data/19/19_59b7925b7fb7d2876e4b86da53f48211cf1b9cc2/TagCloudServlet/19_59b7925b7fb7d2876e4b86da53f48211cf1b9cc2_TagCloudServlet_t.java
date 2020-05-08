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
 
    response.setContentType("text/html");

     String command = request.getParameter("command");
     String text = request.getParameter("text");
 
    this.log("parameters command and text passed. command: "+command);
    TagCloud tc = new TagCloud();
    Cloud cloud = tc.getTagCloud(command, text);
    this.log("Tags are : "+cloud.allTags().size());
 
     request.setAttribute("tagcloud", cloud);
    request.setAttribute("text", text);
    RequestDispatcher view = request.getRequestDispatcher("tagcloud.jsp");
     view.forward(request, response);
   }
 }
