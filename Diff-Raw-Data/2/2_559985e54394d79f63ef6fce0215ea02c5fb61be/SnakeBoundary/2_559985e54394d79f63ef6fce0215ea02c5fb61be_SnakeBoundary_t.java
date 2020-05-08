 
 package game;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.geom.Ellipse2D;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 /**
  * Keeps track of snake's size and movement
  * The old SnakeModel that used two ellipses
  * Used now so we can still test CircleObstacle
  * and GameProcessing
  *
  */
 public class SnakeBoundary {
 	
 	// instance variables
 	Ellipse2D.Double outerEdge;
 	Ellipse2D.Double innerEdge;
 	
 	//private Image img;
 	
 	// constants
 	// TODO confirm snake radius
 	/**
 	 * The distance from the centre of the snake to the outer edge
 	 */
 	private final int DEFAULT_SNAKE_RADIUS = 250;
 	
 	// TODO this should be the actual snake width
 	private final int SNAKE_WIDTH = 30;
 	
 //	private final int MAX_SNAKE_RADIUS = 600;
 	
	private final int MOVE_BY_AMOUNT = 5; // pixels
 	
 //	private final int CHANGE_DIAMETER_BY = 50; // pixels
 	private final int INNER = 0;
 	private final int OUTER = 1;
 	
 	private Dimension[][] stageSizes;
 	
 	// synced with SnakeSpriteManager stage counter
 	private int stageCounter = 3;
 	private final int NUM_STAGES = 6;
 	
 	// constructor
 	SnakeBoundary()
 	{
 		
 		final int DEFAULT_X_POS = 300;
 		final int DEFAULT_Y_POS = 300;
 		outerEdge = new Ellipse2D.Double(
 					DEFAULT_X_POS, 
 					DEFAULT_Y_POS,
 					DEFAULT_SNAKE_RADIUS + DEFAULT_SNAKE_RADIUS,
 					DEFAULT_SNAKE_RADIUS + DEFAULT_SNAKE_RADIUS);
 		double innerRadius = DEFAULT_SNAKE_RADIUS - SNAKE_WIDTH;
 		innerEdge = new Ellipse2D.Double(
 					DEFAULT_X_POS + SNAKE_WIDTH,
 					DEFAULT_Y_POS + SNAKE_WIDTH,
 					innerRadius + innerRadius,
 					innerRadius + innerRadius);
 		
 		stageSizes = new Dimension[NUM_STAGES][];
 		for (int i = 0; i < NUM_STAGES; i++)
 		{
 			stageSizes[i] = new Dimension[2];
 		}
 		stageSizes[0][INNER] = new Dimension(370, 370);
 		stageSizes[0][OUTER] = new Dimension(475, 475);
 		stageSizes[1][INNER] = new Dimension(253, 253);
 		stageSizes[1][OUTER] = new Dimension(363, 363);
 		stageSizes[2][INNER] = new Dimension(235,235);
 		stageSizes[2][OUTER] = new Dimension(339,339);
 		stageSizes[3][INNER] = new Dimension(163,163);
 		stageSizes[3][OUTER] = new Dimension(280,280);
 		stageSizes[4][INNER] = new Dimension(100,100);
 		stageSizes[4][OUTER] = new Dimension(210,210);
 		stageSizes[5][INNER] = new Dimension(100,100);
 		stageSizes[5][OUTER] = new Dimension(210,210);
 	}
 	
 	/**
 	 * Getter method for 2D array of snake image properties
 	 * 
 	 */
 	public Dimension[][] getDimArray() {
 		return stageSizes;
 	}
 	
 	/**
 	 * Getter method for stage counter
 	 * 
 	 */
 	public int getCounter() {
 		return stageCounter;
 	}
 	
 	/**
 	 * Increases the radius of the snake
 	 * @param growRadiusBy
 	 */
 	public void grow()
 	{
 		if (stageCounter > 0 && stageCounter <= 5)
 		{
 			stageCounter--;
 		}
 		else if (stageCounter == 0)
 		{
 			return;
 		}
 		this.outerEdge.width = stageSizes[stageCounter][OUTER].width;
 		this.outerEdge.height = stageSizes[stageCounter][OUTER].height;
 		this.innerEdge.width = stageSizes[stageCounter][INNER].width;
 		this.innerEdge.height = stageSizes[stageCounter][INNER].height;
 	}
 	
 	/**
 	 * Decreases the radius of the snake
 	 * @param shrinkRadiusBy
 	 */
 	public void shrink()
 	{
 		if (stageCounter >= 0 && stageCounter < 5)
 		{
 			stageCounter++;
 		}
 		else if (stageCounter == 5)
 		{
 			return;
 		}
 		this.outerEdge.width = stageSizes[stageCounter][OUTER].width;
 		this.outerEdge.height = stageSizes[stageCounter][OUTER].height;
 		this.innerEdge.width = stageSizes[stageCounter][INNER].width;
 		this.innerEdge.height = stageSizes[stageCounter][INNER].height;
 	}
 	/*
 	public void draw(Graphics2D g) {
 		AffineTransform at = new AffineTransform();
 		g.drawImage(img, at, null);
 	}*/
 	
 	/**
 	 * Gets the upper left hand corner of the snake
 	 * @return
 	 */
 	public Point2D.Double getOrigin()
 	{
 		return new Point2D.Double(outerEdge.x, outerEdge.y);
 	}
 	
 	/**
 	 * Sets the position of the upper left hand corner
 	 * of the snake
 	 * @param origin
 	 */
 	public void setOrigin(Point2D.Double origin)
 	{
 		double outerRadius = outerEdge.height;
 		outerEdge.setFrame(origin.x, origin.y, outerRadius, outerRadius);
 		double innerRadius = innerEdge.height;
 		innerEdge.setFrame(	origin.x + SNAKE_WIDTH,
 							origin.y + SNAKE_WIDTH,
 							innerRadius,
 							innerRadius);
 		
 	}
 	
 	/**
 	 * Returns the radius of the snake
 	 * @return
 	 */
 	public double getOuterRadius()
 	{
 		return outerEdge.height / 2;
 	}
 	
 	/**
 	 * Returns the inner radius of the snake
 	 * @return
 	 */
 	public double getInnerRadius()
 	{
 		return innerEdge.height / 2;
 	}
 	
 	
 	/**
 	 * Moves the snake to the up by a certain amount
 	 */
 	public void moveUp(){
 			
 			outerEdge.setFrame(	outerEdge.x,
 								outerEdge.y+MOVE_BY_AMOUNT,
 								outerEdge.width,
 								outerEdge.height);
 			innerEdge.setFrame(	innerEdge.x,
 					innerEdge.y+MOVE_BY_AMOUNT,
 					innerEdge.width,
 					innerEdge.height);
 		
 	}
 	/**
 	 * Moves the snake to the down by a certain amount
 	 */
 	public void moveDown(){
 		
 		outerEdge.setFrame(	outerEdge.x,
 							outerEdge.y-MOVE_BY_AMOUNT,
 							outerEdge.width,
 							outerEdge.height);
 		innerEdge.setFrame(	innerEdge.x,
 				innerEdge.y-MOVE_BY_AMOUNT,
 				innerEdge.width,
 				innerEdge.height);
 	
 	}
 	/**
 	 * Moves the snake to the right by a certain amount
 	 */
 	public void moveRight(){
 		
 		outerEdge.setFrame(	outerEdge.x+MOVE_BY_AMOUNT,
 							outerEdge.y,
 							outerEdge.width,
 							outerEdge.height);
 		innerEdge.setFrame(	innerEdge.x+MOVE_BY_AMOUNT,
 				innerEdge.y,
 				innerEdge.width,
 				innerEdge.height);
 	
 	}
 
 	/**
 	 * Moves the snake to the left by a certain amount
 	 */
 	public void moveLeft(){
 		
 		outerEdge.setFrame(	outerEdge.x-MOVE_BY_AMOUNT,
 							outerEdge.y,
 							outerEdge.width,
 							outerEdge.height);
 		innerEdge.setFrame(	innerEdge.x-MOVE_BY_AMOUNT,
 				innerEdge.y,
 				innerEdge.width,
 				innerEdge.height);
 	
 	}
 	
 	
 
 //	/**
 //	 * Returns the amount to change the diameter of the
 //	 * snake by based on the stage and if it is growing or shrinking
 //	 * @param growing whether or not the snake is growing
 //	 * @return
 //	 */
 //	public float changeDiameterBy(boolean growing)
 //	{
 //		if (growing)
 //		{
 //			if (stageCounter == 0) {
 //				return 0;
 //			}
 //			
 //		}
 //	}
 	
 	
 	@Override
 	public String toString(){
 		String self = "Origin of outer circ = (" + outerEdge.x + ", " 
 					+ outerEdge.y 
 					+ ")\n";
 		self +="Inner Origin: = ("+innerEdge.x + ", " + innerEdge.y +
 				")\n";
 		self += "Outer radius = "+outerEdge.height/2+"\n";
 		self += "Inner radius = "+innerEdge.height/2+"\n";
 		
 		self += "difference between heights = " + (outerEdge.height - innerEdge.height)
 				+"\n";
 		self += "difference between widths = " + (outerEdge.width - innerEdge.width)
 				+"\n";
 		self += "Snake width (should be half the above) = " + SNAKE_WIDTH;
 				
 		return self;
 		
 	}
 /*
 	public static void main(String[] args)
 	{
 		System.out.println("Default snake");
 		SnakeModel s1 = new SnakeModel();
 		System.out.println(s1);
 		System.out.println();
 		
 		System.out.println("Testing move up");
 		s1.moveUp();
 		System.out.println(s1);
 
 		System.out.println();
 		System.out.println("Testing move down");
 		s1.moveDown();
 		System.out.println(s1);
 		System.out.println();
 		System.out.println("Testing move left");
 		s1.moveLeft();
 		System.out.println(s1);
 		System.out.println();
 		System.out.println("Testing move right");
 		s1.moveRight();
 		System.out.println(s1);
 		
 		System.out.println("Testing grow");
 		s1.grow();
 		System.out.println(s1);
 		
 		System.out.println("\nTesting shrink");
 		s1.shrink();
 		System.out.println(s1);
 		System.out.println();
 		s1.shrink();
 		System.out.println(s1);
 		System.out.println("\nTesting that you can't shrink inner circle past 0");
 		for (int i = 0; i < 10; i++)
 		{
 			s1.shrink();
 		}
 		System.out.println(s1);
 			
 			
 	}*/
 		
 }
 
 
