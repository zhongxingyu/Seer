 import java.util.ArrayList;
 import java.util.List;
 
 
 public class BoardRep {
 
 	private CellTree[][] cellTrees;
	private int globalDeltaId;
 
 	public BoardRep(final Board.CellTypes[][] layout) {
 	  globalDeltaId = 0;
 
 	  // initialize board
 	  final int nrows = layout.length;
 	  final int ncols = layout[0].length;
 	  cellTrees = new CellTree[nrows][ncols];
 
 	  for (int r = 0; r < nrows; r++) {
 	    for (int c = 0; c < ncols; c++) {
 	      cellTrees[r][c] = new CellTree(layout[r][c], globalDeltaId);
 	    }
 	  }
 	}
 
 	public Board.CellTypes get(final int r, final int c) {
 	  return cellTrees[r][c].get(globalDeltaId);
 	}
 
 	public int set(final int r, final int c, final Board.CellTypes newType) {
 	  cellTrees[r][c].addNewCell(globalDeltaId, newType, ++globalDeltaId);
 	  return globalDeltaId;
 	}
 
 	public int set(final int[] r, final int[] c, final Board.CellTypes[] newTypes) {
 	  assert(r.length == c.length && c.length == newTypes.length);
 	  globalDeltaId++;
 	  for (int i = 0; i < r.length; i++) {
 	    cellTrees[r[i]][c[i]].addNewCell(globalDeltaId-1, newTypes[i], globalDeltaId);
 	  }
 	  return globalDeltaId;
 	}
 
 	public void revert(final int deltaId) {
 	  globalDeltaId = deltaId;
 	}
 
 	// --------------- Cells ---------------
 	class CellTree {
 	  private Cell root;     // Root of the cell tree.
 	  private Cell current;  // Cache of the current cell in the cell tree; if current.deltaId !=
 	                         // query deltaId, current is stale.
 
 	  public CellTree(final Board.CellTypes rootCellType, final int rootDeltaId) {
 	    root = new Cell(rootCellType, rootDeltaId);
 	    current = root;
 	  }
 
 	  public Board.CellTypes get(final int queryDeltaId) {
 	    if (current.deltaId != queryDeltaId) {
 	      current = findCell(queryDeltaId);
 	    }
 	    return current.cellType;
 	  }
 
 	  public void addNewCell(final int oldDeltaId,
 	      final Board.CellTypes cellType, final int newDeltaId) {
 	    if (current.deltaId != oldDeltaId) {
 	      current = findCell(oldDeltaId);
 	    }
 	    final Cell newCell = new Cell(cellType, newDeltaId);
 	    current.children.add(newCell);
 	    current = newCell;
 	  }
 
 	  public Cell findCell(final int queryDeltaId) {
 
 	  }
 	}
 
 	private class Cell {
 	  public Board.CellTypes cellType;
 	  public int deltaId;
 	  public List<Cell> children;
 	  public Cell parent;
 
 	  public Cell(final Board.CellTypes inCellType, final int inDeltaId) {
 	    cellType = inCellType;
 	    deltaId = inDeltaId;
 	    children = new ArrayList<Cell>(11);
 	    parent = null;
 	  }
 	}
 }
