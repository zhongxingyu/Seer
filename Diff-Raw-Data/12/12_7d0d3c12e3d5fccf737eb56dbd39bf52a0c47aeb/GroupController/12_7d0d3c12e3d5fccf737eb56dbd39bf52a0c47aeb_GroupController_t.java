 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.uhsarp.billrive.webservices.rest;
 
 import com.uhsarp.billrive.domain.Group;
 import com.uhsarp.billrive.services.GroupService;
 import java.util.List;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 
 /**
  *
  * @author uhsarp
  */
 @Controller
 public class GroupController extends GenericController {
     
     	@Autowired
 	private GroupService groupService;
 
 	@Autowired
 	private View jsonView_i;
         
         
         private static final String DATA_FIELD = "data";
 	private static final String ERROR_FIELD = "error";
         private static final Logger logger_c = Logger.getLogger(GroupController.class);
         
         
     	@RequestMapping(value = "/rest/{userId}/groups/", method = RequestMethod.GET)
 	public ModelAndView getGroups(@PathVariable("userId") int userId) {
 		List<Group> groups = null;
                 System.out.println("In Groups...Calling getGroups");
 		try {
 			groups = groupService.getGroups(userId);
 		} catch (Exception e) {
 			String sMessage = "Error getting all groups. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		logger_c.debug("Returing Groups: " + groups.toString());
 		return new ModelAndView(jsonView_i, DATA_FIELD, groups);
 	}
         
         
         	@RequestMapping(value = "/rest/{userId}/{userId}/groups/{groupId}", method = RequestMethod.GET)
 	public ModelAndView getGroup(@PathVariable("groupId") String groupId_p,@PathVariable("userId") int userId) {
 		Group group = null;
 
 		
 		if (isEmpty(groupId_p) || groupId_p.length() < 5) {
 			String sMessage = "Error invoking getGroup - Invalid group Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		try {
 			group = groupService.getGroupById(groupId_p);
 		} catch (Exception e) {
 			String sMessage = "Error invoking getGroup. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		logger_c.debug("Returing Group: " + group.toString());
 		return new ModelAndView(jsonView_i, DATA_FIELD, group);
 	}
                 
                 
 //        @RequestMapping(value = { "/rest/{userId}/{userId}/groups/" }, method = { RequestMethod.POST })
 //	public ModelAndView createGroup(@RequestBody Group group_p,@PathVariable("userId") int userId,
 //			HttpServletResponse httpResponse_p, WebRequest request_p) {
                 
         @RequestMapping(value = { "/rest/{userId}/groups/" }, method = { RequestMethod.POST })
 	public ModelAndView createGroup(@RequestBody Group group_p,@PathVariable("userId") int userId,
 			HttpServletResponse httpResponse_p, WebRequest request_p) {
 
 
 
 
 
 		Group createdGroup;
 		logger_c.debug("Creating Group: " + group_p.toString());
 
 		try {
 			createdGroup = groupService.createGroup(userId, userId);
 		} catch (Exception e) {
 			String sMessage = "Error creating new group. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		/* set HTTP response code */
 		httpResponse_p.setStatus(HttpStatus.CREATED.value());
 
 		/* set location of created resource */
//		httpResponse_p.setHeader("Location", request_p.getContextPath() + "/rest/{userId}/{userId}/groups/" + group_p.getGroupId());
 		/* set location of created resource */
 //		httpResponse_p.setHeader("Location", request_p.getContextPath() + "/rest/{userId}/{userId}/groups/" + group_p.getGroupId());
 
 
 
 
 
 
 
 
 
 
 
 
 
 		/**
 		 * Return the view
 		 */
 		return new ModelAndView(jsonView_i, DATA_FIELD, createdGroup);
 	}
 
 	/**
 	 * Updates group with given group id.
 	 *
 	 * @param group_p
 	 *            the group_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = { "/rest/{userId}/{userId}/groups/{groupId}" }, method = { RequestMethod.PUT })
 	public ModelAndView updateGroup(@RequestBody Group group_p,@PathVariable("userId") int userId, @PathVariable("groupId") String groupId_p,
 								   HttpServletResponse httpResponse_p) {
 
 		logger_c.debug("Updating Group: " + group_p.toString());
 
 		/* validate group Id parameter */
 		if (isEmpty(groupId_p) || groupId_p.length() < 5) {
 			String sMessage = "Error updating group - Invalid group Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		Group group = null;
 
 		try {
 			group = groupService.updateGroup(group_p);
 		} catch (Exception e) {
 			String sMessage = "Error updating group. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		httpResponse_p.setStatus(HttpStatus.OK.value());
 		return new ModelAndView(jsonView_i, DATA_FIELD, group);
 	}
 
 	/**
 	 * Deletes the group with the given group id.
 	 *
 	 * @param groupId_p
 	 *            the group id_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = "/rest/{userId}/{userId}/groups/{groupId}", method = RequestMethod.DELETE)
 	public ModelAndView removeGroup(@PathVariable("groupId") String groupId_p,@PathVariable("userId") int userId,
 								   HttpServletResponse httpResponse_p) {
 
 		logger_c.debug("Deleting Group Id: " + groupId_p.toString());
 
 		/* validate group Id parameter */
 		if (isEmpty(groupId_p) || groupId_p.length() < 5) {
 			String sMessage = "Error deleting group - Invalid group Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		try {
 			groupService.deleteGroup(groupId_p);
 		} catch (Exception e) {
 			String sMessage = "Error invoking getGroups. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		httpResponse_p.setStatus(HttpStatus.OK.value());
 		return new ModelAndView(jsonView_i, DATA_FIELD, null);
 	}
         
         public static boolean isEmpty(String s_p) {
 		return (null == s_p) || s_p.trim().length() == 0;
 	}
         
         	private ModelAndView createErrorResponse(String sMessage) {
 		return new ModelAndView(jsonView_i, ERROR_FIELD, sMessage);
 	}
 }
