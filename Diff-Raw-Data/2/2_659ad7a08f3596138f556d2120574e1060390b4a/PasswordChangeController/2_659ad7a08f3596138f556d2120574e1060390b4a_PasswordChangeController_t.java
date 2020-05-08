 package org.otherobjects.cms.controllers;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.otherobjects.cms.security.PasswordChanger;
 import org.otherobjects.cms.security.PasswordService;
 import org.otherobjects.cms.tools.FlashMessageTool;
 import org.otherobjects.cms.util.FlashMessage;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 
 /**
  * Controller handling password changes.
  * 
  * FIXME Need to support password change via old-password.
  * 
  * @author rich
  */
 @Controller
 @RequestMapping("/password-change")
 @SessionAttributes("passwordChanger")
 public class PasswordChangeController
 {
     @Autowired
     private PasswordService passwordService;
 
     @RequestMapping(method = RequestMethod.GET)
     public String setupForm(Model model, HttpServletRequest request) throws IOException
     {
         PasswordChanger passwordChanger = new PasswordChanger();
 
         // Validate change request code
         String crc = request.getParameter("crc");
         if (passwordService.validateChangeRequestCode(crc))
         {
             // Pre-fill in form since valid
             model.addAttribute("validCrc", true);
             passwordChanger.setChangeRequestCode(crc);
         }
         else
         {
             // Could not detect valid CRC
             model.addAttribute("validCrc", false);
         }
         model.addAttribute("passwordChanger", passwordChanger);
         return "/otherobjects/templates/workbench/user-management/change-password";
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String processSubmit(@ModelAttribute("passwordChanger") PasswordChanger passwordChanger, BindingResult result, HttpServletRequest request, SessionStatus status)
     {
 
         new PasswordChangerValidator().validate(passwordChanger, result);
 
         if (result.hasErrors())
         {
             // Return to form and try again
            return "/otherobjects/templates/workbench/user-management/change-password";
         }
         else
         {
             // Change password
             passwordService.changePassword(passwordChanger);
             status.setComplete();
 
             FlashMessageTool flashMessageTool = new FlashMessageTool(request);
             flashMessageTool.flashMessage(FlashMessage.INFO, "Your password has been changed successfully.");
             return "redirect:/otherobjects/login/auth";
         }
     }
 
 }
