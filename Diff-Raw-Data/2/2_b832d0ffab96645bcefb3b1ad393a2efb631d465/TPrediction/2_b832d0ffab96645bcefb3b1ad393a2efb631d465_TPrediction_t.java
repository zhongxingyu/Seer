 package com.bluebarracudas.model;
 
 /**
  * An estimated arrival time.
  * This data is provided by the MBTA JSON.
  */
 public class TPrediction {
 	/** Our stop. */
 	private TStop m_pStop;
 	/** Our ETA at our stop. */
 	private int m_nSecToArrival;
 
 	/** Default constructor. */
 	public TPrediction(TStop pStop, int nSecToArrival) {
 		this.m_pStop = pStop;
 		this.m_nSecToArrival = nSecToArrival;
 	}
 	
 	/** Returns the stop **/
 	public TStop getStop() {
 		return this.m_pStop;
 	}
 	
 	/** Returns the ETA at the stop **/
 	public int getSecToArrival() {
 		return this.m_nSecToArrival;
 	}
 
 	/** Returns our string representation. */
 	@Override
 	public String toString() {
		return "(" + m_pStop.toString() + "," + m_nSecToArrival + ")";
 	}
 
 }
