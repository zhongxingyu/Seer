 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 
 @SuppressWarnings("unused")
 public class BonusDisplay extends Entity
 {
	public static final String NAME = "BonusScore";
 	private float startingY;
 	private int currentFrame;
 	
 	protected boolean isActive = false;
 	protected Entity parentEntity;
 	
 	private boolean debug = false;
 	
 	/**
 	 * Constructor
 	 * @param image - score image
 	 * @param parent - entity to draw above
 	 */
 	public BonusDisplay(Image[] images, Entity parent)
 	{
 		super(images);
 		super.velocity = GLOBAL.SCROLL_SPEED;
 		parentEntity = parent;
 		this.colidable = false;
 		this.currentFrame = 0;
 		this.startingY = this.parentEntity.getPosition().y - 30;
 		this.setPosition(new Vector2f(
 				(this.parentEntity.position.x + this.parentEntity.getAnimationFrame().getWidth() / 2),
 				(this.parentEntity.position.y - 30)
 				)
 			);
 		this.setAnimationFrames(this.currentFrame);
 		this.boundingShape = new Rectangle(this.getPosition().x, 
 				this.getPosition().y, 
 				this.getAnimationFrame().getWidth(),
 				this.getAnimationFrame().getHeight());
 	}
 	/**
 	 * Update hit box
 	 */
 	private void updateBoundingRect()
 	{
 		boundingShape.setX(this.position.x);
 		boundingShape.setY(this.position.y);
 	}
 	
 	/**
 	 * @return Get the Name
 	 */
 	public String getName()
 	{
 		return NAME;
 	}
 	/**
 	 * update the score's position etc
 	 * @param delta
 	 */
 	public void update(Input input, int delta)
 	{
 		if (this.startingY - this.position.y > 100){			// if it has risen 100 px
 			if (this.currentFrame < this.images.length -1){		// if end of image list has not been reached
 				this.currentFrame += 1;							// count++
 				this.setAnimationFrames(this.currentFrame);		// increment frame
 			}
 		}
 		this.position.y -= this.velocity * delta;				// move up some more
 	}
 	/**
 	 * draw the score on the screen
 	 */
 	public void draw()
 	{
 		this.getAnimationFrame().draw(this.getPosition().x, this.getPosition().y);
 	}
 	/**
 	 * Activate the score for drawing
 	 */
 }
