 package org.timadorus.webapp.client.character;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
 
 //This Class represents a Character-Class-Object, which is related to a Character-Object 
 @PersistenceCapable
 public class CClass implements Serializable {
 
   /**
    * 
    */
   private static final long serialVersionUID = -9162491221927969566L;
   
   @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
   Long classID=new Long(-1);
 
   @Persistent
   String name="--";
 
   @Persistent
   String description="--";
 
   @NotPersistent
   List<Faction> availableFactions1 = new ArrayList<Faction>();
   
   @Persistent //braucht JDO für 1-to-n Relationship, beginnend von Race-Klasse
   Race race;
 
 
 
   public CClass() {
 
   }
 
   public CClass(String name, String description) {
 
     this.name = name;
     this.description = description;
 
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public String getDescription() {
     return description;
   }
 
   public void setDescription(String description) {
     this.description = description;
   }
 
   public List<Faction> getAvailableFactions() {
     return availableFactions1;
   }
 
   public void addFaction(Faction faction) {
     availableFactions1.add(faction);
   }
 
   public Race getRace() {
     return race;
   }
 
   public void setRace(Race race) {
     this.race = race;
   }
 
   public Long getClassID() {
     return classID;
   }
 
   public void setClassID(Long classID) {
     this.classID = classID;
   }
 
   public void setAvailableFactions(List<Faction> availableFactions) {
     this.availableFactions1 = availableFactions;
   }
   
   public List<Faction> getAvailableFactions1() {
     return availableFactions1;
   }
 
   public void setAvailableFactions1(List<Faction> availableFactions1) {
     this.availableFactions1 = availableFactions1;
   }
 
   @Override
   public String toString() {
   
     return "Character-Class-Name: "+name;
   }
   
 
 }
