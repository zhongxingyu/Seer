 package com.quanleimu.view;
 
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
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.broadcast.CommonIntentAction;
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
 import com.quanleimu.util.Util;
 
 public class PersonalCenterEntryView extends BaseView implements
 		View.OnClickListener {
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
 	private List<ChatSession> sessions = null;
 	private UserProfile up = null;
 	private BroadcastReceiver chatMessageReceiver;
 
 	public PersonalCenterEntryView(Context context, Bundle bundle) {
 		super(context);
 		this.bundle = bundle;
 		init();
 	}
 
 	private void init() {
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.personalentryview, null);
 		this.addView(v);
 		this.findViewById(R.id.rl_wofav).setOnClickListener(this);
 		this.findViewById(R.id.rl_wohistory).setOnClickListener(this);
 		this.findViewById(R.id.rl_wosent).setOnClickListener(this);
 		this.findViewById(R.id.rl_woprivatemsg).setOnClickListener(this);		
 		this.findViewById(R.id.personalEdit).setOnClickListener(this);
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		up = (UserProfile) Util.loadDataFromLocate(this.getContext(), "userProfile");
 		List<GoodsDetail> history = QuanleimuApplication.getApplication().getListLookHistory();
 		TextView tvHistory = (TextView)this.findViewById(R.id.tv_historycount);
 		tvHistory.setText(String.valueOf(history == null ? 0 : history.size()));
 		
 		List<GoodsDetail> favs = QuanleimuApplication.getApplication().getListMyStore();
 		TextView tvFav = (TextView)this.findViewById(R.id.tv_favcount);
 		tvFav.setText(String.valueOf(favs == null ? 0 : favs.size()));
 
 		if(user != null && ((this.bundle != null && bundle.getInt("forceUpdate") == 1)
 			|| QuanleimuApplication.getApplication().getListMyPost() == null)){
 			if (bundle != null) {
 				bundle.remove("forceUpdate");
 			}
 			pd = ProgressDialog.show(this.getContext(), "提示", "正在下载数据，请稍候...");
 			pd.setCancelable(true);
 			
 			new Thread(new GetPersonalAdsThread()).start();
 			new Thread(new GetPersonalProfileThread()).start();
 			new Thread(new GetPersonalSessionsThread()).start();
 		}
 		else{
 			TextView tvPersonalAds = (TextView) PersonalCenterEntryView.this.findViewById(R.id.tv_sentcount);
 			tvPersonalAds.setText(String.valueOf(QuanleimuApplication.getApplication().getListMyPost() == null ?
 					0 : QuanleimuApplication.getApplication().getListMyPost().size()));		
 			if(user == null){
 				clearProfile();
 				((TextView)this.findViewById(R.id.tv_buzzcount)).setText("0");
 			}else{
 				if(up == null){
 					new Thread(new GetPersonalProfileThread()).start();
 				}
 				else{
 					this.fillProfile(up);
 				}
 				if(this.sessions == null){
 					((TextView)this.findViewById(R.id.tv_buzzcount)).setText("0");
 					new Thread(new GetPersonalSessionsThread()).start();
 				}else{
 					((TextView)this.findViewById(R.id.tv_buzzcount)).setText(String.valueOf(sessions.size()));
 				}
 			}
 		}
 		
 		registerReceiver();
 	}
 	
 	
 	
 	@Override
 	protected void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		
 		unregisterReceiver();
 	}
 
 	private void registerReceiver()
 	{
 		if (chatMessageReceiver == null)
 		{
 			chatMessageReceiver = new BroadcastReceiver() {
 
 				public void onReceive(Context outerContext, Intent outerIntent) {
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
 		
 		getContext().registerReceiver(chatMessageReceiver, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_NEW_MSG));
 	}
 	
 	private void unregisterReceiver()
 	{
 		if (chatMessageReceiver != null)
 		{
 			getContext().unregisterReceiver(chatMessageReceiver);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.rl_wofav:
 			if(QuanleimuApplication.getApplication().getListMyStore() != null 
 				&& QuanleimuApplication.getApplication().getListMyStore().size() > 0){
 				m_viewInfoListener.onNewView(new FavoriteAndHistoryView(this.getContext(), this.bundle, true));
 			}
 			break;
 		case R.id.rl_wohistory:
 			if(QuanleimuApplication.getApplication().getListLookHistory() != null
 				&& QuanleimuApplication.getApplication().getListLookHistory().size() > 0){
 				m_viewInfoListener.onNewView(new FavoriteAndHistoryView(this.getContext(), this.bundle, false));
 			}
 			break;
 		case R.id.rl_wosent:
 			if(user == null){
 				m_viewInfoListener.onNewView(new LoginView(this.getContext(), "用户中心"));
 			}else{
 				m_viewInfoListener.onNewView(new PersonalPostView(this.getContext(), bundle));
 			}			
 			break;
 		case R.id.rl_woprivatemsg:
 			if(user == null){
 				m_viewInfoListener.onNewView(new LoginView(this.getContext(), "用户中心"));
 			}else{
 				m_viewInfoListener.onNewView(new SessionListView(this.getContext(), this.sessions));
 			}						
 			break;
 		case R.id.personalEdit:
 			if(user == null){
 				m_viewInfoListener.onNewView(new LoginView(this.getContext(), "用户中心"));
 			}else if (up != null){
 				m_viewInfoListener.onNewView(new ProfileEditView(this.getContext(), bundle, up));
 			}	
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public boolean onLeftActionPressed() {
 		m_viewInfoListener.onNewView(new SetMainView(getContext()));
 		return true;
 	}
 
 	@Override
 	public boolean onRightActionPressed(){
		if(user == null) return true;
 		new Thread(new GetPersonalAdsThread()).start();
 		new Thread(new GetPersonalProfileThread()).start();
 		new Thread(new GetPersonalSessionsThread()).start();
 		return true;
 	}
 	
 	@Override
 	public TitleDef getTitleDef() {
 		TitleDef title = new TitleDef();
 		title.m_leftActionHint = "设置";
 		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 		title.m_title = "用户中心";
 		title.m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_CUSTOM;
 		title.m_rightActionHint = "";
 		title.rightCustomResourceId = R.drawable.btn_refresh;
 		title.m_visible = true;
 		return title;
 	}
 
 	@Override
 	public TabDef getTabDef() {
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
 		return tab;
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
 		((TextView)this.findViewById(R.id.personalNick)).setText("");
 		((ImageView)this.findViewById(R.id.personalGenderImage)).setImageDrawable(null);
 		((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 		((TextView)PersonalCenterEntryView.this.findViewById(R.id.personalLocation)).setText("");
 		((TextView)this.findViewById(R.id.personalRegisterTime)).setText("");
 	}
 	
 
 	private void fillProfile(UserProfile up){
 		if(up.nickName != null){
 			((TextView)this.findViewById(R.id.personalNick)).setText(up.nickName);
 		}else{
 			((TextView)this.findViewById(R.id.personalNick)).setText("");
 		}
 		boolean showBoy = true;
 		if(up.gender != null && !up.equals("")){
 			if(up.gender.equals("男")){
 				((ImageView)this.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_male);
 //				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 			}else if(up.gender.equals("女")){
 				((ImageView)this.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_female);
 				showBoy = false;
 //				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_girl);
 			}
 		}else{
 			((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 		}
 		
 		if(up.location != null && !up.location.equals("")){
 			(new Thread(new GetLocationThread(up.location))).start();
 		}else{
 			((TextView)PersonalCenterEntryView.this.findViewById(R.id.personalLocation)).setText("");
 		}
 		
 		if(up.createTime != null && !up.equals("")){
 			try{
 				Date date = new Date(Long.parseLong(up.createTime) * 1000);
 				SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月", Locale.SIMPLIFIED_CHINESE);
 				((TextView)this.findViewById(R.id.personalRegisterTime)).setText(df.format(date) + "注册");
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}else{
 			((TextView)this.findViewById(R.id.personalRegisterTime)).setText("");
 		}
 		String image = null;
 		if(up.resize180Image != null && !up.resize180Image.equals("")){
 			image = up.resize180Image;
 		}
 		if(image != null && !image.equals("") && !image.equals("null")){
			int height = this.findViewById(R.id.personalImage).getMeasuredHeight();
			int width = this.findViewById(R.id.personalImage).getMeasuredWidth();
			if(height <= 0 || width <= 0){
				Drawable img = ((ImageView)this.findViewById(R.id.personalImage)).getDrawable();
				if(img != null){
					height = img.getIntrinsicHeight();
					width = img.getIntrinsicWidth();
				}
			}
			if(height > 0 && width > 0){
				ViewGroup.LayoutParams lp = this.findViewById(R.id.personalImage).getLayoutParams();
				lp.height = height;
				lp.width = width;
				this.findViewById(R.id.personalImage).setLayoutParams(lp);
			}
				
 			SimpleImageLoader.showImg((ImageView)this.findViewById(R.id.personalImage), 
 					image, this.getContext(), showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
 		}else{
 			((ImageView)this.findViewById(R.id.personalImage)).setImageResource(showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
 		}
 	}
 
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_GETPERSONALADS:
 				if (pd != null) {
 					pd.dismiss();
 				}
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
 					TextView tvPersonalAds = (TextView) PersonalCenterEntryView.this.findViewById(R.id.tv_sentcount);
 					tvPersonalAds.setText(String.valueOf((listMyPost == null) ? 0 : listMyPost.size()));
 					QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 				}
 				break;
 			case MSG_GETPERSONALPROFILE:
 				if(upJson != null){
 					up = UserProfile.from(upJson);
 					Util.saveDataToLocate(PersonalCenterEntryView.this.getContext(), "userProfile", up);
 					if(up != null){
 						fillProfile(up);
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
 										((TextView)PersonalCenterEntryView.this.findViewById(R.id.personalLocation)).setText(location);
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
 				if(sessionsJson != null){
 					sessions = ChatSession.fromJson(sessionsJson);
 					if(sessions != null){
 						((TextView)PersonalCenterEntryView.this.findViewById(R.id.tv_buzzcount)).setText(String.valueOf(sessions.size()));
 					}
 				}
 				break;
 			}
 		}
 	};
 
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
 				myHandler.sendEmptyMessage(MSG_GETPERSONALADS);
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
 			
 			if(pd != null){
 				pd.dismiss();
 			}
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
 				myHandler.sendEmptyMessage(MSG_GETPERSONALLOCATION);
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
 			
 			if(pd != null){
 				pd.dismiss();
 			}
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
 			myHandler.sendEmptyMessage(MSG_GETPERSONALPROFILE);
 			
 			if(pd != null){
 				pd.dismiss();
 			}
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
 				sessionsJson = Communication.getDataByUrl(url, true);
 				myHandler.sendEmptyMessage(MSG_GETPERSONALSESSIONS);
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
 			
 			if(pd != null){
 				pd.dismiss();
 			}
 		}
 	}	
 }
