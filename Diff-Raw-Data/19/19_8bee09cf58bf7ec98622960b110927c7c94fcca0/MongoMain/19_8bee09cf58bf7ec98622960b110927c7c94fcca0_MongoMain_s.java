 package phil.projects.mongo.ui;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.util.Iterator;
 import java.util.Properties;
import java.util.Set;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 import phil.projects.mongo.MongoLoadConfig;
 
 public class MongoMain {
 
 	private static void parseDBObject(DBObject o, int indent) {
		Set<String> set = o.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
 			StringBuffer lineOut = new StringBuffer();
 			for (int i = 0; i < indent; i++) {
 				lineOut.append(" ");
 			}
 			if (o.get(key) instanceof BasicDBObject) {
 				System.out.println(key + " : {");
 				parseDBObject((BasicDBObject)o.get(key), indent+2);
 				System.out.println("}");
 			}
 			else {
 				lineOut.append(key).append(" : ").append(o.get(key).toString()).append(" (").append(o.get(key).getClass().getCanonicalName()).append(")");
 				System.out.println(lineOut.toString());
 			}
 		}
 		
 	}
 	
 	public static void main(String[] args) throws Exception {
 
 		String s = "{\"bool1\": true, \"bool2\": false, \"firstName\":\"TestFirstName\",\"lastName\": \"TestLastName\", \"subdoc\": {\"item\": \"someitem\", \"integer\": 12365465465465445, \"float\": 123.4}, \"empty\": \"\"}"; 
 		DBObject o = (DBObject)JSON.parse(s);
 		parseDBObject(o, 0);
 		
 		
 		//See if we were invoked with args and if so try to read properties filename from first argument
 		Properties prop = new Properties();
 		try {
 			if (args.length > 0) {
 				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(args[0]);
 				if (is == null) {
 					System.err.println("Unable to load specified properties file");
 					System.exit(1);					
 				}
 				prop.load(is);
 			}
 			else {
 				//Otherwise we default to mongoload.properties
 				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mongoload.properties");
 				if (is == null) {
 					System.err.println("Default properties file mongoload.properties not found and no properties file specified");
 					System.exit(-1);
 				}
 				prop.load(is);
 			}
 		}
 		catch (IOException ioe) {
 			System.err.println("Failed to load properties file: " + ioe.getMessage());
 			System.exit(-1);
 		}
 
 		//The config object to be passed to the UI thread, created based on the properties
 		final MongoLoadConfig config = new MongoLoadConfig(prop);
 		
 		//Create and run UI thread
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				MongoLoadUI ui = new MongoLoadUI(config);
 				ui.createAndShow();
 			}
 		});
 	}
 }
