 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.bb;
 
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.model.managers.UserManager;
 import java.io.Serializable;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author Pontus
  */
 @Named("viewprofile")
 @RequestScoped
 public class ViewProfileBB implements Serializable {
 
     @EJB
     private UserManager userManager;
     @Inject
     private LoginBB login;
     private Long userID;
     private FriendzoneUser visitedUser;
     private FriendzoneUser visitingUser;
 
     public void init() {
         //nothing to see here
     }
 
     public FriendzoneUser getUser() {
         return userManager.find(userID);
     }
 
     public String getFollowButtonText() {
        if (login.getLoggedInUser().equals(userManager.find(userID))) {
            return "";
         } else {
            if (!login.getLoggedInUser().getFollowedUsers().contains(userManager.find(userID))) {
                return "Follow";
            } else {
                return "Unfollow";
            }
         }
     }
 
     public String toggleFollow() {
         return "success";
     }

     public void actionListener(Long id) {
         FriendzoneUser visited = userManager.find(id);
         FriendzoneUser visiting = login.getLoggedInUser();

         if (login.isLoggedIn()) {
             if (!visiting.equals(visited)) {
                 if (visiting.getFollowedUsers().contains(visited)) {
                     visiting.unfollow(visited);
                 } else {
                     visiting.follow(visited);
                 }
             }
         }
     }
 
     public Long getUserID() {
         return userID;
     }
 
     public void setUserID(Long userID) {
         this.userID = userID;
     }
 
     public FriendzoneUser getVisitedUser() {
         return visitedUser;
     }
 
     public void setVisitedUser(FriendzoneUser visitedUser) {
         this.visitedUser = visitedUser;
     }
 
     public FriendzoneUser getVisitingUser() {
         return visitingUser;
     }
 
     public void setVisitingUser(FriendzoneUser visitingUser) {
         this.visitingUser = visitingUser;
     }
 }
