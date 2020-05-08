 package net.djmacgyver.bgt.map;
 
 import java.util.HashMap;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.activity.Map;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 
 public class UserOverlay extends ItemizedOverlay<UserOverlayItem> implements UserOverlayItemListener {
 	private HashMap<Integer, UserOverlayItem> overlays = new HashMap<Integer, UserOverlayItem>();
 	private Map map;
 	private RelativeLayout bubble;
 	private UserOverlayItem bubbleUser;
 	private UserOverlayItemListener bubbleListener;
 
 	public UserOverlay(Drawable defaultMarker, Map map) {
 		super(boundCenter(defaultMarker));
 		populate();
 		this.map = map;
 	}
 	
 	public synchronized void addUser(UserOverlayItem user) {
 		overlays.put(user.getUserId(), user);
 		user.addListener(this);
 		populate();
 	}
 	
 	public UserOverlayItem getUser(int userId) {
 		return overlays.get(userId);
 	}
 	
 	public synchronized void removeUser(int userId) {
 		UserOverlayItem i = getUser(userId);
		i.removeListener(this);
 		overlays.remove(userId);
 		setLastFocusedIndex(-1);
 		populate();
 	}
 
 	public synchronized void reset() {
 		overlays.clear();
 		setLastFocusedIndex(-1);
 		populate();
 	}
 
 	@Override
 	protected UserOverlayItem createItem(int i) {
 		return (UserOverlayItem) overlays.values().toArray()[i];
 	}
 
 	@Override
 	public int size() {
 		return overlays.size();
 	}
 
 	@Override
 	/**
 	 * overriding for two reasons:
 	 * 1: synchronized
 	 * 2: no shadow
 	 */
 	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		super.draw(canvas, mapView, false);
 	}
 	
 	private RelativeLayout getBubble() {
 		if (bubble == null) {
 			LayoutInflater inflater = map.getLayoutInflater();
 			bubble = (RelativeLayout) inflater.inflate(R.layout.bubble, map.getMap(), false);
 			
 			ImageView bubbleClose = (ImageView) bubble.findViewById(R.id.balloon_overlay_close);
 			bubbleClose.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					//bubble.setVisibility(View.GONE);
 					resetBubble();
 				}
 			});
 		}
 		return bubble;
 	}
 	
 	private void resetBubble() {
 		map.getMap().removeView(getBubble());
 		getBubble().setVisibility(View.GONE);
 		if (bubbleListener != null) {
 			bubbleUser.removeListener(bubbleListener);
 			bubbleListener = null;
 		}
 	}
 
 	// bubble logic widely taken over from http://www.actionshrimp.com/2011/05/speech-bubble-popups-containing-a-view-for-android-mapview/
 	private void displayBubble(UserOverlayItem item) {
 		// Hide the bubble if it's already showing for another result
 		resetBubble();
 		
 		bubbleUser = item;
 
 		// Set some view content
 		TextView username = (TextView) getBubble().findViewById(R.id.username);
 		username.setText(item.getTitle());
 		
 		TextView comment = (TextView) getBubble().findViewById(R.id.comment);
 		comment.setText(item.getSnippet());
 
 		// This is the important bit - set up a LayoutParams object for
 		// positioning of the bubble.
 		// This will keep the bubble floating over the GeoPoint
 		// result.getPoint() as you move the MapView around,
 		// but you can also keep the view in the same place on the map using a
 		// different LayoutParams constructor
 		final MapView.LayoutParams params = new MapView.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
 				item.getPoint(), MapView.LayoutParams.BOTTOM_CENTER);
 
 		getBubble().setLayoutParams(params);
 
 		map.getMap().addView(getBubble());
 		// Measure the bubble so it can be placed on the map
 		map.getMap().measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
 							 MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
 		
 		final Handler h = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				params.point = (GeoPoint) msg.obj;
 				getBubble().setLayoutParams(params);
 			}
 		};
 		
 		bubbleListener = new UserOverlayItemListener() {
 			@Override
 			public void pointUpdated(GeoPoint newPoint) {
 				Message msg = new Message();
 				msg.obj = newPoint;
 				h.sendMessage(msg);
 			}
 		};
 		item.addListener(bubbleListener);
 		
 		getBubble().setVisibility(View.VISIBLE);
 
 		// Runnable to fade the bubble in when we've finished animatingTo our
 		// OverlayItem (below)
 		/*
 		Runnable r = new Runnable() {
 			public void run() {
 				getBubble().setVisibility(View.VISIBLE);
 			}
 		};
 		*/
 
 		// This projection and offset finds us a new GeoPoint slightly below the
 		// actual OverlayItem,
 		// which means the bubble will end up being centered nicely when we tap
 		// on an Item.
 		/*
 		Projection projection = map.getMap().getProjection();
 		Point p = new Point();
 
 		projection.toPixels(item.getPoint(), p);
 		p.offset(0, -(getBubble().getMeasuredHeight() / 2));
 		GeoPoint target = projection.fromPixels(p.x, p.y);
 		*/
 
 		// Move the MapView to our point, and then call the Runnable that fades
 		// in the bubble.
 		//map.getMap().getController().animateTo(target, r);
 	}
 
 	@Override
 	protected boolean onTap(int index) {
 		displayBubble(getItem(index));
 		return super.onTap(index);
 	}
 	
 	@Override
 	public void pointUpdated(GeoPoint newPoint) {
 		setLastFocusedIndex(-1);
 		populate();
 	}
 }
