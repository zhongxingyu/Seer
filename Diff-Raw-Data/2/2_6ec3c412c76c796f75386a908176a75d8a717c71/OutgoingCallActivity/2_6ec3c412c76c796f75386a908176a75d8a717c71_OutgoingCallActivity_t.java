 package com.richitec.chinesetelephone.call;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Handler;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 
 import com.richitec.chinesetelephone.R;
 import com.richitec.chinesetelephone.R.drawable;
 import com.richitec.chinesetelephone.sip.SipCallMode;
 import com.richitec.chinesetelephone.sip.SipUtils;
 import com.richitec.chinesetelephone.sip.listeners.SipInviteStateListener;
 import com.richitec.chinesetelephone.sip.services.BaseSipServices;
 import com.richitec.chinesetelephone.tab7tabcontent.ContactListTabContentActivity;
 import com.richitec.chinesetelephone.tab7tabcontent.ContactListTabContentActivity.ContactsInABListViewQuickAlphabetBarOnTouchListener;
 import com.richitec.chinesetelephone.tab7tabcontent.DialTabContentActivity;
 import com.richitec.commontoolkit.activityextension.AppLaunchActivity;
 import com.richitec.commontoolkit.customcomponent.ListViewQuickAlphabetBar;
 import com.richitec.commontoolkit.utils.HttpUtils.OnHttpRequestListener;
 
 public class OutgoingCallActivity extends Activity implements
 		SipInviteStateListener {
 
 	private static final String LOG_TAG = "OutgoingCallActivity";
 
 	// call controller gridView keys
 	public static final String CALL_CONTROLLER_ITEM_BACKGROUND = "call_controller_item_background";
 	public static final String CALL_CONTROLLER_ITEM_ONTOUCHLISTENER = "call_controller_item_onTouchListener";
 
 	// keyboard gridView keys
 	public static final String KEYBOARD_BUTTON_CODE = "keyboard_button_code";
 	public static final String KEYBOARD_BUTTON_IMAGE = "keyboard_button_image";
 	public static final String KEYBOARD_BUTTON_BGRESOURCE = "keyboard_button_background_resource";
 	public static final String KEYBOARD_BUTTON_ONCLICKLISTENER = "keyboard_button_onClickListener";
 
 	// sip services
 	private static final BaseSipServices SIPSERVICES = SipUtils
 			.getSipServices();
 
 	// outgoing call activity onCreate param key
 	public static final String OUTGOING_CALL_MODE = "outgoing_call_mode";
 	public static final String OUTGOING_CALL_PHONE = "outgoing_call_phone";
 	public static final String OUTGOING_CALL_OWNERSHIP = "outgoing_call_ownership";
 
 	// sound pool
 	private static final SoundPool SOUND_POOL = new SoundPool(1,
 			AudioManager.STREAM_MUSIC, 0);
 
 	// outgoing call phone number
 	private String _mCalleePhone;
 
 	// audio manager
 	private AudioManager _mAudioManager;
 
 	// call duration timer
 	private final Timer CALLDURATION_TIMER = new Timer();
 
 	// call duration time and set default value is 0
 	private Long _mCallDutation = 0L;
 
 	// update call duration time handle
 	private final Handler UPDATE_CALLDURATIONTIME_HANDLE = new Handler();
 
 	// send callback sip voice call http request listener
 	private final SendCallbackSipVoiceCallHttpRequestListener SEND_CALLBACKSIPVOICECALL_HTTPREQUESTLISTENER = new SendCallbackSipVoiceCallHttpRequestListener();
 
 	// phone state broadcast receiver
 	private BroadcastReceiver _mPhoneStateBroadcastReceiver;
 
 	// hangup and hide keyboard image button
 	private ImageButton _mHangupBtn;
 	private ImageButton _mHideKeyboardBtn;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// keep outgoing call activity screen on
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		// set content view
 		setContentView(R.layout.outgoing_call_activity_layout);
 
 		// define outgoing call mode, callback
 		SipCallMode _outgoingCallMode = SipCallMode.CALLBACK;
 
 		// set sip services sip invite state listener
 		SIPSERVICES.setSipInviteStateListener(this);
 
 		// define phone state intent filter and default filter action is phone
 		// state
 		IntentFilter _phoneStateIntentFilter = new IntentFilter(
 				"android.intent.action.PHONE_STATE");
 
 		// add phone state intent filter action, new outgoing call
 		_phoneStateIntentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
 
 		// set phone state broadcast receiver and register it
 		_mPhoneStateBroadcastReceiver = new PhoneStateBroadcastReceiver();
 		registerReceiver(_mPhoneStateBroadcastReceiver, _phoneStateIntentFilter);
 
 		// get the intent parameter data
 		Bundle _data = getIntent().getExtras();
 
 		// check the data bundle and get call phone
 		if (null != _data) {
 			// check and reset outgoing call mode
 			if (null != _data.get(OUTGOING_CALL_MODE)) {
 				_outgoingCallMode = (SipCallMode) _data.get(OUTGOING_CALL_MODE);
 			}
 
 			// init outgoing call phone and set callee textView text
 			if (null != _data.getString(OUTGOING_CALL_PHONE)) {
 				_mCalleePhone = _data.getString(OUTGOING_CALL_PHONE);
 
 				((TextView) findViewById(R.id.callee_textView))
 						.setText(null != _data
 								.getString(OUTGOING_CALL_OWNERSHIP) ? _data
 								.getString(OUTGOING_CALL_OWNERSHIP)
 								: _mCalleePhone);
 			}
 		}
 
 		// init audio manager
 		_mAudioManager = (AudioManager) this
 				.getSystemService(Context.AUDIO_SERVICE);
 
 		// set wallpaper as outgoing call background
 		((ImageView) findViewById(R.id.outgoingcall_background_imageView))
 				.setImageDrawable(getWallpaper());
 
 		// get call controller gridView
 		GridView _callControllerGridView = (GridView) findViewById(R.id.callController_gridView);
 
 		// set call controller gridView adapter
 		_callControllerGridView.setAdapter(generateCallControllerAdapter());
 
 		// set call controller gridView on item click listener
 		_callControllerGridView
 				.setOnItemClickListener(new CallControllerGridViewOnItemClickListener());
 
 		// add hide contacts list button on click listener
 		((Button) findViewById(R.id.hide_contactslist_button))
 				.setOnClickListener(new HideContactsListOnClickListener());
 
 		// get contacts in address book list view
 		ListView _abContactsListView = (ListView) findViewById(R.id.contactInAB_listView);
 
 		// set contact in address book listView adapter
 		_abContactsListView.setAdapter(ContactListTabContentActivity
 				.getInABContactAdapter(this));
 		// init address book contacts listView quick alphabet bar and add on
 		// touch listener
 		new ListViewQuickAlphabetBar(_abContactsListView)
 				.setOnTouchListener(new ContactsInABListViewQuickAlphabetBarOnTouchListener());
 
 		// set keyboard gridView adapter
 		((GridView) findViewById(R.id.keyboard_gridView))
 				.setAdapter(generateKeyboardAdapter());
 
 		// get back for waiting callback call button
 		ImageButton _back4waitingCallbackCallImgBtn = (ImageButton) findViewById(R.id.back4waiting_callbackCall_button);
 
 		// bind back for waiting callback call button on click listener
 		_back4waitingCallbackCallImgBtn
 				.setOnClickListener(new Back4WaitingCallbackCallBtnOnClickListener());
 
 		// set hangup outgoing call button and bind its on click listener
 		_mHangupBtn = (ImageButton) findViewById(R.id.hangup_button);
 		_mHangupBtn
 				.setOnClickListener(new HangupOutgoingCallBtnOnClickListener());
 
 		// set hide keyboard button and bind its on click listener
 		_mHideKeyboardBtn = (ImageButton) findViewById(R.id.hideKeyboard_button);
 		_mHideKeyboardBtn
 				.setOnClickListener(new HideKeyboardBtnOnClickListener());
 
 		// check outgoing call mode
 		switch (_outgoingCallMode) {
 		case DIRECT_CALL:
 			// hide back for waiting callback call button, show call controller
 			// gridView and call controller footer linearLayout
 			_back4waitingCallbackCallImgBtn.setVisibility(View.GONE);
 
 			_callControllerGridView.setVisibility(View.VISIBLE);
 			((LinearLayout) findViewById(R.id.callController_footerLinearLayout))
 					.setVisibility(View.VISIBLE);
 
 			break;
 
 		case CALLBACK:
 		default:
 			// nothing to do
 			break;
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		// nothing to do
 	}
 
 	@Override
 	protected void onDestroy() {
 		// unregister phone state broadcast receiver
 		unregisterReceiver(_mPhoneStateBroadcastReceiver);
 
 		super.onDestroy();
 	}
 
 	@Override
 	public void onCallInitializing() {
 		// update call state textView text
 		((TextView) findViewById(R.id.callState_textView))
 				.setText(R.string.outgoing_call_trying);
 	}
 
 	@Override
 	public void onCallEarlyMedia() {
 		// update call state textView text
 		((TextView) findViewById(R.id.callState_textView))
 				.setText(R.string.outgoing_call_earlyMedia7RemoteRing);
 	}
 
 	@Override
 	public void onCallRemoteRinging() {
 		// update call state textView text
 		((TextView) findViewById(R.id.callState_textView))
 				.setText(R.string.outgoing_call_earlyMedia7RemoteRing);
 	}
 
 	@Override
 	public void onCallSpeaking() {
 		// check current sip voice call using loudspeaker
 		if (SIPSERVICES.isSipVoiceCallUsingLoudspeaker()) {
 			// set current sip voice call loudspeaker
 			// set mode
 			_mAudioManager.setMode(AudioManager.MODE_NORMAL);
 
 			// open speaker
 			_mAudioManager.setSpeakerphoneOn(true);
 		}
 
 		// increase call duration time per second using timer task
 		CALLDURATION_TIMER.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				// increase call duration time
				_mCallDutation++;
 
 				// update call duration time
 				UPDATE_CALLDURATIONTIME_HANDLE.post(new Runnable() {
 
 					// 60 seconds per minute
 					Integer SECONDS_PER_MINUTE = 60;
 
 					@Override
 					public void run() {
 						// get call duration minutes and seconds
 						Long _durationMinutes = _mCallDutation
 								/ SECONDS_PER_MINUTE;
 						Integer _durationSeconds = (int) (_mCallDutation % SECONDS_PER_MINUTE);
 
 						// format call duration
 						StringBuilder _callDurationTimeFormat = new StringBuilder();
 						_callDurationTimeFormat
 								.append(_durationMinutes <= 9 ? "0"
 										+ _durationMinutes : _durationMinutes)
 								.append(":")
 								.append(_durationSeconds <= 9 ? "0"
 										+ _durationSeconds : _durationSeconds);
 
 						// update call state textView text using call duration
 						((TextView) findViewById(R.id.callState_textView))
 								.setText(_callDurationTimeFormat);
 					}
 				});
 			}
 		}, 0, 1000);
 	}
 
 	@Override
 	public void onCallFailed() {
 		// update call state textView text
 		((TextView) findViewById(R.id.callState_textView))
 				.setText(R.string.outgoing_call_failed);
 
 		// sip voice call terminated
 		onCallTerminated();
 	}
 
 	@Override
 	public void onCallTerminating() {
 		// update call state textView text
 		((TextView) findViewById(R.id.callState_textView))
 				.setText(R.string.end_outgoing_call);
 
 		onCallTerminated();
 	}
 
 	@Override
 	public void onCallTerminated() {
 		// terminate current sip voice call
 		terminateSipVoiceCall(SipVoiceCallTerminatedType.PASSIVE);
 	}
 
 	public SendCallbackSipVoiceCallHttpRequestListener getSendCallbackSipVoiceCallHttpRequestListener() {
 		return SEND_CALLBACKSIPVOICECALL_HTTPREQUESTLISTENER;
 	}
 
 	// generate call controller adapter
 	private ListAdapter generateCallControllerAdapter() {
 		// call controller item adapter data key
 		final String CALL_CONTROLLER_ITEM_PARENTRELATIVELAYOUT = "call_controller_item_parentRelativeLayout";
 		final String CALL_CONTROLLER_ITEM_ICON = "call_controller_item_icon";
 		final String CALL_CONTROLLER_ITEM_LABEL = "call_controller_item_label";
 
 		// define call controller gridView content and mute or unmute, setting
 		// using loudspeaker or earphone on touch listener
 		final int[][] _callControllerGridViewContentArray = new int[][] {
 				{ R.drawable.callcontroller_contactitem6keyboard_1btn_bg,
 						R.drawable.img_callcontroller_contactitem_normal,
 						R.string.callController_contactItem_text },
 				{ R.drawable.callcontroller_keyboarditem6keyboard_3btn_bg,
 						R.drawable.img_callcontroller_keyboarditem_normal,
 						R.string.callController_keyboardItem_text },
 				{ R.drawable.callcontroller_muteitem_bg,
 						R.drawable.img_callcontroller_muteitem_normal,
 						R.string.callController_muteItem_text },
 				{ R.drawable.callcontroller_handfreeitem_bg,
 						R.drawable.img_callcontroller_handfreeitem_normal,
 						R.string.callController_handfreeItem_text } };
 
 		final OnTouchListener[] _callControllerGridViewOnTouchListenerArray = new OnTouchListener[] {
 				new Mute6UnmuteCallControllerGridViewOnTouchListener(),
 				new SetUsingLoudspeaker6EarphoneCallControllerGridViewOnTouchListener() };
 
 		// set call controller data list
 		List<Map<String, ?>> _callControllerDataList = new ArrayList<Map<String, ?>>();
 
 		for (int i = 0; i < _callControllerGridViewContentArray.length; i++) {
 			// generate data
 			Map<String, Object> _dataMap = new HashMap<String, Object>();
 
 			// put value
 			// generate call controller item parent relative layout data map
 			Map<String, Object> _callControllerItemParentRelaviteLayoutData = new HashMap<String, Object>();
 
 			// put call controller item data value
 			_callControllerItemParentRelaviteLayoutData.put(
 					CALL_CONTROLLER_ITEM_BACKGROUND,
 					_callControllerGridViewContentArray[i][0]);
 			if (2 <= i && 3 >= i) {
 				_callControllerItemParentRelaviteLayoutData.put(
 						CALL_CONTROLLER_ITEM_ONTOUCHLISTENER,
 						_callControllerGridViewOnTouchListenerArray[i - 2]);
 			}
 
 			_dataMap.put(CALL_CONTROLLER_ITEM_PARENTRELATIVELAYOUT,
 					_callControllerItemParentRelaviteLayoutData);
 			_dataMap.put(CALL_CONTROLLER_ITEM_ICON,
 					_callControllerGridViewContentArray[i][1]);
 			_dataMap.put(CALL_CONTROLLER_ITEM_LABEL,
 					_callControllerGridViewContentArray[i][2]);
 
 			// add data to list
 			_callControllerDataList.add(_dataMap);
 		}
 
 		return new OutgoingCallControllerAdapter(
 				this,
 				_callControllerDataList,
 				R.layout.call_controller_item,
 				new String[] { CALL_CONTROLLER_ITEM_PARENTRELATIVELAYOUT,
 						CALL_CONTROLLER_ITEM_ICON, CALL_CONTROLLER_ITEM_LABEL },
 				new int[] { R.id.callController_item_relativeLayout,
 						R.id.callController_item_iconImgView,
 						R.id.callController_item_labelTextView });
 	}
 
 	// generate keyboard adapter
 	private ListAdapter generateKeyboardAdapter() {
 		// keyboard adapter data key
 		final String KEYBOARD_BUTTON = "keyboard_button";
 
 		// define keyboard gridView image resource content
 		final int[] _keyboardGridViewImgResourceContentArray = {
 				R.drawable.img_dial_1_btn, R.drawable.img_dial_2_btn,
 				R.drawable.img_dial_3_btn, R.drawable.img_dial_4_btn,
 				R.drawable.img_dial_5_btn, R.drawable.img_dial_6_btn,
 				R.drawable.img_dial_7_btn, R.drawable.img_dial_8_btn,
 				R.drawable.img_dial_9_btn, R.drawable.img_dial_star_btn,
 				R.drawable.img_dial_0_btn, R.drawable.img_dial_pound_btn };
 
 		// set keyboard button data list
 		List<Map<String, ?>> _keyboardButtonDataList = new ArrayList<Map<String, ?>>();
 
 		for (int i = 0; i < _keyboardGridViewImgResourceContentArray.length; i++) {
 			// generate data
 			Map<String, Object> _dataMap = new HashMap<String, Object>();
 
 			// value map
 			Map<String, Object> _valueMap = new HashMap<String, Object>();
 			_valueMap.put(KEYBOARD_BUTTON_CODE, i);
 			_valueMap.put(KEYBOARD_BUTTON_IMAGE,
 					_keyboardGridViewImgResourceContentArray[i]);
 			switch (i) {
 			case 0:
 				// set top left keyboard button background
 				_valueMap.put(KEYBOARD_BUTTON_BGRESOURCE,
 						R.drawable.callcontroller_contactitem6keyboard_1btn_bg);
 				break;
 
 			case 2:
 				// set top right keyboard button background
 				_valueMap
 						.put(KEYBOARD_BUTTON_BGRESOURCE,
 								R.drawable.callcontroller_keyboarditem6keyboard_3btn_bg);
 				break;
 
 			case 9:
 				// set bottom left keyboard button background
 				_valueMap.put(KEYBOARD_BUTTON_BGRESOURCE,
 						R.drawable.callcontroller_keyboard_starbtn_bg);
 				break;
 
 			case 11:
 				// set bottom right keyboard button background
 				_valueMap.put(KEYBOARD_BUTTON_BGRESOURCE,
 						R.drawable.callcontroller_keyboard_poundbtn_bg);
 				break;
 
 			default:
 				// set normal keyboard button background
 				_valueMap.put(KEYBOARD_BUTTON_BGRESOURCE,
 						R.drawable.keyboard_btn_bg);
 				break;
 			}
 			_valueMap.put(KEYBOARD_BUTTON_ONCLICKLISTENER,
 					new KeyboardBtnOnClickListener());
 
 			// put value
 			_dataMap.put(KEYBOARD_BUTTON, _valueMap);
 
 			// add data to list
 			_keyboardButtonDataList.add(_dataMap);
 		}
 
 		return new OutgoingCallKeyboardAdapter(this, _keyboardButtonDataList,
 				R.layout.keyboard_btn_layout, new String[] { KEYBOARD_BUTTON },
 				new int[] { R.id.keyboardBtn_imageBtn });
 	}
 
 	// show or hide keyboard
 	private void show6hideKeyboard(boolean isShowKeyboard) {
 		// get keyboard gridView, call controller gridView and dtmf textView
 		// text
 		GridView _keyboardGridView = (GridView) findViewById(R.id.keyboard_gridView);
 		GridView _callControllerGridView = (GridView) findViewById(R.id.callController_gridView);
 		TextView _dtmfTextView = (TextView) findViewById(R.id.dtmf_textView);
 
 		// check is show keyboard
 		if (isShowKeyboard) {
 			// show hide keyboard image button
 			_mHideKeyboardBtn.setVisibility(View.VISIBLE);
 
 			// show keyboard gridView and hide call controller gridView
 			_keyboardGridView.setVisibility(View.VISIBLE);
 			_callControllerGridView.setVisibility(View.GONE);
 
 			// reset hangup image button source image
 			_mHangupBtn.setImageResource(R.drawable.img_hangup_btn_short);
 
 			// clear dtmf textView text
 			_dtmfTextView.setText("");
 		} else {
 			// hide hide keyboard image button
 			_mHideKeyboardBtn.setVisibility(View.GONE);
 
 			// hide keyboard gridView and show call controller gridView
 			_keyboardGridView.setVisibility(View.GONE);
 			_callControllerGridView.setVisibility(View.VISIBLE);
 
 			// reset hangup image button source image
 			_mHangupBtn.setImageResource(R.drawable.img_hangup_btn_long);
 
 			// show callee textView and hide dtmf textView
 			((TextView) findViewById(R.id.callee_textView))
 					.setVisibility(View.VISIBLE);
 			_dtmfTextView.setVisibility(View.GONE);
 		}
 	}
 
 	// terminate sip voice call
 	private void terminateSipVoiceCall(SipVoiceCallTerminatedType terminatedType) {
 		// update outgoingCall activity UI
 		// disable hangup and hide keyboard button
 		if (_mHangupBtn.isShown()) {
 			_mHangupBtn.setEnabled(false);
 		}
 		if (_mHideKeyboardBtn.isShown()) {
 			_mHideKeyboardBtn.setEnabled(false);
 		}
 
 		// check sip voice call terminated type
 		switch (terminatedType) {
 		case INITIATIVE:
 			// hangup current sip voice call
 			if (!SIPSERVICES.hangupSipVoiceCall(_mCallDutation)) {
 				// cancel call duration timer
 				CALLDURATION_TIMER.cancel();
 
 				// force finish outgoing call activity
 				finish();
 
 				// return immediately
 				return;
 			} else {
 				// update call state textView text
 				((TextView) findViewById(R.id.callState_textView))
 						.setText(R.string.end_outgoing_call);
 			}
 
 			break;
 
 		case PASSIVE:
 		default:
 			// update call log call duration time
 			SIPSERVICES.updateSipVoiceCallLog(_mCallDutation);
 
 			break;
 		}
 
 		// cancel call duration timer
 		CALLDURATION_TIMER.cancel();
 
 		// delayed 0.5 second to terminating
 		new Handler().postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				// update call state textView text
 				((TextView) findViewById(R.id.callState_textView))
 						.setText(R.string.outgoing_call_ended);
 
 				// delayed 0.6 second to back
 				new Handler().postDelayed(new Runnable() {
 
 					@Override
 					public void run() {
 						// finish outgoing call activity
 						finish();
 					}
 				}, 600);
 			}
 		}, 500);
 	}
 
 	// inner class
 	// call controller gridView on item click listener
 	class CallControllerGridViewOnItemClickListener implements
 			OnItemClickListener {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			// check call controller gridView item on clicked
 			switch (position) {
 			case 0:
 				// show contacts list
 				// open contacts list sliding drawer
 				((SlidingDrawer) findViewById(R.id.contactslist_slidingDrawer))
 						.animateOpen();
 
 				break;
 
 			case 1:
 				// show keyboard gridView and hide keyboard image button
 				show6hideKeyboard(true);
 
 				break;
 
 			case 2:
 				Log.e(LOG_TAG,
 						"Mute or unmute call controller gridView item on item click listener error");
 
 				break;
 
 			case 3:
 				Log.e(LOG_TAG,
 						"Handfree or cancel handfree call controller gridView item on item click listener error");
 
 				break;
 			}
 		}
 	}
 
 	// hide contacts list on click listener
 	class HideContactsListOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			// hide contacts list
 			// close contacts list sliding drawer
 			((SlidingDrawer) findViewById(R.id.contactslist_slidingDrawer))
 					.animateClose();
 		}
 
 	}
 
 	// mute and unmute call controller gridView on item touch listener
 	class Mute6UnmuteCallControllerGridViewOnTouchListener implements
 			OnTouchListener {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if (MotionEvent.ACTION_DOWN == event.getAction()) {
 				// check current sip voice call is muted
 				// muted now, unmute it
 				if (SIPSERVICES.isSipVoiceCallMuted()) {
 					// update background resource
 					((RelativeLayout) v)
 							.setBackgroundResource(R.drawable.callcontroller_muteitem_bg);
 
 					// unmute current sip voice call
 					SIPSERVICES.unmuteSipVoiceCall();
 				}
 				// unmuted now, mute it
 				else {
 					// update background resource
 					((RelativeLayout) v)
 							.setBackgroundResource(R.drawable.callcontroller_unmuteitem_bg);
 
 					// mute current sip voice call
 					SIPSERVICES.muteSipVoiceCall();
 				}
 			}
 
 			return true;
 		}
 
 	}
 
 	// set using loudspeaker and earphone call controller gridView on item touch
 	// listener
 	class SetUsingLoudspeaker6EarphoneCallControllerGridViewOnTouchListener
 			implements OnTouchListener {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if (MotionEvent.ACTION_DOWN == event.getAction()) {
 				// check current sip voice call using loudspeaker or earphone
 				// using loudspeaker now, set using earphone
 				if (SIPSERVICES.isSipVoiceCallUsingLoudspeaker()) {
 					// update background resource
 					((RelativeLayout) v)
 							.setBackgroundResource(R.drawable.callcontroller_handfreeitem_bg);
 
 					// set using earphone
 					SIPSERVICES.setSipVoiceCallUsingEarphone();
 				}
 				// using earphone now, set using loudspeaker
 				else {
 					// update background resource
 					((RelativeLayout) v)
 							.setBackgroundResource(R.drawable.callcontroller_cancelhandfreeitem_bg);
 
 					// set using loudspeaker
 					SIPSERVICES.setSipVoiceCallUsingLoudspeaker();
 				}
 			}
 
 			return true;
 		}
 
 	}
 
 	// keyboard button on click listener
 	class KeyboardBtnOnClickListener implements OnClickListener {
 
 		// define keyboard button value data
 		private final String[] _keyboardPhoneButtonValueData = new String[] {
 				"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#" };
 
 		@Override
 		public void onClick(View v) {
 			// get callee and dtmf textView
 			TextView _calleeTextView = (TextView) findViewById(R.id.callee_textView);
 			TextView _dtmfTextView = (TextView) findViewById(R.id.dtmf_textView);
 
 			// check callee and dtmf textView visible
 			if (_calleeTextView.isShown()) {
 				// hide callee textView
 				_calleeTextView.setVisibility(View.GONE);
 			}
 			if (!_dtmfTextView.isShown()) {
 				// show dtmf textView
 				_dtmfTextView.setVisibility(View.VISIBLE);
 			}
 
 			// get clicked phone number
 			String _clickedPhoneNumber = _keyboardPhoneButtonValueData[(Integer) v
 					.getTag()];
 
 			// define keyboard phone string builder
 			StringBuilder _keyboardPhoneStringBuilder = new StringBuilder(
 					_dtmfTextView.getText());
 
 			// dial phone
 			_keyboardPhoneStringBuilder.append(_clickedPhoneNumber);
 
 			// reset dtmf textView text
 			_dtmfTextView.setText(_keyboardPhoneStringBuilder);
 
 			// play keyboard phone button dtmf sound
 			// get volume
 			float _volume = _mAudioManager
 					.getStreamVolume(AudioManager.STREAM_MUSIC);
 
 			// play dial phone button dtmf sound with index
 			SOUND_POOL.play(DialTabContentActivity
 					.getDialPhoneBtnDTMFSoundPoolMap()
 					.get((Integer) v.getTag()), _volume, _volume, 0, 0, 1f);
 
 			// send dtmf
 			SIPSERVICES.sentDTMF(_clickedPhoneNumber);
 		}
 	}
 
 	// back for waiting callback call button on click listener
 	class Back4WaitingCallbackCallBtnOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			// finish outgoing call activity
 			finish();
 		}
 
 	}
 
 	// hangup outgoing call button on click listener
 	class HangupOutgoingCallBtnOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			// terminate current sip voice call
 			terminateSipVoiceCall(SipVoiceCallTerminatedType.INITIATIVE);
 		}
 
 	}
 
 	// hide keyboard button on click listener
 	class HideKeyboardBtnOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			// hide keyboard gridView and hide keyboard image button
 			show6hideKeyboard(false);
 		}
 
 	}
 
 	// send callback sip voice call http request listener
 	class SendCallbackSipVoiceCallHttpRequestListener extends
 			OnHttpRequestListener {
 
 		// define send callback sip voice call state tip text id, callback
 		// waiting imageView image resource id and callback waiting textView
 		// text
 		Integer _sendCallbackSipVoiceCallStateTipTextId = R.string.send_callbackCallRequest_failed;
 		Integer _callbackCallWaitingImageViewImgResId = drawable.img_sendcallbackcall_failed;
 		String _callbackCallWaitingTextViewText = AppLaunchActivity
 				.getAppContext().getResources()
 				.getString(R.string.callbackWaiting_textView_failed);
 
 		@Override
 		public void onFinished(HttpRequest request, HttpResponse response) {
 			// check send callback sip voice call request response
 			checkSendCallbackSipVoiceCallRequestResponse(true);
 		}
 
 		@Override
 		public void onFailed(HttpRequest request, HttpResponse response) {
 			Log.e(LOG_TAG, "Send callback sip voice call http request failed");
 
 			// check send callback sip voice call request response
 			checkSendCallbackSipVoiceCallRequestResponse(false);
 		}
 
 		// check send callback sip voice call request response
 		private void checkSendCallbackSipVoiceCallRequestResponse(
 				Boolean isSuccess) {
 			// update send callback sip voice call state tip text id, callback
 			// waiting imageView image resource id and callback waiting textView
 			// text
 			if (isSuccess) {
 				_sendCallbackSipVoiceCallStateTipTextId = R.string.send_callbackCallRequest_succeed;
 				_callbackCallWaitingImageViewImgResId = drawable.img_sendcallbackcall_succeed;
 				_callbackCallWaitingTextViewText = getResources()
 						.getString(R.string.callbackWaiting_textView_succeed)
 						.replaceFirst("\\*\\*\\*", "caller")
 						.replace("***", _mCalleePhone);
 			}
 
 			// update call state textView text
 			((TextView) findViewById(R.id.callState_textView))
 					.setText(_sendCallbackSipVoiceCallStateTipTextId);
 
 			// update callback waiting imageView image resource
 			((ImageView) findViewById(R.id.callbackWaiting_imageView))
 					.setImageResource(_callbackCallWaitingImageViewImgResId);
 
 			// update callback waiting textView text
 			((TextView) findViewById(R.id.callbackWaiting_textView))
 					.setText(_callbackCallWaitingTextViewText);
 
 			// show callback waiting relativeLayout
 			((RelativeLayout) findViewById(R.id.callbackWaiting_relativeLayout))
 					.setVisibility(View.VISIBLE);
 		}
 
 	}
 
 	// phone state broadcast receiver
 	class PhoneStateBroadcastReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// check the action for phone state
 			if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
 				// outgoing call
 				Log.d(LOG_TAG, "Hava a outgoing call");
 
 				//
 			} else {
 				// incoming call
 				// get telephone manager
 				TelephonyManager _telephoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
 				// check incoming call state
 				switch (_telephoneManager.getCallState()) {
 				case TelephonyManager.CALL_STATE_RINGING:
 					// finish outgoing call activity for making callback sip
 					// voice call if has incoming call
 					finish();
 
 					break;
 
 				default:
 					// nothing to do
 					break;
 				}
 			}
 		}
 
 	}
 
 	// sip voice call terminated type
 	enum SipVoiceCallTerminatedType {
 		// initiative or passive
 		INITIATIVE, PASSIVE
 	}
 
 }
