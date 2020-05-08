 package edu.ucsb.deepspace;
 
 public class GalilCalc {
 	static double calcAccel(double distance, double time){
 		return round((2*distance)/(time*time), 4);
 	}
	static double round(double number, int dec_place){
 		return(Math.round(number*Math.pow(10,dec_place))/Math.pow(10, dec_place));
 	}
 	public static double calcMinScanTime(double distance, double d_max, double v_max, double acc){
 		double time = distance/v_max;
 		double totDistance =.5*acc* time*time+ distance;
 		
 		if (totDistance>d_max)
 			return 0;
 		return time;
 		
 		
 	}
 	
 
 
 }
