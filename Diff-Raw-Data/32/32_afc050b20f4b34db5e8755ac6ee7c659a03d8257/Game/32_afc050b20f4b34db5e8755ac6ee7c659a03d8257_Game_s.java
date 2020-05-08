 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 @Entity
 public class Game extends Model {
 
     @Id
     public Long id;
 
     public String playerName;
 
     public static Finder<Long, Game> find = new Finder<Long, Game>(Long.class, Game.class);
 
 }
