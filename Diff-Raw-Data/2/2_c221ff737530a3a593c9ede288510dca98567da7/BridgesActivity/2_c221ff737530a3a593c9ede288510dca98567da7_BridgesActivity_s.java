 package com.example;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.example.core.Bridge;
 import com.example.core.BridgesDescriptions;
 import com.example.core.BridgesList;
 import com.example.core.SortingOrder;
 import com.example.fragments.AllBridgesFragment;
 import com.example.fragments.FavouriteBridgesFragment;
 
 import java.util.*;
 
 public class BridgesActivity extends SherlockFragmentActivity {
 
     public static final String SORTING_ORDER = "sortingOrder";
     public static final String FAVOURITE_PREFS = "favourite";
     private static final String GENERAL_PREFS = "prefs";
     public static final String TAB_INDEX_PROPERTY = "tabIndex";
 
     private final List<OptionsListener> listeners = new ArrayList<OptionsListener>();
 
     private LocationManager locationManager;
     private SortingOrder sortingOrder = SortingOrder.BY_NAME;
     private Timer updateTimer;
     private BridgesList bridgesList;
     private ViewPager viewPager;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         bridgesList = new BridgesList(getSharedPreferences(FAVOURITE_PREFS, 0), BridgesDescriptions.BRIDGES);
         cachedBridgesSelection = new boolean[bridgesList.size()];
         super.onCreate(savedInstanceState);
         viewPager = new ViewPager(this);
         viewPager.setId(R.id.main_view);
         setContentView(viewPager);
 
         SharedPreferences sharedPreferences = getSharedPreferences();
         sortingOrder = SortingOrder.values()[sharedPreferences.getInt(SORTING_ORDER, 0)];
         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
         buildActionsBar();
     }
 
     public BridgesList getBridgesList() {
         return bridgesList;
     }
 
     public SortingOrder getSortingOrder() {
         return sortingOrder;
     }
 
     public void addOptionsListener(OptionsListener listener) {
         listeners.add(listener);
     }
 
     private void buildActionsBar() {
         ActionBar actionBar = getSupportActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         actionBar.setDisplayOptions(0);
         FragmentTabManager tabManager = new FragmentTabManager(this, viewPager);
         tabManager.addTab(R.string.all_bridges_tab_label, AllBridgesFragment.class);
 
         tabManager.addTab(R.string.favourites_bridges_tab_label, FavouriteBridgesFragment.class);
         actionBar.setSelectedNavigationItem(getSharedPreferences().getInt(TAB_INDEX_PROPERTY, 0));
     }
 
 
     @Override
     protected void onResume() {
         super.onResume();
         updateTimer = new Timer(true);
         updateTimer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 bridgesList.update(getUserLocation());
             }
         }, 0, 15000);
     }
 
     @Override
     protected void onPause() {
         SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putInt(TAB_INDEX_PROPERTY, getActionBar().getSelectedNavigationIndex());
         edit.putInt(SORTING_ORDER, sortingOrder.index);
         edit.commit();
         updateTimer.cancel();
         super.onPause();
     }
 
     private Location getUserLocation() {
         for (String provider : locationManager.getProviders(true)) {
             return locationManager.getLastKnownLocation(provider);
         }
         return null;
     }
 
     private SharedPreferences getSharedPreferences() {
         return getSharedPreferences(GENERAL_PREFS, 0);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.context_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.sort_order:
                 showSortDialog();
                 return true;
             case R.id.choose_favourite:
                 showFavouriteDialog();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private String[] cachedSortOrdersLabels;
     private final DialogInterface.OnClickListener sortDialogListener = new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int item) {
             setSortingOrder(SortingOrder.values()[item]);
             dialog.dismiss();
         }
     };
 
     public void showSortDialog() {
         if (cachedSortOrdersLabels == null) {
             cachedSortOrdersLabels = new String[]{
                     getString(SortingOrder.BY_NAME.label),
                     getString(SortingOrder.BY_OPEN.label),
                     getString(SortingOrder.BY_DISTANCE.label)
             };
         }
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(getString(R.string.sort_dialog_title));
 
         builder.setSingleChoiceItems(cachedSortOrdersLabels, sortingOrder.index, sortDialogListener);
         AlertDialog alert = builder.create();
         alert.show();
     }
 
 
     private String[] cachedBridgesNames;
     private boolean[] cachedBridgesSelection;
     private final DialogInterface.OnMultiChoiceClickListener favoruitesDialgListener = new DialogInterface.OnMultiChoiceClickListener() {
         @Override
         public void onClick(DialogInterface dialogInterface, int index, boolean check) {
             Bridge bridge = bridgesList.get(index);
             bridge.setFavourite(check);
         }
     };
 
     public void showFavouriteDialog() {
         if (cachedBridgesNames == null) {
             cachedBridgesNames = new String[bridgesList.size()];
             for (int i = 0; i < cachedBridgesSelection.length; i++) {
                 cachedBridgesNames[i] = bridgesList.get(i).getDescription().name;
             }
         }
         for (int i = 0; i < cachedBridgesSelection.length; i++) {
             cachedBridgesSelection[i] = bridgesList.get(i).isFavourite();
         }
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(getString(R.string.choose_favourite));
 
         builder.setMultiChoiceItems(cachedBridgesNames, cachedBridgesSelection, favoruitesDialgListener);
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void setSortingOrder(SortingOrder sortingOrder) {
         this.sortingOrder = sortingOrder;
         for (OptionsListener listener : listeners) {
             listener.sortingOrderChanged(sortingOrder);
         }
     }
 
     public void removeOptionsListener(OptionsListener listener) {
         listeners.remove(listener);
     }
 }
