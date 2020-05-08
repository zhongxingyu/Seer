 package com.DGSD.TweeterTweeter.Fragments;
 
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.ActionMode;
 import android.view.ActionMode.Callback;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 import com.DGSD.TweeterTweeter.R;
 import com.DGSD.TweeterTweeter.StatusData;
 import com.DGSD.TweeterTweeter.Receivers.PortableReceiver.Receiver;
 import com.DGSD.TweeterTweeter.Services.UpdaterService;
 import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
 import com.DGSD.TweeterTweeter.UI.Adapters.TimelineCursorAdapter;
 import com.DGSD.TweeterTweeter.Utils.ListUtils;
 import com.DGSD.TweeterTweeter.Utils.Log;
 
 public abstract class BaseStatusFragment extends BaseFragment {
 
 	private static final String TAG = BaseStatusFragment.class.getSimpleName();
 
 	protected static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_SCREEN_NAME,
 		StatusData.C_TEXT, StatusData.C_IMG, StatusData.C_ID};
 
 	protected static final int[] TO = {R.id.timeline_date, R.id.timeline_source, R.id.timeline_tweet,
 		R.id.timeline_profile_image }; 
 
 	@Override
 	public Callback getCallback(int pos) {
 		return new StatusCallback(pos);
 	}
 
 	@Override
 	public void onListItemClick(int pos) {
 		//Show the view
 		Fragment sideMenu = getFragmentManager().findFragmentById(R.id.side_menu);
 		
 		//getFragmentManager().beginTransaction().hide(sideMenu).addToBackStack(null).commit();
 		
 		Fragment newFragment = TweetFragment.newInstance(StatusData.getStatus(getCurrentCursor()));
 		
 		//Insert the fragment!
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		
 		if(mCurrentFragmentTag != null) {
 			try {
 				Fragment f = getFragmentManager().findFragmentByTag(mCurrentFragmentTag);
 				if(f != null) {
 					ft.remove(f);
 				} else {
 					System.err.println("F WAS NULL!!!!!!!!");
 				}
 			} catch(IllegalStateException e) {
 				Log.e(TAG, "Error removing mCurrentFragment", e);
 			}
 		} else {
 			System.err.println("mCurrentFragment WAS NULL!");
 		}
 		
 		mCurrentFragmentTag = String.valueOf(new Random().nextInt());
 		ft.add(R.id.data_container, newFragment, mCurrentFragmentTag);
		ft.commit();
 		
 		if(sideMenu != null && !sideMenu.isHidden()) {
			getFragmentManager().beginTransaction()
								.hide(sideMenu)
								.addToBackStack(null)
								.commit();
 		}
 	}
 
 	@Override
 	public Receiver getReceiver() {
 		return new Receiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				int dataType = intent.getIntExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.ALL_DATA);
 
 				String account = intent.getStringExtra(UpdaterService.ACCOUNT);
 
 				if(intent.getAction().equals(UpdaterService.SEND_DATA)) {
 					Log.v(TAG, "Data Received");
 					startRefresh(dataType, account);
 				}
 				else if(intent.getAction().equals(UpdaterService.NO_DATA)) {
 					Log.v(TAG, "No Data Received");
 					stopRefresh(dataType, account);
 				} 
 				else if(intent.getAction().equals(UpdaterService.ERROR)) {
 					stopRefresh(dataType, account);
 				}
 				else {
 					Log.v(TAG, "Received Mystery Intent: " + intent.getAction());
 				}
 			}
 		};
 	}
 
 	@Override
 	public SimpleCursorAdapter getListAdapter(Cursor cursor) {
 		return new TimelineCursorAdapter(mApplication, R.layout.timeline_list_item, 
 				cursor, FROM, TO);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 		View root = inflater.inflate(R.layout.list_fragment_layout, container, false);
 
 		mListView = (PullToRefreshListView) root.findViewById(R.id.list);
 
 		Log.i(TAG, "Returning root from onCreateView");
 
 		return root;
 	}
 	
 	
 
 	private class StatusCallback implements ActionMode.Callback {
 
 		private String mTweetId;
 
 		private String mScreenName;
 
 		private String mTweetText;
 
 		final String[] userEntities;
 
 		private boolean hasError;
 
 		public StatusCallback(int pos) {
 			hasError = false;
 
 			mTweetId = ListUtils.getTweetProperty(getCurrentCursor(), 
 					StatusData.C_ID, pos);
 
 			mScreenName = ListUtils.getTweetProperty(getCurrentCursor(), 
 					StatusData.C_SCREEN_NAME, pos);
 
 			mTweetText = ListUtils.getTweetProperty(getCurrentCursor(), 
 					StatusData.C_TEXT, pos);
 
 			userEntities = ListUtils.getTweetProperty(getCurrentCursor(), 
 					StatusData.C_USER_ENT, pos).split(",");
 
 			if(mTweetId == "" || mTweetId == null) {
 				//We couldn't get a tweet id :(
 				Toast.makeText(getActivity(), 
 						"Error getting data for tweet", Toast.LENGTH_SHORT).show();
 
 				hasError = true;
 			}
 		}
 
 		@Override
 		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
 			if(hasError) {
 				return false;
 			} else {
 				actionMode.setTitle(mScreenName + "'s tweet");
 
 				MenuInflater inflater = getActivity().getMenuInflater();
 				inflater.inflate(R.menu.status_context_menu, menu);
 				return true;
 			}
 		}
 
 		@Override
 		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
 			return false;
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
 
 			boolean handled = false;
 			switch (menuItem.getItemId()) {
 				case R.id.menu_reply:
 					if(userEntities.length > 0 && userEntities[0].length() > 0) {
 						//We might want to reply to all mentioned users?
 						final CharSequence[] choices = {"Reply", "Reply to all"};
 
 						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 						builder.setTitle("Reply");
 
 						builder.setItems(choices, new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int item) {
 								String val = "";
 								switch(item) {
 									case 0: 
 										//Reply
 										val = "@" + mScreenName;
 										break;
 									case 1:
 										//Reply to all
 										val = "@" + mScreenName;
 										for(String s : userEntities) {
 											if(s.length() > 0) {
 												val += " @" + s;
 											}
 										}
 										break;
 								}
 
 								NewTweetFragment.newInstance(mApplication.getSelectedAccount(),
 										val + " ").show(getActivity().getFragmentManager(), null);
 							}
 						});
 
 						builder.create().show();
 					} else {
 						//There are no other mentioned users, lets just reply
 						NewTweetFragment.newInstance(mApplication.getSelectedAccount(), 
 								"@" + mScreenName + " ").show(getActivity().getFragmentManager(), null);
 					}
 
 					handled = true;
 					break;
 
 				case R.id.menu_retweet:
 					final CharSequence[] choices = {"Retweet", "Retweet and edit"};
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 					builder.setTitle("Retweet");
 
 					builder.setItems(choices, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int item) {
 							switch(item) {
 								case 0: 
 									Toast.makeText(getActivity(), "Retweet!", 
 											Toast.LENGTH_SHORT).show();
 									break;
 								case 1:
 									//Retweet & edit
 									NewTweetFragment.newInstance(mApplication.getSelectedAccount(),
 											"RT " + mTweetText).show(getActivity().getFragmentManager(), null);
 									break;
 							}
 						}
 					});
 
 					builder.create().show();
 
 					handled = true;
 					break;
 
 				case R.id.menu_favourite:
 					System.err.println("FAVOURITE FOR: " + mTweetId);
 					Intent intent = new Intent(getActivity(), UpdaterService.class);
 					intent.putExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.NEW_FAVOURITE);
 					intent.putExtra(UpdaterService.ACCOUNT, mAccountId);
 					intent.putExtra(UpdaterService.TWEETID, mTweetId);
 					getActivity().startService(intent);
 
 					handled = true;
 					break;
 
 				case R.id.menu_share:
 					Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 					sharingIntent.setType("text/plain");
 					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mTweetText);
 					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tweet by " + mScreenName);
 					getActivity().startActivity(Intent.createChooser(sharingIntent, "Share tweet"));
 
 					handled = true;
 					break;
 			}
 
 			actionMode.finish();
 
 			return handled;
 		}
 
 		@Override
 		public void onDestroyActionMode(ActionMode actionMode) {
 			mCurrentActionMode = null;
 		}
 
 	}
 }
