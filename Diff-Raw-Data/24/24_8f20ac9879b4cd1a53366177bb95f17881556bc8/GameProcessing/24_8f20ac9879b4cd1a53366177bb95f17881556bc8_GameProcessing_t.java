 package game;
 
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import obstacles.Obstacle;
 import obstacles.CircleObstacle;
 
 import processing.core.*;
 
 
 public class GameProcessing extends PApplet
 {
 	// constants
 	private static final int GROW_EVERY_NUM_LOOPS = 20;
 	
 	// game instance fields
 	private int gameTime = 0;
 	private int score = 0;
 	/**
 	 * Counts the number of times through the loops.
 	 * Every GROW_EVERY_NUM_LOOPS times, grow the snake
 	 * then reset to 0
 	 */
 	private int loopCounter;
 	
 	private boolean upKeyPressed = false;
 	private boolean downKeyPressed = false;
 	private boolean leftKeyPressed = false;
 	private boolean rightKeyPressed = false;
 	private boolean shrinkKeyPressed = false;
 	
 	private boolean hasCaughtInside = false;
 	
 	Wall wall;
 	SnakeModel snakeModel;
 	PImage snakeSprite;
 	SnakeSpriteManager snakeManager;
 	
 	// look and feel instance fields
 	
 	
 	
 	@Override
 	/**
 	 * Initializes everything
 	 */
 	public void setup()
 	{
		ellipseMode(CORNER);
		
 		// set window size
 		final int WINDOW_X = 600;
 		final int WINDOW_Y = 600;
 		size(WINDOW_X, WINDOW_Y);
 		
 		// set frame rate
 		frameRate(4);
 		
 		loop();
 		
 		// set background colour
 		background(0, 0,  0xff); // rgb
 		
 		// initialize instance classes
 		snakeModel = new SnakeModel();
 		double originX = WINDOW_X / 2.0 - snakeModel.getOuterRadius();
 		double originY = WINDOW_Y / 2.0 - snakeModel.getOuterRadius();
 		Point2D.Double origin = new Point2D.Double(originX, originY);
 		snakeModel.setOrigin(origin);
 		final int DEFAULT_NUM_OBSTACLES = 5;
 		wall = new Wall(DEFAULT_NUM_OBSTACLES, snakeModel);
 		wall.createObstacle();
 		
 		// initialize snake sprite
 		snakeManager = new SnakeSpriteManager();
 		String snakeName = snakeManager.getSpriteName();
 		snakeSprite = loadImage(snakeName);
		//image(snakeSprite, (float) originX, (float) originY);
		fill(0xff, 0, 0);
		ellipseMode(CENTER);
		ellipse((float) originX, (float) originY, (float)snakeModel.getOuterRadius(), (float)snakeModel.getOuterRadius());

		ellipseMode(CENTER);
		ellipse((float) origin.x, (float) origin.x, (float)snakeModel.getInnerRadius(), (float)snakeModel.getInnerRadius());
 	}
 	
 	/**
 	 * Essentially the game loop
 	 */
 	public void draw()
 	{
 		boolean gameover = false;
 		
 		background(0, 0, 0xFF); // rgb
 		
 		// check for key presses
 		if (key == CODED)
 		{
 			if (key == DOWN)
 			{
 				upKeyPressed = true;
 			}
 			else if (key == UP)
 			{
 				downKeyPressed = true;
 			}
 			else if (key == LEFT)
 			{
 				leftKeyPressed = true;
 			}
 			else if (key == RIGHT)
 			{
 				rightKeyPressed = true;
 			}
 		}
 		else if (key == ' ')
 		{
 			shrinkKeyPressed = true;
 		}
 		else if (key == 's' || key == 'S')
 		{
 			upKeyPressed = true;
 		}
 		else if (key == 'w' || key == 'W')
 		{
 			downKeyPressed = true;
 		}
 		else if (key == 'a' || key == 'A')
 		{
 			leftKeyPressed = true;
 		}
 		else if (key == 'd' || key == 'D')
 		{
 			rightKeyPressed = true;
 		} 
 
 		
 		// auto grow snake model
 		if (loopCounter % GROW_EVERY_NUM_LOOPS == 0)
 		{
 			snakeModel.grow();
 			snakeManager.grow();
 			String snakeName = snakeManager.getSpriteName();
 			snakeSprite = loadImage(snakeName);
 			loopCounter = 1;			
 		}
 		else
 		{
 			loopCounter++;
 		}
 		
 		
 		// handle key presses
 		
 		if(upKeyPressed){
 			snakeModel.moveUp();
 		}
 		if(downKeyPressed){
 			snakeModel.moveDown();
 		}
 		if(rightKeyPressed){
 			snakeModel.moveRight();
 		}
 		if(leftKeyPressed){
 			snakeModel.moveLeft();
 		}
 		if(shrinkKeyPressed){
 			snakeModel.shrink();
 			snakeManager.shrink();
 			String snakeName = snakeManager.getSpriteName();
 			snakeSprite = loadImage(snakeName);
 		}
 		Point2D.Double origin = snakeModel.getOrigin();
		//image(snakeSprite, (float)origin.x, (float)origin.y);

		fill(0xff, 0, 0);
		ellipseMode(CENTER);
		ellipse((float) origin.x, (float) origin.y, (float)snakeModel.getOuterRadius(), (float)snakeModel.getOuterRadius());
		fill(0xff, 0xff, 0);

		ellipseMode(CENTER);
		ellipse((float) origin.x, (float) origin.y, (float)snakeModel.getInnerRadius(), (float)snakeModel.getInnerRadius());
 		
 		// reset key pressed
 		upKeyPressed = false;
 		downKeyPressed = false;
 		rightKeyPressed = false;
 		leftKeyPressed = false;
 		shrinkKeyPressed = false;
 		
 		// reset the last key character
 		key = '0';
 		
 		// update obstacles
 		try
 		{
 			ArrayList<Obstacle> obs = wall.getObstacles();
 			for (Obstacle o : obs)
 			{
 				o.update();
 				Ellipse2D.Float circleModel = ((CircleObstacle) o).circle;
 				float circleX, circleY, circleHeight, circleWidth;
 				circleX = (float)circleModel.x;
 				circleY = (float)circleModel.y;
 				circleHeight = (float)circleModel.height;
 				circleWidth = (float)circleModel.width;
 				ellipseMode(CORNER);
				fill(0, 0xff, 0);
 				ellipse(circleX, circleY, circleWidth, circleHeight);
 			}
 		}
 		catch (GameOverException e)
 		{
 			noLoop();
 			showGameOverScene();
 		}
 		
 		// check for captures
 		
 		
 		// redraw stuff
 		
 		// regenerate obstacles
 		int obstaclesAtATime = 1;
 		if (wall.getObstacles().size() < obstaclesAtATime)
 		{
 			wall.createObstacle();
 		}
 		CircleObstacle mycircle = (CircleObstacle) wall.getObstacles().get(0);
 		if (mycircle.circle.getWidth() == 0)
 			wall.getObstacles().remove(0);
 		
 	}
 
 	private void showGameOverScene() {
 		// TODO Auto-generated method stub
 		// TODO replace this for god's sake
 		JOptionPane.showMessageDialog(this, "GAME OVER", "YOU LOSE", JOptionPane.ERROR_MESSAGE);
 	}
 }
