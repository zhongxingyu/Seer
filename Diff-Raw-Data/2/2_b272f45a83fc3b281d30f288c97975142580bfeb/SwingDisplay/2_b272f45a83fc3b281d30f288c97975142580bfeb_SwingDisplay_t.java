 package bam.pong;
 
 import java.awt.Graphics;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 /**
  * Displays a ball on a field via Swing
  * 
  * @author Brian
  */
 public class SwingDisplay extends JComponent {
	/** For serialization (via JComponent) */
 	private static final long serialVersionUID = -1198765485813951172L;
 
 	// Ball position
 	private int x;
 	private int y;
 	
 	public void setBall(int x, int y) {
 		this.x = x;
 		this.y = y;
 		repaint();
 	}
 	
 	/** Draw the ball */
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		g.fillOval(x, y, 20, 20);
 	}
 
 	/**
 	 * Test method to show the display.
 	 * 
 	 * @param args Command line arguments (ignored)
 	 */
 	public static void main(String[] args) {
 		final SwingDisplay disp = new SwingDisplay();
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				JFrame f = new JFrame("BAM!Pong");
 				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				f.setSize(100,125);
 				f.add(disp);
 				f.setVisible(true);
 			}
 		});
 		
 		Thread animator = new Thread() {
 			@Override
 			public void run() {
 				int x = 0;
 				int v = 3;
 				for(;;) {
 					disp.setBall(x, 50);
 
 					x += v;
 					if( x <= 0 || x >= 100 )
 						v *= -1;
 
 					try {
 						Thread.sleep(1000/30);
 					} catch (InterruptedException e) {
 						// I don't care.
 					}
 				}
 			}
 		};
 		
 		animator.start();
 	}
 
 }
