 package com.kuxhausen.projectmeet;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 
 public class Meeting implements Parcelable{
 
 	
 	int duration=60, startMinute, stopMinute, startHour, stopHour, startDay, stopDay, month, year;
 	
 	Boolean preferSMS, autoSelectBestTime;
 	
	String meetingName, hostName, hostEmail, hostNumber;
 	
 	
 	public JSONObject toJSON(ArrayList<Person> pList){
 		JSONObject object = new JSONObject();
 		  try {
 		    object.put("name", meetingName);
 		    object.put("length", duration);
 		    
 		    GregorianCalendar startCal = new GregorianCalendar();
 		    startCal.set(year, month, startDay, startHour, startMinute, 0);
 		    object.put("start", startCal.getTimeInMillis()/1000);
 		    GregorianCalendar stopCal = new GregorianCalendar();
 		    stopCal.set(year, month, stopDay, stopHour, stopMinute, 0);
 		    object.put("end",  stopCal.getTimeInMillis()/1000);
 		    
 		    object.put("narrowToOne", autoSelectBestTime);
 		    
 		    JSONArray jRay = new JSONArray();
 		    for(Person pers : pList)
 		    {
 		    JSONObject jUserDemo = new JSONObject();
 			jUserDemo.put("name", pers.name);
		    jUserDemo.put("email",pers.email);
 		    jUserDemo.put("phone",pers.number);
 		    jRay.put(jUserDemo);
 		    }
 		    object.put("attendees", jRay );
 		    
 		    JSONObject jCreator = new JSONObject();
 		    jCreator.put("name", hostName);
 		    jCreator.put("email", hostEmail);
 		    jCreator.put("phone", hostNumber);
 		    object.put("creator", jCreator);
 		    
 		  } catch (JSONException e) {
 		    e.printStackTrace();
 		  }
 		  Log.e("asdf",object.toString());
 		return object;
 	}
 
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	public Meeting () {
     }
 	
 	public static final Parcelable.Creator<Meeting> CREATOR
     = new Parcelable.Creator<Meeting>() {
 	public Meeting createFromParcel(Parcel in) {
 	    return new Meeting(in);
 	}
 	
 	public Meeting[] newArray(int size) {
 	    return new Meeting[size];
 	}
 	};
 	
 	public Meeting(Parcel in) {
 	 
 	 duration= in.readInt(); startMinute= in.readInt(); stopMinute= in.readInt(); startHour= in.readInt(); stopHour= in.readInt(); startDay= in.readInt(); stopDay= in.readInt(); month= in.readInt(); year= in.readInt();
 	 boolean[] bools = new boolean[2];
 	 in.readBooleanArray(bools);
 	 preferSMS = bools[0]; autoSelectBestTime = bools[1];
 	 
 	 meetingName=in.readString(); hostName=in.readString(); hostEmail=in.readString(); hostNumber=in.readString();
 	}
 
 	@Override
 	public void writeToParcel(Parcel out, int flags) {
 		out.writeInt(duration); out.writeInt(startMinute); out.writeInt(stopMinute); out.writeInt(startHour); out.writeInt(stopHour); out.writeInt(startDay); out.writeInt(stopDay); out.writeInt(month); out.writeInt(year);
 		boolean[] bools = new boolean[2];
 		preferSMS = bools[0]; autoSelectBestTime = bools[1];
 		out.writeBooleanArray(bools);
 		
 		out.writeString(meetingName); out.writeString(hostName); out.writeString(hostEmail); out.writeString(hostNumber);
 	}
 	
 	
 }
