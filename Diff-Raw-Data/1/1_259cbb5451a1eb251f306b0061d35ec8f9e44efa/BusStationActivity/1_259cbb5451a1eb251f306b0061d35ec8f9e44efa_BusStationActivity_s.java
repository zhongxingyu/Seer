 package fr.itinerennes.ui.activity;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.impl.ItinerennesLoggerFactory;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import fr.itinerennes.R;
 import fr.itinerennes.business.facade.BusDepartureService;
 import fr.itinerennes.business.facade.BusRouteService;
 import fr.itinerennes.business.facade.BusService;
 import fr.itinerennes.business.http.keolis.KeolisService;
 import fr.itinerennes.database.DatabaseHelper;
 import fr.itinerennes.exceptions.GenericException;
 import fr.itinerennes.model.BusDeparture;
 import fr.itinerennes.model.BusRoute;
 import fr.itinerennes.model.BusStation;
 import fr.itinerennes.model.LineIcon;
 import fr.itinerennes.ui.adapter.BusTimeAdapter;
 
 /**
  * This activity uses the <code>bus_station.xml</code> layout and displays a window with
  * informations about a bus station.
  * 
  * @author Jérémie Huchet
  * @author Olivier Boudet
  */
 public class BusStationActivity extends Activity {
 
     /** The event logger. */
     private static final Logger LOGGER = ItinerennesLoggerFactory
             .getLogger(BusStationActivity.class);
 
     /** The Bus Service. */
     private BusService busService;
 
     /** The Bus Route Service. */
     private BusRouteService busRouteService;
 
     /** The Departure Service. */
     BusDepartureService busDepartureService;
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onCreate.start");
         }
         super.onCreate(savedInstanceState);
         setContentView(R.layout.bus_station);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onCreate.end");
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onResume()
      */
     @Override
     protected void onResume() {
 
         super.onResume();
 
         final DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext());
         busService = new BusService(dbHelper.getWritableDatabase());
         busRouteService = new BusRouteService(dbHelper.getWritableDatabase());
         busDepartureService = new BusDepartureService(dbHelper.getWritableDatabase());
 
         final String stationId = getIntent().getExtras().getString("item");
 
         try {
             /* Displaying bus stop title */
             BusStation station = busService.getStation(stationId);
             final TextView name = (TextView) findViewById(R.station.name);
             name.setText(station.getName());
             LOGGER.debug("Bus stop title height = {}", name.getMeasuredHeight());
         } catch (final GenericException e) {
             LOGGER.debug(
                     String.format("Can't load station informations for the station %s.", stationId),
                     e);
         }
 
         /* TJHU this must be replaced - start */
         final KeolisService keoServ = new KeolisService();
         List<LineIcon> allIcons = null;
         try {
             allIcons = keoServ.getAllLineIcons();
         } catch (final GenericException e) {
             LOGGER.error("error", e);
         }
         /* TJHU this must be replaced - end */
 
         try {
             /* Fetching routes informations for this station. */
             final List<BusRoute> busRoutes = busRouteService.getStationRoutes(stationId);
 
             /* Displaying routes icons. */
             final ViewGroup lineList = (ViewGroup) findViewById(R.station.line_icon_list);
             if (busRoutes != null) {
                 for (final BusRoute busRoute : busRoutes) {
                     final ImageView lineIcon = (ImageView) getLayoutInflater().inflate(
                             R.layout.line_icon, null);
                     lineIcon.setImageDrawable(getBaseContext().getResources().getDrawable(
                             R.drawable.tmp_lm1));
                     lineList.addView(lineIcon);
                     LOGGER.debug("Showing icon for line {}.", busRoute.getId());
                 }
             }
         } catch (final GenericException e) {
             LOGGER.debug(
                     String.format("Can't load routes informations for the station %s.", stationId),
                     e);
         }
 
         try {
             /* Fetching and displaying departures informations for this station. */
             final ListView listTimes = (ListView) findViewById(R.station.list_bus);
             final List<BusDeparture> departures = busDepartureService
                     .getStationDepartures(stationId);
 
             listTimes.setAdapter(new BusTimeAdapter(getBaseContext(), departures));
         } catch (final GenericException e) {
             LOGGER.debug(String.format("Can't load departures informations for the station %s.",
                     stationId), e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onPause()
      */
     @Override
     protected void onPause() {
 
         busService.release();
         busRouteService.release();
         super.onPause();
     }
 }
