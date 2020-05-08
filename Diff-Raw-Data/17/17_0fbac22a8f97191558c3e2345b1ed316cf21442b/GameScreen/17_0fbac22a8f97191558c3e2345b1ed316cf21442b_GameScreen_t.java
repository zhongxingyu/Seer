 package gui;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 
 import motion.MouseMotionDetector;
 import util.Global;
 import frontEnd.GameManager;
 import frontEnd.Item;
 
 
 public class GameScreen extends JFrame {
 	private JFrame frame;
 	private GameManager game;
 	
 	public GameScreen(GameManager game) {
 		frame = new JFrame();
 		this.game = game;
 		frame.setSize(Global.xResolution,Global.yResolution);
 		frame.setVisible(true);
 	    frame.addWindowListener(new WindowAdapter() {
 	        public void windowClosing(WindowEvent e) {
 	          System.exit(0);
 	        }
 	      });
 	    frame.addMouseListener(new MouseMotionDetector(this));
 	}
	
 	public void processMotion(int startX, int endX, int startY, int endY) {
 		game.processMotion(startX, endX, startY, endY);
 	}
 	
 	public void paintObject(Item _item) {
 		// TODO Auto-generated method stub
 	    frame.add(new PaintableItem(_item));
 	}
 	
 	public void repaint() {
 		frame.repaint();
 	}
 
 }
