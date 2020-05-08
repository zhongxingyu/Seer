 package com.bluebarracudas.model;
 
 /**
  * An estimated arrival time.
  * This data is provided by the MBTA JSON.
  */
public class TPrediction implements Comparable<TPrediction> {
 	/** Our stop. */
 	private TStop m_pStop;
 	/** Our ETA at our stop. */
 	private int m_nSecToArrival;
 	/** The destination of this train */
 	private String m_sDest;
 	/** */
 	private String m_sTripID;
 
 	/** Default constructor. */
 	public TPrediction(TStop pStop, int nSecToArrival, String dest, String tripID) {
 		this.m_pStop = pStop;
 		this.m_nSecToArrival = nSecToArrival;
 		this.m_sDest = dest;
 		this.m_sTripID = tripID;
 	}
 	
 	/** Returns the stop **/
 	public TStop getStop() {
 		return this.m_pStop;
 	}
 	
 	/** Returns the ETA at the stop **/
 	public int getSecToArrival() {
 		return this.m_nSecToArrival;
 	}
 	
 	/** Returns the destination of this train */
 	public String getDestination() {
 		return this.m_sDest;
 	}
 	
 	/** */
 	public String getTripID() {
 		return m_sTripID;
 	}
 
 	/** Returns our string representation. */
 	@Override
 	public String toString() {
 		return "(" + m_pStop.toString() + "," + m_nSecToArrival + ")";
 	}
 
 	@Override
	public int compareTo(TPrediction o) {
 		// TODO Auto-generated method stub
 		if(o instanceof TPrediction)
 			if( ((TPrediction) o).getSecToArrival() < m_nSecToArrival )
 				return 1;
 			else
 				return -1;
 		return 0;
 	}
 
 }
