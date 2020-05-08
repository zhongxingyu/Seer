 package com.tryrosberry.ololostfilm.ui.fragments;
 
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.tryrosberry.ololostfilm.R;
 import com.tryrosberry.ololostfilm.logic.api.FeedParser;
 import com.tryrosberry.ololostfilm.logic.api.LostFilmRestClient;
 import com.tryrosberry.ololostfilm.logic.storage.ConstantStorage;
 import com.tryrosberry.ololostfilm.ui.activities.MainActivity;
 import com.tryrosberry.ololostfilm.ui.adapters.RssAdapter;
 import com.tryrosberry.ololostfilm.ui.models.RssItem;
 import com.tryrosberry.ololostfilm.utils.Connectivity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class RssFragment extends BaseFragment {
 
     private static final String ARG_POSITION = "position";
 
     private int position;
     private ListView mRssListView;
     private ArrayList<RssItem> mRssList;
     private RssAdapter mAdapter;
 
     public static RssFragment newInstance(int position) {
         RssFragment f = new RssFragment();
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
         mRssListView = (ListView) v.findViewById(R.id.contentListView);
         if(mRssListView.getAdapter() == null && mAdapter != null) mRssListView.setAdapter(mAdapter);
 
         return v;
     }
 
     @Override
     public void setUserVisibleHint(boolean isVisibleToUser) {
         super.setUserVisibleHint(isVisibleToUser);
         if (isVisibleToUser) {
             if(getActivity() != null)getData();
         }
 
     }
 
     @Override
     public void getData() {
         if(mRssList == null || mRssList.isEmpty()){
             if(getActivity() != null && Connectivity.isConnected(getActivity())){
                 LostFilmRestClient.get(ConstantStorage.RSS_URL, null, new AsyncHttpResponseHandler() {
                     private ProgressDialog mProgress;
 
                     @Override
                     public void onStart() {
                         super.onStart();
                         if (mProgress == null) {
                             mProgress = ProgressDialog.show(getActivity(), null,
                                     "Getting News...", true, true);
                         }
                     }
 
                     @Override
                     public void onFinish() {
                         super.onFinish();
                         if (mProgress != null) {
                             mProgress.dismiss();
                             mProgress = null;
                         }
                     }
 
                     @Override
                     public void onSuccess(String s) {
                         super.onSuccess(s);
                         if (getActivity() != null) {
                             mRssList = FeedParser.parseRss(FeedParser.parseResponse(s));
                            if(mAdapter == null) mAdapter = new RssAdapter(getActivity(),
                                     mRssList/*,mImageFetcher*/);
                             else mAdapter.setContent(mRssList);
 
                             mRssListView.setAdapter(mAdapter);
                         }
                     }
 
                     @Override
                     public void onFailure(Throwable throwable, String s) {
                         super.onFailure(throwable, s);
                         if (RssFragment.this.isAdded()) {
                             if (throwable instanceof IOException) {
                                 ((MainActivity) getActivity()).showMessage("Error", getString(R.string.error_internet) + "\n" + s);
                             } else ((MainActivity) getActivity()).showMessage("ERROR", throwable.toString());
                         }
                     }
                 });
             } else ((MainActivity)getActivity()).showMessage("Error",getString(R.string.error_internet));
         } else {
             if(mAdapter == null){
                 mAdapter = new RssAdapter(getActivity(),
                         mRssList/*,mImageFetcher*/);
                 mRssListView.setAdapter(mAdapter);
             } else mAdapter.setContent(mRssList);
 
         }
 
     }
 
 }
 
