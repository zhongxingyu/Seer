 package com.agatteclient;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Singleton calss to parse response from the Agatte Server
  * <p/>
  * Created by remi on 03/10/13.
  */
 public class AgatteParser {
 
     private static AgatteParser ourInstance = new AgatteParser();
 
     public static AgatteParser getInstance() {
         return ourInstance;
     }
 
     private AgatteParser() {
     }
 
     public AgatteResponse parse_query_response(HttpResponse response) throws IOException {
         Collection<String> tops = new ArrayList<String>(6);
         if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            return new AgatteResponse(AgatteResponse.Code.UnknownError);
         }
 
         InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
         BufferedReader rd = new BufferedReader(reader);
         StringBuffer result = new StringBuffer();
         String line = "";
         while ((line = rd.readLine()) != null) {
             result.append(line.replace("[\\n\\r\\t]", " "));
             if (line.contains("</")) {
                 result.append(System.getProperty("line.separator"));
             }
         }
         //search for Unauthorized network
         Pattern unauthorized = Pattern.compile("<legend>Acc\ufffds interdit</legend>");
         Matcher matcher;
         matcher = unauthorized.matcher(result);
         if (matcher.find()) {
             return new AgatteResponse(AgatteResponse.Code.NetworkNotAuthorized);
         }
 
         Pattern p = Pattern.compile(".*<li.*([0-9][0-9]:[0-9][0-9])\\s*</li>.*");
         matcher = p.matcher(result);
         while (matcher.find()) {
             tops.add(matcher.group(1));
         }
 
         return new AgatteResponse(AgatteResponse.Code.QueryOK, tops);
     }
 
     public AgatteResponse.Code parse_punch_response(HttpResponse response1, String punchOkDir) {
 
         //verify that response is a redirection to topOk.htm (ie punchOkDir)
 
 
 
 
         return AgatteResponse.Code.TemporaryOK;
     }
 }
