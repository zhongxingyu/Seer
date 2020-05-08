 package com.papagiannis.tuberun;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.papagiannis.tuberun.favorites.Favorite;
 import com.papagiannis.tuberun.fetchers.Observer;
 import com.papagiannis.tuberun.fetchers.OysterFetcher;
 import com.papagiannis.tuberun.stores.CredentialsStore;
 
 public class TubeRun extends Activity implements OnClickListener, Observer {
 	private final TubeRun self = this;
 	public static final String APPNAME = "TubeRun";
 	public static final String VERSION = "1.0.0beta";
 	private static final int DOWNLOAD_IMAGE_DIALOG = -1;
 	private static final int DOWNLOAD_IMAGE_PROGRESS_DIALOG = -2;
 	private static final int DOWNLOAD_IMAGE_FAILED_DIALOG = -3;
 	private static final String TUBE_MAP_URL = "http://www.tfl.gov.uk/assets/downloads/standard-tube-map.gif";
 	private static final String LOCAL_PATH = "standard-tube-map.gif";
 
 	TextView oysterBalance;
 	ProgressBar oysterProgress;
 	LinearLayout oysterLayout;
 	Button oysterButton;
 	Button oysterButtonActive;
 	Button logoButton;
 	ToggleButton favoritesButton;
 
 	private SharedPreferences preferences;
 	private boolean tubeMapDownloaded = false;
 	private ImageDownloadTask task;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		preferences = getPreferences(MODE_PRIVATE);
 		tubeMapDownloaded = preferences.getBoolean("tubeMapDownloaded", false);
 //		tubeMapDownloaded = false; //always
 
 		View statusButton = findViewById(R.id.button_status);
 		statusButton.setOnClickListener(this);
 		View departuresButton = findViewById(R.id.button_departures);
 		departuresButton.setOnClickListener(this);
 		View mapsButton = findViewById(R.id.button_maps);
 		mapsButton.setOnClickListener(this);
 		View nearbyButton = findViewById(R.id.button_nearby);
 		nearbyButton.setOnClickListener(this);
 		logoButton = (Button) findViewById(R.id.button_logo);
 		logoButton.setOnClickListener(this);
 		favoritesButton = (ToggleButton) findViewById(R.id.button_favorites);
 		favoritesButton.setOnClickListener(this);
 		View claimsButton = findViewById(R.id.button_claims);
 		claimsButton.setOnClickListener(this);
 		View planButton = findViewById(R.id.button_planner);
 		planButton.setOnClickListener(this);
 		oysterButton = (Button) findViewById(R.id.button_oyster);
 		oysterButton.setOnClickListener(this);
 		oysterButtonActive = (Button) findViewById(R.id.button_oyster_active);
 		oysterButtonActive.setOnClickListener(this);
 		oysterBalance = (TextView) findViewById(R.id.view_balance);
 		oysterProgress = (ProgressBar) findViewById(R.id.progressbar_balance);
 		oysterLayout = (LinearLayout) findViewById(R.id.layout_balance);
 	}
 
 	public void onClick(View v) {
 		Intent i = null;
 		switch (v.getId()) {
 		case R.id.button_status:
 			i = new Intent(this, StatusActivity.class);
 			break;
 		case R.id.button_departures:
 			i = new Intent(this, SelectLineActivity.class);
 			i.putExtra("type", "departures");
 			break;
 		case R.id.button_maps:
 			// i=new Intent(this, SelectLineActivity.class);
 			// i.putExtra("type", "maps");
 			if (tubeMapDownloaded)
 				i = getMapIntent();
 			else {
 				showDialog(DOWNLOAD_IMAGE_DIALOG);
 				return;
 			}
 			break;
 		case R.id.button_nearby:
 			i = new Intent(this, NearbyStationsActivity.class);
 			break;
 		case R.id.button_favorites:
 			favoritesButton.setChecked(!favoritesButton.isChecked()); //no toggling
 			i = new Intent(this, FavoritesActivity.class);
 			break;
 		case R.id.button_claims:
 			i = new Intent(this, ClaimsActivity.class);
 			break;
 		case R.id.button_planner:
 			i = new Intent(this, PlanActivity.class);
 			break;
 		case R.id.button_oyster:
 		case R.id.button_oyster_active:
 			i = new Intent(this, OysterActivity.class);
 			break;
 		case R.id.button_logo:
 			i = new Intent(this, AboutActivity.class);
 			break;	
 		}
 		startActivity(i);
 	}
 
 	private Intent getMapIntent() {
 		Intent i;
 		i = new Intent(this, StatusMapActivity.class);
 		i.putExtra("line",
 				LinePresentation.getStringRespresentation(LineType.ALL));
 		i.putExtra("type", "maps");
 		return i;
 	}
 
 	private CredentialsStore store = CredentialsStore.getInstance();
 	private OysterFetcher fetcher;
 	private String username = "";
 
 	private void fetchBalance() {
 		oysterButtonActive.setVisibility(View.GONE);
 		oysterButton.setVisibility(View.VISIBLE);
 		oysterBalance.setVisibility(View.GONE);
 		oysterProgress.setVisibility(View.GONE);
 		oysterLayout.setVisibility(View.GONE);
 		ArrayList<String> credentials = store.getAll(this);
 		if (credentials.size() == 0)
 			return;
 		Date now = new Date();
 		// skip fetching oyster balance if it has been fetched before (in the
 		// last 3 min).
 		if (!username.equals("")
 				&& username.equals(credentials.get(0))
 				&& !fetcher.isErrorResult()
 				&& (now.getTime() - fetcher.getUpdateTime().getTime()) / 1000 < 3 * 60) {
 			update();
 			return;
 		}
 		if (credentials.size() == 2) {
 			fetcher = new OysterFetcher(credentials.get(0), credentials.get(1));
 			fetcher.registerCallback(this);
 			oysterButtonActive.setVisibility(View.VISIBLE);
 			oysterButton.setVisibility(View.GONE);
 			oysterLayout.setVisibility(View.VISIBLE);
 			oysterProgress.setVisibility(View.VISIBLE);
 			username = credentials.get(0);
 			oysterLayout.invalidate();
 			fetcher.update();
 		}
 	}
 
 	@Override
 	public void update() {
 		oysterButtonActive.setVisibility(View.VISIBLE);
 		oysterButton.setVisibility(View.GONE);
 		oysterLayout.setVisibility(View.VISIBLE);
 		oysterProgress.setVisibility(View.GONE);
 		oysterBalance.setText(fetcher.getResult());
 		oysterBalance.setVisibility(View.VISIBLE);
 		oysterLayout.invalidate();
 
 	}
 
 	private Dialog wait_dialog;
 	ProgressDialog progressDialog;
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog result = null;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (id) {
 		case DOWNLOAD_IMAGE_DIALOG:
 			builder.setTitle("Tube Map Required")
 					.setMessage(
 							"The official Tube Map is property of TfL and is not included in this app. "
 									+ "However, TubeRun can fetch it from TfL and cache it for future use.\n\n"
 									+ "Use WiFi because mobile operators reduce image quality.")
 					.setCancelable(true)
 					.setPositiveButton("Download",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dismissDialog(DOWNLOAD_IMAGE_DIALOG);
 									showDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 									fetchTubeMap();
 								}
 							})
 					.setNegativeButton("Cancel",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dismissDialog(DOWNLOAD_IMAGE_DIALOG);
 								}
 							});
 			wait_dialog = builder.create();
 			result = wait_dialog;
 			break;
 		case DOWNLOAD_IMAGE_PROGRESS_DIALOG:
 			progressDialog = new ProgressDialog(this);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setMessage("Downloading Tube Map");
 			progressDialog.setCancelable(true);
 			progressDialog
 					.setOnCancelListener(new DialogInterface.OnCancelListener() {
 
 						@Override
 						public void onCancel(DialogInterface dialog) {
 							task.cancel(true);
 							dismissDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 						}
 					});
 			wait_dialog = progressDialog;
 			result = progressDialog;
 			break;
 		case DOWNLOAD_IMAGE_FAILED_DIALOG:
 			builder.setTitle("Download failed")
 					.setMessage(
 							"Could not download the Tube Map. Please try again later. "
 									+ "Make sure that you have Internet access (preferably WiFi) and "
 									+ "your SD card is available.")
 					.setCancelable(true)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dismissDialog(DOWNLOAD_IMAGE_FAILED_DIALOG);
 								}
 							});
 			wait_dialog = builder.create();
 			result = wait_dialog;
 			break;
 		}
 		return result;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		switch (id) {
 		case DOWNLOAD_IMAGE_PROGRESS_DIALOG:
 			progressDialog.setProgress(0);
 			progressDialog.setMax(100);
 		}
 	};
 
 	private void fetchTubeMap() {
 		task = new ImageDownloadTask();
 		task.execute(TUBE_MAP_URL, LOCAL_PATH);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (task != null) {
 			if (progressDialog != null)
 				dismissDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 			task.cancel(true);
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		fetchBalance();
 		ArrayList<Favorite> favs=Favorite.getFavorites(this);
 		favoritesButton.setChecked(favs.size()>0);
 	}
 
 	private class ImageDownloadTask extends AsyncTask<String, Integer, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 			try {
 				URL url = new URL(params[0]);
 				HttpURLConnection urlConnection = (HttpURLConnection) url
 						.openConnection();
 				urlConnection.setRequestMethod("GET");
 				urlConnection.connect();
 
 //				File appdir = Environment.getExternalStorageDirectory();
 //				File dir = new File(appdir.getAbsoluteFile() + "/tuberun/");
 //				Boolean created = dir.mkdirs();
 //				File file = new File(dir, params[1]);
 //				FileOutputStream fileOutput = new FileOutputStream(file);
 
 				//Let's read everything to RAM first
 				InputStream inputStream = urlConnection.getInputStream();
 				int totalSize = urlConnection.getContentLength();
 				int downloadedSize = 0;
 				byte[] buffer = new byte[1024];
 				byte[] fullFile = new byte[totalSize];
 				int bufferLength = 0; // used to store a temporary size of the
 //										// buffer
 				int i=0;
 				while ((bufferLength = inputStream.read(buffer)) > 0) {
 					if (isCancelled()) return false;
 //					fileOutput.write(buffer, 0, bufferLength);
 					
 					int k=0;
 					for (int j=i;j<i+bufferLength;j++) {
 						fullFile[j]=buffer[k++];
 					}
 					i+=bufferLength;
 //					
 //					
 					downloadedSize += bufferLength;
 					publishProgress(downloadedSize, totalSize);
 				}
 //				fileOutput.close();
 				inputStream.close();
 				
 				ContentValues v=new ContentValues();
 				v.put("map", fullFile);
 				ContentResolver r=getContentResolver();
 				r.insert(Uri.parse("content://"+TubeMapContentProvider.AUTHORITY+"/map"), v);
 				
 				//This is an attempt to read from the ContentProvider, it works!
 //				Cursor rrr=r.query(Uri.parse("content://"+TubeMapContentProvider.AUTHORITY+"/map"), new String[]{},"",new String[]{},"");
 //				Boolean suc=rrr.moveToFirst();
 //				if (suc) {
 //					byte[] res=rrr.getBlob(0);
 //					int iii=res.length;
 //					i=i+i;
 //				}
 				
 				
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			dismissDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 			if (result) {
 				tubeMapDownloaded = true;
 				Editor editor = preferences.edit();
 				editor.putBoolean("tubeMapDownloaded", tubeMapDownloaded);
 				editor.commit();
 				Intent i = getMapIntent();
 				startActivity(i);
 			} else
 				showDialog(DOWNLOAD_IMAGE_FAILED_DIALOG);
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 			int current = values[0];
 			int total = values[1];
 			// int percent=(100*current)/total;
 			progressDialog.setProgress(current);
 			progressDialog.setMax(total);
 		}
 	}
 }
