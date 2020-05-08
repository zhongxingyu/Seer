 package test1;
 
 import java.io.Serializable;
 
 import javax.swing.JOptionPane;
 
 
 /**
  * @author cesarcruz && yamilasusta
  *
  */
 public class TheGrid implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8284307328057296169L;
 	public Tiles[][]  theGrid;
 	private static int numberOfGrids = 2;
 	private static final int WIDTH_OF_GRID = 600;
 	private static final int HEIGHT_OF_GRID = 300;
 	private static int numberOfColumns = 10;
 	private static int numberOfRows = 10;
 	private static int gridObjects = 0;
 	private static int boatSerial;
 
 	/**
 	 * Creates a Grid object that in itself is an array of Tiles, each contains its location. 
 	 */
 	public TheGrid(){
 		int topX = ((WIDTH_OF_GRID/numberOfGrids)/(numberOfRows));
 		int topY = (HEIGHT_OF_GRID)/(numberOfColumns);
 		int offSet = (WIDTH_OF_GRID/numberOfGrids)*gridObjects;
 		theGrid = new Tiles[numberOfColumns][numberOfRows];
 		for(int i = 0; i < 10; i++){
 			for(int j = 0; j < 10; j++){
 				theGrid[i][j] = new Tiles((i+1)*topX + offSet, ((j+1)*topY) - 15, 
 						(WIDTH_OF_GRID/numberOfGrids)/(numberOfRows),
 						(WIDTH_OF_GRID/numberOfGrids)/(numberOfRows));
 				theGrid[i][j].addLocation(i, j);
 			}
 		}
 		gridObjects++;
 	}
 
 	/**
 	 * @param x will be the size of the matrix (x*y)
 	 */
 	public TheGrid(int x, int y){
 		numberOfColumns = y;
 		numberOfRows = x;
 		int topX = ((WIDTH_OF_GRID/numberOfGrids)/(numberOfRows));
 		int topY = (HEIGHT_OF_GRID)/(numberOfColumns);
 		int offSet = (WIDTH_OF_GRID/numberOfGrids)*gridObjects;
 		theGrid = new Tiles[x][y];
 		for(int i = 0; i < x; i++){
 			for(int j = 0; j < y; j++){
 				theGrid[i][j] = new Tiles((i+1)*topX + offSet, ((j+1)*topY)-
 						((HEIGHT_OF_GRID/numberOfColumns)-15), 
 						(WIDTH_OF_GRID/numberOfGrids)/(numberOfRows),
 						((HEIGHT_OF_GRID)/(numberOfColumns)));
 				theGrid[i][j].addLocation(i, j);
 			}
 		}
 		gridObjects++;
 	}
 
 	/**
 	 * 
 	 * @param x1 starting point of the boat in the x coordinate
 	 * @param x2 ending point of the boat in the x coordinate
 	 * @param y the number of the row
 	 * @return If the boat was placed.
 	 */
 	public boolean addBoatVertical(int y, int x1, int x2){
 		if(checkIfBoatVertical(y, x1, x2)) {	
 			for (int i = x1; i <= x2; i++) 
 				theGrid[y][i].setSerial(boatSerial);
 			return true;
 		}
 		else
 			return false;
 	}
 	/**
 	 * Adds serial to each boat when using Random.
 	 * @param serial
 	 */
 	public void setSerial(int serial){
 		boatSerial = serial;
 	}
 	/**
 	 * @param x the number of the column
 	 * @param y1 the starting point of the boat in the y coordinate
 	 * @param y2 the ending point of the boat in the y coordinate
 	 * @return If the boat was placed.
 	 */
 	public boolean addBoatHorizontal(int x, int y1, int y2){
 		if(checkIfBoatHorizontal(x, y1, y2)) {
 			for (int i = y1; i <= y2; i++) 
 				theGrid[i][x].setSerial(boatSerial);
 			return true;
 		}
 		else
 			return false;
 	}
 	/**
 	 * 
 	 * @param x Initial x coordinate
 	 * @param y Initial y coordinate
 	 * @param direction Direction on which to add the boat
 	 * @return If the boat was placed.
 	 */
 	public boolean addBoatDiagonal(int x, int y, boolean direction) {
 		if (checkIfBoatDiagonal(x, y, direction)) {
 			if(direction) {
 				for (int i = 0; i < boatSerial; i++) 
 					theGrid[i+y][i+x].setSerial(boatSerial);
 				return true;
 			}
 			else {
 				for (int i = 0; i < boatSerial; i++) 
 					theGrid[y+i][x-i].setSerial(boatSerial);
 				return true;
 			}
 		}
 		else 
 			return false;
 	}
 	/**
 	 * The three parameters indicate the location and the size of the boat
 	 * that will be place
 	 * @param x The current column
 	 * @param y1 Starting point for the row
 	 * @param y2 Final point for the row
 	 * @return true if the boat can be placed in the desired spot, false if it can't
 	 */
 	public boolean checkIfBoatHorizontal(int x, int y1, int y2){
 		for (int i = y1; i <= y2; i++) 
 			try {
 				if(theGrid[i][x].boatSerial() != 0)
 					return false;				
 			} catch (Exception e) {
 				return false;
 			}
		if(boatSerial == 1)
			return false;
 		return true;
 	}
 
 	/**
 	 * The three parameters indicate the location and the size of the boat
 	 * that will be place
 	 * @param y Current row
 	 * @param x1 Starting point for the column
 	 * @param x2 Final point for the column
 	 * @return true if the boat can be placed in the desired spot, false if it can't
 	 */
 	public boolean checkIfBoatVertical(int y, int x1, int x2){
 		try {
 			for (int i = x1; i <= x2; i++) 
 				if(theGrid[y][i].boatSerial() != 0)
 					return false;			
 		} catch (Exception e) {
 			return false;
 		}
		if(boatSerial == 1)
			return false;
 		return true;
 
 	}
 
 	/**
 	 * Verify if the space is available in the grid
 	 * @param x Initial x coordinate
 	 * @param y Initial x coorinate
 	 * @param direction Direction on which to lay the boat
 	 * @return If the space is available.
 	 */
 	public boolean checkIfBoatDiagonal(int x, int y, boolean direction) {
 		if(direction) {
 			try {
 				for (int i = 0; i < boatSerial; i++) 
 					if(theGrid[y+i][x+i].boatSerial() != 0)
 						return false;				
 			} catch (Exception e) {
 				return false;
 			}
 
 		}
 		else {
 			try {
 				for (int i = 0; i < boatSerial; i++) 
 					if(theGrid[y+i][x-i].boatSerial() != 0)
 						return false;					
 			} catch (Exception e) {
 				return false;
 			}
 
 		}
 		return true;
 	}
 
 	/**
 	 * Check the status of a boat.
 	 * @param boatnumber Size of the boat to check
 	 * @return The boat is sunken or not.
 	 */
 	public boolean isSunken(int boatnumber) {
 		int counter = 0;
 		for (int i = 0; i < theGrid.length; i++) 
 			for (int j = 0; j < theGrid[0].length; j++) 
 				if(theGrid[i][j].boatSerial() == boatnumber && theGrid[i][j].isHit())
 					counter++;
 		if (counter == boatnumber) 
 			return true;
 		return false;
 	}
 	/**
 	 * Performs a quick test to verify the winner
 	 * @param totalShips
 	 * @return If the player has won.
 	 */
 	public boolean allSunken(int totalShips) {
 		int counter = 0;
 		for (int i = 2; i <= 10; i++) 
 			if(isSunken(i))
 				counter++;
 		if(counter == totalShips)
 			return true;
 		return false;			
 	}
 
 	/**
 	 * 
 	 * @param x amount of grids that you want to add to the game
 	 */
 	public void numberOfGrids(int x){
 		numberOfGrids = x;
 	}
 	/**
 	 * Cleans up the offset for the gridObjects
 	 */
 	public void resetGridOffset(){
 		gridObjects = 0;
 	}
 	
 	/**
 	 * 
 	 * @param input The string to be parsed
 	 * @return If the input is a valid one.
 	 */
 	static boolean verifyInput(String input) {
 		if(input.isEmpty())
 			return false;
 		char[] parse = input.toCharArray();
 		if(!Character.isLetter(parse[0]))
 			return false;
 		for (int i = 1; i < parse.length; i++) 
 			if (!Character.isDigit(parse[i])) 
 				return false;
 		return true;
 	}
 
 	/**
 	 * Places the boats on the grid.
 	 * @param x The amount of boats to be placed.
 	 * @param diagonal Indicates if diagonal boats may be used.
 	 */
 	public void placeTheBoats(int x, boolean diagonal){
 		boatSerial = 2;
 		if(!diagonal) {
 			for(int i = 0; i < x; i++){
 				boolean accepted = false;
 				do {
 					boolean parsed = false;
 					int y1;
 					int x1;
 					int y2;
 					int x2;
 					String[] first = new String[2];
 					do {
 						String boatCoordinates = JOptionPane.showInputDialog(null, "Enter boat " +(i+1)+ " coordinates. Current size of boat is: " + boatSerial,"");
 						boatCoordinates = boatCoordinates.toLowerCase();
 						try {
 							first = boatCoordinates.split("-");
 							if(verifyInput(first[0]) && verifyInput(first[1]))
 								parsed = true;							
 						} catch (Exception e) {}
 
 					} while (!parsed);
 
 					y1 = Character.getNumericValue(first[0].charAt(0)) - 10;
 					x1 = Integer.parseInt(first[0].substring(1)) - 1;
 					y2 = Character.getNumericValue(first[1].charAt(0)) - 10;
 					x2 = Integer.parseInt(first[1].substring(1)) - 1;
 
 					if(x1 == x2 && Math.abs(y2-y1)+1 == boatSerial) {
 						if(y1 > y2) {
 							int temp = y2;
 							y2 = y1;
 							y1 = temp;
 						}
 						accepted = addBoatHorizontal(x1, y1, y2);
 					}
 					else if (y1 == y2 && Math.abs(x2-x1)+1 == boatSerial) {
 						if(x1 > x2) {
 							int temp = x2;
 							x2 = x1;
 							x1 = temp;
 						}
 						accepted = addBoatVertical(y1, x1, x2);
 					}	
 				} while (!accepted);
 				boatSerial++;
 			}
 		}
 		else {
 			for(int i = 0; i < x; i++){
 				boolean accepted = false;
 				do {
 					boolean parsed = false;
 					int y1;
 					int x1;
 					int y2;
 					int x2;
 					String[] first = new String[2];
 					do {
 						String boatCoordinates = JOptionPane.showInputDialog(null, "Enter boat " +(i+1)+ " coordinates. Current size of boat is: " + boatSerial,"");
 						boatCoordinates = boatCoordinates.toLowerCase();
 						try {
 							first = boatCoordinates.split("-");
 							if(verifyInput(first[0]) && verifyInput(first[1]))
 								parsed = true;							
 						} catch (Exception e) {}
 
 					} while (!parsed);
 
 					y1 = Character.getNumericValue(first[0].charAt(0)) - 10;
 					x1 = Integer.parseInt(first[0].substring(1)) - 1;
 					y2 = Character.getNumericValue(first[1].charAt(0)) - 10;
 					x2 = Integer.parseInt(first[1].substring(1)) - 1;
 
 					if(x1 == x2 && Math.abs(y2-y1)+1 == boatSerial) {
 						if(y1 > y2) {
 							int temp = y2;
 							y2 = y1;
 							y1 = temp;
 						}
 						accepted = addBoatHorizontal(x1, y1, y2);
 					}
 					else if (y1 == y2 && Math.abs(x2-x1)+1 == boatSerial) {
 						if(x1 > x2) {
 							int temp = x2;
 							x2 = x1;
 							x1 = temp;
 						}
 						accepted = addBoatVertical(y1, x1, x2);
 					}	
 					else if (Math.abs(x1-x2)+1 == boatSerial && Math.abs(y1-y2)+1 == boatSerial) {
 						if(y2 < y1) {
 							int temp = y2;
 							y2 = y1;
 							y1 = temp;
 
 							temp = x2;
 							x2 = x1;
 							x1 = temp;
 						}
 						boolean direction = true; //right
 						if(x1 > x2)
 							direction = false; // left
 						accepted = addBoatDiagonal(x1, y1, direction);
 					}
 
 				} while (!accepted);
 				boatSerial++;
 			}
 		}
 	}
 
 
 }
 
