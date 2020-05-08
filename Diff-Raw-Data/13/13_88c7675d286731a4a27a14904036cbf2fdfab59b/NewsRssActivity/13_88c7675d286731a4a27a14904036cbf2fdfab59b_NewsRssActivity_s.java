 package com.newsrss.Feed;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockActivity;
 import android.view.animation.TranslateAnimation;
 import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
 import com.fortysevendeg.android.swipelistview.SwipeListView;
 import com.slidingmenu.lib.SlidingMenu;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class NewsRssActivity extends SherlockActivity {
 
     // idLayout:
     // 1 - Articles
     // 2 - Podcasts
     // 3 - Jobs
     // 4 - Favorites
     // 5 - Search
     // 6 - Contacts
     // 7 - Settings
     int idLayout;
     SlidingMenu slidingMenu;
     private SwipeListView rssListView;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.main);
 
         View app = findViewById(R.id.app);
 
         app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
 
 
         rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
         DataStorage.updateArticleList();
         showAricleList();
 
 
 
 
         slidingMenu = new SlidingMenu(this);
         DisplayMetrics metrics = this.getResources().getDisplayMetrics();
         int otstup =(int) (metrics.widthPixels *0.15) ;
         slidingMenu.setMode(SlidingMenu.LEFT);
         slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
         slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
         slidingMenu.setBehindOffset(otstup);
         slidingMenu.setMenu(R.layout.menu);
 
         miniSwipeActivator();
 
     }
 
 
 
     public void miniSwipeActivator(){
         if (idLayout == 1){
 
             DisplayMetrics metrics = this.getResources().getDisplayMetrics();
             float otstup = (float) (metrics.widthPixels *0.8) ;
             rssListView.setSwipeMode(SwipeListView.SWIPE_MODE_RIGHT);
             rssListView.setOffsetRight(otstup);
             rssListView.setAnimationTime(500);
             rssListView.setSwipeOpenOnLongPress(false);
         }  else {
             rssListView.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);
             rssListView.setSwipeOpenOnLongPress(false);
         }
 
     }
 
     // Methods for reload ListView
     public void showAricleList() {
         SwipeListView rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
         MyCAdapter adapter = new MyCAdapter(this,
                 createArticleList(), R.layout.podcast_item_layout,
                 new String[] { "rssnewstitle", "rssnewsdate"},
                 new int [] { R.id.rss_podcast_title, R.id.rss_podcast_date});
         idLayout = 1;
         adapter.setViewBinder(new CustomViewBinder());
         rssListView.setAdapter(adapter);
     }
 
     private List<Map<String, ?>> createArticleList() {
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
         try
         {
             SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
             ArrayList<Article> articleList = DataStorage.getArticleList();
 
             for (Article article : articleList)
             {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("rssnewstitle", article.getTitle());
                 map.put("rssnewsdate", sdf.format(article.getPubDate()));
                 if (article.getNewsImage() == null) {
                     //System.out.println("NULL");
                     map.put("rssnewsimage", getResources().getDrawable(R.drawable.default_news_icon));
                 }
                 else {
                     //System.out.println("NOT NULL");
                     map.put("rssnewsimage", article.getNewsImage());
                 }
                 items.add(map);
             }
         }
         catch(Exception e) {
             e.printStackTrace();
         }
 
         return items;
     }
 
     public class MyCAdapter extends SimpleAdapter {
         private List<? extends Map<String, ?>> data;
         private Context context;
 
 
 
 
 
 
 
         public MyCAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
             super(context, data, resource, from, to);
             this.context = context;
             this.data = data;
         }
 
         @Override
         public int getCount() {
             return data.size();
         }
 
         @Override
         public Object getItem(int position) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(final int position, View convertView, ViewGroup parent) {
             SimpleDateFormat sdf;
             String dateArticleV ;
             ViewHolder holder;
             switch (idLayout)  {
                 case 1:
                     miniSwipeActivator();
                Article currentArticle = DataStorage.getArticleList().get(position);
 
                 if (convertView == null) {
                     LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     convertView = li.inflate(R.layout.rss_item_layout, parent, false);
                     holder = new ViewHolder();
                     holder.favBtn = (ImageButton) convertView.findViewById(R.id.fav_btn);
                     holder.shareBtn = (ImageButton)convertView.findViewById(R.id.share_btn);
                     holder.articleTitle = (TextView)convertView.findViewById(R.id.rss_news_title);
                     holder.articleDate = (TextView)convertView.findViewById(R.id.rss_news_date);
 
 
                     convertView.setTag(holder);
                 } else {
                     holder = (ViewHolder) convertView.getTag();
                 }
                 holder.articleTitle.setText(currentArticle.getTitle());
                 sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 dateArticleV = sdf.format(currentArticle.getPubDate());
                 holder.articleDate.setText(dateArticleV);
 
                 rssListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                     @Override
                     public void  onClickFrontView (int position){
                         Intent startDetailArticl = new Intent(NewsRssActivity.this, DetailsArticle.class);
                         startDetailArticl.putExtra("position", position);
                         startActivity(startDetailArticl);
                     }
 
 
                 });
 
             holder.favBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Toast toast = Toast.makeText(getApplicationContext(),"Add to Favorit article N " +position ,Toast.LENGTH_SHORT);
                     toast.show();
                 }
             });
 
             holder.shareBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Toast toast = Toast.makeText(getApplicationContext(),"Share article N  " +position ,Toast.LENGTH_SHORT);
                     toast.show();
                 }
             });
 
             break;
 
             case 2:
                 miniSwipeActivator();
 
                 Podcast currentPodcast = DataStorage.getPodcastList().get(position);
 
                 if (convertView == null) {
                     LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     convertView = li.inflate(R.layout.rss_item_layout, parent, false);
                     holder = new ViewHolder();
 
 
                     holder.articleTitle = (TextView)convertView.findViewById(R.id.rss_news_title);
                     holder.articleDate = (TextView)convertView.findViewById(R.id.rss_news_date);
 
                     convertView.setTag(holder);
                 } else {
                     holder = (ViewHolder) convertView.getTag();
                 }
                 holder.articleTitle.setText(currentPodcast.getTitle());
                 sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 dateArticleV = sdf.format(currentPodcast.getPubDate());
                 holder.articleDate.setText(dateArticleV);
                 rssListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                     @Override
                     public void  onClickFrontView (int position){
                         Intent startDetailPodcast = new Intent(NewsRssActivity.this,DetailsPodcast.class );
                         startDetailPodcast.putExtra("position", position);
                         startActivity(startDetailPodcast);
                     }
 
 
                 });
 
                 break;
                 case 3:
                     miniSwipeActivator();
 
                     Job currentJob = DataStorage.getJobList().get(position);
 
                     if (convertView == null) {
                         LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                         convertView = li.inflate(R.layout.rss_item_layout, parent, false);
                         holder = new ViewHolder();
 
 
                         holder.articleTitle = (TextView)convertView.findViewById(R.id.rss_news_title);
                         holder.articleDate = (TextView)convertView.findViewById(R.id.rss_news_date);
 
                         convertView.setTag(holder);
                     } else {
                         holder = (ViewHolder) convertView.getTag();
                     }
                     holder.articleTitle.setText(currentJob.getTitle());
                     sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                     dateArticleV = sdf.format(currentJob.getPubDate());
                     holder.articleDate.setText(dateArticleV);
                     rssListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                         @Override
                         public void  onClickFrontView (int position){
                             Intent startDetailJobs = new Intent(NewsRssActivity.this, DetailsJobs.class );
                             startDetailJobs.putExtra("position", position);
                             startActivity(startDetailJobs);
                         }
 
 
                     });
 
                     break;
         }
 
 
 
             return convertView;
         }
 
         public class ViewHolder {
             ImageButton favBtn;
             ImageButton shareBtn;
             TextView articleTitle;
             TextView articleDate;
 
         }
     }
 
     public void showPodcastList(){
         SwipeListView rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
         if(DataStorage.getPodcastList().size() == 0 ){
             DataStorage.updatePodcastList();
         }
 
         MyCAdapter adapter = new MyCAdapter(
                 this, createPodcastList(), R.layout.rss_item_layout,
                 new String[] { "rssnewstitle", "rssnewsdate"},
                 new int [] { R.id.rss_news_title, R.id.rss_news_date});
         idLayout = 2;
         rssListView.setAdapter(adapter);
 
     }
 
     private List<Map<String, ?>> createPodcastList()   {
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
         String dateArticleV;
         try {
             SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
             ArrayList<Podcast> podcastList = DataStorage.getPodcastList();
 
             for(Podcast podcast : podcastList) {
                 dateArticleV = sdf.format(podcast.getPubDate());
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("rssnewstitle", podcast.getTitle());
                 map.put("rssnewsdate", dateArticleV);
                 items.add(map);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
         return  items;
     }
 
     public void showJobstList(){
         SwipeListView rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
         if(DataStorage.getJobList().size() == 0 ){
             DataStorage.updateJobList();
         }
 
         MyCAdapter adapter = new MyCAdapter(
                 this, createJobsList(), R.layout.rss_item_layout,
                 new String[] { "rssnewstitle", "rssnewsdate"},
                 new int [] { R.id.rss_news_title, R.id.rss_news_date});
         idLayout = 3;
         rssListView.setAdapter(adapter);
     }
 
     private List<Map<String, ?>> createJobsList()   {
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
         String dateArticleV;
         try {
             SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
             ArrayList<Job> jobsList = DataStorage.getJobList();
 
             for(Job job : jobsList) {
                 dateArticleV = sdf.format(job.getPubDate());
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("rssnewstitle", job.getTitle());
                 map.put("rssnewsdate", dateArticleV);
                 items.add(map);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
         return  items;
     }
 
     // SideBar Elements Click
     public void startSearchActivityFromSideBar(final View view){
           //TODO: Утановите вызов Активити для поиска
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Запустить Поиск",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void showArticleListFromSideBar(final View view){
         //Toast toast = Toast.makeText(getApplicationContext(),"Показать все статьи",Toast.LENGTH_SHORT);
         //toast.show();
 
         if (idLayout != 1 & idLayout != 2 & idLayout != 3) {
             slidingMenu.setContent(R.layout.main);
             showAricleList();
             ListView rssListView = (ListView) findViewById(R.id.rssListView);
 
             View app = findViewById(R.id.app);
             app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
         }
         else {
             showAricleList();
             slidingMenu.showContent();
         }
     }
 
     public void filterToAudit(final View view){
         if (idLayout != 1)
             return;
 
         Drawable image = null;
         if (DataStorage.changeFilterStatus(XMLNewsType.AuditNAccounting)) {
             // category on
             image = getResources().getDrawable(R.drawable.check);
         }
         else {
             //category off
             image = getResources().getDrawable(R.drawable.uncheck);
         }
         ImageView imageView = (ImageView) findViewById(R.id.audit_check_img_view);
         imageView.setImageDrawable(image);
         showAricleList();
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать фильтер для Аудита",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void filterToBusiness(final View view){
         if (idLayout != 1)
             return;
 
         Drawable image = null;
         if (DataStorage.changeFilterStatus(XMLNewsType.Business)) {
             // category on
             image = getResources().getDrawable(R.drawable.check);
         }
         else {
             //category off
             image = getResources().getDrawable(R.drawable.uncheck);
         }
         ImageView imageView = (ImageView) findViewById(R.id.business_check_img_view);
         imageView.setImageDrawable(image);
         showAricleList();
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать фильтер для Бизнеса",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void filterToGovernance(final View view){
         if (idLayout != 1)
             return;
 
         Drawable image = null;
         if (DataStorage.changeFilterStatus(XMLNewsType.Governance)) {
             // category on
             image = getResources().getDrawable(R.drawable.check);
         }
         else {
             //category off
             image = getResources().getDrawable(R.drawable.uncheck);
         }
         ImageView imageView = (ImageView) findViewById(R.id.governance_check_img_view);
         imageView.setImageDrawable(image);
         showAricleList();
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать фильтер для Правительства",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void filterToInsolvency(final View view){
         if (idLayout != 1)
             return;
 
         Drawable image = null;
         if (DataStorage.changeFilterStatus(XMLNewsType.Insolvency)) {
             // category on
             image = getResources().getDrawable(R.drawable.check);
         }
         else {
             //category off
             image = getResources().getDrawable(R.drawable.uncheck);
         }
         ImageView imageView = (ImageView) findViewById(R.id.insolvency_check_img_view);
         imageView.setImageDrawable(image);
         showAricleList();
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать фильтер для Insolvency",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void filterToPractice(final View view){
         if (idLayout != 1)
             return;
 
         Drawable image = null;
         if (DataStorage.changeFilterStatus(XMLNewsType.Practice)) {
             // category on
             image = getResources().getDrawable(R.drawable.check);
         }
         else {
             //category off
             image = getResources().getDrawable(R.drawable.uncheck);
         }
         ImageView imageView = (ImageView) findViewById(R.id.practice_check_img_view);
         imageView.setImageDrawable(image);
         showAricleList();
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать фильтер для Practice",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void filterToTax(final View view){
         if (idLayout != 1)
             return;
 
         Drawable image = null;
         if (DataStorage.changeFilterStatus(XMLNewsType.Tax)) {
             // category on
             image = getResources().getDrawable(R.drawable.check);
         }
         else {
             //category off
             image = getResources().getDrawable(R.drawable.uncheck);
         }
         ImageView imageView = (ImageView) findViewById(R.id.tax_check_img_view);
         imageView.setImageDrawable(image);
         showAricleList();
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать фильтер для Tax",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void showToFavoritesFromSideBar(final View view){
         //TODO: Утановите вызов Favorites
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать Favorites",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void showSavedSearchFromSideBar(final View view){
         //TODO: Установите вызов showSavedSearchFromSideBar
 
         //Toast toast = Toast.makeText(getApplicationContext(),"Использовать SavedSearch",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void showPodcastsListFromSideBar(final View view){
         //Toast toast = Toast.makeText(getApplicationContext(),"Показать все Подкасты",Toast.LENGTH_SHORT);
         //toast.show();
 
         if (idLayout != 1 & idLayout != 2 & idLayout != 3) {
             slidingMenu.setContent(R.layout.main);
             showPodcastList();
             ListView rssListView = (ListView) findViewById(R.id.rssListView);
 
             View app = findViewById(R.id.app);
             app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
         }
         else {
             showPodcastList();
             slidingMenu.showContent();
         }
 
     }
 
     public void showJobsListFromSideBar(final View view){
         //Toast toast = Toast.makeText(getApplicationContext(),"Показать все Jobs",Toast.LENGTH_SHORT);
         //toast.show();
 
         if (idLayout != 1 & idLayout != 2 & idLayout != 3) {
             slidingMenu.setContent(R.layout.main);
             showJobstList();
             ListView rssListView = (ListView) findViewById(R.id.rssListView);
 
             View app = findViewById(R.id.app);
             app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
         }
         else {
             showJobstList();
             slidingMenu.showContent();
         }
     }
 
     public void showContactFromSideBar(final View view){
         slidingMenu.setContent(R.layout.contacts);
         idLayout = 6;
 
         View sidebarButton  = findViewById(R.id.contactSidebarButton);
         sidebarButton.setOnClickListener(new ClickListener());
 
         TextView tel1Text=(TextView) findViewById(R.id.contacts_Ireland_tel_text);
         tel1Text.setText(Html.fromHtml("<b>Tel:</b> 00353 1 637 7200"));
         TextView tel2Text=(TextView) findViewById(R.id.contacts_NI_tel_text);
         tel2Text.setText(Html.fromHtml("<b>Tel:</b> 00442 8 904 3584"));
 
         ImageView cal_btnIR = (ImageView) findViewById(R.id.contacts_callIR);
         ImageView cal_btnNI= (ImageView) findViewById(R.id.contacts_callNI);
 
         ImageView.OnClickListener ocCall=new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent callIntent = new Intent(Intent.ACTION_CALL);
                 callIntent.setData(Uri.parse("tel:0035316377200"));
                 startActivity(callIntent);
             }
         };
 
         ImageView.OnClickListener ocCallNI=new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent callIntent = new Intent(Intent.ACTION_CALL);
                 callIntent.setData(Uri.parse("tel:0044289043584"));
                 startActivity(callIntent);
             }
         };
 
 
         cal_btnIR.setOnClickListener(ocCall);
         cal_btnNI.setOnClickListener(ocCallNI);
 
 
     }
 
     public void showSettingsFromSideBar(final View view){
         slidingMenu.setContent(R.layout.details_settings);
         idLayout = 7;
         View sidebarButton  = findViewById(R.id.settingSidebarButton);
         sidebarButton.setOnClickListener(new ClickListener());
         ImageView legalView = (ImageView)findViewById(R.id.settings_legalBG);
         ImageView rateView = (ImageView)findViewById(R.id.settings_rateBG);
         ImageView feedbackView = (ImageView)findViewById(R.id.settings_feedbackBG);
 
         ImageView.OnClickListener ocLegal = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent startLegalActivity = new Intent(NewsRssActivity.this, Legal.class);
                 startActivity(startLegalActivity);
             }
         };
 
         ImageView.OnClickListener ocRate = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //TODO Rate this app click
             }
         };
         ImageView.OnClickListener ocFeedback = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //TODO Send us feedback click
             }
         };
 
         legalView.setOnClickListener(ocLegal);
         rateView.setOnClickListener(ocRate);
         feedbackView.setOnClickListener(ocFeedback);
 
     }
 
     // Main animation for SideBar
     class ClickListener implements View.OnClickListener {
         @Override
         public void onClick(View v) {
             //Toast toast = Toast.makeText(getApplicationContext(),"Показать все статьи",Toast.LENGTH_SHORT);
             //toast.show();
 
             slidingMenu.showMenu();
 
         }
     }
 
     //
     class CustomViewBinder implements SimpleAdapter.ViewBinder {
         @Override
         public boolean setViewValue(View view, Object data,String textRepresentation) {
             if((view instanceof ImageView) & (data instanceof Drawable))
             {
                 ImageView iv = (ImageView) view;
                 Drawable image = (Drawable) data;
                 iv.setImageDrawable(image);
                 return true;
             }
             return false;
         }
 
     }
 
 }
 
