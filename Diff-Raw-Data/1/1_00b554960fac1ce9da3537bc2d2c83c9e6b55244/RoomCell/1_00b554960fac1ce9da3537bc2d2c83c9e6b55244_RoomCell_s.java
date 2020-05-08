 package CluedoGame.Board;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import CluedoGame.*;
 
 
 /**
  * This class represents a Cell that is a room.  It has a position that is it's central square, 
  * and may be connected to another room via a secret passage.
  * 
  * It 
  * 
  * @author Izzi
  *
  */
 public class RoomCell extends Cell {
 	Room room;
 	RoomCell secretPassage;
 	List<CorridorCell> entrances;
 	boolean isFinalRoom;
 	
 	Point midPoint;
 
 	/**
 	 * Constructor for a new RoomCell.  
 	 * 
 	 * @param room - the room it represents
 	 * @param position - point (col, row) that represents the middle square of the room.
 	 * @param isFinalRoom - if the room is the final room for announcing the murder.
 	 */
 	public RoomCell(Room room, Point position, boolean isFinalRoom) {
 		super(position);
 		this.room = room;	
 		this.entrances = new ArrayList<CorridorCell>();
 	}
 	
 	
 	//===============================================================
 	// Required for secret passage functionality
 	//===============================================================
 	
 	/**
 	 * Sets the given room as connected to this room via a secret passage
 	 * @param room - the room at the other end of the secret passage
 	 */
 	public void setSecretPassage(RoomCell room){
 		this.secretPassage = room;
 	}
 	
 	/**
 	 * Returns the Cell at the end of the secret passage.
 	 * @return - null if there isn't one.
 	 */
 	public Cell getSecretPassageDest(){
 		return this.secretPassage;
 	}
 	
 	
 	/**
 	 * This method is needed for finding paths.  If the start and destination are rooms 
 	 * with more than one entrance, then need to check all combinations, so accessing via 
 	 * index is necessary.
 	 * @return - the rooms entrances
 	 */
 	public List<CorridorCell> getEntrances(){
 		return this.entrances;	
 	}
 	
 	
 	/**
 	 * Adds the given cell as a neighbour.  It won't add itself or null as a neighbour.
 	 * This method also adds the Cell to the room's list of entrances (providing it is a CorridorCell).
 	 * 
 	 * @param the cell to be added as the current Cell's neighbour.
 	 * @throws IllegalArgumentException if neighbour is null, or itself.
 	 */
 	@Override
 	public void connectTo(Cell neighbour) {
 		// check the parameters
 		if (neighbour == this)
 			throw new IllegalArgumentException("A cell cannot be it's own neighbour.");
 		if (neighbour == null)
 			throw new IllegalArgumentException("A cell cannot have null as a neighbour.");
 		
 		// add to set of neighbours
 		neighbours.add(neighbour);
 		
 		// add to set of entrances
 		if (neighbour instanceof CorridorCell){
 			entrances.add((CorridorCell) neighbour);
 		}
 	}
 	
 	
 	//===============================================================
 	// Required for Square interface
 	//===============================================================
 	
 	/**
 	 * Returns the room this cell corresponds to.
 	 * @return
 	 */
 	public Room getRoom(){
 		return this.room;
 	}
 	
 	/**
 	 * This method does nothing - currently the game doesn't care if a room is empty or not.
 	 */
 	@Override
 	public void setBlocked(boolean isEmpty) {
 		return;
 	}
 	
 	/**
 	 * Always returns true.
 	 */
 	@Override
 	public boolean isRoom() {
 		return true;
 	}
 	
 	/**
 	 * Returns true if the RoomCell is connected to another RoomCell via a secret passage
 	 * @return
 	 */
 	public boolean isCornerRoom(){
 		return this.secretPassage != null;
 	}
 	
 	/**
 	 * Always returns false.
 	 */
 	@Override
 	public boolean isCorridor() {
 		return false;
 	}
 	
 	/**
 	 * Always returns false.
 	 */
 	@Override
 	public boolean isIntrigueSquare() {
 		return false;
 	}
 	
 	@Override
 	public boolean isFinalRoom() {
 		return this.isFinalRoom;
 	}
 	
 	/**
 	 * Always returns false.
 	 */
 	@Override
 	public boolean isBlocked() {
 		return false;
 	}
 
 	/**
 	 * Prints the name of the room.
 	 */
 	@Override
 	public String toString(){
 		return this.room.toString();
 	}
 
 
 
 
 }
