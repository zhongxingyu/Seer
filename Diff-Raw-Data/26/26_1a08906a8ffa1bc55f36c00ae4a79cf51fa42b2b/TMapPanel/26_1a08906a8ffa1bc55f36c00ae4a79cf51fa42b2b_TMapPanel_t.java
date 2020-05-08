 package com.bluebarracudas.app;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Ellipse2D;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import com.bluebarracudas.model.TFactory;
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
 		setPreferredSize( new Dimension(WIDTH + PADDING * 2, HEIGHT + PADDING * 2) );
 
 		// Get the focus
 		setFocusable( true );
 		requestFocusInWindow();
 
 		// Start our main loop
 		new Thread(this).start();
 	}
 
 	@Override
 	/** Our main loop */
 	public void run() {
 		// Set our thread priority
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		while( true ) {
 
 			// Make sure we're valid before trying to display ourself
 			if( isValid() ) {
 				render();
 				display();
 			}
 
 			// Go to sleep for a bit
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException ex) { }
 
 			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 		}
 	}
 	
 	/** Render our buffer image */
 	private void render() {
 		// First set everything up...

 		// If we don't have a buffer image, create one
 		if( bufferImage == null ) {
 			bufferImage= createImage( getSize().width, getSize().height );

 			// If we couldn't create a buffer image, let the world know.
 			if( bufferImage == null ) {
 				System.out.println( "Failed to create buffer Image..." );
 				return;
 			}

 			// Otherwise associate it with our graphics object.
 			imageGraphics= (Graphics2D)bufferImage.getGraphics();
 			imageGraphics.setBackground( Color.black );
 			imageGraphics.setClip(0, 0, getSize().width, getSize().height);
 		}

 		// Do actual rendering here...

 		// Fill the screen with our background color
 		imageGraphics.setColor( imageGraphics.getBackground() );
 		imageGraphics.fillRect(0, 0, getSize().width, getSize().height);

 		// Draw the stops on the background
 		this.renderStops(imageGraphics);

 		// Draw the lines between stops
 		this.renderLines(imageGraphics);

 		// Draw the trips on the background
 		this.renderTrips(imageGraphics);
 	}

 	/**
 	 * Render all of the stops & their names on the map
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderStops(Graphics2D g) {
 		for (TStop stop : TFactory.getAllStops().values()) {
 			Point position = stop.getCanvasPosition();
 			
 			imageGraphics.setPaint(stop.getColor());
 			imageGraphics.fill(new Ellipse2D.Double(position.x, position.y, stopSize, stopSize));
 			imageGraphics.drawString(stop.getName(), position.x + 12, position.y + 12);
 		}
 	}
 	
 	/** 
 	 * Draw lines between the stations 
 	 * Note: This is a very rough proof of concept.
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderLines(Graphics2D g) {
 		// TODO Make this more efficient
 		// TODO Figure out the best way to handle Ashmont -> JFK & transfer stations
 		
 		int offset = stopSize / 2;
 		
 		for (TStop stop : TFactory.getAllStops().values()) {
 			if (stop.getID() % 2 != 0)
 				continue;
 			
 			TStop nextStop = TFactory.getStop(stop.getID()+2);
 			if (nextStop != null && nextStop.getColor().equals(stop.getColor()) && !stop.getName().equals("Ashmont")) {
 				imageGraphics.setPaint(stop.getColor());
 				g.drawLine(stop.getCanvasPosition().x + offset, stop.getCanvasPosition().y + offset,
 						   nextStop.getCanvasPosition().x + offset, nextStop.getCanvasPosition().y + offset);
 			}
 		}
 	}
 	
 	/**
 	 * Render all of the trips on the map
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderTrips(Graphics2D g) {
 		Color tripColor = new Color(0, 255, 255, 200);
 
 		Map<String, TTrip> trips = TFactory.getAllTrips();
 		for (TTrip trip : trips.values()) {
 			Point position = trip.getPosition();
 
 			imageGraphics.setPaint(tripColor);
 			imageGraphics.fill(new Rectangle(position.x, position.y, tripSize, tripSize));
 		}
 	}
 
 	/** Draw our buffer image on the screen */
 	private void display() {
 		// A reference to our screen graphics object
 		Graphics screenGraphics;
 		
 		// Try to set up the reference...
 		try {
 			// Get the reference
 			screenGraphics= this.getGraphics();
 			
 			// And use it to draw our buffer image
 			if ((screenGraphics != null) && (bufferImage != null))
 				screenGraphics.drawImage(bufferImage, 0, 0, null);
 			
 			// Clean up
 			screenGraphics.dispose();
 		} catch(Exception e) {
 			System.out.println("Exception in display()! "+ e);
 		}
 	}
 }
