 package com.freep.freepsonline;
 
 import java.lang.ref.WeakReference;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Paint;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class FreePrintShopOnline extends Activity {
 
 	private ViewPager mPager;
 	private static int NUMBER_OF_PAGES = 5;
 	private MPagerAdapter mAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mAdapter = new MPagerAdapter();
 		mPager = (ViewPager) findViewById(R.id.my_pager);
 		mPager.setAdapter(mAdapter);
 		
 		}
 	
 	private class MPagerAdapter extends PagerAdapter
 	{
 		
 		@Override
 		public int getCount()
 		{
 			return NUMBER_OF_PAGES;
 		}
 		
 		@Override
 		public boolean isViewFromObject(View view, Object object) 
 		{
 			return (view==((View)object));
 		}
 
 		@Override
 		public void destroyItem(ViewGroup collection, int position, Object view)
 		{
 			collection.removeView((View) view);
 		}
 		
 		@Override
 		public Object instantiateItem(ViewGroup collection, int position)
 		{
 			
 			View layout = null;
 			LayoutInflater inflater = (LayoutInflater) 
 					collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			switch (position){
 			case 0:
 				layout = inflater.inflate(R.layout.webview_holder, null);
 				//layoutRef = new WeakReference<WebView>((WebView) inflater.inflate(R.layout.webview_holder, null));
 				
 				WeakReference<WebView> mWebViewRef = new WeakReference<WebView>((WebView)layout.findViewById(R.id.my_webview));
 			
 				mWebViewRef.get().getSettings().setJavaScriptEnabled(false);
 				WebSettings webSettings0 = mWebViewRef.get().getSettings();
 				
 				webSettings0.setJavaScriptEnabled(false);
 		        webSettings0.setBuiltInZoomControls(true);
 				mWebViewRef.get().requestFocusFromTouch();
 			    
 				mWebViewRef.get().setWebViewClient(new WebViewClient()
 				{
 		    		public boolean shouldOverrideUrlLoading(WebView view, String url){
 		    			if (url.startsWith("http:") || url.startsWith("https:")){
 		    				return false;
 		    			}
 		    			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 		    			startActivity(intent);
 		    			return true;
 		    		}
 
 		        });
 		        mWebViewRef.get().setWebChromeClient(new WebChromeClient());
 		        
 		        mWebViewRef.get().loadUrl("file:///android_asset/shelter" + getString(R.string.assetLocation) +".html");
 		    	((ViewPager)collection).addView((View)layout, 0);
 				break;
 
 			case 1:
 				
 				layout = inflater.inflate(R.layout.webview_holder, null);
 				WeakReference<WebView> mWebViewRef1 = new WeakReference<WebView>((WebView)layout.findViewById(R.id.my_webview));
 				//mWebView = (WebView)layout.findViewById(R.id.my_webview);
 				
 				mWebViewRef1.get().getSettings().setJavaScriptEnabled(false);
 				WebSettings webSettings1 = mWebViewRef1.get().getSettings();
 		        
 				webSettings1.setJavaScriptEnabled(false);
 		        webSettings1.setBuiltInZoomControls(true);
 		        mWebViewRef1.get().requestFocusFromTouch();
 		        
 		        mWebViewRef1.get().setWebViewClient(new WebViewClient()
 		        {
 		    		public boolean shouldOverrideUrlLoading(WebView view, String url){
 		    			if (url.startsWith("http:") || url.startsWith("https:")){
 		    				return false;
 		    			}
 		    			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 		    			startActivity(intent);
 		    			return true;
 		    		}
 		        });
 
 		        mWebViewRef1.get().setWebChromeClient(new WebChromeClient());
 		        mWebViewRef1.get().loadUrl("file:///android_asset/pantry" + getString(R.string.assetLocation) +".html");
 				((ViewPager)collection).addView((View)layout, 0);
 				
 				break;
 				
 			case 2:
 				layout = inflater.inflate(R.layout.webview_holder, null);
 				WeakReference<WebView> mWebViewRef2 = new WeakReference<WebView>((WebView)layout.findViewById(R.id.my_webview));
 				//mWebView = (WebView)layout.findViewById(R.id.my_webview);
 				
 				mWebViewRef2.get().getSettings().setJavaScriptEnabled(false);
 				WebSettings webSettings2 = mWebViewRef2.get().getSettings();
 				
 				webSettings2.setJavaScriptEnabled(false);
 				webSettings2.setBuiltInZoomControls(true);
 				mWebViewRef2.get().requestFocusFromTouch();
 				
 				mWebViewRef2.get().setWebViewClient(new WebViewClient());
 				mWebViewRef2.get().setWebChromeClient(new WebChromeClient());
 		        //mWebView0.loadData(readTextFromResource(R.raw.shelter_english), "text/html", "utf-8");
 				mWebViewRef2.get().loadUrl("file:///android_asset/eats_png1.html");
 				
 				((ViewPager)collection).addView((View)layout, 0);
 				break;
 				
 			case 3:
 				layout = inflater.inflate(R.layout.webview_holder, null);
 				WeakReference<WebView> mWebViewRef3 = new WeakReference<WebView>((WebView)layout.findViewById(R.id.my_webview));
 				//mWebView = (WebView)layout.findViewById(R.id.my_webview);
 				
 				mWebViewRef3.get().getSettings().setJavaScriptEnabled(false);
 				WebSettings webSettings3 = mWebViewRef3.get().getSettings();
 				
 				webSettings3.setJavaScriptEnabled(false);
 				webSettings3.setBuiltInZoomControls(true);
 				mWebViewRef3.get().requestFocusFromTouch();
 				
 				mWebViewRef3.get().setWebViewClient(new WebViewClient());
 				mWebViewRef3.get().setWebChromeClient(new WebChromeClient());
 				mWebViewRef3.get().loadUrl("file:///android_asset/eats_png2.html");
 				((ViewPager)collection).addView((View)layout, 0);
 				break;
 			
 			case 4:
				layout = inflater.inflate(R.layout.webview_holder, null);
//				WeakReference<WebView> mWebViewHeaderRef4 = new WeakReference<WebView>((WebView)layout.findViewById(R.id.my_header));
 				WeakReference<WebView> mWebViewRef4 = new WeakReference<WebView>((WebView)layout.findViewById(R.id.my_webview));
 				//mWebView = (WebView)layout.findViewById(R.id.my_webview);
//				
//				mWebViewHeaderRef4.get().getSettings().setJavaScriptEnabled(false);
//				WebSettings mWebViewHeaderRef4WebSettings = mWebViewHeaderRef4.get().getSettings();
//				mWebViewHeaderRef4WebSettings.setJavaScriptEnabled(false);
//				mWebViewHeaderRef4.get().requestFocusFromTouch();
//				mWebViewHeaderRef4.get().setWebViewClient(new WebViewClient());
//		        mWebViewHeaderRef4.get().setWebChromeClient(new WebChromeClient());
//		        
 				
 				mWebViewRef4.get().getSettings().setJavaScriptEnabled(false);
 				WebSettings webSettings4 = mWebViewRef4.get().getSettings();
 		        
 				webSettings4.setJavaScriptEnabled(false);
 		        webSettings4.setBuiltInZoomControls(true);
 		        mWebViewRef4.get().requestFocusFromTouch();
 		        
 		        mWebViewRef4.get().setWebViewClient(new WebViewClient()
 		        {
 		    		public boolean shouldOverrideUrlLoading(WebView view, String url){
 		    			if (url.startsWith("http:") || url.startsWith("https:")){
 		    				return false;
 		    			}
 		    			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 		    			startActivity(intent);
 		    			return true;
 		    		}
 	    		public void onScaleChanged(WebView view, float oldScale, float newScale)
 		    		{
 	    			}
 
 		        });
 		        mWebViewRef4.get().setWebChromeClient(new WebChromeClient());
 				
		  //      mWebViewHeaderRef4.get().loadUrl("file:///android_asset/medical_header_spanish.png");
 		        mWebViewRef4.get().loadUrl("file:///android_asset/medical" + getString(R.string.assetLocation) +".html");
 				((ViewPager)collection).addView((View)layout, 0);
 				break;
 		
 			}
 			return layout;
 		}
 		
 		//taken out b/c it is too slow between gestures, left in b/c it is nifty
 		/*private String readTextFromResource(int resourceID)
 		{
 			InputStream raw = getResources().openRawResource(resourceID);
 			ByteArrayOutputStream stream  = new ByteArrayOutputStream();
 			int i;
 			try
 			{
 				i=raw.read();
 				while (i != -1)
 				{
 					stream.write(i);
 					i = raw.read();
 					
 				}
 				raw.close();
 			}
 			catch(IOException e)
 			{
 				e.printStackTrace();
 			}
 			return stream.toString();
 		}
 */		
 		@Override
         public void finishUpdate(ViewGroup arg0) {}
 
         @Override
         public void restoreState(Parcelable arg0, ClassLoader arg1) {}
 
         @Override
         public Parcelable saveState() {
                 return null;
         }
 
         @Override
         public void startUpdate(ViewGroup arg0) {}
 
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.activity_main, menu);
 		//MenuItemCompat.setShowAsAction(menu.findItem(R.menu.options), 1);
 		return true;
 	}
 }
