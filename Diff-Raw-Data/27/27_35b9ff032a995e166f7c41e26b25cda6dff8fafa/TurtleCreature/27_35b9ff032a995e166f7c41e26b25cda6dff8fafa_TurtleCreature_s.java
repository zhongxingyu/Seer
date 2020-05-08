 package se.chalmers.kangaroo.model.creatures;
 
 import java.awt.Polygon;
 
 import se.chalmers.kangaroo.model.utils.Direction;
 import se.chalmers.kangaroo.model.utils.Position;
 
 /**
  * An enemy in the form of a turtle. Will go into its shell at random times and
  * will then be impossible to kill.
  * 
  * @author arvidk
  * 
  */
 public class TurtleCreature implements Creature {
 
 	private static final int id = 112;
 	private Position currentPos;
 	private Position startPos;
 	private Direction currentDir;
 	private int speed = 1;
	private boolean inShell = false;
 
 	public TurtleCreature(Position spawnPos) {
 		startPos = spawnPos;
 		currentPos = spawnPos;
 		currentDir = Direction.DIRECTION_WEST;
 	}
 
 	@Override
 	public void updateCreature() {
 		int i = (int)(Math.random() * 600);
 		if (i >= 598)
 			changeState();
 		if(!inShell)
 			move();
 	}
 
 	/**
 	 * A class that change the state of the turtle. The turtle will be in its
 	 * shell for a random amount of time between 1 and 5 seconds.
 	 */
 	public void changeState() {
 		inShell = !inShell;
 
 	}
 
 	/**
 	 * Will change if the Turtle is killable or not. If the Turtle is in its
 	 * shell, the creature shall be invurnable, and the Kangaroo will die if
 	 * they collides.
 	 */
 	@Override
 	public boolean isKillable() {
 		return !inShell;
 	}
 
 	/**
 	 * Will move the Turtle. If the turtle is in its shell, the Turtle will stay
 	 * in its place.
 	 */
 	@Override
 	public void move() {
 			if (this.currentDir == Direction.DIRECTION_EAST) {
 				this.currentPos = new Position(currentPos.getX() + speed,
 						currentPos.getY());
 			} else{
 				this.currentPos = new Position(currentPos.getX() - speed,
 						currentPos.getY());
 
 			}
 	}
 
 	@Override
 	public Polygon getPolygon() {
 		int xs[] = { currentPos.getX() + 0, currentPos.getX() + 14,
 				currentPos.getX() + 14, currentPos.getX() + 20,
 				currentPos.getX() + 20, currentPos.getX() + 44,
 				currentPos.getX() + 44, currentPos.getX() + 50,
 				currentPos.getX() + 50, currentPos.getX() + 64,
 				currentPos.getX() + 64, currentPos.getX() + 54,
 				currentPos.getX() + 54, currentPos.getX() + 10,
 				currentPos.getX() + 10, currentPos.getX() + 0 };
 		int ys[] = { currentPos.getY() + 2, currentPos.getY() + 2,
 				currentPos.getY() + 0, currentPos.getY() + 0,
 				currentPos.getY() + 6, currentPos.getY() + 6,
 				currentPos.getY() + 0, currentPos.getY() + 0,
 				currentPos.getY() + 2, currentPos.getY() + 2,
 				currentPos.getY() + 16, currentPos.getY() + 16,
 				currentPos.getY() + 32, currentPos.getY() + 32,
 				currentPos.getY() + 16, currentPos.getY() + 16 };
 		return new Polygon(xs, ys, 16);
 
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public Position getPosition() {
 		return currentPos;
 	}
 
 	/**
 	 * Makes the turtle change its direction.
 	 */
 	@Override
 	public void changeDirection() {
 		if (currentDir == Direction.DIRECTION_WEST) {
 			currentDir = Direction.DIRECTION_EAST;
 		} else {
 			currentDir = Direction.DIRECTION_WEST;
 		}
 
 	}
 
 	/**
 	 * Returns the direction of this creature.
 	 * 
 	 * @return
 	 */
 	public Direction getDirection() {
 		return currentDir;
 	}
 	
 	public void resetPosition(){
 		currentPos = startPos;
 	}
 
 }
