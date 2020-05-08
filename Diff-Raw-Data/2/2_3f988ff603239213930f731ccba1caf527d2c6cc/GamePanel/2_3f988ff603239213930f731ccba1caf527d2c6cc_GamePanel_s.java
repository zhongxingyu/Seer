 package engine;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 /**
  * This class contains all graphical content.
  * 
  * @author regnaclockers
  */
 @SuppressWarnings("serial")
 public class GamePanel extends JPanel implements Runnable {
 	
 	//for measuring the fps
 	private int fps = 0;
 	private int frames = 0;
 	private long firstFrame;
 	private long currentFrame;
 	
 	private static final int MAX_FPS = 120;
 
 	private KeyBoardControl key = new KeyBoardControl();
 	private GameLoop loop = new GameLoop(key);
 	private Thread t1 = new Thread(loop);
 	private Thread t2 = new Thread(this);
 
 	int x = 128;
 	int y = 128;
 
 	public GamePanel() {
 		setPreferredSize(new Dimension(640, 480));
 		setDoubleBuffered(true);
 		setFocusable(true);
 		addKeyListener(key);
 		t1.start();
 		t2.start();
 	}
 
 	/**
 	 * draws everything.
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 		loop.drawGame(g);
 		showFps(g);
 	}
 
 	@Override
 	public void run() {
 		while (true) {
			if(fps >= 120 || fps == 0) {
 				try {
 					Thread.sleep(1000 / MAX_FPS);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			repaint();
 			measureFps();
 		}
 	}
 
 	private void measureFps() {
 		frames++;
 		currentFrame = System.currentTimeMillis();
 		if (currentFrame > firstFrame + 1000) {
 			firstFrame = currentFrame;
 			fps = frames;
 			frames = 0;
 		}
 	}
 	
 	private void showFps(Graphics g) {
 		if (fps != 0) {
 			g.drawString(fps + "FPS", 0, 10);
 		}
 	}
 }
