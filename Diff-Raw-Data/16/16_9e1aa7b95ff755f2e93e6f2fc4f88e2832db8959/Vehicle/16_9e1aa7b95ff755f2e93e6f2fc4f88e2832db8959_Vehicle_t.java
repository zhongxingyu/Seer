 package edu.pugetsound.npastor.routing;
 
 import java.util.ArrayList;
 
 import edu.pugetsound.npastor.utils.Constants;
 import edu.pugetsound.npastor.utils.TimeSegment;
 public class Vehicle {
 
 	//TODO: what is capacity?
	public static final int VEHICLE_CAPACITY = 20;
 	
 	private int mVehicleId;
 	private ArrayList<VehicleScheduleJob> mSchedule;
 	private TimeSegment[] mInServiceSegments;
 	
 	public Vehicle(int id, TimeSegment[] inServiceSegments) {
 		mVehicleId = id;
 		mInServiceSegments = inServiceSegments;
 		initSchedule();		
 	}
 	
 	public TimeSegment[] getServiceSegments() {
 		return mInServiceSegments;
 	}
 	
 	/**
 	 * Checks if the vehicle can service a job beginning at the
 	 * specified time
 	 * @param timeMins Time to check for service
 	 * @return True if vehicle can service a job starting at timeMins, 
 	 *         false otherwise
 	 */
 	public boolean isServiceableTime(int timeMins) {
 		for(TimeSegment seg : mInServiceSegments) {
 			if(timeMins >= seg.getStartMins() && timeMins <= seg.getEndMins())
 				return true;
 		}
 		// If we've gotten here, the time didn't satisfy any service segments
 		return false;
 	}
 	
 	/**
 	 * Returns true if this vehicle serves all day, false otherwise
 	 * @return
 	 */
 	public boolean servesAllDay() {
 		for(TimeSegment seg : mInServiceSegments) {
 			if(seg.getStartMins() == Constants.BEGIN_OPERATION_HOUR * 60 
 					&& seg.getEndMins() == Constants.END_OPERATION_HOUR * 60)
 				return true;
 		}
 		// If we've gotten here, no segment covers the whole day
 		return false;
 	}
 
 	
 	public ArrayList<VehicleScheduleJob> getSchedule() {
 		return mSchedule;
 	}
 	
 	public int getIdentifier() {
 		return mVehicleId;
 	}
 	
 	private void initSchedule() {
 		mSchedule = new ArrayList<VehicleScheduleJob>();
 		
 		// Find earliest start and latest
 		int minStartMins = -1;
 		int maxEndMins = -1;
 		for(TimeSegment seg : mInServiceSegments) {
 			if(minStartMins < 0 || seg.getStartMins() < minStartMins)
 				minStartMins = seg.getStartMins();
 			if(maxEndMins < 0 || seg.getEndMins() > maxEndMins)
 				maxEndMins = seg.getEndMins();
 		}	
 		
 		// Add start and end jobs
 		mSchedule.add(new VehicleScheduleJob(null, null, minStartMins, 0, VehicleScheduleJob.JOB_TYPE_START, 0));
 		mSchedule.add(new VehicleScheduleJob(null, null, maxEndMins, 0, VehicleScheduleJob.JOB_TYPE_END, 0));
 	}
 	
 	public String scheduleToString() {
 		String str = "Vehicle " + mVehicleId + " schedule:";
 		for(int i = 0; i < mSchedule.size(); i++) {
 			str += "\n " + mSchedule.get(i) ;
 		}
 		return str;
 	}
 }
