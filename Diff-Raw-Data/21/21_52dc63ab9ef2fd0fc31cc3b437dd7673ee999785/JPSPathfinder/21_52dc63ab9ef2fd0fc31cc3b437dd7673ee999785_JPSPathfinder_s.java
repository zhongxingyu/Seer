 package chalmers.dax021308.ecosystem.model.util;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.environment.obstacle.AbstractObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 import chalmers.dax021308.ecosystem.model.util.shape.IShape;
 
 /**
  * Class for calculating the shortest path to target. 
  * 
  * @author Erik
  *
  */
 public class JPSPathfinder {
 	
 	private List<IObstacle> obsList;
 	private IShape shape;
 	private Dimension simulationDim;
 	private double coordinateScaling;
 	private double safetyDistance = 0;
 
 	public JPSPathfinder(List<IObstacle> obsList, IShape shape, Dimension simulationDim) {
 		this.obsList = obsList;
 		this.shape = shape;
 		this.simulationDim = simulationDim;
 	}
 	
 	public List<Position> getShortestPath(Position start, Position end, double safetyDistance) {
 		//double distance = start.getDistance(end);
 		//if(distance > 75) {
 			coordinateScaling = 5;
 			double x = Math.round((end.getX() / coordinateScaling))*coordinateScaling;
 			double y = Math.round((end.getY() / coordinateScaling))*coordinateScaling;
 			Position newEnd = new Position(x, y);
 			x = Math.round((start.getX() / coordinateScaling))*coordinateScaling;
 			y = Math.round((start.getY() / coordinateScaling))*coordinateScaling;
 			Position newStartPos = new Position(x, y);
 			return getShortestPathJumpPointsSearch(newStartPos, newEnd, safetyDistance);
 		//} else {
 		//	coordinateScaling = 1;
 		//	return getShortestPathJumpPointsSearch(new Position(Math.round(start.x), Math.round(start.y)), new Position(Math.round(end.x), Math.round(end.y)));
 		//}
 	}
 
 
 	/**
 	 * Internal Position class for use with A* shortest path algorithm.
 	 * 
 	 * @author Erik
 	 *
 	 */
 	private class JPSNode extends Position {
 		double g_score;
 		JPSNode came_from;
 		public JPSNode(double x, double y) {
 			super(x,y);
 		}
 
 		public JPSNode(JPSNode a) {
 			super(a.x,a.y);
 		}
 		
 		@Override
 		public String toString() {
 			return "JPSNode G: " + g_score + " " + super.toString();
 		}
 		
 		public JPSNode shift(double x, double y) {
 			this.x += x;
 			this.y += y;
 			return this;
 		}
 		
 		protected double getG() {
 			if (g_score == 0) {
 				final JPSNode parent = came_from;
 				g_score = parent != null ? parent.getG() + parent.getMoveCost(this) : 0.0D;
 			}
 			return g_score;
 		}
 
 		public double getMoveCost(JPSNode node) {
 			if (node == null)
 				return 0;
 			double x = node.getX(), y = node.getY(), px = getX(), py = getY();
 			double steps = getSteps(x, y, px, py);
 			return x == px || y == px ? steps : (double) steps * 1;
 		}
 		
 		private double getSteps(double x, double y, double px, double py) {
 			double temp;
 			if ((temp = x - px) != 0) { // straight, horizontal AND will handle
 										// diagonal
 			} else if ((temp = y - py) != 0) { // straight, vertical
 			} else
 				return 0;
 			return Math.abs(temp);
 		}
 		
 		public JPSNode derive(double x, double y) {
 			return clone().shift(x, y);
 		}
 
 		@Override
 		public JPSNode setPosition(double x, double y) {
 			this.x = x;
 			this.y = y;
 			return this;
 		}
 
 		
 		@Override
 		public JPSNode clone() {
 			JPSNode clone = new JPSNode(x, y);
 			clone.came_from = this;
 			return clone;
 		}
 	}
 	
 
 	private List<Position> reconstructPath(JPSNode current_node) {
 		List<Position> result = new LinkedList<Position>();
 		JPSNode current = current_node;
 		while(current != null) {
 			result.add(current);
 			current = current.came_from;
 		}
 	    Collections.reverse(result);
 		return result;
 	}
 
 	public List<Position> getShortestPathJumpPointsSearch(Position startPos, Position endPos, double safetyDistance) {
 		this.safetyDistance = safetyDistance;
 		JPSNode start = new JPSNode(startPos.getX(), startPos.getY());
		JPSNode goal = new JPSNode(endPos.getX(), endPos.getY());
 		HashSet<JPSNode> closedSet = new HashSet<JPSNode>();
 		HashSet<JPSNode> openSet = new HashSet<JPSNode>();
 		openSet.add(start);
 		JPSNode current = start;
 		double lowScore = Integer.MAX_VALUE;
 		while (!openSet.isEmpty()) {
 			lowScore = Integer.MAX_VALUE;
 			for (JPSNode n : openSet) {
 				double score = n.g_score;
 				if (score < lowScore) {
 					current = n;
 					lowScore = score;
 				} else if(score == lowScore) {
 					if(n != current) {
 						current = n;
 					}
 				}
 			}
 //				 Log.v("openset" + openSet);
 ////				 Log.v("closedSet" + closedSet);
 ////				 Log.v("g_score" + current.g_score);
 ////				 Log.v("f_score" + current.f_score);
 //				 Log.v("current: " + current);
 //				 Log.v("--------");
 			if (current.equals(goal)) {
 				return reconstructPath(current);
 			}
 			openSet.remove(current);
 			closedSet.add(current);
 			for(JPSNode neighbour : getJPSNeighbours(current, obsList)) {
 				JPSNode jumpPoint = jump(neighbour, current, goal);
 				if (jumpPoint != null) {
 					if (closedSet.contains(jumpPoint))
 						continue;
 					if (!openSet.contains(jumpPoint)) {
 						jumpPoint.came_from = current;
 						openSet.add(jumpPoint);
 					} else if ((current.getG() + current.getMoveCost(jumpPoint)) < jumpPoint.getG()) { // G score of node with current node as it's parent
 						JPSNode instanceNode = retrieveInstance(openSet, jumpPoint);
 						if (instanceNode != null)
 							instanceNode.came_from = current;
 					}
 				}
 			}
 		}
 		Log.e("Failed to find path to target! Start: " + start + " End: " + endPos + " obsList: " + obsList + " Dimension: "+  simulationDim + " Shape: " + shape);
 		return null;
 	}
 	
 	private JPSNode retrieveInstance(HashSet<JPSNode> openSet, JPSNode node) {
 		if (node == null)
 			return null;
 		final Iterator<JPSNode> nI = openSet.iterator();
 		while (nI.hasNext()) {
 			final JPSNode n = nI.next();
 			if (node.equals(n))
 				return n;
 		}
 		return null;
 	}
 
 	private JPSNode[] getJPSNeighbours(JPSNode current,	List<IObstacle> obsList) {
 		double x = current.getX();
 		double y = current.getY();
 		LinkedList<JPSNode> nodes = new LinkedList<JPSNode>();
 		JPSNode parent = current.came_from;
 		if (parent != null) { // determines whether to prune neighbors
 			JPSNode norm = normalizeDirection(x, y, parent.getX(), parent.getY());
 			double dx = norm.getX()*coordinateScaling;
 			double dy = norm.getY()*coordinateScaling;
 			JPSNode temp = new JPSNode(current);
 			if (((int) dx & (int) dy) != 0) { // diagonal direction
 				// check straight directions in the direction of the diagonal move
 				if (walkable(temp.setPosition(x, y + dy)))
 					nodes.add(new JPSNode(temp));
 				if (walkable(temp.setPosition(x + dx, y)))
 					nodes.add(new JPSNode(temp));
 				if (walkable(temp.setPosition(x + dx, y + dy)))
 					nodes.add(new JPSNode(temp));
 				// forced neighbor checks
 				if (!walkable(temp.setPosition(x - dx, y)))
 					nodes.add(new JPSNode(temp.shift(0, dy)));
 				if (!walkable(temp.setPosition(x, y - dy)))
 					nodes.add(new JPSNode(temp.shift(dx, 0)));
 			} else { // straight direction
 				if (walkable(temp.setPosition(x + dx, y + dy))) {
 					nodes.add(new JPSNode(temp));
 					// forced neighbor checks
 					if (!walkable(temp.setPosition(x + dy, y + dx)))
 						nodes.add(new JPSNode(temp.shift(dx, dy)));
 					if (!walkable(temp.setPosition(x - dy, y - dx)))
 						nodes.add(new JPSNode(temp.shift(dx, dy)));
 				}
 			}
 		} else {
 		// no parent, return all that aren't blocked
 		JPSNode[] ns = new JPSNode[] { new JPSNode(x, y - coordinateScaling), new JPSNode(x + coordinateScaling, y - coordinateScaling), new JPSNode(x + coordinateScaling, y), new JPSNode(x + coordinateScaling, y + coordinateScaling),
 		new JPSNode(x, y + coordinateScaling), new JPSNode(x - coordinateScaling, y + coordinateScaling), new JPSNode(x - coordinateScaling, y), new JPSNode(x - coordinateScaling, y - coordinateScaling) };
 		for (int i = 0; i < ns.length; i++) {
 			if (walkable(ns[i]))
 				nodes.add(ns[i]);
 			}
 		}
 		return nodes.toArray(new JPSNode[nodes.size()]);
 	}
 
 	
 	private JPSNode normalizeDirection(double x, double y, double d, double e) {
 		double dx = x - d, dy = y - e;
 		dx /= Math.max(Math.abs(dx), 1);
 		dy /= Math.max(Math.abs(dy), 1);
 		return new JPSNode(dx, dy);
 	}
 	
 	private JPSNode jump(JPSNode node, JPSNode parent, JPSNode goal) {
 		double x = node.getX(), y = node.getY(), px = parent.getX(), py = parent.getY();
 		double dx = (x - px);
 		double dy = (y - py);
 //		Log.e("Coordinatescaling: " + coordinateScaling);
 //		Log.e("dy: " + dy + " dx: " + dx);
 //		Log.e("JUMP! node: " + node.toString() + " parent: " + parent.toString() + " dx: " + dx + " dy: " + dy);
 		if (!walkable(node)) // check blocked
 			return null;
 		if (node.equals(goal)) // reached goal
 			return new JPSNode(node);
 		
 		// resolve forced neighbors
 		JPSNode temp = new JPSNode(node);
 		if (((int) dx & (int) dy) != 0) { // diagonal
 			if ((walkable(temp.setPosition(x - dx, y + dy)) && !walkable(temp.setPosition(x - dx, y))) ||
 			(walkable(temp.setPosition(x + dx, y - dy)) && !walkable(temp.setPosition(x, y - dy)))) {
 				return new JPSNode(node);
 			}
 			// recurse
 			JPSNode h = jump(node.derive(dx, 0), node, goal);
 			if (h != null)
 				return new JPSNode(node);
 			JPSNode v = jump(node.derive(0, dy), node, goal);
 			if (v != null)
 				return new JPSNode(node);
 			} else if (dx == 0) { // vertical, dx = 0, dy = 1 or -1
 				if ((walkable(temp.setPosition(x + coordinateScaling, y + dy)) && !walkable(temp.setPosition(x + coordinateScaling, y))) ||
 						(walkable(temp.setPosition(x - coordinateScaling, y + dy)) && !walkable(temp.setPosition(x - coordinateScaling, y)))) {
 					return new JPSNode(node);
 				}
 		} else { // horizontal, dx = 1 or -1, dy = 0
 			if ((walkable(temp.setPosition(x + dx, y + coordinateScaling)) && !walkable(temp.setPosition(x, y + coordinateScaling))) ||
 					(walkable(temp.setPosition(x + dx, y - coordinateScaling)) && !walkable(temp.setPosition(x, y - coordinateScaling)))) {
 				return new JPSNode(node);
 			}
 		}
 		return jump(node.derive(dx, dy), node, goal);
 	}
 	
 	private boolean walkable(JPSNode node) {
		return !AbstractObstacle.isInsideObstacleList(obsList, node, safetyDistance) && shape.isInside(simulationDim, node);
 	}
 }
