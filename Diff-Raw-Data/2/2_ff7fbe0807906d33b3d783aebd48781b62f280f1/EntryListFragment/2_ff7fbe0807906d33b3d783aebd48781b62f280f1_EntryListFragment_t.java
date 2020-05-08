 package com.gnuton.newshub;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ListView;
 
 import com.gnuton.newshub.db.DbHelper;
 import com.gnuton.newshub.tasks.UpdateEntryInDB;
 import com.gnuton.newshub.types.RSSEntry;
 import com.gnuton.newshub.types.RSSFeed;
 import com.gnuton.newshub.utils.RSSFeedManager;
 
 /**
  * Created by gnuton on 5/18/13.
  */
 public class EntryListFragment extends Fragment implements RSSFeedManager.OnEntryListFetchedListener {
     private static final String TAG = "MY_LIST_FRAGMENT";
     private OnItemSelectedListener itemSelectedListener;
     private RSSFeed mFeed;
     private ListView mListView;
     private View mListViewHeader;
 
     @Override
     public void onEntryListFetched(final RSSFeed feed) {
         Context context = getActivity();
 
         this.mFeed = feed;
         setBusyIndicatorStatus(false);
 
         View v = getView();
         // This should prevent a crash. The feed will be set in the list as soon as the fragment starts.
         if (v == null ||mListView == null)
             return;
 
         if (feed == null){
             mListView.setAdapter(null);
             return;
         }
 
         // Creates data controller (adapter) for listview abd set "entries" as  data
         if (feed.adapter == null)
             feed.adapter = new EntryListAdapter(context, R.id.entrylistView, feed.entries);
         mListView.setAdapter(feed.adapter);
     }
 
     private void setBusyIndicatorStatus(Boolean busy){
         if (mListViewHeader == null)
             return;
 
         View spinner = mListViewHeader.findViewById(R.id.spinningImage);
         spinner.setVisibility(busy ? View.VISIBLE : View.GONE);
 
         if (busy){
             Animation animation = AnimationUtils.loadAnimation(getView().getContext(), R.animator.fadeout);
             spinner.startAnimation(animation);
         } else {
             spinner.clearAnimation();
         }
     }
 
     // Sends data to another fragment trough the activity using an internal interface.
     public interface OnItemSelectedListener {
         public void onItemSelected(RSSEntry entry);
     }
 
     // onAttach checks that activity implements itemSelectedListener
     @Override
     public void onAttach(Activity activity) {
         Log.d(TAG, "ATTACHED");
         super.onAttach(activity);
         if (activity instanceof OnItemSelectedListener) {
             itemSelectedListener = (OnItemSelectedListener) activity;
         } else {
             throw new ClassCastException(activity.toString() + " must implement EntryListFragment.OnItemSelectedListener");
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.d(TAG, "CREATEVIEW");
         View view = inflater.inflate(R.layout.entrylist_fragment, container, false);
         return view;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.d(TAG, "START");
         // called when fragment is visible
         onEntryListFetched(mFeed);
 
         mListView = (ListView) getView().findViewById(R.id.entrylistView);
         // Define action (open activity) when a list item is selected
         // NOTE: setOnItemClickListener MUST act directly on the adapter to get notifyDataSetChanged
         // working
         mListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                 HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) mListView.getAdapter();
                 EntryListAdapter adapter = (EntryListAdapter) hAdapter.getWrappedAdapter();
 
                RSSEntry entry = (RSSEntry) adapter.getItem((int)l);
 
                 // Set item as read
                 if (!entry.isRead) {
                     entry.isRead = true;
                     entry.columnsToUpdate.add(DbHelper.ENTRIES_ISREAD);
                     new UpdateEntryInDB().execute(entry);
                     adapter.notifyDataSetChanged();
                 }
 
                 itemSelectedListener.onItemSelected(entry);
             }
         });
 
         // Add header to list
         LayoutInflater inflater =
                 (LayoutInflater)getView().getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         mListViewHeader = inflater.inflate(R.layout.entrylist_header, mListView, false);
         mListView.addHeaderView(mListViewHeader);
         setBusyIndicatorStatus(false);
     }
 
     /*private void updateList() {
         Log.d(TAG, "UPDATE");
         if (this.mFeed == null)
             return;
         //String url ="http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";
         Context c = getActivity().getApplicationContext();
         ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             // fetch data
             new DownloadWebTask(this).execute(this.mFeed.url);
         } else {
             Log.w(TAG, "Device not connected");
             //TODO display error (use notification API?)
         }
 
         //String newTime = String.valueOf(System.currentTimeMillis());
     }*/
 
     public void setRSSFeed(RSSFeed feed) {
         this.mFeed= feed;
 
         // reset
         onEntryListFetched(null);
 
         // ask for data
         if (feed != null){
             setBusyIndicatorStatus(true);
             RSSFeedManager mgr = RSSFeedManager.getInstance();
             mgr.requestEntryList(feed, this);
         }
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         Log.d(TAG, "DESTROY");
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         Log.d(TAG, "DETACH");
     }
 }
