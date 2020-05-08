 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.game;
 
 import brutes.db.Identifiable;
 
 /**
  *
  * @author Karl
  */
 public class Character implements Identifiable {
 
     public static final int MAX_BONUSES = 3;
     
     private int id;
     private String name;
     private short level;
     private short life;
     private short strength;
     private short speed;
     private int imageID;
     
     private Bonus[] bonuses;
 
     public Character(int id, String name, short level, short life, short strength, short speed, int imageID) {
         this.id = id;
         this.name = name;
         this.level = level;
         this.life = life;
         this.strength = strength;
         this.speed = speed;
         this.imageID = imageID;
         this.bonuses = new Bonus[Character.MAX_BONUSES];
         for (int i = 0; i < this.bonuses.length; i++) {
             this.bonuses[i] = Bonus.EMPTY_BONUS;
         }
     }
 
     public Character(int id, String name, short level, short life, short strength, short speed, int imageID, Bonus[] bonuses) {
         this(id, name, level, life, strength, speed, imageID);
         System.arraycopy(bonuses, 0, this.bonuses, 0, (bonuses.length < Character.MAX_BONUSES) ? bonuses.length : Character.MAX_BONUSES);
     }
 
     @Override
     public int getId() {
         return this.id;
     }
 
     public String getName() {
         return this.name;
     }
 
     public short getLevel() {
         return this.level;
     }
 
     public short getLife() {
         return this.life;
     }
 
     public short getStrength() {
         short sum = this.strength;
         for (int i = 0; i < this.bonuses.length; i++) {
             if (!this.bonuses[i].equals(Bonus.EMPTY_BONUS)) {
                 sum += this.bonuses[i].getStrength();
             }
         }
         return sum;
     }
 
     public short getSpeed() {
         short sum = this.speed;
         for (int i = 0; i < bonuses.length; i++) {
             if (!this.bonuses[i].equals(Bonus.EMPTY_BONUS)) {
                 sum += bonuses[i].getSpeed();
             }
         }
         return sum;
     }
 
     public int getImageID() {
         return this.imageID;
     }
 
     public Bonus[] getBonuses() {
         return this.bonuses;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setLevel(short level) {
         this.level = level;
     }
 
     public void setLife(short life) {
         this.life = life;
     }
 
     public void setStrength(short strength) {
         this.strength = strength;
     }
 
     public void setSpeed(short speed) {
         this.speed = speed;
     }
 
     public void setBonuses(Bonus[] bonuses) {
         this.bonuses = bonuses;
     }
 }
