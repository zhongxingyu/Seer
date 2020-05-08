 package edu.washington.cs.games.ktuite.pointcraft;
 
 import java.nio.DoubleBuffer;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class LinePellet extends Pellet {
 
 	public static List<LinePellet> current_line = new LinkedList<LinePellet>();
 
 	// temporary holder of intersection pellets
 	public static List<LinePellet> intersection_points = new LinkedList<LinePellet>();
 
 	private boolean is_intersection = false;
 
 	/*
 	 * A Pellet is a magical thing that you can shoot out of a gun that will
 	 * travel towards the model and stick to the first point it intersects.
 	 * 
 	 * Soon it will even stick to other pellets.
 	 */
 	public LinePellet(List<Pellet> _pellets) {
 		super(_pellets);
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
 					System.out.println("pellet stuck to another pellet");
 					pos.set(neighbor_pellet.pos);
 					alive = false;
 					current_line.add(this);
 					fitLine();
 				} else if (closest_point != null) {
 					System.out.println("pellet stuck to some geometry");
 					constructing = true;
 					pos.set(closest_point);
 					current_line.add(this);
 					fitLine();
 				} else {
 					// it didn't hit some existing geometry or pellet
 					// so check the point cloud
 					int neighbors = LibPointCloud.queryKdTree(pos.x, pos.y,
 							pos.z, radius);
 
 					// is it near some points?!
 					if (neighbors > 0) {
 						snapToCenterOfPoints();
 						constructing = true;
 						Main.attach_effect.playAsSoundEffect(1.0f, 1.0f, false);
 						current_line.add(this);
 						fitLine();
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
 
 	public void finalize() {
 		if (current_line.size() == 2 || current_line.size() == 3)
 			Main.geometry_v.remove(Main.geometry_v.size() - 1);
 		if (current_line.contains(this)) {
 			current_line.remove(this);
 			fitLine();
 		}
 	}
 
 	public void draw() {
 		if (is_intersection) {
 			float alpha = 1 - radius / max_radius * .2f;
 			glColor4f(.1f, .8f, .3f, alpha);
 			sphere.draw(radius, 32, 32);
 		} else if (constructing) {
 			float alpha = 1 - radius / max_radius * .2f;
 			glColor4f(.1f, .4f, .7f, alpha);
 			sphere.draw(radius, 32, 32);
 		} else {
 			glColor4f(.1f, .4f, .7f, 1f);
 			sphere.draw(radius, 32, 32);
 		}
 	}
 
 	private void fitLine() {
 		int n = current_line.size();
 		if (n < 2)
 			return;
 
 		DoubleBuffer line_points = BufferUtils.createDoubleBuffer(n * 3);
 		for (Pellet pellet : current_line) {
 			line_points.put(pellet.pos.x);
 			line_points.put(pellet.pos.y);
 			line_points.put(pellet.pos.z);
 		}
 
 		Pointer point_buffer = Native.getDirectBufferPointer(line_points);
 		DoubleBuffer output = LibPointCloud.fitLine(n, point_buffer)
 				.getByteBuffer(0, 6 * 8).asDoubleBuffer();
 
 		float f = findLineExtent();
 
 		Vector3f line_direction = new Vector3f();
 		line_direction.x = (float) output.get(0);
 		line_direction.y = (float) output.get(1);
 		line_direction.z = (float) output.get(2);
 		line_direction.normalise();
 
 		Vector3f center = new Vector3f();
 		center.x = (float) output.get(3);
 		center.y = (float) output.get(4);
 		center.z = (float) output.get(5);
 
 		List<Vector3f> line_pellets = new LinkedList<Vector3f>();
 		line_direction.scale(f);
 		Vector3f.add(center, line_direction, center);
 		line_pellets.add(new Vector3f(center));
 		line_direction.scale(-2);
 		Vector3f.add(center, line_direction, center);
 		line_pellets.add(new Vector3f(center));
 
 		if (Main.geometry_v.size() > 0 && current_line.size() > 2) {
 			Main.geometry_v.remove(Main.geometry_v.size() - 1);
 			System.out.println("removed some geometry");
 		}
 		PrimitiveVertex g = new PrimitiveVertex(GL_LINES, line_pellets, 3);
 		g.setLine(line_pellets.get(0), line_pellets.get(1));
 		Main.geometry_v.add(g);
 
 		checkForIntersections(line_pellets.get(0), line_pellets.get(1));
 	}
 
 	private float findLineExtent() {
 		float max_distance = 0;
 		Vector3f dist = new Vector3f();
 		for (Pellet a : current_line) {
 			for (Pellet b : current_line) {
 				Vector3f.sub(a.pos, b.pos, dist);
 				float d = dist.length();
 				if (d > max_distance)
 					max_distance = d;
 			}
 		}
 		return max_distance;
 	}
 
 	private void checkForIntersections(Vector3f p1, Vector3f p2) {
		intersection_points.clear();
 		System.out.println("checking for new line-plane interesction");
 		for (PrimitiveVertex geom : Main.geometry_v){
 			Vector3f intersect = geom.checkForIntersectionLineWithPlane(p1, p2);
 			if (intersect != null){
 				LinePellet i = new LinePellet(main_pellets);
 				i.alive = true;
 				i.constructing = true;
 				i.is_intersection = true;
 				i.pos.set(intersect);
 				i.radius = current_line.get(0).radius;
 
 				intersection_points.add(i);
 			}
 		}
 	}
 
 	private Vector3f findLineCenter() {
 		Vector3f center = new Vector3f();
 		for (Pellet p : current_line) {
 			Vector3f.add(p.pos, center, center);
 		}
 		center.scale(1f / current_line.size());
 		return center;
 	}
 
 	public static void startNewLine() {
 		for (LinePellet p : intersection_points){
 			ScaffoldPellet sp = new ScaffoldPellet(p);
 			Main.all_pellets_in_world.add(sp);
 		}
 		intersection_points.clear();
 		
 		current_line.clear();
 		System.out.println("making new line");
 	}
 }
