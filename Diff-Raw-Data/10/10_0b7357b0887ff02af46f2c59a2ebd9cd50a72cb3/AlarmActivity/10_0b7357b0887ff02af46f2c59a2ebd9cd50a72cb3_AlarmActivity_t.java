 
 package com.mattprecious.prioritysms.activity;
 
 import android.text.Html;
 import android.widget.ImageView;
 import butterknife.InjectView;
 import butterknife.Views;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.mattprecious.prioritysms.R;
 import com.mattprecious.prioritysms.model.BaseProfile;
 import com.mattprecious.prioritysms.model.SmsProfile;
 import com.mattprecious.prioritysms.util.ContactHelper;
 import com.mattprecious.prioritysms.util.Intents;
 
 import net.sebastianopoggi.ui.GlowPadBackport.GlowPadView;
 
 import android.annotation.TargetApi;
 import android.app.NotificationManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 public class AlarmActivity extends SherlockFragmentActivity implements
         GlowPadView.OnTriggerListener {
 
     private static final String TAG = AlarmActivity.class.getSimpleName();
 
     private static final int WHAT_PING_MESSAGE = 101;
 
     private static final boolean ENABLE_PING_AUTO_REPEAT = true;
 
     private static final long PING_AUTO_REPEAT_DELAY_MSEC = 1200;
 
     @InjectView(R.id.contact_name)
     TextView mNameView;
 
     @InjectView(R.id.message)
     TextView mMessageView;
 
     @InjectView(R.id.image)
     ImageView mIconView;
 
     @InjectView(R.id.glow_pad_view)
     GlowPadView mGlowPadView;
 
     private BaseProfile mProfile;
 
     private SmsProfile mSmsProfile;
 
     private String mNumber;
 
     private String mName;
 
     private String mMessage;
 
     private boolean mPingEnabled = true;
 
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             Log.v(TAG, "onReceive " + action);
 
             if (action.equals(Intents.ACTION_DISMISS)) {
                 dismiss(false, false);
             } else if (action.equals(Intents.ACTION_REPLY)) {
                 reply();
             } else if (action.equals(Intents.ACTION_CALL)) {
                 call();
             } else {
                 BaseProfile profile = intent.getParcelableExtra(Intents.EXTRA_PROFILE);
                 boolean replaced = intent.getBooleanExtra(Intents.ALARM_REPLACED, false);
                 if (profile != null && mProfile.getId() == profile.getId()) {
                     dismiss(true, replaced);
                 }
             }
         }
     };
 
     private final Handler mPingHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case WHAT_PING_MESSAGE:
                     triggerPing();
                     break;
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent intent = getIntent();
         if (!intent.hasExtra(Intents.EXTRA_PROFILE)) {
             missingExtra(Intents.EXTRA_PROFILE);
         } else if (!intent.hasExtra(Intents.EXTRA_NUMBER)) {
             missingExtra(Intents.EXTRA_NUMBER);
         }
 
         mProfile = intent.getParcelableExtra(Intents.EXTRA_PROFILE);
         mNumber = intent.getStringExtra(Intents.EXTRA_NUMBER);
         mName = ContactHelper.getNameByNumber(this, mNumber);
 
         if (mProfile instanceof SmsProfile) {
             mSmsProfile = (SmsProfile) mProfile;
             if (intent.hasExtra(Intents.EXTRA_MESSAGE)) {
                 mMessage = intent.getStringExtra(Intents.EXTRA_MESSAGE);
             } else {
                 missingExtra(Intents.EXTRA_MESSAGE);
             }
         }
 
         final LayoutInflater inflater = LayoutInflater.from(this);
         final View rootView = inflater.inflate(R.layout.alarm, null);
         updateSystemUi(rootView);
         setContentView(rootView);
         Views.inject(this);
 
         final Window win = getWindow();
         win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                 | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                 | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                 | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                 | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
 
         String styledMessage = mMessage;
         if (mSmsProfile != null) {
             for (String keyword : mSmsProfile.getKeywords()) {
                 styledMessage = styledMessage.replace(keyword, String.format("<b>%s</b>", keyword));
             }

            mMessageView.setText(Html.fromHtml(styledMessage));
         }
 
         mNameView.setText(mName);
         mGlowPadView.setOnTriggerListener(this);
 
         mIconView.setImageResource((mProfile instanceof SmsProfile)
                 ? R.drawable.ic_alarm_message : R.drawable.ic_alarm_phone);
 
         triggerPing();
 
         IntentFilter filter = new IntentFilter(Intents.ALARM_KILLED);
         filter.addAction(Intents.ACTION_DISMISS);
         filter.addAction(Intents.ACTION_REPLY);
         filter.addAction(Intents.ACTION_CALL);
         registerReceiver(mReceiver, filter);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unregisterReceiver(mReceiver);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
     }
 
     @Override
     public void onBackPressed() {
         // don't allow the activity to be closed
         return;
     }
 
     private void missingExtra(String extra) {
         throw new IllegalArgumentException(String.format("Missing %s as an intent extra",
                 extra));
     }
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private void updateSystemUi(View view) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
         }
     }
 
     private void triggerPing() {
         if (mPingEnabled) {
             mGlowPadView.ping();
 
             if (ENABLE_PING_AUTO_REPEAT) {
                 mPingHandler
                         .sendEmptyMessageDelayed(WHAT_PING_MESSAGE, PING_AUTO_REPEAT_DELAY_MSEC);
             }
         }
     }
 
     private NotificationManager getNotificationManager() {
         return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
     }
 
     // Dismiss the alarm.
     private void dismiss(boolean killed, boolean replaced) {
         Log.v(TAG, "dismiss");
 
         Log.i(TAG, "Profile id=" + mProfile.getId()
                 + (killed ? (replaced ? " replaced" : " killed") : " dismissed by user"));
         // The service told us that the alarm has been killed, do not modify
         // the notification or stop the service.
         if (!killed) {
             // Cancel the notification and stop playing the alarm
             NotificationManager nm = getNotificationManager();
             nm.cancel(mProfile.getId());
             stopService(new Intent(Intents.ACTION_ALERT));
         }
         if (!replaced) {
             finish();
         }
     }
 
     private void reply() {
         startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", mNumber, null)));
         dismiss(false, false);
     }
 
     private void call() {
         startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mNumber, null)));
         dismiss(false, false);
     }
 
     @Override
     public void onFinishFinalAnimation() {
     }
 
     @Override
     public void onGrabbed(View v, int handle) {
         mPingEnabled = false;
     }
 
     @Override
     public void onGrabbedStateChange(View v, int handle) {
     }
 
     @Override
     public void onReleased(View v, int handle) {
         mPingEnabled = true;
         triggerPing();
     }
 
     @Override
     public void onTrigger(View v, int target) {
         final int resId = mGlowPadView.getResourceIdForTarget(target);
         switch (resId) {
             case android.R.drawable.ic_menu_call:
                 call();
                 break;
             case android.R.drawable.stat_notify_chat:
                 reply();
                 break;
             case android.R.drawable.ic_menu_close_clear_cancel:
                 dismiss(false, false);
                 break;
             default:
                 break;
         }
     }
 }
