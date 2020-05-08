 package edu.aau.utzon.webservice;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.database.Cursor;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.google.android.maps.GeoPoint;
 
 public class PointModel {
 	private GeoPoint mGeoPoint;
 	private int mId;
 	private String mDesc;
 	private String mName;
 	private double mLong;
 	private double mLat;
 
 	public PointModel()
 	{
 		
 	}
 	
 	public int getId() {
 		return mId;
 	}
 	
 	public double getLong() {
 		return mLong;
 	}
 	public double getLat() {
 		return mLat;
 	}
 	public String getDesc() {
 		return mDesc;
 	}
 	public String getName() {
 		return mName;
 	}
 	public GeoPoint getGeoPoint() {
 		return mGeoPoint;
 	}
 
 	public PointModel(int id, String desc, String name, double lat, double lg)
 	{
 		this.mId = id;
 		this.mDesc = desc;
 		this.mName = name;
 		this.mLat = lat;
 		this.mLong = lg;
		this.mGeoPoint = new GeoPoint((int)(lat*1e6), (int)(lg*1e6));
		
 	}
 	
 	static public List<PointModel> asPointModel (Cursor c){
 		List<PointModel> result = new ArrayList<PointModel>();
 		
 		c.moveToFirst();
 		do
 		{
 			int colIndexId = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_ID);
 			int colIndexDesc = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_DESCRIPTION);
 			int colIndexLat = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_LAT);
 			int colIndexLong = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_LONG);
 			int colIndexName = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_NAME);
 
 			int id = c.getInt(colIndexId);
 			String desc = c.getString(colIndexDesc);
 			double lat = c.getDouble(colIndexLat);
 			double lg = c.getDouble(colIndexLong);
 			String name = c.getString(colIndexName);
 			
 			PointModel p = new PointModel(id, desc, name, lat, lg);
 			result.add(p);
 
 		} while (c.moveToNext() == true);
 		
 		c.close();
 		return result;
 	}
 
 //	@Override
 //	public int describeContents() {
 //		// TODO Auto-generated method stub
 //		return 0;
 //	}
 //
 //	@Override
 //	public void writeToParcel(Parcel dest, int flags) {
 //		// TODO Auto-generated method stub
 //		dest.writeInt(mGeoPoint.getLatitudeE6());
 //		dest.writeInt(mGeoPoint.getLongitudeE6());
 //		dest.writeInt(mId);
 //		dest.writeString(mDesc);
 //		dest.writeString(mName);
 //	}
 //
 //	// this is used to regenerate your object. All Parcelables must have a
 //	// CREATOR that implements these two methods
 //	public static final Parcelable.Creator<PointModel> CREATOR = new Parcelable.Creator<PointModel>() {
 //		public PointModel createFromParcel(Parcel in) {
 //			return new PointModel(in);
 //		}
 //
 //		public PointModel[] newArray(int size) {
 //			return new PointModel[size];
 //		}
 //	};
 //
 //	// Constructor that takes a Parcel and gives you an object populated with it's values
 //	private PointModel(Parcel in) {
 //		int lat = in.readInt();
 //		int longitude = in.readInt();
 //		mGeoPoint = new GeoPoint(lat, longitude);
 //		mId = in.readInt();
 //		mDesc = in.readString();
 //		mName = in.readString();
 //	}
 }
