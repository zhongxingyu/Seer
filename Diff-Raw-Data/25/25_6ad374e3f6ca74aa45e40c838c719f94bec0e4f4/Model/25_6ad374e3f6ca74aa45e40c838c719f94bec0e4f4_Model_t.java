 package com.app.afridge;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.os.Build;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * A model class that resolves some of the business logic in the application.
  * Also as a part of the MVC(Model View Controller) programming pattern. It
  * presents changes to the UI thread through the Controller classes.
  * 
  * @author drakuwa
  */
 public class Model {
 
 	private Context ctx;
 	int mYear;
 	int mMonth;
 	int mDay;
 	ArrayList<String> passed_date = new ArrayList<String>();
 	ArrayList<String> warning_date = new ArrayList<String>();
 	ArrayList<String> tempArray = new ArrayList<String>();
 	SharedPreferences prefs;
 	int warning_days = 3;
 
 	/**
 	 * Constructor of the Model class which initializes the activity context.
 	 * 
 	 * @param context
 	 */
 	public Model(Context context) {
 		ctx = context;
 	}
 
 	public Model(Context context, int warningDays) {
 		ctx = context;
 		warning_days = warningDays;
 	}
 
 	/**
 	 * Method that creates and shows a Toast message with the specified Android
 	 * release version.
 	 */
 	public void email_toast() {
 		Toast
 				.makeText(
 						ctx,
 						"Android version "
 								+ Build.VERSION.RELEASE
 								+ " does not support direct calls to "
 								+ "the Email client. Send your mail to drakuwa@gmail.com",
 						Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * Method that checks if the application is run for the first time. It
 	 * checks for the existence of an empty file in the application folder, and
 	 * if it doesn't it creates an AlertDialog with the welcome message, and it
 	 * creates the file.
 	 */
 	public void first_run() {
 		boolean exists = (new File("/data/data/com.app.afridge/notwelcomefirst"))
 				.exists();
 
 		if (!exists) {
 			// Welcome note...
 			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 			builder.setMessage(
 					"Welcome to aFridge, "
 							+ "for more information and a quick HOWTO, "
 							+ "check the 'Info' section. ").setIcon(
 					R.drawable.icon).setTitle(R.string.app_name).setCancelable(
 					false).setPositiveButton("OK..",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 						}
 					});
 			AlertDialog alert = builder.create();
 			alert.show();
 			try {
 				new File("/data/data/com.app.afridge/notwelcomefirst")
 						.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Model method that checks the expiration dates of the fridge items, and
 	 * show two day warning, and expired items. If the method is called from
 	 * inside the fridge (parameter callFromFridge), it also shows a dialog if
 	 * everything is OK.
 	 * 
 	 * @param callFromFridge
 	 */
 	public void check_exp_date(boolean callFromFridge) {
 
 		Calendar c = Calendar.getInstance();
 		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH) + 1;
 		mDay = c.get(Calendar.DAY_OF_MONTH);
 		String exp_date;
 
 		passed_date.clear();
 		warning_date.clear();
 
 		DatabaseHelper myDb = new DatabaseHelper(ctx);
 		{
 			try {
 				myDb.createDataBase();
 			} catch (IOException ioe) {
 				throw new Error("Unable to create database");
 			}
 			try {
 				myDb.openDataBase();
 			} catch (SQLException sqle) {
 				throw sqle;
 			}
 			myDb.getReadableDatabase();
 			Cursor cc = myDb.getItems();
 			if (cc.moveToFirst()) {
 				do {
 					if (!cc.getString(7).equalsIgnoreCase("")) {
 						exp_date = cc.getString(7);
 						String[] exdate = exp_date.split("-");
 
 						if (Integer.parseInt(exdate[2]) < mYear) {
 							passed_date.add(cc.getString(0) + ": "
 									+ cc.getString(1) + " " + cc.getString(2));
 							continue;
 						} else if (Integer.parseInt(exdate[2]) == mYear
 								&& Integer.parseInt(exdate[1]) < mMonth) {
 							passed_date.add(cc.getString(0) + ": "
 									+ cc.getString(1) + " " + cc.getString(2));
 							continue;
 						} else if (Integer.parseInt(exdate[2]) == mYear
 								&& Integer.parseInt(exdate[1]) == mMonth
 								&& Integer.parseInt(exdate[0]) < mDay) {
 							passed_date.add(cc.getString(0) + ": "
 									+ cc.getString(1) + " " + cc.getString(2));
 							continue;
 						} else if (Integer.parseInt(exdate[2]) == mYear
 								&& Integer.parseInt(exdate[1]) == mMonth
 								&& Integer.parseInt(exdate[0]) - mDay < warning_days) {
 							warning_date.add(Integer.parseInt(exdate[0]) - mDay
 									+ " day[s] left for " + cc.getString(0)
 									+ ": " + cc.getString(1) + " "
 									+ cc.getString(2));
 						}
 
 					}
 
 				} while (cc.moveToNext());
 				if (!passed_date.isEmpty()) {
 					passed_exp();
 				}
 				if (!warning_date.isEmpty()) {
 					warning_exp();
 				}
 				if (passed_date.isEmpty() && warning_date.isEmpty()
 						&& callFromFridge) {
 					ok_exp();
 				}
 				myDb.close();
 			}
 		}
 
 	}
 
 	public void passed_exp() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setMessage("This item[s] have expired: " + passed_date)
 				.setIcon(R.drawable.icon).setTitle(R.string.app_name)
 				.setCancelable(false).setPositiveButton("OK..",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 							}
 						});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void warning_exp() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setMessage(
 				"This item[s] are soon going to expire: " + warning_date)
 				.setIcon(R.drawable.icon).setTitle(R.string.app_name)
 				.setCancelable(false).setPositiveButton("OK..",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 							}
 						});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void ok_exp() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setMessage(
 				"There are no items with near expiration date, or expired.")
 				.setIcon(R.drawable.icon).setTitle(R.string.app_name)
 				.setCancelable(false).setPositiveButton("OK..",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 							}
 						});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void show_history() {
 
 		tempArray.clear();
 		DatabaseHelper myDb = new DatabaseHelper(ctx);
 		{
 			try {
 				myDb.createDataBase();
 			} catch (IOException ioe) {
 				throw new Error("Unable to create database");
 			}
 			try {
 				myDb.openDataBase();
 			} catch (SQLException sqle) {
 				throw sqle;
 			}
 			myDb.getReadableDatabase();
 		}
 		Cursor c = myDb.getHistory();
 
 		if (c.moveToFirst()) {
 			do {
 				tempArray.add(c.getString(1) + " " + c.getString(9) + " "
 						+ c.getString(2) + " " + c.getString(3) + " "
 						+ c.getString(4) + " " + c.getString(5) + " "
 						+ c.getString(6) + " " + c.getString(7) + " "
 						+ c.getString(8) + " ");
 			} while (c.moveToNext());
 		}
 		myDb.close();
 	}
 
 	public void clear_history() {
 		final DatabaseHelper myDb = new DatabaseHelper(ctx);
 		{
 			try {
 				myDb.createDataBase();
 			} catch (IOException ioe) {
 				throw new Error("Unable to create database");
 			}
 			try {
 				myDb.openDataBase();
 			} catch (SQLException sqle) {
 				throw sqle;
 			}
 			myDb.getReadableDatabase();
 		}
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setMessage("Are you sure you want to clear the history?")
 				.setIcon(R.drawable.icon).setTitle(R.string.app_name)
 				.setCancelable(true).setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								boolean clear = myDb.clearHistory();
 								Toast.makeText(ctx,
 										"History cleared!", Toast.LENGTH_LONG).show();
 								myDb.close();
 							}
 						});
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 						myDb.close();
 					}
 				});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	public void show_list(){
 		
 		tempArray.clear();
 		DatabaseHelper myDb = new DatabaseHelper(ctx);
 		{
 			try {
 				myDb.createDataBase();
 			} catch (IOException ioe) {
 				throw new Error("Unable to create database");
 			}
 			try {
 				myDb.openDataBase();
 			} catch (SQLException sqle) {
 				throw sqle;
 			}
 			myDb.getReadableDatabase();
 		}
 		
 		Cursor c = myDb.getItems();
 		if (c.moveToFirst())
         {
             do {
             	if(c.getString(6).equalsIgnoreCase("false")){
             		tempArray.add(c.getString(0) + ": " + c.getString(1) + " "
     						+ c.getString(2) + " " + c.getString(3) + " "
     						+ c.getString(4) + " " + c.getString(5) + " "
     						+ c.getString(7) + "; ");
             	}
                 
             } while (c.moveToNext());
         }
 		
 		final Dialog dialog = new Dialog(ctx);
 
 		dialog.setTitle("aFridge");
 		
 		dialog.setContentView(R.layout.list_items_dialog);
 		
 		TextView text = (TextView) dialog.findViewById(R.id.text);
 		text.setText("Items in fridge:");
 		ImageView image = (ImageView) dialog.findViewById(R.id.image);
 		image.setImageResource(R.drawable.icon);
 		
 		ListView lv = (ListView)dialog.findViewById(R.id.inside_items);
 		lv.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, tempArray));
 		lv.setTextFilterEnabled(true);
 		
 		Button dismiss = (Button) dialog.findViewById(R.id.dismiss);
 		dismiss.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 		
 		dialog.show();
 		myDb.close();
 		
 	}
 
 }
