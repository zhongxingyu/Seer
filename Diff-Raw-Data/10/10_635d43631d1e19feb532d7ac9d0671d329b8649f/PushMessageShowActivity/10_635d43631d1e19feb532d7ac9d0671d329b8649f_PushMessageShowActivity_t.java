 package com.zgy.ringforu.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.zgy.ringforu.LogRingForu;
 import com.zgy.ringforu.R;
 import com.zgy.ringforu.bean.PushMessage;
 import com.zgy.ringforu.interfaces.PushMessageCallBack;
 import com.zgy.ringforu.logic.PushMessageController;
 import com.zgy.ringforu.util.NotificationUtil;
 import com.zgy.ringforu.util.PhoneUtil;
 import com.zgy.ringforu.util.PushMessageUtils;
 import com.zgy.ringforu.util.RingForUActivityManager;
 import com.zgy.ringforu.util.StringUtil;
 import com.zgy.ringforu.util.ViewUtil;
 import com.zgy.ringforu.view.FCMenu;
 import com.zgy.ringforu.view.FCMenu.MenuItemOnClickListener;
 import com.zgy.ringforu.view.FCMenuItem;
 import com.zgy.ringforu.view.MyToast;
 
 /**
  * 笑话展示
  * 
  * @Description:
  * @author: zhuanggy
  * @see:
  * @since:
  * @copyright © 35.com
  * @Date:2013-9-24
  */
 
 public class PushMessageShowActivity extends BaseGestureActivity implements OnClickListener {
 
	private static final String TAG = "PushMessageShowActivity";
 
 	private Button btnBack;
 	private Button btnShare;
 	private TextView textInfo;
 	private TextView textTitle;
 	private ImageView imgTag;
 
 	private FCMenu mTopMenu;
 	private OnTopMenuItemClickedListener mTopMenuListener;
 	private static final int ID_MENU_MSG = 1;
 
 	private String mTitle;
 	private String mContent;
 	private String mTag;
 	private long mReceiveTime;
 
 	private static final String TAG_HAND = "hand";
 	private static final String TAG_PANDA = "panda";
 	private static final String TAG_CAT = "cat";
 	private static final String TAG_CAT1 = "cat1";
 	private static final String TAG_CAT2 = "cat2";
 	private static final String TAG_CAT3 = "cat3";
 	private static final String TAG_CAT4 = "cat4";
 	private static final String TAG_CAT5 = "cat5";
 	private static final String TAG_CAT6 = "cat6";
 	private static final String TAG_CAT7 = "cat7";
 
 	private PushMessageController mController;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.push_msg_show_info);
 
 		RingForUActivityManager.push(this);
 
 		Bundle b = getIntent().getExtras();
 		if (b == null) {
 			RingForUActivityManager.pop(this);
 			MyToast.makeText(this, R.string.msg_get_failed, Toast.LENGTH_SHORT, true).show();
 			return;
 		}
 
 		mTitle = b.getString(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_TITLE);
 		mContent = b.getString(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_CONTENT);
 		mTag = b.getString(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_TAG);
 		mReceiveTime = b.getLong(NotificationUtil.INTENT_ACTION_KEY_PUSH_MSG_RECIEVE_TIME);
 
 		if (StringUtil.isNull(mContent) || mReceiveTime == -1) {
 			RingForUActivityManager.pop(this);
 			MyToast.makeText(this, R.string.msg_get_failed, Toast.LENGTH_SHORT, true).show();
 			return;
 		}
 
 		btnBack = (Button) findViewById(R.id.btn_push_msg_return);
 		btnShare = (Button) findViewById(R.id.btn_push_msg_share);
 		textInfo = (TextView) findViewById(R.id.text_push_msg_info);
 		textTitle = (TextView) findViewById(R.id.text_push_msg_title);
 		imgTag = (ImageView) findViewById(R.id.img_push_msg_show);
 
 		if (!StringUtil.isNull(mTitle)) {
 			textTitle.setText(mTitle);
 		}
 
 		mController = PushMessageController.getInstence();
 		mController.addCallBack(mPushMessageCallback);
 
 		mController.setPushMessageReadStatue(mReceiveTime, PushMessage.READ);
 
 		textInfo.setText(mContent.replaceAll(PushMessageUtils.MESSAGE_TAG_BREAKLINE, "\r\n"));
 
 		int srcId = getImageSrcIdByTag(mTag);
 		if (srcId != -1) {
 			imgTag.setImageResource(srcId);
 		}
 
 		initTopMenu();
 
 		btnBack.setOnClickListener(this);
 		btnShare.setOnClickListener(this);
 
 	}
 
 	private int getImageSrcIdByTag(String tag) {
 
 		if (!StringUtil.isNull(tag)) {
 			if (tag.equals(TAG_HAND)) {
 				return R.drawable.ic_joke_hand;
 			} else if (tag.equals(TAG_PANDA)) {
 				return R.drawable.ic_joke_panda;
 			} else if (tag.equals(TAG_CAT)) {
 				return R.drawable.ic_joke_cat;
 			} else if (tag.equals(TAG_CAT1)) {
 				return R.drawable.ic_joke_cat1;
 			} else if (tag.equals(TAG_CAT2)) {
 				return R.drawable.ic_joke_cat2;
 			} else if (tag.equals(TAG_CAT3)) {
 				return R.drawable.ic_joke_cat3;
 			} else if (tag.equals(TAG_CAT4)) {
 				return R.drawable.ic_joke_cat4;
 			} else if (tag.equals(TAG_CAT5)) {
 				return R.drawable.ic_joke_cat5;
 			} else if (tag.equals(TAG_CAT6)) {
 				return R.drawable.ic_joke_cat6;
 			} else if (tag.equals(TAG_CAT7)) {
 				return R.drawable.ic_joke_cat7;
 			}
 		}
 
 		return -1;
 	}
 
 	private void initTopMenu() {
 		DisplayMetrics dMetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
 		int[] widthHeight = new int[2];
 		widthHeight[0] = dMetrics.widthPixels / 4;
 		// widthHeight[1] = dMetrics.heightPixels / 2;
 		widthHeight[1] = ViewGroup.LayoutParams.FILL_PARENT;
 
 		mTopMenu = new FCMenu(PushMessageShowActivity.this, widthHeight, findViewById(R.id.view_push_msg_anchor));
 		mTopMenuListener = new OnTopMenuItemClickedListener();
 		mTopMenu.setMenuItemOnclickListener(mTopMenuListener);
 
 		List<FCMenuItem> items = new ArrayList<FCMenuItem>();
 		items.add(new FCMenuItem(ID_MENU_MSG, -1, R.string.msg_share_msg));
 		mTopMenu.setDatas(items, -1, false);
 	}
 
 	private class OnTopMenuItemClickedListener implements MenuItemOnClickListener {
 
 		@Override
 		public void onItemClicked(FCMenuItem item) {
 			PhoneUtil.doVibraterNormal(PushMessageShowActivity.super.mVb);
 			switch (item.getOpID()) {
 			case ID_MENU_MSG:
 				// 短信分享
 
 				StringBuilder sb = new StringBuilder();
 
 				if (!StringUtil.isNull(mTitle)) {
 					sb.append("【").append(mTitle).append("】");
 				}
 
 				sb.append(mContent);
 				Intent it = new Intent(Intent.ACTION_VIEW);
 				it.putExtra("sms_body", sb.toString());
 				it.setType("vnd.android-dir/mms-sms");
 				startActivity(it);
 
 				mController.addShareTimesLog(mReceiveTime);
 				break;
 
 			default:
 				break;
 			}
 
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
		mController.remoeCallBack(mPushMessageCallback);
 		super.onDestroy();
 	}
 
 	@Override
 	public void onClick(View v) {
 		PhoneUtil.doVibraterNormal(super.mVb);
 		switch (v.getId()) {
 		case R.id.btn_push_msg_return:
 			RingForUActivityManager.pop(PushMessageShowActivity.this);
 			break;
 		case R.id.btn_push_msg_share:
 			mTopMenu.showMenu(-1);
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	@Override
 	public void onSlideToRight() {
 		super.onSlideToRight();
 		ViewUtil.onButtonPressedBack(btnBack);
 		PhoneUtil.doVibraterNormal(super.mVb);
 		if (mTopMenu.isShowing()) {
 			mTopMenu.closeMenu();
 		}
 		RingForUActivityManager.pop(PushMessageShowActivity.this);
 	}
 
 	@Override
 	public void onSlideToLeft() {
 		super.onSlideToLeft();
 		if (!mTopMenu.isShowing()) {
 			ViewUtil.onButtonPressedBlue(btnShare);
 			PhoneUtil.doVibraterNormal(super.mVb);
 			mTopMenu.showMenu(-1);
 		}
 	}
 
 	private PushMessageCallBack mPushMessageCallback = new PushMessageCallBack() {
 
 		@Override
 		public void setPushMessageReadStatueFinished(boolean result) {
 			super.setPushMessageReadStatueFinished(result);
 			LogRingForu.e(TAG, "setPushMessageReadStatueFinished result=" + result);
 		}
 
 	};
 
 }
