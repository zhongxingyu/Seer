 package trivia;
 
 import java.applet.Applet;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 public class Trivia extends Applet implements Runnable, MouseListener, KeyListener
 {
 	private static final long serialVersionUID = 1L;
 	private Thread th = new Thread(this); //Game thread
 	private GameEngine engine;
 	private Boolean debugMode = false;
 	private Actors actors;
 	
 	/**
 	 * Creates a new Trivia game.
 	 * @param debug If True, game prints out debug information.
 	 */
 	public Trivia(Boolean debug)
 	{
 		debugMode = debug;
 	}
 
 	/**
 	 * Called when game is first initialized. Resets all values to default state.
 	 */
 	public void init()
 	{
 		addMouseListener(this);
 		actors = new Actors(debugMode);
 		actors.setSize(WIDTH, HEIGHT);
 		engine = new GameEngine(actors, debugMode);
 		engine.setMode(engine.mainMenu);
 	}
 	
 	/**
 	 * Starts the game thread. Must be called from out of this class.
 	 */
 	public void start()
 	{
 		th.start();
 	}
 	
 	/**
 	 * Called to run the game. Will continue running until game is exitted.
 	 * All game logic is called from here.
 	 */
 	public synchronized void run() 
 	{
 		//run until stopped
 		while (true)
 		{			
 			//controls game flow
 			engine.run();
 			
 			//repaint applet
 			repaint();
 			
 			try
 			{
 				long timeIn = System.currentTimeMillis();
 				wait(); //wait for applet to draw
 				long timeOut = System.currentTimeMillis();
 				while (timeOut-timeIn < 10)
 				{
 					timeOut = System.currentTimeMillis();
 				}
 			}
 			catch (Exception ex)
 			{
 				log(ex.toString());
 			}
 		}
 	}
 	
 	/**
 	 * Updates the graphics of the game using a double buffer system.
 	 */
 	public void update(Graphics g)
 	{	
 		//start buffer
 		Image dbImage = createImage(getWidth(), getHeight());
 		Graphics dbg = dbImage.getGraphics();
 		
 		//clear screen in background
 		dbg.setColor(getBackground());
 		dbg.fillRect(0, 0, getWidth(), getHeight());
 		
 		//draw elements in background
 		dbg.setColor(getForeground());
 		paint(dbg);
 		
 		//draw image on screen
 		g.drawImage(dbImage, 0, 0, this);
 	}
 	
 	/**
 	 * Only called from update(Graphics g).
 	 * Paints all objects and menus in the game.
 	 */
 	public void paint(Graphics g)
 	{
 		engine.setWindowSize(getWidth(), getHeight());
 		engine.paint(g);
 		super.paint(g);
 		
 		synchronized(this) 
 		{
 			notifyAll();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.applet.Applet#resize(int, int)
 	 */
 	@Override
 	public void resize(int width, int height) {
 		if (width < 300)
 			width = 300;
 		if (height < 300)
 			height = 300;
 		super.resize(width, height);
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
		engine.clickedAt(e.getX(), e.getY());
 	}

 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
		
 	}
 
 	/**
 	 * Debug tool.
 	 * Used to print a String if Debug mode is enabled.
 	 * @param s String to print.
 	 */
 	private void log(String s) {
 		if (debugMode)
 		{
 			System.out.println(s);
 		}
 	}
 }
