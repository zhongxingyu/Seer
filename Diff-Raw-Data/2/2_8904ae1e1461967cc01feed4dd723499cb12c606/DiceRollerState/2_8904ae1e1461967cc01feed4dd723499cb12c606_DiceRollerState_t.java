 package com.group5.diceroller;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.TreeMap;
 import java.util.Date;
 
 /**
  * Singleton class representing the state of the dice in the dice roller. It
  * stores the active set, the selectable dice sets, and the roll history.
  *
  * @Author Carlos Valera
  */
 public class DiceRollerState {
     List<DiceSet> dice_sets;
     SetSelection active_selection;
     List<SetSelection> roll_history;
     List<Date> roll_dates;
     List<SumCountPair> per_dice_stats;
 
     SumCountPair dice_rolled;
     int num_rolls;
 
     public static final int kHistorySize = 20;
 
     static DiceRollerState state = null;
 
     private DiceRollerState() {
         // have to add/remove from 2 ends, so a linked list is better here.
         // (TODO fixed size history would suggest ring buffered array for
         // speed, not critical for now)
         roll_history = new LinkedList<SetSelection>();
         roll_dates = new LinkedList<Date>();
         dice_sets = DiceSet.LoadAllFromDB();
         active_selection = new SetSelection();
         num_rolls = 0;
         dice_rolled = new SumCountPair();
         per_dice_stats = new ArrayList<SumCountPair>();
     }
 
     public static void initialize() {
         state = new DiceRollerState();
     }
 
     /**
      * Used to access the singleton instance of the dice roller state. The
      * application should call initialize() first to populate the list of dice
      * sets from the database.
      */
     public static DiceRollerState getState() {
         return state;
     }
 
     public List<DiceSet> diceSets() {
         return dice_sets;
     }
 
     public SetSelection activeSelection() {
         return active_selection;
     }
 
     public void updateRollHistory() {
         // update statistics
         num_rolls += 1;
         for (DiceSet s : activeSelection())
         {
             for (Dice d : s)
             {
                 int dsum = d.sum();
 
                 dice_rolled.sum += dsum;
                 dice_rolled.count += d.count;
 
                 addToPerDiceStat(d.faces, dsum, d.count);
             }
         }
 
         // update history
         rollHistory().add(0, new SetSelection(activeSelection()));
         rollDates().add(0, new Date());
         if (rollHistory().size() > kHistorySize) {
             rollHistory().remove(kHistorySize);
             rollDates().remove(kHistorySize);
         }
     }
 
     public List<SetSelection> rollHistory() {
         return roll_history;
     }
 
     public List<Date> rollDates() {
         return roll_dates;
     }
 
     public double getAvgRolls() {
         return dice_rolled.avg();
     }
 
     public int getNumRolls() {
         return num_rolls;
     }
 
     public List<SumCountPair> getPerDiceStats() {
         return per_dice_stats;
     }
 
     public void addToPerDiceStat(int faces, int sum, int count) {
         int i;
         SumCountPair pair = null;
 
         for (i=0; i<per_dice_stats.size(); i++) {
             pair = per_dice_stats.get(i);
             if (pair.faces == faces)
                 break;
 
             if (pair.faces > faces)
             {
                 pair = null;
                 break;
             }
         }
 
         if (i == per_dice_stats.size()) {
             pair = new SumCountPair();
             pair.faces = faces;
             per_dice_stats.add(pair);
         } else if (pair == null) {
             pair = new SumCountPair();
             pair.faces = faces;
            per_dice_stats.add(i, pair);
         }
 
         pair.sum += sum;
         pair.count += count;
     }
 
     public static class SumCountPair {
         int faces;
         int sum;
         int count;
 
         SumCountPair() {
             sum = 0;
             count = 0;
             faces = 0;
         }
 
         double avg() {
             if (count == 0)
                 return 0;
             return ((double) sum) / ((double) count);
         }
     }
 }
 
