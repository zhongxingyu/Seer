 package com.delin.speedlogger.Math;
 
 import java.util.List;
 
 import android.location.Location;
 
 // uses Cubic Spline interpolation
 // based on http://www.jarvana.com/jarvana/view/org/rrd4j/rrd4j/2.0.7/rrd4j-2.0.7-sources.jar!/org/rrd4j/data/CubicSplineInterpolator.java?format=ok
 public class Interpolator {
 	private static final String provider = "Interpolator";
 	
 	private double[] time;
 	private double[] speed;
 	
 	// second derivatives
 	private double[] time2;   // used in TimeBySpeed()
 	private double[] speed2;  // used in SpeedByTime()
 
     
 	public Interpolator(List<Location> locList) {
     	time = new double[locList.size()];
 		speed = new double[locList.size()];
 		for(int i = 0; i < locList.size(); ++i){
 			time[i] = locList.get(i).getTime();
 			speed[i] = locList.get(i).getSpeed();
 		}
         speed2 = spline(time, speed);
         time2 = spline(speed, time);
     }
     
 	// here we get second derivatives
 	private double[] spline(double[] x, double[] y) {
 		// don't even ask me how it works :p
         int n = x.length;
         double[] y2 = new double[n];
         double[] u = new double[n - 1];
         y2[0] = y2[n - 1] = 0.0;
         u[0] = 0.0;
         for (int i = 1; i <= n - 2; i++) {
             double sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
             double p = sig * y2[i - 1] + 2.0;
             y2[i] = (sig - 1.0) / p;
             u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
             u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
         }
         for (int k = n - 2; k >= 0; k--) {
             y2[k] = y2[k] * y2[k + 1] + u[k];
         }
         return y2;
     }
     
     public Location SpeedByTime(long t) {
     	Location loc = new Location(Interpolator.provider);
     	loc.setTime(t);
     	loc.setSpeed((float) getValue(time, speed, speed2, t));
     	return loc;
     }
     
     public Location TimeBySpeed(float s) {
     	Location loc = new Location(Interpolator.provider);
     	loc.setSpeed(s);
    	loc.setTime((long) getValue(SpeedAsX(), time, time2, s));
     	return loc;
     }
     
     private double getValue(double[] x, double[] y, double[] y2, double xval) {
         if (xval < x[0] || xval > x[x.length-1]) {
             return Double.NaN;
         }
         // interval where xval is
         int low = 0;
         int high = x.length-1;
         while (high - low > 1) {
             int k = (high + low) / 2;
             if (x[k] > xval) {
             	high = k;
             }
             else {
             	low = k;
             }
         }
         double h = x[high] - x[low];
         double a = (x[high] - xval) / h;
         double b = (xval - x[low]) / h;
         return a * y[low] + b * y[high] +
                 ((a * a * a - a) * y2[low] + (b * b * b - b) * y2[high]) * (h * h) / 6.0;
     }
    
    // to use speed as X we must cut values after maxSpeed  
    double[] SpeedAsX() {
    	// get position of first max value
    	int max_n = 0;
    	for(int i=1; i<speed.length; ++i){
    		if (speed[i]>speed[max_n]) max_n = i;
    		else break;
    	}
    	// copy useful part
    	double[] s = new double[max_n+1];
    	System.arraycopy(speed, 0, s, 0, max_n+1);
		return s;
    	
    }
 }
