 /*
  * Copyright (c) 2009, Code Aurora Forum. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *        * Redistributions of source code must retain the above copyright
  *            notice, this list of conditions and the following disclaimer.
  *        * Redistributions in binary form must reproduce the above copyright
  *            notice, this list of conditions and the following disclaimer in the
  *            documentation and/or other materials provided with the distribution.
  *        * Neither the name of Code Aurora nor
  *            the names of its contributors may be used to endorse or promote
  *            products derived from this software without specific prior written
  *            permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.quicinc.fmradio;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.text.TextUtils;
 
 import java.lang.ref.WeakReference;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 import java.util.ArrayList;
 
 import com.quicinc.utils.FrequencyPicker;
 import com.quicinc.utils.FrequencyPickerDialog;
 import android.content.ServiceConnection;
 
 import android.hardware.fmradio.FmConfig;
 
 
 public class FMRadio extends Activity
 {
    public static final String LOGTAG = "FMRadio";
 
    public static final boolean RECORDING_ENABLE = false;
 
    /* menu Identifiers */
    private static final int MENU_SCAN_START = Menu.FIRST + 2;
    private static final int MENU_SCAN_STOP = Menu.FIRST + 3;
    private static final int MENU_RECORD_START = Menu.FIRST + 4;
    private static final int MENU_RECORD_STOP = Menu.FIRST + 5;
    private static final int MENU_SLEEP = Menu.FIRST + 6;
    private static final int MENU_SLEEP_CANCEL = Menu.FIRST + 7;
    private static final int MENU_SETTINGS = Menu.FIRST + 8;
    private static final int MENU_WIRED_HEADSET = Menu.FIRST + 9;
 
    /* Dialog Identifiers */
    private static final int DIALOG_SEARCH = 1;
    private static final int DIALOG_SLEEP = 2;
    private static final int DIALOG_SELECT_PRESET_LIST = 3;
    private static final int DIALOG_PRESETS_LIST = 4;
    private static final int DIALOG_PRESET_LIST_RENAME = 5;
    private static final int DIALOG_PRESET_LIST_DELETE = 6;
    private static final int DIALOG_PRESET_LIST_AUTO_SET = 7;
    private static final int DIALOG_PICK_FREQUENCY = 8;
    private static final int DIALOG_PROGRESS_PROGRESS = 9;
    private static final int DIALOG_PRESET_OPTIONS = 10;
    private static final int DIALOG_PRESET_RENAME = 11;
    private static final int DIALOG_CMD_TIMEOUT = 12;
    private static final int DIALOG_CMD_FAILED = 13;
 
    /* Activity Return ResultIdentifiers */
    private static final int ACTIVITY_RESULT_SETTINGS = 1;
 
    /* Activity Return ResultIdentifiers */
    private static final int MAX_PRESETS_PER_PAGE = 5;
 
 
    /* Station's Audio is Stereo */
    private static final int FMRADIO_UI_STATION_AUDIO_STEREO = 1;
    /* Station's Audio is Mono */
    private static final int FMRADIO_UI_STATION_AUDIO_MONO = 2;
 
    /* The duration during which the "Sleep: xx:xx" string will be toggling
     */
    private static final int SLEEP_TOGGLE_SECONDS = 60;
 
    /* The number of Preset Stations to create.
     * The hardware supports a maximum of 12.
     */
    private static final int NUM_AUTO_PRESETS_SEARCH= 12;
    /*
     * Command time out: For asynchonous operations, if no response
     * is received with int this duration, a timeout msg will be displayed.
     */
    private static final int CMD_TIMEOUT_DELAY_MS = 5000;
    private static final int MSG_CMD_TIMEOUT = 101;
 
    private static final int CMD_NONE = 0;
    private static final int CMD_TUNE = 1;
    private static final int CMD_FMON = 2;
    private static final int CMD_FMOFF = 3;
    private static final int CMD_FMCONFIGURE = 4;
    private static final int CMD_MUTE = 5;
    private static final int CMD_SEEK = 6;
    private static final int CMD_SCAN = 7;
    private static final int CMD_SEEKPI = 8;
    private static final int CMD_SEARCHLIST = 9;
    private static final int CMD_CANCELSEARCH = 10;
    private static final int CMD_SET_POWER_MODE = 11;
    private static final int CMD_SET_AUDIO_MODE = 12;
    private static final int CMD_SET_AUTOAF = 13;
    private static final int CMD_GET_INTERNALANTENNA_MODE = 14;
 
 
    private static final int PRESETS_OPTIONS_TUNE = 0;
    private static final int PRESETS_OPTIONS_REPLACE = 1;
    private static final int PRESETS_OPTIONS_RENAME = 2;
    private static final int PRESETS_OPTIONS_DELETE = 3;
    private static final int PRESETS_OPTIONS_SEARCHPI = 4;
 
    private static IFMRadioService mService = null;
    private static FmSharedPreferences mPrefs;
 
    /* Button Resources */
    private ImageButton mOnOffButton;
    private ImageButton mMuteButton;
    /* Button to navigate Preset pages */
    private ImageButton mPresetPageButton;
    /* 6 Preset Buttons */
    private Button[] mPresetButtons = { null, null, null, null, null};
    private Button mPresetListButton;
    // private ImageButton mSearchButton;
    private ImageView mForwardButton;
    private ImageView mBackButton;
 
    /* Top row in the station info layout */
    private ImageView mRSSI;
    private TextView mProgramServiceTV;
    private TextView mStereoTV;
 
    /* Middle row in the station info layout */
    private TextView mTuneStationFrequencyTV;
    private TextView mStationCallSignTV;
    private TextView mProgramTypeTV;
 
    /* Bottom row in the station info layout */
    private TextView mRadioTextTV;
 
    /* Sleep and Recording Messages */
    private TextView mSleepMsgTV;
    private TextView mRecordingMsgTV;
 
    private double mOutputFreq;
    private int mPresetPageNumber = 0;
    private int mStereo = -1;
 
    // default audio device - speaker
    private static int mAudioRoute = FMRadioService.RADIO_AUDIO_DEVICE_WIRED_HEADSET;
 
    /* Current Status Indicators */
    private static boolean mRecording = false;
    private static boolean mIsScaning = false;
    private static boolean mIsSeeking = false;
    private static boolean mIsSearching = false;
    private static int mScanPty = 0;
 
    private Animation mAnimation = null;
    private ScrollerText mRadioTextScroller = null;
 
    private static PresetStation mTunedStation = new PresetStation("", 102100);
    private PresetStation mPresetButtonStation = null;
 
    /* Radio Vars */
    final Handler mHandler = new Handler();
    /* Search Progress Dialog */
    private ProgressDialog mProgressDialog=null;
 
    /* Asynchronous command active */
    private static int mCommandActive = 0;
 
    /* Command that failed (Sycnhronous or Asynchronous) */
    private static int mCommandFailed = 0;
 
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
 
       super.onCreate(savedInstanceState);
 
       mPrefs = new FmSharedPreferences(this);
       mCommandActive = CMD_NONE;
       mCommandFailed = CMD_NONE;
 
       Log.d(LOGTAG, "onCreate - Height : "+ getWindowManager().getDefaultDisplay().getHeight()
             + " - Width  : "+ getWindowManager().getDefaultDisplay().getWidth());
 
       requestWindowFeature(Window.FEATURE_NO_TITLE);
       setContentView(R.layout.fmradio);
       mAnimation = AnimationUtils.loadAnimation(this,
                                                 R.anim.preset_select);
 
       mMuteButton = (ImageButton) findViewById(R.id.btn_silent);
       mMuteButton.setOnClickListener(mMuteModeClickListener);
 
       mOnOffButton = (ImageButton) findViewById(R.id.btn_onoff);
       mOnOffButton.setOnClickListener(mTurnOnOffClickListener);
 
       mForwardButton = (ImageView) findViewById(R.id.btn_forward);
       mForwardButton.setOnClickListener(mForwardClickListener);
       mForwardButton.setOnLongClickListener(mForwardLongClickListener);
 
       mBackButton = (ImageView) findViewById(R.id.btn_back);
       mBackButton.setOnClickListener(mBackClickListener);
       mBackButton.setOnLongClickListener(mBackLongClickListener);
 
       mPresetPageButton = (ImageButton) findViewById(R.id.btn_preset_page);
       mPresetPageButton.setOnClickListener(mPresetsPageClickListener);
 
       mPresetListButton = (Button) findViewById(R.id.btn_presets_list);
       mPresetListButton.setOnClickListener(mPresetListClickListener);
       mPresetListButton
       .setOnLongClickListener(mPresetListButtonOnLongClickListener);
 
       /* 6 Preset Buttons */
       mPresetButtons[0] = (Button) findViewById(R.id.presets_button_1);
       mPresetButtons[1] = (Button) findViewById(R.id.presets_button_2);
       mPresetButtons[2] = (Button) findViewById(R.id.presets_button_3);
       mPresetButtons[3] = (Button) findViewById(R.id.presets_button_4);
       mPresetButtons[4] = (Button) findViewById(R.id.presets_button_5);
       for (int nButton = 0; nButton < MAX_PRESETS_PER_PAGE; nButton++)
       {
          mPresetButtons[nButton]
          .setOnClickListener(mPresetButtonClickListener);
          mPresetButtons[nButton]
          .setOnLongClickListener(mPresetButtonOnLongClickListener);
       }
 
       mTuneStationFrequencyTV = (TextView) findViewById(R.id.prog_frequency_tv);
       if (mTuneStationFrequencyTV != null)
       {
          mTuneStationFrequencyTV.setOnLongClickListener(mFrequencyViewClickListener);
       }
       mProgramServiceTV = (TextView) findViewById(R.id.prog_service_tv);
       mStereoTV = (TextView) findViewById(R.id.stereo_text_tv);
 
       mStationCallSignTV = (TextView) findViewById(R.id.call_sign_tv);
       mProgramTypeTV = (TextView) findViewById(R.id.pty_tv);
 
       mRadioTextTV = (TextView) findViewById(R.id.radio_text_tv);
       mSleepMsgTV = (TextView) findViewById(R.id.sleep_msg_tv);
       mRecordingMsgTV = (TextView) findViewById(R.id.record_msg_tv);
       /* Disable displaying RSSI */
       mRSSI = (ImageView) findViewById(R.id.signal_level);
       mRSSI.setVisibility(View.INVISIBLE);
 
       if ( (mRadioTextScroller == null) && (mRadioTextTV != null))
       {
          mRadioTextScroller = new ScrollerText(mRadioTextTV);
       }
 
       enableRadioOnOffUI(false);
       if (false == bindToService(this, osc))
       {
          Log.d(LOGTAG, "onCreate: Failed to Start Service");
       } else
       {
          Log.d(LOGTAG, "onCreate: Start Service completed successfully");
       }
    }
 
    @Override
    public void onRestart() {
       Log.d(LOGTAG, "FMRadio: onRestart");
       super.onRestart();
    }
 
    @Override
    public void onStop() {
       Log.d(LOGTAG, "FMRadio: onStop");
       super.onStop();
    }
 
    @Override
    public void onStart() {
       super.onStart();
       Log.d(LOGTAG, "FMRadio: onStart");
       try
       {
          if(mService != null)
          {
             mService.registerCallbacks(mServiceCallbacks);
          }
       }
       catch (RemoteException e)
       {
          e.printStackTrace();
       }
 
    }
 
    @Override
    protected void onPause() {
       super.onPause();
       mRadioTextScroller.stopScroll();
       FmSharedPreferences.setTunedFrequency(mTunedStation.getFrequency());
       mPrefs.Save();
    }
 
    @Override
    public void onResume() {
       super.onResume();
       Log.d(LOGTAG, "FMRadio: onResume");
       mPrefs.Load();
       PresetStation station = FmSharedPreferences.getStationFromFrequency(FmSharedPreferences.getTunedFrequency());
       if (station != null)
       {
          mTunedStation.Copy(station);
       }
       mHandler.post(mUpdateProgramService);
       mHandler.post(mUpdateRadioText);
      mHandler.post(mOnStereo);
       updateStationInfoToUI();
       enableRadioOnOffUI();
    }
 
    @Override
    public void onDestroy() {
       cleanupTimeoutHandler();
       endSleepTimer();
       unbindFromService(this);
       mService = null;
       Log.d(LOGTAG, "onDestroy: unbindFromService completed");
       super.onDestroy();
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
       MenuItem item;
       boolean radioOn = isFmOn();
       boolean recording = isRecording();
       boolean sleepActive = isSleepTimerActive();
       boolean searchActive = isScanActive() || isSeekActive();
 
       item = menu.add(0, MENU_SCAN_START, 0, R.string.menu_scan_start).setIcon(
                                                                               R.drawable.ic_btn_search);
       if (item != null)
       {
          item.setVisible(!searchActive && radioOn);
       }
       item = menu.add(0, MENU_SCAN_STOP, 0, R.string.menu_scan_stop).setIcon(
                                                                             R.drawable.ic_btn_search);
       if (item != null)
       {
          item.setVisible(searchActive && radioOn);
       }
 
       if (RECORDING_ENABLE)
       {
          item = menu.add(0, MENU_RECORD_START, 0, R.string.menu_record_start)
                 .setIcon(R.drawable.ic_menu_record);
          if (item != null)
          {
             item.setVisible(!recording && radioOn);
          }
          item = menu.add(0, MENU_RECORD_STOP, 0, R.string.menu_record_stop)
                 .setIcon(R.drawable.ic_menu_record);
          if (item != null)
          {
             item.setVisible(recording && radioOn);
          }
       }
       /* Settings can be active */
       item = menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings).setIcon(
                                                                           android.R.drawable.ic_menu_preferences);
 
       item = menu.add(0, MENU_SLEEP, 0, R.string.menu_sleep)
              .setTitle(R.string.menu_sleep);
       if (item != null)
       {
          item.setVisible(!sleepActive && radioOn);
       }
       item = menu.add(0, MENU_SLEEP_CANCEL, 0, R.string.menu_sleep_cancel)
              .setTitle(R.string.menu_sleep_cancel);
       if (item != null)
       {
          item.setVisible(sleepActive && radioOn);
       }
 
       if (isWiredHeadsetAvailable())
       {
 
 	    if (mAudioRoute == FMRadioService.RADIO_AUDIO_DEVICE_SPEAKER) {
               item = menu.add(0, MENU_WIRED_HEADSET, 0, R.string.menu_wired_headset);
 	    }
 	    else {
               item = menu.add(0, MENU_WIRED_HEADSET, 0, R.string.menu_speaker);
 	    }
          if (item != null)
          {
             item.setCheckable(true);
             item.setChecked(false);
             item.setVisible(radioOn);
          }
       }
 
       return true;
    }
 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
 
       super.onPrepareOptionsMenu(menu);
 
       MenuItem item;
       boolean radioOn = isFmOn();
       boolean recording = isRecording();
       boolean searchActive = isScanActive() || isSeekActive();
 
       item = menu.findItem(MENU_SCAN_START);
       if (item != null)
       {
          item.setVisible(!searchActive && radioOn);
       }
       item = menu.findItem(MENU_SCAN_STOP);
       if (item != null)
       {
          item.setVisible(searchActive && radioOn);
       }
       if (RECORDING_ENABLE)
       {
          item = menu.findItem(MENU_RECORD_START);
          if (item != null)
          {
             item.setVisible(!recording && radioOn);
          }
          item = menu.findItem(MENU_RECORD_STOP);
          if (item != null)
          {
             item.setVisible(recording && radioOn);
          }
       }
 
       boolean sleepActive = isSleepTimerActive();
       item = menu.findItem(MENU_SLEEP);
       if (item != null)
       {
          item.setVisible(!sleepActive && radioOn);
       }
       item = menu.findItem(MENU_SLEEP_CANCEL);
       if (item != null)
       {
          item.setVisible(sleepActive && radioOn);
       }
 
       if (isWiredHeadsetAvailable() && radioOn)
       {
          if (menu.findItem(MENU_WIRED_HEADSET) == null)
          {
 	    if (mAudioRoute == FMRadioService.RADIO_AUDIO_DEVICE_SPEAKER) {
               item = menu.add(0, MENU_WIRED_HEADSET, 0, R.string.menu_wired_headset);
 	    }
 	    else {
               item = menu.add(0, MENU_WIRED_HEADSET, 0, R.string.menu_speaker);
 	    }
          }
 	 else {
             menu.removeItem(MENU_WIRED_HEADSET);
 	    if (mAudioRoute == FMRadioService.RADIO_AUDIO_DEVICE_SPEAKER) {
               item = menu.add(0, MENU_WIRED_HEADSET, 0, R.string.menu_wired_headset);
 	    }
 	    else {
               item = menu.add(0, MENU_WIRED_HEADSET, 0, R.string.menu_speaker);
 	    }
 
 	 }
          if (item != null)
          {
            item.setCheckable(true);
            item.setChecked(false);
          }
       } else
       {
          menu.removeItem(MENU_WIRED_HEADSET);
       }
       return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId())
       {
       case MENU_SETTINGS:
          Intent launchPreferencesIntent = new Intent().setClass(this,
                                                                 Settings.class);
                   launchPreferencesIntent.putExtra(Settings.RX_MODE,true);
          startActivityForResult(launchPreferencesIntent,
                                 ACTIVITY_RESULT_SETTINGS);
          //startActivity(launchPreferencesIntent);
          return true;
 
       case MENU_SCAN_START:
          showDialog(DIALOG_SEARCH);
          return true;
 
       case MENU_SCAN_STOP:
          cancelSearch();
          return true;
 
       case MENU_RECORD_START:
          startRecord();
          return true;
 
       case MENU_RECORD_STOP:
          stopRecord();
          return true;
 
       case MENU_SLEEP:
          showDialog(DIALOG_SLEEP);
          return true;
 
       case MENU_SLEEP_CANCEL:
          DebugToasts("Sleep Cancelled", Toast.LENGTH_SHORT);
          endSleepTimer();
          return true;
 
       case MENU_WIRED_HEADSET:
          /* Call the mm interface to route the wired headset*/
 
 	 switch (mAudioRoute)
 	 {
 
 	   case  FMRadioService.RADIO_AUDIO_DEVICE_WIRED_HEADSET:
 	         mAudioRoute = FMRadioService.RADIO_AUDIO_DEVICE_SPEAKER;
 		 break;
 
 	   case  FMRadioService.RADIO_AUDIO_DEVICE_SPEAKER:
 	         mAudioRoute = FMRadioService.RADIO_AUDIO_DEVICE_WIRED_HEADSET;
 		 break;
 
            default:
 	         mAudioRoute = FMRadioService.RADIO_AUDIO_DEVICE_WIRED_HEADSET;
 		 break;
 
 	  }
 
 	 audioRoute (mAudioRoute);
 
          return true;
       default:
          break;
       }
       return super.onOptionsItemSelected(item);
    }
 
    private void audioRoute (int audioDevice)
    {
       boolean bStatus;
       if(mService != null)
       {
          try
          {
             bStatus = mService.routeAudio(audioDevice);
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
 
    }
 
    @Override
    protected Dialog onCreateDialog(int id) {
       switch (id)
       {
       case DIALOG_SELECT_PRESET_LIST: {
             return createSelectPresetListDlg(id);
          }
       case DIALOG_PRESETS_LIST: {
             return createPresetListEditDlg(id);
          }
       case DIALOG_PRESET_LIST_RENAME: {
             return createPresetListRenameDlg(id);
          }
       case DIALOG_PRESET_LIST_DELETE: {
             return createPresetListDeleteDlg(id);
          }
       case DIALOG_PRESET_LIST_AUTO_SET: {
             return createPresetListAutoSelectWarnDlg(id);
          }
       case DIALOG_SEARCH: {
             return createSearchDlg(id);
          }
       case DIALOG_SLEEP: {
             return createSleepDlg(id);
          }
       case DIALOG_PICK_FREQUENCY: {
             FmConfig fmConfig = FmSharedPreferences.getFMConfiguration();
             return new FrequencyPickerDialog(this, fmConfig, mTunedStation.getFrequency(), mFrequencyChangeListener);
          }
       case DIALOG_PROGRESS_PROGRESS: {
          return createProgressDialog(id);
         }
       case DIALOG_PRESET_OPTIONS: {
          return createPresetOptionsDlg(id);
        }
       case DIALOG_PRESET_RENAME: {
          return createPresetRenameDlg(id);
        }
       case DIALOG_CMD_TIMEOUT:{
          return createCmdTimeoutDlg(id);
       }
       case DIALOG_CMD_FAILED:{
          return createCmdFailedDlg(id);
       }
       default:
          break;
       }
       return null;
    }
 
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
       super.onPrepareDialog(id, dialog);
       int curListIndex = FmSharedPreferences.getCurrentListIndex();
       PresetList curList = FmSharedPreferences.getStationList(curListIndex);
       switch (id)
       {
       case DIALOG_PRESET_LIST_RENAME: {
             EditText et = (EditText) dialog.findViewById(R.id.list_edit);
             if (et != null)
             {
                et.setText(curList.getName());
             }
             break;
          }
       case DIALOG_PRESET_LIST_DELETE: {
             AlertDialog alertDlg = ((AlertDialog) dialog);
             alertDlg.setTitle(curList.getName());
             alertDlg.setMessage(getString(R.string.presetlist_delete_name, curList.getName()));
             break;
          }
 
       case DIALOG_PRESET_LIST_AUTO_SET: {
             AlertDialog alertDlg = ((AlertDialog) dialog);
             alertDlg.setTitle(curList.getName());
             ((AlertDialog) dialog)
             .setMessage(getString(R.string.presetlist_autoselect_name, curList.getName()));
             break;
          }
       case DIALOG_SELECT_PRESET_LIST: {
             AlertDialog alertDlg = ((AlertDialog) dialog);
             ListView lv = (ListView) alertDlg.findViewById(R.id.list);
             if (lv != null)
             {
                updateSelectPresetListDlg(lv);
             }
             break;
          }
       case DIALOG_PRESETS_LIST: {
             AlertDialog alertDlg = ((AlertDialog) dialog);
             alertDlg.setTitle(curList.getName());
             break;
          }
       case DIALOG_PICK_FREQUENCY:
          {
             ((FrequencyPickerDialog) dialog).UpdateFrequency(mTunedStation.getFrequency());
             break;
          }
       case DIALOG_PRESET_RENAME:
          {
             EditText et = (EditText) dialog.findViewById(R.id.list_edit);
             if ( (et != null) && (mPresetButtonStation != null))
             {
                et.setText(mPresetButtonStation.getName());
             }
             break;
          }
       case DIALOG_PRESET_OPTIONS: {
          AlertDialog alertDlg = ((AlertDialog) dialog);
          if( (alertDlg != null) && (mPresetButtonStation != null))
          {
             alertDlg.setTitle(mPresetButtonStation.getName());
          }
          break;
        }
 
       default:
          break;
       }
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
       Log.d(LOGTAG, "onActivityResult : requestCode -> "+ requestCode);
       Log.d(LOGTAG, "onActivityResult : resultCode -> "+ resultCode);
       if (requestCode ==ACTIVITY_RESULT_SETTINGS)
       {
          if (resultCode == RESULT_OK)
          {
             /* */
             if (data != null)
             {
                String action = data.getAction();
                if (action != null)
                {
                   if (action.equals(Settings.RESTORE_FACTORY_DEFAULT_ACTION))
                   {
                      RestoreDefaults();
                      enableRadioOnOffUI();
                   }
                }
             }
          } //if ACTIVITY_RESULT_SETTINGS
       }//if (resultCode == RESULT_OK)
    }
 
 
 
    /**
     * @return true if a wired headset is connected.
     */
    boolean isWiredHeadsetAvailable() {
       boolean bAvailable = false;
       if(mService != null)
       {
          try
          {
             bAvailable = mService.isWiredHeadsetAvailable();
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
       Log.e(LOGTAG, "isWiredHeadsetAvailable: " + bAvailable);
       return bAvailable;
    }
 
     /**
      * @return true if a internal antenna is available.
      *
      */
     boolean isAntennaAvailable() {
        boolean bAvailable = false;
        if(mService != null)
        {
           try
           {
              bAvailable = mService.isAntennaAvailable();
           } catch (RemoteException e)
           {
              e.printStackTrace();
           }
        }
        return bAvailable;
 
     }
 
    private Dialog createSearchDlg(int id) {
       String[] items;
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       dlgBuilder.setIcon(R.drawable.ic_btn_search);
       dlgBuilder.setTitle(getString(R.string.search_dialog_title));
       /* Pick RBDS or RDS */
       if(FmSharedPreferences.isRBDSStd())
       {
          items = getResources().getStringArray(R.array.search_category_rbds_entries);
       } else // if(FmSharedPreferences.isRDSStd())
       {
          items = getResources().getStringArray(R.array.search_category_rds_entries);
       }
 
       dlgBuilder.setItems(items, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int item) {
              String[] items;
              String[] values;
 
              /* Pick RBDS or RDS */
              if(FmSharedPreferences.isRBDSStd())
              {
                 items = getResources().getStringArray(R.array.search_category_rbds_entries);
                 values = getResources().
                               getStringArray(R.array.search_category_rbds_values);
              } else // if(FmSharedPreferences.isRDSStd())
              {
                 items = getResources().getStringArray(R.array.search_category_rds_entries);
                 values = getResources().
                               getStringArray(R.array.search_category_rds_values);
              }
              if ((items != null) &&
                  (values != null) &&
                   (item >= 0))
               {
                  if ( (item>=0) && (item <= items.length) && (item <= items.length) )
                  {
                     DebugToasts("Search Stations for : " + items[item]
                                 +" ("+values[item]+")",
                                 Toast.LENGTH_SHORT);
                     int pty = Integer.parseInt(values[item]);
                     initiateSearch(pty);
                  }
               }
              removeDialog (DIALOG_SEARCH);
           }
       });
 
       return dlgBuilder.create();
    }
 
    private Dialog createPresetOptionsDlg(int id) {
       if(mPresetButtonStation != null)
       {
          AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
          dlgBuilder.setTitle(mPresetButtonStation.getName());
          ArrayList<String> arrayList = new ArrayList<String>();
          //PRESETS_OPTIONS_TUNE=0
          arrayList.add(getResources().getString(R.string.preset_tune));
          //PRESETS_OPTIONS_REPLACE=1
          arrayList.add(getResources().getString(R.string.preset_replace));
          //PRESETS_OPTIONS_RENAME=2
          arrayList.add(getResources().getString(R.string.preset_rename));
          //PRESETS_OPTIONS_DELETE=3
          arrayList.add(getResources().getString(R.string.preset_delete));
          String piString = mPresetButtonStation.getPIString();
          if (!TextUtils.isEmpty(piString))
          {
             //PRESETS_OPTIONS_SEARCHPI=4
             arrayList.add(getResources().getString(R.string.preset_search, piString));
          }
 
          dlgBuilder.setCancelable(true);
          dlgBuilder.setOnCancelListener (new DialogInterface.OnCancelListener() {
             public void onCancel(DialogInterface dialog) {
                mPresetButtonStation = null;
                removeDialog(DIALOG_PRESET_OPTIONS);
             }
          });
          String[] items = new String [arrayList.size ()];
          arrayList.toArray(items);
          dlgBuilder.setItems(items, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int item) {
                 if(mPresetButtonStation != null)
                 {
                    switch(item)
                    {
                    case PRESETS_OPTIONS_TUNE:
                       {
                          // Tune to the station
                          tuneRadio(mPresetButtonStation.getFrequency());
                          mPresetButtonStation = null;
                          break;
                       }
                    case PRESETS_OPTIONS_REPLACE:
                       {
                          // Replace preset Station with currently tuned station
                          Log.d(LOGTAG, "station - " + mPresetButtonStation.getName() + " ("
                                + mPresetButtonStation.getFrequency() + ")");
                          mPresetButtonStation.Copy(mTunedStation);
                          mPresetButtonStation = null;
                          setupPresetLayout();
                          mPrefs.Save();
                          break;
                       }
                    case PRESETS_OPTIONS_RENAME:
                       {
                          // Rename
                          showDialog(DIALOG_PRESET_RENAME);
                          break;
                       }
                    case PRESETS_OPTIONS_DELETE:
                       {
                          // Delete
                          int curListIndex = FmSharedPreferences.getCurrentListIndex();
                          FmSharedPreferences.removeStation(curListIndex, mPresetButtonStation);
                          mPresetButtonStation = null;
                          setupPresetLayout();
                          mPrefs.Save();
                          break;
                       }
                    case PRESETS_OPTIONS_SEARCHPI:
                       {
                          // SearchPI
                          String piString = mPresetButtonStation.getPIString();
                          int pi = mPresetButtonStation.getPI();
                          if ((!TextUtils.isEmpty(piString)) &&  (pi>0))
                          {
                             initiatePISearch(pi);
                          }
                          mPresetButtonStation = null;
                          break;
                       }
                    default:
                       {
                          // Should not happen
                          mPresetButtonStation = null;
                          break;
                       }
                    }//switch item
                 }//if(mPresetButtonStation != null)
                 removeDialog (DIALOG_PRESET_OPTIONS);
              }//onClick
          });
          return dlgBuilder.create();
       }
       return null;
    }
 
    private Dialog createSleepDlg(int id) {
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       dlgBuilder.setTitle(R.string.dialog_sleep_title);
       String[] items = getResources().getStringArray(R.array.sleep_duration_values);
 
       dlgBuilder.setItems(items, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int item) {
               String[] items = getResources().getStringArray(R.array.sleep_duration_values);
               Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
               if ((item>=0) && (item <= items.length))
               {
                  long seconds = (long) (900 * (item + 1));
                  initiateSleepTimer(seconds);
               }
               removeDialog (DIALOG_SLEEP);
           }
       });
 
       return dlgBuilder.create();
    }
 
 
    private Dialog createProgressDialog(int id) {
       String msgStr="";
       String titleStr="";
       double frequency = mTunedStation.getFrequency() / 1000.0;
       boolean bSearchActive = false;
 
       if(isSeekActive())
       {
          msgStr = getString(R.string.msg_seeking);
          bSearchActive=true;
       }
       else if(isScanActive())
       {
          String ptyStr = PresetStation.parsePTY(mScanPty);
          if (!TextUtils.isEmpty(ptyStr))
          {
             msgStr = getString(R.string.msg_scanning_pty, ptyStr);
          }
          else
          {
             msgStr = getString(R.string.msg_scanning);
          }
          titleStr = getString(R.string.msg_search_title, ("" + frequency));
          bSearchActive=true;
       }
       else if(isSearchActive())
       {
          msgStr = getString(R.string.msg_searching);
          titleStr = getString(R.string.msg_searching_title);
          bSearchActive=true;
       }
 
       if(bSearchActive) {
          mProgressDialog = new ProgressDialog(FMRadio.this);
          mProgressDialog.setTitle(titleStr);
          mProgressDialog.setMessage(msgStr);
          mProgressDialog.setIcon(R.drawable.ic_launcher_fmradio);
          mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
          mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                                    getText(R.string.button_text_stop),
              new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                 cancelSearch();
              }
          }
          );
          mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
             public void onCancel(DialogInterface dialog) {
                cancelSearch();
             }
          }
          );
 
          Message msg = new Message();
          msg.what = TIMEOUT_PROGRESS_DLG;
          mSearchProgressHandler.sendMessageDelayed(msg, SHOWBUSY_TIMEOUT);
       }
       return mProgressDialog;
    }
 
    private void updateSelectPresetListDlg(ListView lv) {
       if (lv != null)
       {
          List<PresetList> presetLists = FmSharedPreferences.getPresetLists();
          ListIterator<PresetList> presetIter;
          presetIter = presetLists.listIterator();
          int numLists = presetLists.size();
          int curIndex = FmSharedPreferences.getCurrentListIndex();
          ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
                                                                      android.R.layout.simple_list_item_single_choice);
          for (int stationIter = 0; stationIter < numLists; stationIter++)
          {
             PresetList temp = presetIter.next();
             if (temp != null)
             {
                typeAdapter.add(getString(R.string.presetlist_select_name, temp.getName()));
             }
          }
          typeAdapter.add(getString(R.string.presetlist_add_new));
          lv.setAdapter(typeAdapter);
          lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
          lv.clearChoices();
          if (curIndex >= numLists)
          {
             curIndex = 0;
          }
          if (lv.getCount() >= curIndex)
          {
             lv.setItemChecked(curIndex, true);
             lv.setSelection(curIndex);
          } else
          {
             lv.setItemChecked(0, true);
             lv.setSelection(0);
          }
       }
    }
 
 
    private Dialog createPresetListEditDlg(int id) {
       String[] items = getResources().getStringArray(R.array.presetlist_edit_category);
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
 
 
       int currentList = FmSharedPreferences.getCurrentListIndex();
       PresetList curList = FmSharedPreferences.getStationList(currentList);
 
       dlgBuilder.setIcon(R.drawable.alert_dialog_icon);
       dlgBuilder.setTitle("\"" + curList.getName() + "\"");
       dlgBuilder.setItems(items, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int item) {
               switch(item)
               {
               case 0:
                  {
                     // Rename
                     showDialog(DIALOG_PRESET_LIST_RENAME);
                     break;
                  }
               case 1:
                  {
                     // Auto-Select - Build Preset List
                     int currentList =
                        FmSharedPreferences.getCurrentListIndex();
                     PresetList curList =
                        FmSharedPreferences.getStationList(currentList);
                     if(curList.getStationCount() > 0)
                     {
                        /* Show a autoselect- deletes presets warning */
                        showDialog(DIALOG_PRESET_LIST_AUTO_SET);
                     }
                     else
                     {
                        curList.clear();
                        /* Since the presets will be changed,
                         * reset the page number
                         */
                        mPresetPageNumber = 0;
                        initiateSearchList();
                        //curList.addDummyStations();
                        setupPresetLayout();
                        mPrefs.Save();
                     }
 
                     break;
                  }
               case 2:
                  {
                     // Delete
                     showDialog(DIALOG_PRESET_LIST_DELETE);
                     break;
                  }
               }//switch item
               removeDialog(DIALOG_PRESETS_LIST);
           }//onClick
       });
       return dlgBuilder.create();
    }
 
    private Dialog createSelectPresetListDlg(int id) {
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       LayoutInflater factory = LayoutInflater.from(this);
       final View listView = factory.inflate(R.layout.alert_dialog_list, null);
       ListView lv = (ListView) listView.findViewById(R.id.list);
 
       if (lv != null)
       {
          updateSelectPresetListDlg(lv);
       } else
       {
          return null;
       }
       dlgBuilder.setView(listView);
       dlgBuilder.setIcon(R.drawable.alert_dialog_icon);
       dlgBuilder.setTitle(R.string.dialog_presetlist_title);
       dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          ListView lv = (ListView) listView
                                          .findViewById(R.id.list);
                                          int selectedPos = -1;
                                          if (lv != null)
                                          {
                                             selectedPos = lv.getCheckedItemPosition();
                                          }
                                          if (selectedPos >= 0)
                                          {
                                             if (selectedPos < FmSharedPreferences.getNumList())
                                             {
                                                /* If the preset List was changed, reset the page number */
                                                if(selectedPos != FmSharedPreferences.getCurrentListIndex())
                                                {
                                                   mPresetPageNumber = 0;
                                                }
                                                FmSharedPreferences.setListIndex(selectedPos);
                                                mPrefs.Save();
                                             }
                                             else
                                             {
                                                String presetListName = getString(R.string.presetlist_prefix_name)
                                                + (FmSharedPreferences.getNumList() + 1);
                                                int newIndex = FmSharedPreferences
                                                .createPresetList(presetListName);
                                                FmSharedPreferences.setListIndex(newIndex);
                                                showDialog(DIALOG_PRESET_LIST_RENAME);
                                             }
                                          }
                                          setupPresetLayout();
                                          removeDialog(DIALOG_SELECT_PRESET_LIST);
                                       }
                                    });
       dlgBuilder.setNegativeButton(R.string.alert_dialog_cancel,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          removeDialog(DIALOG_SELECT_PRESET_LIST);
                                       }
                                    });
       return dlgBuilder.create();
    }
 
    private Dialog createPresetRenameDlg(int id) {
       if(mPresetButtonStation == null)
       {
          return null;
       }
       LayoutInflater factory = LayoutInflater.from(this);
       final View textEntryView = factory.inflate(
                                                 R.layout.alert_dialog_text_entry, null);
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       dlgBuilder.setTitle(R.string.dialog_presetlist_rename_title);
       dlgBuilder.setView(textEntryView);
       dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          //int curList = FmSharedPreferences.getCurrentListIndex();
 
                                          EditText mTV = (EditText) textEntryView
                                          .findViewById(R.id.list_edit);
                                          CharSequence newName = mTV.getEditableText();
                                          String nName = String.valueOf(newName);
                                          mPresetButtonStation.setName(nName);
                                          mPresetButtonStation=null;
                                          setupPresetLayout();
                                          mPrefs.Save();
                                          removeDialog(DIALOG_PRESET_RENAME);
                                       }
                                    });
       dlgBuilder.setNegativeButton(R.string.alert_dialog_cancel,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          removeDialog(DIALOG_PRESET_RENAME);
                                       }
                                    });
       return(dlgBuilder.create());
    }
 
    private Dialog createPresetListRenameDlg(int id) {
       LayoutInflater factory = LayoutInflater.from(this);
       final View textEntryView = factory.inflate(
                                                 R.layout.alert_dialog_text_entry, null);
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       dlgBuilder.setTitle(R.string.dialog_presetlist_rename_title);
       dlgBuilder.setView(textEntryView);
       dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          int curList = FmSharedPreferences.getCurrentListIndex();
 
                                          EditText mTV = (EditText) textEntryView
                                          .findViewById(R.id.list_edit);
                                          CharSequence newName = mTV.getEditableText();
                                          String nName = String.valueOf(newName);
                                          FmSharedPreferences.renamePresetList(nName, curList);
                                          setupPresetLayout();
                                          mPrefs.Save();
                                          removeDialog(DIALOG_PRESET_LIST_RENAME);
                                       }
                                    });
       dlgBuilder.setNegativeButton(R.string.alert_dialog_cancel,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          removeDialog(DIALOG_PRESET_LIST_RENAME);
                                       }
                                    });
       return(dlgBuilder.create());
    }
 
    private Dialog createPresetListDeleteDlg(int id) {
       int currentList = FmSharedPreferences.getCurrentListIndex();
       PresetList curList = FmSharedPreferences.getStationList(currentList);
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       dlgBuilder.setIcon(R.drawable.alert_dialog_icon).setTitle(
                                                                curList.getName());
       dlgBuilder.setMessage(getString(R.string.presetlist_delete_name, curList.getName()));
       dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          int currentList = FmSharedPreferences.getCurrentListIndex();
                                          FmSharedPreferences.removeStationList(currentList);
                                          /* Since the presets will be changed,
                                           * reset the page number
                                           */
                                          mPresetPageNumber = 0;
                                          setupPresetLayout();
                                          mPrefs.Save();
                                          removeDialog(DIALOG_PRESET_LIST_DELETE);
                                       }
                                    });
       dlgBuilder.setNegativeButton(R.string.alert_dialog_cancel,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          removeDialog(DIALOG_PRESET_LIST_DELETE);
                                       }
                                    });
       return(dlgBuilder.create());
    }
 
    private Dialog createPresetListAutoSelectWarnDlg(int id) {
       int currentList = FmSharedPreferences.getCurrentListIndex();
       PresetList curList = FmSharedPreferences.getStationList(currentList);
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       dlgBuilder.setIcon(R.drawable.alert_dialog_icon)
                 .setTitle(R.string.presetlist_autoselect_title);
       dlgBuilder.setMessage(getString(R.string.presetlist_autoselect_name,
                                       curList.getName()));
 
       dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog,
                                                           int whichButton) {
                                          int currentList =
                                             FmSharedPreferences.getCurrentListIndex();
                                          PresetList curList =
                                             FmSharedPreferences.getStationList(currentList);
                                          curList.clear();
                                          /* Since the presets will be changed,
                                           * reset the page number
                                           */
                                          mPresetPageNumber = 0;
                                          initiateSearchList();
                                          //curList.addDummyStations();
                                          setupPresetLayout();
                                          mPrefs.Save();
                                          removeDialog(DIALOG_PRESET_LIST_AUTO_SET);
                                       }
                                    });
 
       dlgBuilder.setNegativeButton(R.string.alert_dialog_cancel,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                          removeDialog(DIALOG_PRESET_LIST_AUTO_SET);
                                       }
                                    });
       return(dlgBuilder.create());
    }
 
 
    private Dialog createCmdTimeoutDlg(int id) {
       if(mCommandActive > 0)
       {
          AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
          dlgBuilder.setIcon(R.drawable.alert_dialog_icon)
                    .setTitle(R.string.fm_command_timeout_title);
          dlgBuilder.setMessage(R.string.fm_tune_timeout_msg);
 
          dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                                       new DialogInterface.OnClickListener() {
                                          public void onClick(DialogInterface dialog,
                                                              int whichButton) {
                                             cleanupTimeoutHandler();
                                             removeDialog(DIALOG_CMD_TIMEOUT);
                                          }
                                       });
 
          return(dlgBuilder.create());
       }
       else
       {
          return (null);
       }
    }
 
    private Dialog createCmdFailedDlg(int id) {
       AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
       dlgBuilder.setIcon(R.drawable.alert_dialog_icon)
                 .setTitle(R.string.fm_command_failed_title);
       dlgBuilder.setMessage(R.string.fm_cmd_failed_msg);
 
       dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog,
                                                           int whichButton) {
                                          removeDialog(DIALOG_CMD_TIMEOUT);
                                          mCommandFailed = CMD_NONE;
                                       }
                                    });
 
       return(dlgBuilder.create());
    }
 
 
    private void RestoreDefaults() {
       FmSharedPreferences.SetDefaults();
       mPrefs.Save();
    }
 
    private View.OnLongClickListener mFrequencyViewClickListener = new View.OnLongClickListener() {
       public boolean onLongClick(View v) {
          showDialog(DIALOG_PICK_FREQUENCY);
          return true;
       }
    };
 
    private View.OnClickListener mForwardClickListener
    = new View.OnClickListener() {
       public void onClick(View v) {
          int frequency = FmSharedPreferences.getNextTuneFrequency();
          Log.d(LOGTAG, "Tune Up: to " + frequency);
          tuneRadio(frequency);
       }
    };
 
    private View.OnClickListener mBackClickListener
    = new View.OnClickListener() {
       public void onClick(View v) {
          int frequency = FmSharedPreferences.getPrevTuneFrequency();
          Log.d(LOGTAG, "Tune Down: to " + frequency);
          tuneRadio(frequency);
       }
    };
 
    private View.OnLongClickListener mForwardLongClickListener
       = new View.OnLongClickListener() {
       public boolean onLongClick(View view) {
          //mForwardButton.startAnimation(mAnimation);
          SeekNextStation();
          //enableScanningOnOffUI();
          return true;
       }
    };
 
    private View.OnLongClickListener mBackLongClickListener = new View.OnLongClickListener() {
       public boolean onLongClick(View view) {
          //mBackButton.startAnimation(mAnimation);
          SeekPreviousStation();
          //enableScanningOnOffUI();
          return true;
       }
    };
 
    private View.OnClickListener mPresetListClickListener = new View.OnClickListener() {
       public void onClick(View v) {
          showDialog(DIALOG_SELECT_PRESET_LIST);
       }
    };
    private View.OnLongClickListener mPresetListButtonOnLongClickListener = new View.OnLongClickListener() {
       public boolean onLongClick(View view) {
          showDialog(DIALOG_PRESETS_LIST);
          return true;
       }
    };
 
    private View.OnClickListener mPresetsPageClickListener = new View.OnClickListener() {
       public void onClick(View v) {
          mPresetPageNumber++;
          setupPresetLayout();
       }
    };
 
    private View.OnClickListener mPresetButtonClickListener = new View.OnClickListener() {
       public void onClick(View view) {
          PresetStation station = (PresetStation) view.getTag();
          if (station != null)
          {
             Log.d(LOGTAG, "station - " + station.getName() + " ("
                   + station.getFrequency() + ")");
             //mTunedStation.Copy(station);
             tuneRadio(station.getFrequency());
             view.startAnimation(mAnimation);
          }
       }
    };
 
    private View.OnLongClickListener mPresetButtonOnLongClickListener = new View.OnLongClickListener() {
       public boolean onLongClick(View view) {
          PresetStation station = (PresetStation) view.getTag();
          mPresetButtonStation = station;
          if (station != null)
          {
             showDialog(DIALOG_PRESET_OPTIONS);
          } else
          {
             addToPresets();
             view.startAnimation(mAnimation);
          }
          return true;
       }
    };
 
    final FrequencyPickerDialog.OnFrequencySetListener mFrequencyChangeListener
    = new FrequencyPickerDialog.OnFrequencySetListener() {
       public void onFrequencySet(FrequencyPicker view, int frequency) {
          Log.d(LOGTAG, "mFrequencyChangeListener: onFrequencyChanged to : "+frequency);
          tuneRadio(frequency);
       }
    };
 
    private View.OnClickListener mMuteModeClickListener
    = new View.OnClickListener() {
       public void onClick(View v) {
          boolean bStatus = false;
          if(mService != null)
          {
             try
             {
                if (true == isMuted())
                {
                   bStatus = mService.unMute();
                } else
                {
                   bStatus = mService.mute();
                }
 
                if(bStatus)
                {
                   setMuteModeButtonImage(true);
                   v.startAnimation(mAnimation);
                }
                else
                {
                   mCommandFailed = CMD_MUTE;
                   showDialog(DIALOG_CMD_FAILED);
                }
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
          }
       }
    };
 
    private View.OnClickListener mTurnOnOffClickListener
    = new View.OnClickListener() {
       public void onClick(View v) {
 
          if (isFmOn())
          {
             disableRadio();
          } else
          {
             enableRadio();
          }
          setTurnOnOffButtonImage();
          cleanupTimeoutHandler();
       }
    };
 
    private void setTurnOnOffButtonImage() {
       if (isFmOn() == true)
       {
          mOnOffButton.setImageResource(R.drawable.ic_btn_onoff);
       } else
       {
          /* Find a icon to indicate off */
          mOnOffButton.setImageResource(R.drawable.ic_btn_onoff);
       }
    }
 
 
    private void setMuteModeButtonImage(boolean notify) {
       String fmMutedString;
       if (isMuted() == true)
       {
          mMuteButton.setImageResource(R.drawable.ic_silent_mode);
          fmMutedString = "FM Radio Muted";
       } else
       {
          /* Find a icon for Stations */
          mMuteButton.setImageResource(R.drawable.ic_silent_mode_off);
          fmMutedString = "FM Radio Playing";
       }
       if (notify)
       {
          //Toast.makeText(this, fmMutedString, Toast.LENGTH_SHORT).show();
          Log.d(LOGTAG, fmMutedString);
       }
    }
 
    private void enableRadio() {
       mIsScaning = false;
       mIsSeeking = false;
       mIsSearching=false;
       boolean bStatus = false;
       if(mService != null)
       {
          try
          {
             bStatus = mService.fmOn();
             if(bStatus)
             {
                if(isAntennaAvailable())
                {
                   tuneRadio(FmSharedPreferences.getTunedFrequency());
                   enableRadioOnOffUI();
                }
                else
                {
                   disableRadio();
                }
             }
             else
             {
                Log.e(LOGTAG, " mService.fmOn failed");
                mCommandFailed = CMD_FMON;
                showDialog(DIALOG_CMD_FAILED);
             }
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
    }
 
    private void disableRadio() {
       boolean bStatus = false;
       cancelSearch();
       endSleepTimer();
       if(mService != null)
       {
          try
          {
             bStatus = mService.fmOff();
             enableRadioOnOffUI();
             if(bStatus == false)
             {
                mCommandFailed = CMD_FMOFF;
                Log.e(LOGTAG, " mService.fmOff failed");
                //showDialog(DIALOG_CMD_FAILED);
             }
          } catch (RemoteException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
    }
 
    public static void fmConfigure() {
       boolean bStatus = false;
       if(mService != null)
       {
          try
          {
             bStatus = mService.fmReconfigure();
             if(bStatus == false)
             {
                mCommandFailed = CMD_FMCONFIGURE;
                Log.e(LOGTAG, " mService.fmReconfigure failed");
                //showDialog(DIALOG_CMD_FAILED);
             }
          } catch (RemoteException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
    }
   public static void fmAutoAFSwitch() {
       boolean bStatus = false;
       if(mService != null)
       {
          try
          {
             bStatus = mService.enableAutoAF(FmSharedPreferences.getAutoAFSwitch());
             if(bStatus == false)
             {
                mCommandFailed = CMD_SET_AUTOAF;
                Log.e(LOGTAG, " mService.enableAutoAF failed");
                //showDialog(DIALOG_CMD_FAILED);
             }
          } catch (RemoteException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
    }
    public static void fmAudioOutputMode() {
       boolean bStatus = false;
       if(mService != null)
       {
          try
          {
             bStatus = mService.enableStereo(FmSharedPreferences.getAudioOutputMode());
             if(bStatus == false)
             {
                mCommandFailed = CMD_SET_AUDIO_MODE;
                Log.e(LOGTAG, " mService.enableStereo failed");
                //showDialog(DIALOG_CMD_FAILED);
             }
          } catch (RemoteException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
    }
 
    private boolean startRecord() {
       mRecording = true;
       DebugToasts("Started Recording", Toast.LENGTH_SHORT);
       return mRecording;
    }
 
    private boolean isRecording() {
       return mRecording;
    }
 
    private boolean stopRecord() {
       mRecording = false;
       DebugToasts("Stopped Recording", Toast.LENGTH_SHORT);
       return mRecording;
    }
 
    private void addToPresets() {
       int currentList = FmSharedPreferences.getCurrentListIndex();
       PresetStation selectedStation = getCurrentTunedStation();
       FmSharedPreferences.addStation(selectedStation.getName(), selectedStation
                         .getFrequency(), currentList);
       setupPresetLayout();
    }
 
    private void enableRadioOnOffUI() {
       boolean bEnable = isFmOn();
       /* Disable if no antenna/headset is available */
       if(!isAntennaAvailable() )
       {
          bEnable = false;
       }
       enableRadioOnOffUI(bEnable);
    }
 
    private void enableRadioOnOffUI(boolean bEnable) {
 
       mMuteButton.setEnabled(bEnable);
       setMuteModeButtonImage(false);
       if (bEnable)
       {
          mTuneStationFrequencyTV.setOnLongClickListener(mFrequencyViewClickListener);
          mRadioTextScroller.startScroll();
          if (!isSleepTimerActive())
          {
             mSleepMsgTV.setText("");
          }
          if (!isRecording())
          {
             mRecordingMsgTV.setText("");
          }
       } else
       {
          mTuneStationFrequencyTV.setOnLongClickListener(null);
          mRadioTextScroller.stopScroll();
       }
 
       mForwardButton.setVisibility(((bEnable == true) ? View.VISIBLE
                                     : View.INVISIBLE));
       mBackButton.setVisibility(((bEnable == true) ? View.VISIBLE
                                  : View.INVISIBLE));
       mTuneStationFrequencyTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                              : View.INVISIBLE));
       mStationCallSignTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                         : View.INVISIBLE));
       mProgramTypeTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                     : View.INVISIBLE));
       mSleepMsgTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                  : View.INVISIBLE));
       mRecordingMsgTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                      : View.INVISIBLE));
       mRadioTextTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                   : View.INVISIBLE));
       mProgramServiceTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                   : View.INVISIBLE));
 
       if(!isAntennaAvailable())
       {
          mRadioTextTV.setVisibility(View.VISIBLE);
          mRadioTextTV.setText(getString(R.string.msg_noantenna));
          mOnOffButton.setEnabled(false);
       }
       else
       {
          mRadioTextTV.setText("");
          mOnOffButton.setEnabled(true);
       }
 
       /* Disable displaying RSSI */
       //mRSSI.setVisibility(((bEnable == true) ? View.VISIBLE
       //                : View.INVISIBLE));
       mStereoTV.setVisibility(((bEnable == true) ? View.VISIBLE
                                : View.INVISIBLE));
       for (int nButton = 0; nButton < MAX_PRESETS_PER_PAGE; nButton++)
       {
          mPresetButtons[nButton].setEnabled(bEnable);
       }
       mPresetListButton.setEnabled(bEnable);
       mPresetPageButton.setEnabled(bEnable &&
                                    (FmSharedPreferences.getListStationCount() >= MAX_PRESETS_PER_PAGE));
    }
 
    private void updateSearchProgress() {
       boolean searchActive = isScanActive() || isSeekActive() || isSearchActive();
       if(searchActive)
       {
          synchronized (this)
          {
             if(mProgressDialog == null)
             {
                showDialog(DIALOG_PROGRESS_PROGRESS);
             }
             else
             {
                Message msg = new Message();
                msg.what = UPDATE_PROGRESS_DLG;
                mSearchProgressHandler.sendMessage(msg);
             }
          }
       }
       else
       {
          Message msg = new Message();
          msg.what = END_PROGRESS_DLG;
          mSearchProgressHandler.sendMessage(msg);
       }
    }
 
    private void setupPresetLayout() {
       int numStations = FmSharedPreferences.getListStationCount();
       int addedStations = 0;
 
       /*
        * Validate mPresetPageNumber (Preset Page Number)
        */
       if (mPresetPageNumber > ((numStations) / MAX_PRESETS_PER_PAGE))
       {
          mPresetPageNumber = 0;
       }
 
       /*
        * For every station, save the station as a tag and update the display
        * on the preset Button.
        */
       for (int buttonIndex = 0; (buttonIndex < MAX_PRESETS_PER_PAGE); buttonIndex++)
       {
          if (mPresetButtons[buttonIndex] != null)
          {
             int stationIdex = (mPresetPageNumber * MAX_PRESETS_PER_PAGE)
                               + buttonIndex;
             PresetStation station = FmSharedPreferences.getStationInList(stationIdex);
             String display = "";
             if (station != null)
             {
                display = station.getName();
                mPresetButtons[buttonIndex].setText(display);
                mPresetButtons[buttonIndex].setTag(station);
                addedStations++;
             } else
             {
                mPresetButtons[buttonIndex].setText(display);
                mPresetButtons[buttonIndex].setTag(station);
             }
          }
       }
       /*
        * Enable "Next" Page: Only if more than 4 presets are added,
        * else there is nothing more
        */
       mPresetPageButton.setEnabled(isFmOn() && (numStations >= MAX_PRESETS_PER_PAGE));
       mPresetListButton.setText(FmSharedPreferences.getListName(FmSharedPreferences
                                                    .getCurrentListIndex()));
    }
 
    private void updateStationInfoToUI() {
       double frequency = mTunedStation.getFrequency() / 1000.0;
       mTuneStationFrequencyTV.setText("" + frequency);
       mStationCallSignTV.setText(mTunedStation.getPIString());
       mProgramTypeTV.setText(mTunedStation.getPtyString());
       mRadioTextTV.setText("");
       mProgramServiceTV.setText("");
       mStereoTV.setText("");
       setupPresetLayout();
    }
 
 
    private boolean isFmOn() {
       boolean bOn = false;
       if(mService != null)
       {
          try
          {
             bOn = mService.isFmOn();
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
       return(bOn);
    }
 
    private boolean isMuted() {
       boolean bMuted = false;
       if(mService != null)
       {
          try
          {
             bMuted = mService.isMuted();
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
       return(bMuted);
    }
 
    private boolean isScanActive() {
       return(mIsScaning);
    }
 
    private boolean isSeekActive() {
       return(mIsSeeking);
    }
    private boolean isSearchActive() {
       return(mIsSearching);
    }
 
    public static PresetStation getCurrentTunedStation() {
       return mTunedStation;
    }
 
    private void SeekPreviousStation() {
       Log.d(LOGTAG, "SeekPreviousStation");
       if(mService != null)
       {
          try
          {
             if(!isSeekActive())
             {
                mIsSeeking = mService.seek(false);
                if(mIsSeeking == false)
                {
                   mCommandFailed = CMD_SEEK;
                   Log.e(LOGTAG, " mService.seek failed");
                   showDialog(DIALOG_CMD_FAILED);
                }
 
             }
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
       updateSearchProgress();
    }
 
    private void SeekNextStation() {
       Log.d(LOGTAG, "SeekNextStation");
       if(mService != null)
       {
          try
          {
             if(!isSeekActive())
             {
                mIsSeeking = mService.seek(true);
                if(mIsSeeking == false)
                {
                   mCommandFailed = CMD_SEEK;
                   Log.e(LOGTAG, " mService.seek failed");
                   showDialog(DIALOG_CMD_FAILED);
                }
             }
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
       updateSearchProgress();
    }
 
    /** Scan related */
    private void initiateSearch(int pty) {
       synchronized (this)
       {
          mIsScaning = true;
          if(mService != null)
          {
             try
             {
                mIsScaning = mService.scan(pty);
                if(mIsScaning == false)
                {
                   mCommandFailed = CMD_SCAN;
                   Log.e(LOGTAG, " mService.scan failed");
                   showDialog(DIALOG_CMD_FAILED);
                }
                else
                {
                   mScanPty = pty;
                }
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
             updateSearchProgress();
          }
       }
    }
 
    /** SEEK Station with the matching PI */
    private void initiatePISearch(int pi) {
       Log.d(LOGTAG, "initiatePISearch");
       if(mService != null)
       {
          try
          {
             if(!isSeekActive())
             {
                mIsSeeking = mService.seekPI(pi);
                if(mIsSeeking == false)
                {
                   mCommandFailed = CMD_SEEKPI;
                   Log.e(LOGTAG, " mService.seekPI failed");
                   showDialog(DIALOG_CMD_FAILED);
                }
             }
          } catch (RemoteException e)
          {
             e.printStackTrace();
          }
       }
       updateSearchProgress();
    }
 
    private void cancelSearch() {
       synchronized (this)
       {
          if(mService != null)
          {
             try
             {
                if( (mIsScaning == true)
                   || (mIsSeeking == true)
                   || (mIsSearching == true))
                {
                   if(true == mService.cancelSearch())
                   {
                       mIsScaning = false;
                       mIsSeeking = false;
                       mIsSearching=false;
                   }
                   else
                   {
                      mCommandFailed = CMD_CANCELSEARCH;
                      Log.e(LOGTAG, " mService.cancelSearch failed");
                      showDialog(DIALOG_CMD_FAILED);
                   }
                }
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
          }
       }
       updateSearchProgress();
    }
 
    /** get Strongest Stations */
    private void initiateSearchList() {
       synchronized (this)
       {
          mIsSearching = false;
          if(mService != null)
          {
             try
             {
                mIsSearching = mService.searchStrongStationList(NUM_AUTO_PRESETS_SEARCH);
                if(mIsSearching == false)
                {
                   mCommandFailed = CMD_SEARCHLIST;
                   Log.e(LOGTAG, " mService.searchStrongStationList failed");
                   showDialog(DIALOG_CMD_FAILED);
                }
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
             updateSearchProgress();
          }
       }
    }
 
    private static final int UPDATE_PROGRESS_DLG = 1;
    private static final int END_PROGRESS_DLG = 2;
    private static final int TIMEOUT_PROGRESS_DLG = 3;
    private static final int SHOWBUSY_TIMEOUT = 300000;
    private Handler mSearchProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_PROGRESS_DLG) {
               //Log.d(LOGTAG, "mTimeoutHandler: UPDATE_PROGRESS_DLG");
               if(mProgressDialog != null)
               {
                  double frequency = mTunedStation.getFrequency() / 1000.0;
                  String titleStr = getString(R.string.msg_search_title, ("" + frequency));
 
                  mProgressDialog.setTitle(titleStr);
               }
            }
            else if (msg.what == END_PROGRESS_DLG) {
               //Log.d(LOGTAG, "mTimeoutHandler: END_PROGRESS_DLG");
               mSearchProgressHandler.removeMessages(END_PROGRESS_DLG);
               mSearchProgressHandler.removeMessages(UPDATE_PROGRESS_DLG);
               mSearchProgressHandler.removeMessages(TIMEOUT_PROGRESS_DLG);
               removeDialog(DIALOG_PROGRESS_PROGRESS);
               mProgressDialog=null;
            }
            else if(msg.what == TIMEOUT_PROGRESS_DLG) {
               cancelSearch();
            }
        }
    };
 
    /** Sleep Handling: After the timer expires, the app needs to shut down */
    private static final int SLEEPTIMER_EXPIRED = 0x1001;
    private static final int SLEEPTIMER_UPDATE = 0x1002;
    private Thread mSleepUpdateHandlerThread = null;
    /*
     * Phone time when the App has to be shut down, calculated based on what the
     * user configured
     */
    private long mSleepAtPhoneTime = 0;
    private boolean mSleepCancelled = false;
 
    private void initiateSleepTimer(long seconds) {
       mSleepAtPhoneTime = (SystemClock.elapsedRealtime()) + (seconds * 1000);
       Log.d(LOGTAG, "Sleep in seconds : " + seconds);
 
       mSleepCancelled = false;
       if (mSleepUpdateHandlerThread == null)
       {
          mSleepUpdateHandlerThread = new Thread(null, doSleepProcessing,
                                                 "SleepUpdateThread");
       }
       /* Launch he dummy thread to simulate the transfer progress */
       Log.d(LOGTAG, "Thread State: " + mSleepUpdateHandlerThread.getState());
       if (mSleepUpdateHandlerThread.getState() == Thread.State.TERMINATED)
       {
          mSleepUpdateHandlerThread = new Thread(null, doSleepProcessing,
                                                 "SleepUpdateThread");
       }
       /* If the thread state is "new" then the thread has not yet started */
       if (mSleepUpdateHandlerThread.getState() == Thread.State.NEW)
       {
          mSleepUpdateHandlerThread.start();
       }
    }
 
    private void endSleepTimer() {
       mSleepAtPhoneTime = 0;
       mSleepCancelled = true;
       // Log.d(LOGTAG, "endSleepTimer");
    }
 
    private boolean hasSleepTimerExpired() {
       boolean expired = true;
       if (isSleepTimerActive())
       {
          long timeNow = ((SystemClock.elapsedRealtime()));
          //Log.d(LOGTAG, "hasSleepTimerExpired - " + mSleepAtPhoneTime + " now: "+ timeNow);
          if (timeNow < mSleepAtPhoneTime)
          {
             expired = false;
          }
       }
       return expired;
    }
 
    private boolean isSleepTimerActive() {
       boolean active = false;
       if (mSleepAtPhoneTime > 0)
       {
          active = true;
       }
       return active;
    }
 
    private void updateExpiredSleepTime() {
       int vis = View.INVISIBLE;
       if (isSleepTimerActive())
       {
          long timeNow = ((SystemClock.elapsedRealtime()));
          if (mSleepAtPhoneTime >= timeNow)
          {
             long seconds = (mSleepAtPhoneTime - timeNow) / 1000;
             String sleepMsg = makeTimeString(seconds);
             //Log.d(LOGTAG, "updateExpiredSleepTime: " + sleepMsg);
             mSleepMsgTV.setText(sleepMsg);
             if (seconds < SLEEP_TOGGLE_SECONDS)
             {
                int nowVis = mSleepMsgTV.getVisibility();
                vis = (nowVis == View.INVISIBLE) ? View.VISIBLE
                      : View.INVISIBLE;
             } else
             {
                vis = View.VISIBLE;
             }
          } else
          {
             /* Clean up timer */
             mSleepAtPhoneTime = 0;
          }
       }
       mSleepMsgTV.setVisibility(vis);
    }
 
    private Handler mUIUpdateHandlerHandler = new Handler() {
       public void handleMessage(Message msg) {
          switch (msg.what)
          {
          case SLEEPTIMER_EXPIRED: {
                mSleepAtPhoneTime = 0;
                if (mSleepCancelled != true)
                {
                   //Log.d(LOGTAG, "mUIUpdateHandlerHandler - SLEEPTIMER_EXPIRED");
                   DebugToasts("Turning Off FM Radio", Toast.LENGTH_SHORT);
                   disableRadio();
                }
                return;
             }
          case SLEEPTIMER_UPDATE: {
                //Log.d(LOGTAG, "mUIUpdateHandlerHandler - SLEEPTIMER_UPDATE");
                updateExpiredSleepTime();
                break;
             }
          default:
             break;
          }
          super.handleMessage(msg);
       }
    };
 
    /* Thread processing */
    private Runnable doSleepProcessing = new Runnable() {
       public void run() {
          boolean sleepTimerExpired = hasSleepTimerExpired();
          while (sleepTimerExpired == false)
          {
             try
             {
                Thread.sleep(500);
             } catch (InterruptedException e)
             {
                Thread.currentThread().interrupt();
             }
             Message statusUpdate = new Message();
             statusUpdate.what = SLEEPTIMER_UPDATE;
             mUIUpdateHandlerHandler.sendMessage(statusUpdate);
             sleepTimerExpired = hasSleepTimerExpired();
          }
          Message finished = new Message();
          finished.what = SLEEPTIMER_EXPIRED;
          mUIUpdateHandlerHandler.sendMessage(finished);
       }
    };
 
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale
                                                        .getDefault());
    private static final Object[] sTimeArgs = new Object[5];
 
    private String makeTimeString(long secs) {
       String durationformat = getString(R.string.durationformat);
 
       /*
        * Provide multiple arguments so the format can be changed easily by
        * modifying the xml.
        */
       sFormatBuilder.setLength(0);
 
       final Object[] timeArgs = sTimeArgs;
       timeArgs[0] = secs / 3600;
       timeArgs[1] = secs / 60;
       timeArgs[2] = (secs / 60) % 60;
       timeArgs[3] = secs;
       timeArgs[4] = secs % 60;
 
       return sFormatter.format(durationformat, timeArgs).toString();
    }
 
    private void tuneRadio(int frequency){
       /* Issue the tune command only if tuneCommand is already not active */
       if( (mService != null) && (mCommandActive != CMD_TUNE))
       {
          boolean bStatus = false;
          try
          {
 
             bStatus = mService.tune(frequency);
             mTunedStation.setName("");
             mTunedStation.setPI(0);
             mTunedStation.setPty(0);
             updateStationInfoToUI();
             if(bStatus)
             {
                postTimeoutHandler(CMD_TUNE);
             }
             else
             {
                mCommandFailed = CMD_TUNE;
                Log.e(LOGTAG, " mService.tune failed");
                showDialog(DIALOG_CMD_FAILED);
             }
          } catch (RemoteException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
    }
 
    /* Start a Command timeout
    */
    private synchronized void postTimeoutHandler(int cmd){
 
       mCommandActive = cmd;
       mCommandTimeoutHandler.sendEmptyMessageDelayed (MSG_CMD_TIMEOUT, CMD_TIMEOUT_DELAY_MS);
    }
 
    /* Stop the Command timeout
    */
    private synchronized void cleanupTimeoutHandler(){
       mCommandActive = CMD_NONE;
       mCommandTimeoutHandler.removeMessages(MSG_CMD_TIMEOUT);
    }
    /* Command timeout Handler
       Routine to handle the Command timeouts for FM operations
       that return asynchronous event callbacks
    */
    private Handler mCommandTimeoutHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
          switch (msg.what)
          {
          case MSG_CMD_TIMEOUT:
             {
                if (mCommandActive>0)
                {
                   Log.d(LOGTAG, "mCommandTimeoutHandler: Cmd failed : "+mCommandActive);
                   mCommandTimeoutHandler.removeMessages(MSG_CMD_TIMEOUT);
                   showDialog(DIALOG_CMD_TIMEOUT);
                   return;
                }
                break;
             }//case MSG_CMD_TIMEOUT
          }//switch
       }//handleMessage
    };
 
 
    private void resetFMStationInfoUI()
    {
       mTunedStation.setFrequency(FmSharedPreferences.getTunedFrequency());
       mTunedStation.setName("");
       mTunedStation.setPI(0);
       mTunedStation.setRDSSupported(false);
       //mTunedStation.setPI(20942);
       mTunedStation.setPty(0);
       mRadioTextTV.setText("");
       mProgramServiceTV.setText("");
       mRadioTextScroller.stopScroll();
       updateStationInfoToUI();
    }
 
    final Runnable mRadioEnabled = new Runnable() {
       public void run() {
          /* Update UI to FM On State */
          enableRadioOnOffUI(true);
          /* Tune to the last tuned frequency */
          tuneRadio(FmSharedPreferences.getTunedFrequency());
       }
    };
 
    final Runnable mRadioDisabled = new Runnable() {
       public void run() {
          /* Update UI to FM Off State */
          enableRadioOnOffUI(false);
       }
    };
 
    final Runnable mUpdateStationInfo = new Runnable() {
       public void run() {
          cleanupTimeoutHandler();
          PresetStation station = FmSharedPreferences.getStationFromFrequency(FmSharedPreferences.getTunedFrequency());
          if (station != null)
          {
             mTunedStation.Copy(station);
          }
 
          updateSearchProgress();
          resetFMStationInfoUI();
       }
    };
 
    final Runnable mSearchComplete = new Runnable() {
       public void run() {
          Log.d(LOGTAG, "mSearchComplete: ");
          mScanPty=0;
          mIsScaning = false;
          mIsSeeking = false;
          mIsSearching = false;
          updateSearchProgress();
          resetFMStationInfoUI();
       }
    };
 
    final Runnable mSearchListComplete = new Runnable() {
       public void run() {
          Log.d(LOGTAG, "mSearchListComplete: ");
          mIsSearching = false;
 
          /* Now get the list*/
          if(mService != null)
          {
             try
             {
                int[] searchList = mService.getSearchList();
                if (searchList != null)
                {
                   /* Add the stations into the preset list */
                   int currentList = FmSharedPreferences.getCurrentListIndex();
                   for( int station=0;
                        (station < searchList.length) && (station < NUM_AUTO_PRESETS_SEARCH);
                        station++)
                   {
                      int frequency = searchList[station];
                      Log.d(LOGTAG, "mSearchListComplete: [" + station + "] = " +  frequency);
                      if( (frequency <= FmSharedPreferences.getUpperLimit())
                         && (frequency >= FmSharedPreferences.getLowerLimit()))
                      {
                         FmSharedPreferences.addStation("", searchList[station],currentList);
                      }
 
                      if(frequency == 0)
                      {
                         break;
                      }
                   }
                }
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
          }
          updateSearchProgress();
          resetFMStationInfoUI();
          setupPresetLayout();
       }
    };
 
    final Runnable mOnMute = new Runnable() {
       public void run() {
          setMuteModeButtonImage(true);
       }
    };
 
    final Runnable mOnStereo = new Runnable() {
       public void run() {
          if(FMRADIO_UI_STATION_AUDIO_STEREO == mStereo)
          {
             mStereoTV.setText(R.string.audio_type_stereo);
          }
          //else if(FMRADIO_UI_STATION_AUDIO_MONO == mStereo)
          //{
          //   mStereoTV.setText(R.string.audio_type_mono);
          //}
          else
          {
             mStereoTV.setText("");
          }
       }
    };
 
    final Runnable mUpdateRadioText = new Runnable() {
       public void run() {
          String str = "";
          if(mService != null)
          {
             try
             {
                /* Get Radio Text and update the display */
                str = mService.getRadioText();
 
                /* Update only if all the characters are printable */
                if (TextUtils.isGraphic(str))
                //if(isStringPrintable(str))
                {
                   Log.d(LOGTAG, "mUpdateRadioText: Updatable string: [" + str + "]");
                   mRadioTextTV.setText(str);
                }
                /* Rest the string to empty*/
                else if (TextUtils.isEmpty(str))
                {
                   mRadioTextTV.setText("");
                } else
                {
                   //Log.d(LOGTAG, "mUpdateRadioText: Leaving old string " + mRadioTextTV.getText());
                }
 
                /* Get PTY and PI and update the display */
                int tempInt =mService.getProgramType();
                /* Save PTY */
                mTunedStation.setPty(tempInt);
                mProgramTypeTV.setText(PresetStation.parsePTY(tempInt));
                tempInt =mService.getProgramID();
                mStationCallSignTV.setText(PresetStation.parsePI(tempInt));
                if(tempInt != 0)
                {
                   mTunedStation.setPI(tempInt);
                }
                /* For non-Empty, non-Printable string, just leave the
                   existing old string
                */
                mRadioTextScroller.startScroll();
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
          }
       }
    };
 
    /* Create runnable for posting */
    final Runnable mUpdateProgramService = new Runnable() {
       public void run() {
          String str = "";
          if(mService != null)
          {
             try
             {
                /* Get the Station PS and update the display */
                str = mService.getProgramService();
                /* Update only if all the characters are printable */
                //if(isStringPrintable(str))
                if (TextUtils.isGraphic(str))
                {
                   Log.d(LOGTAG, "mUpdateProgramService: Updatable string: [" + str + "]");
                   mProgramServiceTV.setText(str);
                }
                /* Rest the string to empty*/
                else if (TextUtils.isEmpty(str))
                {
                   mProgramServiceTV.setText("");
                } else
                {
                   /* For non-Empty, non-Printable string, just leave the
                      existing old string
                   */
                   //Log.d(LOGTAG, "mUpdateProgramService: Leaving old string " + mProgramServiceTV.getText());
                }
                /* Get PTY and PI and update the display */
                int tempInt =mService.getProgramType();
                /* Save PTY */
                mTunedStation.setPty(tempInt);
 
                mProgramTypeTV.setText(PresetStation.parsePTY(tempInt));
                tempInt =mService.getProgramID();
                /* Save the program ID */
                if(tempInt != 0)
                {
                   mTunedStation.setPI(tempInt);
                }
                mStationCallSignTV.setText(PresetStation.parsePI(tempInt));
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
          }
       }
    };
 
    private void DebugToasts(String str, int duration) {
       //Toast.makeText(this, str, duration).show();
       Log.d(LOGTAG, "Debug:" + str);
    }
 
    /**
     * This Handler will scroll the text view.
     * On startScroll, the scrolling starts after SCROLLER_START_DELAY_MS
     * The Text View is scrolled left one character after every
     * SCROLLER_UPDATE_DELAY_MS
     * When the entire text is scrolled, the scrolling will restart
     * after SCROLLER_RESTART_DELAY_MS
     */
    private static final class ScrollerText extends Handler
    {
 
       private static final byte SCROLLER_STOPPED = 0x51;
       private static final byte SCROLLER_STARTING = 0x52;
       private static final byte SCROLLER_RUNNING = 0x53;
 
       private static final int SCROLLER_MSG_START   = 0xF1;
       private static final int SCROLLER_MSG_TICK    = 0xF2;
       private static final int SCROLLER_MSG_RESTART = 0xF3;
 
       private static final int SCROLLER_START_DELAY_MS = 1000;
       private static final int SCROLLER_RESTART_DELAY_MS = 3000;
       private static final int SCROLLER_UPDATE_DELAY_MS = 200;
 
       private final WeakReference<TextView> mView;
 
       private byte mStatus = SCROLLER_STOPPED;
       String mOriginalString;
       int    mStringlength=0;
       int    mIteration=0;
 
       ScrollerText(TextView v) {
          mView = new WeakReference<TextView>(v);
       }
 
       /**
        * Scrolling Message Handler
        */
       @Override
       public void handleMessage(Message msg) {
          switch (msg.what)
          {
          case SCROLLER_MSG_START:
             mStatus = SCROLLER_RUNNING;
             updateText();
             break;
          case SCROLLER_MSG_TICK:
             updateText();
             break;
          case SCROLLER_MSG_RESTART:
             if (mStatus == SCROLLER_RUNNING)
             {
                startScroll();
             }
             break;
          }
       }
 
       /**
        * Moves the text left by one character and posts a
        * delayed message for next update after SCROLLER_UPDATE_DELAY_MS.
        * If the entire string is scrolled, then it displays the entire string
        * and waits for SCROLLER_RESTART_DELAY_MS for scrolling restart
        */
       void updateText() {
          if (mStatus != SCROLLER_RUNNING)
          {
             return;
          }
 
          removeMessages(SCROLLER_MSG_TICK);
 
          final TextView textView = mView.get();
          if (textView != null)
          {
             String szStr2 = "";
             if (mStringlength > 0)
             {
                mIteration++;
                if (mIteration >= mStringlength)
                {
                   mIteration = 0;
                   sendEmptyMessageDelayed(SCROLLER_MSG_RESTART, SCROLLER_RESTART_DELAY_MS);
                } else
                {
                   sendEmptyMessageDelayed(SCROLLER_MSG_TICK, SCROLLER_UPDATE_DELAY_MS);
                }
                //String szStr1 = mOriginalString.substring(0, mTick);
                szStr2 = mOriginalString.substring(mIteration);
             }
             //textView.setText(szStr2+"     "+szStr1);
             textView.setText(szStr2);
          }
       }
 
       /**
        * Stops the scrolling
        * The textView will be set to the original string.
        */
       void stopScroll() {
          mStatus = SCROLLER_STOPPED;
          removeMessages(SCROLLER_MSG_TICK);
          removeMessages(SCROLLER_MSG_RESTART);
          removeMessages(SCROLLER_MSG_START);
          resetScroll();
       }
 
       /**
        * Resets the scroll to display the original string.
        */
       private void resetScroll() {
          mIteration = 0;
          final TextView textView = mView.get();
          if (textView != null)
          {
             textView.setText(mOriginalString);
          }
       }
 
       /** Starts the Scrolling of the TextView after a
        * delay of SCROLLER_START_DELAY_MS
        * Starts only if Length > 0
        */
       void startScroll() {
          final TextView textView = mView.get();
          if (textView != null)
          {
             mOriginalString = (String) textView.getText();
             mStringlength = mOriginalString.length();
             if (mStringlength > 0)
             {
                mStatus = SCROLLER_STARTING;
                sendEmptyMessageDelayed(SCROLLER_MSG_START, SCROLLER_START_DELAY_MS);
             }
          }
       }
    }
 
 
    public static IFMRadioService sService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();
 
    public static boolean bindToService(Context context) {
       Log.e(LOGTAG, "bindToService: Context");
       return bindToService(context, null);
    }
 
    public static boolean bindToService(Context context, ServiceConnection callback) {
       Log.e(LOGTAG, "bindToService: Context with serviceconnection callback");
       context.startService(new Intent(context, FMRadioService.class));
       ServiceBinder sb = new ServiceBinder(callback);
       sConnectionMap.put(context, sb);
       return context.bindService((new Intent()).setClass(context,
                                                          FMRadioService.class), sb, 0);
    }
 
    public static void unbindFromService(Context context) {
       ServiceBinder sb = (ServiceBinder) sConnectionMap.remove(context);
       Log.e(LOGTAG, "unbindFromService: Context");
       if (sb == null)
       {
          Log.e(LOGTAG, "Trying to unbind for unknown Context");
          return;
       }
       context.unbindService(sb);
       if (sConnectionMap.isEmpty())
       {
          // presumably there is nobody interested in the service at this point,
          // so don't hang on to the ServiceConnection
          sService = null;
       }
    }
 
    private static class ServiceBinder implements ServiceConnection
    {
       ServiceConnection mCallback;
       ServiceBinder(ServiceConnection callback) {
          mCallback = callback;
       }
 
       public void onServiceConnected(ComponentName className, android.os.IBinder service) {
          sService = IFMRadioService.Stub.asInterface(service);
          if (mCallback != null)
          {
             Log.e(LOGTAG, "onServiceConnected: mCallback");
             mCallback.onServiceConnected(className, service);
          }
       }
 
       public void onServiceDisconnected(ComponentName className) {
          if (mCallback != null)
          {
             mCallback.onServiceDisconnected(className);
          }
          sService = null;
       }
    }
 
    private ServiceConnection osc = new ServiceConnection() {
       public void onServiceConnected(ComponentName classname, IBinder obj) {
          mService = IFMRadioService.Stub.asInterface(obj);
          Log.e(LOGTAG, "ServiceConnection: onServiceConnected: ");
          if (mService != null)
          {
             try
             {
                mService.registerCallbacks(mServiceCallbacks);
 
                enableRadio();
             } catch (RemoteException e)
             {
                e.printStackTrace();
             }
             return;
          } else
          {
             Log.e(LOGTAG, "IFMRadioService onServiceConnected failed");
          }
          if (getIntent().getData() == null)
          {
             Intent intent = new Intent(Intent.ACTION_MAIN);
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             intent.setClass(FMRadio.this, FMRadio.class);
             startActivity(intent);
          }
          finish();
       }
       public void onServiceDisconnected(ComponentName classname) {
       }
    };
 
    private IFMRadioServiceCallbacks.Stub  mServiceCallbacks = new IFMRadioServiceCallbacks.Stub()
    {
       public void onEnabled()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onEnabled :");
          mHandler.post(mRadioEnabled);
       }
 
       public void onDisabled()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onDisabled :");
          mHandler.post(mRadioDisabled);
       }
 
       public void onTuneStatusChanged()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onTuneStatusChanged :");
          cleanupTimeoutHandler();
          mHandler.post(mUpdateStationInfo);
       }
 
       public void onProgramServiceChanged()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onProgramServiceChanged :");
          mHandler.post(mUpdateProgramService);
       }
 
       public void onRadioTextChanged()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onRadioTextChanged :");
          mHandler.post(mUpdateRadioText);
       }
 
       public void onAlternateFrequencyChanged()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onAlternateFrequencyChanged :");
       }
 
       public void onSignalStrengthChanged()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onSignalStrengthChanged :");
       }
 
       public void onSearchComplete()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onSearchComplete :");
          mHandler.post(mSearchComplete);
       }
       public void onSearchListComplete()
       {
          Log.d(LOGTAG, "mServiceCallbacks.onSearchListComplete :");
          mHandler.post(mSearchListComplete);
       }
 
       public void onMute(boolean bMuted)
       {
          Log.d(LOGTAG, "mServiceCallbacks.onMute :" + bMuted);
          mHandler.post(mOnMute);
       }
 
       public void onAudioUpdate(boolean bStereo)
       {
          if((bStereo) && (FmSharedPreferences.getAudioOutputMode()))
          {
             mStereo = FMRADIO_UI_STATION_AUDIO_STEREO;
          }
          else
          {
             mStereo = FMRADIO_UI_STATION_AUDIO_MONO;
 
          }
          Log.d(LOGTAG, "mServiceCallbacks.onAudioUpdate :" + mStereo);
          mHandler.post(mOnStereo);
       }
 
       public void onStationRDSSupported(boolean bRDSSupported)
       {
          Log.d(LOGTAG, "mServiceCallbacks.onStationRDSSupported :" + bRDSSupported);
          /*
          * Depending on the signal strength etc, RDS Lock Sync/Supported may toggle,
          * Since if a station Supports RDS, it will not change its support intermittently
          * just save the status and ignore any "unsupported" state.
          */
          if(bRDSSupported)
          {
             mTunedStation.setRDSSupported(true);
          }
       }
    };
 }
