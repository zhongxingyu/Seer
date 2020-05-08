 package models;
 
import javax.persistence.*;
 import java.io.Serializable;
 import java.sql.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-25
  * Time: 14:42
  */
 @Entity
 public class Users implements Serializable {
     private int userId;
 
     @Column(name = "userId")
     @Id
    @GeneratedValue
     public int getUserId() {
         return userId;
     }
 
     public void setUserId(int userId) {
         this.userId = userId;
     }
 
     //TODO Check if it is ok to use this constructor instead of the empty one that is meant to be used as a JPA constructor
     public Users(String username, int facebookId, String profileDescription, Date birthDate, int limitPerDay, boolean useDefaultColors) {
         this.username = username;
         this.facebookId = facebookId;
         this.profileDescription = profileDescription;
         this.birthDate = birthDate;
         this.limitPerDay = limitPerDay;
         this.useDefaultColors = useDefaultColors;
     }
 
     public Users() {
     }
 
 
     private String profileDescription;
 
     @Column(name = "profileDescription")
     @Basic
     public String getProfileDescription() {
         return profileDescription;
     }
 
     public void setProfileDescription(String profileDescription) {
         this.profileDescription = profileDescription;
     }
 
     private Date birthDate;
 
     @Column(name = "birthDate")
     @Basic
     public Date getBirthDate() {
         return birthDate;
     }
 
     public void setBirthDate(Date birthDate) {
         this.birthDate = birthDate;
     }
 
     private int limitPerDay;
 
     @Column(name = "limitPerDay")
     @Basic
     public int getLimitPerDay() {
         return limitPerDay;
     }
 
     public void setLimitPerDay(int limitPerDay) {
         this.limitPerDay = limitPerDay;
     }
 
     private boolean useDefaultColors;
 
     @Column(name = "useDefaultColors")
     @Basic
     public boolean isUseDefaultColors() {
         return useDefaultColors;
     }
 
     public void setUseDefaultColors(boolean useDefaultColors) {
         this.useDefaultColors = useDefaultColors;
     }
 
     private int facebookId;
 
     @Column(name = "facebookId")
     @Basic
     public int getFacebookId() {
         return facebookId;
     }
 
     public void setFacebookId(int facebookId) {
         this.facebookId = facebookId;
     }
 
     private String username;
 
     @Column(name = "username")
     @Basic
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Users users = (Users) o;
 
         if (facebookId != users.facebookId) return false;
         if (limitPerDay != users.limitPerDay) return false;
         if (useDefaultColors != users.useDefaultColors) return false;
         if (userId != users.userId) return false;
         if (birthDate != null ? !birthDate.equals(users.birthDate) : users.birthDate != null) return false;
         if (profileDescription != null ? !profileDescription.equals(users.profileDescription) : users.profileDescription != null)
             return false;
         if (username != null ? !username.equals(users.username) : users.username != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = userId;
         result = 31 * result + (profileDescription != null ? profileDescription.hashCode() : 0);
         result = 31 * result + (birthDate != null ? birthDate.hashCode() : 0);
         result = 31 * result + limitPerDay;
         result = 31 * result + (useDefaultColors ? 1 : 0);
         result = 31 * result + facebookId;
         result = 31 * result + (username != null ? username.hashCode() : 0);
         return result;
     }
 }
