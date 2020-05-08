 package com.delin.speedlogger;
 
 import android.util.Log;
 
 //got it here: http://wiki.openstreetmap.org/wiki/Mercator#Java_Implementation
 public class Mercator {
 	// radiuses of an Earth ellipse
 	final private static double R_BIG = 6378137.0;
     final private static double R_SMALL = 6356752.3142;
 
  	public static double mercX(double lon){
  		return R_BIG * Math.toRadians(lon);
  	}
 
  	public static double mercY(double lat) {
          if (lat > 89.5) lat = 89.5;
          else if (lat < -89.5) lat = -89.5;
          
         double temp = R_BIG / R_SMALL;
          double es = 1.0 - (temp * temp);
          double eccent = Math.sqrt(es);
          double phi = Math.toRadians(lat);
          double con = eccent * Math.sin(phi);
          double com = 0.5 * eccent;
          con = Math.pow(((1.0-con)/(1.0+con)), com);
          double ts = Math.tan(0.5 * ((0.5*Math.PI) - phi))/con;
          double y = 0 - R_BIG * Math.log(ts);
          
         Log.i("Mercator.mercY",
        	   "  lat = " + Double.toString(lat) + 
        	   "  temp = " + Double.toString(temp) +
        	   "  es = " + Double.toString(es) +
        	   "  eccent = " + Double.toString(es) +
        	   "  phi = " + Double.toString(phi) +
        	   "  con = " + Double.toString(con) +
        	   "  com = " + Double.toString(com) +
        	   "  ts = " + Double.toString(ts) +
        	   "  y = " + Double.toString(y));
          
          return y;
      }
 }
