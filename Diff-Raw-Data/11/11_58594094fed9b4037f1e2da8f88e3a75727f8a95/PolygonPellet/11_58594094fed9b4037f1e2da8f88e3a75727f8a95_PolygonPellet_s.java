 package edu.washington.cs.games.ktuite.pointcraft;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import org.lwjgl.util.vector.Vector3f;
 import static org.lwjgl.opengl.GL11.*;
 
 public class PolygonPellet extends Pellet {
 
 	public static Stack<PolygonPellet> current_cycle = new Stack<PolygonPellet>();
 	private boolean first_in_cycle = false;
 	private static PlaneScaffold plane = new PlaneScaffold();
 
 	/*
 	 * A Pellet is a magical thing that you can shoot out of a gun that will
 	 * travel towards the model and stick to the first point it intersects.
 	 * 
 	 * Soon it will even stick to other pellets.
 	 */
 	public PolygonPellet(List<Pellet> _pellets) {
 		super(_pellets);
 		pellet_type = Main.GunMode.POLYGON;
 	}
 
 	public PolygonPellet(Pellet lp) {
 		super(lp.main_pellets);
 		pos.set(lp.pos);
 		radius = lp.radius;
 		max_radius = lp.max_radius;
 		constructing = lp.constructing;
 		// pellet_type = lp.pellet_type;
 		pellet_type = Main.GunMode.POLYGON;
 	}
 
 	@Override
 	public void update() {
 		// constructing means the pellet has triggered something to be built at
 		// its sticking location
 		if (!constructing) {
 			// not constructing means the pellet is still traveling through
 			// space
 
 			// move the pellet
 			Vector3f.add(pos, vel, pos);
 
 			// if it's too old, kill it
 			if (Main.timer.getTime() - birthday > 5) {
 				alive = false;
 			} else {
 				// if the pellet is not dead yet, see if it intersected anything
 
 				// did it hit another pellet?
 				Pellet neighbor_pellet = queryOtherPellets();
 
 				// did it hit a line or plane?
 				Vector3f closest_point = queryScaffoldGeometry();
 
 				if (neighbor_pellet != null) {
 					alive = false;
 
 					if (current_cycle.size() > 0
 							&& neighbor_pellet == current_cycle.lastElement()) {
 						System.out.println("shot at same pellet");
 						return;
 					}
 
 					if (!(neighbor_pellet instanceof PolygonPellet)) {
 						neighbor_pellet.visible = false;
 						ActionTracker.hiddenPellet(neighbor_pellet);
 
 						neighbor_pellet = new PolygonPellet(neighbor_pellet);
 						Main.new_pellets_to_add_to_world.add(neighbor_pellet);
 						current_cycle.add((PolygonPellet) neighbor_pellet);
 						if (current_cycle.size() > 1) {
 							makeLine();
 						}
 
 						if (current_cycle.size() > 2
 								&& current_cycle.get(0) == current_cycle
 										.get(current_cycle.size() - 1)) {
 							makePolygon();
 						} else {
 							ActionTracker.newPolygonPellet(neighbor_pellet);
 						}
 					} else {
 						current_cycle.add((PolygonPellet) neighbor_pellet);
						neighbor_pellet.ref_count++;
 						if (current_cycle.size() > 1) {
 							makeLine();
 						}
 
 						if (current_cycle.size() > 2
 								&& current_cycle.get(0) == current_cycle
 										.get(current_cycle.size() - 1)) {
 							makePolygon();
 						} else {
 							ActionTracker.newPolygonPellet(neighbor_pellet);
 						}
 					}
 
 				} else if (current_cycle.size() >= 3
 						&& plane.distanceToPointNoBounds(pos) < radius) {
 					pos.set(plane.closestPoint(pos));
 					constructing = true;
 					if (CONNECT_TO_PREVIOUS)
 						current_cycle.add(this);
 
 					if (CONNECT_TO_PREVIOUS && current_cycle.size() > 1) {
 						makeLine();
 					}
 
 					if (current_cycle.size() > 2
 							&& current_cycle.get(0) == current_cycle
 									.get(current_cycle.size() - 1))
 						makePolygon();
 
 					ActionTracker.newPolygonPellet(this);
 				} else if (closest_point != null) {
 					System.out.println("pellet stuck to some geometry");
 					constructing = true;
 
 					pos.set(closest_point);
 
 					if (CONNECT_TO_PREVIOUS)
 						current_cycle.add(this);
 
 					if (CONNECT_TO_PREVIOUS && current_cycle.size() > 1) {
 						makeLine();
 					}
 
 					if (current_cycle.size() > 2
 							&& current_cycle.get(0) == current_cycle
 									.get(current_cycle.size() - 1))
 						makePolygon();
 
 					ActionTracker.newPolygonPellet(this);
 
 				} else if (Main.draw_points) {
 					// if it's not dead yet and also didn't hit a
 					// neighboring
 					// pellet, look for nearby points in model
 					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);
 
 					// is it near some points?!
 					if (neighbors > 0) {
 						constructing = true;
 						setInPlace();
 
 						snapToCenterOfPoints();
 
 						if (CONNECT_TO_PREVIOUS)
 							current_cycle.add(this);
 
 						if (CONNECT_TO_PREVIOUS && current_cycle.size() > 1) {
 							makeLine();
 						}
 
 						ActionTracker.newPolygonPellet(this);
 					}
 				}
 
 				if (current_cycle != null && current_cycle.size() > 0)
 					current_cycle.get(0).setAsFirstInCycle();
 
 				// if this polygon has 3 vertices in it, fit a plane and use
 				// that to find the position of the 4th pellet
 				if (!plane.isReady() && current_cycle.size() == 3) {
 					makePlane();
 				}
 			}
 		} else {
 			// the pellet has stuck... here we just give it a nice growing
 			// bubble animation
 			if (radius < max_radius) {
 				radius *= 1.1;
 			}
 		}
 	}
 
 	private void makePlane() {
 		System.out.println("polygon has 3 vertices... fit a plane");
 		plane.pellets.clear();
 		for (Pellet p : current_cycle) {
 			plane.pellets.add(p);
 		}
 		plane.fitPlane();
 	}
 
 	public void makeLine() {
 		// make a line between 2 pellets
 		List<Pellet> last_two = new LinkedList<Pellet>();
 		last_two.add(current_cycle.get(current_cycle.size() - 2));
 		last_two.add(current_cycle.get(current_cycle.size() - 1));
 
 		Primitive line = new Primitive(GL_LINES, last_two);
 		Main.geometry.add(line);
 
 		ActionTracker.newPolygonLine(line);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void makePolygon() {
 		// make the polygon
 		List<Pellet> cycle = new LinkedList<Pellet>();
 		for (PolygonPellet p : current_cycle) {
 			cycle.add(p);
 		}
 		Primitive polygon = new Primitive(GL_POLYGON, cycle);
 		polygon.setPlane(plane);
 		polygon.setPlayerPositionAndViewingDirection(pos, vel);
 		Main.geometry.add(polygon);
 
 		ActionTracker.newPolygon(polygon,
 				(Stack<PolygonPellet>) current_cycle.clone());
 
 		current_cycle.clear();
 		plane.pellets.clear();
 		plane.nullifyPlane();
 	}
 
 	public void setAsFirstInCycle() {
 		first_in_cycle = true;
 	}
 
 	public void coloredDraw() {
 		if (constructing) {
 			float alpha = 1 - radius / max_radius * .2f;
 			glColor4f(.9f, .1f, .4f, alpha);
 			if (first_in_cycle)
 				glColor4f(.8f, 0f, .3f, alpha);
 			sphere.draw(radius, 32, 32);
 		} else {
 			glColor4f(1f, .1f, .4f, 1f);
 			sphere.draw(radius, 32, 32);
 		}
 	}
 
 	public static void startNewPolygon() {
 		current_cycle.clear();
 		plane.pellets.clear();
 		plane.nullifyPlane();
 		System.out.println("making new polygon");
 	}
 
 	public void delete() {
 		System.out.println("delete");
 		if (current_cycle.contains(this) && current_cycle.peek() == this) {
 			current_cycle.pop();
 			if (!Main.geometry.isEmpty()
 					&& !Main.geometry.lastElement().isPolygon()) {
 				Main.geometry.pop();
 			}
 			alive = false;
 		}
 		alive = false;
 		for (Primitive g : Main.geometry) {
 			// kill the pellet unless its part of a polygon
 			if (g.getVertices().contains(this)) {
 				alive = true;
 				break;
 			}
 		}
 	}
 }
