 package com.cs1530.group4.addendum.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.cs1530.group4.addendum.shared.User;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.appengine.api.utils.SystemProperty;
 import com.google.gson.Gson;
 
 @SuppressWarnings("serial")
 public class LoginServlet extends HttpServlet
 {
 	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		performActions(req, resp);
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		performActions(req, resp);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void performActions(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		String username = req.getParameter("username");
 		String password = req.getParameter("password");
 		Entity userEntity = getUserEntity(username);
 		User user = null;
 
 		if(userEntity != null)
 		{
 			//only test for valid email in production because the dev server doesn't handle email properly
 			if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
 			{
				if(userEntity.hasProperty("emailValid") && (Boolean)userEntity.getProperty("emailValid"))
 					user = null;
 			}
 			else if(userEntity.hasProperty("password"))
 			{
 				if(userEntity.getProperty("password").toString().equals(password))
 				{
 					user = new User(username);
 					if(userEntity.hasProperty("courseList"))
 						user.setCourseList(((ArrayList<String>)userEntity.getProperty("courseList")));
 					else
 						user.setCourseList(new ArrayList<String>());
 				}
 			}
 		}
 				
 		resp.setContentType("application/json");
 		Gson gson = new Gson();
 		String json = gson.toJson(user);
 		resp.getWriter().print(json);
 	}
 
 	private Entity getUserEntity(String username)
 	{
 		Entity user = null;
 		if(memcache.contains("user_" + username))
 			user = ((Entity) memcache.get("user_" + username));
 		else
 		{
 			try
 			{
 				user = datastore.get(KeyFactory.createKey("User", username));
 			}
 			catch(EntityNotFoundException ex)
 			{
 				ex.printStackTrace();
 			}
 		}
 		return user;
 	}
 }
