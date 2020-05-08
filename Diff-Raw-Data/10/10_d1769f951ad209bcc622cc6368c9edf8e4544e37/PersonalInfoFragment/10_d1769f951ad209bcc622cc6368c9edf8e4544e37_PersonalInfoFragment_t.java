 package com.quanleimu.view.fragment;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.BaseFragment.TabDef;
 import com.quanleimu.activity.BaseFragment.TitleDef;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.broadcast.CommonIntentAction;
 import com.quanleimu.database.ChatMessageDatabase;
 import com.quanleimu.entity.ChatMessage;
 import com.quanleimu.entity.ChatSession;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.entity.UserProfile;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.LoginUtil;
 import com.quanleimu.util.Util;
 import com.quanleimu.view.PersonalCenterEntryView;
 
 public class PersonalInfoFragment extends BaseFragment implements View.OnClickListener, LoginUtil.LoginListener {
 
 	public static final int REQ_EDIT_PROFILE = 1;
 	public static final int REQ_REGISTER = 2;
 	
 	private Bundle bundle = null;
 	private UserBean user = null;
 	private String json = null;
 	private String upJson = null;
 	private String locationJson = null;
 	private String sessionsJson = null;
 	static final int MSG_GETPERSONALADS = 1;
 	static final int MSG_GETPERSONALPROFILE = 2;
 	static final int MSG_GETPERSONALLOCATION = 3;
 	static final int MSG_GETPERSONALSESSIONS = 4;
 	static final int MSG_LOGINSUCCESS = 5;
 	static final int MSG_LOGINFAIL = 6;
 	static final int MSG_NEWREGISTERVIEW = 7;
 	static final int MSG_FORGETPASSWORDVIEW = 8;
 	
 	private List<ChatSession> sessions = null;
 	private UserProfile up = null;
 	private BroadcastReceiver chatMessageReceiver;
 	private LoginUtil loginHelper;
 	
 	@Override
 	public void onLoginFail(String message){
 		sendMessage(MSG_LOGINFAIL, message);
 	}
 	public void onLoginSucceed(String message){
 		sendMessage(MSG_LOGINSUCCESS, message);		
 	}
 	public void onRegisterClicked(){
 		sendMessage(MSG_NEWREGISTERVIEW, null);
 	}
 	public void onForgetClicked(){
 		sendMessage(MSG_FORGETPASSWORDVIEW, null);
 	}
 	@Override
 	public void initTitle(TitleDef title) {
 //		title.m_leftActionHint = "设置";
 //		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 		title.m_title = "用户中心";
 //		title.m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_CUSTOM;
 //		title.m_rightActionHint = "";
 //		title.rightCustomResourceId = R.drawable.btn_refresh;
 		title.m_visible = true;
 	}
 
 	@Override
 	public void initTab(TabDef tab) {
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
 	}
 	
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.bundle = this.getArguments();
 		if (savedInstanceState != null)
 		{
 			Log.e(TAG, "check if arguments is auto saved ? restore:" + this.getArguments());
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		logCreateView(savedInstanceState);
 		if (savedInstanceState != null)
 		{
 			Log.d(TAG, "recreate view from saved data." + this.getClass().getName());
 		}
 
 		View v = inflater.inflate(R.layout.personalentryview, null);
 		v.findViewById(R.id.rl_wofav).setOnClickListener(this);
 		v.findViewById(R.id.rl_wohistory).setOnClickListener(this);
 		v.findViewById(R.id.rl_wosent).setOnClickListener(this);
 		v.findViewById(R.id.rl_woprivatemsg).setOnClickListener(this);		
 		v.findViewById(R.id.personalEdit).setOnClickListener(this);
 		this.loginHelper = null;
 		return v;
 	}
 	
 	private void switchLayoutOnLogin(boolean logined){
 		View root = getView();
 		if(logined){
 			root.findViewById(R.id.rl_login).setVisibility(View.GONE);
 			root.findViewById(R.id.rl_profile).setVisibility(View.VISIBLE);
 			TitleDef title = this.getTitleDef();//new TitleDef();
 			title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 			title.m_title = "用户中心";
 			title.m_leftActionHint="注销";
 			title.m_rightActionHint="设置";
 			root.findViewById(R.id.profile_background).setVisibility(View.VISIBLE);
 			root.findViewById(R.id.seperator_login).setVisibility(View.GONE);
 //			m_viewInfoListener.onTitleChanged(title);
 			refreshHeader();
 		}else{
 			if(loginHelper == null){
 				loginHelper = new LoginUtil(root.findViewById(R.id.rl_login), this);
 			}
 			root.findViewById(R.id.rl_login).setVisibility(View.VISIBLE);
 			root.findViewById(R.id.rl_profile).setVisibility(View.GONE);
 			TitleDef title = this.getTitleDef();
 			title.m_leftActionHint="";
 			title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 			title.m_title = "用户中心";
 			title.m_rightActionHint="设置";
 //			m_viewInfoListener.onTitleChanged(title);
 			refreshHeader();
 			root.findViewById(R.id.profile_background).setVisibility(View.GONE);
 			root.findViewById(R.id.seperator_login).setVisibility(View.VISIBLE);
 		}
 
 	}
 	
 	
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		unregisterReceiver();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		refreshUI(getView());
 		
 		
 		registerReceiver();
 	}
 	
 	private void registerReceiver()
 	{
 		if (chatMessageReceiver == null)
 		{
 			chatMessageReceiver = new BroadcastReceiver() {
 
 				public void onReceive(Context outerContext, Intent outerIntent) {
 					View v = getView();
 					if (v != null)
 					{
 						updateMessageCountInfo(v);
 					}
 					if (outerIntent != null && outerIntent.hasExtra(CommonIntentAction.EXTRA_MSG_MESSAGE))
 					{
 						ChatMessage msg = (ChatMessage) outerIntent.getSerializableExtra(CommonIntentAction.EXTRA_MSG_MESSAGE);
 						if (!hasSession(msg.getSession()))
 						{
 							new Thread(new GetPersonalSessionsThread()).start();
 						}
 					}
 				}
 			};
 		}
 		
 		getActivity().registerReceiver(chatMessageReceiver, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_NEW_MSG));
 	}
 	
 	private void unregisterReceiver()
 	{
 		if (chatMessageReceiver != null)
 		{
 			getActivity().unregisterReceiver(chatMessageReceiver);
 		}
 	}
 	
 	private boolean hasSession(String sessionId)
 	{
 		if (this.sessions == null || this.sessions.size() == 0)
 		{
 			return false;
 		}
 		
 		for (ChatSession session : this.sessions)
 		{
 			if (sessionId.equals(session.getSessionId()))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 
 	private void clearProfile(){
 		Activity activity = getActivity();
 		((TextView)activity.findViewById(R.id.personalNick)).setText("");
 		((ImageView)activity.findViewById(R.id.personalGenderImage)).setImageDrawable(null);
 		((ImageView)activity.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 		((TextView)activity.findViewById(R.id.personalLocation)).setText("");
 		((TextView)activity.findViewById(R.id.personalRegisterTime)).setText("");
 	}
 	
 	
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.rl_wofav:
 			if(QuanleimuApplication.getApplication().getListMyStore() != null 
 				&& QuanleimuApplication.getApplication().getListMyStore().size() > 0){
 				Bundle bundle = createArguments(null, null);
 				bundle.putBoolean("isFav", true);
 				pushFragment(new FavoriteAndHistoryFragment(), bundle);
 			}
 			break;
 		case R.id.rl_wohistory:
 			if(QuanleimuApplication.getApplication().getListLookHistory() != null
 				&& QuanleimuApplication.getApplication().getListLookHistory().size() > 0){
 				Bundle bundle = createArguments(null, null);
 				bundle.putBoolean("isFav", false);
 				pushFragment(new FavoriteAndHistoryFragment(), bundle);
 			}
 			break;
 		case R.id.rl_wosent:
 			if(user == null){
 //				Bundle bundle = createArguments(null, "用户中心");
 //				pushFragment(new LoginFragment(), bundle);
 			}else{
 				pushFragment(new PersonalPostFragment(), null);
 			}			
 			break;
 		case R.id.rl_woprivatemsg:
 			if(user == null){
 //				Bundle bundle = createArguments(null, "用户中心");
 //				pushFragment(new LoginFragment(), bundle);
 			}else{
 				Bundle bundle = createArguments(null, null);
 				ArrayList tmpList = new ArrayList();
 				tmpList.addAll(this.sessions);
 				bundle.putSerializable("sessions", tmpList);
 				pushFragment(new SessionListFragment(), bundle);
 			}						
 			break;
 		case R.id.personalEdit:
 			if(user == null){
 				Bundle bundle = createArguments(null,  "用户中心");
 //				bundle.putString("backPageName", "用户中心");
 //				bundle.putInt(ARG_COMMON_REQ_CODE, REQ_REGISTER);
 				pushFragment(new LoginFragment(), bundle);
 			}else if (up != null){
 				Bundle bundle = createArguments(null, null);
 				bundle.putInt(ARG_COMMON_REQ_CODE, REQ_EDIT_PROFILE);
 				bundle.putSerializable("profile", up);
 				if(null != ((TextView)getView().findViewById(R.id.personalLocation)).getText()){
 					bundle.putSerializable("cityName", 
 							((TextView)getView().findViewById(R.id.personalLocation)).getText().toString());
 				}
 				pushFragment(new ProfileEditFragment(), bundle);
 			}	
 			break;
 		default:
 			break;
 		}
 	}
 	
 	
 	@Override
 	protected void onFragmentBackWithData(int requestCode, Object result) {
 		if (requestCode == REQ_EDIT_PROFILE && result != null)
 		{
 			forceUpdate();
 		}else if(REQ_REGISTER == requestCode && result != null){
 //			forceUpdate();
 		}
 	}
 	
 	private void forceUpdate()
 	{
 		showProgress("提示", "正在下载数据，请稍候...", true);
 		
 		new Thread(new GetPersonalAdsThread()).start();
 		new Thread(new GetPersonalProfileThread()).start();
 		new Thread(new GetPersonalSessionsThread()).start();
 	}
 
 
 
 
 	class GetPersonalAdsThread implements Runnable {
 		@Override
 		public void run() {
 			String apiName = "ad_list";
 			ArrayList<String> list = new ArrayList<String>();
 			 
 			list.add("query=userId:" + user.getId() + " AND status:0");
 			list.add("activeOnly=0");
 			list.add("start=0");
 			list.add("rt=1");
 			list.add("rows=1000");
 			
 			if(bundle != null && bundle.getString("lastPost") != null){
 				list.add("newAdIds=" + bundle.getString("lastPost"));
 			}
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, false);
 				sendMessage(MSG_GETPERSONALADS, null);
 				return;
 			} catch (UnsupportedEncodingException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (IOException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (Communication.BXHttpException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			}
 			
 			hideProgress();
 		}
 	}
 	
 
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		switch (msg.what) {
 		case MSG_FORGETPASSWORDVIEW:
 			pushFragment(new ForgetPassFragment(), createArguments(null, null));
 			break;
 		case MSG_NEWREGISTERVIEW:
 			Bundle bundle = createArguments(null, null);
 			bundle.putInt(ARG_COMMON_REQ_CODE, REQ_REGISTER);
 			pushFragment(new RegisterFragment(), bundle); //FIXME:
 //			m_viewInfoListener.onNewView(new RegisterView(PersonalCenterEntryView.this.getContext()));
 			break;
 		case MSG_LOGINSUCCESS:
 			if(msg.obj != null && msg.obj instanceof String){
 				Toast.makeText(activity, (String)msg.obj, 0).show();
 			}else{
 				Toast.makeText(activity, "登陆成功", 0).show();
 			}
 			if (rootView != null)
 			{
 				refreshUI(rootView);
 			}
 			break;				
 		case MSG_LOGINFAIL:
 			if(msg.obj != null && msg.obj instanceof String){
 				Toast.makeText(getContext(), (String)msg.obj, 0).show();
 			}else{
 				Toast.makeText(getContext(), "登录未成功，请稍后重试！", 0).show();
 			}
 			break;
 		case MSG_GETPERSONALADS:
 			hideProgress();
 			
 			if (json != null) {
 				GoodsList gl = JsonUtil.getGoodsListFromJson(json);
 				
 				List<GoodsDetail> listMyPost = gl.getData();
 				if(listMyPost != null){
 					for(int i = listMyPost.size() - 1; i >= 0; -- i){
 						if(!listMyPost.get(i).getValueByKey("status").equals("0")){
 							listMyPost.remove(i);
 						}
 					}
 				}
 				if(getActivity() != null)
 				{
 					TextView tvPersonalAds = (TextView) getActivity().findViewById(R.id.tv_sentcount);
 					tvPersonalAds.setText(String.valueOf((listMyPost == null) ? 0 : listMyPost.size()));
 				}
 				QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 			}
 			break;
 		case MSG_GETPERSONALPROFILE:
 			if(upJson != null){
 				up = UserProfile.from(upJson);
 				if (getActivity() != null)
 				{
 					Util.saveDataToLocate(getActivity(), "userProfile", up);
 					if(up != null){
 						fillProfile(up);
 					}
 				}
 			}
 			break;
 		case MSG_GETPERSONALLOCATION:
 			if(locationJson != null){
 				try{
 					JSONArray metaAry = new JSONArray(locationJson);
 					if(metaAry != null && metaAry.length() > 0){
 						JSONObject meta = metaAry.getJSONObject(0);
 						if(meta != null){
 							if(meta.has("displayName")){
 								String location = meta.getString("displayName");
 								if(location != null){
 									((TextView)getActivity().findViewById(R.id.personalLocation)).setText(location);
 								}
 							}								
 						}
 					}
 				}catch(JSONException e){
 					e.printStackTrace();
 				}
 			}
 			break;
 		case MSG_GETPERSONALSESSIONS:
			if(this.sessions != null){
 				updateMessageCountInfo(rootView);
 			}
 			break;
 		}
 	
 	}
 
 	@Override
 	public boolean handleBack() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
 		builder.setTitle("提示:")
 				.setMessage("确定注销？")
 				.setNegativeButton("取消", new DialogInterface.OnClickListener(){
 					@Override
 					public void onClick(DialogInterface dialog, int which){
 						dialog.dismiss();
 					}
 				})
 				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
 					@Override
 					public void onClick(DialogInterface dialog, int which){
 						Util.clearData(getContext(), "user");
 						Util.clearData(getContext(), "userProfile");
 						Util.logout();
 						if(bundle != null){
 							bundle.remove("lastPost");
 						}
 						QuanleimuApplication.getApplication().setListMyPost(null);
 						refreshUI(getView());
 					}					
 				});
 		builder.show();
 		
 		return true;
 	}
 	
 	private void refreshUI (View rootView)
 	{
 		Activity activity = this.getActivity();
 		user = (UserBean) Util.loadDataFromLocate(activity, "user");
 		up = (UserProfile) Util.loadDataFromLocate(activity, "userProfile");
 		List<GoodsDetail> history = QuanleimuApplication.getApplication().getListLookHistory();
 		TextView tvHistory = (TextView)activity.findViewById(R.id.tv_historycount);
 		tvHistory.setText(String.valueOf(history == null ? 0 : history.size()));
 		
 		List<GoodsDetail> favs = QuanleimuApplication.getApplication().getListMyStore();
 		TextView tvFav = (TextView)activity.findViewById(R.id.tv_favcount);
 		tvFav.setText(String.valueOf(favs == null ? 0 : favs.size()));
 
 		if(user != null && ((this.bundle != null && bundle.getInt("forceUpdate") == 1)
 			|| QuanleimuApplication.getApplication().getListMyPost() == null)){
 			((TextView)activity.findViewById(R.id.btn_editprofile)).setText("编辑");
 			if (bundle != null) {
 				bundle.remove("forceUpdate");
 			}
 			forceUpdate();
 			switchLayoutOnLogin(true);
 		}
 		else{
 			TextView tvPersonalAds = (TextView)activity.findViewById(R.id.tv_sentcount);
 			tvPersonalAds.setText(String.valueOf(QuanleimuApplication.getApplication().getListMyPost() == null ?
 					0 : QuanleimuApplication.getApplication().getListMyPost().size()));		
 			if(user == null){
 			    ((TextView)activity.findViewById(R.id.btn_editprofile)).setText("登陆");
 				clearProfile();
 				((TextView)rootView.findViewById(R.id.tv_buzzcount)).setText("未登陆");
 				tvPersonalAds.setText("未登陆");
 				switchLayoutOnLogin(false);
 			}else{
 				switchLayoutOnLogin(true);
 				((TextView)rootView.findViewById(R.id.btn_editprofile)).setText("编辑");
 				if(up == null || (up.createTime.equals(""))){
 					new Thread(new GetPersonalProfileThread()).start();
 				}
 				else{
 					this.fillProfile(up);
 				}
 				if(this.sessions == null){
 					((TextView)activity.findViewById(R.id.tv_buzzcount)).setText("0");
 					new Thread(new GetPersonalSessionsThread()).start();
 				}else{
 //					((TextView)this.findViewById(R.id.tv_buzzcount)).setText(String.valueOf(sessions.size()));
 					updateMessageCountInfo(rootView);
 				}
 			}
 		}
 	}
 	
 	
 
 	
 	@Override
 	public void handleRightAction() {
 		pushFragment(new SetMainFragment(), null);
 	}
 	private void updateMessageCountInfo(View rootView)
 	{
 		if (this.sessions != null)
 		{
 			ChatMessageDatabase.prepareDB(getActivity());
 			String count = String.valueOf(ChatMessageDatabase.getUnreadCount(null, Util.getMyId(getContext())));
 			((TextView)rootView.findViewById(R.id.tv_buzzcount)).setText(count + "未读");
 		}else{
 			((TextView)rootView.findViewById(R.id.tv_buzzcount)).setText("0未读");
 		}
 	}
 	
 	class GetLocationThread implements Runnable{
 		public GetLocationThread(String objId){
 			this.objId = objId;
 		}
 		private String objId = "";
 		@Override
 		public void run() {
 			String apiName = "metaobject";
 			ArrayList<String> list = new ArrayList<String>();
 			 
 			list.add("objIds=" + objId);
 			
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				locationJson = Communication.getDataByUrl(url, false);
 				sendMessage(MSG_GETPERSONALLOCATION, null);
 				return;
 			} catch (UnsupportedEncodingException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (IOException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (Communication.BXHttpException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			}
 
 			hideProgress();
 		}		
 	}
 	
 	class GetPersonalProfileThread implements Runnable {
 		@Override
 		public void run() {
 			if (user == null)
 			{
 				return;
 			}
 			upJson = Util.requestUserProfile(user.getId());
 			sendMessage(MSG_GETPERSONALPROFILE, null);
 
 			hideProgress();
 		}
 	}	
 
 	class GetPersonalSessionsThread implements Runnable {
 		@Override
 		public void run() {
 			if (user == null)
 			{
 				return;
 			}
 			
 			String apiName = "read_session";
 			ArrayList<String> list = new ArrayList<String>();
 			 
 			list.add("u_id=" + user.getId());
 			
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				sessionsJson = Communication.getDataByUrl(url, true); //Only load cached data here.
				if(sessionsJson != null){
					sessions = ChatSession.fromJson(sessionsJson);
				}
 				sendMessage(MSG_GETPERSONALSESSIONS, null);
 				return;
 			} catch (UnsupportedEncodingException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (IOException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			} catch (Communication.BXHttpException e) {
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 			}
 
 			hideProgress();
 		}
 	}	
 	
 	
 	private void fillProfile(UserProfile up){
 		
 		if (this.getActivity() == null)
 		{
 			return;
 		}
 		
 		Activity activity = this.getActivity();
 		
 		if(up.nickName != null){
 			((TextView)activity.findViewById(R.id.personalNick)).setText(up.nickName);
 		}else{
 			((TextView)activity.findViewById(R.id.personalNick)).setText("");
 		}
 		boolean showBoy = true;
 		if(up.gender != null && !up.equals("")){
 			if(up.gender.equals("男")){
 				((ImageView)activity.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_male);
 //				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 			}else if(up.gender.equals("女")){
 				((ImageView)activity.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_female);
 				showBoy = false;
 //				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_girl);
 			}
 		}else{
 			((ImageView)activity.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 		}
 		
 		if(up.location != null && !up.location.equals("")){
 			(new Thread(new GetLocationThread(up.location))).start();
 		}else{
 			((TextView)activity.findViewById(R.id.personalLocation)).setText("");
 		}
 		
 		if(up.createTime != null && !up.equals("")){
 			try{
 				Date date = new Date(Long.parseLong(up.createTime) * 1000);
 				SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月", Locale.SIMPLIFIED_CHINESE);
 				((TextView)activity.findViewById(R.id.personalRegisterTime)).setText(df.format(date) + "注册");
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}else{
 			((TextView)activity.findViewById(R.id.personalRegisterTime)).setText("");
 		}
 		String image = null;
 		if(up.resize180Image != null && !up.resize180Image.equals("")){
 			image = up.resize180Image;
 		}
 		if(image != null && !image.equals("") && !image.equals("null")){
 			int height = activity.findViewById(R.id.personalImage).getMeasuredHeight();
 			int width = activity.findViewById(R.id.personalImage).getMeasuredWidth();
 			if(height <= 0 || width <= 0){
 				Drawable img = ((ImageView)activity.findViewById(R.id.personalImage)).getDrawable();
 				if(img != null){
 					height = img.getIntrinsicHeight();
 					width = img.getIntrinsicWidth();
 				}
 			}
 			if(height > 0 && width > 0){
 				ViewGroup.LayoutParams lp = activity.findViewById(R.id.personalImage).getLayoutParams();
 				lp.height = height;
 				lp.width = width;
 				activity.findViewById(R.id.personalImage).setLayoutParams(lp);
 			}
 				
 			SimpleImageLoader.showImg((ImageView)activity.findViewById(R.id.personalImage), 
 					image, null, activity, showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
 		}else{
 			((ImageView)activity.findViewById(R.id.personalImage)).setImageResource(showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
 		}
 	}
 	
 }
