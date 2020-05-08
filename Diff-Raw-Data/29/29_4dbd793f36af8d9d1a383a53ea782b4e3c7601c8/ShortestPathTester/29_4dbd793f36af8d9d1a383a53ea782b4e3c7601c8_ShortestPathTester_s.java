 package chalmers.dax021308.ecosystem.model.util;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import chalmers.dax021308.ecosystem.model.environment.obstacle.AbstractObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 
 /**
  * Class for testing A* algorithm.
  * 
  * @author Erik Ramqvist
  *
  */
 public class ShortestPathTester extends JPanel {
 	private static final long serialVersionUID = 3766084045600317521L;
 	private static final int HEURISTIC_UPSAMPLE = 10;
 	private static final long ITERATION_TIME = 16;
 	private static final Dimension simulationDimension = new Dimension(750, 750);
 	
 	private static HashSet<AStarPosition> closedSet;
 	private static PriorityQueue<AStarPosition> openSet;
 	private static AStarPosition current;
 	
 	
 	public ShortestPathTester() {
 		JFrame frame = new JFrame();
 		frame.setSize(new Dimension(750, 750));
 		frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.add(this);
 //		Position start = new Position(5.0, 5000.0);
 //		Position end = new Position(5.0, -32.0);
 		Position start = new Position(124.0, 121.0);
 		Position end = new Position(563.0, 421.0);
 		System.out.println("Distance: " + start.getDistance(end));
 		//Threshold for using PriorityQueue is 45, lower use HashSet!
 		long time;
 		double elapsed;
 		List<Position> result;
 		List<IObstacle> obsList = new ArrayList<IObstacle>();
 		time = System.nanoTime();
 //		result = getShortestPathHashSet(start, end);
 //		elapsed = (System.nanoTime() - time)*0.000001;
 //		System.out.println("HashSet Completed in: " + elapsed + " ms. Positions: " + result.size()/*  + " Result: " + result*/);
 //		time = System.nanoTime();
 //		result = Position.getShortestPathPriorityQueueLinkedList(start, end);
 //		elapsed = (System.nanoTime() - time)*0.000001;
 //		System.out.println("LinkedList Completed in: " + elapsed + " ms. Positions: " + result.size()/*  + " Result: " + result*/);
 		time = System.nanoTime();
 		result = getShortestPathPriorityQueue(start, end, obsList);
 		elapsed = (System.nanoTime() - time)*0.000001;
 		System.out.println("PriorityQueue Completed in: " + elapsed + " ms. Positions: " + result.size()/*  + " Result: " + result*/);
 //		time = System.nanoTime();
 //		result = Position.getShortestPath(start, end);
 //		elapsed = (System.nanoTime() - time)*0.000001;
 //		System.out.println("Optimal solution Completed in: " + elapsed + " ms. Positions: " + result.size() + " Result: " + result);
 //		
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
		Log.v("Paint");
 		if(closedSet != null) {
 			for(AStarPosition a : closedSet) {
				drawAStarPosition(a, g);
 			}
 		}
 		if(openSet != null) {
			for(AStarPosition a : closedSet) {
				drawAStarPosition(a, g);
 				
 			}
 		}
 		if(current != null) {
			drawAStarPosition(current, g);
 		}
 	}
 
 	
 	
	private void drawAStarPosition(AStarPosition p, Graphics g) {
 		//draw position
		g.setColor(Color.BLACK);
 		g.drawRect((int) p.getX(),(int)  p.getY(), 10, 10);
 	}
 	
 	/**
 	 * Calculates the shortest path to the target using A* search algorithm.
 	 * <p>
 	 * Fast for long distances.
 	 * <p>
 	 * TODO: Supply a obstacle-list and Shape?
 	 * TODO: Make private when finished.
 	 * 
 	 * For use with target agents behind obstacles.
 	 * 
 	 * @see <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">http://en.wikipedia.org/wiki/A*_search_algorithm</a>
 	 * @param target
 	 * @return
 	 * @author Erik Ramqvist
 	 */
 	public List<Position> getShortestPathPriorityQueue(Position startPos, Position endPos, List<IObstacle> obsList/*, IShape simShape*/) {
		AStarPosition start = new AStarPosition(startPos.getX(), startPos.getY());
		AStarPosition goal = new AStarPosition(endPos.getX(), endPos.getY());
 		start.g_score = 0;
 		start.f_score = heuristic_manhattan_distance(start, goal)*HEURISTIC_UPSAMPLE;
 		closedSet = new HashSet<AStarPosition>();
 		openSet = new PriorityQueue<AStarPosition>(100, new AStarPositionComparator());
 		openSet.add(start);
 		current = start;//the node in openset having the lowest f_score[] value
 		while(!openSet.isEmpty()) {
 //			Log.v("openset" + openSet);
 //			Log.v("closedSet" + closedSet);
 ////			Log.v("g_score" + g_score);
 ////			Log.v("f_score" + f_score);
 //			Log.v("current" + current);
 //			Log.v("--------");
 			current = openSet.poll();
 			
 			//Slow down to see movement.
 			repaint();
 			try {
 				Thread.sleep(ITERATION_TIME);
 			} catch (InterruptedException e) {
 				
 			}
 			closedSet.add(current);
 			if(current.equals(goal)) {
 				return reconstructPath(current);
 			}
 			for(AStarPosition neighbour : getNeighbours(current, obsList)) {
 				double tentative_g_score = current.g_score + current.getDistance(neighbour);
 				if(closedSet.contains(neighbour)) {
 					if(tentative_g_score >= neighbour.g_score) {
 						continue;
 					}
 				}
 				if(!openSet.contains(neighbour) || tentative_g_score < neighbour.g_score) {
 					neighbour.came_from = current;
 					neighbour.g_score = tentative_g_score;
 					neighbour.f_score = neighbour.g_score + heuristic_manhattan_distance(goal, neighbour)*HEURISTIC_UPSAMPLE;
 					if(!openSet.contains(neighbour)) {
 						openSet.add(neighbour);
 					}
 				}
 			}
 		}	
 		//Failure to find path.
 		return Collections.emptyList();
 	}
 	
 
 	private static List<Position> reconstructPath(AStarPosition current_node) {
 		List<Position> result = new ArrayList<Position>();
 		AStarPosition current = current_node;
 		while(current != null) {
 			result.add(current);
 			current = current.came_from;
 		}
 	    Collections.reverse(result);
 		return result;
 	}
 	
 	private static class AStarPositionComparator implements Comparator<AStarPosition> {
 
 		@Override
 		public int compare(AStarPosition o1, AStarPosition o2) {
 			if(o1.getGF() > o2.getGF()) {
 				return 1;
 			} else if(o1.getGF() < o2.getGF()) {
 				return -1;
 			}
 			return 0;
 		}
 	}
 	
 
 	/**
 	 * Gets the neighbours of one AStarPosition.
 	 * <p>
 	 * Is shape really needed here? Since we guarantee Agents are inside the shape.
 	 * 
 	 * @param p1
 	 * @param obsList
 	 * @param shape
 	 * @return
 	 */
 	public static List<AStarPosition> getNeighbours(AStarPosition p1, List<IObstacle> obsList/*, IShape shape*/) {
 		List<AStarPosition> neighbours = new ArrayList<AStarPosition>(8);
 		if(obsList.isEmpty()) {
 			neighbours.add(new AStarPosition(p1.getX(), 	p1.getY()+1));
 			neighbours.add(new AStarPosition(p1.getX()+1, p1.getY()));
 			neighbours.add(new AStarPosition(p1.getX()-1, p1.getY()));
 			neighbours.add(new AStarPosition(p1.getX(), 	p1.getY()-1));
 			neighbours.add(new AStarPosition(p1.getX()+1, p1.getY()-1));
 			neighbours.add(new AStarPosition(p1.getX()-1, p1.getY()+1));
 			neighbours.add(new AStarPosition(p1.getX()+1, p1.getY()+1));
 			neighbours.add(new AStarPosition(p1.getX()-1, p1.getY()-1));
 		} else {
 			double x = p1.getX();
 			double y = p1.getY();
 			AStarPosition p2 = new AStarPosition(x, y+1);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			p2 = new AStarPosition(x+1, y);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			p2 = new AStarPosition(x-1, y);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			p2 = new AStarPosition(x, y-1);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			p2 = new AStarPosition(x+1, y-1);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			p2 = new AStarPosition(x-1, y+1);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			p2 = new AStarPosition(x+1, y+1);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			p2 = new AStarPosition(x-1, y-1);
 			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
 				neighbours.add(p2);
 			}
 			
 		}
 		return neighbours;
 	}
 	
 	/**
 	 * Internal Position class for use with A* shortest path algorithm.
 	 * 
 	 * @author Erik
 	 *
 	 */
 	private static class AStarPosition extends Position {
 		double g_score;
 		double f_score;
 		AStarPosition came_from;
 		public double getGF() {
 			return g_score+f_score;
 		}
 		public AStarPosition(double x, double y) {
 			super(x,y);
 		}
 		
 		@Override
 		public String toString() {
 			return "G: " + g_score + " F: " + f_score + " " + super.toString();
 		}
 	}
 
 	
 	@SuppressWarnings("unused")
 	private static double heuristic_vector_distance(AStarPosition start, AStarPosition goal /*, List<IObstacle> obsList, IShape simShape*/) {
 	    return Math.sqrt(Math.pow(Math.abs(goal.getX() - start.getX()), 2) + Math.pow(Math.abs(goal.getY() - start.getY()), 2));
 	}
 	
     public static double heuristic_manhattan_distance(AStarPosition a, AStarPosition b /*, List<IObstacle> obsList, IShape simShape*/){
         return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
     }	
 	
 	public static void main(String[] args) {
 		new ShortestPathTester();
 	}
 }
