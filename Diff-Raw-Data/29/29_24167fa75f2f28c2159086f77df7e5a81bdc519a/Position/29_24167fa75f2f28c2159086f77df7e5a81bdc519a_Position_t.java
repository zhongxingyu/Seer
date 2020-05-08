 package chalmers.dax021308.ecosystem.model.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * Position class.
  * 
  * @author Henrik
  * 
  */
 
 public class Position {
 	private double x;
 	private double y;
 
 	public Position() {
 		this(0, 0);
 	}
 
 	public Position(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Clone a position. Returning a new Position with the value of this one.
 	 * @return
 	 */
 	public Position(Position p) {
 		this.x = p.x;
 		this.y = p.y;
 	}
 
 	public double getDistance(Position p) {
 		double dX = p.x - x;
 		double dY = p.y - y;
 		return Math.sqrt(dX * dX + dY * dY);
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public double getX() {
 		return x;
 	}
 
 	public void setPosition(Position p) {
 		setPosition(p.x, p.y);
 	}
 
 	public Position setPosition(double x, double y) {
 		this.x = x;
 		this.y = y;
 		return Position.this;
 	}
 	
 	/**
 	 * Adds a vector to current position.
 	 * @param v vector to add.
 	 */
 	public Position addVector(Vector v){
 		this.x += v.getX();
 		this.y += v.getY();
 		return this;
 	}
 
 	/**
 	 * Adds a vector to a position and returns the new position.
 	 * @param p the position
 	 * @param v the vector
 	 * @return a position p+v.
 	 */
 	public static Position positionPlusVector(Position p, Vector v) {
 		return new Position(p.x+v.getX(),p.y+v.getY());
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(x);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		temp = Double.doubleToLongBits(y);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Position other = (Position) obj;
 		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
 			return false;
 		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
 			return false;
 		return true;
 	}
 	
 	
 	/**
 	 * Calculates the shortest path to the target using A* search algorithm.
 	 * <p>
 	 * Under development!
 	 * 
 	 * TODO: Move to Position class and supply a obstacle-list?
 	 * 
 	 * For use with target agents behind obstacles.
 	 * 
 	 * @see <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">http://en.wikipedia.org/wiki/A*_search_algorithm</a>
 	 * @param target
 	 * @return
 	 * @author Erik Ramqvist
 	 */
	public static List<Position> getShortestPath(Position startPos, Position endPos /*, List<IObstacle> obsList*/) {
 		Position start = new Position(startPos.getX(), startPos.getY());
 		Position goal = new Position(endPos.getX(), endPos.getY());
 		Set<Position> closedSet = new HashSet<Position>();
 		Set<Position> openSet = new HashSet<Position>();
 		openSet.add(start);
 		Map<Position, Position> came_from = new HashMap<Position, Position>();
 		
 		Map<Position, Double> g_score = new HashMap<Position, Double>();
 		g_score.put(start, 0.0);
 		Map<Position, Double> f_score = new HashMap<Position, Double>(); //Prioritets k?
 		f_score.put(start, g_score.get(start) + heuristic_cost_estimate_vector_distance(start, goal));
 		Position current = start;//the node in openset having the lowest f_score[] value
 		double lowScore = Integer.MAX_VALUE;
 		while(!openSet.isEmpty()) {
 			for(Position n : openSet) {
 				if(f_score.get(n) < lowScore) {
 					current = n;
 					lowScore = f_score.get(n);
 				}
 			}
 
 			Log.v("openset" + openSet);
 			Log.v("closedSet" + closedSet);
 			Log.v("g_score" + g_score);
 			Log.v("f_score" + f_score);
 			Log.v("current" + current);
 			Log.v("--------");
 			if(current.equals(goal)) {
 				return reconstructPath(came_from, goal);
 			}
 			openSet.remove(current);
 			closedSet.add(current);
 			for(Position neighbour : getNeighbours(current)) {
 				double tentative_g_score = g_score.get(current) + current.getDistance(neighbour);
 				if(closedSet.contains(neighbour)) {
 					if(tentative_g_score >= g_score.get(neighbour)) {
 						continue;
 					}
 				}
 				if(!openSet.contains(neighbour) || tentative_g_score < g_score.get(neighbour)) {
 					came_from.put(neighbour, current);
 					g_score.put(neighbour, tentative_g_score);
 					f_score.put(neighbour, g_score.get(neighbour) + heuristic_cost_estimate_vector_distance(neighbour, goal));
 					if(!openSet.contains(neighbour)) {
 						openSet.add(neighbour);
 					}
 				}
 			}
 		}
 		//Failure to find path.
 		return Collections.emptyList();
 	}
 	
 	/* Error with heuristic function! */
 	private static double heuristic_cost_estimate_vector_distance(Position start, Position goal) {
 	    return Math.sqrt(Math.pow(Math.abs(goal.getX() - start.getX()), 2) + Math.pow(Math.abs(goal.getY() - start.getY()), 2));
 	}
 	private static double heuristic_cost_estimate(Position start, Position goal) {
 	    return Math.sqrt(Math.pow(goal.getX() - start.getX(), 2) + Math.pow(goal.getY() - start.getY(), 2));
 	}
 	
 	private static List<Position> reconstructPath(Map<Position, Position> came_from, Position current_node) {
 		List<Position> result = new ArrayList<Position>();
 		Position current = current_node;
 		while(current != null) {
 			result.add(current);
 			current = came_from.get(current);
 		}
 	    Collections.reverse(result);
 		return result;
 	}
 	
 	
	public static List<Position> getNeighbours(Position p /*, List<IObstacle> obsList*/) {
 		List<Position> neighbours = new ArrayList<Position>(8);
 		//TODO: Check here if positions is not inside obstacle.
 		neighbours.add(new Position(p.getX(), 	p.getY()+1));
 		neighbours.add(new Position(p.getX()+1, p.getY()));
 		neighbours.add(new Position(p.getX()-1, p.getY()));
 		neighbours.add(new Position(p.getX(), 	p.getY()-1));
 		neighbours.add(new Position(p.getX()+1, p.getY()-1));
 		neighbours.add(new Position(p.getX()-1, p.getY()+1));
 		neighbours.add(new Position(p.getX()+1, p.getY()+1));
 		neighbours.add(new Position(p.getX()-1, p.getY()-1));
 		return neighbours;
 	}
 	
 	@Override
 	public String toString(){
 		return "("+this.x+","+this.y+")";
 	}
 }
