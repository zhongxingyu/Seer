 package bullets;
 
 import org.newdawn.slick.SlickException;
 
 import entities.Bullet;
 import entities.MainChar;
 
 public class HomingBullet extends Bullet {
 	
 	final float HOMING = 0.03f; //Homing rate
 	final float ACCELERATION = 1f;  //Acceleration rate
 
 	public HomingBullet(String type, String bullet, float x, float y, float speed, float angle, boolean friendly) throws SlickException {
 		super(type, bullet, x, y, speed, angle, friendly);
 		super.bullet.rotate(angle);
 	}
 	
 	public void move(MainChar main, int delta){
		homeOn(main, HOMING*delta);
		System.out.println(bullet.getRotation());
 		float angle = bullet.getRotation(); //Find absolute bullet angle
 		
 		//Calculate speed vector
 		float r1 = speed;
 		float cosa = (float) Math.cos(Math.toRadians(angle));
 		float sina = (float) Math.sin(Math.toRadians(angle));
		speedx += cosa * ACCELERATION;
		speedy += sina * ACCELERATION;
 		
 		//Calculate scaling coefficient
 		float r2 = (float) Math.sqrt(speedx*speedx + speedy*speedy);
 		float k = r1/r2;
 		
 		//Scale vector
 		speedx *= k*k;
 		speedy *= k*k;
 		
 		//Adjust coordinates
 		x += speedx * delta;
 		y += speedy * delta;
 		
 	}
 }
