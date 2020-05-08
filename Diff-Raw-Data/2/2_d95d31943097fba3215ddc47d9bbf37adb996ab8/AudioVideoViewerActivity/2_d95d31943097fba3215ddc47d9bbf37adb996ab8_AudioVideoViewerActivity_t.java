 /*
  *  Copyright (C) 2012 Simon Robinson
  * 
  *  This file is part of Com-Me.
  * 
  *  Com-Me is free software; you can redistribute it and/or modify it 
  *  under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation; either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  Com-Me is distributed in the hope that it will be useful, but WITHOUT 
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
  *  Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with Com-Me.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ac.robinson.mediatablet.activity;
 
 import java.io.File;
 import java.io.IOException;
 
 import ac.robinson.mediatablet.MediaTablet;
 import ac.robinson.mediatablet.MediaViewerActivity;
 import ac.robinson.mediatablet.R;
 import ac.robinson.mediatablet.provider.MediaTabletProvider;
 import ac.robinson.util.IOUtilities;
 import ac.robinson.util.UIUtilities;
 import ac.robinson.view.CustomMediaController;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.VideoView;
 
 public class AudioVideoViewerActivity extends MediaViewerActivity {
 
 	private VideoView mVideoAudioPlayer;
 	private CustomMediaController mMediaController;
 	private boolean mControllerPrepared;
 	private boolean mMediaPrepared;
 
 	@Override
 	protected void initialiseView(Bundle savedInstanceState) {
 		setContentView(R.layout.audio_video_viewer);
 
 		File mediaFile = getCurrentMediaFile(); // guaranteed to exist and not to be null
 
 		// can't play from private data directory, and can't use file descriptors like we do for narratives; instead,
 		// copy to temp before playback (this will take a *long* time)
		File publicFile = mediaFile;
 		if (IOUtilities.isInternalPath(mediaFile.getAbsolutePath())) {
 			try {
 				if (MediaTablet.DIRECTORY_TEMP != null) {
 					publicFile = new File(MediaTablet.DIRECTORY_TEMP, mediaFile.getName());
 					IOUtilities.copyFile(mediaFile, publicFile);
 					IOUtilities.setFullyPublic(publicFile);
 				} else {
 					throw new IOException();
 				}
 			} catch (IOException e) {
 				UIUtilities.showToast(AudioVideoViewerActivity.this, R.string.error_loading_media);
 				finish();
 				return;
 			}
 		}
 
 		mControllerPrepared = false;
 		mMediaPrepared = false;
 
 		mVideoAudioPlayer = (VideoView) findViewById(R.id.media_audio_video);
 		mVideoAudioPlayer.setOnCompletionListener(new OnCompletionListener() {
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 				if (mControllerPrepared) {
 					mMediaController.show(0);
 				}
 			}
 		});
 		mVideoAudioPlayer.setOnPreparedListener(new OnPreparedListener() {
 			@Override
 			public void onPrepared(MediaPlayer mp) {
 				mMediaPrepared = true;
 				startPlayer();
 			}
 		});
 		mVideoAudioPlayer.setVideoURI(Uri.fromFile(publicFile));
 
 		mMediaController = new CustomMediaController(this);
 
 		RelativeLayout parentLayout = (RelativeLayout) findViewById(R.id.audio_video_view_parent);
 		RelativeLayout.LayoutParams controllerLayout = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		controllerLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		parentLayout.addView(mMediaController, controllerLayout);
 
 		if (getCurrentMediaType() == MediaTabletProvider.TYPE_AUDIO) {
 			View audioBackground = findViewById(R.id.media_audio_image);
 			audioBackground.setVisibility(View.VISIBLE);
 			mMediaController.setAnchorView(audioBackground);
 		} else {
 			mMediaController.setAnchorView(findViewById(R.id.media_audio_video));
 		}
 
 		mControllerPrepared = true;
 	}
 
 	@Override
 	protected void onDestroy() {
 		UIUtilities.releaseKeepScreenOn(getWindow());
 		if (mMediaController != null) {
 			mMediaController.hide();
 			((RelativeLayout) findViewById(R.id.audio_video_view_parent)).removeView(mMediaController);
 			mMediaController = null;
 		}
 		super.onDestroy();
 	}
 
 	private void startPlayer() {
 		if (mControllerPrepared && mMediaPrepared) {
 			UIUtilities.acquireKeepScreenOn(getWindow());
 			mVideoAudioPlayer.start();
 			mMediaController.setMediaPlayer(new CustomMediaController.MediaPlayerControl() {
 				@Override
 				public void start() {
 					UIUtilities.acquireKeepScreenOn(getWindow());
 					mVideoAudioPlayer.start();
 				}
 
 				@Override
 				public void pause() {
 					UIUtilities.releaseKeepScreenOn(getWindow());
 					mVideoAudioPlayer.pause();
 				}
 
 				@Override
 				public int getDuration() {
 					return mVideoAudioPlayer.getDuration();
 				}
 
 				@Override
 				public int getCurrentPosition() {
 					return mVideoAudioPlayer.getCurrentPosition();
 				}
 
 				@Override
 				public void seekTo(int pos) {
 					mVideoAudioPlayer.seekTo(pos);
 				}
 
 				@Override
 				public boolean isPlaying() {
 					return mVideoAudioPlayer.isPlaying();
 				}
 
 				@Override
 				public boolean isLoading() {
 					return mVideoAudioPlayer.isPlaying();
 				}
 
 				@Override
 				public int getBufferPercentage() {
 					return 0;
 				}
 
 				@Override
 				public boolean canPause() {
 					return true;
 				}
 
 				@Override
 				public boolean canSeekBackward() {
 					return true;
 				}
 
 				@Override
 				public boolean canSeekForward() {
 					return true;
 				}
 
 				@Override
 				public void onControllerVisibilityChange(boolean visible) {
 				}
 			});
 			mMediaController.show(0); // 0 for permanent visibility TODO: hide playback controls after default timeout
 		}
 	}
 }
