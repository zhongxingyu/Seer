 package forage;
 
 import java.io.File;
 import java.io.IOException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.File;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
  
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Document;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.oauth.OAuthRequestException;
 import com.google.appengine.api.oauth.OAuthService;
 import com.google.appengine.api.oauth.OAuthServiceFactory;
 import com.google.appengine.api.users.User;
 
 /**
  * This servlet serves Get requests from client apps for newly added locations. Returns XML
  * @author M Hudson
  *
  */
 public class GetTypes extends HttpServlet{
 
 	private static final long serialVersionUID = 2550724501785758817L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		//OAUTH check
 		User user = null;
 		try {
 		    OAuthService oauth = OAuthServiceFactory.getOAuthService();
 		    //checks for user with current authorisation
		    user = oauth.getCurrentUser();
 		    resp.getWriter().println("Authenticated: " + user.getEmail());
 		} catch (OAuthRequestException e) {
 		    resp.getWriter().println("Not authenticated: " + e.getMessage());
 		    return;
 		}
 		
 		//create key to run ancestor query on food types
 		String food = "food";
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		Key foodKey = KeyFactory.createKey("Food", food);
 
 		// Run an ancestor query to get food types
 		Query typeQuery = new Query("FoodType", foodKey);
 		List<Entity> types = datastore.prepare(typeQuery).asList(
 				FetchOptions.Builder.withLimit(15));
 
 		 try {
 				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 		 
 				// root elements
 				Document doc = docBuilder.newDocument();
 				Element rootElement = doc.createElement("food");
 				doc.appendChild(rootElement);
 				
 				for(Entity t: types){
 					
 					Element type = doc.createElement("type");
 					rootElement.appendChild(type);
 					
 					Element name = doc.createElement("name");
 					String typeName = (String) t.getProperty("type");
 					name.appendChild(doc.createTextNode(typeName));
 					type.appendChild(name);
 				}
 				
 				// write the content into xml file
 				TransformerFactory transformerFactory = TransformerFactory.newInstance();
 				Transformer transformer = transformerFactory.newTransformer();
 				DOMSource source = new DOMSource(doc);
 				//StreamResult result = new StreamResult(new File("C:\\file.xml"));
 				
 				// Output xml as http response
 				resp.setContentType("text/xml;charset=UTF-8");
 				StreamResult result = new StreamResult(resp.getOutputStream());
 		 
 				transformer.transform(source, result);
 		 
 			  } catch (ParserConfigurationException pce) {
 				pce.printStackTrace();
 			  } catch (TransformerException tfe) {
 				tfe.printStackTrace();
 			  }
 			}
 		
 	}
 	
 	
 
