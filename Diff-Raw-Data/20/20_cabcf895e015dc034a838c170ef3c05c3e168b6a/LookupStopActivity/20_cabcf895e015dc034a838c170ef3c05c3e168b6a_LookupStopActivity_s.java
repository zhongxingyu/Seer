 package uk.ac.cam.cl.dtg.android.time.BusTimetables;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnCreateContextMenuListener;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class LookupStopActivity extends ListActivity {
 	
 	public static final String LOOKUP_ADDR_INTENT = "uk.ac.cam.cl.dtg.android.time.BusTimetables.LookupAddrIntent";
 	protected final IntentFilter filter = new IntentFilter(LOOKUP_ADDR_INTENT);  
 	private LookupIntentReceiver receiver=new LookupIntentReceiver();  
 	private static final int SHOW_ON_MAP = 0;
 	private static final int SHOW_ARRIVALS_DEPS = 1;		
 	
 	List<BusStop> nearbyStops;
 	List<Address> foundAddresses;
 	
 	public void onCreate(Bundle icicle) {
 
 		super.onCreate(icicle);
 		
 		Preferences.openPrefs(this);
 		
 		setContentView(R.layout.lookupstopnoresults);
 
 		// When button clicked new search is started
 		
 
 		
 		registerReceiver(receiver, filter); 
 		
 	} 	
 	
 	@Override
 	protected void onResume() {
 		
 		super.onResume();
 	}
 
 	/* protected void  onListItemClick(ListView l, View v, int position, long id) {
 
 		switch(position) {
 	
 			case 0: lookupByName(); break;
 			case 1: lookupByAddress(); break;
 			case 2: lookupBySMS(); break;
 			default: break;
 		
 		}
 
 	}*/
 
 	private void searchForAddress(String addressInput) {
 
 		Geocoder gc = new Geocoder(this, Locale.UK);
 
 		try {
 			foundAddresses = gc.getFromLocationName(addressInput, 25, 50.450043, -3.559570, 53.218752, 1.889648); //Search addresses
 			Log.i("Geocode","Found addresses: "+foundAddresses);
 
 			// Find no matches?
 			if(foundAddresses.size() == 0) {
 				Toast.makeText(this, "No matches for that address.", Toast.LENGTH_LONG).show();
 				return;
 			}
 
 			// Iterate through Addresses, and make a new list to display to user
 			List<String> displayAddr = new LinkedList<String>();
 			for(Address a : foundAddresses) {
 				StringBuilder s = new StringBuilder();
 				s.append(a.getAddressLine(0));
 				
 				Log.i("AddressFormat", a.getAddressLine(1)+" "+a.getAdminArea());
 				
 				if(a.getAddressLine(1)!=null) s.append(", "+a.getAddressLine(1));
 				if(a.getAdminArea()!=null) s.append(", "+a.getAdminArea());
 				
 				displayAddr.add(s.toString());		    	  
 			}
 
 			new AlertDialog.Builder(this)
 			.setTitle("Find stops near...")
 			.setItems(displayAddr.toArray(new String[]{ }),
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					selectAddress(which);
 				}
 
 			})
 
 			.show();
 		}
 		catch (Exception e) {
 			//@todo: Show error message
 		} 
 
 	}
 
 	private void selectAddress(int i) {
 
 		Address selectedAddr = foundAddresses.get(i);
 
 		Log.i("Location",selectedAddr.getLatitude()+"/"+selectedAddr.getLongitude());
 
 		// Make a location object reflecting where the address is located (for passing to adapter)
 		Location loc = new Location("gps");
 		loc.setLatitude(selectedAddr.getLatitude());
 		loc.setLongitude(selectedAddr.getLongitude());
 
 		// Ask the database for stops nearest this address
 		DataStore db = new DataStore(this, false);
 		nearbyStops = db.findNearestStops((int)(loc.getLatitude() * 1E6), (int)(loc.getLongitude() * 1E6), 10);
 		db.finalize();
 		
 		// If we got some results...
 		if(nearbyStops.size() < 1) return;
 		
 		// ... switch to list view		
 		setContentView(R.layout.lookupresults);
 		
 		// Set the long click handler
 		//TODO: this is duplicated
 		this.getListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
 			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
 				menu.setHeaderTitle("Bus stop actions");
				MenuItem i = menu.add(0, SHOW_ON_MAP, 0, "Show on map");
				i = menu.add(0, SHOW_ARRIVALS_DEPS, 1, "Show arrivals / departures");
 			}
 		});
 		
 		// Feed results into list		
 		this.setListAdapter(new BusStopAdapter(this, nearbyStops, loc)); 
 
 	}
 
 	protected void onListItemClick(ListView l, View v, int position, long id){
 
 		super.onListItemClick(l, v, position, id);
 
 		// Get the item that was clicked
 		Object o = l.getItemAtPosition(position);
 		BusStop stop = (BusStop) o;
 
 		Log.i("Nearby","Clicked nearby stop: "+stop.getStopRef()+" "+stop.getName());
 
 		Intent i = new Intent(this, BusStopActivity.class);
 		i.putExtra("stop",(Serializable)stop);
 		
 		this.startActivity(i);
 
 		boolean haptic = Preferences.getBool("haptics", true);
 		if(haptic) {
 			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
 			vibrator.vibrate(50);
 		}
 	} 
 	
 	class LookupIntentReceiver extends BroadcastReceiver {  
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {  
 			
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			Log.i("LookupIntentReceiver","Got query: "+query);
 			searchForAddress(query);
 			
 		}
  
 	}  
 	
 	// TODO: this is duplicated in NearbyStopActivity
 	
 	public boolean onContextItemSelected(MenuItem item) {
 
 		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 
 
 		switch (item.getItemId()) {
 		case SHOW_ON_MAP:
 
 			// To get the id of the clicked item in the list use menuInfo.id
 			Log.d("ContextMenu", "Show on map");			
 
 			// Get the item that was clicked
 			Object o = this.getListAdapter().getItem(menuInfo.position);
 			BusStop stop = (BusStop) o;
 
 			// make an intent and parcel up data
 			Intent i = new Intent(MapHighlightOverlay.HIGHLIGHT_INTENT);
 			i.putExtra("stop", stop);
 			sendBroadcast(i);
 
 			break;
 
 		case SHOW_ARRIVALS_DEPS:
 
 			Log.d("ContextMenu", "Show arrivals / departures");
 			showStopInfo(menuInfo.position);
 
 		default:
 
 			return super.onContextItemSelected(item);
 
 		}
 		return true;
 	} 
 	
 	private void showStopInfo(int position) {
 
 		// Get the item that was clicked
 		Object o = this.getListAdapter().getItem(position);
 		BusStop stop = (BusStop) o;
 
 		Log.i("Nearby","Clicked nearby stop: "+stop.getStopRef()+" "+stop.getName());
 
 		Intent i = new Intent(this, BusStopActivity.class);
 		i.putExtra("stop",(Serializable)stop);
 		this.startActivityForResult(i, 0);
 
 		boolean haptic = Preferences.getBool("haptics", true);
 		if(haptic) {
 			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
 			vibrator.vibrate(50);
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		this.getListView().invalidate();
 	} 
 	
 }
