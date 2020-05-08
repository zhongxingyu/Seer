 package view;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import javax.swing.JPanel;
 
 import com.googlecode.javacv.FrameGrabber;
 import com.googlecode.javacv.VideoInputFrameGrabber;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 public class GrabberShow extends JPanel implements Runnable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	IplImage image;
 	IplImage img;
 	int width;
 	int height;
 	
 	public GrabberShow(int w, int h) {
 		super();
 		width = w;
 		height = h;
 	}
 	
 	@Override
 	public void run() {
		FrameGrabber grabber = new VideoInputFrameGrabber(2); // 1 for next camera
 		try {
 			grabber.start();
 			
 			while (true) {
 				img = grabber.grab();
 				if (img != null) {
 					this.repaint();
 				}
 			}
 		} catch (Exception e) {
 		}
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if (img != null) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.drawImage(img.getBufferedImage(), 0, 0, width, height, null);
 		}
 	}
 }
 
 
