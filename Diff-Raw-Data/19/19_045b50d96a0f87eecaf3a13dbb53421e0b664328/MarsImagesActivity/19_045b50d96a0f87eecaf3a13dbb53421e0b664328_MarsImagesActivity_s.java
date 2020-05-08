 package gov.nasa.jpl.hi.marsimages;
 
 import static gov.nasa.jpl.hi.marsimages.MarsImagesApp.MARS_IMAGE_FILENAME;
 import static gov.nasa.jpl.hi.marsimages.MarsImagesApp.MARS_PLOT_FILENAME;
 import static gov.nasa.jpl.hi.marsimages.MarsImagesApp.TAG;
 import gov.nasa.jpl.hi.marsimages.activity.AboutThisAppActivity;
 import gov.nasa.jpl.hi.marsimages.activity.CoursePlotActivity;
 import gov.nasa.jpl.hi.marsimages.view.FullscreenImageView;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.SearchRecentSuggestions;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 import com.actionbarsherlock.view.Window;
 import com.evernote.edam.type.Note;
 import com.powellware.marsimages.R;
 
 public class MarsImagesActivity extends SherlockFragmentActivity {
 
 	private final static NumberFormat num4 = DecimalFormat.getIntegerInstance();
 	static {
 		num4.setGroupingUsed(false);
 		num4.setMinimumIntegerDigits(4);
 	}
 	private Menu mOptionsMenu;
 	private String initialQuery = "";
 	private String searchWordsBeforePause;
 	protected ImageListFragment mImageListFragment;
 	protected ImagePreviewFragment mImagePreviewFragment;
 	
 	SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = 
 			new SharedPreferences.OnSharedPreferenceChangeListener() {
 		public void onSharedPreferenceChanged(SharedPreferences prefs,
 				String key) {
 			String mission = prefs.getString("mission", "default");
 			if (!MarsImagesApp.getMission().equals(mission)) {
 				MarsImagesApp.searchWords = ""; //clear any previous search
 				MarsImagesApp.setMission(mission);
 				setTitle("Mars Images: "+mission);
 				mImageListFragment.refresh();
 			}
 		}
 	};
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);       
         
         // Get the intent, see if it is a search, and set the NoteFilter words for a specific search
         Intent intent = getIntent();
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
           String query = intent.getStringExtra(SearchManager.QUERY);
           doSearch(query);
           SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                   SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
           suggestions.saveRecentQuery(query, null);
           finish();
         }
         
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
         SharedPreferences sharedPreferences = getSharedPreferences("com.powellware.marsimages_preferences", Context.MODE_PRIVATE);
 		String mission = sharedPreferences.getString("mission", "default");
 		MarsImagesApp.setMission(mission);
 		setTitle("Mars Images: "+mission);
         setContentView(R.layout.activity_mars_images);
         mImageListFragment = (ImageListFragment) getSupportFragmentManager().findFragmentById(R.id.imageList);
         mImagePreviewFragment = (ImagePreviewFragment) getSupportFragmentManager().findFragmentById(R.id.imagePreview);
     }
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
 	}
 	
 	@Override
 	protected void onStop() {
 	   	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	   	prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
 	   	super.onStop();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		MarsImagesApp.pausedTime = System.currentTimeMillis();
 		searchWordsBeforePause = MarsImagesApp.searchWords;
 	}
 	
 	/**
 	 * Activity comes to foreground after start or pause.
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		setSupportProgressBarIndeterminateVisibility(false);
 		
 		//if search was performed, update the image results
         if (!MarsImagesApp.searchWords.equals(searchWordsBeforePause) && mImageListFragment != null) {
         	mImageListFragment.refresh(false);
         }
 		
 		//refresh image list on resume after pause for more than 30 minutes
 		long pausedTime = MarsImagesApp.pausedTime;
 		long pauseDurationSeconds = (System.currentTimeMillis()-pausedTime)/1000;
 		Log.i(TAG, "resumed after pause of "+pauseDurationSeconds+" seconds.");
 		if (pausedTime != 0 && pauseDurationSeconds > 30*60) {
 			mImageListFragment.refresh(false);
 		}
 		
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_SEARCH)
 			openSearchDialog();
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	/**
 	 * Create our menu items.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) { //sherlock
 		mOptionsMenu = createOptionsMenu(menu);
         
 		return true;
 	}
 	
 	public Menu createOptionsMenu(Menu menu) {
 		menu.clear();
 		if (MarsImagesApp.getMission().equals(MarsImagesApp.OPPORTUNITY_MISSION) ||
 				MarsImagesApp.getMission().equals(MarsImagesApp.SPIRIT_MISSION)) {
 			//use the version of the menu that includes the latest course plot: MER-only
 			getSupportMenuInflater().inflate(R.menu.image_menu_merb, menu);
 			menu.findItem(R.id.latest_plot).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		} else {
 			//use the version of the menu without the course plot action
 			getSupportMenuInflater().inflate(R.menu.image_menu, menu);
 		}
 		menu.findItem(R.id.refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		menu.findItem(R.id.about).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		menu.findItem(R.id.search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
         SubMenu missionsMenu = menu.addSubMenu("Missions");
 		getSupportMenuInflater().inflate(R.menu.mission_preferences_menu, missionsMenu);
 
         MenuItem missionsMenuItem = missionsMenu.getItem();
         missionsMenuItem.setIcon(R.drawable.rover_prefs);
         missionsMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         
         if(MarsImagesApp.getMission().equals(MarsImagesApp.SPIRIT_MISSION)) {
         	missionsMenu.findItem(R.id.spirit).setChecked(true);
         } else if(MarsImagesApp.getMission().equals(MarsImagesApp.OPPORTUNITY_MISSION)) {
         	missionsMenu.findItem(R.id.opportunity).setChecked(true);
         } else {
         	missionsMenu.findItem(R.id.curiosity).setChecked(true);
         }
 		return menu;
 	}
 
 	/** 
 	 * Handle menu item selection.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) { //sherlock
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = settings.edit();
 		
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			mImageListFragment.refresh();
 			return true;
 		case R.id.about:
 			createAboutThisAppActivity();
 			return true;
 		case R.id.search:
 			openSearchDialog();
 			return true;
 		case R.id.latest_plot:
 			
 			new AsyncTask<String, Void, Bitmap>() {
 				@Override
 				protected Bitmap doInBackground(String... strings) {					
 					Bitmap plotImage = null;
 					String latestUrl = strings[0];
 					
 					try {
 						InputStream in = new java.net.URL(latestUrl).openStream();
 						plotImage = BitmapFactory.decodeStream(in);
 					} catch (MalformedURLException e) {
 						Log.e(TAG, "SOL URL not formatted properly");
 						e.printStackTrace();
 					} catch (IOException e) {
 						Log.e(TAG, e.toString());
 						e.printStackTrace();
 					}
 					return plotImage;
 				}
 				
 				/**
 				 * Show plot image.
 				 */
 				@Override
 				protected void onPostExecute(Bitmap plotImage) {
 					ByteArrayOutputStream stream = new ByteArrayOutputStream();
 					plotImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
 					byte[] byteArray = stream.toByteArray();
 					createPlotMapViewActivity(byteArray);
 				}
 				
 			}.execute("http://merpublic.s3.amazonaws.com/oss/merb/ops/ops/surface/tactical/sol/latest/sret/mobidd/mot-all-report/cache-mot-all-report/hyperplots/raw_north_vs_raw_east.png");
 			return true;
 		case R.id.spirit: 
 			editor.putString("mission", MarsImagesApp.SPIRIT_MISSION);
 			editor.commit();
 			createOptionsMenu(mOptionsMenu);
 			return true;
 		case R.id.opportunity:
 			editor.putString("mission", MarsImagesApp.OPPORTUNITY_MISSION);
 			editor.commit();
 			createOptionsMenu(mOptionsMenu);
 			return true;
 		case R.id.curiosity:
 			editor.putString("mission", MarsImagesApp.CURIOSITY_MISSION);
 			editor.commit();
 			createOptionsMenu(mOptionsMenu);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Start plot map view activity with its Intent.
 	 * @param plotMapBytes the plot to view
 	 */
 	protected void createPlotMapViewActivity(byte[] plotMapBytes) {
 		Intent touchViewIntent = new Intent(CoursePlotActivity.INTENT_ACTION_PLOT_VIEW);
 		// write the JPEG image data for the image view to the cache file area 
 		try {
 			FileOutputStream fileOutputStream = new FileOutputStream(
 					new File(getApplication().getCacheDir(), MARS_PLOT_FILENAME));
 			fileOutputStream.write(plotMapBytes);
 			fileOutputStream.flush();
 			fileOutputStream.close();
 
 			startActivity(touchViewIntent);
 		} catch (IOException e) {
 			Log.e(TAG, "Unable to write to internal storage to start FullscreenImageView");
 		}
 	}
 	
 	private void openSearchDialog() {
 		startSearch(initialQuery, true, null, false);
 	}
 
 	private void doSearch(String query) {
 		initialQuery = query;
 		String tokens[] = query.split(" ");
 		ArrayList<String> words = new ArrayList<String>();
 		for (String string : tokens) {
 			try {
 				int sol = Integer.parseInt(string); //a sol number
 				if (sol >= 0) {
 					words.add("\"Sol "+num4.format(sol)+"\"");
 				}
 			} catch (NumberFormatException e) { //not a sol number, but a word like Hazcam, Front, Full, etc.
 				if (!string.toLowerCase(Locale.US).equals("sol"))
 					words.add(string);
 			}
 		}
 		StringBuffer searchWords = new StringBuffer();
 		for (String string : words) {
 			searchWords.append(string).append(" ");
 		}
 		MarsImagesApp.searchWords = searchWords.toString().trim();
 	}
 
 	protected void createAboutThisAppActivity() {
 		Intent aboutThisAppIntent = new Intent(AboutThisAppActivity.INTENT_ACTION_ABOUT_THIS_APP);
 		startActivity(aboutThisAppIntent);
 	}
 
 	public ImageListFragment getImageListFragment() {
 		return mImageListFragment;
 	}
 
 	/**
 	 * Start fullscreen image view activity with its Intent.
 	 * @param selectedImageBytes the image to view
 	 */
 	protected void createFullscreenImageViewActivity(byte[] selectedImageBytes) {
 		Intent touchViewIntent = new Intent(FullscreenImageView.INTENT_ACTION_TOUCH_VIEW);
 		// write the JPEG image data for the image view to the cache file area 
 		try {
 			FileOutputStream fileOutputStream = new FileOutputStream(
 					new File(getApplication().getCacheDir(), MARS_IMAGE_FILENAME));
 			fileOutputStream.write(selectedImageBytes);
 			fileOutputStream.flush();
 			fileOutputStream.close();
 			
 			if (mImagePreviewFragment != null) {
 				 mImagePreviewFragment.setPreviewImage(null);
 			}
 			startActivity(touchViewIntent);
 		} catch (IOException e) {
 			Log.e(TAG, "Unable to write to internal storage to start FullscreenImageView");
 		}
 	}
 
 	/**
 	 * Reusable image preview fragment. Only shown if there is enough screen area.
 	 * Creates an image view with a Progress indicator for image fetching feedback.
 	 * If you select it, it will open a full-screen interactive image view.  
 	 */
 	public static class ImagePreviewFragment extends SherlockFragment {
 
 		private ImageView imageView;
 		private ProgressBar imageProgress;

 		public AlertDialog alertDialog;
 
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			FrameLayout layout = new FrameLayout(getActivity());
 			imageView = new ImageView(getActivity());
 			imageView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
 			layout.addView(imageView);
 			imageView.setClickable(true);
 			imageView.setOnClickListener(imagePreviewClickToCreateFullscreenImageViewListener);
 			imageProgress = new ProgressBar(getActivity());
 			imageProgress.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
 			imageProgress.setIndeterminate(true);
 			imageProgress.setVisibility(View.INVISIBLE);
 			layout.addView(imageProgress);
 			return layout;
 		}
 
 		@Override
 		public void onResume() {
 			super.onResume();
 			if (imageView != null) {
 				byte[] selectedImageBytes = ((MarsImagesApp)getActivity().getApplication()).getSelectedImageBytes();
 				if (selectedImageBytes != null) {
 					setPreviewImage(selectedImageBytes);
 				}
 			}
 		}
 		
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			setPreviewImage(null);
 			if (alertDialog != null) {
 				alertDialog.dismiss();
 				alertDialog = null;
 			}
 		}
 
 		public void setPreviewImage(byte[] result) {
 			//free any existing bitmap memory ASAP
 			if (imageView == null) {
 				return;
 			}
 			Drawable drawable = imageView.getDrawable();
 			imageView.setImageBitmap(null);
 			if (drawable != null && drawable instanceof BitmapDrawable) {
 				Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
 				if (bitmap != null) {
 					bitmap.recycle();
 				}
 				drawable = null;
 			}
 			
 			try {
 				if (result != null) {
 					Options options = new BitmapFactory.Options();
 					options.inScaled = false;
 					imageView.setImageBitmap(BitmapFactory.decodeStream(new ByteArrayInputStream(result), null, options));
 				}
 			} catch (OutOfMemoryError e) {
 				alertDialog = new AlertDialog.Builder(getActivity()).create();
 				alertDialog.setTitle("Insufficient memory");
 				alertDialog.setMessage("Sorry, there is not enough memory to show the preview image.");
 				alertDialog.show();
 			}
 		}
 
 		private OnClickListener imagePreviewClickToCreateFullscreenImageViewListener = new OnClickListener() {
 			public void onClick(View v) {
 				Log.i(TAG, "imageview clicked");
 				byte[] selectedImageBytes = ((MarsImagesApp)getActivity().getApplication()).getSelectedImageBytes();
 				if (selectedImageBytes != null) {
					((MarsImagesActivity)getActivity()).createFullscreenImageViewActivity(selectedImageBytes);
 				}
 			}
 		};
 
 		public ImageView getImageView() {
 			return imageView;
 		}
 	}
 
 	/**
 	 * Reusable list view fragment to display image details. List items are a
 	 * custom widget with a thumbnail image area and two lines of text description.
 	 * The scroll listener detects when the end of the list is displayed and starts
 	 * a thread to load more images, creating an infinite-scrolling effect.
 	 */
 	public static class ImageListFragment extends SherlockListFragment {
 
 		private static final String OFFSET_REQUESTED = "offsetRequested";
 		private static final int NOTE_BATCH_SIZE = 10;
 		private static final String LIST_VIEW_POSITION = "listViewPosition";
 		protected static final Object LOAD_MORE_LOCK = new Object();
 
 		private NoteListAdapter noteListAdapter;
 		private int offsetRequested = -1;
 		private List<Note> moreNotes; //temporary container for newly-fetched notes
 		private AlertDialog alertDialog;
 		
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			setRetainInstance(true); //make this fragment long-lived between activity instances
 		}
 
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			if (alertDialog != null) {
 				alertDialog.dismiss();
 				alertDialog = null;
 			}
 		}
 		
 		/**
 		 * Clear notes and any previous search and reload all from server.
 		 * Activated by refresh button.
 		 */
 		public void refresh() {
 			this.refresh(true);
 		}
 		
 		/**
 		 * Clear notes and reload from server.
 		 * @param clearSearch if true, clear any previously entered search words prior to refresh
 		 */
 		public void refresh(final boolean clearSearch) {
 			if (getActivity() == null)
 				return;
 			
 			getActivity().runOnUiThread(new Runnable() {
 				public void run() {
 					if (clearSearch) {
 						MarsImagesApp.searchWords = ""; //clear any previous search
 					}
 					noteListAdapter.clear();
 					offsetRequested = -1;
 					noteListAdapter.notifyDataSetChanged();
 				}
 			});
 		}
 
 		/**
 		 * Set up a image list view when our activity is created.
 		 */
 		@Override
 		public void onActivityCreated(Bundle savedInstanceState) {
 			super.onActivityCreated(savedInstanceState);
 
 			ListView listView = getListView();
 			listView.setItemsCanFocus(false);
 			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			View footerView = getLayoutInflater(savedInstanceState).inflate(R.layout.list_footer, null, false);
 			listView.addFooterView(footerView);
 
 			if (noteListAdapter == null) {
 				noteListAdapter = new NoteListAdapter(getActivity());
 				//if we get paused or stopped, restore list view UI state
 				if (savedInstanceState != null) {
 					offsetRequested = savedInstanceState.getInt(OFFSET_REQUESTED, -1);
 					int listViewPosition = savedInstanceState.getInt(LIST_VIEW_POSITION, ListView.INVALID_POSITION);
 					if (listViewPosition != ListView.INVALID_POSITION)
 						getListView().setSelection(listViewPosition);
 				}
 				setListAdapter(noteListAdapter);
 			}
 
 			getListView().setOnScrollListener(lastRowListener);
 		}
 
 		/**
 		 * In a low memory situation, clear our image cache.
 		 */
 		@Override
 		public void onLowMemory() {
 			super.onLowMemory();
 			ImagePreviewFragment fragment = ((MarsImagesActivity)getActivity()).mImagePreviewFragment;
 			if (fragment != null) {
 				fragment.setPreviewImage(null);
 			}
 
 			noteListAdapter.getCache().evictAll();
 		}
 
 		/**
 		 * If we need to pause or stop, save list view UI state for when we are restarted
 		 */
 		@Override
 		public void onSaveInstanceState(Bundle outState) {
 			super.onSaveInstanceState(outState);
 			outState.putInt(OFFSET_REQUESTED, offsetRequested);
 			outState.putInt(LIST_VIEW_POSITION, getListView().getSelectedItemPosition());
 		}
 
 		/**
 		 * When an image list item is clicked: if the image preview fragment is
 		 * visible, fetch the image and display it there. If no image preview
 		 * fragment is visible, fetch the image and start a full-screen image
 		 * view activity to view it.
 		 */
 		@Override
 		public void onListItemClick(ListView l, View v, int position, long id) {
 			super.onListItemClick(l, v, position, id);
 			final MarsImagesApp app = (MarsImagesApp)getActivity().getApplication();
 			final ImagePreviewFragment imagePreviewFragment = ((MarsImagesActivity)getActivity()).mImagePreviewFragment;
 			((SherlockFragmentActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);
 			if (imagePreviewFragment != null) {
 				imagePreviewFragment.imageProgress.setVisibility(ProgressBar.VISIBLE);
 			}
 
 			final Note note = (Note)noteListAdapter.getItem(position);
 			if (note == null) { //can happen on list footer
 				return;
 			}
 			final String companionTitle = ImageIDUtils.getCompanionImageTitle(note.getTitle());
 			app.setAnaglyphImageNote(noteListAdapter.getNote(companionTitle));
 
 			new AsyncTask<Note, Void, byte[]>() {
 				/** Fetch the image */
 				@Override
 				protected byte[] doInBackground(Note... note) {
 					try {
 						return app.readImageResourceFromNote(note[0]);
 					} catch (RuntimeException e) {
 						getActivity().runOnUiThread(new ToastServerExceptionRunnable(getActivity(), e));
 						return null;
 					} catch (OutOfMemoryError e) {
 						getActivity().runOnUiThread(new Runnable() {
 							public void run() {
 								alertDialog = new AlertDialog.Builder(getActivity()).create();
 								alertDialog.setTitle("Insufficient memory");
 								alertDialog.setMessage("Sorry, there is not enough memory to load the image.");
 								alertDialog.show();
 							}
 						});
 						return null;
 					}
 				}
 
 				/** If we get the image, display it in either preview or full-screen view */
 				@Override
 				protected void onPostExecute(byte[] result) {
 					if (imagePreviewFragment != null) {
 						imagePreviewFragment.imageProgress.setVisibility(ProgressBar.INVISIBLE);
 					}
 					if (result == null)
 						return;
 
 					app.setSelectedNote(note);
 					app.setSelectedImageBytes(result);
 
 					final String title = note.getTitle(); 
 					final String[] tokens = title.split(" ");
 					final String imageId;
 					if (MarsImagesApp.isMERMission()) {
 						imageId = tokens[2];
 					} else {
 						if (tokens.length == 4)
 							imageId = tokens[3];
 						else
 							imageId = tokens[2];
 					}
 					
 					if (imagePreviewFragment != null) {
 						imagePreviewFragment.imageProgress.setVisibility(ProgressBar.INVISIBLE);
 						imagePreviewFragment.setPreviewImage(result);
 					} else {
 						if(!(imageId.equals("Course"))) {
 							((MarsImagesActivity)getActivity()).createFullscreenImageViewActivity(result);
 						} else {
 							((MarsImagesActivity)getActivity()).createPlotMapViewActivity(result);
 						}
 					}
 				}
 			}.execute(note);
 			
 			new AsyncTask<Note, Void, Bitmap>() {
 				@Override
 				protected Bitmap doInBackground(Note... note) {
 					String prepend = "";
 					String append = "";
 					
 					if (MarsImagesApp.getMission().equals(MarsImagesApp.OPPORTUNITY_MISSION)) {
 						prepend = "http://merpublic.s3.amazonaws.com/oss/merb/ops/ops/surface/tactical/sol/";
 						append = "/sret/mobidd/mot-all-report/cache-mot-all-report/hyperplots/raw_north_vs_raw_east.png";
 					} else if(MarsImagesApp.getMission().equals(MarsImagesApp.SPIRIT_MISSION)) {
 						prepend = "http://merpublic.s3.amazonaws.com/oss/mera/ops/ops/surface/tactical/sol/";
 						append = "/sret/mobidd/mot-all-report/cache-mot-all-report/hyperplots/raw_north_vs_raw_east.png";
 					} else {
 						return null; //for missions that don't include course plots, do nothing
 					}
 					
 					String title = note[0].getTitle();
 					
 					String[] parts = title.split(" ");
 					String urldisplay = "";
 					try { 
 						String sol = String.format(Locale.US, "%03d", Integer.parseInt(parts[1]));
 						urldisplay = prepend + sol + append;					
 					} catch(NullPointerException e) {
 						Log.e(TAG, "SOL image url is not formatted properly.");
 					}
 					
 					Bitmap plotImage = null;
 					try {
 						InputStream in = new java.net.URL(urldisplay).openStream();
 						plotImage = BitmapFactory.decodeStream(in);
 					} catch (MalformedURLException e) {
 						Log.e(TAG, "SOL URL not formatted properly");
 						e.printStackTrace();
 					} catch (IOException e) {
 						Log.e(TAG, "Unable to find image at "+urldisplay);
 					}
 					return plotImage;
 				}
 				
 				/**
 				 * Show plot image.
 				 */
 				@Override
 				protected void onPostExecute(Bitmap plotImage) {
 					app.setPlotMapImage(plotImage);
 				}
 				
 			}.execute(note);
 		}
 
 		/**
 		 * Infinite scrolling listener loads more items in background when the
 		 * last list item is displayed.
 		 */
 		private OnScrollListener lastRowListener = new OnScrollListener() {
 
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				//nothing to do
 			}
 
 			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 				int lastInScreen = firstVisibleItem + visibleItemCount;            
 				//is the bottom item visible & not loading more already ? Load more!
 				if ((lastInScreen == totalItemCount)) {
 					((SherlockFragmentActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);
 					new Thread(null, loadMoreListItems).start();
 				}
 			};
 
 			//Runnable to load the items
 			private Runnable loadMoreListItems = new Runnable() {
 				public void run() {
 					if (noteListAdapter != null) {
 						int offset = noteListAdapter.getCount();
 						boolean shouldRequest = false;
 						synchronized (LOAD_MORE_LOCK) { //one updater thread at a time
 							if (offsetRequested < offset) {
 								shouldRequest = true;
 								offsetRequested = offset;
 							}
 						}
 						if (shouldRequest) {
 							Log.i(TAG, "Requesting notes with offset: "+offset);
 							int numNotes = noteListAdapter.getCount();
 							MarsImagesActivity activity = (MarsImagesActivity)getActivity(); 
 							if (activity != null) {
 								try {
 									moreNotes = ((IEvernoteServer)getActivity().getApplication()).getImageNotes(offset, NOTE_BATCH_SIZE);
 								} catch (RuntimeException e) {
 									activity.runOnUiThread(new ToastServerExceptionRunnable(activity, e));
 									synchronized (LOAD_MORE_LOCK) {
 										offsetRequested--; //cause onScroll to re-request this batch and succeed if re-connected
 									}
 									return;
 								}
 
 								if (numNotes != noteListAdapter.getCount()) { //somebody hit refresh to clear the notes
 									return;
 								}
 								activity.runOnUiThread(returnRes);
 							}
 						}
 					}
 				}
 			};
 
 			/**
 			 * Add newly-fetched image notes to the list view (in UI thread)
 			 */
 			private Runnable returnRes = new Runnable() {
 				public void run() {
 					SherlockFragmentActivity activity = (SherlockFragmentActivity) getActivity();
 					if (activity == null) { //unit test
 						return;
 					}
 
 					activity.setSupportProgressBarIndeterminateVisibility(false);
 					int firstPosition = getListView().getFirstVisiblePosition();
 					if (moreNotes != null && moreNotes.size() > 0) {
 						noteListAdapter.addAll(moreNotes);
 					}
 					noteListAdapter.notifyDataSetChanged();
 					getListView().setSelection(firstPosition);
 				}
 			};
 		}; //onScrollListener
 	}
 }
