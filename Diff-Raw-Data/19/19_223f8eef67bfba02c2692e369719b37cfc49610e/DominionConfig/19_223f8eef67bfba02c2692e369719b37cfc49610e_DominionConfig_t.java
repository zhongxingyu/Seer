 package org.fazio.dominion.config;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 /**
  * @author Michael Fazio
  * @since 10/22/12 10:22 AM
  */
 public class DominionConfig {
 
 	private static Properties properties = DominionConfig.loadProperties();
 
 	public static final String WEB_SERVICE_URL = properties.getProperty("WebServiceURL");
 	public static final String WEB_SERVICE_PORT = properties.getProperty("WebServicePort");
 	public static final String MONGO_DB_URL = properties.getProperty("MongoDBURL");
 	public static final String MAIN_DB = properties.getProperty("MainDB");
 	public static final String TEST_DB = properties.getProperty("TestDB");
 	public static final String CARD_PRICE_LOW = properties.getProperty("CardPriceLow");
 	public static final String CARD_PRICE_HIGH = properties.getProperty("CardPriceHigh");
    public static final String REST_RESOURCE_LOCATION = properties.getProperty("RestResourceLocation");
 
 	private static Properties loadProperties() {
 
 		final Properties properties = new Properties();
 
 		try {
 			final InputStream stream = DominionConfig.class.getClassLoader().getResourceAsStream("config/dominion.properties");
 
 			properties.load(stream);
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 
 		return properties;
 	}
 
 	public static String getProperty(final String propertyName) {
 		return DominionConfig.properties.getProperty(propertyName);
 	}
 
 }
