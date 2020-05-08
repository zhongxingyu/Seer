 /*******************************************************************************
  * Authors:
  *     Jesse Chen
  * 
  * Copyright (c) 2011 Jesse Chen.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package net.jessechen.accessibleyoutube;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import net.jessechen.accessibleyoutube.AsyncImageLoader.ImageCallback;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class QueryResults extends Activity {
 
 	private static NodeList nl;
 	private ListView lv;
 	private HashMap<String, YoutubeResult> h;
 	private String query;
 	private TextView tv;
 	private ArrayList<ListItem> results;
 	private mAdapter m_adapter;
 	private Runnable searchResults;
 	private ProgressDialog m_ProgressDialog = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.queryresults);
 
 		query = (String) getIntent().getExtras().getCharSequence("query");
 
 		results = new ArrayList<ListItem>();
 		m_adapter = new mAdapter(this, R.layout.list_item, results);
 		lv = (ListView) findViewById(R.id.list);
 		lv.setAdapter(m_adapter);
 
 		h = new HashMap<String, YoutubeResult>();
 
 		tv = (TextView) findViewById(R.id.resultstitle);
 
 		searchResults = new Runnable() {
 
 			@Override
 			public void run() {
 				getResults();
 			}
 		};
 		Thread thread = new Thread(null, searchResults, "MagentoBackground");
 		thread.start();
 		m_ProgressDialog = ProgressDialog.show(QueryResults.this,
 				"Please wait...", "Retrieving data...", true);
 
 	}
 
 	private Runnable returnRes = new Runnable() {
 		@Override
 		public void run() {
 			if (results != null && results.size() > 0) {
 				m_adapter.notifyDataSetChanged();
 			}
 			m_ProgressDialog.dismiss();
 			m_adapter.notifyDataSetChanged();
 		}
 	};
 
 	private void getResults() {
 		try {
 			URL url;
 			String encodedQuery;
 			if (query == null) { // means we are pulling a specific user's
 									// videos that has captions
 				query = (String) getIntent().getExtras().getCharSequence(
 						"username");
 				encodedQuery = URLEncoder.encode(query);
 				url = new URL(
 						"http://gdata.youtube.com/feeds/api/videos?author="
 								+ encodedQuery + "&caption&v=2");
 				tv.setText(query + "'s Channel");
 			} else { // normal operation, search for youtube videos matching
 						// query && has captions
 				encodedQuery = URLEncoder.encode(query);
 				url = new URL("http://gdata.youtube.com/feeds/api/videos?q="
 						+ encodedQuery + "&caption&v=2");
 				tv.setText("Results for \"" + query + "\"");
 			}
 
 			URLConnection connection = url.openConnection();
 
 			HttpURLConnection httpConnection = (HttpURLConnection) connection;
 
 			int responseCode = httpConnection.getResponseCode();
 
 			if (responseCode == HttpURLConnection.HTTP_OK) {
 				InputStream in = httpConnection.getInputStream();
 
 				DocumentBuilderFactory dbf = DocumentBuilderFactory
 						.newInstance();
 				DocumentBuilder db = dbf.newDocumentBuilder();
 
 				Document dom = db.parse(in);
 				Element docEle = dom.getDocumentElement();
 
 				nl = docEle.getElementsByTagName("entry");
 				if (nl != null && nl.getLength() > 0) {
 					for (int i = 0; i < nl.getLength(); i++) {
 						Element entry = (Element) nl.item(i);
 						Element title = (Element) entry.getElementsByTagName(
 								"title").item(0);
 						Element id = (Element) entry.getElementsByTagName("id")
 								.item(0);
 						Element media = (Element) entry.getElementsByTagName(
 								"media:group").item(0);
 						Element tn = (Element) media.getElementsByTagName(
 								"media:thumbnail").item(0);
 						Element dp = (Element) media.getElementsByTagName(
 								"media:description").item(0);
 						Element author = (Element) entry.getElementsByTagName(
 								"author").item(0);
 						Element name = (Element) author.getElementsByTagName(
 								"name").item(0);
 
 						String channelName = name.getFirstChild()
 								.getNodeValue();
 						String description = dp.getFirstChild().getNodeValue();
 						String thumbnailUrl = tn.getAttribute("url");
 						String videoId = id.getFirstChild().getNodeValue()
 								.split(":")[3]; // grabs unique video ID from
 												// entry->id
 						String titleString = title.getFirstChild()
 								.getNodeValue();
 						YoutubeResult sr = new YoutubeResult(titleString,
 								videoId, thumbnailUrl, description, channelName);
 						h.put(titleString, sr); // store in HashMap for look up
 						results.add(new ListItem(titleString, sr.getVideoUrl(),
 								thumbnailUrl));
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		if (results.isEmpty()) {
 			TextView t = new TextView(this);
 			t.setText("No results for " + query);
 			t.setTextSize(20);
 			setContentView(t);
 		}
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				YoutubeResult l = h.get(((TextView) view
 						.findViewById(R.id.text)).getText());
 				Intent i = new Intent(QueryResults.this, Result.class);
 				i.putExtra("videotitle", l.getTitle());
 				i.putExtra("videourl", l.getVideoUrl());
 				i.putExtra("thumbnailurl", l.getThumbnailUrl());
 				i.putExtra("description", l.getDescription());
 				i.putExtra("username", l.getUsername());
 				startActivity(i);
 			}
 		});
 		runOnUiThread(returnRes);
 	}
 
 	/**
 	 * Custom ArrayAdapter that loads thumbnails in a separate thread, reuse
 	 * rows in the list, and caching child views within a row.
 	 * 
 	 * Credits go to this article:
 	 * http://blog.jteam.nl/2009/09/17/exploring-the-world-of-android-part-2/
 	 * 
 	 * @author Jesse Chen
 	 * 
 	 */
 	private class mAdapter extends ArrayAdapter<ListItem> {
 		private ArrayList<ListItem> items;
 		private AsyncImageLoader asyncImageLoader;
 
 		public mAdapter(Context context, int textViewResourceId,
 				ArrayList<ListItem> items) {
 			super(context, textViewResourceId, items);
 			this.items = items;
 			asyncImageLoader = new AsyncImageLoader();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			ViewCache viewCache;
 
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.list_item, null);
 				viewCache = new ViewCache(v);
 				v.setTag(viewCache);
 			} else {
 				viewCache = (ViewCache) v.getTag();
 			}
 
 			final ListItem l = items.get(position);
 			if (l != null) {
 				TextView t = (TextView) viewCache.getTextView();
				final ImageView img = (ImageView) viewCache.getImageView();

 				t.setText(l.getTitle());
 
 				Drawable cachedImage = asyncImageLoader.loadDrawable(
						l.getThumbnailURL(), new ImageCallback() {
 							public void imageLoaded(Drawable imageDrawable,
 									String imageUrl) {
								if (img != null) {
									img.setImageDrawable(imageDrawable);
 								}
 							}
 						});
 				img.setImageDrawable(cachedImage);
 			}
 			return v;
 		}
 	}
 }
