 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.game;
 
 import brutes.game.media.CharacterImage;
 import java.util.ArrayList;
 
 /**
  *
  * @author Karl
  */
 public class Character {
     private int id;
     private String name;
     private short level;
     private short life;
     private short strength;
     private short speed;
     private int imageID;
     private ArrayList<Integer> bonusesID;
 
    public Character(int id, String name, int level, int life, int strength, int speed, int imageID) {
         this.id = id;
         this.name = name;
         this.level = level;
         this.life = life;
         this.strength = strength;
         this.speed = speed;
         this.imageID = imageID;
     }
    public Character(int id, String name, int level, int life, int strength, int speed, int imageID, ArrayList<Integer> bonusesID) {
         this(id, name, level, life, strength, speed, imageID);
         this.bonusesID = bonusesID;
     }
     
     public void addBonus(int bonus){
         this.bonusesID.add(new Integer(bonus));
     }
 
     public int getId() {
         return id;
     }
     public String getName() {
         return name;
     }
    public int getLevel() {
         return level;
     }
    public int getLife() {
         return life;
     }
    public int getStrength() {
         return strength;
     }
    public int getSpeed() {
         return speed;
     }
     public int getImageID() {
         return imageID;
     }
     public ArrayList<Integer> getBonuseIDs() {
         return bonusesID;
     }
 }
