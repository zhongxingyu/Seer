 package com.appirio.mobile;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.cordova.CordovaWebViewClient;
 import org.json.JSONArray;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.appirio.mobile.aau.nativemap.AMException;
 import com.appirio.mobile.aau.nativemap.MapManager;
 import com.appirio.mobile.aau.nativemap.Route;
 import com.appirio.mobile.aau.nativemap.StopListAdapter;
 import com.appirio.aau.R;
 
 import com.appirio.mobile.aau.slidingmenu.SlidingMenuAdapter;
 import com.appirio.mobile.aau.slidingmenu.SlidingMenuItem;
 import com.appirio.mobile.aau.slidingmenu.SlidingMenuLayout;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapView;
 import com.salesforce.androidsdk.ui.LoginActivity;
 import com.salesforce.androidsdk.ui.SalesforceDroidGapActivity;
 
 public class AMSalesforceDroidGapActivity extends SalesforceDroidGapActivity implements OnClickListener, OnItemClickListener {
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		getMapManager();		
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		if(mapView != null) {
 			mapView.onPause();
 		}
 	}
 
 	public SlidingMenuLayout rootLayout;
 	
 	private ListView slidingMenuListView;
 	private View menuLayout, mainLayout;
 	private Button showSlidingMenuButton;
 	private WebView webView;
 	private SlidingMenuAdapter menuAdapter;
 	private ArrayList<SlidingMenuItem> slidingMenuList;
 	private View mapLayout;
 	private ImageButton nativeMenuButton;
 	private ImageButton nativeSettingsButton;
 	private MapView mapView;
 	private boolean mapon = false;
 	private GoogleMap map;
 	private MapManager mapManager;
 	private PopupWindow popup;
 	private ViewGroup mapContainer;
 	private StopListAdapter stopListAdapter;
 	private View stopListView;
 	private boolean mapAvailable;
 
 	private View getStopListView() {
 		if(stopListView == null) {
 			stopListView = getStopListAdapter().getStopListView();
 		}
 		
 		return stopListView;
 	}
 	
 	private StopListAdapter getStopListAdapter() {
 		return new StopListAdapter(this, mapManager);
 	}
 	
 	private RadioButton stopListBtn;
 	private RadioButton mapListBtn;
 	
 	//private List<Route> routeList;
 	//The "x" and "y" position of the "Settings Button" on screen.
 	Point p;
 
 	private MapManager getMapManager() {
 		try {
 			if(mapManager == null) {
 				mapManager = new MapManager(this, map);
 			}
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			
 			// TODO handle map initialization error
 		}
 		
 		return mapManager;
 
 	}
 	
 	@Override
 	protected CordovaWebViewClient createWebViewClient() {
 		return new AAUMobileWebViewClient(this);
 	}
 
 	private void mapInit(Bundle savedInstanceState) throws AMException {
 		// Initialize varibles
 		mapLayout = getLayoutInflater().inflate(R.layout.activity_native_map_acivity, null);
 		mapView = (MapView) mapLayout.findViewById(R.id.map);
 		
 		mapView.onCreate(savedInstanceState);
 		
 		map = mapView.getMap();
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 	    try
 	    {
 	        // check if Google Maps is supported on given device
 	        Class.forName("com.google.android.gms.maps.GoogleMap");
 
 	        this.mapAvailable = true;
 	    }
 	    catch (Exception e)
 	    {
 	        e.printStackTrace();
 	        
 	        this.mapAvailable = false;
 	    }		
 		
 	    if(mapAvailable) {
 			try {
 				mapInit(savedInstanceState);
 			} catch (AMException e) {
 				// TODO Handle map initialization errors
 				e.printStackTrace();
 			}
 	
 			/* Create a new SlidingMenuLayout and set Layout parameters. */
 			rootLayout = new SlidingMenuLayout(this);
 			rootLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
 			
 			/* Inflate and add the main view layout and menu layout to root sliding menu layout. Menu layout should be added first.. */
 			menuLayout = getLayoutInflater().inflate(R.layout.sliding_menu_layout, null);
 			mainLayout = getLayoutInflater().inflate(R.layout.main_layout, null);
 			rootLayout.addView(menuLayout);
 			//rootLayout.addView(mainLayout);
 			
 			this.root.removeView(this.appView);
 					
 			rootLayout.addView((View)this.appView);
 			
 			/* Set activity content as sliding menu layout. */
 			this.root.addView(rootLayout);
 			
 			/* Initialize list view and buttons to handle showing of menu. */
 			slidingMenuListView = (ListView) menuLayout.findViewById(R.id.sliding_menu_list_view);
 			showSlidingMenuButton = (Button) mainLayout.findViewById(R.id.show_menu_button);
 			
 			/* Initialize the main web view for displaying of web content. */
 			//webView = (WebView) mainLayout.findViewById(R.id.content_web_view);
 			webView = this.appView;
 			
 			/* Initialize the menu adapter and set to list view to load menu from the XML file. */
 			menuAdapter = new SlidingMenuAdapter(getLayoutInflater(), this);
 			slidingMenuListView.setAdapter(menuAdapter);
 			
 			/* Handle button and list item clicks. */
 			showSlidingMenuButton.setOnClickListener(this);
 			slidingMenuListView.setOnItemClickListener(this);
 			
 			
 			//nativeMenuButton = (Button) mapLayout.findViewById(R.id.menu);
 			// AI Comment: change Buttons to ImageButton
 			nativeMenuButton = (ImageButton) mapLayout.findViewById(R.id.menu);
 			
 			nativeMenuButton.setOnClickListener(this);
 			
 			// Connect Settings popup panel
 			nativeSettingsButton = (ImageButton) mapLayout.findViewById(R.id.settings_popup);
 			nativeSettingsButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 	
 					//Open popup window
 					if (p != null)
 						showPopup(AMSalesforceDroidGapActivity.this, p);
 				}
 			});
 			
 			mapContainer = (ViewGroup) mapLayout.findViewById(R.id.mapContainer);
 			
 			stopListAdapter = new StopListAdapter(this, getMapManager());
 			
 			stopListBtn = (RadioButton) mapLayout.findViewById(R.id.toggle_stop_list_view);
 			mapListBtn = (RadioButton) mapLayout.findViewById(R.id.togle_map_view);
 			
 			stopListBtn.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					if(mapContainer.findViewById(R.id.map) != null) {
 						mapContainer.addView(getStopListView()); 
 						mapContainer.removeView(mapView);
 						
 						stopListBtn.setTextColor(Color.WHITE);
 						mapListBtn.setTextColor(Color.RED);
 					}
 				}
 			});
 			
 			mapListBtn.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					if(mapContainer.findViewById(R.id.map) == null) {
 						mapContainer.removeView(getStopListView()); 
 						mapContainer.addView(mapView);
 	
 						stopListBtn.setTextColor(Color.RED);
 						mapListBtn.setTextColor(Color.WHITE);
 					}
 				}
 			});
 			
 			super.setIntegerProperty("splashscreen", com.appirio.aau.R.drawable.aau_load);
 	
 			super.loadUrl("file:///android_asset/www/bootstrap.html",10000);
 	    } else {
 	    	showGoogleServiceMissingAlert(); 
 	    }
 		
 	}
 
 	private void showGoogleServiceMissingAlert() {
 		AlertDialog ad = new AlertDialog.Builder(this).create();  
 		ad.setCancelable(false); // This blocks the 'BACK' button  
 		ad.setMessage("Google Play services not present on device, please install Google Play");  
 		ad.setButton("OK", new DialogInterface.OnClickListener() {  
 		    @Override  
 		    public void onClick(DialogInterface dialog, int which) {  
 		        dialog.dismiss();                      
 		    }  
 		});  
 		ad.show();
 	}
 
 	// Get the x and y position after the button is draw on screen
 	// (Important: note that we can't get the position in the onCreate(),
 	// because at that stage most probably the view isn't drawn yet, so it will return (0, 0))
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		// Get the x, y location and store it in the location[] array
 		// location[0] = x, location[1] = y.
 		//Initialize the Point with x, and y positions
 		p = getSettingAnchorPoint();
 	}
 
 	private static final String FEEDBACK_PREFS = "feedback_prefs";
 	private static final String ASK_FEEDBACK_ON_PREF = "AskFeedbackOn";
 	private static final int ASK_FEEDBACK_AFTER_DAYS = 3;
 	private static final String FEEDBACK_VERSION = "feedback_version";
 	
 	private boolean isConnected() {
 	    boolean haveConnectedWifi = false;
 	    boolean haveConnectedMobile = false;
 
 	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
 	    for (NetworkInfo ni : netInfo) {
 	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
 	            if (ni.isConnected())
 	                haveConnectedWifi = true;
 	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
 	            if (ni.isConnected())
 	                haveConnectedMobile = true;
 	    }
 	    return haveConnectedWifi || haveConnectedMobile;
 	}
 	
 	@Override
 	public void onResume() {
 		if(mapAvailable) {
 		
 			this.appView.loadUrl("javascript:aauMobile.init.appActivation();");
 			
 			mapView.onResume();
 			
 			if(!isConnected()) {
 			  	AlertDialog.Builder adb = new AlertDialog.Builder(this);
 			  	
 			  	adb.setTitle("Error!");
 			  	adb.setMessage("This device is not currently connected to the internet, please restart the application when a connection is available");
 			  	
 			  	new AlertDialog.Builder(this)
 	  		  	  .setTitle("Error")
 	   		  	  .setMessage("This device is not currently connected to the internet, please restart the application when a connection is available")
 	   		  	  .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
 	   		  		  public void onClick(DialogInterface dialog,
 	   		  		    int which) {
 	   				  	endActivity();
 	   		  		  }
 			  	}).show();
 			} else {
 				String version = null;
 				
 				try {
 					PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
 					version = pInfo.versionName;
 				} catch (Exception ex) {
 					// This should never be thrown since the package name is coming from the current activity
 				}
 				
 				// If the user has the app installed for more than 3 days, ask for feedback
 				SharedPreferences settings = getSharedPreferences(
 						FEEDBACK_PREFS,
 						Context.MODE_PRIVATE);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putString(LoginActivity.SERVER_URL_CURRENT_SELECTION,
 				  getString(com.appirio.aau.R.string.sf_default_url));
 	
 				long askFeedbackOn = settings.getLong(ASK_FEEDBACK_ON_PREF, 0);
 				
 				String feedbackVersion =  settings.getString(FEEDBACK_VERSION, null);
 				
 				// Request feedback when app is updated
 				if((askFeedbackOn == -1) && ((feedbackVersion == null && Double.valueOf(version) < 2.0) || !version.equals(feedbackVersion))) {
 					feedbackVersion = version;
 					askFeedbackOn = 0;
 					editor.putString(FEEDBACK_VERSION, version);
 				}
 				
 				Calendar calendar = Calendar.getInstance();
 				calendar.add(Calendar.DAY_OF_MONTH, ASK_FEEDBACK_AFTER_DAYS);
 	
 				if(askFeedbackOn == 0) {
 					editor.putLong(ASK_FEEDBACK_ON_PREF, calendar.getTimeInMillis());
 				} else if(askFeedbackOn > -1) {
 					if(askFeedbackOn < System.currentTimeMillis()) {
 						Builder dialogBuilder = new Builder(this);
 						
 						dialogBuilder.setMessage("Would you like to provide feedback on this app?");
 						dialogBuilder.setTitle("Feedback");
 						
 						FeedbackDialogListener listener = new FeedbackDialogListener(this, editor);
 						
 						dialogBuilder.setNegativeButton("Don't ask again", listener);
 						dialogBuilder.setNeutralButton("Not now", listener);
 						dialogBuilder.setPositiveButton("Yes!", listener);
 						
 						dialogBuilder.show();
 					}
 				}
 				
 				editor.commit();
 			}
 			
 		} else {
 			showGoogleServiceMissingAlert();
 		}
 
 		super.onStart();
 	}
 	
 	class FeedbackDialogListener implements DialogInterface.OnClickListener {
 
 		private Context ctx;
 		private SharedPreferences.Editor editor;
 		
 		public FeedbackDialogListener(Context ctx, SharedPreferences.Editor editor) {
 			this.ctx = ctx;
 			this.editor = editor;
 		}
 		
 		public void onClick(DialogInterface dialog, int which) {
 			Calendar calendar = Calendar.getInstance();
 			calendar.add(Calendar.DAY_OF_MONTH, ASK_FEEDBACK_AFTER_DAYS);
 			
 			if(which == DialogInterface.BUTTON_NEGATIVE) {
 				editor.putLong(ASK_FEEDBACK_ON_PREF, -2);
 			} else if(which == DialogInterface.BUTTON_NEUTRAL) {
 				editor.putLong(ASK_FEEDBACK_ON_PREF, calendar.getTimeInMillis());
 			} else if (which == DialogInterface.BUTTON_POSITIVE) {
 				editor.putLong(ASK_FEEDBACK_ON_PREF, -1);
 				
 				 startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ctx.getPackageName())));
 			}
 			
 			editor.commit();
 		}
 		
 	}
 	
 	public void showMap() {
 		this.runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				if(!mapon) {
 					rootLayout.removeView(appView);
 					rootLayout.addView(mapLayout);
 					
 					getMapManager().showMap();
 					
 					mapon = true;
 				}
 			}
 		});
 	}
 	
 	public void showWebView() {
 		this.runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				if(mapon) {
 					rootLayout.addView(appView); 
 					rootLayout.removeView(mapLayout);
 					mapon = false;
 				}
 			}
 		});
 		
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
 	{
 		SlidingMenuItem menuItem = (SlidingMenuItem) menuAdapter.getItem(position);
 		if(menuItem.getActionType().equals("js")) {
 			this.showWebView();
 		}
 		webView.loadUrl("javascript:" + menuItem.getAction());
 
 		rootLayout.closeMenu();
 	}
 
 	@Override
 	public void onClick(View view) 
 	{
 		if (view == showSlidingMenuButton || view == nativeMenuButton)
 		{
 			if (rootLayout.isOpen())
 			{
 				rootLayout.closeMenu();
 			}
 			else 
 			{
 				rootLayout.openMenu();
 			}			
 		}		
 	}
 
 	public void loadMenuItems(final JSONArray jsonMenu) {
 		final Context ctx = this;
 		
 		this.runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				menuAdapter.loadMenuItems(jsonMenu, ctx);
 				menuAdapter.notifyDataSetChanged();
 				slidingMenuListView.invalidateViews();		
 			}
 		});
 	}
 
 	
 	
 	//////////////////////////////////////////////
 	// TEST POPUP CODE HERE activity
 
 	
 	// The method that displays the popup.
 	private void showPopup(final Activity context, Point p) {
 		
 		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 		int width = metrics.widthPixels;
 		int height = metrics.heightPixels;
 		
 		int popupWidth = width - 40; // 460;
 		int popupHeight = height - 100; //650;
 
 		// Inflate the popup_layout.xml
 		LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
 		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = layoutInflater.inflate(R.layout.popup_settings_layout, viewGroup);
 
 		// Creating the PopupWindow
 		popup = new PopupWindow(context);
 		popup.setContentView(layout);
 		popup.setWidth(popupWidth);
 		popup.setHeight(popupHeight);
 		popup.setFocusable(true);
 
 		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
 		//int OFFSET_X = nativeSettingsButton.getWidth(); //  20;
 		//int OFFSET_Y = nativeSettingsButton.getHeight(); // 60;
 
 		//Initialize the Point with x, and y positions
 		Point pbtn = getSettingAnchorPoint();
 		
 		// Clear the default translucent background
 		popup.setBackgroundDrawable(new BitmapDrawable());
 
 		// Log.d("NATIVE MAP Activity", "Setting POINT: "+ pbtn.x + OFFSET_X + " " + pbtn.y + OFFSET_Y);
 		// Displaying the popup at the specified location, + offsets.
 				
 		popup.showAtLocation(layout, Gravity.NO_GRAVITY, pbtn.x + nativeSettingsButton.getLeft(), pbtn.y + nativeSettingsButton.getHeight());
 
 		
 		// Dynamically add bttons here
 		//routeList = mapManager.getRoutesShown();
 		addRoutesTable(popup);
 
 		// Set Autoupdate flag from map Manager
 		
 		ToggleButton liveUpdBtn = (ToggleButton)popup.getContentView().findViewById(R.id.toggleLiveUpdatesButton);
 		if (liveUpdBtn != null){
 			liveUpdBtn.setChecked(mapManager.getAutoUpdate());
 		}
 		
 	}
 
 	// Return actual point for Setting anchor button to position pannel
 	private Point getSettingAnchorPoint(){
 		// Get the x, y location and store it in the location[] array
 		// location[0] = x, location[1] = y.
 		int[] location = new int[2];
 		nativeSettingsButton.getLocationOnScreen(location);
 
 		//Initialize the Point with x, and y positions
 		Point pbtn = new Point();
 		pbtn.x = location[0];
 		pbtn.y = location[1];
 		return pbtn;
 	}
 	private void addRoutesTable(PopupWindow popup){
 		
 		TableLayout tl = (TableLayout)popup.getContentView().findViewById(R.id.tableLayout1);
 		int cnt = 1;
 		TableRow tr = null;
 		CheckBox cb = null;
 		boolean isOdd = false;
 		//List<String> rtList = stubRoutsList();
 		List<Route> rtList = mapManager.getRoutes();
 		
 		    for (Route s : rtList) {
 		         cb = createCheckBox(cnt, s.getName());
 		         // Set checkbox value
 		         cb.setChecked(isRouteDisplay(s.getName()));
 		         
 		         if (( cnt & 1) == 0 ) { 
 		        	 // even... 
 			         tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
 			 		 // Get Line sep view
 			         addLineSeparator(tl);
 			         isOdd = false;	
 		         } else { 
 			         // Odd
 		        	 tr = new TableRow(this);
 			         tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
 			         tr.setPadding(5, 0, 5, 0);		
 			         isOdd = true;
 		         }
 		         
 		         
 		         tr.addView(cb);
 
 		         cnt++;
 		    }
 		    
 	         // Check if its last element
 	         if (isOdd){
 		         tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
 		         //tr.addView(cb);
 		         // Get Line sep view
 		         addLineSeparator(tl);
 	         }
 
 	}
 	
 	private boolean isRouteDisplay(String name) {
 		boolean rc = false;
 		if (mapManager != null){
 			List<Route> rtList = mapManager.getRoutesShown();
 			for (Route rt : rtList){
 				if (name.equals(rt.getName())){
 					rc = true;
 					// stop itterator adn return
 					return rc;
 				}
 			}
 		}
 		return rc;
 	}
 	
 	private CheckBox createCheckBox(int id, String s){
         CheckBox cb = new CheckBox(this);
         cb.setId(id);
         cb.setText(s);
       
         TableRow.LayoutParams lp2 = new TableRow.LayoutParams(
        		 TableRow.LayoutParams.WRAP_CONTENT,
        		 TableRow.LayoutParams.WRAP_CONTENT
        		 );
         
         cb.setLayoutParams(lp2);  
         cb.setTextColor(Color.rgb(255, 255, 255));
         cb.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
            	 testCheckHandler(v);	
            	 // TODO Auto-generated method stub
             }
         });     
 		
         return cb;
 	}
 	
 	private void addLineSeparator(TableLayout tl){
 		 // Get Line sep view
 		 View v = new View(this);
 		 v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
 		 v.setBackgroundColor(Color.rgb(255, 255, 255));
         
         tl.addView(v);		
 	}
 	
 	// Method to open WebView and display a static map image
 	public void onStaticMapBtnClicked(View v){
 	
 		rootLayout.removeView(appView);
 		webView.getSettings().setLoadWithOverviewMode(true);
 		webView.getSettings().setUseWideViewPort(true);
 	    webView.loadUrl("javascript:showStaticMap()");
 
 	}
 	
 	
 	// Method to handle Toggle button to set flag for Live Bus updates from Teletrack
 	public void onLiveUpdatesClicked(View v){
 	    if(v.getId() == R.id.toggleLiveUpdatesButton){
 	    	
 	    	boolean on = ((ToggleButton) v).isChecked();
 	        
 	        if (on) {
 	            // Enable Bus live updates
 	        	mapManager.startAutoUpdate();
 	        	//MessageBox("Enable Bus Live Updates");
 	        } else {
 	            // Disable Bus Live updates
 	        	mapManager.stopAutoUpdate();
 	        	//MessageBox("Disable Bus Live Updates");
 	        }	        
 	    	
 	    }
 	}
 	
 	
 	public void onRouteSelectorClicked(View view) {
 		testCheckHandler(view);
 	}	
 	
 	public void testCheckHandler(View view){
 	    // Is the view now checked?
 		String route_name = (String)((CheckBox) view).getText();
 	    boolean checked = ((CheckBox) view).isChecked();
 	    try{
 	    if (checked){
 	    	addDisplayRoute(route_name);
 	    	//MessageBox("Selected: "+view.getId()+" "+route_name);
 	    }
 	    if (!checked){
 	    	removeDisplayRoute(route_name);
 	    	//MessageBox("NOT Selected: "+view.getId()+" "+route_name);
 	    }
 	    }catch(Exception e){
 	    	e.printStackTrace();
 	    }
 	}
 	
 	public Route getRouteByName(String name){
 		List<Route> rtList = mapManager.getRoutes();
 		for (Route rt : rtList){
 			if (name.equals(rt.getName())){
 				return rt;
 			}
 		}
 		return null;
 	}
 	
 	public Set<String> getRouteSet(List<Route> rtList){
 		HashSet<String> hs = new HashSet<String>();
 		for (Route rt : rtList){
 			 hs.add(rt.getName());
 		}
 		return hs;
 	}
 	
 	public void addDisplayRoute(String name) throws Exception {
 		List<Route> rtList = mapManager.getRoutesShown();
 		if (rtList != null){
 			rtList.add(getRouteByName(name));
 		}
 		mapManager.showRoutes(getRouteSet(rtList));
 	}
 
 	public void removeDisplayRoute(String name) throws Exception {
 		List<Route> rtList = mapManager.getRoutesShown();
 		rtList.remove(getRouteByName(name));
 		mapManager.showRoutes(getRouteSet(rtList));
 	}    
     
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
 
         // Checks the orientation of the screen
         if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
         	if (this.popup != null && this.popup.isShowing()){
         		this.popup.dismiss();
         	}
         } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
         	if (this.popup != null && this.popup.isShowing()){
         		this.popup.dismiss();
         	}
         }
       }
 
     // Event handler to switch views on toggle Map/Stop List
     public void onToggleViewClicked(View view) {
         // Is the button now checked?
         boolean checked = ((RadioButton) view).isChecked();
         
         // Check which radio button was clicked
         switch(view.getId()) {
             case R.id.togle_map_view:
                 if (checked)
                     // Map view display
                 	// TODO replace this code with MAP VIEW display
                 	MessageBox("Show Map view");
                 break;
             case R.id.toggle_stop_list_view:
                 if (checked)
                     // Stop List View display
                 	// TODO replace this message code with STOP LIST display view
                 	MessageBox("Show Stop List");
                 break;
         }
     }
     
     public void MessageBox(String message)
     {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
     } 
     
 }
