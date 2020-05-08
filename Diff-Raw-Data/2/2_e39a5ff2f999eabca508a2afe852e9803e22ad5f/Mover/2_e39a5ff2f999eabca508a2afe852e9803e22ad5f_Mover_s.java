 package units;
 
 import java.util.ArrayList;
 
 import application.*;
 
 public class Mover extends UnitRender {
 
 	protected Grid grid;
 	protected ArrayList<Path> paths = new ArrayList<Path>();
 	// You store the paths in the array above and then place the one you are on
 	// in the
 	// Path Class I think it is still called class
 	private Path path = null;
 	// Have to store the next move location so it moves there then updates the path
 	protected Map map;
 	// Tells the system that we have got to that way point.
 	// Can be used to create a patrol path. Don't delete a wayPointIndex
 	private int wayPointIndex = 0;
 	// The unit movement speed is a basic division
 	// You want the unit to move faster you divided by a smaller number.
 	public int movementSpeed = 0;
 	// The delay is basic when you choose to make the unit pause a second before moving. 0 = no delay 1 means pause
 	// once and then keep moving.
 	protected int delay = 0;
 	// number of times delayed
 	private int delayed = 0;
 	// This is the index number for the path
 	// If you don't start at one it will have a delay in the system before it starts moving
 	private int step = 1;
 
 
 	// this tells the unit that there is an new location you need to go two
 	public void newPath(Path path) {
 		paths.clear();
 		step = 0;
 		wayPointIndex = 0;
 		saveWayPoint(path);
 		this.path = path;
 	}
 	// Call this in the next step method and it will correct any errors that may happen do to strange movements speeds
 	private void theFixer() {
 		if (locY != nextConverstionY() || locX != nextConverstionX()){
 			locY = nextConverstionY();
 			locX = nextConverstionX();
 		}
 	}
 
 	// This tells the unit it has some were it needs to go after it get to were
 	// it is going.
 	public void saveWayPoint(Path path) {
 		paths.add(path);
 	}
 
 	private int nextConverstionX() {
 		return grid.locationX(path.getStep(step).getX());
 	}
 
 	private int nextConverstionY() {
 		return grid.locationY(path.getStep(step).getY());
 	}
 	
 	private int nextConverstionX(int step) {
 		return grid.locationX(path.getStep(step).getX());
 	}
 
 	private int nextConverstionY(int step) {
 		return grid.locationY(path.getStep(step).getY());
 	}
 
 	private void moveXY() {
 
 		// If it some how runs this method with out a path set it will try to
 		// set one for you
 		if (path == null && paths.size() >= 1) {
 			System.out.println("Move path was null trying to set");
 			path = paths.get(wayPointIndex);
 		}
 		
 		moveCloser();
 	}
 	
 	public void moveCloser(){
 		
 		int nextStepX = nextConverstionX();
 		int nextStepY = nextConverstionY();
 		
 		int distanceX = Math.abs(locX - nextStepX);
 		int distanceY = Math.abs(locY - nextStepY);
 		int speed = movementSpeed;
 		
 		if (distanceX < movementSpeed && distanceY == 0){
 			// one number is always going to be zero so you can add them to get a solution;
 			// if it some how screws up the fixer will correct any errors
 			System.out.println("Added X " + distanceX + ", " + distanceY + " AND " + movementSpeed);
 			speed = distanceX;
 		} 
 		if (distanceY < movementSpeed && distanceX == 0) {
 			System.out.println("Added Y " + distanceX + ", " + distanceY + " AND " + movementSpeed);
 			speed = distanceY;
 		}
 		
 		if (locX < nextStepX) {
 			locX += speed;
 		}
 		if (locX > nextStepX) {
 			locX -= speed;
 		}
 		if (locY < nextStepY) {
 			locY += speed;
 		}
 		if (locY > nextStepY) {
 			locY -= speed;
 		}
 	}
 	// This will make sure that the path that you are on is still clear if the next tile you are
 	// Trying to go to is blocked it will try to find a new path and then go that directions
 	private void checkPath(){
 		int nextStepX = locX;
 		int nextStepY = locY;
 		// only set the new nextConverstion location if you are not at the end of your path
		if (path.getLength() > step){
 			nextStepX = nextConverstionX(step + 1);
 			nextStepY = nextConverstionY(step + 1);
 		}
 	
 		System.out.println("The next location starts here");
 		System.out.println(locX + ", " + locY);
 		System.out.println(nextStepX + ", " + nextStepY);
 		System.out.println(grid.getTileX(locX) + ", " + grid.getTileY(locY));
 		System.out.println(grid.getTileX(nextStepX) + ", " + grid.getTileY(nextStepY));
 		System.out.println("Your end locations are " + getEndX() + ", " + getEndY());
 
 		if (map.isBlocked(grid.getTileX(nextStepX), grid.getTileY(nextStepY))){
 			System.out.println("Path Blocked Finding new Path " + map.isBlocked(grid.getTileX(nextStepX), grid.getTileY(nextStepY)));
 			
 			Pathfinder pathFinder = new Pathfinder(map);
 			
 			Path newPath = pathFinder.findPath(locX, locY, getEndX(), getEndY());
 			System.out.println("Your Path is " + newPath);
 			newPath(newPath);
 
 		}else{
 			step++;
 			if (path.getLength() == step) {
 				step = 0;
 				path = null;
 			}
 		}
 	}
 	
 	private int getEndX(){
 		return path.getStep(path.getLength() - 1).getX();
 	}
 	
 	private int getEndY(){
 		return path.getStep(path.getLength() - 1).getY();
 	}
 
 
 	private void nextStep() {
 		// If this fails then the fixer should be ran and the steps set to zero and the path set to null
 		// This will prevent the game from going into lock down mode on something silly like out of bounds errors
 		// This will also let the unit still be movable along with trying to fix the units position
 		try {
 			int nextX = nextConverstionX();
 			int nextY = nextConverstionY();
 			
 			if (locX == nextX && locY == nextY){
 					checkPath();
 					
 					// Note to self it is possible to return a null on path finding here
 					// CheckPath method will some times return a null path if there is not a valid destination
 					// result is a Tread null pointer Exception which will make the game crash.
 			}
 		} catch (ArrayIndexOutOfBoundsException e){
 			theFixer();
 			step = 0;
 			path = null;
 			System.out.println(e);
 		}
 		
 
 
 	}
 	// not in use yet still working out the smaller problems
 	private void nextWayPoint() {
 		if (path.getLength() == step) {
 			wayPointIndex++;
 			path = paths.get(wayPointIndex);
 		}
 	}
 	// This handles slowing the unit down even more. 
 	private boolean isDelayed(){
 
 		if (delay != delayed ){
 			delayed++;
 			return false;
 		} else {
 			delayed = 0;
 			return true;
 		}
 	}
 
 	public void move() {
 		if (path != null) {
 			if (isDelayed()){
 				moveXY();
 				nextStep();
 			}
 			// nextWayPoint();
 		}
 	}
 
 }
