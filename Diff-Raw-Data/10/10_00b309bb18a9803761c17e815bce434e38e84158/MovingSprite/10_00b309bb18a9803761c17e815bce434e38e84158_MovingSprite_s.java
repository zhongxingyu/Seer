 package cc.co.majteam.redemption.entities;
 
import java.awt.Color;
 import java.awt.Graphics2D;
 
 public abstract class MovingSprite extends Sprite {
 	private int speed;
 	private Orientation orientation;
 
	public MovingSprite(int x, int y, int width, int height, Color color,
			int speed, Orientation orientation) {
		super(x, y, width, height, color);
 		this.speed = speed;
 		this.orientation = orientation;
 	}
 
 	public int getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(int speed) {
 		this.speed = speed;
 	}
 
 	public Orientation getOrientation() {
 		return orientation;
 	}
 
 	public void setOrientation(Orientation orientation) {
 		this.orientation = orientation;
 	}
 
 	public abstract void draw(Graphics2D g2d);
 }
