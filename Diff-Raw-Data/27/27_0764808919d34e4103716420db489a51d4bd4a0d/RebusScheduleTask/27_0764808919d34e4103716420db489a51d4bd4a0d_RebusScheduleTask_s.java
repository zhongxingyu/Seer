 package edu.pugetsound.npastor.routing;
 
 import java.util.ArrayList;
 import java.util.concurrent.CountDownLatch;
 
 import edu.pugetsound.npastor.utils.Constants;
 import edu.pugetsound.npastor.utils.Log;
 import edu.pugetsound.npastor.utils.Trip;
 
 /**
  * This Runnable class checks the feasibility of scheduling a trip in a vehicle schedule, using the 
  * REBUS algorithm. Because REBUS requires that every trip be evaluated in every vehicle, we can 
  * parallelize the process by delegating work related to different vehicles to different worker 
  * threads. This class represents one of those threads.
  * 
  * @author Nathan Pastor
  */
 public class RebusScheduleTask implements Runnable {
 	
 	private static final String TAG = "RebusScheduleTask";
 	
 	private int mVehiclePlanIndex;
 	private VehicleScheduleJob mPickupJob;
 	private VehicleScheduleJob mDropoffJob;
 	private ArrayList<VehicleScheduleJob> mSchedule;
 	private ScheduleResult[] mResults;
 	private CountDownLatch mLatch;
 	private RouteCache mCache;
 	
 	public RebusScheduleTask (int vehicleIndex, ArrayList<VehicleScheduleJob> schedule, RouteCache cache,
 			VehicleScheduleJob pickupJob, VehicleScheduleJob dropoffJob, ScheduleResult[] results, CountDownLatch latch) {
 		mVehiclePlanIndex = vehicleIndex;
 		mPickupJob = pickupJob;
 		mDropoffJob = dropoffJob;
 		mSchedule = schedule;
 		mResults = results;
 		mLatch = latch;		
 		mCache = cache;
 	}
 
 	/**
 	 * Execute this thread
 	 */
 	public void run() {
 		ScheduleResult result = evaluateTripInVehicle();
 		mResults[mVehiclePlanIndex] = result;
 		// This task's work is done: decrement the counter
 		mLatch.countDown();
 	}
 	
 	/**
 	 * Evaluates this instance's trip (mPickupJob and mDropoffJob) in the given schedule. This will NOT schedule the jobs.
 	 * @return A ScheduleResult object containing the results of the evaluation. If multiple feasible solution were found, this will
 	 *         contain the solution that disturbs the objective function the least (minimizes mJobCost). If no feasible solution was
 	 *         found, mSolutionFound will be set to false
 	 */
 	private ScheduleResult evaluateTripInVehicle() {
 		
 		// Initialize the result
 		ScheduleResult schedResult = new ScheduleResult(mVehiclePlanIndex);
 		
 		int pickupIndex = 1; //s1 in Madsen's notation
 		int dropoffIndex = 2; //s2 in Madsen's notation
 
 		// FOLLOWING COMMENTS (except those in parenthesis) ARE MADSEN'S REBUS PSEUDO-CODE
 		// Step 1: Place s1, s2 just after this first stop T0 in the mSchedule, and update the mSchedule
 		mSchedule.add(pickupIndex, mPickupJob);
 		mSchedule.add(dropoffIndex, mDropoffJob);
 		
 		// Step 2: While all insertions have not been evaluated, do
 		boolean isFirstEval = true;
 		outerloop:
 		while(pickupIndex < mSchedule.size() - 2) {
 			if(isFirstEval) {
 				isFirstEval = false;
 			//  a) if s2 is before the last stop T1 in the mSchedule...
 			} else if(mSchedule.get(dropoffIndex+1).getType() == VehicleScheduleJob.JOB_TYPE_END) {
 //				Log.info(TAG, " --- FIRST");
 				// then move s1 one step to the right...
 				mSchedule.remove(dropoffIndex); // Remove so we don't swap pickup/dropoff order
 				mSchedule.set(pickupIndex, mSchedule.get(pickupIndex+1)); // (swap elements, save time!)
 				pickupIndex++;
 				mSchedule.set(pickupIndex, mPickupJob);
 				// and place s2 just after s1 and update the mSchedule. Go to 2(b).
 				dropoffIndex = pickupIndex + 1;
 				mSchedule.add(dropoffIndex, mDropoffJob);
 			//     else, move s2 one step to the right
 			} else {
 //				Log.info(TAG, " --- SECOND");
 				mSchedule.set(dropoffIndex, mSchedule.get(dropoffIndex+1)); // (swap elements)
 				dropoffIndex++;
 				mSchedule.set(dropoffIndex, mDropoffJob);
 			}
 			
 			// b) Check for feasibility.
 			boolean potentiallyFeasible = true;			
 			while(potentiallyFeasible) {
 				// (ensure this is a valid schedule ordering. Trip related jobs cannot be last in schedule)
 				if(dropoffIndex == mSchedule.size() - 1 || pickupIndex == mSchedule.size() - 1) {
 //					Log.info(TAG, "-------BREAKING ON INDEX TOO HIGH");
 					break;
 				}
 				FeasibilityResult feasResult = checkScheduleFeasibility(mSchedule, mVehiclePlanIndex);
 				int feasCode = feasResult.resultCode;
 				VehicleScheduleJob failsOn = feasResult.failsOn; // The job the test failed on
 				
 				//  i. if the insertion is feasible...
 				if(feasCode == FeasibilityResult.SUCCESS) {
 					// then calculate the change in the objective and compare to the previously found insertions
 					double objectiveFunc = calculateObjFunc(mSchedule);
 //					Log.info(TAG, "success, objective func is " + objectiveFunc);
 					if(objectiveFunc < schedResult.mOptimalScore || schedResult.mSolutionFound == false) {
 						schedResult.mOptimalPickupIndex = pickupIndex;
 						schedResult.mOptimalDropoffIndex = dropoffIndex;
 						schedResult.mOptimalScore = objectiveFunc;
 						schedResult.mSolutionFound = true;
 					}
 //					Log.info(TAG, "-------BREAKING ON SUCCESS");
 					potentiallyFeasible = false; // (No longer potentially feasible, schedule is certainly feasible)
 				// ii. If the insertion is not feasible, check for the following situations:
 				} else {
 					// A. if the capacity constraints, the maximum travel time or the time window
 					//    related to s2 have been violated...
 					if(feasCode == FeasibilityResult.FAIL_MAX_TRAVEL_TIME && 
 							failsOn.getTrip().getIdentifier() == mPickupJob.getTrip().getIdentifier()) {
 //						Log.info(TAG, " --- THIRD");
 						// then move s1 one step to the right...
 						mSchedule.remove(dropoffIndex); // Remove so we don't swap pickup/dropoff order
 						mSchedule.set(pickupIndex, mSchedule.get(pickupIndex+1)); // (swap elements, save time!)
 						pickupIndex++;
 						mSchedule.set(pickupIndex, mPickupJob);
 						// and place s2 just after s1 and update the schedule. Go to 2(b).
 						dropoffIndex = pickupIndex + 1;
 						mSchedule.add(dropoffIndex, mDropoffJob);
 					// B. if the time window related to s1 is violated then stop
 					} else if(feasCode == FeasibilityResult.FAIL_WINDOW && 
 							failsOn.getTrip().getIdentifier() == mPickupJob.getTrip().getIdentifier()) {
 //						Log.info(TAG, "-------BREAKING ON WINDOW");
 						break outerloop;
 					// C. else, go to 2
 					} else {
 //						Log.info(TAG, "-------BREAKING ON ELSE. result code " + feasResult.mResultCode + " fails on " + 
 //								(feasResult.mFailsOn.getTrip() != null ? feasResult.mFailsOn.getTrip().getIdentifier() : feasResult.mFailsOn.getType()));
 						potentiallyFeasible = false;
 					}
 				}
 			}
 		}
 		return schedResult;
 	}
 	
 	/**
 	 * Checks the feasibility of the given schedule. A schedule will FAIL the feasibility test if a time window at
 	 * any stop is not satisfied, if the maximum travel time for any trip is exceeded, or if the vehicle capacity
 	 * is exceeded at any point along its route. Otherwise, it will succeed.
 	 * @param schedule The schedule for which to check feasibility
 	 * @return A FeasibilityResult object containing the result code (mResultCode). In the case of a failure, this
 	 *         result also includes the job (mFailsOn) that the result code applies to. 
 	 */
 	private FeasibilityResult checkScheduleFeasibility(ArrayList<VehicleScheduleJob> schedule, int vehicleNum) {
 		
 		int numPassengers = 0;
 		FeasibilityResult result = new FeasibilityResult();
 		
 		// If soft constraints are enabled, all schedules pass the feasibility check
 		if(Rebus.isSettingEnabled(Rebus.SOFT_CONSTRAINTS)) {
 			result.resultCode = FeasibilityResult.SUCCESS;
 			return result;
 		}
 		
 		Rebus.updateServiceTimes(schedule, mCache, vehicleNum);
 //		String str = "Checking schedule feasibility: \n";
 //		for(int i = 0; i < schedule.size(); i++) {
 //			VehicleScheduleJob job = schedule.get(i);
 //			str += job.toString();
 //			if(i != schedule.size()-1) str += "\n";
 //		}
 //		Log.d(TAG, str);
 		
 
 		for(int i = 0; i < schedule.size(); i++) {
 			VehicleScheduleJob curJob = schedule.get(i);
 			int type = curJob.getType();
 			// For pickup jobs we can test the vehicle capacity and pickup window constraints
 			if(type == VehicleScheduleJob.JOB_TYPE_PICKUP) {
 				numPassengers++;
 				// Check if vehicle capacity has been exceeded
 				if(numPassengers > Vehicle.VEHICLE_CAPACITY) {
 					result.failsOn = curJob;
 					result.resultCode = FeasibilityResult.FAIL_CAPACITY;
 					break;
 				}
 				// Check if pickup window is satisfied
 				// If current time exceeds the max pickup window, fail the feasibility test
 				if(curJob.getWorkingServiceTime(mVehiclePlanIndex) > curJob.getStartTime() + Constants.PICKUP_SERVICE_WINDOW) {
 					result.failsOn = curJob;
 					result.resultCode = FeasibilityResult.FAIL_WINDOW;
 					break;
 				}
 			// For dropoff jobs we can test the maximal travel time constraint
 			} else if(type == VehicleScheduleJob.JOB_TYPE_DROPOFF) {
 				numPassengers--;
 
 				// Get the travel time between last location and here
 				int totalTripTravelTime = curJob.getWorkingServiceTime(mVehiclePlanIndex) - findCorrespondingJob(curJob, schedule).getWorkingServiceTime(mVehiclePlanIndex);
 				
 				// If the total trip travel time exceeds the max allowable trip travel time,
 				// fail the feasibility test.
 				if(totalTripTravelTime > maxTravelTime(curJob.getTrip())) {
 					result.failsOn = curJob;
 					result.resultCode = FeasibilityResult.FAIL_MAX_TRAVEL_TIME;
 					break;
 				}
 			}
 		}
 		Log.d(TAG, "Returning feasibility code: " + result.resultCode + "\n");
 		return result;
 	}
 	
 	/**
 	 * Calculates the objective function for the specified schedule.
 	 * Lower scores are more desirable
 	 * @param schedule The schedule to consider
 	 * @return The objective function score of the specified schedule
 	 */
 	private double calculateObjFunc(ArrayList<VehicleScheduleJob> schedule) {
 		double objectiveFunction = 0;
 		int passengers = 0;
 		
 		for(int i = 0; i < schedule.size(); i++) {
 			VehicleScheduleJob curJob = schedule.get(i);
			// Update passenger count
			if(curJob.getType() == VehicleScheduleJob.JOB_TYPE_PICKUP)
 				passengers++;
			else if(curJob.getType() == VehicleScheduleJob.JOB_TYPE_DROPOFF)
 				passengers--;	
			// Add load cost of current job running total
			objectiveFunction += getLoad(curJob, schedule, passengers);
 		}
 		return objectiveFunction;
 	}
 	
 	/**
 	 * Calculates the maximum allowable travel time for this trip
 	 * @param t The trip
 	 * @return The max allowable travel time for this trip in minutes
 	 */
 	private float maxTravelTime(Trip t) {
 		int timeMins = (int) t.getRoute().getTime() / 60;
 		return timeMins * Rebus.MAX_TRAVEL_COEFF;
 	}
 	
 	/**
 	 * Finds the corresponding job. Pickup and dropoff jobs for the same trip are considered corresponding.
 	 * @param job Job to find corresponding pair for
 	 * @return The corresponding job
 	 */
 	private VehicleScheduleJob findCorrespondingJob(VehicleScheduleJob job, ArrayList<VehicleScheduleJob> jobs) {
 		//For now, assume this is a pickup/dropoff
 		int keyId = job.getTrip().getIdentifier();
 		VehicleScheduleJob correspondingJob = null;
 		for(VehicleScheduleJob j : jobs) {
 			Trip t = j.getTrip();
 			if(t != null) {
 				if(t.getIdentifier() == keyId && j.getType() != job.getType()) {
 					correspondingJob = j;
 					break;
 				}
 			}
 		}
 		if(correspondingJob == null) 
 			Log.e(TAG, "No corresponding job found for job type " +
 					job.getType() + ", trip " + job.getTrip().getIdentifier());
 		return correspondingJob;
 	}
 	
 	// ********************************************
 	//               LOAD FUNCTIONS
 	//  Load functions estimate the desirability of 
 	//  inserting a new job into an existing plan.
 	// ********************************************
 	
 	/**
 	 * Calculates the load of the job at its current location in schedule
 	 * @param j The job to evaluate
 	 * @param schedule The schedule
 	 * @param passengers Number of passengers in the vehicle
 	 * @return The load cost for this stop (the specified job)
 	 */
 	public double getLoad(VehicleScheduleJob job, ArrayList<VehicleScheduleJob> schedule, int passengers) {
 		double cost = loadDrivingTime(job, schedule) +
 				loadWaitingTime(job) +
 				loadDesiredServiceTimeDeviation(job) +
 				loadCapacityUtilization(passengers);
 		if(Rebus.isSettingEnabled(Rebus.FAVOR_BUSY_VEHICLES))
 			cost += loadVehicleUtilization(schedule.size());
 		return cost;
 	}
 	
 	/**
 	 * Calculates the driving time component of the load value
 	 * Madsen notation: Cvariable * Tdr_time + Cconstant * (Twait + Chandle)
 	 * @param job Job to evaluate
 	 * @return The driving time laod cost for this stop (the specified job)
 	 */
 	private double loadDrivingTime(VehicleScheduleJob job, ArrayList<VehicleScheduleJob> schedule) {
 		Trip t = job.getTrip();
 		int minDrivingTime = (int)t.getRoute().getTime();
 		int waitingTime;
 		if(job.getType() == VehicleScheduleJob.JOB_TYPE_PICKUP)
 			waitingTime = job.getWorkingServiceTime(mVehiclePlanIndex) - job.getStartTime();
 		else {
 			VehicleScheduleJob corJob = findCorrespondingJob(job, schedule);
 			waitingTime = corJob.getWorkingServiceTime(mVehiclePlanIndex) - corJob.getStartTime();
 		}
 		
 		// TODO: WHAT IS HANDLING TIME???? (0.0)
 		double cost = Rebus.DR_TIME_C1 * minDrivingTime + Rebus.DR_TIME_C2 * (waitingTime + 0.0);
 		
 		return cost;
 	}
 	
 	/**
 	 * Calculates the waiting time component of the load value
 	 * Madsen notation: C2wait * Twait^2 + C1wait * Twait
 	 * @param job Job to evaluate
 	 * @return The waiting time load cost for this stop (the specified job)
 	 */
 	private double loadWaitingTime(VehicleScheduleJob job) {
 		double cost = 0;
 		//TODO: should this only apply to pickup jobs?
 		if(job.getType() == VehicleScheduleJob.JOB_TYPE_PICKUP) {
 			int waitingTime = job.getWorkingServiceTime(mVehiclePlanIndex) - job.getStartTime();
 			cost = Rebus.WAIT_C2 * (waitingTime * waitingTime) + Rebus.WAIT_C1 * waitingTime;
 		}
 		return cost;
 	}
 	
 	/**
 	 * Calculates the service time deviation component of the load value
 	 * Madsen notation: Cdev * Tdev^2
 	 * @param job Job to evaluate
 	 * @return The deviation from desired service time load cost for this stop (the specified job)
 	 */
 	private double loadDesiredServiceTimeDeviation(VehicleScheduleJob job) {
 		//TODO: what is deviation?
 		int deviation = job.getWorkingServiceTime(mVehiclePlanIndex) - job.getStartTime();
 		double cost = Rebus.DEV_C * (deviation * deviation);
 		return cost;
 	}
 	
 	/**
 	 * Calculates the capacity utilization component of the load value
 	 * Madsen notation: Ci * Vfreei^2
 	 * @param job The job to evaulate
 	 * @param passengers Number of passengers in vehicle
 	 * @return The vehicle capacity utilization load cost for this stop (the specified job)
 	 */
 	private double loadCapacityUtilization(int passengers) {
 		// Number of seats free
 		int free = Vehicle.VEHICLE_CAPACITY - passengers;
 		
 		double cost = Rebus.CAPACITY_C * (free * free);
 		
 		return cost;
 	}
 	
 	/**
 	 * An addition the REBUS algorithm. Penalizes vehicles with less utilization across the day, encouraging
 	 * scheduling in more highly booked vehicles. 
 	 * @param numJobs Number of jobs in the vehicle's schedule
 	 * @return The vehicle utilization cost
 	 */
 	private double loadVehicleUtilization(int numJobs) {
 		return Math.pow(numJobs, -1) * Rebus.VEHICLE_UTIL_C;
 	}
 	
 	/**
 	 * Wrapper class which contains the result of checkFeasibility() function
 	 * @author Nathan P
 	 */
 	public class FeasibilityResult {
 		
 		public static final int FAIL_CAPACITY = 0; // Vehicle over capacity
 		public static final int FAIL_MAX_TRAVEL_TIME = 1; // Excessive trip time
 		public static final int FAIL_WINDOW = 2; // Pickup too late
 		public static final int FAIL_EARLY_SERVICE = 4; // Pickup too early
 		public static final int SUCCESS = 3;
 		
 		public VehicleScheduleJob failsOn;
 		public int resultCode;
 		
 		public FeasibilityResult() {
 			failsOn = null;
 			resultCode = SUCCESS;
 		}
 	}
 }
