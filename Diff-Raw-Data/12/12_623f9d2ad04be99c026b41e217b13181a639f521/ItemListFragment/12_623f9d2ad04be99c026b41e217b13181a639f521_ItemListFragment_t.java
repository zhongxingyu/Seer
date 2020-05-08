 package com.smileyjoedev.catalogue;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.smileyjoedev.genLibrary.Debug;
 
 public class ItemListFragment extends SherlockListFragment{
 
 	private ArrayList<Item> items;
 	private DbItemAdapter itemAdapter;
 	private long categoryId;
 	private long locationId;
 	private Context context;
 	private ItemListAdapter adapter;
 	private boolean onCategories;
 	private boolean onLocations;
 	private boolean onSearch;
 	private String searchTerm;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Debug.d("## onCreate");
 		this.itemAdapter = new DbItemAdapter(this.context);
 		this.getItems();
 		this.adapter = new ItemListAdapter(this.context, this.items);
 		
 		setListAdapter(adapter);
 	}
 	
 	public void getItems(){
 		
 		if(this.onCategories){
 			Debug.d("On Categories");
 			if(this.categoryId == 0){
 				this.items = this.itemAdapter.get();
 			} else {
 				this.items = this.itemAdapter.getByCategory(this.categoryId);
 			}
 			
 		} else if (this.onLocations){
 			Debug.d("on Locations");
 			if(this.locationId == 0){
 				this.items = this.itemAdapter.get();
 			} else {
 				this.items = this.itemAdapter.getByLocation(this.locationId);
 			}
 		} else if(this.onSearch){
 			Debug.d("On Search");
 			if(this.searchTerm.equals("")){
 				this.items = this.itemAdapter.get();
 			} else {
 				this.items = this.itemAdapter.search(this.searchTerm);
 			}
 		} else {
 			Debug.d("On Nothing");
 			this.items = this.itemAdapter.get();
 		}
 		
 	}
 	
 	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// TODO: Save the details, do this for all fragments //
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// TODO: Restore all the data, do for all fragments //
	}
	
	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Toast.makeText(getActivity(),getListView().getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		
 		super.onAttach(activity);
 		
 		try {
 			CategoryDataInterface hostInterface;
 	        hostInterface = (CategoryDataInterface) activity;
 	        this.categoryId = hostInterface.getCategoryId();
 	        Debug.d("## onCategories");
 	        this.onCategories = true;
 	        this.onLocations = false;
 	        this.onSearch = false;
 	    } catch(ClassCastException e) {
 	    	this.onCategories = false;
 	    	this.categoryId = -1;
 	    }
 	    
 	    try {
 	    	LocationDataInterface hostInterface;
 	        hostInterface = (LocationDataInterface) activity;
 	        this.locationId = hostInterface.getLocationId();
 	        Debug.d("## onLocations");
 	        this.onLocations = true;
 	        this.onCategories = false;
 	        this.onSearch = false;
 	    } catch(ClassCastException e) {
 	    	this.onLocations = false;
 	    	this.locationId = -1;
 	    }
 	    
 	    try {
 	    	SearchDataInterface hostInterface;
 	        hostInterface = (SearchDataInterface) activity;
 	        this.searchTerm = hostInterface.getSearchTerm();
 	        Debug.d("## onSearch");
 	        this.onLocations = false;
 	        this.onCategories = false;
 	        this.onSearch = true;
 	    } catch(ClassCastException e) {
 	    	this.onSearch = false;
 	    	this.searchTerm = "";
 	    }
 	    
 	    this.context = activity.getApplicationContext();
 	}
 	
 	public void updateView(){
 		Debug.d("## UpdateView");
 		this.items.clear();
 		this.getItems();
 		Debug.d(this.items.size());
 		this.adapter.setData(this.items);
 		this.adapter.notifyDataSetChanged();
 	}
 	
 	public void changeCategory(long categoryId){
 		this.categoryId = categoryId;
 		this.updateView();
 	}
 	
 	public void changeLocation(long locationId){
 		this.locationId = locationId;
 		this.updateView();
 	}
 	
 	public void updateSearchResults(String searchTerm){
 		this.searchTerm = searchTerm;
 		this.updateView();
 	}
 	
 	public void getCategory(){
 		
 	}
 	
 }
 
