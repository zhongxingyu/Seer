 package mazeEscapeApp;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.List;
 
 import org.jhotdraw.figures.RectangleFigure;
 import org.jhotdraw.framework.DrawingView;
 import org.jhotdraw.framework.FigureAttributeConstant;
 import org.jhotdraw.framework.HandleEnumeration;
 import org.jhotdraw.standard.HandleEnumerator;
 import org.jhotdraw.util.CollectionsFactory;
 
 /**
  * A class representing the Clickable area in a grid cell
  * 
  */
 public class GCellArea extends RectangleFigure {
 	private static final long serialVersionUID = 4658070300128221307L;
 	private int row, column;
 	private MazeEscape maze;
 	private DrawingView view;
	
 	// Variables for creation of player
 	private static Pinhead larry;
 	private static final int SHIFT = 50;
 
 	public GCellArea() {
 		super();
 	}
 
 	public GCellArea(Point origin, Point corner, MazeEscape m) {
 		super(origin, corner);
 		maze = m;
 		view = maze.view();
 	}
 
 	/*
 	 * Used by handleClick to check if two cells are adjacent.
 	 */
 	private boolean areCellsAdjacent(GCellArea cell1, GCellArea cell2) {
 		GCellArea[] adjCells = cell1.getAdjacentGCells();
 
 		for (GCellArea gc : adjCells) {
 			if (gc != null && gc.getRow() == cell2.getRow()
 					&& gc.getColumn() == cell2.getColumn()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Handles the clicks on any given grid cell.
 	 */
 	private void handleClick() {
 		// Don't allow the user to move away from end cell after reached.
 		if (maze.isFirstClick() == true) {
 			// The first click should be the startCell.
 			int r = maze.getStartCell().row;
 			int c = maze.getStartCell().column;
 			larry = new Pinhead(new Point(SHIFT + c
 					* maze.getgCellPixelLength(), SHIFT + r
 					* maze.getgCellPixelLength()), new Point(SHIFT + c
 					* maze.getgCellPixelLength() + maze.getgCellPixelLength(),
 					SHIFT + r * maze.getgCellPixelLength()
							+ maze.getgCellPixelLength()));
 			larry.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.PINK);
 			larry.setAttribute(FigureAttributeConstant.FRAME_COLOR, Color.RED);
 			view.add(larry);
 			maze.setCurrentlySelected(maze.getStartCell());
 			maze.getStartCell().setAttribute(
 					FigureAttributeConstant.FILL_COLOR, Color.CYAN);
 			maze.setFirstClick(false);
 			System.out.println("First Click");
 			maze.setStepsTaken(maze.getStepsTaken() + 1);
 		} else if (maze.isReachedEndCell() == true) {
 
 		} else if (this == maze.getEndCell()
 				&& maze.isReachedEndCell() == false
 				&& areCellsAdjacent(maze.getCurrentlySelected(), this)) {
 			view.remove(larry);
 			maze.setReachedEndCell(true);
 			System.out.println("You win.");
 			maze.setStepsTaken(maze.getStepsTaken() + 1);
 		} else {
 			// Essentially, check to see if the currently selected gridcell is
 			// adjacent to the newly selected cell. If so, then move; otherwise,
 			// let nothing happen.
 
 			if (areCellsAdjacent(maze.getCurrentlySelected(), this)) {
 				maze.setCurrentlySelected(this);
 				int r = maze.getCurrentlySelected().row;
 				int c = maze.getCurrentlySelected().column;
 				// Removes the player from the previous cell
 				view.remove(larry);
 				// Creates player in newly selected cell
 				larry = new Pinhead(new Point(SHIFT + c
 						* maze.getgCellPixelLength(), SHIFT + r
 						* maze.getgCellPixelLength()), new Point(SHIFT + c
						* maze.getgCellPixelLength() + maze.getgCellPixelLength(),
						SHIFT + r * maze.getgCellPixelLength()
								+ maze.getgCellPixelLength()));
				larry.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.PINK);
				larry.setAttribute(FigureAttributeConstant.FRAME_COLOR, Color.RED);
 				view.add(larry);
 				this.setAttribute(FigureAttributeConstant.FILL_COLOR,
 						Color.CYAN);
 				maze.setStepsTaken(maze.getStepsTaken() + 1);
 
 			}
 		}
 
 	}
 
 	// Empty body so no translation happens
 	protected void basicMoveBy(int x, int y) {
 	}
 
 	@Override
 	public HandleEnumeration handles() {
 		@SuppressWarnings("rawtypes")
 		List handles = CollectionsFactory.current().createList();
 		// BoxHandleKit.addHandles(this, handles);
 
 		// Handle clicks on maze boxes. Probably not the best place, but it
 		// works as intended.
 		handleClick();
 
 		return new HandleEnumerator(handles);
 	}
 
 	@Override
 	public void drawFrame(Graphics g) {
 
 	}
 
 	public GCellArea[] getAdjacentGCells() {
 		GCellArea[] adjacentCells = new GCellArea[4];
 
 		GridCell[][] gCells = maze.getGridCells();
 
 		// North, South, East, West
 		if (row > 0 && !gCells[row][column].isUp()) {
 			adjacentCells[0] = maze.getgCellClickableArea()[row - 1][column];
 		}
 		if (row < maze.getgCellClickableArea().length - 1
 				&& !gCells[row][column].isDown()) {
 			adjacentCells[1] = maze.getgCellClickableArea()[row + 1][column];
 		}
 		if (column < maze.getgCellClickableArea().length - 1
 				&& !gCells[row][column].isRight()) {
 			adjacentCells[2] = maze.getgCellClickableArea()[row][column + 1];
 		}
 		if (column > 0 && !gCells[row][column].isLeft()) {
 			adjacentCells[3] = maze.getgCellClickableArea()[row][column - 1];
 		}
 
 		return adjacentCells;
 	}
 
 	public int getRow() {
 		return row;
 	}
 
 	public void setRow(int row) {
 		this.row = row;
 	}
 
 	public int getColumn() {
 		return column;
 	}
 
 	public void setColumn(int column) {
 		this.column = column;
 	}
 
 }
