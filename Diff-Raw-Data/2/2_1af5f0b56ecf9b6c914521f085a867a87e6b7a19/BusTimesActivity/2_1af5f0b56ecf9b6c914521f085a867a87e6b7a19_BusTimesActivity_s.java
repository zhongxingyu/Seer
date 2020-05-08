 /*
  * Copyright 2010 Andrew De Quincey -  adq@lidskialf.net
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.text.InputFilter;
 import android.text.InputType;
 import android.text.method.DigitsKeyListener;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class BusTimesActivity extends ListActivity implements BusDataResponseListener {
 
 	private long StopCode = -1;
 	private String StopName = "";	
 	private String sorting = "";
 
 	private ProgressDialog busyDialog = null;
 	private int expectedRequestId = -1;
 
 	private static final SimpleDateFormat titleDateFormat = new SimpleDateFormat("EEE dd MMM HH:mm");
 	private static final SimpleDateFormat advanceDateFormat = new SimpleDateFormat("EEE dd MMM yyyy");
 
 	private static final String[] temporalAlarmStrings = new String[] { "Due", "5 mins away", "10 mins away" };
 	private static final int[] temporalAlarmTimeouts = new int[] { 0, 5 * 60, 10 *  60};
 
 	private static final String[] proximityAlarmStrings = new String[] { "20 metres", "50  metres", "100 metres", "250 metres", "500 metres" };
 	private static final int[] proximitylAlarmDistances= new int[] { 20, 50, 100, 200, 500};
 
 	public static void showActivity(Context context, long stopCode, String stopName) {
 		Intent i = new Intent(context, BusTimesActivity.class);
 		i.putExtra("StopCode", stopCode);
 		i.putExtra("StopName", stopName);
 		context.startActivity(i);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.bustimes);
 		registerForContextMenu(getListView());
 		
 		StopCode = getIntent().getLongExtra("StopCode", -1);
 		if (StopCode != -1)
 			findViewById(android.R.id.empty).setVisibility(View.GONE);
 
 		StopName = "";
 		CharSequence tmp = getIntent().getCharSequenceExtra("StopName");
 		if (tmp != null)
 			StopName = tmp.toString();
 		
 		LocalDBHelper db = new LocalDBHelper(this);
 		try {
 			sorting = db.getGlobalSetting("bustimesort", "arrival");
 		} finally {
 			db.close();
 		}
 		
 		update();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		busyDialog = null;
 		super.onDestroy();		
 	}
 
 	private void update() {
 		update(0, null);
 	}
 
 	private void update(int daysInAdvance, Date timeInAdvance) {
 		if (StopCode != -1) {
 			Date displayDate = timeInAdvance;
 			if (displayDate == null)
 				displayDate = new Date();
 	
 			setTitle(StopName + " (" + titleDateFormat.format(displayDate) + ")");
 			displayBusy("Getting BusStop times");
 
 			expectedRequestId = BusDataHelper.getBusTimesAsync(StopCode, daysInAdvance, timeInAdvance, this);
 		} else {
 			setTitle("Unknown BusStop");
 			findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void displayBusy(String reason) {
 		dismissBusy();
 
 		busyDialog = ProgressDialog.show(this, "", reason, true, true, new OnCancelListener() {
 			public void onCancel(DialogInterface dialog) {
 				BusTimesActivity.this.expectedRequestId = -1;
 			}
 		});
 	}
 
 	private void dismissBusy() {
 		if (busyDialog != null) {
 			try {
 				busyDialog.dismiss();
 			} catch (Throwable t) {
 			}
 			busyDialog = null;
 		}
 	}
 
 	private void hideStatusBoxes() {
 		findViewById(R.id.bustimes_nodepartures).setVisibility(View.GONE);
 		findViewById(R.id.bustimes_error).setVisibility(View.GONE);
 		findViewById(android.R.id.empty).setVisibility(View.GONE);
 	}
 
 	public void getBusTimesError(int requestId, int code, String message) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		dismissBusy();
 		hideStatusBoxes();
 
 		setListAdapter(new BusTimesAdapter(this, R.layout.bustimes_item, new ArrayList<BusTime>()));
 		findViewById(R.id.bustimes_error).setVisibility(View.VISIBLE);
 
 		new AlertDialog.Builder(this).setTitle("Error").
 			setMessage("Unable to download stop times: " + message).
 			setPositiveButton(android.R.string.ok, null).
 			show();
 	}
 
 	public void getBusTimesSuccess(int requestId, List<BusTime> busTimes) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		dismissBusy();
 		hideStatusBoxes();
 		
 		if (sorting.equalsIgnoreCase("service")) {
 			Collections.sort(busTimes, new Comparator<BusTime>() {
 				public int compare(BusTime arg0, BusTime arg1) {
 					if (arg0.baseService != arg1.baseService)
 						return arg0.baseService - arg1.baseService;
 					return arg0.service.compareTo(arg1.service);
 				}
 			});
 		} else if (sorting.equalsIgnoreCase("arrival")) {
 			Collections.sort(busTimes, new Comparator<BusTime>() {
 				public int compare(BusTime arg0, BusTime arg1) {
 					if ((arg0.arrivalAbsoluteTime != null) && (arg1.arrivalAbsoluteTime != null)) {
 						// bus data never seems to span to the next day, so this string comparison should always work
 						return arg0.arrivalAbsoluteTime.compareTo(arg1.arrivalAbsoluteTime);
 					}
 					return arg0.arrivalSortingIndex - arg1.arrivalSortingIndex;
 				}
 			});
 		}
 
 		setListAdapter(new BusTimesAdapter(this, R.layout.bustimes_item, busTimes));
 		if (busTimes.isEmpty())
 			findViewById(R.id.bustimes_nodepartures).setVisibility(View.VISIBLE);
 	}
 	
 	private void UpdateServicesList(Button b, String[] services, boolean[] selectedServices)
 	{
 		StringBuffer result = new StringBuffer();
 		for(int i=0; i< services.length; i++) {
 			if (selectedServices[i]) {
 				if (result.length() > 0)
 					result.append(", ");
 				result.append(services[i]);
 			}
 		}
 		b.setText(result);
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// get the bus stop details
 		PointTree pt = PointTree.getPointTree(this);
 		PointTree.BusStopTreeNode busStop = pt.lookupStopByStopCode((int) StopCode);
 		if (busStop == null)
 			return;
 
 		// get the list of services for this stop
 		ArrayList<String> servicesList = pt.lookupServices(busStop.getServicesMap());
 		final String[] services = servicesList.toArray(new String[servicesList.size()]);
 		final boolean[] selectedServices = new boolean[services.length];
 
 		// preselect the clicked-on service
 		TextView clickedService = (TextView) v.findViewById(R.id.bustimes_service);
 		for(int i=0; i< services.length; i++) {
 			if (clickedService.getText().toString().equalsIgnoreCase(services[i])) {
 				selectedServices[i] = true;
 				break;
 			}
 		}
 
 		// load the view
 		View dialogView = getLayoutInflater().inflate(R.layout.addtemporalalert, null);		
 
 		// setup services selector
 		final Button servicesButton = (Button) dialogView.findViewById(R.id.addtemporalalert_services);
 		BusTimesActivity.this.UpdateServicesList(servicesButton, services, selectedServices);
 		servicesButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				new AlertDialog.Builder( BusTimesActivity.this )
 	     	       .setMultiChoiceItems( services, selectedServices, new DialogInterface.OnMultiChoiceClickListener() {
 						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 							selectedServices[which] = isChecked;							
 						}
 	     	       })
 	     	       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							BusTimesActivity.this.UpdateServicesList(servicesButton, services, selectedServices);
 						}
 	     	       })
 	     	       .show();
 			}
 		});
 
 		// setup time selector
 		final Spinner timeSpinner = (Spinner) dialogView.findViewById(R.id.addtemporalalert_time);
 		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, temporalAlarmStrings);
 		timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		timeSpinner.setAdapter(timeAdapter);
 
 		// show the dialog!
 		new AlertDialog.Builder(this)
 			.setView(dialogView)
 			.setTitle("Set alarm")
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					// figure out list of services
 					ArrayList<String> selectedServicesList = new ArrayList<String>();
 					for(int i=0; i< services.length; i++) {
 						if (selectedServices[i]) {
 							selectedServicesList.add(services[i]);
 						}
 					}
 					if (selectedServicesList.size() == 0)
 						return;
 
 					// create/update an intent
 					Intent i = new Intent(BusTimesActivity.this, TemporalAlarmReceiver.class);
 					i.putExtra("StopCode", StopCode);
 					i.putExtra("StopName", StopName);
 					i.putExtra("Services", selectedServicesList.toArray(new String[selectedServicesList.size()]));
 					i.putExtra("StartTime", System.currentTimeMillis());
 					i.putExtra("TimeoutSecs", temporalAlarmTimeouts[timeSpinner.getSelectedItemPosition()]);
 					PendingIntent pi = PendingIntent.getBroadcast(BusTimesActivity.this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 					// schedule it in 10 seconds
 					AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 					am.cancel(pi);
 					am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pi);
 	
 					Toast.makeText(BusTimesActivity.this, "Alarm added!", Toast.LENGTH_SHORT).show();
 				}
 			})
 			.setNegativeButton(android.R.string.cancel, null)
 			.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.bustimes_menu, menu);
 		
         LocalDBHelper db = new LocalDBHelper(this);
         try {
         	if (db.isBookmark(StopCode)) {
         		menu.findItem(R.id.bustimes_menu_addbookmark).setEnabled(false);
         	} else {
         		menu.findItem(R.id.bustimes_menu_renamebookmark).setEnabled(false);
         		menu.findItem(R.id.bustimes_menu_deletebookmark).setEnabled(false);
         	}
         } finally {
         	db.close();
         }
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.bustimes_menu_refresh:
 			update();
 			return true;
 
 		case R.id.bustimes_menu_enterstopcode: {
 			final EditText input = new EditText(this);
 			input.setInputType(InputType.TYPE_CLASS_PHONE);
 			input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(8), new DigitsKeyListener() } );
 
 			new AlertDialog.Builder(this)
 					.setTitle("Enter BusStop code")
 					.setView(input)
 					.setPositiveButton(android.R.string.ok,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int whichButton) {
 									long stopCode = -1;
 									try {
 										stopCode = Long.parseLong(input.getText().toString());
 									} catch (Exception ex) {
 										new AlertDialog.Builder(BusTimesActivity.this)
 												.setTitle("Invalid BusStop code")
 												.setMessage("The code was invalid; please try again using only numbers")
 												.setPositiveButton(android.R.string.ok, null)
 												.show();
 										return;
 									}
 									
 									PointTree.BusStopTreeNode busStop = PointTree.getPointTree(BusTimesActivity.this).lookupStopByStopCode((int) stopCode);
 									if (busStop != null) {
 										StopCode = busStop.getStopCode();
 										StopName = busStop.getStopName();
 										update();
 									} else {
 										new AlertDialog.Builder(BusTimesActivity.this).setTitle("Error")
 											.setMessage("The code was invalid; please try again")
 											.setPositiveButton(android.R.string.ok, null)
 											.show();
 									}
 								}
 							})
 					.setNegativeButton(android.R.string.cancel, null)
 					.show();
 			return true;
 		}
 
 		case R.id.bustimes_menu_addbookmark:
 			if (StopCode != -1) {
 				LocalDBHelper db = new LocalDBHelper(this);
 				try {
 					db.addBookmark(StopCode, StopName);
 				} finally {
 					db.close();
 				}
 				Toast.makeText(this, "Added bookmark", Toast.LENGTH_SHORT).show();
 			}
 			return true;
 			
 		case R.id.bustimes_menu_renamebookmark: {
 			 final EditText input = new EditText(this);
 				input.setText(StopName);
 
 				new AlertDialog.Builder(this)
 						.setTitle("Rename bookmark")
 						.setView(input)
 						.setPositiveButton(android.R.string.ok,
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog, int whichButton) {
 				                        LocalDBHelper db = new LocalDBHelper(BusTimesActivity.this);
 				                        try {
 				                        	db.renameBookmark(BusTimesActivity.this.StopCode, input.getText().toString());
 				                        } finally {
 				                        	db.close();
 				                        }
 				                        StopName = input.getText().toString();
 				                        update();
 									}
 								})
 						.setNegativeButton(android.R.string.cancel, null)
 						.show();
 			return true;
 		}
 
 		case R.id.bustimes_menu_deletebookmark: {
 			new AlertDialog.Builder(this).
 				setMessage("Are you sure you want to delete this bookmark?").
 				setNegativeButton(android.R.string.cancel, null).
 				setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int whichButton) {
 	                    LocalDBHelper db = new LocalDBHelper(BusTimesActivity.this);
 	                    try {
 	                    	db.deleteBookmark(BusTimesActivity.this.StopCode);
 	                    } finally {
 	                    	db.close();
 	                    }
 	                    BusTimesActivity.this.update();
 	                }
 				}).
 	            show();
 			return true;
 		}
 
 /*
 		case R.id.bustimes_menu_viewonmap:
 			// FIXME: implement
 			return true;
 */
 
 		case R.id.bustimes_menu_futuredepartures:
 			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View v = vi.inflate(R.layout.futuredepartures, null);
 
 			final GregorianCalendar calendar = new GregorianCalendar();
 			final TimePicker timePicker = (TimePicker) v.findViewById(R.id.futuredepartures_time);
 			timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
 			timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
 			timePicker.setIs24HourView(true);
 
 			final Spinner datePicker = (Spinner) v.findViewById(R.id.futuredepartures_date);
 			String[] dates = new String[4];
 			for(int i=0; i < 4; i++) {
 				dates[i] = advanceDateFormat.format(calendar.getTime());
 				calendar.add(Calendar.DAY_OF_MONTH, 1);
 			}
 			calendar.add(Calendar.DAY_OF_MONTH, -4);
 			ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dates);
 			dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			datePicker.setAdapter(dateAdapter);
 
 			new AlertDialog.Builder(this)
 				.setTitle("Choose the desired date/time")
 				.setView(v)
 				.setPositiveButton(android.R.string.ok,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int whichButton) {
 								calendar.add(Calendar.DAY_OF_MONTH, datePicker.getSelectedItemPosition());
 								calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
 								calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
 								update(datePicker.getSelectedItemPosition(), calendar.getTime());
 							}
 						})
 				.setNegativeButton(android.R.string.cancel, null)
 				.show();
 			return true;
 
 		case R.id.bustimes_menu_sorting_arrival: {
 			sorting = "arrival";
 			
 			LocalDBHelper db = new LocalDBHelper(this);
 			try {
 				db.setGlobalSetting("bustimesort",  sorting);
 			} finally {
 				db.close();
 			}
 			update();
 			return true;
 		}
 
 		case R.id.bustimes_menu_sorting_service: {
 			sorting = "service";
 			
 			LocalDBHelper db = new LocalDBHelper(this);
 			try {
 				db.setGlobalSetting("bustimesort",  sorting);
 			} finally {
 				db.close();
 			}
 			update();
 			return true;
 		}
 		
 		case R.id.bustimes_menu_proximityalert: {
 			// get the bus stop details
 			PointTree pt = PointTree.getPointTree(this);
 			final PointTree.BusStopTreeNode busStop = pt.lookupStopByStopCode((int) StopCode);
 			if (busStop == null)
 				break;
 
 			// load the view
 			View dialogView = getLayoutInflater().inflate(R.layout.addproximityalert, null);		
 
 			// setup distance selector
 			final Spinner distanceSpinner = (Spinner) dialogView.findViewById(R.id.addproximityalert_distance);
 			ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, proximityAlarmStrings);
 			timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			distanceSpinner.setAdapter(timeAdapter);
 
 			// show the dialog!
 			new AlertDialog.Builder(this)
 				.setView(dialogView)
 				.setTitle("Set alarm")
 				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// create/update an intent
 						Intent i = new Intent(BusTimesActivity.this, ProximityAlarmReceiver.class);
 						i.putExtra("StopCode", StopCode);
 						i.putExtra("StopName", StopName);
 						i.putExtra("X", busStop.getX());
 						i.putExtra("Y", busStop.getY());
 						i.putExtra("Distance", proximitylAlarmDistances[distanceSpinner.getSelectedItemPosition()]);
 						PendingIntent pi = PendingIntent.getBroadcast(BusTimesActivity.this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 						LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
						lm.addProximityAlert(busStop.getY(), busStop.getX(), proximitylAlarmDistances[distanceSpinner.getSelectedItemPosition()], 60 * 60 * 1000, pi);
 		
 						Toast.makeText(BusTimesActivity.this, "Alarm added!", Toast.LENGTH_SHORT).show();
 					}
 				})
 				.setNegativeButton(android.R.string.cancel, null)
 				.show();
 		}
 		}
 
 		return false;
 	}
 
 	private class BusTimesAdapter extends ArrayAdapter<BusTime> {
 		private List<BusTime> items;
 		private int textViewResourceId;
 
 		public BusTimesAdapter(Context context, int textViewResourceId, List<BusTime> items) {
 			super(context, textViewResourceId, items);
 
 			this.textViewResourceId = textViewResourceId;
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(textViewResourceId, null);
 			}
 
 			BusTime busTime = items.get(position);
 			if (busTime != null) {
 				TextView serviceView = (TextView) v.findViewById(R.id.bustimes_service);
 				TextView destinationView = (TextView) v.findViewById(R.id.bustimes_destination);
 				TextView timeView = (TextView) v.findViewById(R.id.bustimes_time);
 
 				serviceView.setText(busTime.service);
 				
 				if (busTime.isDiverted) {
 					destinationView.setText("DIVERTED");
 					timeView.setText("");
 				} else {
 					destinationView.setText(busTime.destination);
 	
 					if (busTime.arrivalIsDue)
 						timeView.setText("Due");
 					else if (busTime.arrivalAbsoluteTime != null)
 						timeView.setText(busTime.arrivalAbsoluteTime);
 					else
 						timeView.setText(Integer.toString(busTime.arrivalMinutesLeft));
 					
 					if (busTime.arrivalEstimated)
 						timeView.setText("~" + timeView.getText());
 					}
 			}
 
 			return v;
 		}
 	}
 }
