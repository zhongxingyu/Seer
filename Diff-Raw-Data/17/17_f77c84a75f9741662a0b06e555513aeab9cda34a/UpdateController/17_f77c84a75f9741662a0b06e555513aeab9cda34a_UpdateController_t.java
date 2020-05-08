 package tgseminar.controller;
 
 import java.util.Date;
import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 import org.slim3.datastore.Datastore;
 import org.slim3.datastore.EntityNotFoundRuntimeException;
import org.slim3.memcache.Memcache;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.Expiration;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class UpdateController extends Controller {
 
 	@Override
 	protected Navigation run() throws Exception {
 		// TODO Auto-generated method stub
 		String idString = request.getParameter("id");
 		String title = request.getParameter("title");
 		
 		System.out.println("id:" + idString);
 		System.out.println("title:" + title);
 		
 		// id not specified then return 400.
 		if(idString == null || "".equals(idString)){
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			return null;
 		}
 		
 		int id = 0;
 		
 		// id is not numeric then return 400.
 		try{
 			id = Integer.parseInt(idString);
 		}catch(NumberFormatException ex){
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			return null;
 		}
 
 		// title not specified then return 400.
 		if(title == null || "".equals(title)){
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			return null;
 		}
 
 
 		// id not exists then return 404.
 		Key key = Datastore.createKey("ToDo", id);
 
 		Entity entity = Datastore.getOrNull(key);
 		if(entity == null){
 			System.out.println("check key:" + id);
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;			
 		}
 /*
 		try{
 			entity = Datastore.get(key);			
 		}catch(EntityNotFoundRuntimeException ex){
 			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
 			return null;						
 		}
 */
 		// another user
 		User currentUser
 			= UserServiceFactory.getUserService().getCurrentUser();
 		if(!entity.getProperty("createdBy").equals(currentUser.getEmail())){
 			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
 			return null;						
 		}
 
 //		entity.setProperty("createdAt", new Date());
 //		entity.setProperty("createdBy", currentUser.getEmail());
 		entity.setProperty("title", title);
 		Datastore.put(entity);
 
		List<Entity> entities
			= Datastore.query("ToDo")
				.filter("createdBy", FilterOperator.EQUAL, entity.getProperty("createdBy"))
				.sort("createdAt", SortDirection.DESCENDING)
				.asList();

		// Put entities to Memcache.
		// Memcache expire 60sec.
		Memcache.put(entity.getProperty("createdBy"), entities, 
			Expiration.byDeltaSeconds(60));

 		response.setStatus(HttpServletResponse.SC_OK);
 		return null;
 	}
 
 }
