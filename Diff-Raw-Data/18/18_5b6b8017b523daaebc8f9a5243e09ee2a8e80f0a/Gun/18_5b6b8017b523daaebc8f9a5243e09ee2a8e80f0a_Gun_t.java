 package com.adavieslyons.zombia.item;
 
 import java.util.ArrayList;
import java.util.Random;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.geom.Vector2f;
 
 import com.adavieslyons.zombia.entity.Bullet;
 
 public abstract class Gun {
 	String name;
 	Image gunImage;
 	Image muzzleImage;
 	Sound fireSound;
 	ArrayList<Bullet> bullets;
 	Vector2f position;
 	float angle;
 	
 	Boolean muzzleFlash = false;
 	
 	public Gun(String a_name) throws SlickException {
 		gunImage = new Image("resource/img/" + a_name + ".png");
 		muzzleImage = new Image("resource/img/" + a_name + "_muzzleflash.png");
 		fireSound = new Sound("resource/snd/" + a_name + ".ogg");
 		name = a_name;
 		bullets = new ArrayList<Bullet>();
 	}
 	
 	public void render(GameContainer gc, Graphics graphics) {		
 		graphics.drawImage(gunImage, position.getX(), position.getY());
 		
 		if (muzzleFlash) {
 			graphics.drawImage(muzzleImage, position.getX(), position.getY());
 		}
 		
 		for (Bullet bullet : bullets) {
 			bullet.render(gc, graphics);
 		}
 	}
 	
	protected Random rand = new Random();
	
 	public void update(GameContainer gc, int delta) throws SlickException {
		/*if (gc.getInput().isKeyDown(2))) {
			
		}*/
		if (rand.nextInt(100) > 98) {
			bullets.clear();
		}
		if ((gc.getInput().isMouseButtonDown(0) && !muzzleFlash) || rand.nextInt(100) >= 50) {
 			muzzleFlash = true;
 			
 			Vector2f position2 = position.copy();
 			
 			position2.x += 24;
 			position2.y += 24;
 			
 			//position2.x += 14;
 			//position2.y += 1;
 			
 			//position2.x += 12 * Math.sin(Math.toRadians(-angle));
 			//position2.y += 6 * Math.cos(Math.toRadians(-angle));
 			
 			angle -= 270;
			position2.x = (float) (position2.x - (20 * Math.cos(Math.toRadians(angle)) + 8 * Math.sin(Math.toRadians(angle))));
			position2.y = (float) (position2.y - (20 * Math.sin(Math.toRadians(angle)) - 8 * Math.cos(Math.toRadians(angle))));
 			//
 			
 			bullets.add(getNewBullet(position2, angle));
 			fireSound.play();
 		} else if (!gc.getInput().isMouseButtonDown(0)) {
 			muzzleFlash = false;
 		}
 		
 		for (Bullet bullet : bullets) {
 			bullet.update(gc, delta);
 		}
 	}
 	
 	public void setPosition(Vector2f newPosition) {
 		position = newPosition;
 	}
 	
 	public void setAngle(float newAngle) {
 		angle = newAngle;
 		
 		gunImage.setRotation(angle);
 		muzzleImage.setRotation(angle);
 	}
 	
 	public abstract Bullet getNewBullet(Vector2f origin, float angle) throws SlickException;
 }
