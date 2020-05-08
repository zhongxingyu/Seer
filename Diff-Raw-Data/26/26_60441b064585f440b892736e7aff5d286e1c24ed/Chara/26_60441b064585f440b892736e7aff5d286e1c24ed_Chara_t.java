 package models;
 
 import play.db.jpa.GenericModel;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 @Entity(name = "chara")
 public class Chara extends GenericModel {
 
     @Id
     @GeneratedValue
     public Long characterId;
 

     public String name;
 
     public static Chara makeNewCharacter(String name) {
         Chara chara = new Chara();
         chara.name = name;
         return chara.save();
     }
 
     public static Chara findById(Long characterId) {
         return find("characterId = ?1", characterId).first();
     }
 
     public static Chara dummy() {
        return all().first();
     }
 }
