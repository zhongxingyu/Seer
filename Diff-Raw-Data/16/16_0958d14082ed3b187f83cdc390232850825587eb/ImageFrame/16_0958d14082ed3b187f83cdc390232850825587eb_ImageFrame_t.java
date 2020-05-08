 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.scheme.libraries.gui;
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Robot;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.text.DecimalFormat;
 
 import javax.swing.*;
 import javax.swing.border.BevelBorder;
 
 /**
  * A semi-advanced image viewer.
  * 
  *  Features:
  *  - zoom in on images / parts of images
  *  - hover over the image to get RGB values
  */
 public class ImageFrame extends JFrame implements MouseMotionListener {
 	private static final long serialVersionUID = 138774504307103027L;
 
 	Image MyImage;
 	Robot MyRobot;
 	
 	double Scale = 1.0;
 	boolean Debug = true;
 	
 	int ImageX = -1;
 	int ImageY = -1;
 	int ScreenX = -1;
 	int ScreenY = -1;
 	
 	int MaxWidth = -200 + (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 	int MaxHeight = -200 + (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
 
 	JLabel ImageDisplay;
 	JLabel ImageInformation;
 	
 	static final int ROW_HEIGHT = 14;
 	static final DecimalFormat FORMAT = new DecimalFormat("#.##");
 	
 	/**
 	 * Create the image panel.
 	 * @param image Image data to display.
 	 */
 	public ImageFrame(Image image) {
 		final ImageFrame me = this;
 		MyImage = image;
 		
 		// Basic layout stuff.
 		setTitle("draw-image");
 		setLayout(new BorderLayout(10, 10));
 		setLocationByPlatform(true);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		// Add zoom buttons to the top.
 		JPanel zoomButtoms = new JPanel();
 		
 		JButton zoomOut = new JButton("-");
 		zoomOut.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent arg0) {
 				
 				if (MyImage.getWidth(null) * Scale < 5 || MyImage.getHeight(null) * Scale < 5)
 					return;
 				
 				Scale /= 2.0;
 				
 				int scaleWidth = (int) (MyImage.getWidth(null) * Scale);
 				int scaleHeight = (int) (MyImage.getHeight(null) * Scale);
 				
 				ImageDisplay.setIcon(new ImageIcon(MyImage.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH)));
 				Dimension size = new Dimension(scaleWidth, scaleHeight);
 				ImageDisplay.setSize(size);
 				ImageDisplay.setMinimumSize(size);
 				ImageDisplay.setPreferredSize(size);
 				ImageDisplay.setMaximumSize(size);
 				
 				me.updateInformation();
 				me.pack();
 			}
 		});
 		zoomButtoms.add(zoomOut);
 		
 		JButton zoomIn = new JButton("+");
 		zoomIn.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent arg0) {
 				
 				if (MyImage.getWidth(null) * Scale > MaxWidth || MyImage.getHeight(null) * Scale > MaxHeight)
 					return;
 				
 				Scale *= 2.0;
 				
 				int scaleWidth = (int) (MyImage.getWidth(null) * Scale);
 				int scaleHeight = (int) (MyImage.getHeight(null) * Scale);
 				
 				ImageDisplay.setIcon(new ImageIcon(MyImage.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH)));
 				Dimension size = new Dimension(scaleWidth, scaleHeight);
 				ImageDisplay.setSize(size);
 				ImageDisplay.setMinimumSize(size);
 				ImageDisplay.setPreferredSize(size);
 				ImageDisplay.setMaximumSize(size);
 				
 				me.updateInformation();
 				me.pack();
 			}
 		});
 		zoomButtoms.add(zoomIn);
 		
 		add(zoomButtoms, BorderLayout.NORTH);
 		
 		// Add a description
 		ImageInformation = new JLabel();
		Dimension size = new Dimension(350, 60);
 		ImageInformation.setHorizontalAlignment(JLabel.CENTER);
 		ImageInformation.setSize(size);
 		ImageInformation.setMinimumSize(size);
 		ImageInformation.setPreferredSize(size);
 		ImageInformation.setMaximumSize(size);
 		add(ImageInformation, BorderLayout.SOUTH);
 		
 		// Add the image.
 		ImageDisplay = new JLabel();
 		ImageDisplay.setIcon(new ImageIcon(MyImage));
 		ImageDisplay.setHorizontalAlignment(JLabel.CENTER);
 		ImageDisplay.setIconTextGap(0);
 		ImageDisplay.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 		
 		JPanel imageDisplayPanel = new JPanel();
 		imageDisplayPanel.add(ImageDisplay);
 		add(imageDisplayPanel, BorderLayout.CENTER);
 		
 		// Get the size from that to display everything.
 		updateInformation();
 		pack();
 		
 		try { MyRobot = new Robot(); } catch (AWTException e) {}
 		ImageDisplay.addMouseMotionListener(this);
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
 		updateInformation();
 	}
 	
 	/**
 	 * Update information about the image.
 	 */
 	void updateInformation() {
 		String text = "<html>";
 		
 		text += "Image size: " + MyImage.getHeight(null) + " rows, " + MyImage.getWidth(null) + " columns\n";
 		text += "<br />";
 		text += "Current scale: " + FORMAT.format(100 * Scale) + "%";
 		text += "<br />";
 		
 		if (MyRobot != null) {
 			int displayX = (int) (ImageX / Scale);
 			int displayY = (int) (ImageY / Scale);
 			
 			if (displayX >= 0 && displayX < MyImage.getWidth(null) && displayY >= 0 && displayY <= MyImage.getHeight(null)) {
 				Color c = MyRobot.getPixelColor(ScreenX, ScreenY);
 				text += "Color at " + "row " + displayY + " / column " + displayX + ": " + "#[color " + c.getRed() + " " + c.getGreen() + " " + c.getBlue() + "]";
 			}
 		}
 		
 		text += "</html>";
 			
 		ImageInformation.setText(text);
 		
 		pack();
 	}
 }
