 package thompson.core;
 
 import java.util.*;
 import java.math.*;
 
 public class Sample {
   enum ForestLabel {
     I, N, L, R, X
   }
 
   enum OfPointer {
     LEFT, RIGHT
   }
 
   static class ForestState {
     private ForestLabel forestLabel;
     private OfPointer ofPointer;
     private int excess;
     
     ForestState(ForestLabel forestLabel, OfPointer ofPointer, int excess) {
       this.forestLabel = forestLabel;
       this.ofPointer = ofPointer;
       this.excess = excess;
     }
     
     public int hashCode() {
       return Util.hashCombine(this.forestLabel.hashCode(),
                Util.hashCombine(this.ofPointer.hashCode(),
                                 this.excess));
     }
     
     public boolean equals(Object obj) {
       if (!(obj instanceof ForestState)) {
         return false;
       } else {
         ForestState state = (ForestState) obj;
         return ((this.forestLabel == state.forestLabel) &&
                 (this.ofPointer == state.ofPointer) &&
                 (this.excess == state.excess));
       }
     }
     
     public String toString() {
       return "{" + forestLabel + ", " + ofPointer + ", " + excess + "}";
     }
   }
 
   static class ForestKey {
     int weight;
     ForestState upperState, lowerState;
     
     ForestKey(int weight, ForestState upperState, ForestState lowerState) {
       this.weight = weight;
       this.upperState = upperState;
       this.lowerState = lowerState;
     }
     
     public int hashCode() {
       return Util.hashCombine(upperState.hashCode(),
                Util.hashCombine(lowerState.hashCode(), this.weight));
     }
     
     public boolean equals(Object obj) {
       if (!(obj instanceof ForestKey)) {
         return false;
       } else {
         ForestKey elem = (ForestKey) obj;
         return ((this.weight == elem.weight) &&
                 this.upperState.equals(elem.upperState) &&
                 this.lowerState.equals(elem.lowerState));
       }
     }
     
     public String toString() {
       return "<" + weight + "," +
                    upperState.toString() + "," +
                    lowerState.toString() + ">";
     }
   }
   
   private static ForestState[] updateLeft(ForestState state) {    
     ForestState[] nextStates;
     if (state.forestLabel == ForestLabel.L) {
       nextStates = new ForestState[7];
       nextStates[0] = new ForestState(ForestLabel.L, OfPointer.LEFT,  0);
       nextStates[1] = new ForestState(ForestLabel.N, OfPointer.LEFT,  1);
       nextStates[2] = new ForestState(ForestLabel.I, OfPointer.LEFT,  0);
       nextStates[3] = new ForestState(ForestLabel.N, OfPointer.RIGHT, 1);
       nextStates[4] = new ForestState(ForestLabel.I, OfPointer.RIGHT, 0);
       nextStates[5] = new ForestState(ForestLabel.R, OfPointer.RIGHT, 0);
       nextStates[6] = new ForestState(ForestLabel.X, OfPointer.RIGHT, 0);
     } else if ((state.forestLabel == ForestLabel.N) ||
                ((state.forestLabel == ForestLabel.I) && (state.excess > 0))) {
       nextStates = new ForestState[4];
       nextStates[0] = new ForestState(ForestLabel.N, OfPointer.LEFT, state.excess + 1);
       nextStates[1] = new ForestState(ForestLabel.N, OfPointer.LEFT, state.excess);
       nextStates[2] = new ForestState(ForestLabel.I, OfPointer.LEFT, state.excess);
       nextStates[3] = new ForestState(ForestLabel.I, OfPointer.LEFT, state.excess - 1);
     } else if ((state.forestLabel == ForestLabel.I) && (state.excess == 0)) {
       nextStates = new ForestState[3];
       nextStates[0] = new ForestState(ForestLabel.N, OfPointer.LEFT, 1);
       nextStates[1] = new ForestState(ForestLabel.I, OfPointer.LEFT, 0);
       nextStates[2] = new ForestState(ForestLabel.L, OfPointer.LEFT, 0);
     } else {
       nextStates = new ForestState[0];
     }
     return nextStates;
   }
   
   private static ForestState[] updateRight(ForestState state) {    
     ForestState[] nextStates;
     if (state.forestLabel == ForestLabel.R) {
       nextStates = new ForestState[2];
       nextStates[0] = new ForestState(ForestLabel.R, OfPointer.RIGHT, 0);
       nextStates[1] = new ForestState(ForestLabel.X, OfPointer.RIGHT, 0);
     } else if (state.forestLabel == ForestLabel.X) {
       nextStates = new ForestState[2];
       nextStates[0] = new ForestState(ForestLabel.N, OfPointer.RIGHT, 1);
       nextStates[1] = new ForestState(ForestLabel.I, OfPointer.RIGHT, 0);
     } else if ((state.forestLabel == ForestLabel.N) ||
               ((state.forestLabel == ForestLabel.I) && (state.excess > 0))) {
       nextStates = new ForestState[4];
       nextStates[0] = new ForestState(ForestLabel.N, OfPointer.RIGHT, state.excess + 1);
       nextStates[1] = new ForestState(ForestLabel.N, OfPointer.RIGHT, state.excess);
       nextStates[2] = new ForestState(ForestLabel.I, OfPointer.RIGHT, state.excess);
       nextStates[3] = new ForestState(ForestLabel.I, OfPointer.RIGHT, state.excess - 1);
     } else if ((state.forestLabel == ForestLabel.I) && (state.excess == 0)) {
       nextStates = new ForestState[4];
       nextStates[0] = new ForestState(ForestLabel.N, OfPointer.RIGHT, 1);
       nextStates[1] = new ForestState(ForestLabel.I, OfPointer.RIGHT, 0);
       nextStates[2] = new ForestState(ForestLabel.R, OfPointer.RIGHT, 0);
       nextStates[3] = new ForestState(ForestLabel.X, OfPointer.RIGHT, 0);
     } else {
       nextStates = new ForestState[0];
     }
     return nextStates;
   }
   
   public static int weight(ForestLabel labelA, ForestLabel labelB) {
     switch(labelA) {
       case I:
         switch(labelB) {
           case I: return 2;
           case N: return 4;
           case L: return 2;
           case R: return 1;
           case X: return 3;
         }
       case N:
         switch(labelB) {
           case I: return 4;
           case N: return 4;
           case L: return 2;
           case R: return 3;
           case X: return 3;
         }
       case L:
         switch(labelB) {
           case I: return 2;
           case N: return 2;
           case L: return 2;
           case R: return 1;
           case X: return 1;
         }
       case R:
         switch(labelB) {
           case I: return 1;
           case N: return 3;
           case L: return 1;
           case R: return 2;
           case X: return 2;
         }
       case X:
         switch(labelB) {
           case I: return 3;
           case N: return 3;
           case L: return 1;
           case R: return 2;
           case X: return 0;
         }
       default:
         throw new IllegalArgumentException();
     }
   }
     
   private static ArrayList<ForestKey> weightNKeys(HashMap<ForestKey,?> web, int n) {
     ArrayList<ForestKey> keys = new ArrayList<ForestKey>();
     for (ForestKey key : web.keySet()) {
       if (key.weight == n) {
         keys.add(key);
       }
     }
     return keys;
   }
   
   public static ArrayList<ForestKey> successorKeys(ForestKey fromKey) {
     ArrayList<ForestKey> toKeys = new ArrayList<ForestKey>();
     ForestState upperState = fromKey.upperState;
     ForestState lowerState = fromKey.lowerState;
     ForestState[] upperSet = (upperState.ofPointer == OfPointer.LEFT)  ? updateLeft(upperState) : updateRight(upperState);
     ForestState[] lowerSet = (lowerState.ofPointer == OfPointer.LEFT) ? updateLeft(lowerState) : updateRight(lowerState);
     for (int u = 0; u < upperSet.length; u++) {
       ForestState upperStateP = upperSet[u];
       for (int l = 0; l < lowerSet.length; l++) {
         ForestState lowerStateP = lowerSet[l];
         if (!((upperStateP.forestLabel == lowerStateP.forestLabel) &&
               (lowerStateP.forestLabel == ForestLabel.I) &&
               (upperState.forestLabel != ForestLabel.I) &&
               (lowerState.forestLabel != ForestLabel.I))) {
           int weightP = weight(upperStateP.forestLabel, lowerStateP.forestLabel);
           ForestKey toKey = new ForestKey(fromKey.weight + weightP, upperStateP, lowerStateP); 
           toKeys.add(toKey);
         }
       }
     }
     return toKeys;
   }
 
   public static BigInteger[] countForestDiagrams(int maxWeight) {
     BigInteger[] counts = new BigInteger[maxWeight-3];
     HashMap<ForestKey,BigInteger> countWeb = new HashMap<ForestKey,BigInteger>();
     countWeb.put(
       new ForestKey(2, new ForestState(ForestLabel.L, OfPointer.LEFT, 0),
                        new ForestState(ForestLabel.L, OfPointer.LEFT, 0)),
       BigInteger.ONE);
     for (int n = 2; n < maxWeight; n++) {
       for (ForestKey fromKey : weightNKeys(countWeb, n)) {
         BigInteger fromCount = countWeb.get(fromKey);
         for (ForestKey toKey : successorKeys(fromKey)) {
           BigInteger toCount = countWeb.get(toKey);
           if (toCount == null) { toCount = BigInteger.ZERO; }
           BigInteger newCount = toCount.add(fromCount);
           countWeb.put(toKey, newCount);
         }
         countWeb.remove(fromKey);
       }
       if (n >= 3)
       counts[n-3] = countWeb.get(
                       new ForestKey(n+1,
                         new ForestState(ForestLabel.R, OfPointer.RIGHT, 0),
                         new ForestState(ForestLabel.R, OfPointer.RIGHT, 0)));
     }
     return counts;
   }
   
   static class BackPointer {
     private ForestKey backKey;
     private BigInteger backCount;
     
     BackPointer(ForestKey backKey, BigInteger backCount) {
       this.backKey = backKey;
       this.backCount = backCount;
     }
   }
   
   static class BackPointers {
     private ArrayList<BackPointer> backPointers;
     private BigInteger totalBackCount;
     
     BackPointers(BigInteger totalBackCount) {
       this.backPointers = new ArrayList<BackPointer>();
       this.totalBackCount = totalBackCount;
     }
   }
   
   private static void addBackPointer(BackPointers backPointers, ForestKey backKey, BigInteger backCount) {
     backPointers.backPointers.add(new BackPointer(backKey, backCount));
     backPointers.totalBackCount = backPointers.totalBackCount.add(backCount);
   }
   
   public static HashMap<ForestKey,BackPointers> modelForestDiagrams(int maxWeight) {
     HashMap<ForestKey,BackPointers> modelWeb = new HashMap<ForestKey,BackPointers>();
     modelWeb.put(
       new ForestKey(2, new ForestState(ForestLabel.L, OfPointer.LEFT, 0),
                        new ForestState(ForestLabel.L, OfPointer.LEFT, 0)),
       new BackPointers(BigInteger.ONE));
     for (int n = 2; n < maxWeight; n++) {
       for (ForestKey fromKey : weightNKeys(modelWeb, n)) {
         BackPointers fromPointers = modelWeb.get(fromKey);
         BigInteger fromCount = fromPointers.totalBackCount;
         for (ForestKey toKey : successorKeys(fromKey)) {
           BackPointers toPointers = modelWeb.get(toKey);
           if (toPointers == null) { toPointers = new BackPointers(BigInteger.ZERO); }
           addBackPointer(toPointers, fromKey, fromCount);
           modelWeb.put(toKey, toPointers);
         }
       }
     }
     return modelWeb;
   }
   
   private static ForestKey chooseBackKey(BackPointers backPointers, Random rand) {
     BackPointer chosen = null;
     BigInteger finger = Util.nextRandomBigInteger(backPointers.totalBackCount, rand);
     BigInteger at = BigInteger.ZERO;
     for (BackPointer backPointer : backPointers.backPointers) {
       at = at.add(backPointer.backCount);
       if (at.compareTo(finger) > 0) {
         chosen = backPointer;
         break;
       }
     }
     if (chosen == null) { throw new RuntimeException("unreachable"); }
     return chosen.backKey;
   }
   
   public static LinkedList<ForestKey> chooseRandomPath(HashMap<ForestKey,BackPointers> modelWeb, int weight) {
     ForestKey atKey = new ForestKey(weight, new ForestState(ForestLabel.R, OfPointer.RIGHT, 0),
                                             new ForestState(ForestLabel.R, OfPointer.RIGHT, 0));
    if (!modelWeb.containsKey(atKey)) {
       throw new IllegalArgumentException("Insufficiently deep model");
     }
     ForestKey rootKey = new ForestKey(2, new ForestState(ForestLabel.L, OfPointer.LEFT, 0),
                                          new ForestState(ForestLabel.L, OfPointer.LEFT, 0));
     Random rand = new Random();
     LinkedList<ForestKey> wordKeys = new LinkedList<ForestKey>();
     while (!atKey.equals(rootKey)) {
       wordKeys.addFirst(atKey);
       atKey = chooseBackKey(modelWeb.get(atKey), rand);
     }
     wordKeys.addFirst(rootKey);
     return wordKeys;  
   }
   
   static class ForestPair {
     ForestLabel[] upperLabels, lowerLabels;
     int upperNumLeft, lowerNumLeft;
     
     ForestPair(ForestLabel[] upperLabels, ForestLabel[] lowerLabels, int upperNumLeft, int lowerNumLeft) {
       this.upperLabels = upperLabels;
       this.lowerLabels = lowerLabels;
       this.upperNumLeft = upperNumLeft;
       this.lowerNumLeft = lowerNumLeft;
     }
     
     public int numPairs() {
       return this.upperLabels.length;
     }
 
     public String toString() {
       StringBuffer topBuffer = new StringBuffer();
       StringBuffer upperBuffer = new StringBuffer();
       StringBuffer lowerBuffer = new StringBuffer();
       StringBuffer bottomBuffer = new StringBuffer();
       for (int i = 0; i < this.numPairs(); i++) {
         topBuffer.append((i == this.upperNumLeft) ? "v " : "  ");
         upperBuffer.append((i == 0) ? " " : ",");
         lowerBuffer.append((i == 0) ? " " : ",");
         upperBuffer.append(upperLabels[i]);
         lowerBuffer.append(lowerLabels[i]);
         bottomBuffer.append((i == this.lowerNumLeft) ? "^ " : "  ");
       }
       return topBuffer.toString() + "\n" + upperBuffer.toString() + "\n" +
              lowerBuffer.toString() + "\n" + bottomBuffer.toString() + "\n";
     }
   }
 
   public static ForestPair chooseRandomWord(HashMap<ForestKey,BackPointers> modelWeb, int weight) {
     int attempt = 0;
     while (true) {
       attempt++;
        LinkedList<ForestKey> path = chooseRandomPath(modelWeb, weight+4);
        ForestKey leftKey = path.get(1);
        ForestKey rightKey = path.get(path.size() - 2);  
        if (!((leftKey.upperState.forestLabel == ForestLabel.L &&
               leftKey.lowerState.forestLabel == ForestLabel.L) ||
              (rightKey.upperState.forestLabel == ForestLabel.R &&
               rightKey.lowerState.forestLabel == ForestLabel.R))) {
          ForestLabel[] upperLabels = new ForestLabel[path.size() - 2];
          ForestLabel[] lowerLabels = new ForestLabel[path.size() - 2];
          int upperNumLeft = 0;
          int lowerNumLeft = 0;
          for (int i = 0; i < path.size() - 2; i++) {
            ForestKey key = path.get(i+1);
            if (key.upperState.ofPointer == OfPointer.LEFT) {
              upperNumLeft++;
            }
            if (key.lowerState.ofPointer == OfPointer.LEFT) {
              lowerNumLeft++;
            }
            upperLabels[i] = key.upperState.forestLabel;
            lowerLabels[i] = key.lowerState.forestLabel;
          }
          return new ForestPair(upperLabels, lowerLabels, upperNumLeft, lowerNumLeft);         
        }
     }
   }
 }
