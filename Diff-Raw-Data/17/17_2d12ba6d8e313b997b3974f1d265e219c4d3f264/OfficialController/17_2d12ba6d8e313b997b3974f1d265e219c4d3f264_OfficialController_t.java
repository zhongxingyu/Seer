 package com.scheduler.controllers;
 
 import java.sql.Date;
 import java.util.List;
 import java.util.Random;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.jdbc.BadSqlGrammarException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.scheduler.models.Announcement;
 import com.scheduler.models.Appointment;
 import com.scheduler.models.AppointmentList;
 import com.scheduler.models.Department;
 import com.scheduler.models.GeneralUser;
 import com.scheduler.models.Notification;
 import com.scheduler.models.OfficialUser;
 import com.scheduler.models.Roles;
 import com.scheduler.request.MailMail;
 import com.scheduler.services.AnnouncementService;
 import com.scheduler.services.AppointmentService;
 import com.scheduler.services.DepartmentService;
 import com.scheduler.services.DepartmentTimeslotService;
 import com.scheduler.services.NotificationService;
 import com.scheduler.services.OfficialUserService;
 import com.scheduler.services.RolesService;
 
 @RequestMapping("/official")
 @Controller
 @Slf4j
 public class OfficialController extends SessionController {
 
 	@Autowired(required = true)
 	private AppointmentService appointmentService;
 
 	@Autowired(required = true)
 	private NotificationService notificationService;
 
 	@Autowired(required = true)
 	private AnnouncementService announcementService;
 
 	@Autowired(required = true)
 	private OfficialUserService officialUserService;
 
 	@Autowired(required = true)
 	private DepartmentService departmentService;
 
 	@Autowired(required = true)
 	private RolesService rolesService;
 
 	@Autowired(required = true)
 	private DepartmentTimeslotService departmentTimeslotService;
 
 	public List<AppointmentList> listofAppointment;
 
 	@RequestMapping(value = "", method = RequestMethod.GET)
 	public String showOfficialDashboard(Model model) {
 		return "redirect:official/dashboard";
 	}
 
 	@RequestMapping(value = "/meeting/finish", method = RequestMethod.POST)
 	public String finishMeeting(
 			@ModelAttribute("appointment") Appointment appointment, Model model) {
 		// Code to Finish Appointment
 		boolean meetingFinished = appointmentService
 				.finishAppointment(appointment);
 		if (meetingFinished) {
 			model.addAttribute("finish", true);
 		} else {
 			model.addAttribute("finish", false);
 		}
 		// Redirecting to view the queue
 		return "redirect:/official/meeting/viewqueue";
 	}
 
 	@RequestMapping(value = "/meeting/viewqueue", method = RequestMethod.GET)
 	public String viewQueue(Model model) {
 		System.out.println("view queue started");
 		addUserModel(model);
 		int departmentId = Integer.parseInt(sessionMap.get("deptId"));
 		
 		//String appointmentDate = "2013-11-13";
 		listofAppointment = appointmentService.getAllAppointment(departmentId);
 		model.addAttribute("appointmentList", listofAppointment);
 
 		// passing blank announcement object
 		model.addAttribute("announcement", new Announcement());
 		// Redirecting to view the queue
 		return "meeting/viewqueue";
 	}
 
 	@RequestMapping(value = "/meeting/start", method = RequestMethod.POST)
 	public String startMeeting(Model model) {
 		// insert the following fields to official user session
 		// get official_id and dept_id from the session variable
 		int official_id=Integer.parseInt(sessionMap.get("id"));
 		int department_id = Integer.parseInt(sessionMap.get("deptId")); 
 		addUserModel(model);
 		Appointment nextAppointment = appointmentService
 				.findNextAppointment(department_id);
 		if (nextAppointment != null) {
 			// Code to start appointment
 			Appointment startedAppointment = appointmentService
 					.startAppointmentById(nextAppointment.getAppointmentId(),
 							official_id);
 
 			if (startedAppointment != null) {
 
 				// Get next user in Queue (and send a push notification)
 				GeneralUser nextUser = appointmentService
 						.getNextUserInQueue(department_id);
 				if(nextUser!=null)
 				{
 				//
 				Notification notification = new Notification();
 				notification.setOfficialId(official_id);
 				notification.setUserId(nextUser.getUserId());
 				notification.setNotificationHeader("Meeting starting soon!");
 				notification
 						.setNotificationDescription("You are the next person in queue");
 				boolean notifyNextUser = notificationService.notifyUser(
 						nextUser.getGcmRegId(), notification);
 				if (notifyNextUser) {
 					model.addAttribute("nextUserNotified", true);
 				}
 				}
 				// if appointment is marked as started
 				model.addAttribute("started", "true");
 				model.addAttribute("appointment", startedAppointment);
 
 			} else {
 
 				// if appointment couldn't be marked as started
 				model.addAttribute("started", "false");
 			}
 			return "meeting/meeting";
 		}
 
 		return "redirect:/official/meeting/viewqueue";
 
 	}
 
 	@RequestMapping(value = "/meeting/broadcast", method = RequestMethod.POST)
 	public String broadcastMessage(
 			@ModelAttribute("announcement") Announcement announcement,
 			Model model) {
 		log.info("Got " + announcement.getAnnouncementHeader());
 
 		// TODO insert the following fields to official user session
 		// get official_id and dept_id from the session variable
 		
 
 		announcement.setOfficialId(Integer.parseInt(sessionMap.get("id")));
 		int announcement_id = announcementService
 				.addNewAnnouncement(announcement);
 
 		boolean broadcasted = announcementService.addUserAnnouncement(
 				listofAppointment, announcement_id,
 				announcement.getAnnouncementHeader(),
 				announcement.getAnnouncementDescription());
 
 		// Redirecting to view the queue
 		return "redirect:/official/meeting/viewqueue";
 	}
 
 	@RequestMapping(value = "meeting/late", method = RequestMethod.GET)
 	public String userLate(@ModelAttribute("appointment") Appointment appointment,RedirectAttributes ra, Model model) {
 
 		int result;
 		try {
 			result = appointmentService.userLate(appointment.getAppointmentId());
 			model.addAttribute("result", result);
 		} catch (BadSqlGrammarException e) {
 			model.addAttribute("error", e.getMessage());
 			System.out.println(e.getMessage());
 		}
 
 		return "redirect:/official/meeting/viewqueue";
 	}
 
 	@RequestMapping(value = "meeting/testmeeting", method = RequestMethod.GET)
 	public String testme(Model model) {
 
 		ApplicationContext context = new ClassPathXmlApplicationContext(
 				"Spring-Mail.xml");
 		MailMail mm = (MailMail) context.getBean("mailMail");
 		// mm.sendMail("Scheduler App",
 		// "This is a Test Email \n your activation code : 85647555 ","sanket.scorp@gmail.com");
 
 		return "meeting/testmeeting";
 	}
 
 	// Author - Devraj Valecha
 	// Usage - Login for official user
 	// client
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String loginOfficial(Model model) {
 		addUserModel(model);
 		return "officialuser/loginofficial";
 	}
 
 	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
 	public String authenticateOfficial(
 			@RequestParam("userName") String userName,
 			@RequestParam("password") String password, Model model) {
 
 		OfficialUser o1 = new OfficialUser();
 		o1.setOfficialName(userName);
 		o1.setPassword(password);
 		OfficialUser result = officialUserService.authenticate(o1);
 		if (result.getOfficialId() > 0) {
 			String name = result.getFirstName();
 			int id = result.getOfficialId();
 			/*
 			 * session.setAttribute("officialUserName", userName);
 			 * session.setAttribute("officialName", name);
 			 * session.setAttribute("officialId", id);
 			 */
 		} else {
 			model.addAttribute("result", "Login Failed");
 			return "officialuser/errorofficiallogin";
 		}
 		// System.out.println(session.getAttribute("officialName"));
 		return "redirect:dashboard";
 	}
 
 	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
 	public String showDashboard(Model model) {
 		addUserModel(model);
 		int departmentId = Integer.parseInt(sessionMap.get("deptId"));
 		model.addAttribute("appointmentCount", appointmentService.getAppointmentCount(departmentId));
 		model.addAttribute("staffCount", officialUserService.getStaffCount(departmentId));
 		return "officialuser/dashboard";
 	}
 
 	@RequestMapping(value = "users/view", method = RequestMethod.GET)
 	public String viewOfficialUsers(Model model) {
 
 		
 		addUserModel(model);
 		List<Roles> roles = rolesService.getRoles();
 		List<Department> departments = departmentService
 				.departmentByClient(Integer.parseInt(sessionMap.get("id")));
 		for (Department department : departments) {
 			department.setSlots(departmentTimeslotService
 					.getDepartmentTimeslot(department.getDepartmentId()));
 			department.setOfficialUsers(officialUserService
 					.getOfficialUserByDepartment(department.getDepartmentId()));
 		}
 		model.addAttribute("departments", departments);
 		model.addAttribute("roles", roles);
 		model.addAttribute("officialUser", new OfficialUser());
 		return "client/officialusers";
 	}
 
 	@RequestMapping(value = "users/save", method = RequestMethod.POST)
 	public String viewOfficialUsers(
 			@ModelAttribute("officialUser") OfficialUser officialUser,
 			Model model) {
 
 		int i = officialUserService.saveOfficialUser(officialUser);
 		return "redirect:/official/users/view";
 	}
 
 	@RequestMapping(value = "users/delete/{officialId}", method = RequestMethod.GET)
 	public String deleteOfficialUsers(
 			@PathVariable("officialId") int officialId, Model model) {
 
 		int i = officialUserService.deleteOfficialUser(officialId);
 		return "redirect:/official/users/view";
 	}
 
 	@RequestMapping(value = "users/edit/{officialId}", method = RequestMethod.GET)
 	public String editOfficialUsers(@PathVariable("officialId") int officialId,
 			Model model) {
 		addUserModel(model);
 		List<Roles> roles = rolesService.getRoles();
 		List<Department> departments = departmentService
 				.departmentByClient(Integer.parseInt(sessionMap.get("id")));
 		OfficialUser officialUser = officialUserService
 				.getOfficialUserById(officialId);
 		model.addAttribute("Id", officialId);
 		model.addAttribute("departments", departments);
 		model.addAttribute("roles", roles);
 		model.addAttribute("officialUserEdit", officialUser);
 		return "client/editofficialuser";
 	}
 
 	@RequestMapping(value = "users/update", method = RequestMethod.POST)
 	public String updateOfficialUsers(
 			@ModelAttribute("officialUser") OfficialUser officialUser,
 			Model model) {
 
		addUserModel(model);
 		int i = officialUserService.updateOfficialUser(officialUser);
		if(sessionMap.get("role").equals("CL")) {
			return "redirect:/official/users/view";
		} else {
			return "redirect:/official/users/edit/" + sessionMap.get("id");
		}
 	}
 
 	// Author - Devraj Valecha
 	// Usage - Reset password for official user
 	// official user
 	@RequestMapping(value = "/reset", method = RequestMethod.GET)
 	public String resetPasswordOfficialUser(Model model) {
 		addUserModel(model);
 		return "officialuser/resetpasswordofficialuser";
 	}
 
 	@RequestMapping(value = "/saveTemporaryPassword", method = RequestMethod.POST)
 	public String savePassword(@RequestParam("emailAddress") String email,
 			Model model) {
 		Random randomGenerator = new Random();
 		String myPassword = Integer.toString(randomGenerator.nextInt(20000));
 		int passwordUpdate = officialUserService.resetPassword(email,
 				myPassword);
 		if (passwordUpdate > 0) {
 			String to = email;
 			ApplicationContext context = new ClassPathXmlApplicationContext(
 					"Spring-Mail.xml");
 			MailMail mm = (MailMail) context.getBean("mailMail");
 			String url = "http://localhost:8080/Scheduler/login";
 			mm.sendMail(
 					"Scheduler App",
 					"This is a Test Email \n your temporary password : "
 							+ myPassword
 							+ " \n Below is the link provided to login to scheduler\n"
 							+ url, to);
 		} else {
 			return "officialuser/passworderrorofficialuser";
 		}
 
 		return "officialuser/passwordsentofficialuser";
 	}
 
 	@RequestMapping(value = "/editpassword/{officialId}", method = RequestMethod.GET)
 	public String updatePassword(@PathVariable("officialId") int officialId,
 			Model model) {
 		addUserModel(model);
 		model.addAttribute("userId", officialId);
 		OfficialUser u = officialUserService.getOfficialUserById(officialId);
 		u.setPassword("");
 		model.addAttribute("officialUser", u);
 		return "officialuser/editPassword";
 	}
 
 	@RequestMapping(value = "/savepassword", method = RequestMethod.POST)
 	public String editPassword(
 			@ModelAttribute("officialUser") OfficialUser officialUser,
 			RedirectAttributes ra, Model model) {
 
 		if (officialUser.getPassword().equals(officialUser.getRepassword())) {
 			// both passwords match. Update password
 			int rowsAffected = officialUserService.updatePassword(officialUser);
 			if (rowsAffected > 0) {
 				ra.addFlashAttribute("result", "success");
 				ra.addFlashAttribute("message", "Password changed successfully");
 			} else {
 				ra.addFlashAttribute("result", "fail");
 				ra.addFlashAttribute("message",
 						"Oops. Something went wrong. Try again later");
 			}
 		} else {
 			// send password doesn't match error
 			ra.addFlashAttribute("result", "fail");
 			ra.addFlashAttribute("message", "Password didnt match");
 		}
 
 		return "redirect:/official/editpassword/"
 				+ officialUser.getOfficialId();
 
 	}
 
 }
