 package ch.eonum;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class MapItemizedOverlay extends ItemizedOverlay<OverlayItem>
 {
 	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 	Context mContext;
 
 	public MapItemizedOverlay(Drawable defaultMarker, Context context)
 	{
 		super(boundCenterBottom(defaultMarker));
 		this.mContext = context;
		populate();
 	}
 
 	public void addOverlay(OverlayItem overlay)
 	{
 		this.mOverlays.add(overlay);
 		populate();
 	}
 
 	public void clear()
 	{
 		this.mOverlays.clear();
 	}
 
 	@Override
 	protected OverlayItem createItem(int i)
 	{
 		return this.mOverlays.get(i);
 	}
 
 	@Override
 	public int size()
 	{
 		return this.mOverlays.size();
 	}
 
 	@Override
 	protected boolean onTap(int index)
 	{
 		OverlayItem item = this.mOverlays.get(index);
 		Logger.log("Pressed on Geopoint "+item.getTitle()+".");
 		
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this.mContext);
 		dialog.setTitle(item.getTitle());
 		dialog.setMessage(item.getSnippet());
 		dialog.show();
 		return true;
 	}
 }
