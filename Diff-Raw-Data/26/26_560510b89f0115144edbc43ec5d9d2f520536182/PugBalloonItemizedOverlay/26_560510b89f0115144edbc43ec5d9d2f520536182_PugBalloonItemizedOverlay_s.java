 package eltoraz.pug.android;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.widget.Toast;
 
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 /**
 * This class is the custom overlay built on the Open Source <code>BalloonItemizedOverlay</code>
 * @author oreccb
 *
 *
  */
 public class PugBalloonItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
 
 	private ArrayList<OverlayItem> m_overlays = new ArrayList<OverlayItem>();
 	private Context c;
 	
 	/**
 	 * Constructor 
 	 * 
 	 * @param defaultMarker
 	 * @param mapView
 	 */
 	public PugBalloonItemizedOverlay(Drawable defaultMarker, MapView mapView) {
 		super(boundCenter(defaultMarker), mapView);
 		c = mapView.getContext();
 	}
 
 	/**
 	 * gets the Overlay at index i
 	 * 
 	 * @return Overlay at index i
 	 */
 	@Override
 	protected OverlayItem createItem(int i) {
 		return m_overlays.get(i);
 	}
 
 	/**
 	 * This method returns the size of the Overlay ArrayList
 	 * 
 	 * @return integer - which is the size of the Overlay ArrayList
 	 */
 	@Override
 	public int size() {
 		return m_overlays.size();
 	}
 	
 	
 	public void addOverlay(OverlayItem overlay) {
 	    m_overlays.add(overlay);
 	    populate();
 	}
 	
 	/**
 	 * This method handles a "tap" on a balloon. 
 	 * 
 	 * @param index - The index of the item whose balloon is tapped.
 	 * @param item - The item whose balloon is tapped.
 	 * @return true 
 	 */
 	@Override
 	protected boolean onBalloonTap(int index, OverlayItem item) {
 		
 		//display the info
 		Toast.makeText(c, "onBalloonTap for overlay index " + index,
 				Toast.LENGTH_LONG).show();
 		
 		return true;
 	}
 	
 }
