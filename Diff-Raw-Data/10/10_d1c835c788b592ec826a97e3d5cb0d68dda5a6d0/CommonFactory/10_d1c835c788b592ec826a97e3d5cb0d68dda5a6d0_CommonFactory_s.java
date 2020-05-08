 package edu.mhs.wombat.game.data;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.mhs.wombat.game.core.Entity;
 import edu.mhs.wombat.game.data.common.AccelBullet;
 import edu.mhs.wombat.game.data.common.CurveBullet;
 import edu.mhs.wombat.game.data.common.LinearBullet;
 import edu.mhs.wombat.game.data.common.WobblyBullet;
 
 public class CommonFactory {
 	public static Entity newLinearBullet(Vector2f src, Vector2f target, float velocity){
 		return new LinearBullet(src, target, velocity);
 	}
 	
 	public static Entity newAccelBullet(Vector2f src, Vector2f target, float ivel, float accel){
 		return new AccelBullet(src, target, ivel, accel);
 	}
 	
<<<<<<< HEAD
 	public static Entity newCurveBullet(Vector2f src, Vector2f target, float velocity){
 		return new CurveBullet(src, target, velocity);
=======
 	public static Entity newWobblyBullet(Vector2f src, Vector2f target, float velocity){
 		return new WobblyBullet(src, target, velocity);
>>>>>>> TinyBullet for linear bullet
 	}
 }
