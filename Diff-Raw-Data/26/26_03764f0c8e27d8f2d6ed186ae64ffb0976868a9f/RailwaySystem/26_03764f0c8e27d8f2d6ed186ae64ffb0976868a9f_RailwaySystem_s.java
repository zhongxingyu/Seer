 package de.atlassoft.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.hohenheim.view.map.NodeMap;
 import de.hohenheim.view.node.NodeFigure;
 import de.hohenheim.view.path.PathFigure;
 
 /**
  * Represents a railway system.
  * 
  * @author Alexander Balogh
  * 
  */
 public class RailwaySystem {
 
 	private NodeMap map;
 	private String id;
 	private List<Node> nodes;
 	private List<Path> paths;
 	
 	
 	
 	/**
 	 * Creates a new railway system.
 	 * @param id Name of this railway system
 	 */
 	public RailwaySystem(String id) {
 		if (id == null || id.trim().isEmpty()) {
 			throw new IllegalArgumentException("id must not be null or empty");
 		}
 		nodes = new ArrayList<Node>();
 		paths = new ArrayList<Path>();
 		map = new NodeMap();
 	}
 	
 	
 	
 	/**
 	 * Adds a node to this railway system.
 	 * 
 	 * @param node
 	 *            to be added, must not be null
 	 * @throws IllegalArgumentException
 	 *             if node is null
 	 */
 	public void addNode(Node node) {
 		// check constraints
 		if (node == null) {
 			throw new IllegalArgumentException("node must not be null");
 		}
 		
 		// add node to data structure
 		nodes.add(node);
 		
 		// add node to NodeMap
 		NodeFigure fig = node.getNodeFigure();
		 map.getNodeLayer().add(fig);
		 map.getPaths().put(fig, new ArrayList<PathFigure>());
		 map.getNodes().put(fig.getName(), fig);
 	}
 	
 	/**
 	 * Adds a path to this railway system. The end node and the start node of
 	 * path must have been already added to this railway system, otherwise an
 	 * {@link IllegalArgumentException} is thrown.
 	 * 
 	 * @param path
 	 *            to be added
 	 * @throws IllegalArgumentException
 	 *             if path is null or start or end node haven't been added
 	 */
 	public void addPath(Path path) {
 		// check constraints
 		if (path == null) {
 			throw new IllegalArgumentException("node must not be null");
 		}
 		if (!nodes.contains(path.getStart()) || !nodes.contains(path.getEnd())) {
 			throw new IllegalArgumentException("start or end are unknown");
 		}
 		
 		// add path to data structure
 		paths.add(path);
 		
 		// add path to NodeMap
 		PathFigure fig = path.getPathFigure();
 		NodeFigure start = path.getStart().getNodeFigure();
 		NodeFigure end = path.getEnd().getNodeFigure();
 		map.getNodeLayer().add(fig);
 		map.getPaths().get(start).add(fig);
 		map.getPaths().get(end).add(fig);	
 		map.getNodeLayer().remove(start);
 		map.getNodeLayer().add(start);
 		map.getNodeLayer().remove(end);
 		map.getNodeLayer().add(end);
 	}
 	
 	/**
 	 * Returns the id of this railway system.
 	 * 
 	 * @return the id
 	 */
 	public String getID() {
 		return id;
 	}
 
 	/**
 	 * Returns a List containing all nodes that have been added to this railway
 	 * system.
 	 * 
 	 * @return list of nodes
 	 */
 	public List<Node> getNodes() {
 		return nodes;
 	}
 
 	/**
 	 * Returns a List containing all paths that have been added to this railway
 	 * system.
 	 * 
 	 * @return list of paths
 	 */
 	public List<Path> getPaths() {
 		return paths;
 	}
 
 	/**
 	 * Returns the {@link NodeMap} representing this railroad system.
 	 * 
 	 * @return the node map
 	 */
 	public NodeMap getNodeMap() {
 		return map;
 	}
 }
