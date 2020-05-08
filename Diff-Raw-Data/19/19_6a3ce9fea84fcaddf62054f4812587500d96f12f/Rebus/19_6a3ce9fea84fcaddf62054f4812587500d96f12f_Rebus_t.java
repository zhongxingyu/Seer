 package edu.pugetsound.npastor.routing;
 
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import edu.pugetsound.npastor.TacomaDRTMain;
 import edu.pugetsound.npastor.utils.Log;
 import edu.pugetsound.npastor.utils.Trip;
 
 /**
  * A parallelized implementation of the REBUS algorithm. This is a heuristic solution to the dial-a-ride problem,
  * developed by Madsen et al. (1995). It uses a two-step insertion technique, outlined below.
  * 
  *   A brief REBUS outline:
  *   1) Queue all jobs according to a difficulty cost
  *   2) Consume the job queue. For each job in each vehicle schedule, do:
  *      2a) Insert job into schedule in every legitimate permutation (e.g, no dropffs before pickups)
  *          For each permutation, do:
  *          2aa) Determine insertion feasibility by assessing if any constraints have been violated
  *          2bb) If no constraints have been violated, calculate this schedule's objective function
  *               Lower objective function scores are more desirable.
  *      2b) If one or more feasible insertions have been found, choose the insertion with the smallest
  *          objective function scores. Otherwise, reject the trip as unschedulable
  *     
  * @author Nathan Pastor
  */
 public class Rebus {
 	
 	public static final String TAG = "Rebus";
 
 	// *************************
 	//     Scheduling Hints
 	// *************************
 	/** Use soft constraints on job insertions. This effectively leaves out step 2aa of the algorithm outlined above */
 	public static final int SOFT_CONSTRAINTS = 0x1; 
 	/** Reschedule all previously scheduled future trips when a trip is rejected. 
 	 *  This can dramatically increase execution time */
 	public static final int RESCHEDULE_ALL_ON_REJECTION = 0x2;
 	/** Add a new vehicle when a trip is rejected */
 	public static final int NEW_VEHICLE_ON_REJECTION = 0x4;
 	/** Favor scheduling trips in more heavily booked vehicles */
 	public static final int FAVOR_BUSY_VEHICLES = 0x8;
 	
 		
 	// Job cost constants (job difficulty)
 	public static final float WINDOW_C1 = 1.0f;
 	public static final float WINDOW_C2 = 1.0f;
 	public static final float TR_TIME_C1 = 1.0f;
 	public static final float TR_TIME_C2 = 1.0f;
 	public static final float MAX_TRAVEL_COEFF = 2.0f; //Max travel time for a given trip is this coefficient
 													   //multiplied by direct travel time
 	
 	// Load constants (insertion feasibility)
 	public static final float DR_TIME_C1 = 1.0f; // Cvariable in Madsen's notation
 	public static final float DR_TIME_C2 = 0.0f; // Cconst in Madsen's notation
 	public static final float WAIT_C1 = 0.0f;
 	public static final float WAIT_C2 = 0.0f;
 	public static final float DEV_C = 1.5f;
 	public static final float CAPACITY_C = 0.0f; // Ci in Madsen's notation
 	public static final float VEHICLE_UTIL_C = 1500f;
 	
 	public static final float HANDLE_TIME = 0.0f;
 	
 	private PriorityQueue<REBUSJob> mJobQueue;
 	private int mTotalJobsHandled;
 	private ExecutorService mScheduleExecutor;
 	private RouteCache mCache;
 	private static int mHints;
 	
 	public Rebus(RouteCache cache, int hints) {
 		mJobQueue = new PriorityQueue<REBUSJob>();
 		mTotalJobsHandled = 0;
 		mScheduleExecutor = Executors.newFixedThreadPool(TacomaDRTMain.numThreads);
 		mCache = cache;
 		mHints = hints;
 	}
 	
 	public int getQueueSize() {
 		return mJobQueue.size();
 	}	
 	
 	public void onRebusFinished() {
 		mScheduleExecutor.shutdown();
 	}
 	
 	/**
 	 * Add a new trip request to the job queue. This will NOT schedule
 	 * the job, scheduleQueuedJobs() must be called to do that.
 	 * @param newTrip The trip to be added to job queue
 	 */
 	public void enqueueTripRequest(Trip newTrip) {
 		REBUSJob newRequest = new REBUSJob(REBUSJob.JOB_NEW_REQUEST, newTrip, getCost(newTrip));
 		mJobQueue.add(newRequest);
 	}
 	
 	/**
 	 * Schedules all enqueued jobs, given the existing plan.
 	 * This will modify the existing plan to include new jobs
 	 * @param plan The existing route scheduling
 	 * @result A RebusResults wrapper, containing a list of all rejected trips that
 	 *         Rebus was not able to schedule, and a list of the vehicle plans
 	 */
 	public RebusResults scheduleQueuedJobs(Vehicle[] plan) {
 		Log.iln(TAG, "*************************************");
 		Log.iln(TAG, "       Scheduling " + mJobQueue.size() + " job(s)");
 		ArrayList<Trip> rejectedTrips = new ArrayList<Trip>();
 		while(!mJobQueue.isEmpty()) {
 			REBUSJob job = mJobQueue.poll();
 			mTotalJobsHandled++;
 			if(!scheduleJob(job, plan)) {
 				// Insertion has failed. If NEW_VEHICLE_ON_REJECTION is enabled,
 				// add a new vehicle to the plan and try again. Otherwise,
 				// add the trip to the list of failed jobs and continue to the next.
 				if(isSettingEnabled(Rebus.NEW_VEHICLE_ON_REJECTION)) {
 					Vehicle[] newPlan = new Vehicle[plan.length + 1];
 					
 					// Add all existing vehicles to new plan, and append new vehicle
 					for(int i = 0; i < plan.length; i++)
 						newPlan[i] = plan[i];
					newPlan[plan.length] = new Vehicle(plan.length);
 					plan = newPlan;
 					
 					Log.iln(TAG, "Adding new vehicle. Total now at: " + plan.length);
 					
 					// Put the rejected trip back into the event queue to try again
 					mJobQueue.add(job);
 				} else {
 					rejectedTrips.add(job.getTrip());
 					Log.iln(TAG, "   REJECTED. Trip " + job.getTrip().getIdentifier());
 				}
 			}
 		}
 		Log.iln(TAG, rejectedTrips.size() + " trip(s) rejected from scheduling.");
 		
 		// Wrap results and return
 		RebusResults result = new RebusResults(rejectedTrips, plan);		
 		return result;
 	}
 	
 	/**
 	 * Schedules the specified job
 	 * @param job The job to schedule
 	 * @param plan The existing vehicle plans
 	 * @result true if job was successfully placed in a schedule, false if otherwise
 	 */
 	private boolean scheduleJob(REBUSJob job, Vehicle[] plan) {
 		boolean scheduleSuccessful = false;
 		if(job.getType() != REBUSJob.JOB_NEW_REQUEST)
 			return true;
 	
 		Trip t = job.getTrip();
 		Log.iln(TAG, "On trip " + mTotalJobsHandled + ". Scheduling " + t.toString().replace("\n", "") +
 				   "\n                     Cost: " + job.getCost(), (mTotalJobsHandled % 50 == 0));
 		
 		// Split the trip into pickup and dropoff jobs
 		int durationMins = (int)t.getRoute().getTime() / 60;
 		VehicleScheduleJob pickupJob = new VehicleScheduleJob(t, t.getFirstEndpoint(),
 				t.getPickupTime(), durationMins, VehicleScheduleJob.JOB_TYPE_PICKUP, plan.length);
 		VehicleScheduleJob dropoffJob = new VehicleScheduleJob(t, t.getSecondEndpoint(),
 				t.getPickupTime() + durationMins, 0, VehicleScheduleJob.JOB_TYPE_DROPOFF, plan.length);
 		
 		// A list of thread results. Each thread will insert a result into this list at the index
 		// corresponding to the index of the vehicle it's evaluating
 		ScheduleResult[] results = new ScheduleResult[plan.length];
 		CountDownLatch latch = new CountDownLatch(plan.length);
 		
 		// Job must be evaluated in every vehicle. Prepare and execute threads
 		for(int i = 0; i < plan.length; i++) {
 			Vehicle v = plan[i];
 			
 			// Build new worker threads and copy schedules and jobs. We don't want to modify existing schedule
 			ArrayList<VehicleScheduleJob> existingSchedule = v.getSchedule();
 			ArrayList<VehicleScheduleJob> scheduleCopy  = new ArrayList<VehicleScheduleJob>();
 			for(int j = 0; j < existingSchedule.size(); j++) {
 				scheduleCopy.add(existingSchedule.get(j));
 			}
 			RebusScheduleTask task = new RebusScheduleTask(i, scheduleCopy, 
 					mCache, pickupJob, dropoffJob, results, latch);
 			mScheduleExecutor.execute(task);
 		}
 		
 		// Wait on the countdown latch for thread completion
 		try {
 			latch.await();
 		} catch (InterruptedException e) {
 			Log.e(TAG, e.getMessage());
 			e.printStackTrace();
 		}
 		
 		// Keep track of the most optimal insertion of the job
 		ScheduleResult optimalScheduling = null;
 		
 		for(ScheduleResult curResult : results) {
 			if(curResult.mSolutionFound) {
 				if(optimalScheduling == null || curResult.mOptimalScore < optimalScheduling.mOptimalScore)
 					optimalScheduling = curResult;
 			}
 		}
 		
 		if(optimalScheduling != null) {
 			// Do the scheduling if a feasible result has been found
 			Vehicle optimalVehicle = plan[optimalScheduling.mVehicleIndex];
 			ArrayList<VehicleScheduleJob> optimalSchedule = optimalVehicle.getSchedule();
 			optimalSchedule.add(optimalScheduling.mOptimalPickupIndex, pickupJob);
 			optimalSchedule.add(optimalScheduling.mOptimalDropoffIndex, dropoffJob);
 			updateServiceTimes(optimalSchedule, mCache, -1);
 			
 //				Log.info(TAG, "   SCHEDULED. Trip " + t.getIdentifier() + ". Vehicle: " + optimalVehicle.getIdentifier() + 
 //							". Pickup index: " + optimalScheduling.mOptimalPickupIndex + 
 //							". Dropoff index: " + optimalScheduling.mOptimalDropoffIndex);
 			
 //				String str = "New schedule is:\n";
 //				for(int i = 0; i < optimalSchedule.size(); i++) {
 //					VehicleScheduleJob printJob = optimalSchedule.get(i);
 //					str += printJob.toString();
 //					if(i != optimalSchedule.size()-1) str += "\n";
 //				}
 //				Log.d(TAG, str);
 			scheduleSuccessful = true;
 		}
 		return scheduleSuccessful;
 	}
 	
 	/**
 	 * Updates the service times of each job in this schedule
 	 * @param schedule The schedule to update times for
 	 * @param cache The route cache
 	 * @param vehicleNum the vehicle number if this should update a working schedule,
 	 *                   or -1 if this should update the finalized schedule 
 	 */
 	public static void updateServiceTimes(ArrayList<VehicleScheduleJob> schedule, RouteCache cache, int vehicleNum) {
 		// Initialize time to first pickup/dropoff job in the list
 		int curTime = schedule.get(1).getStartTime();
 		if(vehicleNum < 0)
 			schedule.get(1).setServiceTime(curTime);
 		else
 			schedule.get(1).setWorkingServiceTime(vehicleNum, curTime);
 		
 		// Start at index 2. We skip the start job and the first pickup job
 		for(int i = 2; i < schedule.size() - 1; i++) {
 			VehicleScheduleJob curJob = schedule.get(i);
 			int type = curJob.getType();
 			
 			// Update the current time by adding in transit time from last job to this job
 			VehicleScheduleJob lastJob = schedule.get(i-1);
 			if(lastJob.nextJobIs(vehicleNum, curJob)) {
 				curTime += lastJob.getTimeToNextJob(vehicleNum);
 			} else {
 				// If this distance was not known, check the cache.
 				boolean lastJobIsOrigin = (lastJob.getType() == VehicleScheduleJob.JOB_TYPE_PICKUP);
 				int lastJobId = lastJob.getTrip().getIdentifier();
 
 				byte lastLegMins = cache.getHash(lastJobId, lastJobIsOrigin, 
 						curJob.getTrip().getIdentifier(), type == VehicleScheduleJob.JOB_TYPE_PICKUP);
 				
 				// Update the current time
 				curTime += lastLegMins;
 				
 				// Update the previous job
 				lastJob.setNextJob(vehicleNum, curJob);
 				lastJob.setTimeToNextJob(vehicleNum, lastLegMins);
 			}
 			
 			// Deal with wait time. The service time might occur before the requested start time for a 
 			// pickup job. This indicates that the vehicle has to wait (idle) at this particular stop
 			// until the requested service time. Wait time is undesirable.
 			if(curTime < curJob.getStartTime()) {
 				curJob.setWaitTime(vehicleNum, curJob.getStartTime() - curTime);
 				
 				// Now fast-forward any vehicle idle time.
 				curTime = curJob.getStartTime();
 			} else {
 				curJob.setWaitTime(vehicleNum, 0);
 			}
 
 			// Finally, we can update the current job's service time.
 			if(vehicleNum == -1)
 				curJob.setServiceTime(curTime);
 			else 
 				curJob.setWorkingServiceTime(vehicleNum, curTime);
 		}
 	}
 	
 	/**
 	 * Checks if a setting is enabled. 
 	 * @param setting Setting to check, e.g USE_SOFT_CONSTRAINTS or RESCHEDULE_ALL_ON_REJECTION
 	 * @return True if specified setting is enabled, false otherwise
 	 */
 	public static boolean isSettingEnabled(int setting) {
 		return (mHints & setting) != 0;
 	}
 
 	// *************************************************
 	//                 COST FUNCTIONS
 	//  Cost functions assign a difficulty value to 
 	//  each new trip. We will therefore schedule the 
 	//  high priority (higher cost) trips first.
 	// *************************************************
 	
 	/**
 	 * Calculate the trip cost
 	 * @param t Trip for which to calculate cost
 	 * @return The trip cost
 	 */
 	public double getCost(Trip t) {
 		return costTimeWindow(t) + costMaxTravelTime(t);
 	}
 	
 	/**
 	 * Calculates the time window component of the trip's cost
 	 * @param t Trip
 	 * @return A double representing time window cost
 	 */
 	private double costTimeWindow(Trip t) {
 		//For now, all trip windows have equal size
 		return 0;
 		
 	}
 	
 	/**
 	 * Calculates the maximal travel time component of the trip's cost. Essentially, longer trips
 	 * will be weighed as more difficult
 	 * @param t The trip to calculate max travel cost for
 	 * @return A value representing maximal travel cost of trip
 	 */
 	private double costMaxTravelTime(Trip t) {
 		long minTime = t.getRoute().getTime();
 		// Calculate difference between max allowable travel time and min possible travel time
 		double deltaTransit = (minTime * MAX_TRAVEL_COEFF) - minTime;
 		// Maximal travel time cost function
 		double costFunction = TR_TIME_C2 * Math.pow(deltaTransit, -1) + TR_TIME_C1;
 		return costFunction;
 	}
 	
 	public void printEnabledHints() {
 		StringBuilder hintString = new StringBuilder();
 		// Check for all hints
 		if(isSettingEnabled(Rebus.FAVOR_BUSY_VEHICLES))
 			hintString.append("Favor Busy Schedules, ");
 		if(isSettingEnabled(Rebus.RESCHEDULE_ALL_ON_REJECTION))
 			hintString.append("Reschedule All on Rejection, ");
 		if(isSettingEnabled(Rebus.NEW_VEHICLE_ON_REJECTION))
 			hintString.append("New Vehicle on Rejection, ");
 		if(isSettingEnabled(Rebus.SOFT_CONSTRAINTS))
 			hintString.append("Soft Constraints, ");
 		
 		// If no hints set, add appropriate message
 		if(hintString.length() == 0)
 			hintString.append("None");
 		
 		Log.iln(TAG, "Enabled hints: " + hintString.toString());
 	}
 
 	/**
 	 * Represents a REBUS job. This is either a new request or 
 	 * a beginning/end point
 	 * @author Nathan P
 	 */
 	private class REBUSJob implements Comparable<REBUSJob> {
 		public static final int JOB_NEW_REQUEST = 0;
 		
 		private double mJobCost;
 		private int mType;
 		private Trip mTrip;
 		
 		public REBUSJob(int type, Trip trip, double jobCost) {
 			mTrip = trip;
 			mType = type;
 			mJobCost = jobCost;
 		}
 
 		
 		public double getCost() {
 			return mJobCost;
 		}
 		
 		public int getType() {
 			return mType;
 		}
 		
 		public Trip getTrip() {
 			return mTrip;
 		}
 
 		public int compareTo(REBUSJob REBUSJob) {
 			int compareVal;
 			double result = mJobCost - REBUSJob.getCost();
 			
 			// We could run in to round-down errors if we just return the cost difference
 			// e.g 0.4d -> 0 if returned as an int
 			if (result < 0) compareVal = 1;
 			else if(result > 0) compareVal = -1;
 			else compareVal = 0;
 			
 			return compareVal;
 		}
 	}
 	
 	public class RebusResults {
 		
 		public final ArrayList<Trip> rejectedTrips;
 		public final Vehicle[] vehiclePlans;
 		
 		private RebusResults(ArrayList<Trip> rejectedTrips, Vehicle[] vehiclePlans) {
 			this.rejectedTrips = rejectedTrips;
 			this.vehiclePlans = vehiclePlans;
 		}
 	}
 }
