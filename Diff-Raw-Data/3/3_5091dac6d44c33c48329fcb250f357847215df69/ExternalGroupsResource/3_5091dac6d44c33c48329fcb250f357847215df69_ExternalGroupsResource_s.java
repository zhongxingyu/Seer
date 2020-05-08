 package uk.ac.ox.oucs.vle;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Providers;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 import uk.ac.ox.oucs.vle.ExternalGroupException.Type;
 
 
 @Path("/group/")
 public class ExternalGroupsResource {
 
 	private ExternalGroupManager externalGroupManager;
 	private UserDirectoryService userDirectoryService;
 	
 	String tooManyError;
 
 	
 	static final Comparator<ExternalGroup> sorter = new Comparator<ExternalGroup>() {
 	
 		public int compare(ExternalGroup o1, ExternalGroup o2) {
 			return (o1.getName() != null)?o1.getName().compareTo(o2.getName()):-1;
 		}
 		
 	};
 	static final Comparator<ExternalGroupNode> nodeComparator = new Comparator<ExternalGroupNode>() {
 		
 		public int compare(ExternalGroupNode o1, ExternalGroupNode o2) {
 			return (o1.getName() != null)?o1.getName().compareTo(o2.getName()):-1;
 		}
 		
 	};
 	
 	static final Comparator<User> userComparator = new Comparator<User>() {
 		public int compare(User u1, User u2) {
 			// We check for null users when we put them into a list.
 			return u1.getSortName().compareTo(u2.getSortName());
 		}
 	};
 
 	public ExternalGroupsResource(@Context Providers provider) {
 		ContextResolver<Object> componentMgr = provider.getContextResolver(Object.class, null);
 		this.externalGroupManager = (ExternalGroupManager)componentMgr.getContext(ExternalGroupManager.class);
 		this.userDirectoryService = (UserDirectoryService)componentMgr.getContext(UserDirectoryService.class);
 		
 		Map<String,String> error = new HashMap<String, String>();
 		error.put("key", "499");
 		error.put("message", "Too many results found");
 		tooManyError = new JSONObject(Collections.singletonMap("error", new JSONObject(error))).toString();
 	}
 
 	@Path("{group}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@GET
 	public Response getGroup(@PathParam("group") String id) {
 		if (!loggedIn()) {
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		ExternalGroup group = externalGroupManager.findExternalGroup(id);
 		if (group != null) {
 			return Response.ok(convertGroupToMap(group)).build();
 		} else {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 	}
 
 	@Path("autocomplete")
 	@Produces(MediaType.TEXT_PLAIN)
 	@GET
 	public Response getAutocompleteGroups(@QueryParam("q") String query) {
 		if (!loggedIn()) {
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		try {
 			List<ExternalGroup>groups = externalGroupManager.search(query);
 			Collections.sort(groups, ExternalGroupsResource.sorter);
 			StringBuilder output = new StringBuilder();
 			for(ExternalGroup group: groups) {
 				output.append(group.getName());
 				output.append("\n");
 			}
 			return Response.ok(output.toString()).build();
 		} catch (ExternalGroupException ege) {
 			if (Type.SIZE_LIMIT.equals(ege.getType())) {
 				// Empty response works ok for autocomplete
 				return Response.status(Status.OK).build();
 			} else {
 				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 			}
 		}
 	}
 	
 	@Path("search")
 	@Produces(MediaType.APPLICATION_JSON)
 	@GET
 	public Response getGroups(@QueryParam("q") String query) {
 		if (!loggedIn()) {
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		try {
 			String[] terms = query.split("\\s");
 			List<ExternalGroup>groups = externalGroupManager.search(terms);
 			Collections.sort(groups, ExternalGroupsResource.sorter);
 			JSONArray groupsJson = new JSONArray();
 			for(ExternalGroup group: groups) {
 				groupsJson.put(convertGroupToMap(group));
 			}
 			return Response.ok(groupsJson).build();
 		} catch (ExternalGroupException ege) {
 			if (Type.SIZE_LIMIT.equals(ege.getType())) {
 				return Response.ok(tooManyError).build();
 			} else {
 				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 			}
 		}
 	}
 	
 	@Path("{group}/members")
 	@Produces(MediaType.APPLICATION_JSON)
 	@GET
 	public Response getMembers(@PathParam("group") String id) {
 		if (!loggedIn()) {
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		ExternalGroup group = externalGroupManager.findExternalGroup(id);
 		if (group == null) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		
 		// Put users into a list so we can sort them.
 		List<User> userList = new ArrayList<User>();
 		for (Iterator<User> userIt = group.getMembers(); userIt.hasNext();) {
 			User user = userIt.next();
 			if (user != null) {
 				userList.add(user);
 			}
 		}
 		Collections.sort(userList, userComparator);
 
 		JSONArray membersArray = new JSONArray();
 		for (User user: userList) {
 			Map<Object, Object> userObject = new HashMap<Object, Object>();
 			userObject.put("id", user.getId());
 			userObject.put("name", user.getSortName());
 			userObject.put("username", user.getDisplayId());
 			membersArray.put(new JSONObject(userObject));
 		}
 		return Response.ok(membersArray).build();
 	}
 
 	
 	/**
 	 * jstree format, should we spit it back in this format?
 	{ 
 	attributes: { id : "node_identificator", some-other-attribute : "attribute_value" }, 
 	data: "node_title", 
 	// Properties below are only used for NON-leaf nodes
 	state: "closed", // or "open"
 	children: [ an array of child nodes objects ]
 	}
 
 	 * @param path
 	 * @return
 	 */
 	@Path("browse/")
 	@Produces(MediaType.APPLICATION_JSON)
 	@GET
 	public Response getNodes(@QueryParam("id") String path) {
 		if (!loggedIn()) {
 			return Response.status(Status.UNAUTHORIZED).build();
 		}
 		try {
 			List <ExternalGroupNode> nodes = externalGroupManager.findNodes(path);
 			Collections.sort(nodes, nodeComparator);
 			
 			JSONArray nodeArray = new JSONArray();
 			for (ExternalGroupNode node: nodes) {
 				Map<Object, Object> nodeObject = new HashMap<Object, Object>();
 				Map<Object, Object> nodeAttributes = new HashMap<Object, Object>();
 				nodeAttributes.put("id", node.getPath());
 				nodeAttributes.put("title", node.getName());
 				nodeObject.put("data", node.getName());
 				if (node.hasGroup()) {
 					nodeAttributes.put("rel", "group");
 					nodeAttributes.put("groupId", node.getGroup().getId());
 				} else {
 					nodeObject.put("state", "closed");
 				}
 				nodeObject.put("attributes", nodeAttributes);
 				nodeArray.put(nodeObject);
 			};
 			return Response.ok(nodeArray).build();
 		} catch (ExternalGroupException e) {
 			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 		}
 	}
 	
 	static JSONObject convertGroupToMap(ExternalGroup externalGroup) {
 		Map<Object, Object> groupMap = new HashMap<Object, Object>();
 		groupMap.put("id", externalGroup.getId());
 		groupMap.put("name", externalGroup.getName());
 		return new JSONObject(groupMap);
 	}
 
 	private boolean loggedIn() {
 		User anonUser = userDirectoryService.getAnonymousUser();
 		User currUser = userDirectoryService.getCurrentUser();
 		return (currUser != null && !currUser.equals(anonUser));
 	}
 }
