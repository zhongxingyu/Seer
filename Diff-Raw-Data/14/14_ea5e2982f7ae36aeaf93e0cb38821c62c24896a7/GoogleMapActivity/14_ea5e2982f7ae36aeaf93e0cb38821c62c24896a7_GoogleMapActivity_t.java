 package com.chalmers.schmaps;
 
 import java.util.HashMap;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.graphics.drawable.Drawable;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class GoogleMapActivity extends MapActivity {
 
     	private LocationManager location_manager;
 	private LocationListener location_listener;
 	private List<Overlay> mapOverlays;
 	private MapItemizedOverlay overlay;
 
     	@Override
     	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 	setContentView(R.layout.activity_map); //en xml får skapas med det namnet, ändra namnet annars
 	MapView mapView = (MapView) findViewById(R.id.mapview);
 	mapView.setBuiltInZoomControls(true);
 	mapView.setSatellite(true);
 
 	mapOverlays = mapView.getOverlays();
 	Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher); 
 	//ic_launcer är bilden jag har använt, välj en annan bild och lägg i drawable och referera till den
 
 	overlay = new MapItemizedOverlay(drawable, this);
 
 	GeoPoint gp = new GeoPoint(57689262,11973805); //skapar en geopunkt med följande latitude och logitue
 		
 		
 	String s1 = "Chalmersplatsen 1";
 	String s2 = "Våning 2";
 	OverlayItem over = new OverlayItem(gp, s1, s2); //s1 och s2 visas i dialogrutan
 	overlay.addOverlay(over);
 	mapOverlays.add(overlay);
 
         /* detta är simons kod, markerar bort den än så länge
                 assignInstances();
     }
 
     private void assignInstances() {
         lectureHashMap = new HashMap();
         editButton = (Button) findViewById(R.id.edittextbutton);
         lectureEdit = (EditText) findViewById(R.id.edittextlecture);
         editButton.setOnClickListener(this);
         lectureHashMap.put("Matsalen", 42);
         showLecture = (TextView) findViewById(R.id.showLectureText);
         showLecture.setText("Hi!");
 	}
 	
 		public void onClick(View v) {
 		String lectureText = lectureEdit.getText().toString();
 		if(lectureHashMap.containsKey(lectureText))
 				{
 					showLecture.setText(lectureHashMap.get(lectureText).toString());
 				}
 		else
 			showLecture.setText("WTF?!");
 
 	}
 
 */
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_map, menu);
         return true;
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
