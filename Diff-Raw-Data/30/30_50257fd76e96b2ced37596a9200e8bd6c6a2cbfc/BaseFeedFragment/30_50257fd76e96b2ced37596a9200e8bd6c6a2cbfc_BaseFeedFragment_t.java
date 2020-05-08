 package com.TweetGet.Fragments;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.TweetGet.R;
 import com.TweetGet.Managers.SharedPreferencesManager;
 import com.TweetGet.Utils.AnimationUtils;
 import com.TweetGet.Utils.NetworkUtils;
 
 public abstract class BaseFeedFragment extends Fragment {
 
 	protected ListView mTweetList;
 	private TextView mHeaderView;
 	private FrameLayout mCover;
 
	public static final String HASHTAG_FRAGMENT_TAG = "HASHTAG";
	public static final String USER_FRAGMENT_TAG = "USER";

 	protected abstract void fetchDataAsynchronously();
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		mTweetList = (ListView) view.findViewById(R.id.tweetList);
 		mCover = (FrameLayout) view.findViewById(R.id.no_wifi_cover);
 
 		Bundle args = getArguments();
 		String query;
 		SharedPreferencesManager.init(getActivity());
 		SharedPreferencesManager ms = SharedPreferencesManager.getInstance();
 
 		if (args != null) {
 			query = args.getString("query");
 			query = getTargetUrl(ms, query);
 		} else {
 
 			query = getTargetUrl(ms, null);
 		}
 
 		setUpTweetList(query);
 		if (NetworkUtils.wifiIsEnaled(getActivity())) {
 
 			fetchDataAsynchronously();
 
 		} else {
 			mCover.setVisibility(View.VISIBLE);
 		}
 
 	}
 
 	protected abstract String getTargetUrl(SharedPreferencesManager ms,
 			String tag);
 
 	private void setUpTweetList(String tag) {
 
 		mHeaderView = new TextView(getActivity());
 		mHeaderView.setText("tweets about " + tag);
 		mTweetList.addHeaderView(mHeaderView);
 
 		AnimationUtils.setLoadingAnimation(mTweetList);
 	}
 
 }
