 // StoreMapOverlay
 
 // overlay del mapa para desplegar locales.
 
 package georeduy.client.maps;
 
 import georeduy.client.activities.StoreDetailActivity;
 import georeduy.client.activities.StoresListActivity;
 
 import java.util.ArrayList;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.widget.TextView;
 
 public class StoreMapOverlay extends ItemizedOverlay <OverlayItem> {
 
 	// items del mapa
 	private ArrayList <OverlayItem> mapItems = new ArrayList <OverlayItem>();
 
 	private Context context;
 
 	public StoreMapOverlay (Drawable defaultMarker) {
 		super(boundCenterBottom (defaultMarker));
		
		populate();
 	}
 
 	public StoreMapOverlay (Drawable defaultMarker, Context context) {
 		this(defaultMarker);
 		this.context = context;
 	}
 
 	@Override
 	protected OverlayItem createItem (int i) {
 		return mapItems.get(i);
 	}
 
 	@Override
 	public int size() {
 		return mapItems.size();
 	}
 
 	// cliquear en el botn -> mostrar cuadro
 	
 	@Override
 	protected boolean onTap (int index) {
		if (mapItems.size()>=index)
 		{
 			// obtener item
 			MapOverlayItem item = (MapOverlayItem) mapItems.get(index);
 			
 	    	// crear intent de la actividad Ver datos de un local.
 	    	Intent storeDetail = new Intent (context, StoreDetailActivity.class);
 	    	
 	    	// agregar id del local al intent
 	    	storeDetail.putExtra (StoresListActivity.EXTRA_STORE_ID, "" + item.getId ());
 	    	
 	    	// *** este prrafo en realidad no va ***
 	    	storeDetail.putExtra (StoresListActivity.STORE_ITEM_NAME, "" + item.getTitle());
 	    	storeDetail.putExtra (StoresListActivity.STORE_ITEM_ADDRESS, "" + item.getAddress());
 	    	
 	    	// ejecutar intent.
 	    	context.startActivity (storeDetail);
 		}
 		return true;
 	}
 
 	public void addOverlay (OverlayItem overlay) {
 		mapItems.add (overlay);
 		this.populate();
 	}
 	
 	public void clear() {
 		mapItems.clear();
         this.populate();
     }
 
 }
