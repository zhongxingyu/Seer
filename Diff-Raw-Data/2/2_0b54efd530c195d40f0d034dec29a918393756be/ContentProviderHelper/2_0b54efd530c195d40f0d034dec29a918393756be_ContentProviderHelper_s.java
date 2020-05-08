 package no.ntnu.stud.fallprevention.connectivity;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
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
 			e.printStackTrace();
 		}
 
 		return returner;
 	}
 
 	public double getGaitSpeed(Timestamp start, Timestamp stop) {
 		double returner = -1;
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
 		Log.v(TAG, ""+mStepsDayOne);
 		RiskStatus returner = RiskStatus.OK_JOB;
 		
 		double mTotalRisk = getStepCountScore(mStepsDayOne);
 		Log.v(TAG, "mStepCountScore: "+mTotalRisk);
 		
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
 
 	private double getStepCountScore(double mStepsDayOne) {
 		double mStepCountScore=mStepsDayOne/10;
 		if(mStepCountScore>110){
 			mStepCountScore=110;
 		}
 		return mStepCountScore;
 		
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
 			returner.add((double) getGaitVariability(getHoursBack((i + 1) * 24),
 					getHoursBack(i * 24)));
 			Log.v(TAG,
 					"Variability:"
 							+ getGaitVariability(getHoursBack((i + 1) * 24),
 									getHoursBack(i * 24)));
 		}
 
 		return returner;
 	}
 
 }
