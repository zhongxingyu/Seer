 package edu.cst316.gompersacousticsystem;
 
 
 import java.util.ArrayList;
 
 //////////////////////////////////////////////////////////////////////////////////////////
 //*Revised version of Trilateration program*
 //Coordinates rewritten to match Gompers Vocational Center
 //Unnecessary programs deleted
 //Changed calculations slightly
 //Using Router objects with latitude and longitude coordinates hardcoded into them.
 //////////////////////////////////////////////////////////////////////////////////////////
 
 public class RouterTrilateration {
 
 	private static Router router1;
 	private static Router router2;
 	private static Router router3;
 
 	public RouterTrilateration(Router r1, Router r2, Router r3){
 		router1 = r1;
 		router2 = r2;
 		router3 = r3; 
 				
 		router1.setRouterLat(33.306067);
 		router2.setRouterLat(33.306079);
 		router3.setRouterLat(33.306046);
 		
 		router1.setRouterLong(-111.679155);
 		router2.setRouterLong(-111.679016);
 		router3.setRouterLong(-111.679103);
 	}
 	public static void setLocation(){
 		
 		router1.setRouterLat(33.306067);
 		router2.setRouterLat(33.306079);
 		router3.setRouterLat(33.306046);
 		
 		//router1.setRouterLat(33.524437); //Gompers Coordinates
 		//router2.setRouterLat(33.524344);
 		//router3.setRouterLat(33.524347);
 		
 		//router1.setRouterLong(-112.173313); //Gompers Coordinates
 		//router2.setRouterLong(-112.173544);
 		//router3.setRouterLong(-112.172918);
 		
 		router1.setRouterLong(-111.679155);
 		router2.setRouterLong(-111.679016);
 		router3.setRouterLong(-111.679103);
 		}
 	
 	public void updateRouters(){
 		router1.update();
 		router2.update();
 		router3.update();
 	}
 	
 	static double calcDistance(double signal){
 		
 		double base = 10;
 		double exponent = -(signal + 51.504)/16.532;
 		
 		//double distance = Math.pow(base, exponent);
 	    //104.09004338 + 13.26842562x + 0.57250833x^2 + 0.00986120x^3 + 0.00006099x^4
 		
<<<<<<< HEAD
 		//  double distance = 104.09004338 + 13.26842562 * signal + 0.57250833* Math.pow(signal,2)
=======
		 // double distance = 104.09004338 + 13.26842562 * signal + 0.57250833* Math.pow(signal,2)
>>>>>>> c0acb94b4d7922e08a5958696a82c1ce3631ca8b
         //+ 0.00986120*Math.pow(signal, 3) + 0.00006099 * Math.pow(signal,4);
 		
 		double distance = 730.24198315 + 52.33325511*signal + 1.35152407*Math.pow(signal, 2) 
 	    + 0.01481265*Math.pow(signal, 3) + 0.00005900*Math.pow(signal, 4)+0.00541703*180;
 		
 		return distance;
 		
 	}
 	
 	// Convert Feet into Meter
 		 static double calFeetToMeter(double signal) {		 
 			 return signal*0.3048;
 	}
 		 
 		 
 	// Converts signal distance to latitude and longitude units to find user location
 		 static double convertDistToLongLatDegree(double dist) {
 			 double result;
 			 double DistanceToLongLat;
 
 			 final int lat = 33; //This is Gompers latitude at the Vocational Center
 			 
 			//Additional information for calculating Longitudinal distance using Earth Radius for reference as a point
 			//Information not really needed though since area is fixed at a point.
 			 
 			 //final double EarthRadius = 6367449; 
 			 //final double a = 6378137;			 
 			 //final double b = 6356752.3;
 			 //final double ang = lat*(Math.PI/180);
 			 
 			
 //			 degreeResult = Math.cos(ang)*Math.sqrt((Math.pow(a,4)*(Math.pow(Math.cos(ang),2))
 //					 + (Math.pow(b,4)*(Math.pow(Math.sin(ang),2)))) 
 //					 / (Math.pow((a*Math.cos(ang)),2)+Math.pow((b*Math.sin(ang)),2)))
 //					 * Math.PI/180;
 			 
 			 DistanceToLongLat = 93203.9343;         // unit (meter), based on 33 degrees.
 			 result = dist/DistanceToLongLat;		 // convert distance to lat, long degree.
 			 return result;
 			 
 		 }
 		 
 		 
 		//myRotation used for fixing point of location
 		 static double[] myRotation(double x, double y, double dist, double deg) {
 			 
 			 double tmpX, tmpY;
 			 //ArrayList<Double> myLocation = null;
 			 double[]  myLocation = new double[3];
 			 
 			 tmpX = x*Math.cos((Math.PI/180)*deg)-y*Math.sin((Math.PI/180)*deg);
 			 tmpY = x*Math.sin((Math.PI/180)*deg)+y*Math.cos((Math.PI/180)*deg);
 			 
 //			 myLocation.add(tmpX);
 //			 myLocation.add(tmpY);
 			 myLocation[0] = tmpX;
 			 myLocation[1] = tmpY;
 			 myLocation[2] = dist;
 			 
 			 return myLocation;
 		 }
 		 
 		// public static double[] MyTrilateration(double Lat1, double Long1, double signal1, 
 			//		                     double Lat2, double Long2, double signal2,
 				//	                     double Lat3, double Long3, double signal3) {
 			 
 		   public static double[] MyTrilateration(){
 			 //ArrayList<Double> tmpWAP1, tmpWAP2, tmpWAP3;
 			 
 			   
 			 
 			 //Technical Standard used for accessing info through the internet.
 			 //Temporary array for storing coordinates for actual user location
 			 
 			 double[] tmpWAP1 = new double[3];
 			 double[] tmpWAP2 = new double[3]; 
 			 double[] tmpWAP3 = new double[3]; 
 			 
 			 double dist1, dist2, dist3;		
 			 double tmpLat2, tmpLong2, tmpLat3, tmpLong3;
 			 double tmpSlide, deg;
 			 double MyLat, MyLong;
 			 
 			 double[] MyLocation = new double[2];
 			 
 			 
 			//dist1 = convertDistToLongLatDegree(calFeetToMeter(calcDistance(signal1)));
 			//dist2 = convertDistToLongLatDegree(calFeetToMeter(calcDistance(signal2)));
 			//dist3 = convertDistToLongLatDegree(calFeetToMeter(calcDistance(signal3)));
 			 
 			 router1.update();
 			 router2.update();
 			 router3.update();
 			 int a = router1.getLevel();
 			 double j = calcDistance(a);
 			 double k = calFeetToMeter(j);
 			 double l = convertDistToLongLatDegree(k);
 			 dist1 = convertDistToLongLatDegree(calFeetToMeter(calcDistance(router1.getLevel())));
 			 dist2 = convertDistToLongLatDegree(calFeetToMeter(calcDistance(router2.getLevel())));
 			 dist3 = convertDistToLongLatDegree(calFeetToMeter(calcDistance(router3.getLevel())));
 			 
 			 	
 			 tmpLat2 = router2.getRouterLat() - router1.getRouterLat();
 			 tmpLong2 = router2.getRouterLong() - router1.getRouterLong();
 			 tmpLat3 = router3.getRouterLat() - router1.getRouterLat();
 			 tmpLong3 = router3.getRouterLong() - router1.getRouterLong();
 			 
 			 //tmpLat2  = Lat2 - Lat1;
 			 //tmpLong2 = Long2 - Long1;
 			 //tmpLat3  = Lat3 - Lat1;
 			 //tmpLong3 = Long3 - Long1;
 			 
 			 tmpSlide = Math.sqrt(Math.pow(tmpLat2,2)+Math.pow(tmpLong2,2));
 			 
 			 //deg = (180/Math.PI)*Math.acos( ((Math.pow(tmpLat2,2) + Math.pow(tmpSlide,2) - Math.pow(tmpLong2, 2)) / (2*tmpLat2*tmpSlide)) );
 			 deg = (180/Math.PI)*Math.acos( Math.abs(tmpLat2)/Math.abs(tmpSlide));
 			 
 			 //Quadrants refer to where on an x/y plane the degrees are mapped.
 			 
 			 // 1st quadrant
 			 if( (tmpLat2>0 && tmpLong2>0) ) {
 				 deg = 360 - deg;
 			 }
 			 // 2nd quadrant
 			 else if( (tmpLat2<0 && tmpLong2>0) ) {
 				 deg = 180 + deg;
 			 }
 			 // 3rd quadrant
 			 else if( (tmpLat2<0 && tmpLong2<0)){			
 				 deg = 180 - deg;
 			 }
 			 // 4th quadrant
 			 else if( (tmpLat2>0 && tmpLong2<0)) {
 				 deg = deg;
 			 }
 			 
 			 tmpWAP1[0] = 0.0;
 			 tmpWAP1[1] = 0.0;
 			 tmpWAP1[2] = dist1;		 
 			 tmpWAP2 = myRotation(tmpLat2, tmpLong2, dist2, deg);
 			 tmpWAP3 = myRotation(tmpLat3, tmpLong3, dist3, deg);
 			 
 
 			 MyLat = (Math.pow(tmpWAP1[2],2)-Math.pow(tmpWAP2[2],2)+Math.pow(tmpWAP2[0],2))/(2*tmpWAP2[0]);
 			 
 			 MyLong = (Math.pow(tmpWAP1[2],2)-Math.pow(tmpWAP3[2],2)-Math.pow(MyLat,2)
 					 +Math.pow(MyLat-tmpWAP3[0],2)+Math.pow(tmpWAP3[1], 2))/(2*tmpWAP3[1]);
 			 
 			 MyLocation = myRotation(MyLat, MyLong, 0, -deg);
 			 
 			 MyLocation[0] = MyLocation[0] + router1.getRouterLat();  //Lat1;
 			 MyLocation[1] = MyLocation[1] + router1.getRouterLong(); //Long1; 
 			 
 			 for (int i=0;i<=1;i++){
 				 
 				 System.out.println(MyLocation[i]);
 			 }
 			 
 			 return MyLocation;
 			 
 		 }
 }
 
