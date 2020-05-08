package fr.mleclaire.codestory.resources;
 
 import fr.mleclaire.codestory.jajascript.Candidate;
 import fr.mleclaire.codestory.jajascript.Flight;
 import fr.mleclaire.codestory.jajascript.Jajascript;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Jajascript challenge at "/jajascript" URI
  *
  * @author mathieu
  */
 @Path("jajascript")
 public class JajascriptResource {
 
     @POST
     @Path("optimize")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Response optimize(String content) throws IOException {
 
         List flights = new ObjectMapper().readValue(content, new TypeReference<List<Flight>>(){});
         Candidate result = Jajascript.optimize(flights);
         return Response.ok(result).status(201).build();
     }
 }
