 package com.serb.podpamp.ui.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.*;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.text.TextUtils;
 import android.view.*;
 import android.widget.*;
 import com.foxykeep.datadroid.requestmanager.Request;
 import com.foxykeep.datadroid.requestmanager.RequestManager;
 import com.serb.podpamp.R;
 import com.serb.podpamp.model.managers.FeedsManager;
 import com.serb.podpamp.model.provider.Contract;
 import com.serb.podpamp.model.request.FeedsRequestManager;
 import com.serb.podpamp.model.request.RequestFactory;
 import com.serb.podpamp.ui.FeedItemFilter;
 import com.serb.podpamp.utils.PlayerService;
 import com.serb.podpamp.utils.Utils;
 
 public class FeedItemDetailsActivity extends Activity implements View.OnClickListener {
 	private long itemId = -1;
 	private long feedId;
 	String filePath;
 	boolean isRead;
 	int elapsed;
 	boolean isStarred;
 
 	private FeedItemFilter filter;
 
 	private ProgressBar progressBar;
 	Button downloadButton;
 	Button starButton;
 	Button clearStarButton;
 
 	private FeedsRequestManager requestManager;
 
 	private PlayerService player;
 	private boolean isPlayerBound = false;
 
 
 	private PlayerService.PlayerListener playerListener = new PlayerService.PlayerListener() {
 		@Override
 		public void onResumed() {
 			setPlayerButtonsVisibility(true);
 		}
 
 		@Override
 		public void onPaused(int elapsed) {
 			FeedItemDetailsActivity.this.elapsed = elapsed;
 			setPlayerButtonsVisibility(false);
 		}
 
 		@Override
 		public void onFinished() {
 			setPlayerButtonsVisibility(false);
 			setupItemInfoPanel();
 		}
 
 		@Override
 		public void onPlayNext(long newItemId) {
 			if (newItemId > -1)
 			{
 				itemId = newItemId;
 				setupItemInfoPanel();
 				setPlayerButtonsVisibility(true);
 			}
 		}
 	};
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private ServiceConnection mConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
 			player = binder.getService();
 			isPlayerBound = true;
 			setPlayerButtonsVisibility(player.isPlaying());
 			player.registerClient(playerListener);
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			isPlayerBound = false;
 			setPlayerButtonsVisibility(false);
 		}
 	};
 
 
 
 
 	RequestManager.RequestListener requestListener = new RequestManager.RequestListener() {
 		@Override
 		public void onRequestFinished(Request request, Bundle resultData) {
 			hideProgress();
 			setupItemInfoPanel();
 			Toast.makeText(FeedItemDetailsActivity.this, "Download complete", Toast.LENGTH_LONG).show();
 		}
 
 		@Override
 		public void onRequestDataError(Request request) {
 			showError();
 		}
 
 		@Override
 		public void onRequestCustomError(Request request, Bundle resultData) {
 			showError();
 		}
 
 		@Override
 		public void onRequestConnectionError(Request request, int statusCode) {
 			showError();
 		}
 
 		void showError() {
 			hideProgress();
 			downloadButton.setVisibility(View.VISIBLE);
 			AlertDialog.Builder builder = new AlertDialog.Builder(FeedItemDetailsActivity.this);
 			builder.setTitle(android.R.string.dialog_alert_title)
 				.setMessage(getString(R.string.download_failed))
 				.create()
 				.show();
 		}
 	};
 
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.feed_item_details);
 
 		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
 		downloadButton = (Button) findViewById(R.id.btn_download);
 		starButton = (Button) findViewById(R.id.btn_star);
 		clearStarButton = (Button) findViewById(R.id.btn_clear_star);
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null)
 		{
 			itemId = extras.getLong("item_id");
 			filter = (FeedItemFilter)extras.getSerializable("filter");
 			filter.setShowUnreadOnly(true);
 		}
 
 		if (itemId > -1)
 		{
 			setupItemInfoPanel();
 		}
 
 		downloadButton.setOnClickListener(this);
 		findViewById(R.id.btn_mark_listened).setOnClickListener(this);
 		findViewById(R.id.btn_mark_not_listened).setOnClickListener(this);
 		findViewById(R.id.btn_play).setOnClickListener(this);
 		findViewById(R.id.btn_pause).setOnClickListener(this);
 		findViewById(R.id.btn_next).setOnClickListener(this);
 		findViewById(R.id.btn_prev).setOnClickListener(this);
 		starButton.setOnClickListener(this);
 		clearStarButton.setOnClickListener(this);
 
 		requestManager = FeedsRequestManager.from(this);
 
 		startService(new Intent(this, PlayerService.class));
 	}
 
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.feed_item_menu, menu);
 		return true;
 	}
 
 
 
 	@Override
 	public boolean onPrepareOptionsMenu (Menu menu) {
 		menu.getItem(0).setVisible(!TextUtils.isEmpty(filePath));
 		return menu.size() > 0;
 	}
 
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.mi_remove_download:
 				Utils.showConfirmationDialog(this,
 					getResources().getString(R.string.remove_download_dialog_title),
 					getResources().getString(R.string.remove_download_dialog_message),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							if (FeedsManager.removeDownload(FeedItemDetailsActivity.this, itemId, filePath))
 								setupItemInfoPanel();
 						}
 					});
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 
 	@Override
 	public void onClick(View view) {
 		switch(view.getId()) {
 			case R.id.btn_download:
 				downloadFeed();
 				break;
 			case R.id.btn_mark_listened:
 				FeedsManager.markFeedItemAsReadOrUnread(this, itemId, true);
 				setupItemInfoPanel();
 				break;
 			case R.id.btn_mark_not_listened:
 				FeedsManager.markFeedItemAsReadOrUnread(this, itemId, false);
 				setupItemInfoPanel();
 				break;
 			case R.id.btn_play:
 				if (isPlayerBound)
 					player.play(itemId, filePath, elapsed, filter);
 				break;
 			case R.id.btn_pause:
 				if (isPlayerBound)
 					player.pause();
 				break;
 			case R.id.btn_next:
 				if (isPlayerBound) {
 					//todo
 //					player.pause();
 //					player.playNext();
 				}
 				break;
 			case R.id.btn_star:
 				isStarred = true;
 				FeedsManager.star(this, itemId, isStarred, feedId);
 				setStarVisibility(true);
 				break;
 			case R.id.btn_clear_star:
 				isStarred = false;
 				FeedsManager.star(this, itemId, isStarred, feedId);
 				setStarVisibility(true);
 				break;
 		}
 	}
 
 
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Intent intent = new Intent(this, PlayerService.class);
 		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (isPlayerBound) {
 			player.unregisterClient(playerListener);
 			unbindService(mConnection);
 			isPlayerBound = false;
 		}
 	}
 
 	//region Private Methods.
 
 	private void setupItemInfoPanel() {
 		final String[] projection = {
 			Contract.FeedItems._ID,
 			Contract.FeedItems.FEED_ID,
 			Contract.FeedItems.TITLE,
 			Contract.FeedItems.DESC,
 			Contract.FeedItems.PUBLISHED,
 			Contract.FeedItems.SIZE,
 			Contract.FeedItems.IS_READ,
 			Contract.FeedItems.FILE_PATH,
 			Contract.FeedItems.ELAPSED,
 			Contract.FeedItems.IS_STARRED
 		};
 
 		final String selection = Contract.FeedItems._ID + " = ?";
 		final String[] selectionArgs = { String.valueOf(itemId) };
 
 		Cursor cursor = getContentResolver().query(Contract.FeedItems.CONTENT_URI,
 			projection, selection, selectionArgs, null);
 
 		if (cursor != null)
 		{
 			if (cursor.moveToNext())
 			{
 				String title = cursor.getString(cursor.getColumnIndex(Contract.FeedItems.TITLE));
 				String desc = cursor.getString(cursor.getColumnIndex(Contract.FeedItems.DESC));
 				long published = cursor.getLong(cursor.getColumnIndex(Contract.FeedItems.PUBLISHED));
 				long size = cursor.getLong(cursor.getColumnIndex(Contract.FeedItems.SIZE));
 				feedId = cursor.getLong(cursor.getColumnIndex(Contract.FeedItems.FEED_ID));
 				filePath = cursor.getString(cursor.getColumnIndex(Contract.FeedItems.FILE_PATH));
 				isRead = cursor.getInt(cursor.getColumnIndex(Contract.FeedItems.IS_READ)) > 0;
 				elapsed = cursor.getInt(cursor.getColumnIndex(Contract.FeedItems.ELAPSED));
 				isStarred = cursor.getInt(cursor.getColumnIndex(Contract.FeedItems.IS_STARRED)) > 0;
 
 				TextView titleView = (TextView) findViewById(R.id.txt_feed_item_title);
 				titleView.setText(title);
 				titleView.setTextColor(getResources().getColor(isRead ? R.color.read_item_color : R.color.unread_item_color));
 
				TextView descView = (TextView) findViewById(R.id.txt_feed_item_desc);
				descView.setText(desc);
 
 				TextView publishedView = (TextView) findViewById(R.id.txt_feed_item_published);
 				publishedView.setText(Utils.getDateText(published));
 				publishedView.setTextColor(getResources().getColor(isRead ? R.color.read_item_color : R.color.unread_item_color));
 
 				TextView sizeView = (TextView) findViewById(R.id.txt_feed_item_size);
 				sizeView.setText(Utils.getFileSizeText(size));
 				sizeView.setTextColor(getResources().getColor(isRead ? R.color.read_item_color : R.color.unread_item_color));
 
 				Utils.setImageView(this,
 					(ImageView) findViewById(R.id.img_feed_icon),
 					feedId,
 					R.drawable.icon_rss);
 
 				ViewGroup playerPanel = (ViewGroup) findViewById(R.id.player_panel);
 
 				if (TextUtils.isEmpty(filePath))
 				{
 					playerPanel.setVisibility(View.INVISIBLE);
 					downloadButton.setVisibility(View.VISIBLE);
 					setStarVisibility(false);
 				}
 				else
 				{
 					downloadButton.setVisibility(View.INVISIBLE);
 					playerPanel.setVisibility(View.VISIBLE);
 					setStarVisibility(true);
 				}
 
 				Button btnMarkListened = (Button) findViewById(R.id.btn_mark_listened);
 				Button btnMarkNotListened = (Button) findViewById(R.id.btn_mark_not_listened);
 				if (isRead)
 				{
 					btnMarkListened.setVisibility(View.INVISIBLE);
 					btnMarkNotListened.setVisibility(View.VISIBLE);
 				}
 				else
 				{
 					btnMarkListened.setVisibility(View.VISIBLE);
 					btnMarkNotListened.setVisibility(View.INVISIBLE);
 				}
 			}
 			cursor.close();
 		}
 	}
 
 
 
 	private void setStarVisibility(boolean isVisible) {
 		if (isVisible)
 		{
 			starButton.setVisibility(isStarred ? View.INVISIBLE : View.VISIBLE);
 			clearStarButton.setVisibility(isStarred ? View.VISIBLE : View.INVISIBLE);
 		}
 		else
 		{
 			starButton.setVisibility(View.INVISIBLE);
 			clearStarButton.setVisibility(View.INVISIBLE);
 		}
 	}
 
 
 
 	private void downloadFeed() {
 		if (itemId > -1 && Utils.isNetworkAvailable(this, true))
 		{
 			downloadButton.setVisibility(View.INVISIBLE);
 			showProgress();
 			requestManager.execute(RequestFactory.getDownloadEpisodeRequest(itemId), requestListener);
 		}
 	}
 
 
 
 	private void setPlayerButtonsVisibility(boolean isPlaying) {
 		Button playBtn = (Button) findViewById(R.id.btn_play);
 		Button pauseBtn = (Button) findViewById(R.id.btn_pause);
 
 		playBtn.setVisibility(isPlaying ? View.INVISIBLE : View.VISIBLE);
 		pauseBtn.setVisibility(isPlaying ? View.VISIBLE : View.INVISIBLE);
 	}
 
 
 
 	private void showProgress() {
 		progressBar.setVisibility(View.VISIBLE);
 	}
 
 
 
 	private void hideProgress() {
 		progressBar.setVisibility(View.INVISIBLE);
 	}
 
 	//endregion
 }
