 package com.bluebarracudas.app;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import com.bluebarracudas.model.TFactory;
 import com.bluebarracudas.model.TStation;
 import com.bluebarracudas.model.TStop;
 import com.bluebarracudas.model.TTrip;
 
 /** A panel to display our MBTA map */
 public class TMapPanel extends JPanel implements Runnable {
 
 	/** Our buffered image */
 	private Image bufferImage;
 	/** A reference to our buffer image's graphics object */
 	private Graphics2D imageGraphics;
 
 	/** The height & width of the trip circle **/
 	public static int tripSize = 6;
 	/** The height & width of the stop square **/
 	public static int stopSize = 10;
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 600;
 	public static final int PADDING = 100;
 
 	/** Default constructor */
 	public TMapPanel() {
 
 		// Set our preferred size
 		setPreferredSize(new Dimension(WIDTH + PADDING * 2, HEIGHT + PADDING * 2));
 
 		// Get the focus
 		setFocusable(true);
 		requestFocusInWindow();
 
 		// Start our main loop
 		new Thread(this).start();
 	}
 
 	@Override
 	/** Our main loop */
 	public void run() {
 		// Set our thread priority
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		while (true) {
 
 			// Make sure we're valid before trying to display ourself
 			if (isValid()) {
 				render();
 				display();
 			}
 
 			// Go to sleep for a bit
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException ex) {
 			}
 
 			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 		}
 	}
 
 	/**
 	 * Render our buffer image
 	 *
 	 * @author Tom Fiset
 	 * Revised by: Liz Brown
 	 */
 	private void render() {
 		// First set everything up...
 
 		// If we don't have a buffer image, create one
 		if (bufferImage == null) {
 			bufferImage = createImage(getSize().width, getSize().height);
 
 			// If we couldn't create a buffer image, let the world know.
 			if (bufferImage == null) {
 				System.out.println("Failed to create buffer Image...");
 				return;
 			}
 
 			// Otherwise associate it with our graphics object.
 			imageGraphics = (Graphics2D) bufferImage.getGraphics();
 
 			imageGraphics.setBackground(Color.black);
 			imageGraphics.setClip( this.getBounds() );
 		}
 
 		// Do actual rendering here...
 
 		// Fill the screen with our background color
 		imageGraphics.setColor(imageGraphics.getBackground());
 		imageGraphics.fill(imageGraphics.getClip());
 
 		// Draw the stations on the background
 		renderStations(imageGraphics);
 
 		// Draw the lines between stops
 		renderLines(imageGraphics);
 
 		// Draw the trips on the background
 		renderTrips(imageGraphics);
 	}
 
 	/**
 	 * Render buttons all of the stops
 	 *
 	 * @author Liz Brown, Tom Fiset
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderStations(Graphics2D g) {
 
 		// Get the action handler
 		THandler handler = new THandler();
 		
 		// Place a button on each station
 		for (TStation pStation : TFactory.getAllStations()) {
 			Point2D pPos = pStation.getPosition();
 
 			Insets insets = this.getInsets();
 			JButton stopButton = new JButton();
 			stopButton.setBounds(insets.left + (int)pPos.getX(), insets.right + (int)pPos.getY(), 10, 10);
 			stopButton.setActionCommand(pStation.getName());
 			stopButton.addActionListener(handler);
 			this.add(stopButton);
 		}
 	}
 
 	/**
 	 * Draw lines between the stations Note: This is a very rough proof of
 	 * concept.
 	 *
 	 * @author Liz Brown
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderLines(Graphics2D g) {
 		// TODO Make this more efficient
 		// TODO Figure out the best way to handle Ashmont -> JFK & transfer
 		// stations
 
 		int offset = stopSize / 2;
 
		for (TStation station : TFactory.getAllStations()) {
			List<TStop> stops = station.getStops();
 			TStop nextStop = TFactory.getStop(stop.getID() + 2);
 			if (nextStop != null && nextStop.getColor().equals(stop.getColor())
 					&& !stop.getName().equals("Ashmont")) {
 				imageGraphics.setPaint(stop.getColor());
 				g.drawLine((int)stop.getPosition().getX() + offset,
 						(int)stop.getPosition().getY() + offset,
 						(int)nextStop.getPosition().getX() + offset,
 						(int)nextStop.getPosition().getY() + offset);
 			}
 		}
 	}
 
 	/**
 	 * Render all of the trips on the map
 	 *
 	 * @author Liz Brown
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderTrips(Graphics2D g) {
 		Color tripColor = new Color(0, 255, 255, 200);
 
		Collection<TTrip> trips = TFactory.getAllTrips();
		for (TTrip trip : trips) {
 			Point2D position = trip.getPosition();
 
 			imageGraphics.setPaint(tripColor);
 			imageGraphics.fill(new Rectangle2D.Double(position.getX(), position.getY(), tripSize,
 					tripSize));
 		}
 	}
 
 	/** Draw our buffer image on the screen */
 	private void display() {
 		// A reference to our screen graphics object
 		Graphics screenGraphics;
 
 		// Try to set up the reference...
 		try {
 			// Get the reference
 			screenGraphics = this.getGraphics();
 
 			// And use it to draw our buffer image
 			if ((screenGraphics != null) && (bufferImage != null))
 				screenGraphics.drawImage(bufferImage, 0, 0, null);
 
 			// Clean up
 			screenGraphics.dispose();
 		} catch (Exception e) {
 			System.out.println("Exception in display()! " + e);
 		}
 	}
 }
