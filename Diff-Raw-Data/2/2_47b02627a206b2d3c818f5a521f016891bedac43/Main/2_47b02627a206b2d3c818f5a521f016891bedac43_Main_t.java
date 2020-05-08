package edu.upenn.seas.pennapps.dumbledore.pollio;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class Main extends Activity {
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         GCMUtils.initGCM(this);
     }
 
     public void newPoll(View view)
     {
         Intent i = new Intent(this, MultipleChoicePoll.class);
         startActivity(i);
     }
 
     public void TakePollio(View view)
     {
 
     }
 
     public void ShowMyPollio(View view)
     {
 
     }
 
 
 
 
 }
