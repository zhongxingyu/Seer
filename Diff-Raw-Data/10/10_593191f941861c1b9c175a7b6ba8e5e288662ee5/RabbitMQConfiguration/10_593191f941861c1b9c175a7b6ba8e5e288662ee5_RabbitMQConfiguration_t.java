 package org.youfood.utils;
 
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class RabbitMQConfiguration {
 
    private static final String RABBITMQ_PROPERTIES = "/org/youfood/configuration/rabbitmq.properties";
 
     private String username;
     private String password;
     private String hostname;
     private int port;
 
     public RabbitMQConfiguration() {
         this(RABBITMQ_PROPERTIES);
     }
 
     public RabbitMQConfiguration(String propertiesPath) {
         Properties properties = new Properties();
         try {
             properties.load(getClass().getResourceAsStream(propertiesPath));
             username = properties.getProperty("rabbitmq.username");
             password = properties.getProperty("rabbitmq.password");
             hostname = properties.getProperty("rabbitmq.hostname");
             port = Integer.parseInt(properties.getProperty("rabbitmq.port"));
         } catch (IOException e) {
             throw new RuntimeException("Unable to load RabbitMQ properties", e);
         }
     }
 
     public String getUsername() {
         return username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public int getPort() {
         return port;
     }
 }
