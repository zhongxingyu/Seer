 package com.baixing.view.fragment;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.baixing.entity.FirstStepCate;
 import com.baixing.util.TrackConfig.TrackMobile.PV;
 import com.baixing.util.Tracker;
 import com.baixing.util.ViewUtil;
 import com.baixing.widget.CustomizeGridView;
 import com.baixing.widget.CustomizeGridView.GridInfo;
 import com.baixing.widget.CustomizeGridView.ItemClickListener;
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 
 public class HomeFragment extends BaseFragment implements ItemClickListener{
 
 	public static final String NAME = "HomeFragment";
 	
 	public static final int INDEX_POSTED = 0;
 	public static final int INDEX_LIMITED = 1;
 	public static final int INDEX_DELETED = 2;
 	public static final int INDEX_FAVORITE = 3;
 	public static final int INDEX_MESSAGE = 4;
 	public static final int INDEX_HISTORY = 5;
 	public static final int INDEX_SETTING = 6;	
 	
 	public int postNum = 0;
 	public int limitedNum = 0;
 	public int deletedNum = 0;
 	public int favoriteNum = 0;
 	public int unreadMessageNum = 0;
 	public int historyNum = 0;
 
     public static final int MSG_GETPERSONALPROFILE = 99;
     public static final int MSG_EDIT_USERNAME_SUCCESS = 100;
     public static final int MSG_SHOW_TOAST = 101;
     public static final int MSG_SHOW_PROGRESS = 102;
 
 	protected void initTitle(TitleDef title) {
 		LayoutInflater inflator = LayoutInflater.from(getActivity());
 		title.m_titleControls = inflator.inflate(R.layout.title_home, null);
 
 		title.hasGlobalSearch = true;
 		
 		View logoRoot = title.m_titleControls.findViewById(R.id.logo_root);
 		
 		logoRoot.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
 			}
 		});
 	}
 	
 	public void onAddTitleControl(View titleControl)
 	{
 		View logoRoot = titleControl.findViewById(R.id.logo_root);
 		if (logoRoot != null)
 		{
 			logoRoot.setPadding(logoRoot.getPaddingLeft(), 0, logoRoot.getPaddingRight(), 0); //Fix padding issue for nine-patch.
 		}
 	}
 
 	public void initTab(TabDef tab){
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MAINPAGE;
 	}
 
 	@Override
 	public int[] includedOptionMenus() {
 		return new int[]{OPTION_CHANGE_CITY};
 	}
 	
 	@Override
 	public void handleRightAction(){
 		this.pushFragment(new GridCateFragment(), this.getArguments());
 	}
 	
 	@Override
 	public void handleSearch() {
 		this.pushFragment(new SearchFragment(), this.getArguments());
 	};
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 //		this.pv = PV.HOME;
 	}
 	
 	public boolean hasGlobalTab()
 	{
 		return true;
 	}
 	
 	private List<WeakReference<Bitmap> > bmpCaches = new ArrayList<WeakReference<Bitmap> >();
 	
 	private void setViewContent(){
 		if(getView() == null) return;
 		int []icons 	= {R.drawable.icon_category_wupinjiaoyi, R.drawable.icon_category_car, 		R.drawable.icon_category_house, 	R.drawable.icon_category_quanzhi, 
 				   R.drawable.icon_category_jianzhi,     R.drawable.icon_category_vita, 	R.drawable.icon_category_friend, 	R.drawable.icon_category_pet,
 				   R.drawable.icon_category_service,     R.drawable.icon_category_education};
 		String []texts 	= {"物品交易", "车辆买卖", "房屋租售", "全职招聘", 
 						   "兼职招聘", "求职简历", "交友活动", "宠物", 
 						   "生活服务", "教育培训"};
 
 		List<GridInfo> gitems = new ArrayList<GridInfo>();
 		for (int i = 0; i < icons.length; i++)
 		{
 			GridInfo gi = new GridInfo();
 			if(bmpCaches.size() <= i){
 				Bitmap bmp = ((BitmapDrawable)getResources().getDrawable(icons[i])).getBitmap(); 
 				bmpCaches.add(new WeakReference<Bitmap>(bmp));
 			}
 			if(bmpCaches.get(i).get() == null){
 				bmpCaches.set(i, new WeakReference<Bitmap>(((BitmapDrawable)getResources().getDrawable(icons[i])).getBitmap()));
 				Log.d("shit", "bitmap null");
 			}
 			gi.img = bmpCaches.get(i).get();
 			gi.text = texts[i];
 //			gi.resId = icons[i];
 			gitems.add(gi);
 		}
 
 		CustomizeGridView gv = (CustomizeGridView) getView().findViewById(R.id.gridcategory);
 		gv.setData(gitems, 3);
 		gv.setItemClickListener(this);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		logCreateView(savedInstanceState);
 
 		View v = inflater.inflate(R.layout.homepageview, null);
 
 //		pageMgr.attachView(v, this, this);
 		
 		return v;
 	}
 	@Override
 	public void onStackTop(boolean isBack) {
 //		//Mobile Track Config入口
 //		TrackConfig.getInstance().getConfig();//获取config
		Tracker.getInstance().pv(this.pv).end();
 		
 		String cityName = QuanleimuApplication.getApplication().getCityName();
 		if (null == cityName || "".equals(cityName)) {
 			this.pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
 		}else
 		{
 			TextView titleLabel = (TextView) getTitleDef().m_titleControls.findViewById(R.id.title_label_city);
 			titleLabel.setText(QuanleimuApplication.getApplication().getCityName());			
 		}
 	}
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 		switch (msg.what) {
 			case 1:
 				break;
 			case 2:
 				hideProgress();
 				break;
 			case 4:
 				hideProgress();
 				ViewUtil.postShortToastMessage(rootView, "网络连接失败，请检查设置！", 0);
 				break;
 	        case MSG_USER_LOGIN:
 	        	getView().findViewById(R.id.userInfoLayout).setVisibility(View.VISIBLE);
 	            break;
 	        case MSG_SHOW_TOAST:
 	            hideProgress();
 	            ViewUtil.postShortToastMessage(rootView, msg.obj.toString(), 0);
 	            break;
 	        case MSG_SHOW_PROGRESS:
 	            showProgress(R.string.dialog_title_info, R.string.dialog_message_updating, true);
 	            break;
 		}
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 	}
 	
 	private boolean isActivated = true;
 	@Override
 	public void onResume(){
 		super.onResume();
 //		isActivated = true;
 		synchronized(HomeFragment.this){
 			setViewContent();
 		}
 		Tracker.getInstance().pv(PV.HOME).end();
 	}
 	
 	@Override
 	public void onPause(){
 //		LocationService.getInstance().removeLocationListener(this);
 		super.onPause();
 //		isActivated = false;
 //		final List<Bitmap> tmp = new ArrayList<Bitmap>();
 //		tmp.addAll(bmpCaches);
 //		bmpCaches.clear();
 //		if(this.getView() != null){
 //			getView().postDelayed(new Runnable(){
 //				@Override
 //				public void run(){
 //					synchronized(HomeFragment.this){
 ////						if(!isActivated){
 //							for(int i = 0; i < tmp.size(); ++ i){
 //								tmp.get(i).recycle();
 //							}
 //							tmp.clear();
 ////						}
 //					}
 //				}
 //			}, 2000);
 //		}
 //		if(glDetail != null){
 //			glDetail.setAdapter(null);
 //		}
 	}
 
 	@Override
 	public void onItemClick(GridInfo info, int index) {	
 		List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
 				.getListFirst();
 		if (allCates == null || allCates.size() == 0)
 			return;
 		if (info == null)
 			return;
 		
 		FirstStepCate cate = allCates.get(index);
 		Bundle bundle = new Bundle();
 		bundle.putInt(ARG_COMMON_REQ_CODE, this.requestCode);
 		bundle.putSerializable("cates", cate);
 		bundle.putBoolean("isPost", false);
 		pushFragment(new SecondCateFragment(), bundle);
 	}
 
 	public int getEnterAnimation()
 	{
 		return 0;
 	}
 	
 	public int getExitAnimation()
 	{
 		return 0;
 	}
 
 }
 
 
