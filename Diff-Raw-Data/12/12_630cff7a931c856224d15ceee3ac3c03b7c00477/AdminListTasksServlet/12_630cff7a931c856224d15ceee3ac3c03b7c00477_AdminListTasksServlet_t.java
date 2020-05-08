 /**
  * 
  */
 package org.humanizer.rating;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.datanucleus.store.types.sco.backed.List;
 import org.humanizer.rating.objects.Items;
 import org.humanizer.rating.objects.Project;
 import org.humanizer.rating.objects.Tasks;
 import org.humanizer.rating.objects.TasksByRater;
 import org.humanizer.rating.utils.HTTPClient;
 
 import com.google.gson.Gson;
 
 /**
  * @author sonhv
  *
  * Showing results for a keyword rating
  */
 @SuppressWarnings("serial")
 public class AdminListTasksServlet extends HttpServlet {
   //private static final Logger log = Logger.getLogger(AuthenServlet.class.getName());
   /**
    * @author sonhv
    * 
    * GET handling
    * Redirect to POST 
    */  
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
     doPost(req, resp);
   }
   
   /**
    * @author sonhv
    * 
    * POST handling
    * Listing details for rate 
    */
   public void doPost(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
 	  HttpSession sess = req.getSession(true);
 	  String username = (String) sess.getAttribute("adminuser");	
 		if (username == null){
 			resp.sendRedirect("/admin_login.jsp");
 			return;
 		}  
 	  
 	  String projectId = req.getParameter("project");
 	  String project_name = req.getParameter("project_name");
 	  String rater_count = req.getParameter("raters");
 	  
 	  //1. Get tasks list from this project
 	  String sURL = "http://humanizer.iriscouch.com/tasks/_design/api/_view/tasks_by_project?startkey=%22" + projectId + "%22&endkey=%22" + projectId + "%22";	  
 	  String sResult = HTTPClient.request(sURL);
	  System.out.println(sResult);
 	  Tasks prj = new Tasks();
 	  prj.initTasksList(sResult);  
 	  
 	    
 	  req.setAttribute("data",prj.getData());
 	  req.setAttribute("project", projectId);
 	  req.setAttribute("project_name", project_name);
 	  req.setAttribute("raters", rater_count);
 	  
 	  RequestDispatcher dispatcher = req.getRequestDispatcher("/admin_list_tasks.jsp");
 
	  resp.setCharacterEncoding("UTF-8");
	  resp.setContentType("text/html; charset=UTF-8");	  

 	  if (dispatcher != null){
 	    try {
 	      dispatcher.forward(req, resp);
 	    } catch (ServletException e) {
 	      // TODO Auto-generated catch block
 	      e.printStackTrace();
 	    }
 	  } 
 
   }  
 }
