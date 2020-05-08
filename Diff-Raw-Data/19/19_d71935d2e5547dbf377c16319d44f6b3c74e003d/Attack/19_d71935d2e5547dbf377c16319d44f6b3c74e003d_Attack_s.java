 package risk.game;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class Attack {
     private CountryPair countryPair;
     private int attackerDice;
     private int defenderDice;
     private Collection<Integer> aDiceResults;
     private Collection<Integer> dDiceResults;
     
     public Attack(CountryPair countryPair) {
         super();
         this.countryPair = countryPair;
     }
 
     public int getAttackerDice() {
         return attackerDice;
     }
 
     public void setAttackerDice(int attackerDice) {
         this.attackerDice = attackerDice;
     }
 
     public int getDefenderDice() {
         return defenderDice;
     }
 
     public void setDefenderDice(int defenderDice) {
         this.defenderDice = defenderDice;
     }
 
     public CountryPair getCountryPair() {
         return countryPair;
     }
     
     public void calcLosses(Integer aLosses, Integer dLosses) {
         aLosses = 0;
         dLosses = 0;
         if(aDiceResults==null || dDiceResults==null) return;
         Iterator<Integer> aIterator = aDiceResults.iterator();
         Iterator<Integer> dIterator = dDiceResults.iterator();
         
         while (aIterator.hasNext() && dIterator.hasNext()) {
             if (aIterator.next() > dIterator.next()) {
                 dLosses++;
             } else {
                 aLosses++;
             }
         }
     }
 
     public Collection<Integer> getaDiceResults() {
         return aDiceResults;
     }
 
     public void setaDiceResults(Collection<Integer> aDiceResults) {
         this.aDiceResults = aDiceResults;
     }
 
     public Collection<Integer> getdDiceResults() {
         return dDiceResults;
     }
 
     public void setdDiceResults(Collection<Integer> dDiceResults) {
         this.dDiceResults = dDiceResults;
     }
 }
