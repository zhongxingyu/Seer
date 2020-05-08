 package de.klingbeil.ui.controller;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import de.klingbeil.backend.model.User;
 import de.klingbeil.backend.service.UserService;
 
 /**
  * @author Petri Kainulainen
  */
 @Controller
 @SessionAttributes("user")
 public class UserController extends AbstractController {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(UserController.class);
 
 	protected static final String ERROR_MESSAGE_KEY_DELETED_USER_WAS_NOT_FOUND = "error.message.deleted.not.found";
 	protected static final String ERROR_MESSAGE_KEY_EDITED_USER_WAS_NOT_FOUND = "error.message.edited.not.found";
 
 	protected static final String FEEDBACK_MESSAGE_KEY_USER_CREATED = "feedback.message.user.created";
 	protected static final String FEEDBACK_MESSAGE_KEY_USER_DELETED = "feedback.message.user.deleted";
 	protected static final String FEEDBACK_MESSAGE_KEY_USER_EDITED = "feedback.message.user.edited";
 
 	protected static final String MODEL_ATTIRUTE_USER = "user";
 	protected static final String MODEL_ATTRIBUTE_USERS = "users";
 
 	protected static final String USER_ADD_FORM_VIEW = "user/create";
 	protected static final String USER_EDIT_FORM_VIEW = "user/edit";
 	protected static final String USER_LIST_VIEW = "user/list";
 
 	protected static final String REQUEST_MAPPING_LIST = "/";
 
 	@Resource
 	private UserService userService;
 
 	/**
 	 * Processes delete user requests.
 	 * 
 	 * @param id
 	 *            The id of the deleted user.
 	 * @param attributes
 	 * @return
 	 */
 	@RequestMapping(value = "/user/delete/{id}", method = RequestMethod.GET)
 	public String delete(@PathVariable("id") Long id,
 			RedirectAttributes attributes) {
 		LOGGER.debug("Deleting user with id: " + id);
 
 		try {
 			userService.delete(id);
 			addFeedbackMessage(attributes, FEEDBACK_MESSAGE_KEY_USER_DELETED,
 					id);
 		} catch (IllegalArgumentException e) {
 			LOGGER.debug("No user found with id: " + id);
 			addErrorMessage(attributes,
 					ERROR_MESSAGE_KEY_DELETED_USER_WAS_NOT_FOUND);
 		}
 
 		return createRedirectViewPath(REQUEST_MAPPING_LIST);
 	}
 
 	/**
 	 * Processes create user requests.
 	 * 
 	 * @param model
 	 * @return The name of the create user form view.
 	 */
 	@RequestMapping(value = "/user/create", method = RequestMethod.GET)
 	public String showCreateuserForm(Model model) {
 		LOGGER.debug("Rendering create user form");
 
 		model.addAttribute(MODEL_ATTIRUTE_USER, new User());
 
 		return USER_ADD_FORM_VIEW;
 	}
 
 	/**
 	 * Processes the submissions of create user form.
 	 * 
 	 * @param created
 	 *            The information of the created users.
 	 * @param bindingResult
 	 * @param attributes
 	 * @return
 	 */
 	@RequestMapping(value = "/user/create", method = RequestMethod.POST)
 	public String submitCreateuserForm(
 			@Valid @ModelAttribute(MODEL_ATTIRUTE_USER) User created,
 			BindingResult bindingResult, RedirectAttributes attributes) {
 		LOGGER.debug("Create user form was submitted with information: "
 				+ created);
 
 		if (bindingResult.hasErrors()) {
 			return USER_ADD_FORM_VIEW;
 		}
 
 		User user = userService.create(created);
 
 		addFeedbackMessage(attributes, FEEDBACK_MESSAGE_KEY_USER_CREATED,
 				user.getFirstName() + " " + user.getLastName());
 
 		return createRedirectViewPath(REQUEST_MAPPING_LIST);
 	}
 
 	/**
 	 * Processes edit user requests.
 	 * 
 	 * @param id
 	 *            The id of the edited user.
 	 * @param model
 	 * @param attributes
 	 * @return The name of the edit user form view.
 	 */
 	@RequestMapping(value = "/user/edit/{id}", method = RequestMethod.GET)
 	public String showEdituserForm(@PathVariable("id") Long id, Model model,
 			RedirectAttributes attributes) {
 		LOGGER.debug("Rendering edit user form for user with id: " + id);
 
 		User user = userService.findById(id);
 		if (user == null) {
 			LOGGER.debug("No user found with id: " + id);
 			addErrorMessage(attributes,
 					ERROR_MESSAGE_KEY_EDITED_USER_WAS_NOT_FOUND);
 			return createRedirectViewPath(REQUEST_MAPPING_LIST);
 		}
 
 		model.addAttribute(MODEL_ATTIRUTE_USER, user);
 
 		return USER_EDIT_FORM_VIEW;
 	}
 
 	/**
 	 * Processes the submissions of edit user form.
 	 * 
 	 * @param updated
 	 *            The information of the edited user.
 	 * @param bindingResult
 	 * @param attributes
 	 * @return
 	 */
 	@RequestMapping(value = "/user/edit", method = RequestMethod.POST)
 	public String submitEdituserForm(
 			@Valid @ModelAttribute(MODEL_ATTIRUTE_USER) User updated,
 			BindingResult bindingResult, RedirectAttributes attributes) {
 		LOGGER.debug("Edit user form was submitted with information: "
 				+ updated);
 
 		if (bindingResult.hasErrors()) {
 			LOGGER.debug("Edit user form contains validation errors. Rendering form view.");
 			return USER_EDIT_FORM_VIEW;
 		}
 
 		try {
 			User user = userService.update(updated);
 			addFeedbackMessage(attributes, FEEDBACK_MESSAGE_KEY_USER_EDITED,
 					user.getFirstName() + " " + user.getLastName());
 		} catch (IllegalArgumentException e) {
 			LOGGER.debug("No user was found with id: " + updated.getId());
 			addErrorMessage(attributes,
 					ERROR_MESSAGE_KEY_EDITED_USER_WAS_NOT_FOUND);
 		}
 
 		return createRedirectViewPath(REQUEST_MAPPING_LIST);
 	}
 
 	/**
 	 * Processes requests to home page which lists all available users.
 	 * 
 	 * @param model
 	 * @return The name of the user list view.
 	 */
 	@RequestMapping(value = REQUEST_MAPPING_LIST, method = RequestMethod.GET)
 	public String showList(Model model) {
 		LOGGER.debug("Rendering user list page");
 
 		List<User> users = userService.findAll();
 		model.addAttribute(MODEL_ATTRIBUTE_USERS, users);
 
 		return USER_LIST_VIEW;
 	}
 
 	/**
 	 * This setter method should only be used by unit tests
 	 * 
 	 * @param userService
 	 */
	protected void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 }
