 package ru.rutube.RutubeFeed.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 //import com.huewu.pla.lib.MultiColumnListView;
 //import com.huewu.pla.lib.internal.PLA_AdapterView;
 
 import ru.rutube.RutubeAPI.models.Constants;
 import ru.rutube.RutubeFeed.R;
 import ru.rutube.RutubeFeed.ctrl.FeedController;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Сергей
  * Date: 05.05.13
  * Time: 12:56
  * To change this template use File | Settings | File Templates.
  */
 public class FeedFragment extends Fragment implements FeedController.FeedView, AdapterView.OnItemClickListener {
     private static final String LOG_TAG = FeedFragment.class.getName();
     private MenuItem mRefreshItem;
     private MenuItem mSearchItem;
     private Uri feedUri;
     private ListView sgView;
     protected FeedController mController;
     private SearchView mSearchView;
 
     public ListAdapter getListAdapter() {
         return sgView.getAdapter();
     }
 
     public void setListAdapter(ListAdapter adapter) {
         sgView.setAdapter(adapter);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.feed_menu, menu);
         mRefreshItem = menu.findItem(R.id.menu_refresh);
         mSearchItem = menu.findItem(R.id.menu_search);
         Activity activity = getActivity();
         assert activity != null;
         // Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
         mSearchView = (SearchView) mSearchItem.getActionView();
         assert mSearchView != null;
         // Assumes current activity is the searchable activity
         mSearchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
         mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
         mSearchView.setFocusable(false);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Log.d(LOG_TAG, "onOptionsItemSelected");
         int id = item.getItemId();
         if(id == R.id.menu_refresh){
                 refreshFeed();
                 return true;
         }
         if (id == R.id.menu_search) {
             Log.d(LOG_TAG, "Search btn!");
             return false;
         }
         Log.d(LOG_TAG, "super.onOptionsItemSelected");
         return super.onOptionsItemSelected(item);
 
     }
 
     protected void refreshFeed() {
         mController.refresh();
 
     }
 
     @Override
     public void openPlayer(Uri uri, Uri thumbnailUri) {
         Activity activity = getActivity();
         assert activity != null;
         Intent intent = new Intent("ru.rutube.player.play");
         intent.setData(uri);
         intent.putExtra(Constants.Params.THUMBNAIL_URI, thumbnailUri);
         Log.d(LOG_TAG, "Starting player");
         startActivityForResult(intent, 0);
         Log.d(LOG_TAG, "Player started");
     }
 
     public void showError() {
         Activity activity = getActivity();
         if (activity != null)
         {
             AlertDialog.Builder builder = new AlertDialog.Builder(activity);
             builder.
                     setTitle(android.R.string.dialog_alert_title).
                     setMessage(getString(R.string.faled_to_load_data)).
                     create().
                     show();
         }
         doneRefreshing();
     }
 
     private void init() {
         initFeedUri();
         mController = new FeedController(feedUri);
         mController.attach(getActivity(), this);
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         mController.detach();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         Log.d(LOG_TAG, "onActivityCreated");
         setHasOptionsMenu(true);
         init();
     }
 
     private void initFeedUri() {
         Bundle args = getArguments();
         if (args != null)
             feedUri = args.getParcelable(Constants.Params.FEED_URI);
         if (feedUri == null) {
             Activity activity = getActivity();
             assert activity != null;
             feedUri = activity.getIntent().getData();
         }
         Log.d(LOG_TAG, "Feed Uri:" + String.valueOf(feedUri));
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         Log.d(LOG_TAG, "onPrepareOptionsMenu");
         mRefreshItem = menu.findItem(R.id.menu_refresh);
         super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public void onResume() {
         super.onResume();
        mSearchView.clearFocus();
     }
 
     public void setRefreshing() {
         if (mRefreshItem == null) {
             Log.d(LOG_TAG, "empty refresh item");
             return;
         }
         Activity activity = getActivity();
         if (activity == null)
             return;
         LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_btn, null);
         assert iv != null;
         Animation rotation = AnimationUtils.loadAnimation(activity, R.anim.rotate_icon);
         assert rotation != null;
         rotation.setRepeatCount(Animation.INFINITE);
         iv.startAnimation(rotation);
         mRefreshItem.setActionView(iv);
     }
 
     public void doneRefreshing() {
         if (mRefreshItem == null)
             return;
         View actionView = mRefreshItem.getActionView();
         if (actionView != null)
             actionView.clearAnimation();
         mRefreshItem.setActionView(null);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         setHasOptionsMenu(true);
         sgView = (ListView)inflater.inflate(R.layout.feed_fragment, container, false);
         assert sgView != null;
         sgView.setOnItemClickListener(this);
         return sgView;
     }
 
     @Override
     public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
         mController.onListItemClick(position);
     }
 }
