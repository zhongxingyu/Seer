 package com.directi.train.tweetapp.controllers;
 
import com.directi.train.tweetapp.services.AuthStore;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
import javax.servlet.http.HttpServletRequest;

 /**
  * Created with IntelliJ IDEA.
  * User: elricl
  * Date: 23/7/12
  * Time: 1:24 PM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class GenericController {
    private AuthStore authStore;

    @Autowired
    public GenericController(AuthStore authStore) {
        this.authStore = authStore;
    }

     @RequestMapping("/")
    public String index(HttpServletRequest request) {
        if (authStore.isValid((String) request.getAttribute("accesstoken"))) {
            return "tweet";
        }
         return "index";
     }
 }
