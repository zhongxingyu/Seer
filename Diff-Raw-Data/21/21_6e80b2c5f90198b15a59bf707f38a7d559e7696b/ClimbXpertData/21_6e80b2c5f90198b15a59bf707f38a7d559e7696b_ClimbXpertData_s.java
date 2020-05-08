 package com.example.climbxpert;
 
 import java.util.ArrayList;
 
 import com.example.climbxpert.POI.POI;
 
 public class ClimbXpertData {
 	public static ArrayList<POI> POIList;
 	public static int pid=0;
 	public static int rid=0;
	public static boolean debug = true;
 	
 	public static POI getPOI(int pid) {
 		for (POI poi : POIList ) {
 			if (poi.pid == pid)
 				return poi;
 		}
 		return null;
 	}
 }
