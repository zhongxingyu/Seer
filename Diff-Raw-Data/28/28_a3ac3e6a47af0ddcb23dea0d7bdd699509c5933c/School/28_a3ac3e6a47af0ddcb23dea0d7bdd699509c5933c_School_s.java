 package mwhs.ap.bkat.app;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class School implements Parcelable{
 	private String schoolName;
 	private String setting;
 	private String state;
 	private int totalUndergrads;
 	private int tuitionInState;
 	private int tuitionOutOfState;
 	private int roomAndBoardCost;
 	private String[] majors;
 	private String[] sports;
 	
 	public String[] getMajors() {
 		return majors;
 	}
 
 	public void setMajors(String[] majors) {
 		this.majors = majors;
 	}
 
 	public void setSports(String[] sports) {
 		this.sports = sports;
 	}
 
 	public String[] getSports() {
 		return sports;
 	}
 
 	public School(String schoolName, String setting,
 			String totalUndergrads, String tuitionInState,
 			String tuitionOutOfState, String roomAndBoardCost, String state)  {
 		
 			this.schoolName = schoolName;
 			this.setting = setting;
 			this.state = state;
 			this.totalUndergrads = Integer.parseInt(totalUndergrads);
 			this.tuitionInState = Integer.parseInt(tuitionInState);
 			this.tuitionOutOfState = Integer.parseInt(tuitionOutOfState);
 			this.roomAndBoardCost = Integer.parseInt(roomAndBoardCost);
 			
 	}
 
 	public School(Parcel in) {
 		this.schoolName = in.readString();
 		this.state = in.readString();
 		this.setting = in.readString();
 		this.totalUndergrads = in.readInt();
 		this.tuitionInState = in.readInt();
 		//this.tuitionOutOfState = in.readInt();
 		//this.roomAndBoardCost = in.readInt();
		//this.majors = in.readStringArray(in.));
 	}
 	
 	public School(){
 		
 	}
 
 	public School(String schoolName2, String setting2, String state2,
 			String totalUndergrads2, String tuitionInState2, String[] majors2) {
 		this.schoolName = schoolName2;
 		this.setting = setting2;
 		this.state = state2;
 		if(!totalUndergrads2.equals("Not set")){
 			this.totalUndergrads = Integer.parseInt(totalUndergrads2);
 		}else{
 			this.totalUndergrads = 0;
 		}
 		if(!tuitionInState2.equals("Not set")){
 			this.tuitionInState = Integer.parseInt(tuitionInState2);
 		}else{
 			this.tuitionInState = 0;
 		}
 		
 		this.majors = majors2;
 	}
 
 	public String getSchoolName() {
 		return schoolName;
 	}
 
 	public String getSetting() {
 		return setting;
 	}
 
 	public int getTotalUndergrads() {
 		return totalUndergrads;
 	}
 
 	public int getTuitionInState() {
 		return tuitionInState;
 	}
 
 	public int getTuitionOutOfState() {
 		return tuitionOutOfState;
 	}
 
 	public int getRoomAndBoardCost() {
 		return roomAndBoardCost;
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 	
 	public String toString(){
 		return schoolName;
 	}
 	
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeString(schoolName);
 		dest.writeString(state);
 		dest.writeString(setting);
 		dest.writeInt(totalUndergrads);
 		dest.writeInt(tuitionInState);
 		//dest.writeInt(tuitionOutOfState);
 		//dest.writeInt(roomAndBoardCost);
		dest.writeArray(majors);
 		
 	}
 	   public String getState() {
 		return state;
 	}
 	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
 	        public School createFromParcel(Parcel in)
 	        {
 	            return new School(in);
 	        }
 	 
 	        public School[] newArray(int size)
 	        {
 	            return new School[size];
 	        }
 	    };
 	
 }
 
