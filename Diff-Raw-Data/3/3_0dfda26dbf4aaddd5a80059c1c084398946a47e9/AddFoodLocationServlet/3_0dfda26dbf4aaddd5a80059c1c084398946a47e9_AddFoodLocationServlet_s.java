 package forage;
 
 import java.io.IOException;
 import java.util.List;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 
 public class AddFoodLocationServlet extends HttpServlet {
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		
 		//get list of food items of the kind received in req and return as list
 		String kind = req.getParameter("kind");
 	    String description = req.getParameter("description"); //Specific location description
 	    String name = req.getParameter("name");
 	    String lat = req.getParameter("lat");
 	    String lng = req.getParameter("long");
 	    String health = req.getParameter("health");
 	    
 	    //query datastore for entity of kind FoodItem with name kind. Should actually only return one entity.
 	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	    @SuppressWarnings("deprecation")
 		Query query = new Query("FoodItem").addFilter("name", Query.FilterOperator.EQUAL, kind);
 	    Entity item = datastore.prepare(query).asSingleEntity();
		Key parentKey = null;
		parentKey = item.getKey();
 	
 		// create itemLocation entity with selected foodItem as parentKey 
 		Entity itemLocation = new Entity("Location", parentKey);
 		itemLocation.setProperty("description", description);
 		itemLocation.setProperty("name", name);
 		itemLocation.setProperty("lat", lat);
 		itemLocation.setProperty("long", lng);
 		itemLocation.setProperty("health", health);
 		datastore.put(itemLocation);
 
 		// send back to jsp
 		resp.sendRedirect("/addfoodlocations.jsp");
 	}
 
 }
