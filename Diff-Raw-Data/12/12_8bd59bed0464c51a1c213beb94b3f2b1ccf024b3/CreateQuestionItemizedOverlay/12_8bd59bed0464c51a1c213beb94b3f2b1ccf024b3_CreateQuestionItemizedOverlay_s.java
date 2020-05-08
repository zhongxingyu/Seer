 package com.kalidu.codeblue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.view.MotionEvent;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.kalidu.codeblue.activities.MainActivity;
 import com.kalidu.codeblue.models.User;
 
 @SuppressWarnings("rawtypes")
 public class CreateQuestionItemizedOverlay extends ItemizedOverlay{
 	private List<OverlayItem> items = new ArrayList<OverlayItem>();
 
 	public CreateQuestionItemizedOverlay(Drawable defaultMarker) {
 		super(defaultMarker);
         SharedPreferences preferences = MainActivity.getPreferences();
         User user = MainActivity.getUser();
         int latitude = user.getLatitude();
         int longitude = user.getLongitude();
         this.addItem(new GeoPoint(latitude, longitude));
 		populate();
 	}
 
 	@Override
     protected OverlayItem createItem(int i) {
 		return(items.get(i));
     }
 	
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		super.draw(canvas, mapView, shadow);
 
 //		boundCenterBottom(marker);
 	}
 
 	@Override
 	public int size() {
 		// TODO Auto-generated method stub
 		return this.items.size();
 	}
 	
 	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView){
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
			if (this.items.size() > 1){	// Already a temporary marker, so remove it
				this.items.remove(1);
			}
			CreateQuestionItemizedOverlay.this.addItem(p);
 		}
 		return false;
 		
 	}
 	
 	public void addItem(GeoPoint point){
 		this.items.add(new OverlayItem(point, "String1", "String2"));
 		populate();
 	}
 
 }
