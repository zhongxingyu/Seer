 package factory.graphics;
 
 import java.awt.*;
 import javax.swing.*;
 
 /**
  * @author Tobias Lee <p>
  * <b>{@code GraphicKittingRobot.java}</b> (113x115)<br>
  * The graphical representation of a Kit Robot<br>
  * Can move between the Kit Station and the Conveyor Belt and hold a Kit
  */
 
 public class GraphicKittingRobot {
 
 	/**The x coordinate of the Kit Robot*/
 	private int x;
 	/**The y coordinate of the Kit Robot*/
 	private int y;
 	/**The direction the Kit Robot is facing with 8, 6, 2, 4 being North, East, South, and West respectively*/
 	private int direction;
 	/**The original x coordinate of the Kit Robot*/
 	private int startX;
 	/**The original y coordinate of the Kit Robot*/
 	private int startY;
 	/**The x coordinate of the Conveyor Belt*/
 	private int beltX;
 	/**The y coordinate of the Conveyor Belt*/
 	private int beltY;
 	/**The x coordinate of the Kit Station*/
 	private int stationX;
 	/**The y coordinate of the Kit Station*/
 	private int stationY;
 	/**The Kit the Kit Robot is Holding*/
 	private GraphicKit kit;
 	/**The different directional images to display for the Kit Robot*/
 	ImageIcon robot[];
 	/**The GraphicPanel for intercomponent communication*/
 	GraphicPanel GP;
 	/**The Conveyor Belt to interact with*/
 	GraphicConveyorBelt belt;
 	/**The Kit Station to interact with*/
 	GraphicKittingStation station;
 
 	/**Booleans to determine what pathing the Kit Robot is taking*/
 	private boolean fromBelt, toStation, toBelt, toCheck, checkKit, fromCheck, purgeInspectionKit, toDump, purgeKit;
 	/**The slot in the Kit Station the Kit Robot is going to*/
 	private int stationTarget;
 	
 	/**
 	 * Creates a Kit Robot at the given x and y coordinates
 	 * @param GP The GraphicPanel for intercomponent communication
 	 * @param x The initial x coordinate of the Kit Robot
 	 * @param y The initial y coordinate of the Kit Robot
 	 */
 	public GraphicKittingRobot(GraphicPanel GP, int x, int y) {
 		//Constructor
 		this.GP = GP;
 		this.belt = GP.getBelt();
 		this.station = GP.getStation();
 		this.x = x;
 		this.y = y;
 		startX = x;
 		startY = y;
 		beltX = belt.getX()-belt.getX()%5;
 		beltY = belt.getY()-belt.getY()%5;
 		stationX = station.getX()-station.getX()%5;
 		stationY = station.getY()-station.getY()%5;
 		direction = 2;
 		kit = null;
 		robot = new ImageIcon[9];
 		for (int i = 1; i < 9; i++)
 			robot[i] = new ImageIcon("Images/Robot"+i+".gif");
 		
 		fromBelt = false;
 		toStation = false;
 		toBelt = false;
 		toCheck = false;
 		checkKit = false;
 		fromCheck = false;
 		purgeInspectionKit = false;
 		purgeKit = false;
 		toDump = false;
 		stationTarget = 0;
 	}
 	
 	/**
 	 * Paints the Kit Robot
 	 * @param g The specified graphics window
 	 */
 	public void paint(Graphics g) {
 		g.drawImage(robot[direction].getImage(), x, y, null);
 		drawKit(g);
 	}
 	
 	/**
 	 * Paints the Kit the Kit Robot is holding
 	 * @param g The specified graphics window
 	 */
 	public void drawKit(Graphics g) {
 		if (!kitted())
 			return;
 		
 		switch (direction) {
 		case 2:
 			kit.setX(x+16);
 			kit.setY(y+74);
 			break;
 		case 4:
 			kit.setX(x);
 			kit.setY(y+16);
 			break;
 		case 6:
 			kit.setX(x+74);
 			kit.setY(y+16);
 			break;
 		case 8:
 			kit.setX(x+16);
 			kit.setY(y);
 			break;
 		}
 		
 		kit.setDirection(direction);
 		kit.paint(g);
 		
 	}
 	
 	/**
 	 * Checks if the Kit Robot currently has a Kit or not
 	 * @return {@code true} if the Kit Robot currently has a Kit; {@code false} if it doesn't
 	 */
 	public boolean kitted() {
 		return kit != null;
 	}
 	
 	/**
 	 * Sets the Kit the Kit Robot is holding
 	 * @param k The Kit the Kit Robot will hold
 	 */
 	public void setKit(GraphicKit k) {
 		kit = k;
 	}
 	
 	/**
 	 * Removes the Kit the Kit Robot is holding
 	 * @return The Kit the Kit Robot was holding
 	 */
 	public GraphicKit unkit() {
 		GraphicKit temp = kit;
 		kit = null;
 		return temp;
 	}
 	
 	/**
 	 * Moves the Kit Robot to the given x and y coordinates
 	 * @param tX The final x position
 	 * @param tY The final y position
 	 * @param v The speed at which the Kit Robot moves
 	 * @return {@code true} when the Kit Robot has reached its destination; {@code false} otherwise
 	 */
 	public boolean moveTo(int tX, int tY, int v) {
 		if (x != startX && y != tY)
 			moveToStartX(v);
 		else if (y > tY)
 			moveY(-v);
 		else if (y < tY)
 			moveY(v);
 		else if (x > tX)
 			moveX(-v);
 		else if (x < tX)
 			moveX(v);
 		else
 			return true;
 		return false;
 	}
 	
 	//Movement patterns (To/From indicate whether a kit is arriving FROM or moving TO the next location)
 	
 	/**
 	 * Moves back to the Kit Robot's original horizontal position
 	 * @param v The speed at which the Kit Robot moves
 	 */
 	public void moveToStartX(int v) {
 		if (x > startX)
 			moveX(-v);
 		else if (x < startX)
 			moveX(v);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the Conveyor Belt
 	 * @param v The speed at which the Kit Robot moves
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveFromBelt(int v) {
 		return moveTo(beltX+5, beltY+290, v);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the Kit Station at the {@code target} slot
 	 * @param v The speed at which the Kit Robot moves
 	 * @param target The targeted slot to move to
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveFromStation(int v, int target) {
 		return moveToStation(v, target);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the Kit Station at the {@code target} slot
 	 * @param v The speed at which the Kit Robot moves
 	 * @param target The targeted slot to move to
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveToStation(int v, int target) {
 		if (target != 0 && target != 1)
 			return true;
 		if (target == 0)
 			return moveToStation1(v);
 		return moveToStation2(v);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the first slot of the Kit Station
 	 * @param v The speed at which the Kit Robot moves
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveToStation1(int v) {
		return moveTo(stationX-70, stationY, v);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the second slot of the Kit Station
 	 * @param v The speed at which the Kit Robot moves
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveToStation2(int v) {
 		return moveTo(stationX-70, stationY+95, v);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the Inspection Station of the Kit Station
 	 * @param v The speed at which the Kit Robot moves
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveToCheck(int v) {
		return moveTo(stationX-70, stationY+190, v);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the Dump Station of the Kit Station
 	 * @param v The speed at which the Kit Robot moves
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveToTrash(int v) {
		return moveTo(stationX-70, stationY+295, v);
 	}
 	
 	/**
 	 * Moves the Kit Robot to the Conveyor Belt
 	 * @param v The speed at which the Kit Robot moves
 	 * @return {@code true} when the Kit Robot has arrived; {@code false} otherwise
 	 */
 	public boolean moveToBelt(int v) {
 		return moveFromBelt(v);
 	}
 	
 	/**
 	 * Moves the Kit Robot based on its control booleans
 	 * @param v The speed at which the Kit Robot moves
 	 */
 	public void moveRobot(int v) {
 		
 		//From Conveyor Belt to Kit Station
 		if (fromBelt) {
 			if (moveFromBelt(v)) {
 				fromBelt = false;
 				setKit(belt.unKitIn());
 				toStation = true;
 			}
 		}
 		else if (toStation) {
 			if (moveToStation(v, stationTarget)) {
 				toStation = false;
 				station.addKit(unkit(), stationTarget);
 				GP.moveEmptyKitToSlotDone();
 			}
 		}
 		
 		//From Kit Station to Inspection Station
 		else if (checkKit) {
 			if (moveFromStation(v, stationTarget)) {
 				checkKit = false;
 				setKit(station.popKit(stationTarget));
 				toCheck = true;
 			}
 		}
 		else if (toCheck) {
 			if (moveToCheck(v)) {
 				toCheck = false;
 				station.addCheck(unkit());
 				GP.moveKitToInspectionDone();
 			}
 		}
 		
 		//From Inspection Station to Conveyor Belt
 		else if (fromCheck) {
 			if (moveToCheck(v)) {
 				fromCheck = false;
 				setKit(station.popCheck());
 				toBelt = true;
 			}
 		}
 		else if (toBelt) {
 			if (moveToBelt(v)) {
 				toBelt = false;
 				belt.outKit(unkit());
 				GP.moveKitFromInspectionToConveyorDone();
 			}
 		}
 		
 		//From Kit Station to Dump Station
 		else if (purgeKit) {
 			if (moveToStation(v, stationTarget)) {
 				purgeKit = false;
 				setKit(station.popKit(stationTarget));
 				toDump = true;
 			}
 		}
 		
 		//From Inspection Station to Dump Station
 		else if (purgeInspectionKit) {
 			if (moveToCheck(v)) {
 				purgeInspectionKit = false;
 				setKit(station.popCheck());
 				toDump = true;
 			}
 		}
 		else if (toDump) {
 			if (moveToTrash(v)) {
 				toDump = false;
 				unkit();
 				GP.dumpKitAtInspectionDone();
 			}
 		}
 		
 		//Returns to original horizontal position
//		else
//			moveToStartX(v);
 	}
 	
 	//The following are all getters and setters
 	
 	/**
 	 * Moves the Kit Robot horizontally
 	 * @param v The horizontal distance to move
 	 */
 	public void moveX(int v) {
 		x += v;
 		direction = v > 0 ? 6 : 4;
 		if (kitted())
 			kit.setDirection(direction);
 	}
 	/**
 	 * Moves the Kit Robot vertically
 	 * @param v The vertical distance to move
 	 */
 	public void moveY(int v) {
 		y += v;
 		direction = v > 0 ? 2 : 8;
 		if (kitted())
 			kit.setDirection(direction);
 	}
 	/**
 	 * Moves the Kit Robot to the given coordinates
 	 * @param x The x coordinate to move to
 	 * @param y The y coordinate to move to
 	 */
 	public void moveTo(int x, int y) {
 		setX(x);
 		setY(y);
 	}
 	/**
 	 * Gets the x coordinate of the Kit Robot
 	 * @return The x coordinate of the Kit Robot
 	 */
 	public int getX() {
 		return x;
 	}
 	/**
 	 * Sets the x coordinate of the Kit Robot
 	 * @param x The x coordinate of the Kit Robot
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 	/**
 	 * Gets the y coordinate of the Kit Robot
 	 * @return The y coordinate of the Kit Robot
 	 */
 	public int getY() {
 		return y;
 	}
 	/**
 	 * Sets the y coordinate of the Kit Robot
 	 * @param y The y coordinate of the Kit Robot
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 	/**
 	 * Sets the Conveyor Belt's coordinates
 	 * @param bx The Conveyor Belt's x coordinate
 	 * @param by The Conveyor Belt's y coordinate
 	 */
 	public void setBelt(int bx, int by) {
 		beltX = bx;
 		beltY = by;
 	}
 	/**
 	 * Gets the Conveyor Belt's x coordinate
 	 * @return The Conveyor Belt's x coordinate
 	 */
 	public int getBeltX() {
 		return beltX;
 	}
 	/**
 	 * Sets the Conveyor Belt's x coordinate
 	 * @param beltX The Conveyor Belt's x coordinate
 	 */
 	public void setBeltX(int beltX) {
 		this.beltX = beltX;
 	}
 	/**
 	 * Gets the Conveyor Belt's y coordinate
 	 * @return The Conveyor Belt's y coordinate
 	 */
 	public int getBeltY() {
 		return beltY;
 	}
 	/**
 	 * Sets the Conveyor Belt's y coordinate
 	 * @param beltY The Conveyor Belt's y coordinate
 	 */
 	public void setBeltY(int beltY) {
 		this.beltY = beltY;
 	}
 	/**
 	 * Sets the Kit Station's coordinates
 	 * @param sx The Kit Station's x coordinate
 	 * @param sy The Kit Station's y coordinate
 	 */
 	public void setStation(int sx, int sy) {
 		stationX = sx;
 		stationY = sy;
 	}
 	/**
 	 * Gets the Kit Station's x coordinate
 	 * @return The Kit Station's x coordinate
 	 */
 	public int getStationX() {
 		return stationX;
 	}
 	/**
 	 * Sets the Kit Station's x coordinate
 	 * @param stationX The Kit Station's x coordinate
 	 */
 	public void setStationX(int stationX) {
 		this.stationX = stationX;
 	}
 	/**
 	 * Gets the Kit Station's y coordinate
 	 * @return The Kit Station's y coordinate
 	 */
 	public int getStationY() {
 		return stationY;
 	}
 	/**
 	 * Sets the Kit Station's y coordinate
 	 * @param stationY The Kit Station's y coordinate
 	 */
 	public void setStationY(int stationY) {
 		this.stationY = stationY;
 	}
 	/**
 	 * Gets the Kit Robot's direction
 	 * @return The Kit Robot's direction
 	 */
 	public int getDirection() {
 		return direction;
 	}
 	/**
 	 * Checks if the Kit Robot is going to the Conveyor Belt
 	 * @return {@code true} if the Kit Robot is going to the Conveyor Belt; {@code false} otherwise
 	 */
 	public boolean getFromBelt() {
 		return fromBelt;
 	}
 	/**
 	 * Sets if the Kit Robot is going to the Conveyor Belt
 	 * @param fromBelt If the Kit Robot is going to the Conveyor Belt
 	 */
 	public void setFromBelt(boolean fromBelt) {
 		this.fromBelt = fromBelt;
 	}
 	/**
 	 * Checks if the Kit Robot is going to the Inspection Station
 	 * @return {@code true} if the Kit Robot is going to the Inspection Station; {@code false} otherwise
 	 */
 	public boolean getFromCheck() {
 		return fromCheck;
 	}
 	/**
 	 * Sets if the Kit Robot is going to the Inspection Station
 	 * @param fromBelt If the Kit Robot is going to the Inspection Station
 	 */
 	public void setFromCheck(boolean fromCheck) {
 		this.fromCheck = fromCheck;
 	}
 	/**
 	 * Checks if the Kit Robot is going to the Inspection Station
 	 * @return {@code true} if the Kit Robot is going to the Inspection Station; {@code false} otherwise
 	 */
 	public boolean getCheckKit() {
 		return checkKit;
 	}
 	/**
 	 * Sets if the Kit Robot is going to the Inspection Station
 	 * @param fromBelt If the Kit Robot is going to the Inspection Station
 	 */
 	public void setCheckKit(boolean checkKit) {
 		this.checkKit = checkKit;
 	}
 	public void setPurgeKit(boolean purgeKit) {
 		this.purgeKit = purgeKit;
 	}
 	public boolean getPurgeKit() {
 		return purgeKit;
 	}
 	/**
 	 * Checks if the Kit Robot is going to the Dump Station
 	 * @return {@code true} if the Kit Robot is going to the Dump Station; {@code false} otherwise
 	 */
 	public boolean getpurgeInspectionKit() {
 		return purgeInspectionKit;
 	}
 	/**
 	 * Sets if the Kit Robot is going to the Dump Station
 	 * @param fromBelt If the Kit Robot is going to the Dump Station
 	 */
 	public void setPurgeInspectionKit(boolean purgeInspectionKit) {
 		this.purgeInspectionKit = purgeInspectionKit;
 	}
 	/**
 	 * Gets the slot in the Kit Station the Kit Robot is going to
 	 * @return The slot in the Kit Station the Kit Robot is going to
 	 */
 	public int getStationTarget() {
 		return stationTarget;
 	}
 	/**
 	 * Sets the slot in the Kit Station the Kit Robot is going to
 	 * @param stationTarget The slot in the Kit Station the Kit Robot is going to
 	 */
 	public void setStationTarget(int stationTarget) {
 		this.stationTarget = stationTarget;
 	}
 	
 }
