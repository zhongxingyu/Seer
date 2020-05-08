 package main;
 
 import io.Listener;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * The main class that is run to run the Game of Life simulation.
  * 
  * @author Dominic
  */
 public class GameOfLife
 {
 	private BufferStrategy strategy;
 	
 	private Image icon = new ImageIcon("images/icon.png").getImage();
 	private Information info;
 	private int drawLoops;					// number of drawing loops
 	private int longDrawLoops;				// times that rendering took over the period
 	
 	public JFrame frame;
 	
 	public static long period = 20;			// total time for the drawing cycle in ms
 	private long drawStart;
 	private long drawEnd;
 	private long renderEnd;
 	private long sleepTime;
 	
 	public Thread toolbarThread;
 	public Thread gridThread;
 	
 	/**
 	 * Runs the simulation by creating a GameOfLife object and calling launch() and then run().
 	 * 
 	 * @param args - the command-line arguments
 	 */
 	public static void main(String[] args)
 	{
 		GameOfLife gameOfLife = new GameOfLife();
 		gameOfLife.launch();
 		gameOfLife.run();
 	}
 	
 	/**
 	 * Launches the GameOfLife by loading various components, particularly the Information which initializes most of the other classes.
 	 */
 	public void launch()
 	{
 		info = new Information();
 		info.init(this);
 		
 		info.listener.requestNotification(this, "exit", Listener.TYPE_KEY_PRESSED, KeyEvent.VK_ESCAPE);
 		
 		frame = new JFrame("Game of Life");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setIconImage(icon);
 		frame.setContentPane(new Panel(info));
 		frame.setUndecorated(true);
 		frame.setResizable(false);
 		frame.addKeyListener(info.listener);
 		frame.addMouseListener(info.listener);
 		frame.addMouseMotionListener(info.listener);
 		frame.addMouseWheelListener(info.listener);
 		info.device.setFullScreenWindow(frame);
 		frame.createBufferStrategy(2);
 		
 		toolbarThread = new Thread(info.toolbar);
 		toolbarThread.start();
 		gridThread = new Thread(info.grid);
 		gridThread.start();
 	}
 	
 	/**
 	 * Runs the simulation by drawing and sleeping so that each draw cycle takes the preset period length of time.
 	 * If rendering took over the period, a warning message is printed to the console.
 	 */
 	public void run()
 	{
 		while (true)
 		{
 			drawStart = System.nanoTime();
 			strategy = frame.getBufferStrategy();								// begin "draw"
 			Graphics2D g = (Graphics2D)strategy.getDrawGraphics();
 			draw(g);
 			g.dispose();														// end "draw"
 			drawEnd = System.nanoTime();
 			
 			strategy.show();													// begin and end "render"
 			renderEnd = System.nanoTime();
 			
 			sleepTime = 1000000*period - (System.nanoTime() - drawStart);		// begin "sleep"
 			if (sleepTime <= 0)
 			{
 				System.out.println("[WARNING] Rendering took over the allotted period by " + (-sleepTime/1000000) + " ms.");
 				sleepTime = 0;
 				longDrawLoops++;
 			}
 			else
 			{
 				try
 				{
 					Thread.sleep(sleepTime/1000000);
 				}
 				catch (InterruptedException ex) { }
 			}																	// end "sleep"
 			drawLoops++;
 			info.diagnostics.recordRenderLoop(drawEnd - drawStart, renderEnd - drawEnd, sleepTime);
 		}
 	}
 	
 	/**
 	 * Called when the escape key is pressed or the "X" is clicked in the operation bar, immediately closes the program.
 	 * 
 	 * @param event - the Event that originated this call [not used]
 	 */
 	public void exit(KeyEvent event)
 	{
 		System.out.println(longDrawLoops + " [" + 100*((double)longDrawLoops)/((double)drawLoops) + 
 				"%] of the total number of drawing loops, " + drawLoops + ", took over the period.");
 		System.exit(0);
 	}
 	
 	/**
 	 * Draws the simulation to the screen with the Graphics that should originate from the JFrame.
 	 * First, the window is drawn, making up the background.
 	 * Then, the utility pane is drawn on top of which is the pattern selector.
 	 * Finally, the operation bar is drawn in the top right of the screen.
 	 * 
 	 * @param g - the current Graphics context of the JFrame
 	 */
 	public void draw(Graphics2D g)
 	{
 		info.grid.draw(g);
 		info.toolbar.draw(g);
 		info.toolbar.selector.draw(g);
 		info.controlBar.draw(g);
 		info.diagnostics.draw(g);
 	}
 }
 
 /**
  * Allows for screenshots and screen recording to capture the screen by providing a more typical context in which to draw.
  * 
  * @author Dominic
  */
 class Panel extends JPanel
 {
 	Information info;
 	
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * Creates a new Panel with the given Information.
 	 * 
 	 * @param info - the current Information
 	 */
 	public Panel(Information info)
 	{
 		this.info = info;
 	}
 	
 	/**
 	 * Called during a typical drawing method, simply calls the Game of Life's draw method.
 	 * This allows for screenshots to be made with a conventional method.
 	 * At the beginning of the execution, a null reference is often made because this method is called before initialization is complete.
 	 * Thus, any NullPointerExceptions thrown (either because info or gameOfLife is null) are completely ignored.
 	 */
 	public void paintComponent(Graphics g)
 	{
 		try
 		{
 			info.gameOfLife.draw((Graphics2D)g);
 		}
 		catch (NullPointerException ex) { }
 	}
 }
