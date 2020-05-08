  package feynstein.properties.collision;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 import feynstein.*;
 import feynstein.properties.*;
 import java.util.*;
 
 import com.numericalmethod.suanshu.analysis.function.polynomial.root.*;
 import com.numericalmethod.suanshu.analysis.function.polynomial.*;
 import com.numericalmethod.suanshu.datastructure.list.*;
 import com.numericalmethod.suanshu.license.*;
 import org.joda.time.*;
 import java.lang.Number;
 
 public class ContinuousTimeDetector extends NarrowPhaseDetector<ContinuousTimeDetector> {
 
     Cubic cubic;
     double[] op;
     double[] time;
     double[] zeroi;
     double[] a;
     double[] b;
     double[] c;
     double[] p;
     double[] p1;
     double[] q1;
     double[] p2;
     double[] q2;
 
     private double h = 0;      
 
     public ContinuousTimeDetector(Scene aScene) {
 	super(aScene);
 	cubic = new Cubic();
 	op = new double[4];
 	time = new double[3];
 	zeroi = new double[3];
 	a = new double[3];
 	b = new double[3];
 	c = new double[3];
 	p = new double[3];
 	p1 = new double[3];
 	q1 = new double[3];
 	p2 = new double[3];
 	q2 = new double[3];
     }
 
     public ContinuousTimeDetector set_stepSize(double step) {
 	h = step;
 	return this;
     }
     
     public HashSet<Collision> checkCollision(TrianglePair p, HashSet<Collision> cSet) {
 	return checkCollision(p.t1, p.t2, cSet);
     }
 
     public HashSet<Collision> checkCollision(Triangle t1, Triangle t2, HashSet<Collision> cSet) {
 	double[] X = scene.getGlobalPositions();
 	double[] V = scene.getGlobalVelocities();
 
 	if (t1.getIdx(0) == t2.getIdx(0) || t1.getIdx(0) == t2.getIdx(1) 
 	    || t1.getIdx(0) == t2.getIdx(2) || t1.getIdx(1) == t2.getIdx(0) 
 	    || t1.getIdx(1) == t2.getIdx(1) || t1.getIdx(1) == t2.getIdx(2) 
 	    || t1.getIdx(2) == t2.getIdx(1) || t1.getIdx(2) == t2.getIdx(1) 
 	    || t1.getIdx(2) == t2.getIdx(2)) {
 	    return cSet; // because you can't hit yourself
 	}
 	boolean collision = false;
 	double hit_t = -1;
 	double u = -1, v = -1, w = -1, s = -1, t = -1;
 	double vx0, vy0, vz0, vx1, vy1, vz1, vx2, vy2, vz2, vx3, vy3, vz3;
 	//degrees of freedom
 	int n = X.length/3;
 	//check all 15 possible collisions
 	for (int i = 0; i < 15; i++) {
 	    boolean confirmed = false;
 	    //the triangle with which the vertex is colliding
 	    Triangle tri = null;
 	    //the colliding vertex index
 	    int vertex = -1;
 	    //collding edge indicies
 	    int p_1 = -1, q_1 = -1, p_2 = -1, q_2 = -1;
 	    double x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3;
 	    //vertex face
 	    if (i < 6) {
 		if (i < 3) {
 		    tri = t1;
 		    if(i == 0) vertex = t2.getIdx(0);
 		    if(i == 1) vertex = t2.getIdx(1);
 		    if(i == 2) vertex = t2.getIdx(2);
 		}
 		else {
 		    tri = t2;
 		    if(i == 3) vertex = t1.getIdx(0);
 		    if(i == 4) vertex = t1.getIdx(1);
 		    if(i == 5) vertex = t1.getIdx(2);
 		}
 			
 				
 		//vertex position and velocity
 		x3  = X[3*vertex];
 		y3 = X[3*vertex+1];
 		z3 = X[3*vertex+2];
 		vx3 = V[3*vertex];
 		vy3 = V[3*vertex+1];
 		vz3 = V[3*vertex+2];
 		//triangle vertex positions and velocities
 		x0 = X[3*tri.getIdx(0)];
 		y0 = X[3*tri.getIdx(0)+1];
 		z0 = X[3*tri.getIdx(0)+2];
 		x1 = X[3*tri.getIdx(1)];
 		y1 = X[3*tri.getIdx(1)+1];
 		z1 = X[3*tri.getIdx(1)+2];
 		x2 = X[3*tri.getIdx(2)];
 		y2 = X[3*tri.getIdx(2)+1];
 		z2 = X[3*tri.getIdx(2)+2];
 		//velocities
 		vx0 = V[3*tri.getIdx(0)];
 		vy0 = V[3*tri.getIdx(0)+1];
 		vz0 = V[3*tri.getIdx(0)+2];
 		vx1 = V[3*tri.getIdx(1)];
 		vy1 = V[3*tri.getIdx(1)+1];
 		vz1 = V[3*tri.getIdx(1)+2];
 		vx2 = V[3*tri.getIdx(2)];
 		vy2 = V[3*tri.getIdx(2)+1];
 		vz2 = V[3*tri.getIdx(2)+2];
 				
 		Vector3d v10 = new Vector3d();
 		Vector3d v20 = new Vector3d();
 		Vector3d v30 = new Vector3d();
 		Vector3d x10 = new Vector3d();
 		Vector3d x20 = new Vector3d();
 		Vector3d x30 = new Vector3d();
 
 		//relaitve positions and velocities
 		x10.set(X[3*tri.getIdx(1)]-X[3*tri.getIdx(0)],
 			X[3*tri.getIdx(1)+1]-X[3*tri.getIdx(0)+1],
 			X[3*tri.getIdx(1)+2]-X[3*tri.getIdx(0)+2]);
 		x20.set(X[3*tri.getIdx(2)]-X[3*tri.getIdx(0)],
 		        X[3*tri.getIdx(2)+1]-X[3*tri.getIdx(0)+1],
 		        X[3*tri.getIdx(2)+2]-X[3*tri.getIdx(0)+2]);
 		x30.set(X[3*vertex]-X[3*tri.getIdx(0)],
 			X[3*vertex+1]-X[3*tri.getIdx(0)+1],
 			X[3*vertex+2]-X[3*tri.getIdx(0)+2]);
 		v10.set(V[3*tri.getIdx(1)]-V[3*tri.getIdx(0)],
 			V[3*tri.getIdx(1)+1]-V[3*tri.getIdx(0)+1],
 			V[3*tri.getIdx(1)+2]-V[3*tri.getIdx(0)+2]);
 		v20.set(V[3*tri.getIdx(2)]-V[3*tri.getIdx(0)],
 			V[3*tri.getIdx(2)+1]-V[3*tri.getIdx(0)+1],
 			V[3*tri.getIdx(2)+2]-V[3*tri.getIdx(0)+2]);
 		v30.set(V[3*vertex]-V[3*tri.getIdx(0)],
 			V[3*vertex+1]-V[3*tri.getIdx(0)+1],
 			V[3*vertex+2]-V[3*tri.getIdx(0)+2]);
 				
 		/*[v10, v20, v30]t^3 + ([x10, v20, v30]+[v10, x20, v30]+[v10, v20, x30])t^2
 		  +([x10, x20, v30]+[x10,v20,x30]+[v10, x20, x30])t+[x10,x20, x30]
 		*/
 		op[0] = v10.dot(v20.cross(v30));
 		op[1] = x10.dot(v20.cross(v30)) + v10.dot(x20.cross(v30)) + v10.dot(v20.cross(x30));
 		op[2] = x10.dot(x20.cross(v30)) + x10.dot(v20.cross(x30)) + v10.dot(x20.cross(x30));
 		op[3] = x10.dot(x20.cross(x30));
 			
 	    }
 		
 	    //edge-edge
 	    else {
 		if (i < 9) {
 		    p_1 = t1.getIdx(0); 
 		    q_1 = t1.getIdx(1); 
 		    if (i == 6) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(1); }
 		    if (i == 7) { p_2 = t2.getIdx(0); q_2 = t2.getIdx(2); }
 		    if (i == 8) { p_2 = t2.getIdx(1); q_2 = t2.getIdx(2); }
 		}
 		else if(i < 12){
 		    p_1 = t1.getIdx(0); 
 		    q_1 = t1.getIdx(2);
 		    if(i==9){ p_2 = t2.getIdx(0); q_2 = t2.getIdx(1); }
 		    if(i==10){ p_2 = t2.getIdx(0); q_2 = t2.getIdx(2); }
 		    if(i==11){ p_2 = t2.getIdx(1); q_2 = t2.getIdx(2); }
 		}
 		else if (i < 15){
 		    p_1 = t1.getIdx(1); 
 		    q_1 = t1.getIdx(2);
 		    if(i==12){ p_2 = t2.getIdx(0); q_2 = t2.getIdx(1); }
 		    if(i==13){ p_2 = t2.getIdx(0); q_2 = t2.getIdx(2); }
 		    if(i==14){ p_2 = t2.getIdx(1); q_2 = t2.getIdx(2); }
 		}
 		
 		//edge vertex points
 		x0 = X[3*p_1];
 		y0 = X[3*p_1+1];
 		z0 = X[3*p_1+2];
 		x1 = X[3*q_1];
 		y1 = X[3*q_1+1];
 		z1 = X[3*q_1+2];
 		x2 = X[3*p_2];
 		y2 = X[3*p_2+1];
 		z2 = X[3*p_2+2];
 		x3 = X[3*q_2];
 		y3 = X[3*q_2+1];
 		z3 = X[3*q_2+2];
 		//edge vertex velocities
 		vx0 = V[3*p_1];
 		vy0 = V[3*p_1+1];
 		vz0 = V[3*p_1+2];
 		vx1 = V[3*q_1];
 		vy1 = V[3*q_1+1];
 		vz1 = V[3*q_1+2];
 		vx2 = V[3*p_2];
 		vy2 = V[3*p_2+1];
 		vz2 = V[3*p_2+2];
 		vx3 = V[3*q_2];
 		vy3 = V[3*q_2+1];
 		vz3 = V[3*q_2+2];
 
 		//edges and radius between first two edge points
 		Vector3d v10 = new Vector3d();
 		Vector3d v20 = new Vector3d();
 		Vector3d v30 = new Vector3d();
 		Vector3d x10 = new Vector3d();
 		Vector3d x20 = new Vector3d();
 		Vector3d x30 = new Vector3d();
 
 
 		x10.set(X[3*q_1]-X[3*p_1],
 			X[3*q_1+1]-X[3*p_1+1],
 			X[3*q_1+2]-X[3*p_1+2]);
 		x30.set(X[3*p_2]-X[3*p_1],
 			X[3*q_2+1]-X[3*p_2+1],
 			X[3*p_2+2]-X[3*p_1+2]);
 		x20.set(X[3*q_2]-X[3*p_2],
 			X[3*q_2+1]-X[3*p_2+1],
 			X[3*q_2+2]-X[3*p_2+2]);
 		v10.set(V[3*q_1]-V[3*p_1],
 			V[3*q_1+1]-V[3*p_1+1],
 			V[3*q_1+2]-V[3*p_1+2]);
 		v30.set(V[3*p_2+1]-V[3*p_1+1],
 			V[3*p_2+1]-V[3*p_1+1],
 			V[3*p_2+2]-V[3*p_1+2]);
 		v20.set(V[3*q_2]-V[3*p_2],
 			V[3*q_2+1]-V[3*p_2+1],
 			V[3*q_2+2]-V[3*p_2+2]);
 				
 		/*[v10, v20, v30]t^3 + ([x10, v20, v30]+[v10, x20, v30]+[v10, v20, x30])t^2
 		  +([x10, x20, v30]+[x10,v20,x30]+[v10, x20, x30])t+[x10,x20, x30]
 		*/
 
 		op[0] = v10.dot(v20.cross(v30));
 		op[1] = x10.dot(v20.cross(v30)) + v10.dot(x20.cross(v30)) + v10.dot(v20.cross(x30));
 		op[2] = x10.dot(x20.cross(v30)) + x10.dot(v20.cross(x30)) + v10.dot(x20.cross(x30));
 		op[3] = x10.dot(x20.cross(x30));
 				
 	    }
 		
 	    //get roots of cubic time function
 	    time[0] = -1.0;
 	    time[1] = -1.0;
 	    time[2] = -1.0;
 		
 	    // Creates a Polynomial from a list of coefficients and solves
 	    // for its roots.
 	    // root-finder fails if op[0]==0, and polynomial is not cubic
 	    if (op[0] != 0.0) {
 		NumberList list = cubic.solve(new Polynomial(op));
 		Object[] roots = list.toArray();
 		int timeIndex = 0;
 		// Looks through the root list for real-number roots and,
 		// if there are any, stores them in the time array.
 		for (int j = 0; j < roots.length; j++) {
 		    try {
 			time[timeIndex] = ((Number) roots[j]).doubleValue();
 			timeIndex++;
 		    } catch (IllegalArgumentException iae) {
 		    }
 		}
 	    }
 	    //quadratic roots: (if (b != 0)) 
 	    else if(op[1]!=0){
 		time[0] = (-1*op[2]+Math.sqrt(op[2]*op[2]-4*op[1]*op[3]))/(2*op[1]);
 		time[1] = (-1*op[2]-Math.sqrt(op[2]*op[2]-4*op[1]*op[3]))/(2*op[1]);
 	    }
 	    //linear root
 	    else{
 		time[0] = (-1*op[3])/op[2];
 	    }
 			
 	    //if any roots are between 0 and time h
 	    //collision = true;
 	    if(time[0] > 0 && time[0] <= h ){
 		collision = true;
 		hit_t = time[0];
 	    }
 	    if(time[1] > 0 && time[1] <= h ){
 		collision = true;
 		hit_t = time[1];
 	    }
 	    if(time[2] > 0 && time[2] <= h ){
 		collision = true;
 		hit_t = time[2];
 	    }
 				
 	    //store collision
 	    if (collision) {
 			
 			System.out.println("TIMES "+time[0]+" "+time[1]+" "+time[2]+" "+collision);
 
 		//vertex-face collision point
 		if(i < 6) {
 		    p[0] = x3+hit_t*vx3;
 		    p[1] = y3+hit_t*vy3;
 		    p[2] = z3+hit_t*vz3;
 		    a[0] = x0+hit_t*vx0;
 		    a[1] = y0+hit_t*vy0;
 		    a[2] = z0+hit_t*vz0;
 		    b[0] = x1+hit_t*vx1;
 		    b[1] = y1+hit_t*vy1;
 		    b[2] = z1+hit_t*vz1;
 		    c[0] = x2+hit_t*vx2;
 		    c[1] = y2+hit_t*vy2;
 		    c[2] = z2+hit_t*vz2;
 		    //check for false-positive
 		    double[] distAndCoords = DistanceFinder.vertexFaceDistance(p, a, b, c, u, v, w);
 		    if(distAndCoords[0] < .000001){
 			u = distAndCoords[1];
 			v = distAndCoords[2];
 			w = distAndCoords[3];
 			cSet.add(new Collision(Collision.VERTEX_FACE, u, v, w, vertex, tri.getIdx(0), tri. getIdx(1), tri.getIdx(2), 0.0));
 				 }
 					
 		    }
 				
 		    //edge-edge
 		    else {
 			//edge-edge collision points
 			p1[0] = x0+hit_t*vx0;
 			p1[1] = y0+hit_t*vy0;
 			p1[2] = z0+hit_t*vz0;
 			q1[0] = x1+hit_t*vx1;	
 			q1[1] = y1+hit_t*vy1;
 			q1[2] = z1+hit_t*vz1;
 			p2[0] = x2+hit_t*vx2;
 			p2[1] = y2+hit_t*vy2;
 			p2[2] = z2+hit_t*vz2;
 			q2[0] = x3+hit_t*vx3;
 			q2[1] = y3+hit_t*vy3;
 			q2[2] = z3+hit_t*vz3;
 
 			//check for false-positive
			double[] distAndCoords = DistanceFinder.edgeEdgeDistance(p1, q1, p2, q2, s, t);
 			if (distAndCoords[0] < .000001) {
 			    s = distAndCoords[0];
 			    t = distAndCoords[1];
 			    cSet.add(new Collision(Collision.EDGE_EDGE, 1.0 - s, 1.0 - t, 0.0, p_1, q_1, p_2, q_2, 0.0));
 			}
 		    }
 	    }
 	    collision = false;
 	}
 	return cSet;
     }
 }
