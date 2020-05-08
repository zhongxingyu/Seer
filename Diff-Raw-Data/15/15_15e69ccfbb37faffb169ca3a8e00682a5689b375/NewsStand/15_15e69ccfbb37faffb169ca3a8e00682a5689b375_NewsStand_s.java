 package edu.umd.umiacs.newsstand;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewGroup.MarginLayoutParams;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.Overlay;
 
 
 public class NewsStand extends MapActivity implements View.OnClickListener {
 	private SharedPreferences mPrefsSetting;
 	private SharedPreferences mPrefsSources;
 	private NewsStandMapView mMapView;
 	private SeekBar mSlider;
 	private Refresh mRefresh;
 	private PopupPanel mPanel;
 	private ImageButton mButtonHome;
 	private ImageButton mButtonLocal;
 	private ImageButton mButtonWorld;
 	private ImageButton mButtonLocate;
 	private ImageButton mButtonMinus;
 	private ImageButton mButtonPlus;
 	private ImageButton mButtonRefresh;
 	private ImageButton mButtonInfo;
 	private ImageButton mButtonSearch;
 	private ImageButton mButtonSetting;
 	private ImageButton mButtonSource;
 	private ImageButton mButtonMode;
 	private ImageButton mButtonTopStory;
 	
 	public String mSearchQuery;
 	private LinearLayout mSearchLayout;
 	private TextView mSearchView;
 	
 	private int oneHand;
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	private Context getContext() {
 		return this;
 	}
 	
 	@Override
 	public void onRestart(){
 		super.onRestart();
 		// TODO - fix the map problem
 		mMapView.zoomInMap();
 		mMapView.zoomOutMap();
 	}
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// initialize main MapActivity
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// initialize user preferences
 		initPrefs();
 		
 		// initialize UI
 		initMapView();
 		initOneHand();
 		initSlider();
 		initButtons();
 		initPopupPanel();
 		
 		// handle search requests
 		handleIntent(getIntent());
 		
 		// initialize Refresh processing object
 		initRefresh();
 		mMapView.setRefresh(mRefresh);
 		
 	}
 	//resize map
 	public void onWindowFocusChanged(boolean hasFocus) {
 		//get screen height
 		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
 		//get size of topbar
 		int topHeight = ((LinearLayout)findViewById(R.id.topbar)).getHeight();
 		//get size of bottombar
 		int botHeight = ((LinearLayout)findViewById(R.id.botbar)).getHeight();
 		//set mapview size to height - topbar - bottombar
 		LayoutParams p = mMapView.getLayoutParams();
 		//this adjusts the height without making a call to setlayoutparams
 		p.height = screenHeight - topHeight - botHeight;
 	}
 	
 	private void initOneHand(){
 		oneHand = Integer.valueOf (mPrefsSetting.getString("one_handed", "0"));
 	}
 	
 	public Refresh getRefresh() {
 		return mRefresh;
 	}
 	
 	public NewsStandMapView getMapView() {
 		return mMapView;
 	}
 	
 	public PopupPanel getPanel() {
 		return mPanel;
 	}
 	
 	public SharedPreferences getPrefsSetting() {
 		return mPrefsSetting;
 	}
 	
 	public SharedPreferences getPrefsSource() {
 		return mPrefsSources;
 	}
 	
 	public SeekBar getSlider() {
 		return mSlider;
 	}
 	
 	public ImageButton getHomeButton() {
 		return mButtonHome;
 	}
 	
 	public ImageButton getLocalButton() {
 		return mButtonLocal;
 	}
 	
 	public ImageButton getWorldButton() {
 		return mButtonWorld;
 	}
 	
 	public ImageButton getLocateButton() {
 		return mButtonLocate;
 	}
 	
 	public ImageButton getMinusButton() {
 		return mButtonMinus;
 	}
 	
 	public ImageButton getPlusButton() {
 		return mButtonPlus;
 	}
 	
 	public ImageButton getRefreshButton() {
 		return mButtonRefresh;
 	}
 	/** load default or saved prefs into prefs object **/
 	public void initPrefs() {
 		mPrefsSetting = getSharedPreferences("preferences", 0);
 		mPrefsSources = getSharedPreferences("sources", 0);
 	}
 	
 	@Override
 	public void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleIntent(intent);
 	}
 	
 	/** Handle incoming intents **/
 	private void handleIntent(Intent intent) {
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			addSearch(query);
 		}
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		mRefresh.clearSavedLocation();
 		mapUpdateForce();
 	}
 	
 	/** Delayed call to map refresh function.
 	 *
 	 *  Without delay, the refresh does not always happen. **/
 	public void mapUpdateForce() {
 		mapUpdateForce(250);
 	}
 	
 	public void mapUpdateForce(int ms) {
 		Handler mHandler = new Handler();
 		mHandler.postDelayed(mRefresh, ms);
 	}
 	
 	private void initMapView() {
 		mMapView = (NewsStandMapView) findViewById(R.id.mapview);
 		mMapView.setBuiltInZoomControls(false);
 		mMapView.initfSetHome(mPrefsSetting.getBoolean("set_home", false));
 	}
 	
 	private void initButtons(){
 		mButtonHome = (ImageButton) findViewById(R.id.home);
 		mButtonHome.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				mMapView.goHome();
 			}
 		});
 		
 		mButtonLocal = (ImageButton) findViewById(R.id.local);
 		mButtonLocal.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				mPanel.hide();
 				mMapView.goToCurrentLocation();
 			}
 		});
 		
 		mButtonWorld = (ImageButton) findViewById(R.id.world);
 		mButtonWorld.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				mPanel.hide();
 				mMapView.goWorld();
 				
 			}
 		});
 		
 		mButtonLocate = (ImageButton) findViewById(R.id.locate);
 		mButtonLocate.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				locate();
 			}
 		});
 		
 		
 		mButtonMinus = (ImageButton) findViewById(R.id.minus);
 		mButtonMinus.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				mMapView.zoomOutMap();
 			}
 			
 		});
 		
 		mButtonPlus = (ImageButton) findViewById(R.id.plus);
 		mButtonPlus.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				mMapView.zoomInMap();
 			}
 			
 		});
 		
 		mButtonRefresh = (ImageButton) findViewById(R.id.refresh);
 		mButtonRefresh.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				mapUpdateForce(1000);
 			}
 			
 		});
 		
 		mButtonInfo = (ImageButton) findViewById(R.id.buttonInfo);
 		mButtonInfo.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				Intent l = new Intent(v.getContext(), NewsStandWebView.class);
 				l = l.putExtra("url", "http://www.cs.umd.edu/~hjs/newsstand-first-page.html");
 				startActivity(l);
 			}
 			
 		});
 		
 		mButtonSearch = (ImageButton) findViewById(R.id.buttonSearch);
 		mButtonSearch.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				onSearchRequested();
 			}
 			
 		});
 		
 		mButtonSetting = (ImageButton) findViewById(R.id.buttonSetting);
 		mButtonSetting.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), Settings.class);
 				startActivity(i);
 			}
 			
 		});
 		
 		mButtonSource = (ImageButton) findViewById(R.id.buttonSource);
 		mButtonSource.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				Intent j = new Intent(v.getContext(), Sources.class);
 				startActivity(j);
 			}
 			
 		});
 		mButtonMode = (ImageButton) findViewById(R.id.buttonMode);
 		mButtonMode.setOnClickListener(new OnClickListener(){
 		
 		//	@Override
 			public void onClick(View v) {
 				Toast.makeText(getContext(), "Mode toggle not yet functional", Toast.LENGTH_SHORT).show();
 			}
 		
 		});
 		
 		mButtonTopStory = (ImageButton) findViewById(R.id.buttonTopStory);
 		mButtonTopStory.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				
 				//hide any open popup panel
 				if (mPanel != null && mRefresh != null)
 					mPanel.hide();
 				
 				mMapView.updateMapWindow();
 				Intent k = new Intent(v.getContext(), TopStories.class);
 				startActivity(k);
 			}
 			
 		});
 		
 		RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayoutButton);
 		
 		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(80,80);
 		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(80,80);
 		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(80,80);
 		if (oneHand == 0){
 			params1.leftMargin = params1.topMargin = 0;
 			rl.removeView(mButtonRefresh);
 			rl.addView(mButtonRefresh, params1);
 			params2.leftMargin = 400;
 			params2.topMargin = 0;
 			rl.removeView(mButtonPlus);
 			rl.addView(mButtonPlus, params2);
 			params3.leftMargin = 400;
 			params3.topMargin = 100;
 			rl.removeView(mButtonMinus);
 			rl.addView(mButtonMinus,params3);
 		}else if (oneHand == 1){
 			params1.leftMargin = params1.topMargin = 0;
 			rl.removeView(mButtonRefresh);
 			rl.addView(mButtonRefresh, params1);
 			params2.leftMargin = 350;
 			params2.topMargin = 0;
 			rl.removeView(mButtonPlus);
 			rl.addView(mButtonPlus, params2);
 			params3.leftMargin = 400;
 			params3.topMargin = 100;
 			rl.removeView(mButtonMinus);
 			rl.addView(mButtonMinus,params3);
 		} else {
 			params1.leftMargin = 400;
 			params1.topMargin = 0;
 			rl.removeView(mButtonRefresh);
 			rl.addView(mButtonRefresh, params1);
 			params2.leftMargin = 50;
 			params2.topMargin = 0;
 			rl.removeView(mButtonPlus);
 			rl.addView(mButtonPlus, params2);
 			params3.leftMargin = 0;
 			params3.topMargin = 100;
 			rl.removeView(mButtonMinus);
 			rl.addView(mButtonMinus,params3);
 		}
 		
 	}
 	
 	private void initSlider() {
 		mSlider = (SeekBar) findViewById(R.id.slider);
 		mSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				List<Overlay> mapOverlays = mMapView.getOverlays();
 				if (mapOverlays.size() > 0) {
 					MarkerOverlay o = (MarkerOverlay) mapOverlays.get(0);
 					o.setPctShown(progress, getApplicationContext());
 					mMapView.invalidate();
 				}
 			}
 			
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) { }
 			
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) { }
 		});
 	}
 	
 	private void initPopupPanel() {
 		mPanel = new PopupPanel(this, R.layout.popup_panel, mMapView, 0);
 	}
 	
 	private void initRefresh() {
 		mRefresh = new Refresh(this);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return false;
 //		MenuInflater inflater = getMenuInflater();
 //		inflater.inflate(R.menu.main_menu, menu);
 //		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 				
 			case R.id.search:
 				onSearchRequested();
 				break;        
 			case R.id.settings:
 				Intent i = new Intent(this, Settings.class);
 				startActivity(i);
 				break;
 			case R.id.sources:
 				Intent j = new Intent(this, Sources.class);
 				startActivity(j);
 				break;
 			case R.id.top_stories:
 				mMapView.updateMapWindow();
 				Intent k = new Intent(this, TopStories.class);
 				startActivity(k);
 				break;
 			case R.id.info:
 				Intent l = new Intent(this, NewsStandWebView.class);
 				l = l.putExtra("url", "http://www.cs.umd.edu/~hjs/newsstand-first-page.html");
 				startActivity(l);
 				// a web page , same as individual article. look at iphone code to get the url
 				break;
 			case R.id.mode:
 				// switch between twitterstand and newsstand
 				// break;
 				
 				//		default:
 				//			Toast.makeText(getApplicationContext(),
 				//					"Functionality not yet implemented now.", Toast.LENGTH_SHORT)
 				//					.show();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	
 	
 	public void addSearch(String query) {
 		mSearchQuery = query;
 		LinearLayout searchOptions = (LinearLayout)findViewById(R.id.search_options);
 		
 		mSearchLayout = new LinearLayout(this);
 		mSearchLayout.setOrientation(LinearLayout.HORIZONTAL);
 		mSearchLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		
 		TextView search_view = new TextView(this);
 		search_view.setText("Search: " + query);
 		search_view.setTextColor(Color.BLUE);
 		search_view.setTextSize(16);
 		search_view.setPadding(0, 0, 0, 100);
 		mSearchLayout.addView(search_view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
 																		 LinearLayout.LayoutParams.WRAP_CONTENT));
 		
 		// Instantiate an ImageView and define its properties
 		ImageView mSearchView = new ImageView(this);
 		mSearchView.setImageResource(R.drawable.ic_delete);
 		mSearchView.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
 		MarginLayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		lp.setMargins(3,6,0,0);
 		mSearchView.setLayoutParams(lp);
 		mSearchLayout.addView(mSearchView);
 		
 		mSearchLayout.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				clearSearch();
 			}
 		});
 		
 		searchOptions.addView(mSearchLayout);
 		mMapView.updateMapWindowForce();
 	}
 	
 	public void clearSearch () {
 		mSearchQuery = "";
 		LinearLayout searchOptions = (LinearLayout)findViewById(R.id.search_options);
 		searchOptions.removeView(mSearchLayout);
 		//		Toast.makeText(this, "Search cleared.", Toast.LENGTH_SHORT).show();
 		mMapView.updateMapWindowForce();
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v == mSearchView) {
 			clearSearch();
 		}
 	}
 	
 	public void locate() {
 		final EditText input = new EditText(this);
 		
 		new AlertDialog.Builder(this)
 		.setTitle("Location Query")
 		.setMessage("Enter location to go to:")
 		.setView(input)
 		.setPositiveButton("Go", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				Editable value = input.getText();
 				//Toast.makeText(getContext(), "You searched for: " + value, Toast.LENGTH_SHORT).show();
 				mPanel.hide();
 				mMapView.goToPlace(value.toString());
 			}
 		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Do nothing.
 			}
 		}).show();
 	}
 	
 	public void updateHome()
 	{
 		mMapView.updateHome(mPrefsSetting.getBoolean("set_home", false));	
 	}
 	
 	public void updateOneHand()
 	{
 		int oneHandNew = Integer.valueOf (mPrefsSetting.getString("one_handed", "0"));
 		if (oneHandNew != oneHand)
 		{
 			RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayoutButton);
 			
 			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(80,80);
 			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(80,80);
 			RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(80,80);
 			if (oneHand == 0){
 				params1.leftMargin = params1.topMargin = 0;
 				rl.removeView(mButtonRefresh);
 				rl.addView(mButtonRefresh, params1);
 				params2.leftMargin = 400;
 				params2.topMargin = 0;
 				rl.removeView(mButtonPlus);
 				rl.addView(mButtonPlus, params2);
 				params3.leftMargin = 400;
 				params3.topMargin = 100;
 				rl.removeView(mButtonMinus);
 				rl.addView(mButtonMinus,params3);
 			}else if (oneHand == 1){
 				params1.leftMargin = params1.topMargin = 0;
 				rl.removeView(mButtonRefresh);
 				rl.addView(mButtonRefresh, params1);
 				params2.leftMargin = 350;
 				params2.topMargin = 0;
 				rl.removeView(mButtonPlus);
 				rl.addView(mButtonPlus, params2);
 				params3.leftMargin = 400;
 				params3.topMargin = 100;
 				rl.removeView(mButtonMinus);
 				rl.addView(mButtonMinus,params3);
 			} else {
 				params1.leftMargin = 400;
 				params1.topMargin = 0;
 				rl.removeView(mButtonRefresh);
 				rl.addView(mButtonRefresh, params1);
 				params2.leftMargin = 50;
 				params2.topMargin = 0;
 				rl.removeView(mButtonPlus);
 				rl.addView(mButtonPlus, params2);
 				params3.leftMargin = 0;
 				params3.topMargin = 100;
 				rl.removeView(mButtonMinus);
 				rl.addView(mButtonMinus,params3);
 			}
 			
 			oneHand = oneHandNew;
 		}
 	}
 	
 }
