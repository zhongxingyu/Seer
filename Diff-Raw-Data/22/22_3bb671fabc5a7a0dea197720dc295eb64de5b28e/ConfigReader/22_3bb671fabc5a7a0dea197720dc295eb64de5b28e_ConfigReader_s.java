 package info.plagiatsjaeger;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 
 public class ConfigReader
 {
 	private static final Logger	_logger		= Logger.getLogger(Control.class.getName());
	private static final String	PROP_FILE	= "webapps/PlagiatsJaeger/WEB-INF/classes/config.properties";
 
 	/**
 	 * Schreibt einen Parameter in das ConfigFile.
 	 * 
 	 * @param name
 	 * @param value
 	 */
 	public void setProperty(String name, String value)
 	{
 		Properties prop = new Properties();
 
 		try
 		{
 			prop.load(new FileInputStream(PROP_FILE));
 			// set the properties value
 			prop.setProperty(name, value);
 			// save properties to project root folder
 			prop.store(new FileOutputStream(PROP_FILE), null);
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Liest einen Parameter aus dem ConfigFile aus.
 	 * 
 	 * @param name
 	 * @return Wert als String
 	 */
 	public static String getPropertyString(String name)
 	{
 		Properties prop = new Properties();
 
 		try
 		{
 			// load a properties file
 			prop.load(new FileInputStream(PROP_FILE));
 
 			String result = prop.getProperty(name);
 			_logger.info("Property " + name + ", Value: " + result);
 			// get the property value and print it out
 			return result;
 
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	/**
 	 * Liest einen Parameter aus dem ConfigFile aus.
 	 * 
 	 * @param name
 	 * @return Wert als int
 	 */
 	public static int getPropertyInt(String name)
 	{
 		Properties prop = new Properties();
 
 		try
 		{
 			// load a properties file
 			prop.load(new FileInputStream(PROP_FILE));
 			int result = Integer.parseInt(prop.getProperty(name));
 			_logger.info("Property " + name + ", Value: " + result);
 			// get the property value and print it out
 			return result;
 
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		return 0;
 	}
 }
