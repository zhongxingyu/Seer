 package com.zgy.ringforu.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.zgy.ringforu.LogRingForu;
 import com.zgy.ringforu.MainCanstants;
 import com.zgy.ringforu.R;
 import com.zgy.ringforu.RingForU;
 import com.zgy.ringforu.adapter.PushMessageListAdapter;
 import com.zgy.ringforu.bean.PushMessage;
 import com.zgy.ringforu.interfaces.PushMessageCallBack;
 import com.zgy.ringforu.logic.PushMessageController;
 import com.zgy.ringforu.util.NotificationUtil;
 import com.zgy.ringforu.util.PhoneUtil;
 import com.zgy.ringforu.util.RingForUActivityManager;
 import com.zgy.ringforu.util.ViewUtil;
 import com.zgy.ringforu.view.MyToast;
 
 public class PushMessageListActivity extends BaseGestureActivity implements OnClickListener {
 
 	private static final String TAG = "DirectoryPicker";
 
 	private ListView mListView;
 	// private Button mBtnOk;
 	private Button mBtnBack;
 	private ImageView imgLoading;
 	private AnimationDrawable animationDrawable;
 
 	private List<PushMessage> mPushmessageList = new ArrayList<PushMessage>();
 
 	private PushMessageController mController;
 
 	private static final int LIMIT = 10;
 
 	public static final String INTENT_INSERT_MESSAGE = "com.zgy.ringforu.PUSH_MESSAGE_INSERT";
 	public static final String INTENT_INSERT_MESSAGE_EXTRA = "notificationId";
 
 	private BroadcastReceiver mReciever;
 	private ActivityManager mActivityManager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_push_messagelist);
 
 		RingForUActivityManager.push(PushMessageListActivity.this);
 		initViews();
 
 		mController = PushMessageController.getInstence();
 		mController.addCallBack(mCallBack);
 
 		mController.getPushMessageList(0, LIMIT);
 
 		IntentFilter counterActionFilter = new IntentFilter(INTENT_INSERT_MESSAGE);
 		mReciever = new MyBroadcastReceiver();
 		registerReceiver(mReciever, counterActionFilter);
 		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 
 		// 清除所有消息通知
 		NotificationUtil.hideAllPushMessageNotifications(RingForU.getInstance());
 	}
 
 	@Override
 	public void finalize() {
 		mPushmessageList.clear();
 		mPushmessageList = null;
 	}
 
 	private void initViews() {
 		mListView = (ListView) findViewById(R.id.list_push_messagelist);
 		// mBtnOk = (Button) findViewById(R.id.btn_pick_app_set);
 		mBtnBack = (Button) findViewById(R.id.btn_push_messagelist_back);
 		imgLoading = (ImageView) findViewById(R.id.img_push_messagelist_animation);
 		animationDrawable = (AnimationDrawable) imgLoading.getDrawable();
 		animationDrawable.start();
 
 		// mBtnOk.setOnClickListener(this);
 		mBtnBack.setOnClickListener(this);
 	}
 
 	private void refreshListView() {
 		PushMessageListAdapter a = (PushMessageListAdapter) mListView.getAdapter();
 		if (a != null) {
 			a.notifyDataSetChanged();
 		}
 
 	}
 
 	@Override
 	protected void onResume() {
 		refreshListView();
 		super.onResume();
 	}
 
 	private OnItemClickListener mLsnListItemClick = new OnItemClickListener() {
 
 		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
 			PhoneUtil.doVibraterNormal(PushMessageListActivity.super.mVb);
 			LogRingForu.e(TAG, "onItemClick id=" + pos);
 			PushMessage msg = mPushmessageList.get(pos);
 			msg.setReadStatue(PushMessage.READ);
 			mController.setPushMessageReadStatue(msg.getReceiveTime(), PushMessage.READ);
 
 			Intent i = new Intent(PushMessageListActivity.this, PushMessageShowActivity.class);
 			i.putExtra(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_TITLE, msg.getTitle());
 			i.putExtra(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_CONTENT, msg.getContent());
 			i.putExtra(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_TAG, msg.getTag());
 			i.putExtra(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_RECIEVE_TIME, msg.getReceiveTime());
 			startActivity(i);
 		}
 	};
 
 	@Override
 	public void onClick(View v) {
 		PhoneUtil.doVibraterNormal(PushMessageListActivity.super.mVb);
 
 		switch (v.getId()) {
 		case R.id.btn_push_messagelist_back:
 			RingForUActivityManager.pop(PushMessageListActivity.this);
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	private PushMessageCallBack mCallBack = new PushMessageCallBack() {
 
 		@Override
 		public void getPushMessageListFinished(boolean result, final List<PushMessage> pushMessages) {
 
 			super.getPushMessageListFinished(result, pushMessages);
 
 			if (result) {
 				mPushmessageList = pushMessages;
 				runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						animationDrawable.stop();
 						imgLoading.setVisibility(View.GONE);
 
 						if (pushMessages == null || pushMessages.size() <= 0) {
 							MyToast.makeText(RingForU.getInstance(), R.string.push_message_null, Toast.LENGTH_LONG, true).show();
 						} else {
 							mListView.setAdapter(new PushMessageListAdapter(PushMessageListActivity.this, mPushmessageList));
 							mListView.setOnItemClickListener(mLsnListItemClick);
 						}
 
 					}
 				});
 			}
 		}
 
 	};
 
 	private class MyBroadcastReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(INTENT_INSERT_MESSAGE)) {
 				mController.getPushMessageList(0, mPushmessageList.size() + 1);
 				MyToast.makeText(context, R.string.push_message_receive_one, Toast.LENGTH_LONG, false).show();
 
 				// 若在当前activity正在显示，则清除通知
 				if (mActivityManager != null) {
 					RunningTaskInfo info = mActivityManager.getRunningTasks(1).get(0);
 					// 类名
 					String className = info.topActivity.getClassName(); //
 					// 完整类名
 					String packageName = info.topActivity.getPackageName(); // 包名
 
 					if (packageName.equals(MainCanstants.PACKAGE_NAME) && className.equals(MainCanstants.ACTIVITY_NAME_PUSHMESSAGE_LIST)) {
 						NotificationUtil.showHidePushMessageNotify(false, context, null, intent.getIntExtra(INTENT_INSERT_MESSAGE_EXTRA, -1));
 					}
 				}
 
 			}
 		}
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(mReciever);
		mController.remoeCallBack(mCallBack);
 	}
 
 	@Override
 	public void onSlideToRight() {
 		super.onSlideToRight();
 		ViewUtil.onButtonPressedBack(mBtnBack);
 		PhoneUtil.doVibraterNormal(super.mVb);
 		RingForUActivityManager.pop(this);
 
 	}
 
 	@Override
 	public void onSlideToLeft() {
 		super.onSlideToLeft();
 	}
 
 }
