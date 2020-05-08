 package edu.mines.alterego;
 
 /**
  * Description: This class was originally designed to be an abstraction layer for character stats
  * This class is currently unused but is retained for future use.
  * @author Matt Buland, Maria Deslis, Eric Young
  *
  */
 public class CharacterStat {
 
     private int characterId;
     private int statValue;
     private String statName;
     private int category;
 
     /**
      * 
      * @param charId The database primary key for character
      * @param statVal The numeric value for the current stat
      * @param statN The name for the current stat
      * @param cat The category for the current stat
      */
     CharacterStat(int charId, int statVal, String statN, int cat) {
         characterId = charId;
         statValue = statVal;
         statName = statN;
        category = cat;
     }
 
     @Override
     public String toString() {
         return statName + " " + Integer.toString(statValue);
     }
 }
