 package com.group7.dragonwars.engine;
 
 public class GameField {
 
     private String fieldName;
     private Unit hostedUnit;
     private Building hostedBuilding;
     private Double movementModifier;
     private Double defenseModifier, attackModifier;
     private Boolean flightOnly, accessible;
     private String spriteLocation, spriteDir, spritePack;
 
     public  Boolean doesAcceptUnit(Unit unit) {
         Boolean canStep = true;
 
         if (this.flightOnly)
             canStep = unit.isFlying();
 
         return this.accessible && canStep;
     }
 
     public GameField(String fieldName, Double movementModifier, Double attackModifier,
                      Double defenseModifier, Boolean accessible, Boolean flightOnly,
                      String spriteLocation, String spriteDir, String spritePack) {
         this.fieldName = fieldName;
         this.movementModifier = movementModifier;
        this.attackModifier = attackModifier;
         this.defenseModifier = defenseModifier;
         this.accessible = accessible;
         this.flightOnly = flightOnly;
 
         this.spriteLocation = spriteLocation;
         this.spriteDir = spriteDir;
         this.spritePack = spritePack;
 
     }
 
     public Double getDefenseModifier() {
         if (this.hostsBuilding()) {
             Double mod = this.hostedBuilding.getDefenseBonus();
             mod += this.defenseModifier;
             return mod / 2;
         }
 
         return this.defenseModifier;
     }
 
     public Double getAttackModifier() {
         if (this.hostsBuilding()) {
             Double mod = this.hostedBuilding.getAttackBonus();
             mod += this.attackModifier;
             return mod / 2;
         }
 
         return this.attackModifier;
     }
 
     public Double getMovementModifier() {
         return this.movementModifier;
     }
 
 
     public Boolean hostsUnit() {
         return this.hostedUnit != null;
     }
 
     public Boolean hostsBuilding() {
         /* Could be used by the drawing routine. */
         return this.hostedBuilding != null;
     }
 
     public Unit getUnit() {
         return this.hostedUnit;
     }
 
     public Building getBuilding() {
         return this.hostedBuilding;
     }
 
     /* This will clobber old units/buildings as it is now. */
     public void setBuilding(Building building) {
         this.hostedBuilding = building;
     }
 
     public void setUnit(Unit unit) {
         this.hostedUnit = unit;
     }
 
     public String toString() {
         return this.getFieldName();
     }
 
     public String getFieldName() {
         return this.fieldName;
     }
 
     public String getSpriteLocation() {
         return this.spriteLocation;
     }
 
     public String getSpriteDir() {
         return this.spriteDir;
     }
 
     public String getSpritePack() {
         return this.spritePack;
     }
 }
