 package com.vn.plaudible;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.database.SQLException;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.vn.plaudible.db.NewsSpeakDBAdapter;
 import com.vn.plaudible.tts.SpeechService;
 import com.vn.plaudible.types.NewsSource;
 
 /**
  * The Screen that displays the list of all newspapers and blogs
  * @author vamsi
  *
  */
 public class NewsSourcesActivity extends ListActivity implements TextToSpeech.OnInitListener {
 	
 	private ArrayList<NewsSource> allSources;
 	
 	private NewsSourcesAdapter adapter;
 	private NewsSpeakDBAdapter mDbAdapter;
 	private SpeechService mSpeechService;
 	private EditText filterText;
 
 	private TextToSpeech ttsEngine;
 	
 	private static final int INSTANT_NEWS_SEARCH_POSITION = 0;
 	private static final int TTS_INSTALLED_CHECK_CODE = 1;
 	
 	static class ViewHolder {
 		ImageView image;
 		TextView text;
 		Integer position;
 	}
 	
 	/**
 	 * Called on activity creation
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_page);
         
         allSources = new ArrayList<NewsSource>();
         adapter = new NewsSourcesAdapter(this, R.layout.main_page_list_item, allSources);
         setListAdapter(adapter);
         
         getListView().setTextFilterEnabled(true);
         
         filterText = (EditText) findViewById(R.id.search_box);
         filterText.addTextChangedListener(filterTextWatcher);
 
 		if (mDbAdapter == null) {
 			mDbAdapter = new NewsSpeakDBAdapter(this);
 		    mDbAdapter.open(NewsSpeakDBAdapter.READ_WRITE);
 		}
 		
 		// If it is the firstRun then prepare NewsSpeak
 		// Prepare the DB if this is the first run.
 		// Place a call to AppEngine to get the list of preferred NewsSources for this user
 		if (isFirstRun()) {
 			performFirstRunInitialization();
 		}
 		
         checkAndInstallTTSEngine();
         bindSpeechService();
         
         // Set the context for Utils
         Utils.setContext(this);
     }
 	
 	/**
      *  Check for presence of a TTSEngine and install if not found
      */
     protected void checkAndInstallTTSEngine() {
 	    Intent checkIntent = new Intent();
 	    checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 	    startActivityForResult(checkIntent, TTS_INSTALLED_CHECK_CODE);
     }
     
     /**
      *  Called for the intent which checks if TTS was installed and starts TTS up
      */
     protected void onActivityResult(
             int requestCode, int resultCode, Intent data) {
         if (requestCode == TTS_INSTALLED_CHECK_CODE) {
             if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                 // success, create the TTS instance
                 ttsEngine = new TextToSpeech(this, this);
             } else {
                 // missing data, install it
                 Intent installIntent = new Intent();
                 installIntent.setAction(
                     TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                 startActivity(installIntent);
             }
         }
     }
     
     /**
      *  OnInitListener for TTSEngine initialization
      *  Check if the Service is bound and if it is then we can set the TTS Engine it should use
      */
 	@Override
 	public void onInit(int status) {
 		if (status == TextToSpeech.SUCCESS) {
 	           ttsEngine.setLanguage(Locale.US);	            
 	           
 	           if (mSpeechService != null) {
 	        	   mSpeechService.initializeSpeechService(this.ttsEngine);
 	           }
 		}
 	}
 	
 	private void performFirstRunInitialization() {
 		// Start a spinning wheel to show we are busy
 		ProgressDialog spinningWheel = ProgressDialog.show(NewsSourcesActivity.this, 
 										"Please wait",
 										"We are preparing your news list",
 										true);
 		spinningWheel.show();
 		
 		new PlaudibleAsyncTask().execute(
 							new PlaudibleAsyncTask.Payload(
 		        				PlaudibleAsyncTask.FEATURED_SOURCES_DOWNLOAD_TASK,
 		        				new Object[] { NewsSourcesActivity.this,
 		        								spinningWheel }));
 
 		// We can now know that we successfully completed the first run, so set it
 		setFirstRun();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		// Clear the adapter data first
 		allSources.clear();
 		
         try {
         	populateSubscribedSourcesFromDB();
         } catch (SQLException exception) {
         	exception.printStackTrace();
         }
         
         // Sort the sources as per the display index
 	    Collections.sort(allSources, NewsSource.DISPLAYINDEX_ORDER);
 	    adapter.notifyDataSetChanged();
 	    
 		filterText.setText("");
 	}
 	
 	@Override
 	protected void onDestroy() {
 		unBindSpeechService();
 		filterText.removeTextChangedListener(filterTextWatcher);
 
 		if (mDbAdapter != null) {
 			mDbAdapter.close();
 		}
 		super.onDestroy();
 	}
 	
     /**
      *  Listen for configuration changes and this is basically to prevent the 
      *	activity from being restarted. Do nothing here.
      */
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 	
 	/**
 	 * Populate the sources from the DB
 	 * We are NOT fetching the entire NewsSource object rather
 	 * only the title and the display index which is what we need
 	 * at this activity level. Do lazy fetch when people click
 	 * on an article.
 	 */
 	private void populateSubscribedSourcesFromDB() throws SQLException {
 		// Get only titles and displayIndexes for subscribed NewsSources
 		mDbAdapter.fetchAllNewsPapers(allSources, " NAME, TYPE, DISPLAYINDEX ", false); 			
 	}
 	
 	/**
      * Populate NewsSources in the DB on first time usage.
      * The URL to fetch the JSON results are computed elsewhere
      * and then we get all the sources to begin with. Then we enter them
      * into the DB, but before that we must check the preferred sources
      * and give them the special treatment
      */
     protected void populateSourcesIntoDB() {
     	NewsSpeakDBAdapter dbAdapter = new NewsSpeakDBAdapter(this);
     	dbAdapter.open(NewsSpeakDBAdapter.READ_WRITE);
     	
     	// Clean up the DB with any artifacts
     	dbAdapter.upgrade();
     	
     	// Make a call to AppEngine and get the featured sources
     	String link = getURLForFeaturedSources();
     	
     	try {
         	URL feedUrl = new URL(link);
 
         	// Parse the response stream from AppEngine
 	    	ArrayList<NewsSource> sources = new ArrayList<NewsSource>();
 	    	InputStream responseStream = feedUrl.openConnection().getInputStream();
 			
 			// Construct the Array of sources from the JSON String
 			sources = NewsSource.getNewsSourcesFromJSON(Utils.getStringFromInputStream(responseStream));
 			
 			// Insert the NewsSources into the localDB
 			for (int index = 0; index < sources.size(); ++index) {
 				// Set the display index
 				sources.get(index).setDisplayIndex(index);
 				
 				// Here we treat preferred sources differently
 				if (sources.get(index).isPreferred()) {
 					NewsSource.createPreferred(sources.get(index));
 				}
 				
 				// Insert into DB
 				dbAdapter.createNewsSource(sources.get(index));
 			}
 			
     	} catch (Exception e) {
     		e.printStackTrace();
     	} finally {
     		dbAdapter.close();
     	}
     }
 
     /**
      * We must construct the URL for the featured sources which are
      * used first time the app are called
      * @return
      */
 	private String getURLForFeaturedSources() {
 		String country = Locale.getDefault().getCountry();
 		String language = Locale.getDefault().getLanguage();
 		
 		String url = getString(R.string.appengine_url) + "/featuredSources?";
 		url += "country=" + country;
 		url += "&language=" + language;
 		
 		return url;
 	}
 
 	/**
      * Find out if this is the first run of NewsSpeak
      * @return
      */
     protected boolean isFirstRun() {
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	boolean result = prefs.getBoolean("FirstRun", true);
     	
      	return result;
 	}
     
     /**
      * Set the first run on the successful completion of the first run
      * @return
      */
     protected boolean setFirstRun() {
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = prefs.edit();
 		
 		editor.putBoolean("FirstRun", false);
 		editor.commit();
 	
 		return true;
 	}
 	
 	/**
 	 * Array adapter class for the listView
 	 * @author vamsi
 	 *
 	 */
 	private class NewsSourcesAdapter extends ArrayAdapter<NewsSource> implements View.OnClickListener, Filterable {
 			
 		private ArrayList<NewsSource> filteredSources;
 		private Context context;
 		private NewsSourcesFilter sourcesFilter;
 		
 		public NewsSourcesAdapter(Context context, int viewResourceId, ArrayList<NewsSource> newssources) {
 			super(context, viewResourceId, newssources);
 			
 			this.filteredSources = newssources;
 			this.context = context;
 			
 			setNotifyOnChange(true);
 		}
 		
 		/**
 		 * Create a custom filter
 		 */
 		@Override
 		public Filter getFilter() {
 			if (sourcesFilter == null) {
 				sourcesFilter = new NewsSourcesFilter();
 			}
 			
 			return sourcesFilter;
 		}
 	
 		/**
 		 * Get item's View
 		 */
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 			
 			if (convertView == null) {
 				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = inflater.inflate(R.layout.main_page_list_item, null);
 				
 				holder = new ViewHolder();
 				holder.image = (ImageView) convertView.findViewById(R.id.newsPaperImages);
 				holder.text = (TextView) convertView.findViewById(R.id.NewsPaperTitle);
 				
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 			NewsSource source = getItem(position);
 			
 			holder.position = position;
 			holder.text.setText(source.getTitle());
 			
 			// Decide on the appropriate icon
 			if (source.getType() == NewsSource.SourceType.NEWSPAPER) {
 				holder.image.setImageResource(R.drawable.newspaper_icon);
 			} else if (source.getType() == NewsSource.SourceType.BLOG) {
 				holder.image.setImageResource(R.drawable.blog_icon);
 			}
 			
 			convertView.setOnClickListener(this);
 			return convertView;
 		}
 		
 		/**
 		 * Return the count of the filtered items
 		 */
 		public int getCount() {
 			return filteredSources.size();
 		}
 		
 		/**
 		 * Get item from the filtered sources
 		 */
 		public NewsSource getItem(int position) {
 			return filteredSources.get(position);
 		}
 		
 		public long getItemId(int position) {
 			return position;
 		}
 
 		/**
 		 * The onClick method, which starts Plaudible
 		 */
 		@Override
 		public void onClick(View view) {
 			// If no data link then just don't open Plaudible activity. Best to stop it here.
 			if (!Utils.checkDataConnectivity(NewsSourcesActivity.this)) {
 				Toast butterToast = Toast.makeText(this.getContext(), R.string.connection_unavailable, Toast.LENGTH_SHORT);
 				butterToast.show();
 				return;
 			}
 			
 			// Start a spinning wheel
 			Utils.showSpinningWheel(NewsSourcesActivity.this, "", getString(R.string.loading_articles));
 			
 			ViewHolder holder = (ViewHolder) view.getTag();
 			NewsSource source = getItem(holder.position);
 			
			if (!(filterText.getText().length() > 0 && holder.position == INSTANT_NEWS_SEARCH_POSITION)) {
 				// Our NewsSource objects are incomplete. Fetch the full NewsSource now.
 				source = mDbAdapter.getNewsPaper(source.getTitle());
 			}
 			
 			// Start Plaudible
 			Intent listArticlesInFeed = new Intent();
 			listArticlesInFeed.setClass(context, FeedViewerActivity.class);
 			listArticlesInFeed.putExtra(FeedViewerActivity.INTENT_NEWSSOURCE, source);
 			
 			context.startActivity(listArticlesInFeed);
 		}
 		
 		/**
 		 * Class to implement filter on the array adapter we use to 
 		 * show the news sources. We need a custom implementation
 		 * since Articles are 'complex'.
 		 * @author vamsi
 		 *
 		 */
 		private class NewsSourcesFilter extends Filter {
 
 			/**
 			 * Called on UI thread, to perform the filtering.
 			 * @param constraint
 			 * @return
 			 */
 			@Override
 			protected FilterResults performFiltering(CharSequence constraint) {
 				FilterResults results = new FilterResults();
 				ArrayList<NewsSource> subSources = new ArrayList<NewsSource>();
 				
 				// Only if constraint string is not empty
 				if (constraint != null && constraint.toString().length() > 0) {
 					Pattern regex = Pattern.compile((String) constraint, Pattern.CASE_INSENSITIVE);
 					
 					// Go through 'all' the sources and perform filtering
 					for (int index = 0; index < allSources.size(); ++index) {
 						NewsSource source = allSources.get(index);
 						Matcher m = regex.matcher(source.getTitle());
 						if (m.find()) {
 							subSources.add(source);
 						}
 					}
 					
 					// Add the Google News source to the resulting filtered sets
 					subSources.add(INSTANT_NEWS_SEARCH_POSITION, Utils.generateGoogleNewsSource(constraint.toString()));
 					
 					// Adding into the result set
 					results.values = subSources;
 					results.count = subSources.size();
 				} else {
 					// If no constraint then results must be same as all the sources
 					synchronized (allSources){
 						results.values = allSources;
 						results.count = allSources.size();
 					}
 				}
 				
 				return results;
 			}
 
 			/**
 			 * Publish the results to the array adapter
 			 * @param constraint
 			 * @param results
 			 */
 			@SuppressWarnings("unchecked")
 			@Override
 			protected void publishResults(CharSequence constraint, FilterResults results) {
 				filteredSources = (ArrayList<NewsSource>) results.values;
 				adapter.notifyDataSetChanged();
 			}
 		}
 	}
 	
 	/**
 	 * Listener for edit text changes to perform filtering
 	 */
 	private TextWatcher filterTextWatcher = new TextWatcher() {
 
 	    public void afterTextChanged(Editable s) {
 	    }
 
 	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 	    }
 
 	    /**
 	     * Listen for character by character updates and call filtering and notify the adapter
 	     */
 	    public void onTextChanged(CharSequence s, int start, int before, int count) {
 	        adapter.getFilter().filter(s);
 	        adapter.setNotifyOnChange(true);
 	    }
 	};
 	
 	
 	/**
 	 * Connection to the Service. All Activities must have this.
 	 */
 	private ServiceConnection mConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			mSpeechService = ((SpeechService.SpeechBinder)service).getService();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			mSpeechService = null;
 		}
 	};
 	
 	/**
 	 * Bind to the Speech Service. Called from onCreate() on this activity
 	 */
 	void bindSpeechService() {
 		this.bindService(new Intent(NewsSourcesActivity.this, SpeechService.class), mConnection, Context.BIND_AUTO_CREATE);
 	}
 	
 	/**
 	 * Unbind from the Speech Service. Called from onDestroy() on this activity
 	 */
 	void unBindSpeechService() {
 		if (mSpeechService != null) {
 			this.unbindService(mConnection);
 		}
 	}	
 }
