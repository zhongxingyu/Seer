 package com.fixedd.AndroidTrimet.schemas.Arrivals;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 
 /** 
  * <p>
  * This class holds information on a returned result set. This is basically 
  * the wrapper for all of the other information returned by an Arrivals or Nearby
  * API call. 
  */
 public class ResultSet implements Parcelable, Serializable {
 	private static final long	serialVersionUID	= 1L;
 	
 	protected String                mErrorMessage;
 	protected List<LocationType   > mLocation;
 	protected List<ArrivalType    > mArrival;
 	protected List<RouteStatusType> mRouteStatus;
 	protected Long                  mQueryTime = -9223372036854775808l;
 
 	public ResultSet() {}
 
 	/**
 	 * Gets all of the Arrivals which belong to a certain Location.
 	 * @param locationId The location id you want arrivals for. This is usually a stop id.
 	 * @return all of the arrivals for a location.
 	 */
 	public List<ArrivalType> getArrivalsForLocation(int locationId) {
 		ArrayList<ArrivalType> toReturn = new ArrayList<ArrivalType>();
 
 		int len = mArrival.size();
 		for (int i=0; i<len; i++) {
 			ArrivalType arr = mArrival.get(i);
 			if (arr.mLocid == locationId)
 				toReturn.add(arr);
 		}
 
 		return toReturn;
 	}
 
 	/**
 	 * Gets an error message.
 	 * @return A {@link String} containing an error message or null if there wasn't an error.
 	 *     
 	 */
 	public String getErrorMessage() {
 		return mErrorMessage;
 	}
 
 	/**
 	 * Sets the error message.
 	 * @param The {@link String} that contains the error message.
 	 *     
 	 */
 	public void setErrorMessage(String message) {
 		mErrorMessage = message;
 	}
 
 	/**
 	 * Gets the locations for the ResultSet.
 	 * 
 	 * <p>
 	 * This accessor method returns a reference to the live list, not a 
 	 * snapshot. Therefore any modification you make to the returned 
 	 * list will be present inside the locations list. This is why there 
 	 * is not a <CODE>set</CODE> method for the location property.
 	 * 
 	 * <p>
 	 * For example, to add a new item, do as follows:
 	 * <pre>
 	 *    getLocations().add(newItem);
 	 * </pre>
 	 * @return List<LocationType> All of the locations.
 	 */
 	public List<LocationType> getLocations() {
 		if (mLocation == null) {
 			mLocation = new ArrayList<LocationType>();
 		}
 		return mLocation;
 	}
 
 	/**
 	 * Gets the arrivals for the ResultSet.
 	 * 
 	 * <p>
 	 * This accessor method returns a reference to the live list, not a 
 	 * snapshot. Therefore any modification you make to the returned list 
 	 * will be present inside the arrival list. This is why there is not 
 	 * a <CODE>set</CODE> method for the arrival property.
 	 * 
 	 * <p>
 	 * For example, to add a new item, do as follows:
 	 * <pre>
 	 *    getArrivals().add(newItem);
 	 * </pre>
 	 * @return List<ArrivalType> All of the arrivals. 
 	 */
 	public List<ArrivalType> getArrivals() {
 		if (mArrival == null) {
 			mArrival = new ArrayList<ArrivalType>();
 		}
 		return mArrival;
 	}
 
 	/**
 	 * Gets the routeStatuses for the ResultSet.
 	 * 
 	 * <p>
 	 * This accessor method returns a reference to the live list, not a 
 	 * snapshot. Therefore any modification you make to the returned list 
 	 * will be present inside the RouteStatus list. This is why there is not 
 	 * a <CODE>set</CODE> method for the routeStatus property.
 	 * 
 	 * <p>
 	 * For example, to add a new item, do as follows:
 	 * <pre>
 	 *    getRouteStatuses().add(newItem);
 	 * </pre>
 	 * @return List<RouteStatusType> All of the route statuses.
 	 * 
 	 */
 	public List<RouteStatusType> getRouteStatuses() {
 		if (mRouteStatus == null) {
 			mRouteStatus = new ArrayList<RouteStatusType>();
 		}
 		return mRouteStatus;
 	}
 
 	/**
 	 * Gets the time that the query was made (according to Trimet's servers).
 	 * @return the time that the query was made in milliseconds since epoch or -9223372036854775808 if the time wasn't set correctly.
 	 */
 	public long getQueryTime() {
 		return mQueryTime;
 	}
 
 	/**
 	 * Sets the time the query was made.
 	 * @param time The milliseconds since epoch that the query was made. 
 	 */
 	public void setQueryTime(long time) {
 		mQueryTime = time;
 	}
 
 
 
 
 	// **********************************************
 	//  for implementing Parcelable
 	// **********************************************
 
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		if (mErrorMessage == null)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeString(mErrorMessage);
 		}
 
 		if (mLocation == null || mLocation.size() == 0)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeTypedList(mLocation);
 		}
 
 		if (mArrival == null || mArrival.size() == 0)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeTypedList(mArrival);
 		}
 
 		if (mRouteStatus == null || mRouteStatus.size() == 0)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeTypedList(mRouteStatus);
 		}
 
 		dest.writeLong(mQueryTime);
 	}
 
 	public static final Parcelable.Creator<ResultSet> CREATOR = new Parcelable.Creator<ResultSet>() {
 		public ResultSet createFromParcel(Parcel in) {
 			return new ResultSet(in);
 		}
 
 		public ResultSet[] newArray(int size) {
 			return new ResultSet[size];
 		}
 	};
 
 	private ResultSet(Parcel dest) {
 		mLocation    = new ArrayList<LocationType   >();
 		mArrival     = new ArrayList<ArrivalType    >();
 		mRouteStatus = new ArrayList<RouteStatusType>();
 		
 		if (dest.readInt() == 1) mErrorMessage = dest.readString();
		if (dest.readInt() == 1) dest.readTypedList(mLocation   ,LocationType   .CREATOR);
 		if (dest.readInt() == 1) dest.readTypedList(mArrival    , ArrivalType    .CREATOR);
 		if (dest.readInt() == 1) dest.readTypedList(mRouteStatus, RouteStatusType.CREATOR);
 		mQueryTime = dest.readLong();
 	}
 }
