 package de.atlassoft.model;
 
 import org.eclipse.draw2d.geometry.Rectangle;
 
 import de.hohenheim.view.node.NodeFigure;
 //TODO: Noch unklar ob State notwendig
 /**
  * This class provides a node for the railway system
  * of the application.
  *  
  * @author Alexander Balogh
  */
 public class Node {
 
 	private State state;
 	private NodeFigure nodeFigure;
 	private String name;
 	
 	
 	
 	/**
 	 * Constructor for creating a new Node.
 	 * 
 	 * @param name
 	 *            The name of the node.
 	 * @param x
 	 *            The x-coordinate in the railway system. Must not be a negative
 	 *            number.
 	 * @param y
 	 *            The y-coordinate in the railway system. Must not be a negative
 	 *            number.
 	 * @param width
 	 *            The width of the node. Must not be a negative number.
 	 * @param height
 	 *            The height of the node. Must not be a negative number.
 	 * @throws IllegalArgumentException
 	 *             if name is null or one coordinate or dimension is a negative
 	 *             number
 	 */
 	public Node(String name, int x, int y, int width, int height){
 		// check constraints
 		if (name == null || name.trim().isEmpty()) {
 			throw new IllegalArgumentException("name must not be null or empty");
 		}
 		if (x < 0 || y < 0 || width < 0 || height < 0) {
 			throw new IllegalArgumentException(
 					"coordinates and dimensions must be positive numbers");
 		}
 		
 		// set properties
 		this.name = name;
 		state = new State();
 		state.setState(State.UNBLOCKED);
 		NodeFigure nodeFigure = new NodeFigure(this);
 		nodeFigure.setName(name);    
 		nodeFigure.setBounds(new Rectangle(x, y, width, height));	
		this.nodeFigure = nodeFigure;
 	}
 	
 	
 	
 	/**
 	 * Returns the {@link NodeFigure} of this node.
 	 * 
 	 * @return the NodeFigure
 	 */
 	public NodeFigure getNodeFigure() {
 		return nodeFigure;
 	}
 
 	/**
 	 * Returns the {@link State} of this node.
 	 * 
 	 * @return the state
 	 */
 	public State getState() {
 		return state;
 	}
 
 	/**
 	 * Returns the name of this node.
 	 * 
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 	
 }
