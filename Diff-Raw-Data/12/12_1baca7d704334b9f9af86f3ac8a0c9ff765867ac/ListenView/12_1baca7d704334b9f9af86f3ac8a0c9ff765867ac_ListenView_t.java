 // Copyright 2009 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 // dbartists - Douban artists client for Android
 // Copyright (C) 2011 Max Lv <max.c.lv@gmail.com>
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); you may not
 // use this file except in compliance with the License.  You may obtain a copy
 // of the License at
 //
 //      http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 // WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 // License for the specific language governing permissions and limitations
 // under the License.
 //
 //
 //                           ___====-_  _-====___
 //                     _--^^^#####//      \\#####^^^--_
 //                  _-^##########// (    ) \\##########^-_
 //                 -############//  |\^^/|  \\############-
 //               _/############//   (@::@)   \\############\_
 //              /#############((     \\//     ))#############\
 //             -###############\\    (oo)    //###############-
 //            -#################\\  / VV \  //#################-
 //           -###################\\/      \//###################-
 //          _#/|##########/\######(   /\   )######/\##########|\#_
 //          |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 //          `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 //             `   `  `      `   / | |  | | \   '      '  '   '
 //                              (  | |  | |  )
 //                             __\ | |  | | /__
 //                            (vvv(VVV)(VVV)vvv)
 //
 //                             HERE BE DRAGONS
 
 package org.dbartists;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.dbartists.utils.PlaylistEntry;
 import org.dbartists.utils.PlaylistProvider;
 import org.dbartists.utils.PlaylistProvider.Items;
 
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.net.Uri;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.TextView;
 
 public class ListenView extends FrameLayout implements OnClickListener,
 		OnSeekBarChangeListener, OnDrawerOpenListener, OnDrawerCloseListener {
 
 	private class PlaybackChangeReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String title = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
 			infoText.setText(title);
 		}
 	}
 
 	private class PlaybackCloseReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			playButton.setEnabled(false);
 			playButton.setImageResource(android.R.drawable.ic_media_play);
 			progressBar.setEnabled(false);
 			progressBar.setProgress(0);
 			progressBar.setSecondaryProgress(0);
 			infoText.setText(null);
 		}
 	}
 
 	private class PlaybackUpdateReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int duration = intent
 					.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
 			int position = intent
 					.getIntExtra(PlaybackService.EXTRA_POSITION, 0);
 			int downloaded = intent.getIntExtra(
 					PlaybackService.EXTRA_DOWNLOADED, 1);
			if (player != null && player.isPlaying()) {
 				playButton.setImageResource(android.R.drawable.ic_media_pause);
			} else {
				playButton.setImageResource(android.R.drawable.ic_media_play);
 			}
 			playButton.setEnabled(true);
 			progressBar.setEnabled(true);
 			progressBar.setMax(duration);
 			progressBar.setProgress(position);
 			progressBar.setSecondaryProgress(downloaded);
 		}
 	}
 
 	private class RemotePlayReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			long id = intent.getLongExtra(Constants.EXTRA_TRACK_ID, -1);
 			String name = intent.getStringExtra(Constants.EXTRA_TRACK_NAME);
 			String url = intent.getStringExtra(Constants.EXTRA_TRACK_URL);
 			PlaylistEntry entry = new PlaylistEntry(id, url, name, true, -1);
 			if (player != null) {
 				try {
 					player.setCurrent(entry);
 					player.listen(entry.url, entry.isStream);
 				} catch (IllegalArgumentException e) {
 					Log.e(LOG_TAG, "", e);
 				} catch (IllegalStateException e) {
 					Log.e(LOG_TAG, "", e);
 				} catch (IOException e) {
 					Log.e(LOG_TAG, "", e);
 				}
 			}
 		}
 	}
 
 	private static final String LOG_TAG = ListenView.class.getName();
 	// private ImageButton streamButton;
 	private ImageButton playButton;
 	private SeekBar progressBar;
 
 	private TextView infoText;
 	private TextView lengthText;
 	private SlidingDrawer drawer;
 
 	private BroadcastReceiver changeReceiver = new PlaybackChangeReceiver();
 	private BroadcastReceiver updateReceiver = new PlaybackUpdateReceiver();
 
 	private BroadcastReceiver closeReceiver = new PlaybackCloseReceiver();
 
 	private BroadcastReceiver remotePlayReceiver = new RemotePlayReceiver();
 
 	private ServiceConnection conn;
 
 	private PlaybackService player;
 
 	public ListenView(Context context) {
 		super(context);
 	}
 
 	private void addPlaylistItem(PlaylistEntry entry) {
 		ContentValues values = new ContentValues();
 		values.put(Items.NAME, entry.title);
 		values.put(Items.URL, entry.url);
 		values.put(Items.IS_PLAYING, false);
 		values.put(Items.PLAY_ORDER,
 				PlaylistProvider.getMax(this.getContext()) + 1);
 		values.put(Items.STORY_ID, entry.artist.getId());
 		Log.d(LOG_TAG, "Adding playlist item to db");
 		Uri insert = this.getContext().getContentResolver()
 				.insert(PlaylistProvider.CONTENT_URI, values);
 		entry.id = ContentUris.parseId(insert);
 	}
 
 	protected void addToPlayList(List<PlaylistEntry> entries) {
 		for (PlaylistEntry entry : entries)
 			addPlaylistItem(entry);
 	}
 
 	public void attachToPlaybackService() {
 		Intent serviceIntent = new Intent(getContext(), PlaybackService.class);
 		conn = new ServiceConnection() {
 			@Override
 			public void onServiceConnected(ComponentName name, IBinder service) {
 				player = ((PlaybackService.ListenBinder) service).getService();
 			}
 
 			@Override
 			public void onServiceDisconnected(ComponentName name) {
 				Log.w(LOG_TAG, "DISCONNECT");
 				player = null;
 			}
 		};
 
 		// Explicitly start the service. Don't use BIND_AUTO_CREATE, since it
 		// causes an implicit service stop when the last binder is removed.
 		getContext().getApplicationContext().startService(serviceIntent);
 		getContext().getApplicationContext()
 				.bindService(serviceIntent, conn, 0);
 
 		getContext().registerReceiver(changeReceiver,
 				new IntentFilter(PlaybackService.SERVICE_CHANGE_NAME));
 		getContext().registerReceiver(updateReceiver,
 				new IntentFilter(PlaybackService.SERVICE_UPDATE_NAME));
 		getContext().registerReceiver(closeReceiver,
 				new IntentFilter(PlaybackService.SERVICE_CLOSE_NAME));
 		getContext().registerReceiver(remotePlayReceiver,
 				Constants.REMOTE_PLAY_FILTER);
 	}
 
 	private void init() {
 		View.inflate(getContext(), R.layout.listen, this);
 
 		drawer = (SlidingDrawer) findViewById(R.id.drawer);
 		drawer.setOnDrawerOpenListener(this);
 		drawer.setOnDrawerCloseListener(this);
 		playButton = (ImageButton) findViewById(R.id.StreamPlayButton);
 		playButton.setEnabled(false);
 		playButton.setOnClickListener(this);
 		// streamButton = (ImageButton) findViewById(R.id.StreamShareButton);
 		// streamButton.setOnClickListener(this);
 		// streamButton.setEnabled(false);
 
 		Button playlistButton = (Button) findViewById(R.id.StreamPlaylistButton);
 		playlistButton.setOnClickListener(this);
 
 		progressBar = (SeekBar) findViewById(R.id.StreamProgressBar);
 		progressBar.setOnSeekBarChangeListener(this);
 		progressBar.setEnabled(false);
 
 		infoText = (TextView) findViewById(R.id.StreamTextView);
 
 		lengthText = (TextView) findViewById(R.id.StreamLengthText);
 		lengthText.setText("");
 
 		attachToPlaybackService();
 	}
 
 	protected void listen(PlaylistEntry entry) {
 		if (player != null) {
 			try {
 				addPlaylistItem(entry);
 				player.setCurrent(entry);
 				player.listen(entry.url, entry.isStream);
 			} catch (IllegalArgumentException e) {
 				Log.e(LOG_TAG, "", e);
 			} catch (IllegalStateException e) {
 				Log.e(LOG_TAG, "", e);
 			} catch (IOException e) {
 				Log.e(LOG_TAG, "", e);
 			}
 		}
 	}
 
 	@Override
 	protected void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		init();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.StreamPlayButton:
 			togglePlay();
 			break;
 		case R.id.StreamPlaylistButton:
 			getContext().startActivity(
 					new Intent(getContext(), PlaylistActivity.class));
 			break;
 		// case R.id.StreamShareButton:
 		// Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
 		// // shareIntent.putExtra(Intent.EXTRA_SUBJECT, current.title);
 		// // shareIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s: %s",
 		// // current.title, current.url));
 		// shareIntent.setType("text/plain");
 		// getContext().startActivity(
 		// Intent.createChooser(shareIntent,
 		// getContext().getString(R.string.msg_share_story)));
 		// break;
 		}
 	}
 
 	@Override
 	protected void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		getContext().getApplicationContext().unbindService(conn);
 		Log.d(LOG_TAG, "detached from window");
 		try {
 			getContext().unregisterReceiver(changeReceiver);
 			getContext().unregisterReceiver(updateReceiver);
 			getContext().unregisterReceiver(closeReceiver);
 			getContext().unregisterReceiver(remotePlayReceiver);
 		} catch (IllegalArgumentException ignore) {
 			// Not yet register
 		}
 
 	}
 
 	@Override
 	public void onDrawerClosed() {
 		ImageView arrow = (ImageView) findViewById(R.id.DrawerArrowImage);
 		arrow.setImageDrawable(getResources().getDrawable(R.drawable.arrow_up));
 	}
 
 	@Override
 	public void onDrawerOpened() {
 		ImageView arrow = (ImageView) findViewById(R.id.DrawerArrowImage);
 		arrow.setImageDrawable(getResources()
 				.getDrawable(R.drawable.arrow_down));
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		int possibleProgress = progress > seekBar.getSecondaryProgress() ? seekBar
 				.getSecondaryProgress() : progress;
 		if (fromUser) {
 			// Only seek to position if we've downloaded the content.
 			int msec = player.getDuration() * possibleProgress
 					/ seekBar.getMax();
 			player.seekTo(msec);
 		}
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 	}
 
 	private void togglePlay() {
 		if (player.isPlaying()) {
 			player.pause();
 			playButton.setImageResource(android.R.drawable.ic_media_play);
 		} else {
 			player.play();
 			playButton.setImageResource(android.R.drawable.ic_media_pause);
 		}
 	}
 }
