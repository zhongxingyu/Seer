 package edu.pugetsound.npastor.riderGen;
 
 import java.awt.geom.Point2D;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Scanner;
 
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.DefaultFeatureCollection;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.graphhopper.GHResponse;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 
 import edu.pugetsound.npastor.TacomaDRTMain;
 import edu.pugetsound.npastor.routing.Routefinder;
 import edu.pugetsound.npastor.utils.Constants;
 import edu.pugetsound.npastor.utils.DRTUtils;
 import edu.pugetsound.npastor.utils.Log;
 import edu.pugetsound.npastor.utils.RiderChars;
 import edu.pugetsound.npastor.utils.RiderChars.DayDivision;
 import edu.pugetsound.npastor.utils.Trip;
 
 /**
  * Generates all daily trips on the DRT network
  * @author Nathan Pastor
  *
  */
 public class TripGenerator {
 
 	public final static String TAG = "TripGenerator";
 	
 	private final static String TRIP_FILE_LBL = "Trip";
 	
 	// Minimum allowed trip time
 	public final static int MIN_TRIP_TIME_MINS = 5;
 	
 	private ArrayList<Trip> mTrips;
 	private RiderChars mRiderChars;
 	private PCAgeEmployment mPCData;
 	private Random mRandom;
 	private Routefinder mRouter;
 	private TractPointGenerator mPointGen;
 
 	public TripGenerator(boolean isRerun) {
 		mTrips = new ArrayList<Trip>();
 		mRiderChars = new RiderChars(isRerun);
 		mPCData = new PCAgeEmployment();
 		mRandom = new Random();
 		mRouter = new Routefinder();
 		mPointGen = new TractPointGenerator();
 	}
 
 	/**
 	 * Begins the trip generation process
 	 */
 	public void generateTrips() {
 		for(int i = 0; i < Constants.TOTAL_TRIPS; i++) {
 			mTrips.add(new Trip(i));
 		}
 		// Generate all trip attributes. ORDER IS IMPORTANT!
 		generateTripTypes();
 		generateAges();
 		assignDirections();
 		generateRoutes();
 		generatePickupTimes();
 				
 		onTripsGenerated();
 	}
 	
 	private void onTripsGenerated() {
 		mPointGen.onGenerationComplete();
 		writeTripsToFile();
 		writeTripGeoToShp();
 	}
 	
 	/**
 	 * Generates trips from a trip log. This allows for the same set of data to be used
 	 * across instances of the model, so we can test how different variables affect the
 	 * results
 	 */
 	public void generateTripsFromFile(String tripLogPath) {
 		File file = new File(tripLogPath);
 		Log.iln(TAG, "Loading trips from: " + file.getPath());
 		
 		try {
 			Scanner scanner = new Scanner(file);
 			while (scanner.hasNextLine()) {
 				String[] tokens = scanner.nextLine().split(" ");
 				if(!tokens[0].equals(TRIP_FILE_LBL))
 					continue;
 				// Build the trip from the file line
 				Trip newTrip = new Trip(Integer.valueOf(tokens[1]));
 				newTrip.setTripType(Integer.valueOf(tokens[2]));
 				newTrip.setRiderAge(Integer.valueOf(tokens[3]));
 				newTrip.setDirection(tokens[4].equals("1") ? true : false);
 				newTrip.setOriginTract(tokens[5]);
 				newTrip.setOriginPoint(new Point2D.Double(Double.valueOf(tokens[6]), Double.valueOf(tokens[7])));
 				newTrip.setDestinationTract(tokens[8]);
 				newTrip.setDestinationPoint(new Point2D.Double(Double.valueOf(tokens[9]), Double.valueOf(tokens[10])));
 				newTrip.setPickupTime(Integer.valueOf(tokens[11]));
 				newTrip.setCalInTime(Integer.valueOf(tokens[12]));
 				
 				generateDirections(newTrip);
 				// And add trip to list
 				mTrips.add(newTrip);
 			}
 			scanner.close();
 		} catch(FileNotFoundException ex) {
 			Log.e(TAG, "Unable to find trip file at: " + tripLogPath);
 			ex.printStackTrace();
 			System.exit(1);
 		}
 		
 		onTripsGenerated();
 	}
 	
 	public ArrayList<Trip> getTrips() {
 		return mTrips;
 	}
 
 	/**
 	 * Generates ages based on APTA age distributions
 	 */
 	private void generateAges() {
 		// Will contain all the generated ages
 		Log.iln(TAG, "Generating trip ages");
 		ArrayList<Integer> ages = new ArrayList<Integer>();
 
 		Object[] keys = mRiderChars.getAgeGroupPcts().keySet().toArray();
 		// Loop through the key set, process each age group
 		for(Object curKey : keys) {
 			double percentage = mRiderChars.getAgeGroupPct((Integer)curKey);
 			int groupTotal = (int)Math.ceil(percentage * Constants.TOTAL_TRIPS / 100); // Total riders in this group
 			switch((Integer)curKey) {
 				case Constants.APTA_AGE_0_14:
 					ages.addAll(generateAgesInRange(groupTotal, 0, 14));
 					break;
 				case Constants.APTA_AGE_15_19:
 					ages.addAll(generateAgesInRange(groupTotal, 15, 19));
 					break;
 				case Constants.APTA_AGE_20_24:
 					ages.addAll(generateAgesInRange(groupTotal, 20, 24));
 					break;
 				case Constants.APTA_AGE_25_34:
 					ages.addAll(generateAgesInRange(groupTotal, 25, 34));
 					break;
 				case Constants.APTA_AGE_35_44:
 					ages.addAll(generateAgesInRange(groupTotal, 35, 44));
 					break;
 				case Constants.APTA_AGE_45_54:
 					ages.addAll(generateAgesInRange(groupTotal, 45, 54));
 					break;
 				case Constants.APTA_AGE_55_64:
 					ages.addAll(generateAgesInRange(groupTotal, 55, 64));
 					break;
 				case Constants.APTA_AGE_65_OVER:
 					ages.addAll(generateAgesInRange(groupTotal, 65, 80)); //TODO: DETERMINE MAX AGE HERE
 			}
 		}
		Log.iln(TAG, "Total ages genererated: " + ages.size());
 		// Finally add ages to trips, but do it randomly
 		for(Trip t : mTrips) {
 			int randomIndex = mRandom.nextInt(ages.size());
 			t.setRiderAge(ages.get(randomIndex)); // Pick a random age out of ages list
 			ages.remove(randomIndex);
 		}
 	}
 
 	private ArrayList<Integer> generateAgesInRange(int quantity, int min, int max) {
 		ArrayList<Integer> ages = new ArrayList<Integer>();
 		for(int i = 0; i < quantity; i++) {
 			int age = mRandom.nextInt(max - min + 1) + min; // +1 for exclusivity
 			ages.add(age);
 		}
 		return ages;
 	}
 
 	private void generateTripTypes() {
 		
 		Log.iln(TAG, "Generating trip types");
 		// Will contain all the generated ages
 		ArrayList<Integer> trips = new ArrayList<Integer>();
 
 		Object[] keys = mRiderChars.getTripTypePcts().keySet().toArray();
 		// Loop through the key set, process each trip type
 		for(int i = 0; i < keys.length; i++) {
 			int curKey = (Integer) keys[i];
 			double percentage = mRiderChars.getTripTypePct(curKey);
 			int groupTotal = (int) Math.ceil(percentage * Constants.TOTAL_TRIPS / 100); // Total riders in this group
 			for(int j = 0; j < groupTotal; j++) {
 				trips.add(curKey);
 			}
 		}
 
 		// Finally add ages to trips, but do it randomly
 		for(int i = 0; i < mTrips.size(); i++) {
 			int randomIndex = mRandom.nextInt(trips.size());
 			mTrips.get(i).setTripType(trips.get(randomIndex)); // Pick a random trip out of trips list
 			trips.remove(randomIndex);
 		}
 	}
 
 	private void assignDirections() {
 		Log.iln(TAG, "Assigning trip directions");
 		// Half of list is inbound, other half is outbound
 		ArrayList<Boolean> directions = new ArrayList<Boolean>();
 		for(int i = 0; i < mTrips.size(); i++) {
 			if (i < mTrips.size() / 2)
 				directions.add(true);
 			else 
 				directions.add(false);
 		}
 
 		// Add direction to trips randomly
 		for(int i = 0; i < mTrips.size(); i++) {
 			int randomIndex = mRandom.nextInt(directions.size());
 			mTrips.get(i).setDirection(directions.get(randomIndex)); // Pick a random trip out of tripls list
 		}
 	}
 	
 	/**
 	 * Generates the trip routes, including pickup and dropoff location
 	 * and routing
 	 */
 	private void generateRoutes() {
 		for(int i = 0; i < mTrips.size(); i++) {
 			if(i % 500 == 0)
 				Log.iln(TAG, "Generating trips, on trip " + i);
 			else if(i % 100 == 0)
 				Log.d(TAG, "Generating trips, on trip " + i);
 			Trip t = mTrips.get(i);
 			
 			int tripTime = -1;
 			while(tripTime < MIN_TRIP_TIME_MINS) {
 			// Loop until we've generate a trip longer than the min allowed time
 				generateEndpointTracts(t);
 				generateEndpoints(t);
 				tripTime = generateDirections(t);
 			}
 		}
 	}
 	
 	private void generateEndpointTracts(Trip t) {
 		int[] riderAgeGroup = {DRTUtils.getGroupForAge(t.getRiderAge())};
 		String firstTract = Trip.TRACT_NOT_SET;
 		String secondTract = Trip.TRACT_NOT_SET;
 		switch(t.getTripType()) {
 			case Constants.TRIP_COMMUTE:
 				int[] commuteCode = {Constants.PSRC_TOTAL};
 				firstTract = mPCData.getWeightedTract(riderAgeGroup, false);
 				secondTract = mPCData.getWeightedTract(commuteCode, true);
 				break;
 			case Constants.TRIP_MEDICAL_DENTAL:
 				int[] medCodes = {Constants.PSRC_SERVS, Constants.PSRC_GOVT};
 				firstTract = mPCData.getWeightedTract(riderAgeGroup, false);
 				secondTract = mPCData.getWeightedTract(medCodes, true);
 				break;
 			case Constants.TRIP_OTHER:
 				firstTract = mPCData.getWeightedTract(riderAgeGroup, false);
 				//TODO: should this be random?
 				secondTract = mPCData.getRandomTract();
 				break;
 			case Constants.TRIP_PERSONAL_BUSINESS:
 				firstTract = mPCData.getWeightedTract(riderAgeGroup, false);
 				//TODO: should this be random?
 				secondTract = mPCData.getRandomTract();
 				break;
 			case Constants.TRIP_SCHOOL:
 				int [] schoolCode = {Constants.PSRC_EDU};
 				firstTract = mPCData.getWeightedTract(riderAgeGroup, false);
 				secondTract = mPCData.getWeightedTract(schoolCode, true);
 				break;
 			case Constants.TRIP_SHOPPING_DINING:
 				int[] shopCodes = {Constants.PSRC_SERVS, Constants.PSRC_RETAIL};
 				firstTract = mPCData.getWeightedTract(riderAgeGroup, false);
 				secondTract = mPCData.getWeightedTract(shopCodes, true);
 				break;
 			case Constants.TRIP_SOCIAL:
 				firstTract = mPCData.getWeightedTract(riderAgeGroup, false);
 				//TODO: should this be weighted for total population level?
 				secondTract = mPCData.getWeightedTract(riderAgeGroup, false);
 		}
 		if(t.isOutbound()) {
 			t.setOriginTract(firstTract);
 			t.setDestinationTract(secondTract);
 		} else {
 			t.setOriginTract(secondTract);
 			t.setDestinationTract(firstTract);
 		}
 	}
 	
 	private void generateEndpoints(Trip t) {
 		t.setOriginPoint(mPointGen.randomPointInTract(t.getOriginTract()));
 		t.setDestinationPoint(mPointGen.randomPointInTract(t.getDestinationTract()));
 	}
 	
 	
 	//TODO: determine time distribution across day
 	//TODO: determine when requests are made known to agency
 	/**
 	 * Generates a trip pickup time and the time when the trip was made known to the agency.
 	 * Both in minute precision
 	 */
 	private void generatePickupTimes() {
 		
 		Log.iln(TAG, "Generating pickup times");
 		int minRequestableTime = Constants.BEGIN_OPERATION_HOUR * 60;
 		int maxRequestWindow = Constants.END_REQUEST_WINDOW * 60;
 		
 		Double[] percentByHour = buildDayDistribution();
 		double percentDynamicRequests = mRiderChars.getDynamicRequestPct();
 		
 		int unsatisfiableConditions = 0;
 		for(Trip t : mTrips) {
 			int requestTime = 0;
 			int callInTime = 0;
 			// First calculate request time
 			
 			double random = mRandom.nextDouble() * 100;
 			double runningTotal = 0;
 			for(int i = Constants.BEGIN_OPERATION_HOUR; i < Constants.END_OPERATION_HOUR; i++) {
 				runningTotal += percentByHour[i];
 				if(random < runningTotal) {
 					requestTime = i;
 					break;
 				}
 			} 
 			int mins = mRandom.nextInt(60);
 			requestTime = requestTime * 60 + (mins);// - (mins % Constants.PICKUP_INTERVAL));
 	
 			// Then set time at which trip was requested
 			double requestVal = mRandom.nextDouble() * 100;
 			if(requestVal < percentDynamicRequests) {
 				// Max request time will be the agency determined cutoff, or the requested time minus the buffer
 				int maxRequestTime = Math.min(requestTime - Constants.CALL_REQUEST_BUFFER_MINS, maxRequestWindow);
 				if(maxRequestTime < minRequestableTime) {
 					//TODO: fix this!!
 					unsatisfiableConditions++;
 				} else {
 					int windowMins = maxRequestTime - minRequestableTime;
 					callInTime = mRandom.nextInt(windowMins + 1) + minRequestableTime;
 				}
 			}
 			t.setPickupTime(requestTime);
 			t.setCalInTime(callInTime);
 		}
 		Log.iln(TAG, "Dynamic request: There were " + unsatisfiableConditions + 
 				" times where max request time is less than minimum requestable time. Unsatisfiable condition");
 	}
 	
 	// Using trip distribution data from RiderChars file, makes a list of trip distribution by hour
 	private Double[] buildDayDistribution() {
 		Double[] percentByHour = new Double[24];
 		HashMap<Integer, DayDivision> tripsByPeriod = mRiderChars.getTripDistributiions();
 		
 		// First do peak periods
 		DayDivision morningPeak = tripsByPeriod.get(Constants.MORN_PEAK_PERIOD);
 		int hrLength = morningPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[i + morningPeak.getStart()] = morningPeak.getPercentage() / hrLength;
 		}
 		DayDivision afternoonPeak = tripsByPeriod.get(Constants.AFTNOON_PEAK_PERIOD);
 		hrLength = afternoonPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[i + afternoonPeak.getStart()] = afternoonPeak.getPercentage() / hrLength;
 		}
 		
 		// Then fill in the rest of the day
 		DayDivision nonPeak = tripsByPeriod.get(Constants.MORNING_PERIOD);
 		hrLength = morningPeak.getStart() - Constants.BEGIN_OPERATION_HOUR;
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[Constants.BEGIN_OPERATION_HOUR + i] = nonPeak.getPercentage() / hrLength;
 		}
 		nonPeak = tripsByPeriod.get(Constants.DAY_PERIOD);
 		hrLength = afternoonPeak.getStart() - morningPeak.getStart() - morningPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[morningPeak.getStart() + morningPeak.getLength() + i] = nonPeak.getPercentage() / hrLength;
 		}
 		nonPeak = tripsByPeriod.get(Constants.EVENING_PERIOD);
 		hrLength = Constants.END_OPERATION_HOUR - afternoonPeak.getStart() - afternoonPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[afternoonPeak.getStart() + afternoonPeak.getLength() + i] = nonPeak.getPercentage() / hrLength;
 		}
 
 		return percentByHour;
 	}
 	
 	/**
 	 * Generates directions between trip endpoints
 	 * @return Driving time in minutes between endpoints
 	 */
 	private int generateDirections(Trip t) {
 
 		GHResponse routeResponse = mRouter.findRoute(t.getOriginPoint(), t.getDestinationPoint());
 		t.setRoute(routeResponse);
 		return (int) (routeResponse.getTime() / 60);
 	}
 	
 	/**
 	 * Writes the generated trips to 2 files. There is a "readable" file which contains an easier to read
 	 * representation of the trips, and a separate file which contains an easier to parse representation.
 	 * The latter is used for generateTripsFromFile()
 	 */
 	private void writeTripsToFile() {
 		
 		// Build text lists
 		ArrayList<String> parsableText = new ArrayList<String>();
 		ArrayList<String> readableText = new ArrayList<String>();
 		for(Trip t : mTrips) {
 			parsableText.add(buildParsableTripFileLine(t));
 			readableText.add(t.toString().replace("\n", "")); // Get rid of all line breaks
 		}
 		
 		// Write to file
 		DRTUtils.writeTxtFile(parsableText, Constants.TRIPS_VEHICLES_TXT, false);
 		DRTUtils.writeTxtFile(readableText, Constants.TRIPS_READABLE_TXT, false);
 	}
 	
 	/**
 	 * Helper method for writing trip txt files. Builds a line of the file, which contains all trip data
 	 * @param t The trip
 	 * @return All trip data in a string
 	 */
 	private String buildParsableTripFileLine(Trip t) {
 		String sp = " ";
 		String line = TRIP_FILE_LBL + sp +
 				t.getIdentifier() + sp + 
 				t.getTripType() + sp +
 				t.getRiderAge() + sp + 
 				(t.isOutbound() ? "1" : "0") + sp +
 				t.getOriginTract() + sp +
 				t.getOriginPoint().getX() + sp +
 				t.getOriginPoint().getY() + sp +
 				t.getDestinationTract() + sp +
 				t.getDestinationPoint().getX() + sp +
 				t.getDestinationPoint().getY() + sp +
 				t.getPickupTime() + sp +
 				t.getCallInTime();
 		return line;
 	}
 	
 	/**
 	 * Builds a shapefile feature type for trips. Each trip endpoint is represented as a point.
 	 * @return A point feature type
 	 */
 	private SimpleFeatureType buildFeatureType() {
 		// Build feature type
         SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
         builder.setName("TripEndpoints");
         builder.setCRS(DefaultGeographicCRS.WGS84); // long/lat projection system
         builder.add("Location", Point.class); // Geo data
         builder.add("Trip", String.class); // Trip identifier
         builder.add("Tract", String.class); // Tract number the point falls in
         builder.add("Stop Type", String.class); // Pickup/dropoff
         builder.add("Time", String.class); // Stop time
         
         final SimpleFeatureType featureType = builder.buildFeatureType();
         return featureType;
 	}
 	
 	private SimpleFeatureCollection createGeoFeatureCollection(SimpleFeatureType featureType) {
 		
         // New collection with feature type
 		SimpleFeatureCollection collection = FeatureCollections.newCollection();
 		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
         SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
         
         // Loop through trips, adding endpoints
         for(Trip t : mTrips) {
         	Point originPoint = geometryFactory.createPoint(new Coordinate(t.getOriginPoint().getX(), t.getOriginPoint().getY(), 0.0));
         	Point destPoint = geometryFactory.createPoint(new Coordinate(t.getDestinationPoint().getX(), t.getDestinationPoint().getY(), 0.0));
         	
         	int startTime = t.getPickupTime();
         	
         	// Add both feature to collection
         	if(t.getOriginTract() != Trip.TRACT_NOT_SET) {
 	            featureBuilder.add(originPoint); // Geo data
 	            featureBuilder.add(String.valueOf(t.getIdentifier())); // Trip identifier
 	            featureBuilder.add(String.valueOf(t.getOriginTract())); // Tract
 	            featureBuilder.add("pickup");
 	            featureBuilder.add(DRTUtils.minsToHrMin(startTime));
 	            SimpleFeature firstFeature = featureBuilder.buildFeature(null);
 	            ((DefaultFeatureCollection)collection).add(firstFeature);
         	}
             
         	if(t.getDestinationTract() != Trip.TRACT_NOT_SET) {
 	            featureBuilder.add(destPoint); // Location 
 	            featureBuilder.add(String.valueOf(t.getIdentifier())); // Trip identifier
 	            featureBuilder.add(String.valueOf(t.getDestinationTract())); // Tract
 	            featureBuilder.add("dropoff");
 	            featureBuilder.add(DRTUtils.minsToHrMin(startTime + (int)(t.getRoute().getTime() / 60)));
 	            SimpleFeature secondFeature = featureBuilder.buildFeature(null);
 	            ((DefaultFeatureCollection)collection).add(secondFeature);
         	}
         	
         	// Writing trip waypoints to trip geo file (for debugging mostly, this will clutter up the file)
 //        	PointList points = t.getRoute().getPoints();
 //    		for(int i = 0; i < points.getSize(); i++) {
 //    			Point waypoint = geometryFactory.createPoint(new Coordinate(points.getLongitude(i), points.getLatitude(i), 0.0));
 //    			featureBuilder.add(waypoint); // Location 
 //   	            featureBuilder.add(String.valueOf(t.getIdentifier())); // Trip identifier
 //   	            featureBuilder.add("Waypoint"); // Tract
 //   	            SimpleFeature secondFeature = featureBuilder.buildFeature(null);
 //   	            ((DefaultFeatureCollection)collection).add(secondFeature);
 //    		}
     		
         }
         return collection;
 	}
 	
 	/**
 	 * Writes the trip geographic data to a shapefile
 	 */
 	private void writeTripGeoToShp() {
 
 		// Build feature type and feature collection
 		SimpleFeatureType featureType = buildFeatureType();
 		SimpleFeatureCollection collection = createGeoFeatureCollection(featureType);
 		
 		// Format time and create filename
 		String dateFormatted = DRTUtils.formatMillis(TacomaDRTMain.tripGenStartTime);
 		String filename = TacomaDRTMain.getTripShpSimDirectory() + Constants.TRIP_PREFIX_SHP + dateFormatted + ".shp";
         File shpFile = new File(filename);
         
         DRTUtils.writeShapefile(featureType, collection, shpFile);
 	}
 }
