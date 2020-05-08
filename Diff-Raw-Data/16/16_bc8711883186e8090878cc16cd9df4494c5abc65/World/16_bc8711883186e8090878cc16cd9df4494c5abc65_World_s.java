 package models;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.db.jpa.Model;
 
 import com.google.gson.annotations.SerializedName;
 
 @Entity
 public class World extends Model {
 
	@SerializedName("_id")
	@Id
    public Long id;

     @Column(name = "randomNumber")
     public Long randomNumber;
 
 }
