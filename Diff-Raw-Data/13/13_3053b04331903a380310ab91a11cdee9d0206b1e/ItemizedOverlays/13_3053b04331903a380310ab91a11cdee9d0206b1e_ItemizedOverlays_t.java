 package stetson.CTF.Game;
 
 import java.util.ArrayList;
 
 import stetson.CTF.GameCTF;
 
 import android.graphics.drawable.Drawable;
 import android.view.MotionEvent;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.Projection;
 
 public class ItemizedOverlays extends ItemizedOverlay<OverlayItem> {
 
 	// Constants: Used in the TITLE attribute of OverlayItems to identify menu control
 	public static final String OVERLAY_PLAYER = "player";
 	public static final String OVERLAY_FLAG = "flag";
 	public static final String OVERLAY_OTHER = "other";
 	
 	private GameCTF gameCTF;
 	
 	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 	
 	/**
 	 * 
 	 * @param defaultMarker
 	 * @param context
 	 */
 	public ItemizedOverlays(Drawable defaultMarker, GameCTF game) {
 		  super(defaultMarker);
 		  gameCTF = game;
 	}
 	
 	/**
 	 * Adds an overlay item to the array of overlays.
 	 * @param overlay
 	 */
 	public void addOverlay(OverlayItem overlay) {
 		
 		// Is there an empty overlay right now?
 		for(int i = 0; i < mOverlays.size(); i++) {
 			if(mOverlays.get(i).getMarker(OverlayItem.ITEM_STATE_FOCUSED_MASK) == null) {
 				mOverlays.set(i, overlay);
 				this.populate();
 				return;
 			}
 		}
 		
 		// If not, add it to the end
 	    mOverlays.add(overlay);
 	    populate();
 	}
 
 	/**
 	 * Displays an alert dialog with the title and snippet of an overlay item.
 	 * @param index of an overlay item
 	 * @return true
 	 */
 	protected boolean onTap(int index) {
 		
		if(index > (mOverlays.size() - 1)) {
			return true;
		}
		
 		OverlayItem item = mOverlays.get(index);
 
 		String id = item.getTitle();
 		String info = item.getSnippet();
 
 		int type = -1;
 		
 		if(id.equals(OVERLAY_FLAG)){
 			type = GameMenu.MENU_FLAG;
 		} else if (id.equals(OVERLAY_PLAYER)) {
 			type = GameMenu.MENU_PLAYER;
 		}
 
 		gameCTF.getGameMenu().setMenu(type, info, item.getPoint());
 
 
 		return true;
 	}
 	
 	/**
 	 * Overridden to allow for the moving of flags on ACTION_UP.
 	 */
 	public boolean onTouchEvent(MotionEvent event, MapView map) {
 		
 		
 		switch(event.getAction()) {
 		
 			case MotionEvent.ACTION_UP:
 				
 				if(gameCTF.isMovingFlag() != GameCTF.MOVING_FLAG_NONE) {
 					Projection projection = map.getProjection(); 
 					GeoPoint location = projection.fromPixels((int) event.getX(), (int) event.getY());
 					gameCTF.moveFlag(location);
 				}
 				
 				break;
 		
 		}
 		
 		return super.onTouchEvent(event, map);
 		
 	}
 
 	/**
 	 * 
 	 * @param index of an overlay item
 	 * @return the specified overlay item
 	 */
 	protected OverlayItem createItem(int i) {
 		return mOverlays.get(i);
 	}
 
 	/**
 	 * @return number of overlays in array
 	 */
 	public int size() {
 		return mOverlays.size();
 	}
 	
 	/**
 	 * Clears the overlays array list.
 	 */
 	public void clear() {
 		mOverlays.clear();
 	}
 
 }
