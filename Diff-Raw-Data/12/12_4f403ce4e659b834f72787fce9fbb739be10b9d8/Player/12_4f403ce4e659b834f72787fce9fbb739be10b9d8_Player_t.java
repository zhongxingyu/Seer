 package com.group7.dragonwars.engine;
 
 import java.util.*;
 
 public class Player {
     private String name;
     private Boolean lost;
     private Integer gold = 5;
     List<Unit> ownedUnits = new ArrayList<Unit>();
     List<Building> ownedBuildings = new ArrayList<Building>();
 
     public Player(String name) {
         this.name = name;
         this.lost = false;
     }
 
     public String getName() {
         return this.name;
     }
 
     public Boolean hasLost() {
         return this.lost || (ownedUnits.isEmpty() && ownedBuildings.isEmpty());
     }
 
     public Boolean hasMoveableUnits() {
         for (Unit u : ownedUnits)
             if (!u.hasFinishedTurn())
                 return true;
 
         return false;
     }
 
     public void removeUnit(Unit unit) {
         ownedUnits.remove(unit);
     }
 
     public List<Unit> getOwnedUnits() {
         return this.ownedUnits;
     }
 
     public void addUnit(Unit unit) {
         this.ownedUnits.add(unit);
     }
 
     public void addBuilding(Building building) {
         this.ownedBuildings.add(building);
     }
 
     public String toString() {
         return this.name;
     }
 
    public Integer getGoldAmount() {
         return this.gold;
     }
 
     public void setGoldAmount(Integer amount) {
         this.gold = amount;
     }
 }
