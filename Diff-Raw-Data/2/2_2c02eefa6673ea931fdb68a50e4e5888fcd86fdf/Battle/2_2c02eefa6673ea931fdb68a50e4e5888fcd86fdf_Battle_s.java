 package edu.gatech.cs2340.risky.models;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import edu.gatech.cs2340.risky.Model;
 
 public class Battle extends Model {
     
     private String attackingTerritory;
     private int attackingDie;
     private String defendingTerritory;
     private int defendingDie;
     
     public Battle() {
         
     }
     
     public Battle(String fromTerritory, String toTerritory, int attackingDie, int defendingDie) {
         this.attackingTerritory = fromTerritory;
         this.defendingTerritory = toTerritory;
         this.attackingDie = attackingDie;
         this.defendingDie = defendingDie;
     }
     
     public boolean isReadyToWage() {
         return this.attackingTerritory != null && this.attackingDie > 0 && this.defendingTerritory != null && this.defendingDie > 0;
     }
     
     public BattleRecord wage() throws Exception {
         if (!this.isReadyToWage()) {
             throw new Exception("Yowzers, battle not ready to wage");
         }
         
         // TODO: implement logic of an attack as defined in R15
         // more specifically, under Rules of Risk -> Gameplay -> Attacking in 
         // http://www.cc.gatech.edu/~simpkins/teaching/gatech/cs2340/projects/cs2340-summer2013-project.html
         
 
         Integer[] attackingDiceValues = roll(attackingDie);
         Integer[] defendingDiceValues = roll(defendingDie);
 
         Arrays.sort(attackingDiceValues, Collections.reverseOrder());
         Arrays.sort(defendingDiceValues, Collections.reverseOrder());
         
         BattleRecord record = new BattleRecord();
         
         record.attackingTerritory = this.attackingTerritory;
         record.defendingTerritory = this.defendingTerritory;
         record.attackingCasualties = 0;
         record.defendingCasualties = 0;
         for (int i=0 ; i < attackingDiceValues.length && i < defendingDiceValues.length ; i++) {
             System.out.println(attackingDiceValues[i] + " vs " + defendingDiceValues[i]);
             if (attackingDiceValues[i] > defendingDiceValues[i]) {
                 record.defendingCasualties++;
             } else {
                 record.attackingCasualties++;
             }
         }
         
        record.defendingCasualties = 10;
         
         return record;
     }
 
     private Integer[] roll(int count) {
         Integer[] results = new Integer[count];
         for (int i=0 ; i < count ; i++) {
             results[i] = (int) (Math.random() * 6) + 1;           
         }
         return results;
     }
     
 }
