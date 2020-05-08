 package yasith.invaders;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 
 import yasith.invaders.actors.Invader;
 
 /**
  * Holds information about the current state of the Game
  */
 public class GameState {
 
 	// Singleton instance
 	private static GameState sInstance = null;
 	
 	// variables used to store the game state
 	public TextureAtlas atlas; // no use of the atlas being private
 
 	private int lives = 0; // remaining lives
 	
 	// Create an store the invader sprites.
 	// Because we are reusing them.
 	private Sprite mInvaderSprites[] = null;
 
 	// Store the currently active invaders
 	private ArrayList<Invader> mInvadersList = null;
 	
 	// private constructor prevents initialization outside of GameState
 	private GameState() {
 		atlas = new TextureAtlas(Gdx.files.internal("images/pack"));
 	}
 
 	/** Returns the instance of GameState if there is one, otherwise
 	 *	create a new one.
 	 */
 	public static synchronized GameState getInstance() {
 		if(sInstance == null){
 			sInstance = new GameState();
 		}
 		return sInstance;
 	}
 
 	/**
 	 * Return the remaining lives of the player
 	 */
 	public int getLives() {
 		return lives;
 	}
 
 	/**
 	 * Sets the remaining lives of the player
 	 */
 	public void setLives(int lives)	{
 		this.lives = lives;
 	}
 
 	/**
 	 * Returns the sprite of the invader matching to the given row.
 	 * Initializes mInvaderSprites when required;
 	 */
 	public Sprite getInvaderSprite(int row) {
 		// If we haven't created the Sprites, create them now
 		if(mInvaderSprites == null){
 			mInvaderSprites = new Sprite[GameConstants.INVADER_ROWS];
 			
 			for(int i = 0; i < GameConstants.INVADER_ROWS; ++i){
 				mInvaderSprites[i] = 
 						atlas.createSprite(GameConstants.INVADER_SPRITES[row]);
 			}
 		}
 		
 		return mInvaderSprites[row];
 	}
 	
 	/**
 	 * Returns the list of invaders
 	 */
 	public ArrayList<Invader> getInvaderList(){
 		if(mInvadersList == null){
 			mInvadersList = new ArrayList<Invader>();
 		}
 		return mInvadersList;
 	}
 	
 	/**
 	 * Assign the list of invaders
 	 */
 	public void setInvaderList(ArrayList<Invader> lst){
 		mInvadersList = lst;
 	}
 }
