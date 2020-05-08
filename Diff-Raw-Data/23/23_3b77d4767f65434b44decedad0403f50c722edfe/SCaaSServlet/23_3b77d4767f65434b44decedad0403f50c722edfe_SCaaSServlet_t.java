 package com.google.sites.gdgshikoku.scaas;
 import java.io.IOException;
import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.SortDirection;
//import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectMapper;
 
 @SuppressWarnings("serial")
 public class SCaaSServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		Query query = new Query("SCaaS");
 		query.addSort("createdAt", SortDirection.DESCENDING);
 		
 		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
 		Iterable<Entity> list = datastoreService.prepare(query).asIterable();
 		
 		for(Entity entity:list){
 			
 		}
		
	//	ObjectMapper mapper = new ObjectMapper();
		
		PrintWriter out = resp.getWriter();
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		String title = req.getParameter("title");
 		String article = req.getParameter("article");
 		Date createdAt = new Date();
 		
 		Entity entity = new Entity("SCaaS");
 		entity.setProperty("title", title);
 		entity.setProperty("article", article);
 		entity.setProperty("createdAt", createdAt);
 		
 		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
 		datastoreService.put(entity);
 		
 		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
 		
 	}
 }
