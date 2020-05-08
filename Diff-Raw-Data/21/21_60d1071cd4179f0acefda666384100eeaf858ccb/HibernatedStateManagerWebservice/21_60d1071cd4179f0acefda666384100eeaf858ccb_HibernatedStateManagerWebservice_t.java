 package dk.statsbiblioteket.mediaplatform.workflowstatemonitor;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import java.util.Date;
 import java.util.List;
 
 /**
  * This class is annotated to be exposed as a REST webservice.
  */
 @Path("/")
 public class HibernatedStateManagerWebservice extends HibernatedStateManager {
     @Override
     @POST
     @Path("states/{entityName}/")
     @Consumes("text/xml")
     public void addState(@PathParam("entityName") String entityName, State state) {
         super.addState(entityName,
                        state);
     }
 
     @Override
     @GET
     @Path("entities/")
     @Produces({"text/xml", "application/json"})
     public List<Entity> listEntities() {
         return super.listEntities();
     }
 
     @GET
     @Path("states/{entityName}/")
     @Produces({"text/xml", "application/json"})
     public List<State> listStates(@PathParam("entityName") String entityName,
                                   @QueryParam("onlyLast") boolean onlyLast,
                                   @QueryParam("includes") List<String> includes,
                                   @QueryParam("excludes") List<String> excludes,
                                   @QueryParam("startDate") String startDateString,
                                   @QueryParam("endDate") String endDateString) {
        Date startDate = null;
        if (startDateString != null && !startDateString.isEmpty()) {
            startDate = javax.xml.bind.DatatypeConverter.parseDateTime(startDateString).getTime();
        }
        Date endDate = null;
        if (endDateString != null && !endDateString.isEmpty()) {
            endDate = javax.xml.bind.DatatypeConverter.parseDateTime(endDateString).getTime();
        }
         return super.listStates(entityName, onlyLast, includes, excludes, startDate,
                                 endDate);
     }
 
     @GET
     @Path("states/")
     @Produces({"text/xml", "application/json"})
     public List<State> listStates(@QueryParam("onlyLast") boolean onlyLast,
                                   @QueryParam("includes") List<String> includes,
                                   @QueryParam("excludes") List<String> excludes,
                                   @QueryParam("startDate") String startDateString,
                                   @QueryParam("endDate") String endDateString) {
        Date startDate = null;
        if (startDateString != null && !startDateString.isEmpty()) {
            startDate = javax.xml.bind.DatatypeConverter.parseDateTime(startDateString).getTime();
        }
        Date endDate = null;
        if (endDateString != null && !endDateString.isEmpty()) {
            endDate = javax.xml.bind.DatatypeConverter.parseDateTime(endDateString).getTime();
        }
         return super.listStates(onlyLast, includes, excludes, startDate,
                                 endDate);
     }
 }
