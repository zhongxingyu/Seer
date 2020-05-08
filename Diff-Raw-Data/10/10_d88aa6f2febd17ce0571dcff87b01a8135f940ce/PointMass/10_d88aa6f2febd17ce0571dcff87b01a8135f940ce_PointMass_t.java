 package physics;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glVertex2d;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.vecmath.Vector2d;
 
 import environment.Environment;
 
 public class PointMass {
 
 	/** for physics, joints are modeled as point masses */
 	public static final double DEFAULT_RADIUS = 10;
 	public static final double DEFAULT_MASS = 1.0;
 	public static final double VEL_MAX = 5.0;
 	public static final double ELASTICITY = 0; // Values between 0 and 1 (though negatives are interesting...)
 	public static final double COLLISION_TAX = 1; // EXPERIMENTAL PURPOSES ONLY. Set very close to 1.
 	private double mass;
 	private double radius;
 	private Vector2d pos;
 	private Vector2d vel;
 	private Vector2d acc;
 	private Vector2d force;
 
 	private List<Rod> connections;
 
 	public PointMass(double m, Rod ... rods){
 		connections = new ArrayList<Rod>();
 		for(Rod r : rods){
 			connections.add(r);
 			r.addPoint(this);
 		}
 		pos = new Vector2d();
 		vel = new Vector2d();
 		acc = new Vector2d();
 		force = new Vector2d();
 		mass = m;
 		radius = DEFAULT_RADIUS;
 	}
 
 	public void initPosition(double x, double y){
 		pos.x = x;
 		pos.y = y;
 	}
 
 	public void addForce(double fx, double fy){
 		force.x += fx;
 		force.y += fy;
 	}
 
 	// TODO: Perhaps get rid of the Environment e parameter?
 	public void move(Environment e, double dt){
 
 		double vmag = vel.length();
 		
 		// recover acceleration from mass and forces
 		acc.x = force.x/mass;
 		acc.y = force.y/mass;
 		
 		// test velocity
 		if(vmag > VEL_MAX){
 			vel.x *= VEL_MAX / vmag;
 			vel.y *= VEL_MAX / vmag;
 			
 			double accProj = (vel.x/vmag)*acc.x + (vel.y/vmag)*acc.y;
 			if(accProj > 0) {
 				acc.x -= accProj*(vel.x/vmag);
 				acc.y -= accProj*(vel.y/vmag);
 			}
 			
 		}
 		// apply friction
 		if(vmag > 0)
 			addForce(-vel.x * Environment.FRICTION / vmag, -vel.y * Environment.FRICTION / vmag);
 		
 		// move the point
 		// pos.x += dt * vel.x + 0.5 * acc.x * dt * dt;
 		// pos.y += dt * vel.y + 0.5 * acc.x * dt * dt;
 		pos.x += dt * vel.x;
 		pos.y += dt * vel.y;
 		
 		// newton's law for acceleration
 		vel.x += dt * acc.x;
 		vel.y += dt * acc.y;
 		
 		// reset forces and acceleration to be summed for next update
 		force.x = force.y = acc.x = acc.y = 0.0;
 	}
 	
 	public double getRadius() {
 		return radius;
 	}
 	
 	public Vector2d getPos() {
 		return pos;
 	}
 
 	public double getX(){
 		return pos.x;
 	}
 
 	public double getY(){
 		return pos.y;
 	}
 
 	public void clearPhysics() {
 		vel.x = vel.y = force.x = force.y = 0.0;
 	}
 
 	public double getVX() {
 		return vel.x;
 	}
 
 	public double getVY() {
 		return vel.y;
 	}
 	
 	public double getMass() {
 		return mass;
 	}
 	
 	public double getSpeed() {
 		return Math.sqrt(vel.x*vel.x + vel.y*vel.y);
 	}
 
 	public Vector2d getVel(){
 		return vel;
 	}
 
 	public static Vector2d vecAB(PointMass A, PointMass B){
 		return new Vector2d(B.pos.x - A.pos.x, B.pos.y - A.pos.y);
 	}
 
 	public void addAcc(double fx, double fy) {
 		force.x += fx/mass;
 		force.y += fy/mass;
 	}
 
 	public void addPos(double dx, double dy) {
 		pos.x += dx;
 		pos.y += dy;
 	}
 	
 	public void addVel(double dx, double dy) {
 		vel.x += dx;
 		vel.y += dy;
 	}
 	
 	public boolean collide(PointMass pm) {
 		
 		// Test to see if point masses intersect. Return false if they do not.
 		double dx = pos.x - pm.pos.x;
 		double dy = pos.y - pm.pos.y;
 		double mindist = radius + pm.radius;
 		double dist = Math.hypot(dx, dy);
 		double overlap = mindist - dist;
 		if(overlap < 0)
 			return false;
 		
 		// Normalize dx, dy.
 		if(dist > 0) {
 			dx /= dist;
 			dy /= dist;
 		} else {
 			dx = 1;
 			dy = 0;
 		}
 		
 		// Set point masses apart and adjust velocities.
 		pos.x += overlap*dx*pm.mass/(mass + pm.mass);
 		pos.y += overlap*dy*pm.mass/(mass + pm.mass);
 		pm.pos.x -= overlap*dx*mass/(mass + pm.mass);
 		pm.pos.y -= overlap*dy*mass/(mass + pm.mass);
 		
 		double dvx = vel.x - pm.vel.x;
 		double dvy = vel.y - pm.vel.y;
 		double projdvx = (dvx*dx + dvy*dy)*dx;
 		double projdvy = (dvx*dx + dvy*dy)*dy;
 		vel.x -= projdvx*pm.mass/(mass + pm.mass)*(1 + ELASTICITY);
 		vel.y -= projdvx*pm.mass/(mass + pm.mass)*(1 + ELASTICITY);
 		pm.vel.x += projdvx*mass/(mass + pm.mass)*(1 + ELASTICITY);
 		pm.vel.y += projdvy*mass/(mass + pm.mass)*(1 + ELASTICITY);
 		
 		// OPTIONAL: Apply collision tax to velocity.
 		vel.x *= COLLISION_TAX;
 		vel.y *= COLLISION_TAX;
 		pm.vel.x *= COLLISION_TAX;
 		pm.vel.y *= COLLISION_TAX;
 		
 		return true;
 	}
 	
 	public boolean collide(Rod rod) {
 		
 		// Test to see if PointMass intersects with rod.
 		Vector2d r = rod.asVector();
 		double rodLength = r.length();
 		r.normalize();
 		Vector2d norm = new Vector2d(-r.y, r.x);
 		
 		double dx = pos.x - rod.getEnd(0).pos.x;
 		double dy = pos.y - rod.getEnd(0).pos.y;
 		double dist = Math.hypot(dx,dy);
 		double projNormal = -r.x*dy + r.y*dx;
 		double projTangent = r.x*dx + r.y*dy;
 		
 		if(projTangent <= 0 || projTangent >= rodLength || Math.abs(projNormal) >= radius) {
 			return false;
 		}
 		
 		// Perform collision
 		// Determine "effective mass" of the rod at point t.
 		double t = projTangent/rodLength;
 		double m0 = rod.getEnd(0).getMass();
 		double m1 = rod.getEnd(1).getMass();
 		double rodEffMass = m0*m1/(m0*t*t + m1*(1-t)*(1-t));
 		
 		// Determine magnitude and direction of overlap. We want
 		// the PointMass to be ejected to the nearest free space.
 		double overlap;
 		if(projNormal >= 0) {
 			overlap = radius - projNormal;
 		} else {
 			overlap = -projNormal - radius;
 		}
 		
 		// Determine the rate at which PointMass is approaching rod.
 		double vx0 = rod.getEnd(0).vel.x;
 		double vy0 = rod.getEnd(0).vel.y;
 		double vx1 = rod.getEnd(1).vel.x;
 		double vy1 = rod.getEnd(1).vel.y;
 		double vxt = (1-t)*vx0 + t*vx1;
 		double vyt = (1-t)*vy0 + t*vy1;
 		double dvx = vxt - vel.x;
 		double dvy = vyt - vel.y;
 		double velNormal = dvx*norm.x + dvy*norm.y;
 		
 		// Displace the rod and point mass and adjust velocities.
		rod.displace(overlap*mass/(mass + rodEffMass), -velNormal*mass/(mass + rodEffMass), t);
 		pos.x -= overlap*rodEffMass/(mass + rodEffMass)*norm.x;
 		pos.y -= overlap*rodEffMass/(mass + rodEffMass)*norm.y;
 		vel.x += velNormal*rodEffMass/(mass + rodEffMass)*norm.x*(1 + ELASTICITY);
 		vel.y += velNormal*rodEffMass/(mass + rodEffMass)*norm.y*(1 + ELASTICITY);
 		
 		return true;
 	}
 	
 	public void glDraw() {
 		int n = (int)radius;
 		
 		glColor4f(0.5f, 0.8f, 1.0f, 1.0f);
 		glBegin(GL_LINES);
 		{
 			for(int i = 0; i < n; i++) {
 				glVertex2d(pos.x + radius*Math.cos(2*Math.PI*i/(double)n), pos.y + radius*Math.sin(2*Math.PI*i/(double)n));
 				glVertex2d(pos.x + radius*Math.cos(2*Math.PI*(i+1)/(double)n), pos.y + radius*Math.sin(2*Math.PI*(i+1)/(double)n));
 			}
 		}
 		glEnd();
 	}
 }
