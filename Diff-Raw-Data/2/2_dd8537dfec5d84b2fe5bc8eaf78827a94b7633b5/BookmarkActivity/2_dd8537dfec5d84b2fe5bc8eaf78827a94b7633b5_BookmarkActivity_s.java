 package com.novel.reader;
 
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.adwhirl.AdWhirlLayout;
 import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
 import com.adwhirl.AdWhirlManager;
 import com.adwhirl.AdWhirlTargeting;
 import com.google.ads.AdView;
 import com.ifixit.android.sectionheaders.Section;
 import com.ifixit.android.sectionheaders.SectionHeadersAdapter;
 import com.ifixit.android.sectionheaders.SectionListView;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.api.Setting;
 import com.novel.reader.entity.Bookmark;
 import com.taiwan.imageload.ImageLoader;
 
 public class BookmarkActivity extends SherlockActivity implements AdWhirlInterface {
 
     private SectionListView                      bookmarkListView;
     private ArrayList<Bookmark>                  bookmarks;
     private TreeMap<String, ArrayList<Bookmark>> bookmarksMap;
     private Bundle                               mBundle;
     private boolean                              isRecent;
     private boolean                              alertDeleteBookmark;
     SharedPreferences                            settings;
     private final String                         alertKey   = "alertDeleteBookmark";
     private final String                         adWhirlKey = "215f895eb71748e7ba4cb3a5f20b061e";
     private ArrayList<String>                    arrayKey;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_bookmark);
         // Bookmark b = new Bookmark(0, 1, 1, 45, "novel1", "title1", "", false);
         // Bookmark b1 = new Bookmark(0, 1, 2, 45, "novel1", "title2", "", false);
         // Bookmark b2 = new Bookmark(0, 2, 3, 45, "novel2", "title3", "", false);
         // NovelAPI.insertBookmark(b, getApplicationContext());
         // NovelAPI.insertBookmark(b1, getApplicationContext());
         // NovelAPI.insertBookmark(b2, getApplicationContext());
 
         mBundle = this.getIntent().getExtras();
         isRecent = mBundle.getBoolean("IS_RECNET");
 
         final ActionBar ab = getSupportActionBar();
         bookmarkListView = (SectionListView) findViewById(R.id.bookmark_listview);
 
         if (isRecent) {
             ab.setTitle(getResources().getString(R.string.my_recent_reading));
         } else {
             ab.setTitle(getResources().getString(R.string.my_bookmark));
         }
 
         ab.setDisplayHomeAsUpEnabled(true);
 
         settings = getSharedPreferences(Setting.keyPref, 0);
         alertDeleteBookmark = settings.getBoolean(alertKey, true);
 
         try {
             Display display = getWindowManager().getDefaultDisplay();
             int width = display.getWidth(); // deprecated
             int height = display.getHeight(); // deprecated
 
             if (width > 320) {
                 setAdAdwhirl();
             }
         } catch (Exception e) {
 
         }
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         new LoadDataTask().execute();
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case android.R.id.home:
             finish();
             // Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
             break;
         }
         return true;
     }
 
     private void setListAdatper() {
         SectionHeadersAdapter adapter = new SectionHeadersAdapter();
 
         for (int i = 0; i < arrayKey.size(); i++) {
             adapter.addSection(new BookmarkSectionAdapter(this, bookmarksMap.get(arrayKey.get(i)), arrayKey.get(i)));
         }
         bookmarkListView.setAdapter(adapter);
         bookmarkListView.getListView().setOnItemClickListener(adapter);
         bookmarkListView.getListView().setOnItemLongClickListener(adapter);
     }
 
     private void fetchData() {
         if (isRecent)
             bookmarks = NovelAPI.getAllRecentReadBookmarks(BookmarkActivity.this);
         else
             bookmarks = NovelAPI.getAllBookmarks(BookmarkActivity.this);
 
         if (bookmarks.size() == 0)
             bookmarks.add(new Bookmark(0, 0, 0, 0, getResources().getString(R.string.my_bookmark_none), "", "", false));
         bookmarksMap = getBookmarksMap(bookmarks);
     }
 
     private TreeMap<String, ArrayList<Bookmark>> getBookmarksMap(ArrayList<Bookmark> bs) {
         arrayKey = new ArrayList<String>();
         TreeMap bookMap = new TreeMap<String, ArrayList<Bookmark>>();
         for (int i = 0; i < bs.size(); i++) {
             Bookmark bookmark = bs.get(i);
             // 先確認key是否存在
             if (bookMap.containsKey(bookmark.getNovelName())) {
                 // 已經有的話就把movie加進去
                 ((ArrayList<Bookmark>) bookMap.get(bookmark.getNovelName())).add(bookmark);
             } else {
                 // 沒有的話就建一個加進去
                 ArrayList<Bookmark> newBookList = new ArrayList<Bookmark>(10);
                 newBookList.add(bookmark);
                 arrayKey.add(bookmark.getNovelName());
                 bookMap.put(bookmark.getNovelName(), newBookList);
             }
         }
         return bookMap;
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
             progressdialogInit = ProgressDialog.show(BookmarkActivity.this, "Load", "Loading…");
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
             setListAdatper();
             if (alertDeleteBookmark)
                 showArticleDeleteDialog();
         }
 
     }
 
     private void showArticleDeleteDialog() {
         new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.reminder)).setIcon(R.drawable.noti_app_icon)
                 .setMessage(getResources().getString(R.string.delete_bookmark_reminder))
                 .setPositiveButton(getResources().getString(R.string.do_not_reminder), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface arg0, int arg1) {
                         settings.edit().putBoolean(alertKey, false).commit();
 
                     }
 
                 }).setNegativeButton(getResources().getString(R.string.reminder_next), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface arg0, int arg1) {
 
                     }
 
                 }).show();
 
     }
 
     public class BookmarkSectionAdapter extends Section implements Filterable {
 
         private final Context             mContext;
         private final ArrayList<Bookmark> bookList;
         private final ImageLoader         imageLoader;
         private final String              headerString;
 
         public BookmarkSectionAdapter(Context context, ArrayList<Bookmark> bookList, String headerString) {
             this.mContext = context;
             this.bookList = bookList;
             this.headerString = headerString;
             imageLoader = new ImageLoader(mContext);
         }
 
         public int getCount() {
             return bookList.size();
         }
 
         public Object getItem(int position) {
             return bookList.get(position);
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(final int position, View convertView, ViewGroup parent) {
             LayoutInflater myInflater = LayoutInflater.from(mContext);
             View converView = myInflater.inflate(R.layout.listview_bookmarks, null);
 
             ImageView poster = (ImageView) converView.findViewById(R.id.bookmark_poster);
             TextView novelName = (TextView) converView.findViewById(R.id.bookmark_novel_name);
             TextView articleTitle = (TextView) converView.findViewById(R.id.bookmark_article_name);
             Button novelBtn = (Button) converView.findViewById(R.id.novel_introduce_btn);
             novelBtn.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Bundle bundle = new Bundle();
                     bundle.putInt("NovelId", bookList.get(position).getNovelId());
                     bundle.putString("NovelName", bookList.get(position).getNovelName());
                     bundle.putString("NovelAuthor", "");
                     bundle.putString("NovelDescription", "");
                     bundle.putString("NovelUpdate", "");
                     bundle.putString("NovelPicUrl", bookList.get(position).getNovelPic());
                     bundle.putString("NovelArticleNum", "");
                     Intent intent = new Intent();
                     intent.putExtras(bundle);
                     intent.setClass(mContext, NovelIntroduceActivity.class);
                     startActivity(intent);
                 }
             });
             novelName.setText(bookList.get(position).getNovelName());
             articleTitle.setText(bookList.get(position).getArticleTitle());
 
             imageLoader.DisplayImage(bookList.get(position).getNovelPic(), poster);
 
             return converView;
         }
 
         @Override
         public Object getHeaderItem() {
             return headerString;
         }
 
         @Override
         public View getHeaderView(View convertView, ViewGroup parent) {
             TextView header = (TextView) convertView;
 
             if (header == null) {
                 header = new TextView(mContext);
                 header.setTextSize(16);
                 header.setPadding(12, 1, 2, 1);
                 header.setTypeface(Typeface.SERIF);
                 header.setBackgroundColor(mContext.getResources().getColor(R.color.bookmark_section_head));
                 header.setTextColor(mContext.getResources().getColor(R.color.bookmark_section_text));
             }
             header.setText(headerString);
 
             return header;
         }
 
         public Filter getFilter() {
             return null;
         }
 
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
             Bookmark bookmark = bookList.get(position);
             if (bookmark.getId() == 0)
                 return;
             Intent newAct = new Intent();
             newAct.putExtra("NovelName", bookmark.getNovelName());
             newAct.putExtra("ArticleTitle", bookmark.getArticleTitle());
             newAct.putExtra("ArticleId", bookmark.getArticleId());
             newAct.putExtra("ReadingRate", bookmark.getReadRate());
             newAct.putExtra("NovelPic", bookmark.getNovelPic());
             newAct.putExtra("NovelId", bookmark.getNovelId());
             newAct.setClass(mContext, ArticleActivity.class);
             startActivity(newAct);
         }
 
         @Override
         public void onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
             final Bookmark bookmark = bookList.get(position);
             final String[] ListStr = { getResources().getString(R.string.reading_novel), getResources().getString(R.string.remove_bookmark) };
             AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
             builder.setTitle(bookmark.getArticleTitle());
             builder.setItems(ListStr, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     if (item == 0 && bookmark.getId() != 0) {
                         Intent newAct = new Intent();
                         newAct.putExtra("NovelName", bookmark.getNovelName());
                         newAct.putExtra("ArticleTitle", bookmark.getArticleTitle());
                         newAct.putExtra("ArticleId", bookmark.getArticleId());
                         newAct.putExtra("ReadingRate", bookmark.getReadRate());
                         newAct.putExtra("NovelPic", bookmark.getNovelPic());
                         newAct.putExtra("NovelId", bookmark.getNovelId());
                         newAct.setClass(mContext, ArticleActivity.class);
                         startActivity(newAct);
                     } else {
                         NovelAPI.deleteBookmark(bookmark, mContext);
                         new LoadDataTask().execute();
                     }
                 }
             });
             AlertDialog alert = builder.create();
             alert.show();
         }
     }
 
     private void setAdAdwhirl() {
 
         AdWhirlManager.setConfigExpireTimeout(1000 * 60);
         AdWhirlTargeting.setAge(23);
         AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
         AdWhirlTargeting.setKeywords("online games gaming");
         AdWhirlTargeting.setPostalCode("94123");
         AdWhirlTargeting.setTestMode(false);
 
         AdWhirlLayout adwhirlLayout = new AdWhirlLayout(this, adWhirlKey);
 
         LinearLayout mainLayout = (LinearLayout) findViewById(R.id.adonView);
 
         adwhirlLayout.setAdWhirlInterface(this);
 
         mainLayout.addView(adwhirlLayout);
 
         mainLayout.invalidate();
     }
 
     @Override
     public void adWhirlGeneric() {
 
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
 
 }
