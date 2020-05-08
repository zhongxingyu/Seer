 package org.jefferyemanuel.willowtweetapp;
 
 import static org.jefferyemanuel.willowtweetapp.Utils.createToast;
 import static org.jefferyemanuel.willowtweetapp.Utils.printLog;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import org.jefferyemanuel.listeners.TaskCallbacks;
 import org.jefferyemanuel.listeners.TweeterListObserver;
 import org.jefferyemanuel.willowtweetapp.TweeterListAdapter.Holder;
 
 import com.sothree.multiitemrowlistadapter.MultiItemRowListAdapter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap.CompressFormat;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /*
  * the Main Activity.  Here we use twitter4j library to parse twitter JSON on a seperate thread and after successful
  * parse we set the page adapter and to each page we add a listview and set an adapter where we maniuplate each item
  * in the listview. A dISKCACHE is used to store http images. two implementations of this class 1. TaskCallbacks is an 
  * interface from TaskFragment that reports when a  AsnychTask has completed parsing twitter JSON feed and other is 
  * for cursor loader which loads a cursor to content provider that points to list of tweeters that user entered.  
  * */
 
 public class TweeterListFragment extends Fragment implements
 		LoaderManager.LoaderCallbacks<Cursor>, TweeterListObserver {
 
 	private SectionsPagerAdapter mSectionsPagerAdapter;
 	private static NumberFormat nf;
 	private ArrayList<String> mUsernamesList;
	private boolean fromSavedState = false;
 	private boolean mMultipleColumns;
 	private int mCurrentPage;
 
 	public boolean getMultipleColumns() {
 		return mMultipleColumns;
 	}
 
 	public void setMultipleColumns(boolean mMultipleColumns) {
 		this.mMultipleColumns = mMultipleColumns;
 	}
 
 	FragmentManager fm;
 	//contains each tweeters array of info we collected in background
 	static ArrayList<ArrayList<HashMap<String, String>>> mTweetersObj_map;
 	String[] mpageTitles = {};
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		printLog(Consts.TAG, "calling onDestroy");
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		/*
 		 * If onDestroyView() is called first, we can use the previously
 		 * savedState but we can't call saveState() anymore
 		 */
 		/*
 		 * If onSaveInstanceState() is called first, we don't have savedState,
 		 * so we need to call saveState()
 		 */
 		/* => (?:) operator inevitable! */
 		//    fromSavedState=true;
 		printLog(Consts.TAG,
 				"calling onSavedInstanceState from tweeterListFragment");
 
 		outState.putSerializable("userlist", mUsernamesList);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		{
 
 			TaskFragment task_fragment = (TaskFragment) fm
 					.findFragmentByTag(TaskFragment.class.getName());
 			printLog(Consts.TAG, "calling onResume:" + task_fragment);
 
 		}
 		Utils.hideKeyboard(getActivity());
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		if (container == null) {
 			// We have different layouts, and in one of them this
 			// fragment's containing frame doesn't exist.  The fragment
 			// may still be created from its saved state, but there is
 			// no reason to try to create its view hierarchy because it
 			// won't be displayed.  Note this is not needed -- we could
 			// just run the code below, where we would create and return
 			// the view hierarchy; it would just never be used.
 			return null;
 		}
 
 		View rootView = inflater.inflate(R.layout.tweeterlist_fragment, null);
 
 		mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		return rootView;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//this.setRetainInstance(true);
 
 		if (Consts.DEVELOPER_MODE)
 			LoaderManager.enableDebugLogging(true);
 
 		if (savedInstanceState != null) {
			fromSavedState = true;
 			mUsernamesList = (ArrayList<String>) savedInstanceState
 					.getSerializable("userlist");
 		}
 
 		printLog(Consts.TAG, "calling onCreate from TweetListFragment:"
 				+ (savedInstanceState != null ? "orientationChanged" : "null"));
 
 		/* alloc fragment manager */
 		fm = getActivity().getSupportFragmentManager();
 
 		//this calls expensive so lets do it once as it has to fetch the system locale which can be intense
 		nf = NumberFormat.getInstance();
 
 		/* initialize the map of all our tweeter data sources for each listview */
 		mTweetersObj_map = new ArrayList<ArrayList<HashMap<String, String>>>();
 
 		mSectionsPagerAdapter = new SectionsPagerAdapter(fm);
 
 		/* create cursor loader to get names from tweeters table */
 		getActivity().getSupportLoaderManager().initLoader(Consts.LOADER_ID,
 				null, this);
 
 	}
 
 	public void createWarningDialog(String msg) {
 
 		MyDialogFragment dialogfragment = (MyDialogFragment) MyDialogFragment
 				.newInstance(getActivity(), msg);
 		dialogfragment.show(fm, "dialog2");
 
 	}
 
 	public void reStartLoader() {
 
 		printLog(Consts.TAG, "beginning refresh");
 		getActivity().getSupportLoaderManager().restartLoader(
 				Consts.LOADER_ASYNCH_ID, null, this);
 	}
 
 	/* confirms weather the user has a network connection */
 	protected boolean isOnline() {
 		final ConnectivityManager cm = (ConnectivityManager) getActivity()
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnected()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void goToPage(String name) {
 
 		int index = Arrays.asList(mpageTitles).indexOf(name);
 		mViewPager.setCurrentItem(index, true);
 	}
 
 	public void goToPage(int index) {
 		if (mViewPager != null)
 			mViewPager.setCurrentItem(index, true);
 	}
 
 	public int getcurrentPage() {
 
 		return mViewPager.getCurrentItem();
 
 	}
 
 	public void setupTitles(String[] userList) {
 		//locked just in case as access comes also from cursor loader thread
 		synchronized (mpageTitles) {
 			mpageTitles = userList;
 		}
 	}
 
 	/**
 	 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding
 	 * to one of the sections/tabs/pages
 	 * 
 	 * this is a {@link android.support.v4.app.FragmentStatePagerAdapter}
 	 * derivative, which will NOT keep every loaded fragment in memory. use
 	 * {@link android.support.v4.app.FragmentPagerAdapter} if you want to load
 	 * all fragments into memory.
 	 */
 
 	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a listFragment ) with the page number as one of its argument.
 
 			TweetsListFragment mListfragment = TweetsListFragment.newInstance(
 					position, getMultipleColumns(), nf, mpageTitles[position]);
 
 			return mListfragment;
 		}
 
 		@Override
 		public int getItemPosition(Object object) {
 			printLog(Consts.TAG, "getting item position from pageview adapter");
 			return PagerAdapter.POSITION_NONE;
 		}
 
 		@Override
 		public int getCount() {
 
 			return mpageTitles.length;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 
 			return mpageTitles[position];
 
 		}
 
 		/**
 		 * A reusable fragment representing a section of the app as a listview.
 		 * It gets the page # (position from the activitys section adapter which
 		 * passes it as a bundle. We can use this position to get which tweeter
 		 * the end user is viewing.
 		 */
 	}
 
 	/* sends a message to handler on UI thread */
 	private void threadMsg(String msg) {
 
 		if (!msg.equals(null) && !msg.equals("")) {
 			Message msgObj = handler.obtainMessage();
 			Bundle b = new Bundle();
 			b.putString("message", msg);
 			msgObj.setData(b);
 			handler.sendMessage(msgObj);
 		}
 	}
 
 	/*
 	 * when the cursor completes loading the framework will not allow modifying
 	 * fragments on onloadFinished, so i've set up a handler where we can send a
 	 * message to the UI handler to update the UI
 	 */
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 
 			String TAG = "willow_handler";
 
 			String aResponse = msg.getData().getString("message");
 
 			printLog(TAG, aResponse);
 
 			if (aResponse.equals(Consts.HANDLER_UPDATE_PAGER_ADAPTER)) {
 
 				setupTitles((String[]) mUsernamesList.toArray(new String[0]));
 				mSectionsPagerAdapter.notifyDataSetChanged();
 				mViewPager.setCurrentItem(mCurrentPage);
 			}
 		}
 	};
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 
 		return new CursorLoader(getActivity(), Consts.CONTENT_URI,
 				new String[] { Consts.COLUMN_USER }, null, null,
 				Consts.COLUMN_USER + " COLLATE NOCASE ASC");
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
 
 		/*
 		 * our cursor loader has observed a change in content and notified us of
 		 * user adding new tweets to the tab, lets contact twitter.com to get
 		 * tweets for these users
 		 */
 		mUsernamesList = new ArrayList<String>();
 
 		int index = cursor.getColumnIndex(Consts.COLUMN_USER);
 		cursor.moveToFirst();
 		String username;
 		/* loop through user column from cursor and gather all user names */
 		while (!cursor.isAfterLast()) {
 			username = cursor.getString(index);
 			mUsernamesList.add(username);
 
 			cursor.moveToNext();
 		}
 
 		//setupTitles((String[]) mUsernamesList.toArray(new String[0]));
 		//mSectionsPagerAdapter.notifyDataSetChanged();
 		/*
 		 * send a message to handler to start taskFragment which is really a
 		 * asynchTask to contact Twitter.com for feeds
 		 */
 		if (mUsernamesList.isEmpty()) {
			setupTitles((String[]) mUsernamesList.toArray(new String[0]));
 			mSectionsPagerAdapter.notifyDataSetChanged();
 		}
 
 		else if (isOnline())
 			threadMsg(Consts.HANDLER_UPDATE_PAGER_ADAPTER);
 		else
 			createToast(getActivity(),
 					getString(R.string.warning_no_connection));
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		// TODO Auto-generated method stub	
 	}
 
 	@Override
 	public void changePage(int index) {
 		//goToPage(index);
 		mCurrentPage = index;
 	}
 
 	@Override
 	public void requestRefresh() {
 		reStartLoader();
 	}
 
 	@Override
 	public void requestMultiColumn(boolean multiColumnChoice) {
 		this.setMultipleColumns(multiColumnChoice);
 		mSectionsPagerAdapter.notifyDataSetChanged();
 	}
 
 }
