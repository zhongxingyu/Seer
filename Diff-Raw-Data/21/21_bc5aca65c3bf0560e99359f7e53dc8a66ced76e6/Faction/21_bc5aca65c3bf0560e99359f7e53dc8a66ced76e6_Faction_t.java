 package org.timadorus.webapp.client.character;
 
 import java.io.Serializable;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 
 //This class represents a Faction, which will related to the Character-Object
 @PersistenceCapable
 public class Faction implements Serializable {
 
   private static final long serialVersionUID = -5760923481329375866L;
 
 
   @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  Long factionID = new Long(-1);
 
   @Persistent
   String name = "--";
 
   @Persistent
   String description = "--";
   
   @Persistent
   Race race;
 
   Faction() {
     super();
   }
 
   public Long getFractionID() {
    return factionID;
   }
 
   public void setFractionID(Long fractionIDIn) {
    this.factionID = fractionIDIn;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String nameIn) {
     this.name = nameIn;
   }
 
   public String getDescription() {
     return description;
   }
 
   public void setDescription(String descriptionIn) {
     this.description = descriptionIn;
   }
 
   public Race getRace() {
     return race;
   }
 
   public void setRace(Race raceIn) {
     this.race = raceIn;
   }
   
   @Override
   public String toString() {
     return "Faction-Name: " + name;
   }
 }
