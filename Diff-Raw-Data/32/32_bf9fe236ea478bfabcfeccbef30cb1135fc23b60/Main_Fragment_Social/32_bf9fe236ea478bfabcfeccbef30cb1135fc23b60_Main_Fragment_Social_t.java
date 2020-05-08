 package com.geekytheory.miguelcatalandev.developerdays;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.geekytheory.miguelcatalandev.developerdays.adapters.Social_Adapter_Tweets;
 import com.geekytheory.miguelcatalandev.developerdays.objects.Tweet;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class Main_Fragment_Social extends ListFragment implements
 		OnRefreshListener<ListView> {
 
 	public ArrayList<Tweet> tweets;
 	private Social_Adapter_Tweets adapter;
 	private final static String str_url = "http://search.twitter.com/search.json?rpp=50&q=%23DDIEEEUMH";
 	private PullToRefreshListView listView;
 	DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss Z",
 			Locale.ENGLISH);
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		inflater = (LayoutInflater) getActivity().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 
 		RelativeLayout layout = (RelativeLayout) inflater.inflate(
 				R.layout.main_fragment_social, null);
 
 		tweets = new ArrayList<Tweet>();
 		adapter = new Social_Adapter_Tweets(getActivity(), tweets);
 		return layout;
 	}
 
 	@Override
 	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
 		if (isOnline()) {
 			new DowloadTweets(getActivity(), str_url, this).execute();
 		} else {
 			listView.onRefreshComplete();
 		}
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		listView = (PullToRefreshListView) getView().findViewById(R.id.listica);
 		listView.setAdapter(adapter);
 		listView.setOnRefreshListener(this);
 		listView.setEmptyView(getActivity()
 				.findViewById(R.id.social_empty_view));
 		if (isOnline()) {
 			new DowloadTweets(getActivity(), str_url, this).execute();
 		}
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		getActivity().runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				listView.onRefreshComplete();
 				adapter.notifyDataSetChanged();
 
 			}
 		});
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
		final Tweet tweet = tweets.get(position-1);
 		String url = "https://twitter.com/" + tweet.getUserNick() + "/status/"
 				+ tweet.getTweetUrl();
 
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.setData(Uri.parse(url));
 		startActivity(intent);
 	}
 
 	private class DowloadTweets extends AsyncTask<String, Void, Boolean> {
 
 		private String str_url_tweet;
 
 		public DowloadTweets(Context c, String url,
 				Main_Fragment_Social fragment) {
 			this.str_url_tweet = url;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			if (success) {
 				try {
 					getActivity().runOnUiThread(new Runnable() {
 
 						@Override
 						public void run() {
 
 							listView.onRefreshComplete();
 							adapter.notifyDataSetChanged();
 
 						}
 					});
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			} else {
 				Toast.makeText(getActivity(), "Error en la lectura",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 		protected Boolean doInBackground(final String... args) {
 			String read_hashtag = readTwitterFeed(this.str_url_tweet);
 			try {
 				JSONObject jsonObject = new JSONObject(read_hashtag);
 				String strData = jsonObject.getString("results");
 				JSONArray jsonContent = new JSONArray(strData);
 				int numItems = jsonContent.length();
 
 				tweets.clear();
 				for (int i = 0; i < numItems; i++) {
 					JSONObject itemjson = jsonContent.getJSONObject(i);
 					Tweet tweet = new Tweet();
 					tweet.setUserName(itemjson.getString("from_user_name"));
 					tweet.setTweet(itemjson.getString("text").replace("&gt;",
 							""));
 					tweet.setUserNick(itemjson.getString("from_user"));
 					tweet.setTweetTime(formatter.parse(""
 							+ itemjson.getString("created_at")));
 					tweet.setUserImageUrl(itemjson
 							.getString("profile_image_url"));
 					tweet.setTweetUrl(itemjson.getString("id_str"));
 					tweets.add(tweet);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		}
 
 		private void trustEveryone() {
 			try {
 				HttpsURLConnection
 						.setDefaultHostnameVerifier(new HostnameVerifier() {
 
 							@Override
 							public boolean verify(String hostname,
 									SSLSession session) {
 								return true;
 							}
 						});
 				SSLContext context = SSLContext.getInstance("TLS");
 				context.init(null,
 						new X509TrustManager[] { new X509TrustManager() {
 							public void checkClientTrusted(
 									X509Certificate[] chain, String authType)
 									throws CertificateException {
 							}
 
 							public void checkServerTrusted(
 									X509Certificate[] chain, String authType)
 									throws CertificateException {
 							}
 
 							public X509Certificate[] getAcceptedIssuers() {
 								return new X509Certificate[0];
 							}
 						} }, new SecureRandom());
 				HttpsURLConnection.setDefaultSSLSocketFactory(context
 						.getSocketFactory());
 			} catch (Exception e) { // should never happen
 				e.printStackTrace();
 			}
 		}
 
 		private String readTwitterFeed(String url) {
 			StringBuilder builder = new StringBuilder();
 			trustEveryone();
 			HttpClient client = new DefaultHttpClient();
 			HttpGet httpGet = new HttpGet(url);
 			try {
 				HttpResponse response = client.execute(httpGet);
 				StatusLine statusLine = response.getStatusLine();
 				int statusCode = statusLine.getStatusCode();
 				if (statusCode == 200) {
 					HttpEntity entity = response.getEntity();
 					InputStream content = entity.getContent();
 					BufferedReader reader = new BufferedReader(
 							new InputStreamReader(content));
 					String line;
 					while ((line = reader.readLine()) != null) {
 						builder.append(line);
 					}
 				} else {
 					Log.d("ERROR", "Error pidiendo JSON");
 				}
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return builder.toString();
 		}
 
 	}
 
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getView().getContext()
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnected()) {
 			return true;
 		}
 		return false;
 	}
 
 }
