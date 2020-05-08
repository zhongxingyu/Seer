 package uk.ac.ox.oucs.vle;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ContextResolver;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.sakaiproject.authz.api.GroupProvider;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 
 @Path("/mapped/")
 public class MappedGroupsResource {
 	
 	private final static Log log = LogFactory.getLog(MappedGroupsResource.class);
 	
 	private ExternalGroupManager externalGroupManager;
 	private GroupProvider groupProvider;
 	private SiteService siteService;
 
 	public MappedGroupsResource(@Context ContextResolver<Object> resolver) {
 		externalGroupManager = (ExternalGroupManager)resolver.getContext(ExternalGroupManager.class);
 		groupProvider = (GroupProvider)resolver.getContext(GroupProvider.class);
 		siteService = (SiteService)resolver.getContext(SiteService.class);
 	}
 	
 
 	public Response addMappedGroup(@FormParam("group") String group, @FormParam("role") String role) {
 		// TODO Should split it out.
 		ValidationErrors errors = validateMappedGroup(group, role);
 		
 		if (errors.hasErrors()) {
 			return Response.status(Status.BAD_REQUEST).entity(convertErrors(errors.getAllErrors())).build();
 		} else {
 			try {
 				String id = externalGroupManager.addMappedGroup(group,role);
 				return Response.ok(new JSONObject(Collections.singletonMap("id", id))).build();
 			} catch (IllegalArgumentException iae) {
 				return Response.status(Status.BAD_REQUEST).entity(Collections.singletonList(iae.getMessage())).build();
 			}
 		}
 	}
 
 	@Produces(MediaType.APPLICATION_JSON)
 	@POST
 	public Response addMappedGroupToSite(@FormParam("group") String group, @FormParam("role") String role, @FormParam("site") String siteId) {
 		// This is a bit poo as now we have dependencies on 3 services.
 		ValidationErrors errors = validateMappedGroup(group, role);
 
 		if (errors.hasErrors()) {
 			return Response.status(Status.BAD_REQUEST).entity(convertErrors(errors.getAllErrors())).build();
 		}else {
 			try {
 				Site site = siteService.getSite(siteId);
 				String newId = externalGroupManager.addMappedGroup(group,role);
 				String existingGroupIds = site.getProviderGroupId();
 				List<String> splitNewGroupIds;
 
 				if (existingGroupIds != null) {
 					String[] splitGroupIds = groupProvider.unpackId(existingGroupIds);
 					splitNewGroupIds = new ArrayList<String>(splitGroupIds.length +1);
 					for (String id: splitGroupIds) {
 						if (!newId.equals(id)) {
 							splitNewGroupIds.add(id);
 						}
 					}
 					splitNewGroupIds.add(newId);
 				} else {
 					splitNewGroupIds = Collections.singletonList(newId);
 				}
 				String providedId = groupProvider.packId(splitNewGroupIds.toArray(new String[]{}));
 				site.setProviderGroupId(providedId);
 				if (log.isDebugEnabled()) {
 					log.debug("Set site : "+ site.getId()+ " provided id to: "+ providedId);
 				}
 				siteService.saveSiteMembership(site);
 				
 				return Response.ok().build();
 			} catch (Exception e) {
 				log.warn("Failed to add group.", e);
 				return Response.status(Status.BAD_REQUEST).entity(new JSONArray(Collections.singletonList(e.getMessage()))).build();
 			}
 		}
 	}
 	
 	
 	@Path("{group}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@GET
 	public Response getMappedGroup(@PathParam("group") String group) {
 		String externalGroupId = externalGroupManager.findExternalGroupId(group);
 		Map<Object,Object> jsonMap = new HashMap<Object, Object>();
 		jsonMap.put("id", group);
 		jsonMap.put("group", externalGroupId);
		return Response.ok(jsonMap).build();
 	}
 
 
 	private JSONObject convertErrors(List<String> allErrors) {
 		return new JSONObject(Collections.singletonMap("errors", new JSONArray(allErrors)));
 	}
 
 	private ValidationErrors validateMappedGroup(String group, String role) {
 		ValidationErrors errors = new ValidationErrors();
 		if (group == null || group.length() == 0) {
 			errors.addError("group", "Group cannot be empty");
 		}
 		if (role == null || role.length() == 0) {
 			errors.addError("role", "Role cannot be empty");
 		}
 		return errors;
 	}
 	
 
 }
