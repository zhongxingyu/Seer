 package com.scheduler.controllers;
 
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.Random;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.servlet.http.HttpSession;
 
 import com.scheduler.models.GeneralUser;
 import com.scheduler.request.MailMail;
 import com.scheduler.services.AnnouncementService;
 import com.scheduler.services.AppointmentService;
 import com.scheduler.services.GeneralUserService;
 import com.scheduler.services.NotificationService;
 
 @RequestMapping("/generaluser")
 @Controller
 @Slf4j
 public class GeneralUserController extends SessionController {
 
 	@Autowired(required = true)
 	private GeneralUserService generaluserService;
 	
 	@Autowired(required = true)
 	private AppointmentService appointmentService;
 	
 	@Autowired(required = true)
 	private AnnouncementService announcementService;
 	
 	@Autowired(required = true)
 	private NotificationService notificationService;
 
 	@RequestMapping(value = "", method = RequestMethod.GET)
 	public String showGeneralUserDashboard(Model model) {
 		return "redirect:generaluser/dashboard";
 	}
 
 	@RequestMapping(value = "/register", method = RequestMethod.GET)
 	public String showRegistrationForm(Model model) {
		addUserModel(model);
 		model.addAttribute("generaluser", new GeneralUser());
 		return "generaluser/registergeneraluser";
 	}
 
 	@RequestMapping(value = "/save", method = RequestMethod.POST)
 	public String saveUser(
 			@ModelAttribute("generaluser") GeneralUser generaluser, Model model) {
 		String token;
 		int userId;
 		if (generaluser != null) {
 			String to = generaluser.getEmail().toString();
 			ApplicationContext context = new ClassPathXmlApplicationContext(
 					"Spring-Mail.xml");
 			MailMail mm = (MailMail) context.getBean("mailMail");
 			Random randomGenerator = new Random();
 			int mytoken = randomGenerator.nextInt(999999 - 100000) + 100000;
 			generaluser.setTocken("" + mytoken);
 			System.out.println(generaluser.getTocken());
 			int result = generaluserService.saveUser(generaluser);
 
 			userId = generaluserService.getLastUserId();
 			String username = generaluser.getUsername();
 			token = generaluserService.getUserToken(userId);
 
 			model.addAttribute("generaluser", generaluser);
 			System.out.println("generaluser saved successfully");
 			mm.sendMail(" " + username,
 					"Your Activation Link is http://localhost:8080/Scheduler/generaluser/verify/"
 							+ userId + "/" + token, to);
 			// model.addAttribute("client", new Client());
 		} else {
 			model.addAttribute("result", "fail");
 		}
 		return "generaluser/userdashboard";
 	}
 
 	// Author - Sanket Patel
 	// Usage - uses the user id and token from the link to verify the correct
 	// user
 	@RequestMapping(value = "/verify/{userId}/{token}", method = RequestMethod.GET)
 	public String verifyUser(@PathVariable("userId") int userId,
 			@PathVariable("token") String tokenFromURL, Model model) {
 
 		String tokenFromDB = generaluserService.getUserToken(userId);
 		// select token from client where clientId=client_id;
 
 		if (tokenFromDB.equals(tokenFromURL)) {
 			int result = generaluserService.verifyUser(userId);
 			// update client set emailVerified=1 where clientId=client_id
 			model.addAttribute("result",
 					"Thank you for verifying the email address");
 		} else {
 			model.addAttribute("result", "Sorry, verification failed");
 		}
 		return "generaluser/verifyUser";
 	}
 
 	// Author - Devraj Valecha
 	// Usage - Reset password for general user
 	// general user
 	@RequestMapping(value = "/reset", method = RequestMethod.GET)
 	public String resetPasswordGeneralUser(Model model) {
 		addUserModel(model);
 		return "generaluser/resetpasswordgeneraluser";
 	}
 
 	@RequestMapping(value = "/saveTemporaryPassword", method = RequestMethod.POST)
 	public String savePassword(@RequestParam("emailAddress") String email,
 			Model model) {
 		Random randomGenerator = new Random();
 		String myPassword = Integer.toString(randomGenerator.nextInt(20000));
 		int passwordUpdate = generaluserService
 				.resetPassword(email, myPassword);
 		if (passwordUpdate > 0) {
 			String to = email;
 			ApplicationContext context = new ClassPathXmlApplicationContext(
 					"Spring-Mail.xml");
 			MailMail mm = (MailMail) context.getBean("mailMail");
 			String url = "http://localhost:8080/Scheduler/generaluser/login";
 			mm.sendMail(
 					"Scheduler App",
 					"This is a Test Email \n your temporary password : "
 							+ myPassword
 							+ " \n Below is the link provided to login to scheduler\n"
 							+ url, to);
 		} else {
 			return "generaluser/passworderrorgeneraluser";
 		}
 
 		return "generaluser/passwordsentgeneraluser";
 	}
 
 	// Author - Devraj Valecha
 	// Usage - Login for general user
 	// general user
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String loginGeneralUser(Model model) {
 		addUserModel(model);
 		return "generaluser/logingeneraluser";
 	}
 
 	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
 	public String authenticateGeneralUser(
 			@RequestParam("userName") String userName,
 			@RequestParam("password") String password, Model model,
 			HttpSession session) {
 		GeneralUser gu = new GeneralUser();
 		gu.setUsername(userName);
 		gu.setPassword(password);
 		GeneralUser result = generaluserService.authenticate(gu);
 		if (result.getUserId() > 0) {
 			String name = result.getFirstName();
 			int id = result.getUserId();
 			session.setAttribute("generalUserName", userName);
 			session.setAttribute("generalName", name);
 			session.setAttribute("generalId", id);
 		} else {
 			model.addAttribute("result", "Login Failed");
 			return "generaluser/errorgenerallogin";
 		}
 		return "redirect:dashboard";
 	}
 
 	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
 	public String showDashboard(Model model) {
 		addUserModel(model);
 		int userId = Integer.parseInt(sessionMap.get("id"));
 		model.addAttribute("appointmentCount", appointmentService.getAppointmentCountByUserId(userId));
 		model.addAttribute("announcementCount", announcementService.getAllAnnouncements(userId).size());
 		model.addAttribute("notificationCount", notificationService.findAllNotifications(userId).size());
 		return "generaluser/dashboard";
 	}
 
 	@RequestMapping(value = "/edit", method = RequestMethod.GET)
 	public String editUser(Model model) {
 		addUserModel(model);
 		int userId = Integer.parseInt(sessionMap.get("id"));
 		model.addAttribute("userId", userId);
 		model.addAttribute("generaluser",
 				generaluserService.getGeneralUser(userId));
 		return "generaluser/updateDetails";
 	}
 
 	@RequestMapping(value = "/update", method = RequestMethod.POST)
 	public String updateUser(
 			@ModelAttribute("generaluser") GeneralUser generaluser,
 			RedirectAttributes ra, Model model) {
 		ra.addFlashAttribute("updated", "updated");
 		int result = generaluserService.updateUser(generaluser);
 		model.addAttribute("generaluser", result);
		return "redirect:/generaluser/edit/" + generaluser.getUserId();
 	}
 
 	@RequestMapping(value = "/editpassword/{userId}", method = RequestMethod.GET)
 	public String updatePassword(@PathVariable("userId") int userId, Model model) {
 		model.addAttribute("userId", userId);
 		addUserModel(model);
 		GeneralUser u = generaluserService.getGeneralUser(userId);
 		u.setPassword("");
 		model.addAttribute("generaluser", u);
 		return "generaluser/editPassword";
 	}
 
 	@RequestMapping(value = "/savepassword", method = RequestMethod.POST)
 	public String editPassword(
 			@ModelAttribute("generaluser") GeneralUser generaluser,
 			RedirectAttributes ra, Model model) {
 
 		if (!generaluser.getPassword().equals(generaluser.getRepassword())) {
 			ra.addFlashAttribute("ue", "ue");
 			return "redirect:/generaluser/editpassword/"
 					+ generaluser.getUserId();
 		}
 		ra.addFlashAttribute("updated", "updated");
 		int result = generaluserService.updatePassword(generaluser);
 		model.addAttribute("generaluser", result);
 		return "redirect:/generaluser/editpassword/" + generaluser.getUserId();
 	}
 }
