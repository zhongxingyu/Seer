 package com.hypermurea.hslpushdroid;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.json.JSONException;
 
 import com.google.ads.AdView;
 import com.google.inject.Inject;
 import com.hypermurea.hslpushdroid.reittiopas.FindLinesService;
 import com.hypermurea.hslpushdroid.reittiopas.TransportLine;
 import com.hypermurea.hslpushdroid.user.UserProfile;
 import com.hypermurea.hslpushdroid.user.UserProfileFactory;
 
 import android.annotation.SuppressLint;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 
 @SuppressLint("NewApi")
 public class MainActivity extends RoboActivity implements TaskResultListener<List<TransportLine>>, LinesOfInterestChangeListener {
 
 	private static String TAG = "hslpushdroid";
 
 	@InjectView(R.id.mainLinearLayout)
 	private LinearLayout mainLayout;
 
 	@InjectView(R.id.linesListView) 
 	private ListView linesListView;
 
 	@InjectView(R.id.latestDisruptionTextView)
 	private TextView latestDisruptionTextView;
 
 	@InjectView(R.id.noLinesOfInterestTextView)
 	private TextView noLinesOfInterestTextView;
 
 	@Inject private UserProfileFactory userProfileFactory;
 	@Inject private FindLinesService findLinesService;
 	@Inject private AdViewFactory adViewFactory;
 	
 	@Inject private SharedPreferences preferences;
 
 	private int backgroundTasksRunning = 0;
 
 	// TODO Implement hashset to store currently existing search results
 	private static final String BUNDLED_LINE_SEARCH_RESULTS = "bundled_line_search_results";
 	private TransportLineAdapter searchResultsAdapter;
 	private ArrayList<TransportLine> currentLineSearchResults = new ArrayList<TransportLine>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
 		setContentView(R.layout.main);
 
 		injectDynamicLinesOfInterestViewsToLayout();
 		updateDynamicLinesOfInterestViews(userProfileFactory.getUserProfile(this));
 		updateHelp(userProfileFactory.getUserProfile(this));
 		
 		// TODO make modular
 		latestDisruptionTextView.setText(preferences.getString(ApplicationModule.LAST_DISRUPTION_PREFERENCE, ""));
 
		//setupAlarm();
 
 		setProgressBarIndeterminateVisibility(false);
 
 		LinearLayout mainLayout = (LinearLayout) this.findViewById(R.id.mainLinearLayout);
 		AdView adView = (AdView) adViewFactory.getAdView(this, mainLayout);
 		mainLayout.addView(adView);
 		adView.loadAd(adViewFactory.getAdRequest());
 
 		searchResultsAdapter = new TransportLineAdapter(this, R.layout.line_list_row, currentLineSearchResults, this);
 		linesListView.setAdapter(searchResultsAdapter);
 
 		if(savedInstanceState != null) {
 			ArrayList<TransportLine> bundledResults = savedInstanceState.getParcelableArrayList(BUNDLED_LINE_SEARCH_RESULTS);
 			Log.d(TAG, "Restoring search results:" + bundledResults.size());
 
 			currentLineSearchResults.addAll(bundledResults);
 			searchResultsAdapter.notifyDataSetChanged();
 		} else {
 			Intent intent = getIntent();
 			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 				Log.d(TAG, "search invoked");
 				currentLineSearchResults.clear();
 				searchResultsAdapter.notifyDataSetChanged();
 
 				String query = intent.getStringExtra(SearchManager.QUERY);
 				findLinesService.findLinesByName(this, query);
 			}
 		}
 
 	}
 
 	private void setupAlarm() {
 		// get a Calendar object with current time
 		Calendar cal = Calendar.getInstance();
 		// add 5 minutes to the calendar object
 		cal.add(Calendar.SECOND, 30);
 		Intent intent = new Intent(this, MockNotificationReceiver.class);
 		intent.putExtra("from", "195");
 		intent.putExtra("message", "Liikenteen toiminnassa hiriit");
 		// In reality, you would want to have a static variable for the request code instead of 192837
 		PendingIntent sender = PendingIntent.getBroadcast(this, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		// Get the AlarmManager service
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle bundle) {
 		bundle.putParcelableArrayList(BUNDLED_LINE_SEARCH_RESULTS, currentLineSearchResults);
 		Log.d(TAG, "put " + currentLineSearchResults.size() + " results in bundle");
 	}
 
 	private void injectDynamicLinesOfInterestViewsToLayout() {
 
 		if(mainLayout.findViewById(R.id.bussesOfInterest) == null) {
 			addLineOfInterestRowToLayout(R.id.bussesOfInterest, R.drawable.bussi);
 			addLineOfInterestRowToLayout(R.id.metrosOfInterest, R.drawable.metro);
 			addLineOfInterestRowToLayout(R.id.trainsOfInterest, R.drawable.juna);
 			addLineOfInterestRowToLayout(R.id.tramsOfInterest, R.drawable.ratikka);
 			addLineOfInterestRowToLayout(R.id.ferriesOfInterest, R.drawable.lautta);
 		}
 	}
 
 	private void addLineOfInterestRowToLayout(int viewId, int drawableId) {
 		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View view = inflater.inflate(R.layout.lines_of_interest, null);
 		view.setId(viewId);
 		ImageView imageView = (ImageView) view.findViewById(R.id.lineTypeImageView);
 		imageView.setImageResource(drawableId);
 
 		mainLayout.addView(view, 1);
 	}
 
 	private void updateHelp(UserProfile profile) {
 		if(profile.linesOfInterest.isEmpty()) {
 			noLinesOfInterestTextView.setText("Tst kohdassa nkyvt joukkoliikenteen linjat joiden hiritiedotuksia seuraat. Et ole viel valinnut seurattavia linjoja, mutta voit tehd sen hakua (suurennuslasi) tai paikannusta kytten.");
 		} else {
 			noLinesOfInterestTextView.setText("");	
 		}
 		if(profile.linesOfInterest.size() == 1 && latestDisruptionTextView.getText().length() == 0) {
 			latestDisruptionTextView.setText("Tss kohdassa nkyy viimeisin saamasi hiritiedote, joka ilmestyy puhelimeesi viestin mys tmn sovelluksen ollessa suljettuna. Voit mys list lis joukkoliikennelinjoja, joista haluat hiritiedotuksia.");
 		}
 	}
 
 	private void updateDynamicLinesOfInterestViews(UserProfile profile) {
 		updateDynamicRowOfInterestView(R.id.bussesOfInterest, R.drawable.bussi, profile);
 		updateDynamicRowOfInterestView(R.id.metrosOfInterest, R.drawable.metro, profile);
 		updateDynamicRowOfInterestView(R.id.trainsOfInterest, R.drawable.juna, profile);
 		updateDynamicRowOfInterestView(R.id.tramsOfInterest, R.drawable.ratikka, profile);
 		updateDynamicRowOfInterestView(R.id.ferriesOfInterest, R.drawable.lautta, profile);
 	}
 
 	private void updateDynamicRowOfInterestView(int viewId, int drawableId, UserProfile profile) {
 
 		View view = mainLayout.findViewById(viewId);
 		TextView linesOfInterestTextView = (TextView) view.findViewById(R.id.linesOfInterestTextView);
 		String lineCodesConcatenated = "";
 		List<TransportLine> lines = new ArrayList<TransportLine>();
 		for(TransportLine line : profile.linesOfInterest) {
 			if(TransportLineAdapter.getDrawableId(line) == drawableId) {
 				lineCodesConcatenated += line.shortCode + " ";
 				lines.add(line);
 			}
 		}
 
 		linesOfInterestTextView.setText(lineCodesConcatenated);
 
 		if(lineCodesConcatenated.length() > 0) {
 			view.setVisibility(View.VISIBLE);
 		} else {
 			view.setVisibility(View.GONE);
 		}
 
 		final ArrayAdapter<TransportLine> adapter = new ArrayAdapter<TransportLine>(this,
 				android.R.layout.simple_spinner_dropdown_item, lines);
 
 		ImageView editLinesOfInterest = (ImageView) view.findViewById(R.id.editlinesOfInterestImageButton);
 		editLinesOfInterest.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				new AlertDialog.Builder(MainActivity.this)
 				.setTitle("Poista seurattavia linjoja")
 				.setAdapter(adapter, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 						ListView items = ((AlertDialog) dialog).getListView();
 						MainActivity.this.removeTransportLine((TransportLine) items.getAdapter().getItem(which));
 
 						dialog.dismiss();
 					}
 				}).create().show();
 			}
 		});
 
 
 	}	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 
 			SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
 			searchView.setSearchableInfo(
 					searchManager.getSearchableInfo(getComponentName()));
 			searchView.setIconifiedByDefault(false);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.search:
 			onSearchRequested();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public void removeTransportLine(TransportLine line) {
 		userProfileFactory.getUserProfile(this).linesOfInterest.remove(line);
 		updateDynamicLinesOfInterestViews(userProfileFactory.getUserProfile(this));
 
 		updateHelp(userProfileFactory.getUserProfile(this));
 		
 		// TODO Poor style, unified way of handling exceptions needed
 		try {
 			userProfileFactory.signalChangeInUser(this);
 		} catch(JSONException e) {
 			Toast.makeText(this, "User data update failed", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public void addTransportLine(TransportLine line) {
 
 		TransportLineAdapter listAdapter = (TransportLineAdapter) linesListView.getAdapter();
 		listAdapter.remove(line);
 		listAdapter.notifyDataSetChanged();
 
 		userProfileFactory.getUserProfile(this).linesOfInterest.add(line);
 		updateDynamicLinesOfInterestViews(userProfileFactory.getUserProfile(this));
 
 		updateHelp(userProfileFactory.getUserProfile(this));
 		
 		// TODO Poor style, unified way of handling exceptions needed
 		try {
 			userProfileFactory.signalChangeInUser(this);
 		} catch(JSONException e) {
 			Toast.makeText(this, "User data update failed", Toast.LENGTH_LONG).show();
 		}
 
 	}
 
 	@Override
 	public void receiveResults(List<TransportLine> lines) {	
 		
 		boolean newResults = false;
 		for(TransportLine line : lines) {
 			if(!searchResultsContains(line)) {
 				Log.d(TAG, "search results do not contain: " + line.shortCode);
 				currentLineSearchResults.add(line);
 				newResults = true;
 			}
 		}
 		if(newResults) {
 			searchResultsAdapter.notifyDataSetChanged();			
 		}
 
 	}
 
 	private boolean searchResultsContains(TransportLine pendingAdd) {
 		for(TransportLine line : currentLineSearchResults) {
 			if(line.shortCode.equals(pendingAdd.shortCode) && line.transportType == pendingAdd.transportType) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	public void searchNearbyLinesToggled(View view) {    
 
 		if(((CheckBox) view).isChecked()) {
 			Log.d(TAG, "Starting location updates");
 			currentLineSearchResults.clear();
 			Toast.makeText(this, "Odota krsivllisesti, tm saattaa kest hetken", Toast.LENGTH_LONG).show();
 			searchResultsAdapter.notifyDataSetChanged();
 			findLinesService.startFindingLinesByLocation(this);
 		} else {
 			Log.d(TAG, "Stopping location updates");
 			findLinesService.stopFindingLinesByLocation();
 		}
 
 	}
 
 	@Override
 	public void backgroundTaskStarted() {
 		backgroundTasksRunning++;
 		if(backgroundTasksRunning > 0) {
 			synchronized(this) {
 				if(backgroundTasksRunning > 0) {
 					this.setProgressBarIndeterminateVisibility(true);					
 				}
 			}
 		}
 	}
 
 	@Override
 	public void backgroundTaskStopped() {
 		backgroundTasksRunning--;
 		if(backgroundTasksRunning <= 0) {
 			synchronized(this) {
 				if(backgroundTasksRunning <= 0) {
 					backgroundTasksRunning = 0;
 					this.setProgressBarIndeterminateVisibility(false);
 				}
 			}
 		}
 
 	}
 
 }
