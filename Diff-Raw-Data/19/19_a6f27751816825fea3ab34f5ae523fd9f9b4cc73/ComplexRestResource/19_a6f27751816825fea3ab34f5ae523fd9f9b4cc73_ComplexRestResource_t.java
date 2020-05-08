 package experiments.resources;
 
 import java.util.Collection;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import experiments.exceptions.NotSupportedException;
 
 /**
  * @author zlatka
  * 
  */
 public abstract class ComplexRestResource<T> {
 
     public abstract Collection<T> getResources(Long baseResourceId) throws NotSupportedException;
 
     public abstract T getResource(Long baseResourceId, Long resourceId) throws NotSupportedException;
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
    public Response getAllResources(@PathParam("id") Long baseResourceId) throws NotSupportedException {
         return Response.ok(getResources(baseResourceId)).build();
     }
 
     @GET
     @Path("{resourceId}")
     @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("id") Long baseResourceId, @PathParam("resourceId") Long resourceId) throws NotSupportedException {
         return Response.ok(getResource(baseResourceId, resourceId)).build();
     }
 
 }
