 package edu.agh.tunev.model.kkm;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.agh.tunev.world.Exit;
 import edu.agh.tunev.world.Obstacle;
 import edu.agh.tunev.world.Physics;
 import edu.agh.tunev.world.World;
 
 public class Board {
 	
 	public Point2D.Double getDimension() {
 		return dimension;
 	}
 
 	public List<Agent> getAgents() {
 		return agents;
 	}
 
 	public void addAgent(Agent agent) {
 		agents.add(agent);
 	}
 
 	public List<Exit> getExits() {
 		return world.getExits();
 	}
 
 	public List<Obstacle> getObstacles() {
 		return world.getObstacles();
 	}
 
 	public Point2D.Double getNearestFireSrc(Point2D.Double p) {
 		double min = Double.POSITIVE_INFINITY;
 		Point2D.Double nearest_src = null;
 
 		for (Point2D.Double src : fire_srcs) {
 			double dist = src.distance(p);
 			if (dist < min) {
 				min = dist;
 				nearest_src = src;
 			}
 		}
 
 		return nearest_src;
 	}
 
 	public void addFireSrc(Point2D.Double _src) {
 		fire_srcs.add(_src);
 	}
 
 	public double getPhysics(Point2D.Double point, Physics.Type what) {
 		return world.getPhysicsAt(t, point).get(what);
 	}
 
 	// ------------- internals start here, an Agent should not use those
 	private Point2D.Double dimension;
 
 	/** Środkowy punkt źródła ognia */
 	private List<Point2D.Double> fire_srcs;
 
 	private List<Agent> agents;
 	
 	private World world;
 
 	public Board(World world) {
 		this.world = world;
 		this.dimension = world.getDimension();
 		agents = new ArrayList<Agent>();
 		fire_srcs = new ArrayList<Point2D.Double>();
 	}
 
 	/**
 	 * Jedna iteracja symulacji. Agent uaktualnia swoj stan, tylko jesli zyje,
 	 * jest na planszy i uplynal juz jego pre movement time
 	 * 
 	 * @param dt
 	 *            czas w [ms] który upłynął od poprzedniej iteracji
 	 * @throws NoPhysicsDataException
 	 */
 	private double t = 0;
 	public void update(double t, double dt) {
 		this.t = t;
 		for (Agent agent : agents) {
 			if (agent.isAlive() && !agent.isExited()
 					&& t > agent.getPreMoveTime())
 				agent.update(dt);
 		}
 	}
 
 	public double getExitY(Exit e) {
 		return (e.p1.y + e.p2.y) / 2;
 	}
 
 	public double getExitX(Exit e) {
 		return (e.p1.x + e.p2.x) / 2;
 	}
 
 	/**
 	 * Znajduje punkt leżący na odcinku reprezentującym wyjście, będący w
 	 * najmniejszej odległości do zadanego punktu
 	 * 
 	 * @param p
 	 *            zadany punkt
 	 * @return najbliżej leżący punkt
 	 */
 	public Point2D.Double getExitClosestPoint(Exit e, Point2D.Double p) {
 		Point2D.Double closestPoint;
 
 		double delta_x = e.p2.x - e.p1.x;
 		double delta_y = e.p2.y - e.p1.y;
 
 		if ((delta_x == 0) && (delta_y == 0)) {
 			// throw sth
 		}
 
 		double u = ((p.x - e.p1.x) * delta_x + (p.y - e.p1.y) * delta_y)
 				/ (delta_x * delta_x + delta_y * delta_y);
 
 		if (u < 0) {
 			closestPoint = new Point2D.Double(e.p1.x, e.p2.y);
 		} else if (u > 1) {
 			closestPoint = new Point2D.Double(e.p2.x, e.p2.y);
 		} else {
 			closestPoint = new Point2D.Double(
 					(int) Math.round(e.p1.x + u * delta_x),
 					(int) Math.round(e.p1.y + u * delta_y));
 		}
 
 		return closestPoint;
 	}
 	
 	public boolean isInsideObstacle(Obstacle obstacle, Point2D.Double p, double reserve) {
 		return obstacle.contains(p, reserve);
 	}
 
 	public final static class Wall {
 	}
 
 	public boolean isOutOfBounds(Point2D.Double p) {
		return !(p.x >= 0 && p.y >= 0 && p.x <= dimension.x && p.y <= dimension.y);
 	}
 
 }
