 package com.easytag.core.managers;
 
 import com.easytag.core.entity.jpa.*;
 import com.easytag.core.enums.UserType;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 
 @Startup @Singleton
 public class Application {
     
     private static Long guestUserId = null;
     
     @EJB
     private UserManagerLocal um;
     
     @PostConstruct
     private void init() {
         // create default "guest" account
         initGuestUser();
         
     }
     
     private void initGuestUser() {
         User guestUser = um.getUserByEmail("guest@tagscool.com");
         if (guestUser == null) {
             guestUser = createGuestUser();
         }
         if (guestUser != null) {
             guestUserId = guestUser.getId();
             System.out.println("Guest User created. ID = " + guestUserId);
            UserProfile profile = um.getUserProfile(guestUser);
            profile.setFirstName("Public");
            um.updateUserProfile(profile);
         }
     }
     
     private User createGuestUser() {
         try {
             User guest = um.registerUser("guest@tagscool.com", "guest", UserType.USER);
             return guest;
         } catch (Exception ex) {
             System.out.println("Failed to create guest user!");
             ex.printStackTrace();
         } 
         return null;
     }
     
     public static Long getGuestUserId() {
         return guestUserId;
     } 
     
 }
