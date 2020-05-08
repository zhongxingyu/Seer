 package edu.upenn.cis573;
 
 /**
  * This class contains static methods used for performing statistics and
  * other calculations on GPX data.
 */
 
 
 public class GPXcalculator {
 
     public static final int R = 6371; // radius of the earth in km
     
     long t;
     
     public long calculateElapsedTime(Object o) {
     	if (o instanceof GPXtrk) return time1((GPXtrk)o);
     	else if (o instanceof GPXtrkseg) {
     		//GPXtrkseg seg = (GPXtrkseg)o;
     		return GPXtrkseg.time((GPXtrkseg)o);
     	}
     	else return -1;
     }
 
 
     /**
      * Calculates the elapsed time for all segments in the track.
      * Note that it does not include the time <b>between</b> segments.
      *
      * @param trk The track for which to calculate the elapsed time.
      * @return the elapsed time in seconds; -1 if the track object is null
      */
     private long time1(GPXtrk trk) {
 	
 		if (trk == null) return -1;
 	
 		t = 0;
 	
 		// iterate over all the segments and calculate the time for each
 		GPXtrkseg trksegs[] = trk.trksegs();
 	
 		for (int i = 0; i < trksegs.length; i++) {
 		    // keep a running total of the time for each segment
 		    t += calculateElapsedTime(trksegs[i]);
 		}
 		
 		return t;
     }
 
 
     /**
      * Calculates the distance traveled over the given segment by returning
      * the sum of the distances between successive track points.
      * The distance takes into account latitude, longitude, elevation, and curvature of the earth.
      * To account for the curvature of the earth, the spherical law of cosines
      * is used, based on http://www.movable-type.co.uk/scripts/latlong.html
      *
      * @param trkseg The track segment for which to calculate the distance traveled.
      * @return the total distance in meters
      */
     public double getDistanceInTrkseg(GPXtrkseg trkseg) {
 
 		double totalDistance = 0;
 		
 		// iterate over all the trkpts
 		GPXtrkpt pts[] = trkseg.trkpts();
 	
 		for (int j = 0; j < pts.length-1; j++) {
 		    
 		    // get this point and the next one
 		    GPXtrkpt pt1 = pts[j];
 		    GPXtrkpt pt2 = pts[j+1];
 		    
 //		    // convert lat and lon from degrees to radians
 //		    double lat1 = pt1.lat() * 2 * Math.PI / 360.0;
 //		    double lon1 = pt1.lon() * 2 * Math.PI / 360.0;
 //		    double lat2 = pt2.lat() * 2 * Math.PI / 360.0;
 //		    double lon2 = pt2.lon() * 2 * Math.PI / 360.0;
 //		    
 //		    // use the spherical law of cosines to figure out 2D distance
 //		    double d = Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)) * R;	
 //		    // now we need to take the change in elevation into account
 //		    double ele1 = pt1.ele();
 //		    double ele2 = pt2.ele();
 //		    
 //		    // calculate the 3D distance
 //		    double distance = Math.sqrt(d*d + (ele1-ele2)*(ele1-ele2));
 		    
 		    // add it to the running total
 		    totalDistance += doCalculation(pt1, pt2);
 	    
 		}
 		
 		return totalDistance;
 
     }
     
     private double doCalculation(GPXtrkpt pt1, GPXtrkpt pt2){
     		double distance = 0.0;
     		// convert lat and lon from degrees to radians
 		    double lat1 = pt1.lat() * 2 * Math.PI / 360.0;
 		    double lon1 = pt1.lon() * 2 * Math.PI / 360.0;
 		    double lat2 = pt2.lat() * 2 * Math.PI / 360.0;
 		    double lon2 = pt2.lon() * 2 * Math.PI / 360.0;
 		    
 		    // use the spherical law of cosines to figure out 2D distance
 		    double d = Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)) * R;	
 		    // now we need to take the change in elevation into account
 		    double ele1 = pt1.ele();
 		    double ele2 = pt2.ele();
 		    
 		    // calculate the 3D distance
 		    distance = Math.sqrt(d*d + (ele1-ele2)*(ele1-ele2));
     		return distance;
     }
 
     /**
      * Calculates the distance traveled over all segments in the specified
      * track by returning the sum of the distances for each track segment.
      *
      * @param trk The track for which to calculate the distance traveled.
      * @return the total distance in meters
      */
     public double calculateDistanceTraveled(GPXtrk trk) {
 	
 		double totalDistance = 0;
 	
 		// iterate over all the trksegs
 		GPXtrkseg segs[] = trk.trksegs();
 		for(int i=0;i<segs.length;i++){
 			totalDistance += getDistanceInTrkseg(segs[i]);
 		}
 //		for (int i = 0; i < segs.length; i++) {
 //		    // calculate the distance for each segment
 //
 //			// iterate over all the trkpts
 //			GPXtrkpt pts[] = segs[i].trkpts();
 //		
 //			for (int j = 0; j < pts.length-1; j++) {
 //			    
 //			    // get this point and the next one
 //			    GPXtrkpt pt1 = pts[j];
 //			    GPXtrkpt pt2 = pts[j+1];
 //			    
 //			    // convert lat and lon from degrees to radians
 //			    double lat1 = pt1.lat() * 2 * Math.PI / 360.0;
 //			    double lon1 = pt1.lon() * 2 * Math.PI / 360.0;
 //			    double lat2 = pt2.lat() * 2 * Math.PI / 360.0;
 //			    double lon2 = pt2.lon() * 2 * Math.PI / 360.0;
 //			    
 //			    // use the spherical law of cosines to figure out 2D distance
 //			    double d = Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)) * R;	
 //			    // now we need to take the change in elevation into account
 //			    double ele1 = pt1.ele();
 //			    double ele2 = pt2.ele();
 //			    
 //			    // calculate the 3D distance
 //			    double distance = Math.sqrt(d*d + (ele1-ele2)*(ele1-ele2));
 //			    
 //			    // add it to the running total
 //			    totalDistance += distance;
 //			}
 //		}
 	
 		return totalDistance;
 	
 
     }
 
     /**
      * Calculate the average speed over the entire track by determining
      * the distance traveled and the total time for each segment.
      *
      * @param trk The track for which to calculate the average speed
      * @return the average speed in meters per second.
      */
     public double calculateAverageSpeed(GPXtrk trk) {
 
 		long time = 0;
 
 		// iterate over all the segments and calculate the time for each
 		GPXtrkseg trksegs[] = trk.trksegs();
 	
 		for (int i = 0; i < trksegs.length; i++) {
 		    // keep a running total of the time for each segment
 		    time += calculateElapsedTime(trksegs[i]);
 		}		
 		
 		// figure out the distance in kilometers
 		double distance = calculateDistanceTraveled(trk);
 	
 		// return the average in meters/second
 		return distance/time;
 
     }
 
 
     /**
      * Calculate the bearing (direction) from the first point in the
      * track to the last point in the track, using the bearing calculation
      * from http://www.movable-type.co.uk/scripts/latlong.html
      *
      * @param trk The track for which to calculate the overall bearing.
      * @return the bearing in degrees
      */
     public double bearing(GPXtrk trk) {
 
 //		// get the first trkpt in the first trkseg
 //		GPXtrkpt start = trk.trkseg(0).trkpt(0);
 //		// get the last trkpt in the last trkseg
 //		GPXtrkseg lastSeg = trk.trkseg(trk.numSegments()-1);
 //		GPXtrkpt end = lastSeg.trkpt(lastSeg.numPoints()-1);
 //	
 //		// get the points and convert to radians
 //		double lat1 = start.lat() * 2 * Math.PI / 360.0;
 //		double lon1 = start.lon() * 2 * Math.PI / 360.0;
 //		double lat2 = end.lat() * 2 * Math.PI / 360.0;
 //		double lon2 = end.lon() * 2 * Math.PI / 360.0;
 		
 		return trk.bearingDele();
 	
 
     }
 
 
     /**
      * Determines which track segment has the fastest average speed.
      *
      * @param trk The track for which to calculate the fastest segment.
      * @return the 0-based index of the fastest segment.
      */
     public int calculateFastestSegment(GPXtrk trk) {
 
 		GPXtrkseg trksegs[] = trk.trksegs();
 	
 		int fastestSegment = 0;
 		double fastestTime = 0;
 	
 		for (int i = 0; i < trksegs.length; i++) {
		    if (getDistanceInTrkseg(trksegs[i])/calculateElapsedTime(trksegs[i]) >= fastestTime)
			fastestSegment = i;
 		}
 	
 		return fastestSegment;
 
     }
 
 }
