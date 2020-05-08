 package dk.itu.kf04.g4tw.controller;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Arrays;
 
 import dk.itu.kf04.g4tw.model.Road;
 import dk.itu.kf04.g4tw.util.DynamicArray;
 
 /**
  *
  */
 public class RequestParser {
     
     public static InputStream parseToInputStream(String input) throws IllegalArgumentException, UnsupportedEncodingException {
     	// Variables for the request
     	double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
     	int filter = 0;
     	
         // Decode the input and split it up
         String[] queries = URLDecoder.decode(input, "UTF-8").split("&");
                 
         String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
   				"<roadCollection xmlns=\"http://www.w3schools.com\"" +
   				"\n\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
   				"\n\txsi:noNamespaceSchemaLocation=\"localhost/kraX.xsd\">\n";
 
         if (queries.length != 5) {
             throw new IllegalArgumentException("Must have exactly 5 queries.");
         }
 
         // Iterate through the queries
         for(String query : queries) {
             String[] arr = query.split("=");
             
             // Something is wrong if there are not exactly 2 values
             if (arr.length != 2) {
                 throw new IllegalArgumentException("Must have format x1=lowValue&y1=lowValue&x2=highValue&y2=highValue&filter=[1-?]");
             } else {
             	// Assign input to local variables
                 for(int i = 0; i < arr.length; i++) {
 					String name = arr[i];
 					String value = arr[++i];
 					if(name.equals("x1")) x1 = Double.parseDouble(value);
 					if(name.equals("x2")) x2 = Double.parseDouble(value);
 					if(name.equals("y1")) y1 = Double.parseDouble(value);
 					if(name.equals("y2")) y2 = Double.parseDouble(value);
 					if(name.equals("filter")) filter = Integer.parseInt(value);
 				}
             }
         }
         
         // magi!...
         DynamicArray<Road> search = MapController.model.search(x1, y1, x2, y2, filter);
         for(int i = 0; i < search.length(); i++) {
         	result += search.get(i).toXML() + "\n";
         }
         
         result += "</roadCollection>";
 
         try {
             return new ByteArrayInputStream(result.getBytes("UTF-8"));
         } catch (UnsupportedEncodingException e) {
             System.out.println("Unsupported encoding: " + e.getMessage());
             return null;
         }
     }
 }
