 package com.aracssoftware.azeriquotes;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 
 @SuppressWarnings("serial")
 public class AzeriquotesServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		//String thisURL = req.getRequestURI();
         if (req.getUserPrincipal() != null) {
             resp.getWriter().println("<div class=\"alert\">" +
 			 		"<strong>Salam, " +
                                          req.getUserPrincipal().getName() +
                                          "!  Sistemden chixmaq u4un <a href=\"" +
                                          userService.createLogoutURL("/") +
                                          "\">buraya klikleyin</a>." +
 			 		"</div>");
             DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     		Query q = new Query("Post");
     		PreparedQuery pq = datastore.prepare(q);
 
     		resp.setContentType("text/html ");
     		resp.getWriter().println("<h1>Aforizmler</h1>");
 
     		for (Entity post : pq.asIterable()) {
     		  String author = (String) post.getProperty("author");
     		  String text = (String) post.getProperty("text");
     		  String username = (String) post.getProperty("username");
     		  resp.getWriter().println("<p>"+ text + "<br> <b><u>"+author+"</b></u><br>elave etdi: <i>" +username+ "</i> </p>");
     		}
    		
    		resp.getWriter().println("<form id=\"post-form\" action=\"/azeriquotes\" method=\"post\">" +
    				"<label for=\"text\">Aforizm:</label> <br>" +
    				"<textarea name=\"text\" id=\"text\"></textarea>" +
    				"<br>" +
    				"<label for=\"tag\">teqler</label>" +
    				"<input id=\"tag\" name=\"tag\">" +
    				"<br>" +
    				"<label for=\"author\">Aforizmin müəllifi</label>" +
    				"<input id=\"author\" name=\"author\">" +
    				"<br>" +
    				" <input type=\"submit\">" +
    				"</form>");
         }
     		
     		 else {
     			 resp.getWriter().println("<div class=\"alert\">" +
     			 		"<strong>Aforizmleri gormek u4un <a href=\"" +
     	                                         userService.createLoginURL("/") +
     	                                         "\">sisteme daxil olun</a>." +
     			 		"</div>");
     		 }
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Entity post = new Entity("Post");
 
 		post.setProperty("username",  req.getUserPrincipal().getName());
 		post.setProperty("author", req.getParameter("author"));
 		post.setProperty("text", req.getParameter("text"));
 		post.setProperty("tag", req.getParameter("tag"));
 
 		datastore.put(post);
 
 		resp.sendRedirect("/");
 
 	}
 }
