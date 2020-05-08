 package com.jaredpearson.game.demo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.jaredpearson.game.core.ArcDegree;
 import com.jaredpearson.game.core.Color;
 import com.jaredpearson.game.platform.Abstract2DGameState;
 import com.jaredpearson.game.platform.Drawable;
 import com.jaredpearson.game.platform.GameTime;
 import com.jaredpearson.game.platform.Graphics2D;
 import com.jaredpearson.game.platform.KeyEvent;
 import com.jaredpearson.game.platform.KeyListener;
 import com.jaredpearson.game.platform.Platform;
 import com.jaredpearson.game.platform.Player;
 import com.jaredpearson.game.platform.State;
 import com.jaredpearson.game.platform.Updatable;
 
 /**
  * Basic implementation of the playing game state.
  * @author jaredp
  */
 public class PlayingGameState
 	extends Abstract2DGameState
 	implements State
 {
 	private Player player = null;
 	private List<Object> entities = new ArrayList<Object>();
 	
 	/**
 	 * Initializes the objects/entities used in the game.
 	 */
 	protected void initialize(final Platform platform)
 	{
 		//set up the key listener
 		platform.getInputEngine().addKeyListener(new KeyListener() {
 			public void keyReleased(KeyEvent e) {
 			}
 			
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == 37)
 				{
 					platform.getConsole().writeStatus("Moving player west");
					player.setFacing(ArcDegree.DEGREE_180);
 					player.moveTo(player.getX() - player.getMoveDistance(), player.getY());
 				}
 				else if(e.getKeyCode() == 38)
 				{
 					platform.getConsole().writeStatus("Moving player north");
					player.setFacing(ArcDegree.DEGREE_270);
 					player.moveTo(player.getX(), player.getY() - player.getMoveDistance());
 				}
 				else if(e.getKeyCode() == 39)
 				{
 					platform.getConsole().writeStatus("Moving player east");
					player.setFacing(ArcDegree.DEGREE_0);
 					player.moveTo(player.getX() + player.getMoveDistance(), player.getY());
 				}
 				else if(e.getKeyCode() == 40)
 				{
 					platform.getConsole().writeStatus("Moving player south");
					player.setFacing(ArcDegree.DEGREE_90);
 					player.moveTo(player.getX(), player.getY() + player.getMoveDistance());
 				}
 			}
 		});
 		
 		entities.add(new Grid(0, 0, 640, 480));
 		
 		final Rectangle rectangle = new Rectangle(0, 0, 200, 100);
 		KeyFrame keyFrame1 = new KeyFrame(1000, new Updatable() {
 			public void update(GameTime gameTime) {
 				rectangle.moveTo(100, 100);
 			}
 		});
 		KeyFrame keyFrame2 = new KeyFrame(2000, new Updatable() {
 			public void update(GameTime gameTime) {
 				rectangle.moveTo(200, 200);
 			}
 		});
 		Animation rectangleAnimation = new Animation(keyFrame1, keyFrame2);
 		entities.add(rectangleAnimation);
 		entities.add(rectangle);
 		
 		//create the player
 		player = new Player();
 		
 		//set the starting player position, facing south
 		player.moveTo(100, 100);
		player.setFacing(ArcDegree.DEGREE_90);
 		
 		//create the player's ui
 		PlayerUI playerUI = new PlayerUI(player);
 		entities.add(playerUI);
 	}
 	
 	/**
 	 * Updates the objects/entities with the current time.
 	 */
 	protected void update(GameTime gameTime)
 	{
 		for(Object entity : entities)
 		{
 			if(entity instanceof Updatable)
 			{
 				((Updatable) entity).update(gameTime);
 			}
 		}
 	}
 
 	/**
 	 * Draws the objects/entities to the current graphics buffer.
 	 */
 	protected void draw(Graphics2D graphics)
 	{
 		//clear the screen
 		graphics.setColor(Color.BLACK);
 		graphics.fillRectangle(0,0,640,480);
 		
 		//set the default pen color
 		graphics.setColor(Color.WHITE);
 		
 		for(Object entity : entities)
 		{
 			if(entity instanceof Drawable)
 			{
 				((Drawable) entity).draw(graphics);
 			}
 		}
 	}
 }
