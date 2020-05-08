 package idealproject;
 
 import idealproject.businesslogic.Controller;
 
 import java.io.IOException;
 import java.util.Properties;
 
 public class Application {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Properties properties = new Properties();
		
 		try {
 			properties.load(Application
								.class
								.getClassLoader()
								.getResourceAsStream("idealproject/config.properties"));
 
 		}
 		catch (IOException e) {
 			System.err.println("Unable to read config.properties.");
 			e.printStackTrace();
 			System.exit(1);
 		}
		
 		Controller controller = new Controller(properties);
 
 	}
 
 }
