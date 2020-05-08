 package com.sysu.youtour.controller;
 
 import com.sysu.shen.youtour.R;
 import com.sysu.youtour.util.GlobalConst;
 import com.winsontan520.wversionmanager.library.WVersionManager;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TabHost;
 import android.widget.TabWidget;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 
     TabHost                                    tabHost;
 
     TabWidget                                  tabWidget;
 
     LinearLayout                               bottom_layout;
 
     int                                        CURRENT_TAB                 = 0;
 
     ExploreFragment                            exploreFragment;
 
     MytourFragment                             mytourFragment;
 
     SettingFragment                            settingFragment;
 
     public static String                       SHOWLOCALLIST_HIDE_PROGRESS = "SHOWLOCALLIST_HIDE_PROGRESS";
 
     private HideProgressReceiver               hpReceiver;
 
     private ProgressDialog                     mProgressDialog;
 
     android.support.v4.app.FragmentTransaction ft;
 
     RelativeLayout                             tabIndicator1, tabIndicator2, tabIndicator3;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         IntentFilter filter = new IntentFilter(SHOWLOCALLIST_HIDE_PROGRESS);
         hpReceiver = new HideProgressReceiver();
         registerReceiver(hpReceiver, filter);
 
         setContentView(R.layout.activity_main);
         // 填充tab内容
         findTabView();
         tabHost.setup();
         // 设置tab按钮监听器
         TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {
             @Override
             public void onTabChanged(String tabId) {
 
                 android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                 exploreFragment = (ExploreFragment) fm.findFragmentByTag("explore");
                 mytourFragment = (MytourFragment) fm.findFragmentByTag("mytour");
                 settingFragment = (SettingFragment) fm.findFragmentByTag("setting");
                 ft = fm.beginTransaction();
 
                 if (exploreFragment != null)
                     ft.detach(exploreFragment);
 
                 if (mytourFragment != null)
                     ft.detach(mytourFragment);
 
                 if (settingFragment != null)
                     ft.detach(settingFragment);
 
                 if (tabId.equalsIgnoreCase("explore")) {
                     isTabExplore();
                     CURRENT_TAB = 1;
 
                 } else if (tabId.equalsIgnoreCase("mytour")) {
                     isTabMytour();
                     CURRENT_TAB = 2;
 
                 } else if (tabId.equalsIgnoreCase("setting")) {
                     isTabSetting();
                     CURRENT_TAB = 3;
                 } else {
                     switch (CURRENT_TAB) {
                         case 1:
                             isTabExplore();
                             break;
                         case 2:
                             isTabMytour();
                             break;
                         case 3:
                             isTabSetting();
                             break;
                         default:
                             isTabExplore();
                             break;
                     }
 
                 }
                 ft.commit();
             }
 
             private void isTabExplore() {
                 if (exploreFragment == null) {
                     ft.add(R.id.realtabcontent, new ExploreFragment(), "explore");
                 } else {
                     ft.attach(exploreFragment);
                 }
 
             }
 
             private void isTabMytour() {
                 // Toast.makeText(MainActivity.this, "正在建设中，敬请期待",
                 // Toast.LENGTH_SHORT).show();
                 if (mytourFragment == null) {
                     ft.add(R.id.realtabcontent, new MytourFragment(), "mytour");
                 } else {
                     ft.attach(mytourFragment);
                 }
 
             }
 
             private void isTabSetting() {
                 if (settingFragment == null) {
                     ft.add(R.id.realtabcontent, new SettingFragment(), "setting");
                 } else {
                     ft.attach(settingFragment);
                 }
 
             }
 
         };
         tabHost.setCurrentTab(0);
         tabHost.setOnTabChangedListener(tabChangeListener);
         // 初始化tab
         initTab();
         tabHost.setCurrentTab(0);
 
     }
 
     private void initTab() {
         TabHost.TabSpec tSpecHome = tabHost.newTabSpec("explore");
         tSpecHome.setIndicator(tabIndicator1);
         tSpecHome.setContent(new DummyTabContent(getBaseContext()));
         tabHost.addTab(tSpecHome);
 
         TabHost.TabSpec tSpecWall = tabHost.newTabSpec("mytour");
         tSpecWall.setIndicator(tabIndicator2);
         tSpecWall.setContent(new DummyTabContent(getBaseContext()));
         tabHost.addTab(tSpecWall);
 
         TabHost.TabSpec tSpecCamera = tabHost.newTabSpec("setting");
         tSpecCamera.setIndicator(tabIndicator3);
         tSpecCamera.setContent(new DummyTabContent(getBaseContext()));
         tabHost.addTab(tSpecCamera);
 
     }
 
     private void findTabView() {
         tabHost = (TabHost) findViewById(android.R.id.tabhost);
         tabWidget = (TabWidget) findViewById(android.R.id.tabs);
         LinearLayout layout = (LinearLayout) tabHost.getChildAt(0);
         TabWidget tw = (TabWidget) layout.getChildAt(1);
 
         tabIndicator1 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
         TextView tvTab1 = (TextView) tabIndicator1.getChildAt(1);
         ImageView ivTab1 = (ImageView) tabIndicator1.getChildAt(0);
         ivTab1.setBackgroundResource(R.drawable.selector_explore);
         tvTab1.setText(R.string.explore);
 
         tabIndicator2 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
         TextView tvTab2 = (TextView) tabIndicator2.getChildAt(1);
         ImageView ivTab2 = (ImageView) tabIndicator2.getChildAt(0);
         ivTab2.setBackgroundResource(R.drawable.selector_mytour);
         tvTab2.setText(R.string.my_tour);
 
         tabIndicator3 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
         TextView tvTab3 = (TextView) tabIndicator3.getChildAt(1);
         ImageView ivTab3 = (ImageView) tabIndicator3.getChildAt(0);
         ivTab3.setBackgroundResource(R.drawable.selector_setting);
         tvTab3.setText(R.string.setting);
 
     }
 
     // 菜单按钮
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        unregisterReceiver(hpReceiver);
         this.finish();
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onBackPressed() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setIcon(android.R.drawable.ic_dialog_info);
         builder.setTitle("确定退出吗?");
         builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 finish();
             }
         });
         builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
             }
         });
         builder.create();
         builder.show();
 
     }
 
     // 搜索按钮响应
     public void searchClicked(View v) {
         // Toast.makeText(this, "button is clicked", Toast.LENGTH_SHORT).show();
     }
 
     // 按照地点浏览响应
     public void placeClicked(View v) {
         Intent it = new Intent(MainActivity.this, ExploreMain.class);
         it.putExtra(GlobalConst.EXPLORE_PLACE, true);
         it.putExtra(GlobalConst.EXPLORE_TOPIC, false);
         startActivity(it);
     }
 
     // 按照主题浏览响应
     public void topicClicked(View v) {
         Intent it = new Intent(MainActivity.this, ExploreMain.class);
         it.putExtra(GlobalConst.EXPLORE_PLACE, false);
         it.putExtra(GlobalConst.EXPLORE_TOPIC, true);
         startActivity(it);
     }
 
     // 按照附近浏览响应
     public void nearmeClicked(View v) {
         // Toast.makeText(this, "正在建设中，敬请期待！", Toast.LENGTH_SHORT).show();
         Intent it = new Intent(MainActivity.this, NearMe.class);
         startActivity(it);
     }
 
     // 按照二维码浏览响应
     public void qrcodeClicked(View v) {
         // Toast.makeText(this, "正在建设中，敬请期待！", Toast.LENGTH_SHORT).show();
         showProgress("正在启动二维码……");
         Intent it = new Intent(MainActivity.this, DecoderActivity.class);
         startActivity(it);
     }
 
     // 退出登陆按钮
     public void exitLog(View v) {
 
     }
 
     // 检查更新
     public void checkVersion(View v) {
         WVersionManager versionManager = new WVersionManager(this);
 
         versionManager.setVersionContentUrl(GlobalConst.URL_APP_UPDATE);
         versionManager.setTitle("检查到新的更新");
         versionManager.setUpdateNowLabel("马上更新");
         // versionManager.setRemindMeLaterLabel(remindMeLaterLabel.getText().toString());
         versionManager.setIgnoreThisVersionLabel("取消");
         // versionManager.setUpdateUrl("http://103.31.20.60:3000/YouTour.apk");
         // versionManager.setReminderTimer(Integer.valueOf(reminderTimer.getText()
         // .toString()));
         versionManager.checkVersion();
     }
 
     private class HideProgressReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context arg0, Intent arg1) {
             hideProgress();
         }
     }
 
     private void hideProgress() {
         if (mProgressDialog != null && mProgressDialog.isShowing()) {
             ProgressDialog tmp = mProgressDialog;
             mProgressDialog = null;
             tmp.dismiss();
         }
     }
 
     private void showProgress(String message) {
         mProgressDialog = ProgressDialog.show(this, "请稍后", message);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unregisterReceiver(hpReceiver);
     }
     
     
 }
