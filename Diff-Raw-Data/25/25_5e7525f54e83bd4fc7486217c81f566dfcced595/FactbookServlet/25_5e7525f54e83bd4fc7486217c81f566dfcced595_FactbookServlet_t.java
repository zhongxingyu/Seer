 package org.lenition.servlet;
 
 import com.google.gson.Gson;
 import org.lenition.domain.Factbook;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 @Path("factbook")
 public class FactbookServlet {
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public String factbook() {
         return convertStreamToString(FactbookServlet.class.getClassLoader().getResourceAsStream("factbook-countries.json"));
     }
 
     @GET
     @Path("question")
     @Produces(MediaType.APPLICATION_JSON)
     public String question(@QueryParam("n") int n) {
        return "TODO";

     }
 
     static String convertStreamToString(java.io.InputStream is) {
         java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
         return s.hasNext() ? s.next() : "";
     }
 
 }
