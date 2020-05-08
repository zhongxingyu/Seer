 package com.jalinsuara.android.news;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.webkit.ConsoleMessage;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.RenderPriority;
 import android.webkit.WebView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.jalinsuara.android.BaseFragment;
 import com.jalinsuara.android.R;
 import com.jalinsuara.android.helper.NetworkUtils;
 import com.jalinsuara.android.helpers.lazylist.ImageLoader;
 import com.jalinsuara.android.news.model.News;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 /**
  * Fragment to show news detail
  * 
  * @author tonoman3g
  * 
  */
 public class NewsFragment extends BaseFragment {
 
 	private News mNews;
 	private ImageView mImageView;
 	private TextView mDateUpdatedView;
 	private TextView mTitleTextView;
 	private TextView mDescriptionTextView;
 	private WebView mWebView;
 	public NewsFragment() {
 
 	}
 
 	public NewsFragment(News news) {
 		mNews = news;
 	}
 
 	@Override
 	public int getLayoutId() {
 		return R.layout.fragment_news;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		if (mNews != null) {
 			mImageView = (ImageView) getView().findViewById(
 					R.id.fragment_news_image_imageview);
 			mTitleTextView = (TextView) getView().findViewById(
 					R.id.fragment_news_title_textview);
 			mDescriptionTextView = (TextView) getView().findViewById(
 					R.id.fragment_news_description_textview);
 			mDateUpdatedView = (TextView) getView().findViewById(
 					R.id.fragment_news_date_textview);
 
 			if (mNews.getPictureUrl() != null
 					&& mNews.getPictureUrl().length() > 0) {
 				ImageLoader loader = new ImageLoader(getSherlockActivity());
 				loader.DisplayImage(mNews.getPictureUrl(), mImageView);
 			}
 
 			mTitleTextView.setText(mNews.getTitle());
 			if (mNews.getUpdatedAt() != null) {
 				mDateUpdatedView
 						.setText(com.jalinsuara.android.helper.DateUtils
 								.toStringDateOnly(mNews.getUpdatedAt()));
 			}
 			if (mNews.getDescription() != null) {
 				mDescriptionTextView.setText(Html.fromHtml(mNews
 						.getDescription()));
 			}
 
 			resetStatus();
 			setStatusShowContent();
 		} else {
 			resetStatus();
 			setStatusError(getString(R.string.error));
 		}
 		
 		mWebView = (WebView)getView().findViewById(R.id.activity_show_map_web_view_fragment_news);
 		mWebView.setWebChromeClient(new WebChromeClient() {
 
 			public boolean onConsoleMessage(ConsoleMessage cm) {
 				Log.d("JalinSuara Web View", cm.message() + " -- From line "
 						+ cm.lineNumber() + " of " + cm.sourceId());
 
 				return true;
 			}
 			
 			
 		});
 		WebSettings webSettings = mWebView.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setRenderPriority(RenderPriority.HIGH);
 		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
 		// multi-touch zoom
 		webSettings.setBuiltInZoomControls(true);
 		webSettings.setDisplayZoomControls(false);
 		
 		//LoadOneNews 
 		LoadOneNews task = new LoadOneNews();
 		task.execute();
 		
 		
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();		
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();		
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();		
 	}
 
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();		
 	}
 	private String readAsset(String fileName) {
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getAssets().open(fileName), "UTF-8"));
 			// do reading, usually loop until end of file reading
 			StringBuilder sb = new StringBuilder();
 			String mLine = reader.readLine();
 			while (mLine != null) {
 				// process line
 				sb.append(mLine);
 				mLine = reader.readLine();
 			}
 
 			reader.close();
 			return sb.toString();
 		} catch (IOException e) {
 			// log the exception
 			e.printStackTrace();
 		}
 		return "";
 	}
 	/**
 	 * Load A News
 	 * 
 	 * @author tonoman3g
 	 * 
 	 */
 	private class LoadOneNews extends AsyncTask<Object, Integer, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			resetStatus();
 			setStatusProgress(getString(R.string.loading), false);
 		}
 
 		@Override
 		protected Integer doInBackground(Object... params) {
 			mNews = NetworkUtils.getPosts(0).get(0);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			super.onPostExecute(result);
 			if (mNews != null) {
 				String data = readAsset("leaflet/index.html");
 				
 
 				StringBuilder sb = new StringBuilder();
 				int i = 0;
 	
 					if (mNews.getLatitude() != 0 && mNews.getLongitude() != 0) {
 						if (i == 0) {
 							sb.append("{\"lat\":" + mNews.getLatitude());
 							sb.append(",\"lon\":" + mNews.getLongitude() + ", \"title\":\""+mNews.getTitle()+"\" "+", \"id\":\""+mNews.getId()+"\" "+"}");
 						} else {
 							sb.append(",{\"lat\":" + mNews.getLatitude());
 							sb.append(",\"lon\":" + mNews.getLongitude() + ", \"title\":\""+mNews.getTitle()+"\" "+", \"id\":\""+mNews.getId()+"\" "+"}");
 						}
 					}
 					i++;
 				
 
 				if (i > 0) {
 					log.info("markers: "+sb.toString());
 					data = data.replace("{{posts}}", sb.toString());
 				} else {
 					data = data.replace("{{posts}}", "");
 										
 				}
 				log.info("html "+data);
 
 				mWebView.loadDataWithBaseURL("file:///android_asset/", data,
 						"text/html", "utf-8", "");
 				// or
 				// mWebView.loadUrl("file:///android_asset/leaflet/test.html");
 				
 				resetStatus();
 				setStatusShowContent();
 			}
 		}
 
 	}
 
 }
