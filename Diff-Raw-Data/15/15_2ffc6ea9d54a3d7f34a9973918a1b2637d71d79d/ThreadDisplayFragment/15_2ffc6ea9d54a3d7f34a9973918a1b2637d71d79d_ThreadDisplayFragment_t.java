 /********************************************************************************
  * Copyright (c) 2011, Scott Ferguson
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the software nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 
 package com.ferg.awfulapp;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.PluginState;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.ShareActionProvider;
 import com.ferg.awfulapp.constants.Constants;
 import com.ferg.awfulapp.preferences.AwfulPreferences;
 import com.ferg.awfulapp.preferences.ColorPickerPreference;
 import com.ferg.awfulapp.provider.AwfulProvider;
 import com.ferg.awfulapp.service.AwfulCursorAdapter;
 import com.ferg.awfulapp.service.AwfulSyncService;
 import com.ferg.awfulapp.thread.AwfulMessage;
 import com.ferg.awfulapp.thread.AwfulPagedItem;
 import com.ferg.awfulapp.thread.AwfulPost;
 import com.ferg.awfulapp.thread.AwfulThread;
 import com.ferg.awfulapp.widget.NumberPicker;
 
 /**
  * Uses intent extras:
  *  TYPE - STRING ID - DESCRIPTION
  *	int - Constants.THREAD_ID - id number for that thread
  *	int - Constants.THREAD_PAGE - page number to load
  *
  *  Can also handle an HTTP intent that refers to an SA showthread.php? url.
  */
 public class ThreadDisplayFragment extends AwfulFragment implements AwfulUpdateCallback {
     private static final String TAG = "ThreadDisplayFragment";
     private boolean DEBUG = false;
 
     private PostLoaderManager mPostLoaderCallback;
     private ThreadDataCallback mThreadLoaderCallback;
 
     private ImageButton mToggleSidebar;
 	private boolean mShowSidebarIcon;
     
     private ImageButton mNextPage;
     private ImageButton mPrevPage;
     private ImageButton mRefreshBar;
     private View mPageBar;
     private TextView mPageCountText;
     private ViewGroup mThreadWindow;
     
     private String mActionModeURL;
 
     private WebView mThreadView;
     
     private ListView mThreadListView;
     private AwfulCursorAdapter mCursorAdapter;
 
     private int mThreadId = 0;
     private int mUserId = 0;
     private int mPage = 1;
     private int mLastPage = 0;
     private int mParentForumId = 0;
     private int mReplyDraftSaved = 0;
     private String mDraftTimestamp = null;
     private boolean threadClosed = false;
     private boolean threadBookmarked = false;
     private boolean dataLoaded = false;
     
     //oh god i'm replicating core android functionality, this is a bad sign.
     private LinkedList<AwfulStackEntry> backStack = new LinkedList<AwfulStackEntry>();
     
     private int scrollCheckMinBound = -1;
     private int scrollCheckMaxBound = -1;
     private int[] scrollCheckBounds = null;
     
     private static final int buttonSelectedColor = 0x8033b5e5;//0xa0ff7f00;
     
     private String mTitle = null;
     
 	private String mPostJump = "";
 	private int savedPage = 0;//for reverting from "Find posts by"
 	private int savedScrollPosition = 0;
 	
 	private ShareActionProvider shareProvider;
 	
 	private Handler buttonHandler = new Handler(){
 
 		@Override
 		public void handleMessage(Message msg) {
 			Log.i(TAG, "POST BUTTON HIT "+msg.arg1+" - "+msg.what);
 			switch(msg.what){
 			case R.id.post_quote_button:
 				clickInterface.onQuoteClickInt(msg.arg1);
 				break;
 			case R.id.post_edit_button:
 				clickInterface.onEditClickInt(msg.arg1);
 				break;
 			case R.id.post_last_read:
 				clickInterface.onLastReadClickInt(mCursorAdapter.getInt(msg.arg1, AwfulPost.POST_INDEX));
 				break;
 			case R.id.post_copyurl_button:
 				copyThreadURL(Integer.toString(msg.arg1));
 				break;
 			case R.id.post_userposts_button:
 				clickInterface.onUserPostsClickInt(mCursorAdapter.getInt(msg.arg1, AwfulPost.USER_ID));
 				break;
 			}
 		}
 	};
 	private Messenger buttonCallback = new Messenger(buttonHandler);
 	
 	public static ThreadDisplayFragment newInstance(int id, int page) {
 		ThreadDisplayFragment fragment = new ThreadDisplayFragment();
 		Bundle args = new Bundle();
 		args.putInt(Constants.THREAD_ID, id);
 		args.putInt(Constants.THREAD_PAGE, page);
 		fragment.setArguments(args);
 
         return fragment;
 	}
 
     private ThreadContentObserver mThreadObserver = new ThreadContentObserver(mHandler);
 
     
 	
 	private WebViewClient callback = new WebViewClient(){
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			Log.i(TAG,"PageFinished");
 			setProgress(100);
 			registerPreBlocks();
 		}
 
 		public void onLoadResource(WebView view, String url) {
 			Log.i(TAG,"onLoadResource: "+url);
 		}
 
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView aView, String aUrl) {
 			if(aUrl.contains("http://next.next")){
 				goToPage(mPage+1);
 				return true;
 			}
 			if(aUrl.contains("http://refresh.refresh")){
 				refresh();
 				return true;
 			}
 			Uri link = Uri.parse(aUrl);
 			if(aUrl.contains(Constants.FUNCTION_THREAD)){
 				//for the new quote-link stuff
 				//http://forums.somethingawful.com/showthread.php?goto=post&postid=XXXX
 				if(link.getQueryParameter(Constants.PARAM_GOTO) != null 
 					&& link.getQueryParameter(Constants.PARAM_POST_ID) != null ){
 					startPostRedirect(aUrl);
 					return true;
 				}
 				//http://forums.somethingawful.com/showthread.php?action=showpost&postid=XXXX
 				//but seriously, who uses that function? it doesn't even show up anymore.
 				if(link.getQueryParameter(Constants.PARAM_ACTION) != null 
 						&& link.getQueryParameter(Constants.PARAM_POST_ID) != null ){
 					startPostRedirect(aUrl.replace("action=showpost", "goto=post"));
 					return true;
 				}
 				if(link.getQueryParameter(Constants.PARAM_THREAD_ID) != null){
 					String threadId = link.getQueryParameter(Constants.PARAM_THREAD_ID);
 					String pageNum = link.getQueryParameter(Constants.PARAM_PAGE);
 					if(pageNum != null && pageNum.matches("\\d+")){
 						int pageNumber = Integer.parseInt(pageNum.replaceAll("\\D", ""));
 						int perPage = Constants.ITEMS_PER_PAGE;
 						String paramPerPage = link.getQueryParameter(Constants.PARAM_PER_PAGE);
 						if(paramPerPage != null && paramPerPage.matches("\\d+")){
 							perPage = Integer.parseInt(paramPerPage.replaceAll("\\D", ""));
 						}
 						if(perPage != mPrefs.postPerPage){
 							pageNumber = (int) Math.ceil((double)(pageNumber*perPage) / mPrefs.postPerPage);
 						}
 						pushThread(Integer.parseInt(threadId.replaceAll("\\D", "")), pageNumber, "");
 					}else{
 						pushThread(Integer.parseInt(threadId.replaceAll("\\D", "")), 1, "");
 					}
 					return true;
 				}
 			}
 			if(aUrl.contains(Constants.FUNCTION_FORUM)){
 				if(link.getQueryParameter(Constants.PARAM_FORUM_ID) != null){
 					String forumId = link.getQueryParameter(Constants.PARAM_FORUM_ID);
 					String pageNum = link.getQueryParameter(Constants.PARAM_PAGE);
 					if(pageNum != null && pageNum.matches("\\d+")){
 						displayForum(Integer.parseInt(forumId.replaceAll("\\D", "")), Integer.parseInt(pageNum.replaceAll("\\D", "")));
 					}else{
 						displayForum(Integer.parseInt(forumId.replaceAll("\\D", "")), 1);
 					}
 					return true;
 				}
 			}
 			mActionModeURL = aUrl;
 			startActionMode();
 			return true;
 		}
 	};
 
     @Override
     public void onAttach(Activity aActivity) {
         super.onAttach(aActivity); Log.e(TAG, "onAttach");
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState); Log.e(TAG, "onCreate");
         setHasOptionsMenu(true);
         //setRetainInstance(true);
         DEBUG = mPrefs.debugMode;
         Bundle args = getArguments();
         if(args != null){
 	        mThreadId = args.getInt(Constants.THREAD_ID, 0);
 	        mPage = args.getInt(Constants.THREAD_PAGE, 1);
         }
         
         String c2pThreadID = null;
         String c2pPostPerPage = null;
         String c2pPage = null;
         String c2pURLFragment = null;
         Intent data = getActivity().getIntent();
         // We may be getting thread info from a link or Chrome2Phone so handle that here
         if (data.getData() != null && data.getScheme().equals("http")) {
             c2pThreadID = data.getData().getQueryParameter("threadid");
             c2pPostPerPage = data.getData().getQueryParameter("perpage");
             c2pPage = data.getData().getQueryParameter("pagenumber");
             c2pURLFragment = data.getData().getEncodedFragment();
         }
         if(mThreadId < 1){
 	        mThreadId = data.getIntExtra(Constants.THREAD_ID, mThreadId);
 	        mPage = data.getIntExtra(Constants.THREAD_PAGE, mPage);
 	        if (c2pThreadID != null) {
 	        	mThreadId = Integer.parseInt(c2pThreadID);
 	        }
 	        if (c2pPage != null) {
 	        	int page = Integer.parseInt(c2pPage);
 	
 	        	if (c2pPostPerPage != null && c2pPostPerPage.matches("\\d+")) {
 	        		int ppp = Integer.parseInt(c2pPostPerPage);
 	
 	        		if (mPrefs.postPerPage != ppp) {
 	        			page = (int) Math.ceil((double)(page*ppp) / mPrefs.postPerPage);
 	        		}
 	        	} else {
 	        		if (mPrefs.postPerPage != Constants.ITEMS_PER_PAGE) {
 	        			page = (int) Math.ceil((page*Constants.ITEMS_PER_PAGE)/(double)mPrefs.postPerPage);
 	        		}
 	        	}
 	        	mPage = page;
 	        	if (c2pURLFragment != null && c2pURLFragment.startsWith("post")) {
 	        		setPostJump(c2pURLFragment.replaceAll("\\D", ""));
 	        	}
 	        }
         }
         if (savedInstanceState != null) {
         	Log.e(TAG, "onCreate savedState");
             mThreadId = savedInstanceState.getInt(Constants.THREAD_ID, mThreadId);
     		mPage = savedInstanceState.getInt(Constants.THREAD_PAGE, mPage);
     		savedScrollPosition = savedInstanceState.getInt("scroll_position", 0);
         }
         
         mPostLoaderCallback = new PostLoaderManager();
         mThreadLoaderCallback = new ThreadDataCallback();
         
         if(getThreadId() > 0 && savedScrollPosition < 1){
         	syncThread();
         }
     }
 //--------------------------------
     @Override
     public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedState) {
     	if(DEBUG) Log.e(TAG, "onCreateView");
     	
         View result = inflateView(R.layout.thread_display, aContainer, aInflater);
 
 
 		mPageCountText = aq.find(R.id.page_count).clicked(onButtonClick).getTextView();
 		getAwfulActivity().setPreferredFont(mPageCountText);
 		mToggleSidebar = (ImageButton) aq.find(R.id.toggle_sidebar).clicked(onButtonClick).getView();
 		mNextPage = (ImageButton) aq.find(R.id.next_page).clicked(onButtonClick).getView();
 		mPrevPage = (ImageButton) aq.find(R.id.prev_page).clicked(onButtonClick).getView();
         mRefreshBar  = (ImageButton) aq.find(R.id.refresh).clicked(onButtonClick).getView();
 		mPageBar = result.findViewById(R.id.page_indicator);
 		mThreadWindow = (FrameLayout) result.findViewById(R.id.thread_window);
 		mThreadWindow.setBackgroundColor(mPrefs.postBackgroundColor);
 		return result;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle aSavedState) {
 		super.onActivityCreated(aSavedState); Log.e(TAG, "onActivityCreated");
         if(dataLoaded || savedScrollPosition > 0){
         	refreshPosts();
         }
         updateSidebarHint(isDualPane(), isSidebarVisible());
 		updatePageBar();
 	}
 
 	private void initThreadViewProperties() {
 		mThreadView.resumeTimers();
 		mThreadView.setWebViewClient(callback);
 		mThreadView.setBackgroundColor(mPrefs.postBackgroundColor);
 		mThreadView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 		mThreadView.setDrawingCacheEnabled(false);//TODO maybe
 		mThreadView.getSettings().setJavaScriptEnabled(true);
         mThreadView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
         if(mPrefs.inlineYoutube && Constants.isFroyo()){//YOUTUBE SUPPORT BLOWS
         	mThreadView.getSettings().setPluginState(PluginState.ON_DEMAND);
         }
 
 		if (Constants.isHoneycomb()) {
 			mThreadView.getSettings().setEnableSmoothTransition(true);
 			if(!mPrefs.inlineYoutube){
 				mThreadView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
 			}
 		}
 
 		mThreadView.setWebChromeClient(new WebChromeClient() {
 			public void onConsoleMessage(String message, int lineNumber, String sourceID) {
 				if(DEBUG) Log.d("Web Console", message + " -- From line " + lineNumber + " of " + sourceID);
 			}
 
 			@Override
 			public void onCloseWindow(WebView window) {
 				super.onCloseWindow(window);
 				if(DEBUG) Log.e(TAG,"onCloseWindow");
 			}
 
 			@Override
 			public boolean onCreateWindow(WebView view, boolean isDialog,
 					boolean isUserGesture, Message resultMsg) {
 				if(DEBUG) Log.e(TAG,"onCreateWindow"+(isDialog?" isDialog":"")+(isUserGesture?" isUserGesture":""));
 				return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
 			}
 
 			@Override
 			public boolean onJsTimeout() {
 				if(DEBUG) Log.e(TAG,"onJsTimeout");
 				return super.onJsTimeout();
 			}
 
 			@Override
 			public void onProgressChanged(WebView view, int newProgress) {
 				super.onProgressChanged(view, newProgress);
 				if(DEBUG) Log.e(TAG,"onProgressChanged: "+newProgress);
 				setProgress(newProgress/2+50);//second half of progress bar
 			}
 			
 		});
 	}
 	
 	public void updatePageBar(){
 		mPageCountText.setText("Page " + getPage() + "/" + (getLastPage()>0?getLastPage():"?"));
 		if(getActivity() != null){
 			getAwfulActivity().invalidateOptionsMenu();
 		}
 		mRefreshBar.setVisibility(View.VISIBLE);
 		mPrevPage.setVisibility(View.VISIBLE);
 		mNextPage.setVisibility(View.VISIBLE);
 		if (getPage() <= 1) {
 			mPrevPage.setImageResource(R.drawable.ic_actionbar_load);
 			mPrevPage.setVisibility(View.VISIBLE);
 			mRefreshBar.setVisibility(View.INVISIBLE);
 		} else {
 			mPrevPage.setImageResource(R.drawable.ic_menu_arrowleft);
 		}
 
 		if (getPage() == getLastPage()) {
 			mNextPage.setImageResource(R.drawable.ic_actionbar_load);
 			mRefreshBar.setVisibility(View.INVISIBLE);
 		} else {
 			mNextPage.setImageResource(R.drawable.ic_menu_arrowright);
 		}
 	}
 
     @Override
     public void onStart() {
         super.onStart(); if(DEBUG) Log.e(TAG, "onStart");
         //recreate that fucking webview if we don't have it yet
 		if(!mPrefs.staticThreadView && mThreadView == null){
 	        mThreadView = new WebView(getActivity());
 	        mThreadView.setId(R.id.thread);
 	        initThreadViewProperties();
 	        mThreadWindow.removeAllViews();
 	        mThreadListView = null;
 	        mCursorAdapter = null;
 	        mThreadWindow.addView(mThreadView, new ViewGroup.LayoutParams(
 	                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
 	    	refreshPosts();
 		}
 		if(mPrefs.staticThreadView && mThreadListView == null){
 			mThreadListView = new ListView(getActivity());
 			mThreadListView.setBackgroundColor(mPrefs.postBackgroundColor);
 			mThreadListView.setCacheColorHint(mPrefs.postBackgroundColor);
 			mCursorAdapter = new AwfulCursorAdapter(getAwfulActivity(), null, buttonCallback);
 			mThreadListView.setAdapter(mCursorAdapter);
 	        mThreadWindow.removeAllViews();
 	        if(mThreadView != null){
 	        	mThreadView.destroy();
 	        	mThreadView = null;
 	        }
 	        mThreadWindow.addView(mThreadListView,new ViewGroup.LayoutParams(
 	        			ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
             refreshPosts();
     	}
     }
     
 
     @Override
     public void onResume() {
         super.onResume(); if(DEBUG) Log.e(TAG, "Resume");
         resumeWebView();
         getActivity().getContentResolver().registerContentObserver(AwfulThread.CONTENT_URI, true, mThreadObserver);
         refreshInfo();
     }
     
     public void resumeWebView(){
     	if(getActivity() != null && !mPrefs.staticThreadView){
 	        if (mThreadView == null) {
 	            mThreadView = new WebView(getActivity());
 	            mThreadView.setId(R.id.thread);
 	            initThreadViewProperties();
 	            mThreadWindow.removeAllViews();
 	            mThreadWindow.addView(mThreadView, new ViewGroup.LayoutParams(
 	                        ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
 	        }else{
 	            mThreadView.onResume();
 	            mThreadView.resumeTimers();
 	        }
     	}
     }
     
 	@Override
 	public void onPageVisible() {
 		if(mPrefs != null && !mPrefs.staticThreadView){
 			resumeWebView();
 		}
 	}
 	
 	
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		if(mThreadView != null && dataLoaded){
 			if(currentProgress > 99){
 				registerPreBlocks();
 			}
 			updateLayoutType();
 		}
 	}
 
 	@Override
 	public void onPageHidden() {
 		if(mPrefs != null && !mPrefs.staticThreadView){
 			pauseWebView();
 		}
 	}
 	
     @Override
     public void onPause() {
         super.onPause(); if(DEBUG) Log.e(TAG, "onPause");
         getActivity().getContentResolver().unregisterContentObserver(mThreadObserver);
         getLoaderManager().destroyLoader(Constants.THREAD_INFO_LOADER_ID);
         pauseWebView();
     }
 
     private void pauseWebView(){
         if (mThreadView != null) {
         	mThreadView.pauseTimers();
         	mThreadView.onPause();
         }
     }
         
     @Override
     public void onStop() {
         super.onStop(); if(DEBUG) Log.e(TAG, "onStop");
         if (mThreadView != null && !Constants.isICS()) {
         	//SALT THE FUCKING EARTH
         	mThreadView.stopLoading();
         	savedScrollPosition = mThreadView.getScrollY();
         	mThreadWindow.removeAllViews();
         	mThreadView.destroy();
         	mThreadView = null;
         }
     }
     
     @Override
     public void onDestroyView(){
     	super.onDestroyView(); if(DEBUG) Log.e(TAG, "onDestroyView");
     	if(mThreadView != null){
 	        try {
 	            mThreadWindow.removeView(mThreadView);
 	            mThreadView.destroy();
 	            mThreadView = null;
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
     	}
     }
     @Override
     public void onDestroy() {
         super.onDestroy(); if(DEBUG) Log.e(TAG, "onDestroy");
         getLoaderManager().destroyLoader(Constants.POST_LOADER_ID);
     }
 
     @Override
     public void onDetach() {
         super.onDetach(); if(DEBUG) Log.e(TAG, "onDetach");
     }
     
     public boolean isDualPane(){
     	return (getActivity() != null && getActivity() instanceof ThreadDisplayActivity && ((ThreadDisplayActivity)getActivity()).isDualPane());
     }
     
     public boolean isSidebarVisible(){
     	return (getActivity() != null && getActivity() instanceof ThreadDisplayActivity && ((ThreadDisplayActivity)getActivity()).isSidebarVisible());
     }
     
     public void updateLayoutType(){
     	if(mThreadView != null && getActivity() != null){
 			if(Constants.isWidescreen(getActivity())){
 				mThreadView.loadUrl("javascript:showTabletUI()");
 			}else{
 				mThreadView.loadUrl("javascript:showPhoneUI()");
 			}
     	}
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) { 
     	if(DEBUG) Log.e(TAG, "onCreateOptionsMenu");
     	if(menu.size() == 0){
     		inflater.inflate(R.menu.post_menu, menu);
         	MenuItem share = menu.findItem(R.id.share_thread);
         	if(share != null && share.getActionProvider() instanceof ShareActionProvider){
         		shareProvider = (ShareActionProvider) share.getActionProvider();
         		shareProvider.setShareIntent(createShareIntent());
         	}
     	}
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
     	if(DEBUG) Log.e(TAG, "onCreateOptionsMenu");
         if(menu == null){
             return;
         }
         MenuItem nextArrow = menu.findItem(R.id.next_page);
         if(nextArrow != null){
         	nextArrow.setVisible(mPrefs.upperNextArrow);
         }
         MenuItem bk = menu.findItem(R.id.bookmark);
         if(bk != null){
         	bk.setTitle((threadBookmarked? getString(R.string.unbookmark):getString(R.string.bookmark)));
         }
         MenuItem re = menu.findItem(R.id.reply);
         if(re != null){
         	re.setEnabled(!threadClosed);
         	if(threadClosed){
         		re.setTitle("Thread Locked");
         	}else{
         		re.setTitle(R.string.post_reply);
         	}
         }
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case R.id.next_page:
             	goToPage(getPage() + 1);
                 break;
             case R.id.reply:
                 displayPostReplyDialog();
                 break;
             case R.id.usercp:
                 displayUserCP();
                 break;
             case R.id.go_to:
                 displayPagePicker();
                 break;
             case R.id.refresh:
                 refresh();
                 break;
             case R.id.settings:
                 startActivity(new Intent().setClass(getActivity(), SettingsActivity.class));
                 break;
             case R.id.bookmark:
             	toggleThreadBookmark();
                 break;
     		case R.id.rate_thread:
     			rateThread();
     			break;
     		case R.id.copy_url:
     			copyThreadURL(null);
     			break;
     		//case R.id.find://TODO oops, broke this
     		//	this.mThreadView.showFindDialog(null, true);
     		//	break;
     		default:
     			return super.onOptionsItemSelected(item);
     		}
 
     		return true;
     	}
     
     private String generateThreadUrl(String postId){
     	StringBuffer url = new StringBuffer();
 		url.append(Constants.FUNCTION_THREAD);
 		url.append("?");
 		url.append(Constants.PARAM_THREAD_ID);
 		url.append("=");
 		url.append(getThreadId());
 		url.append("&");
 		url.append(Constants.PARAM_PAGE);
 		url.append("=");
 		url.append(getPage());
 		url.append("&");
 		url.append(Constants.PARAM_PER_PAGE);
 		url.append("=");
 		url.append(mPrefs.postPerPage);
 		if (postId != null) {
 			url.append("#");
 			url.append("post");
 			url.append(postId);
 		}
 		return url.toString();
     }
     
     private Intent createShareIntent(){
     	return new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT, mTitle).putExtra(Intent.EXTRA_TEXT, generateThreadUrl(null));
     }
 
 	private void copyThreadURL(String postId) {
 		String url = generateThreadUrl(postId);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			ClipboardManager clipboard = (ClipboardManager) this.getActivity().getSystemService(
 					Context.CLIPBOARD_SERVICE);
 			ClipData clip = ClipData.newPlainText(this.getText(R.string.copy_url).toString() + this.mPage, url);
 			clipboard.setPrimaryClip(clip);
 
 			Toast.makeText(this.getActivity().getApplicationContext(), getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
 		} else {
 			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) this.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 			clipboard.setText(url);
 			Toast.makeText(this.getActivity().getApplicationContext(), getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
 		}
 	}
 
     	private void rateThread() {
 
     		final CharSequence[] items = { "1", "2", "3", "4", "5" };
 
     		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
     		builder.setTitle("Rate this thread");
     		builder.setItems(items, new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int item) {
     				if (getActivity() != null) {
     					getAwfulActivity().sendMessage(mMessenger, AwfulSyncService.MSG_VOTE, getThreadId(), item);
     				}
     			}
     		});
     		AlertDialog alert = builder.create();
     		alert.show();
     	}
 
     
     @Override
     public void onSaveInstanceState(Bundle outState){
     	super.onSaveInstanceState(outState);
     	if(DEBUG) Log.v(TAG,"onSaveInstanceState");
         outState.putInt(Constants.THREAD_PAGE, getPage());
     	outState.putInt(Constants.THREAD_ID, getThreadId());
     	if(mThreadView != null){
     		outState.putInt("scroll_position", mThreadView.getScrollY());
     	}
     	if(mThreadListView != null){
     		outState.putInt("scroll_position", mThreadListView.getScrollY());
     	}
     }
     
     private void syncThread() {
         if(getActivity() != null){
         	dataLoaded = false;
         	getAwfulActivity().sendMessage(mMessenger, AwfulSyncService.MSG_SYNC_THREAD, getThreadId(), getPage(), Integer.valueOf(mUserId));
         }
     }
     
     private void markLastRead(int index) {
         if(getActivity() != null){
         	getAwfulActivity().sendMessage(mMessenger, AwfulSyncService.MSG_MARK_LASTREAD,getThreadId(),index);
         }
     }
 
     private void toggleThreadBookmark() {
         if(getActivity() != null){
         	getAwfulActivity().sendMessage(mMessenger, AwfulSyncService.MSG_SET_BOOKMARK,getThreadId(),(threadBookmarked?0:1));
         }
     }
     
     private void startPostRedirect(String postUrl) {
         if(getActivity() != null){
         	getAwfulActivity().sendMessage(mMessenger, AwfulSyncService.MSG_TRANSLATE_REDIRECT, getThreadId(), 0, postUrl);
         }
     }
 
     private void displayUserCP() {
     	getAwfulActivity().displayForum(Constants.USERCP_ID, 1);
     }
 
     private void displayPagePicker() {
         final NumberPicker jumpToText = new NumberPicker(getActivity());
 
          
         jumpToText.setRange(1, getLastPage());
         jumpToText.setCurrent(getPage());
         new AlertDialog.Builder(getActivity())
             .setTitle("Jump to Page")
             .setView(jumpToText)
             .setPositiveButton("OK",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface aDialog, int aWhich) {
                         try {
                             int pageInt = jumpToText.getCurrent();
                             if (pageInt > 0 && pageInt <= getLastPage()) {
                                 goToPage(pageInt);
                             }
                         } catch (NumberFormatException e) {
                             Toast.makeText(getActivity(),
                                 R.string.invalid_page, Toast.LENGTH_SHORT).show();
                         } catch (Exception e) {
                             Log.d(TAG, e.toString());
                         }
                     }
                 })
             .setNegativeButton("Cancel", null)
             .show();
         
     }
     
     @Override
     public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
     	Log.e(TAG,"onActivityResult: " + aRequestCode+" result: "+aResultCode);
         // If we're here because of a post result, refresh the thread
         switch (aRequestCode) {
             case PostReplyFragment.REQUEST_POST:
             	if(aResultCode == PostReplyFragment.RESULT_POSTED){
             		startPostRedirect(Constants.FUNCTION_THREAD+"?goto=lastpost&threadid="+getThreadId()+"&perpage="+mPrefs.postPerPage);
             	}else if(aResultCode > 100){//any result >100 it is a post id we edited
             		startPostRedirect(Constants.FUNCTION_THREAD+"?goto=post&postid="+aResultCode+"&perpage="+mPrefs.postPerPage);
             	}
                 break;
         }
     }
 
     public void refresh() {
     	if(mThreadView != null){
     		mThreadView.loadData(getBlankPage(), "text/html", "utf-8");
     	}
         syncThread();
     }
 
     private View.OnClickListener onButtonClick = new View.OnClickListener() {
         public void onClick(View aView) {
             switch (aView.getId()) {
                 case R.id.next_page:
             		if (getPage() == getLastPage()) {
             			refresh();
             		} else {
                     	goToPage(getPage() + 1);
             		}
                     break;
                 case R.id.prev_page:
                 	if (getPage() <= 1) {
             			refresh();
             		} else {
                     	goToPage(getPage() - 1);
             		}
                     break;
                 case R.id.reply:
                     displayPostReplyDialog();
                     break;
                 case R.id.refresh:
                 	refresh();
                     break;
                 case R.id.page_count:
                 	displayPagePicker();
                 	break;
                 case R.id.toggle_sidebar:
                 	if(mShowSidebarIcon){
 	                	if(getActivity() != null && getActivity() instanceof ThreadDisplayActivity){
 	                		((ThreadDisplayActivity)getActivity()).toggleSidebar();
 	                	}
                 	}else{
                 		if (getPage() == getLastPage()) {
                 			refresh();
                 		} else {
                         	goToPage(getPage() + 1);
                 		}
                 	}
                 	break;
             }
         }
     };
 
     public void displayPostReplyDialog() {
 
         if(mReplyDraftSaved >0){
         	displayDraftAlert(mReplyDraftSaved, mDraftTimestamp, mThreadId, -1, AwfulMessage.TYPE_NEW_REPLY);
         }else{
             displayPostReplyDialog(mThreadId, -1, AwfulMessage.TYPE_NEW_REPLY);
         }
     }
     
     private void displayDraftAlert(final int replyType, String timeStamp, final  int threadId, final int postId, final int newType) {
     	TextView draftAlertMsg = new TextView(getActivity());
     	if(timeStamp != null){
     	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
     	    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
     	    try {
 				Date d = sdf.parse(timeStamp);
 				java.text.DateFormat df = DateFormat.getDateFormat(getActivity());
 				df.setTimeZone(TimeZone.getDefault());
 				timeStamp = df.format(d);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
     	}
     	switch(replyType){
     	case AwfulMessage.TYPE_EDIT:
         	draftAlertMsg.setText("Unsent Edit Found"+(timeStamp != null ? " from "+timeStamp : ""));
     		break;
     	case AwfulMessage.TYPE_QUOTE:
         	draftAlertMsg.setText("Unsent Quote Found"+(timeStamp != null ? " from "+timeStamp : ""));
     		break;
     	case AwfulMessage.TYPE_NEW_REPLY:
         	draftAlertMsg.setText("Unsent Reply Found"+(timeStamp != null ? " from "+timeStamp : ""));
     		break;
     	}
         new AlertDialog.Builder(getActivity())
             .setTitle("Draft Found")
             .setView(draftAlertMsg)
             .setPositiveButton(R.string.draft_alert_keep,
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface aDialog, int aWhich) {
                         displayPostReplyDialog(threadId, postId, replyType);
                     }
                 })
             .setNegativeButton(R.string.draft_alert_discard, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface aDialog, int aWhich) {
                     ContentResolver cr = getActivity().getContentResolver();
                     cr.delete(AwfulMessage.CONTENT_URI_REPLY, AwfulMessage.ID+"=?", AwfulProvider.int2StrArray(mThreadId));
                     displayPostReplyDialog(threadId, postId, newType);
                 }
             }).setNeutralButton(R.string.draft_discard_only,  new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface aDialog, int aWhich) {
                     ContentResolver cr = getActivity().getContentResolver();
                     cr.delete(AwfulMessage.CONTENT_URI_REPLY, AwfulMessage.ID+"=?", AwfulProvider.int2StrArray(mThreadId));
                     mReplyDraftSaved = 0;
                 }
             })
             .show();
         
     }
 
     @Override
     public void loadingFailed(Message aMsg) {
     	super.loadingFailed(aMsg);
         Toast.makeText(getActivity(), "Loading Failed!", Toast.LENGTH_LONG).show();
         
     	switch (aMsg.what) {
 	        case AwfulSyncService.MSG_SYNC_THREAD:
 	        	refreshPosts();
 	            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO){
 	    			mNextPage.setColorFilter(0);
 	    			mPrevPage.setColorFilter(0);
 	    			mRefreshBar.setColorFilter(0);
 	            }
 	            break;
 	        case AwfulSyncService.MSG_SET_BOOKMARK:
 	        	refreshInfo();
 	            break;
 	        case AwfulSyncService.MSG_MARK_LASTREAD:
 	        	refreshInfo();
 	            refreshPosts();
 	            break;
 	        default:
 	        	Log.e(TAG,"Message not handled: "+aMsg.what);
 	        	break;
     	}
     }
 
     @Override
     public void loadingStarted(Message aMsg) {
     	super.loadingStarted(aMsg);
     	switch(aMsg.what){
 		case AwfulSyncService.MSG_SYNC_THREAD:
 	    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO){
 	    		if(getPage() == getLastPage()){
 	    			mNextPage.setColorFilter(buttonSelectedColor);
 	    			mPrevPage.setColorFilter(0);
 	    			mRefreshBar.setColorFilter(0);
 	    		}else if(getPage() <= 1){
 	    			mPrevPage.setColorFilter(buttonSelectedColor);
 	    			mNextPage.setColorFilter(0);
 	    			mRefreshBar.setColorFilter(0);
 	    		}else{
 	    			mRefreshBar.setColorFilter(buttonSelectedColor);
 	    			mPrevPage.setColorFilter(0);
 	    			mNextPage.setColorFilter(0);
 	    		}
 	        }
 	        break;
         default:
         	Log.e(TAG,"Message not handled: "+aMsg.what);
         	break;
     	}
     }
 
     @Override
 	public void loadingUpdate(Message aMsg) {
 		super.loadingUpdate(aMsg);
     	setProgress(aMsg.arg2/2);
 	}
 
 	@Override
     public void loadingSucceeded(Message aMsg) {
     	super.loadingSucceeded(aMsg);
     	switch (aMsg.what) {
     	case AwfulSyncService.MSG_TRANSLATE_REDIRECT:
     		if(aMsg.obj instanceof String){
     			Uri resultLink = Uri.parse(aMsg.obj.toString());
     			String postJump = "";
     			if(resultLink.getFragment() != null){
     				postJump = resultLink.getFragment().replaceAll("\\D", "");
     			}
     			if(resultLink.getQueryParameter(Constants.PARAM_THREAD_ID) != null){
 					String threadId = resultLink.getQueryParameter(Constants.PARAM_THREAD_ID);
 					String pageNum = resultLink.getQueryParameter(Constants.PARAM_PAGE);
 					if(pageNum != null && pageNum.matches("\\d+")){
 						int pageNumber = Integer.parseInt(pageNum);
 						int perPage = Constants.ITEMS_PER_PAGE;
 						String paramPerPage = resultLink.getQueryParameter(Constants.PARAM_PER_PAGE);
 						if(paramPerPage != null && paramPerPage.matches("\\d+")){
 							perPage = Integer.parseInt(paramPerPage);
 						}
 						if(perPage != mPrefs.postPerPage){
 							pageNumber = (int) Math.ceil((double)(pageNumber*perPage) / mPrefs.postPerPage);
 						}
 						pushThread(Integer.parseInt(threadId), pageNumber, postJump);
 					}else{
 						pushThread(Integer.parseInt(threadId), 1, postJump);
 					}
 				}
     		}
     		break;
         case AwfulSyncService.MSG_SYNC_THREAD:
         	if(aMsg.arg2 == getPage()){
 	        	setProgress(50);
 	        	refreshPosts();
 	            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO){
 	    			mNextPage.setColorFilter(0);
 	    			mPrevPage.setColorFilter(0);
 	    			mRefreshBar.setColorFilter(0);
 	            }
         	}
             break;
         case AwfulSyncService.MSG_SET_BOOKMARK:
         	refreshInfo();
             break;
         case AwfulSyncService.MSG_MARK_LASTREAD:
         	refreshInfo();
             refreshPosts();
             break;
         default:
         	Log.e(TAG,"Message not handled: "+aMsg.what);
         	break;
     	}
     }
 
     private void populateThreadView(ArrayList<AwfulPost> aPosts) {
 		updatePageBar();
 
         try {
             mThreadView.addJavascriptInterface(clickInterface, "listener");
             mThreadView.addJavascriptInterface(getSerializedPreferences(new AwfulPreferences(getActivity())), "preferences");
             boolean useTabletLayout = mPrefs.threadLayout.equalsIgnoreCase("tablet") || 
             		(mPrefs.threadLayout.equalsIgnoreCase("auto") && Constants.isWidescreen(getActivity()));
             String html = AwfulThread.getHtml(aPosts, new AwfulPreferences(getActivity()), useTabletLayout, mPage, mLastPage, threadClosed);
            if(mPrefs.debugMode && Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)){
             	FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "awful-thread-"+mThreadId+"-"+mPage+".html"));
             	out.write(html.replaceAll("file:///android_res/", "").replaceAll("file:///android_asset/", "").getBytes());
             	out.close();
            }
             mThreadView.loadDataWithBaseURL("http://forums.somethingawful.com", html, "text/html", "utf-8", null);
         } catch (Exception e) {
         	e.printStackTrace();
             // If we've already left the activity the webview may still be working to populate,
             // just log it
         }
         Log.i(TAG,"Finished populateThreadView, posts:"+aPosts.size());
     }
 
     private String getSerializedPreferences(final AwfulPreferences aAppPrefs) {
         JSONObject result = new JSONObject();
 
         try {
             result.put("username", aAppPrefs.username);
             result.put("userQuote", "#a2cd5a");
             result.put("usernameHighlight", "#9933ff");
             result.put("youtubeHighlight", "#ff00ff");
             result.put("showSpoilers", aAppPrefs.showAllSpoilers);
             result.put("postFontSize", aAppPrefs.postFontSizePx);
             result.put("postcolor", ColorPickerPreference.convertToARGB(aAppPrefs.postFontColor));
             result.put("backgroundcolor", ColorPickerPreference.convertToARGB(aAppPrefs.postBackgroundColor));
             result.put("linkQuoteColor", ColorPickerPreference.convertToARGB(aAppPrefs.postLinkQuoteColor));
             result.put("highlightUserQuote", Boolean.toString(aAppPrefs.highlightUserQuote));
             result.put("highlightUsername", Boolean.toString(aAppPrefs.highlightUsername));
             result.put("postjumpid", mPostJump);
             result.put("scrollPosition", savedScrollPosition);
         } catch (JSONException e) {
         }
 
         return result.toString();
     }
     private ClickInterface clickInterface = new ClickInterface();
     private class ClickInterface {
         public static final int SEND_PM  = 0;
         public static final int COPY_URL = 1;
         public static final int USER_POSTS = 2;
 		
         final CharSequence[] mPostItems = {
             "Send Private Message",
             "Copy Post URL",
             "Read Posts by this User"
         };
         
         public void onQuoteClick(final String aPostId) {
         	onQuoteClickInt(Integer.parseInt(aPostId));
         }
         
         //name it differently to avoid ambiguity on the JS interface
         public void onQuoteClickInt(final int aPostId){
             if(mReplyDraftSaved >0){
             	displayDraftAlert(mReplyDraftSaved, mDraftTimestamp, mThreadId, aPostId,AwfulMessage.TYPE_QUOTE);
             }else{
                 displayPostReplyDialog(mThreadId, aPostId, AwfulMessage.TYPE_QUOTE);
             }
         }
         
         public void onLastReadClick(final String index) {
         	markLastRead(Integer.parseInt(index));
         }
         
         //name it differently to avoid ambiguity on the JS interface
         public void onLastReadClickInt(final int index) {
         	markLastRead(index);
         }
 
         public void onSendPMClick(final String aUsername) {
         	startActivity(new Intent(getActivity(), MessageDisplayActivity.class).putExtra(Constants.PARAM_USERNAME, aUsername));
         }
 
         // Post ID is the item tapped
         public void onEditClick(final String aPostId) {
         	onEditClickInt(Integer.parseInt(aPostId));
         }
 
         //name it differently to avoid ambiguity on the JS interface
         public void onEditClickInt(final int aPostId) {
             if(mReplyDraftSaved >0){
             	displayDraftAlert(mReplyDraftSaved, mDraftTimestamp, mThreadId, aPostId, AwfulMessage.TYPE_EDIT);
             }else{
                 displayPostReplyDialog(mThreadId, aPostId, AwfulMessage.TYPE_EDIT);
             }
         }
         
         public void onMoreClick(final String aPostId, final String aUsername, final String aUserId) {
         	new AlertDialog.Builder(getActivity())
             .setTitle("Select an Action")
             .setItems(mPostItems, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface aDialog, int aItem) {
                     onPostActionItemSelected(aItem, aPostId, aUsername, aUserId);
                 }
             })
             .show();
         }
         
         public void onCopyUrlClick(final String aPostId) {
         	copyThreadURL(aPostId);
         }
         
         public void debugMessage(final String msg) {
         	Log.e(TAG,"Awful DEBUG: "+msg);
         }
         
         public void onUserPostsClick(final String aUserId) {
         	onUserPostsClickInt(Integer.parseInt(aUserId));
         }
         public void onUserPostsClickInt(final int aUserId) {
         	if(mUserId >0){
         		deselectUser();
         	}else{
         		selectUser(aUserId);
         	}
         }
         
         public void addCodeBounds(final String minBound, final String maxBound){
         	int min = Integer.parseInt(minBound);
         	int max = Integer.parseInt(maxBound);
         	if(min < scrollCheckMinBound || scrollCheckMinBound < 0){
         		scrollCheckMinBound = min;
         	}
         	if(max > scrollCheckMaxBound || scrollCheckMaxBound < 0){
         		scrollCheckMaxBound = max;
         	}
         	Log.e(TAG,"Register pre block: "+min+" - "+max+" - new min: "+scrollCheckMinBound+" new max: "+scrollCheckMaxBound);
         	//this array is going to be accessed very often during touch events, arraylist has too much processing overhead
         	if(scrollCheckBounds == null){
         		scrollCheckBounds = new int[2];
         	}else{
         		//GOOGLE DIDN'T ADD Arrays.copyOf TILL API 9 fuck
         		//scrollCheckBounds = Arrays.copyOf(scrollCheckBounds, scrollCheckBounds.length+2);
         		int[] newScrollCheckBounds = new int[scrollCheckBounds.length+2];
         		for(int x = 0;x<scrollCheckBounds.length;x++){
         			newScrollCheckBounds[x]=scrollCheckBounds[x];
         		}
         		scrollCheckBounds = newScrollCheckBounds;
         	}
         	scrollCheckBounds[scrollCheckBounds.length-2] = min;
         	scrollCheckBounds[scrollCheckBounds.length-1] = max;
         	Arrays.sort(scrollCheckBounds);
         }
     }
     
 	private void onPostActionItemSelected(int aItem,
 			String aPostId, String aUsername, String aUserId) {
 		switch(aItem){
 		case ClickInterface.SEND_PM:
         	startActivity(new Intent(getActivity(), MessageDisplayActivity.class).putExtra(Constants.PARAM_USERNAME, aUsername));
             //MessageFragment.newInstance(aUsername, 0).show(getFragmentManager(), "new_private_message_dialog");
 			break;
 		case ClickInterface.COPY_URL:
         	copyThreadURL(aPostId);
 			break;
 		case ClickInterface.USER_POSTS:
 			if(mUserId >0){
         		deselectUser();
         	}else{
         		selectUser(Integer.parseInt(aUserId));
         	}
 			break;
 		}
 	}
 
 	@Override
 	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 		if(DEBUG) Log.e(TAG,"onCreateActionMode");
 		menu.add(Menu.NONE, R.id.normal, Menu.NONE, "Open");
 		menu.add(Menu.NONE, R.id.content, Menu.NONE, "Show Image");
 		menu.add(Menu.NONE, R.id.copy_url, Menu.NONE, "Copy URL");
 		return mActionModeURL != null;
 	}
 
 	@Override
 	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 		if(DEBUG) Log.e(TAG,"onPrepareActionMode");
 		MenuItem inline = menu.findItem(R.id.content);
 		if(inline != null && mActionModeURL != null){//TODO make this detection less retarded
 			Uri link = Uri.parse(mActionModeURL);
 			inline.setVisible(link.getLastPathSegment() != null 
 								&& (link.getLastPathSegment().contains(".jpg") 
 									|| link.getLastPathSegment().contains(".jpeg") 
 									|| link.getLastPathSegment().contains(".png") 
 									|| link.getLastPathSegment().contains(".gif")
 									)
 								);
 		}
 		return mActionModeURL != null;
 	}
 
 	@Override
 	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.normal:
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mActionModeURL));
 			PackageManager pacman = getActivity().getPackageManager();
 			List<ResolveInfo> res = pacman.queryIntentActivities(browserIntent,
 					PackageManager.MATCH_DEFAULT_ONLY);
 			if (res.size() > 0) {
 				browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				getActivity().startActivity(browserIntent);
 			} else {
 				String[] split = mActionModeURL.split(":");
 				Toast.makeText(
 						getActivity(),
 						"No application found for protocol" + (split.length > 0 ? ": " + split[0] : "."),
 						Toast.LENGTH_LONG)
 							.show();
 			}
 			break;
 		case R.id.content:
 			if(mThreadView != null){
 				mThreadView.loadUrl("javascript:showInlineImage('"+mActionModeURL+"')");
 			}
 			break;
 		case R.id.copy_url:
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 				ClipData clip = ClipData.newPlainText("Copied URL", mActionModeURL);
 				clipboard.setPrimaryClip(clip);
 			} else {
 				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) this.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 				clipboard.setText(mActionModeURL);
 			}
 			Toast.makeText(this.getActivity().getApplicationContext(), getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
 			break;
 		default:
 			return false;
 			//TODO reimplement internal browser.
 		}
 		mode.finish();
 		return true;
 	}
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode) {
 		mActionModeURL = null;
 	}
 	
 	@Override
 	public void onPreferenceChange(AwfulPreferences mPrefs) {
 		super.onPreferenceChange(mPrefs);
 		getAwfulActivity().setPreferredFont(mPageCountText);
 		if(mPageBar != null){
 			mPageBar.setBackgroundColor(mPrefs.actionbarColor);
 		}
 		if(mPageCountText != null){
 			mPageCountText.setTextColor(mPrefs.actionbarFontColor);
 		}
 		if(mThreadView != null){
 			mThreadView.setBackgroundColor(mPrefs.postBackgroundColor);
 		}
 		if(mThreadListView != null){
 			mThreadListView.setBackgroundColor(mPrefs.postBackgroundColor);
 			mThreadListView.setCacheColorHint(mPrefs.postBackgroundColor);
 		}
 	}
 
 	public void setPostJump(String postID) {
 		mPostJump = postID;
 	}
 	
 	public void goToPage(int aPage){
 		if(aPage > 0 && aPage <= getLastPage()){
 			setPage(aPage);
 			updatePageBar();
 			mPostJump = "";
 			if(mThreadView != null){
 				mThreadView.loadData(getBlankPage(), "text/html", "utf-8");
 			}
 	        syncThread();
 		}
 	}
 	
 	private String getBlankPage(){
 		return "<html><head></head><body style='{background-color:#"+ColorPickerPreference.convertToARGB(mPrefs.postBackgroundColor)+";'></body></html>";
 	}
 	
 	public int getPage() {
         return mPage;
 	}
 	public void setPage(int aPage){
 		mPage = aPage;
 	}
 	public void setThreadId(int aThreadId){
 		mThreadId = aThreadId;
 	}
 	
 	public void selectUser(int id){
 		savedPage = mPage;
 		mUserId = id;
 		setPage(1);
 		mLastPage = 1;
 		mPostJump = "";
 		if(mThreadView != null){
 			mThreadView.loadData(getBlankPage(), "text/html", "utf-8");
 		}
         syncThread();
 	}
 	
 	public void deselectUser(){
 		mUserId = 0;
 		setPage(savedPage);
 		mLastPage = 0;
 		mPostJump = "";
 		if(mThreadView != null){
 			mThreadView.loadData(getBlankPage(), "text/html", "utf-8");
 		}
         syncThread();
 	}
 	
 	public int getLastPage() {
         return mLastPage;
 	}
 
 	public int getThreadId() {
         return mThreadId;
 	}
 
     private class PostLoaderManager implements LoaderManager.LoaderCallbacks<Cursor> {
         private final static String sortOrder = AwfulPost.POST_INDEX + " ASC";
         private final static String selection = AwfulPost.THREAD_ID + "=? AND " + AwfulPost.POST_INDEX + ">=? AND " + AwfulPost.POST_INDEX + "<?";
         public Loader<Cursor> onCreateLoader(int aId, Bundle aArgs) {
             int index = AwfulPagedItem.pageToIndex(getPage(), mPrefs.postPerPage, 0);
             Log.v(TAG,"Displaying thread: "+getThreadId()+" index: "+index+" page: "+getPage()+" perpage: "+mPrefs.postPerPage);
             return new CursorLoader(getActivity(),
             						AwfulPost.CONTENT_URI,
             						AwfulProvider.PostProjection,
             						selection,
             						AwfulProvider.int2StrArray(getThreadId(), index, index+mPrefs.postPerPage),
             						sortOrder);
         }
 
         public void onLoadFinished(Loader<Cursor> aLoader, Cursor aData) {
         	Log.i(TAG,"Load finished, page:"+getPage()+", populating: "+aData.getCount());
         	setProgress(50);
         	if(aData.isClosed()){
         		return;
         	}
         	if(mThreadListView != null && mCursorAdapter != null){
         		mCursorAdapter.swapCursor(aData);
         		setProgress(100);
         	}
         	if(mThreadView != null){
         		populateThreadView(AwfulPost.fromCursor(getActivity(), aData));
         	}
             dataLoaded = true;
 			savedScrollPosition = 0;
         }
 
         @Override
         public void onLoaderReset(Loader<Cursor> aLoader) {
         	if(mCursorAdapter != null){
         		mCursorAdapter.swapCursor(null);
         	}
         }
     }
     
     private class ThreadDataCallback implements LoaderManager.LoaderCallbacks<Cursor> {
 
         public Loader<Cursor> onCreateLoader(int aId, Bundle aArgs) {
             return new CursorLoader(getActivity(), ContentUris.withAppendedId(AwfulThread.CONTENT_URI, getThreadId()), 
             		AwfulProvider.ThreadProjection, null, null, null);
         }
 
         public void onLoadFinished(Loader<Cursor> aLoader, Cursor aData) {
         	Log.v(TAG,"Thread title finished, populating.");
         	if(aData.getCount() >0 && aData.moveToFirst()){
         		mLastPage = AwfulPagedItem.indexToPage(aData.getInt(aData.getColumnIndex(AwfulThread.POSTCOUNT)),mPrefs.postPerPage);
         		threadClosed = aData.getInt(aData.getColumnIndex(AwfulThread.LOCKED))>0;
         		threadBookmarked = aData.getInt(aData.getColumnIndex(AwfulThread.BOOKMARKED))>0;
         		mParentForumId = aData.getInt(aData.getColumnIndex(AwfulThread.FORUM_ID));
         		setTitle(aData.getString(aData.getColumnIndex(AwfulThread.TITLE)));
         		updatePageBar();
         		mReplyDraftSaved = aData.getInt(aData.getColumnIndex(AwfulMessage.TYPE));
         		if(mReplyDraftSaved > 0){
             		mDraftTimestamp = aData.getString(aData.getColumnIndex(AwfulProvider.UPDATED_TIMESTAMP));
             		//TODO add tablet notification
         			Log.i(TAG, "DRAFT SAVED: "+mReplyDraftSaved+" at "+mDraftTimestamp);
         		}
         		if(shareProvider != null){
         			shareProvider.setShareIntent(createShareIntent());
         		}
         	}
         }
         
         @Override
         public void onLoaderReset(Loader<Cursor> aLoader) {
         }
     }
     private class ThreadContentObserver extends ContentObserver {
         public ThreadContentObserver(Handler aHandler) {
             super(aHandler);
         }
         @Override
         public void onChange (boolean selfChange){
         	if(DEBUG) Log.e(TAG,"Thread Data update.");
         	refreshInfo();
         }
     }
     
 	public void refreshInfo() {
 		if(getActivity() != null){
 			getLoaderManager().restartLoader(Constants.THREAD_INFO_LOADER_ID, null, mThreadLoaderCallback);
 		}
 	}
 	
 	public void refreshPosts(){
 		if(getActivity() != null){
 			getLoaderManager().restartLoader(Constants.POST_LOADER_ID, null, mPostLoaderCallback);
 		}
 	}
 	
 	public void setTitle(String title){
 		mTitle = title;
 		if(getActivity() != null && mTitle != null){
 			getAwfulActivity().setActionbarTitle(mTitle, this);
 		}
 	}
 	
 	public String getTitle(){
 		return mTitle;
 	}
 
 	public int getParentForumId() {
 		return mParentForumId;
 	}
 	public void openThread(int id, int page){
     	clearBackStack();
     	loadThread(id, page, "");
 	}
 	public void openThread(int id, int page, String postJump){
     	clearBackStack();
     	loadThread(id, page, postJump);
 	}
 	
 	private void loadThread(int id, int page, String postJump) {
     	if(getActivity() != null){
 	        getLoaderManager().destroyLoader(Constants.THREAD_INFO_LOADER_ID);
 	        getLoaderManager().destroyLoader(Constants.POST_LOADER_ID);
     	}
     	setThreadId(id);//if the fragment isn't attached yet, just set the values and let the lifecycle handle it
 		mUserId = 0;
     	setPage(page);
     	dataLoaded = false;
     	mLastPage = 1;
     	if(postJump != null){
     		mPostJump = postJump;
     	}else{
     		mPostJump = "";
     	}
 		updatePageBar();
     	if(getActivity() != null){
     		if(mThreadView != null){
     			mThreadView.loadData(getBlankPage(), "text/html", "utf-8");
     		}
 			refreshInfo();
 			syncThread();
     	}
 	}
 	
 	private void loadThread(AwfulStackEntry thread) {
     	if(getActivity() != null){
 	        getLoaderManager().destroyLoader(Constants.THREAD_INFO_LOADER_ID);
 	        getLoaderManager().destroyLoader(Constants.POST_LOADER_ID);
     	}
     	setThreadId(thread.id);//if the fragment isn't attached yet, just set the values and let the lifecycle handle it
 		mUserId = 0;
     	setPage(thread.page);
     	dataLoaded = false;
     	mLastPage = 1;
     	mPostJump = "";
     	savedScrollPosition = thread.scrollPos;
 		updatePageBar();
     	if(getActivity() != null){
     		if(mThreadView != null){
     			mThreadView.loadData(getBlankPage(), "text/html", "utf-8");
     		}
 			refreshInfo();
 			refreshPosts();
     	}
 	}
 	
 	private static class AwfulStackEntry{
 		public int id, page, scrollPos;
 		public AwfulStackEntry(int threadId, int pageNum, int scrollPosition){
 			id = threadId; page = pageNum; scrollPos = scrollPosition;
 		}
 	}
 	
 	private void pushThread(int id, int page, String postJump){
 		if(mThreadView != null && getThreadId() != 0){
 			backStack.addFirst(new AwfulStackEntry(getThreadId(), getPage(), mThreadView.getScrollY()));
 		}
 		if(mThreadListView != null && getThreadId() != 0){
 			backStack.addFirst(new AwfulStackEntry(getThreadId(), getPage(), mThreadListView.getScrollY()));
 		}
 		loadThread(id, page, postJump);
 	}
 	
 	private void popThread(){
 		loadThread(backStack.removeFirst());
 	}
 	
 	private void clearBackStack(){
 		backStack.clear();
 	}
 	
 	private int backStackCount(){
 		return backStack.size();
 	}
 	
 	@Override
 	public boolean onBackPressed() {
 		if(backStackCount() > 0){
 			popThread();
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	public void updateSidebarHint(boolean showIcon, boolean sidebarVisible) {
 		mShowSidebarIcon = showIcon;
 		if(mToggleSidebar != null){
 			if(mShowSidebarIcon){
 				mToggleSidebar.setVisibility(View.VISIBLE);
 				mToggleSidebar.setImageResource(R.drawable.ic_menu_sidebar);
 	    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO){
 					if(sidebarVisible){
 						mToggleSidebar.setColorFilter(buttonSelectedColor);
 					}else{
 						mToggleSidebar.setColorFilter(0);
 					}
 	    		}
 			}else{
 				mToggleSidebar.setVisibility(View.VISIBLE);
 				mToggleSidebar.setImageDrawable(null);
 			}
 		}
 	}
 	
 	private void registerPreBlocks() {
 		scrollCheckBounds = null;
 		scrollCheckMinBound = -1;
 		scrollCheckMaxBound = -1;
 		if(mThreadView != null){
 			Log.e(TAG,"Queueing registerPreBlocks()");
 			mHandler.postDelayed(new Runnable(){
 				@Override
 				public void run() {
 					if(mThreadView != null){
 						mThreadView.loadUrl("javascript:registerPreBlocks()");
 					}
 				}
 			}, 2000);
 		}
 	}
 
 	@Override
 	public boolean canScrollX(int x, int y) {
 		if(mThreadView == null || scrollCheckBounds == null){
 			return false;
 		}
 		y = y+mThreadView.getScrollY()+mThreadView.getTop();
 		if(y > scrollCheckMaxBound || y < scrollCheckMinBound){
 			return false;
 		}
 		for(int ix = 0; ix < scrollCheckBounds.length-1;ix+=2){
 			if(y > scrollCheckBounds[ix] && y < scrollCheckBounds[ix+1]){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 }
