 package edu.umd.umiacs.newsstand;
 
 import java.net.URL;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewGroup.MarginLayoutParams;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.Overlay;
 
 import edu.umd.umiacs.newsstand.TopStoriesListView.TopStoryAdapter;
 
 
 
 public class TopStories extends MapActivity implements View.OnClickListener{
 
 	//private SharedPreferences _mPrefs;
 	private SharedPreferences mPrefsSetting;
 	private SharedPreferences mPrefsSources;
 	private TopStoriesMapView _mMapView;
 	private TopStoriesListView _mListView;
 	private SeekBar _mSlider;
 	private TopStoriesRefresh _mRefresh;
 	private PopupPanel _mPanel;
 	private MarkerOverlay mOverlay;
 	private TopStoriesFeed _mFeed;
 
 	public String mSearchQuery;
 	private LinearLayout mSearchLayout;
 	private TextView mSearchView;
 	
 	boolean fSetHome;
 	
 	private ImageButton mButtonMinus;
 	private ImageButton mButtonPlus;
 	private ImageButton mButtonRefresh;
 	private ImageButton mButtonInfo;
 	private ImageButton mButtonSearch;
 	private ImageButton mButtonSetting;
 	private ImageButton mButtonSource;
 	private ImageButton mButtonMode;
 	private ImageButton mButtonMap;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// initialize main MapActivity
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.topstory);
 
 		mOverlay = null;
 		
 		// initialize user preferences
 		initPrefs();
 		
 		// initialize UI
 		initMapView();
 		initSlider();
         initPopupPanel();        
         initFeed();
         initListView();
         initButtons();
         
         // initialize Refresh processing object
         initRefresh();
         _mMapView.setRefresh(_mRefresh);
 
         // handle search requests
         handleIntent(getIntent());
         
         fSetHome = mPrefsSetting.getString("set_home", "false").equals("true");        
 	}
 	
 	public MarkerOverlay getOverlay() {
 		return mOverlay;
 	}
 	
 	public void setOverlay(MarkerOverlay overlay) {
 		//if (mOverlay != null)
 		//	mOverlay.hideAllBalloons();
 		mOverlay = overlay;
 	}
 
     public TopStoriesRefresh getRefresh() {
         return _mRefresh;
     }
 
     public SharedPreferences getSettingPrefs(){
 		return mPrefsSetting;
 	}
     
     public SharedPreferences getSourcePrefs() {
     	return mPrefsSources;
     }
     
     public TopStoriesMapView getMapView() {
         return _mMapView;
     }
 	 
     public PopupPanel getPanel() {
         return _mPanel;
     }
 
     //public SharedPreferences refs() {
     //    return _mPrefs;
     //}
 
     public SeekBar getSlider() {
         return _mSlider;
     }
 
     /** load default or saved prefs into prefs object **/
 	public void initPrefs() {
 		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		//_mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
         //_mRefresh.clearSavedLocation();
         //mapUpdateForce();
        try {
         ((TopStoryAdapter)_mListView.getAdapter()).clickFirstVisible(_mListView.getFirstVisiblePosition());
        } catch (NullPointerException e) {
        	//unable to access newsstand server
        	Toast.makeText(getContext(), "Unable to access server", Toast.LENGTH_SHORT).show();
        }
     }
     
 	/** Delayed call to map refresh function.
 	 *
 	 *  Without delay, the refresh does not always happen. **/
 	
     public void mapUpdateForce() {
         mapUpdateForce(250);
     }
 
     public void mapUpdateForce(int ms) {
     	Handler mHandler = new Handler();
         mHandler.postDelayed(_mRefresh, ms);
     }
     
 	private void initMapView() {
 		_mMapView = (TopStoriesMapView) findViewById(R.id.topStoryView);
 		_mMapView.setBuiltInZoomControls(false);
 	}
 
 	///////////////////////////////////////////
 	private void initSlider() {	
 		_mSlider = (SeekBar) findViewById(R.id.topStorySlider);
         _mSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 List<Overlay> mapOverlays = _mMapView.getOverlays();
                 if (mapOverlays.size() > 0) {
                     MarkerOverlay o = (MarkerOverlay) mapOverlays.get(0);
                     o.setPctShown(progress, getApplicationContext());
                     _mRefresh.resetBound();
                     _mMapView.invalidate();
                 }
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) { }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) { }
         });
     }
 	/////////////////////////////////////////
 
 	private void initPopupPanel() {
         _mPanel = new PopupPanel(this, R.layout.popup_panel, _mMapView, 1);
     }
 	 
 	    private void initRefresh() {
         _mRefresh = new TopStoriesRefresh(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //MenuInflater inflater = getMenuInflater();
         //inflater.inflate(R.menu.main_menu, menu);
         //return true;
     	return false;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	return false;
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
         mSearchLayout.addView(search_view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                          LinearLayout.LayoutParams.WRAP_CONTENT));
 
         // Instantiate an ImageView and define its properties
         ImageView mSearchView = new ImageView(this);
         mSearchView.setImageResource(R.drawable.ic_delete);
         mSearchView.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
         MarginLayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         lp.setMargins(3,6,0,0);
         mSearchView.setLayoutParams(lp);
         mSearchView.setOnClickListener(this);
         mSearchLayout.addView(mSearchView);
 
         searchOptions.addView(mSearchLayout);
         Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
         _mMapView.updateMapWindowForce();
     }
     
     public void clearSearch () {
         mSearchQuery = "";
         LinearLayout searchOptions = (LinearLayout)findViewById(R.id.search_options);
         searchOptions.removeView(mSearchLayout);
         Toast.makeText(this, "Search cleared.", Toast.LENGTH_SHORT).show();
         _mMapView.updateMapWindowForce();
     }
 	
 
 	@Override
 	public void onClick(View v) {
 		if(v == mSearchView)
 			clearSearch();
 	}
 	
     public TopStories getContext() {
         return this;
     }
    
     public void initListView(){
     	_mListView = (TopStoriesListView) findViewById (R.id.topStoryList);
     	if(_mFeed != null)
     		_mListView.initAdapter(_mFeed);
     	_mListView.setOnScrollListener(new OnScrollListener() {
 
 			@Override
 			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
 				//do nothing
 			}
 
 			@Override
 			public void onScrollStateChanged(AbsListView list, int scrollState) {
 				//only trigger when the scrolling stops
 				if (scrollState == SCROLL_STATE_IDLE) {
 					((TopStoryAdapter)list.getAdapter()).clickFirstVisible(list.getFirstVisiblePosition() + 1);
 				}
 			}
     		
     	});
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public TopStoriesListView getListView() {
 		return _mListView;
 	}
 
 	public void updateMapView(String cluster_id) {
 		//hides popup panel when moving to new location
 		if (mOverlay != null)
 			mOverlay.hideAllBalloons();
 		if (_mPanel != null && _mRefresh != null)
 			_mPanel.hide();
 		if (_mRefresh != null)
 			_mRefresh.setClusterID(cluster_id);
 		
 	}
 	
 
 	
 	///////////////////////////////////
 	private TopStoriesFeed getFeed() {
 		try {
 			String topStory = "http://newsstand.umiacs.umd.edu/news/iphone_results?a=5";
 			
 			if (this.mSearchQuery != null && this.mSearchQuery != "") {
 				topStory += String.format("&search=%s", this.mSearchQuery);
 			}
 
 			// sourcesParam - TODO - need to implement
 
 			// rankParam - TODO - three types: &rank=time , &rank=newest , &rank=twitter . - need to implement
 			topStory += rankQuery();
 			
 			// topicParam - DONE
 			topStory += topicQuery();
 
 			// imagesParam - num of images
 			topStory += imageQuery();
 			// videosParam - num of videos
 			topStory += videoQuery();
 			
 			// boundParam TODO
 			// countryParam TODO
 			
 			// set up the url
 			URL url = new URL(topStory);
 
 			// create the factory
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			// create a parser
 			SAXParser parser = factory.newSAXParser();
 
 			// create the reader (scanner)
 			XMLReader xmlreader = parser.getXMLReader();
 			// instantiate our handler
 			TopStoriesHandler theMarkerFeedHandler = new TopStoriesHandler();
 			// assign our handler
 			xmlreader.setContentHandler(theMarkerFeedHandler);
 			// get our data via the url class
 			InputSource is = new InputSource(url.openStream());
 			// perform the synchronous parse
 			xmlreader.parse(is);
 			// get the results - should be a fully populated RSSFeed instance,
 			// or null on error
 			return theMarkerFeedHandler.getFeed();
 			
 		} catch (Exception ee) {
 			// if we have a problem, simply return null
 			//TODO - remove temp code
 			//Toast.makeText(this, "Error fetching markers",
 			//		Toast.LENGTH_SHORT).show();
 			return null;
 			
 			////////////TEMP TEST CODE ////////////////
 /*
 			TopStoriesFeed tempFeed = new TopStoriesFeed();
 			TopStoriesInfo tempInfo = new TopStoriesInfo();
 			tempInfo.setCluster_id("1");
 		    tempInfo.setDescription("description");
 		    tempInfo.setTitle("title");
 		    
 		    tempInfo.setTopic("general_topics");
 		    tempFeed.addItem(tempInfo);
 		    
 		    TopStoriesInfo two = new TopStoriesInfo();
 		    two.setCluster_id("1");
 		    two.setDescription("description: description should wrap around since it could be supper duper upper long long long tilte of this news that will never ever end and will continue like this forever and ever");
 		    two.setTitle("title: supper duper upper long long long tilte of this news that will never ever end and will continue like this forever and ever");
 		    
 		    two.setTopic("general_topics");
 		    tempFeed.addItem(two);
 		    
 		    ////////////TEMP TEST CODE ////////////////
 			return tempFeed;
 */
 		}
 	}
 	
 	
 	private String videoQuery(){
 		String video = mPrefsSetting.getString("videos", "0");
 		int videoInt = Integer.valueOf(video);
 		if(videoInt != 0)
 			return String.format("&num_videos=%s", video);
 		return "";
 	}
 
 	private String imageQuery(){
 		String image = mPrefsSetting.getString("images", "0");
 		int imageInt = Integer.valueOf(image);
 		if(imageInt != 0)
 			return String.format("&num_images=%s", image);
 		return "";
 	}
 	
 	private String rankQuery() {
 			String rank = mPrefsSources.getString("rank", "2");
 			int rankInt = Integer.valueOf(rank);
 			if(rankInt == 0)
 				return "&rank=time";
 			else if(rankInt == 1)
 				//return "&rank=reputable";
 				return "&rank=newest";
 			else if(rankInt == 2)
 				return "&rank=newest";
 			
 			return "";
 	}
 	
 
 	private String topicQuery() {
 		if (mPrefsSetting.getString("all_topics", "false").equals("true")) {
 			// add nothing to query string if showing all topics
 		} else {
 			String topics = ""; 
 			if (mPrefsSetting.getString("general_topics", "false").equals("true")) {
 				topics += "'General',";
 			}
 			if (mPrefsSetting.getString("business_topics", "false").equals("true")) {
 				topics += "'Business',";
 			}
 			if (mPrefsSetting.getString("scitech_topics", "false").equals("true")) {
 				topics += "'SciTech',";
 			}
 			if (mPrefsSetting.getString("entertainment_topics", "false").equals("true")) {
 				topics += "'Entertainment',";
 			}
 			if (mPrefsSetting.getString("health_topics", "false").equals("true")) {
 				topics += "'Health',";
 			}
 			if (mPrefsSetting.getString("sports_topics", "false").equals("true")) {
 				topics += "'Sports',";
 			}
 			if (topics.length() > 0) {
 				return String.format("&cat=(%s)", topics.substring(0, topics.length()-1));
 			}
 		}
 		return "";
 	}
 
 	private void initFeed(){
 		_mFeed = getFeed();
 	}
 	
 	private void initButtons(){
 		/*mButtonMinus = (ImageButton) findViewById(R.id.minus);
 		mButtonMinus.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				//_mMapView.zoomOutMap();
 			}
 			
 		});
 		
 		mButtonPlus = (ImageButton) findViewById(R.id.plus);
 		mButtonPlus.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				//_mMapView.zoomInMap();
 			}
 			
 		});
 		
 		mButtonRefresh = (ImageButton) findViewById(R.id.refresh);
 		mButtonRefresh.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				mapUpdateForce(1000);
 			}
 			
 		});*/
 		
 		mButtonInfo = (ImageButton) findViewById(R.id.buttonInfo2);
 		mButtonInfo.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				Intent l = new Intent(v.getContext(), NewsStandWebView.class);
 				l = l.putExtra("url", "http://www.cs.umd.edu/~hjs/newsstand-first-page.html");
 				startActivity(l);
 			}
 			
 		});
 		
 		mButtonSearch = (ImageButton) findViewById(R.id.buttonSearch2);
 		mButtonSearch.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				//onSearchRequested();
 				Toast.makeText(getContext(), "Search Disabled for TopStories mode", Toast.LENGTH_SHORT).show();
 			}
 			
 		});
 		
 		mButtonSetting = (ImageButton) findViewById(R.id.buttonSetting2);
 		mButtonSetting.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), Settings.class);
 				startActivity(i);
 			}
 			
 		});
 		
 		mButtonSource = (ImageButton) findViewById(R.id.buttonSource2);
 		mButtonSource.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				Intent j = new Intent(v.getContext(), Sources.class);
 				startActivity(j);
 			}
 			
 		});
 		mButtonMode = (ImageButton) findViewById(R.id.buttonMode2);
 		mButtonMode.setOnClickListener(new OnClickListener(){
 		
 		//	@Override
 			public void onClick(View v) {
 				Toast.makeText(getContext(), "Mode toggle not yet functional", Toast.LENGTH_SHORT).show();
 			}
 		
 		});
 		
 		mButtonMap = (ImageButton) findViewById(R.id.buttonTopStory2);
 		mButtonMap.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				
 				//_mMapView.updateMapWindow();
 				//Intent k = new Intent(v.getContext(), TopStories.class);
 				//startActivity(k);
 				finish();
 			}
 			
 		});
 		
 		/*RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayoutButton);
 		
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
 		}*/
 		
 	}
  
 //	@Override
 //	public void onBackPressed(){
 //	}
 }
