 package com.photon.connecttodoor.activity;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public abstract class MainActivity extends Activity{
 
 	public int pYear, untilYear;
 	public int pMonth, untilMonth;
 	public int pDay, untilDay;
 	public static final int DATE_DIALOG_ID = 0;
 	public static final int DATE_UNTIL_DIALOG_ID = 1;
 	public static final String STRIP = "-";
 	public static final String NUMBER_DATE = "0";
 	public static final String SLASH = "/";
 	ArrayAdapter<CharSequence> adapter ;
 	/** 
 	 * Get the current date or reset date to current date after used 
 	 * 
 	 */
 	public void getCurrentDate(){
 		final Calendar cal = Calendar.getInstance();
 		pYear = cal.get(Calendar.YEAR);
 		pMonth = cal.get(Calendar.MONTH);
 		pDay = cal.get(Calendar.DAY_OF_MONTH);
 	}
 
 	public void getUntilCurrentDate(){
 		final Calendar cal = Calendar.getInstance();
 		untilYear = cal.get(Calendar.YEAR);
 		untilMonth = cal.get(Calendar.MONTH);
 		untilDay = cal.get(Calendar.DAY_OF_MONTH);
 	}
 
 	/**
 	 * Create Date for startDate editText and untilDate editText 
 	 * @return getDateEditText as StringBuilder
 	 */
 	public StringBuilder getDateEditText(){
 		// Month is 0 based so add 1
 		String pMonthString = String.valueOf((pMonth+1)).trim().toString();
 		String pDayString = String.valueOf(pDay).trim().toString();
 		String pYearString = String.valueOf(pYear).trim().toString();
 
 		if(pMonthString.length() < 2){
 			pMonthString = NUMBER_DATE+pMonthString;
 		}
 
 		if(pDayString.length() < 2){
 			pDayString = NUMBER_DATE+pDayString;
 		}
 
 		return new StringBuilder()
 		.append(pDayString).append(STRIP)
 		.append(pMonthString).append(STRIP)
 		.append(pYearString).append("");
 	}
 
 	public StringBuilder getUntilDateEditText(){
 		// Month is 0 based so add 1
 		String untilMonthString = String.valueOf((untilMonth+1)).trim().toString();
 		String untilDayString = String.valueOf(untilDay).trim().toString();
 		String untilYearString = String.valueOf(untilYear).trim().toString();
 
 		if(untilMonthString.length() < 2){
 			untilMonthString = NUMBER_DATE+untilMonthString;
 		}
 
 		if(untilDayString.length() < 2){
 			untilDayString = NUMBER_DATE+untilDayString;
 		}
 
 		return new StringBuilder()
 		.append(untilDayString).append(STRIP)
 		.append(untilMonthString).append(STRIP)
 		.append(untilYearString).append("");
 	}
 
 	/**
 	 * change format date 
 	 * use to send date to service
 	 * @param date
 	 * @return
 	 */
 	public String changeFormatDate(String date){
 		String fullDate = "";
		if(!date.matches("")){
			String [] formatDate = date.split(STRIP);
 			String day = formatDate[0];
 			String month = formatDate[1];
 			String year = formatDate[2];
 			fullDate = year+STRIP+month+STRIP+day;
 		}
 		return fullDate;
 	}
 
 	/**
 	 * this method used to make alert message
 	 * @param message : this param used to show alert title
 	 * @param context : context each activity
 	 */
 	public void alertMessage(String message, Context context){
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
 		// set title
 		alertDialogBuilder.setTitle("Alert");
 
 		// set dialog message
 		alertDialogBuilder
 		.setMessage(message)
 		.setCancelable(false)
 		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// if this button is clicked, close
 				// current activity
 				dialog.dismiss();
 			}
 		});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 	/**
 	 * save value to internal storage
 	 * Store private primitive data in key-value pairs.
 	 * @param key 
 	 * @param value
 	 * @param context
 	 */
 	public void savePreference(String key, String value, Context context){
 		SharedPreferences sharedPreferences = context.getSharedPreferences("com.photon.connecttodoor", Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putString(key, value);
 		editor.commit();
 	}
 
 	/**
 	 * get value in internal storage
 	 * @param key
 	 * @param context
 	 * @return
 	 */
 	public String loadStringPreferences(String key, Context context){
 		SharedPreferences sharedPreferences = context.getSharedPreferences("com.photon.connecttodoor", Context.MODE_PRIVATE);
 		return sharedPreferences.getString(key, "");
 	}
 
 	/**
 	 * create dropdown category
 	 * @param context : context each activity
 	 * @param categoryItem : name of each category name
 	 * @param nameSpinnerId : name of spinner
 	 */
 	public void createDropdownCategory(Context context,String[] categoryItem, Spinner nameSpinnerId){
 		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item,categoryItem);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		nameSpinnerId.setAdapter(adapter);
 	}
 	/**
 	 * check internet status connection
 	 * if no internet connection return false
 	 * @return
 	 */
 	public boolean connectionAvailable() {
 		boolean connected = false;
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
 				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
 			connected = true;
 		}
 		return connected;
 	}
 	@Override
 	public void onBackPressed() {
 		finish();
 	}
 }
