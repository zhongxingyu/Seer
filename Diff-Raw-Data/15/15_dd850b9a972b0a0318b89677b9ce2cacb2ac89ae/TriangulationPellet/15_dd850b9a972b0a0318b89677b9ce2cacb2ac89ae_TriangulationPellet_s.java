 package edu.washington.cs.games.ktuite.pointcraft;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import megamu.mesh.Delaunay;
 
 import org.lwjgl.util.vector.Vector3f;
 import static org.lwjgl.opengl.GL11.*;
 
 public class TriangulationPellet extends Pellet {
 
 	public static class TriEdge {
 		Primitive graphic_line;
 		Pellet start;
 		Pellet end;
 		boolean fringe;
 
 		public TriEdge(Pellet v1, Pellet v2) {
 			LinkedList<Pellet> pellets = new LinkedList<Pellet>();
 			pellets.add(v1);
 			pellets.add(v2);
 			graphic_line = new Primitive(GL_LINES, pellets, 3);
 
 			fringe = true;
 
 			start = v1;
 			end = v2;
 		}
 	}
 
 	public static Stack<Pellet> current_vertices = new Stack<Pellet>();
 	public static List<TriEdge> current_edges = new LinkedList<TriEdge>();
 	public static Stack<Primitive> edges_to_display = new Stack<Primitive>();
 
 	private static Vector3f basis1 = new Vector3f();
 	private static Vector3f basis2 = new Vector3f();
 	private static boolean[][] edges = null;
 
 	/*
 	 * A Pellet is a magical thing that you can shoot out of a gun that will
 	 * travel towards the model and stick to the first point it intersects.
 	 * 
 	 * Soon it will even stick to other pellets.
 	 */
 	public TriangulationPellet() {
 		super();
 		pellet_type = Main.GunMode.TRIANGULATION;
 	}
 
 	public TriangulationPellet(Pellet lp) {
 		super();
 		pos.set(lp.pos);
 		radius = lp.radius;
 		max_radius = lp.max_radius;
 		constructing = lp.constructing;
 		pellet_type = Main.GunMode.TRIANGULATION;
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
 					addNewPelletToTriMesh(neighbor_pellet);
 					neighbor_pellet.ref_count++;
 					// TODO: implement this
 				} else if (closest_point != null) {
 					// TODO: implement this
 					pos.set(closest_point);
 					addNewPelletToTriMesh(this);
 				} else if (Main.draw_points) {
 					// if it's not dead yet and also didn't hit a
 					// neighboring pellet, look for nearby points in model
 					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);
 
 					// is it near some points?!
 					if (neighbors > 0) {
 						constructing = true;
 						setInPlace();
 						snapToCenterOfPoints();
 
 						addNewPelletToTriMesh(this);
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
 
 	private void addNewPelletToTriMesh(Pellet p) {
 		// add new this new tri pellet to list of vertices
 		current_vertices.add(p);
 		ActionTracker.newTriangleMeshPellet(p);
 
 		// fit plane to viewing direction
 		if (current_vertices.size() == 1)
 			computeSpanningVectors();
 
 		computeTriangulation();
 
 	}
 
 	public static void computeTriangulation() {
 
 		// do delaunay triangulation (projecting points into viewing plane)
 		float[][] points = new float[current_vertices.size()][2];
 		for (int i = 0; i < current_vertices.size(); i++) {
 			// use other x's and y's with other type of plane
 			Vector3f pt = current_vertices.get(i).pos;
 
 			points[i][0] = Vector3f.dot(pt, basis1);
 			points[i][1] = Vector3f.dot(pt, basis2);
 		}
 
 		Delaunay myDelaunay = new Delaunay(points);
 
 		// form edges
 		current_edges.clear();
 		edges_to_display.clear();
 		int[][] myLinks = myDelaunay.getLinks();
 
 		int n = current_vertices.size();
 		edges = new boolean[n][n];
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				edges[i][j] = false;
 			}
 		}
 
 		for (int i = 0; i < myLinks.length; i++) {
 			int startIndex = myLinks[i][0];
 			int endIndex = myLinks[i][1];
 
 			TriEdge edge = new TriEdge(current_vertices.get(startIndex),
 					current_vertices.get(endIndex));
 			edges_to_display.add(edge.graphic_line);
 
 			edges[startIndex][endIndex] = true;
 			edges[endIndex][startIndex] = true;
 		}
 	}
 
 	private static void computeSpanningVectors() {
 		basis1.set((float) Math.cos(Main.pan_angle), 0,
				(float) Math.sin(Main.pan_angle));
 		Vector3f.cross(basis1, Main.gun_direction, basis2);
 
 	}
 
 	@SuppressWarnings("unused")
 	private void connectToAllOtherVertices() {
 		Pellet vert = this;
 		for (Pellet other_vert : current_vertices) {
 			TriEdge edge = new TriEdge(vert, other_vert);
 			// TODO: check that the edge is acceptable to add
 			current_edges.add(edge);
 			Main.geometry.add(edge.graphic_line);
 		}
 		current_vertices.add(vert);
 		Main.new_pellets_to_add_to_world.add(vert);
 	}
 
 	public void coloredDraw() {
 		if (constructing) {
 			float alpha = 1 - radius / max_radius * .2f;
 			glColor4f(.1f, .7f, .4f, alpha);
 			drawSphere(radius);
 		} else {
 			glColor4f(.1f, .7f, .4f, 1f);
 			drawSphere(radius);
 		}
 	}
 
 	public static void startNewTriMesh() {
 		makeTriangles();
 		edges_to_display.clear();
 		current_edges.clear();
 		current_vertices.clear();
 		System.out.println("making new tri mesh");
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void makeTriangles() {
 		if (edges == null)
 			return;
 		
 		ActionTracker.newTriangulation((Stack<Pellet>) current_vertices.clone(), (Stack<Primitive>)edges_to_display.clone());
 
 
 		int n = current_vertices.size();
 
 		for (int i = 0; i < n; i++) {
 			for (int j = i + 1; j < n; j++) {
 				if (edges[i][j]) {
 					for (int k = j + 1; k < n; k++) {
 						if (edges[i][k] && edges[j][k]) {
 							makeSingleTriangle(i, j, k);
 
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public static void makeSingleTriangle(int i, int j, int k) {
 		List<Pellet> tri_pellets = new LinkedList<Pellet>();
 		tri_pellets.add(current_vertices.get(i));
 		tri_pellets.add(current_vertices.get(j));
 		tri_pellets.add(current_vertices.get(k));
 		tri_pellets.add(current_vertices.get(i));
 		Primitive triangle = new Primitive(GL_POLYGON, tri_pellets);
 		PlaneScaffold plane = new PlaneScaffold();
 		for (int l = 0; l < 3; l++)
 			plane.pellets.add(tri_pellets.get(l));
 		plane.fitPlane();
 		triangle.setPlane(plane);
 		triangle.setPlayerPositionAndViewingDirection(Main.pos,
 				Main.gun_direction);
 		Main.geometry.add(triangle);
 		ActionTracker.newPolygon(triangle, null, null);
 		System.out.println("Triangle: " + i + "," + j + "," + k);
 	}
 
 	public void delete() {
 		// TODO: fill this in
 	}
 }
