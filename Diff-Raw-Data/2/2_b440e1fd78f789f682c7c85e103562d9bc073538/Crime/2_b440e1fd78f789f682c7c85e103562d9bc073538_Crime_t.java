 package com.bignerdranch.android.criminalintent;
 
 import java.util.Date;
 import java.util.UUID;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Crime {
 	private static final String JSON_ID = "id";
 	private static final String JSON_TITLE = "title";
 	private static final String JSON_SOLVED = "solved";
 	private static final String JSON_DATE = "date";
 	
     private UUID mId;
     private String mTitle;
     private Date mDate;
     private boolean mSolved;
 
     public Crime() {
         mId = UUID.randomUUID();
         mDate = new Date();
     }
 
     @Override
     public String toString() {
         return mTitle;
     }
 
     public String getTitle() {
         return mTitle;
     }
 
     public void setTitle(String title) {
         mTitle = title;
     }
 
     public UUID getId() {
         return mId;
     }
 
     public boolean isSolved() {
         return mSolved;
     }
 
     public void setSolved(boolean solved) {
         mSolved = solved;
     }
 
     public Date getDate() {
         return mDate;
     }
 
     public void setDate(Date date) {
         mDate = date;
     }
 
 	public JSONObject toJSON() throws JSONException {
 		JSONObject json = new JSONObject();
 		json.put(JSON_ID, mId.toString());
 		json.put(JSON_TITLE, mTitle);
 		json.put(JSON_SOLVED, mSolved);
 		json.put(JSON_DATE, mDate.getTime());
		return json;
 	}
 
 
 }
