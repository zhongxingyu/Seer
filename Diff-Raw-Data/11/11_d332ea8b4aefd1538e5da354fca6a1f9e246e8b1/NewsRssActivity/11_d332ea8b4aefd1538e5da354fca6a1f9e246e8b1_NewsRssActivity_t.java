 package com.newsrss.Feed;
 
 import android.app.Dialog;
 import android.content.ClipData;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.content.ClipboardManager;
 import android.widget.*;
 import com.facebook.Session;
 import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
 import com.fortysevendeg.android.swipelistview.SwipeListView;
 import com.slidingmenu.lib.SlidingMenu;
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class NewsRssActivity extends shaerToSocial {
     SwipeListView savedSearchListView;
 
     List<Searches> artList = null;
     String blabla;
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
     private static Context context = getAppContext();
     private SwipeListView rssListView;


     @Override
     public void onCreate(Bundle savedInstanceState) {
 
 
         super.onCreate(savedInstanceState);
         setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.main);
 
         View app = findViewById(R.id.app);
 
         app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
 
 
         rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
         LayoutInflater inflater = getLayoutInflater();
         ViewGroup header = (ViewGroup)inflater.inflate(R.layout.rss_header, rssListView, false);
         rssListView.addHeaderView(header, null, false);
 
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
 
         //
         miniSwipeActivator();
 
         //FB
         Session session = Session.getActiveSession();
         if (session == null) {
             if (savedInstanceState != null) {
                 session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
             }
             if (session == null) {
                 session = new Session(this);
             }
             Session.setActiveSession(session);
 
         }
 
     }
 
 
 
     public void miniSwipeActivator(){
         if (idLayout == 1 || idLayout == 4){
 
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
 
         ((TextView)findViewById(R.id.rss_list_header_text)).setText("News");
         if (Build.VERSION.SDK_INT >= 16)
             findViewById(R.id.rss_list_header_image).setBackground(getResources().getDrawable(R.drawable.news_header));
         else
             findViewById(R.id.rss_list_header_image).setBackgroundDrawable(getResources().getDrawable(R.drawable.news_header));
 
         MyCAdapter adapter = new MyCAdapter(this,
                 createArticleList(), R.layout.podcast_item_layout,
                 new String[] { "rssnewstitle", "rssnewsdate","rssnewsimage"},
                 new int [] { R.id.rss_podcast_title, R.id.rss_podcast_date, R.id.rss_img_news_pass});
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
             final ViewHolder holder;

             switch (idLayout)  {
                 case 1:
                     miniSwipeActivator();
                 final Article currentArticle = DataStorage.getArticleList().get(position);
 
                 if (convertView == null) {
                     LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     convertView = li.inflate(R.layout.rss_item_layout, parent, false);
                     holder = new ViewHolder();
                     holder.favBtn = (ImageButton) convertView.findViewById(R.id.fav_btn);
                     holder.shareBtn = (ImageButton)convertView.findViewById(R.id.share_btn);
                     holder.articleTitle = (TextView)convertView.findViewById(R.id.rss_news_title);
                     holder.articleDate = (TextView)convertView.findViewById(R.id.rss_news_date);
                     holder.articleImage = (ImageView) convertView.findViewById(R.id.rss_img_news_pass);
 
                     convertView.setTag(holder);
                 } else {
                     holder = (ViewHolder) convertView.getTag();
                 }
                 holder.articleTitle.setText(currentArticle.getTitle());
                 sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 dateArticleV = sdf.format(currentArticle.getPubDate());
                 holder.articleDate.setText(dateArticleV);
                 if (currentArticle.getNewsImage() == null) {
                     holder.articleImage.setImageDrawable(getResources().getDrawable(R.drawable.default_news_icon));
                 }
                 else {
                     holder.articleImage.setImageDrawable(currentArticle.getNewsImage());
                 }
 
 
                 rssListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                     @Override
                     public void  onClickFrontView (int position){
                         Intent startDetailArticl = new Intent(NewsRssActivity.this, DetailsArticle.class);
                         startDetailArticl.putExtra("position", position-1);
                         startActivity(startDetailArticl);
                     }
 
 
                 });
                     try {
                         LocalDB.open(getApplicationContext());
                     } catch (SQLException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     if (!LocalDB.isArticleFavotites(currentArticle.getGuid())){
                         holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.purple_favorite_vhite));
                     }   else {
                         holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.favorites));
 
                     }
                     LocalDB.close();
 
 
 
                     holder.favBtn.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             try {
                                 LocalDB.open(getApplicationContext());
                             } catch (SQLException e) {
                                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                             }
                             if (!LocalDB.isArticleFavotites(currentArticle.getGuid())){
                                 LocalDB.addArticle(currentArticle);
                                 holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.favorites));
                                 Toast toast = Toast.makeText(getApplicationContext(),"Article Added to Favorites " ,Toast.LENGTH_SHORT);
                                 toast.show();
                             }else{
                                 LocalDB.deleteArticle(currentArticle.getGuid());
                                 holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.purple_favorite_vhite));
                                 Toast toast = Toast.makeText(getApplicationContext(),"Article Remove from Favorites " ,Toast.LENGTH_SHORT);
                                 toast.show();
                             }
                             LocalDB.close();
 
                 }
             });
 
             holder.shareBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     final Dialog dialog = new Dialog(context);
                     dialog.setContentView(R.layout.share_panel_in_list);
                     dialog.setTitle("Share: " +currentArticle.getTitle());
 
                     ImageButton faceBookShare = (ImageButton)dialog.findViewById(R.id.facebook_in_listview);
                     faceBookShare.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             onClickbtnConnectFB(1, position);
 
                         }
                     });
                     ImageButton twitterShare = (ImageButton)dialog.findViewById(R.id.twitter_in_listview);
                     twitterShare.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             onTwitterClick(currentArticle.getTitle()+" "+ currentArticle.getLink().toString());
                         }
                     });
                     ImageButton linkeinShare = (ImageButton)dialog.findViewById(R.id.linkedin_in_listview);
                     linkeinShare.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                            // createServiseToLinkedIn(1, position);
 
                         }
                     });
                     ImageButton mailShare =  (ImageButton)dialog.findViewById(R.id.mail_in_listview);
                     mailShare.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             MailSender.send(NewsRssActivity.this, currentArticle);
                         }
                     });
                     Button copyURLShare = (Button)dialog.findViewById(R.id.copy_url_list);
                     copyURLShare.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             int sdk = android.os.Build.VERSION.SDK_INT;
                             if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                 android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                 clipboard.setText(currentArticle.getLink().toString());
                             } else {
                                 ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                 ClipData clip = ClipData.newPlainText("label",currentArticle.getLink().toString());
                                 clipboard.setPrimaryClip(clip);
                             }
                             Toast toast = Toast.makeText(getApplicationContext(),"URL Copied ",Toast.LENGTH_SHORT);
                             toast.show();
 
                         }
                     });
 
                     dialog.show();
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
                     holder.articleImage = (ImageView) convertView.findViewById(R.id.rss_img_news_pass);
 
                     convertView.setTag(holder);
                 } else {
                     holder = (ViewHolder) convertView.getTag();
                 }
                 holder.articleTitle.setText(currentPodcast.getTitle());
                 sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                 dateArticleV = sdf.format(currentPodcast.getPubDate());
                 holder.articleDate.setText(dateArticleV);
                 holder.articleImage.setImageDrawable(getResources().getDrawable(R.drawable.podcast_icon));
 
                 rssListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                     @Override
                     public void  onClickFrontView (int position){
                         Intent startDetailPodcast = new Intent(NewsRssActivity.this,DetailsPodcast.class );
                         startDetailPodcast.putExtra("position", position-1);
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
                             startDetailJobs.putExtra("position", position-1);
                             startActivity(startDetailJobs);
                         }
 
 
                     });
 
                     break;
 
                 case 4:
                     miniSwipeActivator();
                     try {
                         LocalDB.open(getApplicationContext());
                     } catch (SQLException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     final Article currentFav = LocalDB.getArticle(position);
 
                     if (convertView == null) {
                         LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                         convertView = li.inflate(R.layout.rss_item_layout, parent, false);
                         holder = new ViewHolder();
 
                         holder.favBtn = (ImageButton) convertView.findViewById(R.id.fav_btn);
                         holder.shareBtn = (ImageButton)convertView.findViewById(R.id.share_btn);
                         holder.articleTitle = (TextView)convertView.findViewById(R.id.rss_news_title);
                         holder.articleDate = (TextView)convertView.findViewById(R.id.rss_news_date);
                         holder.articleImage = (ImageView) convertView.findViewById(R.id.rss_img_news_pass);
 
                         convertView.setTag(holder);
                     } else {
                         holder = (ViewHolder) convertView.getTag();
                     }
                    if (currentFav == null)
                        break;

                     holder.articleTitle.setText(currentFav.getTitle());
                     sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                     dateArticleV = sdf.format(currentFav.getPubDate());
                     holder.articleDate.setText(dateArticleV);
                     if (currentFav.getNewsImage() == null) {
                         holder.articleImage.setImageDrawable(getResources().getDrawable(R.drawable.default_news_icon));
                     }
                     else {
                         holder.articleImage.setImageDrawable(currentFav.getNewsImage());
                     }
 
                     rssListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                         @Override
                         public void  onClickFrontView (int position){
 
                             Intent startDetailJobs = new Intent(NewsRssActivity.this, DetailsFavorites.class );
                             startDetailJobs.putExtra("position", position-1);
                             startActivity(startDetailJobs);
 
 
                         }
 
 
                     });
 
                     if (!LocalDB.isArticleFavotites(currentFav.getGuid())){
                         holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.purple_favorite_vhite));
                     }   else {
                         holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.favorites));
 
                     }
                     LocalDB.close();
 
                     holder.favBtn.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             try {
                                 LocalDB.open(getApplicationContext());
                             } catch (SQLException e) {
                                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                             }
                             if (!LocalDB.isArticleFavotites(currentFav.getGuid())){
                                 LocalDB.addArticle(currentFav);
                                 holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.favorites));
                                 Toast toast = Toast.makeText(getApplicationContext(),"Article Added to Favorites " ,Toast.LENGTH_SHORT);
                                 toast.show();
                             }else{
                                 LocalDB.deleteArticle(currentFav.getGuid());
                                 holder.favBtn.setImageDrawable(getResources().getDrawable(R.drawable.purple_favorite_vhite));
                                Toast toast = Toast.makeText(getApplicationContext(),"Article Removed from Favourites " ,Toast.LENGTH_SHORT);
                                 toast.show();
                             }
                             LocalDB.close();
 
                         }
                     });
 
                     holder.shareBtn.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             final Dialog dialog = new Dialog(context);
                             dialog.setContentView(R.layout.share_panel_in_list);
                             dialog.setTitle("Share: " +currentFav.getTitle());
 
                             ImageButton faceBookShare = (ImageButton)dialog.findViewById(R.id.facebook_in_listview);
                             faceBookShare.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     onClickbtnConnectFB(4, position);
 
                                 }
                             });
                             ImageButton twitterShare = (ImageButton)dialog.findViewById(R.id.twitter_in_listview);
                             twitterShare.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     onTwitterClick(currentFav.getTitle()+" "+ currentFav.getLink().toString());
                                 }
                             });
                             ImageButton linkeinShare = (ImageButton)dialog.findViewById(R.id.linkedin_in_listview);
                             linkeinShare.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     // createServiseToLinkedIn(1, position);
 
                                 }
                             });
                             ImageButton mailShare =  (ImageButton)dialog.findViewById(R.id.mail_in_listview);
                             mailShare.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     MailSender.send(NewsRssActivity.this, currentFav);
                                 }
                             });
                             Button copyURLShare = (Button)dialog.findViewById(R.id.copy_url_list);
                             copyURLShare.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     int sdk = android.os.Build.VERSION.SDK_INT;
                                     if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                         android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                         clipboard.setText(currentFav.getLink().toString());
                                     } else {
                                         ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                         ClipData clip = ClipData.newPlainText("label",currentFav.getLink().toString());
                                         clipboard.setPrimaryClip(clip);
                                     }
                                     Toast toast = Toast.makeText(getApplicationContext(),"URL Copied ",Toast.LENGTH_SHORT);
                                     toast.show();
 
                                 }
                             });
 
                             dialog.show();
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
             ImageView articleImage;
 
         }
     }
 
     public void showPodcastList(){
         SwipeListView rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
         ((TextView)findViewById(R.id.rss_list_header_text)).setText("Podcasts");
         if (Build.VERSION.SDK_INT >= 16)
             findViewById(R.id.rss_list_header_image).setBackground(getResources().getDrawable(R.drawable.podcast_header));
         else
             findViewById(R.id.rss_list_header_image).setBackgroundDrawable(getResources().getDrawable(R.drawable.podcast_header));
 
         if(DataStorage.getPodcastList().size() == 0 ){
             DataStorage.updatePodcastList();
         }
 
         MyCAdapter adapter = new MyCAdapter(
                 this, createPodcastList(), R.layout.rss_item_layout,
                 new String[] { "rssnewstitle", "rssnewsdate", "rssnewsimage"},
                 new int [] { R.id.rss_news_title, R.id.rss_news_date,R.id.rss_img_news_pass});
         idLayout = 2;
         adapter.setViewBinder(new CustomViewBinder());
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
                 map.put("rssnewsimage", getResources().getDrawable(R.drawable.podcast_icon));
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
 
         ((TextView)findViewById(R.id.rss_list_header_text)).setText("Jobs");
         if (Build.VERSION.SDK_INT >= 16)
             findViewById(R.id.rss_list_header_image).setBackground(getResources().getDrawable(R.drawable.jobs_header));
         else
             findViewById(R.id.rss_list_header_image).setBackgroundDrawable(getResources().getDrawable(R.drawable.jobs_header));
 
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
         Intent startSearchActivity = new Intent(NewsRssActivity.this,SearchActivity.class);
         String stringgg = " ";
         startSearchActivity.putExtra("searchquery",stringgg);
         startActivity(startSearchActivity);
         //Toast toast = Toast.makeText(getApplicationContext(),"Запустить Поиск",Toast.LENGTH_SHORT);
         //toast.show();
     }
 
     public void showArticleListFromSideBar(final View view){
         //Toast toast = Toast.makeText(getApplicationContext(),"Показать все статьи",Toast.LENGTH_SHORT);
         //toast.show();
 
         if (idLayout != 1 & idLayout != 2 & idLayout != 3 & idLayout != 4) {
             slidingMenu.setContent(R.layout.main);
 
             rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
             LayoutInflater inflater = getLayoutInflater();
             ViewGroup header = (ViewGroup)inflater.inflate(R.layout.rss_header, rssListView, false);
             rssListView.addHeaderView(header, null, false);
 
             showAricleList();
 
             View app = findViewById(R.id.app);
             app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
         }
         else {
             showAricleList();
             slidingMenu.showContent();
         }
         miniSwipeActivator();
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
         if (idLayout != 1 & idLayout != 2 & idLayout != 3 & idLayout != 4) {
             slidingMenu.setContent(R.layout.main);
 
             rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
             LayoutInflater inflater = getLayoutInflater();
             ViewGroup header = (ViewGroup)inflater.inflate(R.layout.rss_header, rssListView, false);
             rssListView.addHeaderView(header, null, false);
 
             showFavoritesList();
 
             View app = findViewById(R.id.app);
             app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
         }
         else {
             showFavoritesList();
         }
         miniSwipeActivator();
 
 
     }
 
     private List<Map<String, ?>> createFavoritesList() {
         List<Article> artList = null;
 
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
         try
         {
             SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
             artList = LocalDB.getAllArticles();
 
             for (Article article : artList)
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
 
     public void showFavoritesList(){
         SwipeListView rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
         try {
             LocalDB.open(this.getApplicationContext());
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         List<Article> artList = null;
         artList = LocalDB.getAllArticles();
         if(artList.isEmpty() ){
             Toast toast = Toast.makeText(getApplicationContext(),"There are currently no favourite articles saved.",Toast.LENGTH_SHORT);
             toast.show();
 
         }  else {
 
             ((TextView)findViewById(R.id.rss_list_header_text)).setText("Favorites");
             if (Build.VERSION.SDK_INT >= 16)
                 findViewById(R.id.rss_list_header_image).setBackground(getResources().getDrawable(R.drawable.favorites_header));
             else
                 findViewById(R.id.rss_list_header_image).setBackgroundDrawable(getResources().getDrawable(R.drawable.favorites_header));
 
 
             MyCAdapter adapter = new MyCAdapter(
                     this,  createFavoritesList(), R.layout.rss_item_layout,
                     new String[] { "rssnewstitle", "rssnewsdate"},
                     new int [] { R.id.rss_news_title, R.id.rss_news_date});
             idLayout = 4;
             rssListView.setAdapter(adapter);
             slidingMenu.showContent();
         }
     }
 
     public void showSavedSearchFromSideBar(final View view){
         //TODO: Установите вызов showSavedSearchFromSideBar
         savedSearchListView = (SwipeListView) findViewById(R.id.saved_search);
         if(savedSearchListView.getVisibility() == View.GONE)       {
             DisplayMetrics metrics = this.getResources().getDisplayMetrics();
             float otstup = (float) (metrics.widthPixels *0.7) ;
             savedSearchListView.setSwipeMode(SwipeListView.SWIPE_MODE_RIGHT);
             savedSearchListView.setOffsetRight(otstup);
             savedSearchListView.setAnimationTime(500);
             savedSearchListView.setSwipeOpenOnLongPress(false);
             showSavedSearchList();
             Utility.setListViewHeightBasedOnChildren(savedSearchListView);
             savedSearchListView.setVisibility(View.VISIBLE);
         }  else{
            savedSearchListView.setVisibility(View.GONE);
         }
 
 
 
     }
 
     public void showSavedSearchList(){
         SavedSearchAdapter SaveSearchadapter;
 
         try {
             LocalDB.open(this.getApplicationContext());
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         artList = LocalDB.get10Searches();
         if(artList.isEmpty() ){
             Toast toast = Toast.makeText(getApplicationContext(),"Non",Toast.LENGTH_SHORT);
             toast.show();
         }  else {
 
             SaveSearchadapter = new SavedSearchAdapter(
                     this,  createSavedSearchList(), R.layout.saved_search_item,
                     new String[] { "rssnewstitle"},
                     new int [] { R.id.search_query});
 
             savedSearchListView.setAdapter(SaveSearchadapter);
         }
 
 
     }
 
     private List<Map<String, ?>> createSavedSearchList() {
         List<Searches> searchQuery;
 
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
 
         try
         {
 
             searchQuery = LocalDB.get10Searches();
 
             for (Searches searches : searchQuery)
             {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("rssnewstitle", searches.getSearch());
 
                 items.add(map);
             }
         }
         catch(Exception e) {
             e.printStackTrace();
         }
 
 
         return items;
     }
 
     public void showPodcastsListFromSideBar(final View view){
         //Toast toast = Toast.makeText(getApplicationContext(),"Показать все Подкасты",Toast.LENGTH_SHORT);
         //toast.show();
 
         if (idLayout != 1 & idLayout != 2 & idLayout != 3 & idLayout != 4) {
             slidingMenu.setContent(R.layout.main);
 
             rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
             LayoutInflater inflater = getLayoutInflater();
             ViewGroup header = (ViewGroup)inflater.inflate(R.layout.rss_header, rssListView, false);
             rssListView.addHeaderView(header, null, false);
 
             showPodcastList();
 
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
 
         if (idLayout != 1 & idLayout != 2 & idLayout != 3 & idLayout != 4) {
             slidingMenu.setContent(R.layout.main);
 
             rssListView = (SwipeListView) findViewById(R.id.rssListView);
 
             LayoutInflater inflater = getLayoutInflater();
             ViewGroup header = (ViewGroup)inflater.inflate(R.layout.rss_header, rssListView, false);
             rssListView.addHeaderView(header, null, false);
 
             showJobstList();
 
 
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
 
         ImageButton facebookGoToPage = (ImageButton)findViewById(R.id.contacts_facebook);
         ImageButton twitterGoToPage = (ImageButton)findViewById(R.id.contacts_twitter);
         ImageButton linkeinGoToPage = (ImageButton)findViewById(R.id.contacts_linkedin);
 
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
 
         facebookGoToPage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/groups/303317250605/?fref=ts"));
                 startActivity(myIntent);
             }
         });
         twitterGoToPage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/charteredaccirl"));
                 startActivity(myIntent);
             }
         });
         linkeinGoToPage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/groups?gid=1783368&trk=myg_ugrp_ovr"));
                 startActivity(myIntent);
             }
         });
 
 
     }
 
     public void showSettingsFromSideBar(final View view){
         slidingMenu.setContent(R.layout.details_settings);
         idLayout = 7;
         View sidebarButton  = findViewById(R.id.settingSidebarButton);
 
         ImageButton facebookGoToPage = (ImageButton)findViewById(R.id.facebook_imageView);
         ImageButton twitterGoToPage = (ImageButton)findViewById(R.id.twitter_imageView);
         ImageButton linkeinGoToPage = (ImageButton)findViewById(R.id.linkedln_imageView);
 
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
                 Toast.makeText(NewsRssActivity.this, "Rate", Toast.LENGTH_LONG).show();
             }
         };
 
         ImageView.OnClickListener ocFeedback = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 MailSender.sendFeedback(NewsRssActivity.this);
             }
         };
 
         legalView.setOnClickListener(ocLegal);
         rateView.setOnClickListener(ocRate);
         feedbackView.setOnClickListener(ocFeedback);
 
         facebookGoToPage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/groups/303317250605/?fref=ts"));
                 startActivity(myIntent);
             }
         });
         twitterGoToPage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/charteredaccirl"));
                 startActivity(myIntent);
             }
         });
         linkeinGoToPage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/groups?gid=1783368&trk=myg_ugrp_ovr"));
                 startActivity(myIntent);
             }
         });
 
 
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
 
     public class SavedSearchAdapter extends SimpleAdapter {
         private List<? extends Map<String, ?>> data;
         private Context context;
 
 
 
 
 
 
 
         public SavedSearchAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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
 
 
             final ViewHolder holder;
 
             Searches currentSearchQuery =artList.get(position);
 
             if (convertView == null) {
                 LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = li.inflate(R.layout.saved_search_item, parent, false);
                 holder = new ViewHolder();
                 holder.searchQuery = (TextView)convertView.findViewById(R.id.search_query);
                 holder.delete_search_query = (ImageButton)convertView.findViewById(R.id.del_btn);
                 convertView.setTag(holder);
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
             holder.searchQuery.setText(currentSearchQuery.getSearch());
             //final String strToIntent =     holder.searchQuery.getText().toString();
             blabla = currentSearchQuery.getSearch();
             savedSearchListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                 @Override
                 public void  onClickFrontView (int position){
 
                    Intent startSearchArticl = new Intent(NewsRssActivity.this, SearchActivity.class);
                    startSearchArticl.putExtra("searchquery",  artList.get(position).getSearch().toString());
                    startActivity(startSearchArticl);
                 }
 
 
             });
 
             holder.delete_search_query.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     try {
                         LocalDB.open(getApplicationContext());
                     } catch (SQLException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     Toast toast = Toast.makeText(getApplicationContext(),"Search Query Deleted " +position ,Toast.LENGTH_SHORT);
                     toast.show();
                     Searches currentSearchToDelete = LocalDB.get10Searches().get(position);
 
                     LocalDB.deleteSearch(currentSearchToDelete.getId());
                     showSavedSearchList();
                     Utility.setListViewHeightBasedOnChildren(savedSearchListView);
                 }
             });
 
             return convertView;
         }
 
         public class ViewHolder {
             ImageButton delete_search_query;
             TextView searchQuery;
 
 
         }
         public void clearData() {
             // clear the data
             data.clear();
         }
     }
 
     public static Context getAppContext() {
         return NewsRssActivity.context;
     }
 }
 
