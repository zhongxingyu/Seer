 package com.hmd.emotibox;
 
 import android.view.View;
 import android.os.Bundle;
 import android.app.Activity;
 import android.widget.TabHost;
 import android.content.Intent;
 import android.widget.TextView;
 import android.app.TabActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.content.res.Resources;
 import android.widget.TabHost.TabSpec;
import android.util.Log;
 
 import com.hmd.emotibox.FirstTabActivity;
 import com.hmd.emotibox.SecondTabActivity;
 import com.hmd.emotibox.ThirdTabActivity;
 import com.hmd.emotibox.FourthTabActivity;
 
 public class Emotibox extends TabActivity {
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         Resources res = getResources(); // Resource object to get Drawables
         TabHost tabHost = getTabHost();  // The activity TabHost
 
         Intent intent1 = new Intent(this, FirstTabActivity.class);
        Log.d("Emotibox", intent1.toString());
         TabSpec spec1 = tabHost.newTabSpec("first");
         spec1.setIndicator("One", res.getDrawable(R.drawable.ic_tab1));
         spec1.setContent(intent1);
         
         Intent intent2 = new Intent(this, SecondTabActivity.class);
        Log.d("Emotibox", intent2.toString());
         TabSpec spec2 = tabHost.newTabSpec("second");
         spec2.setIndicator("Two", res.getDrawable(R.drawable.ic_tab2));
         spec2.setContent(intent2);
         
         Intent intent3 = new Intent(this, ThirdTabActivity.class);
        Log.d("Emotibox", intent4.toString());
         TabSpec spec3 = tabHost.newTabSpec("third");
         spec3.setIndicator("Three", res.getDrawable(R.drawable.ic_tab3));
         spec3.setContent(intent3);
         
         Intent intent4 = new Intent(this, FourthTabActivity.class);
        Log.d("Emotibox", intent4.toString());
         TabSpec spec4 = tabHost.newTabSpec("fourth");
         spec4.setIndicator("Four", res.getDrawable(R.drawable.ic_tab4));
         spec4.setContent(intent4);
         
         tabHost.addTab(spec1);
         tabHost.addTab(spec2);
         tabHost.addTab(spec3);
         tabHost.addTab(spec4);
         
         tabHost.setCurrentTab(0);
     }
 }
