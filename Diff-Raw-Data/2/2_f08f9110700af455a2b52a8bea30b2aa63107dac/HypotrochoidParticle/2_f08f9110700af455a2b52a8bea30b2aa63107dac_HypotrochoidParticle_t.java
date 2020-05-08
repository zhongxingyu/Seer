 package particular;
 
 import processing.core.*;
 
 public class HypotrochoidParticle extends LingerParticle {
 	
 	final float T_STEP_SIZE = PApplet.PI/64F;   //amount we move t per tick, MUST BE A FACTOR OF PI
 	float adjustedTStepSize = T_STEP_SIZE;
 	
 	float a; //radius of fixed circle
 	float b; //radius of moving circle
 	float h; //distance of moving circle from center of fixed circle
 	
 	float t = 0;  //current time/angle
 	float t_limit;    //limit of t based on period before death
 	boolean isDead = false;
 	
 	public HypotrochoidParticle(PApplet p, int a, int b, int h, float timeScale, int color, float weight, int fadeLifetime, int lingerLifetime) {
 		//note that B should be less than A, and H MUST be less than a for hypotrochoid behavior
 
 		super(p, new PVector(0,0), new PVector(0,0), 1, 0, 0,
 				1, color, weight, fadeLifetime, lingerLifetime);
 
 		//undo some of the super constructor stuff
 		path.remove(0);
 		
 		//do one move to get starting position
 		this.a = a;
 		this.b = b;
 		this.h = h;
 		
		this.adjustedTStepSize = adjustedTStepSize * timeScale;
 		
 		this.t_limit = b / GCD(a-b,b) * PApplet.TWO_PI;
 		PApplet.println(b / GCD(a-b,b));
 		
 		move();
 		startingPos = new PVector(pos.x,pos.y);
 	}
 	private int GCD(int a, int b) { return b==0 ? a : GCD(b, a%b); }
 
 	public void move() {
 		
 		//calculate a new spirograph position
 		int x = p.width/2 + (int) ((a-b)*PApplet.cos(t) + h*PApplet.cos(((a-b)/b)*t));
 		int y = p.height/2 + (int) ((a-b)*PApplet.sin(t) - h*PApplet.sin(((a-b)/b)*t));
 		
 		pos = new PVector(x,y);
 		
 		//go in the new direction for a tick
 		updatePath(true);
 		
 		t += adjustedTStepSize;
 		
 		if (t > t_limit) {
 			isDead = true;
 		}
 		
 		//age++;
 	}
 	
 	public boolean isDead() {
 		return isDead;
 	}
 	
 }
