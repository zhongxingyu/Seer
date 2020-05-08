 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  * @author 
  *
  */
 public class Crap extends Entity
 {
 	//public static final String NAME = "Crap";
 	//private static final int SCREEN_HEIGHT = 600;
 	
 	protected boolean isActive = false;
 	protected Bird parentBird;
 	
 	public Crap(Image image, Bird bird)
 	{
 		super(image);
 		super.velocity = 0.3f;
 		parentBird = bird;
 		this.boundingShape = new Rectangle(this.getPosition().x, 
 				this.getPosition().y, 
 				this.getAnimationFrame().getWidth(),
 				this.getAnimationFrame().getHeight());
 	}
 	
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
 		return "Crap";
 	}
 	
 	public void update(int delta)
 	{
 		if(!isActive) return;  //if the poop isnt active, dont do shit!  GET IT
 		
 		this.position.y += this.velocity * delta;
 		if(this.position.y > GLOBAL.SCREEN_HEIGHT)
 		{
 			this.deactivate();
 		}
 		updateBoundingRect();
 	}
 	
 	public void draw()
 	{
 		this.getAnimationFrame().draw(this.getPosition().x, this.getPosition().y);
 	}
 	
 	public void activate()
 	{
 		isActive = true;
 		//Position the sprite at the bottom center of the parent bird sprite
 		this.position = new Vector2f(parentBird.getPosition().x + (parentBird.getAnimationFrame().getWidth() / 2),
 				parentBird.getPosition().y + parentBird.getAnimationFrame().getHeight());
 	}
 	
 	public void deactivate()
 	{
 		isActive = false;
 	}
 	
 	public boolean isActive()
 	{
 		return this.isActive;
 	}
 	
 	protected void handleCollision(Entity entity)
 	{
 		System.out.println("Crap collision with " + entity.getName());
		this.deactivate();
 	}
 }
