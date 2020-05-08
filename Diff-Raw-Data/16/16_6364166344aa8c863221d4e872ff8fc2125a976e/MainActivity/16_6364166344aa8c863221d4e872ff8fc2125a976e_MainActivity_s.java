 /*
  	LogMyNight - Android app for logging night activities. 
  	Copyright (c) 2010 Michael Greifeneder <mikegr@gmx.net>
  
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package at.madexperts.logmynight;
 
 import static java.lang.Math.abs;
 import static java.lang.Math.pow;
 import static java.lang.Math.sqrt;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class MainActivity extends ListActivity
 {
 	
 	private final static String TAG = MainActivity.class.getName();
 	
 	
 	SQLiteDatabase db;
 	AutoCompleteTextView auto;
 	LocationHelper locationHelper;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         db = new DatabaseHelper(this).getWritableDatabase();
         locationHelper = new LocationHelper(this, db);
         
         setContentView(R.layout.main);
         
         DatabaseHelper.debug(db, "drinks");
         DatabaseHelper.debug(db, "drinklog");
         
         setUpLocation();
         
         updateList();
     }
     
     
     private void setUpLocation() {
         auto = (AutoCompleteTextView) findViewById(R.id.mainLocationAutoCompleteTextView);
         auto.setThreshold(0);
         auto.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			
 			public void onFocusChange(View v, boolean hasFocus) {
 				Log.d(TAG, "onFocusChange");
 				updateLocationAdapter();
 			}
 		});
         
         //Set to current location
         updateLocationViewAndSettings();
         
         
         
         auto.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
 			
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (event != null) {
 					Log.d(TAG, "keyCode: " + event.getKeyCode());
 				}
 				Log.d(TAG, "actionId: " + actionId);
 				Log.d(TAG, "ENTER: " + KeyEvent.KEYCODE_ENTER);
 				
 				updateLocationAdapter();
 				Location location = locationHelper.getLocation();
 				double tmplat = 0;
 				double tmplon = 0;
 				if (location != null) {
 					tmplat = location.getLatitude();
 					tmplon = location.getLongitude();
 				}
 				final double lat = tmplat;
 				final double lon = tmplon;
 				final String name = auto.getText().toString();
 				final Integer id = getLocationId(name);
 				Log.d(TAG, "Id of entered Location: " + id);
 				if ((id == null) || (id != null && ! hasLocationData(id))) {
 					Log.d(TAG, "Open dialog");
 					View view = getLayoutInflater().inflate(R.layout.yesnodialog, null);
 					final Dialog dlg = new Dialog(MainActivity.this);
 					Button yes = (Button) view.findViewById(R.id.yesNoDialogYesButton);
 					yes.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							dlg.dismiss();
 							if (id == null) {
 								long newId = insertLocation(name, lat, lon);
 								updateCurrentLocation(newId);
 							}
 							else {
 								updateLocation(id, name, lat, lon);
 								updateCurrentLocation(id);
 							}
 						}
 					});
 					Button no = (Button) view.findViewById(R.id.yesNoDialogNoButton);
 					no.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							dlg.dismiss();
 							if (id == null) {
 								long newId = insertLocation(name);
 								updateCurrentLocation(newId);
 							}
 							//else do nothing
 						}
 					});
 					
 					dlg.setTitle("Sind Sie am aktuellen Ort?");
 					dlg.setContentView(view);
 					Log.d(TAG, "Show dialog");
 					dlg.show();
 				}
 				else {
 					// id not null + has location data already
 					updateCurrentLocation(id);
 					Log.d(TAG, "Lat: " + lat + ";Long:" + lon);
 				}
 				return false;
 			}
 		});
         registerLocationChange();
     }
     
     
     private void updateLocationViewAndSettings() {
     	updateLocationViewAndSettings(locationHelper.getLocation());
     }
     private void updateLocationViewAndSettings(Location loc) {
 		Log.d(TAG, "setting location");
 		String location = locationHelper.getNearestLocation(loc);
		Log.d(TAG, "Found closed location: " + location);
        auto.setText(location);
        updateCurrentLocation(getLocationId(location));
 	}
 
 
 	private void registerLocationChange() {
     	final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
     	String provider = locationHelper.getBestProvider(lm);
     	Log.d(TAG, "Request update from location provider: " + provider);
     	lm.requestLocationUpdates(provider, 5*60*1000, 50, new LocationListener() {
 			
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				
 			}
 			
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void onLocationChanged(Location location) {
 				updateLocationViewAndSettings(location);
 			}
 		});
 		
 	}
 
 
     private void updateLocationAdapter() {
 		auto.setAdapter(locationHelper.getSortedNameAdapter());
     }
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	new MenuInflater(this).inflate(R.menu.mainmenu, menu);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     		case R.id.historyMenuItem: 
     			openHistory();
     			return true;
     		case R.id.galleryMenuItem:
     			openGallery();
     			return true;
     	}
     	// TODO Auto-generated method stub
     	return super.onOptionsItemSelected(item);
     }
     
     private void updateCurrentLocation(long id) {
     	SharedPreferences prefs = getSharedPreferences("default", MODE_PRIVATE);
 		prefs.edit().putLong("location", id);	
     }
     
     
     private Long insertLocation(String name) {
     	ContentValues cv = new ContentValues();
     	cv.put("name", name);
     	return db.insert("location", null, cv); // table, nullColumnHack, values) execSQL("INSERT INTO location (_id, name, latitude, longitude) VALUES (null, ?, null, null)", new String[] {name});
     }
     private Long insertLocation(String name, double latitude, double longitude) {
     	ContentValues cv = new ContentValues();
     	cv.put("name", name);
     	cv.put("latitude", latitude);
     	cv.put("longitude", longitude);
     	return db.insert("location", null, cv);
     }
     
     private void updateLocation(long id, String name, double lat, double lon) {
     	db.execSQL("UPDATE location SET name = ?, latitude = ?, longitude = ? WHERE _id = ?", new String[] {name, Double.toString(lat), Double.toString(lon), Long.toString(id)});
     }
     
     private Integer getLocationId(String name){
     	Cursor c = db.rawQuery("SELECT _id FROM location WHERE lower(name) = lower(?)", new String[] {name});
     	if (c.moveToFirst()) {
     		if (c.isNull(0)) {
     			return null;
     	    }
     		return c.getInt(0);
     	}
     	return null; 
     }
     
     private boolean hasLocationData(int id) {
     	Cursor c = db.rawQuery("SELECT latitude, longitude FROM location WHERE _id = ?", new String[] {Integer.toString(id)});
     	if (c.moveToFirst()) {
         	if (c.isNull(0) || c.isNull(1)) {
         		return false;
         	}
         	return true;
     	}
     	return false;
     }
     
     private void openGallery() {
 		Intent intent = new Intent(this, GalleryActivity.class);
 		startActivity(intent);
 	}
 
 	private void openHistory() {
     	Intent intent = new Intent(this, HistoryActivity.class);
     	startActivity(intent);
     }
 
     public void updateList() {
         //SELECT d._id, d.name as name, COUNT(l._id) as counter FROM drinks d LEFT OUTER JOIN drinklog l ON d._id = l.drink_id GROUP BY d._id, d.name;
         Cursor allCursor = db.rawQuery(
         		"SELECT d._id as _id, d.name as name, d.category as category, COUNT(l._id) as counter " +
         		"FROM drinks d " +
         		"LEFT OUTER JOIN drinklog l ON d._id = l.drink_id " +
         		"GROUP BY d._id, d.name", null);
         
         
         //SELECT d._id, d.name as name, COUNT(l._id) as counter FROM drinks d LEFT OUTER JOIN drinklog l ON d._id = l.drink_id GROUP BY d._id, d.name ORDER BY COUNT(l._id) DESC LIMIT 10;
         Cursor favouriteCursor = db.rawQuery(
         		"SELECT d._id as _id, d.name as name, d.category as category, COUNT(l._id) as counter " +
         		"FROM drinks d " +
         		"LEFT OUTER JOIN drinklog l ON d._id = l.drink_id " +
         		"GROUP BY d._id, d.name " +
         		"ORDER BY COUNT(l._id) DESC LIMIT 3"
         		, null);
         
         //SELECT d._id, d.name as name, COUNT(l._id) as counter FROM drinks d LEFT OUTER JOIN drinklog l ON d._id = l.drink_id GROUP BY d._id, d.name HAVING MAX(l.log_time) = (SELECT MAX(log_time) FROM drinklog);
         Cursor lastCursor = db.rawQuery(
         		"SELECT d._id as _id, d.name as name, d.category as category, COUNT(l._id) as counter " +
         		"FROM drinks d LEFT OUTER JOIN drinklog l ON d._id = l.drink_id " +
         		"GROUP BY d._id, d.name " +
         		"HAVING MAX(l.log_time) = (SELECT MAX(log_time) FROM drinklog)", null);
         
         SectionedAdapter adapter=new SectionedAdapter() {
         	protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
         		Log.d(TAG, "Caption: " + caption + " for " + index);
         		View result = convertView;
         		if (result ==null) {
         			result = getLayoutInflater().inflate(R.layout.sectionheader, parent, false);
         			TextView textView = (TextView) result.findViewById(R.id.sectionHeader);
         			textView.setText(caption);
         		}
         		return(result);
         	}
         	@Override
         	public long getItemId(int position) {
         		long value = super.getItemId(position);
         		Log.d(TAG, "itemId=" + value);
         		return value; 
         	}
         };
         
         adapter.addSection("Last", new GradientAdapter(this, "Last", lastCursor));
         adapter.addSection("Favourites", new GradientAdapter(this, "Favourites", favouriteCursor));
         adapter.addSection("All drinks", new GradientAdapter(this, "All drinks", allCursor));
         
         
         setListAdapter(adapter);
     }
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	updateList();
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
     	super.onListItemClick(l, v, position, id);
     	Log.d(TAG, "Clicked: position:" + position + ", id:" + id);
     	
     	Intent i = new Intent(this, LogActivity.class);
 		i.putExtra("drink", (int) id);
 		startActivityForResult(i, 0);
 		
     }
     
     
     @Override
     protected void onDestroy() {
     	db.close();
     	super.onDestroy();
     }
     
     
     class GradientAdapter extends SimpleCursorAdapter {
     	private String header;
     	public GradientAdapter(Context ctx, String header, Cursor cursor) {
 			super(ctx, R.layout.row, cursor, new String[] {"name", "counter", "category"}, new int[] {R.id.rowText, R.id.rowCounter, R.id.rowImage});
 			this.header = header;
 		}
     	
     	/*
     	@Override
     	
     	public void setViewImage(ImageView v, String value) {
     		Log.d(TAG, "setImageView: " + value);
     		super.setViewImage(v, getIcon(value));
     	}
     	*/
     	
     	
     	
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent) {
     		Log.d(TAG, "Requested in Gradient " + header + ":"+ position);
     		View view = super.getView(position, convertView, parent);
     		//setBackgroundResource(R.drawable.entry);
     		view.setBackgroundResource(R.drawable.entry);
     		ImageView imageView = (ImageView) view.findViewById(R.id.rowImage);
     		Cursor cursor = getCursor();
     		int category = cursor.getInt(cursor.getColumnIndex("category"));
     		imageView.setImageResource(Utilities.getIcon(category));
     		return view;
     	}
     	
     	public String getHeader() {
 			return header;
 		}
     	
     }
 }
