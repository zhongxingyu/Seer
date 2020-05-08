 package com.scheduler.controllers;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.BadSqlGrammarException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.scheduler.models.Notification;
 import com.scheduler.services.GeneralUserService;
 import com.scheduler.services.NotificationService;
 
 
 
 @RequestMapping("/generaluser")
 @Controller
 public class NotificationController extends SessionController {
 
 	protected static final String JSON_CONTENT = "application/json";
 	
 	@Autowired(required = true)
 	private NotificationService notificationService;
 	@Autowired(required = true)
 	private GeneralUserService generalUserService;
 
 	@RequestMapping(value = "/notifications", method = RequestMethod.GET)
 	public String viewAllNotifications(Model model) {
 		List<Notification> notifications = null;
 		try {
 			addUserModel(model);
 			notifications = notificationService.findAllNotifications(Integer.parseInt(sessionMap.get("id")));
 			model.addAttribute("notifications", notifications);
 
 		} catch (BadSqlGrammarException e) {
 			model.addAttribute("ERROR", e.getMessage());
 			System.out.println(e.getMessage());
 		}
 		return "generaluser/viewNotifications";
 	}
 
 	// Author - Shalin Banjara
 	// Usage - Returns a notification popup to official user for send a notification to a particular student in the queue
 	@RequestMapping(value = "/create/notification/{userId}", method = RequestMethod.GET)
 	public String createNotification(@PathVariable("userId") String userId,
 			Model model) {
 		String[] s = userId.split(":");
 		System.out.println(s[0] + s[1] + s[2]);
 		model.addAttribute("userId", Integer.parseInt(s[0]));
 		model.addAttribute("firstName", s[1]);
 		model.addAttribute("lastName", s[2]);
 		model.addAttribute("notification", new Notification());
 		return "partials/notificationpopup";
 	}
 
 	// Author - Shalin Banjara
 	// Usage - Send the notification and stores it in the database for future reference
 	@RequestMapping(value = "/send/notification", method = RequestMethod.POST)
 	public String sendNotification(
 			@ModelAttribute("notification") Notification notification,
 			Model model) {

 		notification.setOfficialId(Integer.parseInt(sessionMap.get("id")));
 		//notification.setUserId(1);
 		System.out.println(generalUserService.getUserGCMregId(notification
 				.getUserId()));
 	
 		boolean notifyNextUser = notificationService.notifyUser(
 			generalUserService.getUserGCMregId(notification.getUserId()),
 			notification);
 		// Stub to check the functionality of send Notification feature
 		System.out.println("UserId :- " + notification.getUserId()
 				+ " Title :- " + notification.getNotificationHeader()
 				+ " Message :- " + notification.getNotificationDescription());
 		return "redirect:/official/meeting/viewqueue/";
 	}
 	
 	
 }
