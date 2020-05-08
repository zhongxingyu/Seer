 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.bb;
 
 import it.chalmers.fannysangles.friendzone.model.EventPost;
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.model.Tag;
 import it.chalmers.fannysangles.friendzone.model.managers.EventPostManager;
 import it.chalmers.fannysangles.friendzone.model.managers.UserFactory;
 import it.chalmers.fannysangles.friendzone.model.managers.UserManager;
 import java.io.Serializable;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author CaptainTec
  */
 @Named("adduser")
 @RequestScoped
 public class AddUserBB implements Serializable {
 
     @EJB
    
     private UserManager userManager;
     private String username;
     private String password;
     private String email;
     private String name;
     private int age;
     private String picture;
     private List<Tag> tags;

    @Inject
    public void setPostManager(UserManager um) {
        this.userManager = um;
    }
     
     public void actionListener(ActionEvent e){
         //send mail
         userManager.add(new FriendzoneUser(username, password, email, name, age, picture, tags));
     }
     
     public String action(){
         return "index";
     }
 }
