 package no.ntnu.stud.fallprevention.connectivity;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import no.ntnu.stud.fallprevention.Constants;
 import no.ntnu.stud.fallprevention.datastructures.RiskStatus;
 import android.annotation.SuppressLint;
 import android.content.ContentProviderClient;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.net.Uri;
 import android.os.RemoteException;
 import android.text.format.DateUtils;
 import android.util.Log;
 
 /**
  * Retrives data from the contentprovider, interprets them, and passes them on
  * in a form useful to the gui
  * 
  * @author Johannes, Dat-Danny
  * 
  */
 @SuppressLint("NewApi")
 public class ContentProviderHelper {
 
 	Context context;
 	private final static String TAG = "ContentProviderHelper";
 
 	public ContentProviderHelper(Context context) {
 		this.context = context;
 
 	}
 
 	/**
 	 * Returns number of steps done the in the period between start and stop
 	 * 
 	 * @return mStepCount
 	 */
 	public int getStepCount(Timestamp start, Timestamp stop) {
 
 		Log.v(TAG, "Getting step count");
 		double mStepCount = 0;
 		// Setting variables for the query
 		// sets the unique resource identifier for the data
 		Uri uri = Uri.parse("content://ntnu.stud.valens.contentprovider");
 		ContentProviderClient stepsProvider = context.getContentResolver()
 				.acquireContentProviderClient(uri);
 
 		uri = Uri.parse("content://ntnu.stud.valens.contentprovider/raw_steps");
 		// sets the projection part of the query
 		String[] projection = new String[] { "count(timestamp) as count" };
 		// sets the selection part of the query
 		String selection = "timestamp > " + start.getTime()
 				+ " AND timestamp < " + stop.getTime();
 
 		// not used, therefore null
 		String[] selectionArgs = null;
 		// no need for sorting
 		String sortOrder = null;
 
 		// uses variables to construct query
 		Log.v(TAG, "Attempting query");
 		try {
 			// Everything in order
 			Cursor cursor = stepsProvider.query(uri, projection, selection,
 					selectionArgs, sortOrder);
 			cursor.moveToFirst();
 			Log.v(TAG, "Steps counted: " + String.valueOf(cursor.getString(0)));
 			mStepCount = cursor.getDouble(0);
 			Log.v(TAG, String.valueOf(mStepCount));
 			cursor.close();
 			Log.v(TAG, "Query done without errors!");
 
 		} catch (SQLException e) {
 			// SQL problems
 			Log.v(TAG, e.toString());
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// Remote binding problems
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			// Nullpointer problems
 			Log.v(TAG, e.toString());
 			e.printStackTrace();
 		}
 
 		return (int) mStepCount;
 	}
 
 	/**
 	 * Returns timestamp for number of hours counting backwards
 	 * 
 	 * @param hours
 	 *            : 0 means current, 24 means 24 hours back, etc
 	 * @return
 	 */
 
 	public Timestamp getHoursBack(int hours) {
 
 		return new Timestamp(System.currentTimeMillis()
 				- TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS));
 	}
 
 	public double getGaitVariability(Timestamp start, Timestamp stop) {
 		double returner = 3500;
 
 		// Setting variables for the query
 		// sets the unique resource identifier for the data
 		Uri uri = Uri.parse("content://ntnu.stud.valens.contentprovider");
 		ContentProviderClient stepsProvider = context.getContentResolver()
 				.acquireContentProviderClient(uri);
 
 		uri = Uri.parse("content://ntnu.stud.valens.contentprovider/gaits");
 		// sets the projection part of the query
 		String[] projection = new String[] { "variability" };
 		// sets the selection part of the query
 		String selection = "start > " + (start.getTime()-DateUtils.HOUR_IN_MILLIS) + " AND stop < "
 				+ (stop.getTime()+DateUtils.HOUR_IN_MILLIS);
 
 		// not used, therefore null
 		String[] selectionArgs = null;// {String.valueOf(start.getTime()),String.valueOf(stop.getTime())};
 		// no need for sorting
 		String sortOrder = null;
 
 		// uses variables to construct query
 		Log.v(TAG, "Attempting query");
 		try {
 			// Everything in order
 			Cursor cursor = stepsProvider.query(uri, projection, selection,
 					selectionArgs, sortOrder);
 			cursor.moveToFirst();
 			if (cursor.getCount() > 0 & cursor != null) {
 				Log.v(TAG,
 						"Variability: " + String.valueOf(cursor.getString(0)));
 				returner = cursor.getDouble(0);
 				Log.v(TAG, String.valueOf(returner));
 				
 			} else {
 				Log.v(TAG, "Variability : Cursor empty");
 			}
 			cursor.close();
 			Log.v(TAG, "Query done without errors!");
 		} catch (SQLException e) {
 			// SQL problems
 			Log.v(TAG, e.toString());
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// Remote binding problems
 
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			// Nullpointer problems
 
 			Log.v(TAG, e.toString());
 			e.printStackTrace();
 		} catch (Exception e) {
 			// f* tha code-police
 		}
 
 		return returner;
 	}
 
 	public double getGaitSpeed(Timestamp start, Timestamp stop) {
 		double returner = 3500;
 		// Setting variables for the query
 		// sets the unique resource identifier for the data
 		Uri uri = Uri.parse("content://ntnu.stud.valens.contentprovider");
 		ContentProviderClient stepsProvider = context.getContentResolver()
 				.acquireContentProviderClient(uri);
 
 		uri = Uri.parse("content://ntnu.stud.valens.contentprovider/gaits");
 		// sets the projection part of the query
 		String[] projection = new String[] { "speed" };
 		// sets the selection part of the query
 		String selection = (start.getTime()-DateUtils.HOUR_IN_MILLIS) + " AND stop < "
                 + (stop.getTime()+DateUtils.HOUR_IN_MILLIS);
 
 		// not used, therefore null
 		String[] selectionArgs = null;// {String.valueOf(start.getTime()),String.valueOf(stop.getTime())};
 		// no need for sorting
 		String sortOrder = null;
 
 		// uses variables to construct query
 		Log.v(TAG, "Attempting query");
 		try {
 			// Everything in order
 			Cursor cursor = stepsProvider.query(uri, projection, selection,
 					selectionArgs, sortOrder);
 			cursor.moveToFirst();
 			if (cursor.getCount() > 0 & cursor != null) {
 				Log.v(TAG, "Speed: " + String.valueOf(cursor.getString(0)));
 				returner = cursor.getDouble(0);
 				Log.v(TAG, String.valueOf(returner));
 				Log.v(TAG, "Query done without errors!");
 			} else {
 				Log.v(TAG, "Speed: Cursor empty");
 			}
 			cursor.close();
 
 		} catch (SQLException e) {
 			// SQL problems
 			Log.v(TAG, e.toString());
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// Remote binding problems
 
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			// Nullpointer problems
 
 			Log.v(TAG, e.toString());
 			e.printStackTrace();
 		} catch (Exception e) {
 			// f* tha code-police
 		}
 		return returner;
 	}
 
 	/**
 	 * returns a code for the appropriate riskstatus, given steps registered in
 	 * the last days
 	 * 
 	 * @return
 	 */
 	public RiskStatus getRiskValue() {
 		Log.v(TAG, "Getting value");
 		
 		double mStepsDayOne = getStepCount(getHoursBack(24), getHoursBack(0));
 		double mStepsDayTwo = getStepCount(getHoursBack(48), getHoursBack(24));
 		Log.v(TAG, ""+mStepsDayOne);
 		RiskStatus returner = RiskStatus.OK_JOB;
 		
 		double mStepCountScore = getStepCountScore(mStepsDayOne);
 		Log.v(TAG, "mStepCountScore: "+mStepCountScore);
 		
 		double mStepCountComparisonScore = getStepCountComparisonScore(
 				mStepsDayOne, mStepsDayTwo);
 		Log.v(TAG, "mStepCountComparisonScore:"+ mStepCountComparisonScore);
 		
 		double mGaitSpeedScore = getGaitSpeedScore();
 		Log.v(TAG,"mGaitSpeedScore: " + mGaitSpeedScore);
 		
 		double mVariabilityScore = getVariabilityScore();
 		Log.v(TAG, "mVariabilityScore: "+mVariabilityScore);
 		
 		double mTotalRisk = getTotalRiskScore(mStepCountScore,
 				mStepCountComparisonScore, mGaitSpeedScore, mVariabilityScore);
 		Log.v(TAG, "mTotalRisk: "+mTotalRisk);
 		
 		if(mTotalRisk<=20){
 			returner = RiskStatus.BAD_JOB;
 		}else if(mTotalRisk<=40){
 			returner = RiskStatus.NOT_SO_OK_JOB;
 		}else if(mTotalRisk<=60){
 			returner = RiskStatus.OK_JOB;
 		}else if(mTotalRisk<=80){
 			returner = RiskStatus.GOOD_JOB;
 		}else {
 			returner = RiskStatus.VERY_GOOD_JOB;
 		}
 		return returner;
 
 	}
 
 	private double getTotalRiskScore(double mStepCountScore,
 			double mStepCountComparisonScore, double mGaitSpeedScore,
 			double mVariabilityScore) {
 		double mTotalRisk=0.2*(mStepCountScore)+ 0.5*(mStepCountComparisonScore)+
 		0.2*(mGaitSpeedScore) + 0.1*(mVariabilityScore);
 		return mTotalRisk;
 	}
 
 	private double getVariabilityScore() {
 		double d = getGaitVariability(getHoursBack(24),getHoursBack(0));
 		double mVariabilityScore;
 		
 		
 		Log.v(TAG, "Variability: " +d);
 		if(d<Constants.GOOD_VARI_NUMBER){
 		    mVariabilityScore=110;
 		}else if(d>Constants.BAD_VARI_NUMBER){
 		    mVariabilityScore=0;
 		} else{
 		    mVariabilityScore=50;
 		  //  mVariabilityScore=100/(d*10+1);
 		}
 		
 		 if(mVariabilityScore>=110){
 			mVariabilityScore=110;
 		 }
 		return mVariabilityScore;
 	}
 
 	private double getGaitSpeedScore() {
 		double mSpeedDayOne=getGaitSpeed(getHoursBack(24),getHoursBack(0));
 		double mSpeedDayTwo=getGaitSpeed(getHoursBack(48),getHoursBack(24));
 		
 		Log.v(TAG, "Speed:" + mSpeedDayOne +", "+ mSpeedDayTwo);
 		
 		double mGaitSpeedScore =0;
 		if(mSpeedDayOne<Constants.GOOD_SPEED_NUMBER){
 		    mGaitSpeedScore=110;
 		}else if(mSpeedDayOne>Constants.BAD_SPEED_NUMBER){
 		mGaitSpeedScore=0;
 		}else{
 		    mGaitSpeedScore=50;
 		}
 		
 		return mGaitSpeedScore;
 	}
 
 	private double getStepCountComparisonScore(double mStepsDayOne,
 			double mStepsDayTwo) {
	    if(mStepsDayTwo==0){
	        mStepsDayTwo=1;
	    }
 		double mStepCountComparisonScore=((mStepsDayOne/mStepsDayTwo)-0.1)*100;
 		if(mStepCountComparisonScore>=110){
 			mStepCountComparisonScore=110;
 		}
 		return mStepCountComparisonScore;
 	}
 
 	private double getStepCountScore(double mStepsDayOne) {
 		double mStepCountScore=(mStepsDayOne*100)/Constants.GOOD_STEPS_NUMBER;
 		if(mStepCountScore>110){
 			mStepCountScore=110;
 		}
 		return mStepCountScore;
 		
 	}
 
 	public void cpGetEventList() {
 		// TODO: Rename and write to database in database helper
 	}
 
 	/**
 	 * returns a list containing information for the statistics class to display
 	 * Information is gotten from the content provider and it is sorted with x
 	 * value and y value interleaved with each other, starting with x
 	 * 
 	 * 
 	 * @param length
 	 *            is the time in number of intervals backwards
 	 * @param interval
 	 *            is the size for each interval, in number of hours
 	 * @return
 	 */
 	public List<Double> cpGetStepsHistory(int length, int interval) {
 
 		List<Double> returner = new ArrayList<Double>();
 
 		for (int i = length; i >= 0; i--) {
 			// the list is supposed to be read in an interleaved format, meaning
 			// x and y values alternating
 			returner.add((double) (-i * interval));
 			returner.add((double) getStepCount(
 					getHoursBack((i + 1) * interval),
 					getHoursBack(i * interval)));
 
 		}
 		return returner;
 	}
 
 	public List<Integer> cpGetStepsHistoryWeek() {
 
 		List<Integer> returner = new ArrayList<Integer>();
 
 		for (int i = 1; i <= 7; i++) {
 			// the list is supposed to be read in an interleaved format, meaning
 			// x and y values alternating
 			Integer temp = getStepCount(getHoursBack((24 * (i))),
 					getHoursBack((24 * (i - 1))));
 			Log.v(TAG,
 					"getStepCount, start" + getHoursBack((24 * (i))).toString()
 							+ " end" + getHoursBack((24 * (i - 1))).toString()
 							+ " steps" + temp.toString());
 			returner.add(temp);
 		}
 		return returner;
 	}
 
 	/**
 	 * returns a list containing information for the statistics class to display
 	 * Information is gotten from the content provider and it is sorted with x
 	 * value and y value interleaved with each other, starting with x
 	 * 
 	 * 
 	 * @param length
 	 *            is the time in number of days backwards
 	 * 
 	 * @return
 	 */
 	public List<Double> cpGetSpeedHistory(int length) {
 
 		List<Double> returner = new ArrayList<Double>();
 
 		for (int i = length; i >= 0; i--) {
 			// the list is supposed to be read in an interleaved format, meaning
 			// x and y values alternating
 			returner.add((double) (-i));
 			returner.add((double) getGaitSpeed(getHoursBack((i + 1) * 24),
 					getHoursBack(i * 24)));
 			Log.v(TAG,
 					"Speed:"
 							+ getGaitSpeed(getHoursBack((i + 1) * 24),
 									getHoursBack(i * 24)));
 		}
 		return returner;
 	}
 
 	/**
 	 * returns a list containing information for the statistics class to display
 	 * Information is gotten from the content provider and it is sorted with x
 	 * value and y value interleaved with each other, starting with x
 	 * 
 	 * 
 	 * @param length
 	 *            is the time in number of days backwards
 	 * 
 	 * @return
 	 */
 	public List<Double> cpGetVariabilityHistory(int length) {
 		List<Double> returner = new ArrayList<Double>();
 
 		for (int i = length; i >= 0; i--) {
 			// the list is supposed to be read in an interleaved format, meaning
 			// x and y values alternating
 			returner.add((double) (-i));
 			returner.add((double) getGaitSpeed(getHoursBack((i + 1) * 24),
 					getHoursBack(i * 24)));
 			Log.v(TAG,
 					"Variability:"
 							+ getGaitSpeed(getHoursBack((i + 1) * 24),
 									getHoursBack(i * 24)));
 		}
 
 		return returner;
 	}
 
 }
