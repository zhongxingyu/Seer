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
 		
         GetNewsItemsTask getNews = new GetNewsItemsTask();
 		getNews.execute();
 	}
 	
 	private class GetNewsItemsTask extends AsyncTask<Void, Void, ArrayList<String>> {
 		
 		protected ArrayList<String> doInBackground(Void... urls) {
 			ArrayList<String> items = new ArrayList<String>();
 			try {
 				Document doc = Jsoup.connect(URL).get();
				for (Element e : doc.getElementsByClass("copy")) {
					if(e.getElementsByTag("a").text().trim().length() != 0)			
 						items.add(e.getElementsByTag("a").text());
 				}
 			} catch (Exception e) {		
 				Toast.makeText(getApplicationContext(), "Couldn't fetch articles, try again later.",
						Toast.LENGTH_SHORT).show();
 			}
 			return items;
 		}
 		
 		protected void onPostExecute(ArrayList<String> items) {
 			
 			setListAdapter(new ArrayAdapter<String>(getBaseContext(),
 	                android.R.layout.simple_list_item_1,
 	                items));
 		}
 	}
 }
