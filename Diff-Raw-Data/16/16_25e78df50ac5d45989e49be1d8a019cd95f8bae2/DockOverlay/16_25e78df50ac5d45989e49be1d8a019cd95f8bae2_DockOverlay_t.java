 package com.concentriclivers.cyclehire;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import org.mapsforge.android.maps.ArrayItemizedOverlay;
 import org.mapsforge.android.maps.GeoPoint;
 import org.mapsforge.android.maps.ItemizedOverlay;
 import org.mapsforge.android.maps.OverlayItem;
 
 import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
 
 /**
  * Map overlay that shows the docks and the number of slots/bikes left.
  */
 public class DockOverlay extends ItemizedOverlay<DockOverlayItem>
 {
 	// These are the markers - coloured balloons with numbers in them from 0 to 9.
 	// Above 9 there is no number. 0 is red, 1 is yellow etc.
 
 	private final int NUM_MARKERS = 11;
 
 	private Drawable markers[] = new Drawable[NUM_MARKERS];
 
 	private static final int ARRAY_LIST_INITIAL_CAPACITY = 8;
 	private static final String THREAD_NAME = "DockOverlay";
 
 	private final Context context;
 	private final ArrayList<DockOverlayItem> overlayItems;
 
 
 	public DockOverlay(Context context)
 	{
 		super(null);
 		this.context = context;
 
 		// God damn it android why do I have to set the bounds myself? Never heard of sane defaults?!
 
 		for (int i = 0; i < markers.length; ++i)
 		{
 			int id = R.drawable.marker;
 			try
 			{
 				id = R.drawable.class.getField("marker_" + Integer.toString(i)).getInt(null);
 			}
 			catch (Exception e)
 			{
 			}
 			Drawable m = context.getResources().getDrawable(id);
 			m.setBounds(0, 0, m.getIntrinsicWidth(), m.getIntrinsicHeight());
 			markers[i] = m;
 		}
 
 		overlayItems = new ArrayList<DockOverlayItem>(ARRAY_LIST_INITIAL_CAPACITY);
 	}
 
 	private boolean showingBikes = true;
 
 	public void showBikes()
 	{
 		showingBikes = true;
 		synchronized (overlayItems)
 		{
 			for (int i = 0; i < overlayItems.size(); ++i)
 			{
 				int b = overlayItems.get(i).bikes;
 				if (b > NUM_MARKERS-1)
 					b = NUM_MARKERS-1;
 				overlayItems.get(i).setMarker(markers[b]);
 			}
 		}
 		populate(); // Redraws everything. Doesn't actually populate anything.
 	}
 
 	public void showSlots()
 	{
 		showingBikes = false;
 		synchronized (overlayItems)
 		{
 			for (int i = 0; i < overlayItems.size(); ++i)
 			{
 				int b = overlayItems.get(i).slots;
 				if (b > NUM_MARKERS-1)
 					b = NUM_MARKERS-1;
 				overlayItems.get(i).setMarker(markers[b]);
 			}
 		}
 		populate(); // Redraws everything. Doesn't actually populate anything.
 	}
 
	private class SortByLatitude implements Comparator<OverlayItem>
	{
		public int compare(OverlayItem o1, OverlayItem o2)
		{
			double diff = o2.getPoint().getLatitude() - o1.getPoint().getLatitude();
			return diff > 0.0 ? 1 : (diff < 0.0 ? -1 : 0);
		}
	}

 	public void updateDocks(DockSet dockSet)
 	{
 		synchronized (overlayItems)
 		{
 			overlayItems.clear();
 			for (int i = 0; i < dockSet.numDocks(); ++i)
 			{
 				double lon = dockSet.longitude(i);
 				double lat = dockSet.latitude(i);
 				int bikes = dockSet.bikesFree(i);
 				int slots = dockSet.slotsFree(i);
 
 				int b = showingBikes ? bikes : slots;
 				if (b > NUM_MARKERS-1)
 					b = NUM_MARKERS-1;
 				
 				overlayItems.add(new DockOverlayItem(new GeoPoint(lat, lon), dockSet.name(i), bikes, slots, markers[b]));
 			}

			// Sort them from bottom to top.
			Collections.sort(overlayItems, new SortByLatitude());
 		}
 		populate();
 	}
 
 	@Override
 	public String getThreadName()
 	{
 		return THREAD_NAME;
 	}
 
 	@Override
 	public int size()
 	{
 		synchronized (overlayItems)
 		{
 			return overlayItems.size();
 		}
 	}
 
 	@Override
 	protected DockOverlayItem createItem(int i)
 	{
 		synchronized (overlayItems)
 		{
 			if (i >= overlayItems.size())
 			{
 				return null;
 			}
 			return overlayItems.get(i);
 		}
 	}
 
 	@Override
 	protected boolean onTap(int index)
 	{
 	/*	synchronized (this.overlayItems)
 		{
 			OverlayItem item = this.overlayItems.get(index);
 			if (item != null)
 			{
 				Builder builder = new AlertDialog.Builder(this.context);
 				builder.setIcon(android.R.drawable.ic_menu_info_details);
 				builder.setTitle(item.getTitle());
 				builder.setMessage(item.getSnippet());
 				builder.setPositiveButton(this.internalMapView.getText(TextField.OKAY), null);
 				builder.show();
 			}
 			return true;
 		}*/
 		return true;
 	}
 }
