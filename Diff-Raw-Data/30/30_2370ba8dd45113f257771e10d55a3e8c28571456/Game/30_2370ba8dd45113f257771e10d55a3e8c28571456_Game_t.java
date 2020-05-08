 package org.knuth.jsnake.game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JPanel;
import javax.swing.Timer;
 
 
 public class Game extends JPanel implements KeyListener, Runnable{
 	
 	/** Default Serial Version */
 	private static final long serialVersionUID = 1L;
 	/** The Image from the Graphics Object */
 	private Image dbimage;
 	/** The Graphics Object used to draw on */
 	private Graphics dbg;
 	/** The Thread, the Game runs in */
 	private Thread th;
 	/** Count of the current Game-Score */
 	private long score;
 	/** False, if the Game isn't over jet */
 	private boolean roundEnd;
 	/** The Speed of the current Game */
 	public static int SPEED = 10;
 	/** Represents the Size of one Snake part */
 	public static final int SNAKE_PART_SIZE = 10;
 	/** The Snake, which represents the Player */
 	private SnakeHead snake;
 	/** The Pray you need to hunt down */
 	private Prey prey;
 	
 	/**
 	 * Creates a new Snake Game
 	 * @param width Width of the Game-Field
 	 * @param height Height of the Game-Field
 	 */
 	public Game(int width, int height){
 		super();
 		super.setSize(width, height);
 		// Initialize:
 		roundEnd = false;
 		snake = new SnakeHead();
 		prey = new Prey(this.getSize().height, this.getSize().width, 10);
 		// Initialize the Thread:
 		th = new Thread(this);
 	}
 	
 	/**
 	 * Paints Stuff on the JPanel
 	 */
 	@Override
 	public void paint(Graphics g){
 		super.paint(g);
 		// Things for the Layout:
 		this.setBackground(Color.white);
 		this.setForeground(Color.black);
 		// Paint the Score:
 		g.drawString("Score: "+this.score, 15, 15);
 		// Paint Wait:
 		if (!th.isAlive()){
 			g.drawString("Ready when you are... Press a Direction", 200, 200);
 		} 
 		if (roundEnd){	
 			g.drawString("Game Over!", 200, 200);
 			g.drawString("Score: "+score, 200, 220);
 		}
 		// Check collisions:
 		this.collision();
 		// Move the Snake:
 		snake.moveAll();
 		// Draw the Snake:
 		snake.paintSnake(g);
 		// Draw the Pray:
 		prey.paintPrey(g);
 	}
 	
 	/**
 	 * Checks for collision with a Snake Part or the Prey
 	 */
 	private void collision(){
 		// Test if the Pray was hit/eaten:
 		if (prey.isHit(snake.getPoint()) ){
 			// If eaten, add a Part to the Snake:
 			snake.addParts(4);
 			// Increase the Score:
 			this.score += 10;
 		}
 		// Test if the Snake-Head hit a Snake-Part:
 		if (snake.hitSelf()){
 			this.roundEnd = true;
 			System.out.println("Hit!");
 		}
 		// Check if the Player left the Field:
 		if (snake.leftField(this.getSize().width, this.getSize().height)){
 			this.roundEnd = true;
 			System.out.println("Out!");
 		}
 	}
 	
 	/**
 	 * Returns false, while the Player is still playing
 	 * @return true if playing, false if not
 	 */
 	public boolean roundEnd(){
 		return roundEnd;
 	}
 	
 	/**
 	 * Resets everything to play again, when the Round is over
 	 */
 	public void playAgain(){
 		if (roundEnd){
 			this.score = 0;
			this.snake = new SnakeHead();
			this.prey = new Prey(this.getSize().height, this.getSize().width, 10);
			this.roundEnd = false;
 		}
 	}
 	
 	/**
 	 * Returns the Score from the last round played
 	 * @return The Score
 	 */
 	public long getScore(){
 		return score;
 	}
 	
 	/**
 	 * Where the Game repaints...
 	 */
 	@Override
 	public void run() {
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		while (!roundEnd){
 			repaint();
 			try {
 				Thread.sleep(80);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		repaint();
		
		// Wait some Time to restart the Game:
		Timer t = new Timer(1000, new ActionListener() {
			/**
			 * Do it off the Render/Logic Thread so the 
			 *  "Ready ..."-message get's probably painted!
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				playAgain();
				repaint();
			}
		});
		t.setRepeats(false);
		t.start();
		
 		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 	}
 	
	
	
 	/**
 	 * Double-Buffer for smooth drawing
 	 */
 	public void update(Graphics g){
 		// Double buffer:
 		if (dbimage == null){
 			dbimage = createImage(this.getSize().width, this.getSize().height);
 			dbg = dbimage.getGraphics();
 		}
 		dbg.setColor(getBackground());
 		paint(dbg);
 		g.drawImage(dbimage, 0, 0, this);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if (!th.isAlive()){
 			// If the Thread is not alive yet, start it:
			th = new Thread(this);
 			th.start();
 		}
 		if (e.getKeyCode() == KeyEvent.VK_UP){
 			// Move Up
 			snake.moveUp();
 		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
 			// Move Right
 			snake.moveRight();
 		} else if (e.getKeyCode() == KeyEvent.VK_DOWN){
 			// Move Down
 			snake.moveDown();
 		} else if (e.getKeyCode() == KeyEvent.VK_LEFT){
 			// Move Left
 			snake.moveLeft();
 		}
 	}
 
 	// Method Dump...
 	@Override public void keyPressed(KeyEvent e) {}
 	@Override public void keyTyped(KeyEvent e) {}
 
 }
