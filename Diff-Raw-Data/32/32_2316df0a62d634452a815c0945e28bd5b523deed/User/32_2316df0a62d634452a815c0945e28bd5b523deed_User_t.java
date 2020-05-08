 package models;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import models.enums.Gender;
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 import play.mvc.Http;
 import play.mvc.Scope;
 
 import play.db.jpa.Model;
 
 import javax.persistence.*;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 @Entity
 public class User extends Model {
 
     @Email
    @Required
     public String email;
 
     @Required
     public String firstName;
 
     @Required
     public String lastName;
 
     @Enumerated(EnumType.STRING)
     public Gender gender;
 
     @Temporal(TemporalType.DATE)
     public Date birthDate;
 
     @ManyToOne(cascade = CascadeType.PERSIST)
     public City city;
 
     public String interestedBy;
 
     @ManyToMany(cascade = CascadeType.PERSIST)
     public List<Artist> artists;
 
     public Long facebookId;
 
 
     public User() {
 
     }
 
     public User(String firstName, String lastName, String email, Gender gender, Date birthDate, City city, String interestedBy) {
         this.firstName = firstName;
         this.lastName = lastName;
         this.email = email;
         this.gender = gender;
         this.birthDate = birthDate;
         this.city = city;
         this.interestedBy = interestedBy;
     }
 
 }
