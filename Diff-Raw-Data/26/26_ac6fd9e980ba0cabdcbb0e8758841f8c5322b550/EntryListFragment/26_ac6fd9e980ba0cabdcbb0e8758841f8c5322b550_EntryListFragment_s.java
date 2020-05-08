 package com.gnuton.newshub;
 
 import android.app.Activity;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.gnuton.newshub.types.RSSEntry;
 import com.gnuton.newshub.tasks.DownloadWebTask;
 import com.gnuton.newshub.tasks.RSSParseTask;
 import com.gnuton.newshub.types.RSSFeed;
 import com.gnuton.newshub.utils.Notifications;
 import com.gnuton.newshub.utils.RSSFeedManager;
 
 import java.util.List;
 
 /**
  * Created by gnuton on 5/18/13.
  */
 public class EntryListFragment extends Fragment implements RSSFeedManager.OnEntryListFetchedListener {
     private static final String TAG = "MY_LIST_FRAGMENT";
     private OnItemSelectedListener itemSelectedListener;
    private RSSFeed mFeed;
 
     @Override
     public void onEntryListFetched(RSSFeed feed) {
         Context context = getActivity();
        ListView listView = (ListView) getView().findViewById(R.id.entrylistView);
 
         this.mFeed = feed;
 
         if (feed == null){
             listView.setAdapter(null);
             return;
         }
 
         // Creates data controller (adapter) for listview abd set "entries" as  data
         EntryListAdapter adapter = new EntryListAdapter(context, R.id.entrylistView, mFeed.entries);
         listView.setAdapter(adapter);
 
         // Define action (open activity) when a list item is selected
         listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
             private final List rssEntries = mFeed.entries;
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                 RSSEntry entry = (RSSEntry) mFeed.entries.get(i);
                 itemSelectedListener.onItemSelected(entry);
             }
         });
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
        if (mFeed != null)
            if (mFeed.entries != null) {
                //onParsingCompleted(this.mFeed);
            }
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
         RSSFeedManager mgr = RSSFeedManager.getInstance();
         mgr.requestEntryList(feed, this);
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
