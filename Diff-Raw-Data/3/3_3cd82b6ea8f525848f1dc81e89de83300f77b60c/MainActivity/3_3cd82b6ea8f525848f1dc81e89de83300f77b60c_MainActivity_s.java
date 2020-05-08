 /* 
  * MainActivity represents the first tab, where users can scan a qr code,
  * select the amount of time they want to park, and park it.  Also while
  * parked, they are able to refill time and unpark themselves.  
  * 
  * */
 
 package com.test;
 
 import android.app.ActivityGroup;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.objects.ParkInstanceObject;
 import com.objects.ParkSync;
 import com.objects.RateObject;
 import com.objects.RateResponse;
 import com.objects.SavedInfo;
 import com.objects.ServerCalls;
 import com.objects.ServerCalls.RateCallback;
 import com.objects.ServerCalls.RefillCallback;
 import com.objects.ServerCalls.UnparkCallback;
 import com.objects.ThrowDialog;
 
 /**
  * connection ready display, ping server.  
  * robust communication model.  
  * 
  * new timer, crashes on timer end.  Also, two vars in saved pref holding endtime (endTime and TIMER).  use one.  
  * erase unnecessary instance variables such as remainSeconds.  
  * 
  * by saving all info, you can resume properly.  on create, use string starttime to find out how much time is left, calculate it
  * and then use it to initiate timer.  service should still be running, to resume app and auto-unpark and stuff.  have service conduct autorefill,
  * not the timer.  do so by editing a saved string for time, a service can edit it.  
  * 
  * edittext with tags, layout similar to citrix meeting app.  you can edit the layout of the actual edittext widget.	
  * 
  * save all info to be displayed, such as email etc, and load to prevent app from looking bad if resume doesn't work.  true "re-load"
  * refill should edit usertable and history table, and then edit app.  unparking then parking is lazy programming.  
  * edit add_history.php to check if users are currently parked, if true then update else insert.
  * sqlite db for parking history
  * internet detection
  * different user  
  * pop-up tutorial
  * write find car w/ pop-up for location, find parking (nearby? in general? how to do?)
  * create managerial app
  * time zone? how is it delt with on phone?  ON PARK, SAVE PRIVATE STARTTIME DATE OBJ.
  * 
  * compatibility!!!! works on emulators but not on phone.
  * app doesn't work on 2.3, the app always quits and services dont stop.
  * OR it isn't quitting, but the resume is just making a new instance of same app each time.  possible?
  * 
  * expense handling, create database of company charge tokens, for specific user email.  
  *    if the user's expense handlingn is flagged, check db for how much company has paid.  
  *    
  * refill complete dialog cancels after a while
  * settings should have option for warning time, save info on back, the time is read and passed when starting service
  * 
  * on park, grab lat/lon and compare with what we get from server for spot, combine and store for later.
  * park only if detect internet  on the parq button, check if internet is active.  
  * 
  * DO NOT RELY ON GOOD CONNECTION.  model should be - ping server, respond okay, make change on app, confirm change on server.
  * also must consider broken connections, so app must re-send refill requests that did not go through
  * 
  * perfect information display (sizing, borders, etc)
  * INSIDE NUMBER PICKER DISPLAY should show hrs:minutes ???
  * 
  * "Share Parq" option, pulls up qr code to scan.  
  * 
  * flash light when dark
  * 
  * BOOT LOAD SERVICE RESUME
  * 
  * TimeLeftDisplay = analog timer, digital countdown (setting gives choice) 
  * Add server calls for rates.
  * 
  * INCORPORATING NUMBERPICKER:  import class, start activity for view, setContentView of dialog
  * 
  * SEcurity in authenticating with server? encrypt, salt, and ssl all communication.
  * 
  * look into city's expenses, number of parks, gauge the server costs, lay out finance to potential
  *    partners
  * */
 
 public class MainActivity extends ActivityGroup implements LocationListener {
 
 	/*Textual Display objects declared here*/
 	private TextView rate;
 	private EditText hours;
 	private EditText minutes;
 	private TextView remainHours;
 	private TextView remainMins;
 	private TextView colon;
 	private TextView price;
 	private TextView increment;
 	private TextView lotDesc;
 	private TextView spot;
 	private TextView timeHeader;
 	private TextView priceHeader;
 
 	private RelativeLayout smallTime;
 	private TextView smallColon;
 	private TextView smallHours;
 	private TextView smallMins;
 
 	/*Buttons declared here*/
 	private EditText spotNum;
 	private Button leftButton;
 	private Button rightButton;
 	private Button plusButton;
 	private Button minusButton;
 
 	/*ints used by calculations*/
 	private int totalTimeParked = 0; //in minutes
 
 	/*various Objects used declared here*/
 	private RateObject rateObj;
 	private CountDownTimer timer;
 	public ViewFlipper vf;
 	private AlertDialog alert;
 	/*final variables*/
 	public static final String SAVED_INFO = "ParqMeInfo";
     private static final int WARN_TIME = 30; //in seconds
     private static final float LOCATION_ACCURACY = 20f;
 
     private static final int DIALOG_PARKING = 1;
     private static final int DIALOG_REFILLING = 2;
     private static final int DIALOG_UNPARKING = 3;
     private static final int DIALOG_GETTING_RATE = 4;
 
 	private LocationManager locationManager;
 	private Location lastLocation;
 	private boolean goodLocation = false;
 
 	private State state;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.flipper);
 
 		//Create refill dialog which has special components
 		alert = makeRefillDialog();
 
 		//hook elements
 		rate = (TextView) findViewById(R.id.rate);
 		colon = (TextView) findViewById(R.id.colon);
 
 		remainHours = (TextView) findViewById(R.id.hours_remaining);
 		remainMins  = (TextView) findViewById(R.id.mins_remaining);
 
         smallTime = (RelativeLayout) findViewById(R.id.small_time);
         smallColon = (TextView) findViewById(R.id.small_colon);
         smallHours = (TextView) findViewById(R.id.small_hours);
         smallMins = (TextView) findViewById(R.id.small_mins);
 		
 		price = (TextView) findViewById(R.id.total_price);
 		increment = (TextView) findViewById(R.id.increment);
 		lotDesc = (TextView) findViewById(R.id.lot_description);
 		spot = (TextView) findViewById(R.id.spot);
 		timeHeader = (TextView) findViewById(R.id.time_header);
 		priceHeader = (TextView) findViewById(R.id.price_header);
 		vf = (ViewFlipper) findViewById(R.id.flipper);
 
 		final SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 
 		final OnFocusChangeListener timeListener = new OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 			    if (rateObj != null)
 			        updateDisplay(getParkMins());
 			}
 		};
 		
 		hours = (EditText) findViewById(R.id.hours);
 		hours.setInputType(InputType.TYPE_NULL);
 		hours.setOnFocusChangeListener(timeListener);
 
 		minutes = (EditText) findViewById(R.id.mins);
 		minutes.setInputType(InputType.TYPE_NULL);
 		minutes.setOnFocusChangeListener(timeListener);
 		
 		//initialize buttons and set actions
 		final Button submitButton = (Button) findViewById(R.id.submitButton);
 		spotNum = (EditText) findViewById(R.id.spot_num);
 		spotNum.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 				submitButton.setEnabled(s.length() > 0);
 			}
 			@Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 			@Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
             }
 		});
 
 		leftButton = (Button) findViewById(R.id.left_button);
 		rightButton = (Button) findViewById(R.id.right_button);
 
 		plusButton = (Button) findViewById(R.id.plus);
 		plusButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				plusTime();
 			}
 		});
 		minusButton = (Button) findViewById(R.id.minus);
 		minusButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				minusTime();
 			}
 		});
 
         final long endTime = SavedInfo.getEndTime(check);
         final long now = System.currentTimeMillis();
         if (endTime > now) {
             int seconds = (int)(endTime - now)/1000;
             if(seconds>0){
                 rateObj = SavedInfo.getRate(check);
                 switchToParkedLayout();
                 final int minimalIncrement = rateObj.getMinIncrement();
                 rate.setText(formatCents(rateObj.getDefaultRate()) + " per " + minimalIncrement + " minutes");
                 lotDesc.setText(rateObj.getDescription());
                 spot.setText("Spot # " + SavedInfo.getSpotNumber(check));
                 if (rateObj.getMinIncrement() != 0) {
                     increment.setText(rateObj.getMinIncrement() + " minute increments");
                 }
                 timer = initiateTimer(endTime, vf);
                 timer.start();
                 vf.showNext();
                 state = State.PARKING;
                 return;
             }
         }
 		switchToParkingLayout();
 		state = State.UNPARKED;
 	}
 
 	public void onSubmitClick(View view) {
         final String contents = spotNum.getText().toString();
         // contents contains string "parqme.com/p/c36/p123456" or w/e...
         final SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 
         final double lat;
         final double lon;
         if (!goodLocation) {
             // TODO: Show a loading dialog and don't do the rest of this stuff yet
             lat = 0f;
             lon = 0f;
         } else {
             lat = lastLocation.getLatitude();
             lon = lastLocation.getLongitude();
         }
         showDialog(DIALOG_GETTING_RATE);
         ServerCalls.getRateGps(contents, lat, lon, check, new RateCallback() {
             @Override
             public void onGetRateComplete(RateResponse rateResponse) {
                 removeDialog(DIALOG_GETTING_RATE);
                 if (rateResponse != null) {
                     rateObj = rateResponse.getRateObject();
 
                     // if we get the object successfully
                     if (rateResponse.getResp().equals("OK")) {
                         vf.showNext();
                         state = State.PARKING;
                         // prepare time picker for this spot
                         // parkTimePicker.setRange(mySpot.getMinIncrement(),
                         // mySpot.getMaxTime());
                         // parkTimePicker.setMinInc(mySpot.getMinIncrement());
 
                         // initialize all variables to match spot
                         final int minimalIncrement = rateObj.getMinIncrement();
                         rate.setText(formatCents(rateObj.getDefaultRate()) + " per "
                                 + minimalIncrement + " minutes");
                         lotDesc.setText(rateObj.getDescription());
                         spot.setText("Spot #" + contents);
                         if (rateObj.getMinIncrement() != 0) {
                             increment.setText(rateObj.getMinIncrement() + " minute increments");
                         }
                         // store some used info
                         SharedPreferences.Editor editor = check.edit();
                         editor.putString("code", contents);
                         editor.putFloat("lat", (float) rateObj.getLat());
                         editor.putFloat("lon", (float) rateObj.getLon());
                         editor.commit();
                         updateDisplay(minimalIncrement);
                         minutes.requestFocus();
                     } else {
                         ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                     }
                 } else {
                     ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                 }
             }
         });
     }
 
 	public void onScanClick(View view) {
         //start scan intent
         IntentIntegrator.initiateScan(MainActivity.this);
 	}
 
     @Override
     protected void onResume() {
         super.onResume();
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         if (state == State.UNPARKED) {
             startGettingLocation();
         }
     }
 
     private int getParkMins() {
 		int h, m;
 		try {
 			h = Integer.valueOf(hours.getText().toString());
 		} catch (NumberFormatException e) {
 			h = 0;
 		}
 		try {
 			m = Integer.valueOf(minutes.getText().toString());
 		} catch (NumberFormatException e) {
 			m = 0;
 		}
 		int time = h*60+m;
 		final int minIncrement = rateObj.getMinIncrement();
 		final int remainder = time % minIncrement;
 		if (remainder != 0)
 		    time += minIncrement - remainder;
 		if (time < minIncrement)
 		    return minIncrement;
 		final int maxTime = rateObj.getMaxTime();
 		if (maxTime > 0 && time > maxTime)
 		    return maxTime;
 		return time;
 	}
 
 	private void plusTime() {
 	    final int maxTime = rateObj.getMaxTime();
 	    final int minIncrement = rateObj.getMinIncrement();
 		if (hours.hasFocus()) {
 			if (maxTime > 0) {
 			    updateDisplay(Math.min(maxTime, getParkMins()+60));
 			} else {
 			    updateDisplay(getParkMins()+60);
 			}
 		} else {
 			if (maxTime > 0) {
 			    updateDisplay(Math.min(maxTime, getParkMins()+minIncrement));
 			} else {
 			    updateDisplay(getParkMins()+minIncrement);
 			}
 		}
 	}
 
 	private void minusTime() {
 	    final int minIncrement = rateObj.getMinIncrement();
 		if (hours.hasFocus()) {
 			updateDisplay(Math.max(minIncrement, getParkMins()-60));
 		} else {
 			updateDisplay(Math.max(minIncrement, getParkMins()-minIncrement));
 		}
 	}
 
 	private void switchToParkingLayout() {
         state = State.PARKING;
 	    remainHours.setVisibility(View.GONE);
 	    remainMins.setVisibility(View.GONE);
 	    smallTime.setVisibility(View.GONE);
 	    hours.setVisibility(View.VISIBLE);
 	    minutes.setVisibility(View.VISIBLE);
         if (rateObj == null) {
             hours.setText("0");
             minutes.setText("0");
         } else {
             updateDisplay(rateObj.getMinIncrement());
         }
 	    minusButton.setVisibility(View.VISIBLE);
 	    colon.setVisibility(View.VISIBLE);
 	    plusButton.setVisibility(View.VISIBLE);
 	    priceHeader.setVisibility(View.VISIBLE);
 	    price.setVisibility(View.VISIBLE);
 	    timeHeader.setText("Parking Meter");
 	    priceHeader.setText("Total");
 	    leftButton.setText("Cancel");
 	    leftButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 rateObj = null;
                 totalTimeParked = 0;
                 //return to previous view
                 vf.showPrevious();
                 state = State.UNPARKED;
             }
         });
 	    rightButton.setText("PARQ now");
 	    rightButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 park();
             }
         });
 	    minutes.requestFocus();
 	}
 
 	private void switchToParkedLayout() {
         state = State.PARKED;
 	    hours.setVisibility(View.GONE);
 	    minutes.setVisibility(View.GONE);
 	    minusButton.setVisibility(View.GONE);
 	    plusButton.setVisibility(View.GONE);
 	    smallTime.setVisibility(View.GONE);
 	    priceHeader.setVisibility(View.INVISIBLE);
 	    price.setVisibility(View.INVISIBLE);
 	    colon.setVisibility(View.VISIBLE);
 	    remainHours.setVisibility(View.VISIBLE);
 	    remainMins.setVisibility(View.VISIBLE);
 		timeHeader.setText("Time Remaining");
 		leftButton.setText("Unpark");
         leftButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 unpark();
             }
         });
         rightButton.setText("Refill");
         rightButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 switchToRefillingLayout();
             }
         });
 	}
 
 	private void switchToRefillingLayout() {
         state = State.REFILLING;
         remainHours.setVisibility(View.GONE);
         remainMins.setVisibility(View.GONE);
         smallTime.setVisibility(View.VISIBLE);
         hours.setVisibility(View.VISIBLE);
         minutes.setVisibility(View.VISIBLE);
         updateDisplay(rateObj.getMinIncrement());
         colon.setVisibility(View.VISIBLE);
         minusButton.setVisibility(View.VISIBLE);
         plusButton.setVisibility(View.VISIBLE);
         priceHeader.setVisibility(View.VISIBLE);
         price.setVisibility(View.VISIBLE);
         timeHeader.setText("Refill Time");
         priceHeader.setText("Total");
         leftButton.setText("Cancel");
         leftButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 switchToParkedLayout();
             }
         });
         rightButton.setText("Add");
         rightButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 refill(getParkMins());
             }
         });
         minutes.requestFocus();
 	}
 
 	private static boolean isLocationProviderAvailable(LocationManager locationManager, String provider) {
 		return locationManager.getProvider(provider) != null && locationManager.isProviderEnabled(provider);
 	}
 
 	private void startGettingLocation() {
 		if (isLocationProviderAvailable(locationManager, LocationManager.GPS_PROVIDER)) {
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 		} else if (isLocationProviderAvailable(locationManager, LocationManager.NETWORK_PROVIDER)) {
 			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
 		} else {
 			ThrowDialog.show(this, ThrowDialog.NO_LOCATION);
 		}
 	}
 
 	private void stopGettingLocation() {
 		locationManager.removeUpdates(this);
 	}
 
 	private AlertDialog makeRefillDialog(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 		builder.setMessage("You are almost out of time!")
 		.setCancelable(false)
 		.setPositiveButton("Refill", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 			    final int maxTime = rateObj.getMaxTime();
 				if(totalTimeParked<maxTime)
 					refill(1);
 				//refillMe(minimalIncrement);
 				else
 					ThrowDialog.show(MainActivity.this, ThrowDialog.MAX_TIME);
 				//mediaPlayer.stop();
 			}
 		})
 		.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 				//mediaPlayer.stop();
 			}
 		});
 		return builder.create();
 	}
 
     private void unpark() {
         new AlertDialog.Builder(this).setMessage("Are you sure you want to unpark?")
                 .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         final SharedPreferences check = getSharedPreferences(SAVED_INFO, 0);
                         final String parkId = SavedInfo.getParkId(check);
                         showDialog(DIALOG_UNPARKING);
                         ServerCalls.unpark(rateObj.getSpot(), parkId, check, new UnparkCallback() {
                             @Override
                             public void onUnparkComplete(boolean success) {
                                 removeDialog(DIALOG_UNPARKING);
                                 if (success) {
                                     timer.cancel();
                                     stopService(new Intent(MainActivity.this, Background.class));
 
                                     // reset ints
                                     totalTimeParked = 0;
 
                                     //SharedPreferences.Editor editor = check.edit();
                                     SavedInfo.unpark(MainActivity.this);
 
                                     switchToParkingLayout();
                                     vf.showPrevious();
                                     state = State.UNPARKED;
                                 } else {
                                     ThrowDialog.show(MainActivity.this, ThrowDialog.UNPARK_ERROR);
                                 }
                             }
                         });
                     }
                 })
                 .setNegativeButton("No", null)
                 .create().show();
     }
 
     private void park() {
         final int parkingTime = getParkMins();
         updateDisplay(parkingTime);
         final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         final TextView confirmMsg = (TextView) inflater.inflate(R.layout.confirm_msg, null);
         confirmMsg.setText(formatCents(getCostInCents(parkingTime, rateObj)));
         new AlertDialog.Builder(this)
                 .setView(confirmMsg)
                 .setTitle("Confirm payment")
                 .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         final SharedPreferences prefs = getSharedPreferences(SAVED_INFO, 0);
                         showDialog(DIALOG_PARKING);
                         ServerCalls.park(parkingTime, rateObj, prefs, new ServerCalls.ParkCallback() {
                             @Override
                             public void onParkComplete(ParkInstanceObject parkInstance) {
                                 removeDialog(DIALOG_PARKING);
                                 if (parkInstance != null) {
                                 	if(parkInstance.getSync()!= null){
                                 		String HELLO = "LOOK AT ME OMFG";
                                 	}
                                     if (parkInstance.getEndTime() > 0) {
                                         SavedInfo.park(MainActivity.this, parkInstance, rateObj);
                                         totalTimeParked += parkingTime;
                                         if (totalTimeParked == rateObj.getMaxTime()) {
                                             ThrowDialog.show(MainActivity.this,
                                                     ThrowDialog.MAX_TIME);
                                         }
                                         switchToParkedLayout();
                                         // create and start countdown// display
                                         timer = initiateTimer(parkInstance.getEndTime(), vf);
                                         timer.start();
                                         // start background
                                         startService(new Intent(MainActivity.this,Background.class));
                                     } else {
                                         ThrowDialog.show(MainActivity.this, ThrowDialog.IS_PARKED);
                                     }
                                 } else {
                                     ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                                 }
                             }
                         });
                     }
                 })
                 .setPositiveButton("Go back", null)
                 .create().show();
     }
 
 	private void refill(final int refillMinutes){
 	    updateDisplay(refillMinutes);
 	    final int maxTime = rateObj.getMaxTime();
 		//if we haven't gone past the total time we're allowed to park
 		if (maxTime <= 0 || totalTimeParked+refillMinutes <= maxTime) {
 	        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	        final TextView confirmMsg = (TextView) inflater.inflate(R.layout.confirm_msg, null);
 	        confirmMsg.setText(formatCents(getCostInCents(refillMinutes, rateObj)));
 		    new AlertDialog.Builder(this)
 		            .setView(confirmMsg)
 		            .setTitle("Confirm payment")
 		            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             final SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
                             final String parkId = SavedInfo.getParkId(check);
                             showDialog(DIALOG_REFILLING);
                             ServerCalls.refill(refillMinutes, rateObj, parkId, check, new RefillCallback() {
                                 @Override
                                 public void onRefillComplete(ParkInstanceObject refillResp) {
                                     removeDialog(DIALOG_REFILLING);
                                     if (refillResp != null && refillResp.getEndTime() > 0) {
                                         SavedInfo.park(MainActivity.this, refillResp, rateObj);
                                         switchToParkedLayout();
                                         //update the total time parked and remaining time.
                                         totalTimeParked += refillMinutes;
                                         //stop current timer, start new timer with current time + selectedNumber.
                                         //calculate new endtime and initiate timer from it.
                                         timer.cancel();
                                         timer = initiateTimer(refillResp.getEndTime(), vf);
                                         timer.start();
                                         boolean result = false;
                                         while(!result){
                                         	result = stopService(new Intent(MainActivity.this, Background.class));
                                         }
                                         startService(new Intent(MainActivity.this, Background.class));
                                         ThrowDialog.show(MainActivity.this, ThrowDialog.REFILL_DONE);
                                     } else {
                                         ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                                     }
                                 }
                             });
                         }
                     })
                     .setPositiveButton("Go back", null)
                     .create().show();
 		} else {
 		    ThrowDialog.show(MainActivity.this, ThrowDialog.MAX_TIME);
 		}
 	}
 
 	//once we scan the qr code
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		if (scanResult != null) {
 			//call server using the qr code, to get a resulting spot's info.
 			final String contents = scanResult.getContents();
 			if (contents != null) {
 				final SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 				//contents contains string "parqme.com/p/c36/p123456" or w/e...
 				showDialog(DIALOG_GETTING_RATE);
 				ServerCalls.getRateQr(contents, check, new RateCallback() {
                     @Override
                     public void onGetRateComplete(RateResponse rateResponse) {
                         if (rateResponse!=null) {
                             rateObj = rateResponse.getRateObject();
 
                             //if we get the object successfully
                             if (rateResponse.getResp().equals("OK")) {
                             	removeDialog(DIALOG_GETTING_RATE);
                                 vf.showNext();
                                 state = State.PARKING;
 
                                 final int minIncrement = rateObj.getMinIncrement();
                                 final String [] test = contents.split("http://|/");
                                 rate.setText(formatCents(rateObj.getDefaultRate()) + " per " + minIncrement + " minutes");
                                 lotDesc.setText(rateObj.getDescription());
                                 spot.setText("Spot #" + test[3]);
                                 if (rateObj.getMinIncrement() != 0) {
                                     increment.setText(rateObj.getMinIncrement() + " minute increments");
                                 }
                                 // store some used info
                                 SavedInfo.setLatLon(MainActivity.this, rateObj.getLat(), rateObj.getLon());
                                 updateDisplay(minIncrement);
                             } else {
                                 ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                             }
                         } else {
                             ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                         }
                     }
                 });
 			}
 		}
 	}
 
 	/* THIS TIMER is only used for in-app visuals.  The actual server updating and such are
 	 * done via user button clicks, and the background service we run.  */
 	private CountDownTimer initiateTimer(long endTime, final ViewFlipper myvf){
 		//creates the countdown timer
 		final long now = System.currentTimeMillis();
 		return new CountDownTimer(endTime - now, 1000){
 			//on each 1 second tick, 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				final int seconds = (int)millisUntilFinished/1000;
 				//if the time is what our warning-time is set to
 //				if(seconds==WARN_TIME && !SavedInfo.autoRefill(MainActivity.this)){
 //					//alert the user
 //					alert.show();
 //				}
 				
 				/*
 		int Dhour = time/60;
 		int Dmins = time%60;
 		if(Dmins==59){
 			//if we have 59 minutes, display h+1 and 00
 			minutes.setText("00");
 			hours.setText(String.valueOf(Dhour+1));
 		}else if(Dmins < 9){
 			minutes.setText("0"+String.valueOf(Dmins+1));
 			hours.setText(String.valueOf(Dhour));
 		}else{
 		
 			minutes.setText(String.valueOf(Dmins+1));
 			hours.setText(String.valueOf(Dhour));
 		}
 		
 		price.setText(formatCents(getCostInCents(time, rateObj)));*/
 				
 				//update remain seconds and timer.
 		        if (smallTime.getVisibility() != View.VISIBLE) {
 		        	int Dhour = seconds/3600;
 		        	int Dmin = (seconds%3600)/60;
 		        	if(Dmin==59){
 		        		//display 00 in minutes and bump hour up 1
 		        		remainMins.setText("00");
 		        		remainHours.setText(String.valueOf(Dhour+1));
 		        	}else if(Dmin<9){
 		        		//add the 0 in front of single digit minute count
 		        		remainMins.setText("0"+String.valueOf(Dmin+1));
 		        		remainHours.setText(String.valueOf(Dhour));
 		        	}else{
 		        		//simply increase minute display by 1
 		        		remainMins.setText(String.valueOf(Dmin+1));
 		        		remainHours.setText(String.valueOf(Dhour));
 		        	}
 		        	
 //                    remainHours.setText(String.valueOf(seconds / 3600));
 //                    remainMins.setText(String.valueOf((seconds % 3600) / 60));
                     flashColon();
 		        } else {
 		        	
 		        	int Dhour = seconds/3600;
 		        	int Dmin = (seconds%3600)/60;
 		        	if(Dmin==59){
 		        		//display 00 in minutes and bump hour up 1
 		        		smallMins.setText("00");
 		        		smallHours.setText(String.valueOf(Dhour+1));
 		        	}else if(Dmin<9){
 		        		//add the 0 in front of single digit minute count
 		        		smallMins.setText("0"+String.valueOf(Dmin+1));
 		        		smallHours.setText(String.valueOf(Dhour));
 		        	}else{
 		        		//simply increase minute display by 1
 		        		smallMins.setText(String.valueOf(Dmin+1));
 		        		smallHours.setText(String.valueOf(Dhour));
 		        	}
 //		            smallHours.setText(String.valueOf(seconds / 3600));
 //		            smallMins.setText(String.valueOf((seconds % 3600) / 60));
 		            flashSmallColon();
 		        }
 			}
 			//on last tick,
 			//TODO calling alert.cancel() or throwdialog when app isn't in forefront may cause crash?
 			@Override
 			public void onFinish() {
 				//timeDisplay.setText("0:00:00");
 				SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 				//if autorefill is on, refill the user minimalIncrement
 				if(SavedInfo.autoRefill(MainActivity.this)){
 					ServerCalls.refill(rateObj.getMinIncrement(), rateObj, SavedInfo.getParkId(check), check, new RefillCallback() {
                         @Override
                         public void onRefillComplete(ParkInstanceObject refillResp) {
                             if (refillResp != null && refillResp.getEndTime() > 0) {
                                 SavedInfo.park(MainActivity.this, refillResp, rateObj);
                                 switchToParkedLayout();
                                 //update the total time parked and remaining time.
                                 totalTimeParked += rateObj.getMinIncrement();
                                 //stop current timer, start new timer with current time + selectedNumber.
                                 //calculate new endtime and initiate timer from it.
                                 timer.cancel();
                                 timer = initiateTimer(refillResp.getEndTime(), vf);
                                 
                                 timer.start();
                                 boolean result = false;
                                 while(!result){
                                 	result = stopService(new Intent(MainActivity.this, Background.class));
                                 }
                                 startService(new Intent(MainActivity.this, Background.class));
                                 ThrowDialog.show(MainActivity.this, ThrowDialog.REFILL_DONE);
                             } else {
                                 ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                             }
                         }
                     });
 				}else{
 					SavedInfo.unpark(MainActivity.this);
 					//else we cancel the running out of tie dialog
 					//alert.cancel();
 					//and restore view
 					switchToParkingLayout();
 					ThrowDialog.show(MainActivity.this, ThrowDialog.TIME_OUT);
 				}
 			}
 		};
 	}
 	
 	private void syncApp(ParkSync sync){
 		rateObj = new RateObject(sync.getLat(),sync.getLon(), sync.getSpotId(),
 				sync.getMinTime(), sync.getMaxTime(), sync.getDefaultRate(), sync.getMinIncrement(), sync.getDescription());
 		
 		SavedInfo.setParkingReferenceNumber(MainActivity.this, sync.getParkingReferenceNumber());
 		state = State.PARKING;
          // prepare time picker for this spot
          //                  parkTimePicker.setRange(mySpot.getMinIncrement(),
          //                          mySpot.getMaxTime());
          //                  parkTimePicker.setMinInc(mySpot.getMinIncrement());
 
          final int minIncrement = rateObj.getMinIncrement();
          rate.setText(formatCents(rateObj.getDefaultRate()) + " per " + minIncrement + " minutes");
          lotDesc.setText(rateObj.getDescription());
          spot.setText("Spot #" + sync.getSpotNumber());
          
          if (rateObj.getMinIncrement() != 0) {
              increment.setText(rateObj.getMinIncrement() + " minute increments");
          }
          // store some used info
          SavedInfo.setLatLon(MainActivity.this, rateObj.getLat(), rateObj.getLon());
          updateDisplay(minIncrement);
 		switchToParkedLayout();
 		timer = initiateTimer(sync.getEndTime(), vf);
         timer.start();
         
 	}
 
 	//converts cents to "$ x.xx"
 	private static String formatCents(int m) {
 		final String dollars = String.valueOf(m / 100);
 		String cents = String.valueOf(m % 100);
 		if (m % 100 < 10) {
 			cents = '0' + cents;
 		}
 		return '$'+dollars+'.'+cents;
 	}
 	private static int getCostInCents(int mins, RateObject rate) {
 		//number of minutes parked, divided by the minimum increment (aka unit of time), multiplied by price per unit in cents
 		return (mins/(rate.getMinIncrement())) * rate.getDefaultRate();
 	}
 	private void updateDisplay(int time) {
 		hours.setText(String.valueOf(time/60));
 		if(time%60<10){
 			minutes.setText("0"+String.valueOf(time%60));
 		}else{
 			minutes.setText(String.valueOf(time%60));
 		}
 		price.setText(formatCents(getCostInCents(time, rateObj)));
 	}
 
 	private void flashColon() {
         if (colon.getVisibility() == View.VISIBLE) {
             colon.setVisibility(View.INVISIBLE);
         } else {
             colon.setVisibility(View.VISIBLE);
         }
 	}
 
     private void flashSmallColon() {
         if (smallColon.getVisibility() == View.VISIBLE) {
             smallColon.setVisibility(View.INVISIBLE);
         } else {
             smallColon.setVisibility(View.VISIBLE);
         }
     }
 
 	@Override
 	public void onLocationChanged(Location location) {
 		if (location != null && (lastLocation == null || location.getAccuracy() < lastLocation.getAccuracy())) {
 			lastLocation = location;
 			if (location.getAccuracy() <= LOCATION_ACCURACY) {
 				stopGettingLocation();
 				goodLocation = true;
 			}
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 	
 	@Override
     public void onPause() {
         stopGettingLocation();
         super.onPause();
     }
 	
     @Override
     public void onBackPressed() {
         if (state == State.PARKING) {
             rateObj = null;
             vf.showPrevious();
             state = State.UNPARKED;
             startGettingLocation();
         } else if (state == State.REFILLING) {
             switchToParkedLayout();
         } else {
             super.onBackPressed();
         }
     }
 
 	private enum State {
 	    UNPARKED, PARKING, PARKED, REFILLING;
 	}
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DIALOG_PARKING: {
                 final ProgressDialog dialog = new ProgressDialog(this);
                 dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                 dialog.setMessage("Parking...");
                 dialog.setIndeterminate(true);
                 dialog.setCancelable(false);
                 return dialog;
             }
             case DIALOG_REFILLING: {
                 final ProgressDialog dialog = new ProgressDialog(this);
                 dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                 dialog.setMessage("Refilling...");
                 dialog.setIndeterminate(true);
                 dialog.setCancelable(false);
                 return dialog;
             }
             case DIALOG_UNPARKING: {
                 final ProgressDialog dialog = new ProgressDialog(this);
                 dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                 dialog.setMessage("Unparking...");
                 dialog.setIndeterminate(true);
                 dialog.setCancelable(false);
                 return dialog;
             }
             case DIALOG_GETTING_RATE: {
                 final ProgressDialog dialog = new ProgressDialog(this);
                 dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                 dialog.setMessage("Looking up your spot...");
                 dialog.setIndeterminate(true);
                 dialog.setCancelable(false);
                 return dialog;
             }
             default: {
                 return super.onCreateDialog(id);
             }
         }
     }
 }
