 /**
  * Class to handle all platform and ground types
  * 
  * @author Glenn Rivkees
  * @author Ian McMahon
  */
 
 package setting;
 
 import java.awt.image.BufferedImage;
 
import characterSprites.NewtonianSprite;
 
 import com.golden.gamedev.Game;
import com.golden.gamedev.object.Sprite;
 
 public abstract class Platform extends NewtonianSprite {
 	
 	protected Game owner;
 	
 	/*
 	 * Constructor for a Concrete Platform
 	 */
 	public Platform (Game owner) {
 		super();
 		this.owner = owner;
 	}
 	
 	/*
 	 * Constructor for a Platform Decorator 
 	 */
 	public Platform () {
 	}
 
 	
 	public void set(BufferedImage[] images, double x, double y) {
         this.setImages(images);
         this.setLocation(x, y);
     }
 	
 	public abstract void hitFromBottomAction();
 	public abstract void hitFromTopAction();
 	public abstract void hitFromLeftAction();
 	public abstract void hitFromRightAction();
 
 }
