 
 import it.chalmers.fannysangles.friendzone.bb.LoginBB;
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
  * A bean for managing the edit_user view.
  * @author CaptainTec
  */
 @Named("edituser")
 @RequestScoped
 public class EditUserBB implements Serializable {
 
     @EJB
     private UserManager userManager;
     @Inject
     LoginBB login;
     
     private FriendzoneUser user;
     private String passwordRepeat;
 
     
     public void actionListenerEdit(ActionEvent e){
       
        userManager.update(this.getUser());
     }
     public String actionEdit(){
         //should check
         return "success";
     }
     
     public String actionDelete(){
         //should check
         return "success";
     }
     
 
     public FriendzoneUser getUser(){
        if(user == null) {
            user = login.getLoggedInUser();
        }
        return user;
     }
     
     public void setUser(FriendzoneUser user){
        this.user = user;
     }
         
     public String getPasswordRepeat() {
         return passwordRepeat;
     }
 
     public void setPasswordRepeat(String passwordRepeat) {
         this.passwordRepeat = passwordRepeat;
     }
 }
