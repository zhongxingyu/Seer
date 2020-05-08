 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package firstcup.webservice;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.Produces;
 
 /**
  * REST Web Service
  *
  * @author Tong
  */
 @Path("dukesAge")
 public class DukesAgeResource {
 
     @Context
     private UriInfo context;
 
     /**
      * Creates a new instance of DukesAgeResource
      */
     public DukesAgeResource() {
     }
 
     /**
      * Retrieves representation of an instance of firstcup.webservice.DukesAgeResource
      * @return an instance of java.lang.String
      */
     @GET
     @Produces("text/plain")
     public String getText() {
         // Duke's birthday
         Calendar dukesBirthday = new GregorianCalendar(1995, Calendar.MAY, 23);
         
         // Current date 
         Calendar now = GregorianCalendar.getInstance();
         
         // Calculate Duke's age
         int dukesAge = now.get(Calendar.YEAR) - dukesBirthday.get(Calendar.YEAR);
         if (now.before(dukesBirthday)) {
            dukesAge = dukesAge - 1;
         }
         
         // Return the Duke's age
         return Integer.toString(dukesAge);
     }
 }
