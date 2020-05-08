 package gov.nih.nci.ncicb.cadsr.common;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class CaDSRUtil {
 	
 	//Key to use for the path to cadsrutil.properties file from system properties
 	protected static String KEY_CADSR_PROPERTIES_PATH = "gov.nih.nci.cadsrutil.properties";
 	
 	//Key to retrieve the actually default context name
 	protected static final String KEY_DEFAULT_CONTEXT_NAME = "default.context.name";
 	
 	//Key to retrieve the NCI Registry ID
 	protected static final String NCI_REGISTRY_ID = "nci.registry.id";	
 	
 	protected static String defaultContextName;
 	protected static String nciRegistryId;
 
 	/**
 	 * Get the default context name from cadsrutil.properties. If a cached context name is valid, return it without
 	 * loading it from properties file again.
 	 * <br><br>
 	 * The property file's path and name should have been set as a System property with key "gov.nih.nci.cadsrutil.properties" <br>
 	 *  
 	 * @return default context name
 	 * @throws IOException if unable to find the properties file
 	 */
 	public static String getDefaultContextName() 
 			throws IOException {
  
		return (defaultContextName == null || defaultContextName.length() == 0) ?   
 				CaDSRUtil.getProperty(CaDSRUtil.KEY_DEFAULT_CONTEXT_NAME) : defaultContextName;
 
 	}
 	
 	/**
 	 * Read cadsrutil.properties file and return the default context name from it. 
 	 * 
 	 * <br><br>
 	 * The property file's path and name should have been set as a System property with key "gov.nih.nci.cadsrutil.properties" <br>
 	 *  
 	 * @return default context name
 	 * @throws IOException if unable to find the properties file
 	 */
 	public static String getDefaultContextNameNoCache() 
 			throws IOException {
  
 		return CaDSRUtil.getProperty(CaDSRUtil.KEY_DEFAULT_CONTEXT_NAME);
 
 	}
 	
 	/**
 	 * Get the NCI Registry ID. If a cached NCI Registry ID is valid, return it without
 	 * loading it from properties file again.
 	 * <br><br>
 	 * The property file's path and name should have been set as a System property with key "gov.nih.nci.cadsrutil.properties" <br>
 	 *  
 	 * @return default context name
 	 * @throws IOException if unable to find the properties file
 	 */
 	public static String getNciRegistryId() 
 			throws IOException {
  
 		return (nciRegistryId == null || nciRegistryId.length() == 0) ?   
 				CaDSRUtil.getProperty(CaDSRUtil.NCI_REGISTRY_ID) : nciRegistryId;
 
 	}
 	
 	/**
 	 * Read cadsrutil.properties file and return the  NCI Registry ID. 
 	 * 
 	 * <br><br>
 	 * The property file's path and name should have been set as a System property with key "gov.nih.nci.cadsrutil.properties" <br>
 	 *  
 	 * @return default context name
 	 * @throws IOException if unable to find the properties file
 	 */
 	public static String getNciRegistryIdNoCache() 
 			throws IOException {
  
 		return CaDSRUtil.getProperty(CaDSRUtil.NCI_REGISTRY_ID);
 
 	}	
 	
 	/**
 	 * Get a property value from the config file:
 	 *   /local/content/cadsrutil/cadsrutil.properties
 	 * @param key
 	 * @return
 	 * @throws IOException if unable to find the properties file
 	 */
 	protected static String getProperty(String key) 
 			throws IOException {
 		
 		String path = System.getProperty(KEY_CADSR_PROPERTIES_PATH);
 		if (path == null || path.length() == 0)
 			throw new IOException("Cadsrutil is unable to get property file path with this key \"" + KEY_CADSR_PROPERTIES_PATH + "\"");
 		
 		Properties properties = loadPropertiesFromFile(path);
		defaultContextName = properties.getProperty(key);
 		
		if (defaultContextName == null || defaultContextName.length() == 0)
			throw new IOException("Unable to find the default context name from file: \"" + path + "\"");
 
		return defaultContextName;
 	}
 	
 	protected static Properties loadPropertiesFromFile(String pathname) 
 	throws IOException {
 		
 		Properties properties = new Properties();
 		if (pathname == null || pathname.length() == 0)
 			return properties;
 
 		FileInputStream in = null;
 		in = new FileInputStream(pathname);
 		
 		if (in != null) {	
 			properties.load(in);
 			in.close();  
 		}
 		
 		return properties;
 	}
 
 }
