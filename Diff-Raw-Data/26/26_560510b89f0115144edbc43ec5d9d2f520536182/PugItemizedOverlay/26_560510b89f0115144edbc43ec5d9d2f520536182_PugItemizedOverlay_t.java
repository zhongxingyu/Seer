 package eltoraz.pug.android;
 
 import java.util.ArrayList;
 
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
/**
 * An overlay to be displayed on a map.
 * @author Bill Jameson
 * @version 1.0
 * @reference http://developer.android.com/guide/tutorials/views/hello-mapview.html
 */
 @SuppressWarnings("rawtypes")
 public class PugItemizedOverlay extends ItemizedOverlay {
 	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 
 	public PugItemizedOverlay(Drawable defaultMarker) {
 		super(boundCenterBottom(defaultMarker));
 	}
 	
 	public void addOverlay(OverlayItem overlay) {
 		mOverlays.add(overlay);
 	    populate();
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
