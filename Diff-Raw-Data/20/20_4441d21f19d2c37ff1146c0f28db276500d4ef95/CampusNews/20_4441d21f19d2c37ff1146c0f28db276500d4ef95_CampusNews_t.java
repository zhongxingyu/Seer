 package com.patmahoneyJR.csu.vikings;
 
 import java.util.ArrayList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import android.app.ListActivity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 public class CampusNews extends ListActivity {
 	
 	private static final String URL = "http://www.csuohio.edu/news/";
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		
        // Make a new AsyncTask to fetch news
         GetNewsItemsTask getNews = new GetNewsItemsTask();
 		getNews.execute();
 	}
 	
 	private class GetNewsItemsTask extends AsyncTask<Void, Void, ArrayList<String>> {
 		
		// Connects using Jsoup, gets all "p" tagged elements from URL,
		// then gets the "a" tag child elements of those, then if those
		// have text, adds them to an ArrayList called items which is returned
		// later.
 		protected ArrayList<String> doInBackground(Void... urls) {
 			ArrayList<String> items = new ArrayList<String>();
 			try {
 				Document doc = Jsoup.connect(URL).get();
				for (Element e : doc.getElementsByTag("p")) {
					if((e.getElementsByTag("a")).hasText()) // Checks is it has text
 						items.add(e.getElementsByTag("a").text());
					else break; // break loop before we pick up crappy links to stuff we don't care about...
 				}
				// ROBUST-IFY THAT SHIT
 			} catch (Exception e) {		
 				Toast.makeText(getApplicationContext(), "Couldn't fetch articles, try again later.",
						Toast.LENGTH_SHORT).show();  
 			}
 			return items;
 		}
 		
		// Called immediately after doInBackground and receives items, then updates UI to reflect the new data!
 		protected void onPostExecute(ArrayList<String> items) {
 			
 			setListAdapter(new ArrayAdapter<String>(getBaseContext(),
 	                android.R.layout.simple_list_item_1,
 	                items));
 		}
 	}
 }
