 package models;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 @Entity
 public class Game extends Model {
 
     @Id
     public Long id;
 
     @Constraints.Required
     public String playerName;
 
     public static Finder<Long, Game> find = new Finder<Long, Game>(Long.class, Game.class);
 
 }
