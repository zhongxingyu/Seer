 package com.bluebarracudas.model;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.List;
 
 /**
  * A real-life MBTA train stop.
  */
 public class TStop {
 	/** Our unique identifier. */
 	private int m_nID;
 	/** Our name. */
 	private String m_sName;
 	/** The lines we connect. */
 	private List<TLine> m_lines;
 	/** Our longitude and latitude. */
 	private Point2D m_position;
 	/** The position of the stop on the canvas */
 	private Point m_canvasPosition;
 
 	/** Package visible constructor */
 	public TStop( int nID, String sName, Point2D position, List<TLine> lines ) {
 		m_nID = nID;
 		m_sName = sName;
 		m_lines = lines;
 		m_position= position;
 	}
 
 	/** Returns our ID */
 	public int getID() {
 		return m_nID;
 	}
 	
 	/** Returns our name. */
 	public String getName() {
 		return m_sName;
 	}
 
 	/**
 	 * Returns the position of the stop adjusted to fit on the GUI
 	 */
 	public Point getCanvasPosition() {
 		// If the canvas position is not already set, calculate it
 		if (this.m_canvasPosition == null) {
			int x = TFactory.longitudeToUnits(m_position.getY(), 700);
			int y = TFactory.latitudeToUnits(m_position.getX(), 700);
 			this.m_canvasPosition = new Point(x, y);
 		}
 		return this.m_canvasPosition;
 	}
 	
 	/**
 	 * Returns the color of the line this stop is on.
 	 * Transfer stops are magenta. 
 	 */
 	public Color getColor() {
 		if (this.m_lines.size() > 1) {
 			return Color.magenta;
 		} else {
 			String line = this.m_lines.get(0).getName();
 			if(line.equals("blue"))		return Color.blue;
 			if(line.equals("orange"))	return Color.orange;
 			if(line.equals("red"))		return Color.red;
 		}
 		return Color.black;
 	}
 	
 	/** Convert this object to a String */
 	public String toString() {
 		return m_nID + ": " + m_sName;
 	}
 	
 	/** Returns the latitude and longitude */
 	public Point2D getPosition(){
 		return m_position;
 	}
 
 }
