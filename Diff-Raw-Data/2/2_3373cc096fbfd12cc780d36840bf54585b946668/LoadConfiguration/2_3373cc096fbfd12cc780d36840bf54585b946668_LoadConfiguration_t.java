 package com.irdeto.activemq;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import org.apache.activemq.ActiveMQConnection;
 
 public class LoadConfiguration {
 	
 	public static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
 	public static String queue = "";
 	
 	public static void loadProperties(String file) {
 
 		Properties properties = new Properties();
 		ClassLoader loader = Thread.currentThread().getContextClassLoader();   
 
 		try {
			InputStream stream = loader.getResourceAsStream(file);			
 			properties.load(stream);
 			
 			if(properties!=null){
 				String urlProperty = properties.getProperty("url");
 				if(urlProperty!=null){
 					url= urlProperty;
 				}else {
 					System.err.println("Url is not defined! Default value will be used: failover://tcp://localhost:61616");
 				}
 				
 				String queueProperty = properties.getProperty("queue");
 				if(queueProperty!=null){
 					queue = queueProperty;
 				}else {
 					System.err.println("Queue name is null!");
 				}
 			}
 		} catch (IOException e) {
 			System.err.println("Unable to load properties file!");
 			e.printStackTrace();
 		}	catch (NullPointerException ex){
 			System.out.println("Missing properties file!");
 			ex.printStackTrace();
 		}
 	}
 }
