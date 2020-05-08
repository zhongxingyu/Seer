 package core;
 
 
 import graphics.GameWindow;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 public class Game {
 	
 	// Game variables
 	private boolean isRunning;
 	private JFrame window;
 	
 	// Loop control
 	final int UPDATES_PER_SECOND = 25;
 	final int SKIP_UPDATES_NANO = 1000000 / UPDATES_PER_SECOND;
 	final int MAX_FRAME_SKIP = 5;
 	
 	public Game()
 	{
 		// Initialize game engine
 		init();
 		
 		// Create and show window
 		window = new GameWindow();
 		window.setVisible(true);
 		
 		// Start main loop
 		Thread gameLoop = new Thread()
 		{
 			@Override
 			public void run()
 			{
 				startGameLoop();
 			}
 		};
 		gameLoop.start();
 	}
 	
 	private void init()
 	{
 
 	}
 	
 	private void update()
 	{
		// TODO : Do some real updating
 		try 
 		{
 			Thread.sleep(100);
 		} 
 		catch (InterruptedException e) 
 		{
 			
 		}
 	}
 	
 	private void render(double interpolation)
 	{
 		window.repaint();
 	}
 	
 	private void startGameLoop()
 	{
 		int numFrameSkip;
 		long nextUpdateNanos = System.nanoTime();
 		isRunning = true;
 		while (isRunning)
 		{
 			// Update at a regular interval
 			numFrameSkip = 0;
 			while (System.nanoTime() > nextUpdateNanos && numFrameSkip <= MAX_FRAME_SKIP)
 			{
 				update();
 				nextUpdateNanos += SKIP_UPDATES_NANO;
 				numFrameSkip++;
 			}
 			
 			// Render with interpolation for smoother graphics when FPS exceeds UPS
 			double interpolation = (double)(System.nanoTime() + SKIP_UPDATES_NANO - nextUpdateNanos) / (double)SKIP_UPDATES_NANO;
 			render(interpolation);
 		}
 	}
 	
 	private void stopGameLoop()
 	{
 		isRunning = false;
 	}
 
 	// Main
 	public static void main(String[] args) 
 	{
 		// Start the game in a different thread than UI
 		SwingUtilities.invokeLater(new Runnable() 
 		{
 	         @Override
 	         public void run() 
 	         {
 	            Game warHorizon = new Game();
 	         }
 		});
 	}
 
 }
