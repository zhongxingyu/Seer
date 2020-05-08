 package cs554.proj.slidingtiles;
 
 import java.util.Random;
 
 import android.view.View;
 import android.widget.Button;
 
 /**
  * This class abstracts the grid UI. This can be used to represent either the user's
  * grid or the AI's grid depending on what information is sent via the constructor. 
  * Row and column numbers are 1 counted.
  * 
  * @author me
  *
  */
 public class Grid {
 	/**
 	 * Matrix of the grid button IDs as laid out in the UI
 	 */
 	private int[][] gbIDs;
 	
 	/**
 	 * Size of the grid (2, 3, 4, or 5)
 	 */
 	private int gridSize;
 	
 	/**
 	 * Matrix of the Button objects as laid out in the UI
 	 */
 	private Button[][] gbButtons;
 	
 	
 	/**
 	 * Row of the hidden button (1 counted)
 	 */
	int hbRow = -1;
 	
 	/**
 	 * Column of the hidden button (1 counted)
 	 */
	int hbCol = -1;
 	
 	/**
 	 * Set up the grid with the IDs and objects used in the UI
 	 * 
 	 * @param size Grid size (2, 3, 4 or 5)
 	 * @param ids Button IDs used in the UI
 	 * @param buttons Button objects created by the UI
 	 */
 	public Grid(int size, int[][] ids, Button[][] buttons) {
 		// Set grid size
 		gridSize = size;
 		
 		// Copy the IDs and objects into the class
 		gbIDs = new int[size][size];
 		gbButtons = new Button[size][size];
 		for(int i = 0; i < size; i++) {
 			for(int j = 0; j < size; j++) {
 				gbIDs[i][j] = ids[i][j];
 				gbButtons[i][j] = buttons[i][j];
 			}
 		}
 	}
     
     /**
      * Gets the size of the grid being used
      * 
      * @return The size of the grid
      */
     public int getGridSize() {
     	return gridSize;
     }
     
     /**
      * Makes sure the button specified by row and col is contained in this grid
      * 
      * @param row Row of the button we're interested in 
      * @param col Column of the button we're interested in
      * @return True if grid contains the button, false otherwise
      */
     public boolean checkBounds(int row, int col) {
     	// Row and column have to be at least 1 and at most maxSize
     	if((row < 1) || (row > gridSize) || (col < 1) || (col > gridSize))
     		return false;
     	return true;
     }
     
     /**
      * Returns the button ID that corresponds to that particular row and column
      * 
      * @param row Row in question (1 counted)
      * @param col Column in question (1 counted)
      * @return -1 if indices are out of bounds, button ID otherwise
      */
     public int getButtonID(int row, int col) {
     	if(!checkBounds(row, col))
     		return -1;
     	else
     		return gbIDs[row-1][col-1];
     }
     
     /**
      * Hides a button on the grid. Only one button can be hidden at a time. If another
      * button was previously hidden, it is made visible again.
      * 
      * @param row Row of the button to hide
      * @param col Column of the button to hide
      * @return True if the button was hidden, false otherwise
      */
     public boolean hideButton(int row, int col) {
     	// Make sure button is in grid
     	if(!checkBounds(row, col))
     		return false;
     	
     	Button b = gbButtons[row-1][col-1];
     	b.setVisibility(View.GONE);
     		
     	// If a previous button was hidden, make visible
     	if(hbCol != -1) {
     		showButton(hbRow, hbCol);
     	}
         	
         // Update hidden button indices
         hbCol = col;
         hbRow = row;
     	
     	return true;
     }
     
     /**
      * Make a button visible
      * 
      * @param row Row of the button
      * @param col Column of the button
      * @return True if made visible, false otherwise
      */
     private boolean showButton(int row, int col) {
     	// Make sure button is in grid
     	if(!checkBounds(row,col))
     		return false;
     	
     	// Get button and make visible
     	Button b = gbButtons[row-1][col-1];
     	b.setVisibility(View.VISIBLE);
     	return true;
     }
     
     /**
      * Set the text shown on a button
      * 
      * @param row Row index of the button
      * @param col Column index of the button
      * @param text Text for the button
      * @return True if value was set, false otherwise
      */
     public boolean setButtonText(int row, int col, String text) {
     	// Make sure button is in grid
     	if(!checkBounds(row,col))
     		return false;
     	
     	// Set text on button
     	Button b = gbButtons[row-1][col-1];
     	b.setText(text);
     	return true;
     }
     
     /**
      * Get the text currently on a button
      * 
      * @param row Row index of the button
      * @param col Column index of the button
      * @return Value on the button or empty string if button isn't in grid
      */
     public String getButtonText(int row, int col) {
     	// Make sure button is in grid
     	if(!checkBounds(row, col))
     		return "";
     	
     	// Get button text
     	Button b = gbButtons[row-1][col-1];
     	return (String) b.getText();
     }
     
     /**
      * Get the row index of a button by button id
      * 
      * @param ID for the button (from R.java)
      * @return row of button or -1 if button not in grid
      */
     public int getRow(int button) {
     	// Find button
     	for(int i = 0; i < gbIDs.length; i++) {
     		for(int j = 0; j < gbIDs[i].length; j++) {
     			if(button == gbIDs[i][j])
     				return i+1;
     		}
     	}
     	
     	// Button not in grid
     	return -1;
     }
     
     /**
      * Get the column index of a button by button id
      * 
      * @param button ID for the button (from R.java)
      * @return Column index for button or -1 if button not in grid
      */
     public int getCol(int button) {
     	// Find button
     	for(int i = 0; i < gbIDs.length; i++) {
     		for(int j = 0; j < gbIDs[i].length; j++) {
     			if(button == gbIDs[i][j])
     				return j+1;
     		}
     	}
     	
     	// Button not in grid
     	return -1;
     }
     
     /**
      * Processes a grid button being pressed. If the button pressed is in the row or
      * column of the missing tile, that button and any between it and the missing tile
      * are shifted one space toward the missing tile. The missing tile will end up
      * being in the location of this button when finished. If the button pressed is not
      * in line with the hidden tile, nothing happens.
      * 
      * @param row Row index of button that was pressed
      * @param col Column index of the button that was pressed
      */
     public void processButtonPress(int row, int col) {
     	// Ignore the AI pressing on the hidden button
     	if((row == hbRow) && (col == hbCol))
     		return;
     	
     	// Check if row or column matches
     	if(row == hbRow) {
     		// Check if we're shifting right or shifting left in the row
     		if(col > hbCol) {
     			// Shift left
     			for(int i = hbCol; i < col; i++)
     				setButtonText(hbRow, i, getButtonText(hbRow, i+1));
     		} else {
     			// Shift right
     			for(int i = hbCol; i > col; i--)
     				setButtonText(hbRow, i, getButtonText(hbRow, i-1));
     		}
     		// Move missing tile
     		hideButton(row, col);
     	} else if(col == hbCol) {
     		// Check if we're shifting up or down in the column
     		if(row > hbRow) {
     			// We're shifting up
         		for(int i = hbRow; i < row; i++)
         			setButtonText(i, hbCol, getButtonText(i+1, hbCol));
     		} else {
     			// We're shifting down
         		for(int i = hbRow; i > row; i--)
         			setButtonText(i, hbCol, getButtonText(i-1, hbCol));
     		}
     		// Move missing tile
     		hideButton(row, col);
     	}    	
     }
     
     /**
      * Scrambles a grid by making numMoves on the grid. All moves are valid. The higher
      * the number, the more scrambled the grid will be from its original layout.
      * 
      * @param numMoves Number of sliding moves to make
      */
     public void scrambleGrid(int numMoves) {
     	// Use a random number generate to choose which tiles to slide
     	Random gen = new Random();
     	
     	// Loop until all moves have been made
     	while(numMoves > 0) {
     		// For choosing whether we move a row or column
     		int dir = gen.nextInt(2);
     		
     		// For choosing which tile is "clicked". We're using a value of
     		// one less then the grid size here so we don't choose the hidden tile
     		int tile = gen.nextInt(gridSize-1)+1;
     		
     		if(dir == 0) {
     			// We're moving a column. If the tile value is greater or equal
     			// to the hidden button row we need to add one to account for the
     			// hidden tile
     			if(tile >= hbRow)
     				tile++;
     			
     			// Move column based on the button at row tile and column hbCol
     			// being pressed
     			processButtonPress(tile, hbCol);
     		} else {
     			// We're moving a row. If the tile value is greater or equal to
     			// the hidden button column we need to add one to account for the
     			// hidden tile
     			if(tile >= hbCol)
     				tile++;
     			
     			// Move row based on the button at row hbRow and column tile
     			// being pressed
     			processButtonPress(hbRow, tile);
     		}
     		
     		// Count this move
     		numMoves--;
     	}
     }
     
     /**
      * Get the row and column index of the hidden tile
      * 
      * @return Array of two ints, first is row index and the second is column index
      */
     public int[] getHiddenButtonLocation() {
     	int loc[] = new int[2];
     	loc[0] = hbRow;
     	loc[1] = hbCol;
     	return loc;
     }
 	
 	/**
 	 * Copies one grid to another. The hidden button will be in the same location and 
 	 * the tiles will have the same text. Both grids have to be the same size.
 	 * 
 	 * @param g Grid to copy from
 	 * @return True if the grid was successfully copied, false otherwise
 	 */
 	public boolean copyGrid(Grid g) {
 		// Make sure the grids are the same size
 		if(g.getGridSize() != gridSize)
 			return false;
 		
 		// Copy tile value
 		for(int i = 0; i < gridSize; i++) {
 			for(int j = 0; j < gridSize; j++) {
 				Button b = (Button) gbButtons[i][j];
 				b.setText(g.getButtonText(i+1, j+1));
 			}
 		}
 		
 		// Hide the same button
 		int[] hbLoc = g.getHiddenButtonLocation();
 		hideButton(hbLoc[0], hbLoc[1]);
 		
 		return true;
 	}
 }
