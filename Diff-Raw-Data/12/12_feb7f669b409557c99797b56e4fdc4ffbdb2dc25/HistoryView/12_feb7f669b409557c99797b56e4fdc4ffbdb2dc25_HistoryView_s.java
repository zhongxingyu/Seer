 package com.vuzzz.android.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ItemSelect;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.vuzzz.android.OnSettingsUpdatedListener;
 import com.vuzzz.android.R;
 import com.vuzzz.android.adapter.HistoryListAdapter;
 import com.vuzzz.android.model.Rating;
 
 @EViewGroup(R.layout.history)
 public class HistoryView extends LinearLayout implements OnSettingsUpdatedListener {
 
 	@Bean
 	HistoryListAdapter adapter;
 	
 	List<Rating> currentRatings = new ArrayList<Rating>();
 	
 	@ViewById
 	protected ListView historyList;
 
 	private final OnRatingSelectedListener onRatingSelectedListener;
 	
 	public HistoryView(Context context, OnRatingSelectedListener onRatingSelectedListener) {
 		super(context);
 		this.onRatingSelectedListener = onRatingSelectedListener;
 	}
 
 	@AfterViews
 	public void afterViews(){
 		historyList.setAdapter(adapter);
 		historyList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				onRatingSelectedListener.onRatingSelectedListener((Rating)adapter.getItem(arg2));
 			}
 		});
 	}
 	
 	public void setRatings(List<Rating> ratings){
 		this.currentRatings = ratings;
 		adapter.setRatings(ratings);
 	}
 
 	@Override
 	public void onSettingsUpdated() {
 		adapter.setRatings(currentRatings);
 	}
 }
