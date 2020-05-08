 package jp.takuo.android.mushroom.nowplaying;
 
 /**
 
  * Copyright (c) 2010, Takuo Kitame (http://bit.ly/northeye)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.preference.PreferenceManager;
 
 public class MushroomNP extends Activity implements OnClickListener {
 
     private static final String PREFS_FORMAT_STRING = "format_string";
     private static final String DEFAULT_FORMAT_STRING = "#NowPlaying %t - %a";
     private static final String LOG_TAG = "MushroomNP";
     private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
     private String mAction;
     private ServiceConnection mConn;
     private Button mButtonOK;
     private Button mButtonCancel;
     private Button mButtonRefresh;
     private EditText mEditFormat;
     private TextView mTextView;
    private String mNowPlaying = "";
     private String mFormatString;
 
    private void replace() {
         Intent out = new Intent(ACTION_INTERCEPT);
        out.putExtra("replace_key", mNowPlaying);
         setResult(RESULT_OK, out);
     }
     
     @Override
     public void onDestroy() {
         super.onDestroy();
         if (mConn != null) unbindService(mConn);
     }
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         mFormatString = 
             PreferenceManager.getDefaultSharedPreferences(
                 getApplicationContext()).getString(
                     PREFS_FORMAT_STRING, DEFAULT_FORMAT_STRING);
         Intent it = getIntent();
         mAction = it.getAction();
 
         mConn = new MediaServiceConnection();
         bindService(new Intent().setClassName("com.android.music",
                                               "com.android.music.MediaPlaybackService"),
                     mConn, 0);
         MushroomNP.this.setContentView(R.layout.main);
         mButtonOK = (Button)findViewById(R.id.btn_ok);
         mButtonCancel = (Button)findViewById(R.id.btn_cancel);
         mButtonRefresh = (Button)findViewById(R.id.btn_refresh);
         mEditFormat = (EditText)findViewById(R.id.edit_format);
         mEditFormat.setText(mFormatString);
         mTextView = (TextView)findViewById(R.id.text_now_playing);
 /*
         if (mAction != null && ACTION_INTERCEPT.equals(mAction)) {
 
         } else {
 
         }
 */
         mButtonOK.setOnClickListener(this);
         mButtonCancel.setOnClickListener(this);
         mButtonRefresh.setOnClickListener(this);
     }
 
     public class MediaServiceConnection implements ServiceConnection {
         private com.android.music.IMediaPlaybackService mService;
 
         public String getFormattedString(String format) {
             String ret;
             ret = format.replace("%%", "%");
             try {
                 if (mService.isPlaying()) {
                     ret = ret.replace("%a", mService.getArtistName());
                     ret = ret.replace("%t", mService.getTrackName());
                     ret = ret.replace("%l", mService.getAlbumName());
                 } else {
                     ret = "Not Playing";
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 ret = "Error!!";
               }
             Log.d(LOG_TAG, "Return value: " + ret);
             return ret;
         }
 
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             mService = com.android.music.IMediaPlaybackService.Stub.asInterface(service);
             mTextView.setText(getFormattedString(mFormatString));
         }
 
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "Disconnected Serivice");
         }
     }
 
     @Override
     public void onClick(View v) {
         if (v == mButtonOK)
            replace();
         if (v == mButtonOK || v == mButtonCancel)
             finish();
         if (v == mButtonRefresh) {
             mFormatString = mEditFormat.getText().toString();
             PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).edit().putString(PREFS_FORMAT_STRING, mFormatString);
             Log.d(LOG_TAG, "Format: " + mFormatString);
             mTextView.setText(((MediaServiceConnection)mConn).getFormattedString(mFormatString));
             // Toast.makeText(this, "update...", 500).show();
         }
     }
 }
