 package bowling.data.domain;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 /**
  * Created by IntelliJ IDEA.
  * User: pnamatya
  * Date: 4/1/12        this is test
  * Time: 7:42 PM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
@Table(name = "frame")
 public class Frame implements Serializable {
     private Long id;
     private int nbr; // Frame Number
 
     private int laneNbr;
     private String notes;
     private Game game;
 
     @Id
     @Column(name = "id")
     @GeneratedValue
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name = "laneNbr")
     public int getLaneNbr() {
         return laneNbr;
     }
 
     public void setLaneNbr(int laneNbr) {
         this.laneNbr = laneNbr;
     }
 
     @Column(name = "notes")
     public String getNotes() {
         return notes;
     }
 
     public void setNotes(String notes) {
         this.notes = notes;
     }
 
     @Column(name ="game")
     public Game getGame() {
         return game;
     }
 
     public void setGame(Game game) {
         this.game = game;
     }
 }
