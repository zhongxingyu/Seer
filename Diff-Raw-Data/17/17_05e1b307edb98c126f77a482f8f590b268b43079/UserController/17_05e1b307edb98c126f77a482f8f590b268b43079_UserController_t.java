 package it.sevenbits.conferences.web.controller;
 
 import it.sevenbits.conferences.domain.*;
 import it.sevenbits.conferences.service.*;
 import it.sevenbits.conferences.service.common.CustomUserDetailsService;
 import it.sevenbits.conferences.utils.file.FileManager;
 import it.sevenbits.conferences.utils.mail.MailSenderUtility;
 import it.sevenbits.conferences.web.form.ChangePasswordForm;
 import it.sevenbits.conferences.web.form.JsonResponse;
 import it.sevenbits.conferences.web.form.LoginForm;
 import it.sevenbits.conferences.web.form.UserRegistrationForm;
 import org.apache.commons.lang3.RandomStringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
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
 
     @Autowired
     private CompanyService companyService;
 
     @Autowired
     private CustomUserDetailsService customUserDetailsService;
 
     @Autowired
     @Qualifier("userRegistrationValidator")
     private Validator validator;
 
     @Autowired
     @Qualifier("loginValidator")
     private Validator loginValidator;
 
     @Autowired
     @Qualifier("changePasswordValidator")
     private Validator changePasswordValidator;
 
 
     private final String GUEST_REGISTRATION_INFO = "Так же, вы зарегистрированы на текущий субботник.";
     private final String REPORT_REGISTRATION_INFO = "Ваша заявка на выступление принята на рассмотрение.";
 
     @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
     public ModelAndView getUserInformation(@PathVariable(value = "userId") final Long userId) {
         ModelAndView modelAndView = new ModelAndView("user-information");
         User user = userService.findUserById(userId);
         List<Report> reports = reportService.findAllPresentedReportsByUser(user);
         modelAndView.addObject("reports",reports);
         modelAndView.addObject("user",user);
         return  modelAndView;
     }
 
     @RequestMapping(value = "/confirmation", method = RequestMethod.GET)
     public ModelAndView confirmUser(
             @RequestParam(value = "confirmation_token", required = true) final String receivedConfirmationToken,
             @RequestParam(value = "confirmation_login", required = true) final String confirmationLogin,
             @RequestParam(value = "conference_status", required = false) final Long conferenceStatus,
             @RequestParam(value = "report_status", required = false) final Long reportStatus
     ) {
         String additionalRegistrationInfo = null;
         User user = userService.getUser(confirmationLogin);
 
         if (user != null || (user.getConfirmationToken().equals(receivedConfirmationToken) && !user.getEnabled())) {
             user.setEnabled(true);
             user = userService.updateUser(user);
             if (conferenceStatus != null && conferenceStatus == 1) {
                 Guest guest = new Guest();
                 guest.setUser(user);
                 Conference currentConference = conferenceService.findNextConference();
                 guest.setConference(currentConference);
                 guestService.addGuest(guest);
                 additionalRegistrationInfo = GUEST_REGISTRATION_INFO;
             }
             if (reportStatus != null) {
                 Report report = reportService.findReportById(reportStatus);
                 report.setUser(user);
                 reportService.updateReport(report);
                 additionalRegistrationInfo = REPORT_REGISTRATION_INFO;
             }
         }
         ModelAndView modelAndView = new ModelAndView("registration-confirm");
         modelAndView.addObject("additionalRegistrationInfo",additionalRegistrationInfo);
         return modelAndView;
     }
 
     @RequestMapping(value = "/registration", method = RequestMethod.GET)
     public ModelAndView usersRegistrationPageGet() {
         ModelAndView modelAndView = new ModelAndView("user-registration");
         UserRegistrationForm userRegistrationForm = new UserRegistrationForm();
         modelAndView.addObject("userRegistrationForm",userRegistrationForm);
         return  modelAndView;
     }
 
     @RequestMapping(value = "/registration", method = RequestMethod.POST)
     public ModelAndView usersRegistrationPagePost(@ModelAttribute(value = "userRegistrationForm") final UserRegistrationForm userRegistrationForm,
             BindingResult bindingResult
     ) {
         ModelAndView response;
         validator.validate(userRegistrationForm,bindingResult);
         if (bindingResult.hasErrors()) {
             Map<String, String> errors = new HashMap<>();
             for (FieldError fieldError: bindingResult.getFieldErrors()) {
                 if (!errors.containsKey(fieldError.getField())) {
                     errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                 }
             }
             response = new ModelAndView("user-registration");
         } else {
             User user = new User();
             user.setFirstName(userRegistrationForm.getFirstName());
             user.setSecondName(userRegistrationForm.getSecondName());
             user.setEmail(userRegistrationForm.getEmail());
             user.setLogin(userRegistrationForm.getEmail());
             PasswordEncoder encoder = new BCryptPasswordEncoder();
             String encodedPassword = encoder.encode(userRegistrationForm.getPassword());
             user.setPassword(encodedPassword);
             Company company = companyService.findCompanyByName(userRegistrationForm.getCompany());
             user.setCompany(company);
             user.setJobPosition(userRegistrationForm.getJobPosition());
             user.setEnabled(false);
             FileManager fileManager = new FileManager();
             if (!userRegistrationForm.getPhoto().isEmpty()) {
                 String photoName = fileManager.saveImage(userRegistrationForm.getPhoto());
                 user.setPhoto(photoName);
             } else {
                 user.setPhoto(null);
             }
             Role role = roleService.findRoleById(1l);
             user.setRole(role);
             String confirmation_token = UUID.randomUUID().toString();
             user.setConfirmationToken(confirmation_token);
             userService.updateUser(user);
             mailSenderUtility.sendConfirmationToken(userRegistrationForm.getEmail(),confirmation_token);
             response = new ModelAndView("user-registration");
         }
         return response;
     }
 
     @RequestMapping(value = "/login", method = RequestMethod.GET)
     public ModelAndView loginGet() {
         ModelAndView modelAndView = new ModelAndView("login");
         return modelAndView;
     }
 
     @RequestMapping(value = "/login", method = RequestMethod.POST)
     @ResponseBody
     public JsonResponse loginPost(@ModelAttribute(value = "loginForm") final LoginForm loginForm, BindingResult bindingResult, HttpServletRequest request) {
         JsonResponse response = new JsonResponse();
         loginValidator.validate(loginForm, bindingResult);
         if (bindingResult.hasErrors()) {
             response.setStatus(JsonResponse.STATUS_FAIL);
             Map<String, String> errors = new HashMap<>();
             for (FieldError fieldError: bindingResult.getFieldErrors()) {
                 if (!errors.containsKey(fieldError.getField())) {
                     errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                 }
             }
             errors.put("message", "Логин или пароль введены неверно");
             response.setResult(errors);
         } else {
             User user = userService.getUser(loginForm.getLogin());
             if (user == null || !user.getPassword().equals(loginForm.getPassword())) {
                 Map<String, String> errors = new HashMap<>();
                 errors.put("message","Логин или пароль введены неверно");
                 response.setResult(errors);
                 response.setStatus(JsonResponse.STATUS_FAIL);
             } else {
                 UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword());
                 UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
                 token.setDetails(userDetails);
                 SecurityContext securityContext = SecurityContextHolder.getContext();
                 securityContext.setAuthentication(token);
                 response.setStatus(JsonResponse.STATUS_SUCCESS);
                 // Create a new session and add the security context.
                 HttpSession session = request.getSession(true);
                 session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
             }
         }
         return response;
     }
 
     @RequestMapping(value = "/login/failed", method = RequestMethod.GET)
     public ModelAndView loginFailed() {
         ModelAndView modelAndView = new ModelAndView("login");
         modelAndView.addObject("error","true");
         return modelAndView;
     }
 
     @RequestMapping(value = "/change-password", method = RequestMethod.GET)
     public ModelAndView changePasswordGet() {
         ModelAndView modelAndView = new ModelAndView("change-password");
         return modelAndView;
     }
 
    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
     @ResponseBody
     public JsonResponse changePasswordPost(@ModelAttribute(value = "ChangePasswordForm") final ChangePasswordForm changePasswordForm, BindingResult bindingResult) {
         JsonResponse jsonResponse;
         changePasswordValidator.validate(changePasswordForm, bindingResult);
         if (bindingResult.hasErrors()) {
             jsonResponse = getFailureResponse(bindingResult, "Неверный e-mail или пароль.");
         } else {
             User user = userService.getUserByEmail(changePasswordForm.getEmail());
             PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
             String encodedNewPassword = passwordEncoder.encode(changePasswordForm.getNewPassword());
             user.setPassword(encodedNewPassword);
             userService.updateUser(user);
             jsonResponse = new JsonResponse();
             jsonResponse.setStatus(JsonResponse.STATUS_SUCCESS);
            Map<String, String> resultMessage = new HashMap<>();
            resultMessage.put("message", "Вам пароль успешно изменен.");
            jsonResponse.setResult(resultMessage);
         }
         return jsonResponse;
     }
 
     private String setNewRandomPassword(User user, final int length) {
         String newPassword = RandomStringUtils.random(length,true,true);
         PasswordEncoder encoder = new BCryptPasswordEncoder();
         String encodedNewPassword = encoder.encode(newPassword);
         user.setPassword(encodedNewPassword);
         userService.updateUser(user);
         return newPassword;
     }
 
     private JsonResponse getFailureResponse(final BindingResult bindingResult, final String commonMessage) {
         JsonResponse response = new JsonResponse();
         response.setStatus(JsonResponse.STATUS_FAIL);
         Map<String, String> errors = new HashMap<>();
         for (FieldError fieldError: bindingResult.getFieldErrors()) {
             if (!errors.containsKey(fieldError.getField())) {
                 errors.put(fieldError.getField(), fieldError.getDefaultMessage());
             }
         }
         errors.put("message", commonMessage);
         response.setResult(errors);
         return response;
     }
 }
