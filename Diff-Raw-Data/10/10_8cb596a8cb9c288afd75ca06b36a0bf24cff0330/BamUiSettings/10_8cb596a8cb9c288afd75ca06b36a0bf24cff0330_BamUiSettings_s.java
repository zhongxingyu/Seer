 /*
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.settings.jellybam;
 
 import android.app.Activity;
 import android.app.ActivityManagerNative;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceGroup;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.TwoStatePreference;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.IWindowManager;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.channels.FileChannel;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Random;
 import java.security.SecureRandom;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import com.android.settings.jellybam.CodeReceiver;
 import com.android.settings.jellybam.AbstractAsyncSuCMDProcessor;
 import com.android.settings.jellybam.CMDProcessor;
 import com.android.settings.jellybam.Helpers;
 import com.android.settings.jellybam.AlphaSeekBar;
 import com.android.settings.jellybam.Executable;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 
 @SuppressWarnings("InstanceVariableMayNotBeInitialized")
 public class BamUiSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
     private static final String TAG = "Bamuisettings";
 
     private static final boolean DEBUG = false;
 
     private static final String PREF_POWER_CRT_SCREEN_ON = "system_power_crt_screen_on";
     private static final String PREF_POWER_CRT_SCREEN_OFF = "system_power_crt_screen_off";
     private static final String PREF_FULLSCREEN_KEYBOARD = "fullscreen_keyboard";
     private static final String FORCE_DUAL_PANEL = "dual_pane";
     private static final String PREF_180 = "rotate_180";
     private static final String PREF_SHOW_OVERFLOW = "show_overflow";
     private static final CharSequence PREF_DISABLE_BOOTANIM = "disable_bootanimation";
     private static final CharSequence PREF_CUSTOM_BOOTANIM = "custom_bootanimation";
     private static final String PREF_LONGPRESS_TO_KILL = "longpress_to_kill";
     private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";
     private static final String PREF_RAM_USAGE_BAR = "ram_usage_bar";
     private static final String PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED = "wakeup_when_plugged_unplugged";
     private static final String KEYBOARD_ROTATION_TOGGLE = "keyboard_rotation_toggle";
     private static final String KEYBOARD_ROTATION_TIMEOUT = "keyboard_rotation_timeout";
     private static final String VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
 
     private static final int REQUEST_PICK_BOOT_ANIMATION = 203;
     //private static final int REQUEST_PICK_CUSTOM_ICON = 202; //unused
     private static final int KEYBOARD_ROTATION_TIMEOUT_DEFAULT = 5000; // 5s
 
     private static final String BOOTANIMATION_USER_PATH = "/data/local/bootanimation.zip";
     private static final String BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip";
 
     CheckBoxPreference mCrtOff;
     CheckBoxPreference mCrtOn;
     CheckBoxPreference mFullscreenKeyboard;
     CheckBoxPreference mDualPane;
 
     private final Configuration mCurConfig = new Configuration();
     private Context mContext;
 
     private boolean isCrtOffChecked = false;
 
     CheckBoxPreference mAllow180Rotation;
     CheckBoxPreference mDisableBootAnimation;
     Preference mCustomBootAnimation;
     ImageView mView;
     TextView mError;
     CheckBoxPreference mShowActionOverflow;
     CheckBoxPreference mLongPressToKill;
     CheckBoxPreference mRecentKillAll;
     CheckBoxPreference mRamBar;
     AlertDialog mCustomBootAnimationDialog;
     CheckBoxPreference mWakeUpWhenPluggedOrUnplugged;
 
     private CheckBoxPreference mKeyboardRotationToggle;
     private ListPreference mKeyboardRotationTimeout;
     private ListPreference mVolumeKeyCursorControl;
 
     private AnimationDrawable mAnimationPart1;
     private AnimationDrawable mAnimationPart2;
     private String mErrormsg;
     private String mBootAnimationPath;
 
     private CMDProcessor mCMDProcessor = new CMDProcessor();
     private static ContentResolver mContentResolver;
     private Random mRandomGenerator = new SecureRandom();
 
     // previous random; so we don't repeat
     private static int mLastRandomInsultIndex = -1;
     private String[] mInsults;
 
     private int mSeekbarProgress;
     int mUserRotationAngles = -1;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.jellybam_ui_settings);
 
 	mCMDProcessor.setLogcatDebugging(DEBUG);
 
         mContentResolver = getContentResolver();
 
         mContext = getActivity();
 
         PreferenceScreen prefs = getPreferenceScreen();
         mInsults = mContext.getResources().getStringArray(
                 R.array.disable_bootanimation_insults);
 
        // respect device default configuration
         // true fades while false animates
         boolean electronBeamFadesConfig = mContext.getResources().getBoolean(
                 com.android.internal.R.bool.config_animateScreenLights);
 
         isCrtOffChecked = Settings.System.getInt(mContentResolver,
                 Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                 electronBeamFadesConfig ? 0 : 1) == 1;
 
         mCrtOff = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_OFF);
         mCrtOff.setChecked(isCrtOffChecked);
         mCrtOff.setOnPreferenceChangeListener(this);
 
         mCrtOn = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_ON);
         mCrtOn.setChecked(Settings.System.getInt(mContentResolver,
                 Settings.System.SYSTEM_POWER_ENABLE_CRT_ON, 0) == 1);
         mCrtOn.setEnabled(isCrtOffChecked);
         mCrtOn.setOnPreferenceChangeListener(this);
 
         mFullscreenKeyboard = (CheckBoxPreference) findPreference(PREF_FULLSCREEN_KEYBOARD);
         mFullscreenKeyboard.setChecked(Settings.System.getInt(mContentResolver,
                 Settings.System.FULLSCREEN_KEYBOARD, 0) == 1);
 
         mDualPane = (CheckBoxPreference) findPreference(FORCE_DUAL_PANEL);
         mDualPane.setChecked(Settings.System.getBoolean(mContentResolver,
                          Settings.System.FORCE_DUAL_PANEL, getResources().getBoolean(
                          com.android.internal.R.bool.preferences_prefer_dual_pane)));
 
         mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
         mUserRotationAngles = Settings.System.getInt(mContentResolver,
                 Settings.System.ACCELEROMETER_ROTATION_ANGLES, -1);
         if (mUserRotationAngles < 0) {
             // Not set by user so use these defaults
             boolean mAllowAllRotations = mContext.getResources().getBoolean(
                             com.android.internal.R.bool.config_allowAllRotations) ? true : false;
             mUserRotationAngles = mAllowAllRotations  ?
                 (1 | 2 | 4 | 8) : // All angles
                 (1 | 2 | 8); // All except 180
         }
         mAllow180Rotation.setChecked(mUserRotationAngles == (1 | 2 | 4 | 8));
 
         mDisableBootAnimation = (CheckBoxPreference)findPreference(PREF_DISABLE_BOOTANIM);
 
         mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM);
 
         mLongPressToKill = (CheckBoxPreference)findPreference(PREF_LONGPRESS_TO_KILL);
         mLongPressToKill.setChecked(Settings.System.getInt(mContentResolver,
                 Settings.System.KILL_APP_LONGPRESS_BACK, 0) == 1);
 
         mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
         mRecentKillAll.setChecked(Settings.System.getBoolean(mContentResolver,
                 Settings.System.RECENT_KILL_ALL_BUTTON, false));
 
         mRamBar = (CheckBoxPreference) findPreference(PREF_RAM_USAGE_BAR);
         mRamBar.setChecked(Settings.System.getBoolean(mContentResolver,
                 Settings.System.RAM_USAGE_BAR, false));
 
         mShowActionOverflow = (CheckBoxPreference) findPreference(PREF_SHOW_OVERFLOW);
         mShowActionOverflow.setChecked(Settings.System.getBoolean(mContentResolver,
                         Settings.System.UI_FORCE_OVERFLOW_BUTTON, false));
 
         mWakeUpWhenPluggedOrUnplugged = (CheckBoxPreference) findPreference(PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED);
         mWakeUpWhenPluggedOrUnplugged.setChecked(Settings.System.getBoolean(mContentResolver,
                         Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED, true));
 
        mKeyboardRotationToggle = (CheckBoxPreference) findPreference(KEYBOARD_ROTATION_TOGGLE);
         mKeyboardRotationToggle.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.KEYBOARD_ROTATION_TIMEOUT, 0) > 0);
 
         mKeyboardRotationTimeout = (ListPreference) findPreference(KEYBOARD_ROTATION_TIMEOUT);
         mKeyboardRotationTimeout.setOnPreferenceChangeListener(this);
         updateRotationTimeout(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.KEYBOARD_ROTATION_TIMEOUT, KEYBOARD_ROTATION_TIMEOUT_DEFAULT));
 
         mVolumeKeyCursorControl = (ListPreference) findPreference(VOLUME_KEY_CURSOR_CONTROL);
         if(mVolumeKeyCursorControl != null) {
             mVolumeKeyCursorControl.setOnPreferenceChangeListener(this);
             mVolumeKeyCursorControl.setValue(Integer.toString(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0)));
             mVolumeKeyCursorControl.setSummary(mVolumeKeyCursorControl.getEntry());
         }
 
         // hide option if device is already set to never wake up
         if(!mContext.getResources().getBoolean(
                 com.android.internal.R.bool.config_unplugTurnsOnScreen)) {
             ((PreferenceGroup) findPreference("misc")).removePreference(mWakeUpWhenPluggedOrUnplugged);
         }
 
         setHasOptionsMenu(true);
         resetBootAnimation();
     }
 
     public void updateRotationTimeout(int timeout) {
         if (timeout == 0)
             timeout = KEYBOARD_ROTATION_TIMEOUT_DEFAULT;
         mKeyboardRotationTimeout.setValue(Integer.toString(timeout));
         mKeyboardRotationTimeout.setSummary(getString(R.string.keyboard_rotation_timeout_summary, mKeyboardRotationTimeout.getEntry()));
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (mDisableBootAnimation != null) {
             if (mDisableBootAnimation.isChecked()) {
                 Resources res = mContext.getResources();
                 String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                 int randomInt = mRandomGenerator.nextInt(insults.length);
                 mDisableBootAnimation.setSummary(insults[randomInt]);
             } else {
                 mDisableBootAnimation.setSummary(null);
             }
         }
     }
 
     public void mKeyboardRotationDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setMessage(R.string.keyboard_rotation_dialog);
         builder.setCancelable(false);
         builder.setPositiveButton(getResources().getString(com.android.internal.R.string.ok), null);
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     @Override
     public void onPause() {
         super.onPause();
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
             Preference preference) {
         if (preference == mAllow180Rotation) {
             boolean checked = ((TwoStatePreference) preference).isChecked();
             Settings.System.putInt(mContentResolver,
                     Settings.System.ACCELEROMETER_ROTATION_ANGLES,
                     checked ? (1 | 2 | 4 | 8) : (1 | 2 | 8));
             return true;
         } else if (preference == mDisableBootAnimation) {
             DisableBootAnimation();
             return true;
         } else if (preference == mCustomBootAnimation) {
             openBootAnimationDialog();
             return true;
          } else if (preference == mKeyboardRotationToggle) {
             boolean isAutoRotate = (Settings.System.getInt(getContentResolver(),
                         Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
             if (isAutoRotate && mKeyboardRotationToggle.isChecked())
                 mKeyboardRotationDialog();
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.KEYBOARD_ROTATION_TIMEOUT,
                     mKeyboardRotationToggle.isChecked() ? KEYBOARD_ROTATION_TIMEOUT_DEFAULT : 0);
             updateRotationTimeout(KEYBOARD_ROTATION_TIMEOUT_DEFAULT);
         } else if (preference == mShowActionOverflow) {
             boolean enabled = mShowActionOverflow.isChecked();
             Settings.System.putBoolean(mContentResolver, Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                     enabled);
             // Show toast appropriately
             if (enabled) {
                 Toast.makeText(getActivity(), R.string.show_overflow_toast_enable,
                         Toast.LENGTH_LONG).show();
             } else {
                 Toast.makeText(getActivity(), R.string.show_overflow_toast_disable,
                         Toast.LENGTH_LONG).show();
             }
             return true;
         } else if (preference == mLongPressToKill) {
             boolean checked = ((TwoStatePreference) preference).isChecked();
             Settings.System.putInt(mContentResolver,
                     Settings.System.KILL_APP_LONGPRESS_BACK, checked ? 1 : 0);
             return true;
         } else if (preference == mRecentKillAll) {
             boolean checked = ((CheckBoxPreference)preference).isChecked();
             Settings.System.putBoolean(mContentResolver,
                     Settings.System.RECENT_KILL_ALL_BUTTON, checked ? true : false);
             return true;
         } else if (preference == mRamBar) {
             boolean checked = ((CheckBoxPreference)preference).isChecked();
             Settings.System.putBoolean(mContentResolver,
                     Settings.System.RAM_USAGE_BAR, checked ? true : false);
             return true;
         } else if (preference == mWakeUpWhenPluggedOrUnplugged) {
             Settings.System.putBoolean(mContentResolver,
                     Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED,
                     ((TwoStatePreference) preference).isChecked());
             return true;
         } else  if (preference == mFullscreenKeyboard) {
             Settings.System.putInt(mContentResolver, Settings.System.FULLSCREEN_KEYBOARD,
                     mFullscreenKeyboard.isChecked() ? 1 : 0);
         } else if (preference == mDualPane) {
             Settings.System.putBoolean(mContentResolver,
                     Settings.System.FORCE_DUAL_PANEL,
                     ((TwoStatePreference) preference).isChecked());
             return true;
          }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == Activity.RESULT_OK) {
             if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                 if (data==null) {
                     //Nothing returned by user, probably pressed back button in file manager
                     return;
                 }
                 mBootAnimationPath = data.getData().getPath();
                 openBootAnimationDialog();
             }
         }
     }
 
     private void openBootAnimationDialog() {
         resetSwaggedOutBootAnimation();
         Log.e(TAG, "boot animation path: " + mBootAnimationPath);
         if(mCustomBootAnimationDialog != null) {
             mCustomBootAnimationDialog.cancel();
             mCustomBootAnimationDialog = null;
         }
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setTitle(R.string.bootanimation_preview);
         if (!mBootAnimationPath.isEmpty()
                 && (!BOOTANIMATION_SYSTEM_PATH.equalsIgnoreCase(mBootAnimationPath)
                 && !BOOTANIMATION_USER_PATH.equalsIgnoreCase(mBootAnimationPath))) {
             builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     installBootAnim(dialog, mBootAnimationPath);
                     resetBootAnimation();
                 }
             });
         } else if (new File(BOOTANIMATION_USER_PATH).exists()) {
             builder.setPositiveButton(R.string.clear_custom_bootanimation, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     new AbstractAsyncSuCMDProcessor() {
                         @Override
                         protected void onPostExecute(String result) {
                             resetBootAnimation();
                         }
                     }.execute("rm '" + BOOTANIMATION_USER_PATH + "'", "rm '/data/media/bootanimation.backup'");
                 }
             });
         }
         builder.setNeutralButton(R.string.set_custom_bootanimation, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 PackageManager packageManager = getActivity().getPackageManager();
                 Intent test = new Intent(Intent.ACTION_GET_CONTENT);
                 test.setType("file/*");
                 List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                         PackageManager.GET_ACTIVITIES);
                 if (!list.isEmpty()) {
                     Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                     intent.setType("file/*");
                     startActivityForResult(intent, REQUEST_PICK_BOOT_ANIMATION);
                 } else {
                     //No app installed to handle the intent - file explorer required
                     Toast.makeText(mContext, R.string.install_file_manager_error,
                             Toast.LENGTH_SHORT).show();
                 }
 
             }
         });
         builder.setNegativeButton(com.android.internal.R.string.cancel,
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         resetBootAnimation();
                         dialog.dismiss();
                     }
                 });
         LayoutInflater inflater =
                 (LayoutInflater) getActivity()
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View layout = inflater.inflate(R.layout.dialog_bootanimation_preview,
                 (ViewGroup) getActivity()
                         .findViewById(R.id.bootanimation_layout_root));
         mError = (TextView) layout.findViewById(R.id.textViewError);
         mView = (ImageView) layout.findViewById(R.id.imageViewPreview);
         mView.setVisibility(View.GONE);
         Display display = getActivity().getWindowManager().getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         mView.setLayoutParams(new LinearLayout.LayoutParams(size.x / 2, size.y / 2));
         mError.setText(R.string.creating_preview);
         builder.setView(layout);
         mCustomBootAnimationDialog = builder.create();
         mCustomBootAnimationDialog.setOwnerActivity(getActivity());
         mCustomBootAnimationDialog.show();
         Thread thread = new Thread(new Runnable() {
             @Override
             public void run() {
                 createPreview(mBootAnimationPath);
             }
         });
         thread.start();
     }
 
     public void copy(File src, File dst) throws IOException {
         // use file channels for faster byte transfers
         FileChannel inChannel = new
                 FileInputStream(src).getChannel();
         FileChannel outChannel = new
                 FileOutputStream(dst).getChannel();
         try {
             // move the bytes from in to out
             inChannel.transferTo(0,
                     inChannel.size(),
                     outChannel);
         } finally {
             // ensure closure
             if (inChannel != null) inChannel.close();
             if (outChannel != null) outChannel.close();
         }
     }
 
     private void createPreview(String path) {
         File zip = new File(path);
         ZipFile zipfile = null;
         String desc = "";
         InputStream inputStream = null;
         InputStreamReader inputStreamReader = null;
         BufferedReader bufferedReader = null;
         try {
             zipfile = new ZipFile(zip);
             ZipEntry ze = zipfile.getEntry("desc.txt");
             inputStream = zipfile.getInputStream(ze);
             inputStreamReader = new InputStreamReader(inputStream);
             StringBuilder sb = new StringBuilder(0);
             bufferedReader = new BufferedReader(inputStreamReader);
             String read = bufferedReader.readLine();
             while (read != null) {
                 sb.append(read);
                 sb.append('\n');
                 read = bufferedReader.readLine();
             }
             desc = sb.toString();
         } catch (Exception handleAllException) {
             mErrormsg = getActivity().getString(R.string.error_reading_zip_file);
             errorHandler.sendEmptyMessage(0);
             return;
         } finally {
             try {
                 if (bufferedReader != null)
                     bufferedReader.close();
             } catch (IOException e) {
                 // we tried
             }
             try {
                 if (inputStreamReader != null)
                     inputStreamReader.close();
             } catch (IOException e) {
                 // we tried
             }
             try {
                 if (inputStream != null)
                     inputStream.close();
             } catch (IOException e) {
                 // moving on...
             }
         }
 
         String[] info = desc.replace("\\r", "").split("\\n");
         // ignore first two ints height and width
         int delay = Integer.parseInt(info[0].split(" ")[2]);
         String partName1 = info[1].split(" ")[3];
         String partName2;
         try {
             if (info.length > 2) {
                 partName2 = info[2].split(" ")[3];
             }
             else {
                 partName2 = "";
             }
         } catch (Exception e) {
             partName2 = "";
         }
 
         BitmapFactory.Options opt = new BitmapFactory.Options();
         opt.inSampleSize = 4;
         mAnimationPart1 = new AnimationDrawable();
         mAnimationPart2 = new AnimationDrawable();
         try {
             for (Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
                     enumeration.hasMoreElements();) {
                 ZipEntry entry = enumeration.nextElement();
                 if (entry.isDirectory()) {
                     continue;
                 }
                 String partname = entry.getName().split("/")[0];
                 if (partName1.equalsIgnoreCase(partname)) {
                     InputStream partOneInStream = null;
                     try {
                         partOneInStream = zipfile.getInputStream(entry);
                         mAnimationPart1.addFrame(new BitmapDrawable(getResources(),
                                 BitmapFactory.decodeStream(partOneInStream,
                                         null, opt)), delay);
                     } finally {
                         if (partOneInStream != null)
                             partOneInStream.close();
                     }
                 } else if (partName2.equalsIgnoreCase(partname)) {
                     InputStream partTwoInStream = null;
                     try {
                         partTwoInStream = zipfile.getInputStream(entry);
                         mAnimationPart2.addFrame(new BitmapDrawable(getResources(),
                                 BitmapFactory.decodeStream(partTwoInStream,
                                         null, opt)), delay);
                     } finally {
                         if (partTwoInStream != null)
                             partTwoInStream.close();
                     }
                 }
             }
         } catch (IOException e1) {
             mErrormsg = getActivity().getString(R.string.error_creating_preview);
             errorHandler.sendEmptyMessage(0);
             return;
         }
 
         if (!partName2.isEmpty()) {
             Log.d(TAG, "Multipart Animation");
             mAnimationPart1.setOneShot(false);
             mAnimationPart2.setOneShot(false);
             mAnimationPart1.setOnAnimationFinishedListener(
                     new AnimationDrawable.OnAnimationFinishedListener() {
                 @Override
                 public void onAnimationFinished() {
                     Log.d(TAG, "First part finished");
                     mView.setImageDrawable(mAnimationPart2);
                     mAnimationPart1.stop();
                     mAnimationPart2.start();
                 }
             });
         } else {
             mAnimationPart1.setOneShot(false);
         }
         finishedHandler.sendEmptyMessage(0);
     }
 
     /**
      * creates a couple commands to perform all root
      * operations needed to disable/enable bootanimations
      *
      * @param checked state of CheckBox
      * @return script to turn bootanimations on/off
      */
     private String[] getBootAnimationCommand(boolean checked) {
         String[] cmds = new String[3];
         String storedLocation = "/system/media/bootanimation.backup";
         String storedUserLocation = "/data/local/bootanimation.backup";
         String activeLocation = "/system/media/bootanimation.zip";
         String activeUserLocation = "/data/local/bootanimation.zip";
         if (checked) {
             /* make backup */
             cmds[0] = "mv " + activeLocation + ' ' + storedLocation + "; ";
             cmds[1] = "mv " + activeUserLocation + ' ' + storedUserLocation + "; ";
         } else {
             /* apply backup */
             cmds[0] = "mv " + storedLocation + ' ' + activeLocation + "; ";
             cmds[1] = "mv " + activeUserLocation + ' ' + storedUserLocation + "; ";
         }
         /*
          * use sed to replace build.prop property
          * debug.sf.nobootanimation=[1|0]
          *
          * without we get the Android shine animation when
          * /system/media/bootanimation.zip is not found
          */
         cmds[2] = "busybox sed -i \"/debug.sf.nobootanimation/ c "
                 + "debug.sf.nobootanimation=" + (checked ? 1 : 0)
                 + "\" " + "/system/build.prop";
         return cmds;
     }
 
     private Handler errorHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             mView.setVisibility(View.GONE);
             mError.setText(mErrormsg);
         }
     };
 
     private Handler finishedHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             mView.setImageDrawable(mAnimationPart1);
             mView.setVisibility(View.VISIBLE);
             mError.setVisibility(View.GONE);
             mAnimationPart1.start();
         }
     };
 
     private void installBootAnim(DialogInterface dialog, String bootAnimationPath) {
         //Update setting to reflect that boot animation is now enabled
         mDisableBootAnimation.setChecked(false);
         DisableBootAnimation();
         dialog.dismiss();
         Executable installScript = new Executable(
                 "cp " + bootAnimationPath + " /data/local/bootanimation.zip",
                 "chmod 644 /data/local/bootanimation.zip");
         mCMDProcessor.su.runWaitFor(installScript);
     }
 
     private void DisableBootAnimation() {
         resetSwaggedOutBootAnimation();
         if (!mCMDProcessor.su.runWaitFor(
                 "grep -q \"debug.sf.nobootanimation\" /system/build.prop")
                 .success()) {
             // if not add value
             Helpers.getMount("rw");
             mCMDProcessor.su.runWaitFor(String.format("echo debug.sf.nobootanimation=%d >> /system/build.prop",
                     mDisableBootAnimation.isChecked() ? 1 : 0));
             Helpers.getMount("ro");
         }
         // preform bootanimation operations off UI thread
         AbstractAsyncSuCMDProcessor processor = new AbstractAsyncSuCMDProcessor(true) {
             @Override
             protected void onPostExecute(String result) {
                 if (mDisableBootAnimation.isChecked()) {
                     // do not show same insult as last time
                     int newInsult = mRandomGenerator.nextInt(mInsults.length);
                     while (newInsult == mLastRandomInsultIndex)
                         newInsult = mRandomGenerator.nextInt(mInsults.length);
 
                     // update our static index reference
                     mLastRandomInsultIndex = newInsult;
                     mDisableBootAnimation.setSummary(mInsults[newInsult]);
                 } else {
                     mDisableBootAnimation.setSummary(null);
                 }
                 resetBootAnimation();
             }
         };
         processor.execute(getBootAnimationCommand(mDisableBootAnimation.isChecked()));
     }
 
     public boolean onPreferenceChange(Preference preference, Object Value) {
         final String key = preference.getKey();
          if (mCrtOff.equals(preference)) {
             isCrtOffChecked = ((Boolean) Value).booleanValue();
             Settings.System.putInt(mContentResolver,
                     Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                     (isCrtOffChecked ? 1 : 0));
             // if crt off gets turned off, crt on gets turned off and disabled
             if (!isCrtOffChecked) {
                 Settings.System.putInt(mContentResolver,
                         Settings.System.SYSTEM_POWER_ENABLE_CRT_ON, 0);
                 mCrtOn.setChecked(false);
             }
             mCrtOn.setEnabled(isCrtOffChecked);
             return true;
 	} else if (preference == mVolumeKeyCursorControl) {
            String volumeKeyCursorControl = (String) value;
             int val = Integer.parseInt(volumeKeyCursorControl);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.VOLUME_KEY_CURSOR_CONTROL, val);
             int index = mVolumeKeyCursorControl.findIndexOfValue(volumeKeyCursorControl);
             mVolumeKeyCursorControl.setSummary(mVolumeKeyCursorControl.getEntries()[index]);
             return true;
         } else if (preference == mKeyboardRotationTimeout) {
             int timeout = Integer.parseInt((String) Value);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.KEYBOARD_ROTATION_TIMEOUT, timeout);
             updateRotationTimeout(timeout);
             return true;
         } else if (mCrtOn.equals(preference)) {
             Settings.System.putInt(mContentResolver,
                     Settings.System.SYSTEM_POWER_ENABLE_CRT_ON,
                     ((Boolean) Value).booleanValue() ? 1 : 0);
             return true;
         }
         return false;
     }
 
     private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
         String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
         Pattern pattern = Pattern.compile("component=([^/]+)/");
         Matcher matcher = pattern.matcher(intentUri);
 
         String packageName = matcher.find() ? matcher.group(1) : null;
         if (packageName != null) {
             try {
                 getPackageManager().getPackageInfo(packageName, 0);
             } catch (NameNotFoundException e) {
                 Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                 getPreferenceScreen().removePreference(preference);
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Resets boot animation path. Essentially clears temporary-set boot animation
      * set by the user from the dialog.
      * @return returns true if a boot animation exists (user or system). false otherwise.
      */
     private boolean resetBootAnimation() {
         boolean bootAnimationExists = false;
         if(new File(BOOTANIMATION_USER_PATH).exists()) {
             mBootAnimationPath = BOOTANIMATION_USER_PATH;
             bootAnimationExists = true;
         } else if (new File(BOOTANIMATION_SYSTEM_PATH).exists()) {
             mBootAnimationPath = BOOTANIMATION_SYSTEM_PATH;
             bootAnimationExists = true;
         } else {
             mBootAnimationPath = "";
         }
         mCustomBootAnimation.setEnabled(!mDisableBootAnimation.isChecked());
         return bootAnimationExists;
     }
 
     private void resetSwaggedOutBootAnimation() {
         if(new File("/data/local/bootanimation.user").exists()) {
             // we're using the alt boot animation
             Executable moveAnimCommand = new Executable("mv /data/local/bootanimation.user /data/local/bootanimation.zip");
             // we must wait for this command to finish before we continue
             mCMDProcessor.su.runWaitFor(moveAnimCommand);
         }
         CodeReceiver.setSwagInitiatedPref(mContext, false);
     }
 
     protected boolean isCheckBoxPrefernceChecked(Preference p) {
         if(p instanceof CheckBoxPreference) {
             return ((CheckBoxPreference) p).isChecked();
         } else {
             return false;
         }
     }
 
 }
 
 
