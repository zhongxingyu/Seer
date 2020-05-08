 package org.homebudget.controllers;
 
import javax.annotation.Resource;
import org.homebudget.dao.UserRepository;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class LoginController {
 
   @Resource
   private UserRepository userRepositoryDao;

    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public String printWelcome(ModelMap model) {
 
       // User user = (User) SecurityContextHolder.getContext()
       // .getAuthentication().getPrincipal();
       // String nickname = user.getUsername();
       //
       // UserDetails userDetails = userRepositoryDao
       // .findByUserUsername(nickname);
 
       // model.addAttribute("username", userDetails.getUserName());
       // model.addAttribute("usersurname", userDetails.getUserSurname());
       // model.addAttribute("message",
       // "Spring Security login + database example");
       return "main";
 
    }
 
    @RequestMapping(value = "/welcome", method = RequestMethod.GET)
    public String login(ModelMap model) {
 
       return "welcome";
 
    }
 
    @RequestMapping(value = "/loginfailed", method = RequestMethod.GET)
    public String loginerror(ModelMap model) {
 
       model.addAttribute("error", "true");
       return "welcome";
 
    }
 
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(ModelMap model) {
 
       return "forward:/welcome.html";
    }
 
   public UserRepository getHibernateDaoImpl() {

      return userRepositoryDao;
   }

   public void setHibernateDaoImpl(UserRepository hibernateDaoImpl) {

      this.userRepositoryDao = hibernateDaoImpl;
   }

 }
