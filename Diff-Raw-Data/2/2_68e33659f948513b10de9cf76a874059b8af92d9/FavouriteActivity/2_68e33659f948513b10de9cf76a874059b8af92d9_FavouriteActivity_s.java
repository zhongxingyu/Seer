 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.whyq.R;
 
 import whyq.WhyqApplication;
 import whyq.WhyqMain;
 import whyq.adapter.WhyqAdapter;
 import whyq.adapter.WhyqAdapter.ViewHolder;
 import whyq.controller.WhyqListController;
 import whyq.interfaces.IServiceListener;
 import whyq.interfaces.Login_delegate;
 import whyq.map.MapsActivity;
 import whyq.model.ResponseData;
 import whyq.model.Store;
 import whyq.model.User;
 import whyq.service.ServiceAction;
 import whyq.service.ServiceResponse;
 import whyq.utils.API;
 import whyq.utils.RSA;
 import whyq.utils.UrlImageViewHelper;
 import whyq.utils.Util;
 import whyq.utils.WhyqUtils;
 import whyq.utils.XMLParser;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
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
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AbsListView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 
 public class FavouriteActivity extends FragmentActivity implements Login_delegate, OnClickListener, IServiceListener{
 
 	
 	public static final String DOWNLOAD_COMPLETED = "DOWNLOAD_COMPLETED";
 	public static final String COFFE = "";
 	public String url = "";
 	public Boolean header = true;
 
 	/**
 	 * MSA
 	 */
 	private ArrayList<Store> permListMain = new ArrayList<Store>();
 
 	private boolean isLoadMore = false;
	private int page = 1;
 	
 	public static int screenWidth;
 	public static int screenHeight;
 	public static boolean isLogin = false;
 	public static int loginType = 0;
 	public static boolean isRefesh = true;
 	public static boolean isCalendar = false;
 	int nextItem = -1;
 	
 	ListView whyqListView;
 	ProgressBar progressBar;
 	WhyqAdapter permListAdapter;
 	View headerView = null;
 	private boolean isFirst = true;
 	public static LoadPermList loadPermList;
 	public boolean isAddHeader = false;
 	private EditText etTextSearch;
 	/** Called when the activity is first created. */
 	/*
 	 * Whyq elements
 	 */
 	
 	private String searchKey="";
 	private String longitude="";
 	private String latgitude="";
 	public static boolean isSearch = false;
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			if (intent.getAction().equals(DOWNLOAD_COMPLETED)) {
 				exeListActivity(false);
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
 					String storeId = store.id;
 					if(storeId !=null)
 						gotoStoreDetail(storeId);
 				}
 				
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 	};
 	private TextView tvNotifyNoData;
 	private whyq.service.Service service;
 	private Button btnCacel;
 	private RelativeLayout rlSearchTools;
 	private TextView tvHeader;
 	private LayoutParams params;
 	public static boolean  isFavorite = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		createUI();
 		IntentFilter intentFilter = new IntentFilter(DOWNLOAD_COMPLETED);
 		registerReceiver(receiver, intentFilter);
 		service = new whyq.service.Service(FavouriteActivity.this);
 		WhyqUtils.clearViewHistory();
 		WhyqUtils utils= new WhyqUtils();
 		utils.writeLogFile(FavouriteActivity.this.getIntent());
     	showProgress();
     	exeListActivity(false);
 
 	}
 
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		isFavorite = false;
 	}
 	protected void gotoStoreDetail(String storeId) {
 		// TODO Auto-generated method stub
 		Intent intent = new Intent(FavouriteActivity.this, ListDetailActivity.class);
 		startActivity(intent);
 	}
 
 	public void createUI() {
 		setContentView(R.layout.whyq_favorite_screen);//
 		isFavorite = true;
 		
 		whyqListView = (ListView) findViewById(R.id.lvWhyqList);
 		loadPermList = new LoadPermList(false);
 		progressBar = (ProgressBar)findViewById(R.id.prgBar);
 		etTextSearch =(EditText) findViewById(R.id.etTextSearch);
 		tvNotifyNoData = (TextView)findViewById(R.id.tvNotifyNoResult);
 		btnCacel = (Button)findViewById(R.id.btnCancel);
 		rlSearchTools = (RelativeLayout)findViewById(R.id.rlSearchtool);
 		tvHeader = (TextView)findViewById(R.id.tvHeaderTittle);
 		tvHeader.setText("Favourites");
 		tvHeader.setTextSize(20);
 		params = (RelativeLayout.LayoutParams)tvHeader.getLayoutParams();
 		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
 		context = FavouriteActivity.this;
 //		tvHeader.setLayoutParams(params);
 		hideFilterGroup();
 		isAddHeader = true;
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
 						exeSearch(text);
 					} catch (Exception e) {
 						// TODO: handle exception
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 		etTextSearch.addTextChangedListener(mTextEditorWatcher);
 		
 		whyqListView.setOnScrollListener(new OnScrollListener() {
 			
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) {
 				// TODO Auto-generated method stub
 				int currentItem = firstVisibleItem + visibleItemCount;
 				Log.d("onScroll","onScroll current "+currentItem+" and total "+totalItemCount);
 				if((currentItem >=  totalItemCount-1) && !isLoadMore){
 					isLoadMore = true;
 					page++;
 					loadPermList = new LoadPermList(isSearch);
 					loadPermList.execute();;
 				}
 			}
 		});
 
 	}
 
 	
 	
 	
 
 	private void hideFilterGroup() {
 		// TODO Auto-generated method stub
 		rlSearchTools.setVisibility(View.GONE);
 	}
 	protected void exeSearch(String string) {
 		// TODO Auto-generated method stub
 		searchKey = string;
 		isSearch = true;
 		exeListActivity(true);
 	}
 
 	@Override
 	protected void onDestroy(){
 		super.onDestroy();
 		isCalendar =false;
 		isRefesh = true;
 		isFavorite = false;
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
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
     	isFavorite = false;
     	isFirst = true;
     	nextItem = -1;
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
 			this.url = API.favoriteListURL;
 			this.header = false;
 		}
 		clearData();
 		showProgress();
 
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
 			
 			loadPermList = new LoadPermList(isSearch);
 			loadPermList.execute();
 		}
 		
 	}
 
 	public void loadNextItems() {
 		if(permListAdapter != null) {
 			nextItem = permListAdapter.getNextItems();
 			showProgress();
 	    	loadPermList = new LoadPermList(isSearch);
 			loadPermList.execute();
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
 				this.permListAdapter = new WhyqAdapter(FavouriteActivity.this,
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
 
 //			int selected = permListAdapter.getCount() - permListMain.size() - 2;
 //			if(selected >= 0) {
 //				whyqListView.setSelection(selected);
 //			} else {
 //				whyqListView.setSelection(0);
 //			}
 
 		}else{
 			
 			showNotifyNoData();
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
 		private String cateId="";
 
 		public LoadPermList(boolean isSearch){
 			this.isSearch = isSearch; 
 		}
 		
 		@Override
 		protected ArrayList<Store> doInBackground(ArrayList<Store>... params) {
 			// TODO Auto-generated method stub
 			Util.getLocation(FavouriteActivity.this);
 			WhyqListController whyqListController = new WhyqListController();
 			ArrayList<Store> permList = null;
 			try {				
 				if (nextItem != -1) {
 //					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 					Map<String, String> postParams = new HashMap<String, String>();
 //					nameValuePairs.add(new BasicNameValuePair("nextItem", String.valueOf(nextItem)));
 					postParams.put("nextItem", String.valueOf(nextItem));
 					if(isCalendar){
 //						nameValuePairs.add(new BasicNameValuePair("uid", WhyqMain.UID));
 //						isCalendar =false;
 						postParams.put("uid", WhyqMain.UID);
 						
 					}
 
 //					permList = whyqListController.getBusinessList(url, nameValuePairs);
 				} else {
 					if(isCalendar){
 						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 						nameValuePairs.add(new BasicNameValuePair("uid", WhyqMain.UID));
 						permList = whyqListController.getBusinessList(url,nameValuePairs);
 						
 					}else{
 						RSA rsa = new RSA();
 						String enToken = rsa.RSAEncrypt(XMLParser.getToken(WhyqApplication.Instance().getApplicationContext()));
 //						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 						Map<String, String> postParams = new HashMap<String, String>();
 //						nameValuePairs.add(new BasicNameValuePair("token",enToken));
 						postParams.put("token", enToken);
 						postParams.put("page", ""+page);
 						postParams.put("longitude", longitude);
 						postParams.put("latitude", latgitude);
 						Log.d("Favourite","Favourite is search"+isSearch);
 						if(isSearch){
 //							nameValuePairs.add(new BasicNameValuePair("key", searchKey));
 //							nameValuePairs.add(new BasicNameValuePair("search_longitude", longitude));
 //							nameValuePairs.add(new BasicNameValuePair("search_latitude", latgitude));
 //							nameValuePairs.add(new BasicNameValuePair("cate_id", cateId));
 							
 							postParams.put("key", searchKey);
 							postParams.put("search_longitude", longitude);
 							postParams.put("search_latitude", latgitude);
 							postParams.put("cate_id", cateId);
 						}else{
 
 						}
 						service.getBusinessList(postParams, url);
 //						permList = whyqListController.getBusinessList(url, nameValuePairs);	
 					}
 					
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 
 //				hideProgress();
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
 //			WhyqListController.isLoading = false;
 			
 			//permListMain.size();
 //			if (dialog != null && dialog.isShowing()) {
 //				dialog.dismiss();
 //			}
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
 	        WhyqMain.back();
 	        return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
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
 		
 		Intent intent = new Intent(FavouriteActivity.this, MapsActivity.class);
 		intent.putExtra(MapsActivity.TAG_BUNDLEBRANCH, bundle);
 		startActivity(intent);
 
 	}
 	@Override
 	public void on_success() {
 		// TODO Auto-generated method stub
 		WhyqMain.refeshFollowerActivity();
 	}
 
 	@Override
 	public void on_error() {
 		// TODO Auto-generated method stub
 		//Logger.appendLog("test log", "loginerror");
 		Intent intent = new Intent(getApplicationContext(), JoinWhyqActivity.class);
 		this.startActivity(intent);		
 	}
 
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
 					exeSearch(text);
 				}
 			
 			} catch (Exception e) {
 				// TODO: handle exception
 			}
 		}
 	};
 	private Context context;
 	private String currentStoreId;
 
 	public void onCancelClicked(View v){
 		exeDisableSearchFocus();
 		etTextSearch.setText("");
 		
 	}
 
 	private void exeDisableSearchFocus() {
 		// TODO Auto-generated method stub
 		btnCacel.setVisibility(View.GONE);
 //		params.width = 60;
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
 		}
 	}
 
 	private void hideProgress() {
 		// TODO Auto-generated method stub
     	if(progressBar.getVisibility() == View.VISIBLE){
     		progressBar.setVisibility(View.GONE);
     	}
 	}
 	private void showProgress() {
 		// TODO Auto-generated method stub
     	if(progressBar.getVisibility() != View.VISIBLE){
     		progressBar.setVisibility(View.VISIBLE);
     	}
 
 	}
 	private void showNotifyNoData(){
 		tvNotifyNoData.setVisibility(View.VISIBLE);
 	}
 	private void hideNotifyNoData(){
 		tvNotifyNoData.setVisibility(View.GONE);
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
 	public void onCompleted(whyq.service.Service service, ServiceResponse result) {
 		// TODO Auto-generated method stub
 
 		if(result.isSuccess()&& result.getAction() == ServiceAction.ActionGetBusinessList){
 			ResponseData data = (ResponseData)result.getData();
 			if(data.getStatus().equals("200")){
 				permListMain = (ArrayList<Store>)data.getData();
 				loadPerms();
 				
 				WhyqListController.isLoading = false;
 				if(permListAdapter != null) {
 					permListAdapter.notifyDataSetChanged();
 				}
 			}else if(data.getStatus().equals("401")){
 				Util.loginAgain(getParent(), data.getMessage());
 			}else if(data.getStatus().equals("204")){
 				
 			}else{
 				Util.showDialog(getParent(), data.getMessage());
 			}
 			hideProgress();
 		} else if(!result.isSuccess()&& result.getAction() == ServiceAction.ActionGetBusinessList){
 			hideProgress();
 		}else if(result.isSuccess()&& result.getAction() == ServiceAction.ActionPostFavorite){
 //			Toast.makeText(context, "Favourite successfully", Toast.LENGTH_SHORT).show();
 			ResponseData data = (ResponseData)result.getData();
 			
 			if(data.getStatus().equals("200")){
 				updateFavoriteWitId(currentStoreId, true);
 			}else if(data.getStatus().equals("401")){
 				Util.loginAgain(getParent(), data.getMessage());
 			}else{
 				Util.showDialog(getParent(), data.getMessage());
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
 				Util.showDialog(getParent(), data.getMessage());
 			}
 			hideProgress();
 		}else if(!result.isSuccess()&& result.getAction() == ServiceAction.ActionPostFavorite){
 			Toast.makeText(context, "Can not favourite for now", Toast.LENGTH_SHORT).show();
 		}else if(!result.isSuccess()&& result.getAction() == ServiceAction.ActionRemoveFavorite){
 			Toast.makeText(context, "Can not un-favourite for now", Toast.LENGTH_SHORT).show();
 		}
 		if(isLoadMore)
 			isLoadMore = false;
 	}
 }
