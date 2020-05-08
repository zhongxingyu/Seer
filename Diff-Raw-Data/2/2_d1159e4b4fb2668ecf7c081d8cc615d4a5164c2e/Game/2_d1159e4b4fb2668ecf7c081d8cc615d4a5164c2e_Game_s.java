 /*
  * Game.java
  * 
  * Part of the GameSDK starterkit made for the seminar at "The Gathering 2012 - At the end of the universe"
  * 
  * This source code is provided AS-IS and without any warranty. Use at own risk.
  * You can use and modify this source any way you want, but please send me an E-mail to petterroea@skymiastudios.com
  * if you make a game from it - I would like to keep a list of all games, as i am curious. You may also use this for games
  * that cost money. So it is basically public domain, except that i own the rights to it, but give you the rights to use
  * it for the purposes above. You cant sell this kit. You can sell a game based on it, but not the source code to this kit.
  * Also, this big comment must stay in all code files that originated from the starterkit, no matter how much they are
  * modified.
  * 
  * If you want support for this SDK, feel free to contact me. If you are at "The Gathering 2012", you can
  * look for me in the creative lounge(Where i sit). If not, feel free to send me an E-Mail to the above
  * adress. If you need normal java help, ask a mentor.
  * 
  */
 package net.petterroea.starterkit;
 
 import java.applet.Applet;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 
 import javax.swing.JFrame;
 /**
  * Game main class. Can be run as applet or application
  *
  */
 public class Game extends Applet implements Runnable{
 	/**
 	 * True if the game is running. Should be true all the time
 	 */
 	boolean running = true;
 	/**
 	 * Width of applet
 	 */
 	static int WIDTH = 400;
 	/**
 	 * Height of applet
 	 */
 	static int HEIGHT = 300;
 	/**
 	 * The backBuffer used to avoid flickering
 	 */
 	Image backBuffer;
 	/**
 	 * The Thread the game is run from
 	 */
 	Thread gameThread;
 	/**
 	 * The screen currently showing
 	 */
 	Screen screen;
 	/**
 	 * Reloads the size
 	 */
 	public void reloadSize()
 	{
 		WIDTH = this.getWidth();
 		HEIGHT = this.getHeight();
 	}
 	/**
 	 * Start method
 	 * @param args The arguments
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Petterroea - BursdagsLAN 2012 invite");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(WIDTH, HEIGHT);
 		Game game = new Game();
 		frame.add(game);
 		frame.setVisible(true);
 		frame.setSize(WIDTH + (WIDTH - game.getWidth()), HEIGHT + (HEIGHT - game.getHeight()));
 		game.init();
 		game.start();
 	}
 	/**
 	 * Initialises the game
 	 */
 	public void init()
 	{
 		reloadSize(); //F tak i strrelsen s vi kan regne noen ting som trenger det
 		//Initkode her
 		screen = new Screen();
 	}
 	/**
 	 * Start the game
 	 */
 	public void start()
 	{
 		gameThread = new Thread(this);
 		gameThread.start();
 	}
 	/**
 	 * Stop the game
 	 */
 	public void stop()
 	{
 		running = false;
 		try {
 			gameThread.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 	@Override
 	/**
 	 * Do not call this. It is automatically called by start()
 	 */
 	public void run()
 	{
 		long lastUpdate = System.currentTimeMillis();
 		long lastFpsUpdate = System.currentTimeMillis();
 		int fps = 0; //FPS last second
 		int frames = 0;
 		while(running)
 		{
 			reloadSize();
 			System.out.println("W: " + WIDTH + ", H: " + HEIGHT);
 			int delta = (int) (System.currentTimeMillis() - lastUpdate);
 			lastUpdate = System.currentTimeMillis();
 			if(System.currentTimeMillis() - lastFpsUpdate > 100)
 			{
 				fps = frames;
 				frames = 0;
 				lastFpsUpdate = System.currentTimeMillis();
 			}
 			frames++;
 			if(backBuffer == null || backBuffer.getWidth(null) != this.getWidth() || backBuffer.getHeight(null) != this.getHeight())
 			{
 				backBuffer = this.createImage(this.getWidth(), this.getHeight());
 			}
 			Graphics g = backBuffer.getGraphics();
 			g.setColor(Color.white);
 			g.fillRect(0, 0, WIDTH, HEIGHT);
 			g.setColor(Color.black);
 			screen.tick(delta, g);
 			this.getGraphics().drawImage(backBuffer, 0, 0, null);
 		}
 	}
 }
