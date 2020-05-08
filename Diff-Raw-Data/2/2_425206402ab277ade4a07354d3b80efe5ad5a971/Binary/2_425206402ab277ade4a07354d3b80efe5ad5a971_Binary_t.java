 package toritools.entrypoint;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
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
 	protected int FRAMERATE = 17;
 	protected Vector2 VIEWPORT = new Vector2(800, 600);
 
 	private JFrame frame;
 
 	/**
 	 * SUBCLASS
 	 */
 	protected abstract boolean render(final Graphics rootCanvas);
 
 	protected abstract void loadResources();
 
 	protected abstract void initialize();
 
 	protected abstract void logic();
 
 	protected abstract void setupCurrentLevel();
 
 	protected abstract File getCurrentLevel();
 
 	@SuppressWarnings("serial")
 	protected Binary() {
 		if (System.getProperty("os.name").contains("Windows ")) {
 			System.setProperty("sun.java2d.d3d", "True");
 			// System.setProperty("sun.java2d.accthreshold", "0");
 		} else {
 			System.setProperty("sun.java2d.opengl=true", "True");
 		}
 
 		frame = new JFrame();
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
 
 		loadResources();
 		initialize();
 
 		try {
 			ScriptUtils.queueLevelSwitch(Importer
 					.importLevel(getCurrentLevel()));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		setupCurrentLevel();
 
 		new Timer().schedule(new TimerTask() {
 			@Override
 			public void run() {
 				coreLogic();
 				panel.repaint();
 			}
 		}, 0, FRAMERATE);
 	}
 
 	private boolean loadingLevel = false;
 
 	private void coreLogic() {
 		if (ScriptUtils.isLevelQueued()) {
 			loadingLevel = true;
 			ScriptUtils.getCurrentLevel().onDeath(true);
 			ScriptUtils.moveToQueuedLevel();
 			setupCurrentLevel();
 			loadingLevel = false;
 		}
 		if (!ScriptUtils.isLevelQueued()) {
 			logic();
 		}
 	}
 
 	private void renderAll(final Graphics rootCanvas) {
 		if (loadingLevel || !render(rootCanvas)) {
 			rootCanvas.clearRect(0, 0, (int) VIEWPORT.x, (int) VIEWPORT.y);
 			rootCanvas.setColor(Color.BLACK);
 			rootCanvas.drawString("Loading...", (int) VIEWPORT.x / 2,
 					(int) VIEWPORT.y / 2);
 			return;
 		}
 		;
 	}
 
 	protected void nextLevel() {
 		try {
 			File levelFile = getCurrentLevel();
 			if (levelFile.canRead()) {
 				System.out.println(levelFile);
 				ScriptUtils.queueLevelSwitch(Importer.importLevel(levelFile));
 			} else {
 				JOptionPane.showMessageDialog(null, "YOU BEAT THE GAME!");
 				System.exit(0);
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 }
