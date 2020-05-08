 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import whyq.WhyqApplication;
 import whyq.WhyqMain;
 import whyq.adapter.ExpandableStoreAdapter;
 import whyq.adapter.WhyqAdapter;
 import whyq.adapter.WhyqAdapter.ViewHolder;
 import whyq.controller.WhyqListController;
 import whyq.interfaces.IServiceListener;
 import whyq.map.MapsActivity;
 import whyq.model.GroupMenu;
 import whyq.model.GroupStore;
 import whyq.model.Menu;
 import whyq.model.ProductTypeInfo;
 import whyq.model.ResponseData;
 import whyq.model.Store;
 import whyq.model.User;
 import whyq.service.Service;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.API;
 import whyq.utils.RSA;
 import whyq.utils.UrlImageViewHelper;
 import whyq.utils.Util;
 import whyq.utils.WhyqUtils;
 import whyq.utils.XMLParser;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.whyq.R;
 
 public class ListActivity extends FragmentActivity implements  OnClickListener,OnFocusChangeListener, IServiceListener, OnScrollListener{
 
 	
 	public static final String DOWNLOAD_COMPLETED = "DOWNLOAD_COMPLETED";
 	public static final String COFFE = "";
 	private static final int CHANGE_LOCATION_REQUEST = 0;
 	protected static final String CHANGE_LOCATION = "CHANGE_LOCATION";
 	public String url = "";
 	public Boolean header = true;
 
 	/**
 	 * MSA
 	 */
 	private ArrayList<Store> permListMain = new ArrayList<Store>();
 
 	public static int screenWidth;
 	public static int screenHeight;
 	public static boolean isLogin = false;
 	public static int loginType = 0;
 	public static boolean isRefesh = true;
 	public static boolean isCalendar = false;
 	int nextItem = -1;
 	
 	public static String storeId;
 //	FragmentManager t = ggetSupportFragmentManager();
 //	private ProgressDialog dialog;
 	
 	ListView whyqListView;
 //	Button btnRefesh;
 	ProgressBar progressBar;
 //	ImageView imageViewBeforRefesh;
 //	RelativeLayout headerLayout;
 	WhyqAdapter permListAdapter;
 	View headerView = null;
 	private boolean isFirst = true;
 	public static LoadPermList loadPermList;
 	public boolean isAddHeader = false;
 	private ImageView bntFilter;
 	private LinearLayout lnFilter;
 	private EditText etTextSearch;
 	private TextView cktViewAll;
 	private TextView cktFriednVised;
 	private TextView cktFriendFavourtie;
 	/** Called when the activity is first created. */
 	/*
 	 * Whyq elements
 	 */
 	
 	LinearLayout lnCutlery;
 	LinearLayout lnWine;
 	private LinearLayout lnCoffe;
 	
 	
 	public static String searchKey="";
 	public static String longitude="";
 	public static String latgitude="";
 	public static String currentLocation;
 	private String filterType="1"; 
 	private String friendFavourite;
 	private String friendVisited;
 	private String cateId="1";
 	
 	public static boolean isSearch = false;
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			if (intent.getAction().equals(DOWNLOAD_COMPLETED)) {
 				exeListActivity(false);
 			} else if(intent.getAction().equals(CHANGE_LOCATION)){
 				updateLocation(intent);
 			}
 		}
 	};
 	
 	
 	private OnItemClickListener onStoreItemListener = new OnItemClickListener() {
 		
 
 		@Override
 		public void onItemClick(AdapterView<?> adapter, View view,
 				int position, long id) {
 			try {
 				ViewHolder store = ((ViewHolder) view.getTag());
 				if(store !=null){
 					storeId = store.id;
 					if(storeId !=null)
 						gotoStoreDetail(storeId, store.tvItemName.getText().toString());
 				}
 				
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 	};
 	private ImageView imgCheckedAll;
 	private ImageView imgCheckedFavorite;
 	private ImageView imgCheckedVisited;
 	private ImageView imgArrowDown;
 	private ImageView imgCoffe;
 	private ImageView imgWine;
 	private ImageView imgHotel;
 	private ImageView imgCutlery;
 	private LinearLayout lnHotel;
 	private Button btnCacel;
 	private RelativeLayout rlSearchTools;
 	private LayoutParams params;
 	private RelativeLayout rlFilterGroup;
 	private Context context;
 	private TextView tvNearLocation;
 	private RelativeLayout rlLocationField;
 	private int storeType;
 	private Service service;
 	private LinearLayout lnNavigation;
 	private LinearLayout lnPageContent;
 	private ExpandableListView expandbleStoreView;
 	protected boolean isExpandableSearch = false;
 	private RelativeLayout rlExpandableStoreContent;
 	private TextView tvNodata;
 	private TextView tvNumberResult;
 	private TextView tvTextSearch;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		createUI();
 		isExpandableSearch = false;
 		showSearchExpandableList(false);
 		FavouriteActivity.isFavorite = false;
 		service = new Service(ListActivity.this);
 		resetTabBarFocus(1);
 //		checkLocationAccess();
 		getLocation();
 		regisReceiver();
 		WhyqUtils.clearViewHistory();
 		WhyqUtils utils= new WhyqUtils();
 		utils.writeLogFile(ListActivity.this.getIntent());
 		whyqListView.setOnScrollListener(this);
     	showProgress();
 
 	}
 
 
 	private void checkLocationAccess() {
 		// TODO Auto-generated method stub
 		Util.checkLocationSetting(getParent());
 	}
 
 
 	private void getLocation() {
 		// TODO Auto-generated method stub
 		Util.checkLocationSetting(getParent());
 		Bundle bundle = Util.getLocation(getParent());
 		if(bundle!=null){
 			longitude = bundle.getString("lon");
 			latgitude = bundle.getString("lat");
 		}
 	}
 
 
 	private void regisReceiver() {
 		// TODO Auto-generated method stub
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction(DOWNLOAD_COMPLETED);
 		intentFilter.addAction(CHANGE_LOCATION);
 		registerReceiver(receiver, intentFilter);
 	}
 	
 	protected void updateLocation(Intent intent) {
 		// TODO Auto-generated method stub
 		
 	}
 	protected void gotoStoreDetail(String storeId, String storeName) {
 		// TODO Auto-generated method stub
 		Intent intent = new Intent(ListActivity.this, ListDetailActivity.class);
 		intent.putExtra("store_type", storeType);
 		intent.putExtra("store_name", storeName);
 		startActivity(intent);
 	}
 
 	public void createUI() {
 		setContentView(R.layout.list_screen);//
 		
 		lnPageContent = (LinearLayout)findViewById(R.id.page_content);
 		lnNavigation = (LinearLayout)findViewById(R.id.lnNavigation);
 		whyqListView = (ListView) findViewById(R.id.lvWhyqList);
 		lnCutlery = (LinearLayout) findViewById(R.id.lnCutleryTab);
 		lnWine = (LinearLayout) findViewById(R.id.lnWineTab);
 		lnCoffe = (LinearLayout) findViewById(R.id.lnCoffeTab);
 		lnHotel = (LinearLayout) findViewById(R.id.lnHotel);
 		bntFilter = (ImageView)findViewById(R.id.btnFilters);
 		lnFilter = (LinearLayout)findViewById(R.id.lnFilterView);
 		imgArrowDown = (ImageView)findViewById(R.id.imgArrowDown);
 		loadPermList = new LoadPermList(false);
 		tvNearLocation = (TextView)findViewById(R.id.tvNearLocation);
 		progressBar = (ProgressBar)findViewById(R.id.prgBar);
 		etTextSearch =(EditText) findViewById(R.id.etTextSearch);
 		rlLocationField = (RelativeLayout)findViewById(R.id.rlLocationField);
 		isAddHeader = true;
 		cktViewAll = (TextView) findViewById(R.id.cktViewAll);
 		cktFriednVised = (TextView)findViewById(R.id.cktViewVisited);
 		cktFriendFavourtie = (TextView) findViewById(R.id.cktViewFavourite);
 		imgCheckedAll = (ImageView) findViewById(R.id.imgCbAll);
 		imgCheckedFavorite = (ImageView)findViewById(R.id.imgCbFavourite);
 		imgCheckedVisited = (ImageView) findViewById(R.id.imgCbVisited);
 
 		btnCacel = (Button)findViewById(R.id.btnCancel);
 		rlSearchTools = (RelativeLayout)findViewById(R.id.rlSearchtool);
 		rlFilterGroup = (RelativeLayout)findViewById(R.id.rlFilter);
 		
 		tvNodata = (TextView)findViewById(R.id.tvNodata);
 		rlExpandableStoreContent = (RelativeLayout)findViewById(R.id.expandable_view_content);
 		imgCutlery = (ImageView)findViewById(R.id.imgCutleryIcon);
 		imgWine = (ImageView)findViewById(R.id.imgWinIcon);
 		imgCoffe = (ImageView)findViewById(R.id.imgCoffeeIcon);
 		imgHotel = (ImageView)findViewById(R.id.imgHotelIcon);
 		
 		expandbleStoreView = (ExpandableListView) findViewById(R.id.expStoreList);
 		tvNumberResult = (TextView)findViewById(R.id.tvNumberResult);
 		tvTextSearch = (TextView)findViewById(R.id.tvTextSearch);
 		
 		context = ListActivity.this;
 		whyqListView.setOnItemClickListener(onStoreItemListener);
 		etTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(etTextSearch
 							.getApplicationWindowToken(), 0);
 					try {
 						String text = etTextSearch.getText().toString();
 						if(text.equals(""))
 						{
 							isSearch = false;
 						}else{
 							isSearch = true;
 						}
 						isExpandableSearch  = true;
 						exeSearch(text,true);
 					} catch (Exception e) {
 						// TODO: handle exception
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 		etTextSearch.addTextChangedListener(mTextEditorWatcher);
 		etTextSearch.setOnFocusChangeListener(this);
 		imgCoffe.setFocusable(true);
 //		etTextSearch.clearFocus();
 		imgCoffe.requestFocus();
 		params = (RelativeLayout.LayoutParams)rlSearchTools.getLayoutParams();
 	}
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("onActivityResult changelocation","");
 	    if (resultCode == RESULT_OK) {
 	    	 if (requestCode == CHANGE_LOCATION_REQUEST ) {
 	    		 latgitude = data.getStringExtra("lat");
 	    		 longitude = data.getStringExtra("lng");
 	    		 exeSearch(etTextSearch.getText().toString(), false);
 	    		 tvNearLocation.setText(data.getStringExtra("name"));
 	    	 }
 	    
 	    }
 	}
 	protected void exeSearch(String string, boolean isExpandable) {
 		// TODO Auto-generated method stub
 		isExpandableSearch = isExpandable;
 		searchKey = string;
 		isSearch = true;
 		exeListActivity(true);
 	}
 
 	@Override
 	protected void onDestroy(){
 		super.onDestroy();
 		isCalendar =false;
 		isRefesh = true;
 		isExpandableSearch = false;
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
 //		if(currentLocation !=null)
 //			tvNearLocation.setText(currentLocation);
 //		imgCoffe.requestFocus();
 //		if(isLogin && WhyqMain.getCurrentTab() == 3){
 //			User user2 = WhyqUtils.isAuthenticated(getApplicationContext());
 //			if(user2 != null){
 //				String id = user2.getId();
 //				if(id != null)
 //					WhyqMain.gotoDiaryTab(id);
 //			}
 //			isLogin = false;
 //		}else if(WhyqMain.getCurrentTab() == 0 && isRefesh){
 //			// Get the screen's size.
 //			exeListActivity(false);
 //		}else if(WhyqMain.getCurrentTab() == 1 || WhyqMain.getCurrentTab() == 4){
 //			if(isRefesh)
 //				exeListActivity(false);
 //		}else if(WhyqMain.getCurrentTab() == 3) { 
 //			isCalendar = true;
 //			exeListActivity(false);
 //		}else if(!isRefesh){
 //			isRefesh = true;
 //		}
 		
 	}
 	
 	protected void onPause () {
     	super.onPause();
     	isFirst = true;
     	nextItem = -1;
     	isExpandableSearch = false;
 //    	showProgress();
     	
     }
 
 
 
 	public void exeListActivity(boolean isSearch) {
 		// TODO Auto-generated method stub
 		showProgress();
 		if(isFirst){
 //	    	clearData();
 	    	if(permListMain !=null)
 	    		permListMain.clear();
 	    	isFirst = false;
 		}
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		
 		
 		//Set to application
 		WhyqApplication state = (WhyqApplication) this.getApplication();
 		if (state != null) {
 			state.setDisplayMetrics(metrics);
 		}
 		
 		
 		screenHeight = metrics.heightPixels;
 		screenWidth = metrics.widthPixels;
 
 		User user = WhyqUtils.isAuthenticated(getApplicationContext());
 		Bundle extras = getIntent().getExtras();
 		if(extras != null && extras.containsKey("allcategory")){
 			this.url = API.getNewPerm;
 			this.header = false;
 		}else if( extras != null && extras.containsKey("permByDate")){
 			this.url = (String)extras.getString("permByDate");
 			this.header = false;
 		}else if (extras != null) {
 			this.url = (String) extras.get("categoryURL");
 			this.header = false;
 		} else if (user != null) {
 //			this.url = API.followingPerm + String.valueOf(user.getId());
 
 		}
 		if(isSearch){
 			this.url = API.searchBusinessListURL;
 			this.header = false;
 		}else{
 			this.url = API.popularBusinessListURL;
 			this.header = false;
 		}
 		clearData();
 		showProgress();
 		exeGetBusinessList();
 	}
 	
 	private void exeGetBusinessList() {
 		// TODO Auto-generated method stub
 		getLocation();
 		loadPermList = new LoadPermList(isSearch);
 		loadPermList.execute();
 	}
 
 
 	private void timeoutDialog() {
 		// TODO Auto-generated method stub
 
 		hideProgress();
 	}
 	public void loadPreviousItems() {
 		if(nextItem > -1) {
 			nextItem = nextItem - 1;
 			clearData();
 			showProgress();
 			
 			exeGetBusinessList();
 		}
 		
 	}
 
 	public void loadNextItems() {
 		if(permListAdapter != null) {
 			nextItem = permListAdapter.getNextItems();
 			showProgress();
 			exeGetBusinessList();
 		}		
 	}
 	
 	public void cancelLoadPermList() {
 		if(loadPermList != null) {
 			loadPermList.cancel(true);
 		}
 	}
 	
 	private void loadPerms() {
 		User user = WhyqUtils.isAuthenticated(getApplicationContext());		
 		if(permListMain != null && !permListMain.isEmpty()){
 //			clearData();
 			//createUI();
 			if(this.permListAdapter == null) {
 				this.permListAdapter = new WhyqAdapter(ListActivity.this,
 					getSupportFragmentManager(),R.layout.whyq_item_1, permListMain, this, screenWidth, screenHeight, header, user);
 			} else {
 				for(int i = 0; i < permListMain.size(); i++) {
 					Store store = permListMain.get(i);
 					if(!permListAdapter.isPermDuplicate(store)) {
 						permListAdapter.add(store);
 					}
 				}
 			}
 
 			
 			whyqListView.setAdapter(permListAdapter);
 			permListAdapter.notifyDataSetChanged();
 
 
 		}else{
 			
 			
 		}
 
 	}
 	
 	public void clearData() {
 		if(permListAdapter != null && !permListAdapter.isEmpty()) {
 			permListAdapter.clear();
 			UrlImageViewHelper.clearAllImageView();				
 		}
 		if(whyqListView != null && headerView != null) {
 			whyqListView.removeHeaderView(headerView);
 		}
 	}
 	
 	public void removeAllData() {
 		if(permListAdapter != null && !permListAdapter.isEmpty()) {
 			permListAdapter.clear();						
 		}
 		clearData();
 	}
 
 	
 	// AsyncTask task for upload file
 
 	public class LoadPermList extends AsyncTask<ArrayList<Store>, Void, ArrayList<Store>> {
 
 		public boolean isSearch;
 
 		public LoadPermList(boolean isSearch){
 			this.isSearch = isSearch;
 			
 		}
 		
 		@Override
 		protected ArrayList<Store> doInBackground(ArrayList<Store>... params) {
 			// TODO Auto-generated method stub
 			WhyqListController whyqListController = new WhyqListController();
 			Map<String, String> postParams = new HashMap<String, String>();
 			ArrayList<Store> permList = null;
 			try {			
 				Log.d("LoadPermList","lat "+latgitude);
 				if (nextItem != -1) {
 					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 					nameValuePairs.add(new BasicNameValuePair("nextItem", String.valueOf(nextItem)));
 
 					if(isCalendar){
 						nameValuePairs.add(new BasicNameValuePair("uid", WhyqMain.UID));
 					}else{
 						
 						RSA rsa = new RSA();
 						String enToken = rsa.RSAEncrypt(XMLParser.getToken(WhyqApplication.Instance().getApplicationContext()));
 //						nameValuePairs.add(new BasicNameValuePair("token",enToken));
 						postParams.put("token", enToken);
 						postParams.put("longitude", longitude);
 						postParams.put("latitude", latgitude);
 						if(filterType.equals("1")){
 							
 						}else if(filterType.equals("2")){
 //							nameValuePairs.add(new BasicNameValuePair("friend_visit",filterType));
 							postParams.put("friend_visit", filterType);
 						}else if(filterType.equals("3")){
 //							nameValuePairs.add(new BasicNameValuePair("friend_favourite",filterType));
 							postParams.put("friend_favourite", filterType);
 						}
 						
 						if(isSearch){
 //							nameValuePairs.add(new BasicNameValuePair("key", searchKey));
 //							nameValuePairs.add(new BasicNameValuePair("search_longitude", longitude));
 //							nameValuePairs.add(new BasicNameValuePair("search_latitude", latgitude));
 //							nameValuePairs.add(new BasicNameValuePair("cate_id", cateId));
 							postParams.put("key", searchKey);
 							postParams.put("cate_id", cateId);
 							postParams.put("mode", "suggest");
 //							postParams.put("search_longitude", longitude);
 //							postParams.put("search_latitude", latgitude);
 						}else{
 //							nameValuePairs.add(new BasicNameValuePair("cate_id", cateId));
 							postParams.put("cate_id", cateId);
 						}
 					}
 
 //					permList = whyqListController.getBusinessList(url, nameValuePairs);
 					service.getBusinessList(postParams, url);
 				} else {
 					if(isCalendar){
 						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 						nameValuePairs.add(new BasicNameValuePair("uid", WhyqMain.UID));
 						permList = whyqListController.getBusinessList(url,nameValuePairs);
 						
 					}else{
 						RSA rsa = new RSA();
 						String enToken = rsa.RSAEncrypt(XMLParser.getToken(WhyqApplication.Instance().getApplicationContext()));
 //						nameValuePairs.add(new BasicNameValuePair("token",enToken));
 						postParams.put("token", enToken);
 						
 						postParams.put("longitude", longitude);
 						postParams.put("latitude", latgitude);
 						if(filterType.equals("1")){
 							
 						}else if(filterType.equals("2")){
 //							nameValuePairs.add(new BasicNameValuePair("friend_visit",filterType));
 							postParams.put("friend_visit", filterType);
 						}else if(filterType.equals("3")){
 //							nameValuePairs.add(new BasicNameValuePair("friend_favourite",filterType));
 							postParams.put("friend_favourite", filterType);
 						}
 
 						if(isSearch){
 //							nameValuePairs.add(new BasicNameValuePair("key", searchKey));
 //							nameValuePairs.add(new BasicNameValuePair("search_longitude", longitude));
 //							nameValuePairs.add(new BasicNameValuePair("search_latitude", latgitude));
 //							nameValuePairs.add(new BasicNameValuePair("cate_id", cateId));
 							postParams.put("key", searchKey);
 							postParams.put("mode", "suggest");
 							postParams.put("cate_id", cateId);
 //							postParams.put("search_longitude", longitude);
 //							postParams.put("search_latitude", latgitude);
 							
 						}else{
 //							nameValuePairs.add(new BasicNameValuePair("cate_id", cateId));
 							postParams.put("cate_id", cateId);
 						}
 					}
 
 //					permList = whyqListController.getBusinessList(url, nameValuePairs);
 					service.getBusinessList(postParams, url);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			permListMain = permList;
 			/**
 			 * MSA
 			 */
 
 						
 			return permListMain;
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... unsued) {
 
 		}
 
 		@Override
 		protected void onPostExecute(ArrayList< Store> sResponse) {
 			/**
 			 * MSA
 			 */
 //			loadPerms();
 			WhyqListController.isLoading = false;
 
 //			hideProgress();
 //			if(permListAdapter != null) {
 //				permListAdapter.notifyDataSetChanged();
 //			}
 		}
 
 	}
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 	    if ((keyCode == KeyEvent.KEYCODE_BACK))
 	    {
 //	        WhyqMain.back();
	    	finish();
 	        return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 //	@Override
 //	public void on_success() {
 //		// TODO Auto-generated method stub
 //		WhyqMain.refeshFollowerActivity();
 //	}
 //
 //	@Override
 //	public void on_error() {
 //		// TODO Auto-generated method stub
 //		//Logger.appendLog("test log", "loginerror");
 //		Intent intent = new Intent(getApplicationContext(), JoinWhyqActivity.class);
 //		this.startActivity(intent);		
 //	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		int id = v.getId();
 		switch (id) {
 		case R.id.btnRefesh:
 			isFirst = true;
 	    	nextItem = -1;
 			exeListActivity(false);
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	public void resetTabBarFocus(int index){
 		
 		switch (index) {
 		case 1:
 			setIconTab(1);
 			lnWine.setBackgroundResource(R.drawable.bg_tab_middle_normal);
 			lnCoffe.setBackgroundResource(R.drawable.bg_tab_middle_normal);
 			lnCutlery.setBackgroundResource(R.drawable.bg_tab_left_active);
 			lnHotel.setBackgroundResource(R.drawable.bg_tab_right_normal);
 			
 //			lnCoffe.setBackgroundResource(R.drawable.icon_cat_coffee);
 //			lnCutlery.setBackgroundResource(R.drawable.bg_tab_active);
 			cateId = "1";
 			storeType = 1;
 //			exeSearch(etTextSearch.getText().toString());
 			exeGetBusiness(etTextSearch.getText().toString());
 			break;
 		case 2:
 			setIconTab(2);
 			lnWine.setBackgroundResource(R.drawable.bg_tab_middle_active);
 			lnCutlery.setBackgroundResource(R.drawable.bg_tab_left_normal);
 			lnCoffe.setBackgroundResource(R.drawable.bg_tab_middle_normal);
 			lnHotel.setBackgroundResource(R.drawable.bg_tab_right_normal);
 			storeType = 2;
 //			lnCoffe.setBackgroundResource(R.drawable.icon_cat_coffee);
 //			lnCutlery.setBackgroundResource(R.drawable.icon_cat_cutlery);
 		
 			cateId = "2";
 //			exeSearch(etTextSearch.getText().toString());
 			exeGetBusiness(etTextSearch.getText().toString());
 			break;
 		case 3:
 			setIconTab(3);
 			lnCoffe.setBackgroundResource(R.drawable.bg_tab_middle_active);
 			lnWine.setBackgroundResource(R.drawable.bg_tab_middle_normal);
 			lnCutlery.setBackgroundResource(R.drawable.bg_tab_left_normal);
 			lnHotel.setBackgroundResource(R.drawable.bg_tab_right_normal);
 			
 			storeType = 3;
 //			lnWine.setBackgroundResource(R.drawable.icon_cat_wine);
 //			lnCutlery.setBackgroundResource(R.drawable.icon_cat_cutlery);
 			cateId = "3";
 //			exeSearch(etTextSearch.getText().toString());
 			exeGetBusiness(etTextSearch.getText().toString());
 			break;
 		case 4:
 			setIconTab(4);
 			lnHotel.setBackgroundResource(R.drawable.bg_tab_right_active);
 			lnCoffe.setBackgroundResource(R.drawable.bg_tab_middle_normal);
 			lnWine.setBackgroundResource(R.drawable.bg_tab_middle_normal);
 			lnCutlery.setBackgroundResource(R.drawable.bg_tab_left_normal);
 			
 			storeType = 4;
 //			lnWine.setBackgroundResource(R.drawable.icon_cat_wine);
 //			lnCutlery.setBackgroundResource(R.drawable.icon_cat_cutlery);
 			cateId = "4";
 //			exeSearch(etTextSearch.getText().toString());
 			exeGetBusiness(etTextSearch.getText().toString());
 			break;
 
 		default:
 			break;
 		}
 		
 	}
 	private void exeGetBusiness(String string) {
 		// TODO Auto-generated method stub
 		searchKey = string;
 		exeListActivity(isSearch);
 	}
 
 
 	private void setIconTab(int id) {
 		// TODO Auto-generated method stub
 		switch (id) {
 		case 1:
 			imgCutlery.setBackgroundResource(R.drawable.icon_tab_cutlery_active);
 			imgWine.setBackgroundResource(R.drawable.icon_tab_wine_normal);
 			imgCoffe.setBackgroundResource(R.drawable.icon_tab_coffee_normal);
 			imgHotel.setBackgroundResource(R.drawable.icon_tab_hotel_normal);
 			break;
 		case 2:
 			imgCutlery.setBackgroundResource(R.drawable.icon_tab_cutlery_normal);
 			imgWine.setBackgroundResource(R.drawable.icon_tab_wine_active);
 			imgCoffe.setBackgroundResource(R.drawable.icon_tab_coffee_normal);
 			imgHotel.setBackgroundResource(R.drawable.icon_tab_hotel_normal);
 			break;
 		case 3:
 			imgCutlery.setBackgroundResource(R.drawable.icon_tab_cutlery_normal);
 			imgWine.setBackgroundResource(R.drawable.icon_tab_wine_normal);
 			imgCoffe.setBackgroundResource(R.drawable.icon_tab_coffee_active);
 			imgHotel.setBackgroundResource(R.drawable.icon_tab_hotel_normal);
 			break;
 		case 4:
 			imgCutlery.setBackgroundResource(R.drawable.icon_tab_cutlery_normal);
 			imgWine.setBackgroundResource(R.drawable.icon_tab_wine_normal);
 			imgCoffe.setBackgroundResource(R.drawable.icon_tab_coffee_normal);
 			imgHotel.setBackgroundResource(R.drawable.icon_tab_hotel_active);
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	/*
  * Clicked Listener
  * 
  */
 	public void onCutleryTabCliked(View v){
 		resetTabBarFocus(1);
 	}
 	
 	public void onWineTabCliked(View v){
 		resetTabBarFocus(2);
 	}
 	
 	public void onCoffeTabClicked(View v){
 		resetTabBarFocus(3);
 	}
 	public void onHotelTabClicked(View v){
 		resetTabBarFocus(4);
 	}
 	
 	public void changeLocationClicked(View v){
 		Intent intent = new Intent(ListActivity.this, ChangeLocationActivity.class);
 		startActivityForResult(intent, CHANGE_LOCATION_REQUEST);
 	}
 	private final TextWatcher mTextEditorWatcher = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 //			exeSearchFocus();
 		}
 
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 			// This sets a textview to the current length
 
 
 		}
 
 		public void afterTextChanged(Editable s) {
 			
 			try {
 				String text = s.toString();
 				Log.d("Text serch","Text "+text);
 				if(text.equals(""))
 				{
 					exeDisableSearchFocus();
 					isSearch = false;
 					exeDisableSearchFocus();
 					exeListActivity(false);
 					
 				}else{
 					exeSearchFocus();
 					isSearch = true;
 					exeSearchFocus();
 					exeSearch(text,false);
 				}
 			
 			} catch (Exception e) {
 				// TODO: handle exception
 			}
 		}
 	};
 	private String currentStoreId;
 	private int mPosition;
 	private int mOffset;
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 	    if(hasFocus){
 //	    	exeSearchFocus();
 	    }else {
 //	        Toast.makeText(getApplicationContext(), "lost the focus", Toast.LENGTH_LONG).show();
 //	    	exeDisableSearchFocus();
 	    }
 	   
 	};
 	private void hideProgress() {
 		// TODO Auto-generated method stub
     	if(progressBar.getVisibility() == View.VISIBLE){
     		progressBar.setVisibility(View.GONE);
     	}
 //		if (dialog != null && dialog.isShowing()) {
 //			dialog.dismiss();
 //		}
 	}
 	protected void exeSearchFocus() {
 		// TODO Auto-generated method stub
 		if(btnCacel.getVisibility()!=View.VISIBLE){
 			
 //			params.width =WhyqApplication.Instance().getDisplayMetrics().densityDpi*10;// LayoutParams.WRAP_CONTENT;
 //			params.height = LayoutParams.WRAP_CONTENT;
 //			params.addRule(RelativeLayout.CENTER_VERTICAL,1);
 //			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
 //			rlSearchTools.setLayoutParams(params);
 
 			btnCacel.setVisibility(View.VISIBLE);
 			
 			imgCoffe.requestFocus();
 			hideFilterGroup();
 			rlLocationField.setVisibility(View.VISIBLE);
 		}
 	}
 
 
 	public void onCancelClicked(View v){
 		exeDisableSearchFocus();
 		etTextSearch.setText("");
 		currentLocation=null;
 		
 	}
 
 	private void exeDisableSearchFocus() {
 		// TODO Auto-generated method stub
 		btnCacel.setVisibility(View.GONE);
 //		params.width = 60;
 		rlLocationField.setVisibility(View.GONE);
 		rlSearchTools.setLayoutParams(params);
 		showFilterGroup();
 	}
 
 	private void hideFilterGroup() {
 		// TODO Auto-generated method stub
 		rlSearchTools.setVisibility(View.GONE);
 	}
 
 	private void showFilterGroup() {
 		// TODO Auto-generated method stub
 		rlSearchTools.setVisibility(View.VISIBLE);
 	}
 	private void showProgress() {
 		// TODO Auto-generated method stub
     	if(progressBar.getVisibility() != View.VISIBLE){
     		progressBar.setVisibility(View.VISIBLE);
     	}
 //		if (dialog != null && !dialog.isShowing()) {
 //			dialog.show();
 //		}
 	}
 	public void onFilterClicked(View v){
 		if(lnFilter.getVisibility()==View.VISIBLE){
 			hideFilterView();
 		}else{
 			showFilterView();
 		}
 		
 	}
 	public void toggle(View v)
 	{
 		int id =v.getId();
 		switch (id) {
 		case R.id.rlViewAll:
 			initCheckAll();
 			break;
 		case R.id.rlFavourite:
 			initCheckFavourite();
 			break;
 		case R.id.rlVisited:
 			initCheckVisited();
 			break;
 			
 		default:
 			break;
 		}
 		
 		hideFilterView();
 		exeGetBusiness(etTextSearch.getText().toString());
 	}
 	private void initCheckAll() {
 		// TODO Auto-generated method stub
 //		cateId = "1";
 		filterType = "1";
 		cktViewAll.setTextColor(Color.parseColor("#805504"));	
 		cktFriendFavourtie.setTextColor(getResources().getColor(R.color.white));
 		cktFriednVised.setTextColor(getResources().getColor(R.color.white));
 		imgCheckedFavorite.setVisibility(View.INVISIBLE);
 		imgCheckedVisited.setVisibility(View.INVISIBLE);
 		imgCheckedAll.setVisibility(View.VISIBLE);
 	}
 
 	private void initCheckFavourite() {
 		// TODO Auto-generated method stub
 //		cateId = "1";
 		filterType = "2";
 		cktFriendFavourtie.setTextColor(Color.parseColor("#805504"));
 		cktViewAll.setTextColor(getResources().getColor(R.color.white));
 		cktFriednVised.setTextColor(getResources().getColor(R.color.white));
 		
 		imgCheckedAll.setVisibility(View.INVISIBLE);
 		imgCheckedVisited.setVisibility(View.INVISIBLE);
 		imgCheckedFavorite.setVisibility(View.VISIBLE);
 	}
 
 	private void initCheckVisited() {
 		// TODO Auto-generated method stub
 //		cateId = "1";
 		filterType = "3";
 		cktFriednVised.setTextColor(Color.parseColor("#805504"));
 		cktViewAll.setTextColor(getResources().getColor(R.color.white));
 		cktFriendFavourtie.setTextColor(getResources().getColor(R.color.white));
 		
 		imgCheckedAll.setVisibility(View.INVISIBLE);
 		imgCheckedFavorite.setVisibility(View.INVISIBLE);
 		imgCheckedVisited.setVisibility(View.VISIBLE);
 	}
 
 	private void showFilterView() {
 		// TODO Auto-generated method stub
 		lnFilter.setVisibility(View.VISIBLE);
 		imgArrowDown.setVisibility(View.VISIBLE);
 	}
 
 	private void hideFilterView() {
 		// TODO Auto-generated method stub
 		lnFilter.setVisibility(View.GONE);
 		imgArrowDown.setVisibility(View.GONE);
 	}
 	public void onDistanceClicked(View v){
 		Store item = (Store)v.getTag();
 		Log.d("onDistanceClicked","id "+item.getStoreId());
 		Bundle bundle = new Bundle();
 		bundle.putString(MapsActivity.TAG_HOTELTITLE_EN, "");
 		bundle.putString(MapsActivity.TAG_HOTELADDRESS_EN,"");
 		bundle.putString(MapsActivity.TAG_HOTELPHONE, "");
 		bundle.putString(MapsActivity.TAG_HOTELFAX, "");
 		bundle.putString(MapsActivity.TAG_HOTELEMAIL_EN, "");
 		
 		Intent intent = new Intent(ListActivity.this, MapsActivity.class);
 		intent.putExtra(MapsActivity.TAG_BUNDLEBRANCH, bundle);
 		startActivity(intent);
 
 	}
 	public void onFavouriteClicked(View v){
 		Store item = (Store)v.getTag();
 		currentStoreId = item.getStoreId();
 		if(item.getIsFavourite()){
 			showProgress();
 			service.removeFavorite(currentStoreId);
 		}else{
 			showProgress();
 			service.postFavorite(currentStoreId);
 		}
 	}
 
 	@Override
 	public void onCompleted(Service service, ServiceResponse result) {
 		// TODO Auto-generated method stub
 //		Store store = (Store)result.getData();
 		if(result.isSuccess()&& result.getAction() == ServiceAction.ActionGetBusinessList){
 			ResponseData data = (ResponseData)result.getData();
 			if(data.getStatus().equals("200")){
 				if(isExpandableSearch){
 					permListMain = (ArrayList<Store>)data.getData();
 					exeBindSearchExpandableStoreData(permListMain);
 					isExpandableSearch = false;
 				}else{
 					showSearchExpandableList(false);
 					permListMain = (ArrayList<Store>)data.getData();
 					loadPerms();
 					
 					WhyqListController.isLoading = false;
 					if(permListAdapter != null) {
 						permListAdapter.notifyDataSetChanged();
 					}
 					
 				}
 			}else if(data.getStatus().equals("401")){
 				Util.loginAgain(getParent(), data.getMessage());
 			}else if(data.getStatus().equals("204")){
 				if(isExpandableSearch){
 					permListMain = (ArrayList<Store>)data.getData();
 					exeBindSearchExpandableStoreData(permListMain);
 					isExpandableSearch = false;
 				}
 			}else{
 //				Util.showDialog(getParent(), data.getMessage());
 			}
 			hideProgress();
 		} else if(result.isSuccess()&& result.getAction() == ServiceAction.ActionPostFavorite){
 //			Toast.makeText(context, "Favourite successfully", Toast.LENGTH_SHORT).show();
 			ResponseData data = (ResponseData)result.getData();
 			
 			if(data.getStatus().equals("200")){
 				updateFavoriteWitId(currentStoreId, true);
 			}else if(data.getStatus().equals("401")){
 				Util.loginAgain(getParent(), data.getMessage());
 			}else{
 //				Util.showDialog(getParent(), data.getMessage());
 			}
 			hideProgress();
 		}else if(result.isSuccess()&& result.getAction() == ServiceAction.ActionRemoveFavorite){
 //			Toast.makeText(context, "Un favourite successfully", Toast.LENGTH_SHORT).show();
 			ResponseData data = (ResponseData)result.getData();
 			if(data.getStatus().equals("200")){
 				updateFavoriteWitId(currentStoreId, false);
 			}else if(data.getStatus().equals("401")){
 				Util.loginAgain(getParent(), data.getMessage());
 			}else{
 //				Util.showDialog(getParent(), data.getMessage());
 			}
 			hideProgress();
 		}else if(!result.isSuccess()&& result.getAction() == ServiceAction.ActionPostFavorite){
 			Toast.makeText(context, "Can not favourite for now", Toast.LENGTH_SHORT).show();
 		}else if(!result.isSuccess()&& result.getAction() == ServiceAction.ActionRemoveFavorite){
 			Toast.makeText(context, "Can not un-favourite for now", Toast.LENGTH_SHORT).show();
 		}
 	}
 	private void updateFavoriteWitId(String id, boolean b) {
 		// TODO Auto-generated method stub
 		int size = whyqListView.getChildCount();
 		int value;
 		Store item2;
 		ViewHolder holder;
 		for(int i=0;i< size;i++){
 			item2 = permListMain.get(i);
 			if(item2.getStoreId().equals(id)){
 				holder = (ViewHolder)whyqListView.getChildAt(i).getTag();
 				if(b){
 					value = Integer.parseInt(holder.tvNumberFavourite.getText().toString())+Integer.parseInt("1");
 					holder.imgFavouriteThumb.setImageResource(R.drawable.icon_fav_enable);
 					item2.setIsFavourite(true);
 				}else{
 					value = Integer.parseInt(holder.tvNumberFavourite.getText().toString())-Integer.parseInt("1");
 					holder.imgFavouriteThumb.setImageResource(R.drawable.icon_fav_disable);
 					item2.setIsFavourite(false);
 				}
 				if(value < 0 )
 					value= 0;
 				holder.tvNumberFavourite.setText(""+value);
 				holder.imgFavouriteThumb.setTag(item2);
 				whyqListView.getChildAt(i).requestLayout();
 			}
 		}
 	}
 
 
     @Override
     public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
     }
 
     @Override
     public void onScrollStateChanged(AbsListView view, int scrollState) {
 
     	if(scrollState==0){
             int position = whyqListView.getFirstVisiblePosition();
             View v = whyqListView.getChildAt(0);
             int offset = (v == null) ? 0 : v.getTop();
 
             if (mPosition < position || (mPosition == position && mOffset < offset)) {
                  // Scrolled up
             	Log.d("onScrollStateChanged","up");
             	hideNavigationBar();
             	hideTabbarInTabhost();
             } else {
                  // Scrolled down
             	showNavigationBar();
             	showTabbarInTabhost();
             }
             mPosition = position;
             mOffset = offset;
     	}
     }
 
 
 
 
 	private void hideNavigationBar() {
 		// TODO Auto-generated method stub
     	LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)lnNavigation.getLayoutParams();
     	params.weight = 0;
     	lnNavigation.setLayoutParams(params);
 	}
 
 
 	private void showNavigationBar() {
 		// TODO Auto-generated method stub
     	Log.d("onScrollStateChanged","down");
     	LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)lnNavigation.getLayoutParams();
     	params.weight = 1;
     	lnNavigation.setLayoutParams(params);
 	}
 
 	private void hideTabbarInTabhost() {
 		// TODO Auto-generated method stub
 		WhyqMain.hideTabBar();
     	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)lnPageContent.getLayoutParams();
     	params.bottomMargin = 0;
     	lnPageContent.setLayoutParams(params);
 	}
 
 
 	private void showTabbarInTabhost() {
 		// TODO Auto-generated method stub
 		WhyqMain.showTabBar();
 		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)lnPageContent.getLayoutParams();
     	params.bottomMargin = (int)(WhyqApplication.Instance().getDensity() * 74);
     	lnPageContent.setLayoutParams(params);
 	}
 
 
 	private void exeBindSearchExpandableStoreData(ArrayList<Store> storeList){
 		boolean isNoData = true;
 		if(storeList!=null){
 			if(storeList.size()>0){
 				isNoData = false;
 				ArrayList<GroupStore> mGroupCollection = new ArrayList<GroupStore>();
 				ArrayList<String> idList = getStoreCateIdList(storeList);
 				int length = idList.size();
 				for (int i = 0; i < length; i++) {
 					try {
 
 						String id = idList.get(i);
 						GroupStore group = getGroupFromId(storeList, id);
 
 						if (group != null)
 							mGroupCollection.add(group);
 
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 				}
 				tvNumberResult.setText(""+storeList.size());
 				tvTextSearch.setText(""+searchKey);
 				ExpandableStoreAdapter adapter = new ExpandableStoreAdapter(context, expandbleStoreView, mGroupCollection);
 				expandbleStoreView.setAdapter(adapter);
 				showSearchExpandableList(true);
 				for (int i = 0; i < mGroupCollection.size(); i++) {
 					expandbleStoreView.expandGroup(i);
 				}
 			}
 		}
 		showSearchExpandableList(true);
 	}
 
 	private GroupStore getGroupFromId(List<Store> storyList, String id) {
 		// TODO Auto-generated method stub
 		GroupStore ge = new GroupStore();
 		List<Store> storiesList = new ArrayList<Store>();
 		int storiesLength = storyList.size();
 		for (int j = 0; j < storiesLength; j++) {
 			try {
 
 				Store story = storyList.get(j);
 				if (story.getCateid().equals(id)) {
 					storiesList.add(story);
 				}
 			
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 		if (storiesList.size() > 0) {
 			ge.setStoriesList(storiesList);
 			String name = storiesList.get(0).getNameCate();
 			if (name != null) {
 				ge.setName(name);
 			} else {
 				ge.setName("");
 			}
 			return ge;
 		} else {
 			return null;
 		}
 
 	}
 
 	// private String getNameStoreTypeById(List<Store> storyList, String id) {
 	// // TODO Auto-generated method stub
 	// for (Store menu : storyList) {
 	// try {
 	// ArrayList<ProductTypeInfo> productTypeInfoList = menu
 	// .getProductTypeInfoList();
 	// for (ProductTypeInfo productTypeInfo : productTypeInfoList) {
 	// try {
 	// if (productTypeInfo.getId().equals(id)) {
 	// return productTypeInfo.getNameProductType();
 	// }
 	//
 	// } catch (Exception e) {
 	// // TODO: handle exception
 	// e.printStackTrace();
 	// }
 	// }
 	//
 	// } catch (Exception e) {
 	// // TODO: handle exception
 	// e.printStackTrace();
 	// }
 	// }
 	// return null;
 	// }
 
 	private ArrayList<String> getStoreCateIdList(List<Store> menuList) {
 		// TODO Auto-generated method stub
 		ArrayList<String> listId = new ArrayList<String>();
 		int length = menuList.size();
 		for (int i = 0; i < length; i++) {
 			Store store = menuList.get(i);
 			if (!listId.contains(store.getCateid())) {
 				listId.add(store.getCateid());
 			}
 		}
 		return listId;
 	}
 
 	private void showSearchExpandableList(boolean isShow){
 		if(isShow){
 			rlExpandableStoreContent.setVisibility(View.VISIBLE);
 			
 		}else{
 			rlExpandableStoreContent.setVisibility(View.GONE);
 		}
 	}
 }
