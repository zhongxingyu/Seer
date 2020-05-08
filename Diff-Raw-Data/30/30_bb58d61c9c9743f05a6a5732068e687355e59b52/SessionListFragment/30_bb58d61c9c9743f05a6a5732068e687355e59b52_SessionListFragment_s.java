 package com.quanleimu.view.fragment;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.SessionListAdapter;
 import com.quanleimu.broadcast.CommonIntentAction;
 import com.quanleimu.broadcast.NotificationIds;
 import com.quanleimu.database.ChatMessageDatabase;
 import com.quanleimu.entity.ChatMessage;
 import com.quanleimu.entity.ChatSession;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ParameterHolder;
 import com.quanleimu.util.TrackConfig.TrackMobile.Key;
 import com.quanleimu.util.TrackConfig.TrackMobile.PV;
 import com.quanleimu.util.Tracker;
 import com.quanleimu.util.Util;
 import com.quanleimu.util.ViewUtil;
 import com.quanleimu.widget.PullToRefreshListView;
 
 public class SessionListFragment extends BaseFragment  implements View.OnClickListener, PullToRefreshListView.OnRefreshListener, OnItemClickListener{
 	private List<ChatSession> sessions = null;
 	private BroadcastReceiver chatMessageReceiver;
 	
 	
 	private static final int MSG_NEW_MESSAGE = 1;
 	private static final int MSG_NEW_SESSION = 2;
 	private static final int MSG_NEW_SESSION_FAIL = 3;
 	private static final int MSG_DEL_SESSION = 4;	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.sessions = (List) getArguments().getSerializable("sessions");
 	}
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.sessionlist, null);
 
 //		PullToRefreshListView plv = (PullToRefreshListView)this.findViewById(R.id.lv_sessionlist);
 		ListView plv = (ListView)v.findViewById(R.id.lv_sessionlist);
 		SessionListAdapter adapter = new SessionListAdapter(this.getActivity(), sessions);
 		adapter.setMessageOutOnDelete(this.handler, MSG_DEL_SESSION);
 		plv.setAdapter(adapter);
 		plv.setOnItemClickListener(this);
 //		plv.setPullToRefreshEnabled(false);
 		
 		if (this.sessions == null || this.sessions.size() == 0)
 		{
 			v.findViewById(R.id.session_loading).setVisibility(View.VISIBLE);
 		}
 		
 		ViewUtil.removeNotification(getActivity(), NotificationIds.NOTIFICATION_ID_CHAT_MESSAGE);
 		
 		return v;
 	}
 	
 	private SessionListAdapter getAdapter(View rootView)
 	{
 		ListView plv = (ListView)rootView.findViewById(R.id.lv_sessionlist);
 		return (SessionListAdapter) plv.getAdapter();
 	}
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		unregisterReceiver();
 	}
 	@Override
 	public void onResume() {
 		super.onResume();
 		this.pv = PV.BUZZLISTING;
 //		ListView plv = (ListView)getView().findViewById(R.id.lv_sessionlist);
 //		plv.requestFocus();
 		
 		syncSessions(Util.getMyId(getActivity()));
 		
 		registerReceiver();
 		BaseAdapter adapter = getAdapter(getView());
 		if(adapter != null){
 			adapter.notifyDataSetChanged();
 		}
 	}
 	
 	public void onDestroy()
 	{
 		super.onDestroy();
 		chatMessageReceiver = null;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId())
 		{
 			case R.id.btnCancel:
 				break;
 		}
 	}
 	
 	@Override
 	public void initTitle(TitleDef title){
 		title.m_leftActionHint = "返回";
 		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
 		title.m_title = "私信";
 		title.m_visible = true;
 	}
 	
 	@Override
 	public void initTab(TabDef tab){
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
 	}
 
 	@Override
 	public void onRefresh() {
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View item, int pos, long id) 
 	{
 		ChatSession session = (ChatSession) arg0.getAdapter().getItem(pos);
 		Bundle bundle = createArguments(null, null);
 		bundle.putString("sessionId", session.getSessionId());
 		bundle.putString("receiverId", session.getOppositeId());
 		bundle.putString("adId", session.getAdId());
 		bundle.putString("adTitle", session.getAdTitle());
 		bundle.putBoolean("forceSync", true);
 		bundle.putString("receiverNick", session.getOppositeNick());
 		
 		updateSessionInfo(session, item);
 		
 		pushFragment(new TalkFragment(), bundle);
 	}
 	
 	private void updateSessionInfo(ChatSession session, View item)
 	{
 		ChatMessageDatabase.prepareDB(getActivity());
 		ChatMessage lastMessage = ChatMessageDatabase.getLastMessage(session.getSessionId());
 		if (lastMessage != null )
 		{
 //TODO:update unread status
 			session.setTimeStamp(lastMessage.getTimestamp() + "");
 			session.setLastMsg(lastMessage.getMessage());
 		}
 	}
 	 
 	
 	
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 		if (rootView == null)
 			return;
 		
 		switch(msg.what)
 		{
 		case MSG_NEW_SESSION_FAIL:
 			rootView.findViewById(R.id.session_loading).setVisibility(View.GONE);
 			break;
 		case MSG_NEW_SESSION:
 			getAdapter(rootView).updateSessions((List<ChatSession>) msg.obj);
 			rootView.findViewById(R.id.session_loading).setVisibility(View.GONE);	
 		case MSG_NEW_MESSAGE:
 			getAdapter(rootView).notifyDataSetChanged();
 			break;
 		case MSG_DEL_SESSION:
 			getAdapter(rootView).notifyDataSetChanged();
 			break;
 		}
 	
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
 						onNewMessage(msg);
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
 	
 	private void onNewMessage(ChatMessage msg)
 	{
 		for (ChatSession session : this.sessions)
 		{
 			if (msg.getSession().equals(session.getSessionId()))
 			{
 				sendMessage(MSG_NEW_MESSAGE, null);
 				return;
 			}
 		}
 		
 		syncSessions(msg.getTo());
 	}
 	
 	private void syncSessions(String userId)
 	{
 		ParameterHolder params = new ParameterHolder();
 		params.addParameter("u_id", userId);
 		
 		Communication.executeAsyncGetTask("read_session", params, new Communication.CommandListener() {
 			
 			@Override
 			public void onServerResponse(String serverMessage) {
 				if (getActivity() != null)
 				{
 //					findViewById(R.id.session_loading).setVisibility(View.GONE);
 					List<ChatSession> newSessions = ChatSession.fromJson(serverMessage);
 					sendMessage(MSG_NEW_SESSION, newSessions);
 					//tracker					
					if (newSessions!=null)
						Tracker.getInstance().pv(PV.BUZZLISTING).append(Key.ADSCOUNT, newSessions.size()).end();
					else
 						Tracker.getInstance().pv(PV.BUZZLISTING).append(Key.ADSCOUNT, "0").end();
 				}
 			}
 			
 			@Override
 			public void onException(Exception ex) {
 				//Ignor this exception.
 				sendMessage(MSG_NEW_SESSION_FAIL, null);
 				//tracker
 				Tracker.getInstance().pv(PV.BUZZLISTING).append(Key.ADSCOUNT, "0").end();
 //				if (getActivity() != null)
 //				{
 //					findViewById(R.id.session_loading).setVisibility(View.GONE);
 //				}
 			}
 		});
 	}
 	
 
 	
 }
