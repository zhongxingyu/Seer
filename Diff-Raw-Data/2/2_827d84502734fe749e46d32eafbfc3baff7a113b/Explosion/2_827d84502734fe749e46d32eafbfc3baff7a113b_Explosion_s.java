 package indaprojekt;
 
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Rectangle2D.Float;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 /**
  * An explosion that will exist for a short moment.
  *
  */
 public class Explosion extends Mover
 {
 	private Animation animation;
 	private Expirer lifetime;
 	private boolean remove;
 	private float scale;
 	private final float centerX, centerY;
 	private final float ogWidth, ogHeight;
 	private float scalePerDelta;
 	
 	public Explosion(float x, float y, Rectangle2D.Float hitBox, Animation animation, int duration, 
 					float centerX, float centerY, float startScale, float scalePerDelta) 
 	{
 		super(x, y, hitBox);
 		lifetime = new Expirer(duration);
 		this.animation = animation;
 		
 		scale = startScale; //0.1f
 		
 		this.centerX = centerX;
 		this.centerY = centerY;
 		
 		this.scalePerDelta = scalePerDelta;
 		
 		ogWidth = (float)hitBox.getWidth();
 		ogHeight = (float)hitBox.getHeight();
 	}
 
 	@Override
 	public void draw() throws SlickException 
 	{
 		animation.getCurrentFrame().draw(x, y, scale);
 	}
 
 	@Override
 	public void doLogic(Input input, int delta) throws SlickException 
 	{
 		if (lifetime.hasExpired()) {
 			remove = true;
 		}
 		scale += scalePerDelta*delta; //0.003f
 
 		hitBox = new Rectangle2D.Float(hitBox.x, hitBox.y, ogWidth*scale, ogHeight*scale);
 		
 		center();
 	}
 	
 	private void center()
 	{
 		moveTo(centerX - (animation.getWidth()/2)*scale,
 			   centerY - (animation.getHeight()/2)*scale);
 	}
 
 	@Override
 	public boolean shouldBeRemoved() 
 	{
 		return remove;
 	}
 
 	@Override
 	public void handleCollision(Entity entity) 
 	{
 
 	}
 
 	@Override
 	public Entity spawnEntity() 
 	{	
 		return null;
 	}
 	
 }
