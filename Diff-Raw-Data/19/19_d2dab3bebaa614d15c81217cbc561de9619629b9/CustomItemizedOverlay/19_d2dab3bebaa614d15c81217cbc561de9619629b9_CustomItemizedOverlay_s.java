 package georeduy.client.maps;
 
 import georeduy.client.activities.R;
 import georeduy.client.util.CommonUtilities;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.widget.TextView;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {
 
 	private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
 
 	private Context context;
 
 	public CustomItemizedOverlay (Drawable defaultMarker) {
 		super(boundCenterBottom (defaultMarker));
 	}
 
 	public CustomItemizedOverlay (Drawable defaultMarker, Context context) {
 		this(defaultMarker);
 		this.context = context;
 	}
 
 	@Override
 	protected OverlayItem createItem (int i) {
 		return mapOverlays.get(i);
 	}
 
 	@Override
 	public int size() {
 		return mapOverlays.size();
 	}
 
 	// cliquear en el botn -> mostrar cuadro
 	
 	@Override
 	protected boolean onTap (int index) {
 		OverlayItem item = mapOverlays.get(index);
 		
 		AlertDialog dialog = new AlertDialog.Builder (context).create ();
 		
 		// mostrar ttulo y descripcin del sitio.
 		dialog.setTitle (item.getTitle());
 		dialog.setMessage (item.getSnippet());
 		
 		// mostrar botones
 		dialog.setButton (DialogInterface.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
 			public void onClick (DialogInterface dialog, int which) {				
 				dialog.cancel();
 			}
 		});
 		
 		dialog.show();
 
         //CommonUtilities.showAlertMessage (context, item.getTitle(), item.getSnippet());
 		
 		return true;
 	}
 
 	public void addOverlay (OverlayItem overlay) {
 		mapOverlays.add (overlay);
 		this.populate();
 	}
 	
 	public void removeOverlay(OverlayItem overlay){
 		mapOverlays.remove(overlay);
 		this.populate();
 	}
 	
 
 }
