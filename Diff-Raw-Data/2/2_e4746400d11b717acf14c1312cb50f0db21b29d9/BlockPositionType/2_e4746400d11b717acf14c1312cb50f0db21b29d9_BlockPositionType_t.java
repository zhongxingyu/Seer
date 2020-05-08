 package com.fixedd.AndroidTrimet.schemas.Arrivals;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * <p>The last known position of the vehicle along its block. Includes path 
  * information from this position to the stop requested.
  */
 public class BlockPositionType implements Parcelable, Serializable {
 	private static final long	serialVersionUID	= 1L;
 
 	protected List<TripType>	mTrips;
 	protected List<LayoverType>	mLayovers;
 	protected long				mAt			= -9223372036854775808l;
 	protected double			mLat		= -2147483648d;
 	protected double			mLng		= -2147483648d;
 	protected int				mHeading	= -2147483648;
 	protected int				mFeet		= -2147483648;
 
 	public BlockPositionType() {}
 
 	/**
 	 * Gets the trips for the BlockPosition.
 	 * 
 	 * <p>
 	 * This accessor method returns a reference to the live list,
 	 * not a snapshot. Therefore any modification you make to the
 	 * returned list will be present inside the trips list.
 	 * This is why there is not a <CODE>set</CODE> method for the trip property.
 	 * 
 	 * <p>
 	 * For example, to add a new item, do as follows:
 	 * <pre>
 	 *    getTrips().add(newItem);
 	 * </pre>
 	 */
 	public List<TripType> getTrips() {
 		if (mTrips == null) {
 			mTrips = new ArrayList<TripType>();
 		}
 		return this.mTrips;
 	}
 
 	/**
 	 * Gets the layovers for the BlockPosition.
 	 * 
 	 * <p>
 	 * This accessor method returns a reference to the live list,
 	 * not a snapshot. Therefore any modification you make to the
 	 * returned list will be present inside the layovers list.
 	 * This is why there is not a <CODE>set</CODE> method for the layover property.
 	 * 
 	 * <p>
 	 * For example, to add a new item, do as follows:
 	 * <pre>
 	 *    getLayovers().add(newItem);
 	 * </pre>
 	 */
 	public List<LayoverType> getLayovers() {
 		if (mLayovers == null) {
 			mLayovers = new ArrayList<LayoverType>();
 		}
 		return this.mLayovers;
 	}
 
 	/**
 	 * Gets the time this position was reported in milliseconds since epoch.
 	 * @return time or <b>-9223372036854775808</b> if it was not set correctly.
 	 */
 	public long getAt() {
 		return mAt;
 	}
 
 	/**
 	 * Sets the time this position was reported in milliseconds since epoch.
 	 */
 	public void setAt(long value) {
 		mAt = value;
 	}
 
 	/**
 	 * Gets the latitude of the vehicle at the time the position was reported.
 	 * @return latitude or or <b>-2147483648</b> if it wasn't set properly.    
 	 */
 	public double getLatitude() {
 		return mLat;
 	}
 
 	/**
 	 * Sets the latitude of the vehicle at the time the position was reported.    
 	 */
 	public void setLatitude(double lat) {
 		mLat = lat;
 	}
 
 	/**
 	 * Gets the longitude of the vehicle at the time the position was reported.
 	 * @return longitude or <b>-2147483648</b> if it wasn't set properly.    
 	 */
 	public double getLongitude() {
 		return mLng;
 	}
 
 	/**
 	 * Sets the longitude of the vehicle at the time the position was reported.    
 	 */
 	public void setLongitude(double lng) {
 		mLng = lng;
 	}
 
 	/**
 	 * Gets the heading of the vehicle at the time of the position was 
 	 * reported.
 	 * @return heading or <b>-2147483648</b> if it wasn't set properly.    
 	 */
 	public Integer getHeading() {
 		return mHeading;
 	}
 
 	/**
 	 * Sets the heading of the vehicle at the time of the position was 
 	 * reported.    
 	 */
 	public void setHeading(int heading) {
 		mHeading = heading;
 	}
 
 	/**
 	 * Gets the number of feet the vehicle is away from the stop at the time 
 	 * the position was reported.
 	 * @return feet or <b>-2147483648</b> if it wasn't set properly.
 	 */
 	public int getFeet() {
 		return mFeet;
 	}
 
 	/**
 	 * Sets the number of feet the vehicle is away from the stop at the time 
 	 * the position was reported.
 	 */
 	public void setFeet(int feet) {
 		mFeet = feet;
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
 		if (mTrips == null || mTrips.size() == 0)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeTypedList(mTrips);
 		}
 
 		if (mLayovers == null || mLayovers.size() == 0)
 			dest.writeInt(0);
 		else {
			dest.writeInt(1);
 			dest.writeTypedList(mLayovers);
 		}
 
 		dest.writeLong  (mAt     );
 		dest.writeDouble(mLat    );
 		dest.writeDouble(mLng    );
 		dest.writeInt   (mHeading);
 		dest.writeInt   (mFeet   );
 	}
 
 	public static final Parcelable.Creator<BlockPositionType> CREATOR = new Parcelable.Creator<BlockPositionType>() {
 		public BlockPositionType createFromParcel(Parcel in) {
 			return new BlockPositionType(in);
 		}
 
 		public BlockPositionType[] newArray(int size) {
 			return new BlockPositionType[size];
 		}
 	};
 
 	private BlockPositionType(Parcel dest) {
 		mTrips    = new ArrayList<TripType   >();
 		mLayovers = new ArrayList<LayoverType>();
 		
 		if (dest.readInt() == 1) dest.readTypedList(mTrips   , TripType   .CREATOR);
 		if (dest.readInt() == 1) dest.readTypedList(mLayovers, LayoverType.CREATOR);
 		mAt      = dest.readLong  ();
 		mLat     = dest.readDouble();
 		mLng     = dest.readDouble();
 		mHeading = dest.readInt   ();
 		mFeet    = dest.readInt   ();
 	}
 }
