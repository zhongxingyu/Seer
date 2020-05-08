 package entities;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SpriteSheet;
 
 import anim.AnimationState;
 
 public class Steam extends Sprite{
 	private static int WIDTH = 32;
 	private static int HEIGHT_MUL = 32;
 
 	public Steam(float x, float y, float height) {
 		super(x, y, WIDTH, height*HEIGHT_MUL);
 		try {
			Animation a = (new Animation(new SpriteSheet("assets/images/nonentities/steam/steam.png", 117, 108), 100));
 			anim.addAnimation(AnimationState.BASIC, a);
 			anim.setDefaultAnimation(AnimationState.BASIC);
			anim.setFrames(AnimationState.BASIC, 5);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		// TODO Auto-generated constructor stub
 	}
 
 }
