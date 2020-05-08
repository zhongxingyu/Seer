 package com.codepath.dealsapp.fragments;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 import com.codepath.dealsapp.DealDetailActivity;
 import com.codepath.dealsapp.DealsAdapter;
 import com.codepath.dealsapp.R;
 import com.codepath.dealsapp.YelpClient;
 import com.codepath.dealsapp.model.Deal;
 import com.codepath.dealsapp.model.DealManager;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class DealsListFragment extends ListFragment {
 	DealsAdapter dealsAdapter;
 	ListView lvDeals;
 	private int categoryID = 0;
 	DealManager dealManager;
 
     // Container Activity must implement this interface
     public interface OnDealSelectedListener {
         public void onDealSelected(int position);
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
     }
 	
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
     	Intent i = new Intent(getView().getContext(), DealDetailActivity.class);
 		i.putExtra("position", position);
 		startActivity(i);
     }
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 		      Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_deals_list, container, false);

 		String fontPath = "fonts/HelveticaNeueMedium.ttf";
         Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), fontPath);
         
         dealManager = DealManager.getInstance();
         dealsAdapter = new DealsAdapter(getActivity(), dealManager.getDeals(), typeFace);
 		
         dealManager.getDeals().clear();
         
 		lvDeals = (ListView) view.findViewById(android.R.id.list);
 		lvDeals.setAdapter(dealsAdapter);
 		
 		return view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
		//setListShown(false);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		int id = getArguments().getInt("categoryID", 0);
 		
 		if(categoryID == id && dealManager.getSize() > 0) {
 			dealsAdapter.notifyDataSetChanged();
 		} else {
 			loadDeals(id);
 		}
 	}
 	
 	public void loadDeals(int id) {
 		String requestUrl;
 		categoryID = id;
 		if(categoryID < 1) {
 			requestUrl = "http://api.8coupons.com/v1/getdeals?key=b115affda61e93374155aca0aeb6adf1c8e16e46cf992bd46201021556d3b2dff5b18ee48b51c78e7ceaff54649ad4c2&zip=94103&mileradius=5&limit=10&orderby=radius";
 		} else {
 			requestUrl = "http://api.8coupons.com/v1/getdeals?key=b115affda61e93374155aca0aeb6adf1c8e16e46cf992bd46201021556d3b2dff5b18ee48b51c78e7ceaff54649ad4c2&zip=94103&mileradius=5&limit=10&orderby=radius&categoryid="+categoryID;
 		}
 
 		Log.d("DEBUG", "list fragment requestUrl:"+requestUrl);
 		fetchDeals(requestUrl);
 	}
 	
 	public void fetchDeals(String requestUrl) {
 		AsyncHttpClient client = new AsyncHttpClient();
 		client.get(requestUrl, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray jsonArray) {
 				/*
 				deals.clear();
 				deals = Deal.fromJSONArray(jsonArray);
 				dealsAdapter.clear();
 				dealsAdapter.addAll(deals);
 				*/
 
 				Log.d("DEBUG", "size of response json:"+jsonArray.length());
 				dealManager.setCategoryID(categoryID);
 				dealManager.getDeals().clear();
 				dealManager.addDeals(Deal.fromJSONArray(jsonArray));
 				dealsAdapter.notifyDataSetChanged();
			//	setListShown(true);
 			}
 		});
 	}
 	
 	public void doYelpSearch() {
 		YelpClient client = new YelpClient(getActivity());
 		client.search("food", "san francisco", new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(int code, JSONObject body) {
 				try {
 					JSONArray businessesJson = body.getJSONArray("businesses");
 		                        // Here we now have the json array of businesses!
 					Log.d("DEBUG", businessesJson.toString());
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			@Override
 			public void onFailure(Throwable arg0) {
 				Log.d("DEBUG", "failed yelp search call");
 			}
 		});
 	}
 	
 
 }
