 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.cognitor.server.registration.web.controller;
 
 import org.cognitor.server.platform.user.domain.User;
 import org.cognitor.server.platform.user.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.cognitor.server.platform.web.util.UrlUtil.createQueryString;
 
 /**
  * @author patrick
  */
 @Controller
 public class RegistrationController {
 
     private static final String REGISTRATION_PAGE = "registration";
     private static final String SECURITY_CHAIN_URL = "/login";
     
     @Autowired
     private UserService userService;
 
     @RequestMapping(value = "/registration")
     public ModelAndView enterPage(HttpServletRequest request) {
         ModelAndView modelAndView = new ModelAndView(REGISTRATION_PAGE);
         modelAndView.addObject("registrationPageUrl", getRegistrationPageUrl(request));
         return modelAndView;
     }
 
     @RequestMapping(value = "/registration", method = RequestMethod.POST)
     public ModelAndView registerUser(@Valid @ModelAttribute RegistrationFormBean formBean,
                                      BindingResult bindingResult,
                                      HttpServletRequest request) {
         if (bindingResult.hasErrors()) {
             return createErrorView(bindingResult.getFieldErrors(), request);
         }
         try {
             userService.registerUser(getUserFromBean(formBean));
         } catch (IllegalStateException ise) {
            FieldError error = new FieldError("User", "email", "User already exists.");
             bindingResult.addError(error);
             return createErrorView(bindingResult.getFieldErrors(), request);
         }
         ModelAndView modelAndView = new ModelAndView("registrationSuccess");
         modelAndView.addObject("loginUrl", getLoginUrl(request));
         return modelAndView;
     }
 
     private User getUserFromBean(RegistrationFormBean formBean) {
         return new User(formBean.getEmail(), formBean.getPassword());
     }
 
     @ModelAttribute
     private RegistrationFormBean createNewUser() {
         return new RegistrationFormBean();
     }
     
     private ModelAndView createErrorView(List<FieldError> errors, HttpServletRequest request) {
         ModelAndView modelAndView = new ModelAndView(REGISTRATION_PAGE);
         modelAndView.addObject("registrationPageUrl", getRegistrationPageUrl(request));
         modelAndView.addObject("errors", errors);
         return modelAndView;
     }
 
     @RequestMapping(value="/", method = RequestMethod.GET)
     public ModelAndView showLogin(HttpServletRequest request) {
         Map<String, String> model = new HashMap<String, String>();
         model.put("actionUrl", getLoginActionUrl(request));
         model.put("registrationPageUrl", getRegistrationPageUrl(request));
         return new ModelAndView("login", model);
     }
 
     @RequestMapping(value = "/loginFailed", method = RequestMethod.GET)
     public ModelAndView loginFailed(HttpServletRequest request) {
         ModelAndView model = showLogin(request);
         model.addObject("error", "login.badCredentials");
         return model;
     }
 
     @RequestMapping(value = "loginSuccess")
     @ResponseBody
     public String showLoginSuccessPage() {
         return "Success";
     }
 
     private String getLoginUrl(HttpServletRequest request) {
         return appendQueryToUrl("/", request.getQueryString());
     }
 
     private String getLoginActionUrl(HttpServletRequest request) {
         return appendQueryToUrl(SECURITY_CHAIN_URL, request.getQueryString());
     }
 
     private String getRegistrationPageUrl(HttpServletRequest request) {
         return appendQueryToUrl("/" + REGISTRATION_PAGE + ".html", request.getQueryString());
     }
 
     private String appendQueryToUrl(String urlPath, String queryParameter) {
         StringBuilder urlBuilder = new StringBuilder(urlPath);
         urlBuilder.append(createQueryString(queryParameter));
         return urlBuilder.toString();
     }
 }
