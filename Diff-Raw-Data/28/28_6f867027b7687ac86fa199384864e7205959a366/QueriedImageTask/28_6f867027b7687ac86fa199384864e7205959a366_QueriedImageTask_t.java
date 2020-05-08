 package com.network;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.ImageView;
 
 import com.lazylist.ImageLoader;
 
 public class QueriedImageTask extends AsyncTask<Void, Void, String>{
 
 
 	public String url = "http://www.airliners.net/search/photo.search?regsearch=B-MAG&distinct_entry=true";
 	Document doc;
 	public ImageView imgV;
 	public ImageLoader imgLoader;
 
 	@Override
 	protected String doInBackground(Void... params) {
 		String result="";
 		
 		try {
 			
 			Document doc = (Document) Jsoup.connect(url).get();
 			Elements elements = doc.getElementsByTag("img");
 			String tag="";
 			for (int i = 0 ; i < elements.size() ; i ++)
 			{
 				tag = elements.get(i).toString();
 				if(tag.contains("http://cdn-www.airliners.net/aviation-photos/"))
 					break;
 			}
 			
 			{
 				String[] arr = tag.split(" ");
 	        	String imageURL = arr[1];
 	        	imageURL = imageURL.replace("\"", "");
 	        	result = imageURL.replace("src=", "");
 	        	
 				Log.d("TEST", imageURL);
 			}
 	        
 		} 
 		catch (Exception e) {
 			
			//Log.d("TEST", e.getMessage());
 		}
 
 		return result;
 	}
 	
 	@Override
 	protected void onPostExecute(String result) {
 		super.onPostExecute(result);
 		imgLoader.DisplayImage(result, imgV);
 	}
 }
