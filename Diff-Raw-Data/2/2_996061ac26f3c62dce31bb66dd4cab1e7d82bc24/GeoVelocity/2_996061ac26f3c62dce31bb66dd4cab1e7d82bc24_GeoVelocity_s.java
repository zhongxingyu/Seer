 package kimononet.geo;
 
 import java.util.Date;
 
 import kimononet.net.parcel.Parcel;
 import kimononet.net.parcel.Parcelable;
 
 /**
  * The class represents a GPS velocity vector. 
  * 
  * <pre>
  * 
  * GeoLocation initialLocation = new GeoLocation(0.0, 0.0, 0.0);
  * GeoVector v = new GeoVector(initialLocation);
  * 
  * GeoLocation finalLocation = new GeoLocation(1.0, 1.0, 0.0);
  * 
  * v.update(finalLocation);
  * 
  * System.out.println(v);
  * 
  * </pre> 
  * 
  * @author Zorayr Khalapyan
  *
  */
 public class GeoVelocity implements Parcelable {
 	
 	/**
 	 * Indicates the number of bytes in a velocity parcel.
 	 */
 	public static final int PARCEL_SIZE = 8;
 	
 	/**
 	 * Change in location over time in m/s.
 	 */
 	private float speed;
 	
 	/**
 	 * Average accuracy of all considered locations in feet.
 	 */
 	private float averageAccuracy = 0;
 	
 	/**
 	 * Last reported location.
 	 */
 	private GeoLocation currentLocation;
 	
 	/**
 	 * Initial bearing as calculated by 
 	 * {@link #computeDistanceAndBearing(GeoLocation, float[])}.
 	 */
 	private float initialBearing;
 	
 	/**
 	 * Final bearing as calculated by 
 	 * {@link #computeDistanceAndBearing(GeoLocation, float[])}.
 	 */
 	private float finalBearing;
 	
 	/**
 	 * Creates a new velocity vector that is not initialized at a certain point.
 	 * At least two invocations of {@link #update(GeoLocation)} will need to be
 	 * made before producing valid @{@link #getLongitudeComponent()} or 
 	 * {@link #getLatitudeComponent()} results.
 	 */
 	public GeoVelocity(){
 		
 	}
 	
 	/**
 	 * Creates a new vector initialized at the provided location. 
 	 * 
 	 * Initially, both change in longitude and change in latitude will be 
 	 * initialized to zero and will be modified once current location is updated 
 	 * using {@link #updateLocation(GeoLocation)}.
 	 *  
 	 * @param currentLocation The current location of the vector. 
 	 */
 	public GeoVelocity(GeoLocation currentLocation){
 		this.currentLocation = currentLocation;
 	}
 	
 	public GeoVelocity(Parcel parcel){
 		parse(parcel);
 	}	
 	
 	public float getInitialBearing(){
 		return initialBearing;
 	}
 	
 	public void update(GeoLocation newLocation){
 		
 		//If an initial location has not been specified, save the new location 
 		//as the current location and exit.
 		if(currentLocation == null){
 			currentLocation = newLocation;
 			return;
 		}
 		
 		//Inverse formula returns three values: distance, initial and
 		//final bearing.
 		float[] inverseFormulaResults = computeDistanceAndBearing(newLocation);
 		
 		//Compute change in time in order to compute speed from distance.
		int dTime = currentLocation.getLastUpdateTime() - newLocation.getLastUpdateTime();
 
 		//Save speed, initial and final bearings as calculated by the inverse
 		//formula.
 		this.speed          = inverseFormulaResults[0] / dTime;
 		this.initialBearing = inverseFormulaResults[1];
 		this.finalBearing   = inverseFormulaResults[2];
 		
 		//Update the average accuracy.
 		this.averageAccuracy = (averageAccuracy + newLocation.getAccuracy()) / 2;
 	    
 		//Save the provided location as the new current location. This location 
 		//will be used for future speed and bearing updates.
 		this.currentLocation = newLocation;
 	}
 	
 	
 	/**
 	 * Returns the average accuracy in feet of all considered points.
 	 * @return the average accuracy in feet of all considered points.
 	 */
 	public double getAverageAccuracy(){
 		return this.averageAccuracy;
 	}
 	
 	/**
 	 * Returns the current speed.
 	 * @return Current speed.
 	 */
 	public float getSpeed(){
 		return this.speed;
 	}
 	
 	/**
 	 * Returns the current bearing.
 	 * @return Current bearing.
 	 */
 	public float getBearing(){
 		return this.finalBearing;
 	}
 	
 	public void parse(Parcel parcel){
 		speed        = parcel.getFloat();
 		finalBearing = parcel.getFloat();
 	}
 	
 	public Parcel toParcel(){
 		
 		Parcel parcel = new Parcel(getParcelSize());
 		
 		parcel.add(this.getSpeed());
 		parcel.add(this.getBearing());
 		
 		return parcel;
 	}
 	
 	/**
 	 * Returns a string representation of the current vector.
 	 * @return A string representation of the current vector.
 	 */
 	public String toString(){
 		
 		String time;
 		
 		if(currentLocation == null){
 			time = "unknown"; 
 		}else{
 			time = new Date(currentLocation.getLastUpdateTime() * 1000).toString();
 		}										  
 		
  		return "Speed: "            + getSpeed() + "\t" +
 			   "Bearing: "          + getBearing() + "\t" +
 	       	   "Average Accuracy: " + getAverageAccuracy() + "\t" +
 	       	   "Timestamp: "        + time;
 	}
 	
 	public int getParcelSize(){
 		return PARCEL_SIZE;
 	}
 	
 	/**
 	 * The function uses the "Inverse Formula" as described in Survey Review, 
 	 * April, 1975 (section 4), to calculate distance, initial bearing, and 
 	 * final bearing. 
 	 * 
 	 * @see <a href="http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf">Survey Review, 1975.</a>
 	 */
 	private float[] computeDistanceAndBearing(GeoLocation newLocation) {
 		
 		int MAXITERS = 20;
 		
 		//Convert latitude and longitude to radians.
 		double lat1 = currentLocation.getLatitude()  * Math.PI / 180.0;
 		double lon1 = currentLocation.getLongitude() * Math.PI / 180.0;
 		
 		double lat2 = newLocation.getLatitude()  * Math.PI / 180.0;
 		double lon2 = newLocation.getLongitude() * Math.PI / 180.0;
 		
 		//No need to make these constants, the radii of the Earth don't change
 		//quite as often. 
         double a = 6378137.0; // WGS84 major axis
         double b = 6356752.3142; // WGS84 semi-major axis
         
 		double f = (a - b) / a;
 		double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);
 		
 		double L = lon2 - lon1;
 		double A = 0.0;
 		double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
 		double U2 = Math.atan((1.0 - f) * Math.tan(lat2));
 		
 		double cosU1 = Math.cos(U1);
 		double cosU2 = Math.cos(U2);
 		double sinU1 = Math.sin(U1);
 		double sinU2 = Math.sin(U2);
 		double cosU1cosU2 = cosU1 * cosU2;
 		double sinU1sinU2 = sinU1 * sinU2;
 		
 		double sigma = 0.0;
 		double deltaSigma = 0.0;
 		double cosSqAlpha = 0.0;
 		double cos2SM = 0.0;
 		double cosSigma = 0.0;
 		double sinSigma = 0.0;
 		double cosLambda = 0.0;
 		double sinLambda = 0.0;
 		
 		double lambda = L; // initial guess
 		for (int iter = 0; iter < MAXITERS; iter++) {
 			double lambdaOrig = lambda;
 			cosLambda = Math.cos(lambda);
 			sinLambda = Math.sin(lambda);
 			double t1 = cosU2 * sinLambda;
 			double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
 			double sinSqSigma = t1 * t1 + t2 * t2; // (14)
 			sinSigma = Math.sqrt(sinSqSigma);
 			cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
 			sigma = Math.atan2(sinSigma, cosSigma); // (16)
 			double sinAlpha = (sinSigma == 0) ? 0.0 :
 			cosU1cosU2 * sinLambda / sinSigma; // (17)
 			cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
 			cos2SM = (cosSqAlpha == 0) ? 0.0 :
 			cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)
 			
 			double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
 			A = 1 + (uSquared / 16384.0) * // (3)
 			(4096.0 + uSquared *
 			(-768 + uSquared * (320.0 - 175.0 * uSquared)));
 			double B = (uSquared / 1024.0) * // (4)
 			(256.0 + uSquared *
 			(-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
 			double C = (f / 16.0) *
 			cosSqAlpha *
 			(4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
 			double cos2SMSq = cos2SM * cos2SM;
 			deltaSigma = B * sinSigma * // (6)
 			(cos2SM + (B / 4.0) *
 			(cosSigma * (-1.0 + 2.0 * cos2SMSq) -
 			(B / 6.0) * cos2SM *
 			(-3.0 + 4.0 * sinSigma * sinSigma) *
 			(-3.0 + 4.0 * cos2SMSq)));
 			
 			lambda = L +
 			(1.0 - C) * f * sinAlpha *
 			(sigma + C * sinSigma *
 			(cos2SM + C * cosSigma *
 			(-1.0 + 2.0 * cos2SM * cos2SM))); // (11)
 			
 			double delta = (lambda - lambdaOrig) / lambda;
 			
 			if (Math.abs(delta) < 1.0e-12) {
 				break;
 			}
 		}
 		
 		float distance = (float) (b * A * (sigma - deltaSigma));
 		
 		float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
 		cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
 		initialBearing *= 180.0 / Math.PI;
 		
 		float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
 		-sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
 		finalBearing *= 180.0 / Math.PI;
 		
 		
 		return new float[] {distance, initialBearing, finalBearing};
 		
 	}
 
 
 
 }
 	
