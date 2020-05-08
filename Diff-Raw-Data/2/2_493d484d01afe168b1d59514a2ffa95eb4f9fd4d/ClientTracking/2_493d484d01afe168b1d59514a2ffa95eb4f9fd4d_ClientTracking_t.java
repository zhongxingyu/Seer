 package edu.umd.lib.wstrack.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.misc.BASE64Encoder;
 
 public class ClientTracking {
 
   public static final Logger log = LoggerFactory
       .getLogger(ClientTracking.class);
 
   /**
    * @param args
    */
 
   public static String generateHash(String input) {
     String hash = "";
     try {
       MessageDigest sha = MessageDigest.getInstance("SHA-1");
       byte[] hashedBytes = sha.digest(input.getBytes());
       hash = (new BASE64Encoder().encode(hashedBytes));
     } catch (Exception e) {
       log.debug("The exception is " + e);
     }
     return hash;
   }
 
   /*
    * @Javadoc - Main method to retrieve the workstation tracking details.
    */
   public static void main(String[] args) throws MalformedURLException,
       IOException {
 
     String username = "user.name";
     String usernameProperty = System.getProperty(username);
 
     String nameOS = "os.name";
     String property = System.getProperty(nameOS);
     log.debug("The OS Name is :" + property);
 
     String hostName = InetAddress.getLocalHost().getHostName();
     log.debug("The hostname is :" + hostName);
 
     String hostAddress = InetAddress.getLocalHost().getHostAddress();
     log.debug("The host address is :" + hostAddress);
 
     String hashedUsername = generateHash(usernameProperty);
 
    boolean guestFlag = false;
     if (usernameProperty.startsWith("libguest")) {
       guestFlag = true;
 
     }
     /*
      * @Javadoc- Code to submit URL
      */
     try {
       // Construct data
       String data = URLEncoder.encode("key1", "UTF-8") + "="
           + URLEncoder.encode("value1", "UTF-8");
       data += "&" + URLEncoder.encode("key2", "UTF-8") + "="
           + URLEncoder.encode("value2", "UTF-8");
 
       // Send data
       // URL url = new URL("http://hostname:80/cgi");
       URL url = new URL("http://" + hostName
           + ":8888/wstrack-client/wstrackClient/track/" + hostAddress + "/"
           + hashedUsername + "/" + property);
       URLConnection conn = url.openConnection();
       conn.setDoOutput(true);
       OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
       wr.write(data);
       wr.flush();
 
       // Get the response
       BufferedReader rd = new BufferedReader(new InputStreamReader(
           conn.getInputStream()));
       String line;
       while ((line = rd.readLine()) != null) {
         // Process line...
       }
       wr.close();
       rd.close();
     } catch (Exception e) {
     }
 
   }
 
 }
