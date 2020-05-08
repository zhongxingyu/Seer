 package org.ph7;
 
 
 import java.util.ArrayList;
 import java.util.List;
import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;

 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
 
 public class Map extends MapActivity {
 	ArrayList<Issue> issues = null;
 	ArrayList<GeoPoint> points = null;
 	int currentIndex = 0;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
         final MapView mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         
         List<Overlay> mapOverlays = mapView.getOverlays();
         final Ph7Overlay overlay = new Ph7Overlay(this);
         boolean single = (boolean)(getIntent().getExtras().getBoolean("single"));
         if (single) {
         	int issueId = getIntent().getExtras().getInt("issue-id");
         	Issue issue = Util.getIssue (this, issueId);
         	overlay.add(issue);
         }
         else {
         	issues = Util.getIssueList(this);
             for (Issue issue : issues) {
     			overlay.add(issue);
     		}	
         }
         
         
         mapOverlays.add(overlay);
         overlay.animateToCurrent (mapView);
         
         
         ((Button)findViewById(R.id.ButtonPrevious)).setOnClickListener(new OnClickListener() {			
 			public void onClick(View view) {
 				overlay.previousIssue(mapView);
 			}
 		});
         
         ((Button)findViewById(R.id.ButtonNext)).setOnClickListener(new OnClickListener() {			
 			public void onClick(View view) {
 				overlay.nextIssue(mapView);
 			}
 		});
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 }
