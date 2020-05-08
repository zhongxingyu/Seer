 package com.pc.programmerslife.fragments;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import java.util.ArrayList;
 import com.pc.programmerslife.Commic;
 import com.pc.programmerslife.CommicManager;
 import com.pc.programmerslife.R;
 import com.pc.programmerslife.activities.CommicActivity;
 import com.pc.programmerslife.adapters.ItemListAdapter;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 public class FavoritesFragment extends SherlockListFragment {
 	private ArrayList<Commic> commics;
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		configureView();
 		
 		commics = new ArrayList<Commic>();
 		
 		setListAdapter(new ItemListAdapter(getSherlockActivity(), R.layout.item_list_layout, commics));
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		load();
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Commic commic = commics.get(position);
 		
 		startActivity(new Intent(getSherlockActivity(), CommicActivity.class).putExtra(Commic.EXTRA_COMMIC, commic));
 	}
 	
 	private void configureView() {
 		getView().setBackgroundColor(Color.WHITE);
		getListView().setDividerHeight(1);
 		getListView().setSelector(getResources().getDrawable(R.drawable.list_selector_holo_light));
 		
 		setEmptyText(getString(R.string.emptyFavorites));
 	}
 	
 	private void load() {
 		commics.clear();
 		
 		ArrayList<Commic> dbCommics = CommicManager.getInstance().getFavorites();
 		
 		if (dbCommics != null)
 			commics.addAll(dbCommics);
 		
 		if (getListAdapter() instanceof ItemListAdapter)
 			((ItemListAdapter) getListAdapter()).notifyDataSetChanged();
 	}
 }
