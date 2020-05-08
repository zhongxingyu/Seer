 package fr.itinerennes.ui.activity;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.slf4j.Logger;
 import org.slf4j.impl.AndroidLoggerFactory;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import fr.dudie.onebusaway.client.IOneBusAwayClient;
 import fr.dudie.onebusaway.model.Route;
 import fr.dudie.onebusaway.model.ScheduleStopTime;
 import fr.dudie.onebusaway.model.StopSchedule;
 
 import fr.itinerennes.ItineRennesConstants;
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.commons.utils.DateUtils;
 import fr.itinerennes.ui.adapter.BusStopTimeAdapter;
 import fr.itinerennes.ui.views.event.ToggleStarListener;
 import fr.itinerennes.ui.views.overlays.MarkerOverlayItem;
 
 /**
  * This activity uses the <code>bus_station.xml</code> layout and displays a window with
  * informations about a bus station.
  * 
  * @author Jérémie Huchet
  * @author Olivier Boudet
  */
 public final class BusStopActivity extends ItineRennesActivity {
 
     /** The event logger. */
     private static final Logger LOGGER = AndroidLoggerFactory.getLogger(BusStopActivity.class);
 
     /** Intent parameter name for the station identifier. */
     public static final String INTENT_STOP_ID = String.format("%s.stopId",
             BusStopActivity.class.getName());
 
     /** Intent parameter name for the trip identifier where to scroll in the schedule list. */
     public static final String INTENT_FROM_TRIP_ID = String.format("%s.fromTripId",
             BusTripActivity.class.getName());
 
     /** Intent parameter name for the station name. */
     public static final String INTENT_STOP_NAME = String.format("%s.stopName",
             BusStopActivity.class.getName());
 
     /** Id for the failure dialog. */
     private static final int FAILURE_DIALOG = 1;
 
     /** Size of the gap between the top and the list and the selection. */
     private static final int SELECTION_FROM_TOP = 50;
 
     /** Duration of toast messages. */
     private static final int TOAST_DURATION = 5000;
 
     /** The identifier of the displayed station. */
     private String stopId;
 
     /** The name of the displayed station. */
     private String stopName;
 
     /** flag indicating if this stop is accessible or not. */
     private boolean isAccessible = false;
 
     /** The OneBusAway client. */
     private IOneBusAwayClient obaClient;
 
     /** Adapter for the departures list view. */
     private BusStopTimeAdapter adapter;
 
     /** Date used to fetch schedule. */
     private Date scheduleDate;
 
     /** The list view showing departures. */
     private ListView listTimes;
 
     /**
      * Creates the activity.
      * <ul>
      * <li>Loads the main layout</li>
      * <li>Sets the station name</li>
      * <li>Set up a message handler to trigger actions when loading</li>
      * <li></li>
      * </ul>
      * <p>
      * {@inheritDoc}
      * </p>
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate(final Bundle savedInstanceState) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onCreate.start");
         }
 
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.act_bus_stop);
 
         obaClient = getApplicationContext().getOneBusAwayClient();
 
         // retrieve intent parameters
         stopId = getIntent().getStringExtra(INTENT_STOP_ID);
         stopName = getIntent().getStringExtra(INTENT_STOP_NAME);
 
         /* Sets the station name. */
         final TextView name = (TextView) findViewById(R.activity_bus_stop.name);
         name.setText(stopName);
 
         /* Display handistar icon if necessary. */
         isAccessible = getApplicationContext().getAccessibilityService().isAccessible(stopId,
                 TypeConstants.TYPE_BUS);
         if (isAccessible) {
             final ImageView handistar = (ImageView) findViewById(R.activity_bus_stop.wheelchair_icon);
             handistar.setVisibility(View.VISIBLE);
         }
 
         /* Sets bookmarked icon. */
         final ToggleButton star = (ToggleButton) findViewById(R.activity_bus_stop.toggle_bookmark);
         star.setChecked(getApplicationContext().getBookmarksService().isStarred(
                 TypeConstants.TYPE_BUS, stopId));
         star.setOnCheckedChangeListener(new ToggleStarListener(this, TypeConstants.TYPE_BUS,
                 stopId, stopName));
 
         // initialize the adapter
         adapter = new BusStopTimeAdapter(this, isAccessible);
         listTimes = (ListView) findViewById(R.activity_bus_stop.list_bus);
         listTimes.setEmptyView(findViewById(R.station.empty));
         listTimes.setAdapter(adapter);
 
         /* On Click listener. */
         listTimes.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(final AdapterView<?> parent, final View view,
                     final int position, final long id) {
 
                 final Intent i = new Intent(getBaseContext(), BusTripActivity.class);
                 final ScheduleStopTime departure = (ScheduleStopTime) parent.getAdapter().getItem(
                         position);
                 i.putExtra(BusTripActivity.INTENT_FROM_STOP_ID, stopId);
                 i.putExtra(BusTripActivity.INTENT_ROUTE_HEADSIGN, departure.getSimpleHeadsign());
                 i.putExtra(BusTripActivity.INTENT_ROUTE_SHORT_NAME, departure.getRoute()
                         .getShortName());
                 i.putExtra(BusTripActivity.INTENT_TRIP_ID, departure.getTripId());
                 i.putExtra(BusTripActivity.INTENT_ROUTE_ID, departure.getRoute().getId());
                 startActivity(i);
             }
         });
 
         // fetch schedule for the current date
         onDayChanged(new Date());
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onCreate.end");
         }
     }
 
     /**
      * {@inheritDoc}
      * 
     * @see fr.itinerennes.ui.activity.ItineRennesActivity#onResume()
     */
    @Override
    protected void onResume() {

        super.onResume();

        if (adapter != null) {
            adapter.notifyDataSetInvalidated();
        }
    }

    /**
     * {@inheritDoc}
     * 
      * @see android.app.Activity#onCreateDialog(int)
      */
     @Override
     protected Dialog onCreateDialog(final int id) {
 
         Dialog d = null;
         switch (id) {
         case FAILURE_DIALOG:
             final AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage(R.string.error_network).setCancelable(true)
                     .setNeutralButton("OK", new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(final DialogInterface dialog, final int id) {
 
                             dialog.dismiss();
                             finish();
                         }
                     });
             d = builder.create();
             break;
 
         default:
             break;
         }
         return d;
     }
 
     /**
      * Triggered when user clicks on the map button in the toolbar.
      * 
      * @param target
      *            should be an image view.
      */
     public void onMapButtonClick(final View target) {
 
         final Cursor c = getApplicationContext().getMarkerDao().getMarker(stopId,
                 TypeConstants.TYPE_BUS);
         if (c != null && c.moveToFirst()) {
 
             final MarkerOverlayItem marker = getApplicationContext().getMarkerDao()
                     .getMarkerOverlayItem(c);
 
             c.close();
 
             startActivity(MapActivity.IntentFactory.getOpenMapBoxIntent(getApplicationContext(),
                     marker, ItineRennesConstants.CONFIG_ZOOM_ON_LOCATION));
 
         } else {
             // TJHU gestion erreur qui ne doit pas arriver dans un cas normal
             Toast.makeText(getApplicationContext(),
                     getString(R.string.error_loading_bus_station_position, stopName),
                     TOAST_DURATION);
         }
     }
 
     /**
      * Called when the day changed so refreshing the schedule view is necessary.
      * 
      * @param newDate
      *            date to fetch schedule
      */
     private void onDayChanged(final Date newDate) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug(String.format("Showing schedule for %s", newDate));
         }
 
         scheduleDate = newDate;
 
         new AsyncTask<Void, Void, StopSchedule>() {
 
             /**
              * {@inheritDoc}
              * 
              * @see android.os.AsyncTask#onPreExecute()
              */
             @Override
             protected void onPreExecute() {
 
                 /* Hide progress bar and show list view. */
                 findViewById(R.activity_bus_stop.progress_bar).setVisibility(View.VISIBLE);
                 findViewById(R.activity_bus_stop.list_bus).setVisibility(View.GONE);
             };
 
             /**
              * {@inheritDoc}
              * 
              * @see android.os.AsyncTask#doInBackground(Params[])
              */
             @Override
             protected StopSchedule doInBackground(final Void... params) {
 
                 try {
                     /* Fetching stop informations for this station from the network. */
 
                     return obaClient.getScheduleForStop(stopId, scheduleDate);
 
                 } catch (final IOException e) {
                     LOGGER.debug(
                             String.format("Can't load informations for the station %s.", stopId), e);
                 }
 
                 return null;
             }
 
             /**
              * {@inheritDoc}
              * 
              * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
              */
             @Override
             protected void onPostExecute(final StopSchedule result) {
 
                 if (result != null) {
 
                     /* Hide progress bar and show list view. */
                     findViewById(R.activity_bus_stop.progress_bar).setVisibility(View.GONE);
                     findViewById(R.activity_bus_stop.list_bus).setVisibility(View.VISIBLE);
 
                     /* Displaying routes icons. */
 
                     final ViewGroup lineList = (ViewGroup) findViewById(R.id.line_icon_container);
                     lineList.removeAllViews();
                     for (final Route busRoute : result.getStop().getRoutes()) {
                         final View imageContainer = getLayoutInflater().inflate(
                                 R.layout.li_line_icon, null);
                         final ImageView lineIcon = (ImageView) imageContainer
                                 .findViewById(R.station.bus_line_icon);
                         lineIcon.setImageDrawable(getApplicationContext().getLineIconService()
                                 .getIconOrDefault(getApplicationContext(), busRoute.getShortName()));
 
                         lineList.addView(imageContainer);
 
                         LOGGER.debug("Showing icon for line {}.", busRoute.getShortName());
                     }
 
                     /* Displaying departures dates. */
                     // get, if available, the tripId of the previous BusTripActivity displayed
                     final String tripId = getIntent().getExtras().getString(INTENT_FROM_TRIP_ID);
 
                     adapter.setTripIdToHighlight(tripId);
                     adapter.setData(result);
 
                     listTimes.setSelectionFromTop(adapter.getInitialIndex(), SELECTION_FROM_TOP);
 
                 } else {
                     showDialog(FAILURE_DIALOG);
                 }
             };
 
         }.execute();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public boolean onCreateOptionsMenu(final Menu menu) {
 
         final MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.act_stop_menu, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
 
         final Calendar c = Calendar.getInstance();
 
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.menu_back_to_map:
 
             final Intent i = new Intent(getApplicationContext(), MapActivity.class);
             i.setAction(Intent.ACTION_VIEW);
             i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(i);
             return true;
         case R.id.menu_previous_day:
             c.setTime(scheduleDate);
             onDayChanged(DateUtils.addDays(c.getTime(), -1));
             return true;
         case R.id.menu_next_day:
             c.setTime(scheduleDate);
             onDayChanged(DateUtils.addDays(c.getTime(), 1));
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
