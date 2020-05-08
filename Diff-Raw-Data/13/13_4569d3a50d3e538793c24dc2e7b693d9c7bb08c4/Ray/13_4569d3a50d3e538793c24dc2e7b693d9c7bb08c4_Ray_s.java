 package game;
 
 import java.awt.Color;
 
 import misc.Direction;
 import misc.Position;
 import tiles.DrawableLayer;
 
 /**
  * Class that models a ray in the game
  */
public class Ray implements DrawableLayer, Cloneable {
 
 	private Board board;
 	private Position position;
 	private Direction direction;
 	private Color color;
 
 	/**
 	 * Creates a new ray
 	 * 
 	 * @param board
 	 * @param position
 	 * @param direction
 	 * @param color
 	 */
 	public Ray(Board board, Position position, Direction direction, Color color) {
 		this.board = board;
 		this.position = position;
 		this.direction = direction;
 		this.color = color;
 	}
 
 	/**
 	 * Moves the ray in a new direction
 	 * 
 	 * @param direction
 	 */
 	public void move(Direction direction) {
 		this.direction = direction;
 		move();
 	}
 
 	/**
 	 * Moves the ray in its direction
 	 */
 	public void move() {
 		position = position.move(direction);
 		if (board.validPosition(position)) {
 			hit();
 		}
 	}
 
 	/**
 	 * Bifurcates the ray in two directions
 	 * 
 	 * @param d1
 	 * @param d2
 	 */
 	public void bifurcate(Direction d1, Direction d2) {
 		if (d1 == null || d2 == null) {
 			throw new IllegalArgumentException();
 		}
 		new Ray(board, position, d2, color).move();
 		move(d1);
 	}
 
 	/**
 	 * Hit the tile at the current position
 	 */
 	private void hit() {
 		board.getTile(position).hit(this);
 	}
 
 	/**
 	 * Returns the ray color
 	 */
 	public Color getColor() {
 		return color;
 	}
 
 	/**
 	 * Sets the ray color
 	 * 
 	 * @param color
 	 */
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	/**
 	 * Returns the ray direction
 	 */
 	public Direction getDirection() {
 		return direction;
 	}
 
 	/**
 	 * Sets the ray direction
 	 * 
 	 * @param direction
 	 */
 	public void setDirection(Direction direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * Clones the ray
 	 */
 	public Ray clone() {
 		return new Ray(board, position, direction, color);
 	}
 
 	/**
 	 * Returns the string representation of a ray
 	 */
 	@Override
 	public String toString() {
 		return "Ray [position=" + position + ", direction=" + direction
 				+ ", color=" + color + "]";
 	}
 
 }
