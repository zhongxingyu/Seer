 package tb14.walkbasehackathon;
 
 import java.util.Date;
 import java.util.List;
 
 import tb14.walkbasehackathon.Adapter.LocationAdapter;
 import tb14.walkbasehackathon.DAOs.LocationDAO;
 import tb14.walkbasehackathon.DTO.Location;
 import android.R.integer;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Tab_SavedLocations extends Fragment {
 	private LocationDAO locationDAO;
 	private ListView list;
 	private final String TAG = "Saved locations";
 	private SharedPreferences prefs;
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		final View view = inflater.inflate(R.layout.tab_savedlocations, container, false);
 		
 		prefs = (SharedPreferences) view.getContext().getSharedPreferences("WBHPrefs", Context.MODE_PRIVATE);
 
 		locationDAO = new LocationDAO(view.getContext());
 		locationDAO.open();
 		final List<Location> values = locationDAO.getAllLocations();
 
 		list = (ListView) view.findViewById(R.id.list);
 		final ArrayAdapter<Location> adapter = new LocationAdapter(view.getContext(),
 				R.layout.locationrow, values);
 		list.setAdapter(adapter);
 		
 		
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					final int position, long id) {
 				final Location location = (Location) adapter.getItem(position);
 				
 				String name = location.getName();
 				Double lat = location.getLatitude();
 				Double lon = location.getLongitude();
 				Double acc = location.getAccuracy();
 				
 				
 				// When clicked, show a toast with the TextView text
 				Log.v(TAG, "item "+position+" clicked");
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
 				builder.setTitle("Delete "+name+"?");
 				builder.setMessage("Longitude: "+lon+"\n Latitude: "+lat+"\nAccuracy: "+acc+"\n");
 				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					    locationDAO.deleteLocation(location);
 					    adapter.remove(location);
 					}
 				});
 				builder.setNegativeButton("Cancel", null);
 
 				
 				AlertDialog dialog = builder.create();
 				dialog.show();
 			}
 		});
 		
 		
 		
 		Button addLocationButton = (Button)view.findViewById(R.id.addLocationButton);
 		addLocationButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TextView locationName = (TextView)view.findViewById(R.id.location_name);
 				if (!locationName.getText().toString().equals("")) {
 					Location tmplocation = new Location();
 					tmplocation.setName( locationName.getText().toString());
 					tmplocation.setTimestamp(new Date(prefs.getLong("timestamp", 0)));
 					tmplocation.setLatitude( (double) prefs.getFloat("latitude", 0));
 					tmplocation.setLongitude( (double) prefs.getFloat("longitude", 0));
 					tmplocation.setAccuracy( (double) prefs.getLong("accuracy", 0));
 					Location location = locationDAO.createLocation(tmplocation);
 					adapter.add(location);
 					adapter.notifyDataSetChanged();
 				} else {
 					Toast.makeText(v.getContext(), "Specify a name for the location", 2000).show();
 					
 				}
 			}
 		});
 		
 
 
 		// Inflate the layout for this fragment
 		return view;
 	}
 }
