 package feynstein.collision;
 
 import feynstein.geometry.*;
 
 public class Collision {
 
     private int type;
     private double[] baryCoords;
     private double[] particles;
     private double dist;
 
     /**
        Creates a Collision object with the given data:
        @param typeConstant Collision.VERTEX_FACE or Collision.EDGE_EDGE,
        @param (b1, b2, b3)  barycentric coordinates
        @param (a, b, c, d) four particles defining either 
             [aPoint, faceVertex1, faceVertex2, faceVertex3] or
             [edge1start, edge2start, edge1end, edge2end]
        @param distance between colliding triangles
     */
        
     public Collision(int typeConstant, double bc1, double bc2, double bc3, int a, int b, int c, int d, double distance) {
 	baryCoords = new double[3];
 	baryCoords[0] = bc1;
 	baryCoords[1] = bc2;
 	baryCoords[2] = bc3;
 
 	//store particle indicies
 	particles = new double[4];
 	particles[0] = a;
 	particles[1] = b;
 	particles[2] = c;
 	particles[3] = d;
 	dist = distance;
     }
 
     public String toString() {
 	String s = "";
 	if (type == VERTEX_FACE) {
 	    s += "Collision type: vertex-face; ";
 	} else if (type == EDGE_EDGE) {
 	    s += "Collision type: edge-edge; ";
 	}
 	s += "between particles " + particles[0] + ", " + particles[1] + ", " + particles[2] + ", and " + particles[3] + "; ";
 	s += "at barycentric coords (" + baryCoords[0] + ", " + baryCoords[1] + ", " + baryCoords[2] + "); ";
 	s += "with distance = " + dist;
 	return s;
     }
 
     public static final int VERTEX_FACE = 0;
     public static final int EDGE_EDGE = 1;
 }
