 package net.nowatch;
 
 // http://developer.android.com/intl/fr/guide/appendix/media-formats.html
 
 import java.io.File;
 
 import net.nowatch.ui.ListItems;
import net.nowatch.ui.TwitterList;
 import net.nowatch.utils.Db;
 import net.nowatch.utils.Prefs;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import android.widget.TabHost;
 
 public class Main extends TabActivity {
 
     public static final String TAG = "NWTV:";
     public static final int TYPE_TV = 1;
     public static final int TYPE_FM = 2;
     public static final int TYPE_BLOG = 3;
     public static final int TYPE_TWITTER = 4;
     public static final String EXTRA_TYPE = "extra_type";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         DisplayMetrics dm = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(dm);
 
         // Save some values in prefs
         SharedPreferences prefs = PreferenceManager
                 .getDefaultSharedPreferences(getApplicationContext());
         Editor edit = prefs.edit();
         edit.putInt(Prefs.KEY_DENSITY, dm.densityDpi);
         edit.commit();
         // Initialize Database
         // new DB(getApplicationContext()).getReadableDatabase();
         if (!new File(Db.DB_PATH + Db.DB_NAME).exists()) {
             new Db(getApplicationContext()).copyDbToDevice();
         }
 
         //startActivity(new Intent(Main.this, ListItems.class).putExtra(EXTRA_TYPE, TYPE_FM));
 
         setContentView(R.layout.main);
 
         // Tabs setup
         Resources res = getResources();
         TabHost tabHost = getTabHost();
         TabHost.TabSpec spec;
         Intent intent;
 
         // NoWatch.TV
         intent = new Intent().setClass(Main.this, ListItems.class).putExtra(EXTRA_TYPE, TYPE_TV);
         spec = tabHost.newTabSpec("TV").setIndicator("", res.getDrawable(R.drawable.tab_tv))
                 .setContent(intent);
         tabHost.addTab(spec);
 
         // NoWatch.FM
         intent = new Intent().setClass(Main.this, ListItems.class).putExtra(EXTRA_TYPE, TYPE_FM);
         spec = tabHost.newTabSpec("FM").setIndicator("", res.getDrawable(R.drawable.tab_fm))
                 .setContent(intent);
         tabHost.addTab(spec);
 
         // Twitter
         intent = new Intent().setClass(Main.this, TwitterList.class).putExtra(EXTRA_TYPE, TYPE_TWITTER);
         spec = tabHost.newTabSpec("TW").setIndicator("", res.getDrawable(R.drawable.tab_twitter))
                 .setContent(intent);
         tabHost.addTab(spec);
 
         tabHost.setCurrentTab(0);
     }
 }
