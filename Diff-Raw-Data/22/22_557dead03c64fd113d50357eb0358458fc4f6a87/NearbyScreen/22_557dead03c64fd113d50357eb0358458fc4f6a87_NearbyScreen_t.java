 package com.cellcity.citiguide.screen;
 
 import java.util.ArrayList;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import com.cellcity.citiguide.adapter.Controller;
 import com.cellcity.citiguide.info.ContentInfo;
 import com.cellcity.citiguide.util.Constants;
 import com.cellcity.citiguide.util.Util;
 
 public  class NearbyScreen extends CitiGuideListActivity implements OnClickListener, OnKeyListener {
 	public static  NearbyScreen instance;
 	private ProgressDialog progressDialog = null;
 	private ArrayList<ContentInfo> m_orders = null;
 	private ListViewAdapter m_adapter;
 	private Runnable viewOrders;
 	
 	private EditText nearbyET;
 	private TextView nearbyTV;
 	private static String nearbyText = "";
 	private double lat, lng;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTheme(android.R.style.Theme_Black_NoTitleBar);
 		getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
 		setContentView(R.layout.nearby);
 
 		instance = this;
 		
 		initActivity(instance, getText(R.string.nearby).toString());
 		init();
 	}
 	
 	public void init(){
 		getAddress();
 		settingLayout();
 		
 		try {
 			progressDialog = ProgressDialog.show(NearbyScreen.this,
 				"", getText(R.string.retrieving_data), true, true, new DialogInterface.OnCancelListener() {
 					
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						instance.finish();
 					}
 				});
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		m_orders = new ArrayList<ContentInfo>();
 		m_adapter = new ListViewAdapter(this, R.layout.choice_list, m_orders);
 		setListAdapter(m_adapter);
 		
 		
 		// == init title bar text ==//
 		TextView titleTView = (TextView)findViewById(R.id.templateTopTitleTView);
 		titleTView.setText(getString(R.string.nearby));
 	
 		viewOrders = new Runnable() {
 			@Override
 			public void run() {
 				ContentInfo cInfo1 = new ContentInfo(Constants.TYPE_GOURMET, Constants.TYPE_GOURMET+"", "Dining", "0", true);
 				//ContentInfo cInfo2 = new ContentInfo(Constants.TYPE_PROMOTION, Constants.TYPE_PROMOTION+"", "Citi Promotions", "0", false);
 				ContentInfo cInfo3 = new ContentInfo(Constants.TYPE_SHOPPING, Constants.TYPE_SHOPPING+"", "Shopping", "0", false);
 				ContentInfo cInfo4 = new ContentInfo(Constants.TYPE_BARS, Constants.TYPE_BARS+"", "Pubs 'n Clubs", "0", false);
 				//ContentInfo cInfo5 = new ContentInfo(Constants.TYPE_BANK, Constants.TYPE_BANK+"", "ATM Finder", "0", false);
 				//ContentInfo cInfo6 = new ContentInfo(Constants.TYPE_MOVIE, Constants.TYPE_MOVIE+"", "Latest Movies", "0", false);
 				ContentInfo cInfo7 = new ContentInfo(Constants.TYPE_HOTEL,Constants.TYPE_HOTEL+"", "Hotels","0", false);
 				m_orders = new ArrayList<ContentInfo>();
 				m_orders.add(cInfo1);
 				//m_orders.add(cInfo2);
 				m_orders.add(cInfo3);
 				m_orders.add(cInfo4);
 				//m_orders.add(cInfo5);
 				//m_orders.add(cInfo6);
 				m_orders.add(cInfo7);
 				
 				runOnUiThread(returnRes);
 			}
 		};
 		Thread thread = new Thread(null, viewOrders, "viewOrders");
 		thread.start();
 	}
 	
 	private Runnable returnRes = new Runnable() {
 		@Override
 		public void run() {
 			if (m_orders != null && m_orders.size() > 0) {
 				m_adapter.notifyDataSetChanged();
 				for (int i = 0; i < m_orders.size(); i++) {
 					m_adapter.add(m_orders.get(i));
 				}
 			}
 			
 			try {
 				if (progressDialog.isShowing())
 					progressDialog.dismiss();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	};
 	
 	private void settingLayout(){
 		//Util.getLatLongOfAddress(instance, "Singapore, Singapore");
 		Button map = (Button)findViewById(R.id.mapButton);
 		Button ar = (Button)findViewById(R.id.arButton);
 		map.setVisibility(Button.GONE);
 		ar.setVisibility(Button.GONE);
 		
 		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.widget35);
 		linearLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
 		
 		Button homeButton = (Button)findViewById(R.id.homeButton);
 		homeButton.setOnClickListener(new MenuListener());
 		
 		Button bCancel = (Button)findViewById(R.id.buttonCancel);
 		bCancel.setOnClickListener(this);
 		Button bSearch = (Button)findViewById(R.id.buttonSearch);
 		bSearch.setOnClickListener(this);
 		Button bChange = (Button)findViewById(R.id.buttonChange);
 		bChange.setOnClickListener(this);
 		
 		nearbyTV = (TextView)findViewById(R.id.nearbyDesc);
 		nearbyET = (EditText)findViewById(R.id.searchEText);
 		if(nearbyText.equalsIgnoreCase("")){
 			nearbyTV.setVisibility(TextView.GONE);
 			nearbyET.setVisibility(EditText.VISIBLE);
 			
 			bSearch.setVisibility(Button.VISIBLE);
 			bCancel.setVisibility(Button.VISIBLE);
 			bChange.setVisibility(Button.GONE);
 		}else{
 			nearbyTV.setVisibility(TextView.VISIBLE);
 			nearbyET.setVisibility(EditText.GONE);
 			
 			//nearbyText = dist + "";
 			
 			nearbyTV.setText(nearbyText);
 			
 			bSearch.setVisibility(Button.VISIBLE);
 			bCancel.setVisibility(Button.GONE);
 			bChange.setVisibility(Button.VISIBLE);
 		}
 	}
 	
 	/**
 	 * =============================================
 	 * inner class 
 	 * create list item.
 	 * =============================================
 	 * @author Administrator
 	 *
 	 */
 	private class ListViewAdapter extends ArrayAdapter<ContentInfo> {
 		private ArrayList<ContentInfo> items;
 		private int layoutResourceId;
 	    private RadioGroup mRadioGroup;
 	    
 		public ListViewAdapter(Context context, int textViewResourceId,
 				ArrayList<ContentInfo> items) {
 			super(context, textViewResourceId, items);
 			this.items = items;
 			layoutResourceId = textViewResourceId;
 		}
 		
 		@Override
 		public View getView(final int position, final View convertView, final ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(layoutResourceId, null);
 			}
 			
 			ContentInfo info = items.get(position);
 			if (info != null) {
 				ImageView imView = (ImageView) v.findViewById(R.id.choiceListIView);
 				imView.setImageResource(Controller.getResourceImageTypeId(info.getgType()));
 				
 				TextView tTView = (TextView) v.findViewById(R.id.choiceListTView);
 				if (tTView != null) {
 					tTView.setText(info.getCountryName());
 				}
 				
 				try {
 					CheckBox rButton = (CheckBox) v.findViewById(R.id.choiceListRButton);
 					int rId = Controller.getResourceImageTypeId(info.getgType());
 					rButton.setId(rId);
 					//info.setCheck(rButton.isChecked());
 					if(info.isCheck()) {
 						if(!rButton.isChecked()) {
 							rButton.toggle();
 						}
 						else {
 							
 						}
 					}
 					items.set(position, info);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 			return v;
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		disableKeyboard();
 		boolean rButton1 = ((CheckBox) findViewById(R.drawable.icon_gourmet_plesure)).isChecked();
 		//boolean rButton2 = ((CheckBox) findViewById(R.drawable.icon_promotions)).isChecked();
 		boolean rButton3 = ((CheckBox)findViewById(R.drawable.icon_shop)).isChecked();
 		boolean rButton4 = ((CheckBox)findViewById(R.drawable.icon_pub_and_bars)).isChecked();
 		//boolean rButton5 = ((CheckBox)findViewById(R.drawable.icon_atm)).isChecked();
 		//boolean rButton6 = ((CheckBox)findViewById(R.drawable.icon_movies)).isChecked();
 		boolean rButton7 = ((CheckBox)findViewById(R.drawable.icon_hotel)).isChecked();
 		String catId = "1";
 		
 		if(rButton1){
 			catId += Constants.TYPE_GOURMET+",";
 		}
 		//if(rButton2){
 		//	catId += Constants.TYPE_PROMOTION+",";
 		//}
 		if(rButton3){
 			catId += Constants.TYPE_SHOPPING+",";
 		}
 		if(rButton4){
 			catId += Constants.TYPE_BARS+",";
 		}
 		//if(rButton5){
 		//	catId += Constants.TYPE_BANK+",";
 		//}
 		//if(rButton6){
 		//	catId += Constants.TYPE_MOVIE+",";
 		//}
 		if(rButton7){
 			catId += Constants.TYPE_HOTEL+",";
 		}
 		if(catId.equalsIgnoreCase(""))
 			catId = Constants.TYPE_GOURMET+","+Constants.TYPE_PROMOTION+","+Constants.TYPE_SHOPPING+","+
 					Constants.TYPE_BARS+","+Constants.TYPE_BANK+","+Constants.TYPE_MOVIE+","+Constants.TYPE_HOTEL;
 		if(catId.endsWith(","))
 			catId = catId.substring(0, catId.length()-1);
 		
 		switch(v.getId()){
 		case R.id.buttonSearch:
 			SharedPreferences preferences = getSharedPreferences(Constants.DEFAUL_SHARE_DATA, 0);
 			SharedPreferences.Editor editor = preferences.edit();
 			editor.putString("name", nearbyText);
 			
 			if(nearbyET.isShown()){
 				if(nearbyET.getText().toString().trim().length() == 0){
 					Util.showAlert(instance, getString(R.string.nearby), getString(R.string.pls_insert_location),
 							getString(R.string.ok), false);
 					return;
 				}else{
 					nearbyText = nearbyET.getText().toString();
 					ListingMerchantScreen.querySearch = "&opType=search&cats="+catId+"&q="+nearbyText;
 				}
 			}else if(nearbyTV.isShown()){
 				ListingMerchantScreen.querySearch = "&opType=search&cats="+catId+"&p="+lat+","+lng;
 			}
 	
 			
 			Intent intent = new Intent(instance, ListingMerchantScreen.class);
 			startActivityForResult(intent, 0);
 			if (editor.commit()) {
 				setResult(RESULT_OK);
 			}
 			break;
 		case R.id.buttonChange:
 			nearbyText = "";
 			settingLayout();
 			break;
 		case R.id.buttonCancel:
 			getAddress();
 			settingLayout();
 //			instance.finish();
 			break;
 		}
 	}
 	
 	public void disableKeyboard(){
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(nearbyET.getWindowToken(), 0);
 	}
 	
 	private void getAddress(){
 		/**
 		 * GET ADDRESS NAME
 		 */
 		String addressName = "";
 		//Get the current location in start-up
 		lat = Constants.SINGAPORE_LATITUDE;
 		lng = Constants.SINGAPORE_LONGITUDE;
		
 		try {
 			System.out.println("lat lng : " + lat + " " + lng);
 			
 			if(MainCitiGuideScreen.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				lat = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
 				lng = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
 			}
 			else {
 				lat = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
 				lng = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
 			}
 			
			try {
				if((lat == Constants.SINGAPORE_LATITUDE && lng == Constants.SINGAPORE_LONGITUDE)) {
					double[] latLong = Util.queryLatLong(instance);
					lat = latLong[0];
					lng = latLong[1];
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			System.out.println("lat lng : " + lat + "," + lng);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			addressName = Util.getGPSAddress(instance, lat, lng);
 			if(addressName.trim().equalsIgnoreCase("")){
 				addressName = getString(R.string.can_not_get_gps);
 			}else{
 				nearbyText = addressName;
 			}
 		}
 		catch(Exception e) {
 			addressName = getString(R.string.can_not_get_gps);
 		}
 	}
 	
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		switch(keyCode){
 		case KeyEvent.KEYCODE_ENTER:
 			disableKeyboard();
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected void onResume() {
 		try {
 			if(MainCitiGuideScreen.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				lat = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
 				lng = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
 			}
 			else {
 				lat = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
 				lng = MainCitiGuideScreen.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
 			}
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 		super.onResume();
 	}
 	
 	@Override
 	protected void onPause() {
 		MainCitiGuideScreen.locationManager.removeUpdates(MainCitiGuideScreen.locationListener);
 		super.onPause();
 	}
 }
