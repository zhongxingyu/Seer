 package smanilov.mandelbrot.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.JPanel;
 
 import smanilov.mandelbrot.compute.Camera;
 import smanilov.mandelbrot.compute.Computer;
 
 /**
  * A custom panel used for drawing the Mandelbrot set on.
  * @author szm
  */
 public class CanvasPanel extends JPanel {
 	
 	/**
 	 * Dark blue.
 	 */
	private static final Color backgroundColor = new Color(36, 201, 255);
 	/**
 	 * Pale yellow.
 	 */
 	private static final Color foregroundColor = new Color(0, 0, 0);
 	
 	/**
 	 * The current render of the Mandelbrot set.
 	 */
 	private Image drawing;
 	
 	/**
 	 * A lock for the image.
 	 */
 	private ReentrantLock drawingLock;
 	
 	/**
 	 * Indicates if the drawing should be redrawn.
 	 */
 	private boolean redraw;
 	
 
 	public CanvasPanel() {
 		super(true);
 		drawing = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
 		redraw = true;
 		
 		drawingLock = new ReentrantLock(true);
 		Timer t = new Timer();
 		t.schedule(new TimerTask() {
 			
 			@Override
 			public void run() {
 				repaint();
 			}
 		}, 0, 50);
 	}
 	
 	
 	
 	@Override
 	public void paint(Graphics graphics) {
 		super.paint(graphics);
 		
 		if (redraw) {
 			redraw = false;
 			Computer.drawMandelbrotCrop(
 					drawing,
 					foregroundColor,
 					backgroundColor,
 					drawingLock,
 					Camera.getScale(), 
 					Camera.getCenter()
 			);
 		}
 		
 		drawingLock.lock();
 		graphics.drawImage(drawing, 0, 0, null);
 		drawingLock.unlock();
 		
 		for (Component child : getComponents()) {
 			child.repaint();
 		}
 	}
 
 	private void resizeDrawing(int width, int height) {
 		if (drawing.getWidth(null) != width || drawing.getHeight(null) != height) {
 			drawing = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 			redraw = true;
 		}
 	}
 	
 	/**
 	 * Sets the redraw flag that indicates if the drawing should be regenerated.
 	 */
 	public void setRedraw(boolean redraw) {
 		this.redraw = redraw;
 	}
 	
 	@Override
 	public void setSize(int width, int height) {
 		resizeDrawing(width, height);
 		super.setSize(width, height);
 	}
 
 	@Override
 	public void setSize(Dimension d) {
 		resizeDrawing(d.width, d.height);
 		super.setSize(d);
 	}
 
 	@Override
 	public void setBounds(int x, int y, int width, int height) {
 		resizeDrawing(width, height);
 		super.setBounds(x, y, width, height);
 	}
 
 	@Override
 	public void setBounds(Rectangle r) {
 		resizeDrawing(r.width, r.height);
 		super.setBounds(r);
 	}
 }
