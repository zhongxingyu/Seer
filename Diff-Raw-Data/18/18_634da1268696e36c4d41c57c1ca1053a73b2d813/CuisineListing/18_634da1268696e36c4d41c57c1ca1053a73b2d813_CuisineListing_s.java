 package com.cellcity.citiguide.screen;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.cellcity.citiguide.info.Cuisine;
 import com.cellcity.citiguide.screen.CitiGuideListActivity.MenuListener;
 import com.cellcity.citiguide.util.Constants;
 import com.cellcity.citiguide.util.Util;
 
 public class CuisineListing extends CitiGuideListActivity {
 	
 	public static CuisineListing instance;
 	public static ArrayList<Cuisine> cuisines;
 	private ListViewAdapter m_adapter;
 	private Runnable queryThread;
 	private ProgressDialog progressDialog = null;
 	private String headerTxt;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTheme(R.style.Theme_Translucent);
 		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.listing);
 
 		instance = this;
 
 		initActivity(instance, "");
 		
 		ListView listView = (ListView)findViewById(android.R.id.list);
 		listView.setDividerHeight(0);
 		settingLayout();
 		init();
 	}
 
 	private void settingLayout() {
 		SharedPreferences preferences = getSharedPreferences(Constants.DEFAUL_SHARE_DATA, 0);
 		headerTxt = preferences.getString("name", "");
 		TextView titleView = (TextView) findViewById(R.id.templateTopTitleTView);
 		titleView.setText(headerTxt);
 		
 		Button mapButton = (Button)findViewById(R.id.mapButton);
 		mapButton.setVisibility(Button.GONE);
 		Button arButton = (Button)findViewById(R.id.arButton);
 		arButton.setVisibility(Button.GONE);
 		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.widget35);
 		linearLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
 		ImageView topMovie = (ImageView)findViewById(R.id.topmovie);
 		topMovie.setVisibility(ImageView.GONE);
 
 		Button footerSearch = (Button)findViewById(R.id.search_Button);
 		footerSearch.setOnClickListener(new MenuListener());
 		Button footerNearby = (Button)findViewById(R.id.nearbyButton);
 		footerNearby.setOnClickListener(new MenuListener());
 		Button footerShare = (Button)findViewById(R.id.shareButton);
 		footerShare.setOnClickListener(new MenuListener());
 		Button footerMap = (Button)findViewById(R.id.map_Button);
 		footerMap.setOnClickListener(new MenuListener());
 		Button homeButton = (Button)findViewById(R.id.homeButton);
 		homeButton.setOnClickListener(new MenuListener());
 	}
 
 	private void init() {
 		cuisines = new ArrayList<Cuisine>();
 		m_adapter = new ListViewAdapter(instance, R.layout.row_list, cuisines);
 		setListAdapter(m_adapter);
 		
 		try {
 			progressDialog = ProgressDialog.show(this, "", "Retrieving data...", true);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		queryThread = new Runnable() {
 			
 			@Override
 			public void run() {
 				getData();
 				runOnUiThread(addToMerchantList);
 			}
 		};
 		
 		Thread thread = new Thread(null, queryThread, "queryThread");
 		thread.start();
 	}
 
 	protected void getData() {
 		String result = "";
 		
 		result = Util.getHttpData(Constants.RESTAURANT_CUISINE_TYPES);
 		
 		if(result == null || result.equalsIgnoreCase("408") || result.equalsIgnoreCase("404")) {
			Util.showAlert(instance, "ILoveDeals", "Please make sure Internet connection is available.", "OK", false);
 		}
 		else {
 			result = Util.toJSONString(result);
 			cuisines = new ArrayList<Cuisine>();
 			
 			try {
 				JSONObject jsonObject1 = new JSONObject(result);
 				JSONArray nameArray = jsonObject1.getJSONArray("data");
 				
 				
 				for(int i = 0; i < nameArray.length(); i++) {
 					JSONObject jsonObject2 = nameArray.getJSONObject(i);
 					
 					int id = Integer.parseInt(jsonObject2.getString("ID"));
 					String name = jsonObject2.getString("CuisineType");
 					
 					Cuisine cuisineItem = new Cuisine(id, name);
 					cuisines.add(cuisineItem);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private Runnable addToMerchantList = new Runnable() {
 
 		@Override
 		public void run() {
 			if (cuisines != null && cuisines.size() > 0) {
 				m_adapter.notifyDataSetChanged();
 				for (int i = 0; i < cuisines.size(); i++) {
 					m_adapter.add(cuisines.get(i));
 				}
 			}
 			else {
 				//TODO
 			}
 			
 			try {
 				if (progressDialog.isShowing()) {
 					progressDialog.dismiss();
 				}					
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}		
 	};
 	
 	private class ListViewAdapter extends ArrayAdapter<Cuisine> {
 		
 		private ArrayList<Cuisine> cuisineItems;
 		
 		public ListViewAdapter(Context context, int resourceLayoutId, ArrayList<Cuisine> cuisineItems) {
 			super(context, resourceLayoutId, cuisineItems);
 			this.cuisineItems = cuisineItems;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			
 			if(view == null) {
 				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = layoutInflater.inflate(R.layout.row_list, null);
 			}
 			
 			final Cuisine cuisineItem = cuisineItems.get(position);
 			if(cuisineItem != null) {
 				TextView categoryName = (TextView)view.findViewById(R.id.topTView);
 				categoryName.setText(cuisineItem.getName());
 				TextView middle = (TextView)view.findViewById(R.id.middleTView);
 				TextView bottom = (TextView)view.findViewById(R.id.bottomTView);
 				middle.setVisibility(TextView.GONE);
 				bottom.setVisibility(TextView.GONE);
 			}
 			
 			return view;
 		}
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		MerchantListingScreen.catID = cuisines.get(position).getId();
 		Intent gourmet = new Intent(instance, MerchantListingScreen.class);
 		instance.startActivityForResult(gourmet, 0);
 	}
 }
