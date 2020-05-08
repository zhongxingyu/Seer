 package com.group7.dragonwars.engine;
 
 import java.util.LinkedList;
 
 public class Unit extends DrawableMapObject {
 
     private Integer maxMovement, movement;
     private Double maxHealth, health;
     private Double attack, meleeDefense, rangeDefense;
     private Position position;
     private Boolean hasFinishedTurn = false;
     private Player owner;
     private Boolean isFlying;
     private Integer productionCost;
 
     public Unit(String name, Double maxHealth, Integer maxMovement,
                 Double attack, Double meleeDefense, Double rangeDefense,
                 Boolean isFlying, Integer productionCost, String spriteLocation,
                 String spriteDir, String spritePack) {
         super(name, spriteLocation, spriteDir, spritePack);
 
         this.maxHealth = maxHealth;
         this.health = this.maxHealth;
 
         this.maxMovement = maxMovement;
         this.movement = this.maxMovement;
 
         this.attack = attack;
         this.meleeDefense = meleeDefense;
         this.rangeDefense = rangeDefense;
 
         this.isFlying = isFlying;
         this.productionCost = productionCost;
 
         generateInfo();
     }
 
     /* Used for copying the unit template */
     public Unit(Unit unit) {
         super(unit.getName(), unit.getSpriteLocation(),
               unit.getSpriteDir(), unit.getSpritePack());
 
         this.maxHealth = unit.getMaxHealth();
         this.health = this.maxHealth;
 
         this.maxMovement = unit.getMaxMovement();
         this.movement = this.maxMovement;
 
         this.attack = unit.getAttack();
         this.meleeDefense = unit.getMeleeDefense();
         this.rangeDefense = unit.getRangeDefense();
 
        this.owner = unit.getOwner();
         this.isFlying = unit.isFlying();
         this.productionCost = unit.getProductionCost();
         this.info = unit.info;
     }
 
     public Boolean isDead() {
         return health <= 0;
     }
 
     public Double getHealth() {
         return this.health;
     }
 
     public Double getMaxHealth() {
         return this.maxHealth;
     }
 
     public Double getAttack() {
         return this.attack;
     }
 
     public Double getMeleeDefense() {
         return this.meleeDefense;
     }
 
     public Double getRangeDefense() {
         return this.rangeDefense;
     }
 
     public Position getPosition() {
         return this.position;
     }
 
     public Integer getRemainingMovement() {
         return this.movement;
     }
 
     public Integer getMaxMovement() {
         return this.maxMovement;
     }
 
     public Player getOwner() {
         return this.owner;
     }
 
     public void setOwner(Player player) {
         this.owner = player;
     }
 
     public void reduceHealth(Double damage) {
         this.health -= damage;
 
         if (this.health < 0) {
             this.health = 0.0;
         }
     }
 
     public void setPosition(Position position) {
         this.position = position;
     }
 
     public void restoreHealth(Double heal) {
         Double newHealth = this.health + heal;
         this.health = (newHealth <= maxHealth) ? newHealth : maxHealth;
     }
 
     public Boolean hasFinishedTurn() {
         return this.hasFinishedTurn;
     }
 
     public void setFinishedTurn(Boolean b) {
         this.hasFinishedTurn = b;
     }
 
     public String toString() {
         return getName();
     }
 
     public Boolean isRanged() {
         return false;
     }
 
     public Boolean isFlying() {
         return this.isFlying;
     }
 
     public Boolean reduceMovement(Integer amount) {
         if (this.movement - amount < 0)
             return false;
 
         this.movement -= amount;
         return true;
     }
 
     public Integer getProductionCost() {
         return this.productionCost;
     }
 
     public String getInfo() {
         String r = getName() + "\n";
         r += "Health: " + getHealth();
         return r + this.info;
     }
 
     public void generateInfo() {
         String  r =  "/"  + getMaxHealth() + "\n";
         r += "Attack: " + getAttack() + "\n";
         r += "Defense: " + getMeleeDefense() + " (Melee) "
             + getRangeDefense() + " (Ranged)\n";
 
         this.info = r;
     }
 }
