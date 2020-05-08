 package demo.client.shared.model;
 
 /*
  * A long straight block in Block Drop.
  */
 public class LongBlockModel extends ToggledBlockModel {
 
   public static final int CODE = 2;
 
   /*
    * Create a LongBlockModel instance.
    */
   public LongBlockModel() {
     // Assign unique id.
     super(generateId());
 
     setOffsets(new int[][] { new int[] { -1, 0 }, // Top square.
         new int[] { 0, 0 }, // Anchor of block.
         new int[] { 1, 0 }, // Second-from-bottom square.
         new int[] { 2, 0 } // Bottom square.
             }, getCode());
   }
 
   public double getCentreRowDiff() {
     return -0.5;
   }
 
   public static int getCode() {
     return CODE;
   }

  @Override
  public int getStartingRow() {
    return -3;
  }
 }
