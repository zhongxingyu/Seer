 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.dao.Order;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.OrderManager;
 import com.exadel.borsch.web.users.UserUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import java.security.Principal;
 
 /**
  *
  * @author Vlad
  */
 @Controller
 public class HomeController {
 
     @Autowired
     private ManagerFactory managerFactory;
 
     @RequestMapping("/home")
     public String processPageRequest(Model model, Principal principal) {
        return pageRequest(model, principal);
    }

    @RequestMapping("/")
    public String processDefaultPageRequest(Model model, Principal principal) {
        return pageRequest(model, principal);
    }

    public String pageRequest(Model model, Principal principal) {
         OrderManager orderManager = managerFactory.getOrderManager();
         User user = UserUtils.getUserByPrincipal(principal);
 
         Order order = orderManager.getCurrentOrderForUser(user);
         model.addAttribute(order);
 
         return ViewURLs.HOME_PAGE;
     }
 }
