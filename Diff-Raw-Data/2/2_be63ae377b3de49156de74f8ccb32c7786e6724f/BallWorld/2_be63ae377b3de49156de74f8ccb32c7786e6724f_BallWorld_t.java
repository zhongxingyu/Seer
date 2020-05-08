 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.Random;
 
 /**
  * A world of balls.
  * 
  * @author Michael Søndergaard
  * @author Mathias Rav
  */
 public class BallWorld extends World
 {
 	private Random r;
 	private final int ballRadius;
 	private Vector gravity;
 
 	/**
 	 * Constructor for objects of class BallWorld.
 	 */
 	public BallWorld()
 	{	
		super(540, 480, 1); 
 		ballRadius = findRadius();
 		r = new Random();
 		setGravity(Vector.zero());
 		addBalls(10);
 	}
 
 	private int findRadius() {
 		Ball b = new Ball();
 		return b.radius;
 	}
 
 	public void addBalls(int amount) {
 		for (int i = 0; i < amount; ++i) {
 			addBall();
 		}
 	}
 
 	public void addBall() {
 		final int lowerX = ballRadius;
 		final int lowerY = ballRadius;
 		final int upperX = getWidth()-ballRadius;
 		final int upperY = getHeight()-ballRadius;
 		double direction = 2.0*Math.PI*r.nextDouble();
 		double speed = 3.5+r.nextGaussian();
 		addBall(lowerX+r.nextInt(upperX-lowerX), lowerY+r.nextInt(upperY-lowerY), speed*Math.cos(direction), speed*Math.sin(direction));
 	}
 	public void addBall(int x, int y, double vX, double vY) {
 		addObject(new Ball(vX, vY), x, y);
 	}
 
 	public void setGravity(Vector v) {
 		gravity = v;
 	}
 	public void setDownwardsGravity(double g) {
 		gravity = new Vector(0, g);
 	}
 	public Vector getGravity() {
 		return gravity;
 	}
 }
