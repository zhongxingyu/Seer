 package main;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JPanel;
 
 /*
  * main game class. Uses a UnitNavigator to determine the active
  * game unit in order to call its update() and draw() methods. All KeyEvents
  * are forwarded to the game unit in use.
  */
 
 public class MainPanel extends JPanel implements Runnable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3212988448230980893L;
 
 	private volatile boolean running = false;
 
 	private UnitNavigator unitNavigator;
 
 	private Thread gameThread;
 
 	public MainPanel() {
 		setBackground(Color.white);
 		setSize(GameConstants.FRAME_SIZE_X, GameConstants.FRAME_SIZE_Y);
 		setFocusable(true);
 		/*
 		 * wait for KeyEvents to be propagated to the active game unit
 		 */
 		addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_UP)
				System.out.println("mainpanel");
 				unitNavigator.getActiveUnit().handleKeyPressed(e);
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				unitNavigator.getActiveUnit().handleKeyReleased(e);
 			}
 		});
		requestFocusInWindow();
 		setDoubleBuffered(true);
 	}
 
 	public void initGame() {
 		unitNavigator = new UnitNavigator(this);
 		/*
 		 * add main menu
 		 */
 		MainMenuUnit mainMenu = new MainMenuUnit();
 		mainMenu.setNavigator(unitNavigator);
 		unitNavigator.addGameUnit(mainMenu, UnitState.BASE_MENU_UNIT);
 		activateThread();
 	}
 
 	private void activateThread() {
 		if (gameThread == null) {
 			gameThread = new Thread(this);
 		}
 		this.start();
 		gameThread.start();
 	}
 
 	public UnitNavigator getGameStateNavigator() {
 		return unitNavigator;
 	}
 
 	@Override
 	public void run() {
 		/*
 		 * some time measurement to maintain constant fps
 		 */
 		long beforeTime, timeDiff, sleepTime;
 		beforeTime = System.nanoTime();
 
 		/*
 		 * infinite loop for updating and drawing the selected game unit
 		 */
 		while (this.isRunning()) {
			requestFocusInWindow();
 			unitNavigator.getActiveUnit().updateComponent();
 			repaint();
 
 			timeDiff = System.nanoTime() - beforeTime;
 			sleepTime = GameConstants.ITERATION_TIME - timeDiff / 1000000L;
 
 			if (sleepTime < 0)
 				sleepTime = 4;
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {
 				System.out.println("interrupted");
 			}
			//System.out.println(sleepTime);
 			beforeTime = System.nanoTime();
 		}
 		quitGame();
 	}
 
 	/*
 	 * get active game unit and delegate drawing
 	 */
 	@Override
 	public void paint(Graphics g) {
 		if (this.isRunning()) {
 			unitNavigator.getActiveUnit().drawComponent(g);
 			Toolkit.getDefaultToolkit().sync();
 			g.dispose();
 		}
 	}
 
 	/*
 	 * end program
 	 */
 	private void quitGame() {
 		System.exit(0);
 	}
 
 	public void stop() {
 		this.running = false;
 	}
 
 	public void start() {
 		this.running = true;
 	}
 
 	public boolean isRunning() {
 		if (running) {
 			return true;
 		}
 		return false;
 	}
 
 }
