 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.bb;
 
 import it.chalmers.fannysangles.friendzone.model.EventPost;
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.model.managers.EventPostManager;
 import it.chalmers.fannysangles.friendzone.model.managers.UserFactory;
 import it.chalmers.fannysangles.friendzone.model.managers.UserManager;
 import java.io.Serializable;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author marcusisaksson
  */
 @Named("addevent")
 @RequestScoped
 public class AddEventPostBB implements Serializable {
     
     @EJB
     private EventPostManager eventPostManager;
     
     private String title;
     private String description;
     private String username;
     private FriendzoneUser user;
     
     public void actionListener(ActionEvent e){
         eventPostManager.add(new EventPost(title, description, null, 
                 UserFactory.getInstance().getUniversalUser(), null, null, null));
     }
     
     public String action(){
         return "index";
     }
     
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 }
