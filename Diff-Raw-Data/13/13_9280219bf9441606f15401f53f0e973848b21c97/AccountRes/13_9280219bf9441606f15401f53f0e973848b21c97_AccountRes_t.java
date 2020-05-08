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
import javax.ws.rs.core.Response.Status;
 
 import pfm.model.Account;
 import pfm.model.User;
 
 @Path("account")
 @Stateless
 public class AccountRes {
     @Context
     private UriInfo context;
 
     @PersistenceContext
     EntityManager em;
     
     public AccountRes() {
     }
     
     @GET
     @Path("/{accountId}")
     @Produces("application/json")
     public Response getJson(@PathParam("accountId") int id) {
         Account acc = em.find(Account.class, id);
         if (acc != null) {
     		return Response.ok(acc).build();
     	} else {
    		return Response.status(Status.NOT_FOUND).build();
     	}
     }
     
     @GET
     @Path("/list/{userId}")
     @Produces("application/json")
     public Response getJsonList(@PathParam("userId") int id) {
     	User user = em.find(User.class, id);
     	if (user != null) {
     		em.refresh(user);
     		List<Account> list = user.getAccounts();
 			GenericEntity<List<Account>> entity = new GenericEntity<List<Account>>(list) {};
 	    	return Response.ok(entity).build();
     	} else {
    		return Response.status(Status.NOT_FOUND).build();
     	}
     }
     
     @POST
 	@Consumes("application/json")
 	public Response postJson(Account acc) {
 		try {
 			User user = em.find(User.class, acc.getUserId());
 			acc.setUser(user);
 			em.persist(acc);
			em.flush();
 			return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();	
 		}
 	}
 
     @PUT
     @Consumes("application/json")
     public Response putJson(Account acc) {
     	try {
     		User user = em.find(User.class, acc.getUserId());
 			acc.setUser(user);
			em.merge(acc);
			em.flush();
 			return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();	
 		}
     }
     
     @DELETE
     @Path("/{accountId}")
     public Response deleteJson(@PathParam("accountId") int id) {
     	try {
     		Account acc = em.find(Account.class, id);
         	em.remove(acc);
         	return Response.ok().build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.serverError().build();	
 		}
     }
 }
