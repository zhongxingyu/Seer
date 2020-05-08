 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.bb;
 
 import it.chalmers.fannysangles.friendzone.model.EventPost;
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.model.Tag;
 import it.chalmers.fannysangles.friendzone.model.managers.EventPostManager;
 import it.chalmers.fannysangles.friendzone.model.managers.UserManager;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.annotation.PostConstruct;
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
     
     @EJB
     private UserManager userManager;
     
     @Inject
     private LoginBB login;
     
     private String tags;
     private String title;
     private String description;
     private String username;
     private FriendzoneUser user;
     private List<Tag> tagList;
     private FriendzoneUser loggedInUser;
     
     public void setLogin(LoginBB login){
         this.login = login;
     }
     
     public LoginBB getLogin(){
         return this.login;
     }
     
     @PostConstruct
     public void init(){
         loggedInUser = login.getLoggedInUser();
     }
     
     public void actionListener(ActionEvent e){
         createTagList(tags); 
         //TODO: Can't add old tags!
         if(login.isLoggedIn()){
             eventPostManager.add(new EventPost(title, description, tagList, 
                 loggedInUser, null, null, null));
         }
         
     }
     
     public String action(){
         return "index";
     }
     
     public void createTagList(String tags){
         if(tags != null){
            String[] s = tags.split(" ");
             tagList = new ArrayList<Tag>();
             for(int i=0; i<s.length; i++){ 
                 tagList.add(new Tag(s[i]));
             }
         }
     }
     
     public List<Tag> getTagList(){
         return tagList;
     }
     
     public String getTags(){
         return tags;
     }
     
     public String getTitle() {
         return title;
     }
 
     public String getDescription() {
         return description;
     }
     
     public void setTagList(List<Tag> tagList){
         this.tagList = tagList;
     }
     
     public void setTags(String s){
         this.tags = s;
     }
     
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
     
 
 }
