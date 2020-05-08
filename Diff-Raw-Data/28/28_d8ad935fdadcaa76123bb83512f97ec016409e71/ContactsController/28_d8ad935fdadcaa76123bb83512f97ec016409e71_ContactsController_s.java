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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
 
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
 
    @RequestMapping(value = "/sendFeedback", method = RequestMethod.POST)
    public String sendFeedback(ModelMap model, @PathVariable String feedback) {
         String adminEmail = settingsService.getProperty("feedbackReceiverEmail");
         User adminUser = userService.findByEmail(adminEmail);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         Map map = new HashMap();
         map.put("name", adminUser.getName());
        map.put("username", currentUser.getName());
        map.put("usermail", currentUser.getEmail());
         map.put("feedback", feedback);
         emailService.setTemplate("feedback-template");
         boolean result = emailService.sendEmail(adminUser, map);
         return "redirect:/pyramid/contacts?sent=" + result;
     }
 }
