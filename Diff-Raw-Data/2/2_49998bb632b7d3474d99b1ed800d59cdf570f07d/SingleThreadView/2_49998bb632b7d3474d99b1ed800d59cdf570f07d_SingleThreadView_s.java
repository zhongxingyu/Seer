 package cc.hughes.droidchatty;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.view.KeyEvent;
 
 public class SingleThreadView extends FragmentActivity
 {
     public static final String THREAD_ID = "threadId";
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         if (savedInstanceState == null)
         {
         	//create the fragment, and show it!
         	ThreadViewFragment frag = new ThreadViewFragment();
         	frag.setArguments(getIntent().getExtras());
         	getSupportFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
         }
     }
     
     @Override
     public boolean dispatchKeyEvent(KeyEvent event)
     {
        ThreadViewFragment fragment = (ThreadViewFragment)getSupportFragmentManager().findFragmentById(R.id.singleThread);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         Boolean handleVolume = prefs.getBoolean("useVolumeButtons", false);
         if (fragment != null && handleVolume)
         {
             if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
             {
                 if (event.getAction() == KeyEvent.ACTION_DOWN)
                     fragment.adjustSelected(-1);
                 return true;
             }
             else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
             {
                 if (event.getAction() == KeyEvent.ACTION_DOWN)
                     fragment.adjustSelected(1);
                 return true;
             }
         }
         return super.dispatchKeyEvent(event);
     }
 }
 
