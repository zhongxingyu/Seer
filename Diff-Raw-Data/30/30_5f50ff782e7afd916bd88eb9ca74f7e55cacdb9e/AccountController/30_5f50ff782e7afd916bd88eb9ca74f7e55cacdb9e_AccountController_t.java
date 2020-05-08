 package com.potatorental.controller;
 
 import com.potatorental.repository.PersonDao;
 import com.potatorental.model.Customer;
 import com.potatorental.model.Person;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import java.security.Principal;
 
 /**
  * User: Milky
  * Date: 5/6/13
  * Time: 2:07 AM
  */
 @Controller
 @RequestMapping("/account")
 @SessionAttributes({"user", "customer", "employee"})
 public class AccountController {
 
     private final PersonDao personDao;
 
     @Autowired
     public AccountController(PersonDao personDao) {
         this.personDao = personDao;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String getSettings(ModelMap modelMap, Principal principal) {
         modelMap.addAttribute("message", "This is going to be " + principal.getName() + "'s profile setting");
 
         if (modelMap.get("user") == null) {
             Person person = personDao.getPersonByEmail(principal.getName());
             modelMap.addAttribute("user", person);
             if (person instanceof Customer)
                 modelMap.addAttribute("customer", true);
             else
                 modelMap.addAttribute("employee", true);
         }
 
         return "account";
     }
 
     @RequestMapping(value = "queue", method = RequestMethod.GET)
     public String getQueue() {
         return "queue";
     }
 
    @RequestMapping(value = "rental", method = RequestMethod.GET)
    public String getRental(ModelMap modelMap, Principal principal) {

        return "rental";
    }

 }
