 package in.ccl.ui;
 
 import in.ccl.adapters.GridAdapter;
 import in.ccl.database.CCLPullService;
 import in.ccl.database.VideoAlbumCursor;
 import in.ccl.helper.Util;
 import in.ccl.imageloader.EndlessScrollListener;
 import in.ccl.model.Items;
 import in.ccl.util.Constants;
 
 import java.util.ArrayList;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class VideoAlbumActivity extends TopActivity {
 
 	ArrayList <Integer> photo_albums = new ArrayList <Integer>();
 
 	private GridView gridView;
 
 	private GridAdapter adapter;
 
 	private ArrayList <Items> videoAlbumList;
 
 	private int videoGalleryId;
 
 	private DownloadStateReceiver mDownloadStateReceiver;
 
 	private IntentFilter statusIntentFilter;
 
 	@Override
 	public void onCreate (Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		addContent(R.layout.grid_layout);
 		// The filter's action is BROADCAST_ACTION
 		statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
 
 		// Sets the filter's category to DEFAULT
 		statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
 
 		// Instantiates a new DownloadStateReceiver
 		mDownloadStateReceiver = new DownloadStateReceiver();
 
 		// Registers the DownloadStateReceiver and its intent filters
 
 		String albumTitle = getIntent().getStringExtra(Constants.EXTRA_ALBUM_TITLE);
 		videoGalleryId = getIntent().getIntExtra(Constants.EXTRA_ALBUM_ID, 1);
 
 		TextView txtAlbumTitle = (TextView) findViewById(R.id.txt_album_title);
 		txtAlbumTitle.setText(albumTitle);
 		Util.setTextFont(this, txtAlbumTitle);
 		TextView txtAlbumHeader = (TextView) findViewById(R.id.txt_album_header);
 		txtAlbumHeader.setText(getResources().getString(R.string.videos));
 		Util.setTextFont(this, txtAlbumHeader);
 
 		gridView = (GridView) findViewById(R.id.photos_gridview);
 		if (getIntent().hasExtra(Constants.EXTRA_VIDEO_ITEMS)) {
 			videoAlbumList = getIntent().getParcelableArrayListExtra(Constants.EXTRA_VIDEO_ITEMS);
 		}
 
 		adapter = new GridAdapter(VideoAlbumActivity.this, videoAlbumList, "video");
 		gridView.setAdapter(adapter);
 		if (videoAlbumList.size() > 0) {
 			gridView.setOnScrollListener(new EndlessScrollListener(this, adapter, videoGalleryId, EndlessScrollListener.RequestType.VIDEO_REQUEST, videoAlbumList.get(0).getNumberOfPages()));
 		}
 		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick (AdapterView <?> arg0, View view, int position, long arg3) {
 				String packageName = "com.google.android.youtube";
 				boolean isYoutubeInstalled = isAppInstalled(packageName);
 				if (isYoutubeInstalled) {
 					String videoId = videoAlbumList.get(position).getPhotoOrVideoUrl();
 					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
 					intent.putExtra("VIDEO_ID", videoId);
 					startActivity(intent);
 				}
 				else {
 					Toast.makeText(VideoAlbumActivity.this, "Youtube application is not installed in your device.", Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onResume () {
 		// TODO Auto-generated method stub
 		super.onResume();
 		LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, statusIntentFilter);
 		if (Util.getInstance().isOnline(VideoAlbumActivity.this)) {
 			new Handler().postDelayed(new Runnable() {
 
 				@Override
 				public void run () {
 					Intent mServiceIntent = new Intent(VideoAlbumActivity.this, CCLPullService.class).setData(Uri.parse(getResources().getString(R.string.video_gallery_url) + videoGalleryId));
 					mServiceIntent.putExtra("KEY", "videos_updates");
 					startService(mServiceIntent);
 				}
 			}, 20000);
 		}
 
 	}
 
 	@Override
 	protected void onPause () {
 		// TODO Auto-generated method stub
 		super.onPause();
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadStateReceiver);
 
 	}
 
 	protected boolean isAppInstalled (String packageName) {
 		Intent mIntent = getPackageManager().getLaunchIntentForPackage(packageName);
 		if (mIntent != null) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	private class DownloadStateReceiver extends BroadcastReceiver {
 
 		private DownloadStateReceiver () {
 		}
 
 		/**
 		 * 
 		 * This method is called by the system when a broadcast Intent is matched by this class' intent filters
 		 * 
 		 * @param context An Android context
 		 * @param intent The incoming broadcast Intent
 		 */
 		@Override
 		public void onReceive (Context context, Intent intent) {
 
 			// Gets the status from the Intent's extended data, and chooses the appropriate action
 
 			switch (intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_ACTION_COMPLETE)) {
 
 				case in.ccl.database.Constants.STATE_ACTION_VIDEO_PAGES_DOWNLOAD_COMPLETE:
 					if (intent != null && intent.hasExtra("list")) {
 						ArrayList <Items> list = intent.getParcelableArrayListExtra("list");
 						adapter.updateList(list);
 					}
 					break;
 				case in.ccl.database.Constants.STATE_ACTION_VIDEO_UPDATES_COMPLETE:
 					videoAlbumList = VideoAlbumCursor.getVideos(VideoAlbumActivity.this, videoGalleryId);
 					adapter = new GridAdapter(VideoAlbumActivity.this, videoAlbumList, "video");
 					gridView.setAdapter(adapter);
 					if (videoAlbumList.size() > 0) {
 						gridView.setOnScrollListener(new EndlessScrollListener(VideoAlbumActivity.this, adapter, videoGalleryId, EndlessScrollListener.RequestType.VIDEO_REQUEST, videoAlbumList.get(0).getNumberOfPages()));
 					}
 					break;
 				default:
 					break;
 			}
 		}
 	}

 }
