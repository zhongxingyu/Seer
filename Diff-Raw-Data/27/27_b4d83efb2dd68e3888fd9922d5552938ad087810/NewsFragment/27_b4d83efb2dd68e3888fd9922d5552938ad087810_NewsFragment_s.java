 package com.tryrosberry.ololostfilm.ui.fragments.main;
 
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.tryrosberry.ololostfilm.R;
 import com.tryrosberry.ololostfilm.logic.api.HtmlParser;
 import com.tryrosberry.ololostfilm.logic.api.LostFilmRestClient;
 import com.tryrosberry.ololostfilm.ui.adapters.NewsAdapter;
 import com.tryrosberry.ololostfilm.ui.fragments.BaseFragment;
 import com.tryrosberry.ololostfilm.ui.models.NewsFeedItem;
 import com.tryrosberry.ololostfilm.utils.Connectivity;
 
 import java.io.IOException;
 import java.util.List;
 
 public class NewsFragment extends BaseFragment {
 
     private static final String ARG_POSITION = "position";
 
     private int position;
     private ListView mNewsListView;
     private List<NewsFeedItem> mNewsList;
     private NewsAdapter mAdapter;
     private int lastPage = 0;
     private int mPage = 0;
     private boolean loadingContent = false;
     private View footerView;
 
     public static NewsFragment newInstance(int position) {
         NewsFragment f = new NewsFragment();
         Bundle b = new Bundle();
         b.putInt(ARG_POSITION, position);
         f.setArguments(b);
         return f;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         position = getArguments().getInt(ARG_POSITION);
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View v = inflater.inflate(R.layout.list_layout,null);
         mNewsListView = (ListView) v.findViewById(R.id.contentListView);
         mNewsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
             @Override
             public void onScrollStateChanged(AbsListView view, int scrollState) {}
 
             @Override
             public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                 if(mNewsList != null && firstVisibleItem >= totalItemCount - 5){
                     if(mPage == lastPage){
                         mPage++;
                         getData();
                     }
                 }
             }
         });
 
        /*if(mNewsListView.getFooterViewsCount() == 0){
            setFooterView();
        } else {
            mNewsListView.removeFooterView(footerView);
            setFooterView();
        }*/

        if(mNewsListView.getAdapter() == null && mAdapter != null) {
            mNewsListView.setAdapter(mAdapter);
        }

         return v;
     }
 
     @Override
     public void setUserVisibleHint(boolean isVisibleToUser) {
         super.setUserVisibleHint(isVisibleToUser);
         if (isVisibleToUser) {
            if(getActivity() != null && mPage == 0)getData();
         }
 
     }
 
     @Override
     public void getData() {
         if(loadingContent) return;
         if(mNewsList == null || mNewsList.isEmpty() || (lastPage != mPage)){
             if(getActivity() != null && Connectivity.isConnected(getActivity())){
                 LostFilmRestClient.get(mPage == 0 ? "/news.php" : "/news.php?o="+mPage+"0", null, new AsyncHttpResponseHandler() {
                     private ProgressDialog mProgress;
 
                     @Override
                     public void onStart() {
                         super.onStart();
                         loadingContent = true;
                         getMainActivity().setProgressVisibility(true);
                         if (mPage == 0 && mProgress == null) {
                             mProgress = ProgressDialog.show(getActivity(), null,
                                     "Getting NewsFeed...", true, true);
                         }
                     }
 
                     @Override
                     public void onFinish() {
                         super.onFinish();
                         loadingContent = false;
                         if (mProgress != null) {
                             mProgress.dismiss();
                             mProgress = null;
                         }
                     }
 
                     @Override
                     public void onSuccess(final String s) {
                         super.onSuccess(s);
                         if (getActivity() != null) {
                             new Thread(new Runnable() {
                                 @Override
                                 public void run() {
                                     mNewsList = HtmlParser.parseNews(s);
                                     if(getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             getMainActivity().setProgressVisibility(false);
                                             if (mAdapter == null) mAdapter = new NewsAdapter(getActivity(),
                                                     mNewsList,getMainActivity().getImageFetcher());
                                             else{
                                                 if(lastPage == mPage) mAdapter.setContent(mNewsList);
                                                 else {
                                                     mAdapter.addContent(mNewsList);
                                                     mNewsList = mAdapter.getContent();
                                                     mAdapter.notifyDataSetChanged();
                                                 }
                                             }
 
                                             if(lastPage < mPage)lastPage++;
                                             else mNewsListView.setAdapter(mAdapter);
                                         }
                                     });
 
                                 }
                             }).start();
                         }
                     }
 
                     @Override
                     public void onFailure(Throwable throwable, String s) {
                         super.onFailure(throwable, s);
                         if(getActivity() != null) getMainActivity().setProgressVisibility(false);
                         if (NewsFragment.this.isAdded()) {
                             if(mPage > 0) mPage--;
                             if (throwable instanceof IOException) {
                                 getMainActivity().showMessage("Error", getString(R.string.error_internet) + "\n" + s);
                             } else getMainActivity().showMessage("ERROR", throwable.toString());
                         }
                     }
                 });
             } else getMainActivity().showMessage("Error", getString(R.string.error_internet));
         } else {
             if(mAdapter == null){
                 mAdapter = new NewsAdapter(getActivity(),
                         mNewsList,getMainActivity().getImageFetcher());
                 mNewsListView.setAdapter(mAdapter);
             } else mAdapter.setContent(mNewsList);
 
         }
 
     }
 
     private void setFooterView(){
         if(footerView == null){
             Button next = new Button(getActivity());
             next.setText("Next Page");
             next.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if(!loadingContent){
                         mPage++;
                         getData();
                     }
                 }
             });
             //next.setBackgroundResource(R.drawable.background_card);
             next.setPadding(10,5,10,5);
             footerView = next;
         }
         mNewsListView.addFooterView(footerView);
     }
 
 }
