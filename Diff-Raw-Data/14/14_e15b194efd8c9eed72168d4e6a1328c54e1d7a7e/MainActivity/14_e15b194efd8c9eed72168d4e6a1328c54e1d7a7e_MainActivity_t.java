 package com.deepmine.by;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Build;
 import android.app.Activity;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxCallback;
 import com.androidquery.callback.AjaxStatus;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.deepmine.by.helpers.ItemImageBinder;
 import com.deepmine.by.helpers.ResourceHelper;
 import com.deepmine.by.models.Blocks;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class MainActivity extends Activity {
 
     public static String TAG = "DEEPMINE:MainActivity";
 
     public static ImageView mPlayBtn;
     public static TextView mTrackTitle1;
     public static TextView mTrackTitle2;
     public static ListView mListView;
     private ProgressDialog loadingDialog = null;
     private Intent radioService;
 
     private AQuery aq = new AQuery(this);
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             getActionBar().hide();
         }
 
         ResourceHelper.getInstance().init(this);
         EasyTracker.getInstance().activityStart(this); // Add this method.
 
         mPlayBtn =(ImageView) findViewById(R.id.playBtn);
         mTrackTitle1 =  (TextView) findViewById(R.id.tackTitle1);
         mTrackTitle2 =  (TextView) findViewById(R.id.tackTitle2);
         mListView = (ListView) findViewById(R.id.listEvents);
 
         radioService = new Intent(this, RadioService.class);
         updateTitle();
         updateEvents();
 
     }
 
     protected void updateTitle()
     {
         Timer timer = new Timer();
 
         class UpdateTask extends TimerTask {
 
             public void run() {
                 aq.ajax("http://deepmine.by/d/index.php/ajaxRadioTitle", JSONObject.class, new AjaxCallback<JSONObject>() {
 
                     @Override
                     public void callback(String url, JSONObject json, AjaxStatus status) {
                         if (json != null) {
                             try
                             {
                                 mTrackTitle1.setText(json.getString("artist"));
                                 mTrackTitle2.setText(json.getString("track"));
                                 aq.id(R.id.trackCover).image("http://deepmine.by/d/static/music/cover/"+json.getString("title")+".jpg",true, true, 0, R.drawable.ic_launcher_full);

                             }
                             catch (JSONException e)
                             {
                                 Log.d(TAG, "Exception parse");
                             }
                         }
                     }
                 });
                updatePlayStatus();
             }
         }
 
         TimerTask updateTask = new UpdateTask();
         timer.scheduleAtFixedRate(updateTask, 0, 5000);
     }
 
     public void updateEvents()
     {
         aq.ajax("http://deepmine.by/android/hotlist", JSONObject.class, new AjaxCallback<JSONObject>() {
 
             @Override
             public void callback(String url, JSONObject json, AjaxStatus status) {
                 if (json != null) {
                     try
                     {
                         Blocks blocks = new Blocks(json.getJSONArray("data"));
 
                         SimpleAdapter simpleAdapter = new SimpleAdapter( getApplicationContext(),
                                 blocks.getList(),
                                 R.layout.menu_row,
                                 getResources().getStringArray(R.array.menu_row_element_names),
                                 ResourceHelper.getInstance().getIntArray(R.array.menu_row_element_ids)
                         );
 
                         simpleAdapter.setViewBinder(new ItemImageBinder().init(
                                 getApplicationContext(), R.drawable.ic_launcher));
 
                         mListView.setAdapter(simpleAdapter);
                         mListView.setDividerHeight(0);
                         mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                         @Override
                         public void onItemClick(AdapterView<?> adapterView, View view,
                                                 int i, long l) {
                             Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(((TextView) view
                                     .findViewById(R.id.link)).getText().toString()));
                             startActivity(browserIntent);
 
 
                         }
                     });
                         simpleAdapter.notifyDataSetChanged();
                     }
                     catch (JSONException e)
                     {
                         Log.d(TAG, "Exception parse");
                     }
 
 
 
                 }
             }
         });
 
     }
 
     public void onPlay(View view)
     {
         if(RadioService.isPlaying())
             stopMedia();
         else
             playMedia();
     }
 
     private void playMedia()
     {
         loadingDialog = new ProgressDialog(this);
         loadingDialog.setMessage(getText(R.string.connection));
         loadingDialog.setCancelable(false);
         loadingDialog.setCanceledOnTouchOutside(false);
         loadingDialog.show();
 
         startService(radioService);
        updatePlayStatus();
     }
 
     private void stopMedia()
     {
         stopService(radioService);
         RadioService.stop();
        updatePlayStatus();
     }
 
    private void updatePlayStatus()
     {
         if(RadioService.isPlaying())
         {
             if(loadingDialog!=null && loadingDialog.isShowing())
                 loadingDialog.dismiss();
 
             mPlayBtn.setImageResource(R.drawable.ic_media_pause);
         }
         else
         {
             mPlayBtn.setImageResource(R.drawable.ic_media_play);
         }
 
         if(RadioService.isErrors())
         {
             if(loadingDialog!=null && loadingDialog.isShowing())
                 loadingDialog.dismiss();
 
             Toast.makeText(this,R.string.error_connection, Toast.LENGTH_SHORT).show();
             RadioService.cleanErrors();
             stopMedia();
         }
 
     }
 
 
     @Override
     public void onStart() {
         EasyTracker.getInstance().activityStart(this);
         super.onStart();
     }
 
     @Override
     public void onStop() {
         EasyTracker.getInstance().activityStop(this);
         super.onStop();
     }
 
     @Override
     public void onDestroy() {
         RadioService.stop();
         super.onDestroy();
     }
 
 }
