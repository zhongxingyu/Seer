 package fr.dr.http;
 
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.net.*;
 import java.util.Properties;
 
 
 /**
  * Make a POST request.
  * It was test with Google App Engine.
  * User: drieu
  * Date: 14/03/13
  * Time: 12:21
  */
 public class Post {
 
     private static final Logger logger = Logger.getLogger(Post.class);
 
     /**
      * Path to properties that contains proxy url, port ...
      */
     private static String propertiesPath;
 
     /**
      * URL
      */
     private String urlDest;
 
     /**
      * Proxy URL.
      */
     private String proxyUrl;
 
     /**
      * Proxy port.
      */
     private String proxyPort;
 
     private String ENCODING="UTF8";
 
 
     /**
      * Constructor with properties path.
      * @param path Properties Path
      */
     public Post(String path) {
         propertiesPath = path;
         initProperties();
     }
 
     /**
      * Load properties file to connect to svn and directory to store the result of checkout.
      */
     public void initProperties() {
         Properties prop = new Properties();
         try {
             prop.load(new FileInputStream(propertiesPath));
             urlDest = prop.getProperty("url");
             proxyUrl = prop.getProperty("proxy_url");
             proxyPort = prop.getProperty("proxy_port");
 
 
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
 
     /**
      * Send a POST message.
      * @param messageToSend
      * @param date e.g: 01/01/2013
      * @param level e.g: INFO
      * @param servers e.g: SERVER1,SERVER2
      */
     public void send(String messageToSend, String date, String level, String servers) {
 
         try {
             String message = URLEncoder.encode(messageToSend, ENCODING);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl, Integer.parseInt(proxyPort)));
 
             URL url = new URL(urlDest);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
             connection.setDoOutput(true);
             connection.setRequestMethod("POST");
 
             OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
             writer.write("date=" + date);
             writer.write("&level=" + level);
             writer.write("&servers=" + servers);
             writer.write("&msg=" + message);
             writer.close();
 
             if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 logger.info("OK ! message returned:" + connection.getResponseCode());
             } else {
                 logger.info("KO ! message returned:" + connection.getResponseCode());
             }
         } catch (MalformedURLException e) {
             logger.error(e.getMessage());
         } catch (IOException e) {
             logger.error(e.getMessage());
         }
     }
 }
