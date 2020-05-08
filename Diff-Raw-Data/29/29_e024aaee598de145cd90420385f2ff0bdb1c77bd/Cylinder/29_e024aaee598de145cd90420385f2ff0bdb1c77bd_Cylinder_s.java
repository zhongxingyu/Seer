 package feynstein.shapes;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /* TODO haldean: support two-radius cylinders */
 
 public class Cylinder extends Shape<Cylinder> {
     private double radius1, radius2, height;
 
     /*
      * The circle_verts variable controls how many points are placed
      * along the edge of each cap in the cylinder. The higher the
      * circle_vert value, the more the cylinder will look like a real
      * cylinder and the less it will look like a polygonal prism.
      */
     private int circle_verts = 40;
 
     public Cylinder() {
 	objectType = "Cylinder";
     }
     
     public Cylinder set_radius(double r) {
 	radius1 = r;
 	radius2 = r;
 	return this;
     }
 
     public Cylinder set_radius1(double r1) {
 	radius1 = r1;
 	return this;
     }
 
     public Cylinder set_radius2(double r2) {
 	radius2 = r2;
 	return this;
     }
 
     public Cylinder set_height(double h) {
 	height = h;
 	return this;
     }
 
     public Cylinder set_accuracy(int verts) {
 	circle_verts = verts;
 	return this;
     }
 
    public Cylinder compile() {
 	ArrayList<Particle> particles = new ArrayList<Particle>();
 	ArrayList<Edge> edges = new ArrayList<Edge>();
 	ArrayList<Triangle> triangles = new ArrayList<Triangle>();
 
 	List<Vector3d> bottom_ring = new ArrayList<Vector3d>();
 
 	Vector3d point;
 	double theta = 2 * Math.PI / (double) circle_verts;
 	
 	/* Generate the bottom cap. */
 	Vector3d bottom_center = location.plus(new Vector3d(radius1, radius1, 0));
 	particles.add(new Particle(bottom_center));
 
 	for (int i=0; i<circle_verts; i++) {
 	    point = bottom_center.plus(new Vector3d(radius1 * Math.cos(theta * i),
 						    radius1 * Math.sin(theta * i), 0));
 	    int index = particles.size();
 	    particles.add(new Particle(point));
 	    bottom_ring.add(point);
 
 	    /* Add an edge to the center point. */
 	    edges.add(new Edge(0, index));
 
 	    if (i > 0) {
 		/* Add an edge to it's previous neighbor. */
 		edges.add(new Edge(index-1, index));
 		
 		/* Create a triangle between it, its previous neighbor
 		 * and the center. */
 		triangles.add(new Triangle(0, index-1, index));
 	    }
 	}
 
 	/* Connect the last point with the first point. */
 	edges.add(new Edge(1, circle_verts));
 	triangles.add(new Triangle(0, 1, circle_verts));
 
 	/* Generate the top cap. */
 	Vector3d height_vector = new Vector3d(0, 0, height),
 	    top_center = bottom_center.plus(height_vector);
 
 	/* Generate the center point on the top cap. */
 	int top_index = -1, bottom_index, top_center_index = particles.size(),
 	    last_bottom_index = particles.size() - 1;
 	particles.add(new Particle(top_center));
 
 	/* Add corresponding particles for each particle in the bottom
 	 * ring, and create faces out of the rectangles defined by two
 	 * corresponding pairs of adjacent points on the caps. */
 	for (bottom_index = 0; bottom_index < top_center_index-1; bottom_index++) {
 	    top_index = particles.size();
 	    point = top_center.plus(new Vector3d(radius2 * Math.cos(theta * bottom_index),
 						 radius2 * Math.sin(theta * bottom_index), 0));
 
 	    /* Add the particle and its edge to the corresponding
 	     * particle in the bottom cap. */
 	    particles.add(new Particle(point));
 	    edges.add(new Edge(bottom_index, top_index));
 
 	    if (bottom_index > 0) {
 		/* Create the edge and triangle with its neighbor. */
 		edges.add(new Edge(top_index-1, top_index));
 		triangles.add(new Triangle(top_center_index, top_index-1, top_index));
 
 		/* Create the face defined by this point, it's
 		 * neighbor and the corresponding points in the bottom
 		 * cap. */
 		if (bottom_index > 1) {
 		    edges.add(new Edge(top_index, bottom_index-1));
 		    triangles.add(new Triangle(bottom_index-1, top_index-1, top_index));
 		    triangles.add(new Triangle(bottom_index-1, bottom_index, top_index));
 		}
 	    } else {
 		/* Create a triangle between the first point in
 		 * the top and the first and last point in the
 		 * bottom. */
 		edges.add(new Edge(last_bottom_index, top_index));
 		triangles.add(new Triangle(1, last_bottom_index, top_index));
 	    }
 	}
 
 	/* Finish the last face with a triangle between the first and
 	 * last points in the top and the last point in the bottom. */
 	edges.add(new Edge(top_center_index+1, top_index));
 	triangles.add(new Triangle(last_bottom_index, top_center_index+1, top_index));
 
 	localMesh = new Mesh(particles, edges, triangles);
 	return this;
     }
 }
