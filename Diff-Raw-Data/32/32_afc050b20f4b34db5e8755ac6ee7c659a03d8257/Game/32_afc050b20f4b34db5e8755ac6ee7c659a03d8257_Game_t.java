 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
import java.util.Random;
 
 @Entity
 public class Game extends Model {
 
     @Id
     public Long id;
 
     public String playerName;
    public Integer selectedDoor;

    public Integer goatDoor() {
        return 3;
    }
 
     public static Finder<Long, Game> find = new Finder<Long, Game>(Long.class, Game.class);
 
 }
