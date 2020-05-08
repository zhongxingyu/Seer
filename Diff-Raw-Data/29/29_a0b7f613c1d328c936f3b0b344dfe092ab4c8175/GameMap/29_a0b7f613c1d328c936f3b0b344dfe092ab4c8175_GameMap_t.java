 package com.group7.dragonwars.engine;
 
 import java.util.*;
 
 public class GameMap implements Iterable<GameField> {
 
     List<List<GameField>> fields;
     HashMap<Character, Unit> units;
     HashMap<Character, Building> buildings;
     HashMap<Character, GameField> gameFields;
 
 
     public GameMap(List<List<GameField>> fields, HashMap<Character, Unit> units,
                    HashMap<Character, Building> buildings, HashMap<Character, GameField> gameFields) {
         this.fields = fields;
         this.units = units;
         this.buildings = buildings;
         this.gameFields = gameFields;
     }
 
     public Iterator<GameField> iterator() {
         /* This really isn't ideal as we will iterate twice. */
         List<GameField> flat = new ArrayList<GameField>();
 
         for (List<GameField> row : this.fields)
             for (GameField gf : row)
                 flat.add(gf);
 
         return flat.iterator();
     }
 
     public Boolean isInstantiated() {
         return fields != null;
     }
 
     public Integer getWidth() {
        return fields.get(0).size();
     }
 
     public Integer getHeight() {
        return fields.size();
     }
 
     public GameField getField(Position position) {
         return getField(position.getX(), position.getY());
     }
 
     public GameField getField(Integer x, Integer y) {
        return fields.get(y).get(x);
     }
 
     public Boolean isValidField(Position position) {
         return isValidField(position.getX(), position.getY());
     }
 
     public Boolean isValidField(Integer x, Integer y) {
         if (this.fields == null || this.fields.get(0) == null) {
             return false;
         }
 
         if (x < 0 || y < 0) {
             return false;
         }
 
         if (x >= this.getWidth() || y >= this.getHeight()) {
             return false;
         }
 
         return true;
     }
 
     public String toString() {
         String m = "";
 
         for (List<GameField> agf : this.fields) {
             for (GameField gf : agf) {
                 m += gf.toString().charAt(0);
             }
 
             m += '\n';
         }
 
         return m;
     }
 
     public String dumpMobMap() {
         String m = "";
 
         for (List<GameField> agf : this.fields) {
             for (GameField gf : agf) {
                 if (gf.hostsUnit())
                     m += gf.getUnit().toString().charAt(0);
                 else
                     m += ' ';
             }
 
             m += '\n';
         }
 
         return m;
     }
 
     public HashMap<Character, Unit> getUnitMap() {
         return this.units;
     }
 
     public HashMap<Character, Building> getBuildingMap() {
         return this.buildings;
     }
 
     public HashMap<Character, GameField> getGameFieldMap() {
         return this.gameFields;
     }
 }
