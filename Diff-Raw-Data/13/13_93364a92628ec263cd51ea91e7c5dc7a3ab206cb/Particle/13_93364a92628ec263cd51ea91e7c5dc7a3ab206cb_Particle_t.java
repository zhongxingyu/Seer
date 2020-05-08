 package org.av.maruvortex;
 
 import java.util.Random;
 
import android.util.Log;

 abstract public class Particle {
 	int radius;
 	double x, y, dx, dy, dt, angle;
 	int screenLength, screenHeight;
 	public static final double EPSILON = 0.00005;
 	double c=4; 
 
 	public boolean onscreen() {
 		return !(x > screenLength+20 || x < -20 || y > screenHeight+20 || y < -20);
 	}
 	public void update(double dt){
 		double norm = Math.sqrt(dx*dx + dy*dy);
 		dx *= c/norm;
 		dy *= c/norm;
 		if (Math.abs(dx)<EPSILON) dx = 0;
 		if (Math.abs(dy)<EPSILON) dy = 0;
 		x += dx*dt;
 		y += dy*dt;
 		angle = Math.atan2(dy, dx)*180/Math.PI;
 
 	}
 	public int getx() {
 		return (int)Math.round(x);
 	}
 
 	public int gety() {
 		return (int)Math.round(y);
 	}
 	public float getAngle() {
 		return (float)angle;
 	}
 	public void updateAngle(){
 		angle = Math.atan2(dy, dx)*180/Math.PI; 
 	}
 	public int getRadius(){
 		return radius;
 	}
 }
 
 class Bullet extends Particle {
 	int x0, y0;
 	int targetX, targetY;
 
 
 	private static final double EPSILON = 0.00005;
 	public Bullet(int targetX, int targetY, int x0, int y0, int h, int l){
 		c = 250;
 		radius = 4;
 		this.targetX = targetX;
 		this.targetY = targetY;
 		this.x0 = x0;
 		this.y0 = y0;
 		x = x0;
 		y = y0;
 		this.screenLength = l;
 		this.screenHeight = h;
 		dx = ((double)targetX-x0);
 		dy = ((double)targetY-y0);
 		double norm = Math.sqrt(dx*dx + dy*dy);
 		dx *= c/norm;
 		dy *= c/norm;
 		if (Math.abs(dx)<EPSILON) dx = 0;
 		if (Math.abs(dy)<EPSILON) dy = 0;
 
 		angle = Math.atan2(dy, dx)*180/Math.PI;
 		//Log.d("STUFF", "angle0: " + angle);
 	}
 	@Override
 	public void update(double dt){
 		double norm = Math.sqrt(dx*dx + dy*dy);
 		dx *= c/norm;
 		dy *= c/norm;
 		if (Math.abs(dx)<EPSILON) dx = 0;
 		if (Math.abs(dy)<EPSILON) dy = 0;
 		x += dx*dt;
 		y += dy*dt;
 		
 
 	}
 
 
 }
 class BoxParticle extends Particle{
 	Random r;
 
 	private int dir; //0 for left right, 1 for down up, 2 for right left, 3 for up down
 
 	public BoxParticle(Random r, int h, int l) {
 		c = 40;
 		radius = 13;
 		this.screenLength = l;
 		this.screenHeight = h;
 		dir = r.nextInt(4);
 		if (dir == 0){dx = 50; x = 0; y = r.nextInt(h);} 
 		else if (dir == 1){dy = -50; x = r.nextInt(l); y = h;}
 		else if (dir == 2){dx = -50; x = l; y = r.nextInt(h);}
 		else if (dir == 3){dy = 50; x = r.nextInt(l); y = 0;}
 		angle = Math.atan2(dy, dx)*180/Math.PI;
 
 	}
 	@Override
 	public void update(double dt){
 		x += dx*dt;
 		y += dy*dt;
 	}
 
 }
 class ParabolicParticle extends Particle{
 	int dir;
 	public ParabolicParticle(Random r, int h, int l) {
 		c = 40;
 		radius = 11;
 		this.screenLength = l;
 		this.screenHeight = h;
 		dir = r.nextInt(4);
 		if (dir == 0){dy = 70; dx = 20; x = 0; y = r.nextInt(h);} 
 		else if (dir == 1){dx = 70; dy = -20; x = r.nextInt(l); y = h;}
 		else if (dir == 2){dy = 70; dx = -20; x = l; y = r.nextInt(h);}
 		else if (dir == 3){dx = 70; dy = 20; x = r.nextInt(l); y = 0;}
 		angle = Math.atan2(dy, dx)*180/Math.PI;
 
 	}
 
 }
 
 class TurningParticle extends Particle{
 	int dir;
 	int t;
 	public TurningParticle(Random r, int h, int l) {
 		c = 40;
 		radius = 13;
 		this.screenLength = l;
 		this.screenHeight = h;
 		dir = r.nextInt(4);
		if (dir == 0){dx = 50; dy = 50; x = 0; y = r.nextInt(h);} 
		else if (dir == 1){dy = -50; dx = 50; x = r.nextInt(l); y = h;}
		else if (dir == 2){dx = -50; dy = 50; x = l; y = r.nextInt(h);}
		else if (dir == 3){dy = 50; dx = 50; x = r.nextInt(l); y = 0;}
 		angle = Math.atan2(dy, dx)*180/Math.PI;
 
 	}
 	@Override
 	public void update(double dt){
 
 		if (dir == 1 || dir == 3)
 			dx*=Math.sin(t/46);
 		else
 			dy*=Math.sin(t/46);
 		double norm = Math.sqrt(dx*dx + dy*dy);
		Log.d("ACCEL", dy+" "+dx);
 		dx *= c/norm;
 		dy *= c/norm;
 		if (Math.abs(dx)<EPSILON) dx = 0;
 		if (Math.abs(dy)<EPSILON) dy = 0;
 		x += dx*dt;
 		y += dy*dt;
 		angle = Math.atan2(dy, dx)*180/Math.PI;
 		t++;
 	}
 
 }
