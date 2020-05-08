 package fi.lolcatz.profiledata;
 
 import java.util.ArrayList;
 
 /**
  * Class that holds counter information from profiling. Use this class by first calling addBasicBlock command for each
  * basic block that is profiled. After that call initialize() and the public arrays become usable.
  */
 public class ProfileData {
 
     /**
      * Holds information how many times basic block has been called. Indexes are the ones returned by addBasicBlock().
      */
     public static long[] callsToBasicBlock;
     private static int basicBlockAmount = 0;
     /**
      * How costly a basic block is. Naive way to count it is by assuming every bytecodes cost is 1 and multiply by
      * amount of bytecodes in the basic block.
      */
     public static long[] basicBlockCost;
     private static ArrayList<Long> basicBlockCostAccumulator = new ArrayList<Long>();
     private static ArrayList<String> basicBlockDesc = new ArrayList<String>();
     
     private static boolean profilingAllowed = true;
     private static int numOfBlockingThreads = 0;
     
     /**
      * Adds basic block with default cost (1).
      * 
      * @return Index of basic block in arrays.
      */
     public static int addBasicBlock() {
         return addBasicBlock(1L);
     }
 
     /**
      * Adds a new basic block.
      * 
      * @param cost
      *            Cost of the basic block.
      * @return Index of the basic block in the arrays.
      */
     public static int addBasicBlock(long cost) {
         return addBasicBlock(cost, "");
     }
 
     /**
      * Adds a new basic block with a description.
      * 
      * @param cost
      *            Cost of the basic block.
      * @param desc
      *            Description of the basic block.
      * @return Index of the basic block in the arrays.
      */
     public static int addBasicBlock(long cost, String desc) {
         basicBlockAmount++;
         basicBlockCostAccumulator.add(cost);
         basicBlockDesc.add(desc);
         return basicBlockAmount - 1;
     }
 
     public static void incrementCallsToBasicBlock(int index) {
         if (profilingAllowed) callsToBasicBlock[index] += 1;
     }
 
     /**
      * Reset callsToBasicBlock arrays elements to 0.
      */
     public static void resetCounters() {
         for (int i = 0; i < callsToBasicBlock.length; i++) {
             callsToBasicBlock[i] = 0L;
         }
     }
 
     /**
      * Initialize arrays from added basic blocks. Needs to be called before using the arrays.
      */
     public static void initialize() {
         long[] newCallsToBasicBlock = new long[basicBlockAmount];
         if (callsToBasicBlock != null) {
             System.arraycopy(callsToBasicBlock, 0, newCallsToBasicBlock, 0, callsToBasicBlock.length);
         }
         callsToBasicBlock = newCallsToBasicBlock;
 
         basicBlockCost = new long[basicBlockCostAccumulator.size()];
         for (int i = 0; i < basicBlockCostAccumulator.size(); i++) {
             basicBlockCost[i] = basicBlockCostAccumulator.get(i);
         }
     }
 
     /**
      * Reset everything. Removes added basic blocks.
      */
     public static void resetBasicBlocks() {
         callsToBasicBlock = null;
         basicBlockCost = null;
         basicBlockAmount = 0;
         basicBlockCostAccumulator = new ArrayList<Long>();
     }
 
     public static long[] getCallsToBasicBlock() {
         return callsToBasicBlock;
     }
 
     public static int getBasicBlockAmount() {
         return basicBlockAmount;
     }
 
     public static long[] getBasicBlockCost() {
         return basicBlockCost;
     }
 
     public static ArrayList<String> getBasicBlockDesc() {
         return basicBlockDesc;
     }
 
     
     /**
      * Are counter increments allowed.
      * @return 
      */
     public static boolean isProfilingAllowed() {
         return profilingAllowed;
     }
     
     /**
      * Disallow counter increments.
      */
     public static synchronized void disallowProfiling() {
        numOfBlockingThreads++;
        profilingAllowed = false;
     }
     
     /**
      * Allow counter increments.
      */
     public static synchronized void allowProfiling() {
        numOfBlockingThreads--;
        if (numOfBlockingThreads <= 0) {
            profilingAllowed = true;
            numOfBlockingThreads = 0;
        }
         
     }
 }
