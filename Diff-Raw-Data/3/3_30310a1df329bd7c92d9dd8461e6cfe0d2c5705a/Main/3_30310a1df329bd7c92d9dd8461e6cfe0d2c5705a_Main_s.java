 
 package org.projectvoodoo.rrc.activities;
 
 import java.io.IOException;
 
 import org.projectvoodoo.rrc.App;
 import org.projectvoodoo.rrc.Utils;
 import org.projectvoodoo.rrc.samsung.FdormancyPreferences;
 import org.projectvoodoo.rrc.samsung.NwkInfo;
 import org.projectvoodoo.rrctool.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends Activity implements OnSeekBarChangeListener, OnCheckedChangeListener,
         OnClickListener {
 
     @SuppressWarnings("unused")
     private static final String TAG = "Voodoo RRC Tool";
 
     private TextView mHasDormPolicyTv;
     private TextView mFdTimerTv;
     private SeekBar mFdTimerSeek;
     private CheckBox mEnableFdPrefCheck;
     private CheckBox mEnableFdPolicyCheck;
     private LinearLayout mServiceModeButtons;
     private LinearLayout mFdPrefsLayout;
     private LinearLayout mFdActionsLayout;
     private Button mApplyButton;
 
     private boolean gainRoot;
 
     private Integer mNetworkId;
     private String mNetworkName;
 
     private NwkInfo mNwkInfo;
     private FdormancyPreferences mFdormPrefs;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         boolean hasServiceMode = false;
         try {
             PackageManager pm = getPackageManager();
 
             int state = pm.getApplicationEnabledSetting("com.sec.android.app.servicemodeapp");
             if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                     || state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
                 hasServiceMode = true;
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         mServiceModeButtons = (LinearLayout) findViewById(R.id.servicemode_buttons);
         mServiceModeButtons.setVisibility(hasServiceMode ? View.VISIBLE : View.GONE);
 
         mHasDormPolicyTv = (TextView) findViewById(R.id.has_dormpolicy);
         mApplyButton = (Button) findViewById(R.id.button_apply);
         mApplyButton.setEnabled(false);
         mApplyButton.setOnClickListener(this);
         ((Button) findViewById(R.id.button_show_rrc)).setOnClickListener(this);
         ((Button) findViewById(R.id.button_show_sysdump)).setOnClickListener(this);
 
         mFdPrefsLayout = (LinearLayout) findViewById(R.id.fd_prefs);
         mFdPrefsLayout.setVisibility(View.GONE);
 
         mFdActionsLayout = (LinearLayout) findViewById(R.id.fd_actions);
         mFdActionsLayout.setVisibility(View.GONE);
 
         mEnableFdPrefCheck = (CheckBox) findViewById(R.id.enable_fd_pref_check);
         mEnableFdPolicyCheck = (CheckBox) findViewById(R.id.enable_fd_policy_check);
         mFdTimerSeek = (SeekBar) findViewById(R.id.fd_policy_timer);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         TextView networkIdTv = (TextView) findViewById(R.id.network_id);
         TextView networkNameTv = (TextView) findViewById(R.id.network_name);
 
         TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 
         boolean mustQuit = false;
         try {
             mNetworkId = Integer.parseInt(tm.getNetworkOperator());
             mNetworkName = tm.getSimOperatorName();
         } catch (Exception e) {
             mustQuit = true;
         }
         if (mNetworkId != null && mNetworkId == 0)
             mustQuit = true;
 
         if (mustQuit) {
             Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
             finish();
             return;
         }
 
         networkIdTv.setText(mNetworkId + "");
         networkNameTv.setText(mNetworkName);
 
         try {
             mFdormPrefs = new FdormancyPreferences();
             mEnableFdPrefCheck.setOnCheckedChangeListener(this);
             mEnableFdPrefCheck.setChecked(mFdormPrefs.isEnabled(mNetworkId));
             mFdPrefsLayout.setVisibility(View.VISIBLE);
 
         } catch (Exception e) {
         }
 
         new NwkInfoReadTask().execute();
     }
 
     class NwkInfoReadTask extends AsyncTask<Void, Void, Void> {
 
         @Override
         protected Void doInBackground(Void... params) {
 
             gainRoot = Utils.canGetRootPermission();
             try {
                 mNwkInfo = new NwkInfo(mNetworkId, mNetworkName);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result) {
 
             if (!gainRoot)
                 mApplyButton.setEnabled(false);
 
             if (mNwkInfo == null)
                 return;
 
             mHasDormPolicyTv.setText(mNwkInfo.isInDb() ?
                     R.string.has_dormpolicy : R.string.has_no_dormpolicy);
 
             mEnableFdPolicyCheck.setChecked(mNwkInfo.isFdEnabled());
             mEnableFdPolicyCheck.setOnCheckedChangeListener(Main.this);
 
             mFdTimerSeek.setMax(40);
             mFdTimerSeek.setEnabled(mNwkInfo.isFdEnabled());
             mFdTimerSeek.setProgress(mNwkInfo.getFdTime());
             mFdTimerSeek.setOnSeekBarChangeListener(Main.this);
 
             mFdTimerTv = (TextView) findViewById(R.id.fd_policy_timer_show);
             updateFdText();
 
             if (mFdormPrefs == null
                     || mFdormPrefs != null && mFdormPrefs.isEnabled(mNetworkId))
                 mFdActionsLayout.setVisibility(View.VISIBLE);
 
             setApplyButton();
         }
     }
 
     class NwkInfoApplyTask extends AsyncTask<Void, Void, Boolean> {
 
         @Override
         protected void onPreExecute() {
             mApplyButton.setEnabled(false);
         }
 
         @Override
         protected Boolean doInBackground(Void... params) {
 
             boolean requiresReboot = false;
 
             try {
                 requiresReboot = mNwkInfo.apply();
                 Thread.sleep(2000);
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
 
             return requiresReboot;
         }
 
         @Override
         protected void onPostExecute(Boolean requiresReboot) {
             if (requiresReboot)
                 Toast.makeText(App.context, R.string.require_reboot, Toast.LENGTH_LONG).show();
 
             mApplyButton.setEnabled(true);
             new NwkInfoReadTask().execute();
         }
     }
 
     private final void launchSecret(String code) {
         String ussdCode = "*" + Uri.encode("#" + code + "#");
         startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ussdCode)));
     }
 
     @Override
     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         if (progress == 0 && fromUser)
             progress = 1;
 
        updateFdText();
         mNwkInfo.setTimes(progress, 0);
     }
 
     @Override
     public void onStartTrackingTouch(SeekBar seekBar) {
     }
 
     @Override
     public void onStopTrackingTouch(SeekBar seekBar) {
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 
         switch (buttonView.getId()) {
             case R.id.enable_fd_pref_check:
                 mFdActionsLayout.setVisibility(isChecked && mNwkInfo != null ?
                         View.VISIBLE : View.GONE);
                 mFdormPrefs.setEnabled(mNetworkId, isChecked);
                 break;
 
             case R.id.enable_fd_policy_check:
                 mFdTimerSeek.setEnabled(isChecked);
                 mNwkInfo.setFdEnabled(isChecked);
 
                 break;
         }
     }
 
     private void setApplyButton() {
         mApplyButton.setEnabled(gainRoot && mNwkInfo != null || mFdormPrefs != null);
     }
 
     private void updateFdText() {
         mFdTimerTv.setText("FD Timer: " + mNwkInfo.getFdTime() + "s");
     }
 
     @Override
     public void onClick(View v) {
 
         switch (v.getId()) {
             case R.id.button_apply:
                 if (mFdormPrefs != null)
                     mFdormPrefs.write();
 
                 new NwkInfoApplyTask().execute();
                 break;
 
             case R.id.button_show_rrc:
                 launchSecret("0011");
                 break;
 
             case R.id.button_show_sysdump:
                 launchSecret("9900");
                 break;
 
             default:
                 break;
         }
 
     }
 }
