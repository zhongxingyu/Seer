 package com.novel.reader;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
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
 import com.kosbrother.fragments.CategoryNewNovelsFragment;
 import com.kosbrother.fragments.CategoryRecommendFragment;
 import com.kosbrother.fragments.CategoryWeekFragment;
 import com.kosbrother.fragments.CategroyHotNovelsFragment;
 import com.kosbrother.tool.Report;
 import com.novel.reader.util.Setting;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class CategoryActivity extends SherlockFragmentActivity {
 
     private static final int    ID_SETTING  = 0;
     private static final int    ID_RESPONSE = 1;
     private static final int    ID_ABOUT_US = 2;
     private static final int    ID_GRADE    = 3;
     private static final int    ID_DOWNLOAD = 4;
     private static final int    ID_SEARCH   = 5;
     private static final int    ID_Report   = 6;
 
     private String[]            CONTENT;
     private EditText            search;
     private Bundle              mBundle;
     private String              categoryName;
     public static int           categoryId;
     private MenuItem            itemSearch;
 
     private AlertDialog.Builder aboutUsDialog;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Setting.setApplicationActionBarTheme(this);
         setContentView(R.layout.simple_titles);
 
         final ActionBar ab = getSupportActionBar();
         mBundle = this.getIntent().getExtras();
         categoryName = mBundle.getString("CategoryName");
         categoryId = mBundle.getInt("CategoryId");
 
         ab.setTitle(categoryName);
         ab.setDisplayHomeAsUpEnabled(true);
 
         Resources res = getResources();
         CONTENT = res.getStringArray(R.array.category_sections);
 
         FragmentPagerAdapter adapter = new NovelPagerAdapter(getSupportFragmentManager());
 
         ViewPager pager = (ViewPager) findViewById(R.id.pager);
         pager.setAdapter(adapter);
 
         TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
         indicator.setViewPager(pager);
 
         setAboutUsDialog();
         AdViewUtil.setBannerAdView((LinearLayout) findViewById(R.id.adonView), this);
         
 
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
                                     intent.setClass(CategoryActivity.this, SearchActivity.class);
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
         case android.R.id.home:
             finish();
             // Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
             break;
         case ID_SETTING: // setting
             Intent intent = new Intent(CategoryActivity.this, SettingActivity.class);
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
                 kk = CategroyHotNovelsFragment.newInstance();
             } else if (position == 1) {
                 kk = CategoryRecommendFragment.newInstance();
             } else if (position == 2) {
                 kk = CategoryWeekFragment.newInstance();
             } else if (position == 3) {
                 kk = CategoryNewNovelsFragment.newInstance();
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
     protected void onResume() {
         super.onResume();
 
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
 
 }
