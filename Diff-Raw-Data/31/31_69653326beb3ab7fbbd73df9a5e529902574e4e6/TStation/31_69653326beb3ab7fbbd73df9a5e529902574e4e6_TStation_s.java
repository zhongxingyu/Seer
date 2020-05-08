 package com.bluebarracudas.model;
 
 import java.awt.geom.Point2D;
 import java.util.List;
 
 /**
  * A real-life MBTA station. A station contains a number of TStops.
  *
  * @author Tom Fiset
  */
 public class TStation {
 	/** Our station meta data */
 	TStationData m_pStationData;
 	/** Our xy position on the screen */
 	Point2D m_pPosition;
 	/** A list of our TStops */
 	List<TStop> m_pStops;
 
 	/** Initialize with station meta data and a position */
 	TStation( TStationData pData, Point2D pPosition ) {
 		m_pStationData = pData;
 		m_pPosition = pPosition;
 	}
 
 	/** Returns our name */
 	public String getName() {
 		return m_pStationData.getName();
 	}
 
 	/** Returns our position */
 	public Point2D getPosition() {
 		return m_pPosition;
 	}
 
 	/** Add a stop */
 	public void addStop(TStop pStop) {
 		m_pStops.add(pStop);
 	}
 }
