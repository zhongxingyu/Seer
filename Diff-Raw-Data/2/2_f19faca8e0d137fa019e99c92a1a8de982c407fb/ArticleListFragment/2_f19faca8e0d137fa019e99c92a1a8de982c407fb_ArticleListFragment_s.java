 package org.gnuton.newshub;
 
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
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import org.gnuton.newshub.adapters.ArticleListAdapter;
 import org.gnuton.newshub.types.RSSEntry;
 import org.gnuton.newshub.types.RSSFeed;
 import org.gnuton.newshub.utils.FontsProvider;
 import org.gnuton.newshub.utils.RSSFeedManager;
 import org.gnuton.newshub.view.ArticleListEmptyView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Fragment which shows a list of aritcles
  */
 public class ArticleListFragment extends Fragment implements RSSFeedManager.OnEntryListFetchedListener {
     private static final String TAG = ArticleListFragment.class.getName();
     private OnItemSelectedListener itemSelectedListener;
     static private RSSFeed mFeed;
     private ListView mListView;
     private View mListViewHeader;
 
     @Override
     public synchronized void onEntryListFetched(final RSSFeed feed) {
         // No UI. We don't need to do anything here!!
         if (getView() == null ||mListView == null)
             return;
 
         mFeed = feed;
 
         if (feed == null){
             mListView.setAdapter(null);
         } else {
             if (feed.adapter == null){
                 // Creates data controller (adapter) for listview abd set "entries" as  data
                 feed.adapter = new ArticleListAdapter(getActivity(), R.id.entrylistView, feed.entries, feed.title);
                 mListView.setAdapter(feed.adapter);
             } else if (feed.adapter != mListView.getAdapter()) {
                 mListView.setAdapter(feed.adapter);
             }
             feed.adapter.notifyDataSetChanged();
         }
     }
 
     @Override
     public synchronized void setBusyIndicator(Boolean busy){
         if (mListViewHeader == null)
             return;
 
         View spinner = mListViewHeader.findViewById(R.id.spinningImage);
         spinner.setVisibility(busy ? View.VISIBLE : View.GONE);
 
         if (busy){
             Context ctx =getView().getContext();
             assert ctx != null;
             Animation animation = AnimationUtils.loadAnimation(ctx, R.animator.fadeout);
             assert animation != null;
             spinner.startAnimation(animation);
         } else {
             spinner.clearAnimation();
         }
     }
 
     // Sends data to another fragment trough the activity using an internal interface.
     public interface OnItemSelectedListener {
         public void onItemSelected(ArticleListAdapter adapter, int entryPosition);
     }
 
     // onAttach checks that activity implements itemSelectedListener
     @Override
     public void onAttach(Activity activity) {
         Log.d(TAG, "ATTACHED");
         super.onAttach(activity);
         if (activity instanceof OnItemSelectedListener) {
             itemSelectedListener = (OnItemSelectedListener) activity;
         } else {
             throw new ClassCastException(activity.toString() + " must implement ArticleListFragment.OnItemSelectedListener");
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.d(TAG, "CREATEVIEW");
         View view = inflater.inflate(R.layout.articlelist_fragment, container, false);
 
         // Add header to list
         assert view != null;
         mListView = (ListView) view.findViewById(R.id.entrylistView);
         mListViewHeader = inflater.inflate(R.layout.articlelist_header, mListView, false);
         mListView.addHeaderView(mListViewHeader);
         setBusyIndicator(false);
 
         // Add Empty List view
         //View articleListEmptyView = view.findViewById(R.id.ArticleListEmpty);
         View articleListEmptyView = new ArticleListEmptyView(getActivity());
         assert ((ViewGroup)mListView.getParent()) != null;
         ((ViewGroup)mListView.getParent()).addView(articleListEmptyView);
         mListView.setEmptyView(articleListEmptyView);
 
         RelativeLayout articleListEmptyMovingLayout = (RelativeLayout) articleListEmptyView.findViewById(R.id.ArticleListEmptyMovingLayout);
         Context ctx = articleListEmptyView.getContext();
         assert ctx != null;
         Animation animation = AnimationUtils.loadAnimation(ctx, R.animator.swipe);
         assert animation != null;
         articleListEmptyMovingLayout.startAnimation(animation);
         TextView articleListEmptyText = (TextView) articleListEmptyView.findViewById(R.id.ArticleListEmptyText);
         articleListEmptyText.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"));
 
         // Define action (open activity) when a list item is selected
         // NOTE: setOnItemClickListener MUST act directly on the adapter to get notifyDataSetChanged
         // working
         mListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                 HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) mListView.getAdapter();
                 ArticleListAdapter adapter = (ArticleListAdapter) hAdapter.getWrappedAdapter();
                 int entryPosition = (int) l;
                 itemSelectedListener.onItemSelected(adapter, entryPosition);
             }
         });
 
         // Logo
         TextView mainLogoTitle = (TextView) view.findViewById(R.id.MainLogoTitle);
         mainLogoTitle.setTypeface(FontsProvider.getInstace().getTypeface("Daily News 1915"));
         mainLogoTitle.setTextSize(110);
 
         /*TextView mainLogoPropIcon = (TextView) view.findViewById(R.id.mainLogoPropIcon);
         mainLogoPropIcon.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
         mainLogoPropIcon.setText(R.string.icon_rocket);*/
         //TextView mainLogoPropText = (TextView) view.findViewById(R.id.mainLogoPropText);
         //mainLogoPropText.setText("Fast");
 
         //
        if (mFeed != null){
             if (mListView !=null)
                 mListView.setAdapter(mFeed.adapter);
             mFeed.adapter.notifyDataSetChanged();
         }
 
         return view;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.d(TAG, "START");
         // called when fragment is visible
         onEntryListFetched(mFeed);
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
         mFeed= feed;
 
         // ask for data
         if (feed != null){
             RSSFeedManager mgr = RSSFeedManager.getInstance();
             // Create dummy adapter with one element
             List<RSSEntry> entries = new ArrayList<RSSEntry>();
             //entries.add(new RSSEntry(-1,-1,"",null,null,null));
             entries.add(new RSSEntry()); //FIXME quite ugly code!!
 
             ArticleListAdapter dummyAdapter = new ArticleListAdapter(getActivity(), R.id.entrylistView, entries, feed.title);
             if (mListView !=null)
                 mListView.setAdapter(dummyAdapter);
             dummyAdapter.notifyDataSetChanged();
 
             mgr.requestEntryList(feed, this);
         } else {
             onEntryListFetched(null);
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
