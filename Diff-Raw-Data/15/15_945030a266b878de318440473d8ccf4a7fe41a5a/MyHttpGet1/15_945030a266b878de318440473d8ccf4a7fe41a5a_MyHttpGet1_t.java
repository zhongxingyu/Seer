 package com.androidapp.CLApp;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.TextView;
 
 //  
 public class MyHttpGet1 extends AsyncTask<TextView, Void, String> {
 	private static final boolean Element = false;
 	TextView myTextView;
 	String result;
 	String url;
 	String url1;
 
 	@Override
 	protected String doInBackground(TextView... params) {
 		this.myTextView = params[0];
 		return makeHttpRequest();
 	}
 
 	final String makeHttpRequest() {
 		try {
 			
 			String title, price, date;
 			Document doc = Jsoup.connect(url).get();
 			// allData = doc.getAllElements();
 			Elements getTitle = doc.getElementsByClass("summaryHeader").select("a[href]");
 //			title = getTitle.toString();
 			Elements getDate = doc.getElementsByClass("date");
 			int cnt = 0;
 			for (Element src : getTitle)
 			{
 				title = src.ownText();
 				date = getDate.get(cnt).ownText();
 				cnt++;
 				if (result == null) {
					result = "-----Results from BackPage-----" + "\n" ;
 				}
 				
				result += "\n" + date + " " + title + "\n";
 			}
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public void setUrl(String query) {
 		if (query.contains(" ")) {
			query = query.replace(" ", "%20");
 		}
 		 url = "http://washingtondc.backpage.com/buyselltrade/?keyword=" + query;
 	}
 
 	
 	protected void onPostExecute(String page) {
 		myTextView.setText(myTextView.getText().toString() + page);
 	}
 
 }
