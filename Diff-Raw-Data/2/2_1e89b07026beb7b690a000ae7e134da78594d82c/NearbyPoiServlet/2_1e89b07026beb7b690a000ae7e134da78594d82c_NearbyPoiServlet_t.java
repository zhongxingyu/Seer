 package com.nearme;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.sql.SQLException;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 /**
  * This servlet responds to HTTP GET requests of the form
  * 
  * http://server/nearme/pois/LAT/LONG/RADIUS
  * 
  * where
  * 
  * LAT is a latitude
  * LONG is a longitude
  * RADIUS is a radius (in metres)
  * 
  * and returns a JSON data structure with a list of Points of Interest
  * 
  * @author twhume
  *
  */
 
 public class NearbyPoiServlet extends GenericNearMeServlet {
 
 	private static final long serialVersionUID = 4851880984536596503L; // Having this stops Eclipse moaning at us
 
 	private static Logger logger = Logger.getLogger(NearbyPoiServlet.class);
 	
 	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
 		res.setContentType("application/json");
 		PoiQuery pq = new PoiQuery(req.getPathInfo());
 
 		try {
 			PoiFinder pf = new DatabasePoiFinder(datasource);
 			UserDAO uf = new UserDAOImpl(datasource);
 	
 			/* Get a list of all nearby points of interest and add in nearby friends */
 			
 			User u = uf.read(1); //TODO fix grotty hardcoding, take device-ID from URL
 			List<Poi> points = pf.find(pq);
 			logger.info("found " + points.size() + " POIs for user 1 within " + pq.getRadius() + " of (" + pq.getLatitude() + "," + pq.getLongitude() + ")");
 			List<Poi> friends = uf.getNearestUsers(u, pq.getRadius());
			logger.info("found " + friends.size() + " friends for user 1 within " + pq.getRadius() + " of (" + pq.getLatitude() + "," + pq.getLongitude() + ")");
 			points.addAll(friends);
 			
 			/* Use GSon to serialise this list onto a JSON structure, and send it to the client.
 			 * This is a little bit complicated because we're asking it to serialise a list of stuff;
 			 * see the manual at https://sites.google.com/site/gson/gson-user-guide#TOC-Collections-Examples
 			 * if you /really/ want to find out why...
 			 */
 			
 			Gson gson = new Gson();
 			Type listOfPois = (Type) (new TypeToken<List<Poi>>(){}).getType();
 			res.getOutputStream().print(gson.toJson(points, listOfPois));
 			logger.debug("delivered POIs OK");
 		} catch (SQLException e) {
 			logger.error(e);
 			e.printStackTrace();
 			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 		
 }
