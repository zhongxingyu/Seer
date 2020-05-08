 package feynstein.shapes;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 
 public class Tetrahedron extends Shape<Tetrahedron> {
     private Vector3d point1, point2, point3, point4;
 
     public Tetrahedron() {
 	objectType = "Tetrahedron";
     }
 
     public Tetrahedron set_point1(double x, double y, double z) {
 	point1 = location.plus(new Vector3d(x, y, z));
 	return this;
     }
 
     public Tetrahedron set_point2(double x, double y, double z) {
 	point2 = location.plus(new Vector3d(x, y, z));
 	return this;
     }
 
     public Tetrahedron set_point3(double x, double y, double z) {
 	point3 = location.plus(new Vector3d(x, y, z));
 	return this;
     }
 
     public Tetrahedron set_point4(double x, double y, double z) {
 	point4 = location.plus(new Vector3d(x, y, z));
 	return this;
     }
 
    public Tetrahedron compile() {
 	/* The particles are just the four points. */
 	ArrayList<Particle> particles = new ArrayList<Particle>(
 	    Arrays.asList(new Particle[] {
 		new Particle(point1), new Particle(point2), 
 		new Particle(point3), new Particle(point4)}));
 
 	/* Edges exist between every pair of particles. */
 	ArrayList<Edge> edges = new ArrayList<Edge>();
 	for (int i=0; i<4; i++) {
 	    for (int j=0; j<4; j++) {
 		if (i != j) edges.add(new Edge(i,j));
 	    }
 	}
 
 	/* Four triangles (all possible combinations of four take three) */
 	ArrayList<Triangle> triangles = new ArrayList<Triangle>(
 	    Arrays.asList(new Triangle[] {
 		new Triangle(0,1,2), new Triangle(0,1,3),
 		new Triangle(0,2,3), new Triangle(1,2,3)}));
 
 	localMesh = new Mesh(particles, edges, triangles);
 	return this;
     }
 }
