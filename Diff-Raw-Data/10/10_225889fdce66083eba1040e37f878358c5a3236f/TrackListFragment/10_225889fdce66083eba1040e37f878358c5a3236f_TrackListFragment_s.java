 /*   
  * Copyright 2013 Karsten Patzwaldt
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.muckebox.android.ui.fragment;
 
 import org.muckebox.android.R;
 import org.muckebox.android.db.MuckeboxContract.CacheEntry;
 import org.muckebox.android.db.MuckeboxContract.DownloadEntry;
 import org.muckebox.android.db.MuckeboxContract.TrackDownloadCacheAlbumPlaylistJoin;
 import org.muckebox.android.db.MuckeboxContract.TrackEntry;
 import org.muckebox.android.db.MuckeboxProvider;
 import org.muckebox.android.db.PlaylistHelper;
 import org.muckebox.android.net.RefreshTracksTask;
 import org.muckebox.android.services.DownloadService;
 import org.muckebox.android.services.PlayerService;
 import org.muckebox.android.ui.utils.ExpandableCursorAdapter;
 import org.muckebox.android.ui.utils.ImageButtonHelper;
 import org.muckebox.android.ui.utils.TimeFormatter;
 import org.muckebox.android.ui.widgets.RefreshableListFragment;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.annotation.SuppressLint;
 import android.app.LoaderManager;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SearchView.OnCloseListener;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 
 public class TrackListFragment extends RefreshableListFragment
 	implements OnCloseListener,
 	LoaderManager.LoaderCallbacks<Cursor> {
 
 	private TrackListCursorAdapter mAdapter;
 	
 	private HandlerThread mHelperThread;
 	private Handler mHelperHandler;
 	
 	private Handler mMainHandler;
 
 	private MenuItem mPinAllItem;
 	private MenuItem mUnpinAllItem;
 	
 	private long mAlbumId = -1;
 	private String mAlbumTitle = null;
 	
 	private final static String STATE_ALBUM_ID = "album_id";
 	private final static String STATE_ALBUM_TITLE = "album_title";
 	
     private OnClickListener mPlayListener;
     private OnClickListener mPinListener;
     private OnClickListener mUnpinListener;
     private OnClickListener mDiscardListener;
     
 	private class TrackListCursorAdapter extends ExpandableCursorAdapter {
 
 		public TrackListCursorAdapter(Context context, int layout, Cursor c,
 				String[] from, int[] to, int flags) {
 			super(context, layout, c, from, to, flags);
 			
 			mPlayListener = new OnClickListener() {
 				public void onClick(View v) {
                     final int index = getItemIndex(v);
                     
 				    mHelperHandler.post(new Runnable() {
 				        public void run() {
 				            final Uri playlistUri = PlaylistHelper.rebuildFromTrackList(
 				                getActivity(), getCursorUri(), index);
 				            
 				            mMainHandler.post(new Runnable() {
 				                public void run() {
 				                    PlayerService.playPlaylistItem(getActivity(),
 				                        Integer.parseInt(playlistUri.getLastPathSegment()));
 				                }
 				            });
 				        }
 				    });
 				    
 					toggleExpanded(v);
 				}
 			};
 			
 			mPinListener = new OnClickListener() {
 				public void onClick(View v) {
 					DownloadService.pinTrack(getActivity(), getTrackId(getItemIndex(v)));
 					toggleExpanded(v);
 				}
 			};
 			
 			mUnpinListener = new OnClickListener() {
 				public void onClick(View v) {
 					DownloadService.unpinTrack(getActivity(), getTrackId(getItemIndex(v)));
 					toggleExpanded(v);
 				}
 			};
 			
 			mDiscardListener = new OnClickListener() {
 			    public void onClick(View v) {
 			        DownloadService.discardTrack(getActivity(), getTrackId(getItemIndex(v)));
 			        toggleExpanded(v);
 			    }
 			};
 		}
 
 		public int getTrackId(int position) {
 			Cursor c = getCursor();
 			
 			c.moveToPosition(position);
 			
 			return c.getInt(c.getColumnIndex(TrackEntry.SHORT_ID));
 		}
 		
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent)
 		{
 			View ret = super.getView(position, convertView, parent);
 			
 			if (ret.getTag() == null)
 			{
 				ret.findViewById(R.id.track_list_play).setOnClickListener(mPlayListener);
 				ret.findViewById(R.id.track_list_pin).setOnClickListener(mPinListener);
 				ret.findViewById(R.id.track_list_unpin).setOnClickListener(mUnpinListener);
 
 				ImageButtonHelper.setImageViewDisabled(
 				    getActivity(),
 				    (ImageView) ret.findViewById(R.id.track_list_play_status),
 				    R.drawable.av_play);
 				
 				ret.setTag(true);
 			}
 			
 			return ret;
 		}
 	}
 
 	private class TracklistViewBinder implements ViewBinder {
         @SuppressLint("DefaultLocale")
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex)
         {
             ImageView icon;
             TextView textView;
             
             switch (view.getId())
             {
             case R.id.track_list_duration:
 				textView = (TextView) view;
 				String text = TimeFormatter.formatDuration(
 				    cursor.getInt(columnIndex));
 				
 				textView.setText(text);
 				
 				return true;
 				
             case R.id.track_list_download_status:
     			icon = (ImageView) view;
     			
     			if (cursor.isNull(columnIndex))
     			{
     				icon.setVisibility(View.GONE);
     			} else
     			{
     				icon.setVisibility(View.VISIBLE);
     				
 	    			switch (cursor.getInt(columnIndex))
 	    			{
 	    			case DownloadEntry.STATUS_VALUE_QUEUED:
 	    			    ImageButtonHelper.setImageViewDisabled(
 	    			        getActivity(), icon, R.drawable.device_access_time);
 	    				break;
 	    			case DownloadEntry.STATUS_VALUE_DOWNLOADING:
 	    			    ImageButtonHelper.setImageViewDisabled(
 	    			        getActivity(), icon, R.drawable.av_download);
 	    				break;
 	    			}
     			}
 	    			
     			return true;
     			
             case R.id.track_list_cache_status:
 				icon = (ImageView) view;
 				
 				if (cursor.isNull(columnIndex))
 				{
 					icon.setVisibility(View.GONE);
 				} else
 				{
 					icon.setVisibility(View.VISIBLE);
 					
 					switch (cursor.getInt(columnIndex))
 					{
 					case 0:
 					    ImageButtonHelper.setImageViewDisabled(
 					        getActivity(), icon, R.drawable.navigation_accept);
 						break;
 					case 1:
 					    ImageButtonHelper.setImageViewDisabled(
                             getActivity(), icon, R.drawable.av_make_available_offline);
 						break;
 					}
 				}
 				
 				return true;
 				
             case R.id.track_list_buttons:
 				boolean downloading;
 				int pinStatus;
 				
 				int downloadStatusIndex = cursor.getColumnIndex(DownloadEntry.ALIAS_STATUS);
 				int pinStatusIndex = cursor.getColumnIndex(CacheEntry.ALIAS_PINNED);
 
 				downloading = ! cursor.isNull(downloadStatusIndex);
 				pinStatus = cursor.isNull(pinStatusIndex) ? -1 : cursor.getInt(pinStatusIndex);
 				
 				ImageButton pinButton =
 						(ImageButton) view.findViewById(R.id.track_list_pin);
 				ImageButton unpinButton =
 						(ImageButton) view.findViewById(R.id.track_list_unpin);
 				
 				if (! downloading && pinStatus == -1)
 				{
 					pinButton.setVisibility(View.VISIBLE);
 					unpinButton.setVisibility(View.GONE);
 				} else if (downloading)
 				{
 					pinButton.setVisibility(View.GONE);
 					unpinButton.setVisibility(View.VISIBLE);
 					
 					unpinButton.setOnClickListener(mDiscardListener);
 				} else
 				{
 					pinButton.setVisibility(pinStatus == 0 ? View.VISIBLE : View.GONE);
 					unpinButton.setVisibility(pinStatus == 1 ? View.VISIBLE : View.GONE);
 					
 					unpinButton.setOnClickListener(mUnpinListener);
 				}
 				
 				return true;
 				
             case R.id.track_list_play_status:
 			    int playing = cursor.getInt(columnIndex);
 			    
 			    view.setVisibility(playing > 0 ? View.VISIBLE : View.GONE);
 			    
 			    return true;
 			}
 			
 			return false;
     	}
 	}
 	
 	public static TrackListFragment newInstanceFromAlbum(long album_id, String title) {
 		TrackListFragment f = new TrackListFragment();
 		
 		f.mAlbumId = album_id;
 		f.mAlbumTitle = title;
 		
 		return f;
 	}
 	
 	public boolean hasAlbumId() {
 		return mAlbumId != -1;
 	}
 	   
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (savedInstanceState != null) {
             mAlbumId = savedInstanceState.getLong(STATE_ALBUM_ID);
             mAlbumTitle = savedInstanceState.getString(STATE_ALBUM_TITLE);
         }
         
         mHelperThread = new HandlerThread("TrackListHelper");
         mHelperThread.start();
         
         mHelperHandler = new Handler(mHelperThread.getLooper());
         mMainHandler = new Handler();
     }
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         outState.putLong(STATE_ALBUM_ID, mAlbumId);
         outState.putString(STATE_ALBUM_TITLE, mAlbumTitle);
     }
     
     @Override
     public void onDestroy() {
         super.onDestroy();
         
         mHelperThread.quit();
     }
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_track_browse, container, false);
 	}
 
 	@Override
     public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
         setHasOptionsMenu(true);
 
         mAdapter = new TrackListCursorAdapter(getActivity(),
                 R.layout.list_row_track, null,
                 new String[] {
         			TrackEntry.ALIAS_TITLE,
         			TrackEntry.ALIAS_DISPLAY_ARTIST,
         			TrackEntry.ALIAS_LENGTH,
         			DownloadEntry.ALIAS_STATUS,
         			CacheEntry.ALIAS_PINNED,
         			TrackDownloadCacheAlbumPlaylistJoin.ALIAS_CANCELABLE,
         			TrackDownloadCacheAlbumPlaylistJoin.ALIAS_PLAYING
         			},
                 new int[] {
         			R.id.track_list_title,
         			R.id.track_list_artist,
         			R.id.track_list_duration,
         			R.id.track_list_download_status,
         			R.id.track_list_cache_status,
         			R.id.track_list_buttons,
         			R.id.track_list_play_status
         			}, 0);
         
         mAdapter.setViewBinder(new TracklistViewBinder());
         
         setListAdapter(mAdapter);
 
         if (hasAlbumId())
             reInit(mAlbumId, mAlbumTitle);
     }
 	
 	public void reInit(long albumId, String albumTitle) {
 	    mAlbumId = albumId;
 	    mAlbumTitle = albumTitle;
 	    
         TextView header = (TextView) getActivity().findViewById(R.id.track_list_title_strip);
         
         header.setText(mAlbumTitle);
 	    
 	    getLoaderManager().restartLoader(0, null, this);
 	}
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     	super.onCreateOptionsMenu(menu, inflater);
     	
     	mPinAllItem = menu.findItem(R.id.track_list_action_pin);
     	
     	if (mPinAllItem == null) {
     	    inflater.inflate(R.menu.track_list, menu);
     	    mPinAllItem = menu.findItem(R.id.track_list_action_pin);
     	}
     	
     	mUnpinAllItem = menu.findItem(R.id.track_list_action_unpin);
     	
     	mPinAllItem.setVisible(false);
     	mUnpinAllItem.setVisible(false);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (super.onOptionsItemSelected(item))
             return true;
         
     	if (item == mPinAllItem)
     	{
     		Cursor c = mAdapter.getCursor();
     		int trackIdIndex = c.getColumnIndex(TrackEntry.SHORT_ID);
     		
     		c.moveToPosition(-1);
     		
     		while (c.moveToNext())
     			DownloadService.pinTrack(getActivity(), c.getInt(trackIdIndex));
     				
     		return true;
     	} else if (item == mUnpinAllItem)
     	{
     		Cursor c = mAdapter.getCursor();
     		int trackIdIndex = c.getColumnIndex(TrackEntry.SHORT_ID);
     		
     		c.moveToPosition(-1);
     		
     		while (c.moveToNext())
     			DownloadService.unpinTrack(getActivity(), c.getInt(trackIdIndex));
     				
     		return true;
     	}
     	
     	return false;
     }
 
     protected void onRefreshRequested() {
 		new RefreshTracksTask().setCallbacks(this).execute(mAlbumId);
     }
 
     @Override
     public boolean onClose() {
         return true;
     }
     
     public Uri getCursorUri() {
         Uri ret;
         
         if (hasAlbumId()) {
             ret = MuckeboxProvider.URI_TRACKS_WITH_DETAILS_ALBUM.buildUpon().appendPath(
                     Long.toString(mAlbumId)).build();
         } else {
             ret = MuckeboxProvider.URI_TRACKS_WITH_DETAILS;
         }
         
         return ret;
     }
 
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         return new CursorLoader(getActivity(), getCursorUri(), null, null, null, null);
     }
 
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         mAdapter.swapCursor(data);
         
         if (data.getCount() == 0 && ! wasRefreshedOnce()) {
             onRefreshRequested();
         }
         
         data.moveToPosition(-1);
 
         boolean oneNotPinned = false;
         
         int pinnedIndex = data.getColumnIndex(CacheEntry.ALIAS_PINNED);
         int downloadingIndex = data.getColumnIndex(DownloadEntry.ALIAS_STATUS);
         
         while (data.moveToNext())
         {
         	if ((data.isNull(pinnedIndex) || data.getInt(pinnedIndex) == 0) &&
         	    data.isNull(downloadingIndex)) {
         		oneNotPinned = true;
         		break;
         	}
         }
 
         if (mPinAllItem != null)
             mPinAllItem.setVisible(oneNotPinned);
         
         if (mUnpinAllItem != null)
             mUnpinAllItem.setVisible(! oneNotPinned);
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         mAdapter.swapCursor(null);
     }
 }
