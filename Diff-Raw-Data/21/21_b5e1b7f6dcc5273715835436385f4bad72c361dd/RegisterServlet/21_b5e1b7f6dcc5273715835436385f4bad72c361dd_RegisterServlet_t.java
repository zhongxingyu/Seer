 package com.cs739.app.servlet.master;
 
import java.io.IOException;

import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cs739.app.model.PlopboxUser;
import com.cs739.app.service.CookieService;
 import com.cs739.app.service.master.UserService;
 import com.cs739.app.servlet.AbstractPlopboxServlet;
import com.cs739.app.util.AppConstants;
 import com.cs739.app.util.AppConstants.MasterPages;
 
 public class RegisterServlet extends AbstractPlopboxServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = 7330238628119085009L;
     
     private static final Logger log = LoggerFactory
             .getLogger(RegisterServlet.class);
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) {
         // Not doin anything right now...
         forward(request, response, MasterPages.REGISTER.toString());
     }
     
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) {
         log.debug("Got a POST request!");
         
         String username = request.getParameter("username");
         String password = request.getParameter("password");
         
         PlopboxUser newUser = new PlopboxUser();
         newUser.setUsername(username);
         newUser.setPassword(UserService.hashString(password));
         
         UserService.saveNewPlopboxUser(newUser);
         log.debug("Just registered new user!");
         log.debug("Username: " + username);
        Cookie idCookie = CookieService.createNewCookie(AppConstants.USER_ID, newUser.getId().toString());
        Cookie nameCookie = CookieService.createNewCookie(AppConstants.USERNAME, newUser.getUsername());
        response.addCookie(idCookie);
        response.addCookie(nameCookie);
        try {
            response.sendRedirect("home");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     }
 
 }
