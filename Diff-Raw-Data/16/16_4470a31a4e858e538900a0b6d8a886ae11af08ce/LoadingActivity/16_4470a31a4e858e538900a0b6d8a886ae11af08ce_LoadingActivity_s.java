 package fr.ybo.ybotv.android.activity;
 
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockActivity;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.database.YboTvDatabase;
 import fr.ybo.ybotv.android.modele.LastUpdate;
 import fr.ybo.ybotv.android.util.UpdateProgrammes;
 
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 public class LoadingActivity extends SherlockActivity {
 
     private TextView messageLoading;
     private ProgressBar loadingBar;
 
     private Handler handler = new Handler();
 
 
     private boolean mustUpdate(LastUpdate lastUpdate) {
         Date date = new Date();
 
         long timeSinceLastUpdate = date.getTime() - lastUpdate.getLastUpdate().getTime();
         long fiveDays = TimeUnit.DAYS.toMillis(5);
 
         return (timeSinceLastUpdate > fiveDays);
     }
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.loading);
 
         YboTvDatabase database = ((YboTvApplication) getApplication()).getDatabase();
         LastUpdate lastUpdate = database.selectSingle(new LastUpdate());
 
         if (lastUpdate == null || mustUpdate(lastUpdate)) {
 
             messageLoading = (TextView) findViewById(R.id.messageLoading);
             loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
 
             String currentVersion = "";
             try {
                 PackageInfo _info = getPackageManager().getPackageInfo(getPackageName(), 0);
                 currentVersion = _info.versionName;
             } catch (PackageManager.NameNotFoundException ignore) {
             }
 
             ((TextView) findViewById(R.id.loading_version)).setText(getString(R.string.version, currentVersion));
 
             getSupportActionBar().setTitle(R.string.loading);
             loadDatas();
         } else {
             finish();
             startActivity(new Intent(this, ((YboTvApplication) getApplication()).getDefaultActivity()));
             ((YboTvApplication) getApplication()).setRecurringAlarm();
         }
 
     }
 
 
     @SuppressWarnings("unchecked")
     private void loadDatas() {
         messageLoading.setText(R.string.getChannels);
         new UpdateProgrammes(this, handler, loadingBar, messageLoading, ((YboTvApplication) getApplication()).getDatabase()) {
             @Override
             protected void onPostExecute(Void aVoid) {
                 super.onPostExecute(aVoid);
                ((YboTvApplication) getApplication()).setRecurringAlarm();
                 finish();
                startActivity(new Intent(LoadingActivity.this, ((YboTvApplication) getApplication()).getDefaultActivity()));
             }
         }.execute();
     }
 
 
     // Verrue pour faire marcher en 1.6.
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ((!(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT)
                 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
             onBackPressed();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public void onBackPressed() {
         // On ne fait rien.
     }
 
 }
