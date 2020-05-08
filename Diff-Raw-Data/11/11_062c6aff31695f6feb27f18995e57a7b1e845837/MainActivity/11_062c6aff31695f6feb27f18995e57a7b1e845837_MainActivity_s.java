 package com.novel.reader;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 import com.kosbrother.fragments.CategoryListFragment;
 import com.kosbrother.fragments.IndexNovelFragment;
 import com.kosbrother.fragments.MyNovelFragment;
 import com.kosbrother.tool.Report;
 import com.novel.db.SQLiteNovel;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.util.Setting;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class MainActivity extends SherlockFragmentActivity{
 
     private static final int    ID_SETTING  = 0;
     private static final int    ID_RESPONSE = 1;
     private static final int    ID_ABOUT_US = 2;
     private static final int    ID_GRADE    = 3;
     private static final int    ID_SEARCH   = 5;
     private static final int    ID_Report   = 6;
 
     private String[]            CONTENT;
     private EditText            search;
     private MenuItem            itemSearch;
     private ViewPager           pager;
     private AlertDialog.Builder aboutUsDialog;
 
 
     //gcm
     public static final String EXTRA_MESSAGE = "message";
     
     
     /**
      * Substitute you own sender ID here.
      */
     String SENDER_ID = "1037018589447";
 	private Context context;
 	String regid;
 	GoogleCloudMessaging gcm;
 	private String local;
 
     
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Setting.setApplicationActionBarTheme(this);
         setContentView(R.layout.simple_titles);
 
         Resources res = getResources();
         CONTENT = res.getStringArray(R.array.sections);
         
         setTextLocale();
         
         FragmentPagerAdapter adapter = new NovelPagerAdapter(getSupportFragmentManager());
 
         pager = (ViewPager) findViewById(R.id.pager);
         pager.setAdapter(adapter);
 
         TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
         indicator.setViewPager(pager);
 
         pager.setCurrentItem(1);
 
         setAboutUsDialog();
         
         if(Setting.getSetting(Setting.keyUpdateAppVersion,this) < Setting.getAppVersion(this)){
         	showUpdateInfoDialog(this);
         	Setting.saveSetting(Setting.keyUpdateAppVersion, Setting.getAppVersion(this), this);
         }
         
         context = getApplicationContext();
         regid = Setting.getRegistrationId(context);
         String device_id = Setting.getDeviceId(context);
 
         if (regid.length() == 0 || device_id.length() == 0) {
             registerBackground();
         }
         gcm = GoogleCloudMessaging.getInstance(this);
         checkDB();
         
         AdViewUtil.setBannerAdView((LinearLayout) findViewById(R.id.adonView), this);
 
     }
     
    private static void showUpdateInfoDialog(Activity mActivity){
 		LayoutInflater inflater = mActivity.getLayoutInflater();
     	LinearLayout recomendLayout = (LinearLayout) inflater.inflate(R.layout.dialog_update_info,null);

     	
    	Builder a = new AlertDialog.Builder(mActivity).setTitle(mActivity.getResources().getString(R.string.update)).setIcon(R.drawable.icon_report)
         .setPositiveButton(mActivity.getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
             	
             	
             }
         });
     	a.setView(recomendLayout);
     	a.show();
 	}
 
     
     private void setTextLocale() {
     	Locale current = getResources().getConfiguration().locale;
         String country = current.getCountry();
         SharedPreferences sharePreference = getSharedPreferences(Setting.keyPref, 0);
         int text_setting_value = sharePreference.getInt(Setting.keyTextLanguage, 1000);
         if(text_setting_value==1000 && country.toLowerCase().contains("cn"))
         	Setting.saveSetting(Setting.keyTextLanguage, Setting.TEXT_CHINA, this);
 	}
 
 	void checkDB() {
         File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "kosnovel");
         if (!cacheDir.exists())
             cacheDir.mkdirs();
         File sdcardDB = new File(cacheDir, SQLiteNovel.DB_NAME);
         if (!sdcardDB.exists()) {
             ProgressDialog progressdialogInit;
             progressdialogInit = ProgressDialog.show(MainActivity.this, "Load", "Loading…");
             progressdialogInit.setTitle("初始化DB");
             progressdialogInit.setMessage("初始化DB中…(原先下載過小說的用戶，會將資料轉至 SD卡）");
             progressdialogInit.setCanceledOnTouchOutside(false);
             progressdialogInit.setCancelable(false);
             progressdialogInit.show();
             SQLiteNovel db = new SQLiteNovel(MainActivity.this);
             progressdialogInit.dismiss();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         // getMenuInflater().inflate(R.menu.activity_main, menu);
 
         menu.add(0, ID_SETTING, 0, getResources().getString(R.string.menu_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_RESPONSE, 1, getResources().getString(R.string.menu_respond)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_ABOUT_US, 2, getResources().getString(R.string.menu_aboutus)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_GRADE, 3, getResources().getString(R.string.menu_recommend)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_Report, 6, getResources().getString(R.string.menu_report)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
 
         itemSearch = menu.add(0, ID_SEARCH, 4, getResources().getString(R.string.menu_search)).setIcon(R.drawable.ic_search_inverse)
                 .setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                     private EditText search;
 
                     @Override
                     public boolean onMenuItemActionExpand(MenuItem item) {
                         search = (EditText) item.getActionView();
                         search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                         search.setInputType(InputType.TYPE_CLASS_TEXT);
                         search.requestFocus();
                         search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                             @Override
                             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                 if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                                     Bundle bundle = new Bundle();
                                     bundle.putString("SearchKeyword", v.getText().toString());
                                     Intent intent = new Intent();
                                     intent.setClass(MainActivity.this, SearchActivity.class);
                                     intent.putExtras(bundle);
                                     startActivity(intent);
                                     itemSearch.collapseActionView();
                                     return true;
                                 }
                                 return false;
                             }
                         });
                         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                         imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                         return true;
                     }
 
                     @Override
                     public boolean onMenuItemActionCollapse(MenuItem item) {
                         // TODO Auto-generated method stub
                         search.setText("");
                         return true;
                     }
                 }).setActionView(R.layout.collapsible_edittext);
         itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case ID_SETTING: // setting
             Intent intent = new Intent(MainActivity.this, SettingActivity.class);
             startActivity(intent);
             break;
         case ID_RESPONSE: // response
             final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
             emailIntent.setType("plain/text");
             emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { getResources().getString(R.string.respond_mail_address) });
             emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.respond_mail_title));
             emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
             startActivity(Intent.createChooser(emailIntent, "Send mail..."));
             break;
         case ID_ABOUT_US:
             aboutUsDialog.show();
             break;
         case ID_GRADE:
             Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.recommend_url)));
             startActivity(browserIntent);
             break;
         case ID_SEARCH: // response
             break;
         case ID_Report:
         	Report.createReportDialog(this,this.getResources().getString(R.string.report_not_novel_problem),this.getResources().getString(R.string.report_not_article_problem));
             break;
         }
         return true;
     }
 
     class NovelPagerAdapter extends FragmentPagerAdapter {
         public NovelPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             Fragment kk = new Fragment();
             if (position == 0) {
                 kk = CategoryListFragment.newInstance(MainActivity.this);
             } else if (position == 1) {
                 kk = MyNovelFragment.newInstance();
             } else if (position == 2) {
             	kk = IndexNovelFragment.newInstance(IndexNovelFragment.LATEST_NOVEL);
             } else if (position == 3) {
             	kk = IndexNovelFragment.newInstance(IndexNovelFragment.WEEK_NOVEL);
             } else if (position == 4) {
             	kk = IndexNovelFragment.newInstance(IndexNovelFragment.MONTH_NOVEL);
             } else if (position == 5) {
             	kk = IndexNovelFragment.newInstance(IndexNovelFragment.HOT_NOVEL);
             }
             return kk;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return CONTENT[position % CONTENT.length];
         }
 
         @Override
         public int getCount() {
             return CONTENT.length;
         }
     }
 
     @Override
     public void onBackPressed() {
         if (pager.getCurrentItem() == 1) {
             finish();
         } else {
             pager.setCurrentItem(1, true);
         }
     }
 
     private void setAboutUsDialog() {
         // TODO Auto-generated method stub
         aboutUsDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.about_us_string)).setIcon(R.drawable.play_store_icon)
                 .setMessage(getResources().getString(R.string.about_us))
                 .setPositiveButton(getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
 
                     }
                 });
     }
 
     public void rotationHoriztion(int beganDegree, int endDegree, AdView view) {
         final float centerX = 320 / 2.0f;
         final float centerY = 48 / 2.0f;
         final float zDepth = -0.50f * view.getHeight();
 
         Rotate3dAnimation rotation = new Rotate3dAnimation(beganDegree, endDegree, centerX, centerY, zDepth, true);
         rotation.setDuration(1000);
         rotation.setInterpolator(new AccelerateInterpolator());
         rotation.setAnimationListener(new Animation.AnimationListener() {
             public void onAnimationStart(Animation animation) {
             }
 
             public void onAnimationEnd(Animation animation) {
             }
 
             public void onAnimationRepeat(Animation animation) {
             }
         });
         view.startAnimation(rotation);
     }
     
     @Override
     public void onStart() {
       super.onStart();
       EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
       super.onStop();
       EasyTracker.getInstance().activityStop(this);
     }
     
     
     
     private void registerBackground() {
         new AsyncTask() {
 
 			@Override
 			protected String doInBackground(Object... params) {
 				String msg = "";
 	            try {
 	                if (gcm == null) {
 	                    gcm = GoogleCloudMessaging.getInstance(context);
 	                }
 	                regid = gcm.register(SENDER_ID);
 	                msg = "Device registered, registration id=" + regid;
 	                String deviceId = Settings.Secure.getString(MainActivity.this.getContentResolver(),Settings.Secure.ANDROID_ID); 
 	                
 	                boolean isRegistered = NovelAPI.sendRegistrationId(regid,deviceId,Locale.getDefault().getCountry(),getPackageManager().getPackageInfo(getPackageName(), 0).versionCode,"GooglePlay");
 	                
 	                if(isRegistered)
 	                  setRegistrationId(context, regid,deviceId);
 	                
 	            } catch (IOException ex) {
 	                msg = "Error :" + ex.getMessage();
 	            } catch (NameNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	            return msg;
 			}
 
 			private void setRegistrationId(Context context, String regid,String deviceId) {
 				final SharedPreferences prefs = Setting.getGCMPreferences(context);
 				SharedPreferences.Editor editor = prefs.edit();
 				editor.putString(Setting.PROPERTY_REG_ID, regid);
 				editor.putString(Setting.PROPERTY_DEVICE_ID, deviceId);
 				editor.putInt(Setting.PROPERTY_APP_VERSION, Setting.getAppVersion(context));
 				editor.putLong(Setting.PROPERTY_ON_SERVER_EXPIRATION_TIME, Setting.REGISTRATION_EXPIRY_TIME_MS + System.currentTimeMillis());
 				editor.commit();
 				
 			}
             
         }.execute(null, null, null);
     }
 
 }
