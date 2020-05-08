 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToOne;
 import play.db.ebean.Model;
 
 /**
  * A rating object so that a user can rate a surfer.
  * @author Andrew
  *
  */
 @Entity
 public class Rating extends Model {
 
     private static final long serialVersionUID = 1L;
     
     @Id
     private long id;
     private int rating;
     private int ratingCount;
     private String userPlusRating = "";
     
     @OneToOne
     private Surfer surfer;
     @ManyToMany(cascade=CascadeType.ALL)
     private List<UserInfo> userInfos = new ArrayList<>();
     
     /**
      * constructor.
      * @param surfer the surfer to add a rating to
      * @param userInfo the user rating the surfer
      */
     public Rating(Surfer surfer, UserInfo userInfo) {
       this.surfer = surfer;
       //this.userInfos.add(userInfo);
       this.rating = 0;
       this.ratingCount = 0;
     }
     
     /**
      * checks if a user has rated this surfer.
      * @param userInfo the user to check
      * @return true if the user has rated this surfer, false otherwise
      */
     public boolean hasRated(UserInfo userInfo) {
       return (userInfos.contains(userInfo));
     }
     
     /**
      * sets the rating average.
      * @param rating the rating to set
      * @param userInfo the user rating this surfer
      */
     public void setRating(int rating, UserInfo userInfo) {
       ratingCount++;
       this.rating += rating;
       userInfos.add(userInfo);
       userPlusRating += "id" + String.valueOf(userInfo.getId()) + ";" + String.valueOf(rating) + ";";
     }
     
     /**
      * gets the surfers rating.
      * @return the rating
      */
     public int getRating() {
       if (ratingCount > 0) {
         return Math.round((float) rating / ratingCount);
       }
       return rating;
     }
     
     /**
      * Gets the rating the specified user gave.
      * @param userInfo the user who gave the rating
      * @return the rating given by the user
      */
     public int getUserRating(UserInfo userInfo) {
       int rating = 0;
       if (hasRated(userInfo)) {
         String[] ratingArray = userPlusRating.split(";");
         for (int i = 0; i < ratingArray.length; i++) {
           if (ratingArray[i].equals("id" + userInfo.getId())) {
             rating = Integer.parseInt(ratingArray[i + 1]);
           }
         }
       }
       return rating;
     }
     
     /**
      * Deletes the rating a specified user gave to a surfer.
      * @param userInfo the user who gave the rating
      */
     public void deleteUserRating(UserInfo userInfo) {
       int rating = 0;
       if (hasRated(userInfo)) {
         String[] ratingArray = userPlusRating.split(";");
         userPlusRating = "";
         for (int i = 0; i < ratingArray.length; i++) {
           
           if (ratingArray[i].equals("id" + userInfo.getId())) {
             rating = Integer.parseInt(ratingArray[++i]);
           }
           else {
            userPlusRating += ratingArray[i];
           }
         }
         userInfos.remove(userInfo);
         this.rating -= rating;
         this.ratingCount -= 1;
       }
     }
     
     /**
      * @return the id
      */
     public long getId() {
       return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(long id) {
       this.id = id;
     }
 
     /**
      * @return the surfer
      */
     public Surfer getSurfer() {
       return surfer;
     }
 
     /**
      * @param surfer the surfer to set
      */
     public void setSurfer(Surfer surfer) {
       this.surfer = surfer;
     }
 
     /**
      * @return the userInfo
      */
     public List<UserInfo> getUserInfos() {
       return userInfos;
     }
 
     /**
      * @param userInfo the userInfo to set
      */
     public void addUserInfo(UserInfo userInfo) {
       userInfos.add(userInfo);
     }
     
     /**
      * The EBean ORM finder method for database queries on ID.
      * @return The finder method for Favorites.
      */
     public static Finder<Long, Rating> find() {
       return new Finder<Long, Rating>(Long.class, Rating.class);
     }
 }
