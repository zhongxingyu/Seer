 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.controllers;
 
 import com.team33.entities.Account;
 import com.team33.services.AccountServiceImpl;
 import com.team33.services.exception.AccountNotActivatedException;
 import com.team33.services.exception.AccountNotFoundException;
 import com.team33.services.exception.AuthenticationException;
 import com.team33.services.exception.LoginException;
 import javax.servlet.http.HttpSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 /**
  *
  * @author Samual Martelli
  */
 @Controller
 @RequestMapping(value = "/login.htm")
 public class LoginController {
     public static final String ACCOUNT_ATTRIBUTE = "account";
 
      // tells the application context to inject an instance of AccountServiceImpl here
     @Autowired
     private AccountServiceImpl accountServiceImpl;
 
     public AccountServiceImpl getAccountSerrvice(){
         return this.accountServiceImpl;
     }
     public void setAccountServiceImpl(AccountServiceImpl service) {
         this.accountServiceImpl = service;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String login() {
         return "login";
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String handleLogin(@RequestParam String username,
             @RequestParam String password, RedirectAttributes redirect, HttpSession session)
             throws AuthenticationException, LoginException {
         try {
             if (username == null || username.equals("")
                     || password == null || password.equals("")) {
                 throw new LoginException("Invalid login info!");
             }
             Account account = this.accountServiceImpl.loginAccount(username, password);
             session.setAttribute(ACCOUNT_ATTRIBUTE, account);
             return "redirect:/index.htm";
         } catch (LoginException le) {
             redirect.addFlashAttribute("exception", le);
             return "redirect:/login.htm";
         } catch (AuthenticationException ae) {
             redirect.addFlashAttribute("exception", ae);
             return "redirect:/login.htm";
         } catch (AccountNotActivatedException ae) {
             redirect.addFlashAttribute("exception", ae);
             return "redirect:/login.htm";
         } catch (AccountNotFoundException ae) {
             redirect.addFlashAttribute("exception", ae);
             return "redirect:/login.htm";
         }
     }
 }
