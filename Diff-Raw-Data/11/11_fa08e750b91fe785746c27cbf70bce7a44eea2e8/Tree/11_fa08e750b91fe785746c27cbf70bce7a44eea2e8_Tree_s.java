 import java.util.Random;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 
 public class Tree extends Entity 
 {
 	protected static final int VARIANCE = 1;
 	protected static final int LOWER_RANDOM_BOUND = 243;
 	
 	
 	public Tree(Image[] images)
 	{
 		 super(images);
 		 this.setPosition(new Vector2f(500, 420));
 		 this.setVelocity(GLOBAL.SCROLL_SPEED);
 		 this.boundingShape = new Rectangle(this.getPosition().x, 
 					this.getPosition().y, 
 					this.getAnimationFrame().getWidth(),
 					this.getAnimationFrame().getHeight());
 		 this.colidable = true;
 	}
 	
 	/**
 	 * Returns a Tree object placed (somewhat) randomly on the sidewalk from the background
 	 * and 100 pixels off the screen to the right
 	 * @return
 	 * @throws SlickException
 	 */
 	public static Tree getRandomlyPlacedTree() throws SlickException
 	{
 		Image[] tree = new Image[1];
 		Boolean randBool = new Random().nextBoolean();
 		if (randBool){
			tree[0] = IMAGES.TREE_1;
 		} else {
			tree[0] = IMAGES.TREE_2;
 		}
 		Random rand = new Random();
 		int randomYValue = rand.nextInt(VARIANCE) + LOWER_RANDOM_BOUND;
 		Tree returnTree = new Tree(tree);
 		returnTree.position = new Vector2f(GLOBAL.SCREEN_WIDTH + 100, randomYValue);		
 		return returnTree;
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
 		return "Tree";
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
 		
 		this.getPosition().y += 100;
 	}
 	
 	public void draw()
 	{
 		this.getAnimation().draw(this.getPosition().x, this.getPosition().y);
 	}
 	
 }
