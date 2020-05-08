 package com.quanleimu.view;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.ChatMessageAdapter;
 import com.quanleimu.broadcast.CommonIntentAction;
 import com.quanleimu.database.ChatMessageDatabase;
 import com.quanleimu.entity.ChatMessage;
 import com.quanleimu.entity.UserProfile;
 import com.quanleimu.entity.compare.MsgTimeComparator;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Util;
 
 public class TalkView extends BaseView 
 {
 	//This is only a temp solution for checking current IM session, will remove within next release. add on version 2.6
 	public static String CURRENT_RECEIVER_RRICKY = null;
 	
 	public static final int MAX_REQ_COUNT = 100;
 	private static final int MSG_GETPROFILE = 1;
 	private static final int MSG_GETTARGETICON = 2;
 	private static final int MSG_GETMYICON = 3;
 	private static final int MSG_CLOSE_PROGRESS = 4;
 	
 	private String targetUserId;
 	private String adId;
 	private String adTitle = "对话";
 	private BroadcastReceiver msgListener;
 	private String sessionId;
 	private String myUserId;
 	private long lastupdateTime = 0;
 	private boolean alwaysSync;
 	
 	public TalkView(Context context) {
 		super(context);
 		
 		doInit(context, null);
 	}
 	
 	public TalkView(Context context, Bundle bundle) {
 		super(context, bundle);
 		ChatMessage msg = null;
 		if (bundle != null)
 		{
 			targetUserId = bundle.getString("receiverId");
 			myUserId = Util.getMyId(getContext()); //FIXME: this is load from file, may cost times to load it on main thread.
 			adId = bundle.getString("adId");
 			if(bundle.containsKey("receiverNick")){
 				adTitle = bundle.getString("receiverNick");
 			}
 			(new Thread(new GetPersonalProfileThread(targetUserId))).start();
 			(new Thread(new GetPersonalProfileThread(myUserId))).start();
 			if (bundle.containsKey("message"))
 			{
 				msg = (ChatMessage) bundle.getSerializable("message");
 				lastupdateTime = msg.getTimestamp();
 			}
 			
 			
 			
 			if (bundle.containsKey("sessionId"))
 			{
 				this.sessionId = bundle.getString("sessionId");
 			}
 			else if (msg != null)
 			{
 				this.sessionId = msg.getSession();//bundle.getString("session");
 			}
 			else
 			{
 				ChatMessageDatabase.prepareDB(context);
 				String cachedSession = ChatMessageDatabase.getSessionId(myUserId, targetUserId, adId);
 				sessionId = cachedSession == null ? sessionId : cachedSession;
 			}
 			
 			alwaysSync = bundle.getBoolean("forceSync", false);
 			
 //			if (bundle.containsKey("adTitle"))
 //			{
 //				adTitle = bundle.getString("adTitle");
 //			}
 			
 		}
 		
 		doInit(context, msg);
 	}
 	
 	
 	
 	@Override
 	protected void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		
 		//Load history or load msg from server.
 		if (sessionId == null)
 		{
 			Thread t = new Thread(new LoadSvrMsgCmd());
 			t.start();
 		}
 		else
 		{
 			Thread t = new Thread(new LoadLocalMsgCmd());
 			t.start();
 		}
 		
 		
 		registerMsgListener();
 		
 		CURRENT_RECEIVER_RRICKY = targetUserId;
 	}
 
 	@Override
 	protected void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		
 		unregisterReceiver();
 		
 		CURRENT_RECEIVER_RRICKY = null;
 	}
 
 	public void onResume()
 	{
 		super.onResume();
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
 						if (msg.getTo().equals(myUserId))
 						{
 							receiveAndUpdateUI(msg);
 						}
 					}
 				}
 				
 			};
 		}
 		
 		getContext().registerReceiver(msgListener, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_NEW_MSG));
 	}
 	
 	protected void unregisterReceiver()
 	{
 		if (msgListener != null)
 		{
 			getContext().unregisterReceiver(msgListener);
 		}
 	}
 	
 	public void onPause()
 	{
 		super.onPause();
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = adTitle;//"对话";
 		
 		title.m_leftActionHint = "返回";
 		
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tabDef = new TabDef();
 		tabDef.m_visible = false;
 		
 		return tabDef;
 	}	
 	
 	private void doInit(Context context, ChatMessage msg)
 	{
 		this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		
 		LayoutInflater inflator = LayoutInflater.from(context);
 		View root = inflator.inflate(R.layout.im_session, null);
 		addView(root, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		
 		UIControl ctrl = new UIControl();
 		final View sendBtn = (View) findViewById(R.id.im_send_btn);
 		sendBtn.setOnClickListener(ctrl);
 		findViewById(R.id.im_input_box).setOnClickListener(ctrl);
 		
 		((ListView) findViewById(R.id.char_history_p)).setAdapter(new ChatMessageAdapter(Util.getMyId(getContext())));
 		
 		initInputBox();
 		
 		//This message should be load from local cache.
 //		if (msg != null)
 //		{
 //			receiveAndUpdateUI(msg);
 //		}
 //		else 
 		if (alwaysSync) //No message to show and need to sync with server.
 		{
 			View loadingTip = findViewById(R.id.tip_loading_message);
 			loadingTip.setVisibility(View.VISIBLE);
 			TextView text = (TextView) loadingTip.findViewById(R.id.loading_more_tips);
 			text.setText(R.string.tip_loading_im_history);
 		}
 
 	}
 	
 	private ChatMessageAdapter getAdapter()
 	{
 		return (ChatMessageAdapter) ((ListView) findViewById(R.id.char_history_p)).getAdapter();
 	}
 	
 	private void initInputBox()
 	{
 		final EditText inputBox = (EditText) findViewById(R.id.im_input_box);
 		inputBox.setPadding(inputBox.getPaddingLeft(), 2, inputBox.getPaddingRight(), 2);//For nine-patch.
 		
 		final View sendBtn = (View) findViewById(R.id.im_send_btn);
 		sendBtn.setEnabled(false);//Disable send by default.
 		inputBox.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
 				if (arg2 != null && arg2.getAction() == KeyEvent.ACTION_DOWN
 						&& arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER
 						&& arg0.getText().length() > 0)
 				{
 					sendAndUpdateUI(arg0.getText().toString());
 					arg0.setText("");
 					return true;
 				}
 				return false;
 			}});
 		
 		
 		inputBox.addTextChangedListener(new TextWatcher(){
 
 			@Override
 			public void afterTextChanged(Editable edit) {
 				
 				if (edit == null || edit.length() == 0)
 				{
 					sendBtn.setEnabled(false);
 				}
 				else
 				{
 					sendBtn.setEnabled(true);
 				}
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 			
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				
 			}
 			
 		});
 		
 	}
 	
 	private void sendAndUpdateUI(final String message)
 	{
 		//First step to update UI.
 		ChatMessage msg = createMessage(message);
 		getAdapter().appendData(msg);
 		//TODO: post delay to scroll to bottom of scroll view.
 		postScrollDelay();
 		
 		//Send the text to server.
 		Thread t = new Thread(new SendMsgCmd(message));
 		t.start();
 	}
 	
 	private ChatMessage createMessage(final String message)
 	{
 		ChatMessage msg = new ChatMessage();
 		msg.setMessage(message);
 		msg.setSession(this.sessionId);
 		msg.setAdId(this.adId);
 		msg.setFrom(this.myUserId);
 		msg.setTo(this.targetUserId);
 		msg.setId(System.currentTimeMillis() + "");
 		msg.setTimestamp(System.currentTimeMillis()/1000);
 		
 		return msg;
 	}
 	
 	private void receiveAndUpdateUI(final ChatMessage msg)
 	{
 		if(msg == null || !targetUserId.equals(msg.getFrom()))
 		{
 			return;
 		}
 
 		if (sessionId == null)
 		{
 			sessionId = msg.getSession();
 		}
 		
 //		this.messageList.add(msg);
 		
 		ChatMessageDatabase.prepareDB(getContext());
 		ChatMessageDatabase.updateReadStatus(msg.getId(), true);
 		
 		this.postDelayed(new Runnable() {
 			public void run() {
 				getAdapter().appendData(msg);
 				
 				postScrollDelay();
 			}
 			
 		}, 10);
 	}
 	
 	private void mergeAndUpdateUI(final List<ChatMessage> list, final boolean isLocal)
 	{
 		Collections.sort(list, new MsgTimeComparator());
 		lastupdateTime = list.get(list.size()-1).getTimestamp();
 		
 		this.postDelayed(new Runnable() {
 			public void run() {
 //				long startTime = System.currentTimeMillis();
 				if (isLocal)
 				{
 					getAdapter().refreshData(list);
 				}
 				else
 				{
 					getAdapter().appendData(list, false);
 				}
 				
 				postScrollDelay();
 //				Log.e("TalkView", "update ui cost : " + (System.currentTimeMillis()-startTime));
 			}
 			
 		}, 10);
 		
 		ChatMessageDatabase.prepareDB(getContext());
 		ChatMessageDatabase.storeMessage(list, true);
 	}
 	
 	private void postScrollDelay()
 	{
 		this.postDelayed(new Runnable() {
 			
 			@Override
 			public void run() {
 				if (getAdapter().getCount() > 0)
 				{
 					ListView scroll = (ListView) findViewById(R.id.char_history_p);
 //				scroll.fullScroll(ScrollView.FOCUS_DOWN);
 					scroll.setSelection(getAdapter().getCount()-1);
 				}
 				
 			}
 		}, 200);
 	}
 	
 	private void updateSendStatus(boolean succed)
 	{
 		//TODO:
 	}
 	
 	class LoadLocalMsgCmd implements Runnable {
 
 		@Override
 		public void run() {
 			List<ChatMessage> msgList = null;
 			ChatMessageDatabase.prepareDB(getContext());
 			
 			if (sessionId != null)
 			{
 				msgList = ChatMessageDatabase.queryMessageBySession(sessionId); 
 			}
 			
 			if (msgList != null && msgList.size() > 0)
 			{
 				myHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
 				mergeAndUpdateUI(msgList, true);
 			}
 			
 			if (msgList == null || msgList.size() == 0 || alwaysSync)
 			{
 				new LoadSvrMsgCmd().run();
 			}
 			
 		}
 	}
 	
 	class LoadSvrMsgCmd implements Runnable 
 	{
 		final String apiName = "read_message";
 		
 		public void run() 
 		{
 			ArrayList<String> cmdOpts = new ArrayList<String>();
 			cmdOpts.add("u_id=" + URLEncoder.encode(myUserId));
 			if (sessionId != null)
 			{
 				cmdOpts.add("session_id=" + URLEncoder.encode(sessionId));
 			}
 			else
 			{
 				cmdOpts.add("u_id_other=" + URLEncoder.encode(targetUserId));
 				cmdOpts.add("ad_id=" + URLEncoder.encode(adId));
 			}
 			
 			//FIXME: only load messages within several days. we use 10 at present.
 			if (lastupdateTime != 0)
 			{
 				cmdOpts.add("last_update_timestamp=" + URLEncoder.encode(lastupdateTime + ""));
 			}
 			cmdOpts.add("limit=" + MAX_REQ_COUNT);
 			
 			String url = Communication.getApiUrl(apiName, cmdOpts);
 			
 			try {
 				String result = Communication.getDataByUrlGet(url);
 				JSONObject obj = new JSONObject(result);
 				if (obj.getInt("count") > 0)
 				{
 					JSONArray tmp = obj.getJSONArray("data");
 					mergeAndUpdateUI(JsonUtil.parseChatMessages(tmp), false);
 				}
 			}
 			catch(Throwable t)
 			{
 				//TODO: show error.
 			}
 			finally
 			{
 				myHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
 			}
 		}
 	}
 	
 	class SendMsgCmd implements Runnable 
 	{
 		private final String apiName = "send_message";
 		private String message;
 		public SendMsgCmd(String messageToSend)
 		{
 			this.message = messageToSend;
 		}
 
 		@Override
 		public void run() {
 			ArrayList<String> cmdOpts = new ArrayList<String>();
 			cmdOpts.add("u_id_from=" + URLEncoder.encode(myUserId));
 			cmdOpts.add("u_id_to=" + URLEncoder.encode(targetUserId));
 			if (adId != null)
 			{
 				cmdOpts.add("ad_id=" + URLEncoder.encode(adId));
 			}
 			cmdOpts.add("message=" + URLEncoder.encode(message));
 			if (sessionId != null)
 			{
 				cmdOpts.add("session_id=" + URLEncoder.encode(sessionId));
 			}
 			
 			String url = Communication.getApiUrl(apiName, cmdOpts);
 			
 			try {
 				String result = Communication.getDataByUrl(url,true);
 				try {
 					JSONObject json = new JSONObject(result);
 					if (sessionId == null)
 					{
 						sessionId = json.getString("session_id");
 					}
 					
 					lastupdateTime = json.getLong("timestamp");
 					
 					json.remove("u_id");
 					json.put("ad_id", adId);
 					json.put("u_id_from", myUserId);
 					json.put("u_id_to", targetUserId);
 					json.put("id", json.get("msg_id"));
 					json.remove("msg_id");
 					json.put("u_nick_from", "");
 					json.put("u_nick_to", "");
 					json.put("ad_title", "");
 					json.put("message", message);
 					
 					ChatMessage chatMsgInst = ChatMessage.fromJson(json);
 					
 					ChatMessageDatabase.prepareDB(getContext());
 					ChatMessageDatabase.storeMessage(chatMsgInst);
 					ChatMessageDatabase.updateReadStatus(chatMsgInst.getId(), true);
 //					messageList.add(chatMsgInst);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				
 				updateSendStatus(true);
 			} catch (Throwable e) {
 				updateSendStatus(false);
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	class UIControl implements View.OnClickListener, View.OnTouchListener
 	{
 
 		public void onClick(View v) {
 			switch (v.getId())
 			{
 			case R.id.im_input_box:
 				postScrollDelay();
 				break;
 			case R.id.im_send_btn:
 				EditText text = (EditText) findViewById(R.id.im_input_box);
 				if (text.length() != 0)
 				{
 					sendAndUpdateUI(text.getText().toString());
 					text.setText("");
 				}
 				break;
 				
 				default:
 					break;
 			}
 		}
 
 		public boolean onTouch(View v, MotionEvent event) {
 			//TODO: check if we need hide scroll bar when scroll message list.
 //			if (v.getId() == R.id.char_history_p && event.getAction() == MotionEvent.ACTION_MOVE)
 //			{
 //			}
 			return false;
 		}
 	}
 	
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_GETPROFILE:
 				if(msg.obj != null){
 					TitleDef title = new TitleDef();
 					title.m_visible = true;
 					title.m_title = msg.obj.toString();				
 					title.m_leftActionHint = "返回";
					m_viewInfoListener.onTitleChanged(title);
 
 				}
 				break;
 			case MSG_GETTARGETICON:
 			{
 				SimpleProfile p = (SimpleProfile) msg.obj;
 				getAdapter().setTargetProfile(p.icon, p.isBoy);
 				break;
 			}
 			case MSG_GETMYICON:
 			{
 				SimpleProfile p = (SimpleProfile) msg.obj;
 				getAdapter().setMyProfile(p.icon, p.isBoy);
 				break;
 			}
 			case MSG_CLOSE_PROGRESS:
 			{
 				findViewById(R.id.tip_loading_message).setVisibility(View.GONE);
 				break;
 			}
 			}
 		}
 	};
 				
 	class GetPersonalProfileThread implements Runnable {
 		private String usrId = null;
 		public GetPersonalProfileThread(String usrId){
 			this.usrId = usrId;
 		}
 		@Override
 		public void run() {
 			if (usrId == null || usrId.equals(""))
 			{
 				return;
 			}
 			String upJson = Util.requestUserProfile(usrId);
 			if(upJson != null){
 				UserProfile up = UserProfile.from(upJson);
 				if(up != null){
 					if(usrId.equals(targetUserId)){
 						SimpleProfile profile = new SimpleProfile();
 						profile.icon = up.squareImage;
 						profile.isBoy = true;
 						if(up.gender != null && up.gender.equals("女")){
 							profile.isBoy = false;
 						}
 						Message msg1 = myHandler.obtainMessage(MSG_GETTARGETICON, profile);
 						myHandler.sendMessage(msg1);
 						
 						Message msg2 = myHandler.obtainMessage();
 						msg2.what = MSG_GETPROFILE;
 						msg2.obj = up.nickName;
 						myHandler.sendMessage(msg2);
 					}else{
 						SimpleProfile profile = new SimpleProfile();
 						profile.icon = up.squareImage;
 						profile.isBoy = true;
 						if(up.gender != null && up.gender.equals("女")){
 							profile.isBoy = false;
 						}	
 						Message msg1 = myHandler.obtainMessage(MSG_GETMYICON, profile);
 						myHandler.sendMessage(msg1);
 					}
 				}
 			}
 		}
 	}
 	
 	class SimpleProfile {
 		public String icon;
 		public boolean isBoy;
 	}
 	
 }
