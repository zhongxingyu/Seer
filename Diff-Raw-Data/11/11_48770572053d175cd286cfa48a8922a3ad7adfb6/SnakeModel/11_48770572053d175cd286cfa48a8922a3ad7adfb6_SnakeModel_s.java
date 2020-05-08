 package game;
 
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.ImageObserver;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 /**
  * Keeps track of snake's size and movement
  *
  */
 public class SnakeModel {
 	
 	private Image img;
 	private int x, y;
 	private int dx, dy;
 	
 	private int r1, r2;
 	
 	// constants
 	// TODO confirm snake radius
 	/**
 	 * The distance from the centre of the snake to the outer edge
 	 */
 	private final int DEFAULT_SNAKE_RADIUS = 250;
 	
 	// TODO this should be the actual snake width
 	private final int SNAKE_WIDTH = 30;
 	
 	private final int MAX_SNAKE_RADIUS = 768;
 	
 	private final int MOVE_BY_AMOUNT = 10; // pixels
 	
 	private float CHANGE_DIAMETER_BY = 0.1f; // pixels
 	
 	private float angle = 0.0f;
 	
 	// constructor
 	SnakeModel(){
 		x = y = 0;
 		dx = dy = 5;
 		try {
 			img = ImageIO.read(new File("test0036.png"));
 		}catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 /*
 	SnakeModel()
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
 		try {
 			img = ImageIO.read(new File("src/resources/snakes2.png"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 	}
 	
 	/**
 	 * Increases the radius of the snake
 	 * @param growRadiusBy
 	 */
 	public void grow(){
 		if(CHANGE_DIAMETER_BY < 3) CHANGE_DIAMETER_BY += 0.0005f;
 	}
 	
 	/**
 	 * Decreases the radius of the snake
 	 * @param shrinkRadiusBy
 	 */
 	public void shrink(){
 		System.err.println("SHRINK");
 		if(CHANGE_DIAMETER_BY > 0.005) CHANGE_DIAMETER_BY -= 0.01f;
 	}
 	
 	public void spin(){
 		angle -= 0.05f;
 		if(angle <= -2*Math.PI) angle = 0.0f;
 	}
 	
 	public void draw(Graphics g) {
 		Graphics2D g2d = (Graphics2D)g;
 		g2d.setColor(Color.red);
 		g2d.fillRect(x+img.getWidth(null)/2,  y+img.getHeight(null)/2, 50, 50);
 		AffineTransform at = new AffineTransform();
 		at.translate(x+img.getWidth(null)/2, y+img.getHeight(null)/2);
 		at.scale(CHANGE_DIAMETER_BY, CHANGE_DIAMETER_BY);
 		at.rotate(angle);
 		//g2d.scale(CHANGE_DIAMETER_BY, CHANGE_DIAMETER_BY);
 		//g2d.translate(x+img.getWidth(null)/2, y+img.getHeight(null)/2);
 		//g2d.scale(CHANGE_DIAMETER_BY, CHANGE_DIAMETER_BY);
 		//g2d.rotate(angle);
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2d.setTransform(at);
 		g2d.drawImage(img, -img.getWidth(null)/2, -img.getHeight(null)/2, img.getWidth(null), img.getHeight(null), null);
 		r1 = img.getWidth(null)/2 - 50;
 		r2 = img.getWidth(null)/2;
 		/*g2d.fillOval(x, y, r2, r2);
 		g2d.setColor(Color.RED);
 		g2d.fillOval(x + img.getWidth(null)/2, y + img.getHeight(null)/2, r1, r1);*/
 	}
 	
 	/**
 	 * Gets the upper left hand corner of the snake
 	 * @return
 	 */
 	public Point2D.Double getOrigin()
 	{
 		//return new Point2D.Double(outerEdge.x, outerEdge.y);
 		return null;
 	}
 	
 	/**
 	 * Sets the position of the upper left hand corner
 	 * of the snake
 	 * @param origin
 	 */
 	public void setOrigin(Point2D.Double origin)
 	{
 		/*double outerRadius = outerEdge.height;
 		outerEdge.setFrame(origin.x, origin.y, outerRadius, outerRadius);
 		double innerRadius = innerEdge.height;
 		innerEdge.setFrame(	origin.x + SNAKE_WIDTH,
 							origin.y + SNAKE_WIDTH,
 							innerRadius,
 							innerRadius);*/
 		
 	}
 	
 	/**
 	 * Returns the radius of the snake
 	 * @return
 	 */
 	public double getOuterRadius()
 	{
 		//return outerEdge.height / 2;
 		return 0d;
 	}
 	
 	/**
 	 * Returns the inner radius of the snake
 	 * @return
 	 */
 	public double getInnerRadius()
 	{
 		//return innerEdge.height / 2;
 		return 0d;
 	}
 	
 	
 	/**
 	 * Moves the snake to the up by a certain amount
 	 */
 	public void moveUp(){
 			
 			/*outerEdge.setFrame(	outerEdge.x,
 								outerEdge.y-MOVE_BY_AMOUNT,
 								outerEdge.width,
 								outerEdge.height);
 			innerEdge.setFrame(	innerEdge.x,
 					innerEdge.y+MOVE_BY_AMOUNT,
 					innerEdge.width,
 					innerEdge.height);*/
 		y -= dy;
 	}
 	/**
 	 * Moves the snake to the down by a certain amount
 	 */
 	public void moveDown(){
 		
 		/*outerEdge.setFrame(	outerEdge.x,
 							outerEdge.y+MOVE_BY_AMOUNT,
 							outerEdge.width,
 							outerEdge.height);
 		innerEdge.setFrame(	innerEdge.x,
 				innerEdge.y-MOVE_BY_AMOUNT,
 				innerEdge.width,
 				innerEdge.height);*/
 		y += dy;
 	}
 	/**
 	 * Moves the snake to the right by a certain amount
 	 */
 	public void moveRight(){
 		
 		/*outerEdge.setFrame(	outerEdge.x+MOVE_BY_AMOUNT,
 							outerEdge.y,
 							outerEdge.width,
 							outerEdge.height);
 		innerEdge.setFrame(	innerEdge.x+MOVE_BY_AMOUNT,
 				innerEdge.y,
 				innerEdge.width,
 				innerEdge.height);*/
 		x += dx;
 	}
 
 	/**
 	 * Moves the snake to the left by a certain amount
 	 */
 	public void moveLeft(){
 		
 		/*outerEdge.setFrame(	outerEdge.x-MOVE_BY_AMOUNT,
 							outerEdge.y,
 							outerEdge.width,
 							outerEdge.height);
 		innerEdge.setFrame(	innerEdge.x-MOVE_BY_AMOUNT,
 				innerEdge.y,
 				innerEdge.width,
 				innerEdge.height);*/
 		x -= dx;
 	}
 @Override
 	public String toString(){
 		/*String self = "Origin of outer circ = (" + outerEdge.x + ", " 
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
 		*/
 		return "";
 	}
 
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
 		
 		
 	}
 	
 }
