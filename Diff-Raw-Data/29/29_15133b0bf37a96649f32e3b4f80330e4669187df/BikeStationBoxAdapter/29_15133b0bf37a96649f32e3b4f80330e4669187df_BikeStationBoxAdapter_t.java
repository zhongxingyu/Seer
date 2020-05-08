 package fr.itinerennes.ui.adapter;
 
 import java.io.IOException;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import fr.itinerennes.ItineRennesConstants;
 import fr.itinerennes.R;
 import fr.itinerennes.keolis.client.JsonKeolisClient;
 import fr.itinerennes.keolis.model.BikeStation;
 import fr.itinerennes.ui.activity.ItinerennesContext;
 import fr.itinerennes.ui.views.event.ToggleStarListener;
 import fr.itinerennes.ui.views.overlays.MarkerOverlayItem;
 
 /**
  * @author Jérémie Huchet
  */
 public class BikeStationBoxAdapter implements MapBoxAdapter<BikeStation> {
 
     /** The itinerennes context. */
     private final ItinerennesContext context;
 
     /** The layout inflater. */
     private final LayoutInflater inflater;
 
     /** Keolis Client. */
     private JsonKeolisClient keolisClient;
 
     /**
      * Creates the bus station adapter for map box.
      * 
      * @param context
      *            the context
      */
     public BikeStationBoxAdapter(final ItinerennesContext context) {
 
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
 
         final View bikeView = inflater.inflate(R.layout.map_box_bike, null);
         ((TextView) bikeView.findViewById(R.id.map_box_title)).setText(item.getLabel());
 
         final ToggleButton star = (ToggleButton) bikeView
                 .findViewById(R.id.map_box_toggle_bookmark);
        star.setChecked(context.getBookmarksService().isStarred(
                ItineRennesConstants.MARKER_TYPE_BIKE, item.getId()));
         star.setOnCheckedChangeListener(new ToggleStarListener(context,
                 ItineRennesConstants.MARKER_TYPE_BIKE, item.getId(), item.getLabel()));
 
         return bikeView;
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
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#doInBackground(android.view.View,
      *      java.lang.Object)
      */
     @Override
     public final BikeStation doInBackground(final View view, final MarkerOverlayItem item) {
 
         try {
             keolisClient = new JsonKeolisClient(context.getHttpClient(),
                     ItineRennesConstants.KEOLIS_API_URL, ItineRennesConstants.KEOLIS_API_KEY);
             return keolisClient.getBikeStation(item.getId());
         } catch (final IOException e) {
             context.getExceptionHandler().handleException(e);
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#updateView(android.view.View, java.lang.Object)
      */
     @Override
     public final void updateView(final View view, final BikeStation upToDateStation) {
 
         final View bikeStationDetails = view.findViewById(R.id.map_box_bike_details);
 
         if (upToDateStation == null) {
             // an error occurred during backgroundLoad()
             bikeStationDetails.setVisibility(View.GONE);
             return;
         } else {
             bikeStationDetails.setVisibility(View.VISIBLE);
         }
 
         final TextView availablesSlots = (TextView) bikeStationDetails
                 .findViewById(R.id.available_slots);
         availablesSlots.setText(String.valueOf(upToDateStation.getAvailableSlots()));
 
         final TextView availablesBikes = (TextView) bikeStationDetails
                 .findViewById(R.id.available_bikes);
         availablesBikes.setText(String.valueOf(upToDateStation.getAvailableBikes()));
 
         final ProgressBar gauge = (ProgressBar) bikeStationDetails
                 .findViewById(R.id.bike_station_gauge);
         gauge.setMax(upToDateStation.getAvailableBikes() + upToDateStation.getAvailableSlots());
         gauge.setIndeterminate(false);
         gauge.setProgress(upToDateStation.getAvailableBikes());
         gauge.setSecondaryProgress(upToDateStation.getAvailableBikes()
                 + upToDateStation.getAvailableSlots());
 
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
