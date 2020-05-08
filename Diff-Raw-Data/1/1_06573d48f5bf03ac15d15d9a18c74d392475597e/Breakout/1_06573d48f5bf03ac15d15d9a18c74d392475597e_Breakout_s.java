 /*
  * File: Breakout.java
  * ----------------------
  * This program creates the classic "Breakout" game in Java.
  * By Adam Gerson 2013
  * 
  */
 
 import acm.program.*;
 import java.awt.event.*;
 
 /** This class is the main class of the game. */
 public class Breakout extends GraphicsProgram {
 
 	private static final int PAUSE_TIME = 15;
 
 	private static final int BRICKS_PER_ROW = 10;
 	private static final int BRICKS_NUMBER_OF_ROWS = 3;
	private static final int BRICK_WIDTH = 60;
 
 	private Paddle thePaddle;
 	private Ball theBall;
 	private Brick[][] theBricks = new Brick[BRICKS_NUMBER_OF_ROWS][BRICKS_PER_ROW];
 
 	boolean gameOver = false;
 
 	/* Initializes the program */
 	public void init() {
 		thePaddle = new Paddle();
 		theBall = new Ball();
 
 		add(thePaddle.getShape());
 		add(theBall.getShape());
 
 		for (int i = 0; i < BRICKS_PER_ROW; i++) {
 			theBricks[0][i] = new Brick((62 * i) + 1, 5);
 			// System.out.println((30*i)+5);
 			add(theBricks[0][i].getShape());
 		}
 		addMouseListeners();
 	}
 
 	public void run() {
 
 		// waitForClick();
 		while (true) {
 			updateBall();
 			updateBricks();
 			pause(PAUSE_TIME);
 		}
 
 	}
 
 	/* Called on mouse drag to reposition the object */
 	public void mouseMoved(MouseEvent e) {
 		thePaddle.setLocation(e.getX());
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		System.out.println("miss!" + theBall);
 	}
 
 	public void updateBricks() {
 		for (int i = 0; i < BRICKS_PER_ROW; i++) {
 			if (!theBricks[0][i].isAlive()) {
 				remove(theBricks[0][i].getShape());
 			}
 		}
 	}
 
 	public void updateBall() {
 		// System.out.println(theBall);
 		theBall.updatePosition();
 
 		// Check horizontal edges
 		if (theBall.getXpos() > getWidth() || theBall.getXpos() < 0) {
 			theBall.bounceX();
 		}
 
 		// Check top vertical edge
 		if (theBall.getYpos() < 0) {
 			theBall.bounceY();
 		}
 
 		// Check for game over
 		if (theBall.getYpos() > getHeight()) {
 			// TO DO: game over
 		}
 
 		// Check paddle
 		if (theBall.intersects(thePaddle)) {
 			System.out.println("Paddle hit");
 			theBall.bounceY();
 		}
 
 		// Check bricks
 		for (int i = 0; i < BRICKS_PER_ROW; i++) {
 			if (theBall.intersects(theBricks[0][i])) {
 				System.out.println("Brick hit");
 				theBricks[0][i].setAlive(false);
 				theBall.bounceY();
 			}
 		}
 
 	}
 
 	public void playBounceSound() {
 		java.awt.Toolkit.getDefaultToolkit().beep();
 	}
 
 	public void p(String s) {
 		System.out.println(s);
 	}
 
 }
