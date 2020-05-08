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
