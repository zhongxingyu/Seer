 package org.imaginea.botbot.api;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.testng.Assert;
 
 public class DefaultProperties {
 	static Properties prop = new Properties();
 	static DefaultProperties instance = null;
 
 	private DefaultProperties() {
 		try {
			prop.load(new FileInputStream("../default.properties"));
 		} catch (FileNotFoundException e1) {
 			Assert.fail("Unable to load file due to error :" + e1.toString());
 		} catch (IOException e1) {
 			Assert.fail("Unable to load file due to error :" + e1.toString());
 		}
 	}
 
 	public static DefaultProperties getDefaultProperty() {
 		if (instance == null) {
 			instance = new DefaultProperties();
 			return instance;
 		} else
 			return instance;
 	}
 
 	public String getValueFromProperty(String locator) {
 		String value = prop.getProperty(locator, "");
 		return value;
 	}
 }
