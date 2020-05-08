 
 package com.AlphaDevs.Web.Helpers;
 
 import java.util.Date;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionListener;
 
 /**
  *
  * @author Mihindu Gajaba Karunarathne
  * Alpha Development Team (Pvt) Ltd
  * 
  */
 
 public class SessionChecker implements HttpSessionListener
 {
 
     @Override
     public void sessionCreated(HttpSessionEvent se) 
     {
         System.out.println("Session Created " + new Date());
        //MessageHelper.addErrorMessage("Session Created!", "Welcome");
     }
 
     @Override
     public void sessionDestroyed(HttpSessionEvent se) 
     {
         System.out.println("Session Destroyed " + new Date());
        //MessageHelper.addErrorMessage("Session Expired!", "Session Has been expired , Please Login again");
     }
 
 }
