 package com.coffeeandpower.cont;
 
 import java.io.Serializable;
 
 import org.json.JSONObject;
 
 import com.coffeeandpower.Constants;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 @SuppressWarnings("serial")
 public class UserSmart implements Parcelable {
 	//Eliminated Serializable
 
 	private int checkInId;
 	private int userId;
 	private String nickName;
 	private String statusText;
 	private String photo; // ???
 	private String majorJobCategory;
 	private String minorJobCategory;
 	private String headLine;
 	private String fileName; // this is Profile Photo
 	private double lat;
 	private double lng;
 	private int checkedIn;
 	private String foursquareId;
 	private String venueName;
 	private int venueId;
 	private int checkInCount;
 	private String skills;
 	private boolean met;
 	private boolean isFirstInList;
 	private String sponsorNickname;
 
 	public boolean isFirstInList() {
 		return isFirstInList;
 	}
 
 	public void setFirstInList(boolean isFirstInList) {
 		this.isFirstInList = isFirstInList;
 	}
 	
 	public UserSmart(JSONObject objUser)
 	{
 		super();		
 		this.checkInId = objUser.optInt("checkin_id");
 		this.userId = objUser.optInt("userid");
		if(this.userId == 0)
		{
			this.userId = objUser.optInt("Id");
			if(this.userId == 0)
				this.userId = objUser.optInt("id");
		}
		if(this.userId == 0)
		{
			Log.d("UserSmart", "User id is still 0, this is bad");
		}
		
 		this.nickName = objUser.optString("nickname");
 		this.statusText = objUser.optString("status_text");
 		this.photo = objUser.optString("photo");
 		this.majorJobCategory = objUser.optString("major_job_category");
 		this.minorJobCategory = objUser.optString("minor_job_category");
 		this.headLine = objUser.optString("headline");
 		
 		if (!objUser.optString("filename").equals(""))
 			this.fileName = objUser.optString("filename");
 		else if (!objUser.optString("imageUrl").equals(""))
 			this.fileName = objUser.optString("imageUrl");
 		else
 			if (Constants.debugLog)
 				Log.d("UserSmart","Warning, could not parse user image URL with keys 'filename' or 'imageUrl'...");
 		
 		this.lat = objUser.optDouble("lat");
 		this.lng = objUser.optDouble("lng");
 		this.checkedIn = objUser.optInt("checked_in");
 		this.foursquareId = objUser.optString("foursquare");
 		this.venueName = objUser.optString("venue_name");
 		this.venueId = objUser.optInt("venue_id");
 		this.checkInCount = objUser.optInt("checkin_count");
 		this.skills = objUser.optString("skills");
 		this.sponsorNickname = objUser.optString("sponsorNickname");
 		if(this.sponsorNickname.equalsIgnoreCase("")==false)
 		{
 			Log.d("UserSmart","Sponsor: %s" + this.sponsorNickname);
 			int test = 5;
 			int test2 = test;
 		}
 		this.met = objUser.optBoolean("met");
 	}
 	
 
 	public UserSmart(int checkInId, int userId, String nickName, String statusText, String photo, String majorJobCategory,
 			String minorJobCategory, String headLine, String fileName, double lat, double lng, int checkedIn, String foursquareId,
 			String venueName, int checkInCount, String skills, boolean met) {
 		super();
 		this.checkInId = checkInId;
 		this.userId = userId;
 		this.nickName = nickName;
 		this.statusText = statusText;
 		this.photo = photo;
 		this.majorJobCategory = majorJobCategory;
 		this.minorJobCategory = minorJobCategory;
 		this.headLine = headLine;
 		this.fileName = fileName;
 		this.lat = lat;
 		this.lng = lng;
 		this.checkedIn = checkedIn;
 		this.foursquareId = foursquareId;
 		this.venueName = venueName;
 		this.checkInCount = checkInCount;
 		this.skills = skills;
 		this.met = met;
 	}
 
 	public int getCheckInId() {
 		return checkInId;
 	}
 
 	public void setCheckInId(int checkInId) {
 		this.checkInId = checkInId;
 	}
 
 	public int getUserId() {
 		return userId;
 	}
 
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 
 	public String getNickName() {
 		return nickName;
 	}
 
 	public void setNickName(String nickName) {
 		this.nickName = nickName;
 	}
 
 	public String getStatusText() {
 		return statusText;
 	}
 
 	public void setStatusText(String statusText) {
 		this.statusText = statusText;
 	}
 
 	public String getPhoto() {
 		return photo;
 	}
 
 	public void setPhoto(String photo) {
 		this.photo = photo;
 	}
 
 	public String getMajorJobCategory() {
 		return majorJobCategory;
 	}
 
 	public void setMajorJobCategory(String majorJobCategory) {
 		this.majorJobCategory = majorJobCategory;
 	}
 
 	public String getMinorJobCategory() {
 		return minorJobCategory;
 	}
 
 	public void setMinorJobCategory(String minorJobCategory) {
 		this.minorJobCategory = minorJobCategory;
 	}
 
 	public String getHeadLine() {
 		return headLine;
 	}
 
 	public void setHeadLine(String headLine) {
 		this.headLine = headLine;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public double getLat() {
 		return lat;
 	}
 
 	public void setLat(double lat) {
 		this.lat = lat;
 	}
 
 	public double getLng() {
 		return lng;
 	}
 
 	public void setLng(double lng) {
 		this.lng = lng;
 	}
 
 	public int getCheckedIn() {
 		return checkedIn;
 	}
 
 	public void setCheckedIn(int checkedIn) {
 		this.checkedIn = checkedIn;
 	}
 
 	public String getFoursquareId() {
 		return foursquareId;
 	}
 
 	public void setFoursquareId(String foursquareId) {
 		this.foursquareId = foursquareId;
 	}
 
 	public String getVenueName() {
 		return venueName;
 	}
 
 	public void setVenueName(String venueName) {
 		this.venueName = venueName;
 	}
 
 	public int getCheckInCount() {
 		return checkInCount;
 	}
 
 	public void setCheckInCount(int checkInCount) {
 		this.checkInCount = checkInCount;
 	}
 
 	public String getSkills() {
 		return skills;
 	}
 
 	public void setSkills(String skills) {
 		this.skills = skills;
 	}
 
 	public boolean isMet() {
 		return met;
 	}
 
 	public void setMet(boolean met) {
 		this.met = met;
 	}
 	
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel out, int flags) {
 		
 		out.writeInt(this.checkInId);
 		out.writeInt(this.userId);
 		out.writeString(this.nickName);
 		out.writeString(this.statusText);
 		out.writeString(this.photo);
 		out.writeString(this.majorJobCategory);
 		out.writeString(this.minorJobCategory);
 		out.writeString(this.headLine);
 		out.writeString(this.fileName);
 		out.writeDouble(this.lat);
 		out.writeDouble(this.lng);
 		out.writeInt(this.checkedIn);
 		out.writeString(this.skills);
 		out.writeInt(this.met ? 1 : 0);
 		out.writeInt(this.isFirstInList ? 1 : 0);
 		
 	}
 	
 	public static final Parcelable.Creator<UserSmart> CREATOR = new Parcelable.Creator<UserSmart>() {
             public UserSmart createFromParcel(Parcel in) {
                 return new UserSmart(in);
             }
         
             public UserSmart[] newArray(int size) {
                 return new UserSmart[size];
             }
 	};
 
         private UserSmart(Parcel in) {
             this.checkInId = in.readInt();
             this.userId = in.readInt();
             this.nickName = in.readString();
             this.statusText = in.readString();
             this.photo = in.readString();
             this.majorJobCategory = in.readString();
             this.minorJobCategory = in.readString();
             this.headLine = in.readString();
             this.fileName = in.readString();
             this.lat = in.readDouble();
             this.lng = in.readDouble();
             this.checkedIn = in.readInt();
             this.skills = in.readString();
             this.met = ( in.readInt() == 1 ? true : false );
             this.isFirstInList = ( in.readInt() == 1 ? true : false );
         		    
         		    
         }
 
 }
