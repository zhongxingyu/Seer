 package edu.calpoly.csc.pulseman.gameobject;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.calpoly.csc.pulseman.SchemeLoader;
 
 public class Tile extends Collidable {
 	private static Image image;
 	public static enum TileType {
 		NORMAL, LBCORNER, LRCORNER, URCORNER, ULCORNER, SURFACE
 	}
 	
 	
 	static public void init(Image image) {
 		Tile.image = image;
 	}
 	
 	public Tile(int x, int y) {
 		super(new Rectangle(x, y, image.getWidth(), image.getHeight()), false);
 	}
 	
 	public Tile(int x, int y, boolean affectedByTime) {
 		super(new Rectangle(x, y, image.getWidth(), image.getHeight()), affectedByTime);
 	}
 	
 	
 	
 	@Override
 	public void render(GameContainer gc, Graphics g) {
 		super.render(gc, g);
		g.drawImage(image, getHitBox().getX(), getHitBox().getY(), SchemeLoader.getColor());
		
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) {
 		super.update(gc, delta);
 		
 	}
 
 	@Override
 	public boolean isAffectedByPulse() {
 		return false;
 	}
 
 }
