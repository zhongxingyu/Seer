 package de.edunet.main.controllers.contact;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 

 import de.edunet.main.models.contact.ContactHandler;
 import de.edunet.main.models.message.MessageHandler;
 import de.edunet24.contact.entityBeans.CRequest;
 import de.edunet24.contact.entityBeans.Contact;
 import de.edunet24.usermanager.entityBeans.User;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class ContactController {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(ContactController.class);
 
 	private ContactHandler contactHandler;
 	private MessageHandler messageHandler;
 
 	public void setMessageHandler(MessageHandler messageHandler) {
 		this.messageHandler = messageHandler;
 	}
 
 	public void setContactHandler(ContactHandler contactHandler) {
 		this.contactHandler = contactHandler;
 	}
 
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/contact", method = RequestMethod.GET)
 	public String home(Model model, HttpSession session) {
 
 		if (!messageHandler.isBridgeSessionOk(session)) {
 			return "redirect:../home-web";
 		}
 		model.addAttribute("currentUser", messageHandler.getCurrentUser(session));
 
 		logger.info("Calling message modul");
 		if (!model.containsAttribute("allUsers")) {
 			List<User> allUsers = contactHandler.getAllUsers();
 			init(model, allUsers);
 		}
 
 		return "contact";
 	}
 
 	@RequestMapping(value = "/contactSearchUser", method = RequestMethod.POST)
 	public String searchUsers(
 			@RequestParam(value = "sfirstname") String firstname,
 			@RequestParam(value = "slastname") String lastname, Locale locale,
 			Model model, HttpSession session) {
 
 		if (!messageHandler.isBridgeSessionOk(session)) {
 			return "redirect:../home-web";
 		}
 
 		if ((firstname == null || firstname.equals(""))
 				&& (lastname == null || lastname.equals(""))) {
 			return "redirect:../../contact";
 		}
 
 		logger.info("start search user");
 
 		List<User> users = contactHandler.searchUsers(firstname, lastname);
 		init(model, users);
 		logger.info("end search user");
 		return "contact";
 	}
 
 	@RequestMapping(value = "/contactAddCrequest", method = RequestMethod.POST)
 	public String addCRequest(
 			@RequestParam(value = "userResponseId") String userId,
 			@RequestParam(value = "requestMes") String message, Model model,
 			HttpSession session) {
 
 		if (!messageHandler.isBridgeSessionOk(session)) {
 			return "redirect:../home-web";
 		}
 
 		logger.info("start add contact request");
 		model.addAttribute("currentUser", messageHandler.getCurrentUser(session));
 
 		contactHandler.addCRequest(Integer.parseInt(userId), message);
 		List<User> allUsers = contactHandler.getAllUsers();
 		init(model, allUsers);
 		logger.info("end add contact request");
 		return "contact";
 	}
 
 	@RequestMapping(value = "/contactlist", method = RequestMethod.GET)
 	public String contactList(Model model, HttpSession session) {
 		model.addAttribute("currentUser", messageHandler.getCurrentUser(session));
 
 		if (!messageHandler.isBridgeSessionOk(session)) {
 			return "redirect:../home-web";
 		}
 
 		List<CRequest> allCRequestsUserResponse = contactHandler.getRequestByUserResponse();
 
 		logger.info("all requests: " + allCRequestsUserResponse);
 		logger.info("all requests size: " + allCRequestsUserResponse.size());
 
 		model.addAttribute("allCRequestsUserResponse", allCRequestsUserResponse);
 		
 		logger.info(" all contacts: "+contactHandler.getAllContact());
 		logger.info(" allContactUsers: "+contactHandler.getContactUsers(contactHandler.getAllContact(),
 				messageHandler.getCurrentUser(session)));
 		
 		model.addAttribute("allContactUsers",
 				contactHandler.getContactUsers(contactHandler.getAllContact(),
 						messageHandler.getCurrentUser(session)));
 		return "contactlist";
 	}
 
 	@RequestMapping(value = "/contactlist/replycrequest", method = RequestMethod.GET)
 	public String replyCRequest(
 			@RequestParam(value = "userRequestId") int userRequestId,
 			@RequestParam(value = "actionValue") String actionValue,
 			@RequestParam(value = "page") String page, Model model,
 			HttpSession session) {
 
 		if (!messageHandler.isBridgeSessionOk(session)) {
 			return "redirect:../home-web";
 		}
 
 		contactHandler.replyCRequest(userRequestId, actionValue);
 
 		model.addAttribute("allCRequestsUserResponse",
 				contactHandler.getRequestByUserResponse());
 		if (page.equals("contactlist")) {
 			return "redirect:../contactlist";
 		} else if (page.equals("contact"))
 			return "redirect:../contact";
 		return null;
 	}
 
 	public void init(Model model, List<User> users) {
 
 		for (User user : users) {
 			if (user.getId() == messageHandler.getUserId()) {
 				users.remove(user);
 				break;
 			}
 		}
 
 		List<User> listContactUsers = new ArrayList<User>();
 		List<User> listCReUserResponse = new ArrayList<User>();
 		List<User> listCReUserRequest = new ArrayList<User>();
 
 		List<Contact> allContacts = contactHandler.getAllContact();
 		List<CRequest> allCRequests = contactHandler.getRequestByUserResponse();
 		allCRequests.addAll(contactHandler.getRequestByUserRequest());
 
 		logger.info("allContacts: " + allContacts);
 		logger.info("allCRequests: " + allCRequests);
 
 		for (User user : users) {
 
 			for (Contact contact : allContacts) {
 				if (contact.getUser1().getId() == user.getId()
 						|| contact.getUser2().getId() == user.getId()) {
 					listContactUsers.add(user);
 					break;
 				}
 			}
 			for (CRequest crequest : allCRequests) {
 				logger.info("crequest.getResponse().getId() "
 						+ crequest.getResponse().getId());
 				logger.info("crequest.getRequest().getId() "
 						+ crequest.getRequest().getId());
 				logger.info("user.getId()" + user.getId());
 				if (crequest.getResponse().getId() == user.getId()) {
 					logger.info("anfrage gesendet an " + user.getFullName());
 					listCReUserResponse.add(user);
 				}
 
 				if (crequest.getRequest().getId() == user.getId()) {
 					logger.info("anfrage bekommt " + user.getFullName());
 					listCReUserRequest.add(user);
 				}
 			}
 
 		}
 		logger.info("listContactUsers: " + listContactUsers);
 		logger.info("listCReUserResponse: " + listCReUserResponse);
 		logger.info("listCReUserRequest: " + listCReUserRequest);
 
 		model.addAttribute("allUsers", users);
 		model.addAttribute("listContactUsers", listContactUsers);
 		model.addAttribute("listCReUserResponse", listCReUserResponse);
 		model.addAttribute("listCReUserRequest", listCReUserRequest);
 	}
 
 }
