 package edu.mines.alterego;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.TileOverlayOptions;
 
 public class MapActivity extends Activity {
 
 	GoogleMap map;
 	Context context = this;
 	int count = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.mapactivity);
 
 		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
 				.getMap();
 		map.setMapType(GoogleMap.MAP_TYPE_NONE);
 		TileOverlayOptions opts = new TileOverlayOptions();
 		opts.tileProvider(new CustomMapTileProvider(getAssets()));
 
 		// Creating onLongClickListener for user to add marker to map
 		map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
 
 			@Override
 			public void onMapLongClick(final LatLng position) {
 				LayoutInflater li = LayoutInflater.from(context);
 				final View v = li.inflate(R.layout.new_marker_dialog, null);
 				AlertDialog.Builder addMarker = new AlertDialog.Builder(context);
 				addMarker.setView(v);
 				addMarker.setTitle("Add Marker");
 				addMarker.setCancelable(false);
 				addMarker.setPositiveButton("Create",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// grab user input for marker name
 								EditText mTitle = (EditText) v
 										.findViewById(R.id.marker_title);
 
 								float mColor;
 
 								
 								switch(count) {
 									case 0:
 										mColor = BitmapDescriptorFactory.HUE_RED;
 										break;
 									case 1:
 										mColor = BitmapDescriptorFactory.HUE_ORANGE;
 										break;
 									case 2:
 										mColor = BitmapDescriptorFactory.HUE_YELLOW;
 										break;
 									case 3:
 										mColor = BitmapDescriptorFactory.HUE_GREEN;
 										break;
 									case 4:
 										mColor = BitmapDescriptorFactory.HUE_BLUE;
 										break;
 									case 5:
 										mColor = BitmapDescriptorFactory.HUE_CYAN;
 										break;
 									case 6:
 										mColor = BitmapDescriptorFactory.HUE_VIOLET;
 										break;
 									case 7:
 										mColor = BitmapDescriptorFactory.HUE_MAGENTA;
 										break;
 									case 8:
 										mColor = BitmapDescriptorFactory.HUE_AZURE;
 										break;
 									case 9:
 										mColor = BitmapDescriptorFactory.HUE_ROSE;
 										count = -1;
 										break;
 									default:
 										mColor = BitmapDescriptorFactory.HUE_RED;
 										break;
 								}		
 							
 
 								if (mTitle.getText().toString().equals("")) {
 									Toast name = Toast.makeText(MapActivity.this, "Required: Name", Toast.LENGTH_SHORT);
 									name.show();
 								} else {
 									map.addMarker(new MarkerOptions()
 											.title(mTitle.getText().toString())
 											.position(position)
 											.icon(BitmapDescriptorFactory
 													.defaultMarker(mColor))
 											.draggable(true));
 									count++;
 								}
 							}
 
 						});
 
 				addMarker.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.cancel();
 
 							}
 						});
 
 				AlertDialog alert = addMarker.create();
 				alert.show();
 
 			}
 		});
 
 	}
 }
