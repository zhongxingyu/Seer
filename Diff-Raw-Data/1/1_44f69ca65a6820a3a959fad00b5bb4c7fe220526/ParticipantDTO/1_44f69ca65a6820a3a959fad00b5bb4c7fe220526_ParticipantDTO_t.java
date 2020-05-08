 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package drinkcounter.web.controllers.api.v2;
 
 import com.timgroup.jgravatar.Gravatar;
 import com.timgroup.jgravatar.GravatarDefaultImage;
 import drinkcounter.model.User;
 import drinkcounter.model.User.Sex;
 
 /**
  *
  * @author Toni
  */
 public class ParticipantDTO {
     private Integer id;
     private String name;
     private String email;
     private Integer totalDrinks;
     private User.Sex sex;
     private Float promilles;
     private String profilePictureUrl;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
     
     public Float getPromilles() {
         return promilles;
     }
 
     public void setPromilles(Float promilles) {
         this.promilles = promilles;
     }
 
     public Sex getSex() {
         return sex;
     }
 
     public void setSex(Sex sex) {
         this.sex = sex;
     }
 
     public Integer getTotalDrinks() {
         return totalDrinks;
     }
 
     public void setTotalDrinks(Integer totalDrinks) {
         this.totalDrinks = totalDrinks;
     }
 
     public String getProfilePictureUrl() {
         return profilePictureUrl;
     }
 
     public void setProfilePictureUrl(String profilePictureUrl) {
         this.profilePictureUrl = profilePictureUrl;
     }
 
     public static ParticipantDTO fromUser(User user){
         ParticipantDTO participant = new ParticipantDTO();
         participant.setId(user.getId());
         participant.setName(user.getName());
         participant.setPromilles(user.getPromilles());
         participant.setSex(user.getSex());
         participant.setTotalDrinks(user.getTotalDrinks());
         if(user.getEmail() != null){
             participant.setProfilePictureUrl(new Gravatar().setDefaultImage(GravatarDefaultImage.WAVATAR).getUrl(user.getEmail()));
         }
         return participant;
     }
 }
