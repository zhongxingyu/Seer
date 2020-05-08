 package models;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToOne;
 import play.db.ebean.Model;
 import play.db.ebean.Model.Finder;
 
 @Entity
 public class Rating extends Model{
 
     private static final long serialVersionUID = 1L;
     
     @Id
     private long id;
     private int rating;
     private int ratingCount;
     
     @OneToOne
     private Surfer surfer;
     @ManyToMany(cascade=CascadeType.ALL)
     private List<UserInfo> userInfos = new ArrayList<>();
     
     /**
      * The constructor.
      */
     public Rating(Surfer surfer, UserInfo userInfo){
       this.surfer = surfer;
      //this.userInfos.add(userInfo);
       this.rating = 0;
       this.ratingCount = 0;
     }
     
     /**
      * 
      * @param surfer
      * @param userInfo
      * @return
      */
     public boolean hasRated(UserInfo userInfo) {
       return (userInfos.contains(userInfo));
     }
     
     /**
      * @param rating the rating to set
      */
     public void setRating(int rating, UserInfo userInfo) {
       ratingCount++;
       this.rating += rating;
       userInfos.add(userInfo);
     }
     
     /**
      * @return the rating
      */
     public int getRating() {
       if(ratingCount > 0){
         return Math.round((float)rating / ratingCount);
       }
       return rating;
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
