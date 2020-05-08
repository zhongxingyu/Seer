 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import models.Group;
 import models.GroupUserMap;
 import models.User;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.Logger.ALogger;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.html.groups.createForm;
 import views.html.groups.editForm;
 import views.html.groups.list;
 
 /**
  * Manage a database of groups
  */
 @Security.Authenticated(Secured.class)
 public class GroupApplication extends Controller {
 
 	static ALogger logger = play.Logger.of(GroupApplication.class);
 
 	/**
 	 * This result directly redirect to application home.
 	 */
 	public static Result GO_HOME = redirect(routes.GroupApplication.list(0,
 			"name", "asc", ""));
 
 	/**
 	 * Handle default path requests, redirect to groups list
 	 */
 	public static Result index() {
 		return GO_HOME;
 	}
 
 	/**
 	 * Display the paginated list of groups.
 	 * 
 	 * @param page
 	 *            Current page number (starts from 0)
 	 * @param sortBy
 	 *            Column to be sorted
 	 * @param order
 	 *            Sort order (either asc or desc)
 	 * @param filter
 	 *            Filter applied on group names
 	 */
 	public static Result list(int page, String sortBy, String order,
 			String filter) {
 		
 		Long userId = new Long(session().get("userId"));
 		
 		return ok(list.render(Group.page(page, 10, sortBy, order, filter),
 				sortBy, order, filter));
 	}
 	
 	public static Result listByAccess(int page, String sortBy, String order) {
 		
 		Long userId = new Long(session().get("userId"));
 		
 		return ok(list.render(Group.page(page, 10, sortBy, order, userId),
 				sortBy, order, ""));
 	}
 
 	/**
 	 * Display the 'edit form' of a existing Group.
 	 * 
 	 * @param id
 	 *            Id of the group to edit
 	 */
 	public static Result edit(Long id) {
 
 		Form<Group> groupForm = form(Group.class).fill(Group.find.byId(id));
 		return ok(editForm.render(id, groupForm));
 	}
 
 	/**
 	 * Handle the 'edit form' submission
 	 * 
 	 * @param id
 	 *            Id of the group to edit
 	 */
 	public static Result update(Long id) {
 		
 		Form<Group> groupForm = form(Group.class).bindFromRequest();
 		if (groupForm.hasErrors()) {
 			return badRequest(editForm.render(id, groupForm));
 		}
 		groupForm.get().update(id);
 		flash("success", "Group " + groupForm.get().name + " has been updated");
 		return GO_HOME;
 	}
 	
 	public static Result addMember(Long id) {
 		
 		Map<String, String[]> formUrlEncoded = request().body().asFormUrlEncoded();
 		// iterate through the keys to find values and pre-fill required Set(s)
 		
 		User member = null;
 		
 		for (String key : formUrlEncoded.keySet()) {
 	        String[] values = formUrlEncoded.get(key);
 	        for (String val : values) {
 	            if (!val.equals("") && "member.id".equals(key)){
 	            	member = User.find.byId(new Long(val));
 	            }
 	        }
 	    }
 		
 		GroupUserMap groupUser = new GroupUserMap();
 		groupUser.user = member;
 		
 		
 		Group group = Group.find.byId(id);
 		
 		groupUser.group = group;
 		
 		groupUser.save();
 		
 		Form<Group> groupForm = form(Group.class).fill(Group.find.byId(id));
 		return redirect(routes.GroupApplication.edit(id));
 	}
 	
	
 	public static Result removeMember(Long id, Long memberId) {
 		
 		GroupUserMap groupUser = GroupUserMap.findByGroupIdAndUserId(id, memberId);
 		groupUser.delete();
 		
 		Form<Group> groupForm = form(Group.class).fill(Group.find.byId(id));
 		return redirect(routes.GroupApplication.edit(id));
 	}
 	
 	public static Result apiUpdateMember(Long id, Long memberId) {
 		
 		JsonNode jn = request().body().asJson();
 		
 		Boolean isAdmin = jn.findPath("admin").asBoolean();
 		
 
 		GroupUserMap groupUser = GroupUserMap.findByGroupIdAndUserId(id, memberId);
 		
 		groupUser.admin = isAdmin;
 		groupUser.update();
 		
 		return ok();
 	}
 	
 	public static Result apiGetMember(Long id, Long memberId) {
 		
 		GroupUserMap gum = GroupUserMap.findByGroupIdAndUserId(id, memberId);
 		gum.group.members = null;
 		gum.user.credential = null;
 		
 		logger.debug(gum.group.members.toString());
 		
 		return ok(Json.toJson(gum));
 	}
 
 	/**
 	 * Display the 'new group form'.
 	 */
 	public static Result create() {
 		Form<Group> groupForm = form(Group.class);
 		return ok(createForm.render(groupForm));
 	}
 
 	/**
 	 * Handle the 'new group form' submission
 	 */
 	public static Result save() {
 		Form<Group> groupForm = form(Group.class).bindFromRequest();
 		if (groupForm.hasErrors()) {
 			return badRequest(createForm.render(groupForm));
 		}
 		groupForm.get().save();
		
		GroupUserMap groupUser = new GroupUserMap();
		groupUser.user = User.findById(new Long(session().get("userId")));
		groupUser.group = groupForm.get();
		groupUser.admin = true;
		groupUser.save();
		
 		flash("success", "Group " + groupForm.get().name + " has been created");
 		return GO_HOME;
 	}
 
 	/**
 	 * Handle group deletion
 	 */
 	public static Result delete(Long id) {
 		Group.find.ref(id).delete();
 		flash("success", "Group has been deleted");
 		return GO_HOME;
 	}
 	
 	public static List<Long> groupAdminAccess(Long userId) {
 		
 		List<Long> groupIds = new ArrayList<Long>();
 		
 		List<GroupUserMap> gums = GroupUserMap.groupsWithAdmin(userId);
 		
 		for(GroupUserMap gum : gums){
 			groupIds.add(gum.group.id);
 		}
 		
 		return groupIds;
 	}
 
 }
