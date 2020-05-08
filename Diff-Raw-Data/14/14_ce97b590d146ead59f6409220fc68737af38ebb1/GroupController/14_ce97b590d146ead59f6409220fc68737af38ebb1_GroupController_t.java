 package se.ryttargardskyrkan.rosette.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Order;
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
 import se.ryttargardskyrkan.rosette.model.GroupMembership;
 import se.ryttargardskyrkan.rosette.model.Permission;
 import se.ryttargardskyrkan.rosette.model.User;
 import se.ryttargardskyrkan.rosette.security.MongoRealm;
 
 @Controller
 public class GroupController extends AbstractController {
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	@Autowired
 	private MongoRealm mongoRealm;
 
 	@RequestMapping(value = "groups/{id}", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public Group getGroup(@PathVariable String id) {
 		checkPermission("read:groups:" + id);
 		
 		Group group = mongoTemplate.findById(id, Group.class);
 		if (group == null) {
 			throw new NotFoundException();
 		}
 		return group;
 	}
 
 	@RequestMapping(value = "groups", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public List<Group> getGroups(HttpServletResponse response) {
         Query query = new Query();
        query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "name")));
 
 		List<Group> groupsInDatabase = mongoTemplate.find(query, Group.class);
 		List<Group> groups = new ArrayList<Group>();
 		if (groupsInDatabase != null) {
 			for (Group groupInDatabase : groupsInDatabase) {
 				if (isPermitted("read:groups:" + groupInDatabase.getId())) {
 					groups.add(groupInDatabase);
 				}
 			}
 		}
 
 		return groups;
 	}
 
 	@RequestMapping(value = "groups", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
 	@ResponseBody
 	public Group postGroup(@RequestBody Group group, HttpServletResponse response) {
 		checkPermission("create:groups");
 		validate(group);
 		
 		mongoTemplate.insert(group);
 		
 		response.setStatus(HttpStatus.CREATED.value());
 		return group;
 	}
 
 	@RequestMapping(value = "groups/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
 	public void putGroup(@PathVariable String id, @RequestBody Group group, HttpServletResponse response) {
 		checkPermission("update:groups:" + id);
 		validate(group);
 
 		Update update = new Update();
 		if (group.getName() != null)
 			update.set("name", group.getName());
 		update.set("description", group.getDescription());
 		
 		if (mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(id)), update, Group.class).getN() == 0) {
 			throw new NotFoundException();
 		}
 		
 		// Updating groupName in permissions
 		Group groupInDatabase = mongoTemplate.findById(id, Group.class);
 		Update permissionUpdate = new Update();
 		permissionUpdate.set("groupName", groupInDatabase.getName());
 		mongoTemplate.updateMulti(Query.query(Criteria.where("groupId").is(id)), permissionUpdate, Permission.class);
 
 		response.setStatus(HttpStatus.OK.value());
 	}
 
 	@RequestMapping(value = "groups/{id}", method = RequestMethod.DELETE, produces = "application/json")
 	public void deleteGroup(@PathVariable String id, HttpServletResponse response) {
 		checkPermission("delete:groups:" + id);
 
 		Group group = mongoTemplate.findById(id, Group.class);
 		if (group == null) {
 			throw new NotFoundException();
 		} else {
 			// Removing permissions for the group
 			mongoTemplate.findAndRemove(Query.query(Criteria.where("groupId").is(id)), Permission.class);
 			
 			// Removing group memberships with the group that is about to be deleted
 			mongoTemplate.findAndRemove(Query.query(Criteria.where("groupId").is(id)), GroupMembership.class);
 			
 			// Removing the group
 			Group deletedGroup = mongoTemplate.findAndRemove(Query.query(Criteria.where("id").is(id)), Group.class);
 			if (deletedGroup == null) {
 				throw new NotFoundException();
 			} else {
 				response.setStatus(HttpStatus.OK.value());
 			}
 			
 			// Clearing auth cache
 			mongoRealm.clearCache(null);
 		}
 	}
 }
