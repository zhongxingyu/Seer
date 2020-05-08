 package se.ryttargardskyrkan.rosette.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import se.ryttargardskyrkan.rosette.exception.NotFoundException;
 import se.ryttargardskyrkan.rosette.model.Group;
 import se.ryttargardskyrkan.rosette.model.Permission;
 import se.ryttargardskyrkan.rosette.model.User;
 import se.ryttargardskyrkan.rosette.security.MongoRealm;
 
 @Controller
 public class PermissionController extends AbstractController {
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	@Autowired
 	private MongoRealm mongoRealm;
 	
 	@RequestMapping(value = "permissions/{id}", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public Permission getPermission(@PathVariable String id) {
 		checkPermission("read:permissions");
 		
 		Permission permission = mongoTemplate.findById(id, Permission.class);
 		if (permission == null) {
 			throw new NotFoundException();
 		}
 		return permission;
 	}
 
 	@RequestMapping(value = "permissions", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public List<Permission> getPermissions(HttpServletResponse response) {
		List<Permission> permissionsInDatabase = mongoTemplate.findAll(Permission.class);
 		List<Permission> permissions = new ArrayList<Permission>();
 		if (permissionsInDatabase != null) {
 			for (Permission permissionInDatabase : permissionsInDatabase) {
 				if (isPermitted("read:permissions:" + permissionInDatabase.getId())) {
 					permissions.add(permissionInDatabase);
 				}
 			}
 		}
 
 		return permissions;
 	}
 
 	@RequestMapping(value = "permissions", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
 	@ResponseBody
 	public Permission postPermission(@RequestBody Permission permission, HttpServletResponse response) {
 		checkPermission("create:permissions");
 		validate(permission);
 
 		// Setting groupName and userFullname
 		if (permission.getGroupId() != null) {
 			Group group = mongoTemplate.findById(permission.getGroupId(), Group.class);
 			if (group != null) {
 				permission.setGroupName(group.getName());
 			}
 		} else if (permission.getUserId() != null) {
 			User user = mongoTemplate.findById(permission.getUserId(), User.class);
 			if (user != null) {
 				permission.setUserFullName(user.getFullName());
 			}
 		}
 		
 		mongoTemplate.insert(permission);
 		
 		// Clearing auth cache
 		mongoRealm.clearCache(null);
 
 		response.setStatus(HttpStatus.CREATED.value());
 		return permission;
 	}
 
 	@RequestMapping(value = "permissions/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
 	public void putPermission(@PathVariable String id, @RequestBody Permission permission, HttpServletResponse response) {
 		checkPermission("update:permissions:" + id);
 		validate(permission);
 
 		Update update = new Update();
 		if (permission.getPatterns() != null)
 			update.set("patterns", permission.getPatterns());
 
 		if (mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(id)), update, Permission.class).getN() == 0) {
 			throw new NotFoundException();
 		}
 		
 		// Clearing auth cache
 		mongoRealm.clearCache(null);
 
 		response.setStatus(HttpStatus.OK.value());
 	}
 
 	@RequestMapping(value = "permissions/{id}", method = RequestMethod.DELETE, produces = "application/json")
 	public void deletePermission(@PathVariable String id, HttpServletResponse response) {
 		checkPermission("delete:permissions:" + id);
 
 		Permission deletedPermission = mongoTemplate.findAndRemove(Query.query(Criteria.where("id").is(id)), Permission.class);
 		if (deletedPermission == null) {
 			throw new NotFoundException();
 		} else {
 			// Clearing auth cache
 			mongoRealm.clearCache(null);
 			
 			response.setStatus(HttpStatus.OK.value());
 		}
 	}
 }
