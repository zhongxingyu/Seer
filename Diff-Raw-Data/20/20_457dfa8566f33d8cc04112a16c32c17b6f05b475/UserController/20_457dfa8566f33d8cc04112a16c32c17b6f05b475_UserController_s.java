 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.UserType;
 import org.motechproject.ghana.national.service.EmailTemplateService;
 import org.motechproject.ghana.national.service.UserService;
 import org.motechproject.ghana.national.web.form.CreateUserForm;
 import org.motechproject.mrs.exception.UserAlreadyExistsException;
 import org.motechproject.mrs.model.Attribute;
 import org.motechproject.mrs.model.User;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.validation.Valid;
 import java.util.HashMap;
 import java.util.Locale;
 
 @Controller
 @RequestMapping(value = "/admin/users")
 public class UserController {
     private UserService userService;
     private MessageSource messageSource;
 
     public static final String USER_ID = "userId";
     public static final String USER_NAME = "userName";
     public static final String EMAIL = "email";
     public static final String CREATE_USER_FORM = "createUserForm";
     public static final String USER_ALREADY_EXISTS = "user_email_already_exists";
     public static final String NEW_USER_VIEW = "users/new";
     public static final String CREATE_USER_SUCCESS_VIEW = "users/success";
     private EmailTemplateService emailTemplateService;
 
     public UserController() {
     }
 
     @Autowired
     public UserController(UserService userService, MessageSource messageSource, EmailTemplateService emailTemplateService) {
         this.userService = userService;
         this.messageSource = messageSource;
         this.emailTemplateService = emailTemplateService;
     }
 
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newUser(ModelMap modelMap) {
         modelMap.addAttribute("createUserForm", new CreateUserForm());
         modelMap.addAttribute("roles", userService.fetchAllRoles());
         return NEW_USER_VIEW;
     }
 
     @RequestMapping(value = "create", method = RequestMethod.POST)
     public String createUser(@Valid CreateUserForm createUserForm, BindingResult bindingResult, ModelMap model) {
         User user = new User();
         user.firstName(createUserForm.getFirstName()).middleName(createUserForm.getMiddleName()).lastName(createUserForm.getLastName());
         user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_EMAIL, createUserForm.getEmail()));
         user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER, createUserForm.getPhoneNumber()));
        user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE, createUserForm.getRole()));
 
        user.securityRole(UserType.Role.securityRoleFor(createUserForm.getRole()));
        if (UserType.Role.isAdmin(createUserForm.getRole())) user.id(createUserForm.getEmail());
         try {
             HashMap userData = userService.saveUser(user);
             model.put(USER_ID, userData.get("userLoginId"));
             model.put(USER_NAME, user.fullName());
            if (createUserForm.getRole().equals("Super Admin") || createUserForm.getRole().equals("Facility Admin") || createUserForm.getRole().equals("CallCenter Admin"))
                 emailTemplateService.sendEmailUsingTemplates((String) userData.get("userLoginId"), (String) userData.get("password"));
         } catch (UserAlreadyExistsException e) {
             bindingResult.addError(new FieldError(CREATE_USER_FORM, EMAIL, messageSource.getMessage(USER_ALREADY_EXISTS, null, Locale.getDefault())));
             model.addAttribute("roles", userService.fetchAllRoles());
             model.mergeAttributes(bindingResult.getModel());
             return NEW_USER_VIEW;
         }
         return CREATE_USER_SUCCESS_VIEW;
     }
 }
