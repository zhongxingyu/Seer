 package it.sevenbits.conferences.web.controller;
 
 import it.sevenbits.conferences.domain.*;
 import it.sevenbits.conferences.service.*;
 import it.sevenbits.conferences.utils.mail.MailSenderUtility;
 import it.sevenbits.conferences.web.form.JsonResponse;
 import it.sevenbits.conferences.web.form.UserRegistrationForm;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.*;
 
 
 @Controller
 @RequestMapping(value = "user")
 public class UserController {
 
     @Autowired
     private UserService userService;
 
     @Autowired
     private MailSenderUtility mailSenderUtility;
 
     @Autowired
     private RoleService roleService;
 
     @Autowired
     private ConferenceService conferenceService;
 
     @Autowired
     private GuestService guestService;
 
     @Autowired
     private ReportService reportService;
 
     @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
     public ModelAndView userInformation(
             @PathVariable(value = "userId") final Long userId) {
         ModelAndView modelAndView = new ModelAndView("user-information");
        User user = userService.findUserById(userId);
         List<Report> reports = reportService.findAllPresentedReportsByUser(user);
         modelAndView.addObject("reports",reports);
         modelAndView.addObject("user",user);
         return  modelAndView;
     }
 
     @RequestMapping(value = "/confirmation", method = RequestMethod.GET)
     public ModelAndView confirmUser(
             @RequestParam(value = "confirmation_token", required = true) final String received_confirmation_token,
             @RequestParam(value = "confirmation_login", required = true) final String confirmation_login,
             @RequestParam(value = "conference_status", required = false) final Long conference_status
     ) {
 
         User user = userService.getUser(confirmation_login);
         if (user.getConfirmationToken().equals(received_confirmation_token)) {
             user.setEnabled(true);
             user = userService.updateUser(user);
         }
         if (conference_status == 1) {
             Guest guest = new Guest();
             guest.setUser(user);
             Conference currentConference = conferenceService.findNextConference();
             guest.setConference(currentConference);
             guestService.addGuest(guest);
         }
         ModelAndView modelAndView = new ModelAndView("redirect:/");
         return modelAndView;
     }
 
     @RequestMapping(value = "/registration", method = RequestMethod.GET)
     public ModelAndView showUsersRegistrationPage() {
         ModelAndView modelAndView = new ModelAndView("user-registration");
         return  modelAndView;
     }
 
     @Autowired
     @Qualifier("userRegistrationValidator")
     private Validator validator;
 
     @RequestMapping(value = "/registration", method = RequestMethod.POST)
     @ResponseBody
     public JsonResponse registerUser(@ModelAttribute(value = "userRegistrationForm") final UserRegistrationForm userRegistrationForm,
             BindingResult bindingResult
     ) {
         JsonResponse response = new JsonResponse();
         validator.validate(userRegistrationForm,bindingResult);
         if (bindingResult.hasErrors()) {
             response.setStatus(JsonResponse.STATUS_FAIL);
             Map<String, String> errors = new HashMap<>();
             for (FieldError fieldError: bindingResult.getFieldErrors()) {
                 if (!errors.containsKey(fieldError.getField())) {
                     errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                 }
             }
             errors.put("message", "Форма заполнена неверно.");
             response.setResult(errors);
         } else {
             response.setStatus(JsonResponse.STATUS_SUCCESS);
             User user = new User();
             user.setFirstName(userRegistrationForm.getFirstName());
             user.setSecondName(userRegistrationForm.getSecondName());
             user.setEmail(userRegistrationForm.getEmail());
             user.setLogin(userRegistrationForm.getEmail());
             user.setPassword(userRegistrationForm.getPassword());
             user.setJobPosition(userRegistrationForm.getJobPosition());
             user.setEnabled(false);
             Role role = roleService.findRoleById(1l);
             user.setRole(role);
             String confirmation_token = UUID.randomUUID().toString();
             user.setConfirmationToken(confirmation_token);
             userService.updateUser(user);
             mailSenderUtility.sendConfirmationToken(userRegistrationForm.getEmail(),confirmation_token);
             response.setStatus(JsonResponse.STATUS_SUCCESS);
             response.setResult(Collections.singletonMap("message", "На ваш email послано письмо для подтверждения."));
         }
         return response;
     }
 }
