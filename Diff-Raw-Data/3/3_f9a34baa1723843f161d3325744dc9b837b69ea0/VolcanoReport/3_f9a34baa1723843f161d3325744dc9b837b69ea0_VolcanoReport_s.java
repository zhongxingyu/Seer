 package com.nbapps.volcanoreport;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class VolcanoReport extends MapActivity {
 
 	private MapView mapView;
 	private Drawable volcanoIcon;
 
 	private List<Overlay> mapOverlays;
 	private VolcanoOverlay weeklyOverlay;
 
 	private ArrayList<VolcanoInfo> weeklyVolcanoList = null;
 
 	private PreferencesManager prefsManager;
 
 	static final int DIALOG_NONETWORKWARNING_ID = 1;
 	static final int DIALOG_DOWNLOADING_ID = 2;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		prefsManager = new PreferencesManager(this.getApplicationContext());
 
 		setContentView(R.layout.main);
 
 		/*
 		 * load preferences
 		 */
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setSatellite(prefsManager.isSatelliteMapMode());
 		mapView.setBuiltInZoomControls(true);
 		mapView.getController().setCenter(new GeoPoint(0, 0));
 		mapView.getController().setZoom(2);
 		mapView.invalidate();
 
 		volcanoIcon = this.getResources().getDrawable(R.drawable.volcano_eruption2_red);
 		mapOverlays = mapView.getOverlays();
 		weeklyOverlay = new VolcanoOverlay(volcanoIcon, this);
 
 		/*
 		 * TODO: check for valid local kml file, start update if new file is
 		 * available, this should replace firstStart checke TODO: store map mode
 		 * in preferences
 		 * 
 		 * TODO: re-implement holocene volcanoes
 		 * 
 		 * TODO: new icon for volcanoes with new unrest
 		 * 
 		 * TODO: change back to kml and use WebView.loadData() for description
 		 * -> link
 		 * 
 		 * TODO: implement menu with map settings, about dialog, copyright
 		 * 
 		 * TODO: implement list view
 		 * 
 		 * TODO: implement donation
 		 * 
 		 * TODO: create logos and screenshots
 		 * 
 		 * TODO: implement earthquakes?
 		 */
 
 		tryGettingVolcanoLists();
 	}
 
 	protected void tryGettingVolcanoLists() {
 		// try to parse local file
 		if (parseWeeklyVolcanoXML()) {
 			// get date of the parsed xml
 			Date now = new Date();
 			long dateDiff = (now.getTime() - VolcanoXMLHandler.parsedVolcanoListDate
 					.getTime());
 			// is it older than 7 days?
 			if ((dateDiff / 1000 / 60 / 60 / 24) > 7) {
 				// get new file and parse it
 				downloadWeeklyVolcanoXML();
 			}
 			// no local file could be parsed, get new file and parse it
 		} else {
 			downloadWeeklyVolcanoXML();
 		}
 		// check if volcano list is valid update overlays
 		if (weeklyVolcanoList != null) {
 			// create google maps overlays
 			updateOverlay();
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch (id) {
 		case DIALOG_NONETWORKWARNING_ID:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(
 					"Sorry, no valid file with volcano information is available. Please ensure a working internet connection and click 'Retry'.")
 					.setCancelable(false)
 					.setPositiveButton("Retry",
 							new DialogInterface.OnClickListener() {
 
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.dismiss();
 								}
 							});
 			dialog = builder.create();
 			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
 				public void onDismiss(DialogInterface dialog) {
 					tryGettingVolcanoLists();
 				}
 			});
 			break;
 		case DIALOG_DOWNLOADING_ID:
 			dialog = ProgressDialog.show(this, "",
 					"Downloading volcano data. Please wait...", true);
 			break;
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem menuItem) {
 		switch (menuItem.getItemId()) {
 		// case R.id.about:
 		// return true;
 		case R.id.mapmode:
 			if (mapView.isSatellite()) {
 				mapView.setSatellite(false);
 				prefsManager.setSatelliteMapMode(false);
 			} else {
 				mapView.setSatellite(true);
 				prefsManager.setSatelliteMapMode(true);
 			}
 
 			return true;
 			// case R.id.support_it:
 			// return true;
 		default:
 			return super.onOptionsItemSelected(menuItem);
 		}
 	}
 
 	private void downloadWeeklyVolcanoXML() {
 		showDialog(DIALOG_DOWNLOADING_ID);
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					new ListDownloader().DownloadFromUrl(getResources()
 							.getString(R.string.urlWeeklyReport),
 							getResources()
 									.getString(R.string.localWeeklyReport));
 				} catch (Exception e) {
 					Log.d("VOLCANO_DEBUG", "Exception: " + e);
 				}
 				parseWeeklyVolcanoXML();
 				updateOverlay();
 				dismissDialog(DIALOG_DOWNLOADING_ID);
 			}
 		}).start();
 	}
 
 	private Boolean parseWeeklyVolcanoXML() {
 		// parse weekly report
 		try {
 			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 			SAXParser parser = parserFactory.newSAXParser();
 			File file = new File(getResources().getString(
 					R.string.localWeeklyReport));
 			FileInputStream inputStream = new FileInputStream(file);
 			Reader reader = new InputStreamReader(inputStream, "ISO-8859-1");
 			InputSource is = new InputSource(reader);
 			is.setEncoding("ISO-8859-1");
 			VolcanoXMLHandler xmlHandler = new VolcanoXMLHandler();
 			parser.parse(is, xmlHandler);
 		} catch (Exception e) {
 			Log.d("VOLCANO_DEBUG", "XML Parser Exception: " + e);
 			return false;
 		}
 		weeklyVolcanoList = VolcanoXMLHandler.volcanoList;
 		return true;
 	}
 
 	private void updateOverlay() {
 		for (int i = 0; i < weeklyVolcanoList.size(); i++) {
 			GeoPoint point = new GeoPoint(weeklyVolcanoList.get(i)
 					.getLatitude(), weeklyVolcanoList.get(i).getLongitude());
 			OverlayItem overlayItem = new OverlayItem(point, weeklyVolcanoList
 					.get(i).getTitle(), weeklyVolcanoList.get(i)
 					.getDescription());
 			weeklyOverlay.addOverlay(overlayItem);
 
 		}
 		mapOverlays.add(weeklyOverlay);
 		mapView.postInvalidate();
 	}
 
 	public void showVolcanoDetails(int index) {
 		Intent intent = new Intent(this, VolcanoDetails.class);
 		intent.putExtra("TITLE", weeklyVolcanoList.get(index).getTitle());
 		intent.putExtra("REPORTDATE", weeklyVolcanoList.get(index)
 				.getReportDate());
 		intent.putExtra("NEWUNREST", weeklyVolcanoList.get(index)
 				.getNewUnrest());
 		intent.putExtra("LATITUDE", weeklyVolcanoList.get(index).getLatitude());
 		intent.putExtra("LONGITUDE", weeklyVolcanoList.get(index)
 				.getLongitude());
 		intent.putExtra("DESCRIPTION", weeklyVolcanoList.get(index)
 				.getDescription());
 		intent.putExtra("LINK", weeklyVolcanoList.get(index).getLink());
 		this.startActivity(intent);
 	}
 }
