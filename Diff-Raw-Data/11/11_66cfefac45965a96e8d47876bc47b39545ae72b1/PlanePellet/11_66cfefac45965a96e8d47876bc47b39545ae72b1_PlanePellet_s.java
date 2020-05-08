 package edu.washington.cs.games.ktuite.pointcraft;
 
 import java.nio.DoubleBuffer;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class PlanePellet extends Pellet {
 
 	public static List<PlanePellet> current_plane = new LinkedList<PlanePellet>();
 
 	// temporary holder of intersection pellets
 	public static List<PlanePellet> intersection_points = new LinkedList<PlanePellet>();
 
 	private boolean is_intersection = false;
 	
 	/*
 	 * A Pellet is a magical thing that you can shoot out of a gun that will
 	 * travel towards the model and stick to the first point it intersects.
 	 * 
 	 * Soon it will even stick to other pellets.
 	 */
 	public PlanePellet(List<Pellet> _pellets) {
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
 					current_plane.add(this);
 					fitPlane();
 				} else if (closest_point != null) {
 					System.out.println("pellet stuck to some geometry");
 					constructing = true;
 					pos.set(closest_point);
 					current_plane.add(this);
 					fitPlane();
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
 						current_plane.add(this);
 						fitPlane();
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
 		if (current_plane.size() == 3 || current_plane.size() == 4)
 			Main.geometry_v.remove(Main.geometry_v.size() - 1);
 		if (current_plane.contains(this)) {
 			current_plane.remove(this);
 			fitPlane();
 		}
 	}
 
 	public void draw() {
 		if (is_intersection) {
 			float alpha = 1 - radius / max_radius * .2f;
 			glColor4f(.1f, .8f, .3f, alpha);
 			sphere.draw(radius, 32, 32);
 		} else if (constructing) {
 			float alpha = 1 - radius / max_radius * .2f;
 			glColor4f(.2f, .2f, .7f, alpha);
 			sphere.draw(radius, 32, 32);
 		} else {
 			glColor4f(.2f, .2f, .7f, 1f);
 			sphere.draw(radius, 32, 32);
 		}
 	}
 
 	private void fitPlane() {
 		int n = current_plane.size();
 		if (n < 3)
 			return;
 
 		DoubleBuffer plane_points = BufferUtils.createDoubleBuffer(n * 3);
 		for (Pellet pellet : current_plane) {
 			plane_points.put(pellet.pos.x);
 			plane_points.put(pellet.pos.y);
 			plane_points.put(pellet.pos.z);
 		}
 
 		Pointer point_buffer = Native.getDirectBufferPointer(plane_points);
 		DoubleBuffer output = LibPointCloud.fitPlane(n, point_buffer)
 				.getByteBuffer(0, 4 * 8).asDoubleBuffer();
 
 		float a = (float) output.get(0);
 		float b = (float) output.get(1);
 		float c = (float) output.get(2);
 		float d = (float) output.get(3);
 
 		float pts[] = new float[12];
 		float f = findPlaneExtent();
 
 		Vector3f center = findPlaneCenter();
 
 		if (Math.abs(a) > Math.abs(b) && Math.abs(a) > Math.abs(c)) {
 			// set y and z
 			pts[0 * 3 + 1] = 1 * f + center.y;
 			pts[0 * 3 + 2] = 1 * f + center.z;
 			pts[0 * 3 + 0] = -1 * (pts[0 * 3 + 1] * b + pts[0 * 3 + 2] * c + d)
 					/ a;
 
 			pts[1 * 3 + 1] = 1 * f + center.y;
 			pts[1 * 3 + 2] = -1 * f + center.z;
 			pts[1 * 3 + 0] = -1 * (pts[1 * 3 + 1] * b + pts[1 * 3 + 2] * c + d)
 					/ a;
 
 			pts[2 * 3 + 1] = -1 * f + center.y;
 			pts[2 * 3 + 2] = -1 * f + center.z;
 			pts[2 * 3 + 0] = -1 * (pts[2 * 3 + 1] * b + pts[2 * 3 + 2] * c + d)
 					/ a;
 
 			pts[3 * 3 + 1] = -1 * f + center.y;
 			pts[3 * 3 + 2] = 1 * f + center.z;
 			pts[3 * 3 + 0] = -1 * (pts[3 * 3 + 1] * b + pts[3 * 3 + 2] * c + d)
 					/ a;
 		} else if (Math.abs(b) > Math.abs(a) && Math.abs(b) > Math.abs(c)) {
 			// horizontal plane! set x and z
 			pts[0 * 3 + 0] = 1 * f + center.x;
 			pts[0 * 3 + 2] = 1 * f + center.z;
 			pts[0 * 3 + 1] = -1 * (pts[0 * 3 + 0] * a + pts[0 * 3 + 2] * c + d)
 					/ b;
 
 			pts[1 * 3 + 0] = 1 * f + center.x;
 			pts[1 * 3 + 2] = -1 * f + center.z;
 			pts[1 * 3 + 1] = -1 * (pts[1 * 3 + 0] * a + pts[1 * 3 + 2] * c + d)
 					/ b;
 
 			pts[2 * 3 + 0] = -1 * f + center.x;
 			pts[2 * 3 + 2] = -1 * f + center.z;
 			pts[2 * 3 + 1] = -1 * (pts[2 * 3 + 0] * a + pts[2 * 3 + 2] * c + d)
 					/ b;
 
 			pts[3 * 3 + 0] = -1 * f + center.x;
 			pts[3 * 3 + 2] = 1 * f + center.z;
 			pts[3 * 3 + 1] = -1 * (pts[3 * 3 + 0] * a + pts[3 * 3 + 2] * c + d)
 					/ b;
 		} else {
 			// set x and y
 			pts[0 * 3 + 0] = 1 * f + center.x;
 			pts[0 * 3 + 1] = 1 * f + center.y;
 			pts[0 * 3 + 2] = -1 * (pts[0 * 3 + 0] * a + pts[0 * 3 + 1] * b + d)
 					/ c;
 
 			pts[1 * 3 + 0] = 1 * f + center.x;
 			pts[1 * 3 + 1] = -1 * f + center.y;
 			pts[1 * 3 + 2] = -1 * (pts[1 * 3 + 0] * a + pts[1 * 3 + 1] * b + d)
 					/ c;
 
 			pts[2 * 3 + 0] = -1 * f + center.x;
 			pts[2 * 3 + 1] = -1 * f + center.y;
 			pts[2 * 3 + 2] = -1 * (pts[2 * 3 + 0] * a + pts[2 * 3 + 1] * b + d)
 					/ c;
 
 			pts[3 * 3 + 0] = -1 * f + center.x;
 			pts[3 * 3 + 1] = 1 * f + center.y;
 			pts[3 * 3 + 2] = -1 * (pts[3 * 3 + 0] * a + pts[3 * 3 + 1] * b + d)
 					/ c;
 		}
 
 		List<Vector3f> boundary_pellets = new LinkedList<Vector3f>();
 
 		float grid = 40;
 		for (int i = 0; i <= grid; i++) {
 			Vector3f begin = new Vector3f();
 			Vector3f end = new Vector3f();
 			begin.x = pts[0 * 3 + 0] * i / grid + pts[1 * 3 + 0]
 					* (1 - i / grid);
 			begin.y = pts[0 * 3 + 1] * i / grid + pts[1 * 3 + 1]
 					* (1 - i / grid);
 			begin.z = pts[0 * 3 + 2] * i / grid + pts[1 * 3 + 2]
 					* (1 - i / grid);
 			end.x = pts[3 * 3 + 0] * i / grid + pts[2 * 3 + 0] * (1 - i / grid);
 			end.y = pts[3 * 3 + 1] * i / grid + pts[2 * 3 + 1] * (1 - i / grid);
 			end.z = pts[3 * 3 + 2] * i / grid + pts[2 * 3 + 2] * (1 - i / grid);
 			boundary_pellets.add(begin);
 			boundary_pellets.add(end);
 		}
 		for (int i = 0; i <= grid; i++) {
 			Vector3f begin = new Vector3f();
 			Vector3f end = new Vector3f();
 			begin.x = pts[0 * 3 + 0] * i / grid + pts[3 * 3 + 0]
 					* (1 - i / grid);
 			begin.y = pts[0 * 3 + 1] * i / grid + pts[3 * 3 + 1]
 					* (1 - i / grid);
 			begin.z = pts[0 * 3 + 2] * i / grid + pts[3 * 3 + 2]
 					* (1 - i / grid);
 			end.x = pts[1 * 3 + 0] * i / grid + pts[2 * 3 + 0] * (1 - i / grid);
 			end.y = pts[1 * 3 + 1] * i / grid + pts[2 * 3 + 1] * (1 - i / grid);
 			end.z = pts[1 * 3 + 2] * i / grid + pts[2 * 3 + 2] * (1 - i / grid);
 			boundary_pellets.add(begin);
 			boundary_pellets.add(end);
 		}
 
 		if (Main.geometry_v.size() > 0 && current_plane.size() > 3) {
 			Main.geometry_v.remove(Main.geometry_v.size() - 1);
 			System.out.println("removed some geometry");
 		}
 		PrimitiveVertex g = new PrimitiveVertex(GL_LINES, boundary_pellets, 1);
 		g.setPlane(a, b, c, d);
 		Main.geometry_v.add(g);
 		
 		checkForIntersections(a, b, c, d);
 
 	}
 
 	private float findPlaneExtent() {
 		float max_distance = 0;
 		Vector3f dist = new Vector3f();
 		for (Pellet a : current_plane) {
 			for (Pellet b : current_plane) {
 				Vector3f.sub(a.pos, b.pos, dist);
 				float d = dist.length();
 				if (d > max_distance)
 					max_distance = d;
 			}
 		}
 		return max_distance;
 	}
 	
 	private Vector3f findPlaneCenter() {
 		Vector3f center = new Vector3f();
 		for (Pellet p : current_plane) {
 			Vector3f.add(p.pos, center, center);
 		}
 		center.scale(1f / current_plane.size());
 		return center;
 	}
 
 	private void checkForIntersections(float a, float b, float c, float d){
 		System.out.println("checking for new plane-line interesction");
 		for (PrimitiveVertex geom : Main.geometry_v){
 			Vector3f intersect = geom.checkForIntersectionPlaneWithLine(a,b,c,d);
 			if (intersect != null){
 				PlanePellet i = new PlanePellet(main_pellets);
 				i.alive = true;
 				i.constructing = true;
 				i.is_intersection = true;
 				i.pos.set(intersect);
 				i.radius = current_plane.get(0).radius;
 
 				intersection_points.add(i);
				break;
 			}
 		}
 	}
 	
 	public static void startNewPlane() {
 		for (PlanePellet p : intersection_points){
 			ScaffoldPellet sp = new ScaffoldPellet(p);
 			Main.all_pellets_in_world.add(sp);
 		}
 		intersection_points.clear();
 		
 		current_plane.clear();
 		System.out.println("making new plane");
 	}
 }
