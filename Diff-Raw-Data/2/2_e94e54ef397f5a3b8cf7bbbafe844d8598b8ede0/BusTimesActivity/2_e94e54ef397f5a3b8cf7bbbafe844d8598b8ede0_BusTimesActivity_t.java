 package org.redbus;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BusTimesActivity extends ListActivity implements BusDataResponseListener {
 	
 	private long StopCode = -1;
 	private String StopName = "";
 	private ProgressDialog busyDialog = null;
 	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.bustimes);
         registerForContextMenu(getListView());
 
         StopCode = getIntent().getLongExtra("StopCode", -1);
         StopName = "";
         CharSequence tmp = getIntent().getCharSequenceExtra("StopName");
         if (tmp != null)
         	StopName =  tmp.toString();
 	}
 
 	@Override
 	protected void onStart() 
 	{
 		super.onStart();		
 		Update();
 	}
 	
 	public void Update()
 	{
 		if (StopCode != -1) {
 			setTitle(StopName + " (" + dateFormat.format(new Date()) + ")");
 			BusDataHelper.GetBusTimesAsync(StopCode, this);
 			busyDialog = ProgressDialog.show(this, "Busy", "Getting BusStop times");
 			findViewById(android.R.id.empty).setVisibility(View.GONE);
 		} else {
 			setTitle("Unknown BusStop");
 			findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
 		}
 	}
 
 	public void getBusTimesError(int code, String message) {
 		if (busyDialog != null) {
 			busyDialog.dismiss();
 			busyDialog = null;
 		}
 		
 		setListAdapter(new BusTimesAdapter(this, R.layout.bustimes_item, new ArrayList<BusTime>()));
 		findViewById(R.id.bustimes_nodepartures).setVisibility(View.GONE);
 		findViewById(R.id.bustimes_error).setVisibility(View.VISIBLE);
 		findViewById(android.R.id.empty).setVisibility(View.GONE);
 
 		new AlertDialog.Builder(this).
 			setTitle("Error").
 			setMessage("Unable to download stop times: " + message).
 			setPositiveButton(android.R.string.ok, null).
 	        show();
 	}
 
 	public void getBusTimesSuccess(List<BusTime> busTimes) {
 		if (busyDialog != null) {
 			busyDialog.dismiss();
 			busyDialog = null;
 		}
 		
 		setListAdapter(new BusTimesAdapter(this, R.layout.bustimes_item, busTimes));
 		
 		findViewById(R.id.bustimes_nodepartures).setVisibility(View.GONE);
 		findViewById(R.id.bustimes_error).setVisibility(View.GONE);
 		findViewById(android.R.id.empty).setVisibility(View.GONE);
 		if (busTimes.isEmpty())
 			findViewById(R.id.bustimes_nodepartures).setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.bustimes_menu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch(item.getItemId()) {
 		case R.id.bustimes_menu_refresh:
 			Update();
 			return true;
 
 		case R.id.bustimes_menu_enterstopcode:
 			final EditText input = new EditText(this);
 
 			new AlertDialog.Builder(this)
 				.setTitle("Enter BusStop code")
 				.setView(input)
 				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {  
 						public void onClick(DialogInterface dialog, int whichButton) {
 						  String value = input.getText().toString();
 						  try {
 							long newStopCode = Long.parseLong(value);
 							busyDialog = ProgressDialog.show(BusTimesActivity.this, "Busy", "Validating BusStop code");
 							BusDataHelper.GetStopNameAsync(newStopCode, BusTimesActivity.this);
 
 						  } catch (Exception ex) {
 								new AlertDialog.Builder(BusTimesActivity.this).
 									setTitle("Invalid BusStop code").
 									setMessage("The code was invalid; please try again using only numbers").
 									setPositiveButton(android.R.string.ok, null).
 							        show();
 						  }
 						}
 					})
 				.setNegativeButton(android.R.string.cancel, null)
 				.show();
 			return true;
 
 		case R.id.bustimes_menu_addbookmark:
 			if (StopCode != -1) {
 		        LocalDBHelper db = new LocalDBHelper(this, false);
 		        try {
 			        db.AddBookmark(StopCode, StopName);
 		        } finally {
 		        	db.close();
 		        }
 		        Toast.makeText(this, "Added bookmark", Toast.LENGTH_SHORT).show();
 			}
 			return true;
 
 		case R.id.bustimes_menu_settings:
 			// FIXME: implement
 			return true;
 
 		case R.id.bustimes_menu_viewonmap:
 			// FIXME: implement
 			return true;
 		}
 		
 		return false;
 	}
 
 	public void getStopNameError(int code, String message) {
 		if (busyDialog != null) {
 			busyDialog.dismiss();
 			busyDialog = null;
 		}
 
 		new AlertDialog.Builder(this).
 			setTitle("Error").
 			setMessage("Unable to validate BusStop code: " + message).
 			setPositiveButton(android.R.string.ok, null).
 	        show();
 	}
 
 	public void getStopNameSuccess(long stopCode, String stopName) {
 		if (busyDialog != null) {
 			busyDialog.dismiss();
 			busyDialog = null;
 		}
 		
 		StopCode = stopCode;
 		StopName = stopName;
 		Update();
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
                 LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = vi.inflate(textViewResourceId, null);
             }
             
             BusTime busTime = items.get(position);
             if (busTime != null) {
                 TextView serviceView = (TextView) v.findViewById(R.id.bustimes_service);
                 TextView destinationView = (TextView) v.findViewById(R.id.bustimes_destination);
                 TextView timeView = (TextView) v.findViewById(R.id.bustimes_time);
                 TextView detailsView = (TextView) v.findViewById(R.id.bustimes_details);
                 
         		serviceView.setText(busTime.service);
             	destinationView.setText(busTime.destination);
             	
             	if (busTime.arrivalIsDue)
             		timeView.setText("Due");
             	else if (busTime.arrivalAbsoluteTime != null)
             		timeView.setText(busTime.arrivalAbsoluteTime);
             	else 
            		timeView.setText(Integer.toString(busTime.arrivalMinutesLeft));
             	if (busTime.arrivalEstimated)
             		timeView.setText("(" + timeView.getText() + ")");
                 
                 if (busTime.lowFloorBus)
                     detailsView.setText("LOWFLOOR");
             }
             
             return v;        	
         }
 	}
 }
