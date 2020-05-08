 package com.gamalinda.practice.activity;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.widget.DrawerLayout;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.gamalinda.practice.R;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.ItemClick;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.googlecode.androidannotations.annotations.res.StringArrayRes;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 
 @EActivity(R.layout.main)
 public class MainAppActivity extends SherlockActivity {
 
     @StringArrayRes
     String[] things;
 
     @ViewById(R.id.drawer_layout)
     DrawerLayout drawerLayout;
 
     @ViewById(R.id.left_drawer)
     android.widget.ListView leftDrawer;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         initImageLoader();
         getSupportActionBar().show();
     }
 
     private void initImageLoader() {
         ImageLoaderConfiguration config = new ImageLoaderConfiguration
                 .Builder(getApplicationContext())
                 .build();
         ImageLoader.getInstance().init(config);
     }
 
     @AfterViews
     void afterViews() {
         getSupportActionBar().show();
         leftDrawer.setAdapter(new DrawerNavigationListAdapter(this, things));
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         drawerLayout.closeDrawer(leftDrawer);
     }
 
     @ItemClick(resName = "left_drawer")
     void list(int position) {
         Toast.makeText(this, things[position], 1500).show();
         switch (position) {
             case 0:
                 ImageViewActivity_.intent(this).start();
                 break;
             case 1:
                 MapViewActivity_.intent(this).start();
                 break;
             case 2:
                 TabNavigationActivity_.intent(this).start();
                 break;
             case 3:
                 ShareViewActivity_.intent(this).start();
             case 4:
                 UploadViewActivity_.intent(this).start();
             default:
                 break;
         }
     }
 
     private class DrawerNavigationListAdapter extends BaseAdapter {
 
         private LayoutInflater layoutInflater;
 
         String[] listOfThings;
 
         public DrawerNavigationListAdapter(Context context, String[] listOfThings) {
             layoutInflater = LayoutInflater.from(context);
             this.listOfThings = listOfThings;
         }
 
         @Override
         public int getCount() {
             return listOfThings.length;
         }
 
         @Override
         public Object getItem(int position) {
             return listOfThings[position];
         }
 
         @Override
         public long getItemId(int position) {
             return 0;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View view = this.layoutInflater.inflate(R.layout.drawer_item_layout, parent, false);
             TextView textView = (TextView) view.findViewById(R.id.text);
             textView.setText(listOfThings[position]);
             return view;
         }
     }
 }
