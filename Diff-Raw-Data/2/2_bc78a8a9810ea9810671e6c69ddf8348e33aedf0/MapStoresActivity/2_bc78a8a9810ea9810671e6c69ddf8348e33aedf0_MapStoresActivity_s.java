 package com.parse3.storefinder.views;
 
 import java.util.ArrayList;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.telephony.PhoneNumberUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.parse3.storefinder.Program;
 import com.parse3.storefinder.R;
 import com.parse3.storefinder.StoreOverlayItem;
 import com.parse3.storefinder.controllers.MapStoresController;
 import com.parse3.storefinder.models.Store;
 import com.parse3.storefinder.views.controls.FinderMapView;
 import com.parse3.storefinder.views.interfaces.IMapStoresView;
 
 public class MapStoresActivity extends MapActivity implements IMapStoresView {
 	private static final int OPTION_REFRESH = 0;
 	private static final int OPTION_MYLOCATION = 1;
 	private static final int OPTION_SETTINGS = 2;
 	
 	private MapStoresController controller;
 	private FinderMapView mapView;
 	private Dialog dialog;
 	private StoreOverlayItems overlayItems;
 	
 	private OnClickListener navigateToOnClickListener = new OnClickListener() {
 		public void onClick(View arg0) {
 			TextView address = (TextView)findViewById(R.id.store_address);
 			TextView citystate = (TextView)findViewById(R.id.store_citystate);
 			Uri navigateUri = Uri.parse("google.navigation:q=" + Uri.encode(address.getText().toString() + " " + citystate.getText().toString()));
 			startActivity(new Intent(Intent.ACTION_VIEW, navigateUri));
 		}
 	};
 	
 	private OnClickListener callOnClickListener = new OnClickListener() {
 		public void onClick(View arg0) {
 			TextView phone = (TextView)findViewById(R.id.store_phone);
 			Uri callUri = Uri.parse("tel:" + Uri.encode(phone.getText().toString()));
 			startActivity(new Intent(Intent.ACTION_DIAL, callUri));
 		}
 	};
 	
 	private OnClickListener mapViewOnClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View view) {
 			Log.v(Program.LOG, "MapStoresActivity.mapViewOnClickListener.onClick()");
 			findViewById(R.id.popup).setVisibility(View.GONE);
 		}
 		
 	};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Log.i(Program.LOG, "MapStoresActivity.onCreate()");
 		
 		setContentView(R.layout.map_stores);
 		
 		mapView = (FinderMapView)findViewById(R.id.mapview);
 		mapView.setView(this);
 		overlayItems = new StoreOverlayItems(getResources().getDrawable(R.drawable.marker));
 		mapView.setBuiltInZoomControls(true);
 		mapView.setOnClickListener(mapViewOnClickListener);
 		
 		controller = new MapStoresController(this);
 		controller.bindData();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		Log.i(Program.LOG, "MapStoresActivity.onPause()");
 		
 		controller.cleanup();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		controller.startUserLocationFinder();
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.i(Program.LOG, "MapStoresActivity.onActivityResult()");
 		
 		mapView.getOverlays().clear();
 		overlayItems.clear();
 		((View)findViewById(R.id.popup)).setVisibility(View.GONE);
 		overlayItems.onTap(-1);
 		
 		controller.setUserOverlay();
 		
 		controller.bindData();
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.i(Program.LOG, "ListStoresActivity.onCreateOptionsMenu()");
 		
 		menu.add(0, OPTION_REFRESH, 2, "Refresh").setIcon(android.R.drawable.ic_menu_rotate);
 		menu.add(0, OPTION_SETTINGS, 1, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
 		menu.add(0, OPTION_MYLOCATION, 0, "Center Results").setIcon(android.R.drawable.ic_menu_mylocation);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.v(Program.LOG, "ListStoresActivity.onOptionsItemSelected()");
 		
 		switch (item.getItemId()) {
 			case OPTION_REFRESH:
 				mapView.getOverlays().clear();
 				overlayItems.clear();
 				
 				controller.setUserOverlay();
 				
 				controller.refreshStores();
 				return true;
 			case OPTION_MYLOCATION:
 				centerOverlays();
 				return true;
 			case OPTION_SETTINGS:
 				Intent i = new Intent(this, SettingsActivity.class);
 				startActivityForResult(i, 0);
 				
 				return true;
 			default:
 				
 				return false;
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	public Context getContext() {
 		Log.v(Program.LOG, "MapStoresActivity.getContext()");
 		
 		return this;
 	}
 
 	@Override
 	public MapView getMapView() {
 		Log.v(Program.LOG, "MapStoresActivity.getMapView()");
 		
 		return mapView;
 	}
 
 	@Override
 	public MapController getMapController() {
 		return mapView.getController();
 	}
 
 	@Override
 	public void addOverlay(Overlay overlay) {
 		mapView.getOverlays().add(overlay);
 	}
 
 	@Override
 	public void addOverlay(StoreOverlayItem overlayItem) {
 		overlayItems.addOverlay(overlayItem);
 	}
 	
 	@Override
 	public void centerOverlays() {
 		int minLat = 81 * MapStoresController.MAP_SCALE;
 		int maxLat = -81 * MapStoresController.MAP_SCALE;
 		int minLon = 181 * MapStoresController.MAP_SCALE;
 		int maxLon = -181 * MapStoresController.MAP_SCALE;
 		
 		for (int i = 0; i < overlayItems.size(); i++) {
 			Store s = overlayItems.getItem(i).getStore();
 			minLat = (int) ((minLat > (s.getLocation().getLatitude() * MapStoresController.MAP_SCALE)) ? 
 																						s.getLocation().getLatitude() * MapStoresController.MAP_SCALE :
 																						minLat);
 			maxLat = (int) ((maxLat < (s.getLocation().getLatitude() * MapStoresController.MAP_SCALE)) ? 
 																						s.getLocation().getLatitude() * MapStoresController.MAP_SCALE :
 																						maxLat);
 			minLon = (int) ((minLon > (s.getLocation().getLongitude() * MapStoresController.MAP_SCALE)) ? 
 																						s.getLocation().getLongitude() * MapStoresController.MAP_SCALE :
 																						minLon);
 			maxLon = (int) ((maxLon < (s.getLocation().getLongitude() * MapStoresController.MAP_SCALE)) ? 
 																						s.getLocation().getLongitude() * MapStoresController.MAP_SCALE :
 																						maxLon);
 		}
 		
 		GeoPoint gp = controller.getUserLocation();
 		
 		minLat = (minLat > gp.getLatitudeE6()) ? gp.getLatitudeE6() : minLat;
 		maxLat = (maxLat < gp.getLatitudeE6()) ? gp.getLatitudeE6() : maxLat;
 		minLon = (minLon > gp.getLongitudeE6()) ? gp.getLongitudeE6() :	minLon;
 		maxLon = (maxLon < gp.getLongitudeE6()) ? gp.getLongitudeE6() :	maxLon;
 		
 		mapView.getController().zoomToSpan((maxLat - minLat), (maxLon - minLon));
 		mapView.getController().animateTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
 	}
 	
 	@Override
 	public void displayOverlays() {
 		mapView.getOverlays().add(overlayItems);
 	}
 
 	@Override
 	public void hideDialog() {
 		if (dialog != null && dialog.isShowing())
 			dialog.dismiss();		
 	}
 
 	@Override
 	public void showDialog() {
 		dialog = ProgressDialog.show(this, "", "Refreshing stores...", true);
 	}
 	
 	@Override
 	public void mapClick() {
 		((View)findViewById(R.id.popup)).setVisibility(View.GONE);
 		overlayItems.onTap(-1);
 	}
 	
 	public void overlayTapped(int index) {
 		if (index < 0)
 			return;
 		
 		Store s = overlayItems.getItem(index).getStore();
 		View panel = (View)findViewById(R.id.popup);
 		panel.setVisibility(View.VISIBLE);
 		
 		TextView name = (TextView)findViewById(R.id.store_name);
 		name.setText(s.getName());
 
 		TextView address = (TextView)findViewById(R.id.store_address);
 		address.setText(s.getAddress());
 		
 		TextView citystate = (TextView)findViewById(R.id.store_citystate);
 		citystate.setText(s.getCitystate());
 		
 		TextView phone = (TextView)findViewById(R.id.store_phone);
 		phone.setText("Phone: " + PhoneNumberUtils.formatNumber(s.getPhone()));
 
 		ImageButton navigate = (ImageButton)findViewById(R.id.navigate_to);
 		navigate.setOnClickListener(navigateToOnClickListener);
 		
 		ImageButton call = (ImageButton)findViewById(R.id.call);
 		call.setOnClickListener(callOnClickListener);
 		
 		TextView distance = (TextView)findViewById(R.id.distance);
 		distance.setText(Program.round(s.getDistance(), 2) + " miles");
 	}
 	
 	private class StoreOverlayItems extends ItemizedOverlay<StoreOverlayItem> {
 		private ArrayList<StoreOverlayItem> overlays = new ArrayList<StoreOverlayItem>();
 		private int lastIndex;	
 
 		public StoreOverlayItems(Drawable defaultMarker) {
 			super(boundCenterBottom(defaultMarker));
 			
 			Log.i(Program.LOG, "StoreOverlayItems._construct()");
 			
 			lastIndex = -1;
 			populate();
 		}
 
 		@Override
 		protected StoreOverlayItem createItem(int i) {
 			Log.v(Program.LOG, "StoreOverlayItems.createItem()");
 			
 			return overlays.get(i);
 		}
 
 		@Override
 		public int size() {
 			return overlays.size();
 		}
 		
 		public void addOverlay(StoreOverlayItem overlay) {
 			Log.v(Program.LOG, "StoreOverlayItems.addOverlay()");
 			
 			overlays.add(overlay);
 			populate();
 		}
 
 		public void clear() {
 			Log.v(Program.LOG, "StoreOverlayItems.clear()");
 			
 			overlays.clear();
 			populate();
 		}
 		
 		@Override
 		public boolean onTap(int index) {
 			if (index >= 0) {
 				overlays.get(index).setMarker(boundCenterBottom(getResources().getDrawable(R.drawable.marker_highlight)));
 			}
 			
 			if (lastIndex >= 0) {
 				overlays.get(lastIndex).setMarker(boundCenterBottom(getResources().getDrawable(R.drawable.marker)));
 			}
 			
 			lastIndex = index;
 			
 			if (index >= 0)
 				overlayTapped(index);
 			
 			return true;
 		}
 	}
 }
