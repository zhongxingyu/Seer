 package de.flower.rmt.model.event;
 
 import de.flower.rmt.model.Jersey;
 import de.flower.rmt.model.Opponent;
 import de.flower.rmt.model.Team;
 import org.hibernate.annotations.Type;
 import org.joda.time.LocalTime;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 /**
  * @author flowerrrr
  */
 @Entity
 @DiscriminatorValue("Match")
 public class Match extends Event {
 
     @Column
    @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalTimeAsTime")
     private LocalTime kickOff;
 
     @ManyToOne
     private Opponent opponent;
 
     @ManyToOne
     private Jersey jersey;
 
     public Match() {
     }
 
     public Match(Team team) {
         super(team);
     }
 }
