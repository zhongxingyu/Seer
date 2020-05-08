 package com.google.sites.gdgshikoku.scaas;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 @SuppressWarnings("serial")
 public class SCaaSServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		Query query = new Query("SCaaS");
 		query.addSort("createdAt", SortDirection.DESCENDING);
 
 		DatastoreService datastoreService = DatastoreServiceFactory
 				.getDatastoreService();
 		Iterable<Entity> list = datastoreService.prepare(query).asIterable();
 
 		List<LinkedHashMap<String, String>> json = new ArrayList<LinkedHashMap<String, String>>();
 
 		for (Entity entity : list) {
 
 			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			// TODO: 
			map.put("key", (String) entity.getProperty("Key") );
 			map.put("title", (String) entity.getProperty("title"));
 			map.put("article", (String) entity.getProperty("article"));
 			json.add(map);
 
 		}
 
 		ObjectMapper mapper = new ObjectMapper();
 		String jsonString = null;
 
 		jsonString = mapper.writeValueAsString(json);
 
 		resp.setCharacterEncoding("utf-8");
 		resp.setContentType("application/json");
 		resp.getWriter().print(jsonString);
 
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String status = req.getParameter("status");
 
 		if ("create".equals(status)) {
 
 			String title = req.getParameter("title");
 			String article = req.getParameter("article");
 			Date createdAt = new Date();
 
 			Entity entity = new Entity("SCaaS");
 			entity.setProperty("title", title);
 			entity.setProperty("article", article);
 			entity.setProperty("createdAt", createdAt);
 
 			DatastoreService datastoreService = DatastoreServiceFactory
 					.getDatastoreService();
 			datastoreService.put(entity);
 
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
 		}
 	}
 }
