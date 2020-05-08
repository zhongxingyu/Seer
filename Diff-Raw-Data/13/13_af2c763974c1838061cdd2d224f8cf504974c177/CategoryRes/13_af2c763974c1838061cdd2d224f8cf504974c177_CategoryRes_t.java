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
 
 import pfm.model.Category;
 import pfm.model.User;
 
 
 @Path("category")
 @Stateless
 public class CategoryRes {
     @Context
     private UriInfo context;
 
     @PersistenceContext
     EntityManager em;
     
     public CategoryRes() {
     }
         
     @GET
     @Path("/{categoryId}")
     @Produces("application/json")
     public Response getJson(@PathParam("categoryId") int id) {
     	Category cat = em.find(Category.class, id);
     	if (cat != null) {
     		return Response.ok(cat).build();
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
    		em.refresh(user);
     		List<Category> list = user.getCategories();
 			GenericEntity<List<Category>> entity = new GenericEntity<List<Category>>(list) {};
 	    	return Response.ok(entity).build();
     	} else {
     		return Response.noContent().build();
     	}
 	}
     
 	@POST
 	@Consumes("application/json")
 	public Response postJson(Category cat) {
 		try {
 			User user = em.find(User.class, cat.getUserId());
 			cat.setUser(user);
 			em.persist(cat);
 			return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();	
 		}
 	}
 
     @PUT
     @Consumes("application/json")
     public Response putJson(Category cat) {
     	try {
     		User user = em.find(User.class, cat.getUserId());
 			cat.setUser(user);
 			em.merge(cat);
 			return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();
 		}
     }
     
     @DELETE
     @Path("/{categoryId}")
     public Response deleteJson(@PathParam("categoryId") int id) {
     	try {
     		Category cat = em.find(Category.class, id);
         	em.remove(cat);
         	return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();
 		}
     }
 
 }
