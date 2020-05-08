 package io.hgeg.cloudboard;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.pubnub.api.Callback;
 import com.pubnub.api.Pubnub;
 import com.pubnub.api.PubnubError;
 
 import java.util.Hashtable;
 
 public class MainActivity extends Activity {
 
     public static MainActivity model;
     Pubnub pubnub  = new Pubnub("pub-56806acb-9c46-4008-b8cb-899561b7a762","sub-26001c28-a260-11e1-9b23-0916190a0759","",false);
     String channel = "Hgeg";
     public static ClipboardManager clipboard;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         model = this;
         clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
         clipboard.addPrimaryClipChangedListener(new SyncListener());
         this.registerReceiver(new BroadcastReceiver() {
             @Override
             public void onReceive(Context arg0, Intent intent) {
                 pubnub.disconnectAndResubscribe();
             }
 
         }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
 
         TextView clipView = (TextView) this.findViewById(R.id.clipView);
         clipView.setMovementMethod(new ScrollingMovementMethod());
 
 
         subscribe();
 
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onResume(){
         SharedPreferences prefs = getSharedPreferences("clip",MODE_PRIVATE);
         String clipData = prefs.getString("data","");
 
         TextView clipView = (TextView) this.findViewById(R.id.clipView);
         clipView.setText(clipData);
        super.onResume();
         Synchronize s = new Synchronize();
         s.execute();
     }
 
     private void subscribe() {
         Hashtable args = new Hashtable(1);
         args.put("channel", channel);
 
         try {
             pubnub.subscribe(args, new Callback() {
                 @Override
                public void connectCallback(String channel,Object message) {}
                 @Override
                 public void disconnectCallback(String channel,Object message) {}
                 @Override
                 public void reconnectCallback(String channel,Object message) {}
                 @Override
                 public void successCallback(String channel,Object message) {
                    System.out.println(message);
                     Synchronize s = new Synchronize();
                     s.execute();
                 }
                 @Override
                 public void errorCallback(String channel,PubnubError error) {}
             });
         } catch (Exception e) {
         }
     }
 
     public void push() {
         TextView clipView = (TextView) this.findViewById(R.id.clipView);
         clipView.setText(MainActivity.getClipText());
         Push p = new Push();
         p.execute();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
         switch (item.getItemId()) {
             case R.id.action_settings:
                 Intent settings = new Intent();
                 settings.setClassName("io.hgeg.cloudboard", "io.hgeg.cloudboard.SettingsActivity");
                 //settings.putExtra("com.android.samples.SpecialValue", "Hello, Joe!");
                 startActivity(settings);
 
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
     public static String getClipText() {
         String textToPaste = "";
         if (clipboard.hasPrimaryClip()) {
             ClipData clip = clipboard.getPrimaryClip();
             // if you need text data only, use:
             if (clip.getDescription().hasMimeType("text/plain"))
                textToPaste = clip.getItemAt(0).getText().toString();
         }

         if (!TextUtils.isEmpty(textToPaste))
             return textToPaste;
         else return "";
     }
 
     public static void setClipText(String clipText) {
         clipboard.setPrimaryClip(ClipData.newPlainText("io.hgeg.cloudboard.clipdata",clipText));
     }
 
 
 }
 
