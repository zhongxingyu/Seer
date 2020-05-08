 package com.github.joukojo.testgame;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import com.github.joukojo.testgame.images.ImageFactory;
 import com.github.joukojo.testgame.world.core.Moveable;
 
 public class Bullet implements Moveable {
 
	int locationX;
	int locationY;
 	
	double directionX = 0.0;
	double directionY = 0.0;
 	
 	private boolean isDestroyed = false;
 
 	public int getLocationX() {
 		return locationX;
 	}
 
 	public void setLocationX(int locationX) {
 		this.locationX = locationX;
 	}
 
 	public int getLocationY() {
 		return locationY;
 	}
 
 	public void setLocationY(int locationY) {
 		this.locationY = locationY;
 	}
 
 	public double getDirectionX() {
 		return directionX;
 	}
 
 	public void setDirectionX(double directionX) {
 		this.directionX = directionX;
 	}
 
 	public double getDirectionY() {
 		return directionY;
 	}
 
 	public void setDirectionY(double directionY) {
 		this.directionY = directionY;
 	}
 
 
 	@Override
 	public void draw(Graphics graphics) {
 		if (!isDestroyed()) {
 			final BufferedImage bulletImage = ImageFactory.getBulletImage();
 			ImageObserver observer = null;
 			graphics.drawImage(bulletImage, locationX, locationY, 15, 15, observer );
 		}
 
 	}
 
 	@Override
 	public void move() {
 		locationX = (int) (locationX + (directionX * 1.2));
 		locationY = (int) (locationY + directionY * 1.2);
 
 	}
 
 	@Override
 	public boolean isOutside(int x, int y) {
 		return (locationX > x || locationX == 0)
 				|| (locationY > y || locationY <= 0);
 	}
 
 	public boolean isDestroyed() {
 		return isDestroyed;
 	}
 
 	public void setDestroyed(boolean isDestroyed) {
 		this.isDestroyed = isDestroyed;
 	}
 
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this);
 	}
 
 }
