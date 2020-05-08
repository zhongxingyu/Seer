 /*
  * Class Ship describes characteristics common to all the ships.
  *
  */
 package Battleships;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 /**
  * @author MARGARET WRIGHT
  * @author KLM
  */
 
 public abstract class Ship {
 	// TODO add appropriate comments
 	@Getter
 	private int size;
 	private String type;
 	private String shortForm;
 
 	// TODO add appropriate comments
 	@Getter
 	@Setter(AccessLevel.PACKAGE)
 	private int bowRow;
 	@Getter
 	@Setter(AccessLevel.PACKAGE)
 	private int bowColumn;
 	@Getter
 	@Setter(AccessLevel.PACKAGE)
 	private boolean horizontal;
 
 	/**
 	 * An array of boolean which indicates whether that part of the ship has
 	 * been hit. This is initialised by the appropriate sub-class. Battleships
 	 * use all 4 locations; cruisers use the first 3; destroyers 2; submarines
 	 * 1; and "empty sea" 1.
 	 */
 	protected boolean[] hit;
 	
 
 	/**
 	 * clears the hit array indicating whether that part of the "Ship" has been
 	 * hit
 	 */
 	protected Ship(int size, String type, String shortForm) {
 		this.size = size;
 		this.setType(type);
 		this.shortForm = shortForm;
 		hit = new boolean[size];
 		for (int i = 0; i < hit.length; i++)
 			hit[i] = false;
 	}
 
 	/**
 	 * Checks that ship of this size will not overlap another ship, or touch
 	 * another ship (vertically, horizontally, or diagonally) and that ship will
 	 * not "stick out" beyond the array.
 	 * 
 	 * @param row
 	 *            that will contain the bow
 	 * @param column
 	 *            that will contain the bow
 	 * @param horizontal = true if horizontal
 	 * @param ocean
 	 * @return true if it is okay to put a ship of this size with its bow in
 	 *         this location, with the given orientation.
 	 */
 	public boolean okToPlaceShipAt(int row, int column, boolean horizontal,
 			Ocean ocean) {
 		// Try to catch exception error if ship goes past board
 		try{
 			Ship ships[][] = ocean.getShipArray();
 						
 			for (int i = 0; i < getSize(); i++){
 			if (!(ships[row][column] instanceof EmptySea)){	
 			//	Can't Print here 
 					return false;
 			}else{
 				if (horizontal) {
 					column++; 
 				} else {
 					row++;	
 					}
 				}
 			}
 			return true;
 		}
 		catch(Exception err){
 		//	System.out.println("OMG an error");
 			return false;
 		} 
 
 	
 		
 	}
 
 	/**
 	 * "places" the ship in the ocean, assigning values to the bowRow,
 	 * bowColumn, and horizontal. Places a reference to the ship in the ships
 	 * array in the Ocean object.
 	 * 
 	 * @param row
 	 *            to contain the bow
 	 * @param column
 	 *            to contain the bow
 	 * @param horizontal
 	 * @param ocean
 	 */
 	public void placeShipAt(int row, int column, boolean horizontal, Ocean ocean) {
 
 		this.setBowRow(row);
 		this.setBowColumn(column);
 		this.setHorizontal(horizontal);
 
 		Ship ships[][] = ocean.getShipArray();
 
 		for (int i = 0; i < getSize(); i++) {
 			// set position in array to contain the ship
 			ships[row][column] = this;
 			if (horizontal) {
 				column++;
 			} else {
 				row++;
 			}
 		}
 	}
 
 	private int getSize() {
 		// TODO Auto-generated method stub
 		return size;
 	}
 
 	private void setHorizontal(boolean horizontal2) {
 		// TODO Auto-generated method stub
 		horizontal = horizontal2;
 	}
 
 	private void setBowColumn(int column) {
 		// TODO Auto-generated method stub
 		bowColumn = column;
 	}
 
 	private void setBowRow(int row) {
 		// TODO Auto-generated method stub
 		bowRow = row;
 	}
 
 	/**
 	 * If this ship has been hit, marks that part of the ship as "hit"
 	 * 
 	 * @param row
 	 *            User's supplied row shot
 	 * @param column
 	 *            User's supplied column shot
 	 * @return true if ship is hit, false otherwise
 	 */
 	public boolean shootAt(int row, int column) {
 		if ((isHorizontal() && (row != getBowRow()))
 				|| (!isHorizontal() && (column != getBowColumn()))){
 			return false; // it's not a hit
 		} else {
 			// it's a hit. Work out offset & set that position in hit array to true
			hit[(row - getBowRow() + column - getBowColumn())] = true;
		
 				return true;
 		}
 	}
 
 	private int getBowRow() {
 		// TODO Auto-generated method stub
 		return bowRow;
 	}
 
 	private int getBowColumn() {
 		// TODO Auto-generated method stub
 		return bowColumn;
 	}
 
 	private boolean isHorizontal() {
 		// TODO Auto-generated method stub
		return horizontal;
		
		
 	}
 
 	/**
 	 * checks whether this ship is sunk - using the hit array
 	 * 
 	 * @return true if every part of the ship has been hit, false otherwise.
 	 */
 	public boolean isSunk() {
 
	//	for (boolean c : hit)
	//		System.out.println("The Value of b is " + c);
 			
		for (boolean b : hit)
 			if (b == false)
 				return false;
			
 				return true;
 	}
 	
 	public void sinkShip(Ocean ocean) {
 		Ship ships[][] = ocean.getShipArray();
 		int brow = getBowRow(); 
 		int bcolumn = getBowColumn(); 
 		boolean bhorizontal = isHorizontal();
 		
 		for (int i = 0; i < hit.length ; i++){
 			ships[brow][bcolumn] = new HitType("$");
 			
 			if (bhorizontal) {
 				bcolumn++;
 			} else {
 				brow++;
 			}
 			
			//System.out.println("Bow Column " + getBowColumn() + " Bow Row " + getBowRow() + " Is Horizontal " + isHorizontal());
 			//row = row + i;
 			
 		}
 			
 	}
 
 	/**
 	 * @return a single character String to use in Ocean's print method
 	 */
 	@Override
 	public String toString() {
 		return shortForm;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 	public String setHit(String hit){
 		return hit;
 	}
 }
