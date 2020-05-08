 package com.virtualroadside.grouptagger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 
 import org.json.JSONException;
 
 import com.example.android.musicplayer.MusicRetriever.Item;
 import com.example.android.musicplayer.MusicService;
 import com.example.android.musicplayer.MusicService.MusicEventNotification;
 import com.example.android.musicplayer.MusicService.MusicServiceBinder;
 import com.example.android.musicplayer.MusicService.State;
 import com.virtualroadside.grouptagger.MainActivity.HasTitle;
 import com.virtualroadside.grouptagger.tagging.TagCategories;
 import com.virtualroadside.grouptagger.tagging.TagCategories.Tag;
 import com.virtualroadside.grouptagger.tagging.TagCategories.TagCategory;
 import com.virtualroadside.grouptagger.tagging.TagUtil;
 import com.virtualroadside.grouptagger.ui.MultiViewListAdapter;
 import com.virtualroadside.grouptagger.ui.MultiViewListAdapter.Row;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.support.v4.app.Fragment;
 import android.util.Log;
import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.CompoundButton;
 import android.widget.ExpandableListView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TagViewFragment extends Fragment implements HasTitle
 {
 	private static final String TAG = "TagViewFragment";
 	
 	TagCategories defaultCategories = null;
 	TagCategories currentTags = new TagCategories();
 	
 	ExpandableListView listView;
 	TagViewAdapter mAdapter;
 	
 	TextView mTitleText;
 	TextView mArtistText;
 	
 	ImageButton mPlayButton;
 	ImageButton mPrevButton;
 	ImageButton mNextButton;
 	ImageButton mStopButton;
 	
 	MusicService mService;
 	
 	SeekBar mMusicSeekBar;
 	
 	// item currently being played
 	Item mCurrentItem = null;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		// load the default tags
 		new TagLoader().execute();
 		
 		View view = inflater.inflate(R.layout.fragment_tag_view, container, false);
 		
 		listView = (ExpandableListView)view.findViewById(R.id.tagListView);
 		
 		mTitleText = (TextView)view.findViewById(R.id.tag_file_title);
 		mArtistText = (TextView)view.findViewById(R.id.tag_file_artist);
 		
 		mPlayButton = (ImageButton)view.findViewById(R.id.tag_play_button);
 		mPrevButton = (ImageButton)view.findViewById(R.id.tag_prev_button);
 		mNextButton = (ImageButton)view.findViewById(R.id.tag_next_button);
 		mStopButton = (ImageButton)view.findViewById(R.id.tag_stop_button);
 		
 		mMusicSeekBar = (SeekBar)view.findViewById(R.id.tag_music_bar);
 		mMusicSeekBar.setOnTouchListener(onSeekBarTouched);
 		
 		mPlayButton.setOnClickListener(onPlayClicked);
 		mPrevButton.setOnClickListener(onPrevClicked);
 		mNextButton.setOnClickListener(onNextClicked);
 		mStopButton.setOnClickListener(onStopClicked);
 		
 		return view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		
 		mAdapter = new TagViewAdapter();
 		listView.setAdapter(mAdapter);
 		
 		// connect to the service
 		Intent intent = new Intent(getActivity(), MusicService.class);
 		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 		
 		Log.i(TAG, "onStop");
 		
 		saveCurrentItem();
 		
 		if (mService != null)
 		{
 			Log.i(TAG, "unbind");
 			mService.unsubscribeForNotifications(mEventNotification);
 			getActivity().unbindService(mConnection);
 			mService = null;
 		}
 		
 		cancelProgressHandler();
 		mCurrentItem = null;
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public String getTitle() 
 	{
 		return "GroupTagger";
 	}
 	
 	public void saveCurrentItem()
 	{
 		if (mCurrentItem == null)
 			return;
 		
 		String tagString = currentTags.getSelectedAsString();
 		
 		Log.i(TAG, "Saving tags: " + tagString);
 		
 		File tagsFile = Util.getFileFromUri(getActivity(), mCurrentItem.getURI());
 		TagUtil.setTagsOnFileIfChanged(tagsFile, tagString);
 	}
 	
 	public void showTags()
 	{
 		// reset the categories to defaults
 		if (defaultCategories != null)
 		{
 			currentTags = new TagCategories(defaultCategories);
 		}
 
 		// load the tags for the currently selected file, and set them
 		if (mCurrentItem != null)
 		{
 			File tagsFile = Util.getFileFromUri(getActivity(), mCurrentItem.getURI());
 			String tags = TagUtil.getTagsFromFile(tagsFile);
 			
 			currentTags.setSelectedFromString(tags);
 		}
 		
 		// change it for the user
 		if (mAdapter != null)
 			mAdapter.notifyDataSetChanged();
 	}
 	
 	
 	// load all tags
 	class TagLoader extends AsyncTask<Void, Void, TagCategories>
 	{
 		String errString = null;
 
 		@Override
 		protected TagCategories doInBackground(Void... params) 
 		{
 			// todo: improve.. 
 			String tagPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/tags.json";
 			FileInputStream fStream;
 			try 
 			{
 				fStream = new FileInputStream(tagPath);
 				return TagCategories.loadJson(fStream);
 			} 
 			catch (FileNotFoundException e1) 
 			{
 				errString = tagPath + " not found!";
 			} 
 			catch (JSONException e) 
 			{
 				errString = "Error reading tags.json!";
 			}
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(TagCategories result) 
 		{
 			if (result == null)
 			{
 				Toast.makeText(getActivity(), errString, Toast.LENGTH_LONG).show();
 			}
 			
 			defaultCategories = result;
 			showTags();
 		}
 		
 	}
 	
 	//
 	// Service connection
 	//
 	
 	ServiceConnection mConnection = new ServiceConnection() 
 	{	
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) 
 		{
 			Log.i(TAG, "onServiceConnect");
 			
 			MusicServiceBinder binder = (MusicServiceBinder)service;
 			mService = binder.getService();
 			
 			mService.subscribeForNotifications(mEventNotification);
 		}
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) 
 		{
 			mService = null;
 		}
 	};
 	
 	//
 	// Audio player synchronization
 	//
 	
 	MusicEventNotification mEventNotification = new MusicEventNotification() 
 	{	
 		@Override
 		public void onStateChange(State newState, Item newItem) 
 		{
 			// update the tag display to show the current item
 			
 			if (newItem != null)
 			{
 				if (!newItem.equals(mCurrentItem))
 				{
 					saveCurrentItem();
 					mCurrentItem = newItem;
 					
 					showTags();
 				}
 				
 				mTitleText.setText(newItem.getTitle());
 				mArtistText.setText(newItem.getArtist());
 			}
 			else 
 			{
 				if (mCurrentItem != null)
 				{
 					saveCurrentItem();
 
 					mCurrentItem = null;
 					showTags();
 				}
 				
 				mTitleText.setText("No file selected");
 				mArtistText.setText(null);
 			}
 			
 			switch (newState)
 			{
 				case Paused:
 					mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
 					mStopButton.setEnabled(true);
 					mMusicSeekBar.setEnabled(true);
 					cancelProgressHandler();
 					break;
 					
 				case Playing:
 					mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause));
 					mStopButton.setEnabled(true);
 					mMusicSeekBar.setEnabled(true);
 					mMusicSeekBar.setMax(mService.getTrackDuration());
 					progressUpdater.run();
 					break;
 					
 				case Stopped:
 					mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
 					mStopButton.setEnabled(false);
 					mMusicSeekBar.setEnabled(false);
 					mMusicSeekBar.setProgress(0);
 					break;
 				
 				default:
 					break;
 			}
 		}
 	};
 	
 	
 	// on new track started!
 	
 	// connect buttons to service
 	OnClickListener onPlayClicked = new OnClickListener() 
 	{	
 		@Override
 		public void onClick(View v) 
 		{
 			getActivity().startService(new Intent(MusicService.ACTION_TOGGLE_PLAYBACK));
 		}
 	};
 	
 	OnClickListener onPrevClicked = new OnClickListener() 
 	{	
 		@Override
 		public void onClick(View v) 
 		{
 			getActivity().startService(new Intent(MusicService.ACTION_PREV));
 		}
 	};
 	
 	OnClickListener onNextClicked = new OnClickListener() 
 	{	
 		@Override
 		public void onClick(View v) 
 		{
 			getActivity().startService(new Intent(MusicService.ACTION_NEXT));
 		}
 	};
 	
 	OnClickListener onStopClicked = new OnClickListener() 
 	{	
 		@Override
 		public void onClick(View v) 
 		{
 			getActivity().startService(new Intent(MusicService.ACTION_STOP));
 		}
 	};
 	
 	//
 	// SeekBar synchronization
 	//
 	
 	OnTouchListener onSeekBarTouched = new OnTouchListener() 
 	{	
 		@Override
 		public boolean onTouch(View v, MotionEvent event) 
 		{
 			mService.seekTo(mMusicSeekBar.getProgress());
 			return false;
 		}
 	};
 	
 	Handler progressHandler = new Handler();
 	Runnable progressUpdater = new Runnable() 
 	{	
 		@Override
 		public void run() 
 		{
 			if (mCurrentItem != null)
 			{
 				mMusicSeekBar.setProgress(mService.getTrackPosition());
 				progressHandler.postDelayed(this, 1000);
 			}
 		}
 	};
 	
 	void cancelProgressHandler()
 	{
 		progressHandler.removeCallbacks(progressUpdater);
 	}
 	
 	// other crap.. 
 	
 	// load tags from track
 	
 	// interpolate tags into array...
 	
 	// notify list view that things changed
 	
 	class TagViewAdapter extends BaseExpandableListAdapter
 	{
		float padding;
		
		public TagViewAdapter()
		{
			super();
			
			TypedValue value = new TypedValue();
			getActivity().getTheme().resolveAttribute(android.R.attr.expandableListPreferredItemPaddingLeft, value, true);
			padding = value.getDimension(getResources().getDisplayMetrics());
		}
 
 		@Override
 		public int getGroupCount() 
 		{
 			return currentTags.getCategoryCount();
 		}
 
 		@Override
 		public int getChildrenCount(int groupPosition) 
 		{
 			return currentTags.getTagCount(groupPosition);
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) 
 		{
 			return currentTags.getCategory(groupPosition);
 		}
 
 		@Override
 		public Object getChild(int groupPosition, int childPosition) 
 		{
 			return currentTags.getTag(groupPosition, childPosition);
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) 
 		{
 			return groupPosition;
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) 
 		{
 			return childPosition;
 		}
 
 		@Override
 		public boolean hasStableIds() 
 		{
 			return false;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
 		{
 			TextView textView;
 			
 			if (convertView == null)
 			{
 				textView = new TextView(getActivity());
 				textView.setTypeface(null, Typeface.BOLD);
				
				
				textView.setPadding((int)padding, 0, 0, 0);
 			}
 			else
 			{
 				textView = (TextView)convertView;
 			}
 			
 			TagCategory category = (TagCategory)getGroup(groupPosition);
 			
 			textView.setText(category.name);
 			return textView;
 
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) 
 		{
 			CheckBox checkBox;
 			
 			if (convertView == null)
 			{
 				checkBox = new CheckBox(getActivity());
 				checkBox.setTextSize(0);
 				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() 
 				{	
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
 					{
 						Tag tag = (Tag)buttonView.getTag();
 						tag.selected = isChecked;
 					}
 				});
 			}
 			else
 			{
 				checkBox = (CheckBox)convertView;
 			}
 			
 			Tag tag = (Tag)getChild(groupPosition, childPosition);
 			
 			checkBox.setTag(tag);
 			checkBox.setText(tag.name);
 			checkBox.setChecked(tag.selected);
 			
 			return checkBox;
 		}
 
 		@Override
 		public boolean isChildSelectable(int groupPosition, int childPosition) 
 		{
 			return false;
 		}
 		
 	}
 	
 }
