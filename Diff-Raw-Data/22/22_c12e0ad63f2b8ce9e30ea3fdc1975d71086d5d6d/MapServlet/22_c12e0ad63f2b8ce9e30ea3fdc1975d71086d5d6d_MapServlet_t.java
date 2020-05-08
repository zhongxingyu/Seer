 package forage;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

 @SuppressWarnings("serial")
 public class MapServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String maps = "";
 		try{
 			resp.setContentType("text/html");
 			ServletContext  s= this.getServletContext();
 			String path = s.getRealPath("mapcode.txt");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				new FileInputStream(path), "UTF8"));
		req.getAttribute("type");
		/*Query itemQuery = new Query("FoodItem")
		.setAncestor(pKeys.get(0))
		.addFilter(Entity.KEY_RESERVED_PROPERTY,
				Query.FilterOperator.GREATER_THAN, pKeys.get(0));*/
 		maps = reader.readLine();
 		//get locations here format placeMarker(lon, lat, title, info) (dont change info) Also title is formatted as html
 		maps += reader.readLine();
		
 		resp.getWriter().println(maps);
 		reader.close();
 		}catch(IOException e){
 			resp.setContentType("text/html");
 			resp.getWriter().println(
 					"<!DOCTYPE html><html><body>Error Reading Text</body></html>");
 		}
 	}
 }
