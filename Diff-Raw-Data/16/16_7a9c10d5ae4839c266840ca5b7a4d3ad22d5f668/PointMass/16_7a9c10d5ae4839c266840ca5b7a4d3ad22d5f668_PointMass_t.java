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
 	public static final double VEL_MAX = 10.0;
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
 		// apply viscosity and friction
 //		addForce(-vel.x * Environment.VISCOSITY, -vel.y * Environment.VISCOSITY);
 		if(vmag > 0)
 			addForce(-vel.x * Environment.FRICTION / vmag, -vel.y * Environment.FRICTION / vmag);
 		
 		// move the point - acceleration needed for extreme forces
 		// TODO: Test against 0.5 * acc * acc * dt.
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
 		double dist = Math.sqrt(dx*dx + dy*dy);
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
		vel.x -= projdvx*pm.mass/(mass + pm.mass);
		vel.y -= projdvx*pm.mass/(mass + pm.mass);
		pm.vel.x += projdvx*mass/(mass + pm.mass);
		pm.vel.y += projdvy*mass/(mass + pm.mass);
 		
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
