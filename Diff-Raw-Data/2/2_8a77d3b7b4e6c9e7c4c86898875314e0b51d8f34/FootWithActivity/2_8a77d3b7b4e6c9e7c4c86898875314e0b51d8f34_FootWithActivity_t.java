 package edu.thu.cslab.footwith.client;
 
 import android.app.ActivityGroup;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.TabHost;
 import android.widget.TabWidget;
 import android.widget.TextView;
 
 public class FootWithActivity extends ActivityGroup {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
         
         TabHost mainTabHost=(TabHost)findViewById(R.id.tabhost);
         mainTabHost.setup(this.getLocalActivityManager());
         
         TabHost.TabSpec spec=mainTabHost.newTabSpec("tab1");  
         spec.setContent(new Intent(this,Home.class));
         spec.setIndicator("首页",getResources().getDrawable(R.drawable.home));
         mainTabHost.addTab(spec);  
           
         spec=mainTabHost.newTabSpec("tab2");  
         spec.setContent(new Intent(this,Messages.class));  
         spec.setIndicator("通知",getResources().getDrawable(R.drawable.news));  
         mainTabHost.addTab(spec);  
           
         spec=mainTabHost.newTabSpec("tab3");  
         spec.setContent(new Intent(this,AboutMe.class));  
         spec.setIndicator("我",getResources().getDrawable(R.drawable.me));  
         mainTabHost.addTab(spec); 
         
         TabWidget tabWidget=mainTabHost.getTabWidget();
         
         for (int i=0;i<tabWidget.getChildCount();i++){
         	ImageView iv = (ImageView)tabWidget.getChildTabViewAt(i).findViewById(android.R.id.icon);
         	iv.setPadding(10, 0, 10, 25);
         	TextView tv=(TextView)tabWidget.getChildTabViewAt(i).findViewById(android.R.id.title);
         	tv.setPadding(0, 0, 0, 5);
         }
         mainTabHost.setCurrentTab(0);  
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.main_menu, menu);
         menu.add("景点").setIcon(R.drawable.menu_sites);
         menu.add("帮助").setIcon(R.drawable.menu_help);
         menu.add("联系").setIcon(R.drawable.menu_contact);
         //menu.add("hello");
         return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         String title = (String) item.getTitle();
         if(title.equals("景点")){
             Intent intent = new Intent();
            intent=new Intent(FootWithActivity.this,ProvinceList.class);
             FootWithActivity.this.startActivity(intent);
 
         }else if(title.equals("帮助")){
 
         }else if(title.equals("联系")){
 
 
         }
         return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
     }
 }
