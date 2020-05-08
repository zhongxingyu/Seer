 package com.imps.activities;
 
 
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.style.ImageSpan;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.imps.IMPSDev;
 import com.imps.R;
 import com.imps.basetypes.Constant;
 import com.imps.basetypes.ListContentEntity;
 import com.imps.basetypes.MediaType;
 import com.imps.basetypes.SystemMsgType;
 import com.imps.basetypes.UserMessage;
 import com.imps.media.audio.Record;
 import com.imps.media.audio.Track;
 import com.imps.net.handler.UserManager;
 import com.imps.receivers.IMPSBroadcastReceiver;
 import com.imps.services.impl.ServiceManager;
 import com.imps.ui.widget.ChattingAdapter;
 import com.imps.util.LocalDBHelper;
 
 public class ChatView extends Activity{
 	protected static final String TAG = ChatView.class.getCanonicalName();
 	private static boolean DEBUG = IMPSDev.isDEBUG();
 	private ListView mListView;
 	private Button send;
 	public static ArrayList<ListContentEntity> list = new ArrayList<ListContentEntity>();
 	private ChattingAdapter listAdapter;
 	private String fUsername = "";
 	private EditText textEditor;
 	private View recording;
 	private ImageView faceButton;
 	private ImageView audioImageIv;
 	private ImageView captureImageIv;
 	private ImageView graffitiImageIv;
 	private PopupWindow menuWindow = null;
 	private Record record = null;
 	private ChatViewReceiver receiver = new ChatViewReceiver();
 	private LocalDBHelper localDB = new LocalDBHelper(this); 
 	
 	@Override
 	public void onResume()
 	{
 		UserManager.activeFriend = fUsername;
 		super.onResume();
 		registerReceiver(receiver,receiver.getFilter());
 		listAdapter.notifyDataSetChanged();
 	}
 	@Override
 	public void onPause()
 	{   
 		super.onPause();
 		UserManager.activeFriend = null;
 	}
 	@Override
 	public void onStop(){
 		super.onStop();
 		unregisterReceiver(receiver);
 	}
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.chatview);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.chatting_title_bar);
 		list = new ArrayList<ListContentEntity>();
 		mListView = (ListView) findViewById(R.id.chatting_history_lv);
 		Intent fi = this.getIntent();
 		fUsername = fi.getStringExtra("fUsername");
 		setAdapterForThis();
 		send = (Button) findViewById(R.id.send_button);
 		send.setOnClickListener(l);
 		textEditor = (EditText) findViewById(R.id.text_editor);
 		audioImageIv = (ImageView) findViewById(R.id.send_image);
 		captureImageIv = (ImageView) findViewById(R.id.capture_image);
 		graffitiImageIv = (ImageView)findViewById(R.id.start_graffiti);
 		graffitiImageIv.setOnClickListener(l);
 		captureImageIv.setOnClickListener(l);
 		((TextView)findViewById(R.id.chatting_contact_name)).setText(this.getResources().getString(R.string.chat_title, fUsername));
 		recording = findViewById(R.id.recording);
 		recording.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				int action = event.getAction();
 
 				switch (action) {
 				case MotionEvent.ACTION_UP:
 					v.setBackgroundResource(R.drawable.hold_to_talk_normal);
 					if (menuWindow != null)
 						menuWindow.dismiss();
 					if(DEBUG) Log.d(TAG, "---onTouchEvent action:ACTION_UP");
 					record.stop = true;
                 	record.stopThread();
                     Track track = new Track();
                     for(int i=0;i<record.dataList.size();i++)
                     {
                     	track.data.add(record.dataList.get(i));
                     }
                     track.run();
                     MediaType media = new MediaType(MediaType.AUDIO,record.dataList,MediaType.to);
                     media.setFriend(fUsername);media.setTime(getTime());
                     if(DEBUG){
                     	Log.d(TAG,"Audio size:"+media.getContant().size());
                     }
                     if(media.getContant().size()<3){
                     	if(DEBUG)Log.d(TAG,"Audio data too small...");
                     	break;
                     }
         			if(UserManager.CurSessionFriList.containsKey(fUsername))
         			{
         				 UserManager.CurSessionFriList.get(fUsername).add(media);
         				 if(DEBUG) Log.d(TAG,"adding to the list");
         			}
         			else{
         				List<MediaType> newmsgbox = new ArrayList<MediaType>();
         				newmsgbox.add(media);
         				UserManager.CurSessionFriList.put(fUsername, newmsgbox);
         				if(DEBUG) Log.d(TAG,"adding to the list with new msg");
         			}
                     ServiceManager.getmSms().SendAudio(media);
             	    ListContentEntity d4 = new ListContentEntity(UserManager.getGlobaluser().getUsername(),getTime(),
             	    		"",ListContentEntity.MESSAGE_TO_AUDIO,media.getContant());
             		list.add(d4);
             		listAdapter.notifyDataSetChanged();
 					break;
 				case MotionEvent.ACTION_DOWN:
 					v.setBackgroundResource(R.drawable.hold_to_talk_pressed);
 					ViewGroup root = (ViewGroup) findViewById(R.id.chat_root);
 					View view = LayoutInflater.from(ChatView.this).inflate(R.layout.audio_recorder_ring, null);
 					menuWindow = new PopupWindow(view, 180, 180);
 					view.findViewById(R.id.recorder_ring).setVisibility(View.VISIBLE);
 					view.setBackgroundResource(R.drawable.pls_talk);
 					menuWindow.showAtLocation(root, Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
 					if(DEBUG) Log.d(TAG, "---onTouchEvent action:ACTION_DOWN");
 					record = new Record();
 					record.start();
 					break;
 				}
 				return true;
 			}
 		});
 		audioImageIv.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(!ServiceManager.getmNet().isAvailable()){
 					Toast.makeText(ChatView.this, getResources().getString(R.string.net_problem), Toast.LENGTH_LONG);
 					return;
 				}
 				String ip = ServiceManager.getmNet().getLocalIP(false);
 				if(DEBUG) Log.d(TAG, "IP sent is "+ ip);
 				ServiceManager.getmAudio().SendPTPAudioReq(fUsername);
 				if(DEBUG) Log.d(TAG, "ptp audio request sent");
 				ServiceManager.getmSound().playRingTone();
 				ComponentName cn=new ComponentName(ChatView.this,AudioChat.class);
 				Intent intent=new Intent();
 				intent.putExtra("fUsername", fUsername);
 				intent.setComponent(cn);
 				startActivity(intent);
 				//do what?
 			}			
 		});
 		faceButton = (ImageView)findViewById(R.id.sms_button_insert);
 		faceButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				ComponentName cn=new ComponentName(ChatView.this,FaceDialog.class);
 				Intent intent=new Intent();
 				intent.setComponent(cn);
 				startActivityForResult(intent, 0);
 			}
 			
 		});
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		switch(requestCode){
 		case 0:{   //face
 			switch (resultCode){
 			case 0:{
 				if(data==null)
 					return;
 				int faceId = data.getIntExtra("selectedFace", 0);
 				String id = changeIdToStr(faceId);
 		        Drawable drawable = getResources().getDrawable(faceId);   
 		        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());   
 		        SpannableString spannable = new SpannableString(id);   
 		       
 		        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);   
 		      
 		        spannable.setSpan(span, 0, id.length(), 
 		        		Spannable.SPAN_INCLUSIVE_EXCLUSIVE);     
 		        textEditor.append(spannable);
 		        break;
 			    }
 			default:
 			    break;
 			}			
 		}
 		case 1:{  //graffiti
 			switch(resultCode){
 			case 0:  //false ,do nothing
 				break;
 			case 1:  //true, get bitmap path
 				String path = data.getStringExtra("path");
 				if(path!=null){
 					sendImageMsg(path);
 				}
 				break;
 			}		
 		}
 			break;
 		}
 		
 	}
 	
 	private String changeIdToStr(int faceId) {
 		// TODO Auto-generated method stub
 		switch (faceId){
 		case R.drawable.exp_01:
 			return "[exp_01]";
 		case R.drawable.exp_02:
 			return "[exp_02]";
 		case R.drawable.exp_03:
 			return "[exp_03]";
 		case R.drawable.exp_04:
 			return "[exp_04]";
 		case R.drawable.exp_05:
 			return "[exp_05]";
 		case R.drawable.exp_06:
 			return "[exp_06]";
 		case R.drawable.exp_07:
 			return "[exp_07]";
 		case R.drawable.exp_08:
 			return "[exp_08]";
 		case R.drawable.exp_09:
 			return "[exp_09]";
 		case R.drawable.exp_10:
 			return "[exp_10]";
 		case R.drawable.exp_11:
 			return "[exp_11]";
 		case R.drawable.exp_12:
 			return "[exp_12]";
 		case R.drawable.exp_13:
 			return "[exp_13]";
 		case R.drawable.exp_14:
 			return "[exp_14]";
 		case R.drawable.exp_15:
 			return "[exp_15]";
 		case R.drawable.exp_16:
 			return "[exp_16]";
 		case R.drawable.exp_17:
 			return "[exp_17]";
 		case R.drawable.exp_18:
 			return "[exp_18]";
 		case R.drawable.exp_19:
 			return "[exp_19]";
 		case R.drawable.exp_20:
 			return "[exp_20]";
 		case R.drawable.exp_21:
 			return "[exp_21]";
 		case R.drawable.exp_22:
 			return "[exp_22]";
 		case R.drawable.exp_23:
 			return "[exp_23]";
 		case R.drawable.exp_24:
 			return "[exp_24]";
 		case R.drawable.exp_25:
 			return "[exp_25]";
 		case R.drawable.exp_26:
 			return "[exp_26]";
 		}
 		return null;
 	}
 	private void setAdapterForThis() {
 		// TODO Auto-generated method stub
 		listAdapter = new ChattingAdapter(this,list);
 		mListView.setAdapter(listAdapter);
 		initMessages();
 		
 	}
 	
 	private void initMessages() {
 		// TODO Auto-generated method stub
 		if(DEBUG) Log.d(TAG, "ChatView:listview count is "+mListView.getCount());
 		
 		if(fUsername.equals("SysAdmin")){
 			for(int i=0;i<UserManager.mSysMsgs.size();i++){
 				SystemMsgType sysmsg=UserManager.mSysMsgs.get(i);
 				String content=sysmsg.text;
 				content+="\n【进入系统消息查看】";
 				list.add(new ListContentEntity(fUsername,
 
 						sysmsg.time,content,ListContentEntity.MESSAGE_FROM));
 			}
 			return;
 		}
 		
 		
 		if(mListView.getCount()==0&&UserManager.CurSessionFriList.containsKey(fUsername))
 		{
 			Log.d(TAG, "ChatView:initialing the chat view with old msg");
 			
 			// Add local history message to current session's message list
			ArrayList<UserMessage> history = localDB.fetchMsg(fUsername);
			if (history != null) {
				for (UserMessage m : history) {
					if (m.getDir() == 1)
						list.add(new ListContentEntity(m.getFriend(), m
								.getTime(), m.getContent(),
								ListContentEntity.MESSAGE_FROM));
					else
						list.add(new ListContentEntity(m.getFriend(), m
								.getTime(), m.getContent(),
								ListContentEntity.MESSAGE_TO));
 				}
 			}
 			
 			// Add received messages in the active chat session
 			List<MediaType> mbox = UserManager.CurSessionFriList.get(fUsername);
 			MediaType media;
 			String msg;
 			for(int i=0;i<mbox.size();i++)
 			{
 				if(DEBUG) Log.d(TAG, ""+i+" "+ mbox.get(i));
 				media = mbox.get(i);
                 String fname = media.getDirecet()==MediaType.from?media.getFriend():UserManager.globaluser.getUsername();
                 msg = media.getMsgContant();
                 String date = media.getTime();
                 if(fname.equals(UserManager.getGlobaluser().getUsername()))
                 {
                 	if(media.getType()==MediaType.SMS){
                     	ListContentEntity d1 = new ListContentEntity(fname,date,msg,ListContentEntity.MESSAGE_TO);
                     	list.add(d1);
                 	}else if(media.getType()==MediaType.IMAGE){
                 		list.add(new ListContentEntity(fname,date,"",ListContentEntity.MESSAGE_TO_PICTURE,media.getMsgContant()));
                 	}else if(media.getType()==MediaType.AUDIO){
                 		list.add(new ListContentEntity(fname,date,"",ListContentEntity.MESSAGE_TO_AUDIO,media.getContant()));
                 	}
                 }
                 else{
                 	if(media.getType()==MediaType.SMS){
                     	ListContentEntity d1 = new ListContentEntity(fname,date,msg,ListContentEntity.MESSAGE_FROM);
                     	list.add(d1);
                 	}
                 	else if(media.getType()==MediaType.IMAGE){
                 		list.add(new ListContentEntity(fname,date,"",ListContentEntity.MESSAGE_FROM_PICTURE,media.getMsgContant()));
                 	}else if(media.getType()==MediaType.AUDIO){
                 		list.add(new ListContentEntity(fname,date,"",ListContentEntity.MESSAGE_FROM_AUDIO,media.getContant()));
                 	}
                 }
                 if(DEBUG) Log.d(TAG,"add the existing messages");
 			}
 		}
 	}
 	
 	private View.OnClickListener l = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			if (v.getId() == send.getId()) {
 				String str = textEditor.getText().toString();
 				String sendStr;
 				if (str != null
 						&& (sendStr = str.trim().replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "")
 								.replaceAll("\f", "")) != "") {
 					sendMessage(sendStr);
 					
 					/**
 					 * Store messages which have been sent
 					 * into local message database
 					 * @author Styx
 					 */
 					localDB.storeMsg(textEditor.getText().toString(),
 							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
 							fUsername, 0);
 
 				}
 				textEditor.setText("");
 
 			} else if (v.getId() == captureImageIv.getId()) {
 				if(!ServiceManager.getmNet().isAvailable()){
 					Toast.makeText(ChatView.this,getResources().getString(R.string.net_problem), Toast.LENGTH_LONG);
 					return;
 				}
 				String ip = ServiceManager.getmNet().getLocalIP(false);
 				if(DEBUG) Log.d(TAG, "IP sent is "+ ip);
 				//ServiceManager.getmMedia().SendPTPVideoReq(fUsername, ip, 1300);
 				if(DEBUG) Log.d(TAG, "ptp video request sent");
 				//ComponentName cn=new ComponentName(ChatView.this,VideoContact2.class);
 				//Intent intent=new Intent();
 				//intent.putExtra("fUsername", fUsername);
 				//intent.setComponent(cn);
 				//startActivity(intent);
 			}else if(v.getId()==graffitiImageIv.getId()){
 				if(DEBUG) Log.d(TAG, "graffiti clicked...");
 				ComponentName cn=new ComponentName(ChatView.this,Graffiti.class);
 				Intent intent=new Intent();
 				intent.setComponent(cn);
 				startActivityForResult(intent, 1);
 			}
 		}
 
 		private void sendMessage(String sendStr) {
 			String date = getTime();
 			list.add(new ListContentEntity(UserManager.globaluser.getUsername(),date,sendStr,ListContentEntity.MESSAGE_TO));
 			listAdapter.notifyDataSetChanged();
 			MediaType item = new MediaType(MediaType.SMS,sendStr,MediaType.to);
 			item.setTime(date);item.setFriend(fUsername);
 			if(UserManager.CurSessionFriList.containsKey(fUsername))
 			{
 				 UserManager.CurSessionFriList.get(fUsername).add(item);
 				 if(DEBUG) Log.d(TAG,"adding to the list jyh");
 			}
 			else{
 				List<MediaType> newmsgbox = new ArrayList<MediaType>();
 				newmsgbox.add(item);
 				UserManager.CurSessionFriList.put(fUsername, newmsgbox);
 				if(DEBUG) Log.d(TAG,"adding to the list with new msg jyh");
 			}
 			ServiceManager.getmSms().sendSms(item);
 			
 		}
 	};
 	public void sendImageMsg(String path){
 	    String date = getTime();
 		list.add(new ListContentEntity(UserManager.getGlobaluser().getUsername(),date,"",ListContentEntity.MESSAGE_TO_PICTURE,path));
 		listAdapter.notifyDataSetChanged();
 		MediaType item = new MediaType(MediaType.IMAGE,MediaType.to);
 		item.setMsgContant(path);
 		item.setFriend(fUsername);
 		if(UserManager.CurSessionFriList.containsKey(fUsername))
 		{
 			 UserManager.CurSessionFriList.get(fUsername).add(item);
 			 if(DEBUG) Log.d(TAG,"adding to the list");
 		}
 		else{
 			List<MediaType> newmsgbox = new ArrayList<MediaType>();
 			newmsgbox.add(item);
 			UserManager.CurSessionFriList.put(fUsername, newmsgbox);
 			if(DEBUG) Log.d(TAG,"adding to the list with new msg");
 		}
 		ServiceManager.getmSms().SendImage(item);
 	}
 	
 	
 	/**
 	 * append message to the list view and update it
 	 * @param name
 	 * @param msg
 	 * @param stime
 	 */
 	public void appendToList(String name,String msg,String stime)
 	{
 	    ListContentEntity d4 = new ListContentEntity(name,stime,msg,ListContentEntity.MESSAGE_FROM);
 		list.add(d4);
 		if(DEBUG) Log.d(TAG,"apend to list! ");
 	}
 	
 	public class ChatViewReceiver extends IMPSBroadcastReceiver{
 		@Override
 		public void onReceive(Context context,Intent intent){
 			super.onReceive(context, intent);
 			if(intent.getAction().equals(Constant.SMS)){
 				String friend = intent.getStringExtra(Constant.USERNAME);
 				if(DEBUG) Log.d(TAG,"Sms recv from:"+friend+":"+fUsername);
 				if(friend.equals(fUsername)){
 					String name = intent.getStringExtra(Constant.USERNAME);
 					String stime = intent.getStringExtra(Constant.TIME);
 					String msg = intent.getStringExtra(Constant.SMSCONTENT);
 					ListContentEntity d4 = new ListContentEntity(name,stime,msg,ListContentEntity.MESSAGE_FROM);
 					list.add(d4);
 					listAdapter.notifyDataSetChanged();
 				}
 
 				// Store received message into local database
 				// including displayed on the current chat session GUI
 				// and those from other friends
 				localDB.storeMsg(intent.getStringExtra(Constant.SMSCONTENT),
 						intent.getStringExtra(Constant.TIME),
 						intent.getStringExtra(Constant.USERNAME),
 						1/* From friend */);
 
 			}else if(intent.getAction().equals(Constant.SMSRSP)){
 				
 			}else if(intent.getAction().equals(Constant.IMAGE)){
 				String friend = intent.getStringExtra(Constant.USERNAME); 
 				if(DEBUG) Log.d(TAG,"Image recv from:"+friend+":"+fUsername);
 				if(friend.equals(fUsername)){
 					listAdapter.notifyDataSetChanged();
 				}
 			}else if(intent.getAction().equals(Constant.AUDIO)){
 				String friend = intent.getStringExtra(Constant.USERNAME); 
 				if(DEBUG) Log.d(TAG,"Audio recv from:"+friend+":"+fUsername);
 				if(friend.equals(fUsername)){
 					listAdapter.notifyDataSetChanged();
 				}
 			}
 		}
 		@Override
 		public IntentFilter getFilter(){
 			IntentFilter filter= super.getFilter();
 			filter.addAction(Constant.SMS);
 			filter.addAction(Constant.SMSRSP);
 			filter.addAction(Constant.IMAGE);
 			filter.addAction(Constant.AUDIO);
 			return filter;
 		}
 	}
 
 
 	
 	private String getTime(){
 		Date now = new Date();
 		SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    String date = d1.format(now);
 	    return date;
 	}
 }
