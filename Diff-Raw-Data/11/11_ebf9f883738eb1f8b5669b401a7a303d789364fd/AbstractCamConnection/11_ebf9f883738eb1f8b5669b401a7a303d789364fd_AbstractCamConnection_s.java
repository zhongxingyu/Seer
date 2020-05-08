 package com.mattbertolini.camclient.net;
 
 import com.mattbertolini.camclient.CamCredentials;
 import com.mattbertolini.camclient.request.CamRequest;
 import com.mattbertolini.camclient.response.CamResponse;
 import com.mattbertolini.camclient.response.CamResponseImpl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 /**
  * @author Matt Bertolini
  */
 public abstract class AbstractCamConnection<Request, Response> implements CamConnection {
     protected static final String USER_AGENT = "User-Agent";
     private static final String COMMENT_BEGIN_PATTERN = "\\s*<!--\\s*";
     private static final String COMMENT_END_PATTERN = "\\s*-->\\s*";
     private static final String NAME_VALUE_DELIMITER_PATTERN = "[=,]";
     private static final String ERROR_KEY = "error";
     private static final String COUNT_KEY = "count";
     private static final String EMPTY_STRING = "";
     private static final String ZERO = "0";
     private static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
     private URI uri;
     private CamCredentials credentials;
 
     public AbstractCamConnection(URI uri, CamCredentials credentials) {
         this.uri = uri;
         this.credentials = credentials;
     }
 
     public abstract Request buildRequest(CamRequest camRequest);
     public abstract CamResponse buildResponse(Response httpResponse);
     public abstract Response submitRequest(Request request);
 
     public String getUserAgent() {
         // TODO: Finish
         UserAgentBuilder userAgentBuilder = new UserAgentBuilder();
         return userAgentBuilder.buildUserAgentString();
     }
 
     @Override
     public CamResponse executeRequest(CamRequest camRequest) {
         Request request = this.buildRequest(camRequest);
         Response response = this.submitRequest(request);
         return this.buildResponse(response);
     }
 
     protected CamResponse parseResponse(InputStream responseBody, String encoding) {
         Scanner scanner = new Scanner(responseBody, encoding);
         StringBuilder sb = new StringBuilder();
         while(scanner.hasNextLine()) {
             sb.append(scanner.nextLine());
             sb.append(LINE_SEPARATOR);
         }
         scanner.close();
         try {
             responseBody.close();
         } catch (IOException e) {
             // TODO: finish
         }
 
         boolean error = false;
         String errorText = null;
         String responseString = sb.toString();
         String[] rows = responseString.split(COMMENT_END_PATTERN);
         if(rows == null || rows.length == 0) {
             // TODO: throw exception
             throw new RuntimeException();
         }
         List<Map<String, String>> data = new ArrayList<Map<String, String>>(rows.length);
         for(String row : rows) {
             Map<String, String> rowData = new HashMap<String, String>();
             row = row.replaceAll(COMMENT_BEGIN_PATTERN, EMPTY_STRING);
             String[] nvp = row.split(NAME_VALUE_DELIMITER_PATTERN);
             if(nvp.length % 2 != 0) {
                 // TODO: throw exception
                 throw new RuntimeException();
             }
 
             for(int i = 0; i < nvp.length; i += 2) {
                 rowData.put(nvp[i].trim().toLowerCase(), nvp[i + 1].trim());
             }
 
             if(rowData.containsKey(ERROR_KEY)
                     && !rowData.get(ERROR_KEY).equals(ZERO)) {
                 error = true;
                 errorText = "CAM Error - " + rowData.get(ERROR_KEY);
                 data = Collections.emptyList();
                 break;
             } else if(rowData.containsKey(COUNT_KEY)) {
                 if(rowData.get(COUNT_KEY).equals(ZERO)) {
                     data = Collections.emptyList();
                     break;
                 }
             } else {
                 // Since the error value is zero (success), we don't need it
                 // anymore. Not adding it to the final data collection.
                 if(!rowData.containsKey(ERROR_KEY)
                         && !rowData.containsKey(COUNT_KEY)) {
                     data.add(rowData);
                 }
             }
         }
 
         return new CamResponseImpl(responseString, data, error, errorText);
     }
 
     public URI getUri() {
         return this.uri;
     }
 
     public CamCredentials getCredentials() {
         return this.credentials;
     }
 }
