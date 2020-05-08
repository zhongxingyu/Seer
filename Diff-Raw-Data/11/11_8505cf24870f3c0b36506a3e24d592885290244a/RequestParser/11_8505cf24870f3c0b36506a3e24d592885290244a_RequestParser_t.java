 package HttpServer;
 
 import HttpServer.Exceptions.BadRequestException;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * Author: Myles Megyesi
  */
 public class RequestParser {
 
     public Request parse(InputStream inputStream) throws BadRequestException {
         String rawRequest;
         try {
             rawRequest = inputStreamToString(inputStream);
         } catch (IOException e) {
             throw new BadRequestException(e.getMessage());
         }
         Scanner scanner = new Scanner(rawRequest);
         String firstLine = scanner.nextLine();
         String[] requestItems = firstLine.split(" ");
         if (requestItems.length != 3) {
             throw new BadRequestException(String.format("Improperly formatted request line: %s", firstLine));
         }
         String action = requestItems[0].trim();
         String url = requestItems[1].trim();
         String[] urlParts = url.split("\\?");
         String requestUri = urlParts[0];
         String query = "";
         if (urlParts.length > 1) {
             query = urlParts[1];
         }
         String protocol = requestItems[2].trim();
         List<RequestHeader> requestHeaders = new ArrayList<RequestHeader>();
         while (scanner.hasNextLine()) {
             String nextLine = scanner.nextLine().trim();
             if (nextLine.equals("")) {
                 break; //no more headers
             }
             requestHeaders.add(parseHeader(nextLine));
         }
         String body = "";
         if (scanner.hasNextLine()) {
             body = scanner.nextLine();
         }
         return new Request(action, requestUri, query, protocol, requestHeaders, body, rawRequest);
     }
 
     private RequestHeader parseHeader(String headerLine) throws BadRequestException {
         int colonIndex = headerLine.indexOf(':');
         String name = headerLine.substring(0, colonIndex).trim();
         String value = headerLine.substring(colonIndex + 1, headerLine.length()).trim();
         if (name == null || value == null) {
             throw new BadRequestException(String.format("Improperly formatted header: %s", headerLine));
         }
         return new RequestHeader(name, value);
     }
 
     private String inputStreamToString(InputStream input) throws IOException {
         StringBuilder buffer = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(input));
         if (!reader.ready()) {
             buffer.append((char) reader.read());
         }
         while (reader.ready()) {
             buffer.append((char) reader.read());
         }
         return buffer.toString();
     }
 }
