 package net.thumbtack.updateNotifierBackend.resourceHandlers;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.inject.Singleton;
 import javax.ws.rs.BadRequestException;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.NotFoundException;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 import org.eclipse.jetty.http.HttpStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonSyntaxException;
 
 import static net.thumbtack.updateNotifierBackend.UpdateNotifierBackend.getDatabaseService;
 import net.thumbtack.updateNotifierBackend.database.entities.Resource;
 import net.thumbtack.updateNotifierBackend.database.entities.Tag;
 import net.thumbtack.updateNotifierBackend.database.entities.User;
 import net.thumbtack.updateNotifierBackend.database.exceptions.DatabaseException;
 import net.thumbtack.updateNotifierBackend.database.exceptions.DatabaseSeriousException;
 import net.thumbtack.updateNotifierBackend.database.exceptions.DatabaseTinyException;
 import net.thumbtack.updateNotifierBackend.updateChecker.UpdateChecker;
 
 @Path("/users")
 @Singleton
 public class UsersHandler {
 
 	// TODO Why do not I use this and create new gSon object every time?
 	private static final Gson GSON = new GsonBuilder()
 			.setDateFormat("yyyy-MM-dd hh:mm:ss.S")
 			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
 			.create();
 	private static final Logger log = LoggerFactory
 			.getLogger(UsersHandler.class);
 
 	@Path("signin")
 	@GET
 	public long signIn(@QueryParam("email") String userEmail) {
 		log.trace("Sign in: " + userEmail);
 		if (userEmail == null) {
 			throw new BadRequestException(
 					"Missing 'email' parameter in the url");
 		}
 		long userId = 0;
 		try {
 			User user = new User();
 			user.setEmail(userEmail);
 			userId = getDatabaseService().getUserIdByEmailOrAdd(user);
 		} catch (DatabaseSeriousException e) {
 			log.error("Database request failed. Sign in failed");
 			throw (new WebApplicationException("Database get account error"));
 		}
 		return userId;
 	}
 
 	@Path("/{id}/resources")
 	@POST
 	@Consumes({ "application/json" })
 	public Response addUserResource(@PathParam("id") long userId,
 			String resourceJson) {
 		log.trace("Add resource");
 		Resource resource = parseResource(resourceJson);
 		if (resource == null) {
 			log.debug("Database add request failed: resource expected in request body");
 			throw new BadRequestException(
 					"Database add request failed: resource expected in request body");
 		}
 		try {
 			resource.setUserId(userId);
 			getDatabaseService().addResource(resource);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database add request failed. Add resources bad request");
 			throw (new BadRequestException("Incorrect params"));
 		} catch (DatabaseSeriousException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return Response.status(HttpStatus.CREATED_201)
 				.entity(resource.getId().toString()).build();
 	}
 
 	@Path("/{id}/resources")
 	@GET
 	@Produces({ "application/json" })
 	public String getUserResources(@PathParam("id") long userId,
 			@DefaultValue("") @QueryParam("tags") String tagsString) {
 		log.trace("Get resources");
 		List<Long> tags = parseTags(tagsString);
 		List<Resource> resources = null;
 		try {
 			resources = getDatabaseService().getResourcesByIdAndTags(userId,
 					tags);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database get request failed. Get resources bad request");
 			throw (new BadRequestException("Incorrect userId"));
 		}
 		return GSON.toJson(resources);
 	}
 
 	@Path("/{id}/resources/{resourceId}")
 	@GET
 	@Produces({ "application/json" })
 	public String getUserResource(@PathParam("id") long userId,
 			@PathParam("resourceId") long resourceId) {
 		log.trace("Get resource");
 		Resource resource = null;
 		try {
 			resource = getDatabaseService().getResource(userId, resourceId);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database get request failed. User not found");
 			throw (new NotFoundException("Invalid user id"));
 		}
 		if (resource == null) {
 			log.debug("Database get request failed. Get resource not found");
 			throw (new NotFoundException("Resource not exist"));
 		}
 		return GSON.toJson(resource);
 	}
 
 	@Path("/{id}/resources")
 	@DELETE
 	public Response deleteUserResources(@PathParam("id") long userId,
 			@DefaultValue("") @QueryParam("tags") String tagsString) {
 		log.trace("Delete resources");
 		List<Long> tags = parseTags(tagsString);
 		try {
 			getDatabaseService().deleteResourcesByIdAndTags(userId, tags);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database delete request failed. Incorrect params");
 			throw (new NotFoundException("Incorrect params"));
 		} catch (DatabaseSeriousException e) {
 			log.error("Database delete request failed. Failed");
 			throw (new NotFoundException("Deletion failed"));
 		}
 		return Response.status(HttpStatus.NO_CONTENT_204).build();
 	}
 
 	@Path("/{id}/resources/{resourceId}")
 	@DELETE
 	@Produces({ "application/json" })
 	public Response deleteUserResource(@PathParam("id") long userId,
 			@PathParam("resourceId") long resourceId) {
 		log.trace("Get resource");
 
 		try {
 			Resource resource = new Resource();
 			resource.setId(resourceId);
 			resource.setUserId(userId);
 			getDatabaseService().deleteResource(resource);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database delete request failed. Delete resource not found");
 			throw (new NotFoundException("Resource not exist"));
 		} catch (DatabaseSeriousException e) {
 			log.error("Database delete request failed. Delete resource not found");
 			throw (new NotFoundException("Resource not exist"));
 		}
 		return Response.status(HttpStatus.NO_CONTENT_204).build();
 	}
 
 	@Path("/{id}/resources/{resourceId}")
 	@PUT
 	@Consumes({ "application/json" })
 	public void editUserResource(@PathParam("id") long userId,
 			@PathParam("resourceId") long resourceId, String resourceJson) {
 		log.trace("Edit resource");
 		Resource resource = parseResource(resourceJson);
 		resource.setId(resourceId);
 		try {
 			getDatabaseService().editResource(resource);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database edit request failed. Edit resources bad request");
 			throw (new BadRequestException());
 		} catch (DatabaseSeriousException e) {
 			log.debug("Database edit request failed. Failed");
 			throw (new BadRequestException());
 		}
 
 	}
 
 	@Path("/{id}/tags")
 	@GET
 	@Produces({ "application/json" })
 	public String getUserTags(@PathParam("id") long userId) {
 		log.trace("Get tags");
 		Set<Tag> tags;
 		try {
 			tags = getDatabaseService().getTags(userId);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database get request failed. User not found");
 			throw (new NotFoundException());
 		}
 		if (tags == null) {
 			log.debug("Database get request failed. Get tags not found");
 			throw (new NotFoundException());
 		}
 		return GSON.toJson(tags);
 	}
 
 	@Path("/{id}/tags")
 	@POST
 	@Consumes({ "application/json" })
 	public Response addTag(@PathParam("id") long userId, String tagNameJson) {
 		log.trace("Add tag");
 		String tagName = parseTagName(tagNameJson);
 		Tag tag = new Tag(null, userId, tagName);
 		try {
 			getDatabaseService().addTag(tag);
 		} catch (DatabaseTinyException e) {
 			log.debug("Database add request failed. User not found");
 			throw (new BadRequestException());
 		} catch (DatabaseSeriousException e) {
 			log.debug("Database add request failed. Failed");
 			throw (new BadRequestException());
 		}
 		Long id = tag.getId();
 		if (id == null) {
 			log.debug("Database add request failed. Edit resources bad request");
 			throw (new BadRequestException());
 		}
 		return Response.status(HttpStatus.CREATED_201).entity(id.toString())
 				.build();
 	}
 
 	@Path("/{id}/tags/{tagId}")
 	@PUT
 	@Consumes({ "application/json" })
 	public void editTag(@PathParam("id") long userId,
 			@PathParam("tagId") long tagId, String tagNameJson) {
 		log.trace("Edit tag");
 		String tagName = null;
 		try {
 			tagName = GSON.fromJson(tagNameJson, String.class);
 		} catch (JsonSyntaxException e) {
 			throw new BadRequestException("Invalid json param: tag name");
 		}
 		if (tagName == null) {
 			log.debug("Database put request failed. Tag name can't be null.");
 			throw new BadRequestException(
 					"Database put request failed. Tag name can't be null.");
 		}
 		try {
 			getDatabaseService().editTag(new Tag(tagId, userId, tagName));
 		} catch (DatabaseTinyException e) {
 			log.debug("Database add request failed. Edit resources bad request");
 			throw (new BadRequestException());
 		} catch (DatabaseSeriousException e) {
 			log.debug("Database add request failed. Failed");
 			throw (new BadRequestException());
 		}
 	}
 
 	@Path("/{id}/tags/{tag_id}")
 	@DELETE
 	public Response deleteTag(@PathParam("id") long userId,
 			@PathParam("tag_id") long tagId) {
 		log.trace("Delete tag; user id: {}, tag id: {}", userId, tagId);
 		if ((userId <= 0) || (tagId <= 0)) {
 			log.debug("Database delete request failed. Invalid user or tag id");
 			throw new BadRequestException(
 					"Database delete request failed. Invalid user or tag id");
 		}
 		try {
 			getDatabaseService().deleteTag(new Tag(tagId, userId, null));
 		} catch (DatabaseException e) {
 			log.debug("Database delete request failed. Invalid user or tag id");
 			throw new BadRequestException(
 					"Database delete request failed. Invalid user or tag id");
 		}
 		return Response.status(HttpStatus.NO_CONTENT_204).build();
 	}
 
 	private static Resource parseResource(String resourceJson) {
 		try {
 			Resource res = GSON.fromJson(resourceJson, Resource.class);
 			if (res == null || res.getDomPath() == null
 					|| res.getSheduleCode() < 0
 					|| res.getSheduleCode() > UpdateChecker.MAGIC_NUMBER
 					|| res.getUrl() == null) {
 				log.debug("Resource parsing error: bad or expecting params");
 				throw (new BadRequestException("Json parsing error"));
 			}
 			return res;
 		} catch (JsonSyntaxException ex) {
 			log.debug("Resource parsing error");
 			throw (new BadRequestException("Json parsing error"));
 		}
 	}
 
 	private String parseTagName(String tagNameJson) {
 		try {
 			String tagName = GSON.fromJson(tagNameJson, String.class);
 			if (tagName == null) {
 				log.debug("Database post request failed. Tag name can't be null.");
 				throw new BadRequestException(
 						"Database post request failed. Tag name can't be null.");
 			}
 			return tagName;
 		} catch (JsonSyntaxException ex) {
 			log.debug("Tag name parsing error");
 			throw (new BadRequestException("Tag name parsing error"));
 		}
 	}
 
 	private static List<Long> parseTags(String tagsString) {
 		List<Long> tags;
 		if ("".equals(tagsString) || tagsString == null) {
 			tags = null;
 		} else {
 			String[] tagsStrings = tagsString.split(",");
 			tags = new LinkedList<Long>();
 			try {
 				for (int i = 0; i < tagsStrings.length; i += 1) {
 					tags.add(Long.parseLong(tagsStrings[i]));
 				}
 			} catch (NumberFormatException ex) {
 				log.debug("Tags id parsing error");
 				throw (new BadRequestException("Tags id parsing error"));
 			}
 		}
 		return tags;
 	}
 }
