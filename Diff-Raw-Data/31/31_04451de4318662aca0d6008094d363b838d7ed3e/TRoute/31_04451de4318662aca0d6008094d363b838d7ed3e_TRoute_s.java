 package com.bluebarracudas.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A path on the MBTA.
  */
 public class TRoute {
 	/** Our stops */
 	private List<TStop> m_stops;
 	
 	public TRoute() {
 		m_stops = new ArrayList<TStop>();
 	}
 	
 	public TRoute(TRoute pRouteA, TRoute pRouteB) {
 		m_stops = pRouteA.getStops();
 		m_stops.addAll(pRouteB.getStops());
 	}
 	
 	public TRoute(List<TStop> stops) {
 		m_stops = stops;
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
 			sb.append("Stop: ");
 			sb.append(stop.toString());
 			sb.append(", ");
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
 }
