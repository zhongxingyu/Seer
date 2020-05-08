 package com.bluebarracudas.app;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.RenderingHints;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import com.bluebarracudas.model.TFactory;
 import com.bluebarracudas.model.TStation;
 import com.bluebarracudas.model.TStop;
 import com.bluebarracudas.model.TStopData;
 import com.bluebarracudas.model.TTrip;
 
 /** A panel to display our MBTA map */
 public class TMapPanel extends JPanel implements Observer {
 
 	/** The height & width of the trip circle **/
 	public static int tripSize = 6;
 	/** The height & width of the stop square **/
 	public static int stopSize = 10;
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 600;
 	public static final int PADDING = 100;
 
 	/** The size of our trip indicators */
 	private static final int TRIP_SIZE = 7;
 	/** The size of our station buttons */
 	private static final int STATION_SIZE = 10;
 
 	/** The color of our trip indicators */
 	private static final Color TRIP_COLOR = new Color(100, 190, 140);
 
 	/** Our station buttons */
 	private List<JButton> m_pStationButtons;
 
 	/** Default constructor */
 	public TMapPanel() {
 
 		// Set our preferred size
 		setPreferredSize(new Dimension(WIDTH + PADDING * 2, HEIGHT + PADDING * 2));
 
 		// Set the layout manager to be null so we can put buttons where we want them
 		this.setLayout(null);
 
 		// Get the action handler
 		THandler handler = new THandler();
 
 		m_pStationButtons = new ArrayList<JButton>();
 
 		// Create our station buttons
 		for (TStation pStation : TFactory.getAllStations()) {
 			Point2D pPos = pStation.getPosition();
 
 			// Create and set up a button
 			JButton stopButton = new JButton();
 			stopButton.setBounds((int)pPos.getX(), (int)pPos.getY(), STATION_SIZE, STATION_SIZE);
 			stopButton.setActionCommand(""+pStation.getID());
 			stopButton.addActionListener(handler);
 			stopButton.setVisible(true);
 
 			// Add it to our button list
 			m_pStationButtons.add(stopButton);
 		}
 
 		// Now add the buttons to our panel
 		for(JButton pButton : m_pStationButtons)
 			this.add(pButton);
 
 		// Get the focus
 		setFocusable(true);
 		requestFocusInWindow();
 	}
 
 	/**
 	 * Render our buffer image
 	 *
 	 * @author Tom Fiset
 	 * Revised by: Liz Brown
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 
 		Graphics2D g2 = (Graphics2D)g;
 		// Fill the screen with our background color
 		g2.setColor(Color.BLACK);
 		g2.fill(this.getBounds());
 		// Enable anti-aliasing
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 		                    RenderingHints.VALUE_ANTIALIAS_ON);
 
 		// Draw the lines between stops
		//renderLines(g2);
 
 		// Draw the trips on the background
 		renderTrips(g2);
 	}
 
 	/**
 	 * Draw lines between the stations Note: This is a very rough proof of
 	 * concept.
 	 *
 	 * @author Liz Brown
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderLines(Graphics2D g2) {
		renderLine(g2, TFactory.getStop(TStopData.OAK_GROVE.getID()));
		renderLine(g2, TFactory.getStop(TStopData.WONDERLAND.getID()));
		renderLine(g2, TFactory.getStop(TStopData.ALEWIFE.getID()));
		renderLine(g2, TFactory.getStop(TStopData.JFK_S_B.getID()));
 	}
 
	private void renderLine(Graphics2D g2, TStop stop) {
 		List<TStop> nextStops = stop.getNextStops();
 		TStop nextStop = null;
 		if (nextStops != null)
 			nextStop = nextStops.get(0);
 
		while (nextStop != null) {
 
 			// Draw a line
 			int offset = stopSize / 2;
			g2.setPaint(Color.white);
 			g2.drawLine((int)stop.getStation().getPosition().getX() + offset,
 					(int)stop.getStation().getPosition().getY() + offset,
 					(int)nextStop.getStation().getPosition().getX() + offset,
 					(int)nextStop.getStation().getPosition().getY() + offset);
 
 			stop = nextStop;
 
 			List<TStop> newNextStops = stop.getNextStops();
 			nextStop = (!newNextStops.isEmpty()) ? newNextStops.get(0) : null;
 		}
 	}
 
 	/**
 	 * Render all of the trips on the map
 	 *
 	 * @author Liz Brown, Tom Fiset
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderTrips(Graphics2D g2) {
 		// Set our trip color
 		g2.setPaint(TRIP_COLOR);
 
 		// Now draw our trips
 		for (TTrip trip : TFactory.getAllTrips())
 			g2.fill( new Ellipse2D.Double(trip.getPosition().getX(),
 			                              trip.getPosition().getY(),
 			                              TRIP_SIZE, TRIP_SIZE) );
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		System.out.println("UPDATE!");
 		repaint();
 	}
 }
