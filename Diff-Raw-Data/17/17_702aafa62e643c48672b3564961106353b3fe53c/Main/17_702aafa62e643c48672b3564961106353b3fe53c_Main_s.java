 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package testbotr;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
import java.net.MalformedURLException;
 import java.net.URL;
import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.Collections;
import java.util.Date;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 /**
  *
  * @author waxzce
  */
 public class Main {
 
     private static String convertToHex(byte[] data) {
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < data.length; i++) {
             int halfbyte = (data[i] >>> 4) & 0x0F;
             int two_halfs = 0;
             do {
                 if ((0 <= halfbyte) && (halfbyte <= 9)) {
                     buf.append((char) ('0' + halfbyte));
                 } else {
                     buf.append((char) ('a' + (halfbyte - 10)));
                 }
                 halfbyte = data[i] & 0x0F;
             } while (two_halfs++ < 1);
         }
         return buf.toString();
     }
 
     private static String getNewNonce() {
         String nonce = "";
         Random random = new Random();
         while (nonce.length() < 8) {
            nonce.concat(String.valueOf(random.nextInt(100)));
         }
         return nonce.substring(0, 7);
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws NoSuchAlgorithmException,
             IOException {
         String key = "KEY";
         String secret = "SECRET";
         String basicurl = "https://api.bitsontherun.com/v1/videos/list";
 
         Map<String, String> argsquery = new HashMap<String, String>();
         argsquery.put("api_key", key);
         argsquery.put("api_format", "json");
         argsquery.put("api_timestamp", String.valueOf(System.currentTimeMillis()));
         argsquery.put("api_nonce", getNewNonce());



         ArrayList<String> listkey = new ArrayList<String>(argsquery.keySet());
         Collections.sort(listkey);
         String querystring = "";
         for (String string : listkey) {
             querystring = querystring + string + "=" + URLEncoder.encode(argsquery.get(string)) + "&";
         }
         querystring.substring(0, querystring.length());
         querystring = querystring + secret;
         MessageDigest md;
         md = MessageDigest.getInstance("SHA-1");
 
         //querystring = URLEncoder.encode(querystring);
         String signature = convertToHex(md.digest(querystring.getBytes()));
 
         URL url = new URL(basicurl + "?" + querystring + "&api_signature=" + signature);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
         connection.setRequestMethod("GET");
         connection.setDoInput(true);
         connection.connect();
         System.out.println(connection.getResponseCode());
         System.out.println(connection.getResponseMessage());
         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String line = null;
         String full = "";
         while ((line = in.readLine()) != null) {
             full = full + line;
         }
         System.out.println(full);
     }
 }
