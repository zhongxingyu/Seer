 package com.example.psyhology_tests;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ruslik
  * Date: 15.10.13
  * Time: 9:08
  * To change this template use File | Settings | File Templates.
  */
 public class TestListActivity extends ActionBarActivity {
 
     Intent intent = new Intent();
     StartFragment k;
     FragmentTransaction tx;
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
    boolean DrawerState;
     private ActionBarDrawerToggle mDrawerToggle;
      String[] fragmentsList = {"com.example.psyhology_tests.StartFragment","com.example.psyhology_tests.FragmentTestList"};
   //  private String[] testArray;
     Button exit;
     String[] listForList = {"Главная", "Список тестов"};
     // FragmentTransaction fTrans;
     StartFragment fragmentTestList = new StartFragment();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
         setContentView(R.layout.activity_main);
         if(getResources().getBoolean(R.bool.istablet)){
             fragmentsList [1] = "com.example.psyhology_tests.FragmentAbout";
             listForList[1] = "О програме";
         }
         tx = getSupportFragmentManager().beginTransaction();
         k = new StartFragment();
           if(!getResources().getBoolean(R.bool.istablet)){
 
 
               tx.replace(R.id.content_frame, k);
 
         }
         if(getResources().getBoolean(R.bool.istablet)){
             tx.replace(R.id.fragment2ForTablet, k);
         }
         tx.commit();
 
 
         ///To Navigation Drawer
 
         //testArray = getResources().getStringArray(R.array.tests_array);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.tvlayouts, listForList));
 
         mDrawerList.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, final View view, final int pos, long id) {
                 mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
 
                     @Override
                     public void onDrawerClosed(View drawerView) {
                         super.onDrawerClosed(drawerView);
                         tx = getSupportFragmentManager().beginTransaction();
                         if(!getResources().getBoolean(R.bool.istablet)){
                             tx.replace(R.id.content_frame, Fragment.instantiate(TestListActivity.this, fragmentsList[pos]));
                         }
                         if(getResources().getBoolean(R.bool.istablet)){
                             tx.replace(R.id.fragment2ForTablet, Fragment.instantiate(TestListActivity.this, fragmentsList[pos]));
                               }
                         tx.commit();
                     }
                 });
                 mDrawerLayout.closeDrawer(mDrawerList);
             }
         });
 
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
          getSupportActionBar().setDisplayHomeAsUpEnabled(true);
          getSupportActionBar().setHomeButtonEnabled(true);
 

         /*mDrawerLayout.setDrawerListener(mDrawerToggle);
         if (savedInstanceState == null) {
            fTrans.replace(R.id.content_frame,fragmentTestList);
         }*/
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     public void replace(Fragment fr) {
 
         tx.replace(R.id.content_frame, fr);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         tx = getSupportFragmentManager().beginTransaction();
         switch (item.getItemId()) {
 
             case R.id.item2:
                 // Toast.makeText(getApplicationContext(),"item1",Toast.LENGTH_LONG).show();
                 DrawerState=mDrawerLayout.isDrawerOpen(mDrawerList);
                if(DrawerState){
                mDrawerLayout.closeDrawer(mDrawerList);
            }
                if(!DrawerState) {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                //tx.replace(R.id.content_frame, Fragment.instantiate(TestListActivity.this, "com.example.psyhology_tests.FragmentTestList")).commit();
 
                 //  replace(Fragment.instantiate(TestListActivity.this, "com.example.psyhology_tests.FragmentTestList"));
                 return true;
             case R.id.item1:
                 //Toast.makeText(getApplicationContext(),"item2",Toast.LENGTH_LONG).show();
                 if(!getResources().getBoolean(R.bool.istablet)){
                     tx.replace(R.id.content_frame, Fragment.instantiate(TestListActivity.this, "com.example.psyhology_tests.StartFragment")).commit();
                 }
               else  tx.replace(R.id.fragment2ForTablet, Fragment.instantiate(TestListActivity.this, "com.example.psyhology_tests.StartFragment")).commit();
                 // replace(Fragment.instantiate(TestListActivity.this, "com.example.psyhology_tests.Fragment2"));
                 return true;
            case android.R.id.home:
                DrawerState=mDrawerLayout.isDrawerOpen(mDrawerList);
                if(DrawerState){
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
                if(!DrawerState) {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
         }
 
         return super.onOptionsItemSelected(item);
 
     }
 }
