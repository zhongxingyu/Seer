 package dk.dthomasen.aarhus.activity;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.fima.cardsui.views.CardUI;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 
 
 import java.io.File;
 import java.util.Date;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import dk.dthomasen.aarhus.R;
 import dk.dthomasen.aarhus.cards.DescCard;
 import dk.dthomasen.aarhus.cards.ForecastCard;
 import dk.dthomasen.aarhus.cards.NowWeatherCard;
 import dk.dthomasen.aarhus.cards.SunCard;
 import dk.dthomasen.aarhus.service.BaalXmlDownload;
 import dk.dthomasen.aarhus.service.FitnessXmlDownload;
 import dk.dthomasen.aarhus.service.HundeskovXmlDownload;
 import dk.dthomasen.aarhus.service.Service;
 import dk.dthomasen.aarhus.service.ShelterXmlDownload;
 import dk.dthomasen.aarhus.weather.Weather;
 import dk.dthomasen.aarhus.weather.WeatherDownload;
 
 public class MainActivity extends Activity implements View.OnClickListener{
     protected final String TAG = this.getClass().getName();
     private SlidingMenu slidingMenu;
     private CardUI mCardView;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         slidingMenu = new SlidingMenu(this);
         slidingMenu.setMode(SlidingMenu.LEFT);
         slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
         slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
         slidingMenu.setFadeDegree(0.35f);
         slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
         slidingMenu.setMenu(R.layout.slidingmenu);
 
         ActionBar ab = getActionBar();
 
         ab.setDisplayHomeAsUpEnabled(true);
         ab.setTitle("Friluft Aarhus");
         ab.setSubtitle("App'en om friluftsliv i Aarhus");
 
         PreferenceManager.setDefaultValues(this, R.xml.settings, false);
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 
         mCardView = (CardUI) findViewById(R.id.maincardsview);
         mCardView.setSwipeable(false);
 
         DescCard descCard = new DescCard("Velkommen til Friluft Aarhus", "Slide til højre for at vise menuen");
         mCardView.addCard(descCard);
 
         try {
             Weather weather = new WeatherDownload().execute().get();
             NowWeatherCard nowWeatherCard = new NowWeatherCard(this, "Vejret lige nu", "Temperatur: "+weather.getNowTemp()+"°C", weather.getNowCode(), weather.getWindSpeed());
             nowWeatherCard.setOnClickListener(this);
             mCardView.addCard(nowWeatherCard);
 
             SunCard suncard = new SunCard("Sol", weather.getSunrise(), weather.getSunset());
             suncard.setOnClickListener(this);
             mCardView.addCard(suncard);
 
             ForecastCard forecastcard = new ForecastCard(this, "Vejrudsigt", weather);
             forecastcard.setOnClickListener(this);
             mCardView.addCard(forecastcard);
         } catch (InterruptedException e) {
             e.printStackTrace();
         } catch (ExecutionException e) {
             e.printStackTrace();
         }
 
         mCardView.refresh();
 
         int versionCode = 0;
         try {
             PackageInfo packageInfo = this.getPackageManager()
                     .getPackageInfo(this.getPackageName(), 0);
             versionCode = packageInfo.versionCode;
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
 
         SharedPreferences sp = getSharedPreferences("version", Activity.MODE_PRIVATE);
         int versionCodeStored = sp.getInt("version",0);

         SharedPreferences runCheck = getSharedPreferences("hasRunBefore", 0);
         Boolean hasRun = runCheck.getBoolean("hasRun", false);
         if (!hasRun || versionCode != versionCodeStored) {
             SharedPreferences settings = getSharedPreferences("hasRunBefore", 0);
             SharedPreferences.Editor edit = settings.edit();
             edit.putBoolean("hasRun", true); //set to has run
             edit.commit(); //apply
 
             SharedPreferences.Editor editor = sp.edit();
             editor.putInt("version", versionCode);
             editor.commit();
 
             new BaalXmlDownload().execute(this);
             new ShelterXmlDownload().execute(this);
             new FitnessXmlDownload().execute(this);
             new HundeskovXmlDownload().execute(this);
 
            ChangeLogDialog ChangelogDialog = new ChangeLogDialog(this);
            ChangelogDialog.show();
         }
         else {
             //Checking baalsteder update
             File file = new File(getFilesDir().getAbsolutePath()+"/baalsteder.xml");
             Date lastModDate = new Date(file.lastModified());
             Date today = new Date();
             String baalUpdateFreq = sharedPreferences.getString("baal","30");
             long diff = Service.getInstance().getDateDiff(lastModDate, today, TimeUnit.DAYS);
             if(diff <= Long.valueOf(baalUpdateFreq)){
                 new BaalXmlDownload().execute(this);
             }
 
             //Checking shelters update
             file = new File(getFilesDir().getAbsolutePath()+"/shelters.xml");
             lastModDate = new Date(file.lastModified());
             today = new Date();
             String shelterUpdateFreq = sharedPreferences.getString("shelter","30");
             diff = Service.getInstance().getDateDiff(lastModDate, today, TimeUnit.DAYS);
             if(diff <= Long.valueOf(shelterUpdateFreq)){
                 new ShelterXmlDownload().execute(this);
             }
 
             //Checking fitness update
             file = new File(getFilesDir().getAbsolutePath()+"/fitness.xml");
             lastModDate = new Date(file.lastModified());
             today = new Date();
             String fitnessUpdateFreq = sharedPreferences.getString("fitness","30");
             diff = Service.getInstance().getDateDiff(lastModDate, today, TimeUnit.DAYS);
             if(diff <= Long.valueOf(fitnessUpdateFreq)){
                 new FitnessXmlDownload().execute(this);
             }
 
             //Checking hundeskov update
             file = new File(getFilesDir().getAbsolutePath()+"/hundeskove.xml");
             lastModDate = new Date(file.lastModified());
             today = new Date();
             String hundeskoveUpdateFreq = sharedPreferences.getString("hundeskove","30");
             diff = Service.getInstance().getDateDiff(lastModDate, today, TimeUnit.DAYS);
             if(diff <= Long.valueOf(hundeskoveUpdateFreq)){
                 new HundeskovXmlDownload().execute(this);
             }
         }
 
 
     }
 
     @Override
     public void onBackPressed() {
         if ( slidingMenu.isMenuShowing()) {
             slidingMenu.toggle();
         }
         else {
             super.onBackPressed();
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ( keyCode == KeyEvent.KEYCODE_MENU ) {
             this.slidingMenu.toggle();
             return true;
         }else if(keyCode == KeyEvent.KEYCODE_BACK) {
             //Ask the user if they want to quit
             new AlertDialog.Builder(this)
                     .setIcon(android.R.drawable.ic_dialog_alert)
                     .setTitle(R.string.quit)
                     .setMessage(R.string.really_quit)
                     .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
 
                             //Stop the activity
                             MainActivity.this.finish();
                         }
 
                     })
                     .setNegativeButton(R.string.no, null)
                     .show();
 
             return true;
         }
         else {
             return super.onKeyDown(keyCode, event);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 this.slidingMenu.toggle();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         overridePendingTransition(0, 0);
     }
 
     @Override
     public void onClick(View v) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse("http://weather.yahoo.com/danmark/midtjylland/aarhus-552015/"));
         startActivity(intent);
     }
 }
