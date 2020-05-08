 package com.bluebarracudas.model;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.List;
 
 
 /**
  * A trip on the MBTA.
  * This data is provided by the MBTA JSON. A trip can be
  * thought of as an individual train.
  */
 public class TTrip {
 	/** Our unique identifier. */
 	private String m_sID;
 	/** Our current predictions. */
 	private List<TPrediction> m_pPredictions;
 
 	/** Package visible constructor */
 	public TTrip( String sID ) {
 		m_sID= sID;
 	}
 
 	/**
 	 * Set our current predictions.
 	 *
 	 * @param pPredictions The predictions to change to.
 	 */
 	public void setPredictions( List<TPrediction> pPredictions ) {
 		this.m_pPredictions= pPredictions;
 	}
 
 	/** Get our current predictions. */
 	public List<TPrediction> getPredictions() {
 		return this.m_pPredictions;
 	}
 
 	/** Gets the id of the trip */
 	public String getID() {
 		return this.m_sID;
 	}
 
 	/**
 	 * Gets the canvas position of a trip.  This is either at a stop or at the
 	 * midpoint of the last and next stops.
 	 *
 	 * @author Liz Brown
 	 */
 	public Point2D getPosition() {
 		if (this.m_pPredictions != null && !this.getPredictions().isEmpty()) {
 			TPrediction nextPred = this.getPredictions().get(0);
 			TStop nextStop = nextPred.getStop();
 			Point2D nextPosn = nextStop.getPosition();
 			int offset = 4;
 			// If the trip is basically at the next stop, just display it at
 			// that stop.
 			if (nextPred.getSecToArrival() <= 20) {
 				if (nextStop.getID() % 2 != 0)
 					offset *= -1;
 				
 				return new Point2D.Double(nextPosn.getX() + offset, nextPosn.getY() + offset);
 			}
 			
 			// Otherwise, get the last stop's ID so we can use its position
 			int lastStopID = nextStop.getID();
 			if (nextStop.getID() % 2 == 0) {
 				lastStopID += 2;
 			} else {
 				lastStopID -= 2;
 				offset *= -1;
 			}
 			
 			// Make sure the last stop exists & is on the same line before doing calculations
 			TStop lastStop = TFactory.getStop(lastStopID);
 			if (lastStop != null && nextStop.getColor().equals(lastStop.getColor())) {
 				Point2D lastPosn = lastStop.getPosition();
 				Point2D midpt = getMidPoint(nextPosn.getX(), nextPosn.getY(), lastPosn.getX(), lastPosn.getY());
 				return new Point2D.Double(midpt.getX() + offset, midpt.getY() + offset);
 			} else {
 				return new Point2D.Double(nextPosn.getX() + offset, nextPosn.getY() + offset);
 			}
 		}
 		
 		return new Point(-100, -100);
 	}
 	
 	/**
 	 * Determines the midpoint of (x1, y1) and (x2, y2)
 	 *
 	 * @author Liz Brown
 	 */
 	public static Point2D getMidPoint(double x1, double y1, double x2, double y2) {
 		return new Point2D.Double((x1 + x2) / 2, (y1 + y2) / 2);
 	}
 }
