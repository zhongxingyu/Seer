 package fr.itinerennes.ui.adapter;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import fr.itinerennes.ItineRennesConstants;
 import fr.itinerennes.R;
 import fr.itinerennes.keolis.model.SubwayStation;
 import fr.itinerennes.ui.activity.ItinerennesContext;
 import fr.itinerennes.ui.views.event.ToggleStarListener;
 import fr.itinerennes.ui.views.overlays.MarkerOverlayItem;
 
 /**
  * @author Jérémie Huchet
  */
 public class SubwayStationBoxAdapter implements MapBoxAdapter<SubwayStation> {
 
     /** The itinerennes context. */
     private final ItinerennesContext context;
 
     /** The layout inflater. */
     private final LayoutInflater inflater;
 
     /**
      * Creates the subway station adapter for map box.
      * 
      * @param context
      *            the context
      */
     public SubwayStationBoxAdapter(final ItinerennesContext context) {
 
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
 
         final View subwayView = inflater.inflate(R.layout.map_box_subway, null);
         ((TextView) subwayView.findViewById(R.id.map_box_title)).setText(item.getLabel());
 
         final ToggleButton star = (ToggleButton) subwayView
                 .findViewById(R.id.map_box_toggle_bookmark);
        star.setChecked(context.getBookmarksService().isStarred(SubwayStation.class.getName(),
                item.getId()));
         star.setOnCheckedChangeListener(new ToggleStarListener(context,
                 ItineRennesConstants.MARKER_TYPE_SUBWAY, item.getId(), item.getLabel()));
 
         return subwayView;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#onStartLoading(android.view.View)
      */
     @Override
     public final void onStartLoading(final View view) {
 
         // nothing to load
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#doInBackground(android.view.View,
      *      java.lang.Object)
      */
     @Override
     public final SubwayStation doInBackground(final View view, final MarkerOverlayItem item) {
 
         return null;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#updateView(android.view.View, java.lang.Object)
      */
     @Override
     public final void updateView(final View view, final SubwayStation item) {
 
         // nothing to load
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#onStopLoading(android.view.View)
      */
     @Override
     public final void onStopLoading(final View view) {
 
         // nothing to load
     }
 
 }
