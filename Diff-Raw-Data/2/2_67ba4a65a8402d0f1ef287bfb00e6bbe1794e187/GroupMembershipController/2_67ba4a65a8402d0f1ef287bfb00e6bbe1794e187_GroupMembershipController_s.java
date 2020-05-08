 package se.ryttargardskyrkan.rosette.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.shiro.subject.SimplePrincipalCollection;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import se.ryttargardskyrkan.rosette.exception.NotFoundException;
 import se.ryttargardskyrkan.rosette.exception.SimpleValidationException;
 import se.ryttargardskyrkan.rosette.model.*;
 import se.ryttargardskyrkan.rosette.security.MongoRealm;
 import se.ryttargardskyrkan.rosette.security.RosettePasswordService;
 
 @Controller
 public class GroupMembershipController extends AbstractController {
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	@Autowired
 	private MongoRealm mongoRealm;
 
 	@RequestMapping(value = "groupMemberships/{id}", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public GroupMembership getGroupMembership(@PathVariable String id) {
 		checkPermission("read:groupMemberships:" + id);
 
 		GroupMembership groupMembership = mongoTemplate.findById(id, GroupMembership.class);
 		if (groupMembership == null) {
 			throw new NotFoundException();
 		} else {
             List<User> users = mongoTemplate.findAll(User.class);
             List<Group> groups = mongoTemplate.findAll(Group.class);
 
             for (Group group : groups) {
                 if (group.getId().equals(groupMembership.getGroupId())) {
                     groupMembership.setGroupName(group.getName());
                     break;
                 }
             }
             for (User user : users) {
                 if (user.getId().equals(groupMembership.getUserId())) {
                     groupMembership.setUsername(user.getUsername());
                     groupMembership.setUserFullName(user.getFullName());
                     break;
                 }
             }
         }
 		return groupMembership;
 	}
 
 	@RequestMapping(value = "groupMemberships", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public List<GroupMembership> getGroupMemberships(@RequestParam(value = "groupId", defaultValue = "") String groupId, HttpServletRequest request, HttpServletResponse response) {
 		List<GroupMembership> groupMemberships = new ArrayList<GroupMembership>();
 
 		List<GroupMembership> groupMembershipsInDatabase = null;
 
         Query query = new Query();
         query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "userFullName")));
 
         if ("".equals(groupId)) {
             groupMembershipsInDatabase = mongoTemplate.find(query, GroupMembership.class);
         } else {
             groupMembershipsInDatabase = mongoTemplate.find(query.addCriteria(Criteria.where("groupId").is(groupId)), GroupMembership.class);
         }
 
 		if (groupMembershipsInDatabase != null) {
 
             List<User> users = mongoTemplate.findAll(User.class);
             List<Group> groups = mongoTemplate.findAll(Group.class);
 
 			for (GroupMembership groupMembershipInDatabase : groupMembershipsInDatabase) {
 				if (isPermitted("read:groupMemberships:" + groupMembershipInDatabase.getId())) {
                     for (Group group : groups) {
                         if (group.getId().equals(groupMembershipInDatabase.getGroupId())) {
                             groupMembershipInDatabase.setGroupName(group.getName());
                             break;
                         }
                     }
                     for (User user : users) {
                         if (user.getId().equals(groupMembershipInDatabase.getUserId())) {
                             groupMembershipInDatabase.setUsername(user.getUsername());
                             groupMembershipInDatabase.setUserFullName(user.getFullName());
                             break;
                         }
                     }
 
 					groupMemberships.add(groupMembershipInDatabase);
 				}
 			}
 		}
 
 		return groupMemberships;
 	}
 
 	@RequestMapping(value = "groupMemberships", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
 	@ResponseBody
 	public GroupMembership postGroupMembership(@RequestBody GroupMembership groupMembership, HttpServletResponse response) {
 		checkPermission("create:groupMemberships");
 		validate(groupMembership);
 
         long count = mongoTemplate.count(Query.query(Criteria.where("userId").is(groupMembership.getUserId()).and("groupId").is(groupMembership.getGroupId())), GroupMembership.class);
         if (count > 0) {
             throw new SimpleValidationException(new ValidationError("groupMembership", "groupMembership.alreadyExists"));
         } else {
             mongoTemplate.insert(groupMembership);
 
             response.setStatus(HttpStatus.CREATED.value());
 
             // Clearing auth cache
             mongoRealm.clearCache(null);
         }
 
 		return groupMembership;
 	}
 
     @RequestMapping(value = "groupMemberships/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
     public void putGroupMembership(@PathVariable String id, @RequestBody GroupMembership groupMembership, HttpServletResponse response) {
         checkPermission("update:groupMemberships:" + id);
         validate(groupMembership);
 
         long count = mongoTemplate.count(Query.query(Criteria.where("userId").is(groupMembership.getUserId()).and("groupId").is(groupMembership.getGroupId())), GroupMembership.class);
         if (count > 0) {
             throw new SimpleValidationException(new ValidationError("groupMembership", "groupMembership.alreadyExists"));
         } else {
             Update update = new Update();
             if (groupMembership.getGroupId() != null)
                 update.set("groupId", groupMembership.getGroupId());
             if (groupMembership.getUserId() != null)
                 update.set("userId", groupMembership.getUserId());
 
             if (mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(id)), update, GroupMembership.class).getN() == 0) {
                 throw new NotFoundException();
             }
 
             response.setStatus(HttpStatus.OK.value());
 
             // Clearing auth cache
             mongoRealm.clearCache(null);
         }
     }
 
 	@RequestMapping(value = "groupMemberships/{id}", method = RequestMethod.DELETE, produces = "application/json")
 	public void deleteGroupMembership(@PathVariable String id, HttpServletResponse response) {
 		checkPermission("delete:groupMemberships:" + id);
 
 		GroupMembership deletedGroupMembership = mongoTemplate.findAndRemove(Query.query(Criteria.where("id").is(id)), GroupMembership.class);
 		if (deletedGroupMembership == null) {
 			throw new NotFoundException();
 		} else {
 			response.setStatus(HttpStatus.OK.value());
 
             // Clearing auth cache
            mongoRealm.clearCache(new SimplePrincipalCollection(id, "mongoRealm"));
 		}
 	}
 }
