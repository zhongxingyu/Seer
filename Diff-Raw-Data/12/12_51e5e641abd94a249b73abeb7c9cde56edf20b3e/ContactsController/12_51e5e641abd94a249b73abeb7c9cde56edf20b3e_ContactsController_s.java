 package com.financial.pyramid.web;
 
 import com.financial.pyramid.domain.Contact;
import com.financial.pyramid.domain.User;
 import com.financial.pyramid.service.ContactService;
 import com.financial.pyramid.service.EmailService;
 import com.financial.pyramid.service.SettingsService;
 import com.financial.pyramid.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: dbudunov
  * Date: 22.08.13
  * Time: 14:03
  */
 @Controller
 @RequestMapping("/contacts")
 public class ContactsController {
 
     @Autowired
     ContactService contactService;
 
     @Autowired
     EmailService emailService;
 
     @Autowired
     SettingsService settingsService;
 
     @Autowired
     UserService userService;
 
     @RequestMapping(value = "/save", method = RequestMethod.POST)
     public String save(ModelMap model, @ModelAttribute("contact") Contact contact) {
         contactService.save(contact);
         return "redirect:/pyramid/admin/contact_settings";
     }
 
     @RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
     public String remove(ModelMap model, @PathVariable Long id) {
         contactService.remove(id);
         return "redirect:/pyramid/admin/contact_settings";
     }
 
     @RequestMapping(value = "/feedback", method = RequestMethod.POST)
     public String sendFeedback(ModelMap model, @RequestParam(value = "feedback") String feedback) {
         String adminEmail = settingsService.getProperty("feedbackReceiverEmail");
        User adminUser = userService.findByEmail(adminEmail);
         Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         String currentUserName = "";
         String currentUserEmail = "";
         if (user.equals("anonymousUser")) {
             currentUserName = "anonymous";
             currentUserEmail = "unknown";
         } else {
             User currentUser = (User) user;
            currentUserName = currentUser.getName();
            currentUserEmail = currentUser.getEmail();
         }
         Map map = new HashMap();
         map.put("name", adminUser.getName());
         map.put("username", currentUserName);
         map.put("usermail", currentUserEmail);
         map.put("feedback", feedback);
         emailService.setTemplate("feedback-template");
         boolean result = emailService.sendEmail(adminUser, map);
         return "redirect:/pyramid/contacts?sent=" + result;
     }
 }
