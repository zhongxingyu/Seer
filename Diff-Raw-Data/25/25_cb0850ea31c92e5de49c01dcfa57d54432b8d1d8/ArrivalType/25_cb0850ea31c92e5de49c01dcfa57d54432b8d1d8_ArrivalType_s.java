 package com.fixedd.AndroidTrimet.schemas.Arrivals;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * <p>Contains arrival details.
  */
 public class ArrivalType implements Parcelable, Serializable {
 	private static final long	serialVersionUID	= 1L;
 	
 	/**
 	 * Arrival time was estimated with vehicle position information.
 	 * @see #setStatus(int)
 	 * @see #getStatus()
 	 */
 	public static int STATUS_ESTIMATED = 0;
 	/**
 	 * Scheduled arrival time is available only. No real time information 
 	 * available for estimation. Bus's radio may be down or vehicle may not be 
 	 * in service. Arrivals are not estimated when further than an hour away.
 	 * @see #setStatus(int)
 	 * @see #getStatus()
 	 */
 	public static int STATUS_SCHEDULED = 1;
 	/**
 	 * Status of service is uncertain.
 	 * @see #setStatus(int)
 	 * @see #getStatus()
 	 */
 	public static int STATUS_DELAYED   = 2;
 	/**
 	 * scheduled arrival was canceled for the day.
 	 * @see #setStatus(int)
 	 * @see #getStatus()
 	 */
 	public static int STATUS_CANCELED  = 3;
 
 
 
 	protected List<BlockPositionType>	mBlockPositions;
 	protected int						mRoute				= -2147483648;
 	protected int						mLocid				= -2147483648;
 	protected int						mDir				= -2147483648;
 	protected String					mShortSign;
 	protected String					mFullSign;
 	protected long						mEstimated			= -9223372036854775808l;
 	protected long						mScheduled			= -9223372036854775808l;
 	protected int						mBlock				= -2147483648;
 	protected String					mPiece;
 	protected boolean					mDeparted			= false;
 	protected boolean					mDetour				= false;
 	protected int						mStatus;
 
 	public ArrivalType() {}
 
 	/**
 	 * Gets the value of the blockPosition property.
 	 * 
 	 * <p>
 	 * This accessor method returns a reference to the live list,
 	 * not a snapshot. Therefore any modification you make to the
 	 * returned list will be present inside the blockPosition list.
 	 * This is why there is not a <CODE>set</CODE> method for the 
 	 * blockPositions property.
 	 * 
 	 * <p>
 	 * For example, to add a new item, do as follows:
 	 * <pre>
 	 *    getBlockPositions().add(newItem);
 	 * </pre>
 	 *  
 	 * @return a List<BlockPostionType> 
 	 */
 	public List<BlockPositionType> getBlockPositions() {
 		if (mBlockPositions == null) {
 			mBlockPositions = new ArrayList<BlockPositionType>();
 		}
 		return mBlockPositions;
 	}
 
 	/**
 	 * Gets the route number of the arrival.
 	 * @return the route's number or -2147483648 if it wasn't set properly.
 	 */
 	public int getRouteNumber() {
 		return mRoute;
 	}
 
 	/**
 	 * Sets the route number of the arrival.
 	 * @param the route number of the arrival.
 	 */
 	public void setRouteNumber(int routeId) {
 		mRoute = routeId;
 	}
 
 	/**
 	 * Gets the location id of the arrival.
 	 * @return the location id of the arrival or -2147483648 if it wasn't set properly.
 	 */
 	public int getLocationId() {
 		return mLocid;
 	}
 
 	/**
 	 * Sets the location id.
 	 * @param locationId the location id for the arrival. 
 	 */
 	public void setLocationId(int locationId) {
 		mLocid = locationId;
 	}
 
 	/**
 	 * Gets the direction of the route for this arrival.
 	 * @return the direction of travel. Either <b>1</b> for inbound, <b>0</b> for outbound, or <b>-2147483648</b> if it wasn't set.
 	 */
 	public int getDirection() {
 		return mDir;
 	}
 
 	/**
 	 * Sets the direction of the route for this arrival.
 	 * @param direction Either <b>1</b> for inbound, <b>0</b> for outbound.
 	 */
 	public void setDirection(int direction) {
 		if (direction != 0 && direction != 1)
 			throw new RuntimeException("The direction must be either 0 or 1.");
 		mDir = direction;
 	}
 
 	/**
 	 * Gets the short version of text from the overhead sign of the vehicle 
 	 * when it arrives at the stop.
 	 * @return overhead sign as a {@link String}    
 	 */
 	public String getShortSign() {
 		return mShortSign;
 	}
 
 	/**
 	 * Sets the short version of text from the overhead sign of the vehicle 
 	 * when it arrives at the stop.
 	 * @param signText overhead sign as a {@link String}
 	 */
 	public void setShortSign(String signText) {
 		mShortSign = signText;
 	}
 
 	/**
 	 * Gets the full text of the overhead sign of the vehicle when it arrives 
 	 * at the stop.
 	 * @return overhead sign as a {@link String}    
 	 */
 	public String getFullSign() {
 		return mFullSign;
 	}
 
 	/**
 	 * Sets the full text of the overhead sign of the vehicle when it arrives 
 	 * at the stop.
 	 * @param signText overhead sign as a {@link String}    
 	 */
 	public void setFullSign(String signText) {
 		mFullSign = signText;
 	}
 
 	/**
 	 * Gets the estimated time for this arrival.
 	 * @return the milliseconds since epoch when the arrival is estimated as a 
 	 * {@link Long }. If this value is <b>-9223372036854775808</b> the arrival 
 	 * could not be estimated and the time from getSchedule() should be used 
 	 * instead.
 	 */
 	public Long getEstimatedTime() {
 		return mEstimated;
 	}
 
 	/**
 	 * Sets the estimated time for this arrival.
 	 * @param time The milliseconds since epoch when the arrival is estimated 
 	 * as a {@link Long}. 
 	 */
 	public void setEstimatedTime(Long time) {
 		mEstimated = time;
 	}
 
 	/**
 	 * Gets the scheduled time for this arrival.
 	 * @return the milliseconds since epoch when the arrival is scheduled as a 
 	 * {@link Long }. If this value is <b>-9223372036854775808</b> the time 
 	 * wasn't properly set.
 	 */
 	public long getScheduledTime() {
 		return mScheduled;
 	}
 
 	/**
 	 * Sets the scheduled time for this arrival.
 	 * @param time The milliseconds since epoch when the arrival is scheduled 
 	 * as a {@link Long}.
 	 */
 	public void setScheduledTime(long time) {
 		mScheduled = time;
 	}
 
 	/**
 	 * Gets the block for this arrival.
 	 */
 	public int getBlock() {
 		return mBlock;
 	}
 
 	/**
 	 * Sets the block for this arrival.
 	 */
 	public void setBlock(int value) {
 		mBlock = value;
 	}
 
 	/**
 	 * Gets the piece of the block for this arrival.    
 	 */
 	public String getPiece() {
 		return mPiece;
 	}
 
 	/**
 	 * Sets the block for this arrival.    
 	 */
 	public void setPiece(String value) {
 		mPiece = value;
 	}
 
 	/**
 	 * Indicates if the vehicle has begun the trip which will arrival at the requested stop.
 	 */
 	public boolean isDeparted() {
 		return mDeparted;
 	}
 
 	/**
 	 * Sets whether the vehicle has begun the trip which will arrival at the requested stop.
 	 */
 	public void setDeparted(boolean departed) {
 		mDeparted = departed;
 	}
 
 	/**
 	 * Indicates if the arrival may be effected by a detour in effect along the route.
 	 */
 	public Boolean isDetour() {
 		return mDetour;
 	}
 
 	/**
 	 * Sets whether the arrival may be effected by a detour in effect along the route.    
 	 */
 	public void setDetour(Boolean value) {
 		mDetour = value;
 	}
 
 	/**
 	 * Reports the current status of the arrival.
 	 * 
 	 * @return The status of the arrival. Either {@link #STATUS_ESTIMATED},
 	 *     {@link #STATUS_SCHEDULED}, {@link #STATUS_DELAYED}, or 
 	 *     {@link #STATUS_CANCELED}.
 	 */
 	public int getStatus() {
 		return mStatus;
 	}
 
 	/**
 	 * Sets the status of the arrival.
 	 * @param status The status of the arrival as a {@link String}. Must be 
 	 *     either "estimated", "scheduled", "delayed", or "canceled".
 	 */
 	public void setStatus(String status) {
 		if (status.equalsIgnoreCase("estimated"))
 			mStatus = STATUS_ESTIMATED;
 		else if (status.equalsIgnoreCase("scheduled"))
 			mStatus = STATUS_SCHEDULED;
 		else if (status.equalsIgnoreCase("delayed"))
 			mStatus = STATUS_DELAYED;
 		else if (status.equalsIgnoreCase("canceled"))
 			mStatus = STATUS_CANCELED;
 		else
 			throw new RuntimeException("status must be either \"estimated\", \"scheduled\", \"delayed\", or \"canceled\".");
 	}
 
 	/**
 	 * Sets the status of the arrival.
 	 * @param status The status of the arrival. Either {@link #STATUS_ESTIMATED},
 	 *     {@link #STATUS_SCHEDULED}, {@link #STATUS_DELAYED}, or 
 	 *     {@link #STATUS_CANCELED}.
 	 */
 	public void setStatus(int status) {
 		mStatus = status;
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
		if (mBlockPositions == null || mBlockPositions.size() == 0)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeTypedList(mBlockPositions);
 		}
 
 		dest.writeInt(mRoute);
 		dest.writeInt(mLocid);
 		dest.writeInt(mDir  );
 
 		if (mShortSign == null)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeString(mShortSign);
 		}
 
 		if (mFullSign == null)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeString(mFullSign );
 		}
 
 		dest.writeLong(mEstimated);
 		dest.writeLong(mScheduled);
 		dest.writeInt (mBlock    );
 
 		if (mPiece == null)
 			dest.writeInt(0);
 		else {
 			dest.writeInt(1);
 			dest.writeString(mPiece    );
 		}
 
 		dest.writeString(Boolean.toString(mDeparted));
 		dest.writeString(Boolean.toString(mDetour  ));
 		dest.writeInt(mStatus   );
 	}
 
 	public static final Parcelable.Creator<ArrivalType> CREATOR = new Parcelable.Creator<ArrivalType>() {
 		public ArrivalType createFromParcel(Parcel in) {
 			return new ArrivalType(in);
 		}
 
 		public ArrivalType[] newArray(int size) {
 			return new ArrivalType[size];
 		}
 	};
 
 	private ArrivalType(Parcel dest) {
 		if (dest.readInt() == 1)
 			dest.readTypedList(mBlockPositions, BlockPositionType.CREATOR);
 		mRoute     = dest.readInt();
 		mLocid     = dest.readInt();
 		mDir       = dest.readInt();
 		if (dest.readInt() == 1)
 			mShortSign = dest.readString();
 		if (dest.readInt() == 1)
 			mFullSign  = dest.readString();
 		mEstimated = dest.readLong();
 		mScheduled = dest.readLong();
 		mBlock     = dest.readInt();
 		if (dest.readInt() == 1)
 			mPiece     = dest.readString();
 		mDeparted  = Boolean.parseBoolean(dest.readString());
 		mDetour    = Boolean.parseBoolean(dest.readString());
 		mStatus    = dest.readInt();
 	}
 
 }
