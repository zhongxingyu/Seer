 package com.wishop.mvc.controllers;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.context.request.WebRequest;
 
 import com.wishop.model.BaseObject;
 import com.wishop.model.User;
 import com.wishop.model.exceptions.WishopException;
 import com.wishop.mvc.validators.BaseValidator;
 import com.wishop.mvc.validators.UserValidator;
 import com.wishop.service.BaseService;
 import com.wishop.service.UserService;
 import com.wishop.utils.WishopSecurityContext;
 
 /**
  * JavaBean User controller that is used to respond to <code>User</code> requests.
  * @author Paulo Monteiro
  *
  */
 @Controller 
 @RequestMapping("/workbench/user")
 @SessionAttributes(value = {"user"})
 public class UserController extends BaseController<User, Long>  {
 
 	private static final String PASSWORD_FORM = "/password_form";
 
 	public static final String FORM_STATUS_UPDATE = "UPDATE";
 	public static final String FORM_STATUS_SUBMIT = "SUBMIT";
 	
 	
 	/**
 	 * UserController Constructor
 	 * @param userService
 	 * @param userValidator
 	 */
 	@Autowired
 	public UserController(BaseService<?, BaseObject<?, Long>> userService, BaseValidator<User, Long> userValidator) {
 		super(userService, userValidator);
 	}
 
 	/**
 	 * Registers custom editors to bind String values to Classes
 	 * @param dataBinder - used to register custom editors to bind String values to Classes 
 	 * @param request
 	 */
 	@Override
 	@InitBinder
 	public void initBinder(WebDataBinder dataBinder, WebRequest request) {
 		super.initBinder(dataBinder, request);
 	}
 	
 	/**
 	 * Method responsible for setting up the <b>new</b> User form. 
 	 * @param model
 	 * @return view name for the model
 	 */
 	@Override
	@RequestMapping(method = RequestMethod.GET)
 	public String create(Model model) {
 		model.addAttribute(getEntityShortName(), getEntityInstance());
 		return WORKBENCH + getEntityShortName() + FORM;
 	}
 	
 	/**
 	 * Method responsible for setting up the <b>edit</b> User form. 
 	 * @param model
 	 * @return view name for the model
 	 */
 	@Override
 	@RequestMapping(value="/edit/{objectId}", method = RequestMethod.GET)
 	public String edit(@PathVariable("objectId") int objectId, Model model) {
 		model.addAttribute(getEntityShortName(), getBaseService().getById(objectId));
 		return WORKBENCH + getEntityShortName() + FORM;
 	}
 	
 	/**
 	 * Enables an user account.<br>
 	 * @param user - User
 	 * @return View name, to redirect the user to
 	 */
 	@RequestMapping(value="/enableAccount", method=RequestMethod.POST)
     public String enableAccount(User user) {
 		((UserService)getBaseService()).setAccountActive(user, true);
         return REDIRECT + WORKBENCH + getEntityShortName() + SHOW + user.getId();
     }
 	
 	/**
 	 * Disable an user account.<br>
 	 * @param user - User
 	 * @return View name, to redirect the user to
 	 */
 	@RequestMapping(value="/disableAccount", method=RequestMethod.POST)
     public String disableAccount(User user) {
 		((UserService)getBaseService()).setAccountActive(user, false);
 		return REDIRECT + WORKBENCH + getEntityShortName() + SHOW + user.getId();
     }
 	
 	/**
 	 * Method responsible for setting the form when changing password for a User. 
 	 * @param model
 	 * @param userId
 	 * @return view name for the model
 	 */
 	@RequestMapping(value="/changePassword/{userId}", method = RequestMethod.GET)
 	public String changePassword(@PathVariable("userId") int userId, Model model) {
 		model.addAttribute(getEntityShortName(), getBaseService().getById(userId));
 		return WORKBENCH + getEntityShortName() + PASSWORD_FORM;
 	}
 	
 	/**
 	 * Method that validates the change password form for an existing <b>User</b> object, previously submitted by the user
 	 * @param user - the User object
 	 * @param result - BindingResult
 	 * @param status - SessionStatus
 	 * @return view to display the object
 	 * @throws WishopException 
 	 */
 	@RequestMapping(value="/updatePassword", method = RequestMethod.POST)
 	public String updatePassword(@ModelAttribute User user,	BindingResult result, SessionStatus status) throws WishopException {
 		//TODO: CHECK TO SEE If THE CURRENT PASSWORD IS CORRECT
 		((UserValidator)getBaseObjectValidator()).setUserFormStatus(FORM_STATUS_SUBMIT);
 		((UserValidator)getBaseObjectValidator()).validatePasswordChange(result, user, WishopSecurityContext.getSessionUser());
 		if (result.hasErrors()) {
 			return WORKBENCH + getEntityShortName() + PASSWORD_FORM;
 		} else {
 			((UserService)getBaseService()).update(user);
 			status.setComplete();
 			return REDIRECT + WORKBENCH + getEntityShortName() + SHOW + user.getId();
 		}
 	}	
 }
