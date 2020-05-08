 package com.example.excommonersebenelib;
 
 import java.util.ArrayList;
 
 
 
 public class Route {
 	public int no;
 	public String from;
 	public String to;
 	public ArrayList<BusStop> busStops;
 	
 	public Route(int x,String from,String to){
 		no = x;
 		this.from = from;
 		this.to = to;
 		busStops = new ArrayList<BusStop>();
 	}
 	public void addBusStop(BusStop x){
 		busStops.add(x);
 	}
 }
