 package com.web.achievetimeline.server;
 
 import java.io.IOException;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gson.Gson;
 
 @SuppressWarnings("serial")
 public class ApplicationsService extends HttpServlet {
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 				
 		String userId = GetUserId();
         
         String institutionName = req.getParameter("institutionName");
         String programName = req.getParameter("programName");
         String colorCode = req.getParameter("colorCode");
         String key = req.getParameter("key");
         
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         Entity entity;
         
         if(null != key)
         {
         	// We are updating an existing entity.
         	entity = new Entity(KeyFactory.stringToKey(key));
         }
         else
         {
         	// Create an entity to store event properties.
         	entity = new Entity("Application");
         }
         
         entity.setProperty("institutionName", institutionName);
         entity.setProperty("programName", programName);
         entity.setProperty("colorCode", colorCode);
         entity.setProperty("userId", userId);
         
 		// Put the entity in the data store.
 		datastore.put(entity);
 		
 		System.out.println("entity persisted: "+KeyFactory.keyToString(entity.getKey()));
 		
 		// Notify the client of success.
 		resp.setContentType("application/json");
 		
 		Gson gson = new Gson();
 		String json = gson.toJson(entity);
 		resp.getWriter().println(json);
 	}
         
     private String GetUserId() {
     	UserService userService = UserServiceFactory.getUserService();
         User user = userService.getCurrentUser();
         return user.getUserId();    
     }
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 				
         // Grab all applications belonging to the user from the datastore.
         Filter f = new FilterPredicate("userId", FilterOperator.EQUAL, GetUserId());
         Query q = new Query("Application").setFilter(f).addSort("institutionName", SortDirection.DESCENDING);;
 		
 		PreparedQuery pq = datastore.prepare(q);
 		System.out.println("ApplicationsService::doGet - Number of applications: " + pq.countEntities());
 		
 		List<Entity> applications = new ArrayList<Entity>();
 		
 		for (Entity result : pq.asIterable()) {
 			result.setProperty("key", KeyFactory.keyToString(result.getKey()));
 			applications.add(result);	
 		}
 						
 		Gson gson = new Gson();
 		String applicationsJson = gson.toJson(applications);
 		
 		try {
 			resp.setContentType("application/json");
 			resp.getWriter().println(applicationsJson);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
 		
 		String userId = GetUserId();
         
         String institutionName = req.getParameter("institutionName");
         String programName = req.getParameter("programName");
         String keyStr = req.getParameter("key");
         
         System.out.print("ApplicationsService::doDelete(");
         System.out.print("institutionName=" + institutionName + ", programName=" + programName + ", key=" + keyStr + ")");
         
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         
         // Grab all applications belonging to the user from the datastore.
         Filter f = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
         Query q = new Query("Application").setFilter(f);
 		
 		PreparedQuery pq = datastore.prepare(q);
         
 		if(null != keyStr)
         {
 			Key key = KeyFactory.stringToKey(keyStr);
 			for (Entity result : pq.asIterable()) {
 				if(key == result.getKey())
 				{
 					System.out.println("Deleting application " + keyStr);
 					datastore.delete(key);
 				}
 			}
         }
         else
         {
 	     	for (Entity result : pq.asIterable()) {
 				System.out.println("Result.institutionName=" + result.getProperty("institutionName"));
 				System.out.println("Result.programName=" + result.getProperty("programName"));
 				
 				if(institutionName == result.getProperty("institutionName") &&
 				   programName == result.getProperty("programName"))
 				{
 					System.out.println("Deleting key " + KeyFactory.keyToString(result.getKey()));
 					datastore.delete(result.getKey());
 				}
 			}
         }
 	}
 }
