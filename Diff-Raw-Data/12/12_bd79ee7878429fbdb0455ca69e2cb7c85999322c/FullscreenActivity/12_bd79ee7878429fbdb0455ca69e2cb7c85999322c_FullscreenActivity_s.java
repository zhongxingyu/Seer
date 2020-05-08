 /* Copyright 2012, Robert Myers */
 
 /*
  * This file is part of ComicsApp.
 
     ComicsApp is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Comics is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ComicsApp.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package com.robandjen.comicsapp;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.res.XmlResourceParser;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class FullscreenActivity extends Activity {
 
     private List<ComicsEntry> mComicList;
     private int mCurComic = 0;
     private static final String TAG = "ComicsAppWebActiviy";
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_fullscreen);
 
         if (mComicList == null) {
         	try {
         		String xmlstring = getResources().getString(R.xml.comics);
         		Log.v(TAG,xmlstring);
         		XmlResourceParser parser = getResources().getXml(R.xml.comics);
         		mComicList = ComicsParser.parse(parser);
         	}
         	catch (Exception e) {
         		//TODO: Cleanly exit, no valid XML. Shouldn't happen with embedded one
         		Log.e(TAG, "Unable to parse XML", e);
         	}
         }
         
     }
 
     void showCurrentComic() {
     	final WebView contentView = (WebView) findViewById(R.id.fullscreen_content);
     	contentView.loadUrl(mComicList.get(mCurComic).getURL());
     	
     	final TextView comicName = (TextView) findViewById(R.id.curcomic);
     	comicName.setText(mComicList.get(mCurComic).getName());
     }
     
     void nextComic() {
     	if (++mCurComic >= mComicList.size()) {
     		mCurComic = 0;
     	}
     	showCurrentComic();
     }
     
     void previousComic() {
     	if (mCurComic == 0) {
     		mCurComic = mComicList.size() - 1;
     	}
     	else {
     		--mCurComic;
     	}
     	showCurrentComic();
     }
     
 
    @Override
     protected void onResume() {
     	super.onResume();
     	
     	final ComicsWebView v = (ComicsWebView) findViewById(R.id.fullscreen_content);
     	v.setListener(new ComicsEvents() {
     		@Override 
     		public void onNextComic(View v) {
     			nextComic();
     		}
     		@Override 
     		public void onPreviousComic(View v) {
     			previousComic();
     		}
     	});
     	
     	v.setWebViewClient(new WebViewClient() {
     		@Override
     		public boolean shouldOverrideUrlLoading(WebView view,String url) {
     			return false;
     		}
     	});
     	
     	
    	v.getSettings().setBuiltInZoomControls(true);
     	
     	final Button next = (Button) findViewById(R.id.next);
     	next.setOnClickListener(new View.OnClickListener() {
     		@Override
     		public void onClick(View v) {
     			nextComic();
     		}
     	});
     	
     	final Button prev = (Button) findViewById(R.id.previous);
     	prev.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				previousComic();
 			}
 		});
     	
     	showCurrentComic();
     	
     }
     
     @Override
     protected void onStop() {
     	final ComicsWebView v = (ComicsWebView) findViewById(R.id.fullscreen_content);
     	v.setListener(null);
     	super.onStop();
     }
 
 }
