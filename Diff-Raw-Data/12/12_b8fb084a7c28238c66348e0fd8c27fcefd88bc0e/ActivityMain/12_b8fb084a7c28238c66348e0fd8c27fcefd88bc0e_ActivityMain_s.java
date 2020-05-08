 package put.medicallocator.ui;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.actionbarsherlock.widget.SearchView;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import put.medicallocator.R;
 import put.medicallocator.io.helper.DataSourceConfigurator;
 import put.medicallocator.io.model.Facility;
 import put.medicallocator.io.model.FacilityType;
 import put.medicallocator.ui.animation.InOutAnimationController;
 import put.medicallocator.ui.animation.SlideInOutAnimationController;
 import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryListener;
 import put.medicallocator.ui.async.DataSourceConfigurerAsyncTask;
 import put.medicallocator.ui.async.DataSourceConfigurerAsyncTask.DataSourceInitializerListener;
 import put.medicallocator.ui.async.QueryController;
 import put.medicallocator.ui.dialogs.AboutDialogFactory;
 import put.medicallocator.ui.dialogs.DataSourceConfigurationDialogFactory;
 import put.medicallocator.ui.dialogs.FacilityDialogFactory;
 import put.medicallocator.ui.dialogs.FacilityTypeChooserDialogFactory;
 import put.medicallocator.ui.intent.IntentHandler;
 import put.medicallocator.ui.intent.ShowBubbleIntentHandler;
 import put.medicallocator.ui.location.MapLocationListener;
 import put.medicallocator.ui.misc.RouteOverlayManager;
 import put.medicallocator.ui.overlay.FaciltiesOverlayBuilder;
 import put.medicallocator.ui.overlay.utils.FacilityTapListener;
 import put.medicallocator.ui.utils.State;
 import put.medicallocator.utils.AsyncTaskUtils;
 import put.medicallocator.utils.LocationManagerUtils;
 import put.medicallocator.utils.MyLog;
 import put.medicallocator.utils.StringUtils;
 
import java.util.List;

 // TODO: Close DB connection, since it leaks from time to time.
 // TODO: Roboguice?
 // TODO: Consider OverlayManager
 
 /**
  * Main {@link Activity} in this application. It's responsible for managing the {@link MapView}.
  */
 public class ActivityMain extends SherlockMapActivity
         implements DataSourceInitializerListener, FacilityQueryListener, FacilityTapListener {
 
     private static final String TAG = ActivityMain.class.getSimpleName();
 
     /** Stores the identifiers of the Dialogs' keys. */
     private interface DialogKeys {
         int DATASOURCE_INIT_DIALOG = 1;
         int ABOUT_DIALOG = 2;
         int FACILITY_TYPE_CHOOSER_DIALOG = 3;
     }
 
     /**
      * Stores the {@link State} of this activity. It is used every time configuration change occurs
      * (like orientation change).
      */
     private State state;
 
     /* UI related */
     private MapView mapView;
     private ViewGroup searchControlsViewGroup;
     private Button facilityTypeSearchButton;
 
     /**
      * Array of the {@link IntentHandler}s, iterated over to find the proper {@link IntentHandler} for particular
      * {@link Intent}.
      */
     private IntentHandler[] intentHandlers;
 
     private InOutAnimationController searchInOutAnimationController;
 
     private MyLocationOverlay locationOverlay;
 
     private LocationListener myLocationListener;
 
     private QueryController queryController;
 
     private RouteOverlayManager routeOverlayManager;
 
     private final Runnable onFirstFixRunnable = new Runnable() {
 
         @Override
         public void run() {
             runOnUiThread(new Runnable() {
 
                 @Override
                 public void run() {
                     final MapController mapController = mapView.getController();
                     mapController.animateTo(locationOverlay.getMyLocation());
                 }
             });
         }
     };
 
     private final SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {
         @Override
         public boolean onQueryTextSubmit(String query) {
             return false;
         }
 
         @Override
         public boolean onQueryTextChange(String newText) {
             state.criteria.setQuery(newText);
             return true;
         }
     };
 
     private final SearchView.OnQueryTextListener listQueryListener = new SearchView.OnQueryTextListener() {
         @Override
         public boolean onQueryTextSubmit(String query) {
             final Intent intent = new Intent(Intent.ACTION_SEARCH, null, getApplicationContext(), ActivitySearchable.class);
             intent.putExtra(SearchManager.QUERY, query);
             startActivity(intent);
             return true;
         }
 
         @Override
         public boolean onQueryTextChange(String newText) {
             return false;
         }
     };
 
 
     private FacilityTypeChooserDialogFactory.OnFacilitiesTypesSelectedListener facilitiesTypesSelectedListener = new FacilityTypeChooserDialogFactory.OnFacilitiesTypesSelectedListener() {
         @Override
         public void onTypesSelected(List<FacilityType> types, String[] labels) {
             state.criteria.setAllowedTypes(types);
             updateChosenTypesButton();
             removeDialog(DialogKeys.FACILITY_TYPE_CHOOSER_DIALOG);
         }
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         MyLog.d(TAG, "onCreate @ " + this.getClass().getSimpleName());
 
     	/* Prepare the UI */
         setContentView(R.layout.activity_main);
 
         /* Cache the views */
         this.mapView = (MapView) findViewById(R.id.map_view);
         this.searchControlsViewGroup = (ViewGroup) findViewById(R.id.search_controls_viewgroup);
         this.facilityTypeSearchButton = (Button) findViewById(R.id.facilitytype_spinner);
         this.facilityTypeSearchButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDialog(DialogKeys.FACILITY_TYPE_CHOOSER_DIALOG);
             }
         });
 
         this.intentHandlers = new IntentHandler[]{new ShowBubbleIntentHandler(this, mapView)};
 
         /* Retrieve the state object if Configuration (f.e. orientation) change occured */
         restoreLastState();
 
         /* Ensure that DataSource is prepared and current */
         supportDataSourceInitialization();
 
         this.searchInOutAnimationController = new SlideInOutAnimationController(searchControlsViewGroup);
 
         /* Do view initialization */
         initializeMapView(this.mapView, this.state);
 
         /* Do other structures initialization*/
         this.queryController = new QueryController(getApplicationContext(), mapView, this.state.criteria, this);
         this.routeOverlayManager = new RouteOverlayManager(mapView, state);
 
         this.locationOverlay = new MyLocationOverlay(this, mapView);
         this.myLocationListener = new MapLocationListener(mapView, state);
 
         setSupportProgressBarIndeterminate(true);
         getSherlock().setProgressBarIndeterminateVisibility(true);
 
         updateChosenTypesButton();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         MyLog.d(TAG, "onResume @ " + this.getClass().getSimpleName());
 
 		/* Register for the Location updates */
         LocationManagerUtils.register(this, myLocationListener, new String[]{
                 LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER
         });
 
         restoreOverlays();
 
         if (state.daoInitializerAsyncTask == null) {
             /* Post query job */
             queryController.attach();
         }
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         for (IntentHandler handler : intentHandlers) {
             if (handler.supports(intent)) {
                 handler.process(intent);
             }
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         MyLog.d(TAG, "onPause @ " + this.getClass().getSimpleName());
 
     	/* Unregister for the Location updates */
         LocationManagerUtils.unregister(this, myLocationListener);
 
         /* Disable the MyLocationOverlay as well */
         locationOverlay.disableMyLocation();
 
     	/* Disable querying */
         queryController.detach();
     }
 
     @Override
     protected void onDestroy() {
         MyLog.d(TAG, "onDestroy @ " + this.getClass().getSimpleName());
         queryController.onDestroy();
         super.onDestroy();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.menu_main, menu);
 
         final MenuItem menuSearchItem = menu.findItem(R.id.menu_search_command);
         final SearchView mapQuerySearchView = (SearchView) menuSearchItem.getActionView();
         final SearchView listQuerySearchView = (SearchView) menu.findItem(R.id.menu_search_list_command).getActionView();
 
         menuSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
             @Override
             public boolean onMenuItemActionExpand(MenuItem item) {
                 searchInOutAnimationController.animateIn();
                 return true;
             }
 
             @Override
             public boolean onMenuItemActionCollapse(MenuItem item) {
                 searchInOutAnimationController.animateOut();
                 return true;
             }
         });
         mapQuerySearchView.setOnQueryTextListener(queryListener);
         listQuerySearchView.setOnQueryTextListener(listQueryListener);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
 
         final MenuItem item = menu.findItem(R.id.menu_toggle_tracking);
         final int resourceId = state.isTrackingEnabled ? R.string.activitymain_disabletracking : R.string.activitymain_enabletracking;
         item.setTitle(getString(resourceId));
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_search_command:
                 return true;
             case R.id.menu_about_command:
                 showDialog(DialogKeys.ABOUT_DIALOG);
                 return true;
             case R.id.menu_toggle_tracking:
                 state.isTrackingEnabled = !state.isTrackingEnabled;
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     // TODO: Change to (int, Bundle) version and provide parameters?
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DialogKeys.DATASOURCE_INIT_DIALOG:
                 return new DataSourceConfigurationDialogFactory().createDialog(this);
             case DialogKeys.ABOUT_DIALOG:
                 return new AboutDialogFactory().createDialog(this);
             case DialogKeys.FACILITY_TYPE_CHOOSER_DIALOG:
                 return new FacilityTypeChooserDialogFactory(state.criteria, facilitiesTypesSelectedListener).createDialog(this);
         }
         return super.onCreateDialog(id);
     }
 
     @Override
     public Object onRetainNonConfigurationInstance() {
         state.currentPoint = mapView.getMapCenter();
         state.zoomLevel = mapView.getZoomLevel();
         for (IntentHandler handler : intentHandlers) {
             state.intentHandlersState.put(handler.getClass(), handler.retainState());
         }
         return state;
     }
 
     protected void restoreLastState() {
         if (getLastNonConfigurationInstance() instanceof State) {
             this.state = (State) getLastNonConfigurationInstance();
             for (IntentHandler handler : intentHandlers) {
                 final Object handlerState = state.intentHandlersState.get(handler.getClass());
                 if (handlerState != null) {
                     handler.restoreState(handlerState);
                 }
             }
         } else {
             this.state = new State();
         }
     }
 
     private void initializeMapView(MapView mapView, State state) {
         mapView.setBuiltInZoomControls(false);
 
         final MapController mapController = mapView.getController();
         mapController.setCenter(state.currentPoint);
         mapController.setZoom(state.zoomLevel);
     }
 
     @Override
     public void onFacilityTap(Facility facility) {
         new FacilityDialogFactory(this, facility, routeOverlayManager).createDialog(this).show();
     }
 
     @Override
     public void onAsyncFacilityQueryStarted() {
         getSherlock().setProgressBarIndeterminateVisibility(true);
     }
 
     @Override
     public void onAsyncFacilityQueryCompleted(List<Facility> result) {
         MyLog.i(TAG, "Query finished. Returned rows: " + result.size());
 
         final List<Overlay> overlays = mapView.getOverlays();
         overlays.clear();
         if (result.size() == 0) {
             MyLog.d(TAG, "Removing overlays from the MapView");
             overlays.add(locationOverlay);
         } else {
             overlays.add(new FaciltiesOverlayBuilder(this).buildOverlay(result, this));
             overlays.add(locationOverlay);
             if (state.routeSpec != null) {
                 overlays.add(routeOverlayManager.getOrRestoreRoute(state.routeSpec));
             }
         }
 
         MyLog.d(TAG, "Posting invalidate on MapView");
         mapView.invalidate();
         getSherlock().setProgressBarIndeterminateVisibility(false);
     }
 
     @Override
     public void onDatabaseInitialized(boolean success) {
         state.daoInitializerAsyncTask = null;
         if (success) {
             dismissDialog(DialogKeys.DATASOURCE_INIT_DIALOG);
             queryController.attach();
         } else {
             // Not good, we encountered a real FATAL error..
             // TODO: Push information to the user.
         }
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     private void restoreOverlays() {
         locationOverlay.enableMyLocation();
        locationOverlay.runOnFirstFix(onFirstFixRunnable);
         mapView.getOverlays().add(locationOverlay);
     }
 
     private void updateChosenTypesButton() {
         if (state.criteria.isEverythingAllowed()) {
             facilityTypeSearchButton.setText(getString(R.string.activitymain_type_filter_all_enabled));
         } else {
             facilityTypeSearchButton.setText(StringUtils.join(state.criteria.getAllowedTypesLabels(this), ", "));
         }
     }
 
     private void supportDataSourceInitialization() {
         if (AsyncTaskUtils.isRunning(state.daoInitializerAsyncTask)) {
             state.daoInitializerAsyncTask.setListener(this);
         } else {
             final DataSourceConfigurator dataSourceConfigurator = DataSourceConfigurator.getInstance(getApplicationContext());
             if (!dataSourceConfigurator.isConfigured()) {
                 showDialog(DialogKeys.DATASOURCE_INIT_DIALOG);
                 state.daoInitializerAsyncTask = new DataSourceConfigurerAsyncTask(dataSourceConfigurator, this);
                 state.daoInitializerAsyncTask.execute();
             }
         }
     }
 
 }
