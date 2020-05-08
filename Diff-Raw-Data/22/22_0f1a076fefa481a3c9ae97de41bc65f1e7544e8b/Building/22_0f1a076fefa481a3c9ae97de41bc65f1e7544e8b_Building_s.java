 package com.group7.dragonwars.engine;
 
 import java.util.*;
 
 public class Building extends DrawableMapObject {
 
     private Integer captureWorth;
     private Integer captureDifficulty, remainingCaptureTime;
     private Double attackBonus, defenseBonus;
     private Player owner;
     private Boolean goalBuilding;
     private Player lastCapturer;
    private List<Unit> producableUnits = new ArrayList<Unit>();
     private Position position;
 
 
     public Building(String name, Integer captureDifficulty, Double attackBonus,
                     Double defenseBonus, Boolean goalBuilding, Integer captureWorth,
                     String spriteLocation, String spriteDir, String spritePack) {
         super(name, spriteLocation, spriteDir, spritePack);
 
         this.captureDifficulty = captureDifficulty;
         this.remainingCaptureTime = this.captureDifficulty;
 
         this.attackBonus = attackBonus;
         this.defenseBonus = defenseBonus;
 
         this.goalBuilding = goalBuilding;
         this.captureWorth = captureWorth;
 
         generateInfo();
 
     }
 
     public Building(Building building) {
         super(building.getName(), building.getSpriteLocation(),
               building.getSpriteDir(), building.getSpritePack());
 
         this.captureDifficulty = building.getCaptureDifficulty();
         this.remainingCaptureTime = this.captureDifficulty;
 
         this.attackBonus = building.getAttackBonus();
         this.defenseBonus = building.getDefenseBonus();
 
         this.goalBuilding = building.isGoalBuilding();
         this.captureWorth = building.getCaptureWorth();
 
         this.info = building.info;
 
     }
 
     public Boolean canProduceUnits() {
        return !this.producableUnits.isEmpty();
     }
 
     public void addProducibleUnit(Unit unit) {
        this.producableUnits.add(unit);
     }
 
     public List<Unit> getProducibleUnits() {
        return this.producableUnits;
     }
 
     public String toString() {
         return getName();
     }
 
     public Player getLastCapturer() {
         return this.lastCapturer;
     }
 
     public void setLastCapturer(Player player) {
         this.lastCapturer = player;
     }
 
     public Integer getCaptureDifficulty() {
         return this.captureDifficulty;
     }
 
     public Integer getRemainingCaptureTime() {
         return this.remainingCaptureTime;
     }
 
     public Double getAttackBonus() {
         return this.attackBonus;
     }
 
     public Double getDefenseBonus() {
         return this.defenseBonus;
     }
 
     public Boolean hasOwner() {
         return this.owner != null;
     }
 
     public Player getOwner() {
         return this.owner;
     }
 
     public void setOwner(Player player) {
         this.owner = player;
     }
 
     public Boolean isGoalBuilding() {
         return this.goalBuilding;
     }
 
     public void reduceCaptureTime(Integer captureAmount) {
         this.remainingCaptureTime -= captureAmount;
 
         if (this.remainingCaptureTime <= 0) {
             this.remainingCaptureTime = 0;
             this.owner = this.lastCapturer;
             this.lastCapturer.addBuilding(this);
         }
     }
 
     public void resetCaptureTime() {
         this.remainingCaptureTime = this.captureDifficulty;
     }
 
     public Position getPosition() {
         return this.position;
     }
 
     public void setPosition(Position position) {
         this.position = position;
     }
 
     public Integer getCaptureWorth() {
         return this.captureWorth;
     }
 
     public String getInfo() {
         String r = getName();
         if (hasOwner()) {
             r += " ~ " + getOwner().getName();
         }
         r += "\n";
 
         return r + this.info;
     }
 
     public void generateInfo() {
         String r = "";
         if (canProduceUnits()) {
             for (Unit u : getProducibleUnits()) {
                r += "I can produce " + u + " - "
                    + u.getProductionCost() + "g\n";
             }
        } else {
            r += "I can't produce anything.\n";
         }
 
         this.info = r;
 
     }
 }
