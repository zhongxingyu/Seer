 package phil.projects.mongo.ui;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import phil.projects.mongo.MongoLoadConfig;
 
 public class MongoMain {
 
 	public static void main(String[] args) throws Exception {
 
 		//See if we were invoked with args and if so try to read properties filename from first argument
 		Properties prop = new Properties();
 		try {
 			if (args.length > 0) {
 				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(args[0]);
 				if (is == null) {
					System.err.println("Unable to load properties file");
 					System.exit(1);					
 				}
 				prop.load(is);
 			}
 			else {
 				//Otherwise we default to mongoload.properties
 				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mongoload.properties");
 				if (is == null) {
					System.err.println("Default properties file mongoload.properties not found");
					System.exit(1);
 				}
 				prop.load(is);
 			}
 		}
 		catch (IOException ioe) {
			System.err.println("Unable to load properties file");
			System.exit(1);
 		}
 
		//The config object to be passed to the UI thread
//		final MongoLoadConfig config = new MongoLoadConfig("ukrhas6", 27017, "test", "admin", "admin", "admin", docCount);
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
