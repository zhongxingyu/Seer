 package fr.itinerennes.ui.adapter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import fr.itinerennes.ItineRennesConstants;
 import fr.itinerennes.R;
 import fr.itinerennes.onebusaway.client.IOneBusAwayClient;
 import fr.itinerennes.onebusaway.client.JsonOneBusAwayClient;
import fr.itinerennes.onebusaway.model.BusStation;
 import fr.itinerennes.onebusaway.model.Route;
 import fr.itinerennes.onebusaway.model.Stop;
 import fr.itinerennes.ui.activity.BusStationActivity;
 import fr.itinerennes.ui.activity.ItinerennesContext;
 import fr.itinerennes.ui.views.event.ToggleStarListener;
 import fr.itinerennes.ui.views.overlays.MarkerOverlayItem;
 
 /**
  * @author Jérémie Huchet
  */
 public class BusStationBoxAdapter implements MapBoxAdapter<Stop> {
 
     /** The itinerennes context. */
     private final ItinerennesContext context;
 
     /** The layout inflater. */
     private final LayoutInflater inflater;
 
     /** The bus station for which informations are displayed. */
     private Stop station = null;
 
     /** The icons for each route stopping at this stop. */
     private List<Drawable> routesIcons;
 
     /**
      * Creates the bus station adapter for map box.
      * 
      * @param context
      *            the context
      */
     public BusStationBoxAdapter(final ItinerennesContext context) {
 
         this.context = context;
         inflater = context.getLayoutInflater();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#getView(java.lang.Object)
      */
     @Override
     public final View getView(final MarkerOverlayItem item) {
 
         final View busView = inflater.inflate(R.layout.map_box_bus, null);
         ((TextView) busView.findViewById(R.id.map_box_title)).setText(item.getLabel());
         busView.findViewById(R.id.line_icon_container).setVisibility(View.GONE);
 
         busView.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(final View v) {
 
                 final Intent i = new Intent(context, BusStationActivity.class);
                 i.putExtra(BusStationActivity.INTENT_STOP_ID, item.getId());
                 i.putExtra(BusStationActivity.INTENT_STOP_NAME, item.getLabel());
                 context.startActivity(i);
             }
         });
 
         final ToggleButton star = (ToggleButton) busView.findViewById(R.id.map_box_toggle_bookmark);
        star.setChecked(context.getBookmarksService().isStarred(BusStation.class.getName(),
                item.getId()));
         star.setOnCheckedChangeListener(new ToggleStarListener(context,
                 ItineRennesConstants.MARKER_TYPE_BUS, item.getId(), item.getLabel()));
 
         final ImageView handistar = (ImageView) busView.findViewById(R.id.map_box_wheelchair);
        if (context.getAccessibilityService()
                .isAccessible(item.getId(), BusStation.class.getName())) {
             handistar.setVisibility(View.VISIBLE);
         }
         return busView;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#onStartLoading(android.view.View)
      */
     @Override
     public final void onStartLoading(final View view) {
 
         view.findViewById(R.id.map_box_progressbar).setVisibility(View.VISIBLE);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @return
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#doInBackground(android.view.View,
      *      java.lang.Object)
      */
     @Override
     public final Stop doInBackground(final View view, final MarkerOverlayItem item) {
 
         station = null;
         routesIcons = new ArrayList<Drawable>();
         try {
 
             final IOneBusAwayClient obaClient = new JsonOneBusAwayClient(context.getHttpClient(),
                     ItineRennesConstants.OBA_API_URL, ItineRennesConstants.OBA_API_KEY);
 
             station = obaClient.getStop(item.getId());
 
         } catch (final IOException e) {
             context.getExceptionHandler().handleException(e);
         }
 
         return station;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#updateView(android.view.View, java.lang.Object)
      */
     @Override
     public final void updateView(final View view, final Stop item) {
 
         // updates the station name just to be sure
         if (station != null) {
             ((TextView) view.findViewById(R.id.map_box_title)).setText(station.getName());
         }
 
         final LinearLayout iconsView = (LinearLayout) view.findViewById(R.id.line_icon_container);
 
         final List<Route> busRoutes = station.getRoutes();
         if (!busRoutes.isEmpty()) {
             for (final Route route : busRoutes) {
 
                 final View imageContainer = inflater.inflate(R.layout.line_icon, null);
                 final ImageView lineIcon = (ImageView) imageContainer
                         .findViewById(R.station.bus_line_icon);
                 lineIcon.setImageDrawable(context.getLineIconService().getIconOrDefault(context,
                         route.getShortName()));
                 iconsView.addView(imageContainer);
             }
             // set the list of routes icons visible only if it contains icons
             view.findViewById(R.id.line_icon_container).setVisibility(View.VISIBLE);
         }
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#onStopLoading(android.view.View)
      */
     @Override
     public final void onStopLoading(final View view) {
 
         view.findViewById(R.id.map_box_progressbar).setVisibility(View.GONE);
     }
 
 }
