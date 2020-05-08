 package fr.itinerennes.ui.views.overlays;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedOverlay;
 import org.osmdroid.views.overlay.OverlayItem;
 import org.slf4j.Logger;
 import org.slf4j.impl.ItinerennesLoggerFactory;
 
import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 
 import fr.itinerennes.R;
 import fr.itinerennes.business.event.IBookmarkModificationListener;
 import fr.itinerennes.business.service.BookmarkService;
 import fr.itinerennes.model.Station;
 import fr.itinerennes.ui.activity.ITRContext;
 import fr.itinerennes.ui.views.overlays.event.OnItemizedOverlayUpdateListener;
 
 /**
  * @author Jérémie Huchet
  */
 public final class BookmarkItemizedOverlay<D extends Station> extends ItemizedOverlay<OverlayItem>
         implements OnItemizedOverlayUpdateListener<D>, IBookmarkModificationListener {
 
     /** The event logger. */
     private static final Logger LOGGER = ItinerennesLoggerFactory
             .getLogger(BookmarkItemizedOverlay.class);
 
     /** The itinerennes context. */
     private final ITRContext context;
 
     /** The bookmark service. */
     private final BookmarkService bookmarkService;
 
     /** The map view on which this overlay is displayed. */
     private final MapView osmView;
 
     /** The overlay for which this overlay is displaying favorites icons. */
     private final SelectableItemizedOverlay<SelectableMarker<D>, D> overlay;
 
    private final Drawable starDrawable;

    private final Point starHostspot;

     /**
      * Creates the bookmark overlay.
      * 
      * @param context
      *            the itinerennes context
      */
     public BookmarkItemizedOverlay(final ITRContext context, final MapView osmView,
             final SelectableItemizedOverlay<SelectableMarker<D>, D> overlay) {
 
         super(context, new ArrayList<OverlayItem>(), getStarOverlayDrawable(context), null, null,
                 null, null);
         this.context = context;
         this.bookmarkService = context.getBookmarksService();
         bookmarkService.addListener(this);
         this.osmView = osmView;
         this.overlay = overlay;
        starDrawable = getStarOverlayDrawable(context);
        starHostspot = new Point(starDrawable.getIntrinsicWidth() * 4 / 5,
                starDrawable.getIntrinsicHeight() * 4 / 5);
     }
 
     /**
      * Gets the star overlay drawable.
      * 
      * @return the star overlay drawable
      */
     private static Drawable getStarOverlayDrawable(final ITRContext context) {
 
         return context.getResources().getDrawable(R.drawable.marker_overlay_star);
     }
 
     /**
     * Gets the star overlay drawable marker hotspot.
     * 
     * @return the star overlay drawable marker hotspot
     */
    // private static Point getStarOverlayDrawableHotspot(final ITRContext context) {
    //
    // final Drawable d = context.getResources().getDrawable(R.drawable.marker_overlay_star);
    // return new Point(d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() / 2);
    // }

    /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.views.overlays.event.OnItemizedOverlayUpdateListener#onContentUpdated(java.util.List)
      */
     @Override
     public final <I extends Marker<D>> void onContentUpdated(final List<I> items) {
 
         super.mItemList.clear();
         for (final I item : items) {
             final D bus = item.getData();
            LOGGER.warn("lon=" + bus.getGeoPoint().getLongitudeE6() + ", lat="
                    + bus.getGeoPoint().getLatitudeE6());
             if (bookmarkService.isStarred(bus.getClass().getName(), bus.getId())) {
                 final OverlayItem i = new OverlayItem(bus.getClass().getName(), bus.getId(),
                        bus.getGeoPoint());
                i.setMarker(starDrawable);
                i.setMarkerHotspot(starHostspot);
                 super.mItemList.add(i);
             }
         }
         osmView.postInvalidate();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.business.event.IBookmarkModificationListener#onBookmarkRemoval(java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void onBookmarkRemoval(final String type, final String id) {
 
         overlay.updateOverlay(osmView);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.business.event.IBookmarkModificationListener#onBookmarkAddition(java.lang.String,
      *      java.lang.String, java.lang.String)
      */
     @Override
     public void onBookmarkAddition(final String type, final String id, final String label) {
 
         overlay.updateOverlay(osmView);
     }
 }
