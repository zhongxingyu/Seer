 package com.cs371m.notesync;
 
 import android.widget.MediaController.MediaPlayerControl;
 
 public class PlaybackController implements MediaPlayerControl{
 
 	PlaybackService service;
 	String mediaPath;
 	
 	public PlaybackController(PlaybackService ps) {
 		service = ps;
 	}
 	
 	public void setMediaPath(String path) {
 		mediaPath = path;
 	}
 	
 	@Override
 	public boolean canPause() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean canSeekBackward() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean canSeekForward() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public int getAudioSessionId() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getBufferPercentage() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getCurrentPosition() {
 		// TODO Auto-generated method stub
 		if (service != null) {
 			if (service.mPlayer != null) {
 				return service.mPlayer.getCurrentPosition();
 			}
 		}
 		return 0;
 	}
 
 	@Override
 	public int getDuration() {
 		// TODO Auto-generated method stub
 		if (service != null) {
 			if (service.mPlayer != null) {
 				return service.mPlayer.getDuration();
 			}
 		}
 		return 0;
 	}
 
 	@Override
 	public boolean isPlaying() {
 		// TODO Auto-generated method stub
 		if (service != null) {
 			if (service.mPlayer != null) {
 				return service.mPlayer.isPlaying();
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void pause() {
 		if (service != null)
 			service.Pause();
 	}
 
 	@Override
 	public void seekTo(int arg0) {
 		if (service != null)
 			service.Seek(arg0);
 	}
 
 	@Override
 	public void start() {
		if (service != null)
 			service.Play(mediaPath);
 	}
 	
 	public void stop() {
 		if (service != null)
 			service.Stop();
 	}
 }
