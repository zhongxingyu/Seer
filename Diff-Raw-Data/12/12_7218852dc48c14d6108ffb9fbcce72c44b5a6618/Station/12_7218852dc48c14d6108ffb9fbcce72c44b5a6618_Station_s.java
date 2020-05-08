 package com.af.mbtwhere;
 
 import java.util.ArrayList;
 
 public class Station {
 	public final String name;
 	public final String lat;
 	public final String lng;
 	public String flag;
 	private ArrayList<Route> inbound_routes = new ArrayList<Route>();
 	private ArrayList<Route> outbound_routes = new ArrayList<Route>();
 	
 	public Station(String name, String lat, String lng, String flag) {
 		this.name = name;
 		this.lat = lat;
 		this.lng = lng;
 		this.flag = flag;
 	}
 	
 	public void addInbound(Route r) {
 		inbound_routes.add(r);
 	}
 	
 	public void addOutbound(Route r) {
 		outbound_routes.add(r);
 	}
 	
 	public Route[] getInbound() {
		return (Route[]) inbound_routes.toArray();
 	}
 	
 	public Route[] getOutbound() {
		return (Route[]) outbound_routes.toArray();
 	}
 	
 	@Override public boolean equals(Object o) {
 		if (!(o instanceof Station)) {
 			return false;
 		}
 		Station s = (Station)o; return s.name == name;
 	}
 	
 	@Override public int hashCode() {
 		return name.hashCode();
 	}
 	
 	@Override public String toString() {
 		return name;
 	}
 }
