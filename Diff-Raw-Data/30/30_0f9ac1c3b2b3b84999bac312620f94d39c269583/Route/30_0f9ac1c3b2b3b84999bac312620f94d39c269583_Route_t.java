 package com.example.excommonersebenelib;
 
 import java.util.ArrayList;
 
 
 
 public class Route {
 	public int no;
 	public String from;
 	public String to;
 	public ArrayList<BusStop> busStops;
	public ArrayList<Bus> buses;
 	
 	public Route(int x,String from,String to){
 		no = x;
 		this.from = from;
 		this.to = to;
 		busStops = new ArrayList<BusStop>();
		buses = new ArrayList<Bus>();
 	}
 	public void addBusStop(BusStop x){
 		busStops.add(x);
 	}
	
	public void addBus(Bus x){
		buses.add(x);
	}
 }
