 /**
  *
  */
 package datamodel;
 
 import java.util.Observable;
 import java.lang.Math;
 /**
  * one single GridElement<br>
  * provides information over its state
  *
  * @author Jakob Karolus, Kevin Munk
  * @version 1.0
  *
  */
 public class GridElement extends Observable {
 
 	public static final int INFINITE = Integer.MAX_VALUE;
 	private GridElementState state;
 	private GridElementAlgoState algoState;
 	private int row;
 	private int column;
 	private int distance;
 	private int lin_distance;
 	private GridElement path;
 
 	protected static GridElement currentEnd;
 	protected static GridElement currentStart;
 
 	/**
 	 * constructor<br>
 	 * sets state to GridElementState.FREE and the distance to -1
 	 *
 	 * @param row
 	 * @param column
 	 */
 	public GridElement(int row, int column) {
 		this.row = row;
 		this.column = column;
 		this.setState(GridElementState.FREE);
 		this.algoState = GridElementAlgoState.NONE;
 		this.distance = -1;
 	}
 
 	/**
 	 * constructor<br>
 	 * sets the distance to -1
 	 *
 	 * @param state
 	 *            the state to set
 	 * @param row
 	 * @param column
 	 */
 	public GridElement(int row, int column, GridElementState state) {
 		this(row, column);
 		this.setState(state);
 		this.algoState = GridElementAlgoState.NONE;
 
 	}
 
 	/**
 	 * @param state
 	 *            the state to set
 	 */
 	public void setState(GridElementState state) {
 
 		if (state == GridElementState.START) {
 			if (currentStart != null)
 				currentStart.setState(GridElementState.FREE);
 			currentStart = this;
 		} else if (state == GridElementState.END) {
 			if (currentEnd != null)
 				currentEnd.setState(GridElementState.FREE);
 			currentEnd = this;
 		}
 		this.state = state;
 
 
 		this.setChanged();
 		this.notifyObservers(new UpdateEvent(UpdateEvent.ELEMENT_CHANGED,
 				this.row, this.column, state, null));
 	}
 
 	/**
 	 * @return the state
 	 */
 	public GridElementState getState() {
 		return state;
 	}
 
 	/**
 	 * @return the row
 	 */
 	public int getRow() {
 		return row;
 	}
 
 	/**
 	 * @return the column
 	 */
 	public int getColumn() {
 		return column;
 	}
 
 	/**
 	 * @param distance
 	 *            the distance to set
 	 */
 	public void setDistance(int distance) {
 		this.distance = distance;
 
 		//this.setAlgoState(GridElementAlgoState.LOOKED_AT);
 	}
 
 	/**
 	 * @return the distance
 	 */
 	public int getDistance() {
 		return distance;
 	}
 
 	/**
 	 * @return the linear distance (Luftlinie) to the currentEnd
 	 */
 	public int getLinearDistance() {
 		int rowDiff = 0;
 		if (currentEnd.getRow() <= row){
 			rowDiff = currentEnd.getRow() - row;
 		} else {
 			rowDiff = row - currentEnd.getRow();
 		}
 		int colDiff = 0;
 		if (currentEnd.getColumn() <= column){
 			colDiff = currentEnd.getColumn() - column;
 		} else {
 			colDiff = column - currentEnd.getColumn();
 		}
 		lin_distance = (int) Math.sqrt((Math.pow(rowDiff, 2) + Math.pow(colDiff, 2)));
 		return lin_distance;
 		//throw new UnsupportedOperationException("Implement me!");
 	}
 
 
 	/**
 	 * get the weight of an element
 	 *
 	 * @return 4,  free
 	 *         8,  swamp
 	 *         10, mountain
 	 *         INFINITE, blocked
 	 */
 	public int getWeight() {
 		if (getState() == GridElementState.FREE){
 			return 4;
 		}
 		if (getState() == GridElementState.SWAMP){
 			return 8;
 		}
 		if (getState() == GridElementState.MOUNTAIN){
 			return 10;
 		}
 		if (getState() == GridElementState.BLOCKED){
 			return INFINITE;
 		}
		if (getState() == GridElementState.END)
			return 4;
		if (getState() == GridElementState.START)
			return 4;
		throw new IllegalArgumentException("Unknown State");
 	}
 
 	/**
 	 * @param algoState
 	 *            the algoState to set
 	 */
 	public void setAlgoState(GridElementAlgoState algoState) {
 		this.algoState = algoState;
 
 		this.setChanged();
 		this.notifyObservers(new UpdateEvent(UpdateEvent.ELEMENT_CHANGED,
 				this.row, this.column, null, algoState));
 	}
 
 
 	public void setPath(GridElement path) {
 		this.path = path;
 	}
 
 	public GridElement getPath() {
 		return path;
 	}
 
 
 	/**
 	 * @return the algoState
 	 */
 	public GridElementAlgoState getAlgoState() {
 		return algoState;
 	}
 
 	/**
 	 * Resets the element. sets distance to -1, the algo state to NONE and the
 	 * bestNeighbor to null.
 	 */
 	public void resetElement() {
 		this.setDistance(-1);
 		this.setAlgoState(GridElementAlgoState.NONE);
 	}
 
 	/**
 	 * resets the class variables
 	 */
 	public static void resetStartEnd(){
 		GridElement.currentStart = null;
 		GridElement.currentEnd = null;
 	}
 }
