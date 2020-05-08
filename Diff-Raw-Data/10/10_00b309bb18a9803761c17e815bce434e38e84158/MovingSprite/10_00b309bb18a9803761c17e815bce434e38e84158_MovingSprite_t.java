 package cc.co.majteam.redemption.entities;
 
 import java.awt.Graphics2D;
 
import cc.co.majteam.redemption.shapes.Coords;

 public abstract class MovingSprite extends Sprite {
 	private int speed;
 	private Orientation orientation;
 
	public MovingSprite(Coords center, int speed, Orientation orientation) {
		super(center);
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
