 package messages.web;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import messages.orm.UserAccount;
 import messages.orm.UserRole;
 import messages.repository.user.UserAccountService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Action based Spring MVC @Controller controller handling requests for user
  * account summary, user account details, edit user account form, create user
  * account form, remove user account.
  */
 @Controller
 @RequestMapping(value = "/users")
 public class UserAccountController {
 
 	private UserAccountService userAccountService;
 
 	private MailSender mailSender;
 
 	/**
 	 * Creates a new UserAccountController with a given user account service.
 	 */
 	@Autowired
 	public UserAccountController(UserAccountService userAccountService, MailSender mailSender) {
 		this.userAccountService = userAccountService;
 		this.mailSender = mailSender;
 	}
 
 	/**
 	 * Provides a model with an user account for the user account detail page.
 	 * 
 	 * @param password the username of the user
 	 * @param model the "implicit" model created by Spring MVC
 	 */
 	@RequestMapping(value = "/userDetails", method = RequestMethod.GET)
 	public String userDetails(@RequestParam("username") String username, Model model) {
 		model.addAttribute("user", this.userAccountService.findByUsername(username));
 		return "userDetails";
 	}
 
 	/**
 	 * Provides a model with an user account for the user account form page to
 	 * edit a user account
 	 * 
 	 * @param password the username of the user
 	 * @param model the "implicit" model created by Spring MVC
 	 */
 	@RequestMapping(value = "/editUser", method = RequestMethod.GET)
 	public String getEditUser(@RequestParam("name") String username, Model model) {
 		UserAccount user = this.userAccountService.findByUsername(username);
 		model.addAttribute("user", user);
 		return "userForm";
 	}
 
 	/**
 	 * Provides a model with a list of all users for the summary page.
 	 * 
 	 * @param model the "implicit" model created by Spring MVC
 	 */
 	@RequestMapping("/userSummary")
 	public String userSummary(Model model) {
 		List<UserAccount> users = this.userAccountService.getAllUsers();
 		model.addAttribute("users", users);
 		return "userSummary";
 	}
 
 	/**
 	 * Creates a new user account object and puts it to the model.
 	 * 
 	 * @param password
 	 * @param model the "implicit" model created by Spring MVC
 	 */
 	@RequestMapping(value = "/createUser", method = RequestMethod.GET)
 	public String getCreateUser(Model model) {
 		UserAccount user = new UserAccount();
 		model.addAttribute("user", user);
 		return "userForm";
 	}
 
 	/**
 	 * Removes a user account from the database.
 	 * 
 	 * @param username
 	 */
 	@RequestMapping(value = "/removeUser")
 	public String removeUser(@RequestParam("username") String username) {
 		UserAccount user = this.userAccountService.findByUsername(username);
 		this.userAccountService.remove(user);
 		return "redirect:/board/users/userSummary";
 	}
 
 	/**
 	 * Posts a user and stores in the database.
 	 * 
 	 * @param user the user account to be stored
 	 * @param bindingResult
 	 * @throws NoSuchAlgorithmException
 	 */
 	@RequestMapping(value = "/createUser", method = RequestMethod.POST)
 	public String postCreateUser(@ModelAttribute("user") @Valid UserAccount user, BindingResult result)
 			throws NoSuchAlgorithmException {
 		if (result.hasErrors()) {
 			return "userForm";
 		}
 		user.addAuthority(UserRole.ROLE_MEMBER);
		this.userAccountService.create(user);
 		// this.sendEmail(user);
 		return "redirect:/board/users/userDetails?username=" + user.getUsername();
 	}
 
 	/**
 	 * Posts an edited existing user account and stores in the database.
 	 * 
 	 * @param uer account the user to be stored
 	 * @param bindingResult
 	 * @throws NoSuchAlgorithmException
 	 */
 	@RequestMapping(value = "/editUser", method = RequestMethod.POST)
 	public String postEditUser(@ModelAttribute("user") @Valid UserAccount user, BindingResult result)
 			throws NoSuchAlgorithmException {
 		if (result.hasErrors()) {
 			return "userForm";
 		}
 		this.userAccountService.update(user);
 		return "redirect:/board/users/userDetails?username=" + user.getUsername();
 	}
 
 	private void sendEmail(UserAccount user) {
 		SimpleMailMessage mail = new SimpleMailMessage();
 		mail.setSubject("New user " + user.getUsername());
 		mail.setText("New user " + user.getUsername() + " has been created");
 		mail.setFrom("nesi_bud@mail.ru");
 		mail.setTo("alexei.budko@netum.fi");
 		this.mailSender.send(mail);
 	}
 }
