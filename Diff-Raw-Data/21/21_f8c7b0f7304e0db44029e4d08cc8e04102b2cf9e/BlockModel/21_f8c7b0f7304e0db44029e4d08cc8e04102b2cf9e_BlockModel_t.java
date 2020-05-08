 package demo.client.shared.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.errai.common.client.api.annotations.Portable;
 
 /*
  * The base class for a falling block in a Block Drop BoardModel.
  */
 @Portable
 public class BlockModel {
 
   public static final int BASIC_CODE = 1;
 
   private static int idGen = 1;
 
   /*
    * A list of SquareModels, containing the squares of squares.
    */
   private List<SquareModel> squares;
   /* A unique id for identifying this block. */
   private int id;
 
   public BlockModel() {
     this(generateId());
   }
 
   /*
    * Initializes BlockModel id. All subclasses should invoke this constructor using the return value
    * of generateId() as the argument.
    */
   protected BlockModel(int id) {
     this.id = id;
     squares = new ArrayList<SquareModel>();
   }
 
   /*
    * Set the squares of the squares in this block from the blocks main position. The position of a
    * single square on the board is calculated as main position + offset.
    * 
    * @param squares An array of integer pairs, representing row and column squares from this blocks
    * main position.
    */
   protected void setOffsets(int[][] offsets, int code) {
     this.squares = new ArrayList<SquareModel>();
 
     for (int i = 0; i < offsets.length; i++) {
       this.squares.add(new SquareModel(offsets[i][0], offsets[i][1], code));
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object other) {
     return other.getClass() == BlockModel.class && this.getId() == ((BlockModel) other).getId();
   }
 
   /*
    * Get the id of this BlockModel.
    * 
    * @return The id of this BlockModel.
    */
   public int getId() {
     return id;
   }
 
   protected static int generateId() {
     return idGen++;
   }
 
   /*
    * Get the integer representing this type of block on the board.
    * 
    * @return An integer representing this type of block on the board.
    */
   public static int getCode() {
     return BASIC_CODE;
   }
 
   public Iterable<SquareModel> getIterator() {
     return squares;
   }
 
   /*
    * Rotate the squares of the squares in this block by 90 degrees clockwise. Blocks that do not
    * rotate around a central square should override this method.
    */
   public void rotate() {
     for (SquareModel square : squares) {
       // Calculate new squares (calculation derived from rotation matrix by 90 degrees)
       int newRowOffset = -1 * square.getCol();
       int newColOffset = square.getRow();
 
       // Assign new squares.
       square.setRow(newRowOffset);
       square.setCol(newColOffset);
     }
   }
 
   /*
    * Reverse the effect of a call to this.rotate.
    */
   public void unrotate() {
     for (SquareModel square : squares) {
       // Calculate new squares (calculation derived from rotation matrix by -90 degrees).
       int newRowOffset = square.getCol();
       int newColOffset = -1 * square.getRow();
 
       // Assign new squares.
       square.setRow(newRowOffset);
       square.setCol(newColOffset);
     }
   }
 
   public void addSquare(SquareModel square) {
     squares.add(square);
   }
 
   public double getCentreRowDiff() {
     return 0;
   }
 
   public double getCentreColDiff() {
     return 0;
   }

  public int getStartingRow() {
    return -2;
  }
 }
