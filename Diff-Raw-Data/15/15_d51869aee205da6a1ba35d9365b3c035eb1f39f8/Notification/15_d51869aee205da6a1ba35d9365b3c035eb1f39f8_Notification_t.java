 package it.antreem.birretta.service.model;
 
 import java.util.Date;
 
 /**
  *
  * @author alessio
  */
 public class Notification extends MongoDBObject
 {
     private String idNotification;
     private String jumpTo;
     private String targetName;
     private String idBeer;
     private String beerName;
     private String idFriend;
     private String friendName;
     private String idPlace;
     private String placeName;
     private String image;
    private Integer type;
     private Boolean status;
     private Date insertedOn;
 
     public String getBeerName() {
         return beerName;
     }
 
     public void setBeerName(String beerName) {
         this.beerName = beerName;
     }
 
     public String getTargetName() {
         return targetName;
     }
 
     public void setTargetName(String targetName) {
         this.targetName = targetName;
     }
 
     public String getFriendName() {
         return friendName;
     }
 
     public void setFriendName(String friendName) {
         this.friendName = friendName;
     }
 
     public String getIdBeer() {
         return idBeer;
     }
 
     public void setIdBeer(String idBeer) {
         this.idBeer = idBeer;
     }
 
     public String getIdFriend() {
         return idFriend;
     }
 
     public void setIdFriend(String idFriend) {
         this.idFriend = idFriend;
     }
 
     public String getIdNotification() {
         return super.getId().toString();
     }
 /*
     public void setIdNotification(String idNotification) {
         this.idNotification = idNotification;
     }
 */
     public String getIdPlace() {
         return idPlace;
     }
 
     public void setIdPlace(String idPlace) {
         this.idPlace = idPlace;
     }
 
     public String getPlaceName() {
         return placeName;
     }
 
     public void setPlaceName(String placeName) {
         this.placeName = placeName;
     }
 
 
     public String getJumpTo() {
         return jumpTo;
     }
 
     public void setJumpTo(String jumpTo) {
         this.jumpTo = jumpTo;
     }
 
     public String getImage() {
         return image;
     }
 
     public void setImage(String image) {
         this.image = image;
     }
 
     public Boolean getStatus() {
         return status;
     }
 
     public void setStatus(Boolean status) {
         this.status = status;
     }
 
     public Date getInsertedOn() {
         return insertedOn;
     }
 
     public void setInsertedOn(Date insertedOn) {
         this.insertedOn = insertedOn;
     }
 
    public Integer getType() {
         return type;
     }
 
    public void setType(Integer type) {
         this.type = type;
     }
 }
