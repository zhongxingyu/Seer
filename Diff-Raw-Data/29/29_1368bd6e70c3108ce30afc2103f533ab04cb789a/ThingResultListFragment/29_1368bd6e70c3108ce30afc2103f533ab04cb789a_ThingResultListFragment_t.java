 /*   Copyright 2012 Mario Bhmer
  *
  *   Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) 
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 package com.blogspot.marioboehmer.thingibrowse.fragments;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.blogspot.marioboehmer.thingibrowse.OnNetworkErrorListener;
 import com.blogspot.marioboehmer.thingibrowse.R;
 import com.blogspot.marioboehmer.thingibrowse.ThingResultListActivity;
 import com.blogspot.marioboehmer.thingibrowse.adapter.ThingListAdapter;
 import com.blogspot.marioboehmer.thingibrowse.domain.ThingResultListItem;
 import com.blogspot.marioboehmer.thingibrowse.network.ThingException;
 import com.blogspot.marioboehmer.thingibrowse.network.ThingRequester;
 import com.blogspot.marioboehmer.thingibrowse.network.ThingRequester.ThingResultListType;
 
 /**
  * {@link Fragment} showing a list of {@link ThingResultListItem}s.
  * 
  * @author Mario Bhmer
  */
 public class ThingResultListFragment extends SherlockListFragment implements OnScrollListener{
 
 	private static final String TAG = ThingResultListFragment.class.getSimpleName();
 	
 	private OnThingSelectedListener onThingSelectedListener;
 	private OnNetworkErrorListener onNetworkErrorListener;
 	
 	private ThingListAdapter thingListAdapter;
 	private ArrayList<ThingResultListItem> thingResultList = new ArrayList<ThingResultListItem>();
 	
 	private ThingResultListType thingResultListType = ThingResultListType.NEWEST_THINGS;
 	private boolean isLoading = false;
 
 	private int currentPage = 1; 
 	private int lastPageIndex = -1;
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		ThingResultListItem thingResultListItem = (ThingResultListItem) l.getAdapter().getItem(position);
 		onThingSelectedListener.onThingSelected(thingResultListItem.getThingUrl());
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		thingListAdapter = new ThingListAdapter(getActivity().getApplicationContext());
 		setListAdapter(thingListAdapter);
 		if(savedInstanceState == null) {
 			loadThingResults(currentPage);
 		} else {
 			currentPage = savedInstanceState.getInt("pageNumber");
 			updateView((ArrayList<ThingResultListItem>)savedInstanceState.get("thingResultList"), true);
 		}
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.thing_result_list_fragment, container);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		getListView().setOnScrollListener(this);
 	}
 	
 	@Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
         	onThingSelectedListener = (OnThingSelectedListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + " must implement OnThingSelectedListener");
         }
         try {
         	onNetworkErrorListener = (OnNetworkErrorListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + " must implement OnNetworkErrorListener");
         }
     }
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable("thingResultList", thingResultList);
 		outState.putInt("pageNumber", currentPage);
 		super.onSaveInstanceState(outState);
 	}
 	
 	public interface OnThingSelectedListener {
         public void onThingSelected(String thingUrl);
     }
 	
 	public void refresh() {
 		currentPage = 1;
 		lastPageIndex = -1;
 		
 		loadThingResults(currentPage);
 	}
 	
 	private void loadThingResults(int pageNumber){
 		new AsyncTask<Integer, Void, ArrayList<ThingResultListItem>>() {
 
 			@Override
 			protected ArrayList<ThingResultListItem> doInBackground(Integer... params) {
 				isLoading = true;
 				ArrayList<ThingResultListItem> thingResultList = null;
 				try{
 					String html = ThingRequester.getInstance(getActivity().getApplicationContext()).getResponseHtmlForThingResultList(thingResultListType, params[0]);
 					thingResultList = ThingRequester.getInstance(getActivity().getApplicationContext()).getThingResultList(html);
 					if(currentPage == 1) {
 						lastPageIndex = ThingRequester.getInstance(getActivity().getApplicationContext()).getThingResultListLastPageIndex(html, thingResultListType);
 					}
 				} catch(IOException e) {
 					onNetworkErrorListener.onNetworkError();
 				} catch(ThingException e) {
					Log.w(TAG, "There was a problem while loading the ThingResultList: " + e);
 				} catch(Exception e) {
					Log.w(TAG, e);
 				}
 				return thingResultList;
 			}
 			
 			@Override
 			protected void onPostExecute(ArrayList<ThingResultListItem> result) {
 				if(result != null){
 					isLoading = false;
 					if(currentPage == 1) {
 						updateView(result, true);
 						if(((ThingResultListActivity)getActivity()).isDualFragmentsLayout) {
 							onListItemClick(getListView(), null, 0, 0);
 						}
 					} else {
 						updateView(result, false);
 					}
 				}
 			}	
 		}.execute(pageNumber);
 	}
 	
 	private void updateView(ArrayList<ThingResultListItem> thingResultListItems, boolean clearList) {
 		thingResultList = (thingResultListItems);
 		if(clearList) {
 			thingListAdapter.getThingResultList().clear();
 			thingListAdapter.notifyDataSetInvalidated();
 		}
 		thingListAdapter.getThingResultList().addAll(thingResultList);
 		thingListAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		//not implemented
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		boolean loadNextDataSet = firstVisibleItem + visibleItemCount >= totalItemCount;
 		if(loadNextDataSet && totalItemCount > 0 && !isLoading) {
 			currentPage++;
 			if(currentPage <= lastPageIndex) {
 				isLoading = true;
 				loadThingResults(currentPage);
 			}
 		}
 	}
 	
 	public void resetFragmentWithThingResultListType(ThingResultListType thingResultListType) {
 		this.thingResultListType = thingResultListType;
 		refresh();
 	}
 
 }
