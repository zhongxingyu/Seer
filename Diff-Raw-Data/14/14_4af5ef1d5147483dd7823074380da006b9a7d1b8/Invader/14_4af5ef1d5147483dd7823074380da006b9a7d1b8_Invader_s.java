 package yasith.invaders.actors;
 
 import yasith.invaders.GameConstants;
 import yasith.invaders.GameState;
 import yasith.util.DynamicActor;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 
 /**
  * Represents an invader
  */
 public class Invader extends DynamicActor{
 
 
 	/**
 	 * Creates new instance of an invader,
 	 * Use createInvader factory method to get the Actor
 	 */
 	private Invader(Sprite sprite) {
 		super(GameState.getInstance().atlas.createSprite("invader"));
 	}
 	
 	/**
 	 * Creates and returns a new instance of an invader
 	 */
 	public static Invader createInvader(float x, float y, int row){
 		Sprite sprite = GameState.getInstance().getInvaderSprite(row);
 		
 		Invader invader = new Invader(sprite);
 		invader.setPosition(x, y);
 		
 		return invader;
 	}
 	
 	/**
 	 * Sets the position of the sprite
 	 */
 	public void setPosition(float x, float y){
 		this.x = x;
 		this.y = y;
 	}
 
 	@Override
 	public void act(float delta) {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Callback function when a bullet is hit.
 	 * Not to be confused with the hit callback function used by Actor
 	 * to get touch events.
 	 */
 	public void hit() {
 		//TODO: Sound effect or something
 		markToRemove(true);
 	}
 }
