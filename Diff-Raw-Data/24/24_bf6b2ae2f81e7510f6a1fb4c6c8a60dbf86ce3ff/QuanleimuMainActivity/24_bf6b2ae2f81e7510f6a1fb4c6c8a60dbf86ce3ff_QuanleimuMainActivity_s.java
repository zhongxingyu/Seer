 package com.quanleimu.activity;
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.Pair;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CheckBox;
 
 import com.quanleimu.activity.BaseFragment.ETAB_TYPE;
 import com.quanleimu.activity.SplashJob.JobDoneListener;
 import com.quanleimu.broadcast.CommonIntentAction;
 import com.quanleimu.broadcast.PushMessageService;
 import com.quanleimu.database.ChatMessageDatabase;
 import com.quanleimu.entity.ChatMessage;
 import com.quanleimu.entity.PostMu;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.LocationService;
 import com.quanleimu.util.MobileConfig;
 import com.quanleimu.util.Sender;
 import com.quanleimu.util.ShortcutUtil;
 import com.quanleimu.util.Communication.BXHttpException;
 import com.quanleimu.util.TrackConfig.TrackMobile.BxEvent;
 import com.quanleimu.util.Tracker;
 import com.quanleimu.util.Util;
 import com.quanleimu.view.AdViewHistory;
 import com.quanleimu.view.CustomizeTabHost;
 import com.quanleimu.view.CustomizeTabHost.TabIconRes;
 import com.quanleimu.view.CustomizeTabHost.TabSelectListener;
 import com.quanleimu.view.fragment.GridCateFragment;
 import com.quanleimu.view.fragment.HomeFragment;
 import com.quanleimu.view.fragment.PersonalInfoFragment;
 import com.quanleimu.view.fragment.TalkFragment;
 import com.umeng.update.UmengUpdateAgent;
 
 //import com.tencent.mm.sdk.openapi.BaseReq;
 //import com.tencent.mm.sdk.openapi.BaseResp;
 //import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
 public class QuanleimuMainActivity extends BaseActivity implements /*IWXAPIEventHandler,*/ JobDoneListener, TabSelectListener {
 	
 	private CustomizeTabHost globalTabCtrl;// = new CustomizeTabHost();
 	
 	public static boolean isInActiveStack;
 	
 	private List<Runnable> pendingTask;
 	
 //	private boolean isRestoring;
 	private SplashJob splashJob;
 	private BroadcastReceiver msgListener;
 	
 	public QuanleimuMainActivity(){
 		super();
 		
 		
 	}
 	
 	public void onSwitchToTab(ETAB_TYPE tabType){
 
 		BaseFragment fm = this.getCurrentFragment();
 		if (fm == null)
 		{
 			return;
 		}
 		
 		switch(tabType){
 		case ETAB_TYPE_MAINPAGE:
 //			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MAINPAGE)break;
 //			
 //			needClearViewStack = true;
 //			onNewView(new HomePageView(this, bundle));
 //			if (fm.getTabDef() != null && fm.getTabDef().m_tabSelected == BaseFragment.ETAB_TYPE.ETAB_TYPE_MAINPAGE) break;
 //			
 //			pushFragment(new HomeFragment(), bundle, true);
 			
 			break;
 		case ETAB_TYPE_CATEGORY:				
 //			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY)break;
 //			
 //			needClearViewStack = true;
 //			onNewView(new CateMainView(this));
 //			if (fm.getTabDef() != null && fm.getTabDef().m_tabSelected == BaseFragment.ETAB_TYPE.ETAB_TYPE_CATEGORY) break;
 //			
 //			pushFragment(new CatMainFragment(), bundle, true);
 			break;
 		case ETAB_TYPE_PUBLISH:
 //			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH)break;
 //			
 //			needClearViewStack = false;
 ////			onNewView(new PostGoodsCateMainView(this, bundle));
 ////			onNewView(new PostGoodsView(this, bundle, ""));
 //			onNewView(new GridCategoryView(this, bundle));
 //			
 			
 //			if (fm.getTabDef() != null && fm.getTabDef().m_tabSelected == BaseFragment.ETAB_TYPE.ETAB_TYPE_PUBLISH) break;
 //
 //			this.pushFragment(new GridCateFragment(), bundle, false);
 			
 			
 			break;
 		case ETAB_TYPE_MINE:
 //			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MINE)break;
 //			
 //			needClearViewStack = true;
 ////			onNewView(new PersonalCenterView(this, bundle));
 //			onNewView(new PersonalCenterEntryView(this, bundle));
 			
 //			if (fm.getTabDef() != null && fm.getTabDef().m_tabSelected == BaseFragment.ETAB_TYPE.ETAB_TYPE_MINE) break;
 //			
 //			pushFragment(new PersonalInfoFragment(), bundle, true);
 			break;
 		case ETAB_TYPE_SETTING:
 //			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_SETTING)break;
 //			
 //			needClearViewStack = true;
 //			onNewView(new SetMainView(this));
 			
 //			if(fm.getTabDef() != null && fm.getTabDef().m_tabSelected == BaseFragment.ETAB_TYPE.ETAB_TYPE_SETTING)break;
 //			
 //			pushFragment(new SetMainFragment(), bundle, true);
 			
 			break;			
 		}
 
 	}
 	
 	protected void onStatckTop(BaseFragment f) {
 		findViewById(R.id.tab_parent).setVisibility(f.hasGlobalTab() ? View.VISIBLE : View.GONE);
 		
 		//FIXME: 中文注释：tab bar的临时方案，下个版本会全面替换tab展现方式， 因为现在的tab的切换是不太符合产品设计这边的期望的
 		if (f.hasGlobalTab() && f instanceof HomeFragment)
 		{
 			this.globalTabCtrl.showTab(0);
 		}
 		else if (f.hasGlobalTab() && f instanceof GridCateFragment)
 		{
 			this.globalTabCtrl.showTab(1);
 		}
 		else if (f.hasGlobalTab() && f instanceof PersonalInfoFragment)
 		{
 			this.globalTabCtrl.showTab(2);
 		}
 					
 		
 	}
 	
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.e(TAG, "activity on activity result.");
 		BaseFragment fragment = getCurrentFragment();
 		if(fragment != null){
 			fragment.onActivityResult(requestCode, resultCode, data);
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	
 	}
 
 	public void handleBack(){
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
         imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout).getWindowToken(), 0); 
 
         BaseFragment currentFragmet = getCurrentFragment();
         
 		try{
 	    	if( currentFragmet != null && !currentFragmet.handleBack()){
     			if (getSupportFragmentManager().getBackStackEntryCount()>1)
     			{
     				popFragment(currentFragmet);
     			}
     			else
     			{
     				exitMainActivity();
     			}
 	    	}	
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void exitMainActivity() {
 		SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
 		String hasShowShortcutMessage = settings.getString("hasShowShortcut", "no");
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		LayoutInflater adbInflater = LayoutInflater.from(QuanleimuMainActivity.this);
 		View shortcutLayout = adbInflater.inflate(R.layout.shortcutshow, null);
 
 		final CheckBox shortcutCheckBox = (CheckBox) shortcutLayout.findViewById(R.id.shortcut);
 		final boolean needShowShortcut = "no".equals(hasShowShortcutMessage) && !ShortcutUtil.hasShortcut(this);
 		if (needShowShortcut)
 		{
 		    builder.setView(shortcutLayout);
 		}
 
 		builder.setTitle(R.string.dialog_title_info)
 			.setMessage(R.string.dialog_message_confirm_exit)
 			.setNegativeButton(R.string.no, null)
 			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 		{
 
 		    @Override
 		    public void onClick(DialogInterface dialog, int which)
 		    {
 		    	
 		    		LocationService.getInstance().stop();
 		        if (needShowShortcut && shortcutCheckBox.isChecked())
 		        {
 		            ShortcutUtil.addShortcut(QuanleimuMainActivity.this);
 		        }
 		        SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
 		        SharedPreferences.Editor editor = settings.edit();
 		        editor.putString("hasShowShortcut", "yes");
 		        // Commit the edits!
 		        editor.commit();
 				Intent pushIntent = new Intent(QuanleimuMainActivity.this, com.quanleimu.broadcast.BXNotificationService.class);
 				QuanleimuMainActivity.this.startService(pushIntent);
 
 				Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
 				startPush.putExtra("updateToken", true);
 				QuanleimuMainActivity.this.startService(startPush);
 				
 				QuanleimuApplication.deleteOldRecorders(3600 * 24 * 3);
 //		            		Debug.stopMethodTracing();
 				QuanleimuApplication.mDemoApp = null;
 				isInActiveStack = false;
 				
 				ChatMessageDatabase.prepareDB(QuanleimuMainActivity.this);
 				ChatMessageDatabase.clearOldMessage(1000);
 				
 				// BxSender & BxTracker save data into file.
 		    	try {
 					Sender.getInstance().save();
 					Tracker.getInstance().save();
 				} catch (Exception e) {}
 		    	Log.d("quanleimu", "exit");
 		    	dialog.dismiss();
 		    	AdViewHistory.getInstance().clearHistory();
 		    	QuanleimuMainActivity.this.finish();
 //		        System.exit(0);
 //		            		QuanleimuMainActivity.this.finish();
 		    }
 		});
 		builder.create().show();
 	}
 	
 	
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		isInActiveStack = false;
 	}
 
 //	@Override
 //	public void onExit(BaseView view){
 //        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
 //        imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout).getWindowToken(), 0); 
 //		
 //    	if(view == currentView ){//FIXME:
 //    		LinearLayout scroll = (LinearLayout)findViewById(R.id.contentLayout);
 //    		currentView.onPause();
 //    		((LinearLayout)findViewById(R.id.linearTitleControls)).removeAllViews();
 //    		scroll.removeAllViews();    		
 //    		currentView.onDestroy();
 //    		
 //    		currentView = null;
 //    		
 //    		
 //    		/*currentView = QuanleimuApplication.getApplication().getViewStack().pop();
 //    		if(null != currentView){
 //	    		setBaseLayout(currentView);
 //	    		scroll.addView(currentView);
 //	    		
 //    		}*/
 //    		/*else{
 //    			onNewView(new HomePageView(this));
 //    		}*/
 //    	}
 //	}
 	
 
 	@Override
 	protected void onPause() {
 
 		Tracker.getInstance().event(BxEvent.APP_PAUSE).end();
 		unregisterMsgListener();
 		
 		super.onPause();
 		
 //		
 //		MobclickAgent.onPause(this);
 //		
 //		Log.d("Umeng SDK API call", "onPause() called from QuanleimuMainActivity:onPause()!!");
 	}
 	
 	@Override
 	protected void onResume() {
 		Tracker.getInstance().event(BxEvent.APP_RESUME).end();
 		
 //		Profiler.markStart("mainresume");
 		bundle.putString("backPageName", "");
 		super.onResume();
 		isInActiveStack = true;
 		BaseFragment bf = this.getCurrentFragment();
 		if(bf == null && splashJob == null){
 			splashJob = new SplashJob(this, this);
 		}
 		if (splashJob != null && !splashJob.isJobDone())
 		{
 			splashJob.doSplashWork();
 		}
 		else
 		{
 			startTalking(getIntent());
 			responseOnResume();
 		}
 		
 //		Profiler.markEnd("mainresume");
 		
 //		MobclickAgent.onResume(this);
 //		
 //		Log.d("Umeng SDK API call", "onResume() called from QuanleimuMainActivity:onResume()!!");
 	} 
 	
 	
 	
 	
 	@Override
 	protected void onStop() {
 		Tracker.getInstance().event(BxEvent.APP_STOP).end();
 		super.onStop();
 	}
 	
 	protected void onDestory()
 	{
 		LocationService.getInstance().stop();
 		super.onDestroy();
 	}
 
 	private void responseOnResume()
 	{
 		this.checkAndUpdateBadge(0);
 		registerMsgListener();
 	}
 	
 	static public final String WX_APP_ID = "wx862b30c868401dbc";
 //	static public final String WX_APP_ID = "wx47a12013685c6d3b";//debug
 //	static public final String WX_APP_ID = "wxc54c9e29fcd6993d";////burizado, baixingwang2
 	
 //	private void showDetailViewFromWX(){
 //		Intent intent = this.getIntent();
 //		if(intent != null){
 //			Bundle bundle = intent.getExtras();
 //			if(bundle != null){
 //				if(bundle.getBoolean("isFromWX") && bundle.getString("detailFromWX") != null){
 //					
 //					GoodsList gl = JsonUtil.getGoodsListFromJson((String)bundle.getString("detailFromWX"));
 //					if(gl != null){
 //						GoodsListLoader glLoader = new GoodsListLoader(null, null, null, gl);
 //						glLoader.setGoodsList(gl);
 //						glLoader.setHasMore(false);		
 ////						BaseView pb = QuanleimuApplication.getApplication().getViewStack().peer();
 //						BaseFragment currentF = getCurrentFragment();
 //						if (currentF instanceof GoodDetailFragment)
 //						{
 //							popFragment(currentF);
 //						}
 //						
 //						Bundle args = new Bundle();
 //						args.putAll(this.bundle);
 //						args.putSerializable("loader", glLoader);
 //						args.putInt("index", 0);
 //						pushFragment(new GoodDetailFragment(), args, false);
 ////						if(pb != null && currentView != null){
 ////							if((currentView instanceof GoodDetailView) && (pb instanceof GoodDetailView)){
 ////								this.onBack();
 ////							}
 ////						}
 ////						onNewView(new GoodDetailView(this, this.bundle, glLoader, 0, null));
 //					}
 //				}
 //			}
 //		}		
 //	}
 	
 	
 	
 //	@Override
 //	public boolean onContextItemSelected(MenuItem item) {
 //		if (currentView != null)
 //		{
 //			return currentView.handleContextMenuSelect(item);
 //		}
 //		
 //		return super.onContextItemSelected(item);
 //	}
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		Log.d("quanleimu", "onSaveInstanceState");
 
 		// BxSender & BxTracker save data into file.
 		try {
 			Sender.getInstance().save();
 			Tracker.getInstance().save();
 		} catch (Exception e) {
 		}
 		
 		if (globalTabCtrl != null)
 		{
 			savedInstanceState.putSerializable("tabCtrl", globalTabCtrl);
 		}
 		
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		Log.d("quanleimu", "onRestoreInstanceState");
 //		// BxSender & BxTracker save data into file.
 //		try {
 //			Tracker.getInstance().load();
 //		} catch (Exception e) {
 //		}
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public void onJobDone() {
 		
 		//Start server when application is start.
 		Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
 		startPush.putExtra("updateToken", true);
 		this.startService(startPush);
 		
 		//Update UI after splash.
 		
 		View left = findViewById(R.id.left_action);
 		left.setOnClickListener(this);
 		View right = findViewById(R.id.right_action);
 		right.setOnClickListener(this);
 		View search = findViewById(R.id.search_action);
 		search.setOnClickListener(this);
 		
 		if(!QuanleimuApplication.update){
 			QuanleimuApplication.update = true;
             //去掉幽梦旧sdk 更新调用
 		}
 		
 		/*//TODO: refactor
 		BaseView childView = new HomePageView(this, bundle);		
 		currentView = childView;
 		childView.setInfoChangeListener(this);		
 		setBaseLayout(childView);
 		scroll.addView(childView);*/
 		
 		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
 		{
 			this.pushFragment(new HomeFragment(), bundle, true);
 		}
 		else
 		{
 			this.notifyStackTop();
 		}
 		findViewById(R.id.splash_cover).setVisibility(View.GONE);
 		
 //		findViewById(R.id.splash_cover).setVisibility(View.GONE);
 //		findViewById(R.id.splash_cover).setBackgroundColor(color.transparent); //this may remove image reference.
 		
 //		QuanleimuApplication.wxapi = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
 //		QuanleimuApplication.wxapi.registerApp(WX_APP_ID);
 //		QuanleimuApplication.wxapi.handleIntent(this.getIntent(), this);
 //		showDetailViewFromWX();
 		
 		startTalking(getIntent()); //Launch after splash job.
 		
 		responseOnResume();
 		
 //		findViewById(R.id.splash_cover).setVisibility(View.GONE);
 //		findViewById(R.id.splash_cover).setBackgroundColor(color.transparent); //this may remove image reference.
 		
 		this.splashJob = null; //Remove splash job reference.
 		if (pendingTask != null && pendingTask.size() > 0)
 		{
 			for (Runnable task : pendingTask)
 			{
 				task.run();
 			}
 		}
 		((new Thread(new Runnable(){
 			@Override
 			public void run(){
 				PostMu postMu = (PostMu)Util.loadDataFromLocate(QuanleimuApplication.getApplication().getApplicationContext(), "saveFirstStepCate");
 				if(postMu != null){
 					long time = postMu.getTime();
 					if (time + (7 * 24 * 3600 * 1000) < System.currentTimeMillis()) {
 						String apiName = "category_list";
 						ArrayList<String> list = new ArrayList<String>();
 						String url = Communication.getApiUrl(apiName, list);
 						try {
 							String json = Communication.getDataByUrl(url, false);
 							if (json != null) {
 								postMu.setJson(json);
 								postMu.setTime(System.currentTimeMillis());
 								Util.saveDataToLocate(QuanleimuApplication.getApplication().getApplicationContext(), "saveFirstStepCate", postMu);
 							}
 						} catch(Exception e){
 							
 						}
 					}
 				}
 				
 				try {
 					// 1. load from locate.
 					Pair<Long, Object> pair = Util.loadDataAndTimestampFromLocate(QuanleimuApplication.getApplication().getApplicationContext(), "cityjson");
 					
 					long timestamp = pair.first;
 					String content = (String) pair.second;
 					
 					// 2. load from server.
 					long updateTimestamp = MobileConfig.getInstance().getCityTimestamp();
 					if (timestamp < updateTimestamp || content == null || content.length() == 0) {
 						String apiName = "city_list";
 						String url = Communication.getApiUrl(apiName, new ArrayList<String>());
 						content = Communication.getDataByUrl(url, true);
 						if (content != null && content.length() > 0) 
 						{
 							Util.saveDataToLocate(QuanleimuApplication.getApplication().getApplicationContext(), "cityjson", content);
 						}
 					}
 		
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (BXHttpException e) {
 					e.printStackTrace();
 				}
 			}
 		}))).start();
 //		Toast.makeText(this, Profiler.dump(), Toast.LENGTH_LONG).show();
 //		Profiler.clear();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		this.setTheme(R.style.lightTheme);
 		Log.d("push", "push, on create");
 //		Profiler.markStart("maincreate");
 //		Debug.startMethodTracing();
 		super.onCreate(savedInstanceState);
 
        if (MobileConfig.getInstance().isUseUmengUpdate()) {
            UmengUpdateAgent.update(this);
        }
 
 		if (savedInstanceState != null)
 		{
 			globalTabCtrl = (CustomizeTabHost)savedInstanceState.get("tabCtrl");
 		}
 		
 		if (globalTabCtrl == null) {
 	        String tabBrowse = getString(R.string.tab_browse);
 	        String tabUserCenter = getString(R.string.tab_user_center);
 	        String tabPost = getString(R.string.tab_post);
 			globalTabCtrl = CustomizeTabHost.createTabHost(0, new String[] {tabBrowse, tabPost, tabUserCenter}, 
 					new TabIconRes[] {new TabIconRes(R.drawable.icon_footer_category_on, R.drawable.icon_footer_category),
 					new TabIconRes(R.drawable.icon_footer_post_on, R.drawable.icon_footer_post),
 					new TabIconRes(R.drawable.icon_footer_profile_on, R.drawable.icon_footer_profile)});
 		}
 		
 		QuanleimuApplication.context = new WeakReference<Context>(this);
 		QuanleimuApplication.getApplication().setErrorHandler(this);
 		Intent pushIntent = new Intent(this, com.quanleimu.broadcast.BXNotificationService.class);
 		this.stopService(pushIntent);
 		
 //		Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
 //		startPush.putExtra("updateToken", true);
 //		this.startService(startPush);
 		pendingTask = new ArrayList<Runnable>();
 		
 		setContentView(R.layout.main_activity);
 		if (savedInstanceState == null)
 		{
 			findViewById(R.id.splash_cover).setVisibility(View.VISIBLE);
 		}
 		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
 		
 		splashJob = new SplashJob(this, this);
 		
 //		isRestoring = savedInstanceState != null;
 		Intent intent = this.getIntent();
 		if(intent != null){
 			if(intent.getBooleanExtra("fromNotification", false)){
 				QuanleimuApplication.version = Util.getVersion(this);
 				QuanleimuApplication.udid = Util.getDeviceUdid(this);
 			}
 		}
 //		Profiler.markEnd("maincreate");
 	}
 	
 	@Override
 	protected void onStart() {
 		Tracker.getInstance().event(BxEvent.APP_START).end();
 		super.onStart();
 	}
 	
 	private void showDataFromAlbamOrPhoto(){
 		Intent intent = this.getIntent();
 		Bundle bd = intent.getExtras();
 		Log.w(TAG, bundle.toString());
 		if(bd != null && bd.containsKey(ThirdpartyTransitActivity.isFromPhotoOrAlbam) && bd.getBoolean(ThirdpartyTransitActivity.isFromPhotoOrAlbam)){
 //			if(this.currentView instanceof PostGoodsView)
 			{
 				getCurrentFragment().onActivityResult(bd.getInt(ThirdpartyTransitActivity.Key_RequestCode),
 						bd.getInt(ThirdpartyTransitActivity.Key_RequestResult),
 						(Intent)bd.getParcelable(ThirdpartyTransitActivity.Key_Data));
 //				currentView.onActivityResult(bd.getInt(ThirdpartyTransitActivity.Key_RequestCode),
 //						bd.getInt(ThirdpartyTransitActivity.Key_RequestResult),
 //						(Intent)bd.getParcelable(ThirdpartyTransitActivity.Key_Data));
 			}
 		}
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		setIntent(intent);
 //		Runnable task = new Runnable() {
 //			public void run()
 //			{
 ////				QuanleimuApplication.wxapi.handleIntent(getIntent(), QuanleimuMainActivity.this);
 ////				showDetailViewFromWX();
 ////				showDataFromAlbamOrPhoto();
 //				
 //				startTalking(getIntent());
 //			}
 //		};
 //		
 //		if (splashJob == null || splashJob.isJobDone()) //do not handle any intent before splash job done.
 //		{
 //			task.run();
 //		}
 //		else if (!splashJob.isJobDone())
 //		{
 //			pendingTask.add(task);
 //		}
 	}
 	
 	private void startTalking(Intent intent)
 	{
 		if (intent.getBooleanExtra("isTalking", false) && Util.getMyId(this) != null)//
 		{
 			ChatMessage msg = (ChatMessage) intent.getSerializableExtra(CommonIntentAction.EXTRA_MSG_MESSAGE);
 			Bundle bundle = new Bundle();
 			bundle.putString("receiverId", msg.getFrom());
 			bundle.putString("adId", msg.getAdId());
 			bundle.putString("sessionId", msg.getSession());
 			bundle.putSerializable("message", msg);
 //			onNewView(new Talk(this,bundle));
 			pushFragment(new TalkFragment(), bundle, false);
 		}
 		
 		intent.removeExtra("isTalking"); //Only use this flag once.
 	}
 
 //	// ΢�ŷ������󵽵���Ӧ��ʱ����ص����÷���
 //	@Override
 //	public void onReq(BaseReq req) {
 //		int i = 0;
 //		if(i == 1)
 //			return;
 ////		switch (req.getType()) {
 ////		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
 ////			goToGetMsg();		
 ////			break;
 ////		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
 ////			goToShowMsg((ShowMessageFromWX.Req) req);
 ////			break;
 ////		default:
 ////			break;
 ////		}
 //	}
 
 	// ����Ӧ�÷��͵�΢�ŵ�����������Ӧ����ص����÷���
 //	@Override
 //	public void onResp(BaseResp resp) {
 //		
 //		int result = 0;
 //		if(result == 1)
 //			return;
 ////		switch (resp.errCode) {
 ////		case BaseResp.ErrCode.ERR_OK:
 ////			result = R.string.errcode_success;
 ////			break;
 ////		case BaseResp.ErrCode.ERR_USER_CANCEL:
 ////			result = R.string.errcode_cancel;
 ////			break;
 ////		case BaseResp.ErrCode.ERR_AUTH_DENIED:
 ////			result = R.string.errcode_deny;
 ////			break;
 ////		default:
 ////			result = R.string.errcode_unknown;
 ////			break;
 ////		}
 ////		
 //	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.right_action:
 //			currentView.onRightActionPressed();
 //			intent.setClass(HomePage.this, Search.class);
 //			bundle.putString("searchType", "homePage");
 //			intent.putExtras(bundle);
 //			startActivity(intent);
 			getCurrentFragment().handleRightAction();
 			
 			break;
 		case R.id.left_action:
 //			if (!getCurrentFragment().handleBack())
 			{
 				this.handleBack();
 			}
 			break;
 		case R.id.search_action:
 		{
 			getCurrentFragment().handleSearch();
 			break;
 		}
 //		case R.id.ivHomePage:{
 //			this.onSwitchToTab(BaseFragment.ETAB_TYPE.ETAB_TYPE_MAINPAGE);
 //			break;
 //		}
 ////		case R.id.ivCateMain:				
 ////			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY);
 ////			break;
 //		case R.id.ivPostGoods:
 //			this.onSwitchToTab(BaseFragment.ETAB_TYPE.ETAB_TYPE_PUBLISH);			
 //			break;
 //		case R.id.ivMyCenter:
 //			this.onSwitchToTab(BaseFragment.ETAB_TYPE.ETAB_TYPE_MINE);			
 //			break;
 //		case R.id.ivSetMain:
 //			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_SETTING);	
 //			break;
 		}
 		super.onClick(v);
 	}
 
 	private final static String SHARE_PREFS_NAME = "baixing_shortcut_app";
 	
 	@Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if (keyCode == KeyEvent.KEYCODE_BACK)
         {
         	handleBack();
         }
         
         else{
         	return super.onKeyDown(keyCode, event);
         }
         
         return true;
     }
 	
 	private void checkAndUpdateBadge(long uiDelay)
 	{
 //		final BadgeView v = (BadgeView) findViewById(R.id.badge);
 //		uiDelay = uiDelay > 0 ? uiDelay : 0;
 //			v.postDelayed(new Runnable() {
 //
 //			public void run() {
 //				ChatMessageDatabase.prepareDB(QuanleimuMainActivity.this);
 //				final String myId = Util.getMyId(QuanleimuMainActivity.this);
 //				int count = ChatMessageDatabase.getUnreadCount(null, myId);
 //				Log.d("badge", "count" + count);
 //				v.setText(count + "");
 //
 //				if (count == 0 ||  myId == null) {
 //					v.setVisibility(View.GONE);
 //				} else {
 //					v.setVisibility(View.VISIBLE);
 //				}
 //			}
 //
 //		}, uiDelay);
 	}
 	
 	private void registerMsgListener()
 	{
 		if (msgListener == null)
 		{
 			msgListener = new BroadcastReceiver() {
 
 				public void onReceive(Context outerContext, Intent outerIntent) {
 					if (outerIntent != null && outerIntent.hasExtra(CommonIntentAction.EXTRA_MSG_MESSAGE))
 					{
 						ChatMessage msg = (ChatMessage) outerIntent.getSerializableExtra(CommonIntentAction.EXTRA_MSG_MESSAGE);
 						if (msg.getTo().equals(Util.getMyId(QuanleimuMainActivity.this)))
 						{
 							checkAndUpdateBadge(50);
 						}
 					}
 				}
 				
 			};
 		}
 		
 		registerReceiver(msgListener, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_NEW_MSG));
 	}
 	
 	protected void unregisterMsgListener()
 	{
 		if (msgListener != null){
 			try{
 				unregisterReceiver(msgListener);
 			}catch(IllegalArgumentException e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 //	private int lastIndex;
 	@Override
 	public void beforeChange(int currentIndex, int nextIndex) {
 //		if (nextIndex != 1)
 //		{
 //			lastIndex = nextIndex;
 //		}
 	}
 
 	/**
 	 * This code only have short life and will be replaced with new logic after 3.0 release. So just ignor if you think it's ugly
 	 */
 	public void afterChange(int newSelectIndex) {
 		switch(newSelectIndex)
 		{
 		case 0:
 			this.pushFragment(new HomeFragment(), bundle, true);
 			break;
 		case 1:
             //发布赌约
             QuanleimuApplication.postEntryFlag = 0;
 
 			Bundle args = new Bundle(bundle);
 			args.putInt(BaseFragment.ARG_COMMON_ANIMATION_IN, 0);
 			args.putInt(BaseFragment.ARG_COMMON_ANIMATION_EXIT, 0);
 			pushFragment(new GridCateFragment(), args, true);
 			break;
 		case 2:
 			this.pushFragment(new PersonalInfoFragment(), bundle, true);
 			break;
 		}
 	}
 	
 }
