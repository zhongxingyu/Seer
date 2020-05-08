 package kth.csc.inda.ads.snake;
 
 import java.awt.Color;
 import java.util.LinkedList;
 
 import kth.csc.inda.ads.Location;
 import kth.csc.inda.ads.NField;
 import kth.csc.inda.ads.TwoControls;
 
 public class NSnake {
 	// Name of the snake.
 	private String name;
 	// Color of the snake.
 	private Color color;
 	// LinkedList of the snake-segments.
 	private LinkedList<NSnakeSeg> body = new LinkedList<NSnakeSeg>();
 	// Max length of the snake, if <= 0 then endless snake size.
 	private int maxLength;
 	// Current direction of the snake.
 	private Direction direction;
 	// Boolean field to see if it is turning.
 	private boolean turning;
 	// If the snake is dead.
 	private boolean isAlive;
 	// Field where the snake is placed in.
 	private NField field;
 	// The cause of death if there is any.
 	private String causeOfDeath;
 	// Controls to steer the snake with.
 	private TwoControls controls;
 
 	public NSnake(String name, Color color, NField field, Location startLoc,
 			Direction direction, TwoControls controls, int maxLength) {
 		this.name = name;
 		this.color = color;
 		this.field = field;
 		this.controls = controls;
 		this.maxLength = maxLength;
 		this.direction = direction;
 		this.isAlive = true;
 
 		// Add the starting segment of the snake.
 		body.addFirst(new NSnakeSegHead(this, startLoc));
 	}
 
 	/**
 	 * @return The color of the snake.
 	 */
 	public Color getColor() {
 		return color;
 	}
 
 	/**
 	 * Sets the color of the snake.
 	 * 
 	 * @param color
 	 */
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	public TwoControls getControls() {
 		return controls;
 	}
 
 	/**
 	 * @return Current length of the snake.
 	 */
 	public int getMaxLength() {
 		return maxLength;
 	}
 
 	/**
 	 * @return Name of the snake.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the maximum allowed length of the snake.
 	 * 
 	 * @param maxLength
 	 *            The maximum allowed length of the snake.
 	 */
 	public void setMaxLength(int maxLength) {
 		this.maxLength = maxLength;
 	}
 	
 	/**
 	 * Change direction to the left.
 	 */
 	public boolean turnRight() {
 		if (!turning) {
 			switch (direction) {
 			case UP:
 				direction = Direction.RIGHT;
 				break;
 			case LEFT:
 				direction = Direction.UP;
 				break;
 			case DOWN:
 				direction = Direction.LEFT;
 				break;
 			case RIGHT:
 				direction = Direction.DOWN;
 				break;
 			}
 			turning = true;
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Change direction to the right.
 	 */
 	public boolean turnLeft() {
 		if (!turning) {
 			switch (direction) {
 			case UP:
 				direction = Direction.LEFT;
 				break;
 			case LEFT:
 				direction = Direction.DOWN;
 				break;
 			case DOWN:
 				direction = Direction.RIGHT;
 				break;
 			case RIGHT:
 				direction = Direction.UP;
 				break;
 			}
 			turning = true;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 
 	/**
 	 * Move the snake to the location provided.
 	 */
 	public void move(Location newLoc, boolean underground) {
 		int col = newLoc.getCol();
 		int row = newLoc.getRow();
 
 		// Checks for if the next move is within the field.
 		if (col < 0 || col >= field.getDepth() || row < 0
 				|| row >= field.getWidth()) {
 			kill("crashed into a wall");
 			return;
 		}
 
 		// Add the new location to snakes body.
 		addFirst(newLoc, underground);
 
 		while (body.size() > maxLength) {
 			// Remove the corresponding segment and field if the snake is to
 			// long.
 			removeLast();
 		}
 	}
 
 	/**
 	 * Returns the location that the snake's head will move to.
 	 * 
 	 * @return the location the snake's head will move to
 	 */
 	public Location getNextMove() {
 		Location loc = body.getFirst().getLoc();
 		int row = loc.getRow();
 		int col = loc.getCol();
 		switch (direction) {
 		case RIGHT:
 			col++;
 			break;
 		case UP:
 			row--;
 			break;
 		case LEFT:
 			col--;
 			break;
 		case DOWN:
 			row++;
 			break;
 		}
 		// Reset the turning
 		turning = false;
 		
 		return new Location(row, col);
 	}
 
 	/**
 	 * Adds a new segment at the specified locations.
 	 */
 	private void addFirst(Location loc, boolean underground) {
 		// Replace the current first segment with a body instead of head.
		NSnakeSeg oldHead = body.getFirst();
		if (oldHead instanceof NSnakeSegHead) {
			body.removeFirst();
 			body.addFirst(new NSnakeSegBody(this, oldHead.getLoc()));
			field.place(body.getFirst(), body.getFirst().getLoc());
 		}
 		// Creates the new segment to add.
		NSnakeSeg newSeg;
		if (underground) {
			newSeg = new NSnakeSegEmpty(this, loc);
		} else {
			newSeg = new NSnakeSegHead(this, loc);
		}
		body.addFirst(newSeg);
		field.place(newSeg, loc);
 	}
 
 	private void removeLast() {
 		field.clear(body.removeLast().getLoc());
 	}
 
 	/**
 	 * Sets the snake to be dead.
 	 */
 	public void kill(String causeOfDeath) {
 		isAlive = false;
 		this.causeOfDeath = causeOfDeath;
 	}
 
 	/**
 	 * @return True if alive, otherwise false.
 	 */
 	public boolean isAlive() {
 		return isAlive;
 	}
 
 	/**
 	 * Returns info about the player.
 	 */
 	public String toString() {
 		if (isAlive()) {
 			return name + ", " + body.getFirst().getLoc() + " is alive.";
 		}
 		return name + " " + causeOfDeath;
 	}
 
 }
