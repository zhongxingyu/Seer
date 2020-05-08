 package inc.meh.MileageTracker;
 
 import inc.meh.MileageTracker.R;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 import android.app.Activity;
 import android.app.AlertDialog;
 //import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 //import android.graphics.Color;
 //import android.database.Cursor;
 import android.location.*;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends Activity {
 //	private static final int DIALOG_CONFIRM_DELETE_ID = 0;
 	private TextView tv;
 	private DAO dh;
 	private LocationManager mLocationManager;
 	public LocationListener mLocationListener;
 	Button buttonStop;
 	Button buttonStart;
 	Button buttonDelete;
 	Button buttonManualInsert;
 	Button buttonRetrieve;
 	Button buttonRetrieve1;
 	Button buttonExport;
 	private boolean debug=false;
 	public boolean isTracking=false;
 	int InsertStringTripId;
 	int iMinTime=3000;
 	int iMinDist=1;
 
 	final Criteria criteria = new Criteria();
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		// initialize the database
 		this.dh = new DAO(Main.this);
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		if (!mLocationManager
 				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
 			Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
 			startActivity(myIntent);
 		}
 
 
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		// criteria.setAccuracy(Criteria.ACCURACY_COARSE);
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 
 		// Delete all rows
 		buttonDelete = (Button) findViewById(R.id.button3);
 		
 		if (!debug)
 			buttonDelete.setVisibility(Button.GONE);
 		
 		buttonDelete.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				tv = (TextView) findViewById(R.id.TextView1);
 
 				dh.deleteAll();
 			}
 		});
 		// Close Delete all rows
 
 		// Start Button activity
 		buttonStart = (Button) findViewById(R.id.btnStart);
 		//buttonStart.setBackgroundColor(Color.GREEN);
 		//buttonStart.setWidth(560);
 		//buttonStart.setHeight(90);
 		
 		buttonStart.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 
 				// Perform action on click
 				tv = (TextView) findViewById(R.id.TextView1);
 
 				String locationprovider = mLocationManager.getBestProvider(
 						criteria, true);
 
 				mLocationManager.requestLocationUpdates(
 						LocationManager.GPS_PROVIDER, iMinTime, iMinDist, mLocationListener);
 				Location mLocation = mLocationManager
 						.getLastKnownLocation(locationprovider);
 
 				// Register the listener with the Location Manager to receive
 				// location updates
 				// mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,50,
 				// 1, mLocationListener);
 
 				if (mLocation != null) {
 					
 					// buttonStart.setClickable(false);
 					
 					//if (buttonStart.getText() == getResources().getString(R.string.Insert)) {
 					if (!isTracking) {
 						StartTrack(mLocation);
 						buttonStart.setBackgroundResource(R.drawable.pressed_button);
 						isTracking=true;
 						}
 					else {
 						StopTrack();
 						buttonStart.setBackgroundResource(R.drawable.nice_button);
 						isTracking=false;
 					}
 					
 					TableLayout tl=(TableLayout) findViewById(R.id.ShowDataTable);
 
 					//empty table to make sure data is not appended for each click of menu
 					tl.removeAllViews();
 					
 					} // if (mLocatoin != null) 
 					else {
 						Toast.makeText(Main.this, getResources().getString(R.string.NoGPS),	Toast.LENGTH_SHORT).show();
 				}
 			}
 
 		});
 		// Close of Start Button Activity
 
 		// Manual Insert
 		buttonManualInsert = (Button) findViewById(R.id.buttonManualInsert);
 
 		if (!debug)
 			buttonManualInsert.setVisibility(Button.GONE);
 		
 		buttonManualInsert.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Perform action on click
 				//String InsertString;
 
 				// String coordinates = dh.SelectRow("In");
 				String locationprovider = mLocationManager.getBestProvider(criteria, true);
 				Location mLocation = mLocationManager.getLastKnownLocation(locationprovider);
 				if (mLocation != null) {
 					String InsertStringInsertype = "Manual";
 					Double InsertStringLat = mLocation.getLatitude();
 					Double InsertStringLon = mLocation.getLongitude();
 					
 					double[] calcedDist = CalculateDistance(InsertStringLat, InsertStringLon);
 					
 					// String InsertString = InsertStringInsertype + ",'" +
 					// InsertStringLat + "','" + InsertStringLon + "','" +
 					// dist2Prev + "'," + cumDist;
 					double dist2Prev = calcedDist[0];
 					double dcumDist = calcedDist[1];
 					
 					//InsertString = InsertStringInsertype + "," + InsertStringLat + "," + InsertStringLon + "," + dist2Prev + "," + dcumDist;
 					// Toast.makeText(Main.this, InsertString, Toast.LENGTH_SHORT).show();
 					//  dh.insert(InsertString, InsertStringLat, InsertStringLon,dist2Prev, dcumDist);  - causing duplicate string insertion into 1 row
 					
 					dh.insert(InsertStringTripId,InsertStringInsertype, InsertStringLat, InsertStringLon,dist2Prev, dcumDist);
 
 					//Toast.makeText(Main.this, InsertString, Toast.LENGTH_SHORT).show();
 					
 				} else {
 					Toast.makeText(
 							Main.this,
 							"I can't insert this location because I don't know where you are.",
 							Toast.LENGTH_SHORT).show();
 				}
 
 			}
 
 		});
 		// Close Manual Insert
 
 		// Retrieve1 button
 		buttonRetrieve1 = (Button) findViewById(R.id.button4);
 		
 		if (!debug)
 			buttonRetrieve1.setVisibility(Button.GONE);
 		
 		buttonRetrieve1.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				List<String> OneRow = dh.selectOneRow("");
 				if (OneRow.isEmpty()) {
 					tv.setText("There are no coordinate entries");
 				}
 				else {
 					tv.setText(OneRow.toString());
 				}
 			}
 		});
 
 		// Export button
 		buttonExport = (Button) findViewById(R.id.btnExport);
 		
 		if (!debug)
 			buttonExport.setVisibility(Button.GONE);
 		
 		buttonExport.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				
 				Toast.makeText(Main.this, "Export", Toast.LENGTH_SHORT).show();
 				
 				Intent myIntent = new Intent(v.getContext(), Email.class);
                 startActivityForResult(myIntent, 0);
 
 			}
 		});
 		
 
 		
 		// Stop location updates
 		// buttonStop = (Button) findViewById(R.id.buttonStop1);
 /*		buttonStop.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				buttonStop.setClickable(false);
 				buttonStart.setClickable(true);
 
 				String sStartLat = "";
 				String sStartLong = "";
 
 				String sStopLat = "";
 				String sStopLong = "";
 				//mLocationManager.removeUpdates(mLocationListener);  // try moving this further down in code
 
 				// calculate distance
 				tv.setText("calc this");
 				// get last location in database
 				double[] dLastLocation = getLastLocation();
 
 				// break out lat and long
 				double dStartLat = dLastLocation[0];
 				double dStartLong = dLastLocation[1];
 				float[] results = { 999f };
 
 				String locationprovider = mLocationManager.getBestProvider(criteria, true);
 				Location mLocation = mLocationManager.getLastKnownLocation(locationprovider);
 
 				if (mLocation != null) {
 					String InsertStringInsertype = "Stop";
 					Double InsertStringLat = mLocation.getLatitude();
 					Double InsertStringLon = mLocation.getLongitude();
 
 					Double dist2Prev = 0.0;
 					Double cumDist = 0.0;
 
 					// is the db empty?
 					if (dLastLocation != null) {
 						// double dDist2Prev = dLastLocation[2];
 						double dCumDist = dLastLocation[3];
 
 						if (dCumDist != 0) {
 							cumDist = dCumDist;
 						}
 						
 						// float[] results = {999f};
 						// get distance between here and last record in database
 						android.location.Location.distanceBetween(dStartLat,
 								dStartLong, InsertStringLat, InsertStringLon,
 								results);
 
 						double dDist2Prev = results[0];
 
 						dist2Prev = dDist2Prev;
 						cumDist += dDist2Prev;
 						mLocationManager.removeUpdates(mLocationListener);  // moved later in routine to allow for removal of requestLocationupdates
 					}
 					// show me the money!!!
 					android.location.Location.distanceBetween(dStartLat,
 							dStartLong, InsertStringLat, InsertStringLon,
 							results);
 
 					tv.setText("distanceBetween: " + results[0]);
 
 					sStopLat = InsertStringLat.toString();
 					sStopLong = InsertStringLon.toString();
 
 					sStartLat = Double.toString(dStartLat);
 					sStartLong = Double.toString(dStartLong);
 
 					tv.setText("start Lat: " + sStartLat + " start Long: "
 							+ sStartLong + "stop Lat: " + sStopLat
 							+ "stop Long: " + sStopLong);
 					tv.setText("distanceBetween: " + results[0]
 							+ "\n\nstart Lat: " + sStartLat + " start Long: "
 							+ sStartLong + "\nstop Lat: " + sStopLat
 							+ "stop Long: " + sStopLong);
 
 					String InsertString = InsertStringInsertype + ",'"
 							+ InsertStringLat + "','" + InsertStringLon + "','";
 
 					Toast.makeText(Main.this, InsertString, Toast.LENGTH_SHORT)
 							.show();
 					dh.insert(InsertStringInsertype, InsertStringLat,
 							InsertStringLon, dist2Prev, cumDist);
 
 				} else {
 					Toast.makeText(Main.this, "turn on your GPS lame-o",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		// Close of buttonStop.setOnClickListener
 	//	buttonStop.setClickable(false);
 */
 		// Retrieve button activity
 		buttonRetrieve = (Button) findViewById(R.id.button2);
 		
 		if (!debug)
 			buttonRetrieve.setVisibility(Button.GONE);
 		
 		buttonRetrieve.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				tv = (TextView) findViewById(R.id.TextView1);
 
 				List<String> names = dh.selectAll("coordinateid");
 				StringBuilder sb = new StringBuilder();
 				sb.append("Coordinates in database:\n");
 				for (String name : names) {
 					sb.append(name + ", ");
 					// sb.append("\n");
 				}
 
 				Log.d("EXAMPLE", "names size - " + names.size());
 				tv.setText(sb.toString());
 			}
 		});
 		// Close Retrieve button activity
 
 		// Define a listener that responds to location updates
 		// i.e. auto update
 		mLocationListener = new LocationListener() {
 			public void onLocationChanged(Location mlocation) {
 				// Called when a new location is found by the network location
 				// provider.
 
 				// make sure there is an active trip b4 logging to db
 
 				if (isTracking) {
 
 					String InsertStringInsertype = "Auto";
 					Double InsertStringLat = mlocation.getLatitude();
 					Double InsertStringLon = mlocation.getLongitude();
 
 					double[] calcedDist = CalculateDistance(InsertStringLat, InsertStringLon);
 					
 					// String InsertString = InsertStringInsertype + ",'" +
 					// InsertStringLat + "','" + InsertStringLon + "','" +
 					// dist2Prev + "'," + cumDist;
 					double dist2Prev = calcedDist[0];
 					double dcumDist = calcedDist[1];
 					
 					//String InsertString = InsertStringInsertype + ","
 					//		+ InsertStringLat + "," + InsertStringLon + ","
 					//		+ dist2Prev + "," + dcumDist;
 					
 					// Toast.makeText(Main.this, InsertString, Toast.LENGTH_SHORT).show();
 					//  dh.insert(InsertString, InsertStringLat, InsertStringLon,dist2Prev, dcumDist);  - causing duplicate string insertion into 1 row
 					dh.insert(InsertStringTripId,InsertStringInsertype, InsertStringLat, InsertStringLon,
 							dist2Prev, dcumDist);
 					
 					//String sCumDist= dcumDist.toString();
 					
 					tv.setTextSize(24);
 					tv.setText("Total Trip Mileage: \n" + Util.Meters2Miles(dcumDist));
 
 				}// end isTripActive()
 
 			}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 			}
 
 			public void onProviderEnabled(String provider) {
 			}
 
 			public void onProviderDisabled(String provider) {
 			}
 
 		};
 		// Register the listener with the Location Manager to receive location
 		// updates
 //		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 	//			0, 0, mLocationListener);
 
 	}   // Close onCreate
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.export_data:
 	        	ExportData();
 	        	Toast.makeText(this, "Exporting data...", Toast.LENGTH_SHORT).show();
 	        	return true;
 	        
 	        case R.id.truncate_data:
 	        	
 	        	if (isTracking){
 	        		
 	        		Toast.makeText(this, getResources().getString(R.string.TruncateWhileTripRunning), Toast.LENGTH_LONG).show();
 	        		
 	        	}
 	        	else
 	        	{
 	        		
 	        		//confirm before delete
 	        		//Toast.makeText(this, "confirm", Toast.LENGTH_SHORT).show();
 	        		
 	    	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        		builder.setMessage("Are you sure you want to remove ALL Data?")
 	        		       .setCancelable(false)
 	        		       .setTitle("Confirm Delete")
 	        		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	        		         
 	        		    	   public void onClick(DialogInterface dialog, int id) {
 	        		                //MyActivity.this.finish();
 	        		    		   
 	        		    		   TruncateData();
 	        		    		  //Toast.makeText(this, "Erasing ALL data...", Toast.LENGTH_SHORT).show();
 	        		           
 	        		    	   }
 	        		       })
 	        		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	        		           public void onClick(DialogInterface dialog, int id) {
 	        		                dialog.cancel();
 	        		           }
 	        		       });
 	        		
 	        		AlertDialog alert = builder.create();
 	        		
 	        		alert.show();
 
 	        	}
 
 	        	return true;
 	        
 	        case R.id.show_all_data:
 	        	ShowAllData();
 	            return true;
 	        
 	        case R.id.exit:
 	        	finishConfirm();
 	            return true;
 	            
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 
 		//return true;
 	}
 	
 	
 	@Override
 	public void onBackPressed() {
 	   finishConfirm();
 	}
 	
 	//@Override
 	public void finishConfirm()
 	{
 
 		if (isTracking)
 		{
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Are you sure you want to exit while a trip is running?\n\nTo use other applications, please use the Home button or select Exit and Keep Trip Running.")
 			       .setCancelable(false)
 			       .setTitle("Confirm Exit")
 			       .setPositiveButton("Exit and Stop Trip", new DialogInterface.OnClickListener() {
 			         
 			    	   public void onClick(DialogInterface dialog, int id) {
 			    		   Main.this.finish();
 			    	   }
 			       })
 			       .setNeutralButton("Exit and Keep Trip Running", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                
 			        	   //Intent homeIntent = new Intent(Intent.ACTION_MAIN);
 			        	   //homeIntent.setAction(Intent.CATEGORY_HOME);
 			        	   //Main.this.startActivity(homeIntent);
 			        	   
 			        	   onPause();
 			        	   onStop();
 			           }
 			           })
 			       .setNegativeButton("Don't Exit", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       }
 			       
 			       );
 			
 			AlertDialog alert = builder.create();
 			
 			alert.show();
 			
 			
 		}
 		else
 		{
 			//exit without prompt
 			Main.this.finish();
 		}
 		
 }
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		//save button state
 		outState.putString("ButtonState", buttonStart.getText().toString());
 		outState.putBoolean("isTracking", isTracking);
 		//outState.putString(tv., value)
 		
 		super.onSaveInstanceState(outState);
 		
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState)
 	{
 		super.onRestoreInstanceState(savedInstanceState);
 		//---retrieve the information persisted earlier---
 		//String sButtonState = savedInstanceState.getString("ButtonState");
 		isTracking = savedInstanceState.getBoolean("isTracking");
 		
 		//if (sButtonState == getResources().getString(R.string.Stop))
 		if (isTracking)
 		{
 			//buttonStart.setText(sButtonState);
 			buttonStart.setText(getResources().getString(R.string.Stop));
 			buttonStart.setBackgroundResource(R.drawable.pressed_button);
 		}
 	}
 	
 	
 		
 	/*
 	protected Dialog onCreateDialog(int id) {
 	    Dialog dialog = null;
 	    switch(id) {
 	    case 0:
 	        // Confirm delete all data
 	    	
 
     		
 	    	
 	    	
 	        break;
 	    default:
 	        dialog = null;
 	    }
 	    return dialog;
 	}
 
 	*/
 	
 
 	
 	private void ExportData()
 	{
 		
 		//String columnString =   "\"PersonName\",\"Gender\",\"Street1\",\"postOffice\",\"Age\"";
     	//String dataString   =   "currentUser,userName,gender,currentUser.street1,currentUser.postOFfice,currentUser.age";
     	
     	//String columnString ="Insertype, Latitude, Longitude, Distance To Previous, Cumulative Distance, Date Created";  
 		
 		String columnString ="Trip Number, Date Created, Distance Travelled ";
     	
     	String combinedString = columnString + "\n"; //+ dataString;
 
     	List<String> names = dh.getTripInfo("tripid");
     	
     	
 		StringBuilder sb = new StringBuilder();
 		
 		int i = 0;
 		
 		for (String name : names) {
 			 
 			sb.append(name + ", ");
 			i++;
 			
 			//end of line
 			if (i==3) {
 				sb.append("\n");
 				i=0;
 			}
 		}
 
 		combinedString += sb.toString();
 		//Log.d("EXAMPLE", "names size - " + names.size());
 		
     	File file   = null;
     	File root   = Environment.getExternalStorageDirectory();
     	if (root.canWrite()){
     	    File dir    =   new File (root.getAbsolutePath() + "/PersonData");
     	     dir.mkdirs();
     	     file   =   new File(dir, "mileage.csv");
     	     FileOutputStream out   =   null;
     	    try {
     	        out = new FileOutputStream(file);
     	        } catch (FileNotFoundException e) {
     	            e.printStackTrace();
     	        }
     	        try {
     	            out.write(combinedString.getBytes());
     	        } catch (IOException e) {
     	            e.printStackTrace();
     	        }
     	        try {
     	            out.close();
     	        } catch (IOException e) {
     	            e.printStackTrace();
     	        }
     	    }
 
     	Uri u1  =   null;
     	
     	u1  =   Uri.fromFile(file);
 
     	Intent sendIntent = new Intent(Intent.ACTION_SEND);
     	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "MileageTracker Export");
     	
     	Calendar currentDate = Calendar.getInstance();
     	  SimpleDateFormat formatter= 
     	  new SimpleDateFormat("EEE, MMM d yyyy HH:mm:ss");
     	  String dateNow = formatter.format(currentDate.getTime());
 
     	sendIntent.putExtra(Intent.EXTRA_TEXT, "Mileage Exported on: " + dateNow);
     	sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
     	sendIntent.setType("text/html");
 
     	//startActivity(sendIntent);
 /*
                   final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            
           //        emailIntent.setType("plain/text");
              
                   emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "");
            
                   emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MileageTracker Export");
            
                   //emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailtext.getText());
                   
                   //emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, emailtext.getText());
    
         //        Email.this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
         
         */
                   this.startActivity(Intent.createChooser(sendIntent, "Send mail..."));
 
 
 	}
 	
 
 
 	private void ShowAllData()
 	{
 
 		String[] columns ={"Trip ", "Date Created ", "Distance Travelled "};
 
 		TableLayout tl=(TableLayout) findViewById(R.id.ShowDataTable);
 
 		//empty table to make sure data is not appended for each click of menu
 		tl.removeAllViews();
 		
 		//add the column headers
 		TableRow trc=new TableRow(this);
 		trc.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT ));
         
 		TableLayout.LayoutParams tableRowParams=
 		  new TableLayout.LayoutParams
 		  (TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);
 
 		int leftMargin=0;
 		int topMargin=0;
 		int rightMargin=0;
 		int bottomMargin=0;
 
 		tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
 
 		trc.setLayoutParams(tableRowParams);
 		
 		for (String col : columns) {
 			TextView tvc = new TextView(this);
 			tvc.setText(col);
 			trc.addView(tvc);
 		}
 		
 		tl.addView(trc);
 		
 		int i = 0;
 
     	List<String> names = dh.getTripInfo("tripid");
 		
 		for (int row=0; row< names.size()/3; row++) {
 
 			TableRow tr=new TableRow(this);
 			tr.setLayoutParams(tableRowParams);
 			
 			for (int col=0; col<3; col++) {
 				TextView tv = new TextView(this);
 				tv.setText(names.get(i));
 				tr.addView(tv);
 				i++;
 			}
 		
 
 		tl.addView(tr);
 		}
 		
 		//removeContentView(tl);
 		//setContentView(tl);
 		//setContentView(tl.
 			
 			
 		//setContentView(tl);
 			/*
 			 sb.append(name + ", ");
 			 
 			i++;
 			
 			//end of line
 			if (i==3) {
 				sb.append("\n");
 				i=0;
 			}
 		}
 			
 			
 		List<String> names = dh.selectAll("coordinateid");
 		StringBuilder sb = new StringBuilder();
 		sb.append("Coordinates in database:\n");
 		for (String name : names) {
 			sb.append(name + ", ");
 			// sb.append("\n");
 		}
 
 	*/
 	//	combinedString += sb.toString();
 		
 		Log.d("EXAMPLE", "names size - " + names.size());
 		//tv.setText(combinedString);
 		
 		
 		
 		
 	}
 	
 	private void TruncateData()
 	{
 		dh.deleteAll();
 		ShowAllData();
 	}
 	
 	
 	
 	
 	// helper method to calculate distances
 	private double[] CalculateDistance(double InsertStringLat, double InsertStringLon) {
 		// get last location in database
 		//double[] dLastLocation = {23,45,34,56,67,78};//getLastLocation();
 		double[] dLastLocation = getLastLocation();
 
 		Double dist2Prev = 0.0;
 		Double dcumDist = 0.0;
 
 		// is the db empty?
 		if (dLastLocation != null) {
 			// break out lat and long
 			double dStartLat = dLastLocation[0];
 			double dStartLong = dLastLocation[1];
 
 			// double dDist2Prev = dLastLocation[3];
 			dcumDist = dLastLocation[3];
 
 			float[] results = { 999f };
 			// get distance between here and last record in database
 			android.location.Location.distanceBetween(dStartLat,
 					dStartLong, InsertStringLat, InsertStringLon,
 					results);
 
 			// float fDistanceBeween= results[0];
 
 			double dDist2Prev = results[0];
 
 			dist2Prev = dDist2Prev;
 			dcumDist += dDist2Prev;
 			
 		}  // end if (dLastLocation != null) 
 
 		double[] dReturn = { dist2Prev, dcumDist };
 		return dReturn;
 
 	}
 	
 	
 	// helper method to get last location in db for breadcrumbs
 	private double[] getLastLocation() {
 		String sStartLat = "";
 		String sStartLong = "";
 		List<String> dbRow = dh.selectOneRow("");
 
 		/*
 		 * StringBuilder sb = new StringBuilder(); for (String name : names) {
 		 * sb.append(name + ", "); }
 		 * 
 		 * String sFromDB=sb.toString();
 		 * 
 		 * String[] sFromDBArray=sFromDB.split(",");
 		 */
 
 		// String[] sFromDBArray=(String[]) dbRow.toArray();
 		
 		sStartLat = dbRow.get(1);// sFromDBArray[1];
 		sStartLong = dbRow.get(2);// sFromDBArray[2];
 
 		String sDist2Prev = dbRow.get(3);// sFromDBArray[3];
 		String sCumDist = dbRow.get(4);// sFromDBArray[4];
 
 		sStartLat = sStartLat.replace("'", "");
 		sStartLong = sStartLong.replace("'", "");
 		double dStartLat = Double.parseDouble(sStartLat);
 		double dStartLong = Double.parseDouble(sStartLong);
 
 		double dDist2Prev = Double.parseDouble(sDist2Prev);
 		double dCumDist = Double.parseDouble(sCumDist);
 
 		double[] dReturn = { dStartLat, dStartLong, dDist2Prev, dCumDist };
 		
 		return dReturn;
 
 	}
 	
 	private void StartTrack(Location mLocation) {
 		buttonStart.setText(getResources().getString(R.string.Stop));
 		InsertStringTripId = dh.getTripId();
 		String InsertStringInsertype = "Start";
 		Double InsertStringLat = mLocation.getLatitude();
 		Double InsertStringLon = mLocation.getLongitude();
 
 		//String InsertString = InsertStringInsertype + ",'" + InsertStringLat + "','" + InsertStringLon + "','";
 
 		//Toast.makeText(Main.this, InsertString, Toast.LENGTH_SHORT).show();
 		
 		dh.insert(InsertStringTripId,InsertStringInsertype, InsertStringLat,
 				InsertStringLon, 0.0, 0.0);
 
 		//Toast.makeText(Main.this, InsertString, Toast.LENGTH_SHORT).show();
 	}
 	
 	
 	
 	private void StopTrack() {
 		//if (buttonStart.getText() == getResources().getString(R.string.Stop)) {
 			buttonStart.setText(getResources().getString(R.string.Insert));
 			String InsertStringInsertype = "Stop";
 			
 			/*
 				String sStartLat = "";
 				String sStartLong = "";
 
 				String sStopLat = "";
 				String sStopLong = "";
 				
 				*/
 				
 				
 				//mLocationManager.removeUpdates(mLocationListener);  // try moving this further down in code
 
 				// calculate distance
 				tv.setText("calc this");
 				// get last location in database
 				double[] dLastLocation = getLastLocation();
 
 				// break out lat and long
 				double dStartLat = dLastLocation[0];
 				double dStartLong = dLastLocation[1];
 				float[] results = { 999f };
 
 				String locationprovider = mLocationManager.getBestProvider(criteria, true);
 				Location mLocation = mLocationManager.getLastKnownLocation(locationprovider);
 
 				if (mLocation != null) {
 					InsertStringInsertype = "Stop";
 					Double InsertStringLat = mLocation.getLatitude();
 					Double InsertStringLon = mLocation.getLongitude();
 
 					Double dist2Prev = 0.0;
 					Double cumDist = 0.0;
 
 					// is the db empty?
 					if (dLastLocation != null) {
 						// double dDist2Prev = dLastLocation[2];
 						double dCumDist = dLastLocation[3];
 
 						if (dCumDist != 0) {
 							cumDist = dCumDist;
 						}
 						
 						// float[] results = {999f};
 						// get distance between here and last record in database
 						android.location.Location.distanceBetween(dStartLat,
 								dStartLong, InsertStringLat, InsertStringLon,
 								results);
 
 						double dDist2Prev = results[0];
 
 						dist2Prev = dDist2Prev;
 						cumDist += dDist2Prev;
 						mLocationManager.removeUpdates(mLocationListener);  // moved later in routine to allow for removal of requestLocationupdates
 					}
 					// show me the money!!!
 					android.location.Location.distanceBetween(dStartLat, dStartLong, InsertStringLat, InsertStringLon, results);
 
 					/*
 					sStopLat = InsertStringLat.toString();
 					sStopLong = InsertStringLon.toString();
 
 					sStartLat = Double.toString(dStartLat);
 					sStartLong = Double.toString(dStartLong);
 					*/
 
 					// convert the distance numbers to miles rather then the default meters.
					tv.setText("Trip # " + InsertStringTripId + "\nTrip Distance:\n" + Util.Meters2Miles(cumDist));
 					
 					/* + " \n\tdistanceBetween: " + results[0] / 1609.344
 							+ "\n\nstart Lat: " + sStartLat + " start Long: "
 							+ sStartLong + "\nstop Lat: " + sStopLat
 							+ "stop Long: " + sStopLong);
 							*/
 
 					//String InsertString = InsertStringInsertype + ",'" + InsertStringLat + "','" + InsertStringLon + "','";
 
 					//Toast.makeText(Main.this, InsertString, Toast.LENGTH_SHORT).show();
 					
 					dh.insert(InsertStringTripId,InsertStringInsertype, InsertStringLat,
 							InsertStringLon, dist2Prev, cumDist);
 
 				} else {
 					Toast.makeText(Main.this, "turn on your GPS lame-o",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		
 		// Close of buttonStop.setOnClickListener
 	//	buttonStop.setClickable(false);
 	//}
 }
