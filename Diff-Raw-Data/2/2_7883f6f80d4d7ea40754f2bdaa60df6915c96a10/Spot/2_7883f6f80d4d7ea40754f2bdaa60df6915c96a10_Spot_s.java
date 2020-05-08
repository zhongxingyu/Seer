 package fi.action.wpoint;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 // handles all the overlay items (spots) of the map view 
public class Spot extends ItemizedOverlay {
 	private ArrayList<OverlayItem> spots = new ArrayList<OverlayItem>();
 	private Context context;
 	
 	public Spot(Drawable icon, Context context) {
 		super(boundCenterBottom(icon)); // so that the icon is drawn over the spot
 		this.context = context;
 	}
 
 	public void add(OverlayItem overlay) {
 	    spots.add(overlay);
 	    populate();
 	}
 	
 	@Override
 	protected boolean onTap(int index) {
 	  OverlayItem item = spots.get(index);
 	  AlertDialog.Builder dialog = new AlertDialog.Builder(context);
 	  dialog.setTitle(item.getTitle());
 	  //dialog.setMessage(item.getSnippet());
 	  dialog.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int which) {
 			  // TODO OPEN THE CONNECTION / SOMETHING TO GET THERE
 				
 		  }
 	  });
 	  dialog.show();
 	  return true;
 	}
 	
 	@Override
 	protected OverlayItem createItem(int i) {
 		return spots.get(i);
 	}
 
 	@Override
 	public int size() {
 		return spots.size();
 	}
 
 }
