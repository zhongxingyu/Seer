 package org.muckebox.android.ui.fragment;
 
 import org.muckebox.android.R;
 import org.muckebox.android.db.MuckeboxContract.CacheEntry;
 import org.muckebox.android.db.MuckeboxContract.DownloadEntry;
 import org.muckebox.android.db.MuckeboxContract.TrackDownloadCacheAlbumJoin;
 import org.muckebox.android.db.MuckeboxContract.TrackEntry;
 import org.muckebox.android.db.MuckeboxProvider;
 import org.muckebox.android.db.PlaylistHelper;
 import org.muckebox.android.net.RefreshTracksTask;
 import org.muckebox.android.services.DownloadService;
 import org.muckebox.android.services.PlayerService;
 import org.muckebox.android.ui.utils.ExpandableCursorAdapter;
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
 	
 	private static final String ALBUM_ID_ARG = "album_id";
 	private static final String ALBUM_TITLE_ARG = "album_title";
 	
 	private TrackListCursorAdapter mAdapter;
 	private boolean mListLoaded = false;
 	
 	private HandlerThread mHelperThread;
 	private Handler mHelperHandler;
 	
 	private Handler mMainHandler;
 
 	MenuItem mPinAllItem;
 	MenuItem mRemoveAllItem;
 	
 	private class TrackListCursorAdapter extends ExpandableCursorAdapter {
 		private OnClickListener mPlayListener;
 		private OnClickListener mDownloadListener;
 		private OnClickListener mPinListener;
 		private OnClickListener mDiscardListener;
 		
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
 			
 			mDownloadListener = new OnClickListener() {
 				public void onClick(View v) {
 				    DownloadService.downloadTrack(getActivity(), getTrackId(getItemIndex(v)));
 					toggleExpanded(v);
 				}
 			};
 			
 			mPinListener = new OnClickListener() {
 				public void onClick(View v) {
 					DownloadService.pinTrack(getActivity(), getTrackId(getItemIndex(v)));
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
 				ret.findViewById(R.id.track_list_download).setOnClickListener(mDownloadListener);
 				ret.findViewById(R.id.track_list_pin).setOnClickListener(mPinListener);
 				ret.findViewById(R.id.track_list_discard).setOnClickListener(mDiscardListener);
 				
 				ret.setTag(true);
 			}
 			
 			return ret;
 		}
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    mHelperThread = new HandlerThread("TrackListHelper");
 	    mHelperThread.start();
 	    
 	    mHelperHandler = new Handler(mHelperThread.getLooper());
 	    mMainHandler = new Handler();
 	}
 	
 	@Override
 	public void onDestroy() {
 	    super.onDestroy();
 	    
 	    mHelperThread.quit();
 	}
 
 	private class TracklistViewBinder implements ViewBinder {
         @SuppressLint("DefaultLocale")
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex)
         {
 			if (columnIndex == cursor.getColumnIndex(TrackEntry.ALIAS_LENGTH))
 			{
 				TextView textview = (TextView) view;
 				String text = TimeFormatter.formatDuration(
 				    cursor.getInt(columnIndex));
 				
 				textview.setText(text);
 				
 				return true;
     		} else if (columnIndex == cursor.getColumnIndex(DownloadEntry.ALIAS_STATUS))
 			{
     			ImageView icon = (ImageView) view;
     			
     			if (cursor.isNull(columnIndex))
     			{
     				icon.setVisibility(View.GONE);
     			} else
     			{
     				icon.setVisibility(View.VISIBLE);
     				
 	    			switch (cursor.getInt(columnIndex))
 	    			{
 	    			case DownloadEntry.STATUS_VALUE_QUEUED:
 	    				icon.setImageResource(R.drawable.device_access_time);
 	    				break;
 	    			case DownloadEntry.STATUS_VALUE_DOWNLOADING:
 	    				// XXX add download animation
 	    				icon.setImageResource(R.drawable.av_download);
 	    				break;
 	    			}
     			}
 	    			
     			return true;
 			} else if (columnIndex == cursor.getColumnIndex(CacheEntry.ALIAS_PINNED))
 			{
 				ImageView icon = (ImageView) view;
 				
 				if (cursor.isNull(columnIndex))
 				{
 					icon.setVisibility(View.GONE);
 				} else
 				{
 					icon.setVisibility(View.VISIBLE);
 					
 					switch (cursor.getInt(columnIndex))
 					{
 					case 0:
 						icon.setImageResource(R.drawable.navigation_accept);
 						break;
 					case 1:
 						icon.setImageResource(R.drawable.av_make_available_offline);
 						break;
 					}
 				}
 				
 				return true;
 			} else if (columnIndex ==
 					cursor.getColumnIndex(TrackDownloadCacheAlbumJoin.ALIAS_CANCELABLE))
 			{
 				boolean downloading;
 				int pinStatus;
 				
 				int downloadStatusIndex = cursor.getColumnIndex(DownloadEntry.ALIAS_STATUS);
 				int pinStatusIndex = cursor.getColumnIndex(CacheEntry.ALIAS_PINNED);
 
 				downloading = ! cursor.isNull(downloadStatusIndex);
 				pinStatus = cursor.isNull(pinStatusIndex) ? -1 : cursor.getInt(pinStatusIndex);
 				
 				ImageButton downloadButton =
 						(ImageButton) view.findViewById(R.id.track_list_download);
 				ImageButton pinButton =
 						(ImageButton) view.findViewById(R.id.track_list_pin);
 				ImageButton discardButton =
 						(ImageButton) view.findViewById(R.id.track_list_discard);
 				
 				if (! downloading && pinStatus == -1)
 				{
 					downloadButton.setVisibility(View.VISIBLE);
 					pinButton.setVisibility(View.VISIBLE);
 					discardButton.setVisibility(View.GONE);
 				} else if (downloading)
 				{
 					downloadButton.setVisibility(View.GONE);
 					pinButton.setVisibility(View.GONE);
 					discardButton.setVisibility(View.VISIBLE);
 					
 					discardButton.setImageResource(R.drawable.navigation_cancel);
 				} else
 				{
 					downloadButton.setVisibility(View.GONE);
 					pinButton.setVisibility(pinStatus == 0 ? View.VISIBLE : View.GONE);
 					discardButton.setVisibility(View.VISIBLE);
 					
 					discardButton.setImageResource(R.drawable.content_discard);
 				}
 				
 				return true;
 			}
 			
 			return false;
     	}
 	}
 	
 	public static TrackListFragment newInstanceFromAlbum(long album_id, String title) {
 		TrackListFragment f = new TrackListFragment();
 		Bundle args = new Bundle();
 		
 		args.putLong(ALBUM_ID_ARG, album_id);
 		args.putString(ALBUM_TITLE_ARG, title);
 		f.setArguments(args);
 		
 		return f;
 	}
 	
 	public long getAlbumId() {
 		Bundle args = getArguments();
 		
 		if (args == null)
 			return -1;
 		
 		return args.getLong(ALBUM_ID_ARG, -1);
 	}
 	
 	public boolean hasAlbumId() {
 		return getAlbumId() != -1;
 	}
 	
 	public String getAlbumTitle() {
 		Bundle args = getArguments();
 		
 		return args.getString(ALBUM_TITLE_ARG, "");
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
         			TrackDownloadCacheAlbumJoin.ALIAS_CANCELABLE
         			},
                 new int[] {
         			R.id.track_list_title,
         			R.id.track_list_artist,
         			R.id.track_list_duration,
         			R.id.track_list_download_status,
         			R.id.track_list_cache_status,
         			R.id.track_list_buttons,
         			}, 0);
         
         mAdapter.setViewBinder(new TracklistViewBinder());
         
         TextView header = (TextView) getActivity().findViewById(R.id.track_list_title_strip);
         
         header.setText(getAlbumTitle());
  
         setListAdapter(mAdapter);
         getLoaderManager().initLoader(0, null, this);
         
         if (! mListLoaded)
         	onRefreshRequested();
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     	super.onCreateOptionsMenu(menu, inflater);
     	
     	inflater.inflate(R.menu.track_list, menu);
     	
     	mPinAllItem = menu.findItem(R.id.track_list_action_pin);
     	mRemoveAllItem = menu.findItem(R.id.track_list_action_remove);
     	
     	mRemoveAllItem.setVisible(false);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (item == mPinAllItem)
     	{
     		Cursor c = mAdapter.getCursor();
     		int trackIdIndex = c.getColumnIndex(TrackEntry.SHORT_ID);
     		
     		c.moveToPosition(-1);
     		
     		while (c.moveToNext())
     			DownloadService.pinTrack(getActivity(), c.getInt(trackIdIndex));
     				
     		return true;
     	} else if (item == mRemoveAllItem)
     	{
     		Cursor c = mAdapter.getCursor();
     		int trackIdIndex = c.getColumnIndex(TrackEntry.SHORT_ID);
     		
     		c.moveToPosition(-1);
     		
     		while (c.moveToNext())
     			DownloadService.discardTrack(getActivity(), c.getInt(trackIdIndex));
     				
     		return true;
     	}
     	
     	return false;
     }
 
     protected void onRefreshRequested() {
 		new RefreshTracksTask().setCallbacks(this).execute(getAlbumId());
 		mListLoaded = true;
     }
 
     @Override
     public boolean onClose() {
         return true;
     }
     
     public Uri getCursorUri() {
         Uri ret;
         
         if (hasAlbumId()) {
             ret = MuckeboxProvider.URI_TRACKS_WITH_DETAILS_ALBUM.buildUpon().appendPath(
                     Long.toString(getAlbumId())).build();
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
         
         data.moveToPosition(-1);
         
         boolean oneCached = false;
         boolean oneNotPinned = false;
         
         int cachedIndex = data.getColumnIndex(CacheEntry.ALIAS_PINNED);
         int downloadingIndex = data.getColumnIndex(DownloadEntry.ALIAS_STATUS);
         
         while (data.moveToNext())
         {
         	if (data.isNull(cachedIndex) && data.isNull(downloadingIndex))
         		oneNotPinned = true;
         	else
         		oneCached = true;
         	
         	if (oneNotPinned && oneCached)
         		break;
         }
         
         if (mPinAllItem != null)
             mPinAllItem.setVisible(oneNotPinned);
         
         if (mRemoveAllItem != null)
             mRemoveAllItem.setVisible(oneCached);
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         mAdapter.swapCursor(null);
     }
 }
