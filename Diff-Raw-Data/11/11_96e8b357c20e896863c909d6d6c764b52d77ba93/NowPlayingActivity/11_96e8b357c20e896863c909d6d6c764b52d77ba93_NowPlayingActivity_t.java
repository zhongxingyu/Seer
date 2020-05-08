 package com.purplehatstands.clementine.remote;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.MediaController.MediaPlayerControl;
 import android.widget.TextView;
 
 import com.purplehatstands.libxrme.RemoteControlInterface;
 import com.purplehatstands.libxrme.State;
 
 public class NowPlayingActivity extends Activity {
 	private static final String TAG = "NowPlayingActivity";
 	TextView track_;
 	TextView artist_;
 	TextView album_;
 	ImageView album_cover_;
 	MediaController controls_view_;
 	Controls controls_;
 	private String full_jid_;
 	
 	private RemoteControlService service_;
	
	private boolean showing_controls_ = false;
 
   private ServiceConnection connection_ = new ServiceConnection() {
     public void onServiceConnected(ComponentName className, IBinder service) {
       service_ = ((RemoteControlService.LocalBinder)service).getService();
 
       service_.AddMediaStateHandler(new RemoteControlService.MediaStateHandler() {
         public void OnStateChanged(final State state) {
           NowPlayingActivity.this.runOnUiThread(new Runnable() {
             public void run() {
               track_.setText(state.metadata.title);
               artist_.setText(state.metadata.artist);
               album_.setText(state.metadata.album);
               controls_.setState(state);
             }
           });
         }
         
         public void OnAlbumArtChanged(final Bitmap image) {
           NowPlayingActivity.this.runOnUiThread(new Runnable() {
             public void run() {
               album_cover_.setImageBitmap(image);
             }
           });
         }
       });
       
     }
 
     public void onServiceDisconnected(ComponentName className) {
     }
   };
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		InitialiseUI();
 		
 		bindService(new Intent(this, RemoteControlService.class), connection_, BIND_AUTO_CREATE);
 	}
 	
 	private void InitialiseUI() {
 		setContentView(R.layout.now_playing);
 		track_ = (TextView) findViewById(R.id.track);
 		artist_ = (TextView) findViewById(R.id.artist);
 		album_ = (TextView) findViewById(R.id.album);
 		album_cover_ = (ImageView) findViewById(R.id.album_cover);
 		
 		controls_ = new Controls();
 		
 		controls_view_ = new MediaController(this, false);
 		controls_view_.setAnchorView(findViewById(R.id.now_playing_layout));
 		controls_view_.setMediaPlayer(controls_);
 		controls_view_.setEnabled(true);
 		controls_view_.setPrevNextListeners(new OnClickListener() {    
       public void onClick(View v) {
         Log.d(TAG, "Next clicked");
         service_.GetRemoteControl().Next(full_jid_);
       }
     }, new OnClickListener() {
       
       public void onClick(View v) {
         // TODO Auto-generated method stub
         
       }
     });
 	}
 	
 	@Override
 	protected void onResume() {
 	  Intent intent = getIntent();
 	  full_jid_ = (String) intent.getExtras().get("full_jid");
 	  super.onResume();
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
	  if (hasFocus && !showing_controls_) {
 	    controls_view_.show(0);
	    showing_controls_ = true;
	  } else {
	    controls_view_.clearFocus();
 	  }
 	}
 	
 	private static class Controls implements MediaPlayerControl {
 	  private State current_state_ = null;
 	  
 	  public void setState(State state) {
 	    current_state_ = state;
 	  }
 
 		public boolean canPause() {
 			if (current_state_ != null) {
 			  return current_state_.playback_state == 1;
 			}
 			return false;
 		}
 
 		public boolean canSeekBackward() {
 			if (current_state_ != null) {
 			  return current_state_.can_seek;
 			}
 			return false;
 		}
 
 		public boolean canSeekForward() {
 		  if (current_state_ != null) {
         return current_state_.can_seek;
       }
       return false;
 		}
 
 		public int getBufferPercentage() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public int getCurrentPosition() {
 			if (current_state_ != null) {
 			  return current_state_.position_millisec;
 			}
 			return 0;
 		}
 
 		public int getDuration() {
 			if (current_state_ != null) {
 			  return current_state_.length_millisec;
 			}
 			return 0;
 		}
 
 		public boolean isPlaying() {
 			if (current_state_ != null) {
 			  return current_state_.playback_state == 2;
 			}
 			return false;
 		}
 
 		public void pause() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void seekTo(int pos) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void start() {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 
 }
