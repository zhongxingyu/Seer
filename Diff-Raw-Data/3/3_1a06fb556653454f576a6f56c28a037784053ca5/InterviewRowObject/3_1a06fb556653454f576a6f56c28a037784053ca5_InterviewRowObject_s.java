 package com.yahoo.interviewr;
 
 import android.content.Context;
 
 /*
  * This class holds information about a time slot in the schedule.
  */
 public class InterviewRowObject {
 	String mName;
 	int mPhotoId;
 	String mInterviewTime;
 	String mPosition;
 	String mAboutMe;
 	boolean mExpanded;
 	
 	public InterviewRowObject(Context activityContext, String name, 
 			String interviewTime, String position, String aboutMe) {
 		this.mName = name;
 		this.mInterviewTime = interviewTime;
 		this.mPosition = position;
 		this.mAboutMe = aboutMe;
 		this.mExpanded = false;
 		
 		// find the drawable corresponding to the interviewer's name, which
 		// we have scraped from web - assume name is in format "John Smith"
 		String[] nameArray = name.split(" ");
 		if (nameArray.length > 0) {
 			String photoName = nameArray[0].toLowerCase();
 			if (photoName != null) {
 				// assume photo name will be something like 
 				// john.jpg in the drawable/ folder
 				mPhotoId = activityContext.getResources()
 						.getIdentifier(photoName, "drawable", activityContext.getPackageName());
 			}
 		}
 	}
 }
