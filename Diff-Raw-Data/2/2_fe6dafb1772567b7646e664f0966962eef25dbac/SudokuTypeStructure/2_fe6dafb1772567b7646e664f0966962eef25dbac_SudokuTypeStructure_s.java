 package net.lemonfactory.sudoku;
 
 import java.awt.Color;
 
 /**
  * <p>
  * {@link SudokuTypeStructure} represents a structure of a Sudoku type,
  * which is independent of symbols or a blank character.
  * </p>
  * <p>
 * This interface must be implement as immutable.
  * </p>
  *
  * @author Chungmin Lee
  */
 public interface SudokuTypeStructure {
 
     /**
      * Returns the size of this structure.
      * Size is the number of cells in one cell group (a row, column or etc.).
      *
      * @return the size of this structure
      */
     public int size();
 
     /**
      * Returns the width of a box. If the structure has no boxes, it returns 0.
      *
      * @return the width of a box or 0 if the structure has no boxes
      */
     public int boxWidth();
 
     /**
      * Returns the height of a box. If the structure has no boxes,
      * it returns 0.
      *
      * @return the height of a box or 0 if the structure has no boxes
      */
     public int boxHeight();
 
     /**
      * Returns the number of all cells of this structure.
      *
      * @return the number of all cells of this structure
      */
     public int getTotalCells();
 
     public int getNumCellGroups();
 
     /**
      * <p>
      * Returns indexes of cell groups to which the specified cell belongs.
      * </p>
      * <p>
      * <b>Warning:</b> Unlike other normal methods returning an array,
      * the returned array must not be a newly allocated one (for performance);
      * a caller must not change the values in the returned array (and there is
      * absolutely no reason to do so).
      * </p>
      *
      * @param cell indes of the cell whose cell groups' indexes to be returned
      * @return indexes of cell groups to which the specified cell belongs
      */
     public int[] getCellGroupIndexes(int cell);
 
     /**
      * <p>
      * Returns the speicified cell group. A cell group is an array of indexes
      * of cells where each cell must have distinct symbol from each other.
      * It is guaranteed that the returned array is sorted in ascending order.
      * </p>
      * <p>
      * <b>Warning:</b> Unlike other normal methods returning an array,
      * the returned array must not be a newly allocated one (for performance);
      * a caller must not change the values in the returned array (and there is
      * absolutely no reason to do so).
      * </p>
      *
      * @param cellGroupIndex index of the cell group to be returned
      * @return the specified cell group
      */
     public int[] getCellGroup(int cellGroupIndex);
 
     public int[] getCellGroupDiff(int cgIndex1, int cgIndex2);
 
     /**
      * <p>
      * Returns indexes of cells which are belongs to the same cell group as the
      * specified cell. It is guaranteed that the returned array is sorted in
      * ascending order.
      * </p>
      * <p>
      * <b>Warning:</b> Unlike other normal methods returning an array,
      * the returned array must not be a newly allocated one (for performance);
      * a caller must not change the values in the returned array (and there is
      * absolutely no reason to do so).
      * </p>
      *
      * @param cell index of the cell whose neighbors to be returned
      * @return indexes of cells which are belongs to the same cell group as the
      *     specified cell
      */
     public int[] getNeighborCells(int cell);
 
     public int getNumIntersections(int cellGroupIndex1, int cellGroupIndex2);
 
     /**
      * <p>
      * Returns the color of the specified cell group, for use of graphical
      * interfaces. {@code null} is returned if there is no assigned color for
      * the cell group.
      * </p>
      * <p>
      * Note that colors are not considered when two structures are tested for
      * equality.
      * </p>
      *
      * @param cellGroupIndex index of the cell group whose color to be returned
      * @return the color of the specified cell group, or {@code null} if there
      *     is no assigned color for the cell group
      */
     public Color getCellGroupColor(int cellGroupIndex);
 
     public boolean equals(Object o);
 
     public int hashCode();
 }
