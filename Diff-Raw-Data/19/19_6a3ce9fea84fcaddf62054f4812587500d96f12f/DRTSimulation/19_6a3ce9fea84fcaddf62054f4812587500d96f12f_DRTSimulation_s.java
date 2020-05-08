 package edu.pugetsound.npastor.simulation;
 
 import java.awt.geom.Point2D;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.DefaultFeatureCollection;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 
 import edu.pugetsound.npastor.TacomaDRTMain;
 import edu.pugetsound.npastor.routing.Rebus;
 import edu.pugetsound.npastor.routing.RouteCache;
 import edu.pugetsound.npastor.routing.RoutefinderTask;
 import edu.pugetsound.npastor.routing.Vehicle;
 import edu.pugetsound.npastor.routing.VehicleScheduleJob;
 import edu.pugetsound.npastor.routing.Rebus.RebusResults;
 import edu.pugetsound.npastor.utils.Constants;
 import edu.pugetsound.npastor.utils.DRTUtils;
 import edu.pugetsound.npastor.utils.Log;
 import edu.pugetsound.npastor.utils.ShapefileWriter;
 import edu.pugetsound.npastor.utils.Trip;
 
 public class DRTSimulation {
 	
 	public static final String TAG = "DRTSimulation";
 	
 	private static final String NUM_VEHICLES_FILE_LBL = "num_vehicles";
 	
 	private static final int ROUTE_UPDATE_INCREMENT = 10; // Update route progress at this percentage increment
 	
 	private static final String COMMA_DELIM = ",";
 	
 	// Rebus settings
 	private static final int REBUS_HINTS = Rebus.NEW_VEHICLE_ON_REJECTION;
 	
 	private ArrayList<Trip> mTrips;
 	private PriorityQueue<SimEvent> mEventQueue;
 	private Vehicle[] mVehiclePlans;
 	private Rebus mRebus;
 	private boolean mFromFile;
 	private RouteCache mCache;
 	private ArrayList<Trip> mRejectedTrips;
 	private int mTotalTrips;
 
 	public DRTSimulation(ArrayList<Trip> trips, boolean fromFile) {
 		mFromFile = fromFile;
 		mTrips = trips;
 		mTotalTrips = trips.size();
 		mEventQueue = new PriorityQueue<SimEvent>();
 		mVehiclePlans = new Vehicle[0];
 		mRejectedTrips = new ArrayList<Trip>();
 	}
 	
 	/**
 	 * Runs the DRT simulation, but parses the specified file to determine
 	 * how many vehicles to model
 	 */
 	public void runSimulation() {
 			
 		Log.iln(TAG, "Running simulation");
 		
 		if(mCache == null) {
 			throw new IllegalStateException("Cache has not been instantiated. Call buildCache() before runSimulation()");
 		}
 		mRebus = new Rebus(mCache, REBUS_HINTS);
 		mRebus.printEnabledHints();
 		
 		// If a file path is specified, parse out the number of vehicles to generate
 		// Otherwise, use the value defined in Constants
 		int vehicleQuantity = -1;
 		if(!mFromFile) {
 			vehicleQuantity = Constants.VEHICLE_QUANTITY;
 		} else {
 			File file = new File(TacomaDRTMain.getSourceTripVehDir());
 			Log.iln(TAG, "Loading number of vehicles from: " + file.getPath());
 	
 			try {
 				Scanner scanner = new Scanner(file);
 				while (scanner.hasNextLine()) {
 					String[] tokens = scanner.nextLine().split(" ");
 					if(tokens[0].equals(NUM_VEHICLES_FILE_LBL)) {
 						vehicleQuantity = Integer.valueOf(tokens[1]);
 						break;
 					}						
 				}
 				scanner.close();
 				if(vehicleQuantity == -1)
 					throw new IllegalArgumentException("Vehicle quantity not specified in file at " + file.getPath());				
 			} catch(FileNotFoundException ex) {
 				Log.e(TAG, "Unable to find trip file at: " + file.getPath());
 				ex.printStackTrace();
 				System.exit(1);
 			}
 		}
 	
 		generateVehicles(vehicleQuantity);
 		enqueueTripRequestEvents();
 		doAPrioriScheduling();
		
 		int lastTime = 0;
 		// Run simulation until event queue is empty
 		while(!mEventQueue.isEmpty()) {
 			SimEvent nextEvent = mEventQueue.poll();
 			int nextTime = nextEvent.getTimeMins();
 			switch(nextEvent.getType()) {
 				case SimEvent.EVENT_NEW_REQUEST:
 					consumeNewRequestEvent(nextEvent, (lastTime != nextTime ? true : false));
 					break;
 			}
 			lastTime = nextTime;
 		}
 		
 		onSimulationFinished();
 	}
 	
 	private void generateVehicles(int numVehicles) {
 		Log.iln(TAG, "Generating " + numVehicles + " vehicles");
 		mVehiclePlans = new Vehicle[numVehicles];
 		for(int i = 0; i < numVehicles; i++) {
			mVehiclePlans[i] = new Vehicle(i+1);
 		}
 	}
 	
 	/**
 	 * Enqueues all trip requests in the event queue
 	 */
 	private void enqueueTripRequestEvents() {
 		Log.iln(TAG, "Enqueueing all trip request events in simulation queue");
 		for(Trip t: mTrips) {
 			int requestTime = t.getCallInTime();
 			SimEvent requestEvent = new SimEvent(SimEvent.EVENT_NEW_REQUEST, t, requestTime);
 			mEventQueue.add(requestEvent);
 		}
 	}
 	
 	/**
 	 * Contains procedures to execute when a simulation has finished running
 	 */
 	private void onSimulationFinished() {
 		Log.iln(TAG, "*************************************");
 		Log.iln(TAG, "       SIMULATION COMPLETE");
 		Log.iln(TAG, "*************************************");
 		
 		mRebus.onRebusFinished();
 		
 		for(Vehicle v : mVehiclePlans) {
 			Log.iln(TAG, v.scheduleToString());
 		}
 		
 		// Print total rejected trips and rate
 		float rejectionRate = (float) mRejectedTrips.size() / mTotalTrips * 100;
 		Log.iln(TAG, "Total trips simulated: " + mTotalTrips + ". Total trips rejected by REBUS: " + mRejectedTrips.size() +
 				". Rejection rate: " + rejectionRate + "%");
 		
 		// Write simulation files
		appendTripVehicleTxtFile();
 		writeScheduleTxtFile();
 		writeScheduleShpFile();
 		writeStatisticsTxtFile();
 		writeRebusSettingsFile();
 	}
 	
 	/**
 	 * Schedule all trips that are known a priori. That is, all trips which
 	 * are known to the agency before service hours begin. These are the static requests.
 	 */
 	private void doAPrioriScheduling() {
 		Log.iln(TAG, "Doing a priori scheduling (all static trip requests)");
 		boolean moreStaticRequests = true;
 		while(moreStaticRequests) {
 			SimEvent event = mEventQueue.peek();
 			if(event == null) moreStaticRequests = false;
 			else if(event.getType() == SimEvent.EVENT_NEW_REQUEST) { // Ensure this is correct event type
 				if(event.getTimeMins() < Constants.BEGIN_OPERATION_HOUR * 60) { // Ensure request time is before operation begins
 					consumeNewRequestEvent(event, false); // Enqueue trip in the scheduler, don't immediately schedule
 					mEventQueue.poll();
 				} else {
 					moreStaticRequests = false; // Break loop when we've reached operation hours
 				}
 			}
 		}
 		Log.iln(TAG, mRebus.getQueueSize() + " static jobs queued");
 		// Now that all static trips are enqueued we can schedule them.
 		RebusResults results = mRebus.scheduleQueuedJobs(mVehiclePlans);
 		mRejectedTrips.addAll(results.rejectedTrips);
 		mVehiclePlans = results.vehiclePlans;
 	}
 	
 	/**
 	 * Consumes a new trip request event
 	 * @param event Trip request event
 	 * @parm schedule Specifies if scheduling should be executed immediately.
 	 */
 	private void consumeNewRequestEvent(SimEvent event, boolean schedule) {
 		Trip t = event.getTrip();
 		// Enqueue the trip in the REBUS queue, and schedule if requested
 		mRebus.enqueueTripRequest(t);
 		if(schedule) {
 			RebusResults results = mRebus.scheduleQueuedJobs(mVehiclePlans);
 			mRejectedTrips.addAll(results.rejectedTrips);
 			mVehiclePlans = results.vehiclePlans;
 		}
 	}
 	
 	// ******************************
 	//         SIM FILE STUFF
 	// ******************************
 	
 	/**
 	 * Write vehicle schedules to a text file
 	 */
 	private void writeScheduleTxtFile() {
 
 		// Build text list
 		ArrayList<String> text = new ArrayList<String>();
 		
 		// Add all vehicle schedules
 		for(Vehicle v : mVehiclePlans) {
 			text.add(v.scheduleToString());
 		}	
 		
 		// Add all rejected trips
 		text.add("\r\n REJECTED TRIPS \r\n");
 		for(Trip t : mRejectedTrips) {
 			text.add(t.toString());
 		}
 
 		// Write file
 		DRTUtils.writeTxtFile(text, Constants.SCHED_TXT);
 	}
 	
 	/**
 	 * Append the vehicle quantity to the trip/vehicle file
 	 */
 	private void appendTripVehicleTxtFile() {
 		ArrayList<String> text = new ArrayList<String>(1);
 		text.add(NUM_VEHICLES_FILE_LBL + " " + mVehiclePlans.length);
 		DRTUtils.writeTxtFile(text, Constants.TRIPS_VEHICLES_TXT);
 		DRTUtils.writeTxtFile(text, Constants.TRIPS_READABLE_TXT);
 	}
 	
 	/**
 	 * Compiles statistics for this simulation and writes to file
 	 */
 	private void writeStatisticsTxtFile() {
 		ArrayList<String> text = new ArrayList<String>();
 
 		String headers = "Vehicle" + COMMA_DELIM
 						+ "trips serviced" + COMMA_DELIM
 						+ "max travel time dev (m)" + COMMA_DELIM
 						+ "avg travel time dev (m/j)" + COMMA_DELIM
 						+ "max pickup dev (m)" + COMMA_DELIM
 						+ "avg pickup dev (m/j)" + COMMA_DELIM
 						+ "avg veh wait time (m/j)" + COMMA_DELIM
 						+ "avg seats occupied w/ 1+ jobs" + COMMA_DELIM;
 		text.add(headers);
 		
 		int numTripsServiced = mTrips.size() - mRejectedTrips.size();
 				
 		// Global travel time deviation max and pickup deviation max
 		int globalMaxTrTimeDev = 0;
 		int globalMaxPickupDev = 0;
 		// Global travel time deviation and pickup deviation totals
 		double globalTravelTimeDev = 0;
 		double globalPickupDev = 0;
 		// Global wait time total
 		double globalAvgWaitTime = 0;
 		// Global capacity utilization at each stop
 		double globalCapUtil = 0;;
 		
 		for(Vehicle curVeh : mVehiclePlans) {
 			StatsWrapper result = calcVehicleStats(curVeh);
 
 			// Percent of total riders that the current vehicle services
 			double curVehiclePct = ((double) result.numTrips / numTripsServiced);
 			
 			// Build vehicle statistics string
 			String vehString = "" + curVeh.getIdentifier() + COMMA_DELIM
 							+ result.numTrips + COMMA_DELIM
 							+ result.maxTravelTimeDev + COMMA_DELIM
 							+ result.avgTravelTimeDev + COMMA_DELIM
 							+ result.maxPickupDev + COMMA_DELIM		
 							+ result.avgPickupDev + COMMA_DELIM
 							+ result.avgPickupWaitTime + COMMA_DELIM
 							+ result.avgCapUtil + COMMA_DELIM;
 			text.add(vehString);
 						
 			// Update global statistics
 			globalTravelTimeDev += result.avgTravelTimeDev * curVehiclePct;
 			globalPickupDev += result.avgPickupDev * curVehiclePct;
 			globalAvgWaitTime +=  result.avgPickupWaitTime * curVehiclePct;
 			globalCapUtil += result.avgCapUtil * curVehiclePct;
 			globalMaxTrTimeDev = (int) Math.max(globalMaxTrTimeDev, result.maxTravelTimeDev);
 			globalMaxPickupDev = (int) Math.max(globalMaxPickupDev, result.maxPickupDev);
 		}
 		
 		// Build global statistics string
 		String globalString = "Global" + COMMA_DELIM
 							+ numTripsServiced + COMMA_DELIM
 							+ globalMaxTrTimeDev + COMMA_DELIM
 							+ globalTravelTimeDev + COMMA_DELIM
 							+ globalMaxPickupDev + COMMA_DELIM
 							+ globalPickupDev + COMMA_DELIM
 							+ globalAvgWaitTime + COMMA_DELIM
 							+ globalCapUtil + COMMA_DELIM;
 		text.add(globalString);
 		
 		DRTUtils.writeTxtFile(text, Constants.STATS_CSV);
 	}
 	
 	/**
 	 * Calculates vehicle statistics for the specified vehicle
 	 * @param v The vehicle
 	 * @return A StatsWrapper containing all pertinent statistics for the 
 	 *         specified vehicle
 	 */
 	private StatsWrapper calcVehicleStats(Vehicle v) {
 		StatsWrapper result = new StatsWrapper();
 		ArrayList<VehicleScheduleJob> schedule = v.getSchedule();
 		result.numTrips = schedule.size() / 2 - 1; // Ignore start/end jobs
 		int pickupDevTotal = 0;  // Running pickup deviation total
 		int travelTimeDevTotal = 0; // Running excess travel time total
 		int pickupWaitTotal = 0; // Running pickup wait total (vehicle idle time)
 		int totalCapUtil = 0; // Sum of seats occupied at each stop when at least 1 is occupied
 		int capUtilStops = 0; // Number of stops where vehicle contains 1+ riders
 		
 		int numRiders = 0;
 		
 		for(int i = 0; i < schedule.size(); i++) {
 			VehicleScheduleJob job = schedule.get(i);
 			switch(job.getType()) {
 			case VehicleScheduleJob.JOB_TYPE_START:
 			case VehicleScheduleJob.JOB_TYPE_END:
 				break;
 			case VehicleScheduleJob.JOB_TYPE_PICKUP:
 				// Update riders
 				numRiders++;
 				capUtilStops++;
 				totalCapUtil += numRiders;
 				
 				// Pickup deviation time difference between service time and
 				// requested service time
 				int curPickupDev = job.getServiceTime() - job.getStartTime();
 				pickupDevTotal += curPickupDev;
 				result.maxPickupDev = Math.max(result.maxPickupDev, curPickupDev);
 				
 				// Add to wait total as long as this is not the first pickup
 				if(i != 1)
 					pickupWaitTotal += job.getWaitTime(-1);
 				break;
 			case VehicleScheduleJob.JOB_TYPE_DROPOFF:
 				// Update riders
 				numRiders--;
 				if(numRiders > 0) {
 					totalCapUtil += numRiders;
 					capUtilStops++;
 				}
 				
 				// Travel time deviation is the excess travel time for a trip
 				double curTrTimeDev = job.getServiceTime() - job.getStartTime();
 				travelTimeDevTotal += curTrTimeDev;
 				result.maxTravelTimeDev = Math.max(result.maxTravelTimeDev, curTrTimeDev);
 				break;
 			}
 		}
 		
 		result.avgPickupDev = (double) pickupDevTotal / result.numTrips;
 		result.avgTravelTimeDev = (double) travelTimeDevTotal / result.numTrips;
 		result.avgPickupWaitTime = (double) pickupWaitTotal / result.numTrips;
 		result.avgCapUtil = (double) totalCapUtil / capUtilStops;
 		
 		return result;		
 	}
 	
 	/**
 	 * Writes REBUS settings to file
 	 */
 	private void writeRebusSettingsFile() {
 		ArrayList<String> text = new ArrayList<String>();
 		
 		StringBuilder stringBuilder = new StringBuilder();
 		// Add headers related to job difficulty
 		stringBuilder.append("job: window_c1" + COMMA_DELIM).append("job: window_c2" + COMMA_DELIM).
 			append("job: tr_time_c1" + COMMA_DELIM).append("job: tr_time_c2" + COMMA_DELIM)
 			.append("job: max_travel_coeff" + COMMA_DELIM);
 		text.add(stringBuilder.toString());
 
 		// Add job difficulty parameters
 		stringBuilder = new StringBuilder();
 		stringBuilder.append(Rebus.WINDOW_C1 + COMMA_DELIM)
 			.append(Rebus.WINDOW_C2 + COMMA_DELIM).append(Rebus.TR_TIME_C1 + COMMA_DELIM)
 			.append(Rebus.TR_TIME_C2 + COMMA_DELIM).append(Rebus.MAX_TRAVEL_COEFF + COMMA_DELIM);
 		text.add(stringBuilder.toString());
 		
 		// Add headers related to job insertion quality
 		stringBuilder = new StringBuilder();
 		stringBuilder.append("insert: dr_time_c1" + COMMA_DELIM)
 			.append("insert: dr_time_c2" + COMMA_DELIM).append("insert: wait_c1" + COMMA_DELIM)
 			.append("insert: wait_c2" + COMMA_DELIM).append("insert: dev_c" + COMMA_DELIM)
 			.append("insert: capacity_c" + COMMA_DELIM).append("insert: vehicle_util_c");
 		text.add(stringBuilder.toString());
 		
 		// Add job insertion quality parameters
 		stringBuilder = new StringBuilder();
 		stringBuilder.append(Rebus.DR_TIME_C1 + COMMA_DELIM).append(Rebus.DR_TIME_C2 + COMMA_DELIM)
 			.append(Rebus.WAIT_C1 + COMMA_DELIM).append(Rebus.WAIT_C2 + COMMA_DELIM).append(Rebus.DEV_C + COMMA_DELIM)
 			.append(Rebus.CAPACITY_C + COMMA_DELIM).append(Rebus.VEHICLE_UTIL_C + COMMA_DELIM);
 		text.add(stringBuilder.toString());
 		
 		DRTUtils.writeTxtFile(text, Constants.REBUS_SETTINGS_CSV);
 	}
 	
 	// **************************************
 	//            SHAPEFILE STUFF
 	// **************************************
 	
 	/**
 	 * Write vehicle schedules to a shapefile
 	 */
 	private void writeScheduleShpFile() {
 		// Build feature type and feature collection
 		SimpleFeatureType featureType = buildFeatureType();
 		SimpleFeatureCollection collection = createShpFeatureCollection(featureType);
 		
 		// Format time and create filename
 		String dateFormatted = DRTUtils.formatMillis(TacomaDRTMain.tripGenStartTime);
 		String filename = TacomaDRTMain.getRouteShpSimDirectory() + Constants.ROUTE_PREFIX_SHP + dateFormatted + ".shp";
         File shpFile = new File(filename);
         
         ShapefileWriter.writeShapefile(featureType, collection, shpFile);
 	}
 	
 	/**
 	 * Creates a Line feature type for the the route shapefile
 	 * @return A line feature type
 	 */
 	private SimpleFeatureType buildFeatureType() {
 		// Build feature type
         SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
         builder.setName("TripRoutes");
         builder.setCRS(DefaultGeographicCRS.WGS84); // long/lat projection system
         builder.add("Route", LineString.class); // Geo data
         builder.add("Vehicle", String.class); // Vehicle identifier
         
         final SimpleFeatureType featureType = builder.buildFeatureType();
         return featureType;
 	}
 	
 	private SimpleFeatureCollection createShpFeatureCollection(SimpleFeatureType featureType) {
 	   // New collection with feature type
 		SimpleFeatureCollection collection = FeatureCollections.newCollection();
 		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
         SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
         
         // Loop through vehicles
         for(Vehicle v : mVehiclePlans) {
         	ArrayList<VehicleScheduleJob> schedule = v.getSchedule();
         	Coordinate[] coordinates = new Coordinate[schedule.size()-2];
         	// Add all pickup/dropoff points to the line
         	for(int i = 1; i < schedule.size()-1; i++) {
         		VehicleScheduleJob curJob = schedule.get(i);
         		Point2D loc = null;
         		if(curJob.getType() == VehicleScheduleJob.JOB_TYPE_PICKUP)
         			loc = curJob.getTrip().getFirstEndpoint();
         		else if(curJob.getType() == VehicleScheduleJob.JOB_TYPE_DROPOFF)
         			loc = curJob.getTrip().getSecondEndpoint();
 
         		coordinates[i-1] = new Coordinate(loc.getX(), loc.getY());	
         	}	        	
         	LineString line = geometryFactory.createLineString(coordinates);
         	
         	// Build the feature
             featureBuilder.add(line); // Geo data
             featureBuilder.add(String.valueOf(v.getIdentifier())); // Trip identifier
             SimpleFeature feature = featureBuilder.buildFeature(null);
             ((DefaultFeatureCollection)collection).add(feature);			
         }
         return collection;
 	}
 	
 	// **************************************
 	//             CACHE STUFF
 	// **************************************
 	
 	/**
 	 * Builds the route cache. If this simulation instance is a re-run, we can use
 	 * the previously generated cache. Otherwise, we compute every route...
 	 */
 	public void buildCache() {
 		mCache = new RouteCache(mTrips.size());
 		// If we're re-running a simulation, we can re-use the previous routes
 		if(mFromFile) {
 			buildCacheFromFile();
 		} else {
 			doAllRoutefinding();
 		}
 		writeCacheToFile();
 	}
 	
 	/**
 	 * Delegates routefinding to worker threads
 	 */
 	private void doAllRoutefinding() {
 		int numThreads = TacomaDRTMain.numThreads;
 		
 		long routeStartTime = System.currentTimeMillis();
 		Log.iln(TAG, "Building route cache with " + numThreads + " threads. This may take a while...");
 		CountDownLatch latch = new CountDownLatch(numThreads); // To inform of thread completion
 		AtomicInteger progress = new AtomicInteger(); // For tracking caching progress
 		int totalRoutes = (int) Math.pow(mTrips.size()*2, 2);
 		
 		// Number of trips each thread will be calculating routes from
 		int threadTaskSize = mTrips.size() / numThreads;
 		
 		for(int i = 0; i < numThreads; i++) {
 			int startIndex = threadTaskSize * i;
 			int endIndex = (i+1 == numThreads) ? mTrips.size() : startIndex + threadTaskSize;
 			RoutefinderTask routeTask = new RoutefinderTask(mCache, mTrips, startIndex, endIndex, latch, progress);
 			new Thread(routeTask).start();
 		}
 		
 		// Alternate waiting and updating progress. You should bring a book.
 		Log.i(TAG, "Routing at 0%", false, true);
 		int lastPercent = -1;
 		try {
 			boolean tasksComplete = false;
 			while(!tasksComplete) {
 				int percent = (int)(((double)progress.get() / totalRoutes) * 100);
 				if(lastPercent + ROUTE_UPDATE_INCREMENT <= percent) {
 					Log.i(TAG, ", " + percent + "%", true,
 							(percent % 25 == 0 && percent / 25 != lastPercent / 25) ? true : false);
 					lastPercent = percent;
 				}
 				tasksComplete = latch.await(5, TimeUnit.SECONDS);
 			}
 		} catch (InterruptedException e) {
 			Log.e(TAG, e.getMessage());
 			e.printStackTrace();
 		}
 		long routeEndTime = System.currentTimeMillis();
 		TacomaDRTMain.printTime("All routes calculated and cached in ", routeEndTime, routeStartTime);
 	}
 	
 	/**
 	 * Moves a source cache into memory
 	 */
 	private void buildCacheFromFile() {
 		File file = new File(TacomaDRTMain.getSourceCacheDir());
 		Log.iln(TAG, "Loading cache from file at " + file.getPath());
 		
 		Scanner scanner;
 		try {
 			scanner = new Scanner(file);
 			int size = mTrips.size()*2;
 			for(int i = 0; i < size; i++) {
 				String[] tokens = scanner.nextLine().split(COMMA_DELIM);
 				for(int j = 0; j < size; j++) {
 					mCache.putDirect(i, j, Byte.valueOf(tokens[j]));
 				}				
 			}
 			scanner.close();	
 		} catch(FileNotFoundException ex) {
 			Log.e(TAG, "Unable to find trip file at: " + file.getPath());
 			ex.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Writes the cache to file. Re-runs of this simulation can read 
 	 * the cache file to avoid recomputing travel times
 	 */
 	private void writeCacheToFile() {
 		
 		// Get filename
 		String path = TacomaDRTMain.getSimulationDirectory() + Constants.ROUTE_CACHE_CSV;
 		Log.iln(TAG, "Writing cache file to: " + path);
 		
 		// Build a table
 		try {
 			FileWriter writer = new FileWriter(path, true);
 			PrintWriter lineWriter = new PrintWriter(writer);
 			
 			int size = mTrips.size() * 2;
 			for(int i = 0; i < size; i++) {
 				StringBuilder str = new StringBuilder();
 				for(int j = 0; j < size; j++) {
 					str.append(mCache.getDirect(i, j) + COMMA_DELIM);
 				}
 				// Write distances to file
 				lineWriter.println(str.toString());	
 			}
 
 			lineWriter.close();
 			writer.close();
 			
 			// This cache is valuable! Set read only
 			new File(path).setReadOnly();
 			Log.iln(TAG, "  File succesfully writen at:" + path);
 		} catch (IOException ex) {
 			Log.e(TAG, "Unable to write to file");
 			ex.printStackTrace();
 		}		
 	}
 	
 	private class StatsWrapper {
 		private double maxPickupDev;
 		private double avgPickupDev;
 		private double maxTravelTimeDev;
 		private double avgTravelTimeDev;
 		private double avgPickupWaitTime;
 		private double avgCapUtil; // Average capacity utilization at each stop
 		private int numTrips;
 
 		public StatsWrapper() {
 			numTrips = 0;
 			maxPickupDev = -1;
 			avgPickupDev = -1;
 			maxTravelTimeDev = -1;
 			avgTravelTimeDev = -1;
 			avgPickupWaitTime = 0;
 			avgCapUtil = 0;
 		}
 	}
 }
