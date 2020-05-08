 package de.atlassoft.model;
 
 import de.hohenheim.view.path.CenterAnchor;
 import de.hohenheim.view.path.PathFigure;
 //TODO: Noch unklar ob State notwendig
 /**
  * This class defines a path from one node to another.
  * 
  * @author Alexander Balogh
  */
 public class Path {
 	
 	private PathFigure pathFigure;
 	private double topSpeed;
 	private State state;
 	private Node start;
 	private Node end;
 	
 	
 	
 	/**
 	 * The constructor for the Path.
 	 * 
 	 * @param start
 	 *            The start node of the path
 	 * @param end
 	 *            The end node of the path
 	 * @param topSpeed
 	 *            The top speed that is allowed while driving on this path in
 	 *            km/h
 	 * @throws IllegalArgumentException
 	 *             if start or end is null or topSpeed is a negative number
 	 */
 	public Path(Node start, Node end, double topSpeed) {
 		// check constraints
 		if (start == null || end == null) {
 			throw new IllegalArgumentException("start and end must not be null");
 		}
 		if (topSpeed < 0) {
 			throw new IllegalArgumentException(
 					"topSpeed must not be a negative number");
 		}
 
 		// set properties
 		this.topSpeed = topSpeed;
 		state = new State();
 		state.setState(State.UNBLOCKED);
 		this.start = start;
 		this.end = end;
 		PathFigure path = new PathFigure(this);
 		path.setSourceAnchor(new CenterAnchor(start.getNodeFigure())); // new ChopboxAnchor(start)
 		path.setTargetAnchor(new CenterAnchor(end.getNodeFigure()));// new ChopboxAnchor(end)
		this.pathFigure = path;
 	}
 	
 	
 	/**
 	 * Returns the top speed that is allowed in this path.
 	 * 
 	 * @return The top speed.
 	 */
 	public double getTopSpeed() {
 		return topSpeed;
 	}
 
 	/**
 	 * Sets the top speed that is allowed in this path. If topSpeed is a
 	 * negative number, an {@link IllegalArgumentException} is thrown.
 	 * 
 	 * @param topSpeed
 	 *            The top speed to be set
 	 * @throws IllegalArgumentException
 	 *             if topSpeed is a negative number
 	 */
 	public void setTopSpeed(double topSpeed) {
 		if (topSpeed < 0) {
 			throw new IllegalArgumentException(
 					"topSpeed must not be a negative number");
 		}
 		this.topSpeed = topSpeed;
 	}
 	
 	/**
 	 * Returns the path figure of the path.
 	 * 
 	 * @return the path figure
 	 */
 	public PathFigure getPathFigure() {
 		return pathFigure;
 	}
 
 	/**
 	 * Returns the {@link State} in order to see whether the path is blocked or
 	 * not blocked.
 	 * 
 	 * @return the state object
 	 */
 	public State getState() {
 		return state;
 	}
 	
 	/**
 	 * Returns the start {@link Node} of this path.
 	 * 
 	 * @return the start node
 	 */
 	public Node getStart() {
 		return start;
 	}
 
 	/**
 	 * Returns the end {@link Node} of this path.
 	 * 
 	 * @return the end node
 	 */
 	public Node getEnd() {
 		return end;
 	}
 	
 }
