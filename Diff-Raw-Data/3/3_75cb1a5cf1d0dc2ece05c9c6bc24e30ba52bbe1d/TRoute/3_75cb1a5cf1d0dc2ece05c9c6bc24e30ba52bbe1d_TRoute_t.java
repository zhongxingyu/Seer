 package com.bluebarracudas.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A path on the MBTA.
  */
 public class TRoute {
 	/** Our stops */
 	private List<TStop> m_stops;
 	/** A list of predictions for the stops in m_stops */
 	private List<TPrediction> m_preds;
 	
 	public TRoute() {
 		m_stops = new ArrayList<TStop>();
 		m_preds = new ArrayList<TPrediction>();
 	}
 
 	public TRoute(TRoute pRouteA, TRoute pRouteB) {
 		this(pRouteA, pRouteB, 0);
 	}
 
 	public TRoute(TRoute pRouteA, TRoute pRouteB, int secondsAfter) {
 		m_stops = pRouteA.getStops();
 		if (m_stops.size() > 0 && pRouteB.getStops().size() > 0) {
 			if (m_stops.get(m_stops.size() - 1).getID() == pRouteB.getStops().get(0).getID())
 				m_stops.remove(m_stops.size() - 1);
 		}		
 		m_stops.addAll(pRouteB.getStops());
 		
 		setPredictions(secondsAfter);
 	}
 
 	public TRoute(List<TStop> stops) {
 		this(stops, 0);
 	}
 
 	public TRoute(List<TStop> stops, int secondsAfter) {
 		m_stops = stops;
 		m_preds = new ArrayList<TPrediction>();
 		
 		setPredictions(secondsAfter);
 	}
 	
 	public TRoute(List<TStop> stops, List<TPrediction> preds) {
 		m_stops = stops;
 		m_preds = preds;
 	}
 	
 	public List<TStop> getStops() {
 		return m_stops;
 	}
 	
 	public void addStop(TStop stop) {
 		m_stops.add(stop);
 	}
 	
 	public String printRoute() {
 		StringBuilder sb = new StringBuilder();
 		for (TStop stop : getStops()) {
 			if (stop != null) {
 				sb.append("Stop: ");
 				sb.append(stop.toString());
 				sb.append(", ");
 			}
 			
 		}
 		return sb.toString();
 	}
 	
 	public boolean equals(Object o) {
 		if (o.getClass() == this.getClass()) {
 			TRoute r = (TRoute) o;
 			if (getStops().size() == r.getStops().size()) {
 				for (int i = 0; i < getStops().size(); i++) {
 					if (getStops().get(i).getID() != r.getStops().get(i).getID()) {
 						return false;
 					}
 				}
 				return true;
 			}
 		}
 		
 		
 		return false;
 		
 	}
 	
 	private void setPredictions(int afterXSeconds) {
 		List<TPrediction> preds = new ArrayList<TPrediction>();
 		
 		TPrediction pred = m_stops.get(0).getNextPred(afterXSeconds);
 		preds.add(pred);
 		
 		for (int i = 1; i < m_stops.size(); i++) {
 			if (pred != null) {
 				int secToArrival = pred.getSecToArrival();
 				if (m_stops.get(i).getStopData().getLineData().equals(m_stops.get(i-1).getStopData().getLineData())) {
 					String tripID = pred.getTripID();
 					pred = m_stops.get(i).getNextPred(tripID, secToArrival);
					if (pred == null) {
						pred = m_stops.get(i).getNextPred(secToArrival);
					}
 				} else {
 					// If we are not on the same line, then we are at a transfer station.
 					// Give the user a minute to run to the other line's platform.
 					pred = m_stops.get(i).getNextPred(secToArrival + 60);
 				}
 			}
 			preds.add(pred);
 		}
 		this.m_preds = preds;
 	}
 
 	public List<TPrediction> getPredictions() {
 		return m_preds;
 	}
 }
