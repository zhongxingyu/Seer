 package pbs;
 
 import jig.engine.util.Vector2D;
 import pbs.Entity.*;
import pbs.GameRun.*;
 import pbs.Level.Layer;
 import pbs.Updater.MissileArc;
 
 public class Weapons {
 
 	public Weapons(){
 	
 	}
 	
 	public static class TriShot implements CustomWeapon{
 
 		public Entity shoot_bullet(Vector2D pos, Vector2D vel, double arc){
 			Entity e = new Entity("resources/pbs-spritesheet.png#ball");
 			
 			e.theta = arc;
 			e.setPosition(pos);
 			e.setVelocity(vel);
 			e.setCustomUpdate(new MissileArc(e.theta));
 			return e;
 		}
 		
 		@Override
 		public void shoot(Level ld, Entity e, long deltaMs) {
 			Entity m0, m1, m2;
 			Vector2D pos = e.getCenterPosition();
 			
 			m0 = shoot_bullet(pos, new Vector2D(20, -1), 0.0);
 			m1 = shoot_bullet(pos, new Vector2D(20, 0), 0.0);
 			m2 = shoot_bullet(pos, new Vector2D(20, 1), 0.0);
 			
 			ld.add(m0, Layer.ENEMY);
 			ld.add(m1, Layer.ENEMY);
 			ld.add(m2, Layer.ENEMY);
 		}
 	}
 	
 }
