 package petri.api;
 
 import java.awt.Color;
 import java.awt.Graphics;
import java.awt.Polygon;
 
 /**
  * An Actor that uses an Image when drawing itself.
  * 
  * @author Cody Swendrowski
  */
 public abstract class ImageActor extends Actor {
 
 	protected GameImage image;
 
 	/**
 	 * Creates a new ImageActor.
 	 * 
 	 * @param e
 	 *            GameEngine to utilize
 	 * @param i
 	 *            GameImage to draw from
 	 */
 	public ImageActor(GameEngine e, GameImage i) {
 		super(e);
 		image = i;
 		if (image == null)
 			GameEngine.log("Null Image in Actor " + this.toString());
		setBasePoly(new Polygon(image.getOutline().xpoints,image.getOutline().ypoints, image.getOutline().npoints));
 	}
 
 	@Override
 	public void move(int ms) {
 		super.move(ms);
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.drawImage(image.getImage(), (int) center.x, (int) center.y,(int) size.x,(int) size.y, null);
 	}
 	
 	public String toString() {
 		return "ImageActor";
 	}
 
 }
