 package com.libcorp.shootmaniacenter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.libcorp.shootmaniacenter.R;
 import com.libcorp.shootmaniacenter.adapters.feedsAdapter;
 import com.libcorp.shootmaniacenter.global.Variables;
 import com.libcorp.shootmaniacenter.menu.MenuDrawerClass;
 import com.libcorp.shootmaniacenter.adapters.menuItemAdapter;
 import com.libcorp.shootmaniacenter.structures.RSS.FeedMessage;
 import com.libcorp.shootmaniacenter.utilities.BufferBitmap;
 import com.libcorp.shootmaniacenter.utilities.NadeoDataSeeker;
 import com.libcorp.shootmaniacenter.utilities.RSS.RssParseHandler;
 import com.libcorp.shootmaniacenter.utilities.cache.NewsCacheManager;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 /**
  * Created by artum on 10/06/13.
  */
 
 public class NewsReader extends Activity {
 
     ListView listView;
     feedsAdapter mAdapter;
     ArrayList<FeedMessage> feedMessages;
 
     _getNewsFeeds task = new _getNewsFeeds();
     _getNewsFeedsImages taskImg = new _getNewsFeedsImages();
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.news_reader);
 
         Variables.LoadVariables(this);
 
         feedMessages = new ArrayList<FeedMessage>();
         listView = (ListView)findViewById(R.id.feedList);
         mAdapter = new feedsAdapter(this, feedMessages);
 
         setupDrawer();
 
 
             //SHOOTMANIA OFFICIAL NEWS
         if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://blog.maniaplanet.com/blog/tag/shootmania-2/feed/");
         else
            task.execute("http://blog.maniaplanet.com/blog/tag/shootmania-2/feed/");
 
 
         /*
             //SHOOTMANIA ITALIA NEWS
         if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
             task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://www.shootmania.it/feed/");
         else
             task.execute("http://www.shootmania.it/feed/");
         */
 
         //new _getNewsFeeds().execute("http://mania-actu.com/rss/news-en-sm.xml");
 
 
 
         AdapterView.OnItemClickListener newsListener = new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 if(feedMessages != null && feedMessages.size() > 1)
                 {
                     //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(feedMessages.get(i).getGuid()));
                     //startActivity(browserIntent);
                     Intent intent = new Intent(NewsReader.this, ShowNews.class);
                     intent.putExtra("title", feedMessages.get(i).getTitle());
                     intent.putExtra("content", feedMessages.get(i).getEncodedcontent());
                     intent.putExtra("date", feedMessages.get(i).getPubdate());
                     intent.putExtra("author", feedMessages.get(i).getAuthor());
                     startActivity(intent);
                 }
             }
         };
 
         listView.setOnItemClickListener(newsListener);
     }
 
     private class _getNewsFeeds extends AsyncTask<String, Void, List<FeedMessage>> {
 
         @Override
         protected List<FeedMessage> doInBackground(String... urls) {
             String feed = urls[0];
             try {
                 URL url = new URL(feed);
                 SAXParserFactory spf = SAXParserFactory.newInstance();
                 SAXParser sp = spf.newSAXParser();
                 XMLReader xr = sp.getXMLReader();
                 NadeoDataSeeker seeker = new NadeoDataSeeker();
 
                 RssParseHandler rh = new RssParseHandler();
 
                 xr.setContentHandler(rh);
                 xr.parse(new InputSource(url.openStream()));
 
                 setProgress(1);
 
                 return rh.getItems();
             }
             catch (IOException e) {
                 Log.e("RSS Handler IO", e.getMessage() + " >> " + e.toString());
             } catch (SAXException e) {
                 Log.e("RSS Handler SAX", e.toString());
                 e.printStackTrace();
             } catch (ParserConfigurationException e) {
                 Log.e("RSS Handler Parser Config", e.toString());
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(List<FeedMessage> messages) {
             if(messages != null && messages.size() > 0)
             {
                 if(NewsCacheManager.hasToUpdate(NewsReader.this))
                 {
                     BufferBitmap.ereaseBitmapCache();
                     for(FeedMessage message : messages)
                         feedMessages.add(message);
                     listView.setAdapter(mAdapter);
 
                     if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                         taskImg.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                     else
                         taskImg.execute("");
                 }
                 else
                 {
                     if(!showNewsFromCache())
                     {
                         FeedMessage error = new FeedMessage();
                         error.setAuthor("artum");
                         error.setTitle("Cannot Retrive News");
                         error.setDescription("Errore retriving information from the news feed, check your connection!");
                         feedMessages.add(error);
                         listView.setAdapter(mAdapter);
                     }
 
                }
             }
             else
             {
                 if(!showNewsFromCache())
                 {
                     FeedMessage error = new FeedMessage();
                     error.setAuthor("artum");
                     error.setTitle("Cannot Retrive News");
                     error.setDescription("Errore retriving information from the news feed, check your connection!");
                     feedMessages.add(error);
                     listView.setAdapter(mAdapter);
                 }
             }
     }
     }
 
     private class _getNewsFeedsImages extends AsyncTask<String, Void, List<FeedMessage>> {
 
         @Override
         protected List<FeedMessage> doInBackground(String... urls) {
             if(urls[0] != "cached")
             {
             JSONArray newsJson = new JSONArray();
 
             for(int i = 0; i < feedMessages.size(); i++)
             {
                 if(!isCancelled() && getFirstHTMLImage(feedMessages.get(i).getEncodedcontent()) != null)
                 {
                     Bitmap bitmap = BufferBitmap.loadBitmap(getFirstHTMLImage(feedMessages.get(i).getEncodedcontent()));
                     if(bitmap == null && getFirstHTMLImage(feedMessages.get(i).getDescription()) != null)
                         bitmap = BufferBitmap.loadBitmap(getFirstHTMLImage(feedMessages.get(i).getDescription()));
                     feedMessages.get(i).setImage(bitmap);
                 }
 
                 JSONObject messageJson = new JSONObject();
                 try {
                     messageJson.put("title", feedMessages.get(i).getTitle());
                     messageJson.put("description", feedMessages.get(i).getDescription());
                     messageJson.put("author", feedMessages.get(i).getAuthor());
                     messageJson.put("content", feedMessages.get(i).getEncodedcontent());
                     messageJson.put("pubdate", feedMessages.get(i).getPubdate());
                     messageJson.put("guid", feedMessages.get(i).getGuid());
 
                     newsJson.put(messageJson);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
 
             NewsCacheManager.saveToCache(NewsReader.this, "news", newsJson.toString());
             }
             else
             {
                 for(int i = 0; i < feedMessages.size(); i++)
                 {
                     if(!isCancelled() && getFirstHTMLImage(feedMessages.get(i).getEncodedcontent()) != null)
                     {
                         Bitmap bitmap = BufferBitmap.loadBitmap(getFirstHTMLImage(feedMessages.get(i).getEncodedcontent()));
                         if(bitmap == null && getFirstHTMLImage(feedMessages.get(i).getDescription()) != null)
                             bitmap = BufferBitmap.loadBitmap(getFirstHTMLImage(feedMessages.get(i).getDescription()));
                         feedMessages.get(i).setImage(bitmap);
                         publishProgress();
                     }
                 }
             }
             return null;
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
             listView.invalidateViews();
         }
 
         @Override
         protected void onPostExecute(List<FeedMessage> messages) {
             listView.invalidateViews();
         }
     }
 
 
     private String getFirstHTMLImage(String content)
     {
         if(content != null)
         {
             Pattern pattern = Pattern.compile("<img.+src=[\\'\"]([^\\'\"]+)[\\'\"].*>");
             Matcher matcher = pattern.matcher(content);
             if(matcher.find())
                 return matcher.group(1);
         }
         return null;
     }
 
     private boolean showNewsFromCache()
     {
         String newsJson;
         if((newsJson = NewsCacheManager.loadFromCache(NewsReader.this, "news")) != null)
         {
             try {
 
                 JSONArray messages = new JSONArray(newsJson);
 
                 for(int i = 0; i < messages.length(); i++)
                 {
                     JSONObject message = messages.getJSONObject(i);
                     FeedMessage news = new FeedMessage();
                     news.setTitle(message.getString("title"));
                     news.setDescription(message.getString("description"));
                     news.setAuthor(message.getString("author"));
                     news.setEncodedContent(message.getString("content"));
                     news.setPubDate(message.getString("pubdate"));
                     //news.setLink(message.getString("link"));
                     news.setGuid(message.getString("guid"));
 
                     feedMessages.add(i, news);
 
 
                 }
                 listView.setAdapter(mAdapter);
 
                 //SHOOTMANIA OFFICIAL NEWS
                 if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
                     new _getNewsFeedsImages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cached");
                 else
                     new _getNewsFeedsImages().execute("cached");
 
                 return true;
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
         return false;
     }
 
     // <editor-fold defaultstate="collapsed" desc="MENU DRAWER CODE">
 
     ActionBarDrawerToggle mDrawerToggle;
     DrawerLayout mDrawerLayout;
     ListView mDrawerList;
 
     private void setupDrawer()      //Insert this code in OnCreate function!
     {
 
         String[] mTitles = getResources().getStringArray(R.array.menu_items);
         mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
         mDrawerList = (ListView)findViewById(R.id.navigation_list);
 
         mDrawerToggle = new ActionBarDrawerToggle(this,
                 mDrawerLayout,
                 R.drawable.ic_drawer,
                 R.string.app_name,
                 R.string.app_name){
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 invalidateOptionsMenu();
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 invalidateOptionsMenu();
             }
         };
 
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // Set the adapter for the list view
         mDrawerList.setAdapter(new menuItemAdapter(this, mTitles));
         // Set the list's click listener
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Pass the event to ActionBarDrawerToggle, if it returns
         // true, then it has handled the app icon touch event
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle your other action bar items...
 
         return super.onOptionsItemSelected(item);
     }
 
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
             MenuDrawerClass menuDrawerClass = new MenuDrawerClass();
             mDrawerLayout.closeDrawers();
             menuDrawerClass.selectItem(position, NewsReader.this);
             if(task != null && task.getStatus() == AsyncTask.Status.RUNNING)
                 task.cancel(true);
             if(taskImg != null && taskImg.getStatus() == AsyncTask.Status.RUNNING)
                 taskImg.cancel(true);
         }
     }
     // </editor-fold>
 
 
 
 }
