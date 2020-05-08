 package org.csie.mpp.buku.helper;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.csie.mpp.buku.App;
 import org.csie.mpp.buku.Util;
 import org.csie.mpp.buku.db.BookEntry;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.text.Html;
 import android.text.Spanned;
 import android.util.Log;
 
 public abstract class BookUpdater {
 	private static final String SOURCE_GOOGLE_BOOKS = "Google Books";
 	private static final String SOURCE_BOOKS_TW = "博客來";
 
 	private static final String BOOKS_TW_PREFIX = "http://www.books.com.tw/exep/prod/booksfile.php?item=";
 	private static final String GOOGLE_BOOKS_PREFIX = "http://books.google.com/books?id=";
 	
 	public static interface OnUpdateStatusChangedListener {
 		public enum Status {
 			OK_ENTRY,
 			OK_INFO,
 			BOOK_NOT_FOUND,
 			UNKNOWN
 		}
 
 		public void onUpdateStart();
 		public void onUpdateProgress();
 		public void onUpdateFinish(Status status);
 	}
 	
 	public static BookUpdater create(BookEntry entry) {
 		switch(entry.isbn.length()) {
 			case 10:
 				entry.isbn = Util.toIsbn13(entry.isbn);
 				break;
 			case 17:
 				entry.isbn = Util.upcToIsbn(entry.isbn);
 				break;
 			default:
 				break;
 		}
 		String countryCode = entry.isbn.substring(3, 6);
         if(countryCode.equals("957") || countryCode.equals("986"))
         	return new ChineseUpdater(entry);
 		return new NormalUpdater(entry);
 	}
 	
 	public static BookUpdater create(BookEntry entry, String link) {
 		return new LinkUpdater(entry, link);
 	}
 	
 	protected final BookEntry entry;
 	protected OnUpdateStatusChangedListener listener;
 	
 	protected BookUpdater(BookEntry e) {
 		entry = e;
 	}
 	
 	public void setOnUpdateFinishedListener(OnUpdateStatusChangedListener l) {
 		listener = l;
 	}
 	
 	public abstract void updateEntry();
 	public abstract void updateInfo();
 	
 	protected abstract class AsyncUpdater extends AsyncTask<URL, Integer, OnUpdateStatusChangedListener.Status> {
 		@Override
 		protected OnUpdateStatusChangedListener.Status doInBackground(URL... urls) {
 			return update(urls);
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			if(listener != null)
 				listener.onUpdateStart();
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer... progresses) {
 			if(listener != null)
 				listener.onUpdateProgress();
 		}
 		
 		@Override
 		protected void onPostExecute(OnUpdateStatusChangedListener.Status result) {
 			/* this will be run on UI thread */
 			if(listener != null)
 				listener.onUpdateFinish(result);
 		}
 		
 		protected abstract OnUpdateStatusChangedListener.Status update(URL... urls);
 	}
 	
 	public static class NormalUpdater extends BookUpdater {
 		protected NormalUpdater(BookEntry e) {
 			super(e);
 		}
 		
 		@Override
 		public void updateEntry() {
 			try {
 				AsyncUpdater async = new AsyncUpdater() {
 					@Override
 					protected OnUpdateStatusChangedListener.Status update(URL... urls) {
 						try {
 							JSONObject json = new JSONObject(Util.urlToString(urls[0]));
 							entry.vid = json.getJSONArray("items").getJSONObject(0).getString("id");
 							
 							json = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo");
 							entry.title = json.getString("title");
 							
 							entry.author = parseJSONArray(json.getJSONArray("authors"));
 							publishProgress();
 							
 							if(json.has("imageLinks"))	{
 								entry.coverLink = json.getJSONObject("imageLinks").getString("thumbnail");
 								entry.cover = Util.urlToImage(new URL(entry.coverLink));
 							}
 						}
 						catch(Exception e) {
 							Log.e(App.TAG, e.toString());
 							return OnUpdateStatusChangedListener.Status.UNKNOWN;
 						}
 
 						return OnUpdateStatusChangedListener.Status.OK_ENTRY;
 					}
 				};
 				
 				URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn" + entry.isbn);
 				async.execute(url);
 			}
 			catch(MalformedURLException e) {
 				Log.e(App.TAG, e.toString());
 			}
 		}
 
 		@Override
 		public void updateInfo() {
 			try {
 				AsyncUpdater async = new AsyncUpdater() {
 					@Override
 					protected OnUpdateStatusChangedListener.Status update(URL... urls) {
 						try {
 							JSONObject json = new JSONObject(Util.urlToString(urls[0]));
 							json = json.getJSONObject("volumeInfo");
 							
 							if(json.has("averageRating"))
 								entry.info.rating = (float)json.getDouble("averageRating");
 							if(json.has("ratingsCount"))
 								entry.info.ratingsCount = json.getInt("ratingsCount");
 							if(json.has("description"))
 								entry.info.description = Html.fromHtml(json.getString("description"));
 							publishProgress();
 
 							HttpClient httpclient = new DefaultHttpClient();
 						    HttpGet httpget = new HttpGet(urls[1].toURI());
 					    	HttpResponse response = httpclient.execute(httpget);
 					    	int statusCode = response.getStatusLine().getStatusCode();
 					    	if (statusCode != HttpStatus.SC_OK) {
 					    		return OnUpdateStatusChangedListener.Status.UNKNOWN;
 					    	}
 
 					    	HttpEntity entity = response.getEntity();
 					    	String result = EntityUtils.toString(entity, "UTF-8");
 					    	result = result.substring(result.indexOf(">User reviews<") + ">User reviews<".length());
 					    	entry.info.reviews = new ArrayList<Spanned>();
 					    	while(result.indexOf("<p dir=ltr>")!=-1){
 					    		result = result.substring(result.indexOf("<p dir=ltr>") + "<p dir=ltr>".length());
 					    		entry.info.reviews.add(Util.htmlToText(result.substring(0, result.indexOf("</p>"))));
 					    	}
 
 					    	entry.info.sourceName = SOURCE_GOOGLE_BOOKS;
 					    	entry.info.source = GOOGLE_BOOKS_PREFIX + entry.vid;
 						}
 						catch(Exception e) {
 							Log.e(App.TAG, e.toString());
 							return OnUpdateStatusChangedListener.Status.UNKNOWN;
 						}
 						
 						return OnUpdateStatusChangedListener.Status.OK_INFO;
 					}
 				};
 				
 				URL url0 = new URL("https://www.googleapis.com/books/v1/volumes/" + entry.vid);
 				URL url1 = new URL("http://books.google.com.tw/books?id=" + entry.vid + "&sitesec=reviews&hl=eng");
 				async.execute(url0, url1);
 			}
 			catch(MalformedURLException e) {
 				Log.e(App.TAG, e.toString());
 			}
 		}
 	}
 	
 	public static class ChineseUpdater extends BookUpdater {
 		protected ChineseUpdater(BookEntry e) {
 			super(e);
 		}
 
 
 		@Override
 		public void updateEntry() {
 			try {
 				AsyncUpdater async = new AsyncUpdater() {
 					@Override
 					protected OnUpdateStatusChangedListener.Status update(URL... urls) {
 						try {
 							HttpGet httpget = new HttpGet(urls[0].toURI());
 							HttpResponse response = new DefaultHttpClient().execute(httpget);
 					    	int statusCode = response.getStatusLine().getStatusCode();
 					    	if (statusCode != HttpStatus.SC_OK) {
 					    		return OnUpdateStatusChangedListener.Status.UNKNOWN;
 					    	}
 
 					    	HttpEntity entity = response.getEntity();
 					    	String result = EntityUtils.toString(entity, "UTF-8");
 					    	if(result.indexOf("item=")<0) {
 					    		return OnUpdateStatusChangedListener.Status.BOOK_NOT_FOUND;
 					    	}
 					    	
 					    	result = result.substring(result.indexOf("item=")+"item=".length());
 					    	entry.vid = result.substring(0, result.indexOf("\"")).trim();
 					    	result = result.substring(result.indexOf("title=\"") + "title=\"".length()); 	
 					    	entry.title = result.substring(0, result.indexOf("\"")).trim();
 					    	publishProgress();
 					    	
 					    	result = result.substring(result.indexOf("?image=") + "?image=".length());
 					    	entry.coverLink = result.substring(0, result.indexOf("&"));
 							entry.cover = Util.urlToImage(new URL(entry.coverLink));
 							publishProgress();
 							
 							if(result.indexOf("\"go_author\"")!=-1){
 								result = result.substring(result.indexOf("\"go_author\"") + "\"go_author\"".length());
 								result = result.substring(result.indexOf("title=\"") + "title=\"".length());
 						    	entry.author = result.substring(0, result.indexOf("\"")).trim();
 							}
 							
 					    	return OnUpdateStatusChangedListener.Status.OK_ENTRY;
 						}
 						catch(Exception e) {
 							Log.e(App.TAG, e.toString());
 							return OnUpdateStatusChangedListener.Status.UNKNOWN;
 						}
 					}
 				};
 				
 				URL url = new URL("http://search.books.com.tw/exep/prod_search.php?key=" + entry.isbn);
 				async.execute(url);
 			}
 			catch(MalformedURLException e) {
 				Log.e(App.TAG, e.toString());
 			}
 		}
 		
 		@Override
 		public void updateInfo() {
 			try {
 				AsyncUpdater async = new AsyncUpdater() {
 					@Override
 					protected OnUpdateStatusChangedListener.Status update(URL... urls) {
 						try {
 							HttpClient httpclient = new DefaultHttpClient();
 						    HttpGet httpget = new HttpGet(urls[0].toURI());
 							HttpResponse response = httpclient.execute(httpget);
 					    	int statusCode = response.getStatusLine().getStatusCode();
 					    	if (statusCode != HttpStatus.SC_OK) {
 					    		return OnUpdateStatusChangedListener.Status.UNKNOWN;
 					    	}
 
 					    	HttpEntity entity = response.getEntity();
 					    	String result = EntityUtils.toString(entity, "big5");
 					    	result = result.substring(result.indexOf("class=\"content_word\"")+"class=\"content_word\"".length());
 					    	result = result.substring(result.indexOf("<BR><BR>")+"<BR><BR>".length());
 					    	result = result.substring(0, result.indexOf("</td>"));
 					    	entry.info.description = Util.htmlToText(result.trim());
 					    	publishProgress();
 
 					    	httpget = new HttpGet(urls[1].toURI());
 					    	response = httpclient.execute(httpget);
 					    	statusCode = response.getStatusLine().getStatusCode();
 					    	if (statusCode != HttpStatus.SC_OK) {
 					    		return OnUpdateStatusChangedListener.Status.UNKNOWN;
 					    	}
 					    	entity = response.getEntity();
 					    	result = EntityUtils.toString(entity, "big5");
 					    	
 					    	if(result.indexOf(">讀者書評等級：")!=-1 && result.indexOf("</tt>")!=-1){
 					    		String tmp = result.substring(result.indexOf(">讀者書評等級："), result.indexOf("</tt>"));
 					    		entry.info.rating = 0;
 					    		while(tmp.indexOf("images/m_bul11.gif")!=-1){
 					    			++entry.info.rating;
 					    			tmp = tmp.substring(tmp.indexOf("images/m_bul11.gif") + "images/m_bul11.gif".length());
 					    		}
 					    	}
 					    	entry.info.reviews = new ArrayList<Spanned>();
 					    	while(result.indexOf("<p class=\"des\">")!=-1){
 					    		result = result.substring(result.indexOf("<p class=\"des\">") + "<p class=\"des\">".length());
 					    		entry.info.reviews.add(Util.htmlToText(result.substring(0, result.indexOf("</p>"))));
 					    	}
 
 					    	entry.info.sourceName = SOURCE_BOOKS_TW;
 					    	entry.info.source = BOOKS_TW_PREFIX + entry.vid;
 						}
 						catch(Exception e) {
 							Log.e(App.TAG, e.toString());
 							return OnUpdateStatusChangedListener.Status.UNKNOWN;
 						}
 						
 						return OnUpdateStatusChangedListener.Status.OK_INFO;
 					}
 				};
 				
 				URL url0 = new URL("http://m.books.com.tw/product/showmore/" + entry.vid);
 				URL url1 = new URL("http://www.books.com.tw/exep/prod/reader_opinion.php?item=" + entry.vid);
 				async.execute(url0, url1);
 			}
 			catch(MalformedURLException e) {
 				Log.e(App.TAG, e.toString());
 			}
 		}
 	}
 	
 	public static class LinkUpdater extends BookUpdater {
 		private String link;
 		
 		protected LinkUpdater(BookEntry e, String link) {
 			super(e);
 			
 			this.link = link;
 		}
 
 		@Override
 		public void updateEntry() {
 			try {
 				if(link.startsWith(BOOKS_TW_PREFIX)) {
 					entry.vid = link.substring(BOOKS_TW_PREFIX.length());
 					
 					AsyncUpdater async = new AsyncUpdater() {
 						@Override
 						protected OnUpdateStatusChangedListener.Status update(URL... urls) {
 							try {
 								HttpClient httpclient = new DefaultHttpClient();
 							    HttpGet httpget = new HttpGet(urls[0].toURI());
 								HttpResponse response = httpclient.execute(httpget);
 						    	int statusCode = response.getStatusLine().getStatusCode();
 						    	if (statusCode != HttpStatus.SC_OK) {
 						    		return OnUpdateStatusChangedListener.Status.UNKNOWN;
 						    	}
 	
 						    	HttpEntity entity = response.getEntity();
 						    	String result = EntityUtils.toString(entity, "big5");
 						    	result = result.substring(result.indexOf("?image=") + "?image=".length());
 						    	entry.coverLink = result.substring(0, result.indexOf("&"));
 								entry.cover = Util.urlToImage(new URL(entry.coverLink));
 								
 						    	result = result.substring(result.indexOf("class=\"prd001\""));
 						    	result = result.substring(result.indexOf("<h1"));
 						    	result = result.substring(result.indexOf("<span>") + "<span>".length());
 						    	entry.title = result.substring(0, result.indexOf("</span>"));
 						    	
 						    	result = result.substring(result.indexOf("class=\"prd002\""));
 						    	result = result.substring(result.indexOf("f=author\">") + "f=author\">".length());
 						    	entry.author = result.substring(0, result.indexOf("</a>"));
 						    	
 						    	result = result.substring(result.indexOf("ISBN"));
 						    	result = result.substring(result.indexOf("<dfn>") + "<dfn>".length());
 						    	entry.isbn = result.substring(0, result.indexOf("</dfn>"));
 							}
 							catch(Exception e) {
 								Log.e(App.TAG, e.toString());
 								return OnUpdateStatusChangedListener.Status.UNKNOWN;
 							}
 							return OnUpdateStatusChangedListener.Status.OK_ENTRY;
 						}
 					};
 					
 					URL url0 = new URL("http://www.books.com.tw/exep/prod/booksfile.php?item=" + entry.vid);
 					async.execute(url0);
 				}
 				else if(link.startsWith(GOOGLE_BOOKS_PREFIX)) {
 					entry.vid = link.substring(GOOGLE_BOOKS_PREFIX.length());
 					
 					AsyncUpdater async = new AsyncUpdater() {
 						@Override
 						protected OnUpdateStatusChangedListener.Status update(URL... urls) {
 							try {
 								JSONObject json = new JSONObject(Util.urlToString(urls[0]));
 								json = json.getJSONObject("volumeInfo");
 								
 								String isbn10 = null, isbn13 = null;
 								JSONArray array = json.getJSONArray("industryIdentifiers");
 								for(int i = 0; i < array.length(); i++) {
 									JSONObject item = array.getJSONObject(i);
 									if(item.getString("type").equals("ISBN_10"))
 										isbn10 = item.getString("identifier");
 									else if(item.getString("type").equals("ISBN_13"))
 										isbn13 = item.getString("identifier");
 								}
 								
 								if(isbn13 != null)
 									entry.isbn = isbn13;
 								else if(isbn10 != null)
									entry.isbn = Util.toIsbn13(isbn10);
 								else
 									return OnUpdateStatusChangedListener.Status.BOOK_NOT_FOUND;
 								
 								entry.title = json.getString("title");
 								entry.author = parseJSONArray(json.getJSONArray("authors"));
 								publishProgress();
 
 								if(json.has("imageLinks"))	{
 									entry.coverLink = json.getJSONObject("imageLinks").getString("thumbnail");
 									entry.cover = Util.urlToImage(new URL(entry.coverLink));
 								}
 							}
 							catch(Exception e) {
 								Log.e(App.TAG, e.toString());
 								return OnUpdateStatusChangedListener.Status.UNKNOWN;
 							}
 							return OnUpdateStatusChangedListener.Status.OK_ENTRY;
 						}
 					};
 					
 					URL url0 = new URL("https://www.googleapis.com/books/v1/volumes/" + entry.vid);
 					async.execute(url0);
 				}
 			}
 			catch(MalformedURLException e) {
 				Log.e(App.TAG, e.toString());
 			}
 		}
 
 		@Override
 		public void updateInfo() {
 			if(entry.isbn != null) {
 				BookUpdater updater = BookUpdater.create(entry);
 				updater.setOnUpdateFinishedListener(listener);
 				updater.updateInfo();
 			}
 		}
 	}
 	
 	protected String parseJSONArray(JSONArray array) throws JSONException {
 		StringBuilder builder = new StringBuilder();
 		for(int i = 0; i < array.length(); i++) {
 			if(i > 0)
 				builder.append(",");
 			builder.append(array.getString(i));
 		}
 		return builder.toString();
 	}
 }
