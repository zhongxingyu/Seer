 package edu.washington.cs.games.ktuite.pointcraft.tools;
 
 import java.util.LinkedList;
 import java.util.List;
 import org.lwjgl.util.vector.Vector3f;
 
 import edu.washington.cs.games.ktuite.pointcraft.ActionTracker;
 import edu.washington.cs.games.ktuite.pointcraft.Main;
 import edu.washington.cs.games.ktuite.pointcraft.geometry.PlaneScaffold;
 import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
 import static org.lwjgl.opengl.GL11.*;
 
 public class VerticalLinePellet extends Pellet {
 
 	public static VerticalLinePellet top_pellet = null;
 	public static VerticalLinePellet bottom_pellet = null;
 	public boolean is_upward_pellet = false;
 	public boolean is_downward_pellet = false;
 	private Vector3f last_good_position = new Vector3f();
 	public static Vector3f up_vector = new Vector3f(0,1,0);
 
 	/*
 	 * A Pellet is a magical thing that you can shoot out of a gun that will
 	 * travel towards the model and stick to the first point it intersects.
 	 * 
 	 * Soon it will even stick to other pellets.
 	 */
 	public VerticalLinePellet() {
 		super();
 		pellet_type = Main.GunMode.VERTICAL_LINE;
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
 
 			if (!is_upward_pellet && !is_downward_pellet) {
 				// if it's too old, kill it
 				if (Main.timer.getTime() - birthday > 5) {
 					alive = false;
 				} else {
 					// if the pellet is not dead yet, see if it intersected
 					// anything
 
 					// did it hit another pellet?
 					Pellet neighbor_pellet = queryOtherPellets();
 
 					// did it hit a line or plane?
 					Vector3f closest_point = queryScaffoldGeometry();
 
 					if (neighbor_pellet != null) {
 						System.out.println("pellet stuck to another pellet");
 						pos.set(neighbor_pellet.pos);
 						alive = false;
 						attachVerticalLine();
 					} else if (closest_point != null) {
 
 						System.out.println("pellet stuck to some geometry");
 						constructing = true;
 						pos.set(closest_point);
 						attachVerticalLine();
 
 					} else if (Main.draw_points) {
 						// it didn't hit some existing geometry or pellet
 						// so check the point cloud
 						int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);
 
 						// is it near some points?!
 						if (neighbors > 0) {
 							snapToCenterOfPoints();
 							constructing = true;
 							Main.attach_effect.playAsSoundEffect(1.0f, 1.0f,
 									false);
 
 							attachVerticalLine();
 						}
 					}
 				}
 			} else {
 				// this is one of the upward or downward pellets
 				if (Main.timer.getTime() - birthday > 2) {
 					if (last_good_position != null) {
 						constructing = true;
 						pos.set(last_good_position);
 					} else {
 						alive = false;
 					}
 				} else {
 					// did it hit another pellet?
 					Pellet neighbor_pellet = queryOtherPellets();
 
 					// did it hit a line or plane?
 					Vector3f closest_point = queryScaffoldGeometry();
 
 					if (neighbor_pellet != null) {
 						// something like top pellet = this neighbor pellet, or
 						// the position anyway
 					} else if (closest_point != null) {
 						// something like leave the pellet here... set
 						// constructing = true
 						// call some makeactualline-or-try-to function
 						constructing = true;
 					} else {
 						// it didn't hit some existing geometry or pellet
 						// so check the point cloud
 						int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);
 
 						// is it near some points?!
 						if (neighbors > 2) {
 							last_good_position.set(pos);
 
 						} else {
 							pos.set(last_good_position);
 							if (last_good_position.x != 0
 									&& last_good_position.y != 0
 									&& last_good_position.z != 0)
 								constructing = true;
 							else {
 								alive = false;
 								if (is_downward_pellet)
 									bottom_pellet = null;
 								if (is_upward_pellet)
 									top_pellet = null;
 							}
 						}
 					}
 
 					if (constructing && alive)
 						ActionTracker.newVerticalLinePellet(this);
 					if (top_pellet != null && bottom_pellet != null
 							&& top_pellet.constructing && top_pellet.alive
 							&& bottom_pellet.constructing
 							&& bottom_pellet.alive) {
 						ActionTracker.newVerticalHeightSet();
 					}
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
 
 	public static void setNewUpVector(Vector3f new_up){
 		up_vector.set(new_up);
 	}
 	
 	private void attachVerticalLine() {
 		if (bottom_pellet == null || top_pellet == null) {
 			float speed = vel.length() / 2;
 			if (speed == 0){
 				speed = Main.gun_speed / 2;
 			}
 			up_vector.normalise();
 			up_vector.scale(radius * 1.5f);
 
 			top_pellet = new VerticalLinePellet();
 			top_pellet.pos.set(pos);
 			Vector3f.add(top_pellet.pos, up_vector, top_pellet.pos);
 			top_pellet.vel.set(up_vector);
 			top_pellet.vel.normalise();
 			top_pellet.vel.scale(speed);
 			top_pellet.is_upward_pellet = true;
 			Main.new_pellets_to_add_to_world.add(top_pellet);
 
 			bottom_pellet = new VerticalLinePellet();
 			bottom_pellet.pos.set(pos);
 			Vector3f.sub(bottom_pellet.pos, up_vector, bottom_pellet.pos);
 			bottom_pellet.vel.set(up_vector);
 			bottom_pellet.vel.normalise();
 			bottom_pellet.vel.scale(speed * -1);
 			bottom_pellet.is_downward_pellet = true;
 			Main.new_pellets_to_add_to_world.add(bottom_pellet);
 
 		} else {
 			Vector3f new_up = new Vector3f();
 			Vector3f.sub(top_pellet.pos, bottom_pellet.pos, new_up);
 			float height = new_up.length();
 			new_up.normalise();
 
 			Vector3f center = new Vector3f();
 			Vector3f.add(top_pellet.pos, bottom_pellet.pos, center);
 			center.scale(.5f);
 
 			// change pos to be where plane defined by normal and midpoint of
 			// line intersects ...
 			PlaneScaffold temp_plane = new PlaneScaffold();
 			temp_plane.a = new_up.x;
 			temp_plane.b = new_up.y;
 			temp_plane.c = new_up.z;
 			temp_plane.d = -1
 					* (temp_plane.a * center.x + temp_plane.b * center.y + temp_plane.c
 							* center.z);
 
 			Vector3f new_pos = temp_plane.closestPoint(pos);
 
 			new_up.scale(height / 2f);
 
 			VerticalLinePellet new_top_pellet = new VerticalLinePellet();
 			new_top_pellet.pos.set(new_pos);
 			Vector3f.add(new_top_pellet.pos, new_up, new_top_pellet.pos);
 			new_top_pellet.constructing = true;
 			Main.new_pellets_to_add_to_world.add(new_top_pellet);
 
 			VerticalLinePellet new_bottom_pellet = new VerticalLinePellet();
 			new_bottom_pellet.pos.set(new_pos);
 			Vector3f.sub(new_bottom_pellet.pos, new_up, new_bottom_pellet.pos);
 			new_bottom_pellet.constructing = true;
 			Main.new_pellets_to_add_to_world.add(new_bottom_pellet);
 
 			// make polygon
 			List<Pellet> cycle = new LinkedList<Pellet>();
 			cycle.add(top_pellet);
 			cycle.add(bottom_pellet);
 			cycle.add(new_bottom_pellet);
 			cycle.add(new_top_pellet);
 			cycle.add(top_pellet);
 
 			Primitive polygon = new Primitive(GL_POLYGON, cycle);
			polygon.setPlayerPositionAndViewingDirection(Main.getTransformedPos(), Main.gun_direction);
 			Main.geometry.add(polygon);
 
 			// fit the new polygon's plane
 			polygon.getPlane().pellets.add(new_bottom_pellet);
 			polygon.getPlane().pellets.add(new_top_pellet);
 			polygon.getPlane().pellets.add(bottom_pellet);
 			polygon.getPlane().fitPlane();
 
 			ActionTracker.newPolygon(polygon, null, null);
 
 			ActionTracker.newVerticalWall(top_pellet, bottom_pellet);
 
 			top_pellet = new_top_pellet;
 			bottom_pellet = new_bottom_pellet;
 		}
 		alive = false;
 	}
 
 	public void coloredDraw() {
 		if (constructing) {
 			float alpha = 1 - radius / max_radius * .2f;
 			glColor4f(.3f, .4f, .7f, alpha);
 			drawSphere(radius);
 		} else {
 			glColor4f(.3f, .4f, .7f, 1f);
 			drawSphere(radius);
 		}
 	}
 
 	public static void clearAllVerticalLines() {
 		System.out.println("clearing vertical line heights");
 		bottom_pellet = null;
 		top_pellet = null;
 	}
 
 }
