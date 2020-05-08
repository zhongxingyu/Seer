 package com.delin.speedlogger.Results;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.delin.speedlogger.Math.Geometry;
 import com.delin.speedlogger.Math.Interpolator;
 import com.delin.speedlogger.Serialization.GPXSerializer;
 import android.location.Location;
 
 public class SessionResult {
 	class Point {
 		public long time = -1;
 		public float speed = -1.f;
 		public float distance = -1.f;
 	}
 	private long mStartTime = 0;
 	private long mTotalTime = 0;
 	private float mMaxSpeed = 0;
 	private float mTotalDistance = 0;
 	private boolean mValid = false;
 	private List<Location> mLocList = new ArrayList<Location>();
 	private String mFilename; // gpx file
 	
 	public SessionResult() {
 	}
 	
 	public SessionResult(List<Location> locList) {
 		SetLocations(locList);
 	}
 
 	public List<Location> getLocations() {
 		if(mLocList == null) LoadGPX();
 		return mLocList;
 	}
 	
 	public void SaveGPX() {
 		if(mLocList == null || mLocList.size() < 2) return;
 		GPXSerializer gpxLog = new GPXSerializer(mFilename, true);
 		gpxLog.SaveAllFixes(mLocList);
 		gpxLog.Stop();
 	}
 	
 	private void LoadGPX() {
 		GPXSerializer gpxLog = new GPXSerializer(mFilename, false);
 		mLocList = gpxLog.GetAllFixes();
 		// HINT: we can check gpx data here to prevent hacks
 	}
 	
 	public void DeleteGPX() {
 		GPXSerializer.DeleteGPX(mFilename);
 	}
 
     public List<Location> GetLocations() {
     	return mLocList;
     }
 
     public void SetLocations(List<Location> locs) { // do a full copy
     	mLocList.clear();
     	mLocList.addAll(locs);
     	Recalc();
     }
     
     public void SaveToGPX(final String filename) {
     	GPXSerializer gpxSaver = new GPXSerializer(filename, true);
     	for (Iterator<Location> it = mLocList.iterator(); it.hasNext(); ) {
     		gpxSaver.AddFix(it.next());
     	}
     	gpxSaver.Stop();
     }
 
 	/**
 	 * Returns time at which specified speed has been reached
 	 *
 	 * @param speed desired speed, in m/s
 	 * @return Time at which specified speed has been reached, in milliseconds
 	 * or -1 in case of error
 	 */
     public long GetTimeAtSpeed(final float speed) {
     	// look at the cache
     	// then look at loclist itself
 		for (int i=1; i<mLocList.size(); i++) {
 			if (speed < mLocList.get(i).getSpeed()) {
 				// we found a closest largest fix, do linear interpolation
 				return Interpolator.Lerp(mLocList.get(i-1).getSpeed(), mLocList.get(i-1).getTime(),
 						mLocList.get(i).getSpeed(), mLocList.get(i).getTime(), speed) - mStartTime;
 				//return mLocList.get(i).getTime() - mStartTime;
 			}
 		}
     	return -1;
     }
     
 	/**
 	 * Returns time at which specified speed has been reached after
 	 * starting from origin speed
 	 *
 	 * @param toSpeed origin speed, in m/s
 	 * @param fromSpeed desired speed, in m/s
 	 * @return Time at which specified speed has been reached, in milliseconds
 	 * or -1 in case of error
 	 */
     public long GetTimeAtSpeed(final float toSpeed, final float fromSpeed) {
     	long retval = -1;
     	long fromTime = 0;
     	// check input parameters
     	if (toSpeed<fromSpeed || toSpeed<0 ) return retval;
     	
     	fromTime = retval = GetTimeAtSpeed(fromSpeed); // get 'from' time
     	if (retval!=-1) {  // if it's ('from') ok - get 'to' time
     		retval = GetTimeAtSpeed(toSpeed);
     		if (retval!=-1) retval = retval - fromTime >0 ? retval - fromTime : -1; // if 'to' is ok - calc difference
         }
     	return retval;
     }
     
     public long GetTimeAtDistance(final float distance) {
     	// look at the cache
     	// then look at loclist itself
     	float dist= 0.f;
 		for (int i=1; i<mLocList.size(); i++) {
 			dist = Geometry.DistBetweenLocs(mLocList.get(0),mLocList.get(i),false);
 			if (distance < dist) {
 				// we found a closest largest fix
 				return mLocList.get(i).getTime() - mStartTime; // TODO: very rough, intepolate that
 			}
 		}
     	return -1;
     }
     
     public Point GetInfoAtDistance(final float distance) {
     	// look at the cache
     	// then look at loclist itself
     	Point point = new Point();
     	float dist= 0.f;
 		for (int i=1; i<mLocList.size(); i++) {
 			dist = Geometry.DistBetweenLocs(mLocList.get(0),mLocList.get(i),false);
 			if (distance < dist) {
 				// we found a closest largest fix
 				point.time = mLocList.get(i).getTime() - mStartTime; // TODO: very rough, intepolate that
 				point.speed = mLocList.get(i).getSpeed();
 				point.distance = distance;
 				return point;
 			}
 		}
     	return point;
     }
 
     /**
 	 * Indicates state of results
 	 * 
 	 * If state is invalid - results may be incorrect
 	 *
 	 * @return Valid or not
 	 */
     public boolean IsValid() {
     	return mValid;
     }
     
 	/**
 	 * Returns top speed
 	 *
 	 * @return Speed, in m/s
 	 */
     public float GetMaxSpeed() {
     	return mMaxSpeed;
     }
     
     /**
 	 * Returns distance on which top speed has been achieved
 	 *
 	 * @return Distance, in meters
 	 */
     public float GetTotalDistance() {
     	return mTotalDistance;
     }
     
 	/**
 	 * Returns time taken on achievement top speed
 	 *
 	 * @return Time, in milliseconds, since StartTime!
 	 */
     public long GetTotalTime() {
     	return mTotalTime;
     }
     
 	/**
 	 * Returns time at which session has been started
 	 *
 	 * @return Time at which session has been started, in milliseconds since 01.01.1970 UTC
 	 */
     public long GetStartTime() {
     	return mStartTime; 
     }
     
     public void SetStartTime(long startTime) {
 		mStartTime = startTime;
 		mFilename = Long.toString(mStartTime) + ".gpx";
 	}
 
 	public void SetMaxSpeed(float maxSpeed) {
 		mMaxSpeed = maxSpeed;
 	}
 
 	public void SetTotalDistance(float totalDistance) {
 		mTotalDistance = totalDistance;
 	}
 
 	public void SetTotalTime(long totalTime) {
 		mTotalTime = totalTime;
 	}
     
     private void Recalc() {
     	mValid = false;
 		if(mLocList == null || mLocList.size() < 2) return;
 		mValid = true;
 		Location origin = mLocList.get(0);
		Location dest = mLocList.get(0);
 		
 		mStartTime = mLocList.get(0).getTime();
 		for (int i=1; i<mLocList.size(); i++){
 			if (mMaxSpeed < mLocList.get(i).getSpeed()) {
				dest = mLocList.get(i);
				mMaxSpeed = dest.getSpeed();
 			}
		}
		mTotalTime = dest.getTime() - mStartTime;
 		mTotalDistance = Geometry.DistBetweenLocs(origin,dest,false); // seems incorrect
 		float[] results = new float[3];
 		Location.distanceBetween(origin.getLatitude(), origin.getLongitude(), dest.getLatitude(), dest.getLongitude(), results);
 		mTotalDistance = results[0];
     }
 
 }
