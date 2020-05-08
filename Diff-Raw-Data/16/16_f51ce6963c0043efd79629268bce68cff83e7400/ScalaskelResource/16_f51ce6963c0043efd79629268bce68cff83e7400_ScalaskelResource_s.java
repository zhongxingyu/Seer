package fr.mleclaire.codestory.Resources;
 
 import fr.mleclaire.codestory.scalaskel.Currency;
 
 import javax.ws.rs.*;
 import java.util.Collection;
 
 /**
  * Scalaskel challenge at "/scalaskel" URI
  *
  * @author mathieu
  */
 
 @Path("scalaskel")
 public class ScalaskelResource {
 
     @GET
     @Path("change/{cents}")
     @Produces("application/json")
     public Collection<Currency> change(@PathParam("cents") int cents) {
         if (cents > 0 && cents <=100) {
             Currency c = new Currency(cents);
             return Currency.exchange(c);
         } else return null;
     }
 }
