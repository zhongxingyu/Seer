 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package android.app;
 
 import static android.app.SuggestionsAdapter.getColumnString;
 
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.server.search.SearchableInfo;
 import android.speech.RecognizerIntent;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.text.util.Regex;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import java.util.ArrayList;
 import java.util.WeakHashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * System search dialog. This is controlled by the 
  * SearchManagerService and runs in the system process.
  * 
  * @hide
  */
 public class SearchDialog extends Dialog implements OnItemClickListener, OnItemSelectedListener {
 
     // Debugging support
     private static final boolean DBG = false;
     private static final String LOG_TAG = "SearchDialog";
     private static final boolean DBG_LOG_TIMING = false;
 
     private static final String INSTANCE_KEY_COMPONENT = "comp";
     private static final String INSTANCE_KEY_APPDATA = "data";
     private static final String INSTANCE_KEY_GLOBALSEARCH = "glob";
     private static final String INSTANCE_KEY_STORED_COMPONENT = "sComp";
     private static final String INSTANCE_KEY_STORED_APPDATA = "sData";
     private static final String INSTANCE_KEY_PREVIOUS_COMPONENTS = "sPrev";
     private static final String INSTANCE_KEY_USER_QUERY = "uQry";
 
     private static final int SEARCH_PLATE_LEFT_PADDING_GLOBAL = 12;
     private static final int SEARCH_PLATE_LEFT_PADDING_NON_GLOBAL = 7;
     
     // interaction with runtime
     private IntentFilter mCloseDialogsFilter;
     private IntentFilter mPackageFilter;
     
     // views & widgets
     private TextView mBadgeLabel;
     private ImageView mAppIcon;
     private SearchAutoComplete mSearchAutoComplete;
     private Button mGoButton;
     private ImageButton mVoiceButton;
     private View mSearchPlate;
     private AnimationDrawable mWorkingSpinner;
 
     // interaction with searchable application
     private SearchableInfo mSearchable;
     private ComponentName mLaunchComponent;
     private Bundle mAppSearchData;
     private boolean mGlobalSearchMode;
     private Context mActivityContext;
     
     // Values we store to allow user to toggle between in-app search and global search.
     private ComponentName mStoredComponentName;
     private Bundle mStoredAppSearchData;
     
     // stack of previous searchables, to support the BACK key after
     // SearchManager.INTENT_ACTION_CHANGE_SEARCH_SOURCE.
     // The top of the stack (= previous searchable) is the last element of the list,
     // since adding and removing is efficient at the end of an ArrayList.
     private ArrayList<ComponentName> mPreviousComponents;
 
     // For voice searching
     private Intent mVoiceWebSearchIntent;
     private Intent mVoiceAppSearchIntent;
 
     // support for AutoCompleteTextView suggestions display
     private SuggestionsAdapter mSuggestionsAdapter;
     
     // Whether to rewrite queries when selecting suggestions
     private static final boolean REWRITE_QUERIES = true;
     
     // The query entered by the user. This is not changed when selecting a suggestion
     // that modifies the contents of the text field. But if the user then edits
     // the suggestion, the resulting string is saved.
     private String mUserQuery;
     
     // A weak map of drawables we've gotten from other packages, so we don't load them
     // more than once.
     private final WeakHashMap<String, Drawable> mOutsideDrawablesCache =
             new WeakHashMap<String, Drawable>();
 
     // Last known IME options value for the search edit text.
     private int mSearchAutoCompleteImeOptions;
 
     /**
      * Constructor - fires it up and makes it look like the search UI.
      * 
      * @param context Application Context we can use for system acess
      */
     public SearchDialog(Context context) {
         super(context, com.android.internal.R.style.Theme_GlobalSearchBar);
     }
 
     /**
      * We create the search dialog just once, and it stays around (hidden)
      * until activated by the user.
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(com.android.internal.R.layout.search_bar);
 
         Window theWindow = getWindow();
         WindowManager.LayoutParams lp = theWindow.getAttributes();
         lp.type = WindowManager.LayoutParams.TYPE_SEARCH_BAR;
         lp.width = ViewGroup.LayoutParams.FILL_PARENT;
         // taking up the whole window (even when transparent) is less than ideal,
         // but necessary to show the popup window until the window manager supports
         // having windows anchored by their parent but not clipped by them.
         lp.height = ViewGroup.LayoutParams.FILL_PARENT;
         lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
         theWindow.setAttributes(lp);
 
         // get the view elements for local access
         mBadgeLabel = (TextView) findViewById(com.android.internal.R.id.search_badge);
         mSearchAutoComplete = (SearchAutoComplete)
                 findViewById(com.android.internal.R.id.search_src_text);
         mAppIcon = (ImageView) findViewById(com.android.internal.R.id.search_app_icon);
         mGoButton = (Button) findViewById(com.android.internal.R.id.search_go_btn);
         mVoiceButton = (ImageButton) findViewById(com.android.internal.R.id.search_voice_btn);
         mSearchPlate = findViewById(com.android.internal.R.id.search_plate);
         mWorkingSpinner = (AnimationDrawable) getContext().getResources().
                 getDrawable(com.android.internal.R.drawable.search_spinner);
         
         // attach listeners
         mSearchAutoComplete.addTextChangedListener(mTextWatcher);
         mSearchAutoComplete.setOnKeyListener(mTextKeyListener);
         mSearchAutoComplete.setOnItemClickListener(this);
         mSearchAutoComplete.setOnItemSelectedListener(this);
         mGoButton.setOnClickListener(mGoButtonClickListener);
         mGoButton.setOnKeyListener(mButtonsKeyListener);
         mVoiceButton.setOnClickListener(mVoiceButtonClickListener);
         mVoiceButton.setOnKeyListener(mButtonsKeyListener);
 
         mSearchAutoComplete.setSearchDialog(this);
         
         // pre-hide all the extraneous elements
         mBadgeLabel.setVisibility(View.GONE);
 
         // Additional adjustments to make Dialog work for Search
 
         // Touching outside of the search dialog will dismiss it 
         setCanceledOnTouchOutside(true);
         
         // Set up broadcast filters
         mCloseDialogsFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
         mPackageFilter = new IntentFilter();
         mPackageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
         mPackageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
         mPackageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
         mPackageFilter.addDataScheme("package");
         
         // Save voice intent for later queries/launching
         mVoiceWebSearchIntent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
         mVoiceWebSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         mVoiceWebSearchIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                 RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
         
         mVoiceAppSearchIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         mVoiceAppSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
         mSearchAutoCompleteImeOptions = mSearchAutoComplete.getImeOptions();
     }
 
     /**
      * Set up the search dialog
      * 
      * @return true if search dialog launched, false if not
      */
     public boolean show(String initialQuery, boolean selectInitialQuery,
             ComponentName componentName, Bundle appSearchData, boolean globalSearch) {
 
         // Reset any stored values from last time dialog was shown.
         mStoredComponentName = null;
         mStoredAppSearchData = null;
         
         return doShow(initialQuery, selectInitialQuery, componentName, appSearchData, globalSearch);
     }
     
     /**
      * Called in response to a press of the hard search button in
      * {@link #onKeyDown(int, KeyEvent)}, this method toggles between in-app
      * search and global search when relevant.
      * 
      * If pressed within an in-app search context, this switches the search dialog out to
      * global search. If pressed within a global search context that was originally an in-app
      * search context, this switches back to the in-app search context. If pressed within a
      * global search context that has no original in-app search context (e.g., global search
      * from Home), this does nothing.
      * 
      * @return false if we wanted to toggle context but could not do so successfully, true
      * in all other cases
      */
     private boolean toggleGlobalSearch() {
         String currentSearchText = mSearchAutoComplete.getText().toString();
         if (!mGlobalSearchMode) {
             mStoredComponentName = mLaunchComponent;
             mStoredAppSearchData = mAppSearchData;
             return doShow(currentSearchText, false, null, mAppSearchData, true);
         } else {
             if (mStoredComponentName != null) {
                 // This means we should toggle *back* to an in-app search context from
                 // global search.
                 return doShow(currentSearchText, false, mStoredComponentName,
                         mStoredAppSearchData, false);
             } else {
                 return true;
             }
         }
     }
     
     /**
      * Does the rest of the work required to show the search dialog. Called by both
      * {@link #show(String, boolean, ComponentName, Bundle, boolean)} and
      * {@link #toggleGlobalSearch()}.
      * 
      * @return true if search dialog showed, false if not
      */
     private boolean doShow(String initialQuery, boolean selectInitialQuery,
             ComponentName componentName, Bundle appSearchData,
             boolean globalSearch) {
         // set up the searchable and show the dialog
         if (!show(componentName, appSearchData, globalSearch)) {
             return false;
         }
 
         // finally, load the user's initial text (which may trigger suggestions)
         setUserQuery(initialQuery);
         if (selectInitialQuery) {
             mSearchAutoComplete.selectAll();
         }
 
         return true;
     }
 
     /**
      * Sets up the search dialog and shows it.
      * 
      * @return <code>true</code> if search dialog launched
      */
     private boolean show(ComponentName componentName, Bundle appSearchData, 
             boolean globalSearch) {
         
         if (DBG) { 
             Log.d(LOG_TAG, "show(" + componentName + ", " 
                     + appSearchData + ", " + globalSearch + ")");
         }
         
         SearchManager searchManager = (SearchManager)
                 mContext.getSystemService(Context.SEARCH_SERVICE);
         // Try to get the searchable info for the provided component (or for global search,
         // if globalSearch == true).
         mSearchable = searchManager.getSearchableInfo(componentName, globalSearch);
         
         // If we got back nothing, and it wasn't a request for global search, then try again
         // for global search, as we'll try to launch that in lieu of any component-specific search.
         if (!globalSearch && mSearchable == null) {
             globalSearch = true;
             mSearchable = searchManager.getSearchableInfo(componentName, globalSearch);
             
             // If we still get back null (i.e., there's not even a searchable info available
             // for global search), then really give up.
             if (mSearchable == null) {
                 // Unfortunately, we can't log here.  it would be logspam every time the user
                 // clicks the "search" key on a non-search app.
                 return false;
             }
         }
         
         mLaunchComponent = componentName;
         mAppSearchData = appSearchData;
         // Using globalSearch here is just an optimization, just calling
         // isDefaultSearchable() should always give the same result.
         mGlobalSearchMode = globalSearch || searchManager.isDefaultSearchable(mSearchable);
         mActivityContext = mSearchable.getActivityContext(getContext());
         
         // show the dialog. this will call onStart().
         if (!isShowing()) {
             // First make sure the keyboard is showing (if needed), so that we get the right height
             // for the dropdown to respect the IME.
             if (getContext().getResources().getConfiguration().hardKeyboardHidden ==
                 Configuration.HARDKEYBOARDHIDDEN_YES) {
                 InputMethodManager inputManager = (InputMethodManager)
                 getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                         inputManager.showSoftInputUnchecked(0, null);
             }
             
             // The Dialog uses a ContextThemeWrapper for the context; use this to change the
             // theme out from underneath us, between the global search theme and the in-app
             // search theme. They are identical except that the global search theme does not
             // dim the background of the window (because global search is full screen so it's
             // not needed and this should save a little bit of time on global search invocation).
             Object context = getContext();
             if (context instanceof ContextThemeWrapper) {
                 ContextThemeWrapper wrapper = (ContextThemeWrapper) context;
                 if (globalSearch) {
                     wrapper.setTheme(com.android.internal.R.style.Theme_GlobalSearchBar);
                 } else {
                     wrapper.setTheme(com.android.internal.R.style.Theme_SearchBar);
                 }
             }
             show();
         }
 
         updateUI();
         
         return true;
     }
     
     @Override
     protected void onStart() {
         super.onStart();
         
         // receive broadcasts
         getContext().registerReceiver(mBroadcastReceiver, mCloseDialogsFilter);
         getContext().registerReceiver(mBroadcastReceiver, mPackageFilter);
     }
 
     /**
      * The search dialog is being dismissed, so handle all of the local shutdown operations.
      * 
      * This function is designed to be idempotent so that dismiss() can be safely called at any time
      * (even if already closed) and more likely to really dump any memory.  No leaks!
      */
     @Override
     public void onStop() {
         super.onStop();
         
         // stop receiving broadcasts (throws exception if none registered)
         try {
             getContext().unregisterReceiver(mBroadcastReceiver);
         } catch (RuntimeException e) {
             // This is OK - it just means we didn't have any registered
         }
         
         closeSuggestionsAdapter();
         
         // dump extra memory we're hanging on to
         mLaunchComponent = null;
         mAppSearchData = null;
         mSearchable = null;
         mActivityContext = null;
         mUserQuery = null;
         mPreviousComponents = null;
     }
 
     /**
      * Sets the search dialog to the 'working' state, which shows a working spinner in the
      * right hand size of the text field.
      * 
      * @param working true to show spinner, false to hide spinner
      */
     public void setWorking(boolean working) {
         if (working) {
             mSearchAutoComplete.setCompoundDrawablesWithIntrinsicBounds(
                     null, null, mWorkingSpinner, null);
             mWorkingSpinner.start();
         } else {
             mSearchAutoComplete.setCompoundDrawablesWithIntrinsicBounds(
                     null, null, null, null);
             mWorkingSpinner.stop();
         }
     }
     
     /**
      * Closes and gets rid of the suggestions adapter.
      */
     private void closeSuggestionsAdapter() {
         // remove the adapter from the autocomplete first, to avoid any updates
         // when we drop the cursor
         mSearchAutoComplete.setAdapter((SuggestionsAdapter)null);
         // close any leftover cursor
         if (mSuggestionsAdapter != null) {
             mSuggestionsAdapter.changeCursor(null);
         }
         mSuggestionsAdapter = null;
     }
     
     /**
      * Save the minimal set of data necessary to recreate the search
      * 
      * @return A bundle with the state of the dialog.
      */
     @Override
     public Bundle onSaveInstanceState() {
         Bundle bundle = new Bundle();
         
         // setup info so I can recreate this particular search       
         bundle.putParcelable(INSTANCE_KEY_COMPONENT, mLaunchComponent);
         bundle.putBundle(INSTANCE_KEY_APPDATA, mAppSearchData);
         bundle.putBoolean(INSTANCE_KEY_GLOBALSEARCH, mGlobalSearchMode);
         bundle.putParcelable(INSTANCE_KEY_STORED_COMPONENT, mStoredComponentName);
         bundle.putBundle(INSTANCE_KEY_STORED_APPDATA, mStoredAppSearchData);
         bundle.putParcelableArrayList(INSTANCE_KEY_PREVIOUS_COMPONENTS, mPreviousComponents);
         bundle.putString(INSTANCE_KEY_USER_QUERY, mUserQuery);
 
         return bundle;
     }
 
     /**
      * Restore the state of the dialog from a previously saved bundle.
      * 
      * TODO: go through this and make sure that it saves everything that is saved
      *
      * @param savedInstanceState The state of the dialog previously saved by
      *     {@link #onSaveInstanceState()}.
      */
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         ComponentName launchComponent = savedInstanceState.getParcelable(INSTANCE_KEY_COMPONENT);
         Bundle appSearchData = savedInstanceState.getBundle(INSTANCE_KEY_APPDATA);
         boolean globalSearch = savedInstanceState.getBoolean(INSTANCE_KEY_GLOBALSEARCH);
         ComponentName storedComponentName =
                 savedInstanceState.getParcelable(INSTANCE_KEY_STORED_COMPONENT);
         Bundle storedAppSearchData =
                 savedInstanceState.getBundle(INSTANCE_KEY_STORED_APPDATA);
         ArrayList<ComponentName> previousComponents =
                 savedInstanceState.getParcelableArrayList(INSTANCE_KEY_PREVIOUS_COMPONENTS);
         String userQuery = savedInstanceState.getString(INSTANCE_KEY_USER_QUERY);
 
         // Set stored state
         mStoredComponentName = storedComponentName;
         mStoredAppSearchData = storedAppSearchData;
         mPreviousComponents = previousComponents;
 
         // show the dialog.
         if (!doShow(userQuery, false, launchComponent, appSearchData, globalSearch)) {
             // for some reason, we couldn't re-instantiate
             return;
         }
     }
     
     /**
      * Called after resources have changed, e.g. after screen rotation or locale change.
      */
     public void onConfigurationChanged(Configuration newConfig) {
         if (isShowing()) {
             // Redraw (resources may have changed)
             updateSearchButton();
             updateSearchAppIcon();
             updateSearchBadge();
             updateQueryHint();
         } 
     }
     
     /**
      * Update the UI according to the info in the current value of {@link #mSearchable}.
      */
     private void updateUI() {
         if (mSearchable != null) {
             updateSearchAutoComplete();
             updateSearchButton();
             updateSearchAppIcon();
             updateSearchBadge();
             updateQueryHint();
             updateVoiceButton();
             
             // In order to properly configure the input method (if one is being used), we
             // need to let it know if we'll be providing suggestions.  Although it would be
             // difficult/expensive to know if every last detail has been configured properly, we 
             // can at least see if a suggestions provider has been configured, and use that
             // as our trigger.
             int inputType = mSearchable.getInputType();
             // We only touch this if the input type is set up for text (which it almost certainly
             // should be, in the case of search!)
             if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
                 // The existence of a suggestions authority is the proxy for "suggestions 
                 // are available here"
                 inputType &= ~InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
                 if (mSearchable.getSuggestAuthority() != null) {
                     inputType |= InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
                 }
             }
             mSearchAutoComplete.setInputType(inputType);
             mSearchAutoCompleteImeOptions = mSearchable.getImeOptions();
             mSearchAutoComplete.setImeOptions(mSearchAutoCompleteImeOptions);
         }
     }
     
     /**
      * Updates the auto-complete text view.
      */
     private void updateSearchAutoComplete() {
         // close any existing suggestions adapter
         closeSuggestionsAdapter();
         
         mSearchAutoComplete.setDropDownAnimationStyle(0); // no animation
         mSearchAutoComplete.setThreshold(mSearchable.getSuggestThreshold());
         // we dismiss the entire dialog instead
         mSearchAutoComplete.setDropDownDismissedOnCompletion(false);
 
         if (mGlobalSearchMode) {
             mSearchAutoComplete.setDropDownAlwaysVisible(true);  // fill space until results come in
         } else {
             mSearchAutoComplete.setDropDownAlwaysVisible(false);
         }
 
         // attach the suggestions adapter, if suggestions are available
         // The existence of a suggestions authority is the proxy for "suggestions available here"
         if (mSearchable.getSuggestAuthority() != null) {
             mSuggestionsAdapter = new SuggestionsAdapter(getContext(), this, mSearchable, 
                     mOutsideDrawablesCache, mGlobalSearchMode);
             mSearchAutoComplete.setAdapter(mSuggestionsAdapter);
         }
     }
 
     /**    
      * Update the text in the search button.  Note: This is deprecated functionality, for 
      * 1.0 compatibility only.
      */  
     private void updateSearchButton() { 
         String textLabel = null;
         Drawable iconLabel = null;
         int textId = mSearchable.getSearchButtonText(); 
         if (textId != 0) {
             textLabel = mActivityContext.getResources().getString(textId);  
         } else {
             iconLabel = getContext().getResources().
                     getDrawable(com.android.internal.R.drawable.ic_btn_search);
         }
         mGoButton.setText(textLabel);
         mGoButton.setCompoundDrawablesWithIntrinsicBounds(iconLabel, null, null, null);
     }
     
     private void updateSearchAppIcon() {
         if (mGlobalSearchMode) {
             mAppIcon.setImageResource(0);
             mAppIcon.setVisibility(View.GONE);
             mSearchPlate.setPadding(SEARCH_PLATE_LEFT_PADDING_GLOBAL,
                     mSearchPlate.getPaddingTop(),
                     mSearchPlate.getPaddingRight(),
                     mSearchPlate.getPaddingBottom());
         } else {
             PackageManager pm = getContext().getPackageManager();
             Drawable icon = null;
             try {
                 ActivityInfo info = pm.getActivityInfo(mLaunchComponent, 0);
                 icon = pm.getApplicationIcon(info.applicationInfo);
                 if (DBG) Log.d(LOG_TAG, "Using app-specific icon");
             } catch (NameNotFoundException e) {
                 icon = pm.getDefaultActivityIcon();
                 Log.w(LOG_TAG, mLaunchComponent + " not found, using generic app icon");
             }
             mAppIcon.setImageDrawable(icon);
             mAppIcon.setVisibility(View.VISIBLE);
             mSearchPlate.setPadding(SEARCH_PLATE_LEFT_PADDING_NON_GLOBAL,
                     mSearchPlate.getPaddingTop(),
                     mSearchPlate.getPaddingRight(),
                     mSearchPlate.getPaddingBottom());
         }
     }
 
     /**
      * Setup the search "Badge" if requested by mode flags.
      */
     private void updateSearchBadge() {
         // assume both hidden
         int visibility = View.GONE;
         Drawable icon = null;
         CharSequence text = null;
         
         // optionally show one or the other.
         if (mSearchable.useBadgeIcon()) {
             icon = mActivityContext.getResources().getDrawable(mSearchable.getIconId());
             visibility = View.VISIBLE;
             if (DBG) Log.d(LOG_TAG, "Using badge icon: " + mSearchable.getIconId());
         } else if (mSearchable.useBadgeLabel()) {
             text = mActivityContext.getResources().getText(mSearchable.getLabelId()).toString();
             visibility = View.VISIBLE;
             if (DBG) Log.d(LOG_TAG, "Using badge label: " + mSearchable.getLabelId());
         }
         
         mBadgeLabel.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
         mBadgeLabel.setText(text);
         mBadgeLabel.setVisibility(visibility);
     }
 
     /**
      * Update the hint in the query text field.
      */
     private void updateQueryHint() {
         if (isShowing()) {
             String hint = null;
             if (mSearchable != null) {
                 int hintId = mSearchable.getHintId();
                 if (hintId != 0) {
                     hint = mActivityContext.getString(hintId);
                 }
             }
             mSearchAutoComplete.setHint(hint);
         }
     }
 
     /**
      * Update the visibility of the voice button.  There are actually two voice search modes, 
      * either of which will activate the button.
      */
     private void updateVoiceButton() {
         int visibility = View.GONE;
         if (mSearchable.getVoiceSearchEnabled()) {
             Intent testIntent = null;
             if (mSearchable.getVoiceSearchLaunchWebSearch()) {
                 testIntent = mVoiceWebSearchIntent;
             } else if (mSearchable.getVoiceSearchLaunchRecognizer()) {
                 testIntent = mVoiceAppSearchIntent;
             }      
             if (testIntent != null) {
                 ResolveInfo ri = getContext().getPackageManager().
                         resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                 if (ri != null) {
                     visibility = View.VISIBLE;
                 }
             }
         }
         mVoiceButton.setVisibility(visibility);
     }
     
     /**
      * Listeners of various types
      */
 
     /**
      * {@link Dialog#onTouchEvent(MotionEvent)} will cancel the dialog only when the
      * touch is outside the window. But the window includes space for the drop-down,
      * so we also cancel on taps outside the search bar when the drop-down is not showing.
      */
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         // cancel if the drop-down is not showing and the touch event was outside the search plate
         if (!mSearchAutoComplete.isPopupShowing() && isOutOfBounds(mSearchPlate, event)) {
             if (DBG) Log.d(LOG_TAG, "Pop-up not showing and outside of search plate.");
             cancel();
             return true;
         }
         // Let Dialog handle events outside the window while the pop-up is showing.
         return super.onTouchEvent(event);
     }
     
     private boolean isOutOfBounds(View v, MotionEvent event) {
         final int x = (int) event.getX();
         final int y = (int) event.getY();
         final int slop = ViewConfiguration.get(mContext).getScaledWindowTouchSlop();
         return (x < -slop) || (y < -slop)
                 || (x > (v.getWidth()+slop))
                 || (y > (v.getHeight()+slop));
     }
     
     /**
      * Dialog's OnKeyListener implements various search-specific functionality
      *
      * @param keyCode This is the keycode of the typed key, and is the same value as
      *        found in the KeyEvent parameter.
      * @param event The complete event record for the typed key
      *
      * @return Return true if the event was handled here, or false if not.
      */
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (DBG) Log.d(LOG_TAG, "onKeyDown(" + keyCode + "," + event + ")");
         
         // handle back key to go back to previous searchable, etc.
         if (handleBackKey(keyCode, event)) {
             return true;
         }
         
         if (keyCode == KeyEvent.KEYCODE_SEARCH) {
             // If the search key is pressed, toggle between global and in-app search. If we are
             // currently doing global search and there is no in-app search context to toggle to,
             // just don't do anything.
             return toggleGlobalSearch();
         }
 
         // if it's an action specified by the searchable activity, launch the
         // entered query with the action key
         SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
         if ((actionKey != null) && (actionKey.getQueryActionMsg() != null)) {
             launchQuerySearch(keyCode, actionKey.getQueryActionMsg());
             return true;
         }
         
         return false;
     }
     
     /**
      * Callback to watch the textedit field for empty/non-empty
      */
     private TextWatcher mTextWatcher = new TextWatcher() {
 
         public void beforeTextChanged(CharSequence s, int start, int before, int after) { }
 
         public void onTextChanged(CharSequence s, int start,
                 int before, int after) {
             if (DBG_LOG_TIMING) {
                 dbgLogTiming("onTextChanged()");
             }
             updateWidgetState();
             if (!mSearchAutoComplete.isPerformingCompletion()) {
                 // The user changed the query, remember it.
                 mUserQuery = s == null ? "" : s.toString();
             }
         }
 
         public void afterTextChanged(Editable s) {
             if (!mSearchAutoComplete.isPerformingCompletion()) {
                 // The user changed the query, check if it is a URL and if so change the search
                 // button in the soft keyboard to the 'Go' button.
                 int options = (mSearchAutoComplete.getImeOptions() & (~EditorInfo.IME_MASK_ACTION));
                 if (Regex.WEB_URL_PATTERN.matcher(mUserQuery).matches()) {
                     options = options | EditorInfo.IME_ACTION_GO;
                 } else {
                     options = options | EditorInfo.IME_ACTION_SEARCH;
                 }
                 if (options != mSearchAutoCompleteImeOptions) {
                     mSearchAutoCompleteImeOptions = options;
                     mSearchAutoComplete.setImeOptions(options);
                     // This call is required to update the soft keyboard UI with latest IME flags.
                     mSearchAutoComplete.setInputType(mSearchAutoComplete.getInputType());
                 }
             }
         }
     };
 
     /**
      * Enable/Disable the cancel button based on edit text state (any text?)
      */
     private void updateWidgetState() {
         // enable the button if we have one or more non-space characters
         boolean enabled = !mSearchAutoComplete.isEmpty();
         mGoButton.setEnabled(enabled);
         mGoButton.setFocusable(enabled);
     }
 
     /**
      * React to typing in the GO search button by refocusing to EditText. 
      * Continue typing the query.
      */
     View.OnKeyListener mButtonsKeyListener = new View.OnKeyListener() {
         public boolean onKey(View v, int keyCode, KeyEvent event) {
             // guard against possible race conditions
             if (mSearchable == null) {
                 return false;
             }
             
             if (!event.isSystem() && 
                     (keyCode != KeyEvent.KEYCODE_DPAD_UP) &&
                     (keyCode != KeyEvent.KEYCODE_DPAD_DOWN) &&
                     (keyCode != KeyEvent.KEYCODE_DPAD_LEFT) &&
                     (keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) &&
                     (keyCode != KeyEvent.KEYCODE_DPAD_CENTER)) {
                 // restore focus and give key to EditText ...
                 if (mSearchAutoComplete.requestFocus()) {
                     return mSearchAutoComplete.dispatchKeyEvent(event);
                 }
             }
 
             return false;
         }
     };
 
     /**
      * React to a click in the GO button by launching a search.
      */
     View.OnClickListener mGoButtonClickListener = new View.OnClickListener() {
         public void onClick(View v) {
             // guard against possible race conditions
             if (mSearchable == null) {
                 return;
             }
             launchQuerySearch();
         }
     };
     
     /**
      * React to a click in the voice search button.
      */
     View.OnClickListener mVoiceButtonClickListener = new View.OnClickListener() {
         public void onClick(View v) {
             // guard against possible race conditions
             if (mSearchable == null) {
                 return;
             }
             try {
                 if (mSearchable.getVoiceSearchLaunchWebSearch()) {
                     getContext().startActivity(mVoiceWebSearchIntent);
                 } else if (mSearchable.getVoiceSearchLaunchRecognizer()) {
                     Intent appSearchIntent = createVoiceAppSearchIntent(mVoiceAppSearchIntent);
                     getContext().startActivity(appSearchIntent);
                 }
             } catch (ActivityNotFoundException e) {
                 // Should not happen, since we check the availability of
                 // voice search before showing the button. But just in case...
                 Log.w(LOG_TAG, "Could not find voice search activity");
             }
          }
     };
     
     /**
      * Create and return an Intent that can launch the voice search activity, perform a specific
      * voice transcription, and forward the results to the searchable activity.
      * 
      * @param baseIntent The voice app search intent to start from
      * @return A completely-configured intent ready to send to the voice search activity
      */
     private Intent createVoiceAppSearchIntent(Intent baseIntent) {
         // create the necessary intent to set up a search-and-forward operation
         // in the voice search system.   We have to keep the bundle separate,
         // because it becomes immutable once it enters the PendingIntent
         Intent queryIntent = new Intent(Intent.ACTION_SEARCH);
         queryIntent.setComponent(mSearchable.getSearchActivity());
         PendingIntent pending = PendingIntent.getActivity(
                 getContext(), 0, queryIntent, PendingIntent.FLAG_ONE_SHOT);
         
         // Now set up the bundle that will be inserted into the pending intent
         // when it's time to do the search.  We always build it here (even if empty)
         // because the voice search activity will always need to insert "QUERY" into
         // it anyway.
         Bundle queryExtras = new Bundle();
         if (mAppSearchData != null) {
             queryExtras.putBundle(SearchManager.APP_DATA, mAppSearchData);
         }
         
         // Now build the intent to launch the voice search.  Add all necessary
         // extras to launch the voice recognizer, and then all the necessary extras
         // to forward the results to the searchable activity
         Intent voiceIntent = new Intent(baseIntent);
         
         // Add all of the configuration options supplied by the searchable's metadata
         String languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
         String prompt = null;
         String language = null;
         int maxResults = 1;
         Resources resources = mActivityContext.getResources();
         if (mSearchable.getVoiceLanguageModeId() != 0) {
             languageModel = resources.getString(mSearchable.getVoiceLanguageModeId());
         }
         if (mSearchable.getVoicePromptTextId() != 0) {
             prompt = resources.getString(mSearchable.getVoicePromptTextId());
         }
         if (mSearchable.getVoiceLanguageId() != 0) {
             language = resources.getString(mSearchable.getVoiceLanguageId());
         }
         if (mSearchable.getVoiceMaxResults() != 0) {
             maxResults = mSearchable.getVoiceMaxResults();
         }
         voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);
         voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
         voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
         voiceIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);
         
         // Add the values that configure forwarding the results
         voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT, pending);
         voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE, queryExtras);
         
         return voiceIntent;
     }
 
     /**
      * Corrects http/https typo errors in the given url string, and if the protocol specifier was
      * not present defaults to http.
      * 
      * @param inUrl URL to check and fix
      * @return fixed URL string.
      */
     private String fixUrl(String inUrl) {
         if (inUrl.startsWith("http://") || inUrl.startsWith("https://"))
             return inUrl;
 
         if (inUrl.startsWith("http:") || inUrl.startsWith("https:")) {
             if (inUrl.startsWith("http:/") || inUrl.startsWith("https:/")) {
                 inUrl = inUrl.replaceFirst("/", "//");
             } else {
                 inUrl = inUrl.replaceFirst(":", "://");
             }
         }
 
         if (inUrl.indexOf("://") == -1) {
             inUrl = "http://" + inUrl;
         }
 
         return inUrl;
     }
 
     /**
      * React to the user typing "enter" or other hardwired keys while typing in the search box.
      * This handles these special keys while the edit box has focus.
      */
     View.OnKeyListener mTextKeyListener = new View.OnKeyListener() {
         public boolean onKey(View v, int keyCode, KeyEvent event) {
             // guard against possible race conditions
             if (mSearchable == null) {
                 return false;
             }
 
             if (DBG_LOG_TIMING) dbgLogTiming("doTextKey()");
             if (DBG) { 
                 Log.d(LOG_TAG, "mTextListener.onKey(" + keyCode + "," + event 
                         + "), selection: " + mSearchAutoComplete.getListSelection());
             }
             
             // If a suggestion is selected, handle enter, search key, and action keys 
             // as presses on the selected suggestion
             if (mSearchAutoComplete.isPopupShowing() && 
                     mSearchAutoComplete.getListSelection() != ListView.INVALID_POSITION) {
                 return onSuggestionsKey(v, keyCode, event);
             }
 
             // If there is text in the query box, handle enter, and action keys
             // The search key is handled by the dialog's onKeyDown(). 
             if (!mSearchAutoComplete.isEmpty()) {
                 if (keyCode == KeyEvent.KEYCODE_ENTER 
                         && event.getAction() == KeyEvent.ACTION_UP) {
                     v.cancelLongPress();
 
                     // If this is a url entered by the user and we displayed the 'Go' button which
                     // the user clicked, launch the url instead of using it as a search query.
                     if ((mSearchAutoCompleteImeOptions & EditorInfo.IME_MASK_ACTION)
                             == EditorInfo.IME_ACTION_GO) {
                         Uri uri = Uri.parse(fixUrl(mSearchAutoComplete.getText().toString()));
                         Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         launchIntent(intent);
                     } else {
                         // Launch as a regular search.
                         launchQuerySearch();
                     }
                     return true;
                 }
                 if (event.getAction() == KeyEvent.ACTION_DOWN) {
                     SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
                     if ((actionKey != null) && (actionKey.getQueryActionMsg() != null)) {
                         launchQuerySearch(keyCode, actionKey.getQueryActionMsg());
                         return true;
                     }
                 }
             }
             return false;
         }
     };
         
     /**
      * When the ACTION_CLOSE_SYSTEM_DIALOGS intent is received, we should close ourselves 
      * immediately, in order to allow a higher-priority UI to take over
      * (e.g. phone call received).
      * 
      * When a package is added, removed or changed, our current context
      * may no longer be valid.  This would only happen if a package is installed/removed exactly
      * when the search bar is open.  So for now we're just going to close the search
      * bar.  
      * Anything fancier would require some checks to see if the user's context was still valid.
      * Which would be messier.
      */
     private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                 cancel();
             } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                     || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                     || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                 cancel();
             }
         }
     };
 
     @Override
     public void cancel() {
         // We made sure the IME was displayed, so also make sure it is closed
         // when we go away.
         InputMethodManager imm = (InputMethodManager)getContext()
                 .getSystemService(Context.INPUT_METHOD_SERVICE);
         if (imm != null) {
             imm.hideSoftInputFromWindow(
                     getWindow().getDecorView().getWindowToken(), 0);
         }
         
         super.cancel();
     }
     
     /**
      * React to the user typing while in the suggestions list. First, check for action
      * keys. If not handled, try refocusing regular characters into the EditText. 
      */
     private boolean onSuggestionsKey(View v, int keyCode, KeyEvent event) {
         // guard against possible race conditions (late arrival after dismiss)
         if (mSearchable == null) {
             return false;
         }
         if (mSuggestionsAdapter == null) {
             return false;
         }
         if (event.getAction() == KeyEvent.ACTION_DOWN) {
             if (DBG_LOG_TIMING) {
                 dbgLogTiming("onSuggestionsKey()");
             }
             
             // First, check for enter or search (both of which we'll treat as a "click")
             if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
                 int position = mSearchAutoComplete.getListSelection();
                 return launchSuggestion(position);
             }
             
             // Next, check for left/right moves, which we use to "return" the user to the edit view
             if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                 // give "focus" to text editor, with cursor at the beginning if
                 // left key, at end if right key
                 // TODO: Reverse left/right for right-to-left languages, e.g. Arabic
                 int selPoint = (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ? 
                         0 : mSearchAutoComplete.length();
                 mSearchAutoComplete.setSelection(selPoint);
                 mSearchAutoComplete.setListSelection(0);
                 mSearchAutoComplete.clearListSelection();
                 return true;
             }
             
             // Next, check for an "up and out" move
             if (keyCode == KeyEvent.KEYCODE_DPAD_UP 
                     && 0 == mSearchAutoComplete.getListSelection()) {
                 restoreUserQuery();
                 // let ACTV complete the move
                 return false;
             }
             
             // Next, check for an "action key"
             SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
             if ((actionKey != null) && 
                     ((actionKey.getSuggestActionMsg() != null) || 
                      (actionKey.getSuggestActionMsgColumn() != null))) {
                 // launch suggestion using action key column
                 int position = mSearchAutoComplete.getListSelection();
                 if (position != ListView.INVALID_POSITION) {
                     Cursor c = mSuggestionsAdapter.getCursor();
                     if (c.moveToPosition(position)) {
                         final String actionMsg = getActionKeyMessage(c, actionKey);
                         if (actionMsg != null && (actionMsg.length() > 0)) {
                             return launchSuggestion(position, keyCode, actionMsg);
                         }
                     }
                 }
             }
         }
         return false;
     }
     
     /**
      * Launch a search for the text in the query text field.
      */
     protected void launchQuerySearch()  {
         launchQuerySearch(KeyEvent.KEYCODE_UNKNOWN, null);
     }
 
     /**
      * Launch a search for the text in the query text field.
      *
      * @param actionKey The key code of the action key that was pressed,
      *        or {@link KeyEvent#KEYCODE_UNKNOWN} if none.
      * @param actionMsg The message for the action key that was pressed,
      *        or <code>null</code> if none.
      */
     protected void launchQuerySearch(int actionKey, String actionMsg)  {
         String query = mSearchAutoComplete.getText().toString();
         Intent intent = createIntent(Intent.ACTION_SEARCH, null, null, query, null,
                 actionKey, actionMsg);
         launchIntent(intent);
     }
     
     /**
      * Launches an intent based on a suggestion.
      * 
      * @param position The index of the suggestion to create the intent from.
      * @return true if a successful launch, false if could not (e.g. bad position).
      */
     protected boolean launchSuggestion(int position) {
         return launchSuggestion(position, KeyEvent.KEYCODE_UNKNOWN, null);
     }
     
     /**
      * Launches an intent based on a suggestion.
      * 
      * @param position The index of the suggestion to create the intent from.
      * @param actionKey The key code of the action key that was pressed,
      *        or {@link KeyEvent#KEYCODE_UNKNOWN} if none.
      * @param actionMsg The message for the action key that was pressed,
      *        or <code>null</code> if none.
      * @return true if a successful launch, false if could not (e.g. bad position).
      */
     protected boolean launchSuggestion(int position, int actionKey, String actionMsg) {
         Cursor c = mSuggestionsAdapter.getCursor();
         if ((c != null) && c.moveToPosition(position)) {
 
             Intent intent = createIntentFromSuggestion(c, actionKey, actionMsg);
 
             // report back about the click
             if (mGlobalSearchMode) {
                 // in global search mode, do it via cursor
                 mSuggestionsAdapter.callCursorOnClick(c, position);
             } else if (intent != null
                     && mPreviousComponents != null
                     && !mPreviousComponents.isEmpty()) {
                 // in-app search (and we have pivoted in as told by mPreviousComponents,
                 // which is used for keeping track of what we pop back to when we are pivoting into
                 // in app search.)
                 reportInAppClickToGlobalSearch(c, intent);
             }
 
             // launch the intent
             launchIntent(intent);
 
             return true;
         }
         return false;
     }
 
     /**
      * Report a click from an in app search result back to global search for shortcutting porpoises.
      *
      * @param c The cursor that is pointing to the clicked position.
      * @param intent The intent that will be launched for the click.
      */
     private void reportInAppClickToGlobalSearch(Cursor c, Intent intent) {
         // for in app search, still tell global search via content provider
         Uri uri = getClickReportingUri();
         final ContentValues cv = new ContentValues();
         cv.put(SearchManager.SEARCH_CLICK_REPORT_COLUMN_QUERY, mUserQuery);
         final ComponentName source = mSearchable.getSearchActivity();
         cv.put(SearchManager.SEARCH_CLICK_REPORT_COLUMN_COMPONENT, source.flattenToShortString());
 
         // grab the intent columns from the intent we created since it has additional
         // logic for falling back on the searchable default
         cv.put(SearchManager.SUGGEST_COLUMN_INTENT_ACTION, intent.getAction());
         cv.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, intent.getDataString());
         cv.put(SearchManager.SUGGEST_COLUMN_INTENT_COMPONENT_NAME,
                         intent.getStringExtra(SearchManager.COMPONENT_NAME_KEY));
 
         // ensure the icons will work for global search
         cv.put(SearchManager.SUGGEST_COLUMN_ICON_1,
                         wrapIconForPackage(
                                 source,
                                 getColumnString(c, SearchManager.SUGGEST_COLUMN_ICON_1)));
         cv.put(SearchManager.SUGGEST_COLUMN_ICON_2,
                         wrapIconForPackage(
                                 source,
                                 getColumnString(c, SearchManager.SUGGEST_COLUMN_ICON_2)));
 
         // the rest can be passed through directly
         cv.put(SearchManager.SUGGEST_COLUMN_FORMAT,
                 getColumnString(c, SearchManager.SUGGEST_COLUMN_FORMAT));
         cv.put(SearchManager.SUGGEST_COLUMN_TEXT_1,
                 getColumnString(c, SearchManager.SUGGEST_COLUMN_TEXT_1));
         cv.put(SearchManager.SUGGEST_COLUMN_TEXT_2,
                 getColumnString(c, SearchManager.SUGGEST_COLUMN_TEXT_2));
         cv.put(SearchManager.SUGGEST_COLUMN_QUERY,
                 getColumnString(c, SearchManager.SUGGEST_COLUMN_QUERY));
         cv.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                 getColumnString(c, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID));
         // note: deliberately omitting background color since it is only for global search
         // "more results" entries
         mContext.getContentResolver().insert(uri, cv);
     }
 
     /**
      * @return A URI appropriate for reporting a click.
      */
     private Uri getClickReportingUri() {
         Uri.Builder uriBuilder = new Uri.Builder()
                 .scheme(ContentResolver.SCHEME_CONTENT)
                 .authority(SearchManager.SEARCH_CLICK_REPORT_AUTHORITY);
 
         uriBuilder.appendPath(SearchManager.SEARCH_CLICK_REPORT_URI_PATH);
 
         return uriBuilder
                 .query("")     // TODO: Remove, workaround for a bug in Uri.writeToParcel()
                 .fragment("")  // TODO: Remove, workaround for a bug in Uri.writeToParcel()
                 .build();
     }
 
     /**
      * Wraps an icon for a particular package.  If the icon is a resource id, it is converted into
      * an android.resource:// URI.
      *
      * @param source The source of the icon
      * @param icon The icon retrieved from a suggestion column
      * @return An icon string appropriate for the package.
      */
     private String wrapIconForPackage(ComponentName source, String icon) {
         if (icon == null || icon.length() == 0 || "0".equals(icon)) {
             // SearchManager specifies that null or zero can be returned to indicate
             // no icon. We also allow empty string.
             return null;
         } else if (!Character.isDigit(icon.charAt(0))){
             return icon;
         } else {
             String packageName = source.getPackageName();
             return new Uri.Builder()
                     .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                     .authority(packageName)
                     .encodedPath(icon)
                     .toString();
         }
     }
 
     /**
      * Launches an intent and dismisses the search dialog (unless the intent
      * is one of the special intents that modifies the state of the search dialog).
      */
     private void launchIntent(Intent intent) {
         if (intent == null) {
             return;
         }
         if (handleSpecialIntent(intent)){
             return;
         }
         dismiss();
         getContext().startActivity(intent);
     }
     
     /**
      * Handles the special intent actions declared in {@link SearchManager}.
      * 
      * @return <code>true</code> if the intent was handled.
      */
     private boolean handleSpecialIntent(Intent intent) {
         String action = intent.getAction();
         if (SearchManager.INTENT_ACTION_CHANGE_SEARCH_SOURCE.equals(action)) {
             handleChangeSourceIntent(intent);
             return true;
         }
         return false;
     }
     
     /**
      * Handles {@link SearchManager#INTENT_ACTION_CHANGE_SEARCH_SOURCE}.
      */
     private void handleChangeSourceIntent(Intent intent) {
         Uri dataUri = intent.getData();
         if (dataUri == null) {
             Log.w(LOG_TAG, "SearchManager.INTENT_ACTION_CHANGE_SOURCE without intent data.");
             return;
         }
         ComponentName componentName = ComponentName.unflattenFromString(dataUri.toString());
         if (componentName == null) {
             Log.w(LOG_TAG, "Invalid ComponentName: " + dataUri);
             return;
         }
         if (DBG) Log.d(LOG_TAG, "Switching to " + componentName);
         
         ComponentName previous = mLaunchComponent;
         if (!show(componentName, mAppSearchData, false)) {
             Log.w(LOG_TAG, "Failed to switch to source " + componentName);
             return;
         }
         pushPreviousComponent(previous);
 
         String query = intent.getStringExtra(SearchManager.QUERY);
         setUserQuery(query);
        mSearchAutoComplete.showDropDown();
     }
 
     /**
      * Sets the list item selection in the AutoCompleteTextView's ListView.
      */
     public void setListSelection(int index) {
         mSearchAutoComplete.setListSelection(index);
     }
 
     /**
      * Saves the previous component that was searched, so that we can go
      * back to it.
      */
     private void pushPreviousComponent(ComponentName componentName) {
         if (mPreviousComponents == null) {
             mPreviousComponents = new ArrayList<ComponentName>();
         }
         mPreviousComponents.add(componentName);
     }
     
     /**
      * Pops the previous component off the stack and returns it.
      * 
      * @return The component name, or <code>null</code> if there was
      *         no previous component.
      */
     private ComponentName popPreviousComponent() {
         if (mPreviousComponents == null) {
             return null;
         }
         int size = mPreviousComponents.size();
         if (size == 0) {
             return null;
         }
         return mPreviousComponents.remove(size - 1);
     }
     
     /**
      * Goes back to the previous component that was searched, if any.
      * 
      * @return <code>true</code> if there was a previous component that we could go back to.
      */
     private boolean backToPreviousComponent() {
         ComponentName previous = popPreviousComponent();
         if (previous == null) {
             return false;
         }
         if (!show(previous, mAppSearchData, false)) {
             Log.w(LOG_TAG, "Failed to switch to source " + previous);
             return false;
         }
         
         // must touch text to trigger suggestions
         // TODO: should this be the text as it was when the user left
         // the source that we are now going back to?
         String query = mSearchAutoComplete.getText().toString();
         setUserQuery(query);
         
         return true;
     }
     
     /**
      * When a particular suggestion has been selected, perform the various lookups required
      * to use the suggestion.  This includes checking the cursor for suggestion-specific data,
      * and/or falling back to the XML for defaults;  It also creates REST style Uri data when
      * the suggestion includes a data id.
      * 
      * @param c The suggestions cursor, moved to the row of the user's selection
      * @param actionKey The key code of the action key that was pressed,
      *        or {@link KeyEvent#KEYCODE_UNKNOWN} if none.
      * @param actionMsg The message for the action key that was pressed,
      *        or <code>null</code> if none.
      * @return An intent for the suggestion at the cursor's position.
      */
     private Intent createIntentFromSuggestion(Cursor c, int actionKey, String actionMsg) {
         try {
             // use specific action if supplied, or default action if supplied, or fixed default
             String action = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_ACTION);
 
             // some items are display only, or have effect via the cursor respond click reporting.
             if (SearchManager.INTENT_ACTION_NONE.equals(action)) {
                 return null;
             }
 
             if (action == null) {
                 action = mSearchable.getSuggestIntentAction();
             }
             if (action == null) {
                 action = Intent.ACTION_SEARCH;
             }
             
             // use specific data if supplied, or default data if supplied
             String data = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_DATA);
             if (data == null) {
                 data = mSearchable.getSuggestIntentData();
             }
             // then, if an ID was provided, append it.
             if (data != null) {
                 String id = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
                 if (id != null) {
                     data = data + "/" + Uri.encode(id);
                 }
             }
             Uri dataUri = (data == null) ? null : Uri.parse(data);
 
             String componentName = getColumnString(
                     c, SearchManager.SUGGEST_COLUMN_INTENT_COMPONENT_NAME);
 
             String query = getColumnString(c, SearchManager.SUGGEST_COLUMN_QUERY);
             String extraData = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
 
             return createIntent(action, dataUri, extraData, query, componentName, actionKey,
                     actionMsg);
         } catch (RuntimeException e ) {
             int rowNum;
             try {                       // be really paranoid now
                 rowNum = c.getPosition();
             } catch (RuntimeException e2 ) {
                 rowNum = -1;
             }
             Log.w(LOG_TAG, "Search Suggestions cursor at row " + rowNum + 
                             " returned exception" + e.toString());
             return null;
         }
     }
     
     /**
      * Constructs an intent from the given information and the search dialog state.
      * 
      * @param action Intent action.
      * @param data Intent data, or <code>null</code>.
      * @param extraData Data for {@link SearchManager#EXTRA_DATA_KEY} or <code>null</code>.
      * @param query Intent query, or <code>null</code>.
      * @param componentName Data for {@link SearchManager#COMPONENT_NAME_KEY} or <code>null</code>.
      * @param actionKey The key code of the action key that was pressed,
      *        or {@link KeyEvent#KEYCODE_UNKNOWN} if none.
      * @param actionMsg The message for the action key that was pressed,
      *        or <code>null</code> if none.
      * @return The intent.
      */
     private Intent createIntent(String action, Uri data, String extraData, String query,
             String componentName, int actionKey, String actionMsg) {
         // Now build the Intent
         Intent intent = new Intent(action);
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         if (data != null) {
             intent.setData(data);
         }
         intent.putExtra(SearchManager.USER_QUERY, mUserQuery);
         if (query != null) {
             intent.putExtra(SearchManager.QUERY, query);
         }
         if (extraData != null) {
             intent.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
         }
         if (componentName != null) {
             intent.putExtra(SearchManager.COMPONENT_NAME_KEY, componentName);
         }
         if (mAppSearchData != null) {
             intent.putExtra(SearchManager.APP_DATA, mAppSearchData);
         }
         if (actionKey != KeyEvent.KEYCODE_UNKNOWN) {
             intent.putExtra(SearchManager.ACTION_KEY, actionKey);
             intent.putExtra(SearchManager.ACTION_MSG, actionMsg);
         }
         // attempt to enforce security requirement (no 3rd-party intents)
         intent.setComponent(mSearchable.getSearchActivity());
         return intent;
     }
     
     /**
      * For a given suggestion and a given cursor row, get the action message.  If not provided
      * by the specific row/column, also check for a single definition (for the action key).
      * 
      * @param c The cursor providing suggestions
      * @param actionKey The actionkey record being examined
      * 
      * @return Returns a string, or null if no action key message for this suggestion
      */
     private static String getActionKeyMessage(Cursor c, SearchableInfo.ActionKeyInfo actionKey) {
         String result = null;
         // check first in the cursor data, for a suggestion-specific message
         final String column = actionKey.getSuggestActionMsgColumn();
         if (column != null) {
             result = SuggestionsAdapter.getColumnString(c, column);
         }
         // If the cursor didn't give us a message, see if there's a single message defined
         // for the actionkey (for all suggestions)
         if (result == null) {
             result = actionKey.getSuggestActionMsg();
         }
         return result;
     }
         
     /**
      * Local subclass for AutoCompleteTextView.
      */
     public static class SearchAutoComplete extends AutoCompleteTextView {
 
         private int mThreshold;
         private SearchDialog mSearchDialog;
         
         public SearchAutoComplete(Context context) {
             super(context);
             mThreshold = getThreshold();
         }
         
         public SearchAutoComplete(Context context, AttributeSet attrs) {
             super(context, attrs);
             mThreshold = getThreshold();
         }
 
         public SearchAutoComplete(Context context, AttributeSet attrs, int defStyle) {
             super(context, attrs, defStyle);
             mThreshold = getThreshold();
         }
 
         private void setSearchDialog(SearchDialog searchDialog) {
             mSearchDialog = searchDialog;
         }
         
         @Override
         public void setThreshold(int threshold) {
             super.setThreshold(threshold);
             mThreshold = threshold;
         }
 
         /**
          * Returns true if the text field is empty, or contains only whitespace.
          */
         private boolean isEmpty() {
             return TextUtils.getTrimmedLength(getText()) == 0;
         }
 
         /**
          * We override this method to avoid replacing the query box text
          * when a suggestion is clicked.
          */
         @Override
         protected void replaceText(CharSequence text) {
         }
         
         /**
          * We override this method so that we can allow a threshold of zero, which ACTV does not.
          */
         @Override
         public boolean enoughToFilter() {
             return mThreshold <= 0 || super.enoughToFilter();
         }
 
         /**
          * {@link AutoCompleteTextView#onKeyPreIme(int, KeyEvent)}) dismisses the drop-down on BACK,
          * so we must override this method to modify the BACK behavior.
          */
         @Override
         public boolean onKeyPreIme(int keyCode, KeyEvent event) {
             if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                 if (mSearchDialog.backToPreviousComponent()) {
                     return true;
                 }
                 return false; // will dismiss soft keyboard if necessary
             }
             return false;
         }
     }
     
     protected boolean handleBackKey(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
             if (backToPreviousComponent()) {
                 return true;
             }
             cancel();
             return true;
         }
         return false;
     }
     
     /**
      * Implements OnItemClickListener
      */
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         if (DBG) Log.d(LOG_TAG, "onItemClick() position " + position);
         launchSuggestion(position);
     }
 
     /** 
      * Implements OnItemSelectedListener
      */
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          if (DBG) Log.d(LOG_TAG, "onItemSelected() position " + position);
          // A suggestion has been selected, rewrite the query if possible,
          // otherwise the restore the original query.
          if (REWRITE_QUERIES) {
              rewriteQueryFromSuggestion(position);
          }
      }
 
      /** 
       * Implements OnItemSelectedListener
       */
      public void onNothingSelected(AdapterView<?> parent) {
          if (DBG) Log.d(LOG_TAG, "onNothingSelected()");
      }
      
      /**
       * Query rewriting.
       */
 
      private void rewriteQueryFromSuggestion(int position) {
          Cursor c = mSuggestionsAdapter.getCursor();
          if (c == null) {
              return;
          }
          if (c.moveToPosition(position)) {
              // Get the new query from the suggestion.
              CharSequence newQuery = mSuggestionsAdapter.convertToString(c);
              if (newQuery != null) {
                  // The suggestion rewrites the query.
                  if (DBG) Log.d(LOG_TAG, "Rewriting query to '" + newQuery + "'");
                  // Update the text field, without getting new suggestions.
                  setQuery(newQuery);
              } else {
                  // The suggestion does not rewrite the query, restore the user's query.
                  if (DBG) Log.d(LOG_TAG, "Suggestion gives no rewrite, restoring user query.");
                  restoreUserQuery();
              }
          } else {
              // We got a bad position, restore the user's query.
              Log.w(LOG_TAG, "Bad suggestion position: " + position);
              restoreUserQuery();
          }
      }
      
      /** 
       * Restores the query entered by the user if needed.
       */
      private void restoreUserQuery() {
          if (DBG) Log.d(LOG_TAG, "Restoring query to '" + mUserQuery + "'");
          setQuery(mUserQuery);
      }
      
      /**
       * Sets the text in the query box, without updating the suggestions.
       */
      private void setQuery(CharSequence query) {
          mSearchAutoComplete.setText(query, false);
          if (query != null) {
              mSearchAutoComplete.setSelection(query.length());
          }
      }
      
      /**
       * Sets the text in the query box, updating the suggestions.
       */
      private void setUserQuery(String query) {
          if (query == null) {
              query = "";
          }
          mUserQuery = query;
          mSearchAutoComplete.setText(query);
          mSearchAutoComplete.setSelection(query.length());
      }
 
     /**
      * Debugging Support
      */
 
     /**
      * For debugging only, sample the millisecond clock and log it.
      * Uses AtomicLong so we can use in multiple threads
      */
     private AtomicLong mLastLogTime = new AtomicLong(SystemClock.uptimeMillis());
     private void dbgLogTiming(final String caller) {
         long millis = SystemClock.uptimeMillis();
         long oldTime = mLastLogTime.getAndSet(millis);
         long delta = millis - oldTime;
         final String report = millis + " (+" + delta + ") ticks for Search keystroke in " + caller;
         Log.d(LOG_TAG,report);
     }
 }
