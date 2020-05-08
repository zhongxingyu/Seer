 package jp.co.nikkeibp.itpro.rssreader;
 
 import java.util.ArrayList;
 
import android.R;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 public class RssReaderActivity extends ListActivity {
 	public static final String RSS_FEED_URL = "http://itpro.nikkeibp.co.jp/rss/ITpro.rdf";
 	private ArrayList<Item> mItems;
 	private RssListAdapter mAdapter;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mItems = new ArrayList<Item>();
         mAdapter = new RssListAdapter(this, mItems);
         
         RssParserTask task = new RssParserTask(this, mAdapter);
         task.execute(RSS_FEED_URL);
     }
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Item item = mItems.get(position);
 		Intent intent = new Intent(this, ItemDetailActivity.class);
 		intent.putExtra("TITLE", item.getmTitle());
 		intent.putExtra("DESCRIPTION", item.getmDescription());
 		startActivity(intent);
 	}
     
     
 }
