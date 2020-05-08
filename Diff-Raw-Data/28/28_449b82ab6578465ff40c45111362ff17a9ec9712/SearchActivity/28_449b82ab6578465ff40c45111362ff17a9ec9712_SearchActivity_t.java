 package com.novel.reader;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.entity.Novel;
 import com.novel.reader.util.Setting;
 import com.taiwan.imageload.ImageLoader;
 
 public class SearchActivity extends SherlockListActivity {
 
     private static final int    ID_SETTING  = 0;
     private static final int    ID_RESPONSE = 1;
     private static final int    ID_ABOUT_US = 2;
     private static final int    ID_GRADE    = 3;
     private static final int    ID_SEARCH   = 5;
 
     private Bundle              mBundle;
     private String              keyword;
     private ArrayList<Novel>    novels;
     private ListView            novelListView;
     private MenuItem            item;
 
     private LinearLayout        layoutNoSearch;
     private AlertDialog.Builder aboutUsDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Setting.setApplicationActionBarTheme(this);
         setContentView(R.layout.layout_search);
         layoutNoSearch = (LinearLayout) findViewById(R.id.layout_no_search);
 
         final ActionBar ab = getSupportActionBar();
         mBundle = this.getIntent().getExtras();
         keyword = mBundle.getString("SearchKeyword");
         novelListView = this.getListView();
 
         novelListView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Novel movie = novels.get(position);
                 Intent newAct = new Intent();
                 Bundle bundle = new Bundle();
                 bundle.putInt("NovelId", movie.getId());
                 bundle.putString("NovelName", movie.getName());
                 bundle.putString("NovelAuthor", movie.getAuthor());
                 bundle.putString("NovelDescription", movie.getDescription());
                 bundle.putString("NovelUpdate", movie.getLastUpdate());
                 bundle.putString("NovelPicUrl", movie.getPic());
                 bundle.putString("NovelArticleNum", movie.getArticleNum());
                 newAct.putExtras(bundle);
                 newAct.setClass(SearchActivity.this, NovelIntroduceActivity.class);
                 startActivity(newAct);
             }
 
         });
 
         ab.setDisplayHomeAsUpEnabled(true);
 
         setAboutUsDialog();
         new LoadDataTask().execute();
         AdViewUtil.setBannerAdView((LinearLayout) findViewById(R.id.adonView), this);
 
     }
 
     private void fetchData() {
         novels = NovelAPI.searchNovels(keyword);
     }
 
     public class SearchAdapter extends BaseAdapter {
 
         private final Context          mContext;
         private final ArrayList<Novel> novels;
         private final ImageLoader      imageLoader;
 
         public SearchAdapter(Context mContext, ArrayList<Novel> novels) {
             this.novels = novels;
             this.mContext = mContext;
             imageLoader = new ImageLoader(mContext);
         }
 
         @Override
         public int getCount() {
             return novels.size();
         }
 
         @Override
         public Object getItem(int position) {
             return position;
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             LayoutInflater myInflater = LayoutInflater.from(mContext);
             View converView = myInflater.inflate(R.layout.item_novel_search, null);
             ImageView pic = (ImageView) converView.findViewById(R.id.grid_item_image);
             TextView name = (TextView) converView.findViewById(R.id.grid_item_name);
             TextView author = (TextView) converView.findViewById(R.id.grid_item_author);
             TextView articleNum = (TextView) converView.findViewById(R.id.grid_item_counts);
             TextView textFinish = (TextView) converView.findViewById(R.id.grid_item_finish);
             TextView textSerialize = (TextView) converView.findViewById(R.id.serializing);
 
             imageLoader.DisplayImage(novels.get(position).getPic(), pic);
             name.setText(novels.get(position).getName());
             author.setText(novels.get(position).getAuthor());
             articleNum.setText(novels.get(position).getArticleNum());
             textFinish.setText(novels.get(position).getLastUpdate());
 
             if (novels.get(position).isSerializing()) {
                 textSerialize.setText("連載中...");
             } else {
                 textSerialize.setText("全本");
             }
 
             return converView;
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
 
         item = menu.add(0, ID_SEARCH, 4, getResources().getString(R.string.menu_search)).setIcon(R.drawable.ic_search_inverse)
                 .setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                     private EditText search;
 
                     @Override
                     public boolean onMenuItemActionExpand(MenuItem item) {
                         search = (EditText) item.getActionView();
                         search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                         search.requestFocus();
                         search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                             @Override
                             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                                     keyword = v.getText().toString();
                                     new LoadDataTask().execute();
 
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
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 
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
             Intent intent = new Intent(SearchActivity.this, SettingActivity.class);
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
         }
         return true;
     }
 
     class LoadDataTask extends AsyncTask<Integer, Integer, String> {
 
         private ProgressDialog         progressdialogInit;
         private final OnCancelListener cancelListener = new OnCancelListener() {
                                                           public void onCancel(DialogInterface arg0) {
                                                               LoadDataTask.this.cancel(true);
                                                               finish();
                                                           }
                                                       };
 
         @Override
         protected void onPreExecute() {
             progressdialogInit = ProgressDialog.show(SearchActivity.this, "Load", "Loading…");
             progressdialogInit.setTitle("Load");
             progressdialogInit.setMessage("Loading…");
             progressdialogInit.setOnCancelListener(cancelListener);
             progressdialogInit.setCanceledOnTouchOutside(false);
             progressdialogInit.setCancelable(true);
             progressdialogInit.show();
             super.onPreExecute();
         }
 
         @Override
         protected String doInBackground(Integer... params) {
             fetchData();
             return "progress end";
         }
 
         @Override
         protected void onProgressUpdate(Integer... progress) {
             super.onProgressUpdate(progress);
         }
 
         @Override
         protected void onPostExecute(String result) {
             progressdialogInit.dismiss();
             if (novels != null && novels.size() != 0) {
                 novelListView.setAdapter(new SearchAdapter(SearchActivity.this, novels));
                 layoutNoSearch.setVisibility(View.GONE);
             } else {
                 layoutNoSearch.setVisibility(View.VISIBLE);
             }
             try {
                 item.expandActionView();
                 EditText search = (EditText) item.getActionView();
                 search.setText(keyword);
             } catch (Exception e) {
 
             }
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
 
 }
