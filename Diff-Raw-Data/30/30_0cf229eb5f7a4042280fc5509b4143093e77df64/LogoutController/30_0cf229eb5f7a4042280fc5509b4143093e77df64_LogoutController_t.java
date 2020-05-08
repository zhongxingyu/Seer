 package com.zhiweiwang.datong.controller;
 
 import com.zhiweiwang.datong.DTContants;
 import com.zhiweiwang.datong.DTMessage;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import java.io.UnsupportedEncodingException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 @Controller
 @RequestMapping("/logout")
 public class LogoutController {
   
     @RequestMapping(method = GET)
     public ModelAndView get(HttpServletRequest request) throws UnsupportedEncodingException {
 
         HttpSession session = request.getSession();
         session.removeAttribute(DTContants.USER_IN_SESSION);
        session.removeAttribute(DTContants.IMG_PATH);
         ModelAndView mav = new ModelAndView();
 
         mav.setViewName("redirect:/message");
         mav.getModel().remove(DTContants.USER_IN_SESSION);
         mav.getModel().put(DTContants.MSG_ERRER, DTMessage.LOGOUT_SUCCESS);
         return mav;
     }
 
 }
