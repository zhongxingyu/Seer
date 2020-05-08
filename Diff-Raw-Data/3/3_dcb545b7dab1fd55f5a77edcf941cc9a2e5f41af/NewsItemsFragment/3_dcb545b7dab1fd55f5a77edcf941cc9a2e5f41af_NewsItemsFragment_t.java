 package com.nineducks.hereader.ui;
 
 import java.io.Serializable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager.LayoutParams;
 import android.util.Log;
 import android.view.HapticFeedbackConstants;
 import android.view.View;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 import com.markupartist.android.widget.ActionBar;
 import com.nineducks.hereader.AboutAction;
 import com.nineducks.hereader.HEReaderState;
 import com.nineducks.hereader.HackfulItemsController;
 import com.nineducks.hereader.HackfulReaderActivity;
 import com.nineducks.hereader.ItemsLoadedListener;
 import com.nineducks.hereader.LoadAskItemsAction;
 import com.nineducks.hereader.LoadFrontpageItemsAction;
 import com.nineducks.hereader.LoadNewItemsAction;
 import com.nineducks.hereader.NewsItemsLoaderTask;
 import com.nineducks.hereader.R;
 import com.nineducks.util.rss.HEMessage;
 
 public class NewsItemsFragment 
 	extends ListFragment 
 	implements ItemsLoadedListener, HackfulItemsController, 
 		AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
 		DialogInterface.OnClickListener{
 
 	public static final String SAVED_ITEMS_KEY = "saved_items";
 
 	public static final String SAVED_ITEMS_SOURCE_KEY = "saved_items_source";
 	
 	private List<ListFragmentItem> items = new ArrayList<ListFragmentItem>();
 	private boolean mDualPane = true;
 	private HEMessagesAdapter adapter = null;
 	private WebView webView = null;
 	private FrameLayout webViewContainer;
 	private int currentItemsSource;
 	private int currentPage = 1;
 	private ActionBar actionBar;
 	private int currentPosition = -1;
 	private int clickedPosition = -1;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		Log.d("hereader", "entering NewsItemsFragment.onActivityCreated");
 		super.onActivityCreated(savedInstanceState);
 		initUI();
 		getListView().setHapticFeedbackEnabled(true);
 		getListView().setLongClickable(true);
 		getListView().setOnItemClickListener(this);
 		getListView().setOnItemLongClickListener(this);
         if (savedInstanceState != null && 
             	savedInstanceState.containsKey(NewsItemsFragment.SAVED_ITEMS_KEY) &&
             	savedInstanceState.containsKey(NewsItemsFragment.SAVED_ITEMS_SOURCE_KEY)) {
             		List<ListFragmentItem> items = (List<ListFragmentItem>) savedInstanceState.getSerializable(NewsItemsFragment.SAVED_ITEMS_KEY);
             		int itemsSource = savedInstanceState.getInt(NewsItemsFragment.SAVED_ITEMS_SOURCE_KEY);
             		putItems(items, itemsSource);
         	} else {
         		if (savedInstanceState ==  null)
         			Log.d("hereader", "savend instancestate null");
             	loadFrontpageItems();
             }
 		Log.d("hereader", "leaving NewsItemsFragment.onActivityCreated");
 	}
 	
 	private void initUI() {
 		Log.d("hereader", "Creating UI");
 		webViewContainer = (FrameLayout) getActivity().findViewById(R.id.webview_container);
 		mDualPane = webViewContainer != null  && webViewContainer.getVisibility() == View.VISIBLE;
 		actionBar = (ActionBar) getActivity().findViewById(R.id.action_bar);
 		actionBar.setHomeIcon(R.drawable.hackful_icon);
 		actionBar.addAction(new LoadFrontpageItemsAction(HackfulReaderActivity.getContext(), this));
 		actionBar.addAction(new LoadNewItemsAction(HackfulReaderActivity.getContext(), this));
 		actionBar.addAction(new LoadAskItemsAction(HackfulReaderActivity.getContext(), this));
 		actionBar.addAction(new AboutAction(HackfulReaderActivity.getContext(), this));
 		if (mDualPane || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			actionBar.setTitle(R.string.app_name);
 		}
 		if (mDualPane) {
 			if (webView == null) {
 				Log.d("hereader", "WebView is null, creating new instance");
 				final ActionBar actionB = actionBar;
 				webView = new WebView(getActivity());
 				webView.setId(R.id.webview_id);
 				webView.getSettings().setJavaScriptEnabled(true);
				webView.getSettings().setSupportZoom(true);
				//webView.getSettings().setBuiltInZoomControls(false);
 				webView.setWebChromeClient(new WebChromeClient() {
 	
 					@Override
 					public void onProgressChanged(WebView view, int newProgress) {
 						if (newProgress == 100) {
 							actionB.setProgressBarVisibility(View.GONE);
 						}
 					}
 					
 				});
 			}
 			webViewContainer.addView(webView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		}
 		Log.d("hereader", "UI created");
 	}
 	
 	private void showDetails(int position, URL itemUrl) {
 		Log.d("hereader", "entering NewsItemsFragment.showDetails");
 		if (mDualPane) {
 			Log.d("hereader", "Is dual pane");
 			if (currentPosition >= 0) {
 				items.get(currentPosition).setSelected(false);
 			}
 			currentPosition = position;
 			items.get(currentPosition).setSelected(true);
 			actionBar.setProgressBarVisibility(View.VISIBLE);
 			webView.stopLoading();
 			webView.loadUrl(itemUrl.toString());
 			adapter.notifyDataSetChanged();
 			Log.d("hereader", "WebView loadUrl invoked");
 		} else {
 			Log.d("hereader", "Is single pane");
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse(itemUrl.toString()));
 			startActivity(intent);
 			Log.d("hereader", "Intent launched for single pane");
 		}
 		Log.d("hereader", "leaving NewsItemsFragment.showDetails");
 	}
 
 	@Override
 	public void onItemsLoaded(List<HEMessage> items) {
 		Log.d("hereader", "received notification from NewsItemsLoaderTask");
 		if (items != null) {
 			this.items.addAll(transformItems(items));
 			if (adapter == null) {
 				adapter = new HEMessagesAdapter(getActivity(), R.layout.item, this.items);
 				setListAdapter(adapter);
 			} else {
 				adapter.notifyDataSetChanged();
 			}
 			actionBar.setProgressBarVisibility(View.GONE);
 			getListView().setSelectionAfterHeaderView();
 		} else {
 			Toast.makeText(getActivity(), R.string.connection_error, 7).show();
 		}
 
 	}	
 
 	public boolean isDualPane() {
 		return mDualPane;
 	}
 
 	private void loadItems(String source) {
 		Log.d("hereader", "entering NewsItemsFragment.loadItems");
 		if (actionBar != null) {
 			actionBar.setProgressBarVisibility(View.VISIBLE);
 		}
 		NewsItemsLoaderTask task =  new NewsItemsLoaderTask();
 		task.addNewsItemsLoadedListener(this);
 		task.setContext(getActivity().getApplicationContext());
 		task.execute(source, getString(R.string.he_namespace));
 		Log.d("hereader", "leaving NewsItemsFragment.loadItems");
 	}
 
 	@Override
 	public void loadFrontpageItems() {
 		currentItemsSource = R.string.frontpage_feed;
 		items.clear();
 		if (adapter != null) {
 			adapter.clear();
 		}
 		loadItems(getString(R.string.frontpage_feed));
 	}
 
 	@Override
 	public void loadNewItems() {
 		currentItemsSource = R.string.new_feed;
 		items.clear();
 		if (adapter != null) {
 			adapter.clear();
 		}
 		loadItems(getString(R.string.new_feed));
 	}
 	
 	@Override
 	public void loadAskItems() {
 		currentItemsSource = R.string.ask_feed;
 		items.clear();
 		if (adapter != null) {
 			adapter.clear();
 		}
 		loadItems(getString(R.string.ask_feed));
 	}
 
 	@Override
 	public void loadMoreItems() {
 		actionBar.setProgressBarVisibility(View.VISIBLE);
 		switch (currentItemsSource) {
 		case R.string.frontpage_feed:
 			break;
 		case R.string.new_feed:
 			break;
 		}
 	}
 
 	@Override
 	public List<ListFragmentItem> getItems() {
 		return items;
 	}
 
 	@Override
 	public void putItems(List<ListFragmentItem> items, int itemsSource) {
 		this.items = items;
 		currentItemsSource = itemsSource;
 	}
 
 	@Override
 	public int getItemsSource() {
 		return currentItemsSource;
 	}
 
 	@Override
 	public HEReaderState getCurrentState() {
 		return new HEReaderState(currentItemsSource, items, currentPage); 
 	}
 
 	@Override
 	public void setCurrentState(HEReaderState state) {
 		currentItemsSource = state.getCurrentSource();
 		items = state.getItems();
 		currentPage = state.getCurrentPage();
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void onDetach() {
 		if (mDualPane) {
 			webViewContainer.removeView(webView);
 		}
 		super.onDetach();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 	
 	private List<ListFragmentItem> transformItems(List<HEMessage> items) {
 		List<ListFragmentItem> result = new ArrayList<ListFragmentItem>();
 		for(HEMessage msg : items) {
 			result.add(new ListFragmentItem(msg, false));
 		}
 		return result;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Log.d("hereader", "saving instance state");
 		outState.putSerializable(SAVED_ITEMS_KEY, (Serializable) items);
 		outState.putInt(SAVED_ITEMS_SOURCE_KEY, currentItemsSource);
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 		getListView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
 		clickedPosition = position;
 		getActivity().showDialog(R.id.open_dialog);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		URL itemUrl = items.get(position).getHEMessage().getLink();
 		clickedPosition = position;
 		showDetails(position, itemUrl);
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int item) {
 		String selected = getResources().getStringArray(R.array.open_dialog_options)[item];
 		URL url = null;
 		if (selected.equals(getResources().getString(R.string.open_link))) {
 			url = items.get(clickedPosition).getHEMessage().getLink();
 		} else if (selected.equals(getResources().getString(R.string.open_post))) {
 			url = items.get(clickedPosition).getHEMessage().getGuid();
 		}
 		if (url != null) {
 			showDetails(clickedPosition, url);
 		}
 	}
 
 }
