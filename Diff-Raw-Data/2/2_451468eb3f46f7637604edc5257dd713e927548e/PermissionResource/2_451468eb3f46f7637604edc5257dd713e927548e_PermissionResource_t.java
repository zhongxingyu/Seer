 package pl.psnc.dl.wf4ever.accesscontrol;
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
 import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
 import pl.psnc.dl.wf4ever.accesscontrol.model.dao.PermissionDAO;
 import pl.psnc.dl.wf4ever.auth.RequestAttribute;
 import pl.psnc.dl.wf4ever.db.UserProfile;
 import pl.psnc.dl.wf4ever.db.dao.UserProfileDAO;
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.model.Builder;
 
 import com.hp.hpl.jena.shared.NotFoundException;
 
 /**
  * API for granting permissions.
  * 
  * @author pejot
  * 
  */
 @Path("accesscontrol/permissions/")
 public class PermissionResource {
 
 	/** logger. */
 	private static final Logger LOGGER = Logger
 			.getLogger(PermissionResource.class);
 
 	/** URI info. */
 	@Context
 	UriInfo uriInfo;
 
 	/** Resource builder. */
 	@RequestAttribute("Builder")
 	private Builder builder;
 
 	/** Permissions dao. */
 	PermissionDAO dao = new PermissionDAO();
 
 	/** User profile doa. */
 	UserProfileDAO userProfileDAO = new UserProfileDAO();
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response addPermissions(Permission permission)
 			throws BadRequestException {
 		if (dao.findByUserROAndPermission(permission.getUser(),
 				permission.getRo(), permission.getRole()).size() > 0) {
 			throw new ConflictException("The permission was already given");
 		}
 		// Request author should be an RO owner. Otherwie Bad Request.
 		// first check permissions
 		if (!builder.getUser().getRole()
 				.equals(pl.psnc.dl.wf4ever.dl.UserMetadata.Role.ADMIN)) {
 			UserProfile userProfile = userProfileDAO.findByLogin(builder
 					.getUser().getLogin());
 			if (userProfile == null) {
 				throw new BadRequestException("There is no user like this");
 			}
 
 			List<Permission> permissions = dao.findByUserROAndPermission(
 					userProfile, permission.getRo(), Role.OWNER);
 			if (permissions.size() == 0) {
 				throw new BadRequestException(
 						"The given ro doesn't exists or doesn't belong to user");
 			} else if (permissions.size() > 1) {
 				LOGGER.error("Multiply RO ownership detected for"
 						+ permission.getRo());
 				throw new WebApplicationException(500);
 			}
 		}
 		if (permission.getUser() == null) {
 			throw new BadRequestException(
					"Given user login doesn't exist"); 
 		}
 		dao.save(permission);
 		permission.setUri(uriInfo.getRequestUri().resolve("")
 				.resolve(permission.getId().toString()));
 		return Response
 				.created(
 						uriInfo.getRequestUri().resolve("")
 								.resolve(permission.getId().toString()))
 				.type(MediaType.APPLICATION_JSON).entity(permission).build();
 	}
 
 	@Path("{permission_id}/")
 	@Produces(MediaType.APPLICATION_JSON)
 	@GET
 	public Permission getPermission(
 			@PathParam("permission_id") String permission_id) {
 		Permission result = dao.findById(Integer.valueOf(permission_id));
 		if (result != null) {
 			result.setUri(uriInfo.getRequestUri().resolve(
 					result.getId().toString()));
 		}
 		return result;
 	}
 
 	@Path("{permission_id}/")
 	@Produces(MediaType.APPLICATION_JSON)
 	@DELETE
 	public Response deletePermission(
 			@PathParam("permission_id") String permission_id)
 			throws BadRequestException {
 		Permission permission = dao.findById(Integer.valueOf(permission_id));
 		if (permission == null) {
 			throw new NotFoundException("The permission " + permission_id
 					+ " doesn't exists");
 		}
 		if (permission.getRole().equals(Role.OWNER)) {
 			throw new BadRequestException("Can't remove owner Role");
 		}
 		dao.delete(permission);
 		return Response.noContent().build();
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Permission[] getPermissions(@QueryParam("ro") String ro) {
 		List<Permission> result = dao.findByResearchObject(ro);
 		if (result == null || result.size() == 0) {
 			return new Permission[0];
 		}
 		Permission[] permissionArray = new Permission[result.size()];
 		for (int i = 0; i < result.size(); i++) {
 			permissionArray[i] = result.get(i);
 			permissionArray[i].setUri(uriInfo.getRequestUri().resolve(
 					permissionArray[i].getId().toString()));
 		}
 		return permissionArray;
 
 	}
 }
