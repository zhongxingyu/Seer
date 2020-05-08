 package com.quanleimu.view.fragment;
 
 import java.io.BufferedOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.GridAdapter;
 import com.quanleimu.entity.ChatSession;
 import com.quanleimu.entity.FirstStepCate;
 import com.quanleimu.entity.HotList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.entity.UserProfile;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.TrackConfig;
 import com.quanleimu.util.Util;
 import com.quanleimu.view.CategorySelectionView;
 import com.quanleimu.view.CustomizePagerManager;
 import com.quanleimu.view.CustomizePagerManager.PageProvider;
 import com.quanleimu.view.CustomizePagerManager.PageSelectListener;
 import com.quanleimu.widget.CustomizeGridView;
 import com.quanleimu.widget.CustomizeGridView.GridInfo;
 import com.quanleimu.widget.CustomizeGridView.ItemClickListener;
 
 public class HomeFragment extends BaseFragment implements PageProvider, PageSelectListener, ItemClickListener , View.OnClickListener{
 
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
 	
 	public static final String[] TAB_LABELS = new String[] {
 		"浏览信息", "我的百姓网"
 	};
 
 	private CustomizePagerManager pageMgr;
 	private int selectedIndex = 0;
 
     private UserBean user;
     private String userProfileJson;
     static final int MSG_GETPERSONALPROFILE = 99;
 
 	private String json;
 	private List<HotList> tempListHot = new ArrayList<HotList>();
 	private List<Boolean> tempUpdated = new ArrayList<Boolean>();
 
 	protected void initTitle(TitleDef title) {
 		LayoutInflater inflator = LayoutInflater.from(getActivity());
 		title.m_titleControls = inflator.inflate(R.layout.title_home, null);
 
 		title.hasGlobalSearch = true;
 		title.m_rightActionHint = "发布";
 		title.m_rightActionBg = R.drawable.bg_post_selector;
 		
 		View logoRoot = title.m_titleControls.findViewById(R.id.logo_root);
 		
 		logoRoot.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
 			}
 		});
		logoRoot.setPadding(logoRoot.getPaddingLeft(), logoRoot.getPaddingBottom(), logoRoot.getPaddingRight(), logoRoot.getBottom()); //Fix padding issue for nine-patch.
 	}
 	
 	public void onAddTitleControl(View titleControl)
 	{
 		View logoRoot = titleControl.findViewById(R.id.logo_root);
 		if (logoRoot != null)
 		{
			logoRoot.setPadding(logoRoot.getPaddingLeft(), logoRoot.getPaddingBottom(), logoRoot.getPaddingRight(), logoRoot.getBottom()); //Fix padding issue for nine-patch.
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
 //		BxTracker.getInstance().createPageLogData("", "").appendProperty("", "").appendProperty("", "");
 	}
 	
 	@Override
 	public void handleSearch() {
 		this.pushFragment(new SearchFragment(), this.getArguments());
 	};
 	
 	
 //	@Override
 //	protected int getFirstRunId() {
 //		return R.layout.first_run_main;
 //	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle bundle = this.getArguments();
 		if(bundle != null && bundle.containsKey("defaultPageIndex")){
 			selectedIndex = bundle.getInt("defaultPageIndex");
 		}
 		pageMgr = CustomizePagerManager.createManager(TAB_LABELS, selectedIndex);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		logCreateView(savedInstanceState);
 
 		View v = inflater.inflate(R.layout.homepageview, null);
 
 		pageMgr.attachView(v, this, this);
 		
 		return v;
 		
 		/*
 		LinearLayout hotlistView = (LinearLayout)inflater.inflate(R.layout.hotlist, null);
 		rlHotList = (RelativeLayout)hotlistView.findViewById(R.id.rlHotList);
 		glDetail = (ViewFlow) hotlistView.findViewById(R.id.glDetail);
 		glDetail.setFadingEdgeLength(10);
 		indicator = (CircleFlowIndicator) hotlistView.findViewById(R.id.viewflowindic);
 		glDetail.setFlowIndicator(indicator);
 
 		glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				if (listHot.get(arg2).getType() == 0) {
 					Bundle bundle = createArguments(listHot.get(arg2).getHotData().getTitle(), "首页");
 					bundle.putString("actType", "homepage");
 					bundle.putString("searchContent", (listHot.get(arg2)
 							.getHotData().getKeyword()));
 					
 					pushFragment(new SearchGoodsFragment(), bundle);
 				} else if (listHot.get(arg2).getType() == 1) {
 					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(listHot
 							.get(arg2).getHotData().getWeburl()));
 					startActivity(i);
 				}else if(listHot.get(arg2).getType() == 2){
 					Bundle bundle = createArguments(listHot.get(arg2).getHotData().getTitle(), "首页");
 					bundle.putString("actType", "homepage");
 					bundle.putString("categoryEnglishName", (listHot.get(arg2)
 							.getHotData().getKeyword()));
 					
 					pushFragment(new GetGoodFragment(), bundle);
 				}
 				BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_HOTS_SEND, null);
 			}
 		});
 		
 		listHot = QuanleimuApplication.listHot;
 		if(listHot == null){	
 			//try to load from last-saved hot-list file
 			boolean lastSavedValid = true;
 			try {
 				FileInputStream jsonFile = getActivity().openFileInput("hotlist.json");
 				BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
 				int bytesJson = bufferedStream.available();
 				byte[] json = new byte[bytesJson];
 				int readBytes = bufferedStream.read(json, 0, bytesJson);
 				
 				String jsonRaw = new String(json, 0, readBytes);
 				String jsonDecoded = Communication.decodeUnicode(jsonRaw);
 				listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
 				
 				for(int i = 0; i < listHot.size(); ++i){
 					if(!QuanleimuApplication.getImageLoader().checkWithImmediateIO(listHot.get(i).imgUrl)){
 						lastSavedValid = false;
 					}
 				}
 				
 				if(listHot == null || listHot.size() == 0)	lastSavedValid = false;
 				
 			} catch (FileNotFoundException e) {
 				lastSavedValid = false;
 			}catch(IOException e){
 				lastSavedValid = false;
 			}
 			
 			//if last-saved is not ready, parse from initial hot-list in asset 
 			if(!lastSavedValid){
 				try {
 					InputStream jsonFile = getActivity().getAssets().open("hotlist.json");
 					BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
 					int bytesJson = bufferedStream.available();
 					byte[] json = new byte[bytesJson];
 					int readBytes = bufferedStream.read(json, 0, bytesJson);
 					
 					String jsonDecoded = Communication.decodeUnicode(new String(json, 0, readBytes));
 					listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 
 			if(listHot == null)	listHot = new ArrayList<HotList>();
 			
 //			new Thread(new HotListThread()).start(); 
 		}
 		
 		adapter = new HotListAdapter(getActivity(), 
 				listHot, 
 				tempListHot, 
 				QuanleimuApplication.getImageLoader());
 		glDetail.setAdapter(adapter);
 		
 		final TextView editSearch = (TextView) v.findViewById(R.id.etSearch);
 		
 		v.findViewById(R.id.rlSearch).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Bundle bundle = new Bundle();
 				bundle.putString("backPageName", "首页");
 				bundle.putString("searchContent", editSearch.getText().toString());
 				bundle.putString("actType", "search");
 
 				pushFragment(new SearchFragment(), bundle);
 			}
 		});
 		
 		((TextView)v.findViewById(R.id.tvCityName)).setText(cityName);
 		
 		Log.w(TAG, "do we have view here homeFragmengCreatView ? " + (this.getView() != null));
 		return v;
 		*/
 	}
 	
 	
 	
 	@Override
 	public void onStackTop(boolean isBack) {
 		View v = getView();
 		if (v != null)
 		{
 			CategorySelectionView catesView = (CategorySelectionView)v.findViewById(R.id.cateSelection);
 			if (catesView != null)
 			{
 				catesView.setRootCateList(QuanleimuApplication.getApplication().getListFirst());
 			}
 		}
 //		new Thread(new HotListThread()).start(); 
 		
 		//Mobile Track Config入口
 		TrackConfig.getInstance().getConfig();//获取config
 		
 		String cityName = QuanleimuApplication.getApplication().getCityName();
 		if (null == cityName || "".equals(cityName)) {
 			this.pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
 		}else
 		{
 			TextView titleLabel = (TextView) getTitleDef().m_titleControls.findViewById(R.id.title_label_city);
 			titleLabel.setText(QuanleimuApplication.getApplication().getCityName());			
 		}
 	}
 
 
 
 //	class HotListThread implements Runnable {
 //
 //		@Override
 //		public void run() {
 //			String apiName = "city_hotlist";
 //			ArrayList<String> list = new ArrayList<String>();
 //			list.add("v=_v2");
 //			String url = Communication.getApiUrl(apiName, list);
 //			try {
 //				json = Communication.getDataByUrl(url, true);
 //				if (json != null) {
 ////					myHandler.sendEmptyMessage(1);
 //					sendMessage(1, null);
 //				} else {
 ////					myHandler.sendEmptyMessage(2);
 //					sendMessage(2, null);
 //				}
 //			} catch (UnsupportedEncodingException e) {
 ////				myHandler.sendEmptyMessage(4);
 //				sendMessage(4, null);
 //			} catch (IOException e) {
 ////				myHandler.sendEmptyMessage(4);
 //				sendMessage(4, null);
 //			} catch (Communication.BXHttpException e){
 //				
 //			}
 //
 //		}
 //	}
 	
 	
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		switch (msg.what) {
 		case 1:
 			tempListHot = JsonUtil.parseCityHotFromJson(Communication
 					.decodeUnicode(json));
 			if(tempListHot != null){
 				for(int i = 0; i < tempListHot.size(); ++i){
 					tempUpdated.add(false);
 				}
 
 				QuanleimuApplication.listHot = tempListHot;
 				
 				//save to context data
 				QuanleimuApplication.context.deleteFile("hotlist.json");
 				try {
 					FileOutputStream fo = QuanleimuApplication.context.openFileOutput("hotlist.json", Context.MODE_PRIVATE);
 					if(fo != null){
 						BufferedOutputStream outFileStream = new BufferedOutputStream(fo);
 						if(outFileStream != null){
 							outFileStream.write(json.getBytes(), 0, json.length());
 							outFileStream.flush();
 							outFileStream.close();
 						}
 					}				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}catch(IOException e){
 					e.printStackTrace();
 				}
 			}
 			
 
 			break;
 		case 2:
 			hideProgress();
 			break;
 		case 4:
 			hideProgress();
 			 Toast.makeText(QuanleimuApplication.context, "网络连接失败，请检查设置！", 3).show();
 			//tvInfo.setVisibility(View.VISIBLE);
 			 break;
         case MSG_GETPERSONALPROFILE:
             if(userProfileJson != null){
                 UserProfile up = UserProfile.from(userProfileJson);
                 if (getActivity() != null)
                 {
                     Util.saveDataToLocate(getActivity(), "userProfile", up);
                     if(up != null){
                         fillProfile(up, getView());
                     }
                 }
             }
             break;
         case MSG_USER_LOGOUT:
         	getView().findViewById(R.id.userInfoLayout).setVisibility(View.GONE);
 			break;
         case MSG_USER_LOGIN:
         	getView().findViewById(R.id.userInfoLayout).setVisibility(View.VISIBLE);
 			break;
 		}
 	}
 	
 	@Override
 	public void onDestroy(){
 //		LocationService.getInstance().removeLocationListener(this);
 		super.onDestroy();
 	}
 	
 //	public void logPage(int selectedIndex) {
 //		if (selectedIndex == 0) {
 //			// /home
 //			HashMap<String,String> map = new HashMap<String,String>();
 //			map.put("tracktype", "pageview");
 ////			map.put("city", "shanghai");
 ////			map.put("timestamp", "1234567");
 //			map.put("category", "ershou");
 //			BxTrackData data = new BxTrackData(map);
 //			BxTracker.getInstance().addTrackData(this.getAppContext(), data);
 //		} else if (selectedIndex == 1) {
 //			// /my
 //			
 //		}
 //	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		Log.d("HomeFragment", ""+selectedIndex);
 //		LogPage(selectedIndex);
 //		if(!QuanleimuApplication.getApplication().getCurrentLocation(this)){
 //			LocationService.getInstance().addLocationListener(getContext(), this);
 //		}
 	}
 	
 	@Override
 	public void onPause(){
 //		LocationService.getInstance().removeLocationListener(this);
 		super.onPause();
 //		if(glDetail != null){
 //			glDetail.setAdapter(null);
 //		}
 	}
 
 
 	@Override
 	public void onPageSelect(int index) {
 		selectedIndex = index;
 	}
 
 	@Override
 	public View onCreateView(Context context, int index) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		View v;		
 		if (index == 0)
 		{
 			v =  inflater.inflate(R.layout.gridcategory, null);
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
 				gi.imgResourceId = icons[i];
 				gi.text = texts[i];
 				gitems.add(gi);
 			}
 
 //			GridAdapter adapter = new GridAdapter(this.getActivity());
 //			adapter.setList(gitems, 3);
 			CustomizeGridView gv = (CustomizeGridView) v.findViewById(R.id.gridcategory);
 			gv.setData(gitems, 3);
 			gv.setItemClickListener(this);
 //			gv.setAdapter(adapter);
 //			gv.setOnItemClickListener(this);
 		}
 		else
 		{
 			v = inflater.inflate(R.layout.persongridfactory, null);
 			int []icons 	= {R.drawable.icon_my_posted, R.drawable.icon_my_limited, 		R.drawable.icon_my_deleted, 	R.drawable.icon_my_fav, 
 							   R.drawable.icon_my_mail,     R.drawable.icon_my_history, 	R.drawable.icon_my_setting};
 							   
 			String []texts 	= {"已发布", "审核未通过", "已删除", "收藏", 
 							   "私信", "最近浏览", "设置"};
 			
 			int []numbers = {postNum, limitedNum, deletedNum, favoriteNum, 
 							unreadMessageNum, historyNum, 0};
 			
 			boolean []stars = {false, false, false, false,
 								(unreadMessageNum > 0), false, false};
 			
 			List<GridInfo> gitems = new ArrayList<GridInfo>();
 			for (int i = 0; i < icons.length; i++)
 			{
 				GridInfo gi = new GridInfo();
 				gi.imgResourceId = icons[i];
 				gi.text = texts[i];
 //				gi.number = numbers[i]; //数字不用加
 				gi.starred = stars[i];
 				gitems.add(gi);
 			}
 		
 //			GridAdapter adapter = new GridAdapter(this.getActivity());
 //			adapter.setList(gitems, 3);
 			CustomizeGridView gv = (CustomizeGridView) v.findViewById(R.id.gridcategory);
 			gv.setData(gitems, 3);
 			gv.setItemClickListener(this);
 //			gv.setAdapter(adapter);
 //			gv.setOnItemClickListener(this);
 
             //set user profile info view
             user = Util.getCurrentUser();
             if (user != null && user.getPhone() != null && !user.getPhone().equals("")) {
                 UserProfile up = (UserProfile) Util.loadDataFromLocate(context, "userProfile");
                 if (up != null) {
                     fillProfile(up, v);
                 } else {
                     new Thread(new GetPersonalProfileThread()).start();
                 }
             }
 
 		}
 
 		return v;
 	}
 
 	@Override
 	public void onItemClick(GridInfo info, int index) {	
 		if (selectedIndex == 0) // 浏览信息页面
 		{
 			List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
 					.getListFirst();
 			if (allCates == null)
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
 		else // 我的百姓网页面
 		{
 			//TODO 登录判断，talk session 获取
 			switch (index)
 			{
 			case INDEX_POSTED:
                 {
                 	pushPersonalPostFragment(PersonalPostFragment.TYPE_MYPOST);				
                 }
 				break;
 			case INDEX_LIMITED:
 				{
 					pushPersonalPostFragment(PersonalPostFragment.TYPE_INVERIFY);
 				}
 				break;
 			case INDEX_DELETED:
                 {
                 	pushPersonalPostFragment(PersonalPostFragment.TYPE_DELETED);
                 }
                 break;
 			case INDEX_FAVORITE:
 				{
 					Bundle bundle = createArguments(null, null);
 					bundle.putBoolean("isFav", true);
 					pushFragment(new FavoriteAndHistoryFragment(), bundle);					
 				}
 				break;
 			case INDEX_MESSAGE:
 				{
 					Bundle bundle = createArguments(null, null);
 					ArrayList<ChatSession> tmpList = new ArrayList<ChatSession>();
 //					tmpList.addAll(this.sessions); 需要获取 sessions 数据
 					bundle.putSerializable("sessions", tmpList);
 					pushFragment(new SessionListFragment(), bundle);
 				}
 				break;
 			case INDEX_HISTORY:
 				{
 					Bundle bundle = createArguments(null, null);
 					bundle.putBoolean("isFav", false);
 					pushFragment(new FavoriteAndHistoryFragment(), bundle);
 				}
 				break;
 			case INDEX_SETTING:
 				{
 					pushFragment(new SetMainFragment(), null);
 				}
 				break;
 			}
 
 		}
 	}
 
     private void pushPersonalPostFragment(int type) {
 //        if(user == null){
 //            Bundle bundle = createArguments(null, "用户中心");
 //            pushFragment(new LoginFragment(), bundle);
 //        }else{
             Bundle bundle = createArguments(null, null);
             bundle.putInt(PersonalPostFragment.TYPE_KEY, type);
             pushFragment(new PersonalPostFragment(), bundle);
 //        }
     }
     
     private void fillProfile(UserProfile up, View userInfoView){
         View activity = userInfoView;
 
         if(up.nickName != null){
             ((TextView)activity.findViewById(R.id.userInfoNickname)).setText(up.nickName);
         }else{
             ((TextView)activity.findViewById(R.id.userInfoNickname)).setText("");
         }
         boolean showBoy = true;
         // 新版本只保留 nickname
 //        if(up.gender != null && !up.equals("")){
 //            if(up.gender.equals("男")){
 //                ((ImageView)activity.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_male);
 ////				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 //            }else if(up.gender.equals("女")){
 //                ((ImageView)activity.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_female);
 //                showBoy = false;
 ////				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_girl);
 //            }
 //        }else{
 //            ((ImageView)activity.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 //        }
 
 //        if(up.location != null && !up.location.equals("")){
 ////            (new Thread(new GetLocationThread(up.location))).start();
 //            //TODO fixme 开新线程拿什么定位？
 //        }else{
 ////            ((TextView)activity.findViewById(R.id.userInfoLocation)).setText("");
 //        }
 //
         if(up.createTime != null && !up.equals("")){
             try{
                 Date date = new Date(Long.parseLong(up.createTime) * 1000);
                 SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月", Locale.SIMPLIFIED_CHINESE);
                 ((TextView)activity.findViewById(R.id.userInfoJoinDays)).setText(df.format(date) + "");
             }catch(Exception e){
                 e.printStackTrace();
             }
         }else{
             ((TextView)activity.findViewById(R.id.personalRegisterTime)).setText("");
         }
 //        String image = null;
 //        if(up.resize180Image != null && !up.resize180Image.equals("")){
 //            image = up.resize180Image;
 //        }
 //        if(image != null && !image.equals("") && !image.equals("null")){
 ////            int height = activity.findViewById(R.id.userInfoAvatar).getMeasuredHeight();
 ////            int width = activity.findViewById(R.id.userInfoAvatar).getMeasuredWidth();
 ////            if(height <= 0 || width <= 0){
 ////                Drawable img = ((ImageView)activity.findViewById(R.id.userInfoAvatar)).getDrawable();
 ////                if(img != null){
 ////                    height = img.getIntrinsicHeight();
 ////                    width = img.getIntrinsicWidth();
 ////                }
 ////            }
 ////            if(height > 0 && width > 0){
 ////                ViewGroup.LayoutParams lp = activity.findViewById(R.id.userInfoAvatar).getLayoutParams();
 ////                lp.height = height;
 ////                lp.width = width;
 ////                activity.findViewById(R.id.userInfoAvatar).setLayoutParams(lp);
 ////            }
 //
 //            SimpleImageLoader.showImg((ImageView) activity.findViewById(R.id.userInfoAvatar),
 //                    image, null, activity.getContext(), showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
 //        }else{
 //            ((ImageView)activity.findViewById(R.id.userInfoAvatar)).setImageResource(showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
 //        }
         View userInfoLayout = activity.findViewById(R.id.userInfoLayout);
         userInfoLayout.setVisibility(View.VISIBLE);
         userInfoLayout.setOnClickListener(this);
     }
 
     class GetPersonalProfileThread implements Runnable {
         @Override
         public void run() {
             if (user == null)
             {
                 return;
             }
             userProfileJson = Util.requestUserProfile(user.getId());
             sendMessage(MSG_GETPERSONALPROFILE, null);
 
             hideProgress();
         }
     }
 
     @Override
     public void onClick(View v) {
         switch(v.getId()){
             case R.id.userInfoLayout:
                 Bundle bundle = createArguments(null, "修改资料");
                 pushFragment(new ProfileEditFragment(), bundle);
                 break;
             default:
                 break;
         }
     }
 
     //fixme ming 小手机下载更新失败
 
 }
 
 
