 package com.nagazuka.mobile.android.goedkooptanken;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import com.nagazuka.mobile.android.goedkooptanken.exception.LocationException;
 import com.nagazuka.mobile.android.goedkooptanken.exception.NetworkException;
 import com.nagazuka.mobile.android.goedkooptanken.model.Place;
 import com.nagazuka.mobile.android.goedkooptanken.model.PlaceDistanceComparator;
 import com.nagazuka.mobile.android.goedkooptanken.model.PlacesConstants;
 import com.nagazuka.mobile.android.goedkooptanken.model.PlacesParams;
 import com.nagazuka.mobile.android.goedkooptanken.service.DownloadService;
 import com.nagazuka.mobile.android.goedkooptanken.service.GeocodingService;
 import com.nagazuka.mobile.android.goedkooptanken.service.LocationService;
 import com.nagazuka.mobile.android.goedkooptanken.service.impl.AndroidLocationService;
 import com.nagazuka.mobile.android.goedkooptanken.service.impl.GoogleGeocodingService;
 import com.nagazuka.mobile.android.goedkooptanken.service.impl.ZukaService;
 
 public class PlacesListActivity extends ListActivity {
 
 	private static final String TAG = PlacesListActivity.class.getName();
 
 	private PlacesAdapter m_adapter;
 	private PlaceDistanceComparator comparator = new PlaceDistanceComparator();
 	private ProgressDialog m_progressDialog;
 
 	private List<Place> m_places = Collections.emptyList();
 	private String m_postalCode = "";
 	private String m_fuelChoice = "";
 
 	private static final int DIALOG_PROGRESS = 1;
 	private static final int MAX_PROGRESS = 100;
 	private static final int CONTEXT_MENU_MAPS_ID = 0;
 	private static final int CONTEXT_MENU_DETAILS_ID = 1;
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		switch (id) {
 		case DIALOG_PROGRESS:
 			m_progressDialog = new ProgressDialog(PlacesListActivity.this);
 			m_progressDialog.setIcon(R.drawable.ic_gps_satellite);
 			m_progressDialog.setTitle(R.string.progressdialog_title_location);
 			m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			m_progressDialog.setMax(MAX_PROGRESS);
 			m_progressDialog.setButton2(
 					getText(R.string.progressdialog_cancel),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 
 							/* User clicked No so do some stuff */
 						}
 					});
 			dialog = m_progressDialog;
 		}
 		return dialog;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Check whether places have been downloaded before
 		// to avoid expensive loading on screen orientation change
 		final Object data = getLastNonConfigurationInstance();
 		final List<Place> downloadedPlaces = (List<Place>) data;
 
 		if (downloadedPlaces != null && downloadedPlaces.size() > 0) {
 			m_places = downloadedPlaces;
 			m_adapter = new PlacesAdapter(this, R.layout.row, m_places);
 			setListAdapter(m_adapter);
 		} else {
 			m_fuelChoice = getIntent().getStringExtra(
 					PlacesConstants.INTENT_EXTRA_FUEL_CHOICE);
 
 			ListView listView = getListView();
 			listView.setTextFilterEnabled(true);
 
 			m_places = new ArrayList<Place>();
 			m_adapter = new PlacesAdapter(this, R.layout.row, m_places);
 			setListAdapter(m_adapter);
 
 			new LocationTask().execute();
 		}
 
 		registerForContextMenu(getListView());
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenu.ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, CONTEXT_MENU_MAPS_ID, 0, "Open in Google Maps");
 		menu.add(0, CONTEXT_MENU_DETAILS_ID, 1, "Bekijk details");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case CONTEXT_MENU_MAPS_ID:
 			openItemInGoogleMaps(info.position);
 			return true;
 		case CONTEXT_MENU_DETAILS_ID:
 			showDetailsDialog(info.position);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	private void openItemInGoogleMaps(int position) {
 		Log.d(TAG, "<< Position selected [" + position + "]");
 		if (m_places != null) {
 			Place selectedItem = m_places.get(position);
 			Uri geoUri = createGeoURI(selectedItem);
 			Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
 			startActivity(mapCall);
 		}
 	}
 	
 	private void showDetailsDialog(int position) {
 		Log.d(TAG, "<< Position selected [" + position + "]");
 		if (m_places != null) {
 			Place selectedItem = m_places.get(position);
 			String summary = selectedItem.getSummary();
 			
 			DialogInterface.OnClickListener back = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
 				}
 			};
 			
 			new AlertDialog.Builder(PlacesListActivity.this)
 			.setTitle("Details")
 			.setMessage(summary)
 			.setPositiveButton("OK",back)
 			.show();
 			
 		}
 	}	
 
 	private Uri createGeoURI(Place selectedItem) {
 		String geoUriString = "geo:0,0?q=Nederland, ";
 		geoUriString += selectedItem.getAddress() + ", "
 				+ selectedItem.getPostalCode() + "," + selectedItem.getTown();
 		Log.d(TAG, "<< Geo Uri String [" + geoUriString + "]");
 		Uri geoUri = Uri.parse(geoUriString);
 		return geoUri;
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		// Save downloaded places for future self
 		// (e.g. on screen orientation change)
 		List<Place> places = null;
 		if (m_places != null && m_places.size() > 0) {
 			places = m_places;
 			Collections.sort(places, comparator);
 		} else {
 			places = Collections.emptyList();
 		}
 		return places;
 	}
 
 	private void showExceptionAlert(String message, Exception e) {
 		Resources res = getResources();
 		if (e != null) {
 			Log.e(TAG, "<< Exception occurred in LocationTask."
 					+ e.getMessage());
 		}
 
 		if (!PlacesListActivity.this.isFinishing()) {
 			if (e instanceof LocationException) {				
 				String buttonText = res.getString(R.string.error_alert_location_button);
 				showSettingsExceptionAlert(message,Settings.ACTION_LOCATION_SOURCE_SETTINGS, buttonText);
 			}
 			else if (e instanceof NetworkException) {
 				String buttonText = res.getString(R.string.error_alert_network_button);
 				showSettingsExceptionAlert(message,Settings.ACTION_WIRELESS_SETTINGS, buttonText);
 			} else {
 				showDefaultExceptionAlert(message);
 			}
 		}
 	}
 	
 	private void showDefaultExceptionAlert(String message) {
 		Resources res = getResources();
 		
 		new AlertDialog.Builder(PlacesListActivity.this).setTitle(
 				res.getString(R.string.error_alert_title)).setMessage(
 				message).setPositiveButton(
 				res.getString(R.string.error_alert_pos_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						PlacesListActivity.this.finish();
 					}
 				}).show();
 	}
 	
 	private void showSettingsExceptionAlert(final String message, final String settingsType, final String buttonText) {
 		Resources res = getResources();
 
 		DialogInterface.OnClickListener back = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				PlacesListActivity.this.finish();
 			}
 		};
 
 		DialogInterface.OnClickListener locationSettings = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Intent intent = new Intent(
 						settingsType);
 				startActivity(intent);
 			}
 		};
 
 		new AlertDialog.Builder(PlacesListActivity.this).setTitle(
 				res.getString(R.string.error_alert_title)).setMessage(message)
 				.setNegativeButton(
 						res.getString(R.string.error_alert_neg_button), back)
 				.setPositiveButton(
 						buttonText,
 						locationSettings).show();
 	}
 	
 	private class LocationTask extends AsyncTask<Void, Integer, String> {
 
 		private Exception m_exception = null;
 		private LocationManager m_locationManager = null;
 		private LocationService m_locationService = null;
 		private GeocodingService m_geocodingService = null;
 
 		@Override
 		public void onPreExecute() {
 			m_exception = null;
 			m_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 			m_locationService = new AndroidLocationService();
 			m_geocodingService = new GoogleGeocodingService();
 
 			showDialog(DIALOG_PROGRESS);
 			m_progressDialog.setTitle(R.string.progressdialog_title_location);
 			m_progressDialog.setProgress(0);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			String postalCode = "";
 
 			try {
 				Location location = m_locationService.getCurrentLocation(m_locationManager);
 
 				double latitude = location.getLatitude();
 				double longitude = location.getLongitude();
 
 				((GoedkoopTankenApp) getApplication()).setLocation(location);
 
 				Log.d(TAG, "<< Latitude: " + latitude + " Longitude: "
 						+ longitude + ">>");
 
 				publishProgress((int) (MAX_PROGRESS * 0.25));
 
 				// Transform location to address using reverse geocoding
 				postalCode = m_geocodingService.getPostalCode(latitude,
 						longitude);
 
 				publishProgress((int) (MAX_PROGRESS * 0.33));
 			} catch (Exception e) {
 				m_exception = e;
 			}
 
 			return postalCode;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			m_progressDialog.setProgress(progress[0]);
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			m_progressDialog.setProgress((int) (MAX_PROGRESS * 0.5));
 			m_postalCode = result;
 
 			Log.d(TAG, "<< LocationTask: mFuelChoice " + m_fuelChoice
 					+ " m_postalCode " + m_postalCode + ">>");
 
 			if (m_exception != null || m_postalCode == null
 					|| m_postalCode.length() == 0) {
 				m_progressDialog.setProgress(MAX_PROGRESS);
 				m_progressDialog.dismiss();
 				showExceptionAlert(
 						m_exception.getMessage(),
 						m_exception);
 			} else {
 				new DownloadTask().execute(m_fuelChoice, m_postalCode);
 			}
 		}
 	}
 
 	private class DownloadTask extends AsyncTask<String, Integer, List<Place>> {
 		private Exception m_exception = null;
 
 		@Override
 		protected void onPreExecute() {
 			m_exception = null;
 			m_progressDialog.setTitle(R.string.progressdialog_title_download);
 		}
 
 		@Override
 		protected List<Place> doInBackground(String... params) {
 			List<Place> results = Collections.emptyList();
 
 			try {
 				PlacesParams placesParams = new PlacesParams(params[0],
 						params[1]);
 
 				publishProgress((int) (MAX_PROGRESS * 0.75));
 
 				DownloadService downloader = new ZukaService();
 				results = downloader.fetchPlaces(placesParams);
 
 				publishProgress((int) (MAX_PROGRESS * 0.90));
 			} catch (Exception e) {
 				m_exception = e;
 			}
 
 			return results;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			m_progressDialog.setProgress(progress[0]);
 		}
 
 		@Override
 		protected void onPostExecute(List<Place> result) {
 			m_progressDialog.setProgress(MAX_PROGRESS);
 			m_progressDialog.dismiss();
 
 			if (m_exception != null) {
 				showExceptionAlert(
 						m_exception.getMessage(),
 						m_exception);
 			} else if (result == null || result.size() == 0) {
 				showExceptionAlert("Geen resultaten gevonden", m_exception);
 			} else {
 				Log.d(TAG, "<< DownloadTask: result size = " + result.size()
 						+ ">>");
 
 				m_places.addAll(result);
 				m_adapter.notifyDataSetChanged();
 
 				((GoedkoopTankenApp) getApplication()).setPlaces(m_places);
 			}
 		}
 	}
 }
