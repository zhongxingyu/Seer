 package xinxat.server;
 
 
 /**
  * This class syncrhronizes the frontend database with the backend's
  * It is called every minute by a cron job
  * 
  * @author Fran Hermoso <franhp@franstelecom.com>
  */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 
 
 @SuppressWarnings("serial")
 public class UpdateDB extends HttpServlet {
 	
 	/**
 	 * Logger
 	 */
	private static final Logger log = Logger.getLogger(Server.class.getName());
 	
 	/**
 	 * Syncronizes the database from the frontend and the backend
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
     		throws IOException {		
 			try {
 				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 				//Ir a buscar usuarios
 				URL url = new URL("http://api.xinxat.com/?users");
 				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
 				String entrada = "";
 				String cadena = "";
 
 				while ((entrada = br.readLine()) != null){
 					cadena += entrada;
 				}
 
 				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 				DocumentBuilder db = dbf.newDocumentBuilder();
 
 				InputSource archivo = new InputSource();
 				archivo.setCharacterStream(new StringReader(cadena)); 
 
 				Document documento = db.parse(archivo);
 
 				NodeList nodeLista = documento.getElementsByTagName("user");
 				
 				for (int s = 0; s < nodeLista.getLength(); s++) {
 					Element element = (Element) nodeLista.item(s);
 					String name = element.getAttribute("nickname");
 					String code = element.getAttribute("code");
 					
 					Query q = new Query("user");
 					q.addFilter("username", FilterOperator.EQUAL, name);
 					PreparedQuery pq = datastore.prepare(q);
 					
 					//If the user exists, just update the password
 					if(pq.countEntities(FetchOptions.Builder.withDefaults()) >= 1){
 						for (Entity result : pq.asIterable()){
 							if(!result.getProperty("password").equals(code)){
 								result.setProperty("password", code);
 								datastore.put(result);
 							}
 						}
 					}
 					//If the user doesn't exist, create a new one
 					else {
 						Entity user = new Entity("user");
 						user.setProperty("username", name);
 						user.setProperty("password", code);
 						long now = (long)(System.currentTimeMillis() / 1000L);
 						user.setProperty("lastonline", now);
 						datastore.put(user);
 					}
 
 				}
 				
 		  }
 		  catch (Exception e) {
 		    	e.printStackTrace();
 		  }
 				
 	}
 
 
 	/**
 	 * This method is able to delete all the users from the datastore
 	 * or a single user depending on the parameters:
 	 * 		POST all=true
 	 * 			deletes all the users
 	 * 		POST delete={username}
 	 * 			deletes {username} from the datastore
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
     		throws IOException {	
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		
 		if("true".equals(req.getParameter("all"))){
 			Query q = new Query("user");
 			PreparedQuery pq = datastore.prepare(q);
 			for (Entity result : pq.asIterable())
 				datastore.delete(result.getKey());
 			resp.getWriter().println("Everything is gone!");
 			log.warning("Someone just deleted all the users in the database");
 		} else {
 			Query q = new Query("user");
 			q.addFilter("username", FilterOperator.EQUAL, req.getParameter("delete"));
 			PreparedQuery pq = datastore.prepare(q);
 			for (Entity result : pq.asIterable())
 				datastore.delete(result.getKey());
 			resp.getWriter().println(req.getParameter("delete") + " is gone!");
 			log.warning("Someone just deleted " + req.getParameter("delete") + " from the database");
 		}
 		
 	}
 
 }
