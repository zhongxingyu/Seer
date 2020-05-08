 package com.techram.pondyapp;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import com.google.ads.*;
 import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
 import com.techram.R;
 
 public class NewsList extends Activity {
 	/** Called when the activity is first created. */
 	ListView lv1;
 	private AdView adView;
 	ProgressDialog ShowProgress;
 	public ArrayList<Post> PostList = new ArrayList<Post>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.newslist);
 
 		lv1 = (ListView) findViewById(R.id.listView1);
 
 		ShowProgress = ProgressDialog.show(NewsList.this, "",
 				"Loading. Please wait...", true);
 		new loadingTask().execute("http://puducherrynews.com/?feed=rss2");
 
 		lv1.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				 Intent intent = new Intent (NewsList.this,WebActivity.class);
 				 intent.putExtra("Thumbnail", PostList.get(position).getThumbnail());
 				 intent.putExtra("content", PostList.get(position).getDescription());
 		          startActivity(intent);
 				
 			}
 		});
 
 		if (adView == null) {
 			adView = (AdView) this.findViewById(R.id.AdView);
 			adView.loadAd(new AdRequest());
 
 		} else {
 			adView.loadAd(new AdRequest());
 		}
 
 	}
 
 	@Override
 	public void onDestroy() {
 		if (adView != null) {
 			adView.destroy();
 		}
 		super.onDestroy();
 	}
 
 	class loadingTask extends AsyncTask<String, Void, String> {
 
 		protected String doInBackground(String... urls) {
 
 			SAXHelper sh = null;
 			try {
 				sh = new SAXHelper(urls[0]);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 			sh.parseContent("");
 			return "";
 
 		}
 
 		protected void onPostExecute(String s) {
 			PostList.remove(0);
 			lv1.setAdapter(new EfficientAdapter(NewsList.this, PostList,
 					getAssets()));
 			ShowProgress.dismiss();
 
 		}
 	}
 
 	class SAXHelper {
 		public HashMap<String, String> userList = new HashMap<String, String>();
 		private URL url2;
 
 		public SAXHelper(String url1) throws MalformedURLException {
 			this.url2 = new URL(url1);
 		}
 
 		public RSSHandler parseContent(String parseContent) {
 			RSSHandler df = new RSSHandler();
 			try {
 
 				SAXParserFactory spf = SAXParserFactory.newInstance();
 				SAXParser sp = spf.newSAXParser();
 				XMLReader xr = sp.getXMLReader();
 				xr.setContentHandler(df);
 				xr.parse(new InputSource(url2.openStream()));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return df;
 		}
 	}
 
 	class RSSHandler extends DefaultHandler {
 		public Boolean isItem = false;
 		private Post currentPost = new Post();
 		StringBuffer chars = new StringBuffer();
 
 		@Override
 		public void startElement(String uri, String localName, String qName,
 				Attributes atts) {
 
 			chars = new StringBuffer();
 			if (localName.equalsIgnoreCase("item")) {
 
 			}
 		}
 
 		@Override
 		public void endElement(String uri, String localName, String qName)
 				throws SAXException {
 
 			// Log.i("this.isItem:", localName
 			// +" : "+String.valueOf(this.isItem));
 			if (localName.equalsIgnoreCase("title")
 					&& currentPost.getTitle() == null) {
 				currentPost.setTitle(chars.toString());
 
 			}
 			if (localName.equalsIgnoreCase("pubDate")
 					&& currentPost.getPubDate() == null) {
 				currentPost.setPubDate(chars.toString());
 
 			}
 			if (localName.equalsIgnoreCase("encoded")
 					&& currentPost.getThumbnail() == null) {
 				String str = chars.toString();
 				currentPost.setDescription(str);
 				Pattern p = Pattern
 						.compile("<img[^>]*src=[\"|']([^(\"|')]+)[\"|'][^>]*>");
 				Matcher m = p.matcher(str);
 				if (m.find()) {
 					String url = m.group(1);
 					// Log.i("Title", data.get(position).getTitle().toString());
 					Log.i("URL", url);
 					// UrlImageViewHelper.setUrlDrawable(holder.image, url);
 					currentPost.setThumbnail(url.trim());
 				}
 			}
 			if (localName.equalsIgnoreCase("link")
 					&& currentPost.getUrl() == null) {
 				currentPost.setUrl(chars.toString());
 			}
 
 			if (localName.equalsIgnoreCase("item")) {
 				PostList.add(currentPost);
 				currentPost = new Post();
 			}
 
 		}
 
 		@Override
 		public void characters(char ch[], int start, int length) {
 			chars.append(new String(ch, start, length));
 		}
 
 	}
 
 }
