 package com.vertigem.dukex;
 
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
import android.webkit.WebView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
     PostsPagerAdapter postPagerAdapter;
 
     ViewPager postsPager;
 
     private final ArrayList<Post> posts = new ArrayList<Post>();
 
 
     private static final int PROGRESS = 0x1;
 
     private ProgressBar mProgress;
     private int mProgressStatus = 0;
     private Handler mHandler = new Handler();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
 
     	mProgress = (ProgressBar) findViewById(R.id.progress_bar);
     	new Thread(new Runnable() {
             public void run() {
                 while (mProgressStatus < 100) {
                     mProgressStatus = getPosts();
 
                   
                     mHandler.post(new Runnable() {
                         public void run() {
                             mProgress.setProgress(mProgressStatus);
                         }
                     });
                 }
             }
         }).start();
     	updateDisplay();
     }
 
     private int getPosts(){
     	RSSFeed feed = getFeed("http://duke.vertigem.xxx/tagged/poemas/rss");
     	for(RSSItem item: feed.getAllItems()){
     		mProgressStatus = 50;
     		posts.add(new Post(item.getTitle(), item.getDescription()));
     	}
 		return 100;
 
     }
     private RSSFeed getFeed(String urlToRssFeed)
     {
 
         try
         {
 
             // setup the url
            URL url = new URL(urlToRssFeed);
 
 
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
 
 
            XMLReader xmlreader = parser.getXMLReader();
 
            RSSHandler theRssHandler = new RSSHandler();
            // assign our handler
            xmlreader.setContentHandler(theRssHandler);
            // get our data through the url class
            InputSource is = new InputSource(url.openStream());
            // perform the synchronous parse
            xmlreader.parse(is);
            // get the results - should be a fully populated RSSFeed instance,
 		   // or null on error
            return theRssHandler.getFeed();
         }
         catch (Exception ee)
         {
         	Log.e("ERROR", ee.toString());
             // if you have a problem, simply return null
             return null;
         }
     }
 
     public void updateDisplay(){
     	postPagerAdapter = new PostsPagerAdapter(getSupportFragmentManager(), posts);
 
 
         postsPager = (ViewPager) findViewById(R.id.pager);
         postsPager.setAdapter(postPagerAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
      * sections of the app.
      */
     public class PostsPagerAdapter extends FragmentPagerAdapter {
 
 
 
         private ArrayList<Post> posts;
 
 		public PostsPagerAdapter(FragmentManager fm, ArrayList<Post> posts) {
             super(fm);
             this.posts = posts;
         }
 
         @Override
         public Fragment getItem(int i) {
             Fragment fragment = new PostFragment(this.posts.get(i));
             Bundle args = new Bundle();
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             return posts.size();
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
         	return (CharSequence) posts.get(position).title;
         }
     }
 
     public static class PostFragment extends Fragment{
    	 	public Post post;
 
     	public PostFragment(Post post){
     		this.post = post;
     	}
 
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View layout = inflater.inflate(R.layout.post, container, false);
 
           
           
            WebView webview = (WebView) layout.findViewById(R.id.html_text);
            webview.loadData(post.text, "text/html; charset=UTF-8", null);
 
             return layout;
         }
 
     }
 }
