 package edu.calpoly.csc.pulseman.gameobject;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Rectangle;
 
 public class Goal extends Collidable {
	public static Image image;
 	
	public Goal(int x, int y) {
 		super(new Rectangle(x, y, image.getWidth(), image.getHeight()));
 	}
 
 	public static void init(Image image) {
 		Goal.image = image;
 	}
 	
 	@Override
 	public void render(GameContainer gc, Graphics g) {
		g.drawImage(image, getHitBox().getX(), getHitBox().getY());
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) {
		
 	}
 
 }
