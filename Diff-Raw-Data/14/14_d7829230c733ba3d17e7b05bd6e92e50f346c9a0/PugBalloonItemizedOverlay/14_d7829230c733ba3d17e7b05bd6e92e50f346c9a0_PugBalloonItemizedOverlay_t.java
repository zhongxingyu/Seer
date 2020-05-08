 package eltoraz.pug.android;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.widget.Toast;
 
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 /**
  * The custom overlay built on the Open Source <code>BalloonItemizedOverlay</code>
  * @author Brian Orecchio
  * @version 0.1
  */
 public class PugBalloonItemizedOverlay extends BalloonItemizedOverlay<PugOverlayItem> {
 
 	private ArrayList<PugOverlayItem> m_overlays = new ArrayList<PugOverlayItem>();
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
 	protected PugOverlayItem createItem(int i) {
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
 	
 	/**
 	 * Adds overlay to list
 	 * 
 	 * @param overlay
 	 */
 	public void addOverlay(PugOverlayItem overlay) {
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
 	protected boolean onBalloonTap(int index, PugOverlayItem item) {
 		
 		PugNetworkInterface.joinGame(item.getUser().getId(), item.getGame().getId());
 		
		Toast.makeText(c, "Joined Game!", Toast.LENGTH_LONG).show();
		
 		return true;
 	}
 	
 }
