 package toritools.entrypoint;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import toritools.entity.Level;
 import toritools.io.Importer;
 import toritools.math.Vector2;
 import toritools.scripting.ScriptUtils;
 
 /**
  * This class is a good way to get started with ToriTools. Just extend it, and
  * fill in what you need. Instantiate the subclass, and you're done!
  * 
  * @author toriscope
  * 
  */
 public abstract class Binary {
 
 	// CORE VARS
 	protected final int FRAMERATE;
 	protected final Vector2 VIEWPORT;
 	
 
 	private JFrame frame;
 	
 	/**
 	 * Some basic settings.
 	 * @param VIEWPORT_SIZE the dimensions of the viewport/window.
 	 * @param frameRate the frame-rate as a ratio. 60FPS would be 60, for example.
 	 */
 	public Binary(final Vector2 VIEWPORT_SIZE, final int frameRate) {
 		this.FRAMERATE = frameRate;
 		this.VIEWPORT = VIEWPORT_SIZE;
 		
 		//Hardware accel.
 		if (System.getProperty("os.name").contains("Windows")) {
 			System.setProperty("sun.java2d.d3d", "True");
 		} else {
 			System.setProperty("sun.java2d.opengl=true", "True");
 		}
 
 		frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		@SuppressWarnings("serial")
 		final JPanel panel = new JPanel() {
 			public void paintComponent(final Graphics g) {
 				renderAll(g);
 			}
 		};
 		frame.add(panel);
 		frame.addKeyListener(ScriptUtils.getKeyHolder());
 		frame.setFocusable(true);
 		frame.setVisible(true);
 		panel.setPreferredSize(new Dimension((int) VIEWPORT.x, (int) VIEWPORT.y));
 		frame.pack();
 
 		initialize();
 
 		try {
 			ScriptUtils.queueLevelSwitch(Importer
 					.importLevel(getStartingLevel()));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		setupCurrentLevel(ScriptUtils.getCurrentLevel());
 		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
 				new Thread() {
 					public void run() {
 						coreLogic();
 						panel.repaint();
 					}
 				}, 0, 1000/ FRAMERATE, TimeUnit.MILLISECONDS);
 	}
 
 	/*
 	 * SUBCLASS
 	 */
 
 	/**
 	 * Render your game.
 	 * 
 	 * @param rootCanvas
 	 *            the panel's drawing surface.
 	 * @return true if drawing was successful, false otherwise.
 	 */
 	protected abstract boolean render(final Graphics rootCanvas);
 
 	/**
 	 * Load anything you need (besides entities), be it large background images
 	 * or fonts. This is your time to prepare for the update logic which will
 	 * begin ticking after this method is run.
 	 */
 	protected abstract void initialize();
 
 	/**
 	 * The global logic loop. Poll controls here if you want, check for win
 	 * condition, etc. Entity updating should not be done here. It is a good
 	 * idea to package control polling for most entities with their script,
 	 * rather than here. This is the place for global menus, state changing,
 	 * etc. The level will update after this method is run, followed by a
 	 * graphical repaint. Keys queued for release are also released after this.
 	 */
 	protected abstract void globalLogic();
 
 	/**
 	 * Configure the current level to be loaded. Set up your special entity
 	 * types and scripts here. Spawning will be done elsewhere.
 	 * 
 	 * @param levelBeingLoaded
 	 */
 	protected abstract void setupCurrentLevel(Level levelBeingLoaded);
 
 	protected abstract File getStartingLevel();
 
 	private boolean loadingLevel = false;
 
 	private void coreLogic() {
 		if (ScriptUtils.isLevelQueued()) {
 			loadingLevel = true;
 			ScriptUtils.getCurrentLevel().onDeath(true);
 			ScriptUtils.moveToQueuedLevel();
 			setupCurrentLevel(ScriptUtils.getCurrentLevel());
 			loadingLevel = false;
 		} else {
 			globalLogic();
			ScriptUtils.getCurrentLevel().onUpdate(FRAMERATE / 1000);
 			ScriptUtils.getKeyHolder().freeQueuedKeys();
 		}
 	}
 
 	private void renderAll(final Graphics rootCanvas) {
 		if (loadingLevel) {
 			rootCanvas.clearRect(0, 0, (int) VIEWPORT.x, (int) VIEWPORT.y);
 			rootCanvas.setColor(Color.BLACK);
 			rootCanvas.drawString("Loading...", (int) VIEWPORT.x / 2,
 					(int) VIEWPORT.y / 2);
 			return;
 		}
 		render(rootCanvas);
 	}
 }
