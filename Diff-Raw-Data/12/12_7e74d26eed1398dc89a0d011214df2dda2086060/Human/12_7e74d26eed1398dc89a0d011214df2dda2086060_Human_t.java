 import java.util.Random;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 
 public class Human extends Entity
 {
 	protected static final int VARIANCE = 42;
 	protected static final int LOWER_RANDOM_BOUND = 390;
 	
 	private static Color chromakey = new Color(255, 0, 255);
 	
 	
 	public Human(Image[] images)
 	{
 		 super(images);
 		 this.setPosition(new Vector2f(500, 420));
 		 this.setVelocity(0.15f);
 		 this.boundingShape = new Rectangle(this.getPosition().x, 
 					this.getPosition().y, 
 					this.getAnimationFrame().getWidth(),
 					this.getAnimationFrame().getHeight());
 		 this.colidable = true;
 	}
 	
 	/**
 	 * Returns a Human object placed (somewhat) randomly on the sidewalk from the background
 	 * and 100 pixels off the screen to the right
 	 * @return
 	 * @throws SlickException
 	 */
 	public static Human getRandomlyPlacedHuman() throws SlickException
 	{
 		Image[] dude = {
 				new Image("resources/dude/walk/07.png", chromakey).getScaledCopy(1.7f), 
 				new Image("resources/dude/walk/06.png", chromakey).getScaledCopy(1.7f), 
 				new Image("resources/dude/walk/05.png", chromakey).getScaledCopy(1.7f),
 				new Image("resources/dude/walk/04.png", chromakey).getScaledCopy(1.7f),
 				new Image("resources/dude/walk/03.png", chromakey).getScaledCopy(1.7f),
 				new Image("resources/dude/walk/02.png", chromakey).getScaledCopy(1.7f),
 				new Image("resources/dude/walk/01.png", chromakey).getScaledCopy(1.7f)
 				};
 		Random rand = new Random();
 		int randomYValue = rand.nextInt(VARIANCE) + LOWER_RANDOM_BOUND;
 		Human returnHuman = new Human(dude);
 		returnHuman.position = new Vector2f(GLOBAL.SCREEN_WIDTH + 100, randomYValue);		
 		return returnHuman;
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
 		return "Human";
 	}
 	
 	public void update(Input input, int delta)
 	{
 		this.getPosition().x -=  this.getVelocity() * delta;
 		this.updateBoundingRect();	
 	}
 	
 	public void handleCollision(Entity entity)
 	{
 		Random rand = new Random();
 		
 		this.colidable = false;
 		this.getAnimation().stop();
 		
 		if(rand.nextBoolean()){
 			this.getAnimationFrame().rotate(90);
 		}else{
 			this.getAnimationFrame().rotate(-90);
 		}
		this.setVelocity(.1f);
 	}
 	
 	public void draw()
 	{
 		this.getAnimation().draw(this.getPosition().x, this.getPosition().y);
 	}
 	
 }
