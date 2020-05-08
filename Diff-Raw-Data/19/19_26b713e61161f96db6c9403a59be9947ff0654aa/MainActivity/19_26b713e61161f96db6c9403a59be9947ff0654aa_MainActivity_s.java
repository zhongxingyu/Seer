 package com.dynobjx;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTabHost;
 import butterknife.InjectView;
 import butterknife.Views;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.dynobjx.fragments.FavoritesFragment;
 import com.dynobjx.fragments.HomeFragment;
 import com.dynobjx.fragments.SearchFragment;
 import com.dynobjx.fragments.SettingsFragment;
 import com.dynobjx.fragments.TopRatedFragment;
 
 public class MainActivity extends SherlockFragmentActivity {
 	@InjectView(android.R.id.tabhost) FragmentTabHost ftabHost;
 	 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         Views.inject(this);
         ftabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
         
         ActionBar actionBar = getSupportActionBar();
         actionBar.setTitle(R.string.app_name);
         actionBar.setDisplayShowHomeEnabled(false);
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
         actionBar.setCustomView(R.layout.custom_actionbar);
         
         ftabHost.addTab(ftabHost.newTabSpec("Top").setIndicator(null, getResources().getDrawable(R.drawable.btn_toprated)),
                 TopRatedFragment.class, null);
         ftabHost.addTab(ftabHost.newTabSpec("Search").setIndicator(null, getResources().getDrawable(R.drawable.btn_search)),
                 SearchFragment.class, null);
         ftabHost.addTab(ftabHost.newTabSpec("Home").setIndicator(null, getResources().getDrawable(R.drawable.btn_home)),
                 HomeFragment.class, null);
         ftabHost.addTab(ftabHost.newTabSpec("Settings").setIndicator(null, getResources().getDrawable(R.drawable.btn_settings)),
                 SettingsFragment.class, null);
         ftabHost.addTab(ftabHost.newTabSpec("Faves").setIndicator(null, getResources().getDrawable(R.drawable.btn_favorites)),
                 FavoritesFragment.class, null);
 
         ftabHost.setCurrentTabByTag("Home");
         
         for (int ctr = 0; ctr < ftabHost.getTabWidget().getChildCount(); ctr++) {
        	//if(ctr != 2) {
        		ftabHost.getTabWidget().getChildAt(ctr).getLayoutParams().height = 130;
        	//}
         }
 
        //ftabHost.getTabWidget().getChildAt(2).getLayoutParams().width = 136;
        //ftabHost.getTabWidget().getChildAt(2).getLayoutParams().height = 134;
     }
 
 }
