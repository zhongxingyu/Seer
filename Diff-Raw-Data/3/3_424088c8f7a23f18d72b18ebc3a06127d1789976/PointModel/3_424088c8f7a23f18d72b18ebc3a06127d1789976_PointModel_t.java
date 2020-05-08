 package edu.aau.utzon.webservice;
 
 import java.io.Serializable;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.google.android.maps.GeoPoint;
 
 public class PointModel implements Parcelable{
 	public GeoPoint mGeoPoint;
 	public int mId;
	public String mDesc
	public String mName;
 
 	public PointModel() {
 	}
 
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		// TODO Auto-generated method stub
 		dest.writeInt(mGeoPoint.getLatitudeE6());
 		dest.writeInt(mGeoPoint.getLongitudeE6());
 		dest.writeInt(mId);
 		dest.writeString(mDesc);
 	}
 
 	// this is used to regenerate your object. All Parcelables must have a
 	// CREATOR that implements these two methods
 	public static final Parcelable.Creator<PointModel> CREATOR = new Parcelable.Creator<PointModel>() {
 		public PointModel createFromParcel(Parcel in) {
 			return new PointModel(in);
 		}
 
 		public PointModel[] newArray(int size) {
 			return new PointModel[size];
 		}
 	};
 
 	// Constructor that takes a Parcel and gives you an object populated with it's values
 	private PointModel(Parcel in) {
 		int lat = in.readInt();
 		int longitude = in.readInt();
 		mGeoPoint = new GeoPoint(lat, longitude);
 		mId = in.readInt();
 		mDesc = in.readString();
 	}
 }
