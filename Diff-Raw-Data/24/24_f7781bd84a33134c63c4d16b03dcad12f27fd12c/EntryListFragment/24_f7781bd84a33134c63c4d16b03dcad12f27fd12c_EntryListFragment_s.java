 /**
  * Copyright 2013 Ognyan Bankov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.yairkukielka.feedhungry;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.yairkukielka.feedhungry.AppData.MARK_ACTION;
 import com.yairkukielka.feedhungry.app.App_Feedhungry;
 import com.yairkukielka.feedhungry.app.MyVolley;
 import com.yairkukielka.feedhungry.feedly.ListEntry;
 import com.yairkukielka.feedhungry.settings.PreferencesActivity;
 import com.yairkukielka.feedhungry.toolbox.NetworkUtils;
 
 /**
  * Shows the list of entries for a stream
  */
 @EFragment(R.layout.feed_grid_view)
 public class EntryListFragment extends SherlockFragment {
 	private static final String TAG = EntryListFragment.class.getSimpleName();
 	private static final String STREAM_PATH = "/v3/streams/contents?streamId=";
 	private static final String MIXES_PATH = "/v3/mixes/contents?streamId=";
 	public static final String STREAM_ID = "stream_id";
 	public static final String ENTRY_POSITION_IN_STREAM = "entryPosition";
 	public static final String ACCESS_TOKEN = "accessToken";
 	public static final String IS_MIX = "mix";
 	private static final String ENTRY_ID = "entryId";
 	public static final int POPULAR_ITEMS_PAGE_SIZE = 20;
 	public static final String RESULTS_PAGE_SIZE = "RESULTS_PAGE_SIZE";
 	private static final String COUNT_PARAM = "&count=";
 	private static final String CONTINUATION_PARAM = "&continuation=";
 	private static final String ONLY_UNREAD_PARAM = "&unreadOnly=";
 	private Fragment loadingFragment;
 	public String continuation = null;
 
 	@ViewById(R.id.lv_picasa)
 	AbsListView mLvPicasa;
 	private boolean mHasData = false;
 	private ArrayList<ListEntry> mEntries = new ArrayList<ListEntry>();
 	private ListViewEntryArrayAdapter mAdapter;
 	private boolean initialized = false;
 	private String streamId;
 	private String accessToken;
 	private Boolean isMix;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 		
 	@AfterViews
 	void afterViews() {
 		initialized = true;
 		refreshItems();
 	}
 
 	public void setStreamToLoad(final String streamId, final String accessToken, final Boolean isMix) {
 		this.streamId = streamId;
 		this.accessToken = accessToken;
 		this.isMix = isMix;
 		if (initialized) {
 			refreshItems();
 		}
 	}
 
 	private void refreshItems() {
		if (streamId == null || accessToken == null || isMix == null) {
			Log.e(TAG, "No stream to load in EntryListFragment.refreshItems");
 			return;
 		}
 		mEntries.clear();
 		mHasData = false;
 		mAdapter = new ListViewEntryArrayAdapter(getActivity(), 0, mEntries, MyVolley.getImageLoader());
 		((AdapterView) mLvPicasa).setAdapter(mAdapter);
 		mLvPicasa.setOnScrollListener(new EndlessScrollListener());
 
 		if (!mHasData) {
 			addLoadingFragment();
 		}
 		mLvPicasa.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent(EntryListFragment.this.getActivity(), FeedEntryActivity_.class);
 				ListEntry listEntry = mEntries.get(position);
 				mAdapter.markEntry(MARK_ACTION.READ, mEntries.get(position));
 				Bundle b = new Bundle();
 				b.putString(ACCESS_TOKEN, accessToken);
 				b.putString(ENTRY_ID, listEntry.getId());
 				b.putString(STREAM_ID, streamId);
 				b.putInt(ENTRY_POSITION_IN_STREAM, position);
 				intent.putExtras(b);
 				startActivity(intent);
 				EntryListFragment.this.getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_main);
 			}
 		});
 
 		if (!mHasData) {
 			loadPage(false);
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		if (activity instanceof MainActivity) {
 			MainActivity act = (MainActivity) activity;
 			act.setEntriesFragment(this);
 		} else {
 			throw new ClassCastException(activity.toString() + " must be MainActivity");
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 	}
 
 	private void loadPage(boolean calledByScrolListener) {
 		RequestQueue queue = MyVolley.getRequestQueue();
 		JsonObjectRequest myReq = NetworkUtils.getJsonObjectRequest(getPath(),
 				createMyReqSuccessListener(calledByScrolListener), createMyReqErrorListener(), accessToken);
 		queue.add(myReq);
 	}
 
 	/**
 	 * Builds the path for the url. Usually it will be a stream, but sometimes
 	 * it can be a mix (like for popular articles)
 	 * 
 	 * @return the path
 	 */
 	private String getPath() {
 		String path;
 		if (isMix) {
 			path = MainActivity.ROOT_URL + MIXES_PATH + streamId + getMixCountParameter() + getContinuationParameter()
 					+ getOnlyUnreadParameter();
 		} else {
 			path = MainActivity.ROOT_URL + STREAM_PATH + streamId + getPageSizeParameter() + getContinuationParameter()
 					+ getOnlyUnreadParameter();
 		}
 		return path;
 	}
 
 	private Response.Listener<JSONObject> createMyReqSuccessListener(final boolean calledByScrolListener) {
 		return new Response.Listener<JSONObject>() {
 			@Override
 			public void onResponse(JSONObject response) {
 				try {
 					if (!calledByScrolListener) {
 						removeLoadingFragment();
 					}
 					if (response.has("continuation")) {
 						continuation = response.getString("continuation");
 					} else {
 						// to mark the end of the scrolling and to not ask for
 						// more entries
 						continuation = null;
 					}
 					JSONArray items = response.getJSONArray("items");
 					for (int i = 0; i < items.length(); i++) {
 						ListEntry e = new ListEntry((JSONObject) items.get(i));
 						mEntries.add(e);
 					}
 					if (!mHasData) {
 						mHasData = true;
 					}
 					mAdapter.notifyDataSetChanged();
 					setLastStreamLoaded();
 				} catch (JSONException e) {
 					if (e != null && e.getMessage() != null) {
 						Log.e(TAG, e.getMessage());
 					}
 					showErrorDialog(e.getMessage());
 				}
 			}
 		};
 	}
 
 	private Response.ErrorListener createMyReqErrorListener() {
 		return new Response.ErrorListener() {
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				removeLoadingFragment();
 				if (error != null && error.getMessage() != null) {
 					Log.e(TAG, error.getMessage());
 					// showErrorDialog(error.getMessage());
 				} else if (error != null && error.getCause() != null) {
 					Log.e(TAG, error.getCause().toString());
 				}
 			}
 		};
 	}
 
 	private void showErrorDialog(String error) {
 		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
 		b.setMessage(getResources().getString(R.string.generic_exception));
 		b.show();
 	}
 
 	private void setLastStreamLoaded() {
 		if (getActivity() != null) {
 			App_Feedhungry applicState = ((App_Feedhungry) getActivity().getApplicationContext());
 			AppData appData = applicState.getAppData();
 			appData.setLastStreamLoaded(mEntries);
 		}
 	}
 
 	/**
 	 * Used to page through the contents in the feedly api
 	 * 
 	 * @return parameter continuation if necessary (it's not necessary the first
 	 *         load)
 	 */
 	private String getContinuationParameter() {
 		if (continuation != null) {
 			return CONTINUATION_PARAM + continuation;
 		}
 		return "";
 	}
 
 	/**
 	 * Used to page through the contents in the feedly api
 	 * 
 	 * @return parameter continuation if necessary (it's not necessary the first
 	 *         load)
 	 */
 	private String getOnlyUnreadParameter() {
 		if (getPrefOnlyUnread(getActivity())) {
 			return ONLY_UNREAD_PARAM + true;
 		}
 		return "";
 	}
 
 	/**
 	 * Gets the url part of the parameter for the number of entries to retrieve
 	 * from feedly servers for a stream
 	 * 
 	 * @return number of entries
 	 * @throws Exception
 	 */
 	private String getPageSizeParameter() {
 		Integer pageSize = Integer.parseInt(getPrefPageSize(getActivity()));
 		return COUNT_PARAM + pageSize;
 	}
 
 	/**
 	 * Gets the parameter of the number of entries to retrieve from feedly
 	 * servers for a mix
 	 * 
 	 * @return number of entries
 	 */
 	private String getMixCountParameter() {
 		return COUNT_PARAM + POPULAR_ITEMS_PAGE_SIZE;
 	}
 
 	/**
 	 * Get only unread articles setting from preferences
 	 * 
 	 * @param context
 	 *            context
 	 * @return true if only unread articles
 	 */
 	public static Boolean getPrefOnlyUnread(Context context) {
 		if (context != null) {
 			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
 			return sharedPref.getBoolean(PreferencesActivity.KEY_ONLY_UNREAD, false);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets, from preferences, the parameter of the number of entries to
 	 * retrieve from feedly servers for a stream
 	 * 
 	 * @param context
 	 *            activity context
 	 * @return the parameter page size
 	 */
 	public static String getPrefPageSize(Context context) {
 		if (context != null) {
 			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
 			return sharedPref.getString(PreferencesActivity.KEY_PAGE_SIZE, "30");
 		}
 		return null;
 	}
 
 	private void addLoadingFragment() {
 		if (loadingFragment == null) {
 			loadingFragment = new LoadingFragment_();
 		}
 		FragmentManager fragmentManager = getSherlockActivity().getSupportFragmentManager();
 		if (fragmentManager.findFragmentById(loadingFragment.getId()) == null) {
 			fragmentManager.beginTransaction().add(R.id.content_frame, loadingFragment).commit();
 		}
 
 	}
 
 	private void removeLoadingFragment() {
 		if (getActivity() != null) {
 			FragmentManager fragmentManager = getSherlockActivity().getSupportFragmentManager();
 			if (loadingFragment != null && fragmentManager.findFragmentById(loadingFragment.getId()) != null) {
 				fragmentManager.beginTransaction().remove(loadingFragment).commitAllowingStateLoss();
 			}
 		}
 
 	}
 
 	/**
 	 * Detects when user is close to the end of the current page and starts
 	 * loading the next page so the user will not have to wait (that much) for
 	 * the next entries.
 	 * 
 	 * @author Ognyan Bankov (ognyan.bankov@bulpros.com)
 	 */
 	public class EndlessScrollListener implements OnScrollListener {
 		// how many entries earlier to start loading next page
 		private int visibleThreshold = 5;
 		private int currentPage = 0;
 		private int previousTotal = 0;
 		private boolean loading = true;
 		Fragment srcollerLoadingFragment = new LoadingFragment_();
 
 		public EndlessScrollListener() {
 		}
 
 		public EndlessScrollListener(int visibleThreshold) {
 			this.visibleThreshold = visibleThreshold;
 		}
 
 		@Override
 		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 			if (loading) {
 				if (totalItemCount > previousTotal) {
 					loading = false;
 
 					FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
 					fragmentManager.beginTransaction().remove(srcollerLoadingFragment).commit();
 
 					previousTotal = totalItemCount;
 					currentPage++;
 				}
 			}
 			// continuation is null when Feedly API says there are no more
 			// entries to retrieve
 			if (!loading && continuation != null
 					&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
 				// I load the next page of gigs using a background task,
 				// but you can call any function here.
 				loadPage(true);
 				loading = true;
 				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
 				fragmentManager.beginTransaction().add(R.id.content_frame, srcollerLoadingFragment).commit();
 			}
 		}
 
 		@Override
 		public void onScrollStateChanged(AbsListView view, int scrollState) {
 
 		}
 
 		public int getCurrentPage() {
 			return currentPage;
 		}
 	}
 }
