 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package twosnakes;
 
 // BrawlerPanel.java
 // Jovan Ristic, November 2008
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import javax.swing.JPanel;
 
 
 // import com.sun.j3d.utils.timer.J3DTimer;
 public class MainPanel extends JPanel implements Runnable
 {
 
 	private static final int PWIDTH = 1280;   // size of panel
 	private static final int PHEIGHT = 720;
 	// record stats every 1 second (roughly)
 	private static final int NO_DELAYS_PER_YIELD = 16;
 	/*
 	 * Number of frames with a delay of 0 ms before the animation thread yields
 	 * to other running threads.
 	 */
 	private static int MAX_FRAME_SKIPS = 5;   // was 2;
 	// no. of frames that can be skipped in any one animation loop
 	// i.e the games state is updated but not rendered
 	private long gameStartTime;
 	private long lastUpdateTime;
 	private Thread animator;           // the thread that performs the animation
 	private volatile boolean running = false;   // used to stop the animation thread
 	private volatile boolean isPaused = false;
 	private int period;                // period between drawing in _ms_
 	
 	// used at game termination
 	private volatile boolean gameOver = false;
 	private volatile boolean gameStarted = false;
 	
 	// off screen rendering
 	private Graphics dbg;
 	private Image dbImage = null;
 	
 	private GameState state;
 	private Update update;
 	private Render render;
 	private Setup setup;
 
 	public MainPanel(Main main, int period)
 	{
 		this.period = period;
 		
 		this.state = new GameState();
 		setup = new Setup(state);
 		update = new Update(state);
 		render = new Render(state, this);
 		
 		setBackground(Color.white);
 		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
 
 		setFocusable(true);
 		requestFocus();    // the JPanel now has focus, so receives key events
 
 		//key event listeners
 		addKeyListener(new KeyAdapter()
 		{
 			public void keyPressed(KeyEvent e)
 			{
				update.processKeyPress(e);
 			}
 
 			public void keyReleased(KeyEvent e)
 			{
				update.processKeyRelease(e);
 			}
 		});
 
 		readyForTermination();
 	}
 
 	public static int getFrameHeight()
 	{
 		return PHEIGHT;
 	}
 
 	public static int getFrameWidth()
 	{
 		return PWIDTH;
 	}
 
 	private void readyForTermination()
 	{
 		addKeyListener(new KeyAdapter()
 		{
 			// listen for esc, q, end, ctrl-c on the canvas to
 			// allow a convenient exit from the full screen configuration
 
 			public void keyPressed(KeyEvent e)
 			{
 				int keyCode = e.getKeyCode();
 				if (keyCode == KeyEvent.VK_ESCAPE)
 				{
 					running = false;
 				}
 			}
 		});
 	}
 
 	private void readyForStart()
 	{
 		addKeyListener(new KeyAdapter()
 		{
 			// listen for exnter to begin the game
 
 			public void keyPressed(KeyEvent e)
 			{
 				int keyCode = e.getKeyCode();
 				if (keyCode == KeyEvent.VK_ENTER && !gameStarted)
 				{
 					gameStarted = true;		
 					// Setup game elements
 					setup.gameSetup();
 				}
 			}
 		});
 	}
 
 	public void addNotify()
 	// wait for the JPanel to be added to the JFrame before starting
 	{
 		super.addNotify();   // creates the peer
 		startGame();    // start the thread
 	}
 
 	// ------------- game life cycle methods ------------
 	// called by the JFrame's window listener methods
 	private void startGame()
 	// initialise and start the thread 
 	{
 		if (animator == null || !running)
 		{
 			animator = new Thread(this);
 			animator.start();
 		}
 	}
 
 	public void resumeGame()
 	// called when the JFrame is activated / deiconified
 	{
 		isPaused = false;
 	}
 
 	public void pauseGame()
 	// called when the JFrame is deactivated / iconified
 	{
 		isPaused = true;
 	}
 
 	public void stopGame()
 	// called when the JFrame is closing
 	{
 		running = false;
 	}
 
 	//------------------------------------------------
 	public void run()
 	/*
 	 * The frames of the animation are drawn inside the while loop.
 	 */
 	{
 		long beforeTime, afterTime, timeDiff, sleepTime;
 		int overSleepTime = 0;
 		int noDelays = 0;
 		int excess = 0;
 		Graphics g;
 
 		gameStartTime = System.currentTimeMillis();
 		lastUpdateTime = gameStartTime;
 		beforeTime = gameStartTime;
 
 		running = true;
 
 		while (running)
 		{
 			gameUpdate();
 			gameRender();   // render the game to a buffer
 			paintScreen();  // draw the buffer on-screen
 
 			afterTime = System.currentTimeMillis();
 			timeDiff = afterTime - beforeTime;
 			sleepTime = (period - timeDiff) - overSleepTime;
 
 			if (sleepTime > 0)
 			{   // some time left in this cycle
 				try
 				{
 					Thread.sleep(sleepTime);  // already in ms
 				} 
 				catch (InterruptedException ex)
 				{
 				}
 				overSleepTime = (int) ((System.currentTimeMillis() - afterTime) - sleepTime);
 			} 
 			else
 			{    // sleepTime <= 0; the frame took longer than the period
 				excess -= sleepTime;  // store excess time value
 				overSleepTime = 0;
 
 				if (++noDelays >= NO_DELAYS_PER_YIELD)
 				{
 					Thread.yield();   // give another thread a chance to run
 					noDelays = 0;
 				}
 			}
 
 			beforeTime = System.currentTimeMillis();
 
 			/*
 			 * If frame animation is taking too long, update the game state
 			 * without rendering it, to get the updates/sec nearer to the
 			 * required FPS.
 			 */
 			int skips = 0;
 			while ((excess > period) && (skips < MAX_FRAME_SKIPS))
 			{
 				excess -= period;
 				gameUpdate();    // update state but don't render
 				skips++;
 			}
 		}
 		System.exit(0);   // so window disappears
 	}
 
 	private void gameUpdate()
 	{
 		if (gameStarted && !isPaused && !gameOver)
 		{
 			long timePassed = System.currentTimeMillis() - lastUpdateTime;
 			update.gameUpdate(timePassed);
 			lastUpdateTime = System.currentTimeMillis();
 		}
 	}
 
 	private void gameRender()
 	{
 		if (dbImage == null)
 		{
 			dbImage = createImage(PWIDTH, PHEIGHT);
 			if (dbImage == null)
 			{
 				System.out.println("dbImage is null");
 				return;
 			} else
 			{
 				dbg = dbImage.getGraphics();
 			}
 		}
 
 		// draw the game elements to the buffer
 		if (gameStarted && !gameOver)
 		{
 			render.drawGame(dbg);
 		}
 
 		if (!gameStarted)
 		{
 			render.drawStartMessage(dbg);
 			readyForStart();
 		}
 		if (gameOver)
 		{
 			render.drawGameOverMessage(dbg);
 		}
 	}
 
 	private void paintScreen()
 	// use active rendering to put the buffered image on-screen
 	{
 		render.paintScreen(this.getGraphics(), dbImage);
 	}
 }
