 package com.example.excommonersebenelib;
 
 import java.util.ArrayList;
 
 
 
 public class Route {
 	int no;
 	public ArrayList<BusStop> busStops;
 	
 	public Route(int x){
 		no = x;
		busStops = new ArrayList<BusStop>();
 	}
 	public void addBusStop(BusStop x){
 		busStops.add(x);
 	}
 }
