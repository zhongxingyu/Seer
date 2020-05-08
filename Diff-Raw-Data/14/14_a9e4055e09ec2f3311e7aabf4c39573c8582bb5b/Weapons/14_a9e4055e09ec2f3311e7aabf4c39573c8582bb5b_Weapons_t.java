 package pbs;
 
 import jig.engine.util.Vector2D;
 import pbs.Entity.*;
 //import pbs.GameRun.*;
 import pbs.Level.Layer;
 import pbs.Updater.*;
 import pbs.Renders.*;
 import pbs.Animations.*;
 
 public class Weapons {
 
     public static long DEFAULT_TTL = 20000;
 
 	public static class TriShot implements CustomWeapon {
 
 		public Entity shoot_bullet(Vector2D pos, Vector2D vel, double arc) {
 			Entity e = new Entity("resources/pbs-spritesheet.png#ball");
 
 			e.setPosition(pos);
 			e.setVelocity(vel);
 			e.setCustomUpdate(new MissileArc(arc));
 			return e;
 		}
 
 		@Override
 		public boolean shoot(Level ld, Entity e, long deltaMs) {
 			Entity m;
 			Vector2D pos = e.getCenterPosition();
 
 			for (int i = 0, j = -1; i < 3; i++, j++) {
 				m = shoot_bullet(pos, new Vector2D(20, j), 0.0);
 				ld.add(m, Layer.ENEMY);
 			}
 
 			return true;
 		}
 	}
 
 	// base class for all weapons
 	public static abstract class AbstractWeapon implements CustomWeapon {
 	    Layer targetLayer;
 	    
 	    long repeatTimer;
 	    long lastShot;
 	    
 	    public AbstractWeapon(Layer l) {
 		targetLayer = l;
 		repeatTimer = 0;
 		lastShot = repeatTimer;
 	    }
 
 	    //mechanism for timed weapons
 	    //to use it, add:
 	    //if(timerReady(deltaMs)){//firing behavior}
 	    //to your shoot method
 	    public boolean timerReady(long deltaMs){
 		lastShot += deltaMs;
 		if(lastShot >= repeatTimer){
 		    lastShot -= repeatTimer;
 		    return true;
 		}
 		return false;
 	    }
 	}
 
 	// base class for all player's weapons
 	public static abstract class FriendlyWeapon extends AbstractWeapon {
 		public FriendlyWeapon() {
 			super(Layer.FRIENDLY);
 		}
 	}
 
     public static class FriendlySpread extends FriendlyWeapon {
 	boolean last;
 	public FriendlySpread() {
 	    super();
 	    repeatTimer = 48;
 	    lastShot = repeatTimer;
 	}
 	
 	public boolean shoot(Level lvl, Entity e, long deltaMs) {
 	    Entity shot;
 
 	    if(timerReady(deltaMs)){
 		
 		for (int i = 0; i < 4; i++) {
 		    
 		    //this chunk could get moved to factory
 		    shot = new Entity("resources/pbs-spritesheet.png#green_laser");
 		    shot.setPosition(e.getCenterPosition().translate(new Vector2D(0, -8 + (i * 4))));
 		    shot.setVelocity(new Vector2D(100, (i * 2) - 4)
 				     .translate(new Vector2D(e.getVelocity().getX(), 0)));
 		    shot.setCustomUpdate(new Strait());
 		    shot.setCustomRender(new Scale(2));
 		    shot.setTimeToLive(1000);
 		    lvl.add(shot, targetLayer);
 		}
 
 		//same here... ideally: lvl.add(SOMEFACTORYMETHOD(), Layer.FX);
 		shot = new Entity("resources/pbs-spritesheet.png#laser_trail");
 		shot.setPosition(e.getCenterPosition().translate(new Vector2D(0, -8)));
 		shot.setVelocity(new Vector2D(-Math.random()*20, -Math.random()*40+20));
 		shot.setCustomAnimation(new AnimateOnce(64));
 		lvl.add(shot, Layer.FX);
 
 		last = !last;
 		return last;
 	    }
 
 	    return false;
 	}
     }
     
     // base class for all enemy weapons
     public static abstract class HostileWeapon extends AbstractWeapon {
 	public HostileWeapon() {
 	    super(Layer.HOSTILE);
 	}
     }
 
     public static class HostileSpread extends HostileWeapon {
 	boolean last;
 	int burst;
 	int shotsfired;
 	public HostileSpread(int b) {
 	    super();
 	    repeatTimer = 1000;
 	    lastShot = repeatTimer;
 	    burst = b;
 	    shotsfired = 0;
 	}
 	
 	public boolean shoot(Level lvl, Entity e, long deltaMs) {
 	    Entity shot;
 	    int sf = 10; //spreadfactor
 
 	    lastShot += deltaMs;
 	    if(lastShot >= repeatTimer){
 		shotsfired++;
 		if(shotsfired >= burst){
 		    shotsfired = 0;
 		    lastShot = 0;
 		}
 		
 		for (int i = 0; i < 5; i++) {
 		    
 		    //this chunk could get moved to factory
 		    shot = new Entity("resources/pbs-spritesheet.png#red_laser");
 		    shot.setPosition(e.getCenterPosition().translate(new Vector2D(0, -8 + (i * 4))));
 		    shot.setVelocity(new Vector2D(-50, (i/2.0 - 1)*sf)
 				     .translate(new Vector2D(e.getVelocity().getX(), 0)));
 		    shot.setCustomUpdate(new Strait());
 		    shot.setCustomRender(new Stretch(-2, 2));
 		    shot.setTimeToLive(1000);
 		    lvl.add(shot, targetLayer);
 		}
 
 		last = !last;
 		return last;
 	    }
 
 	    return false;
 	}
     }
 
 
     public static class SurroundShot extends HostileWeapon {
 	int numShots;
 	double increment;
 	double speed;
 
 
 	public SurroundShot(int ns, double s, long rt){ 
 	    super();
 	    numShots = ns;
 	    increment = (2*Math.PI)/numShots;
 	    speed = s;
 	    repeatTimer = rt;
 	    lastShot = rt;
 	    
 	}
 
 	public boolean shoot(Level lvl, Entity e, long deltaMs){
 	    Entity shot;
 	    double xv;
 	    double yv;
 	    double ang = 0; 
 
 	    if(timerReady(deltaMs)){
 		for(int i = 0; i < numShots; i++){
 	    
 		    xv = Math.cos(ang);
 		    yv = Math.sin(ang);
 		    ang += increment;
 		    
 		    shot = new Entity("resources/pbs-spritesheet.png#red_bullet");
 		    shot.setPosition(e.getCenterPosition().translate(new Vector2D(xv, yv)));
 		    shot.setVelocity(e.getVelocity().translate(new Vector2D(xv*speed, yv*speed)));
 		    shot.setCustomRender(new Scale(1.25));
 		    shot.setTimeToLive(DEFAULT_TTL);
 		    lvl.add(shot, targetLayer);
 		    
 		}
 
 		return true;
 	    }
 
 	    return false;
 
 	}
     }
     
     public static class SpinningSurroundShot extends SurroundShot {
 
 	double angleoffset;
 	double dtheta; //how much the angle moves after eveery shot
 
 	public SpinningSurroundShot(int ns, double s, double rad, long rt){ 
 	    super(ns, s, rt);
 	    angleoffset = 0;
 	    dtheta = rad;
 	}
 	
 	public boolean shoot(Level lvl, Entity e, long deltaMs){
 	    Entity shot;
 	    double xv;
 	    double yv;
 	    double ang = angleoffset; 

 
 	    lastShot += deltaMs;
 	    if(lastShot >= repeatTimer){
		angleoffset += dtheta;
 		lastShot -= repeatTimer;
 		for(int i = 0; i < numShots; i++){
 	    
 		    xv = Math.cos(ang);
 		    yv = Math.sin(ang);
 		    ang += increment;
 		    
 		    shot = new Entity("resources/pbs-spritesheet.png#red_bullet");
 		    shot.setPosition(e.getCenterPosition().translate(new Vector2D(xv, yv)));
 		    shot.setVelocity(new Vector2D(xv*speed, yv*speed).translate(e.getVelocity()));
 		    shot.setCustomRender(new Scale(1.25));
 		    shot.setTimeToLive(DEFAULT_TTL);
 		    lvl.add(shot, targetLayer);
 		    
 		}
 		return true;
 	    }
 
 	    return false;
 
 	}
     }
 
 
     
 }
