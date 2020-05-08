 package com.barti.couriersclient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.barti.adapters.PackagesListAdapter;
 import com.barti.dialogs.LoadingDialog;
 import com.barti.dialogs.MessageDialog;
 import com.barti.models.Package;
 import com.barti.models.PackageSerialize;
 import com.barti.providers.DatabasePackageProvider;
 import com.barti.providers.LoginProvider;
 import com.barti.providers.ScreenOnProvider;
 import com.barti.services.ILogger;
 import com.barti.services.PreferencesService;
 import com.barti.tests.AdminMode;
 import com.barti.tests.PackagesTestProvider;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class DefaultActivity extends Activity {
 
     private RelativeLayout buttonOverflow;
     private ImageButton buttonLogOut;
     private Context mContext;
     private String UserName;
     private LoginProvider mLoginProvider;
     private ListView packagesList;
     private ListView overflowList;
     private TextView userNameText;
     private PreferencesService preferencesService;
     DrawerLayout overflowMenu;
     OverflowView overflowViewAdapter;
     PackagesListAdapter packagesListAdapter;
     Animation showInAnimation;
     DatabasePackageProvider test1;
     private int currentTransport = -1;
     @Override
     protected void onResume()
     {
         if(preferencesService.getCurrentTransport()!=-1 && preferencesService.getCurrentTransport()!= currentTransport)
         {
             currentTransport = preferencesService.getCurrentTransport();
             new GetPackagesAsync(this, test1,String.valueOf(currentTransport)).execute();
 
         }
         ScreenOnProvider.setScreen(this, preferencesService.getScreenOn());
         super.onResume();
     }
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
         setContentView(R.layout.activity_default);
         getValues();
         initialize();
 
     }
 
     private void initialize() {
         mLoginProvider = new LoginProvider(this);
         preferencesService = new PreferencesService(this);
         mContext = this;
         buttonOverflow = (RelativeLayout) findViewById(R.id.button_menu);
         buttonLogOut = (ImageButton) findViewById(R.id.log_out_button);
         packagesList = (ListView) findViewById(R.id.packages_list);
         showInAnimation = AnimationUtils.loadAnimation(this, R.anim.show_in);
         userNameText = (TextView)findViewById(R.id.user_name);
         overflowMenu = (DrawerLayout) findViewById(R.id.overflow_menu);
         overflowList = (ListView) findViewById(R.id.left_drawer);
         test1 = new DatabasePackageProvider(this);
         overflowViewAdapter = new OverflowView(this);
         overflowList.setAdapter(overflowViewAdapter.getAdapter());
         overflowList.setOnItemClickListener(overflowViewAdapter.getListener());
         overflowMenu.setScrimColor(getResources().getColor(R.color.semi_transparent));
 
     }
 
     private void setListeners() {
         buttonOverflow.setOnClickListener(new OnOverflowClick());
         buttonLogOut.setOnClickListener(new OnLogOutClick(mLoginProvider));
         userNameText.setText(UserName);
         setAdminModeText();
     }
 
     @Override
     protected void onStart() {
         super.onStart();

         setListeners();



     }

     private void getValues() {
         UserName = getIntent().getExtras().getString("User");
 
     }
 
     @Override
     protected void onDestroy() {
         // TODO Auto-generated method stub
         super.onDestroy();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK  ) {
             showMessageDialog();
         }
         if ( keyCode == KeyEvent.KEYCODE_HOME) {
             preferencesService.setUserLogin(PreferencesService.UNKNOWN_USER);
             preferencesService.setLogIn(false);
         }
         Log.d("KEYCODE",String.valueOf(keyCode));
         return super.onKeyDown(keyCode, event);
     }
 
     private void showMessageDialog() {
 
         String msg = mContext.getString(R.string.use_log_out_message);
         MessageDialog msgDialog = new MessageDialog(mContext, msg);
         msgDialog.show();
 
     }
 
     private class OnOverflowClick implements OnClickListener {
 
         @Override
         public void onClick(View v) {
         if(!overflowMenu.isDrawerOpen(overflowList))  {
                     overflowMenu.openDrawer(overflowList);
            }else {
                overflowMenu.closeDrawer(overflowList);
         }
         }
 
     }
 
     private class OnLogOutClick implements OnClickListener {
         ILogger mILogger;
 
         public OnLogOutClick(ILogger iLogger) {
             mILogger = iLogger;
         }
 
         @Override
         public void onClick(View v) {
             if (mILogger.LogOut(UserName)) {
                 finish();
             }
 
         }
 
     }
 
     private class GetPackagesAsync extends AsyncTask<String, Void, Integer> {
         DatabasePackageProvider service;
         List<Package> packageList;
         LoadingDialog loadingDialog;
         Context context;
         String transportId;
         private GetPackagesAsync(Context context, DatabasePackageProvider service, String transportId) {
             this.service = service;
             loadingDialog = new LoadingDialog(context);
             this.context = context;
             packageList = new ArrayList<Package>();
             this.transportId = transportId;
         }
 
         @Override
         protected Integer doInBackground(String... strings) {
             packageList = service.getPackages(transportId);
             return null;
         }
 
         @Override
         protected void onPreExecute() {
             loadingDialog.show();
             super.onPreExecute();
         }
 
         @Override
         protected void onPostExecute(Integer integer) {
             loadingDialog.dismiss();
             packagesListAdapter = new PackagesListAdapter(context, packageList);
             packagesList.setAdapter(packagesListAdapter);
 
             packagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     Package pack = packageList.get(i);
                     if(pack.deliveryDate.equals("Unknown"))
                     {
                         Intent intent = new Intent(context, PackageDetailsActivity.class);
                         intent.putExtra(Package.PACKAGE_KEY,pack);
                         startActivity(intent);
                     }
                 }
             });
             super.onPostExecute(integer);
         }
     }
     private void setAdminModeText()
     {
         AdminMode.getInstance().setTextColor(getContext(),userNameText,true);
     }
     public Context getContext()
     {
         return this;
     }
 
 
 }
