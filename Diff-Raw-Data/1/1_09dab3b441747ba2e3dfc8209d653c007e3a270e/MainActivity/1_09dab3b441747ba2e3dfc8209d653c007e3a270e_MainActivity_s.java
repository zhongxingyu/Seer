 package hk.com.novare.smart.infinitylifestyle;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Build;
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 
 import hk.com.novare.smart.infinitylifestyle.adapter.CategoryPagerAdapter;
 
 public class MainActivity extends FragmentActivity{
 
     CategoryPagerAdapter cpa;
     ViewPager vp;
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         final ActionBar ab = getActionBar();
         ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         cpa = new CategoryPagerAdapter(getSupportFragmentManager());
         vp = (ViewPager) findViewById(R.id.pager);
 
         ActionBar.TabListener tabListener = new ActionBar.TabListener(){
 
             @Override
             public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                 vp.setCurrentItem(tab.getPosition());
             }
 
             @Override
             public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 
             }
 
             @Override
             public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 
             }
         };
 
         // Add 3 tabs, specifying the tab's text and TabListener
         for (int i = 0; i < 5; i++) {
             ActionBar.Tab tab = ab.newTab();
             switch (i){
                 case 0: tab.setText("Latest"); break;
                 case 1: tab.setText("Privileges"); break;
                 case 2: tab.setText("Perks"); break;
                 case 3: tab.setText("mySmart"); break;
                 case 4: tab.setText("Services"); break;
             }
 
             tab.setTabListener(tabListener);
             ab.addTab(tab);
         }
 
         vp.setAdapter(cpa);
         vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
             @Override
             public void onPageSelected(int position) {
                 getActionBar().setSelectedNavigationItem(position);
             }
         });
         getActionBar().setSelectedNavigationItem(1);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
