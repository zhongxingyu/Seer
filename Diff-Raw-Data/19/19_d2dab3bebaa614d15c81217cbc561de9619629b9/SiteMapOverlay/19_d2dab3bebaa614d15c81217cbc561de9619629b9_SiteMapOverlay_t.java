 // SiteMapOverlay
 
 // overlay del mapa para desplegar sitios.
 
 package georeduy.client.maps;
 
 import georeduy.client.activities.SiteDetailActivity;
 import georeduy.client.activities.SitesListActivity;
 
 import java.util.ArrayList;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.widget.TextView;
 
 public class SiteMapOverlay extends ItemizedOverlay <OverlayItem> {
 
 	// items del mapa
 	private ArrayList <OverlayItem> mapItems = new ArrayList <OverlayItem>();
 
 	private Context context;
 
 	public SiteMapOverlay (Drawable defaultMarker) {
 		super(boundCenterBottom (defaultMarker));
		
		populate();
 	}
 
 	public SiteMapOverlay (Drawable defaultMarker, Context context) {
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
 		if (mapItems.size()>0)
 		{
 			// obtener item
 			MapOverlayItem item = (MapOverlayItem) mapItems.get(index);
 			
 	    	// crear intent de la actividad Ver datos de un sitio.
 	    	Intent siteDetail = new Intent (context, SiteDetailActivity.class);
 	    	
 	    	// agregar id del sitio al intent
 	    	siteDetail.putExtra (SitesListActivity.EXTRA_SITE_ID, "" + item.getId ());
 	
 	    	// *** este prrafo en realidad no va ***
 	    	siteDetail.putExtra (SitesListActivity.SITE_ITEM_NAME, "" + item.getTitle());
 	    	siteDetail.putExtra (SitesListActivity.SITE_ITEM_ADDRESS, "" + item.getAddress());
 	    	
 	    	// ejecutar intent.
 	    	context.startActivity (siteDetail);
 			
 			/*
 			
 			// crear cuadro de dilogo
 			AlertDialog dialog = new AlertDialog.Builder (context).create ();
 			
 			// mostrar ttulo y descripcin del sitio.
 			dialog.setTitle (item.getTitle());
 			dialog.setMessage (item.getSnippet());
 			
 			// mostrar botones
 			dialog.setButton (DialogInterface.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
 				public void onClick (DialogInterface dialog, int which) {
 					CommonUtilities.showAlertMessage (context, "Confirmacin", "Agregaste la visita.");
 					dialog.cancel();
 				}
 			});
 			
 			dialog.setButton (DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
 				public void onClick (DialogInterface dialog, int which) {
 					CommonUtilities.showAlertMessage (context, "Cancelacin", "Descartaste la visita.");
 					dialog.cancel();
 				}
 			});
 			
 			dialog.show();
 			*/
 	
 	        //CommonUtilities.showAlertMessage (context, item.getTitle(), item.getSnippet());
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
