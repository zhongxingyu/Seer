 package com.parse.starter;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.Parse;
 import com.parse.ParseACL;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseException;
 
 import com.parse.ParseUser;
 
 import android.app.Application;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class ParseApplication extends Application {
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		// Add your initialization code here
 		Parse.initialize(this, "dF4lf8kvjIWL3PqE8ANdS5ancdatrrlYMw0Dm2nM", "jIg3EUugNpG7aMKsSGWiIGrW9jeJPuzTYtUwwnDK");

 		
 		ParseUser.enableAutomaticUser();
		ParseUser.getCurrentUser().increment("RunCount");
 		ParseACL defaultACL = new ParseACL();
 		// Optionally enable public read access by default.
 		// defaultACL.setPublicReadAccess(true);
 		ParseACL.setDefaultACL(defaultACL, true);
 		 
 	}
 
 	public static ArrayList<ParseObject> getFoodItems() {
 		ParseQuery query = new ParseQuery("FoodEvent");
 		ArrayList<ParseObject> retval = new ArrayList<ParseObject>();
 		try {
 			ArrayList<ParseObject> prelim = (ArrayList) query.find();
 			Collections.sort(prelim, new ParseObjectComparator());
 			for (ParseObject obj : prelim) {
 				//Filter based on if it took place before today
 				String date = obj.getString("end_time");
 				
 				GregorianCalendar curr = (GregorianCalendar) Calendar.getInstance();
 				HoosEatingDatetime d = new HoosEatingDatetime(date, curr);
 				
 				if (d.isRelevant()) {
 					retval.add(obj);
 				}
 			}
 		} catch (ParseException e) {
 			
 		}
 		return retval;
 	}
 	
 	public static boolean checkConn(Context ctx)
     {
         ConnectivityManager conMgr =  (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo i = conMgr.getActiveNetworkInfo();
         if (i == null)
           return false;
         if (!i.isConnected())
           return false;
         if (!i.isAvailable())
           return false;
         return true;
 
     }
 
 
 }
