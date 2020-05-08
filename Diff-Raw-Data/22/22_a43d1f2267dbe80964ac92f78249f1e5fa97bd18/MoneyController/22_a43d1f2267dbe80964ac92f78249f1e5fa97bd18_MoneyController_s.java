 package com.financial.pyramid.web;
 
 import com.financial.pyramid.service.PerfectMoneyService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * User: dbudunov
  * Date: 06.08.13
  * Time: 21:06
  */
 @Controller
 @RequestMapping("/money")
 public class MoneyController {
 
     @Autowired
     private PerfectMoneyService perfectMoneyService;
 
     @RequestMapping(value = "/", method = RequestMethod.GET)
     public ModelAndView get(ModelMap model){
         return new ModelAndView("balance");
     }
 
     @ResponseBody
     @RequestMapping(value = "/balance", method = RequestMethod.POST)
     public String getBalance(ModelMap model,
                              @ModelAttribute("accountID") String accountId,
                              @ModelAttribute("password") String password) {
        String result = perfectMoneyService.getAccountBalance(accountId, password);
        return null;
     }
 
 }
