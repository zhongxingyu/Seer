 package com.potatorental.controller;
 
 import com.potatorental.model.Account.AccountType;
 import com.potatorental.model.Customer;
 import com.potatorental.model.Location;
 import com.potatorental.repository.AccountDao;
 import com.potatorental.repository.LocationDao;
 import com.potatorental.repository.PersonDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 /**
  * User: Milky
  * Date: 4/22/13
  * Time: 4:52 AM
  */
 @Controller
 @RequestMapping("/signup")
 @SessionAttributes("customer")
 public class SignUpController {
 
     private PersonDao personDao;
     private LocationDao locationDao;
     private AccountDao accountDao;
 
     private Customer customer;
 
     @Autowired
     public SignUpController(PersonDao personDao, LocationDao locationDao, AccountDao accountDao) {
         this.personDao = personDao;
         this.locationDao = locationDao;
         this.accountDao = accountDao;
     }
 
     @ModelAttribute("signupForm")
     public Customer newCustomer() {
         if (customer != null)
             return customer;
         return (customer = new Customer());
     }
 
     @ModelAttribute("locationForm")
     public Location newLocation() {
         return new Location();
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String getSignUp(Model model) {
         return "signup";
     }
 
 /*    @RequestMapping(method = RequestMethod.POST)
     public String signUp(@Valid @ModelAttribute("signupForm") Customer customer, BindingResult bindingResult,
                          Model model, RedirectAttributes redirectAttrs, SessionStatus sessionStatus,
                          @RequestParam("state") String state, @RequestParam("city") String city) {
         if (bindingResult.hasErrors()) {
             model.addAttribute("message", "There is an error with the form submission");
             return null;
         }
 
         *//*Need to validate zipcode, haven't done that*//*
         try {
             personDao.insertCustomer(customer, new Location(customer.getZipCode(), state, city));
         } catch (DataIntegrityViolationException e) {
             model.addAttribute("message", "email has already been used");
             return null;
         }
 
         sessionStatus.setComplete();
         *//* TODO need to authenticate user so that they will automatically login after signup*//*
         authenticateUser();
         return "redirect:/users/" + customer.getEmail();
     }*/
 
     @RequestMapping(value = "signup_1", method = RequestMethod.POST)
     public String signupForm_1(@Valid @ModelAttribute("signupForm") Customer customer, BindingResult result,
                                HttpSession session, RedirectAttributes redirectAttributes) {
         if (result.hasErrors()) {
             redirectAttributes.addFlashAttribute("message", "There is an error");
             return "signup";
         }
         if (personDao.isEmailExist(customer.getEmail())) {
             redirectAttributes.addFlashAttribute("message", "Email is take");
             return "signup";
         }
 
         session.setAttribute("customer", customer);
         return "redirect:/signup/signup_2";
     }
 
     @RequestMapping(value = "signup_2", method = RequestMethod.GET)
     public String getSignUp_2() {
         return "signup_2";
     }
 
     @RequestMapping(value = "signup_2", method = RequestMethod.POST)
     public String signupForm_2(@Valid @ModelAttribute("locationForm") Location location, BindingResult result,
                                HttpSession session, RedirectAttributes redirectAttributes,SessionStatus sessionStatus,
                                @RequestParam String account, @RequestParam String address){
         if (result.hasErrors()) {
             redirectAttributes.addFlashAttribute("message", "There is an error in form data");
             return "redirect:/signup/signup_2";
         }
 
         Customer customer = (Customer) session.getAttribute("customer");
         customer.setAddress(address);
         customer.setZipCode(location.getZipCode());
 
         personDao.insertCustomer(customer, location);
         accountDao.insertAccount(customer, AccountType.valueOf(account));
 
         session.invalidate();
         sessionStatus.setComplete();;
         return "redirect:/";
     }
 

     private void authenticateUser() {
 
     }
 }
