 package tilt.gameplay;
 
 import java.util.Random;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import tilt.collision.CollisionManager;
 
 public class GameplayState extends BasicGameState
 {
 
 	@Override
 	public void init( GameContainer gc , StateBasedGame game ) throws SlickException
 	{
 
 		gc.setVSync( true );
 		collisionManager = new CollisionManager(); // initialize collision manager
 		
 		// load resources
 		playerImage = new Image("res/player.png");
 		platformImage = new Image("res/platform.png");
 		
 		player = new Player("Player", playerImage, new Vector2f(gc.getWidth()/2-playerImage.getWidth()/2,gc.getHeight()/2), new org.newdawn.slick.geom.Rectangle(0,0,playerImage.getWidth(),playerImage.getHeight()));
 		floor = new Platform("Floor", platformImage, new Vector2f(gc.getWidth()/2-platformImage.getWidth()/2,gc.getHeight()-platformImage.getHeight()), new org.newdawn.slick.geom.Rectangle(0,0 , platformImage.getWidth() , platformImage.getHeight()), 0);
 		
 		//bgImage = new Image( "res/bg.jpg" );
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg , Graphics g ) throws SlickException
 	{
 		//bgImage.draw( 0 , 0 , gc.getWidth() , gc.getHeight() );
 
 		player.render(g);
 		floor.render(g);
 	}
 
 	@Override
	public void update( GameContainer gc , StateBasedGame sbg , int delta ) throws SlickException
 	{
 		//get input
 		input = gc.getInput();
 		
 		//look for pressed keys
 		if( input.isKeyDown( Input.KEY_ESCAPE ) ) gc.exit(); // if escape pressed, exit game
 		
 		//make sure paddle is within bounds of the canvas/window
 		if( input.isKeyDown( Input.KEY_W ) ) player.getPosition().y-=1*delta*PADDLE_SPEED;
 		if( input.isKeyDown( Input.KEY_S ) ) player.getPosition().y+=1*delta*PADDLE_SPEED;
 		if( player.getPosition().y < 10 ) player.getPosition().y=11;
 		if( player.getPosition().y+player.getImage().getHeight() > gc.getHeight()-10 ) player.getPosition().y=gc.getHeight()-11-player.getImage().getHeight();
 		
 		/*switch( currentState )
 		{
 		
 		}*/
 		
 		collisionManager.processCollisions();
 	}
 
 	public int getPlayerLives() { return playerLives; }
 	public void setPlayerLives( int lives ) { this.playerLives = lives; }
 	
 	public CollidableImageObject getFloor() { return floor; }
 	
 	public CollisionManager getCollisionManager() { return collisionManager; }
 	
 	@Override
 	public int getID()
 	{
 		return 1;
 		
 		/*switch(currentState)
 		{
 		case(NORMAL_GAME)
 		{
 			return 1;
 		}
 		}*/
 	}
 	
 	private static enum STATES { NORMAL_GAME };
 	private STATES currentState;
 	
 	private Player player;
 	private Platform floor;
 	
 	private static float PADDLE_SPEED = 0.8f; //paddle speed
 	
 	private int playerLives;
 
 	private Image platformImage;
 	private Image playerImage;
 	private Image bgImage;
 	
 	private Input input;
 	
 	private CollisionManager collisionManager;
 	
 }
