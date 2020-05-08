 package se.mah.kd330a.project.itsl;
 
 import se.mah.kd330a.project.R;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import android.app.ActionBar;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /*
  * @author asampe, marcusmansson
  * 
  * 
  * HISTORY:
  * 
  * 0.8.1
  *  o added recent changes (notification, background updates, some rewrites, resources)
  *    from now on we will work in this project and not in our own
  *
  * 0.8.0
  *  o transfered files and resources to the framwork project, adapted code for
  *    fragment
  *   
  * 0.7.4
  *  o moved all file handling to FeedManager
  *  o code and comments cleaned up
  *   
  * 0.7.3
  *  o merged two branches (unknown version) with 0.7.2 
  *  o course colors in the UI 
  *  o fixed text overflow in the UI
  *  
  * 0.7.2 
  * 	o added saving/loading of articles to/from cache
  *  o load saved articles and initialize list on app start 
  * 	o removed unused code
  * 
  * TODO:
  *  o fix a better looking progressbar, until then use old progressdialog
  *  o seem to be a glitch when expanding, sometimes the child gets a black background 
  *    
  *    
  */
 public class FragmentITSL extends Fragment implements FeedManager.FeedManagerDoneListener, 
 	OnScrollListener, OnChildClickListener, OnClickListener
 {
 	static final String TAG = "ITSL_fragment";
	static final long UPDATE_INTERVAL = 120000; //every other minute
 	// 1800000 = 30 minutes
 
 	ExpandableListAdapter listAdapter;
 	ExpandableListView expListView;
 	FeedManager feedManager;
 	ProgressDialog dialog;
 	ProgressBar progBar;
 	TextView txProgress;
 	View headerView;
 	PendingIntent backgroundUpdateIntent;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		/*
 		 * set up the repeating task of updating data in the background 
 		 * (but stop it while the app is running)
 		 */
 		Context appContext = getActivity().getApplicationContext(); 
 		backgroundUpdateIntent = PendingIntent.getService(
 				appContext, 0, 
 				new Intent(appContext, TimeAlarm.class), 0);
 		
 		feedManager = new FeedManager(this, appContext);
 	}
 
 	public void onPause()
 	{
 		super.onPause();
 		
 		Log.i(TAG, "Paused: Setting up background updates");
 
 		AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
 		alarm.setRepeating(AlarmManager.RTC_WAKEUP, 
 				System.currentTimeMillis() + UPDATE_INTERVAL, 
 				UPDATE_INTERVAL, backgroundUpdateIntent);
 		
 		/*
 		 * Remember when we last had this view opened 
 		 */
 		Util.setLatestUpdate(getActivity().getApplicationContext(), 
 				new Date(System.currentTimeMillis()));
 	}
 	
 	public void onResume()
 	{
 		super.onResume();
 		
 		Log.i(TAG, "Resumed: Stopping background updates");
 
 		AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
 		alarm.cancel(backgroundUpdateIntent);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_itsl, container, false);
 
 		/*
 		 * custom ActionBar
 		 */
 		/*
 		ColorDrawable colorDrawable = new ColorDrawable();
 		colorDrawable.setColor(0xffeeeeee);
 		ActionBar actionBar = getActivity().getActionBar();
 		actionBar.setBackgroundDrawable(colorDrawable);
 		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
 		actionBar.setCustomView(R.layout.itsl_abs_layout);
 		*/
 		
 		/*
 		 *  set up progressbar
 		 */
 		progBar = (ProgressBar) rootView.findViewById(R.id.progress);
 		txProgress = (TextView) rootView.findViewById(R.id.txProgess);
 		progBar.setVisibility(ProgressBar.GONE);
 		txProgress.setVisibility(TextView.GONE);
 		
 		/*
 		 *  create settings view and hide it
 		 */
 		headerView = inflater.inflate(R.layout.itsl_list_header, null);
 		headerView.findViewById(R.id.button1).setOnClickListener(this);
 		headerView.findViewById(R.id.button2).setOnClickListener(this);
 		hideSettingsView();
 
 		/*
 		 *  set up the listview
 		 */
 		listAdapter = new ExpandableListAdapter(getActivity(), feedManager.getArticles());
 		expListView = (ExpandableListView) rootView.findViewById(R.id.lvExp);
 		expListView.addHeaderView(headerView);
 		expListView.setAdapter(listAdapter);
 		expListView.setOnScrollListener(this);
 		expListView.setOnChildClickListener(this);
 
 		feedManager.loadCache();
 
 		for (String url : Util.getBrowserBookmarks(getActivity().getApplicationContext()))
 		{
 			Log.i(TAG, "Got URL from bookmarks: " + url);
 			feedManager.addFeedURL(url);
 		}
 		
 		/*
 		 *  in case there is nothing in the cache, or it doesn't exist
 		 *  we have to refresh
 		 */
 		if (feedManager.getArticles().isEmpty())
 			refresh();
 		
 		
 		for (String title : getFeedObjects().keySet())
 		{
 			Log.i(TAG, "Filter list has key: " + title);
 		}
 
 		return rootView;
 	}
 
 	public class FeedObject {
 		public ArrayList<Article> articles;
 		public FeedObject()
 		{
 			articles = new ArrayList<Article>();
 		}
 	}
 
 	public HashMap<String, FeedObject> getFeedObjects() {
 		HashMap<String, FeedObject> foList = new HashMap<String, FeedObject>();
 		
 		for (Article a : feedManager.getArticles())
 		{
 			FeedObject fo;
 			
 			if (foList.containsKey(a.getArticleCourseCode()))
 			{
 				fo = foList.get(a.getArticleCourseCode());
 			}
 			else
 			{
 				fo = new FeedObject();
 				foList.put(a.getArticleCourseCode(), fo);
 			}
 			
 			fo.articles.add(a);
 		}
 		
 		return foList;
 	}
 
 	public void onFeedManagerProgress(FeedManager fm, int progress, int max)
 	{
 		/*
 		 *  set up progress dialog if there isn't one.
 		 */
 		if (dialog == null)
 		{
 			dialog = new ProgressDialog(getActivity());
 			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			dialog.setMessage("Downloading...");
 			dialog.show();
 		}
 
 		dialog.setProgress(progress);
 		dialog.setMax(max);
 
 		/*
 		progBar.setVisibility(ProgressBar.VISIBLE);
 		txProgress.setVisibility(TextView.VISIBLE);
 		progBar.setProgress(progress);
 		progBar.setMax(max);
 		*/
 	}
 
 	@Override
 	public void onFeedManagerDone(FeedManager fm, ArrayList<Article> articles)
 	{
 		/*
 		 * display the data in our listview
 		 */
 		listAdapter.notifyDataSetInvalidated();
 
 		/*
 		progBar.setVisibility(ProgressBar.GONE);
 		txProgress.setVisibility(TextView.GONE);
 		*/
 		dialog.dismiss();
 		dialog = null;
 
 		Toast.makeText(getActivity(), "" + articles.size() + " articles", Toast.LENGTH_LONG).show();
 	}
 
 	private void refresh()
 	{
 		/*
 		 * close all expanded childviews, otherwise they will incorrectly 
 		 * linger in the UI even after we invalidate the dataset
 		 */
 		int count = listAdapter.getGroupCount();
 		for (int i = 0; i < count; i++)
 			expListView.collapseGroup(i);
 
 		feedManager.reset();
 		feedManager.processFeeds();
 	}
 
 	private void hideSettingsView()
 	{
 		headerView.findViewById(R.id.headerLayout).setVisibility(View.GONE);
 	}
 
 	private void showSettingsView()
 	{
 		headerView.findViewById(R.id.headerLayout).setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
 	{
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState)
 	{
 		if (expListView.getFirstVisiblePosition() == 0 && scrollState == OnScrollListener.SCROLL_STATE_IDLE)
 			/*
 			 * you could refresh content here directly instead of showing the header
 			 */
 			showSettingsView();
 		else
 			hideSettingsView();
 	}
 
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
 	{
 		return parent.collapseGroup(groupPosition);
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		switch (v.getId()) {
 		case R.id.button1:
 			refresh();
 			hideSettingsView();
 			break;
 		case R.id.button2:
 			feedManager.reset();
 			feedManager.deleteCache();
 			listAdapter.notifyDataSetInvalidated();
 			break;
 		}
 	}
 }
