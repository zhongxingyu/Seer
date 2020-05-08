 package com.af.mbtwhere;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class Line {
 	public final static String TAG = "MBTLine";
 	public final String name;
 	public final static int ANY = 0;
 	public final static int INBOUND = 1;
 	public final static int OUTBOUND = 2;
 	public final static String ANY_BRANCH = "-1";
 	public final static String BRAINTREE_BRANCH = "0";
 	public final static String ASHMONT_BRANCH = "1";
 	private ArrayList<Station> stations = new ArrayList<Station>();
 	
 	public Line(String name) {
 		this.name = name;
 	}
 	
 	@Override public boolean equals(Object o) {
 		if (!(o instanceof Line)) {
 			return false;
 		}
 		Line l = (Line)o; return l.name == name;
 	}
 	
 	@Override public int hashCode() {
 		return name.hashCode();
 	}
 	
 	@Override public String toString() {
 		return name;
 	}
 	
 	public void addStation(Station s) {
 		stations.add(s);
 	}
 	
 	public Station[] stations() {
 		return (Station[]) stations.toArray();
 	}
 	
 	public String[] getStationsByName() {
 		String[] ret = new String[stations.size()];
 		int i = 0;
 		for(Station station : stations) {
 			ret[i++] = station.name;
 		}
 		return ret;
 	}
 	
 	public Station getStation(String name) {
 		for(Station station : stations) {
			if(name.equals(station.name)) {
 				return station;
 			}
 		}
 		return null;
 	}
 	
 	public Station getStationByCode(String code) {
 		return getStationByCode(code, ANY);
 	}
 	
 	public Station getStationByCode(String code, int direction) {
 		for(Station station : stations) {
 			for(Route route: station.getInbound()) {
 				if (direction==INBOUND || direction==ANY) {
 					if(code.equals(route.code)) {
 						return route.station;
 					}
 				}
 			}
 			for(Route route: station.getOutbound()) {
 				if (direction==OUTBOUND || direction==ANY) {
 					if(code.equals(route.code)) {
 						return route.station;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	public String getCodeFor(String start, String end) {
 		Station start_station = getStation(start);
 		Station end_station = getStation(end);
 		
 		Route next_out = nextOutboundTo(start_station, end_station);
 		Route next_in = nextInboundTo(start_station, end_station);
 		if(next_out != null) {
 			return next_out.code;
 		} else if (next_in != null) {
 			return next_in.code;
 		} else {
 			return null;
 		}
 	}
 	
 	public Route nextOutboundTo(Station start, Station end) {
 		if(start.equals(end)) {
 			return start.getOutbound()[0]; //doesn't matter which, since we are referencing `code` which is the same for all outbound routes
 		} else {
 			for(Route route : start.getOutbound()) {
 				if(route.is_on(end.flag)) {
 					Station next_station = getStationByCode(route.nextCode, OUTBOUND);
 					if(next_station == null) {
 						Log.v(TAG, "flipping from outbound to inbound");
 						next_station = getStationByCode(route.nextCode, INBOUND);
 						return nextInboundTo(next_station, end);
 					} else {
 						return nextOutboundTo(next_station, end);
 					}
 				}
 			}
 			return null;
 		}
 	}
 	
 	public Route nextInboundTo(Station start, Station end) {
 		if(start.equals(end)) {
 			return start.getInbound()[0];
 		} else {
 			for(Route route : start.getInbound()) {
 				if(route.is_on(end.flag)) {
 					Station next_station = getStationByCode(route.nextCode, INBOUND);
 					if(next_station == null) {
 						Log.v(TAG, "flipping from inbound to outbound");
 						next_station = getStationByCode(route.nextCode, OUTBOUND);
 						return nextOutboundTo(next_station, end);
 					}
 					return nextInboundTo(next_station, end);
 				}
 			}
 			return null;
 		}
 	}
 }
