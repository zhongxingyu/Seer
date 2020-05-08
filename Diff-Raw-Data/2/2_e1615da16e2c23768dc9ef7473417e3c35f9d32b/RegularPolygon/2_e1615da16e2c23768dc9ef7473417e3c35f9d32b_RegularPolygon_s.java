 package feynstein.shapes;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 
 public class RegularPolygon extends Shape<RegularPolygon> {
     private int verteces;
     private double radius;
 
     public RegularPolygon() {
 	objectType = "RegularPolygon";
     }
 
     public RegularPolygon set_verteces(int verts) {
 	verteces = verts;
 	return this;
     }
 
     public RegularPolygon set_radius(double r) {
 	radius = r;
 	return this;
     }
 
     public RegularPolygon compileShape() {
 	ArrayList<Particle> particles = new ArrayList<Particle>();
 	ArrayList<Edge> edges = new ArrayList<Edge>();
 	ArrayList<Triangle> triangles = new ArrayList<Triangle>();
 
 	/* The center is the location plus the radius in the X and Y
 	 * direction. */
 	Vector3d point, center = location.plus(new Vector3d(radius, radius, 0));
 	particles.add(new Particle(center));
 
 	double theta = 2 * Math.PI / (double) verteces;
 	int index = -1;
 
 	/* Sweep through the polygon and add the verteces. */
 	for (int i=0; i<verteces; i++) {
 	    index = particles.size();
 	    point = center.plus(new Vector3d(radius * Math.cos(theta * i),
 					     radius * Math.sin(theta * i), 0));
 	    particles.add(new Particle(point));
 	    if (i > 0) {
 		edges.add(new Edge(index-1, index));
 		triangles.add(new Triangle(0, index-1, index));
 	    }
 	}
 	
 	edges.add(new Edge(1, index));
 	triangles.add(new Triangle(0, 1, index));
 
 	localMesh = new Mesh(particles, edges, triangles);
 	return this;
     }
 }
