 package plugin;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 
 /**
  * Initializes and maintains ProductIdentificationPlugins.
  * 
  * @author Matthew
  * @version 1.0 -- Snell CS 340 Phase 4.0
  */
 public class ProductIdentificationPluginManager {
 	private final BufferedReader pluginsBufferedReader;
 	private ProductIdentificationPluginWrapper root;
 	private final IHttpClient httpClient;
 
 	public ProductIdentificationPluginManager() {
 		Reader pluginsReader;
 		try {
 			pluginsReader = new FileReader(new File("plugins.cfg"));
 		} catch (FileNotFoundException e) {
 			pluginsReader = new StringReader("");
 		}
 
 		pluginsBufferedReader = new BufferedReader(pluginsReader);
 
 		httpClient = new HttpClient();
 
 		loadPlugins();
 	}
 
 	public ProductIdentificationPluginManager(String pluginString, IHttpClient httpClient) {
 		Reader pluginsReader = new StringReader(pluginString);
 		pluginsBufferedReader = new BufferedReader(pluginsReader);
 
 		this.httpClient = httpClient;
 
 		loadPlugins();
 	}
 
 	/**
 	 * Uses the ProductIdentificationPlugin to determine the description of the product
 	 * 
 	 * @param productBarcode
 	 * @return the description for the product, or null if not found
 	 * 
 	 * @pre product != null
 	 * @post true
 	 */
 	public String getDescriptionForProduct(String productBarcode) {
		return "";// root.getDescriptionForProduct(productBarcode);
 	}
 
 	/**
 	 * Loads all plugins based on config file
 	 */
 	public void loadPlugins() {
 		try {
 			ProductIdentificationPluginWrapper previousWrapper = null;
 
 			String line = null;
 			while ((line = pluginsBufferedReader.readLine()) != null) {
 				Class c = null;
 
 				try {
 					c = Class.forName(line);
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					continue;
 				}
 				ProductIdentificationPlugin plugin = null;
 				try {
 					plugin = (ProductIdentificationPlugin) c.newInstance();
 				} catch (InstantiationException e) {
 					System.out.println("Unable to instantiate " + c.getCanonicalName());
 					e.printStackTrace();
 					continue;
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					continue;
 				}
 
 				plugin.setClient(httpClient);
 				ProductIdentificationPluginWrapper currentWrapper = new ProductIdentificationPluginWrapper(
 						plugin);
 				if (root == null) {
 					root = currentWrapper;
 				} else {
 					previousWrapper.setSuccessor(currentWrapper);
 				}
 				previousWrapper = currentWrapper;
 			}
 			pluginsBufferedReader.close();
 		} catch (IOException e) {
 			System.err.println("Unable to read plugin configuration");
 		}
 	}
 }
