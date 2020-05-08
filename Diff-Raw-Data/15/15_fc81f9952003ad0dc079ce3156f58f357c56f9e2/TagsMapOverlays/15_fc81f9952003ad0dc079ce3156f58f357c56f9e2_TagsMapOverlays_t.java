 package net.solajpafistoj.tag.client;
 
 import java.util.ArrayList;
 
import android.content.Context;
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 //https://developers.google.com/maps/documentation/android/hello-mapview
 //https://github.com/commonsguy/cw-advandroid/blob/master/Maps/ILuvNooYawk/src/com/commonsware/android/luv/NooYawk.java
 
 public class TagsMapOverlays extends ItemizedOverlay {
 	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
 	
	public TagsMapOverlays(Context context) {
 		super(null);
		mContext = context;
 	}
 
 
 	public void addOverlay(OverlayItem overlay) {
 	    mOverlays.add(overlay);
 	    populate();
 	}
 	
 	public void draw(android.graphics.Canvas canvas,MapView mapView,boolean shadow) {
 
 			super.draw(canvas, mapView, false);
 
 	}
 	
 
 	@Override
 	protected OverlayItem createItem(int i) {
 	  return mOverlays.get(i);
 	}
 
 	@Override
 	public int size() {
 	  return mOverlays.size();
 	}
 
 }
