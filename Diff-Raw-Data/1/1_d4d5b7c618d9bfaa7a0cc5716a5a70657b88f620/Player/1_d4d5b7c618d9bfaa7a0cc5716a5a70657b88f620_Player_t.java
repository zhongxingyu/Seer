 package fruit.sim;
 
 public abstract class Player
 {
     // group id, e.g. 1, 2, 3...
     int id;
    String name;
 
     // your position in the game
     // the simulator will set your position before
     // the game starts
     // you can access your position with getIndex()
     int index;
     
     public int getIndex() {
         return index;
     }
 
     // Override this method
     public abstract void init(int nplayers, int[] pref);
 
     // Override this method
     // pass the bowl to the player
     // if the player does not have a bowl yet (canPick = true), he/she can choose to 
     // take it or not
     // if the player took a bowl before (canPick = false), the bowl is shown to the player
     // for bookkeeping
 
     // Return - true if keeps the bowl
     //          false don't keep the bowl
     // Note: the return value is ignored if canPick = false
     // Note 2: if mustTake = true, the return value is set to true no matter
     //       what value the player returns
     public abstract boolean pass(int[] bowl, 
                                  int bowlId,
                                  int round, // 0 or 1
                                  boolean canPick,   // can I choose the bowl?
                                  boolean mustTake); // I must take the bowl?
 }
