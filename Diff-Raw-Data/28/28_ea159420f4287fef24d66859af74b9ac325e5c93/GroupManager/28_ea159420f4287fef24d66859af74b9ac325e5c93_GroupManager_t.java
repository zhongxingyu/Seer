 package com.vacuumhead.wesplit.rest;
 
 import com.vacuumhead.wesplit.application.IGroupApplicationService;
 import com.vacuumhead.wesplit.tables.Group;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pratyushverma
  * Date: 03/03/13
  * Time: 2:27 AM
  * To change this template use File | Settings | File Templates.
  */
 
 @Controller
 @RequestMapping("/group")
 public class GroupManager {
 
     @Autowired
     private IGroupApplicationService groupApplicationService;
 
     public void setGroupApplicationService(IGroupApplicationService groupApplicationService) {
         this.groupApplicationService = groupApplicationService;
     }
 
     @RequestMapping(value = "/createGroup/{groupname}/{user}", method = RequestMethod.GET)
     public
     @ResponseBody
     ResponseEntity<String> createGroup(@PathVariable("groupname") String groupName, @PathVariable("user") String _user, HttpServletRequest request) {
 
         Integer accountId = Integer.parseInt(_user);
         Group group = groupApplicationService.createGroup(groupName, accountId);
 
         return new ResponseEntity<String>(group.toString(),
                 new HttpHeaders(), HttpStatus.OK);
 
     }
 
     @RequestMapping(value = "/getGroupByName/{groupname}", method = RequestMethod.GET)
     public
     @ResponseBody
     ResponseEntity<String> getGroupByName(@PathVariable("groupname") String _groupName, HttpServletRequest request) {
 
         Group group = groupApplicationService.retrieveGroupByName(_groupName);
 
         return new ResponseEntity<String>(group.toString(),
                 new HttpHeaders(), HttpStatus.OK);
 
     }
 
     @RequestMapping(value = "/getGroupById/{groupId}", method = RequestMethod.GET)
     public
     @ResponseBody
     ResponseEntity<String> getGroupById(@PathVariable("groupId") String _groupId, HttpServletRequest request) {
 
         Integer groupId = Integer.parseInt(_groupId);
         Group group = groupApplicationService.retrieveGroupById(groupId);
 
         return new ResponseEntity<String>(group.toString(),
                 new HttpHeaders(), HttpStatus.OK);
 
     }
 
     @RequestMapping(value = "/addMembersToGroup/{groupId}/{userId}", method = RequestMethod.GET)
     public
     @ResponseBody
     ResponseEntity<String> addMembersToGroup(@PathVariable("groupId") String _groupId, @PathVariable("userId") String _userId, HttpServletRequest request) {
 
         Integer groupId = Integer.parseInt(_groupId);
         Integer accountId = Integer.parseInt(_userId);
         Group group = groupApplicationService.addMembersToGroup(groupId, accountId);
 
         return new ResponseEntity<String>(group.toString(),
                 new HttpHeaders(), HttpStatus.OK);
 
     }
 
     @RequestMapping(value = "/addAdminToGroup/{groupId}/{userId}", method = RequestMethod.GET)
     public
     @ResponseBody
     ResponseEntity<String> addAdminToGroup(@PathVariable("groupId") String _groupId, @PathVariable("userId") String _userId, HttpServletRequest request) {
 
         Integer groupId = Integer.parseInt(_groupId);
         Integer accountId = Integer.parseInt(_userId);
         Group group = groupApplicationService.addAdminToGroup(groupId, accountId);
 
         return new ResponseEntity<String>(group.toString(),
                 new HttpHeaders(), HttpStatus.OK);
 
     }
 
     @RequestMapping(value = "/isAdmin/{groupId}/{userId}", method = RequestMethod.GET)
     public
     @ResponseBody
     ResponseEntity<String> isAdmin(@PathVariable("groupId") String _groupId, @PathVariable("userId") String _userId, HttpServletRequest request) {
 
         Integer groupId = Integer.parseInt(_groupId);
         Integer accountId = Integer.parseInt(_userId);
        boolean isAdmin = groupApplicationService.isAdminOfGroup(groupId, accountId);
 
         return new ResponseEntity<String>(isAdmin ? "true" : "false",
                 new HttpHeaders(), HttpStatus.OK);
 
     }
 }
