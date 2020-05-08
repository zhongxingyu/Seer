 package it.chalmers.fannysangles.friendzone.bb;
 
 
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.model.Tag;
 import it.chalmers.fannysangles.friendzone.model.managers.UserManager;
 import java.io.Serializable;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  * Bean for handling the delete_user view.
  * @author CaptainTec
  */
 @Named("deleteuser")
 @RequestScoped
 public class DeleteUserBB implements Serializable {
 
     @EJB
     private UserManager userManager;
 
     
     @Inject
     LoginBB login;
 
     /**
      * Deletes the current logged in user
      * @param e 
      */
     public void actionListenerDelete(ActionEvent e){
         if(login.isLoggedIn()){
             userManager.remove(login.getLoggedInUser().getId());
            login.logout();
         }
     }
    
     public String actionDelete(){
         return "success";
     }
     
     public String actionCancel(){
         return "success";
     }
 
     public FriendzoneUser getUser(){
          return login.getLoggedInUser();
     }
     
 }
