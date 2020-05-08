 package com.nagazuka.mobile.android.goedkooptanken;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
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
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import com.nagazuka.mobile.android.goedkooptanken.exception.LocationException;
 import com.nagazuka.mobile.android.goedkooptanken.exception.NetworkException;
 import com.nagazuka.mobile.android.goedkooptanken.model.Place;
 import com.nagazuka.mobile.android.goedkooptanken.model.PlaceDistanceComparator;
 import com.nagazuka.mobile.android.goedkooptanken.model.PlacePriceDistanceComparator;
 import com.nagazuka.mobile.android.goedkooptanken.model.PlacesParams;
 import com.nagazuka.mobile.android.goedkooptanken.service.DownloadService;
 import com.nagazuka.mobile.android.goedkooptanken.service.GeocodingService;
 import com.nagazuka.mobile.android.goedkooptanken.service.LocationService;
 import com.nagazuka.mobile.android.goedkooptanken.service.impl.AndroidLocationService;
 import com.nagazuka.mobile.android.goedkooptanken.service.impl.GoogleGeocodingService;
 import com.nagazuka.mobile.android.goedkooptanken.service.impl.ZukaService;
 
 public class PlacesListActivity extends ListActivity {
 
 	private static final String TAG = PlacesListActivity.class.getName();
 
 	private GoedkoopTankenApp app;
 	private PlacesAdapter m_adapter;
 	private PlaceDistanceComparator distanceComparator = new PlaceDistanceComparator();
 	private PlacePriceDistanceComparator priceDistanceComparator = new PlacePriceDistanceComparator();
 
 	private ProgressDialog m_progressDialog;
 
 	private List<Place> m_places = null;
 	private String m_postalCode = "";
 
 	private static final int DIALOG_PROGRESS = 1;
 	private static final int DIALOG_SEARCH = 2;
 
 	private static final int MAX_PROGRESS = 100;
 
 	private static final int CONTEXT_MENU_MAPS_ID = 0;
 	private static final int CONTEXT_MENU_DETAILS_ID = 1;
 	private static final int CONTEXT_MENU_NAVIGATION_ID = 2;
 
 	private static final int LOCATION_TASK = 0;
 	private static final int DOWNLOAD_TASK = 1;
 	private static final int GEOCODE_TASK = 2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
 
 		app = (GoedkoopTankenApp) getApplication();
 		m_places = app.getPlaces();
 
 		if (m_places != null) {
 			m_adapter = new PlacesAdapter(this, R.layout.row, m_places);
 			setListAdapter(m_adapter);
 		} else {
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
 			break;
 		case DIALOG_SEARCH:
 			Context mContext = getApplicationContext();
 			LayoutInflater inflater = (LayoutInflater) mContext
 					.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layout = inflater.inflate(R.layout.search_dialog, null);
 
 			final EditText edittext = (EditText) layout
 					.findViewById(R.id.search_postalcode_text);
 
 			ImageView image = (ImageView) layout
 					.findViewById(R.id.ic_search_dialog);
 			image.setImageResource(R.drawable.ic_mail);
 
 			DialogInterface.OnClickListener close = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.dismiss();
 				}
 			};
 
 			DialogInterface.OnClickListener search = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					String inputString = edittext.getText().toString();
 					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
 					if (inputString.length() == 4) {
 						m_postalCode = inputString;
						app.setPostalCode(m_postalCode);
 						new DownloadTask().execute();
 					}
 					dialog.dismiss();
 				}
 			};
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setView(layout);
 			builder.setTitle(R.string.search_postalcode);
 			builder.setPositiveButton(R.string.error_alert_search_button,
 					search);
 			builder.setNegativeButton(R.string.error_alert_neg_button, close);
 			dialog = builder.create();
 
 			break;
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenu.ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, CONTEXT_MENU_MAPS_ID, 0, "Open in Google Maps");
 		// menu.add(0, CONTEXT_MENU_NAVIGATION_ID, 1,
 		// "Open in Google Navigatie");
 		menu.add(0, CONTEXT_MENU_DETAILS_ID, 2, "Bekijk details");
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.sort_distance:
 			Collections.sort(m_places, distanceComparator);
 			m_adapter.notifyDataSetChanged();
 			return true;
 		case R.id.sort_price:
 			Collections.sort(m_places, priceDistanceComparator);
 			m_adapter.notifyDataSetChanged();
 			return true;
 		case R.id.search_postalcode:
 			showDialog(DIALOG_SEARCH);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case CONTEXT_MENU_MAPS_ID:
 			openItemInGoogleMaps(info.position, false);
 			return true;
 		case CONTEXT_MENU_NAVIGATION_ID:
 			openItemInGoogleMaps(info.position, true);
 			return true;
 		case CONTEXT_MENU_DETAILS_ID:
 			showDetailsDialog(info.position);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	private void openItemInGoogleMaps(int position, boolean navigation) {
 		if (m_places != null) {
 			Place selectedItem = m_places.get(position);
 			Uri geoUri = createGeoURI(selectedItem, navigation);
 			Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
 			startActivity(mapCall);
 		}
 	}
 
 	private void showDetailsDialog(int position) {
 		if (m_places != null) {
 			Place selectedItem = m_places.get(position);
 			String title = selectedItem.getName();
 			String summary = selectedItem.getSummary();
 
 			DialogInterface.OnClickListener back = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.dismiss();
 				}
 			};
 
 			new AlertDialog.Builder(PlacesListActivity.this)
 					.setTitle(title).setMessage(summary).setPositiveButton(
 							"OK", back).show();
 		}
 	}
 
 	private Uri createGeoURI(Place selectedItem, boolean navigation) {
 		String geoUriString;
 		if (!navigation) {
 			geoUriString = "geo:0,0?q=Nederland, ";
 		} else {
 			geoUriString = "google.navigation:?q=Nederland, ";
 		}
 
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
 		if (m_places != null) {
 			Collections.sort(m_places, distanceComparator);
 		} else {
 			m_places = Collections.emptyList();
 		}
 		return places;
 	}
 
 	private void showExceptionAlert(String message, Exception e, int taskType) {
 		Resources res = getResources();
 		if (e != null) {
 			Log.e(TAG, "<< Exception occurred in LocationTask."
 					+ e.getMessage());
 		}
 
 		if (!PlacesListActivity.this.isFinishing()) {
 			if (e instanceof LocationException) {
 				String buttonText = res
 						.getString(R.string.error_alert_location_button);
 				showRetryAlert(message, taskType,
 						Settings.ACTION_LOCATION_SOURCE_SETTINGS, buttonText);
 			} else if (e instanceof NetworkException) {
 				String buttonText = res
 						.getString(R.string.error_alert_network_button);
 				showRetryAlert(message, taskType,
 						Settings.ACTION_WIRELESS_SETTINGS, buttonText);
 			} else {
 				showDefaultExceptionAlert(message);
 			}
 		}
 	}
 
 	private void showDefaultExceptionAlert(String message) {
 
 		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.dismiss();
 			}
 		};
 
 		PlacesAlertDialogBuilder.createDefaultExceptionAlert(this, message,
 				positiveListener).show();
 	}
 
 	private void showRetryAlert(final String message, final int taskType,
 			final String settingsType, final String buttonText) {
 		Resources res = getResources();
 
 		DialogInterface.OnClickListener back = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				PlacesListActivity.this.finish();
 			}
 		};
 
 		DialogInterface.OnClickListener settings = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Intent intent = new Intent(settingsType);
 				startActivity(intent);
 			}
 		};
 
 		DialogInterface.OnClickListener retry = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				switch (taskType) {
 				case LOCATION_TASK:
 					new LocationTask().execute();
 					break;
 				case DOWNLOAD_TASK:
 					new DownloadTask().execute();
 					break;
 				case GEOCODE_TASK:
 					new GeocodeTask().execute();
 					break;
 				default:
 					break;
 				}
 			}
 		};
 
 		new AlertDialog.Builder(PlacesListActivity.this).setTitle(
 				res.getString(R.string.error_alert_title)).setMessage(message)
 				.setNegativeButton(
 						res.getString(R.string.error_alert_neg_button), back)
 				.setNeutralButton(buttonText, settings).setPositiveButton(
 						R.string.error_alert_retry_button, retry).show();
 	}
 
 	private class LocationTask extends AsyncTask<Void, Integer, String> {
 
 		private Exception m_exception = null;
 		private LocationManager m_locationManager = null;
 		private LocationService m_locationService = null;
 
 		@Override
 		public void onPreExecute() {
 			m_exception = null;
 			m_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 			m_locationService = new AndroidLocationService();
 
 			showDialog(DIALOG_PROGRESS);
 			m_progressDialog.setIcon(R.drawable.ic_gps_satellite);
 			m_progressDialog.setTitle(R.string.progressdialog_title_location);
 			m_progressDialog.setProgress(0);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			String postalCode = "";
 
 			try {
 				Location location = m_locationService
 						.getCurrentLocation(m_locationManager);
 				app.setLocation(location);
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
			m_progressDialog.setProgress((int) (MAX_PROGRESS * 0.33));
 			m_postalCode = result;
 
 			if (m_exception != null) {
 				m_progressDialog.dismiss();
 				showExceptionAlert(m_exception.getMessage(), m_exception,
 						LOCATION_TASK);
 			} else {
 				new GeocodeTask().execute();
 			}
 		}
 	}
 
 	private class GeocodeTask extends AsyncTask<Void, Integer, String> {
 
 		private Exception m_exception = null;
 		private GeocodingService m_geocodingService = null;
 
 		@Override
 		public void onPreExecute() {
 			m_exception = null;
 			m_geocodingService = new GoogleGeocodingService();
 
 			showDialog(DIALOG_PROGRESS);
			m_progressDialog.setProgress((int)(MAX_PROGRESS*0.33));			
 			m_progressDialog.setIcon(R.drawable.ic_mail);
 			m_progressDialog.setTitle(R.string.progressdialog_title_geocode);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			String postalCode = "";
 
 			try {
 				Location location = app.getLocation();
 				double latitude = location.getLatitude();
 				double longitude = location.getLongitude();
 
 				// Transform location to address using reverse geocoding
 				postalCode = m_geocodingService.getPostalCode(latitude,
 						longitude);
 				app.setPostalCode(postalCode);
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
 			m_progressDialog.setProgress((int) (MAX_PROGRESS * 0.67));
 			m_postalCode = result;
 
 			if (m_exception != null) {
 				m_progressDialog.dismiss();
 				showExceptionAlert(m_exception.getMessage(), m_exception,
 						GEOCODE_TASK);
 			} else if (m_postalCode == null || m_postalCode.length() == 0) {
 				m_progressDialog.dismiss();
 				showExceptionAlert(
 						"Postcode onbekend, kan tankstations niet downloaden",
 						null, GEOCODE_TASK);
 			} else {
 				new DownloadTask().execute();
 			}
 		}
 	}
 
 	private class DownloadTask extends AsyncTask<Void, Integer, List<Place>> {
 		private Exception m_exception = null;
 
 		@Override
 		protected void onPreExecute() {
 			m_exception = null;
 			showDialog(DIALOG_PROGRESS);
			m_progressDialog.setProgress((int)(MAX_PROGRESS*0.67));
 			m_progressDialog.setIcon(R.drawable.ic_web);
 			m_progressDialog.setTitle(R.string.progressdialog_title_download);
 		}
 
 		@Override
 		protected List<Place> doInBackground(Void... params) {
 			List<Place> results = Collections.emptyList();
 
 			try {
 				PlacesParams placesParams = new PlacesParams(app
 						.getFuelChoice(), app.getPostalCode());				
 				DownloadService downloader = new ZukaService();
				results = downloader.fetchPlaces(placesParams);				
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
 				showExceptionAlert(m_exception.getMessage(), m_exception,
 						DOWNLOAD_TASK);
 			} else if (result == null || result.size() == 0) {
 				m_places.clear();
 				m_adapter.notifyDataSetChanged();
 				app.setPlaces(m_places);
 			} else {
 				m_places.clear();
 				m_places.addAll(result);
 				m_adapter.notifyDataSetChanged();				
 				app.setPlaces(m_places);
 			}
 		}
 	}
 }
