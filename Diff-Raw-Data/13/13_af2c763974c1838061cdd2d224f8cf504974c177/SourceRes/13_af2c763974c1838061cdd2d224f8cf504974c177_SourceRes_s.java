 package pfm.jaxrs;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import pfm.model.Source;
 import pfm.model.User;
 
 @Path("/source")
 @Stateless
 public class SourceRes {
     @Context
     private UriInfo context;
 
     @PersistenceContext
     EntityManager em;
 
     public SourceRes() {
     }
     
     @GET
     @Path("/{sourceId}")
     @Produces("application/json")
     public Response getJson(@PathParam("sourceId") int id) {
         Source src = em.find(Source.class, id);
         if (src != null) {
     		return Response.ok(src).build();
     	} else {
     		return Response.noContent().build();
     	}
     }
     
     @GET
     @Path("/list/{userId}")
     @Produces("application/json")
     public Response getJsonList(@PathParam("userId") int id) {
     	User user = em.find(User.class, id);
     	if (user != null) {
     		List<Source> list = user.getSources();
 			GenericEntity<List<Source>> entity = new GenericEntity<List<Source>>(list) {};
 	    	return Response.ok(entity).build();
     	} else {
     		return Response.noContent().build();
     	}
     }
     
     @POST
 	@Consumes("application/json")
 	public Response postJson(Source src) {
 		try {
 			User user = em.find(User.class, src.getUserId());
 			src.setUser(user);
 			em.persist(src);
 			return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();	
 		}
 	}
 
     @PUT
     @Consumes("application/json")
     public Response putJson(Source src) {
     	try {
 			User user = em.find(User.class, src.getUserId());
 			src.setUser(user);
 			em.merge(src);
 			return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();	
 		}
     }
     
     @DELETE
     @Path("/{sourceId}")
     public Response deleteJson(@PathParam("sourceId") int id) {
     	try {
     		Source src = em.find(Source.class, id);
         	em.remove(src);
         	return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();
 		}
     }
 
 }
