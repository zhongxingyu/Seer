package fr.mleclaire.codestory.Resources;
 
 import fr.mleclaire.codestory.calc.Calculator;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Main resource at "/" URI (root)
  * @author mathieu
  */
 @Path("/")
 public class MainResource {
 
     private final static Logger logger = LoggerFactory.getLogger(MainResource.class);
 
     private final static String VALID_EXP_PATTERN = "([0-9+\\-*()\\./])*";
 
     private Configuration param; // properties file where queries are
 
     public MainResource() {
         try {
             param = new PropertiesConfiguration("queryParameters.properties");
         } catch (ConfigurationException e) {
             logger.error("Failed to load Queries ", e);
         }
     }
 
     @GET
     @Produces({"text/plain", "text/html"})
     public Response email(@DefaultValue("") @QueryParam("q") String q) {
         if (this.param.containsKey(q)) {
             return Response.ok(param.getString(q),"text/plain").build();
 
         } else if(format(q).matches(VALID_EXP_PATTERN) && !"".equals(q)) {
             return Response.ok(Calculator.compute(format(q)),"text/plain").build();
 
         }else {
             return Response.ok(getClass().getResourceAsStream( "/web/index.html")).build();
         }
     }
 
 
     @POST
     @Path("enonce/{id}")
     @Consumes("application/x-www-form-urlencoded")
     public Response acceptChallenge(String content, @PathParam("id") int id) {
         File newTextFile = new File("challenge"+id+".md");
         try {
             FileWriter fileWriter = new FileWriter(newTextFile);
             fileWriter.write(content);
             fileWriter.close();
             return Response.status(201).build();
         } catch (IOException e) {
             logger.debug("failed to save challenge in a file",e);
         }
         return Response.status(500).build();
     }
 
     /**
      * Format string into a valid math expression
      * Replace blank by <b>+</b> and <b>,</b> by <b>.</b>
      * @param q
      * @return
      */
     private String format(String q) {
         if (q != null)
             return q.replace(" ","+").replace(",", ".");
         else
             return q;
     }
 }
