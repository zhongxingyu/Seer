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
 import fr.ybo.ybotv.android.exception.YboTvErreurReseau;
 import fr.ybo.ybotv.android.modele.Channel;
 import fr.ybo.ybotv.android.modele.LastUpdate;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.service.YboTvService;
 import fr.ybo.ybotv.android.util.TacheAvecGestionErreurReseau;
 
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 public class LoadingActivity extends SherlockActivity {
 
     private TextView messageLoading;
     private ProgressBar loadingBar;
 
     private Handler handler = new Handler();
 
 
 
     private boolean mustUpdate(LastUpdate lastUpdate) {
         Date date = new Date();
 
         long timeSinceLastUpdate = date.getTime() - lastUpdate.getLastUpdate().getTime();
         long twoDays = TimeUnit.DAYS.toMillis(2);
 
         Log.d(YboTvApplication.TAG, "timeSinceLastUpdate : " + timeSinceLastUpdate);
         Log.d(YboTvApplication.TAG, "twoDays : " + twoDays);
 
         return (timeSinceLastUpdate > twoDays);
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
            } catch (PackageManager.NameNotFoundException ignore) {}

            ((TextView) findViewById(R.id.loading_version)).setText(getString(R.string.version, currentVersion));

             getSupportActionBar().setTitle(R.string.loading);
             loadDatas();
         } else {
             finish();
             startActivity(new Intent(this, NowActivity.class));
         }
 
     }
 
 
     @SuppressWarnings("unchecked")
     private void loadDatas() {
         messageLoading.setText(R.string.getChannels);
         new TacheAvecGestionErreurReseau(this) {
 
             @Override
             protected void myDoBackground() throws YboTvErreurReseau{
                 // Récupération des chaines
                 List<Channel> channels = YboTvService.getInstance().getChannels();
 
                 handler.post(new Runnable() {
 
                     @Override
                     public void run() {
                         messageLoading.setText(R.string.getProgrammes);
                     }
                 });
 
                 Set<String> programeIds = new HashSet<String>();
 
                 List<Programme> programmesToInsert = new ArrayList<Programme>();
 
                 int count = 0;
                 int nbChaines = channels.size();
 
                 for (Channel channel : channels) {
 
                     for (Programme programme : YboTvService.getInstance().getProgrammes(channel)) {
 
                         if (!programeIds.contains(programme.getId())) {
                             programme.fillCsaRating();
                             programmesToInsert.add(programme);
                             programeIds.add(programme.getId());
                         }
                     }
 
                     count++;
                     final int progress = 100 * count / (nbChaines + 2);
                     handler.post(new Runnable() {
                         @Override
                         public void run() {
                             loadingBar.setProgress(progress);
                         }
                     });
                 }
 
 
                 handler.post(new Runnable() {
 
                     @Override
                     public void run() {
                         messageLoading.setText(R.string.loadingChannels);
                     }
                 });
 
                 YboTvDatabase database = ((YboTvApplication) getApplication()).getDatabase();
                 // Suppression des anciennes chaînes.
                 database.deleteAll(Channel.class);
 
                 // insertion des nouvelles chaines
                 try {
                     database.beginTransaction();
                     for (Channel channel : channels) {
                         database.insert(channel);
                     }
                 } finally {
                     database.endTransaction();
                 }
 
                 handler.post(new Runnable() {
 
                     @Override
                     public void run() {
                         messageLoading.setText(R.string.loadingProgrammes);
                     }
                 });
 
                 count++;
                 final int progress = 100 * count / (nbChaines + 2);
                 handler.post(new Runnable() {
                     @Override
                     public void run() {
                         loadingBar.setProgress(progress);
                     }
                 });
 
                 // Suppression de tout les programmes.
                 database.deleteAll(Programme.class);
 
                 try {
                     database.beginTransaction();
                     for (Programme programme : programmesToInsert) {
                         database.insert(programme);
                     }
                 } finally {
                     database.endTransaction();
                 }
 
                 database.deleteAll(LastUpdate.class);
                 LastUpdate lastUpdate = new LastUpdate();
                 lastUpdate.setLastUpdate(new Date());
                 database.insert(lastUpdate);
             }
 
             @Override
             protected void onPostExecute(Void aVoid) {
                 super.onPostExecute(aVoid);
                 finish();
                 startActivity(new Intent(LoadingActivity.this, NowActivity.class));
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
