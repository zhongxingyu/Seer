 package com.skplanet.seminar.spring.board.controller;
 
 import com.skplanet.seminar.spring.board.entity.User;
 import com.skplanet.seminar.spring.board.service.AccountService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpSession;
 
 /**
  * Created with IntelliJ IDEA.
  * User: synusia
  * Date: 13. 6. 20
  * Time: 오전 10:29
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/account")
 public class AccountController {
     @Autowired
     AccountService accountService;
 
     @RequestMapping(value = "login", method = RequestMethod.GET)
     public String loginForm() {
         return "account/login";
     }
 
     @RequestMapping(value = "login", method = RequestMethod.POST)
    public String login(@ModelAttribute User user, HttpSession httpSession) {
         User loginUser = accountService.loginUser(user.getId(), user.getPw());
         httpSession.setAttribute("loginUser", loginUser);
 
         return "redirect:/board/boardList";
     }
 
     @RequestMapping(value = "logout", method = RequestMethod.GET)
     public String logout(HttpSession httpSession) {
         httpSession.setAttribute("loginUser", null);
 
         return "account/login";
     }
 }
