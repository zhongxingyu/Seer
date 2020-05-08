 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.service.EmailTemplateService;
 import org.motechproject.ghana.national.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 
 @Controller
 @RequestMapping(value = "/forgotPassword")
 public class ForgotPasswordController {
     private UserService userService;
     private EmailTemplateService emailTemplateService;
 
     public ForgotPasswordController() {
     }
 
     @Autowired
     public ForgotPasswordController(UserService userService, EmailTemplateService emailTemplateService) {
         this.userService = userService;
         this.emailTemplateService = emailTemplateService;
     }
 
     @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
     public ModelAndView changePasswordAndSendEmail(HttpServletRequest request) throws Exception {
         ModelAndView modelAndView = new ModelAndView("redirect:/forgotPasswordStatus.jsp");
         String emailId = request.getParameter("emailId");
 
         String newPassword = userService.changePasswordByEmailId(emailId);
         if (!newPassword.equals("")) {
             String emailSentStatus = emailTemplateService.sendEmailUsingTemplates(emailId, newPassword);
             if (emailSentStatus.equals(Constants.EMAIL_SUCCESS))
                 modelAndView.addObject(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_SUCCESS);
             else
                 modelAndView.addObject(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_FAILURE);
         } else {
             modelAndView.addObject(Constants.FORGOT_PASSWORD_MESSAGE, Constants.FORGOT_PASSWORD_USER_NOT_FOUND);
         }
 
         return modelAndView;
     }
 
 
 }
