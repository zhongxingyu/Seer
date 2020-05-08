 package game;
 
 import java.awt.Color;
 import java.util.Stack;
 
 import misc.Direction;
 import misc.Position;
 
 /**
  * Class that models a ray in the game.
  */
 public class Ray implements Beam, Cloneable {
 
 	private Color color;
 	private Direction direction;
 	private boolean stopped = true;
 
 	/**
 	 * Creates a new ray.
 	 * 
 	 * @param direction
 	 * @param color
 	 */
 	public Ray(Color color, Direction direction) {
 		this.color = color;
 		this.direction = direction;
 	}
 
 	/**
 	 * Starts ray propagation across the board using the specified position as
 	 * origin.
 	 * 
 	 * @param board
 	 * @param position
 	 */
 	public void propagate(Board board, Position position) {
 		stopped = false;
 		position = displace(board, position);
 		while (!stopped) {
 			hit(board, position);
 			position = displace(board, position);
 		}
 	}
 
 	/**
 	 * Hits the tile at the current position.
 	 */
 	private void hit(Board board, Position position) {
 		Stack<Ray> bifurcations = new Stack<Ray>();
 		board.getTile(position).hit(this, bifurcations);
 		if (!stopped) {
 			while (!bifurcations.isEmpty()) {
 				bifurcations.pop().propagate(board, position);
 			}
 		}
 	}
 
 	/**
 	 * Moves the ray in its direction.
 	 */
 	public Position displace(Board board, Position position) {
 		position = position.displace(direction);
 		if (!board.validPosition(position)) {
 			stopped = true;
 		}
 		return position;
 	}
 
 	/**
 	 * Bifurcates the ray in a new direction.
 	 * 
 	 * @param direction
 	 */
 	public Ray bifurcate(Direction direction) {
 		if (direction == null) {
 			throw new IllegalArgumentException();
 		}
 		// Prevent bifurcation of stopped rays
 		if (stopped) {
 			return null;
 		}
 		return new Ray(color, direction);
 	}
 
 	/* (non-Javadoc)
 	 * @see game.Beam#getColor()
 	 */
	@Override
 	public Color getColor() {
 		return color;
 	}
 
 	/**
 	 * Sets the ray color.
 	 * 
 	 * @param color
 	 */
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	/* (non-Javadoc)
 	 * @see game.Beam#getDirection()
 	 */
	@Override
 	public Direction getDirection() {
 		return direction;
 	}
 
 	/**
 	 * Sets the ray direction.
 	 * 
 	 * @param direction
 	 */
 	public void setDirection(Direction direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * Stops the ray propagation.
 	 */
 	public void stop() {
 		stopped = true;
 	}
 
 	/**
 	 * Clones the ray.
 	 */
 	public Ray clone() {
 		return new Ray(color, direction);
 	}
 
 	/**
 	 * Returns the string representation of a ray.
 	 */
	@Override
 	public String toString() {
 		return "Ray [direction=" + direction + ", color=" + color + "]";
 	}
 
 }
