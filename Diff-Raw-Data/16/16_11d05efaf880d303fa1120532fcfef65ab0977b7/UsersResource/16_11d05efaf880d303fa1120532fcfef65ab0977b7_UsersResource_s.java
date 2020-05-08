 package org.robbins.flashcards.webservices;
 
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.robbins.flashcards.model.User;
 import org.robbins.flashcards.service.UserService;
 import org.robbins.flashcards.service.base.GenericJpaService;
 import org.robbins.flashcards.webservices.base.AbstractGenericResource;
 import org.springframework.stereotype.Component;
 
 import com.wordnik.swagger.annotations.ApiOperation;
 
 @Path("/v1/users/")
 @Component("usersResource")
//@Api(value="/users", description = "Operations about Users")
 @Produces("application/json")
 public class UsersResource extends AbstractGenericResource<User, Long> {
 	
 	@Inject
 	private UserService service;
 
 	protected GenericJpaService<User, Long> getService() {
 		return service;
 	}
 	
 	@GET
 	@Path("/search")
 	@ApiOperation(value = "Find a user by their OpenId", responseClass = "org.robbins.flashcards.model.User")
 	public User getUserSearch(@QueryParam("openid") String openid) {
 			return service.findUserByOpenid(openid);
 	}
 	
 	@Override
 	@ApiOperation(value = "Replace a user", responseClass = "javax.ws.rs.core.Response")
 	public Response put(@PathParam("id") Long id, User entity) {
 
 		if (entity.getCreatedBy() == null) {
 			User orig = service.findOne(id);
 			entity.setCreatedBy(orig.getCreatedBy());
 			entity.setCreatedDate(orig.getCreatedDate());
 		}
 		return super.put(id,  entity);
 	}
 }
