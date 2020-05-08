 /* 
  * MainActivity represents the first tab, where users can scan a qr code,
  * select the amount of time they want to park, and park it.  Also while
  * parked, they are able to refill time and unpark themselves.  
  * 
  * */
 
 package com.test;
 
 import android.app.ActivityGroup;
 import android.app.AlertDialog;
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
 import com.objects.RateObject;
 import com.objects.RateResponse;
 import com.objects.SavedInfo;
 import com.objects.ServerCalls;
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
 	private Button submitButton;
 	private Button scanButton;
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
 
 	private LocationManager locationManager;
 	private Location lastLocation;
 	private boolean goodLocation = false;
 
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
 		submitButton = (Button) findViewById(R.id.submitButton);
 		submitButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final String contents = spotNum.getText().toString();
 				// contents contains string "parqme.com/p/c36/p123456" or w/e...
 				SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 
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
 				final RateResponse rateResponse = ServerCalls.getRateGps(contents, lat, lon, check);
 				if(rateResponse!=null){
 					rateObj = rateResponse.getRateObject();
 
 					// if we get the object successfully
 					if (rateResponse.getResp().equals("OK")) {
 						vf.showNext();
 						// prepare time picker for this spot
 						//					parkTimePicker.setRange(mySpot.getMinIncrement(),
 						//							mySpot.getMaxTime());
 						//					parkTimePicker.setMinInc(mySpot.getMinIncrement());
 
 						// initialize all variables to match spot
 						final int minimalIncrement = rateObj.getMinIncrement();
 						rate.setText(formatCents(rateObj.getDefaultRate()) + " per " + minimalIncrement + " minutes");
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
 		scanButton = (Button) findViewById(R.id.scanButton);
 		scanButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 					//start scan intent
 					IntentIntegrator.initiateScan(MainActivity.this);
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
                 spot.setText("Spot #" + rateObj.getSpot());
                 if (rateObj.getMinIncrement() != 0) {
                     increment.setText(rateObj.getMinIncrement() + " minute increments");
                 }
                 timer = initiateTimer(endTime, vf);
                 timer.start();
                 vf.showNext();
                 return;
             }
         }
 
 		switchToParkingLayout();
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		startGettingLocation();
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
 	    hours.setVisibility(View.GONE);
 	    minutes.setVisibility(View.GONE);
 	    colon.setVisibility(View.GONE);
 	    minusButton.setVisibility(View.GONE);
 	    plusButton.setVisibility(View.GONE);
 	    smallTime.setVisibility(View.GONE);
 	    priceHeader.setVisibility(View.INVISIBLE);
 	    price.setVisibility(View.INVISIBLE);
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
                         if (ServerCalls.unPark(rateObj.getSpot(), parkId, check)) {
                             timer.cancel();
                             // stopService(new Intent(MainActivity.this,
                             // Background.class));
 
                             // reset ints
                             totalTimeParked = 0;
 
                             SharedPreferences.Editor editor = check.edit();
                             SavedInfo.unpark(editor);
                             editor.commit();
 
                             switchToParkingLayout();
                             vf.showPrevious();
                         } else {
                             ThrowDialog.show(MainActivity.this, ThrowDialog.UNPARK_ERROR);
                         }
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
                         ParkInstanceObject parkInstance = ServerCalls.park(parkingTime, rateObj, prefs);
                         if(parkInstance != null){
                             if (parkInstance.getEndTime() > 0) {
                                 SavedInfo.park(MainActivity.this, parkInstance, rateObj);
                                 totalTimeParked += parkingTime;
                                 if (totalTimeParked == rateObj.getMaxTime()) {
                                     ThrowDialog.show(MainActivity.this, ThrowDialog.MAX_TIME);
                                 }
                                 switchToParkedLayout();
                                 //create and start countdown display
                                 timer = initiateTimer(parkInstance.getEndTime(), vf);
                                 timer.start();
                                 //start timer background service
                                 //startService(new Intent(MainActivity.this, Background.class).putExtra("time", remainSeconds));
                             } else {
                                 ThrowDialog.show(MainActivity.this, ThrowDialog.IS_PARKED);
                             }
                         }else{
                             ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                         }
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
                             final ParkInstanceObject refillResp = ServerCalls.refill(refillMinutes, rateObj, parkId, check);
                             if (refillResp != null && refillResp.getEndTime() > 0) {
                                 SavedInfo.park(MainActivity.this, refillResp, rateObj);
                                 //update the total time parked and remaining time.
                                 totalTimeParked += refillMinutes;
                                 //stop current timer, start new timer with current time + selectedNumber.
                                 //calculate new endtime and initiate timer from it.
                                 timer.cancel();
                                 timer = initiateTimer(refillResp.getEndTime(), vf);
                                 //stopService(new Intent(MainActivity.this, Background.class));
                                 timer.start();
                                 //startService(new Intent(MainActivity.this, Background.class).putExtra("time", remainSeconds));
                                 ThrowDialog.show(MainActivity.this, ThrowDialog.REFILL_DONE);
                             } else {
                                 ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
                             }
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
 			String contents = scanResult.getContents();
 			if (contents != null) {
 				final SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 				//contents contains string "parqme.com/p/c36/p123456" or w/e...
 				final RateResponse rateResponse = ServerCalls.getRateQr(contents, check);
 				if(rateResponse!=null){
 					rateObj = rateResponse.getRateObject();
 					
 					//if we get the object successfully
 					if(rateResponse.getResp().equals("OK")){
 						vf.showNext();
 						// prepare time picker for this spot
 						//					parkTimePicker.setRange(mySpot.getMinIncrement(),
 						//							mySpot.getMaxTime());
 						//					parkTimePicker.setMinInc(mySpot.getMinIncrement());
 
 						final int minIncrement = rateObj.getMinIncrement();
 						final String [] test = contents.split("/");
 						rate.setText(formatCents(rateObj.getDefaultRate()) + " per " + minIncrement + " minutes");
 						lotDesc.setText(rateObj.getDescription());
 						spot.setText("Spot #" + test[2]);
 						if (rateObj.getMinIncrement() != 0) {
 							increment.setText(rateObj.getMinIncrement() + " minute increments");
 						}
 						// store some used info
 						SharedPreferences.Editor editor = check.edit();
 						editor.putString("code", contents);
 						editor.putFloat("lat", (float) rateObj.getLat());
 						editor.putFloat("lon", (float) rateObj.getLon());
 						editor.commit();
 						updateDisplay(minIncrement);
 					}else{
 						ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
 					}
 				}else{
 					ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
 				}
 			} else {
 				//do nothing if the user doesn't scan and just cancels.
 			
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
 				if(seconds==WARN_TIME && !SavedInfo.autoRefill(MainActivity.this)){
 					//alert the user
 					alert.show();
 				}
 				//update remain seconds and timer.
 		        if (smallTime.getVisibility() != View.VISIBLE) {
                    remainHours.setText(String.valueOf(seconds / 360));
                    remainMins.setText(String.valueOf((seconds % 360 + 59) / 60));
                     flashColon();
 		        } else {
		            smallHours.setText(String.valueOf(seconds / 360));
		            smallMins.setText(String.valueOf((seconds % 360 + 59) / 60));
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
 				if(check.getBoolean("autoRefill", false)){
 					alert.cancel();
 					refill(1);
 				}else{
 					SavedInfo.eraseTimer(MainActivity.this);
 					//else we cancel the running out of tie dialog
 					alert.cancel();
 					//and restore view
 					switchToParkingLayout();
 					ThrowDialog.show(MainActivity.this, ThrowDialog.TIME_OUT);
 				}
 			}
 		};
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
 	//TODO:  THIN METHOD -- will become robust later.  must support varying units and different parking minutes and all day parking etc.
 	private static int getCostInCents(int mins, RateObject rate) {
 		//number of minutes parked, divided by the minimum increment (aka unit of time), multiplied by price per unit in cents
 		return (mins/(rate.getMinIncrement())) * rate.getDefaultRate();
 	}
 	private void updateDisplay(int time) {
 		hours.setText(String.valueOf(time/60));
 		minutes.setText(String.valueOf(time%60));
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
 	public void onPause(){
 		super.onPause();
 		stopGettingLocation();
 	}
 	
 	@Override
 	public void onBackPressed() {
 	//user can be on 3 different pages.  
 	
 		//if on front page, exit app
 		try{
 			rateObj.getDefaultRate();
 		}catch(Exception e){
 			super.finish();
 			finish();
 		}
 		final SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 		if(SavedInfo.isParked(check)){
 			//if on parked page, throw dialog.  
 			ThrowDialog.show(MainActivity.this, ThrowDialog.IS_PARKED);
 		}else{
 			//if on rate page, go back
 			rateObj = null;
 			vf.showPrevious();
 			startGettingLocation();
 		}
 	
 	}
 }
