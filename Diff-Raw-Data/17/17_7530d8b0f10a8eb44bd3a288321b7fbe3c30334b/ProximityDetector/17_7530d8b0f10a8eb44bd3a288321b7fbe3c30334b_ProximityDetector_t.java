 package feynstein.properties.collision;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 import feynstein.*;
 import feynstein.properties.*;
 import java.util.*;
 
 public class ProximityDetector extends NarrowPhaseDetector<ProximityDetector> {
 
     private double proximity = 0;   
 
     public ProximityDetector(Scene aScene) {
 	super(aScene);
     }
 
     public ProximityDetector set_proximity(double p) {
 	proximity = p;
 	return this;
     }
     
     public HashSet<Collision> checkCollision(TrianglePair p, double[] X, 
 											 double[] V, HashSet<Collision> cSet) {
 	return checkCollision(p.t1, p.t2, X, V, cSet);
     }
 
     public HashSet<Collision> checkCollision(Triangle t1, Triangle t2, double[] X, double[] V, HashSet<Collision> cSet) {
 
 	// make sure triangles are not connected:
 	if (t1.getIdx(0) == t2.getIdx(0) || t1.getIdx(0) == t2.getIdx(1) 
 	    || t1.getIdx(0) == t2.getIdx(2) || t1.getIdx(1) == t2.getIdx(0) 
 	    || t1.getIdx(1) == t2.getIdx(1) || t1.getIdx(1) == t2.getIdx(2) 
	    || t1.getIdx(2) == t2.getIdx(0) || t1.getIdx(2) == t2.getIdx(1) 
 	    || t1.getIdx(2) == t2.getIdx(2)) {
 	    return cSet; //can't hit self
 	}
 	
 	double[] a = new double[3];
 	double[] b = new double[3];
 	double[] c = new double[3];
 	double[] p = new double[3];
 
 	// check all 6 vertex-face collisions:
 	for (int i = 0; i < 6; i++) {
 	    //the triangle with which the vertex is colliding
 	    Triangle t;
 	    //the colliding vertex
 	    int vertex = -1;
 	    if (i < 3) {
 		t = t2;
 		if (i == 0) vertex = t1.getIdx(0);
 		if (i == 1) vertex = t1.getIdx(1);
 		if (i == 2) vertex = t1.getIdx(2);
 	    } else {
 		t = t1;
 		if(i == 3) vertex = t2.getIdx(0);
 		if(i == 4) vertex = t2.getIdx(1);
 		if(i == 5) vertex = t2.getIdx(2);
 	    }
 
 	    p[0] = X[3*vertex];
 	    p[1] = X[3*vertex + 1];
 	    p[2] = X[3*vertex + 2];
 	    a[0] = X[3*t.getIdx(0)];	
 	    a[1] = X[3*t.getIdx(0) + 1];
 	    a[2] = X[3*t.getIdx(0) + 2];
 	    b[0] = X[3*t.getIdx(1)];
 	    b[1] = X[3*t.getIdx(1) + 1];
 	    b[2] = X[3*t.getIdx(1) + 2];
 	    c[0] = X[3*t.getIdx(2)];
 	    c[1] = X[3*t.getIdx(2) + 1];
 	    c[2] = X[3*t.getIdx(2) + 2];
 
 	    //barycentric coordinates
 	    double u = -1, v = -1, w = -1;
 
 	    //vertex-face distance
 	    double[] distAndCoords = DistanceFinder.vertexFaceDistance(p, a, b, c, u, v, w);
 	    double distance = Math.sqrt(distAndCoords[0]);
 	    u = distAndCoords[1];
 	    v = distAndCoords[2];
 	    w = distAndCoords[3];
 
 	    //if triangles are within proximity
 	    if(distance <= 2 * proximity + .000001) {
 		double[] xa = new double[3]; //(was Vector3d xa, xb)
 		double[] xb = new double[3];
 		xa[0] = p[0];
 		xa[1] = p[1];
 		xa[2] = p[2];
 		xb[0] = u*a[0]+v*b[0]+w*c[0];
 		xb[1] = u*a[1]+v*b[1]+w*c[1];
 		xb[2] = u*a[2]+v*b[2]+w*c[2];
 
 		cSet.add(new Collision(Collision.VERTEX_FACE, u, v, w, vertex, t.getIdx(0), t.getIdx(1), t.getIdx(2), distance));
 	    }
 	}
 	
 	double[] p1 = new double[3];
 	double[] q1 = new double[3];
 	double[] p2 = new double[3];
 	double[] q2 = new double[3];
 
 	//check all 9 edge-edge collisions
 	for(int i = 0; i < 9; i++) {
 	    //edge indices (so we don't account for the same collision twice)
 	    int p_1 = -1, q_1 = -1, p_2 = -1, q_2 = -1;
 
 	    //check all possible edge combinations
 	    if (i < 3) {
 		p_1 = t1.getIdx(0); 
 		q_1 = t1.getIdx(1); 
 		if (i == 0) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(1); }
 		if (i == 1) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(2); }
 		if (i == 2) { p_2 = t2.getIdx(1); q_2 = t2.getIdx(2); }
 	    } else if (i < 6) {
 		p_1 = t1.getIdx(0); 
 		q_1 = t1.getIdx(2);
 		if (i == 3) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(1); }
 		if (i == 4) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(2); }
 		if (i == 5) { p_2 = t2.getIdx(1); q_2 = t2.getIdx(2); }
 	    } else if (i < 9) {
 		p_1 = t1.getIdx(1);
 		q_1 = t1.getIdx(2);
 		if (i == 6) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(1); }
 		if (i == 7) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(2); }
 		if (i == 8) { p_2 = t2.getIdx(1); q_2 = t2.getIdx(2); }
 	    }
 
 	    //get edge positions
 	    p1[0] = X[3*p_1];
 	    p1[1] = X[3*p_1+1];
 	    p1[2] = X[3*p_1+2];
 	    q1[0] = X[3*q_1];	
 	    q1[1] = X[3*q_1+1];
 	    q1[2] = X[3*q_1+2];
 	    p2[0] = X[3*p_2];
 	    p2[1] = X[3*p_2+1];
 	    p2[2] = X[3*p_2+2];
 	    q2[0] = X[3*q_2];
 	    q2[1] = X[3*q_2+1];
 	    q2[2] = X[3*q_2+2];
 
 	    //barycentric coordinates
 	    double s = -1, t = -1; 
 
 	    //collision
 	    double[] distAndCoords = DistanceFinder.edgeEdgeDistance(p1, q1, p2, q2, s, t);
 	    double distance = Math.sqrt(distAndCoords[0]);
 	    s = distAndCoords[1];
 	    t = distAndCoords[2];
 
 	    //edges within proximity
 	    if(distance <= 2 * proximity + .000001) {
 		cSet.add(new Collision(Collision.EDGE_EDGE, 1.0 - s, 1.0 - t, 0.0, p_1, q_1, p_2, q_2, distance));
 	    }
 	}
 	return cSet;
     }
 }
