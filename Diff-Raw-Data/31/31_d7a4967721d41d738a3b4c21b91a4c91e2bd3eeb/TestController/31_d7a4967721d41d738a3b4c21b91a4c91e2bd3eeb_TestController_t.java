 package com.financial.pyramid.web;
 
 import com.financial.pyramid.domain.Account;
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.service.AccountService;
 import com.financial.pyramid.service.UserService;
 import com.financial.pyramid.utils.Session;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * User: dbudunov
  * Date: 29.08.13
  * Time: 14:12
  */
 @Controller
 @RequestMapping("/test")
 public class TestController extends AbstractController {
 
     @Autowired
     private AccountService accountService;
 
    @Autowired
    private UserService userService;

     @ResponseBody
     @RequestMapping(value = "/pay", method = RequestMethod.GET)
     public String payTest(ModelMap model) {
         User currentUser = Session.getCurrentUser();
        Account account = userService.getAccount(currentUser);
         accountService.calculateSum(account, 0.01);
         return "Done";
     }
 
     @ResponseBody
     @RequestMapping(value = "/extend", method = RequestMethod.GET)
     public String extendTest(ModelMap model) {
         User currentUser = Session.getCurrentUser();
        Account account = userService.getAccount(currentUser);
         accountService.activate(account);
         return "Done";
     }
 
 }
