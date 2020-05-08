 package com.DGSD.TweeterTweeter.Fragments;
 
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.ListFragment;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.DGSD.TweeterTweeter.R;
 import com.DGSD.TweeterTweeter.TTApplication;
 import com.DGSD.TweeterTweeter.Utils.Log;
 
 public class SideMenuFragment extends ListFragment {
 	public static final String TAG = SideMenuFragment.class.getSimpleName();
 
 	private static final String KEY_SELECTED_ITEM = "selected_item";
 
 	private static final int ITEM_HOME_TIMELINE = 0;
 	private static final int ITEM_DM_RECEIVED = 1;
 	private static final int ITEM_DM_SENT = 2;
 	private static final int ITEM_FAVOURITES = 3;
 	private static final int ITEM_FOLLOWERS = 4;
 	private static final int ITEM_FOLLOWING = 5;
 	private static final int ITEM_MENTIONS = 6;
 	private static final int ITEM_RETWEETS_BY = 7;
 	private static final int ITEM_RETWEETS_OF = 8;
 	private static final int ITEM_SAVED_SEARCH = 9;
 	private static final int ITEM_LISTS = 10;
 
 	private int mSelectedItem;
 
 	private String[] mListItems;
 
 	private TTApplication mApplication;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		mApplication = (TTApplication) getActivity().getApplication();
 
 		mListItems = getResources().getStringArray(R.array.menu_list);
 
 		ListView lv = getListView();
 
 		setListAdapter(new ArrayAdapter<String>(getActivity(), 
 				R.layout.side_menu_list_item, mListItems));
 
 		lv.setCacheColorHint(Color.TRANSPARENT);
 
 		lv.setBackgroundResource(R.drawable.sidebar_background_repeat);
 
 		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
 		if(savedInstanceState != null) {
 			mSelectedItem = savedInstanceState.getInt(KEY_SELECTED_ITEM);
 		} else {
 			//We aren't creating after a rotation!
 			mSelectedItem = 0;
 			displayFragmentOfItem(mSelectedItem);
 		}
 		
 	}
 
 	@Override
 	public void onListItemClick(ListView lv, View v, int pos, long id) {
 		if(mSelectedItem == pos) {
 			//We are already showing this item!
 			return;
 		} else {
 			mSelectedItem = pos;
 
 			displayFragmentOfItem(mSelectedItem);
 		}
 	}
 
 	private void displayFragmentOfItem(int itemPos) {
 		getListView().setItemChecked(itemPos, true);
 
 		Fragment fragment = null;
 
 		String account = mApplication.getSelectedAccount();
 		String username = mApplication.getUserName(account);
 
 		switch(itemPos) {
 			case ITEM_HOME_TIMELINE:
 				fragment = HomeTimelineFragment.newInstance(account);
 				break;
 
 			case ITEM_DM_RECEIVED:
 
 				break;
 
 			case ITEM_DM_SENT:
 
 				break;
 
 			case ITEM_FAVOURITES:
 				fragment = FavouritesListFragment.newInstance(account, username);
 				break;
 
 			case ITEM_FOLLOWERS:
 				fragment = FollowersFragment.newInstance(account, username);
 				break;
 
 			case ITEM_FOLLOWING:
 				fragment = FollowingFragment.newInstance(account, username);
 				break;
 
 			case ITEM_MENTIONS:
 				fragment = MentionsListFragment.newInstance(account);
 				break;
 
 			case ITEM_RETWEETS_BY:
 				fragment = RetweetsByFragment.newInstance(account, username);
 				break;
 
 			case ITEM_RETWEETS_OF:
 				fragment = RetweetsOfFragment.newInstance(account);
 				break;
 
 			case ITEM_SAVED_SEARCH:
 
 				break;
 
 			case ITEM_LISTS:
 
 				break;
 		}
 
 		/*getActivity().findViewById(R.id.secondary_container).setVisibility(View.GONE);
 		
 		for(int i = 0, size = getFragmentManager().getBackStackEntryCount(); i < size; i++) {
 			getFragmentManager().popBackStack();
 		}*/
 		
 		getFragmentManager().beginTransaction()
 		.replace(R.id.container, fragment, String.valueOf(itemPos))
 		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
 		.commit();
 	}
 
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Log.i(TAG, "Saving State: " + mSelectedItem);
 		outState.putInt(KEY_SELECTED_ITEM, mSelectedItem);
 	}
 }
