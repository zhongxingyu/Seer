 package com.gwenhael.idontwannawakeuplikecrap;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.content.Intent;
import android.provider.AlarmClock;
 
 import com.gwenhael.idontwannawakeuplikecrap.AboutActivity;
 
 public class IDontWannaWakeUpLikeCrapActivity
     extends Activity
 {
     private static final int DEFAULT_NAP_TIME = 20;
     private static final int DEFAULT_FALL_ASLEEP_TIME = 14;
 
     protected LinearLayout main_layout;
     protected LinearLayout result_layout;
     protected TextView presentation;
 
    protected Calendar wake_up;

     private Timer timer;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main); // before trying to link widgets
 
         main_layout = (LinearLayout) findViewById( R.id.layout );
         result_layout = (LinearLayout) findViewById( R.id.result_layout );
         presentation = (TextView) findViewById( R.id.presentation );
         
         refresh(  );
 
         timer = new Timer(  );
         timer.schedule( new TimerRefresh(  ), 100, 200 );
     }
    
     @Override
    protected void onDestroy(  )
     {
         super.onDestroy();
         
         timer.cancel();
     }
 
     @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch ( item.getItemId(  ) ) {
             case R.id.about:
                 about(  );
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void about(  )
     {
         startActivity( new Intent(this, AboutActivity.class) );
     }
     
     private void refresh(  )
     {
         int time_to_fall_asleep = this.DEFAULT_FALL_ASLEEP_TIME;
 
         Calendar asleep = Calendar.getInstance();
         asleep.add( Calendar.MINUTE, time_to_fall_asleep );
        wake_up = (Calendar)asleep.clone(  ); // what's with the casting crap?
 
         result_layout.removeAllViewsInLayout(  );
         for ( int i = 0 ; i < 6 ; i++ ) {
             Button result_text = new Button( this );
 
             wake_up.add( Calendar.HOUR, 1 );
             wake_up.add( Calendar.MINUTE, 30 );
             result_text.setText( justTheTime( wake_up ) );
            result_text.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        int h = wake_up.get( Calendar.HOUR );
                        // h.add( Calendar.MILLISECOND, wake_up.get( Calendar.ZONE_OFFSET ) );
                        int m = wake_up.get( Calendar.MINUTE );

                        Intent i = new Intent( AlarmClock.ACTION_SET_ALARM );
                        i.putExtra( AlarmClock.EXTRA_HOUR, h );
                        i.putExtra( AlarmClock.EXTRA_MINUTES, m );
                        startActivity(i);
                    }
                });
 
             result_layout.addView( result_text, i );
         }
     }
 
     private String justTheTime( Calendar c ) {
         String r = new String( ( (Calendar.getInstance()).get( Calendar.DAY_OF_YEAR ) != c.get( Calendar.DAY_OF_YEAR ) ? getString( R.string.tomorrow ) : getString( R.string.today ) ) + getString( R.string.at ) + c.get( Calendar.HOUR_OF_DAY ) + ":" + ( ( c.get( Calendar.MINUTE ) < 10 ) ? "0" : "" ) + c.get( Calendar.MINUTE ) );
 
         return r;
     }
 
     class TimerRefresh extends TimerTask
     {
         public void run()
         {
             IDontWannaWakeUpLikeCrapActivity.this.runOnUiThread(new Runnable()
                 {
                     public void run()
                     {
                         refresh(  );
                     }
                 });
         }
     }
 }
