 package xinxat.server;
 
 /**
  * This class returns a list of all the users and their presence status.
  * 
  * @author Fran Hermoso <franhp@franstelecom.com>
  */
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 
 @SuppressWarnings("serial")
 public class Roster extends HttpServlet {
 	
 	/**
 	 * This variable defines the time after which the user is considered offline
 	 */
	private int time2BeOffline = 90;
 
 	/**
 	 * Datastore connection
 	 */
 	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	
 	/**
 	 * Returns the presence of every user when sending a request to:
 	 * 		http://projecte-xinxat.appspot.com/roster
 	 * 			GET user={username} : gets the presence of an specific user
 	 * 			GET room={roomname} : gets the presence for all the users in the room
 	 * 			GET : will output all the presences
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
     		throws IOException {
 		
 		String user = req.getParameter("user");
 		String room = req.getParameter("room");
 
 		Query q = new Query("user");
 		//Roster of specific user
 		if(user != null) 
 			q.addFilter("username", FilterOperator.EQUAL, req.getParameter("user"));
 		//Roster of room
 		else if (room != null){
 			List<String> users = xinxat.server.Server.getUsersFromRoom(req.getParameter("room"));
 			if(users != null && !users.isEmpty())
 				q.addFilter("username", FilterOperator.IN, users);
 		}
 		PreparedQuery pq = datastore.prepare(q);
 		try {
 			resp.getWriter().println("<presences>\n");
 			for (Entity result : pq.asIterable()) {
 				long comp = 0;
 					try{
 						//If the user hasn't refreshed the lastonline key on the datastore for a long time ...
 						long lastonline = Long.parseLong(result.getProperty("lastonline").toString());
 						long now = (long)(System.currentTimeMillis() / 1000L);
 						comp = now - lastonline;
 					} catch (NumberFormatException e) {
 						resp.getWriter().println("");
 					}
 					//The user is considered to be offline
 					if(comp > time2BeOffline)
 						result.setProperty("show", "offline");
 					
 					resp.getWriter().println("<presence from=\""+ result.getProperty("username") + "\">" +
 												"\n\t<show>" +result.getProperty("show") + "</show>" + 
 												"\n\t<status>" +result.getProperty("status") +"</status>" + 
 										"\n</presence>");
 			}
 			resp.getWriter().println("\n</presences>");
 		}
 		catch (NullPointerException e){
 			resp.getWriter().println("Reload");
 		}
 	}
 	
     
 
 }
 
 
