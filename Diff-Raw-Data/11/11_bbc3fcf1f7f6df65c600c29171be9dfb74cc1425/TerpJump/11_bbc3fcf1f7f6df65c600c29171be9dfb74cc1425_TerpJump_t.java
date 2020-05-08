 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 
 @SuppressWarnings("serial")
 public class TerpJump extends JFrame implements Runnable {
 	
 	World world;
	static final int HEIGHT=Toolkit.getDefaultToolkit().getScreenSize().height; 
	static final int WIDTH=Toolkit.getDefaultToolkit().getScreenSize().width;
 	static final int FPS = 60;
 	static final int REFRESH_RATE = 1000/FPS;
 	
 	public TerpJump() {
 		super("TerpJump");
 		world = new World(WIDTH, HEIGHT, 3);
 		this.add(world);
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(screenSize);
 		setResizable(false);
 		setVisible(true);
 		setLocationRelativeTo(null);
 	}
 
 	public static void main(String[] args) {
 		new TerpJump().run();
 	}
 
 	public void run() {
 		
 		while (true) {
 			world.update();
 			world.repaint();
 			try {
 				Thread.sleep(REFRESH_RATE);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
