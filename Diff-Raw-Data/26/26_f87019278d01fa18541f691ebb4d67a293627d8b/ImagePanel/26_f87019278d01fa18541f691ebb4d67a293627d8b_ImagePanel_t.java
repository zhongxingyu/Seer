 package wombat.scheme.libraries.gui;
 
 import java.awt.AWTException;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Robot;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.text.DecimalFormat;
 
 import javax.swing.*;
 
 /**
  * A semi-advanced image viewer.
  * 
  *  Features:
  *  - zoom in on images / parts of images
  *  - hover over the image to get RGB values
  */
 public class ImagePanel extends JPanel implements MouseMotionListener {
 	private static final long serialVersionUID = 138774504307103027L;
 
 	Image MyImage;
 	Robot MyRobot;
 	
 	double Scale = 1.0;
 	boolean Debug = true;
 	
	int ImageX = -1;
	int ImageY = -1;
	int ScreenX = -1;
	int ScreenY = -1;
 
 	static final int ROW_HEIGHT = 14;
 	static final DecimalFormat FORMAT = new DecimalFormat("#.##");
 	
 	/**
 	 * Create the image panel.
 	 * @param image Image data to display.
 	 */
 	public ImagePanel(Image image) {
 		MyImage = image;
 		try {
 			MyRobot = new Robot();
 		} catch (AWTException e) {
 		}
 		
 		addMouseMotionListener(this);
 	}
 	
 	/**
 	 * Custom paint method.
 	 */
 	public void paintComponent(Graphics graphics) {
 		super.paintComponent(graphics);
 		Graphics2D g = (Graphics2D) graphics;
 
 		// Draw the background.
 		g.setColor(Color.WHITE);
 		g.fillRect(0,  0, getWidth(), getHeight());
 
 		// Calculate the image scale.
 		Scale = 0.9 * Math.min(
 			(double) getWidth() / (double) MyImage.getWidth(null),
 			(double) (getHeight() - 4 * ROW_HEIGHT) / (double) MyImage.getHeight(null)
 		);
 		
 		// Get the scaled width and height of the image.
 		int scaleWidth = (int) (Scale * MyImage.getWidth(null));
 		int scaleHeight = (int) (Scale * MyImage.getHeight(null));
 		int left = (getWidth() - scaleWidth) / 2;
 		int top = ((getHeight() - 4 * ROW_HEIGHT) - scaleHeight) / 2;
 		
 		// Finally, draw the image.
 		g.translate(left, top);
 		g.drawImage(MyImage, AffineTransform.getScaleInstance(Scale, Scale), null);
 		
 		// Draw a border.
 		g.setColor(Color.BLACK);
 		g.setStroke(new BasicStroke(2.0f));
 		g.drawRect(0, 0, scaleWidth, scaleHeight);
 		
 		// Print image information (if requested)
 		if (Debug) {
 			g.setColor(Color.BLACK);
 			g.translate(-left, scaleHeight);
 			
 			g.drawString(
 				"Image size: " + 
 					MyImage.getHeight(null) + " rows, " + 
 					MyImage.getWidth(null) + " columns",
 				ROW_HEIGHT, 2 * ROW_HEIGHT);
 			g.drawString(
 				"Current scale: " + FORMAT.format(100 * Scale) + "%", 
 				ROW_HEIGHT, 3 * ROW_HEIGHT);
 			
 			if (MyRobot != null 
					&& ImageX >= left && ImageX <= left + scaleWidth 
					&& ImageY >= top && ImageY <= top + scaleHeight) {
 				
				int displayX = (int) ((ImageX - left) / Scale);
				int displayY = (int) ((ImageY - top) / Scale);
 				
				Color c = MyRobot.getPixelColor(ScreenX, ScreenY);
 				g.drawString(
 					"Color at " + 
 						"row " + displayY + " / column " + displayX + ": " +
 						"#[color " + c.getRed() + " " + c.getGreen() + " " + c.getBlue() + "]", 
 						ROW_HEIGHT, 4 * ROW_HEIGHT);
 			} else {
 				g.drawString(
 					"Mouse over the image to view colors", 
 					ROW_HEIGHT, 4 * ROW_HEIGHT);
 			}
 		}
 	}
 
 	/**
 	 * When the mouse is dragged (drug?)
 	 * @param event Details.
 	 */
 	@Override public void mouseDragged(MouseEvent event) {}
 
 	/**
 	 * When the mouse is moved.
 	 * @param event Details
 	 */
 	@Override public void mouseMoved(MouseEvent event) {
		ImageX = event.getX();
		ImageY = event.getY();
		ScreenX = event.getXOnScreen();
		ScreenY = event.getYOnScreen();
 		repaint();
 	}
 }
