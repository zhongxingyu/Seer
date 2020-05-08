 package com.kosbrother.fragments;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 import com.novel.reader.R;
 import com.novel.reader.adapter.GridViewAdapter;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.entity.GameAPP;
 import com.novel.reader.entity.Novel;
 import com.taiwan.imageload.LoadMoreGridView;
 
 public class MyBookcaseFragment extends Fragment {
 
     private ArrayList<Novel> novels = new ArrayList<Novel>();
     private LoadMoreGridView myGrid;
     private GridViewAdapter  myGridViewAdapter;
     private LinearLayout     progressLayout;
     private LinearLayout     loadmoreLayout;
     private LinearLayout     noDataLayout;
 	private Activity mActivity;
 	public ArrayList<Novel> novelsFromServer;
     
     @Override
 	  public void onAttach(Activity activity) {
 	    super.onAttach(activity);
 	    mActivity= activity;
 	  }
 
     public static MyBookcaseFragment newInstance() {
 
         MyBookcaseFragment fragment = new MyBookcaseFragment();
 
         return fragment;
 
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         new DownloadChannelsTask().execute();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View myFragmentView = inflater.inflate(R.layout.loadmore_grid, container, false);
         progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
         loadmoreLayout = (LinearLayout) myFragmentView.findViewById(R.id.load_more_grid);
         noDataLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_no_data);
         myGrid = (LoadMoreGridView) myFragmentView.findViewById(R.id.news_list);
         myGrid.setOnLoadMoreListener(new LoadMoreGridView.OnLoadMoreListener() {
             public void onLoadMore() {
 
             }
         });
         return myFragmentView;
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
     }
 
     private class DownloadChannelsTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             // TODO Auto-generated method stub
             super.onPreExecute();
 
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             // TODO Auto-generated method stub
 
             novels = NovelAPI.getCollectedNovels(mActivity);
             // moreNovels = NovelAPI.getThisWeekHotNovels();
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             progressLayout.setVisibility(View.GONE);
             loadmoreLayout.setVisibility(View.GONE);
 
             if (novels != null && novels.size() != 0) {
                 try {
                     myGridViewAdapter = new GridViewAdapter(mActivity, novels, new ArrayList<GameAPP>());
                     myGrid.setAdapter(myGridViewAdapter);
                 } catch (Exception e) {
 
                 }
             } else {
                 myGrid.setVisibility(View.GONE);
                 noDataLayout.setVisibility(View.VISIBLE);
             }
 //            new UpdateServerCollectTask().execute();
             new getNovelsInfoFromServerTask().execute();
         }
     }
     
     private class getNovelsInfoFromServerTask extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... params) {
 
 			novelsFromServer = NovelAPI.getCollectNovelsInfoFromServer(novels);
 			return null;
 		}
 		
 		@Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             if(novelsFromServer != null){
             	myGridViewAdapter = new GridViewAdapter(mActivity, novelsFromServer, new ArrayList<GameAPP>());
             	myGrid.setAdapter(myGridViewAdapter);
            	NovelAPI.updateNovelsInfo(novelsFromServer, mActivity);
             }
         }
     	
     }
 
     
     private class UpdateServerCollectTask extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... params) {
 			String collect_novels_str = "";
 			for(Novel novel :novels){
 				collect_novels_str += novel.getId() + ",";
 			}
 			
 			NovelAPI.sendCollectedNovels(collect_novels_str, Settings.Secure.getString(mActivity.getContentResolver(),Settings.Secure.ANDROID_ID));
 			return null;
 		}
     	
     }
 
 }
