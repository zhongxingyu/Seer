 package com.cs739.app.servlet.master;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cs739.app.model.PlopboxUser;
 import com.cs739.app.service.master.UserService;
 import com.cs739.app.servlet.AbstractPlopboxServlet;
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
        
        forward(request, response, MasterPages.HOME.toString());
        
     }
 
 }
