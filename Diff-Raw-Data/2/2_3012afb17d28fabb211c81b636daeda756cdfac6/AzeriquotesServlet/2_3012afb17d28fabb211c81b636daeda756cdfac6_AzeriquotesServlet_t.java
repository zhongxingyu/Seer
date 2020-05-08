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
 		int isUser = 0;
 		UserService userService = UserServiceFactory.getUserService();
 		//String thisURL = req.getRequestURI();
         if (req.getUserPrincipal() != null) {
             resp.getWriter().println("<p>Hello, " +
                                          req.getUserPrincipal().getName() +
                                          "!  You can <a href=\"" +
                                          userService.createLogoutURL("/") +
                                          "\">sign out</a>.</p>");
             isUser = 1;
             DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     		Query q = new Query("Post");
     		PreparedQuery pq = datastore.prepare(q);
 
     		resp.setContentType("text/html ");
     		resp.getWriter().println("<h1>Aforizmler</h1>");
 
     		for (Entity post : pq.asIterable()) {
     		  String author = (String) post.getProperty("author");
     		  String text = (String) post.getProperty("text");
     		  String username = (String) post.getProperty("username");
    		  resp.getWriter().println("<p>"+ text + "<br> <b><u>"+author+"</b></u><br>added by <i>" +username+ "</i> </p>");
     		}
         }
     		
     		 else {
     	            resp.getWriter().println("<p>Aforizmleri gormek u4un <a href=\"" +
     	                                         userService.createLoginURL("/") +
     	                                         "\">sisteme daxil olun</a>.</p>");
     	            isUser = 0;
     		 }
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Entity post = new Entity("Post");
 		
 		post.setProperty("username",  req.getUserPrincipal().getName());
 		post.setProperty("author", req.getParameter("author"));
 		post.setProperty("text", req.getParameter("text"));
 
 		datastore.put(post);
 
 		resp.sendRedirect("/");
 
 	}
 }
