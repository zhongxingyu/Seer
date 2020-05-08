 package Paging;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * User: jpipe
  * Date: 4/1/13
  */
 public abstract class Pager {
 
     //for output of the table
     public static final int ROW_SIZE = 10;
 
     public static enum PAGER_TYPE {
         FIFO, LRU, OPTIMAL, LFU
     }
 
     public static Pager getPager(PAGER_TYPE type, int frames, List<Integer> tries) {
         switch (type) {
             case FIFO: return new FirstInFirstOut(frames, tries);
             case LRU : return new LeastRecentlyUsed(frames, tries);
             case LFU: return new LeastFrequentlyUsed(frames, tries);
             case OPTIMAL: return new OptimalPager(frames, tries);
             default: return null;
         }
     }
 
     protected int frameCount = 0;
 
     protected List<Page> state = new ArrayList<Page>();
     protected List<Integer> tries = new ArrayList<Integer>();
 
     protected int faults = 0;
 
     private List<List<Integer>> stateSnapshot = new ArrayList<List<Integer>>();
 
     public Pager(int frameCount, Integer... tries) {
         this.frameCount = frameCount;
         Collections.addAll(this.tries, tries);
     }
 
     public Pager(int frameCount, List<Integer> tries) {
         this.frameCount = frameCount;
         this.tries = new ArrayList<Integer>(tries);
     }
 
     protected boolean isPageFault(int pageNum) {
         for (Page p: state) {
             if (p.getId() == pageNum) {
                 return false;
             }
         }
         faults++;
         return true;
     }
 
     protected void takeStateSnapshot(int currentTry, boolean fault) {
         List<Integer> currentState = new ArrayList<Integer>();
         currentState.add(currentTry);
         if (fault) {
             for (int i = 0; i < frameCount; i++) {
                 if (i < state.size()) {
                     currentState.add(state.get(i).getId());
                 } else {
                     currentState.add(0);
                 }
             }
         }
         stateSnapshot.add(currentState);
     }
 
     public abstract void execute();
 
     public int getFaults() {
         return faults;
     }
 
     public void printTable() {
         printSnapshots(stateSnapshot);
     }
 
     private void printSnapshots(List<List<Integer>> snapshots) {
         if (snapshots.size() < ROW_SIZE) {
             for (List<Integer> snapshot : snapshots) {
                 System.out.print(snapshot.get(0) + "\t");
             }
             System.out.println("\n----------------------------------------" +
                     "----------------------------------------");
             for (int i = 0; i < frameCount; i++) {
                 for (List<Integer> snapshot : snapshots) {
                     if (snapshot.size() > 1) {
                         System.out.print(snapshot.get(i + 1) + "\t");
                     } else {
                         System.out.print(" \t");
                     }
                 }
                 System.out.println();
             }
             System.out.println("========================================" +
                     "========================================");
         } else {
             printSnapshots(snapshots.subList(0, ROW_SIZE - 1));
            printSnapshots(snapshots.subList(ROW_SIZE, snapshots.size()));
         }
     }
 
 }
