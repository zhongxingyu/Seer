 package se.chalmers.h_sektionen;
 
 import java.util.ArrayList;
 
 import se.chalmers.h_sektionen.utils.DataSource;
 import se.chalmers.h_sektionen.utils.MenuItems;
 import se.chalmers.h_sektionen.utils.NewsAdapter;
 import se.chalmers.h_sektionen.utils.NewsItem;
 import se.chalmers.h_sektionen.utils.MockTemp;
 
 import android.widget.ListView;
 
 public class MainActivity extends BaseActivity {
     
 	@Override
 	protected void onResume() {
 
 		super.onResume();
 		setCurrentView(MenuItems.NEWS);
 		createNewsView();
     
     private void createNewsView(){
     	getFrameLayout().removeAllViews();
 		getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_news, null));
     	
     	ListView newsFeed;
         NewsAdapter newsAdapter;
         ArrayList<NewsItem> list = new ArrayList<NewsItem>();
     
 		newsFeed = (ListView) findViewById(R.id.news_feed);
 
 		newsAdapter = new NewsAdapter(this, R.layout.news_feed_item, list);
 
 		newsFeed.setAdapter(newsAdapter);
 		
 		newsAdapter.refresh(new DataSource<ArrayList<NewsItem>>(){	
 			@Override
 			public ArrayList<NewsItem> getData(){
 				return (ArrayList<NewsItem>) MockTemp.parseData(MockTemp.getData());
 			}
 		});
     }
 }
