 package feynstein.shapes;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 
 public class Cube extends Shape<Cube> {
     private double side_x, side_y, side_z;
 
     public Cube() {
 	objectType = "Cube";
     }
 
     public Cube set_sides(double x, double y, double z) {
 	side_x = x;
 	side_y = y;
 	side_z = z;
 	return this;
     }
 
     public Cube set_allSides(double side) {
 	side_x = side;
 	side_y = side;
 	side_z = side;
 	return this;
     }
 
    public Cube compile() {
 	/*
 	 * A cube is defined by 8 points; below, these are referred to
 	 * in terms of the "reference corner" (which is the point that the
 	 * user specifies as the cube's location). reference_x is
 	 * therefore the corner defined by adding the x-length vector to
 	 * reference, and reference_yz is the corner defined by adding
 	 * the y-length and z-length vectors to reference.
 	 */
 	Vector3d x_length = new Vector3d(side_x, 0, 0);
 	Vector3d y_length = new Vector3d(0, side_y, 0);
 	Vector3d z_length = new Vector3d(0, 0, side_z);
 
 	Particle reference = new Particle(location);
 
 	Particle reference_x = new Particle(location.plus(x_length));
 	Particle reference_y = new Particle(location.plus(y_length));
 	Particle reference_z = new Particle(location.plus(z_length));
 
 	Particle reference_xy = new Particle(location.plus(x_length).plus(y_length));
 	Particle reference_xz = new Particle(location.plus(x_length).plus(z_length));
 	Particle reference_yz = new Particle(location.plus(y_length).plus(z_length));
 
 	Particle reference_xyz = new Particle(location.plus(x_length)
 					      .plus(y_length).plus(z_length));
 
 	/*
 	 * Particle IDs are counted as if x, y and z combined to form
 	 * a binary number, where x is the MSB and z is the LSB.
 	 */
 	ArrayList<Particle> particles = new ArrayList<Particle>(
 	    Arrays.asList(new Particle[] {
 		reference, reference_z, reference_y, reference_yz, 
 		reference_x, reference_xz, reference_xy, reference_xyz}));
 
 	/*
 	 * Each particle has an edge to any other particle that exists
 	 * on the same cube edge that it does (i.e., reference_x and
 	 * reference_xz). Also, there are diagonals along each face.
 	 */
 	ArrayList<Edge> edges = new ArrayList<Edge>(
 	    Arrays.asList(new Edge[] {
 		new Edge(0,1), new Edge(0,2), new Edge(0,3), new Edge(0,4),
 		new Edge(0,5), new Edge(0,6), new Edge(1,3), new Edge(1,5),
 		new Edge(1,7), new Edge(2,3), new Edge(2,6), new Edge(2,7), 
 		new Edge(3,7), new Edge(4,5), new Edge(4,6), new Edge(4,7), 
 		new Edge(5,7), new Edge(6,7)}));
 
 	ArrayList<Triangle> triangles = new ArrayList<Triangle>(
 	    Arrays.asList(new Triangle[] {
 		new Triangle(0,1,3), new Triangle(0,1,5), new Triangle(0,2,3),
 		new Triangle(0,2,6), new Triangle(0,4,5), new Triangle(0,4,6),
 		new Triangle(1,3,7), new Triangle(1,5,7), new Triangle(2,3,7),
 		new Triangle(2,6,7), new Triangle(4,5,7), new Triangle(4,6,7)}));
 
 	localMesh = new Mesh(particles, edges, triangles);
 	return this;
     }
 }
