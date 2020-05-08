 package eltoraz.pug.android;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.widget.Toast;
 
 import com.google.android.maps.MapView;
 
 /**
  * A custom overlay built on the Open Source <code>BalloonItemizedOverlay</code>.
  * @author Brian Orecchio
  * @version 0.9
  * @reference https://github.com/jgilfelt/android-mapviewballoons
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
 	 * Retrieve a specific Overlay item.
 	 * @return <code>PugOverlayItem</code> Overlay at index i
 	 */
 	@Override
 	protected PugOverlayItem createItem(int i) {
 		return m_overlays.get(i);
 	}
 
 	/**
 	 * Retrieve the number of Overlays.
 	 * @return <code>int</code> the number of Overlays in the list
 	 */
 	@Override
 	public int size() {
 		return m_overlays.size();
 	}
 	
 	/**
 	 * Add the specified overlay to the list.
 	 * @param overlay <code>PugOverlayItem</code> to be added to the list
 	 */
 	public void addOverlay(PugOverlayItem overlay) {
 	    m_overlays.add(overlay);
 	    populate();
 	}
 	
 	/**
 	 * Handle taps on a balloon. In this context, join the Game represented by
 	 *  the balloon if the user is not the owner; otherwise, open the game for
 	 *  editing.
 	 * @param index <code>int</code> index of the item whose balloon is tapped
 	 * @param item <code>PugOverlayItem</code> item whose balloon is tapped
 	 * @return true 
 	 */
 	@Override
 	protected boolean onBalloonTap(int index, PugOverlayItem item) {
 		if (item.getUser().getId() == item.getGame().getOwner().getId()) {
 			Intent intent = new Intent(c.getApplicationContext(), EditGameActivity.class);
 			intent.putExtra("user", item.getUser());
 			intent.putExtra("game", item.getGame());
 			c.startActivity(intent);
 		}
		else {
 			PugNetworkInterface.joinGame(item.getUser().getId(), item.getGame().getId());
			Toast.makeText(c, "Joined Game!", Toast.LENGTH_LONG).show();
		}
 		
 		
 		return true;
 	}	
 }
