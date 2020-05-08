 /*
  *    Copyright (c) 2012 Hai Bison
  *
  *    See the file LICENSE at the root directory of this project for copying
  *    permission.
  */
 
 package group.pals.android.lib.ui.filechooser;
 
 import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs;
 import group.pals.android.lib.ui.filechooser.providers.BaseFileProviderUtils;
 import group.pals.android.lib.ui.filechooser.providers.DbUtils;
 import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
 import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
 import group.pals.android.lib.ui.filechooser.providers.history.HistoryContract;
 import group.pals.android.lib.ui.filechooser.providers.history.HistoryProviderUtils;
 import group.pals.android.lib.ui.filechooser.providers.localfile.LocalFileContract;
 import group.pals.android.lib.ui.filechooser.providers.localfile.LocalFileProvider;
 import group.pals.android.lib.ui.filechooser.utils.E;
 import group.pals.android.lib.ui.filechooser.utils.EnvUtils;
 import group.pals.android.lib.ui.filechooser.utils.FileUtils;
 import group.pals.android.lib.ui.filechooser.utils.Ui;
 import group.pals.android.lib.ui.filechooser.utils.Utils;
 import group.pals.android.lib.ui.filechooser.utils.history.History;
 import group.pals.android.lib.ui.filechooser.utils.history.HistoryFilter;
 import group.pals.android.lib.ui.filechooser.utils.history.HistoryListener;
 import group.pals.android.lib.ui.filechooser.utils.history.HistoryStore;
 import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;
 import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;
 import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;
 import group.pals.android.lib.ui.filechooser.utils.ui.bookmark.BookmarkFragment;
 import group.pals.android.lib.ui.filechooser.utils.ui.history.HistoryFragment;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.Manifest;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 /**
  * Main activity for this library.<br>
  * <br>
  * <b>Notes:</b><br>
  * <br>
  * <b>I.</b> About keys {@link FileChooserActivity#_Rootpath},
  * {@link FileChooserActivity#_SelectFile} and preference
  * {@link DisplayPrefs#isRememberLastLocation(Context)}, the priorities of them
  * are:<br>
  * <li>1. {@link FileChooserActivity#_SelectFile}</li>
  * 
  * <li>2. {@link DisplayPrefs#isRememberLastLocation(Context)}</li>
  * 
  * <li>3. {@link FileChooserActivity#_Rootpath}</li>
  * 
  * @author Hai Bison
  * 
  */
 public class FileChooserActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 
     /**
      * The full name of this class. Generally used for debugging.
      */
     private static final String _ClassName = FileChooserActivity.class.getName();
 
     /**
      * Types of view.
      * 
      * @author Hai Bison
      * @since v4.0 beta
      */
     public static enum ViewType {
         /**
          * Use {@link ListView} to display file list.
          */
         List,
         /**
          * Use {@link GridView} to display file list.
          */
         Grid
     }
 
     /*---------------------------------------------
      * KEYS
      */
 
     /**
      * Sets value of this key to a theme in {@code android.R.style.Theme_*}.<br>
      * Default is:<br>
      * 
      * <li>{@link android.R.style#Theme_DeviceDefault} for {@code SDK >= }
      * {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}</li>
      * 
      * <li>{@link android.R.style#Theme_Holo} for {@code SDK >= }
      * {@link Build.VERSION_CODES#HONEYCOMB}</li>
      * 
      * <li>{@link android.R.style#Theme} for older systems</li>
      * 
      * @since v4.3 beta
      */
     public static final String _Theme = _ClassName + ".theme";
 
     /**
      * Key to hold the root path.<br>
      * <br>
      * If {@link LocalFileProvider} is used, then default is sdcard, if sdcard
      * is not available, "/" will be used.<br>
      * <br>
      * <b>Note</b>: The value of this key is a {@link Uri}
      */
     public static final String _Rootpath = _ClassName + ".rootpath";
 
     /**
      * Key to hold the authority of file provider.<br>
      * Default is {@link LocalFileContract#_Authority}.
      */
     public static final String _FileProviderAuthority = _ClassName + ".file_provider_authority";
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold filter mode, can be one of
      * {@link BaseFile#_FilterDirectoriesOnly},
      * {@link BaseFile#_FilterFilesAndDirectories},
      * {@link BaseFile#_FilterFilesOnly}. Default is
      * {@link BaseFile#_FilterFilesOnly}.
      */
     public static final String _FilterMode = _ClassName + ".filter_mode";
 
     // flags
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold max file count that's allowed to be listed, default =
      * {@code 1000}.
      */
     public static final String _MaxFileCount = _ClassName + ".max_file_count";
     /**
      * Key to hold multi-selection mode, default = {@code false}
      */
     public static final String _MultiSelection = _ClassName + ".multi_selection";
     /**
      * Key to hold the positive regex to filter files, default is {@code null}.
      * 
      * @since v5.1 beta
      */
     public static final String _PositiveRegexFilter = _ClassName + ".positive_regex_filter";
     /**
      * Key to hold the negative regex to filter files, default is {@code null}.
      * 
      * @since v5.1 beta
      */
     public static final String _NegativeRegexFilter = _ClassName + ".negative_regex_filter";
     /**
      * Key to hold display-hidden-files, default = {@code false}
      */
     public static final String _DisplayHiddenFiles = _ClassName + ".display_hidden_files";
     /**
      * Sets this to {@code true} to enable double tapping to choose files/
      * directories. In older versions, double tapping is default. However, since
      * v4.7 beta, single tapping is default. So if you want to keep the old way,
      * please set this key to {@code true}.
      * 
      * @since v4.7 beta
      */
     public static final String _DoubleTapToChooseFiles = _ClassName + ".double_tap_to_choose_files";
     /**
      * Sets the file you want to select when starting this activity. This is a
      * {@link Uri}.<br>
      * <b>Notes:</b><br>
      * <li>Currently this key is only used for single selection mode.</li>
      * 
      * <li>If you use save dialog mode, this key will override key
      * {@link #_DefaultFilename}.</li>
      * 
      * @since v4.7 beta
      */
     public static final String _SelectFile = _ClassName + ".select_file";
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold property save-dialog, default = {@code false}
      */
     public static final String _SaveDialog = _ClassName + ".save_dialog";
     /**
      * Key to hold default filename, default = {@code null}
      */
     public static final String _DefaultFilename = _ClassName + ".default_filename";
     /**
      * Key to hold results (can be one or multiple files)
      */
     public static final String _Results = _ClassName + ".results";
 
     /**
      * This key holds current location (a {@link Uri}), to restore it after
      * screen orientation changed.
      */
     private static final String _CurrentLocation = _ClassName + ".current_location";
     /**
      * This key holds current history (a {@link History}{@code <}{@link Uri}
      * {@code >}), to restore it after screen orientation changed
      */
     private static final String _History = _ClassName + ".history";
 
     private static final String _Path = _ClassName + ".path";
 
     /**
      * Default loader ID.
      */
     private final int _LoaderData = EnvUtils.genId();
 
     // ====================
     // "CONSTANT" VARIABLES
 
     /**
      * Task ID.
      */
     private static int mTaskId = 0;
 
     /**
      * Task ID for loading directory content.
      */
     private final int _IdLoaderData = newTaskId();
 
     private String mFileProviderAuthority;
     private Uri mRoot;
     private int mFilterMode;
     private int mMaxFileCount;
     private boolean mIsMultiSelection;
     private boolean mIsSaveDialog;
     private boolean mDoubleTapToChooseFiles;
 
     private History<Uri> mHistory;
     private Uri mCurrentLocation;
     private Handler mViewLoadingHandler = new Handler();
 
     /**
      * The adapter of list view.
      */
     private BaseFileAdapter mFileAdapter;
 
     private boolean mLoading = false;
 
     /*
      * Controls.
      */
 
     private View mBtnGoHome;
     private View mBtnBookmarkManager;
     private BookmarkFragment mBookmarkFragment;
     private HorizontalScrollView mViewLocationsContainer;
     private ViewGroup mViewLocations;
     private View mViewGroupFiles;
     private ViewGroup mViewFilesContainer;
     private TextView mTxtFullDirName;
     private AbsListView mViewFiles;
     private TextView mFooterView;
     private View mViewLoading;
     private Button mBtnOk;
     private EditText mTxtSaveas;
     private ImageView mViewGoBack;
     private ImageView mViewGoForward;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         /*
          * THEME
          */
 
         if (getIntent().hasExtra(_Theme)) {
             int theme;
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                 theme = getIntent().getIntExtra(_Theme, android.R.style.Theme_DeviceDefault);
             else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                 theme = getIntent().getIntExtra(_Theme, android.R.style.Theme_Holo);
             else
                 theme = getIntent().getIntExtra(_Theme, android.R.style.Theme);
             setTheme(theme);
         }
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.afc_file_chooser);
 
         initGestureDetector();
 
         /*
          * Load configurations.
          */
 
         mFileProviderAuthority = getIntent().getStringExtra(_FileProviderAuthority);
         if (mFileProviderAuthority == null)
             mFileProviderAuthority = LocalFileContract._Authority;
 
         mIsMultiSelection = getIntent().getBooleanExtra(_MultiSelection, false);
 
         mIsSaveDialog = getIntent().getBooleanExtra(_SaveDialog, false);
         if (mIsSaveDialog)
             mIsMultiSelection = false;
 
         mDoubleTapToChooseFiles = getIntent().getBooleanExtra(_DoubleTapToChooseFiles, false);
 
         mRoot = getIntent().getParcelableExtra(_Rootpath);
         mFilterMode = getIntent().getIntExtra(_FilterMode, BaseFile._FilterFilesOnly);
         mMaxFileCount = getIntent().getIntExtra(_MaxFileCount, 1000);
         mFileAdapter = new BaseFileAdapter(this, mFilterMode, mIsMultiSelection);
         mFileAdapter.setBuildOptionsMenuListener(mOnBuildOptionsMenuListener);
 
         /*
          * Load BookmarkFragment.
          */
 
         View viewBookmarks = findViewById(R.id.afc_filechooser_activity_fragment_bookmarks);
         if (viewBookmarks != null) {
             mBookmarkFragment = (BookmarkFragment) getSupportFragmentManager().findFragmentById(
                     R.id.afc_filechooser_activity_fragment_bookmarks);
             if (mBookmarkFragment == null) {
                 mBookmarkFragment = BookmarkFragment.newInstance(false);
                 mBookmarkFragment.setOnBookmarkItemClickListener(mBookmarkFragmentOnBookmarkItemClickListener);
 
                 FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                 ft.add(R.id.afc_filechooser_activity_fragment_bookmarks, mBookmarkFragment);
                 ft.commit();
             }
         }
 
         /*
          * Load controls.
          */
 
         mBtnGoHome = findViewById(R.id.afc_filechooser_activity_textview_home);
         mBtnBookmarkManager = findViewById(R.id.afc_filechooser_activity_textview_bookmarks);
         mViewGoBack = (ImageView) findViewById(R.id.afc_filechooser_activity_button_go_back);
         mViewGoForward = (ImageView) findViewById(R.id.afc_filechooser_activity_button_go_forward);
         mViewLocations = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_locations);
         mViewLocationsContainer = (HorizontalScrollView) findViewById(R.id.afc_filechooser_activity_view_locations_container);
         mTxtFullDirName = (TextView) findViewById(R.id.afc_filechooser_activity_textview_full_dir_name);
         mViewGroupFiles = findViewById(R.id.afc_filechooser_activity_viewgroup_files);
         mViewFilesContainer = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_files_container);
         mFooterView = (TextView) findViewById(R.id.afc_filechooser_activity_view_files_footer_view);
         mViewLoading = findViewById(R.id.afc_filechooser_activity_view_loading);
         mTxtSaveas = (EditText) findViewById(R.id.afc_filechooser_activity_textview_saveas_filename);
         mBtnOk = (Button) findViewById(R.id.afc_filechooser_activity_button_ok);
 
         /*
          * History.
          */
         if (savedInstanceState != null && savedInstanceState.get(_History) instanceof HistoryStore<?>)
             mHistory = savedInstanceState.getParcelable(_History);
         else
             mHistory = new HistoryStore<Uri>();
         mHistory.addListener(new HistoryListener<Uri>() {
 
             @Override
             public void onChanged(History<Uri> history) {
                 int idx = history.indexOf(getCurrentLocation());
                 mViewGoBack.setEnabled(idx > 0);
                 mViewGoForward.setEnabled(idx >= 0 && idx < history.size() - 1);
             }// onChanged()
         });
 
         /*
          * Make sure RESULT_CANCELED is default.
          */
         setResult(RESULT_CANCELED);
 
         setupHeader();
         setupViewFiles();
         setupFooter();
 
         loadInitialPath(savedInstanceState);
     }// onCreate()
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.afc_file_chooser_activity, menu);
         return true;
     }// onCreateOptionsMenu()
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         /*
          * Sorting.
          */
 
         final boolean _sortAscending = DisplayPrefs.isSortAscending(this);
         MenuItem miSort = menu.findItem(R.id.afc_filechooser_activity_menuitem_sort);
 
         switch (DisplayPrefs.getSortType(this)) {
         case BaseFile._SortByName:
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                 miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_name_asc
                         : R.drawable.afc_ic_menu_sort_by_name_desc);
             else
                 miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_name_asc_light
                         : R.drawable.afc_ic_menu_sort_by_name_desc_light);
             break;
         case BaseFile._SortBySize:
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                 miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_size_asc
                         : R.drawable.afc_ic_menu_sort_by_size_desc);
             else
                 miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_size_asc_light
                         : R.drawable.afc_ic_menu_sort_by_size_desc_light);
             break;
         case BaseFile._SortByModificationTime:
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                 miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_date_asc
                         : R.drawable.afc_ic_menu_sort_by_date_desc);
             else
                 miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_date_asc_light
                         : R.drawable.afc_ic_menu_sort_by_date_desc_light);
             break;
         }
 
         /*
          * View type.
          */
 
         MenuItem menuItem = menu.findItem(R.id.afc_filechooser_activity_menuitem_switch_viewmode);
         switch (DisplayPrefs.getViewType(this)) {
         case Grid:
             menuItem.setIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? R.drawable.afc_ic_menu_listview
                     : R.drawable.afc_ic_menu_listview_light);
             menuItem.setTitle(R.string.afc_cmd_list_view);
             break;
         case List:
             menuItem.setIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? R.drawable.afc_ic_menu_gridview
                     : R.drawable.afc_ic_menu_gridview_light);
             menuItem.setTitle(R.string.afc_cmd_grid_view);
             break;
         }
 
         /*
          * New folder.
          */
 
         menu.findItem(R.id.afc_filechooser_activity_menuitem_new_folder).setEnabled(!mLoading);
 
         return true;
     }// onPrepareOptionsMenu()
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_sort) {
             doResortViewFiles();
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_new_folder) {
             doCreateNewDir();
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_switch_viewmode) {
             doSwitchViewType();
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_home) {
             doGoHome();
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_reload) {
             goTo(getCurrentLocation());
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_history) {
             doShowHistoryManager();
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_bookmarks) {
             doShowBookmarkManager();
         }
 
         return true;
     }// onOptionsItemSelected()
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }// onConfigurationChanged()
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putParcelable(_CurrentLocation, getCurrentLocation());
         outState.putParcelable(_History, mHistory);
     }// onSaveInstanceState()
 
     @Override
     protected void onStart() {
         super.onStart();
         if (!mIsMultiSelection && !mIsSaveDialog && mDoubleTapToChooseFiles)
             Dlg.toast(this, R.string.afc_hint_double_tap_to_select_file, Dlg._LengthShort);
     }// onStart()
 
     @Override
     public void onBackPressed() {
         if (mLoading) {
             if (BuildConfig.DEBUG)
                 Log.d(_ClassName, "onBackPressed() >> cancelling previous query...");
             cancelPreviousLoader();
             Dlg.toast(FileChooserActivity.this, R.string.afc_msg_cancelled, Dlg._LengthShort);
         } else
             super.onBackPressed();
     }// onbackPressed()
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         cancelPreviousLoader();
         HistoryProviderUtils.doCleanupOutdatedHistoryItems(this);
     }// onDestroy()
 
     /*
      * LOADERMANAGER.LOADERCALLBACKS
      */
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         mLoading = true;
 
         mViewGroupFiles.setVisibility(View.GONE);
         mViewLoadingHandler.postDelayed(new Runnable() {
 
             @Override
             public void run() {
                 mViewLoading.setVisibility(View.VISIBLE);
             }// run()
         }, DisplayPrefs._DelayTimeForShortAnimation);
 
         supportInvalidateOptionsMenu();
 
         Uri path = ((Uri) args.getParcelable(_Path));
         createLocationButtons(path);
 
         String positiveRegex = getIntent().getStringExtra(_PositiveRegexFilter);
         String negativeRegex = getIntent().getStringExtra(_NegativeRegexFilter);
 
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "onCreateLoader() >> path = " + path);
 
         return new CursorLoader(this, path
                 .buildUpon()
                 .appendQueryParameter(BaseFile._ParamTaskId, Integer.toString(_IdLoaderData))
                 .appendQueryParameter(BaseFile._ParamListFiles, Boolean.toString(true))
                 .appendQueryParameter(BaseFile._ParamShowHiddenFiles,
                         Boolean.toString(getIntent().getBooleanExtra(_DisplayHiddenFiles, false)))
                 .appendQueryParameter(BaseFile._ParamFilterMode, Integer.toString(mFilterMode))
                 .appendQueryParameter(BaseFile._ParamSortBy, Integer.toString(DisplayPrefs.getSortType(this)))
                 .appendQueryParameter(BaseFile._ParamSortAscending,
                         Boolean.toString(DisplayPrefs.isSortAscending(this)))
                 .appendQueryParameter(BaseFile._ParamLimit, Integer.toString(mMaxFileCount))
                 .appendQueryParameter(BaseFile._ParamPositiveRegexFilter,
                         TextUtils.isEmpty(positiveRegex) ? "" : positiveRegex)
                 .appendQueryParameter(BaseFile._ParamNegativeRegexFilter,
                         TextUtils.isEmpty(negativeRegex) ? "" : negativeRegex).build(), null, null, null, null);
     }// onCreateLoader()
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
         mLoading = false;
 
         /*
          * Update list view.
          */
         mFileAdapter.changeCursor(data);
 
         mViewGroupFiles.setVisibility(View.VISIBLE);
         mViewLoadingHandler.removeCallbacksAndMessages(null);
         mViewLoading.setVisibility(View.GONE);
         supportInvalidateOptionsMenu();
 
         if (data == null) {
             showFooterView(true, getString(R.string.afc_msg_failed_please_try_again), true);
             return;
         }
 
         final Uri _lastPath = mHistory.prevOf(getCurrentLocation());
 
         data.moveToLast();
         final Uri _uriInfo = BaseFileProviderUtils.getUri(data);
         final Uri _selectedFile = (Uri) getIntent().getParcelableExtra(_SelectFile);
         final int _colUri = data.getColumnIndex(BaseFile._ColumnUri);
         if (_selectedFile != null)
             getIntent().removeExtra(_SelectFile);
 
         /*
          * Footer.
          */
 
         if (_selectedFile != null && mIsSaveDialog && BaseFileProviderUtils.isFile(this, _selectedFile))
             mTxtSaveas.setText(BaseFileProviderUtils.getFileName(this, _selectedFile));
 
         boolean hasMoreFiles = ProviderUtils.getBooleanQueryParam(_uriInfo, BaseFile._ParamHasMoreFiles);
         showFooterView(hasMoreFiles || mFileAdapter.isEmpty(),
                 hasMoreFiles ? getString(R.string.afc_pmsg_max_file_count_allowed, mMaxFileCount)
                         : getString(R.string.afc_msg_empty), mFileAdapter.isEmpty());
 
         /*
          * Select either the parent path of last path, or the file provided by
          * key _SelectFile. Use a Runnable to make sure this work. Because if
          * the list view is handling data, this might not work.
          */
         mViewFiles.post(new Runnable() {
 
             @Override
             public void run() {
                 int shouldBeSelectedIdx = -1;
                 final Uri _uri = _selectedFile != null ? _selectedFile : _lastPath;
                 if (_uri != null && BaseFileProviderUtils.fileExists(FileChooserActivity.this, _uri)) {
                     final String _fileName = BaseFileProviderUtils.getFileName(FileChooserActivity.this, _uri);
                     if (_fileName != null) {
                         Uri parentUri = BaseFileProviderUtils.getParentFile(FileChooserActivity.this, _uri);
                         if ((_uri == _lastPath && !getCurrentLocation().equals(_lastPath) && BaseFileProviderUtils
                                 .isAncestorOf(FileChooserActivity.this, getCurrentLocation(), _uri))
                                 || getCurrentLocation().equals(parentUri)) {
                             if (data.moveToFirst()) {
                                 while (!data.isLast()) {
                                     Uri subUri = Uri.parse(data.getString(_colUri));
                                     if (_uri == _lastPath) {
                                         if (data.getInt(data.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeDirectory) {
                                             if (BaseFileProviderUtils.isAncestorOf(FileChooserActivity.this, subUri,
                                                     _uri)) {
                                                 shouldBeSelectedIdx = Math.max(0, data.getPosition() - 2);
                                                 break;
                                             }
                                         }
                                     } else {
                                         if (_uri.equals(subUri)) {
                                            shouldBeSelectedIdx = Math.max(0, data.getPosition() - 2);
                                             break;
                                         }
                                     }
 
                                     data.moveToNext();
                                 }
                             }
                         }
                     }
                 }
 
                 if (shouldBeSelectedIdx >= 0 && shouldBeSelectedIdx < mFileAdapter.getCount())
                     mViewFiles.setSelection(shouldBeSelectedIdx);
                 else if (!mFileAdapter.isEmpty())
                     mViewFiles.setSelection(0);
             }// run()
         });
     }// onLoadFinished()
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         /*
          * Cancel previous loader if there is one.
          */
         cancelPreviousLoader();
 
         mFileAdapter.changeCursor(null);
         mViewGroupFiles.setVisibility(View.GONE);
         mViewLoadingHandler.postDelayed(new Runnable() {
 
             @Override
             public void run() {
                 mViewLoading.setVisibility(View.VISIBLE);
             }// run()
         }, DisplayPrefs._DelayTimeForShortAnimation);
 
         supportInvalidateOptionsMenu();
     }// onLoaderReset()
 
     /**
      * Generates new task ID.
      * 
      * @return the new task ID.
      */
     private int newTaskId() {
         return mTaskId++;
     }// newTaskId()
 
     /**
      * Connects to file provider service, then loads root directory. If can not,
      * then finishes this activity with result code =
      * {@link Activity#RESULT_CANCELED}
      * 
      * @param savedInstanceState
      */
     private void loadInitialPath(final Bundle savedInstanceState) {
         /*
          * Priorities for starting path:
          * 
          * 1. Current location (in case the activity has been killed after
          * configurations changed).
          * 
          * 2. Selected file from key _SelectFile.
          * 
          * 3. Last location.
          * 
          * 4. Root path from key _Rootpath.
          */
 
         // current location
         Uri path = (Uri) (savedInstanceState != null ? savedInstanceState.getParcelable(_CurrentLocation) : null);
 
         // selected file
         if (path == null) {
             path = (Uri) getIntent().getParcelableExtra(_SelectFile);
             if (path != null && BaseFileProviderUtils.fileExists(this, path))
                 path = BaseFileProviderUtils.getParentFile(this, path);
         }
 
         // last location
         if (path == null && DisplayPrefs.isRememberLastLocation(this)) {
             String lastLocation = DisplayPrefs.getLastLocation(this);
             if (lastLocation != null)
                 path = Uri.parse(lastLocation);
         }
 
         if (path == null || !BaseFileProviderUtils.isDirectory(this, path)) {
             path = mRoot;
             if (path == null || !BaseFileProviderUtils.isDirectory(this, path))
                 path = BaseFileProviderUtils.getDefaultPath(this, mFileProviderAuthority);
             if (path == null) {
                 doShowCannotConnectToServiceAndFinish();
                 return;
             }
         }
 
         if (!BaseFileProviderUtils.fileCanRead(this, path)) {
             Dlg.toast(FileChooserActivity.this,
                     getString(R.string.afc_pmsg_cannot_access_dir, BaseFileProviderUtils.getFileName(this, path)),
                     Dlg._LengthShort);
             finish();
         }
 
         setCurrentLocation(path);
 
         /*
          * Prepare the loader. Either re-connect with an existing one, or start
          * a new one.
          */
         Bundle b = new Bundle();
         b.putParcelable(_Path, path);
         getSupportLoaderManager().initLoader(_LoaderData, b, this);
     }// loadInitialPath()
 
     /**
      * Cancels the loader in progress.
      */
     private void cancelPreviousLoader() {
         /*
          * Adds a fake path...
          */
         getContentResolver().query(
                 getCurrentLocation().buildUpon()
                         .appendQueryParameter(BaseFile._ParamTaskId, Integer.toString(_IdLoaderData))
                         .appendQueryParameter(BaseFile._ParamCancel, Boolean.toString(true)).build(), null, null, null,
                 null);
         mLoading = false;
     }// cancelPreviousLoader()
 
     /**
      * As the name means...
      */
     private void doShowCannotConnectToServiceAndFinish() {
         Dlg.showError(this, R.string.afc_msg_cannot_connect_to_file_provider_service,
                 new DialogInterface.OnCancelListener() {
 
                     @Override
                     public void onCancel(DialogInterface dialog) {
                         setResult(Activity.RESULT_CANCELED);
                         finish();
                     }// onCancel()
                 });
     }// doShowCannotConnectToServiceAndFinish()
 
     /**
      * Setup:<br>
      * - title of activity;<br>
      * - button go back;<br>
      * - button location;<br>
      * - button go forward;
      */
     private void setupHeader() {
         if (mBtnGoHome != null)
             mBtnGoHome.setOnClickListener(mBtnGoHomeOnClickListener);
         if (mBtnBookmarkManager != null)
             mBtnBookmarkManager.setOnClickListener(mBtnBookmarkManagerOnClickListener);
 
         if (mIsSaveDialog) {
             setTitle(R.string.afc_title_save_as);
         } else {
             switch (mFilterMode) {
             case BaseFile._FilterFilesOnly:
                 setTitle(R.string.afc_title_choose_files);
                 break;
             case BaseFile._FilterFilesAndDirectories:
                 setTitle(R.string.afc_title_choose_files_and_directories);
                 break;
             case BaseFile._FilterDirectoriesOnly:
                 setTitle(R.string.afc_title_choose_directories);
                 break;
             }
         }// title of activity
 
         mViewGoBack.setEnabled(false);
         mViewGoBack.setOnClickListener(mBtnGoBackOnClickListener);
 
         mViewGoForward.setEnabled(false);
         mViewGoForward.setOnClickListener(mBtnGoForwardOnClickListener);
 
         for (ImageView v : new ImageView[] { mViewGoBack, mViewGoForward })
             v.setOnLongClickListener(mBtnGoBackForwardOnLongClickListener);
     }// setupHeader()
 
     /**
      * Setup:<br>
      * - {@link #mViewFiles}<br>
      * - {@link #mViewFilesContainer}<br>
      * - {@link #mFileAdapter}
      */
     private void setupViewFiles() {
         switch (DisplayPrefs.getViewType(this)) {
         case Grid:
             mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_gridview_files, null);
             break;
         case List:
             mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_listview_files, null);
             break;
         }
 
         mViewFilesContainer.removeAllViews();
         mViewFilesContainer.addView(mViewFiles, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT, 1));
 
         mViewFiles.setOnItemClickListener(mViewFilesOnItemClickListener);
         mViewFiles.setOnItemLongClickListener(mViewFilesOnItemLongClickListener);
         mViewFiles.setOnTouchListener(new View.OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 return mListviewFilesGestureDetector.onTouchEvent(event);
             }
         });
 
         /*
          * API 13+ does not recognize AbsListView.setAdapter(), so we cast it to
          * explicit class
          */
         if (mViewFiles instanceof ListView)
             ((ListView) mViewFiles).setAdapter(mFileAdapter);
         else
             ((GridView) mViewFiles).setAdapter(mFileAdapter);
 
         // no comments :-D
         mFooterView.setOnLongClickListener(new View.OnLongClickListener() {
 
             @Override
             public boolean onLongClick(View v) {
                 E.show(FileChooserActivity.this);
                 return false;
             }
         });
     }// setupViewFiles()
 
     /**
      * Setup:<br>
      * - button Cancel;<br>
      * - text field "save as" filename;<br>
      * - button Ok;
      */
     private void setupFooter() {
         // by default, view group footer and all its child views are hidden
 
         ViewGroup viewGroupFooterContainer = (ViewGroup) findViewById(R.id.afc_filechooser_activity_viewgroup_footer_container);
         ViewGroup viewGroupFooter = (ViewGroup) findViewById(R.id.afc_filechooser_activity_viewgroup_footer);
 
         if (mIsSaveDialog) {
             viewGroupFooterContainer.setVisibility(View.VISIBLE);
             viewGroupFooter.setVisibility(View.VISIBLE);
 
             mTxtSaveas.setVisibility(View.VISIBLE);
             mTxtSaveas.setText(getIntent().getStringExtra(_DefaultFilename));
             mTxtSaveas.setOnEditorActionListener(mTxtFilenameOnEditorActionListener);
 
             mBtnOk.setVisibility(View.VISIBLE);
             mBtnOk.setOnClickListener(mBtnOk_SaveDialog_OnClickListener);
             mBtnOk.setBackgroundResource(R.drawable.afc_selector_button_ok_saveas);
 
             int size = getResources().getDimensionPixelSize(R.dimen.afc_button_ok_saveas_size);
             LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mBtnOk.getLayoutParams();
             lp.width = size;
             lp.height = size;
             mBtnOk.setLayoutParams(lp);
         }// this is in save mode
         else {
             if (mIsMultiSelection) {
                 viewGroupFooterContainer.setVisibility(View.VISIBLE);
                 viewGroupFooter.setVisibility(View.VISIBLE);
 
                 ViewGroup.LayoutParams lp = viewGroupFooter.getLayoutParams();
                 lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                 viewGroupFooter.setLayoutParams(lp);
 
                 mBtnOk.setMinWidth(getResources().getDimensionPixelSize(R.dimen.afc_single_button_min_width));
                 mBtnOk.setText(android.R.string.ok);
                 mBtnOk.setVisibility(View.VISIBLE);
                 mBtnOk.setOnClickListener(mBtnOk_OpenDialog_OnClickListener);
             }
         }// this is in open mode
     }// setupFooter()
 
     /**
      * Shows footer view.
      * 
      * @param show
      *            {@code true} or {@code false}.
      * @param text
      *            the message you want to set.
      * @param center
      *            {@code true} or {@code false}.
      */
     private void showFooterView(boolean show, String text, boolean center) {
         if (show) {
             mFooterView.setText(text);
 
             RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                     RelativeLayout.LayoutParams.MATCH_PARENT);
             if (!center)
                 lp.addRule(RelativeLayout.ABOVE, R.id.afc_filechooser_activity_view_files_footer_view);
             mViewFilesContainer.setLayoutParams(lp);
 
             lp = (RelativeLayout.LayoutParams) mFooterView.getLayoutParams();
             lp.addRule(RelativeLayout.CENTER_IN_PARENT, center ? 1 : 0);
             lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, center ? 0 : 1);
             mFooterView.setLayoutParams(lp);
 
             mFooterView.setVisibility(View.VISIBLE);
         } else
             mFooterView.setVisibility(View.GONE);
     }// showFooterView()
 
     /**
      * Gets current location.
      * 
      * @return the current location.
      */
     private Uri getCurrentLocation() {
         return mCurrentLocation;
     }// getCurrentLocation()
 
     /**
      * Sets current location.
      * 
      * @param location
      *            the location to set.
      */
     private void setCurrentLocation(Uri location) {
         /*
          * Do this so history's listener will retrieve the right current
          * location.
          */
         Uri lastLocation = mCurrentLocation;
         mCurrentLocation = location;
 
         if (mHistory.indexOf(location) < 0) {
             mHistory.truncateAfter(lastLocation);
             mHistory.push(location);
         } else
             mHistory.notifyHistoryChanged();
 
         updateDbHistory(location);
     }// setCurrentLocation()
 
     private void doGoHome() {
         goTo(mRoot);
     }// doGoHome()
 
     /**
      * Shows bookmark manager.
      */
     private void doShowBookmarkManager() {
         BookmarkFragment bf = BookmarkFragment.newInstance(true);
         bf.setOnBookmarkItemClickListener(mBookmarkFragmentOnBookmarkItemClickListener);
         bf.show(getSupportFragmentManager().beginTransaction(), BookmarkFragment.class.getName());
     }// doShowBookmarkManager()
 
     /**
      * Shows history manager.
      */
     private void doShowHistoryManager() {
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "doShowHistoryManager()");
 
         // Create and show the dialog.
         final HistoryFragment _fragmentHistory = HistoryFragment.newInstance();
         _fragmentHistory.setOnHistoryItemClickListener(new HistoryFragment.OnHistoryItemClickListener() {
 
             @Override
             public void onItemClick(String providerId, final Uri uri) {
                 /*
                  * TODO what to do with `providerId`?
                  */
 
                 /*
                  * Check if `uri` is in internal list, then use it instead of
                  * that.
                  */
                 if (!mHistory.find(new HistoryFilter<Uri>() {
 
                     @Override
                     public boolean accept(Uri item) {
                         if (uri.equals(item)) {
                             goTo(item);
                             return true;
                         }
 
                         return false;
                     }// accept()
                 }, false))
                     goTo(uri);
             }// onItemClick()
         });
 
         /*
          * DialogFragment.show() will take care of adding the fragment in a
          * transaction. We also want to remove any currently showing dialog, so
          * make our own transaction and take care of that here.
          */
         _fragmentHistory.show(getSupportFragmentManager().beginTransaction(), HistoryFragment.class.getName());
     }// doShowHistoryManager()
 
     private static final int[] _BtnSortIds = { R.id.afc_settings_sort_view_button_sort_by_name_asc,
             R.id.afc_settings_sort_view_button_sort_by_name_desc, R.id.afc_settings_sort_view_button_sort_by_size_asc,
             R.id.afc_settings_sort_view_button_sort_by_size_desc, R.id.afc_settings_sort_view_button_sort_by_date_asc,
             R.id.afc_settings_sort_view_button_sort_by_date_desc };
 
     /**
      * Show a dialog for sorting options and resort file list after user
      * selected an option.
      */
     private void doResortViewFiles() {
         final AlertDialog _dialog = Dlg.newDlg(this);
 
         // get the index of button of current sort type
         int btnCurrentSortTypeIdx = 0;
         switch (DisplayPrefs.getSortType(this)) {
         case BaseFile._SortByName:
             btnCurrentSortTypeIdx = 0;
             break;
         case BaseFile._SortBySize:
             btnCurrentSortTypeIdx = 2;
             break;
         case BaseFile._SortByModificationTime:
             btnCurrentSortTypeIdx = 4;
             break;
         }
         if (!DisplayPrefs.isSortAscending(this))
             btnCurrentSortTypeIdx++;
 
         View.OnClickListener listener = new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 _dialog.dismiss();
 
                 Context c = FileChooserActivity.this;
 
                 if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_name_asc) {
                     DisplayPrefs.setSortType(c, BaseFile._SortByName);
                     DisplayPrefs.setSortAscending(c, true);
                 } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_name_desc) {
                     DisplayPrefs.setSortType(c, BaseFile._SortByName);
                     DisplayPrefs.setSortAscending(c, false);
                 } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_size_asc) {
                     DisplayPrefs.setSortType(c, BaseFile._SortBySize);
                     DisplayPrefs.setSortAscending(c, true);
                 } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_size_desc) {
                     DisplayPrefs.setSortType(c, BaseFile._SortBySize);
                     DisplayPrefs.setSortAscending(c, false);
                 } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_date_asc) {
                     DisplayPrefs.setSortType(c, BaseFile._SortByModificationTime);
                     DisplayPrefs.setSortAscending(c, true);
                 } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_date_desc) {
                     DisplayPrefs.setSortType(c, BaseFile._SortByModificationTime);
                     DisplayPrefs.setSortAscending(c, false);
                 }
 
                 /*
                  * Reload current location.
                  */
                 goTo(getCurrentLocation());
                 supportInvalidateOptionsMenu();
             }// onClick()
         };// listener
 
         View view = getLayoutInflater().inflate(R.layout.afc_settings_sort_view, null);
         for (int i = 0; i < _BtnSortIds.length; i++) {
             View v = view.findViewById(_BtnSortIds[i]);
             v.setOnClickListener(listener);
             if (i == btnCurrentSortTypeIdx) {
                 v.setEnabled(false);
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && v instanceof Button)
                     ((Button) v).setText(R.string.afc_bullet);
             }
         }
 
         _dialog.setTitle(R.string.afc_title_sort_by);
         _dialog.setView(view);
 
         _dialog.show();
     }// doResortViewFiles()
 
     /**
      * Switch view type between {@link ViewType#List} and {@link ViewType#Grid}
      */
     private void doSwitchViewType() {
         switch (DisplayPrefs.getViewType(FileChooserActivity.this)) {
         case Grid:
             DisplayPrefs.setViewType(FileChooserActivity.this, ViewType.List);
             break;
         case List:
             DisplayPrefs.setViewType(FileChooserActivity.this, ViewType.Grid);
             break;
         }
 
         setupViewFiles();
         supportInvalidateOptionsMenu();
         goTo(getCurrentLocation());
     }// doSwitchViewType()
 
     /**
      * Confirms user to create new directory.
      */
     private void doCreateNewDir() {
         if (LocalFileContract._Authority.equals(mFileProviderAuthority)
                 && !Utils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
             Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_create_files, Dlg._LengthShort);
             return;
         }
 
         if (getCurrentLocation() == null || !BaseFileProviderUtils.fileCanWrite(this, getCurrentLocation())) {
             Dlg.toast(this, R.string.afc_msg_cannot_create_new_folder_here, Dlg._LengthShort);
             return;
         }
 
         final AlertDialog _dlg = Dlg.newDlg(this);
 
         View view = getLayoutInflater().inflate(R.layout.afc_simple_text_input_view, null);
         final EditText _textFile = (EditText) view.findViewById(R.id.afc_simple_text_input_view_text1);
         _textFile.setHint(R.string.afc_hint_folder_name);
         _textFile.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 
             @Override
             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_DONE) {
                     Ui.showSoftKeyboard(v, false);
                     _dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                     return true;
                 }
                 return false;
             }
         });
 
         _dlg.setView(view);
         _dlg.setTitle(R.string.afc_cmd_new_folder);
         _dlg.setIcon(android.R.drawable.ic_menu_add);
         _dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok),
                 new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         String name = _textFile.getText().toString().trim();
                         if (!FileUtils.isFilenameValid(name)) {
                             Dlg.toast(FileChooserActivity.this, getString(R.string.afc_pmsg_filename_is_invalid, name),
                                     Dlg._LengthShort);
                             return;
                         }
 
                         ContentValues values = new ContentValues();
                         values.put(BaseFile._ColumnUri, name);
                         values.put(BaseFile._ColumnType, BaseFile._FileTypeDirectory);
 
                         if (getContentResolver().insert(getCurrentLocation(), values) != null) {
                             Dlg.toast(FileChooserActivity.this, getString(R.string.afc_msg_done), Dlg._LengthShort);
                             goTo(getCurrentLocation());
                         } else
                             Dlg.toast(FileChooserActivity.this,
                                     getString(R.string.afc_pmsg_cannot_create_folder, name), Dlg._LengthShort);
                     }// onClick()
                 });
         _dlg.show();
         Ui.showSoftKeyboard(_textFile, true);
 
         final Button _btnOk = _dlg.getButton(DialogInterface.BUTTON_POSITIVE);
         _btnOk.setEnabled(false);
 
         _textFile.addTextChangedListener(new TextWatcher() {
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 // do nothing
             }
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                 // do nothing
             }
 
             @Override
             public void afterTextChanged(Editable s) {
                 _btnOk.setEnabled(FileUtils.isFilenameValid(s.toString().trim()));
             }
         });
     }// doCreateNewDir()
 
     /**
      * Deletes a file.
      * 
      * @param position
      *            the position of item to be delete.
      */
     private void doDeleteFile(final int position) {
         Cursor cursor = (Cursor) mFileAdapter.getItem(position);
 
         /*
          * The cursor can be changed if the list view is updated, so we take its
          * properties here.
          */
         final boolean _isFile = BaseFileProviderUtils.isFile(cursor);
         final String _filename = BaseFileProviderUtils.getFileName(cursor);
 
         if (!BaseFileProviderUtils.fileCanWrite(cursor)) {
             Dlg.toast(
                     FileChooserActivity.this,
                     getString(R.string.afc_pmsg_cannot_delete_file, _isFile ? getString(R.string.afc_file)
                             : getString(R.string.afc_folder), _filename), Dlg._LengthShort);
             return;
         }
 
         if (LocalFileContract._Authority.equals(mFileProviderAuthority)
                 && !Utils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
             Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_delete_files, Dlg._LengthShort);
             return;
         }
 
         /*
          * The cursor can be changed if the list view is updated, so we take its
          * properties here.
          */
         final int _id = cursor.getInt(cursor.getColumnIndex(BaseFile._ID));
         final Uri _uri = BaseFileProviderUtils.getUri(cursor);
 
         mFileAdapter.markItemAsDeleted(_id, true);
 
         Dlg.confirmYesno(
                 this,
                 getString(R.string.afc_pmsg_confirm_delete_file, _isFile ? getString(R.string.afc_file)
                         : getString(R.string.afc_folder), _filename), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         new LoadingDialog(FileChooserActivity.this, getString(R.string.afc_pmsg_deleting_file,
                                 _isFile ? getString(R.string.afc_file) : getString(R.string.afc_folder), _filename),
                                 true) {
 
                             final int mTaskId = newTaskId();
 
                             private void notifyFileDeleted() {
                                 mHistory.removeAll(new HistoryFilter<Uri>() {
 
                                     @Override
                                     public boolean accept(Uri item) {
                                         return !BaseFileProviderUtils.isDirectory(FileChooserActivity.this, item);
                                     }// accept()
                                 });
                                 /*
                                  * TODO remove all duplicate items?
                                  */
 
                                 Dlg.toast(
                                         FileChooserActivity.this,
                                         getString(
                                                 R.string.afc_pmsg_file_has_been_deleted,
                                                 _isFile ? getString(R.string.afc_file) : getString(R.string.afc_folder),
                                                 _filename), Dlg._LengthShort);
                                 goTo(getCurrentLocation());
                             }// notifyFileDeleted()
 
                             @Override
                             protected Object doInBackground(Void... arg0) {
                                 getContentResolver().delete(
                                         _uri.buildUpon()
                                                 .appendQueryParameter(BaseFile._ParamTaskId, Integer.toString(mTaskId))
                                                 .build(), null, null);
 
                                 return null;
                             }// doInBackground()
 
                             @Override
                             protected void onCancelled() {
                                 getContentResolver().delete(
                                         _uri.buildUpon()
                                                 .appendQueryParameter(BaseFile._ParamTaskId, Integer.toString(mTaskId))
                                                 .appendQueryParameter(BaseFile._ParamCancel, Boolean.toString(true))
                                                 .build(), null, null);
 
                                 if (BaseFileProviderUtils.fileExists(FileChooserActivity.this, _uri)) {
                                     mFileAdapter.markItemAsDeleted(_id, false);
                                     Dlg.toast(FileChooserActivity.this, R.string.afc_msg_cancelled, Dlg._LengthShort);
                                 } else
                                     notifyFileDeleted();
 
                                 super.onCancelled();
                             }// onCancelled()
 
                             @Override
                             protected void onPostExecute(Object result) {
                                 super.onPostExecute(result);
 
                                 if (BaseFileProviderUtils.fileExists(FileChooserActivity.this, _uri)) {
                                     mFileAdapter.markItemAsDeleted(_id, false);
                                     Dlg.toast(
                                             FileChooserActivity.this,
                                             getString(R.string.afc_pmsg_cannot_delete_file,
                                                     _isFile ? getString(R.string.afc_file)
                                                             : getString(R.string.afc_folder), _filename),
                                             Dlg._LengthShort);
                                 } else
                                     notifyFileDeleted();
                             }// onPostExecute()
                         }.execute();// LoadingDialog
                     }// onClick()
                 }, new DialogInterface.OnCancelListener() {
 
                     @Override
                     public void onCancel(DialogInterface dialog) {
                         mFileAdapter.markItemAsDeleted(_id, false);
                     }// onCancel()
                 });
     }// doDeleteFile()
 
     /**
      * As the name means.
      * 
      * @param filename
      * @since v1.91
      */
     private void doCheckSaveasFilenameAndFinish(String filename) {
         if (!BaseFileProviderUtils.fileCanWrite(this, getCurrentLocation())) {
             Dlg.toast(this, getString(R.string.afc_msg_cannot_save_a_file_here), Dlg._LengthShort);
             return;
         }
         if (TextUtils.isEmpty(filename) || !FileUtils.isFilenameValid(filename)) {
             Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_invalid, filename), Dlg._LengthShort);
             return;
         }
 
         final Cursor _cursor = getContentResolver().query(
                 getCurrentLocation().buildUpon().appendQueryParameter(BaseFile._ParamAppendName, filename).build(),
                 null, null, null, null);
         if (_cursor != null) {
             try {
                 if (_cursor.moveToFirst()) {
                     final Uri _uri = BaseFileProviderUtils.getUri(_cursor);
                     switch (_cursor.getInt(_cursor.getColumnIndex(BaseFile._ColumnType))) {
                     case BaseFile._FileTypeDirectory:
                         Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_directory, filename), Dlg._LengthShort);
                         break;// _FileTypeDirectory
 
                     case BaseFile._FileTypeFile:
                         Dlg.confirmYesno(FileChooserActivity.this,
                                 getString(R.string.afc_pmsg_confirm_replace_file, filename),
                                 new DialogInterface.OnClickListener() {
 
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         doFinish(_uri);
                                     }// onClick()
                                 });
 
                         break;// _FileTypeFile
 
                     case BaseFile._FileTypeNotExisted:
                         /*
                          * TODO file type unknown?
                          */
                         doFinish(_uri);
                         break;// _FileTypeNotExisted
                     }
                 }
             } finally {
                 _cursor.close();
             }
         }
     }// doCheckSaveasFilenameAndFinish()
 
     /**
      * Goes to a specified location.
      * 
      * @param dir
      *            a directory, of course.
      * @return {@code true} if {@code dir} <b><i>can</i></b> be browsed to.
      * @since v4.3 beta
      */
     private boolean goTo(Uri dir) {
         if (dir == null)
             dir = BaseFileProviderUtils.getDefaultPath(this, mFileProviderAuthority);
         if (dir == null) {
             doShowCannotConnectToServiceAndFinish();
             return false;
         }
 
         /*
          * Check if the path of `dir` is same as current location, then set
          * `dir` to current location. This avoids of pushing two same paths into
          * history, because we compare the pointers (not the paths) when pushing
          * it to history.
          */
         if (dir.equals(getCurrentLocation()))
             dir = getCurrentLocation();
 
         if (BaseFileProviderUtils.fileCanRead(this, dir)) {
             /*
              * Cancel previous loader if there is one.
              */
             cancelPreviousLoader();
 
             setCurrentLocation(dir);
 
             Bundle b = new Bundle();
             b.putParcelable(_Path, dir);
             getSupportLoaderManager().restartLoader(_LoaderData, b, this);
             return true;
         }
 
         Dlg.toast(FileChooserActivity.this,
                 getString(R.string.afc_pmsg_cannot_access_dir, BaseFileProviderUtils.getFileName(this, dir)),
                 Dlg._LengthShort);
         return false;
     }// goTo()
 
     /**
      * Updates or inserts {@code path} into history database.
      */
     private void updateDbHistory(Uri path) {
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "updateDbHistory() >> path = " + path);
 
         Calendar cal = Calendar.getInstance();
         final long _beginTodayMillis = cal.getTimeInMillis()
                 - (cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 + cal.get(Calendar.MINUTE) * 60 * 1000 + cal
                         .get(Calendar.SECOND) * 1000);
         if (BuildConfig.DEBUG) {
             Log.d(_ClassName, String.format("beginToday = %s (%s)", DbUtils.formatNumber(_beginTodayMillis), new Date(
                     _beginTodayMillis)));
             Log.d(_ClassName,
                     String.format("endToday = %s (%s)", DbUtils.formatNumber(_beginTodayMillis
                             + DateUtils.DAY_IN_MILLIS), new Date(_beginTodayMillis + DateUtils.DAY_IN_MILLIS)));
         }
 
         /*
          * Does the update and returns the number of rows updated.
          */
         long time = new Date().getTime();
         ContentValues values = new ContentValues();
         values.put(HistoryContract.History._ColumnProviderId,
                 BaseFileProviderUtils.getProviderId(mFileProviderAuthority));
         values.put(HistoryContract.History._ColumnFileType, BaseFile._FileTypeDirectory);
         values.put(HistoryContract.History._ColumnUri, path.toString());
         values.put(HistoryContract.History._ColumnModificationTime, DbUtils.formatNumber(time));
 
         int count = getContentResolver().update(
                 HistoryContract.History._ContentUri,
                 values,
                 String.format("%s >= '%s' and %s < '%s' and %s = %s and %s like %s",
                         HistoryContract.History._ColumnModificationTime, DbUtils.formatNumber(_beginTodayMillis),
                         HistoryContract.History._ColumnModificationTime,
                         DbUtils.formatNumber(_beginTodayMillis + DateUtils.DAY_IN_MILLIS),
                         HistoryContract.History._ColumnProviderId,
                         DatabaseUtils.sqlEscapeString(values.getAsString(HistoryContract.History._ColumnProviderId)),
                         HistoryContract.History._ColumnUri,
                         DatabaseUtils.sqlEscapeString(values.getAsString(HistoryContract.History._ColumnUri))), null);
         if (count <= 0) {
             values.put(HistoryContract.History._ColumnCreateTime, DbUtils.formatNumber(time));
             getContentResolver().insert(HistoryContract.History._ContentUri, values);
         }
     }// updateDbHistory()
 
     /**
      * As the name means.
      */
     private void createLocationButtons(Uri path) {
         if (path == null)
             return;
 
         mViewLocations.removeAllViews();
 
         LinearLayout.LayoutParams lpBtnLoc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                 LinearLayout.LayoutParams.WRAP_CONTENT);
         lpBtnLoc.gravity = Gravity.CENTER;
         LinearLayout.LayoutParams lpDivider = null;
         LayoutInflater inflater = getLayoutInflater();
         final int _dim = getResources().getDimensionPixelSize(R.dimen.afc_5dp);
         int count = 0;
 
         Cursor cursor = getContentResolver().query(path, null, null, null, null);
         while (cursor != null) {
             String lastUri = null;
             if (cursor.moveToFirst()) {
                 lastUri = cursor.getString(cursor.getColumnIndex(BaseFile._ColumnUri));
 
                 TextView btnLoc = (TextView) inflater.inflate(R.layout.afc_button_location, null);
                 String name = BaseFileProviderUtils.getFileName(cursor);
                 btnLoc.setText(TextUtils.isEmpty(name) ? getString(R.string.afc_root) : name);
                 btnLoc.setTag(Uri.parse(lastUri));
                 btnLoc.setOnClickListener(mBtnLocationOnClickListener);
                 btnLoc.setOnLongClickListener(mBtnLocationOnLongClickListener);
                 mViewLocations.addView(btnLoc, 0, lpBtnLoc);
 
                 if (count++ == 0) {
                     Rect r = new Rect();
                     btnLoc.getPaint().getTextBounds(name, 0, name.length(), r);
                     if (r.width() >= getResources().getDimensionPixelSize(R.dimen.afc_button_location_max_width)
                             - btnLoc.getPaddingLeft() - btnLoc.getPaddingRight()) {
                         mTxtFullDirName.setText(cursor.getString(cursor.getColumnIndex(BaseFile._ColumnName)));
                         mTxtFullDirName.setVisibility(View.VISIBLE);
                     } else
                         mTxtFullDirName.setVisibility(View.GONE);
                 }
             }
 
             cursor.close();
 
             if (TextUtils.isEmpty(lastUri))
                 break;
 
             /*
              * Process the parent directory.
              */
             cursor = getContentResolver().query(
                     Uri.parse(lastUri).buildUpon()
                             .appendQueryParameter(BaseFile._ParamGetParent, Boolean.toString(true)).build(), null,
                     null, null, null);
             if (cursor != null) {
                 View divider = inflater.inflate(R.layout.afc_view_locations_divider, null);
 
                 if (lpDivider == null) {
                     lpDivider = new LinearLayout.LayoutParams(_dim, _dim);
                     lpDivider.gravity = Gravity.CENTER;
                     lpDivider.setMargins(_dim, _dim, _dim, _dim);
                 }
                 mViewLocations.addView(divider, 0, lpDivider);
             }
         }
 
         /*
          * Sometimes without delay time, it doesn't work...
          */
         mViewLocationsContainer.postDelayed(new Runnable() {
 
             public void run() {
                 mViewLocationsContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
             }
         }, DisplayPrefs._DelayTimeForVeryShortAnimation);
     }// createLocationButtons()
 
     /**
      * Finishes this activity.
      * 
      * @param files
      *            list of {@link Uri}.
      */
     private void doFinish(Uri... files) {
         List<Uri> list = new ArrayList<Uri>();
         for (Uri f : files)
             list.add(f);
         doFinish((ArrayList<Uri>) list);
     }// doFinish()
 
     /**
      * Finishes this activity.
      * 
      * @param files
      *            list of {@link Uri}.
      */
     private void doFinish(ArrayList<Uri> files) {
         if (files == null || files.isEmpty()) {
             setResult(RESULT_CANCELED);
             finish();
             return;
         }
 
         Intent intent = new Intent();
 
         // set results
         intent.putExtra(_Results, files);
 
         // return flags for further use (in case the caller needs)
         intent.putExtra(_FilterMode, mFilterMode);
         intent.putExtra(_SaveDialog, mIsSaveDialog);
 
         setResult(RESULT_OK, intent);
 
         if (DisplayPrefs.isRememberLastLocation(this) && getCurrentLocation() != null)
             DisplayPrefs.setLastLocation(this, getCurrentLocation().toString());
         else
             DisplayPrefs.setLastLocation(this, null);
 
         finish();
     }// doFinish()
 
     /**********************************************************
      * BUTTON LISTENERS
      */
 
     private final View.OnClickListener mBtnGoHomeOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             doGoHome();
         }// onClick()
     };// mBtnGoHomeOnClickListener
 
     private final View.OnClickListener mBtnBookmarkManagerOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             if (mBookmarkFragment != null)
                 mBookmarkFragment.setEditor(!mBookmarkFragment.isEditor());
         }// onClick()
     };// mBtnBookmarkManagerOnClickListener
 
     private final View.OnClickListener mBtnGoBackOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             /*
              * If user deleted a dir which was one in history, then maybe there
              * are duplicates, so we check and remove them here.
              */
             Uri currentLoc = getCurrentLocation();
             Uri preLoc = null;
 
             while (currentLoc.equals(preLoc = mHistory.prevOf(currentLoc)))
                 mHistory.remove(preLoc);
 
             if (preLoc != null)
                 goTo(preLoc);
             else
                 mViewGoBack.setEnabled(false);
         }
     };// mBtnGoBackOnClickListener
 
     private final View.OnClickListener mBtnLocationOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             if (v.getTag() instanceof Uri) {
                 goTo((Uri) v.getTag());
             }
         }// onClick()
     };// mBtnLocationOnClickListener
 
     private final View.OnLongClickListener mBtnLocationOnLongClickListener = new View.OnLongClickListener() {
 
         @Override
         public boolean onLongClick(View v) {
             if (BaseFile._FilterFilesOnly == mFilterMode || mIsSaveDialog)
                 return false;
 
             doFinish((Uri) v.getTag());
 
             return false;
         }// onLongClick()
 
     };// mBtnLocationOnLongClickListener
 
     private final View.OnClickListener mBtnGoForwardOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             /*
              * If user deleted a dir which was one in history, then maybe there
              * are duplicates, so we check and remove them here.
              */
             Uri currentLoc = getCurrentLocation();
             Uri nextLoc = null;
 
             while (currentLoc.equals(nextLoc = mHistory.nextOf(currentLoc)))
                 mHistory.remove(nextLoc);
 
             if (nextLoc != null)
                 goTo(nextLoc);
             else
                 mViewGoForward.setEnabled(false);
         }// onClick()
     };// mBtnGoForwardOnClickListener
 
     private final View.OnLongClickListener mBtnGoBackForwardOnLongClickListener = new View.OnLongClickListener() {
 
         @Override
         public boolean onLongClick(View v) {
             doShowHistoryManager();
             return true;
         }// onLongClick()
     };// mBtnGoBackForwardOnLongClickListener
 
     private final TextView.OnEditorActionListener mTxtFilenameOnEditorActionListener = new TextView.OnEditorActionListener() {
 
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             if (actionId == EditorInfo.IME_ACTION_DONE) {
                 Ui.showSoftKeyboard(v, false);
                 mBtnOk.performClick();
                 return true;
             }
             return false;
         }// onEditorAction()
     };// mTxtFilenameOnEditorActionListener
 
     private final View.OnClickListener mBtnOk_SaveDialog_OnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             Ui.showSoftKeyboard(v, false);
             String filename = mTxtSaveas.getText().toString().trim();
             doCheckSaveasFilenameAndFinish(filename);
         }// onClick()
     };// mBtnOk_SaveDialog_OnClickListener
 
     private final View.OnClickListener mBtnOk_OpenDialog_OnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             doFinish(mFileAdapter.getSelectedItems());
         }// onClick()
     };// mBtnOk_OpenDialog_OnClickListener
 
     /*
      * FRAGMENT LISTENERS
      */
 
     private final BookmarkFragment.OnBookmarkItemClickListener mBookmarkFragmentOnBookmarkItemClickListener = new BookmarkFragment.OnBookmarkItemClickListener() {
 
         @Override
         public void onItemClick(String providerId, final Uri uri) {
             /*
              * TODO what to do with `providerId`?
              */
 
             /*
              * Check if `uri` is in internal list, then use it instead of that.
              */
             if (!mHistory.find(new HistoryFilter<Uri>() {
 
                 @Override
                 public boolean accept(Uri item) {
                     if (uri.equals(item)) {
                         goTo(item);
                         return true;
                     }
 
                     return false;
                 }// accept()
             }, false))
                 goTo(uri);
         }// onItemClick()
     };// mBookmarkFragmentOnBookmarkItemClickListener
 
     /*
      * LIST VIEW HELPER
      */
 
     private GestureDetector mListviewFilesGestureDetector;
 
     private void initGestureDetector() {
         mListviewFilesGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
 
             private Object getData(float x, float y) {
                 int i = getSubViewId(x, y);
                 if (i >= 0)
                     return mViewFiles.getItemAtPosition(mViewFiles.getFirstVisiblePosition() + i);
                 return null;
             }// getSubView()
 
             private int getSubViewId(float x, float y) {
                 Rect r = new Rect();
                 for (int i = 0; i < mViewFiles.getChildCount(); i++) {
                     mViewFiles.getChildAt(i).getHitRect(r);
                     if (r.contains((int) x, (int) y))
                         return i;
                 }
 
                 return -1;
             }// getSubViewId()
 
             /**
              * Gets {@link Cursor} from {@code e}.
              * 
              * @param e
              *            {@link MotionEvent}.
              * @return the cursor, or {@code null} if not available.
              */
             private Cursor getData(MotionEvent e) {
                 Object o = getData(e.getX(), e.getY());
                 return o instanceof Cursor ? (Cursor) o : null;
             }// getDataModel()
 
             @Override
             public void onLongPress(MotionEvent e) {
                 // do nothing
             }// onLongPress()
 
             @Override
             public boolean onSingleTapConfirmed(MotionEvent e) {
                 // do nothing
                 return false;
             }// onSingleTapConfirmed()
 
             @Override
             public boolean onDoubleTap(MotionEvent e) {
                 if (mDoubleTapToChooseFiles) {
                     if (mIsMultiSelection)
                         return false;
 
                     Cursor data = getData(e);
                     if (data == null)
                         return false;
 
                     if (BaseFileProviderUtils.isDirectory(data) && BaseFile._FilterFilesOnly == mFilterMode)
                         return false;
 
                     /*
                      * If mFilterMode == _FilterDirectoriesOnly, files won't be
                      * shown.
                      */
 
                     if (mIsSaveDialog) {
                         if (BaseFileProviderUtils.isFile(data)) {
                             mTxtSaveas.setText(BaseFileProviderUtils.getFileName(data));
                             doCheckSaveasFilenameAndFinish(BaseFileProviderUtils.getFileName(data));
                         } else
                             return false;
                     } else
                         doFinish(BaseFileProviderUtils.getUri(data));
                 }// double tap to choose files
                 else {
                     // do nothing
                     return false;
                 }// single tap to choose files
 
                 return true;
             }// onDoubleTap()
 
             @Override
             public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                 /*
                  * Sometimes e1 or e2 can be null. This came from users'
                  * experiences.
                  */
                 if (e1 == null || e2 == null)
                     return false;
 
                 final int _max_y_distance = 19;// 10 is too short :-D
                 final int _min_x_distance = 80;
                 final int _min_x_velocity = 200;
                 if (Math.abs(e1.getY() - e2.getY()) < _max_y_distance
                         && Math.abs(e1.getX() - e2.getX()) > _min_x_distance && Math.abs(velocityX) > _min_x_velocity) {
                     int pos = getSubViewId(e1.getX(), e1.getY());
                     if (pos >= 0) {
                         /*
                          * Don't let this event to be recognized as a single
                          * tap.
                          */
                         MotionEvent cancelEvent = MotionEvent.obtain(e1);
                         cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                         mViewFiles.onTouchEvent(cancelEvent);
 
                         doDeleteFile(mViewFiles.getFirstVisiblePosition() + pos);
                     }
                 }
 
                 /*
                  * Always return false to let the default handler draw the item
                  * properly.
                  */
                 return false;
             }// onFling()
         });// mListviewFilesGestureDetector
     }// initGestureDetector()
 
     private final AdapterView.OnItemClickListener mViewFilesOnItemClickListener = new AdapterView.OnItemClickListener() {
 
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             Cursor cursor = (Cursor) mFileAdapter.getItem(position);
 
             if (BaseFileProviderUtils.isDirectory(cursor)) {
                 goTo(BaseFileProviderUtils.getUri(cursor));
                 return;
             }
 
             if (mIsSaveDialog)
                 mTxtSaveas.setText(BaseFileProviderUtils.getFileName(cursor));
 
             if (mDoubleTapToChooseFiles) {
                 // do nothing
                 return;
             }// double tap to choose files
             else {
                 if (mIsMultiSelection)
                     return;
 
                 if (mIsSaveDialog)
                     doCheckSaveasFilenameAndFinish(BaseFileProviderUtils.getFileName(cursor));
                 else
                     doFinish(BaseFileProviderUtils.getUri(cursor));
             }// single tap to choose files
         }// onItemClick()
     };// mViewFilesOnItemClickListener
 
     private final AdapterView.OnItemLongClickListener mViewFilesOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
 
         @Override
         public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
             Cursor cursor = (Cursor) mFileAdapter.getItem(position);
 
             if (mDoubleTapToChooseFiles) {
                 // do nothing
             }// double tap to choose files
             else {
                 if (!mIsSaveDialog
                         && !mIsMultiSelection
                         && BaseFileProviderUtils.isDirectory(cursor)
                         && (BaseFile._FilterDirectoriesOnly == mFilterMode || BaseFile._FilterFilesAndDirectories == mFilterMode)) {
                     doFinish(BaseFileProviderUtils.getUri(cursor));
                 }
             }// single tap to choose files
 
             /*
              * Notify that we already handled long click here.
              */
             return true;
         }// onItemLongClick()
     };// mViewFilesOnItemLongClickListener
 
     private final BaseFileAdapter.OnBuildOptionsMenuListener mOnBuildOptionsMenuListener = new BaseFileAdapter.OnBuildOptionsMenuListener() {
 
         @Override
         public void onBuildOptionsMenu(View view, Cursor cursor) {
             if (!BaseFileProviderUtils.fileCanRead(cursor) || !BaseFileProviderUtils.isDirectory(cursor))
                 return;
 
             final Uri _uri = BaseFileProviderUtils.getUri(cursor);
             final String _name = BaseFileProviderUtils.getFileName(cursor);
 
             ContextMenuUtils.showContextMenu(FileChooserActivity.this, 0, 0,
                     new Integer[] { R.string.afc_cmd_add_to_bookmarks },
                     new ContextMenuUtils.OnMenuItemClickListener() {
 
                         @Override
                         public void onClick(final int resId) {
                             if (resId == R.string.afc_cmd_add_to_bookmarks) {
                                 BookmarkFragment.doEnterNewNameOrRenameBookmark(FileChooserActivity.this,
                                         BaseFileProviderUtils.getProviderId(mFileProviderAuthority), -1, _uri, _name);
                             }
                         }// onClick()
                     });
         }// onBuildOptionsMenu()
 
         @Override
         public void onBuildAdvancedOptionsMenu(View view, Cursor cursor) {
             // TODO Auto-generated method stub
         }// onBuildAdvancedOptionsMenu()
     };// mOnBuildOptionsMenuListener
 }
