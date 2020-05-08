 package ar.edu.unicen.ringo.agent.api;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import ar.edu.unicen.ringo.persistence.InvocationData;
 import ar.edu.unicen.ringo.persistence.PersistenceService;
 
 /**
  * Service API exposed by the agent that accepts logging calls from different
  * services.
  *
  * @author psaavedra
  */
 @Path("/agent")
 public class AgentService {
 
     /**
      * The persistence service.
      */
     private volatile PersistenceService persistenceService;
 
     /**
      * Logs a call from a service for later reporting.
      * 
      * @param info
      *            The info about the call.
      * @return A JAX-RS response object representing the result.
      */
     @POST
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     public Response monitor(CallInfo info) {
         if (persistenceService == null) {
             return Response.status(Status.SERVICE_UNAVAILABLE)
                     .entity("Service unavailable").build();
         }
         try {
             InvocationData data = new InvocationData();
             data.setExecutionTime(info.getExecutionTime());
             data.setMethod(info.getMethod());
             //TODO Locally set?
             data.setNode(info.getNode());
             data.setSla(info.getSla());
             data.setTimestamp(info.getTimestamp());
             this.persistenceService.persist(data);
             return Response.ok().build();
         } catch (RuntimeException e) {
             return Response.status(Status.SERVICE_UNAVAILABLE)
                     .entity("Service unavailable").build();
         }
     }
 
     /**
      * Reports the status of the agent API.
      * @return A JAX-RS response describing the status.
      */
     @GET
     @Produces(MediaType.TEXT_PLAIN)
     public Response status() {
         if (this.persistenceService == null) {
             return Response.status(Status.SERVICE_UNAVAILABLE)
                     .entity("Persistence service not available").build();
         }
         return Response.ok("It's working. Persistence service is: " + this.persistenceService).build();
     }
 
     public void setPersistenceService(PersistenceService persistenceService) {
         this.persistenceService = persistenceService;
     }
 
 }
