 package com.bluebarracudas.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * A real-life MBTA train stop.
  */
 public class TStop {
 	/** Our station meta data */
 	private TStationData m_pStationData;
 	/** The TStation we are a part of */
 	private TStation m_pStation;
 	/** Our stop meta data */
 	private TStopData m_pStopData;
 	/** The next TStop in our line */
 	private List<TStop> m_pNextStops;
 	/** The previous TStop in our line */
 	private List<TStop> m_pPrevStops;
 
 	/** Package visible constructor */
 	public TStop( TStationData pStationData, TStopData pStopData ) {
 		m_pStationData = pStationData;
 		m_pStopData = pStopData;
 		m_pNextStops = new ArrayList<TStop>();
 		m_pPrevStops = new ArrayList<TStop>();
 	}
 
 	/** Convert this object to a String */
 	public String toString() {
 		return m_pStopData.name();
 	}
 
 	/** Returns the unique identifier for this stop */
 	public int getID() {
 		return m_pStopData.getID();
 	}
 
 	/** Returns the Station we are in */
 	public TStation getStation() {
 		if(m_pStation == null)
 			m_pStation = TFactory.getStation(m_pStationData.getID());
 		return m_pStation;
 	}
 
 	/** */
 	public TStopData getStopData() {
 		return m_pStopData;
 	}
 	
 	/** Returns the previous TStop on our line */
 	public List<TStop> getNextStops() {
 		return m_pNextStops;
 	}
 
 	/** Sets the next TStop on our line. */
 	public void addNextStop(TStop pStop) {
 		m_pNextStops.add(pStop);
 	}
 
 	/** Returns the previous TStop on our line. */
 	public List<TStop> getPrevStops() {
 		return m_pPrevStops;
 	}
 
 	/** Sets the previous TStop on our line. */
 	public void addPrevStop(TStop pStop) {
 		m_pPrevStops.add(pStop);
 	}
 	
 	public TPrediction getNextPred(String tripID, int seconds) {
 		TTrip trip = TFactory.getTrip(tripID);
 		
 		for (TPrediction pred : trip.getPredictions()) {
 				if (this.equals(pred.getStop()) && pred.getSecToArrival() >= seconds) {
 					return pred;
 				}
 		}
 		return null;
 	}
 	
 	public TPrediction getNextPred(int after) {
 		List<TPrediction> preds = getPredictionsAfterTime(after);
 		Collections.sort(preds);
 		if (!preds.isEmpty())
 			return preds.get(0);
 		else
 			return null;
 	}
 	
 	private List<TPrediction> getPredictionsAfterTime(int seconds) {
 		List<TPrediction> predictions = new ArrayList<TPrediction>();
 		
 		for (TTrip trip : TFactory.getAllTrips()) {
 			for (TPrediction pred : trip.getPredictions()) {
 				if (this.equals(pred.getStop()) && pred.getSecToArrival() >= seconds) {
 					predictions.add(pred);
 				}
 			}
 		}
 		return predictions;
 	}
 
 	public String getName() {
 		return m_pStopData.name();
 	}
 }
