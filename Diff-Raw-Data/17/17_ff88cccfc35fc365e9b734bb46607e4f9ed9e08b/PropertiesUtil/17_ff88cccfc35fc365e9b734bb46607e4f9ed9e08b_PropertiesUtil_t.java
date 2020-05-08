 package gov.nih.nci.cdmsconnector.util;
 
 import java.io.FileInputStream;
 import java.io.IOException;
import java.sql.Connection;
 import java.sql.DriverManager;
import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.Properties;
 import java.util.Set;
 
 
 public class PropertiesUtil {
 
 	// prc added this string for v2.3 DB property retrieval
 	private final static String GET_PROPS_FROM_DB = 
 		"select PROPERTY_KEY, PROPERTY_VALUE " 
 		+ "from CLINICAL_CONNECTOR_PROPERTIES ";
 
 	private static Properties props;
 	private static String propertiesFileLocation;
 
 	public static Properties getPropertiesFromFile() throws Exception {
 
 		if (props == null) {
 			String catalinaHome = System.getProperty("catalina.home");
 			String propertiesFilePath = "c3d/c3dgridservice.properties";
 			java.io.InputStream inputStream = null;
 
 			if (catalinaHome != null) {
 				propertiesFilePath = catalinaHome + "/conf/"
 						+ propertiesFilePath;
 				inputStream = new FileInputStream(propertiesFilePath);
 			} else {
 				inputStream = FileLoader.getInstance().getFileAsStream(
 						"registerConsumer_gridservice.properties");
 			}
 
 			Properties properties = new Properties();
 			if (inputStream != null) {
 				try {
 					properties.load(inputStream);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if (!properties.isEmpty()) {
 
 				validateProperties(properties);
 				props = properties;
 			}
 		}
 
 		return props;
 	}
 
 	public static Properties getDBProperties() throws Exception {
 
 		if (props == null) {
 			String catalinaHome = System.getProperty("catalina.home");
 			String propertiesFilePath = "c3d/c3dgridservice.properties";
 			java.io.InputStream inputStream = null;
 
 			if (catalinaHome != null) {
 				propertiesFilePath = catalinaHome + "/conf/"
 						+ propertiesFilePath;
 				inputStream = new FileInputStream(propertiesFilePath);
 			} else {
 				inputStream = FileLoader.getInstance().getFileAsStream(
 						"registerConsumer_gridservice.properties");
 			}
 
 			Properties properties = new Properties();
 			if (inputStream != null) {
 				try {
 					properties.load(inputStream);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if (!properties.isEmpty()) {
 
 				validateDBProperties(properties);
 				props = properties;
 			}
 		}
 
 		return props;
 	}
 
 	public static Properties getPropertiesFromDB() throws Exception {
 		
 		Connection cn = null;
 		PreparedStatement stmt1 = null;
 
 		//if (props == null) {
 		try {
 			getDBProperties();
 		} catch (Exception e) {
 
 		}
 
 		try {
 			String propKey = null, propVal = null;
 
 			Class.forName(props.getProperty(Constants.C3DDS_DRIVERCLASSNAME));
 			cn = DriverManager.getConnection(
 					props.getProperty(Constants.C3DDS_URL), 
 					props.getProperty(Constants.C3DDS_USERNAME), 
 					props.getProperty(Constants.C3DDS_PASSWORD));
 
 			stmt1 = cn.prepareStatement(GET_PROPS_FROM_DB);
 				
 			ResultSet rs1 = stmt1.executeQuery();
 			while (rs1.next()) {
 					
 				//recordExists = true;
 				propKey = rs1.getString("PROPERTY_KEY");
 				propVal = rs1.getString("PROPERTY_VALUE");
 
 				props.setProperty(propKey, propVal);
 			}
 	            				
 		} finally {
 			try {
 				stmt1.close();
 			} catch (Exception ex) {
 			}
 			try {
 				cn.close();
 			} catch (Exception ex) {
 			}
 		}
 
 		if (!props.isEmpty()) {
 			validateProperties(props);
 		}
 
 		//}
 		//showDBProperties();
 		return props;
 
 	}
 	
 	public static Properties loadProperties(String path) throws Exception{
 		propertiesFileLocation=path+"/c3dgridservice.properties";
 		getPropertiesFromFile();
 		props.put("CONF_FILES_DIR", path);
 		return props;
 	}
 
 	private static void validateProperties(Properties properties)
 			throws Exception {
 
 		validateDBProperties(properties);
 		// TODO
 		/*
 		 * Added Validations for required properties
 		 */
 		}
 
 	private static void validateDBProperties(Properties properties)
 	throws Exception {
 
 		String errorMessage = "";
 		boolean isValue = true;
 		
 		if (properties.containsKey(Constants.C3DDS_URL)) {
 			String url = properties.getProperty(Constants.C3DDS_URL);
 			if (isValue && url != null && url.length() > 0)
 				isValue = true;
 			else
 				isValue = false;
 		} else {
 			errorMessage = errorMessage + Constants.C3DDS_URL
 					+ " property is missing.";
 			isValue = false;
 		}
 		
 		if (properties.containsKey(Constants.C3DDS_DRIVERCLASSNAME)) {
 			String driverClassName = properties.getProperty(Constants.C3DDS_DRIVERCLASSNAME);
 			if (isValue && driverClassName != null
 					&& driverClassName.length() > 0)
 				isValue = true;
 			else
 				isValue = false;
 		} else {
 			errorMessage = errorMessage + Constants.C3DDS_DRIVERCLASSNAME
 					+ " property is missing.";
 			isValue = false;
 		}
 		
 		if (properties.containsKey(Constants.C3DDS_USERNAME)) {
 			String username = properties.getProperty(Constants.C3DDS_USERNAME);
 			if (isValue && username != null && username.length() > 0)
 				isValue = true;
 			else
 				isValue = false;
 		} else {
 			errorMessage = errorMessage + Constants.C3DDS_USERNAME
 					+ " property is missing.";
 			isValue = false;
 		}
 		
 		if (properties.containsKey(Constants.C3DDS_PASSWORD)) {
 			String password = properties.getProperty(Constants.C3DDS_PASSWORD);
 			if (isValue && password != null && password.length() > 0)
 				isValue = true;
 			else {
 				isValue = false;
 			}
 		} else {
 			errorMessage = errorMessage + Constants.C3DDS_PASSWORD
 					+ " property is missing.";
 			isValue = false;
 		}
 
 		if (!isValue)
 			throw new Exception(errorMessage);
 
 	}
 
 	private static void showDBProperties()
 	throws Exception {
 		Set properties;
 		String str = null;
 		
 		if (props == null) {
 			try {
 				getDBProperties();
 			} catch (Exception e) {
 
 			}
 		}
 				
 		properties = props.keySet();
 		java.util.Iterator itr = properties.iterator();
 		while (itr.hasNext()){
 			str = (String) itr.next();
 			System.out.println("Property '"+ str +" = '" + props.getProperty(str)+ "'.");
 		}
 		System.out.println();
 		
 	}
 }
