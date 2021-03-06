 package org.linphone;
 /*
 InCallActivity.java
 Copyright (C) 2012  Belledonne Communications, Grenoble, France
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 import java.util.Arrays;
 import java.util.List;
 
 import org.linphone.LinphoneSimpleListener.LinphoneOnCallEncryptionChangedListener;
 import org.linphone.LinphoneSimpleListener.LinphoneOnCallStateChangedListener;
 import org.linphone.compatibility.Compatibility;
 import org.linphone.core.LinphoneCall;
 import org.linphone.core.LinphoneCall.State;
 import org.linphone.core.LinphoneCallParams;
 import org.linphone.core.LinphoneCore;
 import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
 import org.linphone.ui.Numpad;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 /**
  * @author Sylvain Berfini
  */
 public class InCallActivity extends FragmentActivity implements
 									LinphoneOnCallStateChangedListener,
 									LinphoneOnCallEncryptionChangedListener,
 									OnClickListener {
 	private final static int SECONDS_BEFORE_HIDING_CONTROLS = 3000;
 	private static InCallActivity instance;
 	
 	private Handler mHandler = new Handler();
 	private Handler mControlsHandler = new Handler();
 	private Runnable mControls;
 	private ImageView video, micro, speaker, addCall, pause, hangUp, dialer, switchCamera, options, transfer;
 	private StatusFragment status;
 	private AudioCallFragment audioCallFragment;
 	private VideoCallFragment videoCallFragment;
 	private boolean isSpeakerEnabled = false, isMicMuted = false, isVideoEnabled, isTransferAllowed, isAnimationDisabled;
 	private LinearLayout mControlsLayout;
 	private Numpad numpad;
 	private int cameraNumber;
 	private Animation slideOutLeftToRight, slideInRightToLeft, slideInBottomToTop, slideInTopToBottom, slideOutBottomToTop, slideOutTopToBottom;
 	
 	public static InCallActivity instance() {
 		return instance;
 	}
 	
 	public static boolean isInstanciated() {
 		return instance != null;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		instance = this;
 		
 		Compatibility.setFullScreen(getWindow());
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         setContentView(R.layout.incall);
         
         isVideoEnabled = getIntent().getExtras() != null && getIntent().getExtras().getBoolean("VideoEnabled");
         isTransferAllowed = getApplicationContext().getResources().getBoolean(R.bool.allow_transfers);
         isAnimationDisabled = getApplicationContext().getResources().getBoolean(R.bool.disable_animations);
         cameraNumber = AndroidCameraConfiguration.retrieveCameras().length;
         
         if (findViewById(R.id.fragmentContainer) != null) {
             initUI();
             
             if (LinphoneManager.getLc().getCallsNb() > 0) {
             	LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
 
             	if (LinphoneUtils.isCallEstablished(call)) {
 	    			isVideoEnabled = call.getCurrentParamsCopy().getVideoEnabled();
 	    			enableAndRefreshInCallActions();
             	}
             }
             
             if (savedInstanceState != null) { 
             	// Fragment already created, no need to create it again (else it will generate a memory leak with duplicated fragments)
             	return;
             }
             
             Fragment callFragment;
             if (isVideoEnabled) {
             	callFragment = new VideoCallFragment();
             	videoCallFragment = (VideoCallFragment) callFragment;
             	
             	if (cameraNumber > 1) {
             		switchCamera.setVisibility(View.VISIBLE); 
             	}
             } else {
             	callFragment = new AudioCallFragment();
             	audioCallFragment = (AudioCallFragment) callFragment;
         		switchCamera.setVisibility(View.INVISIBLE);
             }
             callFragment.setArguments(getIntent().getExtras());
             getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, callFragment).commitAllowingStateLoss();
         }
 	}
 	
 	private void initUI() {
 		video = (ImageView) findViewById(R.id.video);
 		video.setOnClickListener(this);
 		video.setEnabled(false);
 		micro = (ImageView) findViewById(R.id.micro);
 		micro.setOnClickListener(this);
 		micro.setEnabled(false);
 		speaker = (ImageView) findViewById(R.id.speaker);
 		speaker.setOnClickListener(this);
 		speaker.setEnabled(false);
 		addCall = (ImageView) findViewById(R.id.addCall);
 		addCall.setOnClickListener(this);
 		addCall.setEnabled(false);
 		transfer = (ImageView) findViewById(R.id.transfer);
 		transfer.setOnClickListener(this);
 		transfer.setEnabled(false);
 		options = (ImageView) findViewById(R.id.options);
 		options.setOnClickListener(this);
 		options.setEnabled(false);
 		pause = (ImageView) findViewById(R.id.pause);
 		pause.setOnClickListener(this);
 		pause.setEnabled(false);
 		hangUp = (ImageView) findViewById(R.id.hangUp);
 		hangUp.setOnClickListener(this);
 		dialer = (ImageView) findViewById(R.id.dialer);
 		dialer.setOnClickListener(this);
 		dialer.setEnabled(false);
 		numpad = (Numpad) findViewById(R.id.numpad);
 		
 		switchCamera = (ImageView) findViewById(R.id.switchCamera);
 		switchCamera.setOnClickListener(this);
 		
 		mControlsLayout = (LinearLayout) findViewById(R.id.menu);
 		
         if (!isTransferAllowed) {
         	addCall.setImageResource(R.drawable.options_add_call);
         }
 
         if (!isAnimationDisabled) {
 	        slideInRightToLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_to_left);
 	        slideOutLeftToRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_left_to_right);
 	        slideInBottomToTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom_to_top);
 	        slideInTopToBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_top_to_bottom);
 	        slideOutBottomToTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom_to_top);
 	        slideOutTopToBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_top_to_bottom);
         }
 	}
 	
 	private void enableAndRefreshInCallActions() {
 		mHandler.post(new Runnable() {
 			@Override
 			public void run() {
 				options.setEnabled(true);
 				video.setEnabled(true);
 				micro.setEnabled(true);
 				speaker.setEnabled(true);
 				addCall.setEnabled(true);
 				transfer.setEnabled(true);
 				pause.setEnabled(true);
 				dialer.setEnabled(true);
 
 				if (!isVideoActivatedInSettings()) {
 					video.setEnabled(false);
 				} else {
 					if (isVideoEnabled) {
 			        	video.setImageResource(R.drawable.video_on);
 					} else {
 						video.setImageResource(R.drawable.video_off);
 					}
 				}
 				
 				if (isSpeakerEnabled) {
 					speaker.setImageResource(R.drawable.speaker_on);
 				} else {
 					speaker.setImageResource(R.drawable.speaker_off);
 				}
 				
 				if (isMicMuted) {
 					micro.setImageResource(R.drawable.micro_off);
 				} else {
 					micro.setImageResource(R.drawable.micro_on);
 				}
 			}
 		});
 	}
 
 	public void updateStatusFragment(StatusFragment statusFragment) {
 		status = statusFragment;
 	}
 	
 	private boolean isVideoActivatedInSettings() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean settingsVideoEnabled = prefs.getBoolean(getString(R.string.pref_video_enable_key), false);
 		return settingsVideoEnabled;
 	}
 
 	@Override
 	public void onClick(View v) {
 		int id = v.getId();
 		
 		if (isVideoEnabled) {
 			displayVideoCallControlsIfHidden();
 		}
 		
 		if (id == R.id.video) {
 			isVideoEnabled = !isVideoEnabled;
 			switchVideo(isVideoEnabled);
 		} 
 		else if (id == R.id.micro) {
 			toogleMicro();
 		} 
 		else if (id == R.id.speaker) {
 			toogleSpeaker();
 		} 
 		else if (id == R.id.addCall) {
 			goBackToDialer();
 		} 
 		else if (id == R.id.pause) {
 			pause();
 		} 
 		else if (id == R.id.hangUp) {
 			hangUp();
 		} 
 		else if (id == R.id.dialer) {
 			hideOrDisplayNumpad();
 		}
 		else if (id == R.id.switchCamera) {
 			if (videoCallFragment != null) {
 				videoCallFragment.switchCamera();
 			}
 		}
 		else if (id == R.id.transfer) {
 			goBackToDialerAndDisplayTransferButton();
 		}
 		else if (id == R.id.options) {
 			hideOrDisplayCallOptions();
 		} 
 	}
 
 	
 	private void switchVideo(final boolean displayVideo) {
 		final LinphoneCall call = LinphoneManager.getLc().getCurrentCall();
 		if (call == null) {
 			return;
 		}
 		
 		mHandler.post(new Runnable() {
 			@Override
 			public void run() {
 				if (!displayVideo) {
 					LinphoneCallParams params = call.getCurrentParamsCopy();
 					params.setVideoEnabled(false);
 					LinphoneManager.getLc().updateCall(call, params);
 					replaceFragmentVideoByAudio();
 					
 					video.setImageResource(R.drawable.video_on);
 					setCallControlsVisibleAndRemoveCallbacks();
 					
 				} else {
 					if (!call.getCurrentParamsCopy().getVideoEnabled()) {
 						LinphoneManager.getInstance().addVideo();
 					}
 					
 					isSpeakerEnabled = true;
 					LinphoneManager.getInstance().routeAudioToSpeaker();
 					speaker.setImageResource(R.drawable.speaker_on);
 					
 					replaceFragmentAudioByVideo();
 					video.setImageResource(R.drawable.video_off);
 					displayVideoCallControlsIfHidden();
 				}
 			}
 		});
 	}
 	
 	private void replaceFragmentVideoByAudio() {
 		audioCallFragment = new AudioCallFragment();
 		
 		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
 		transaction.replace(R.id.fragmentContainer, audioCallFragment);
 		try {
 			transaction.commitAllowingStateLoss();
 		} catch (Exception e) {
 		}
 	}
 	
 	private void replaceFragmentAudioByVideo() {
 //		Hiding controls to let displayVideoCallControlsIfHidden add them plus the callback
 		mControlsLayout.setVisibility(View.GONE);
 		switchCamera.setVisibility(View.INVISIBLE);
 		
 		videoCallFragment = new VideoCallFragment();
 		
 		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
 		transaction.replace(R.id.fragmentContainer, videoCallFragment);
 		try {
 			transaction.commitAllowingStateLoss();
 		} catch (Exception e) {
 		}
 	}
 	
 	private void toogleMicro() {
 		LinphoneCore lc = LinphoneManager.getLc();
 		isMicMuted = !isMicMuted;
 		lc.muteMic(isMicMuted);
 		if (isMicMuted) {
 			micro.setImageResource(R.drawable.micro_off);
 		} else {
 			micro.setImageResource(R.drawable.micro_on);
 		}
 	}
 	
 	private void toogleSpeaker() {
 		isSpeakerEnabled = !isSpeakerEnabled;
 		if (isSpeakerEnabled) {
 			LinphoneManager.getInstance().routeAudioToSpeaker();
 			speaker.setImageResource(R.drawable.speaker_on);
 		} else {
 			LinphoneManager.getInstance().routeAudioToReceiver();
 			speaker.setImageResource(R.drawable.speaker_off);
 		}
 		LinphoneManager.getLc().enableSpeaker(isSpeakerEnabled);
 	}
 	
 	private void pause() {
 		LinphoneCore lc = LinphoneManager.getLc();
 		LinphoneCall call = lc.getCurrentCall();
 		if (call != null && LinphoneUtils.isCallRunning(call)) {
 			lc.pauseCall(call);
 			pause.setImageResource(R.drawable.pause_on);
 		} else {
 			List<LinphoneCall> pausedCalls = LinphoneUtils.getCallsInState(lc, Arrays.asList(State.Paused));
 			if (pausedCalls.size() == 1) {
 				LinphoneCall callToResume = pausedCalls.get(0);
 				lc.resumeCall(callToResume);
 				pause.setImageResource(R.drawable.pause_off);
 			}
 		}
 	}
 	
 	private void hangUp() {
 		LinphoneCore lc = LinphoneManager.getLc();
 		LinphoneCall currentCall = lc.getCurrentCall();
 		
 		if (currentCall != null) {
 			lc.terminateCall(currentCall);
 		} else if (lc.isInConference()) {
 			lc.terminateConference();
 		} else {
 			lc.terminateAllCalls();
 		}
 	}
 	
 	public void displayVideoCallControlsIfHidden() {
 		if (mControlsLayout != null) {
 			if (mControlsLayout.getVisibility() == View.GONE) {
 				if (isAnimationDisabled) {
 					mControlsLayout.setVisibility(View.VISIBLE);
 					if (cameraNumber > 1) {
 	            		switchCamera.setVisibility(View.VISIBLE); 
 	            	}
 				} else {
 					Animation animation = slideInBottomToTop;
 					animation.setAnimationListener(new AnimationListener() {
 						@Override
 						public void onAnimationStart(Animation animation) {
 							mControlsLayout.setVisibility(View.VISIBLE);
 							if (cameraNumber > 1) {
 			            		switchCamera.setVisibility(View.VISIBLE); 
 			            	}
 						}
 						
 						@Override
 						public void onAnimationRepeat(Animation animation) {
 						}
 						
 						@Override
 						public void onAnimationEnd(Animation animation) {
 							animation.setAnimationListener(null);
 						}
 					});
 					mControlsLayout.startAnimation(animation);
 					if (cameraNumber > 1) {
 						switchCamera.startAnimation(slideInTopToBottom);
 					}
 				}
 				
 				resetControlsHidingCallBack();
 			}
 		}		
 	}
 
 	public void resetControlsHidingCallBack() {
 		if (mControlsHandler != null && mControls != null) {
 			mControlsHandler.removeCallbacks(mControls);
 		}
 		mControls = null;
 		
 		if (isVideoEnabled) {
 			mControlsHandler.postDelayed(mControls = new Runnable() {
 				public void run() {
 					hideNumpad();
 					
 					if (isAnimationDisabled) {
 						transfer.setVisibility(View.INVISIBLE);
 						addCall.setVisibility(View.INVISIBLE);
 						mControlsLayout.setVisibility(View.GONE);
 						switchCamera.setVisibility(View.INVISIBLE);
 						options.setImageResource(R.drawable.options);
 					} else {					
 						Animation animation = slideOutTopToBottom;
 						animation.setAnimationListener(new AnimationListener() {
 							@Override
 							public void onAnimationStart(Animation animation) {
 								video.setEnabled(false); // HACK: Used to avoid controls from being hided if video is switched while controls are hiding
 							}
 							
 							@Override
 							public void onAnimationRepeat(Animation animation) {
 							}
 							
 							@Override
 							public void onAnimationEnd(Animation animation) {
 								video.setEnabled(true); // HACK: Used to avoid controls from being hided if video is switched while controls are hiding
 								transfer.setVisibility(View.INVISIBLE);
 								addCall.setVisibility(View.INVISIBLE);
 								mControlsLayout.setVisibility(View.GONE);
 								switchCamera.setVisibility(View.INVISIBLE);
 								options.setImageResource(R.drawable.options);
 								
 								animation.setAnimationListener(null);
 							}
 						});
 						mControlsLayout.startAnimation(animation);
 						if (cameraNumber > 1) {
 							switchCamera.startAnimation(slideOutBottomToTop);
 						}
 					}
 				}
 			}, SECONDS_BEFORE_HIDING_CONTROLS);
 		}
 	}
 
 	public void setCallControlsVisibleAndRemoveCallbacks() {
 		if (mControlsHandler != null && mControls != null) {
 			mControlsHandler.removeCallbacks(mControls);
 		}
 		mControls = null;
 		
 		mControlsLayout.setVisibility(View.VISIBLE);
 		switchCamera.setVisibility(View.INVISIBLE);
 	}
 	
 	private void hideNumpad() {
 		if (numpad == null || numpad.getVisibility() != View.VISIBLE) {
 			return;
 		}
 			
 		dialer.setImageResource(R.drawable.dialer_alt);
 		if (isAnimationDisabled) {
 			numpad.setVisibility(View.GONE);
 		} else {
 			Animation animation = slideOutTopToBottom;
 			animation.setAnimationListener(new AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation animation) {
 					
 				}
 				
 				@Override
 				public void onAnimationRepeat(Animation animation) {
 					
 				}
 				
 				@Override
 				public void onAnimationEnd(Animation animation) {
 					numpad.setVisibility(View.GONE);
 					animation.setAnimationListener(null);
 				}
 			});
 			numpad.startAnimation(animation);
 		}
 	}
 	
 	private void hideOrDisplayNumpad() {
 		if (numpad == null) {
 			return;
 		}
 		
 		if (numpad.getVisibility() == View.VISIBLE) {
 			hideNumpad();
 		} else {	
 			dialer.setImageResource(R.drawable.dialer_alt_back);	
 			if (isAnimationDisabled) {
 				numpad.setVisibility(View.VISIBLE);
 			} else {
 				Animation animation = slideInBottomToTop;
 				animation.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationStart(Animation animation) {
 						
 					}
 					
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 						
 					}
 					
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						numpad.setVisibility(View.VISIBLE);
 						animation.setAnimationListener(null);
 					}
 				});
 				numpad.startAnimation(animation);
 			}
 		}
 	}
 	
 	private void hideOrDisplayCallOptions() {
 		if (addCall.getVisibility() == View.VISIBLE) {
 			options.setImageResource(R.drawable.options);
 			if (isAnimationDisabled) {
 				if (isTransferAllowed) {
 					transfer.setVisibility(View.INVISIBLE);
 				}
 				addCall.setVisibility(View.INVISIBLE);
 			} else {
 				Animation animation = slideOutLeftToRight;
 				animation.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationStart(Animation animation) {
 						
 					}
 					
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 						
 					}
 					
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						if (isTransferAllowed) {
 							transfer.setVisibility(View.INVISIBLE);
 						}
 						addCall.setVisibility(View.INVISIBLE);
 						animation.setAnimationListener(null);
 					}
 				});
 				if (isTransferAllowed) {
 					transfer.startAnimation(animation);
 				}
 				addCall.startAnimation(animation);
 			}
 		} else {		
 			if (getResources().getBoolean(R.bool.disable_animations)) {
 				if (isTransferAllowed) {
 					transfer.setVisibility(View.VISIBLE);
 				}
 				addCall.setVisibility(View.VISIBLE);
 				options.setImageResource(R.drawable.options_alt);
 			} else {
 				Animation animation = slideInRightToLeft;
 				animation.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationStart(Animation animation) {
 						
 					}
 					
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 						
 					}
 					
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						options.setImageResource(R.drawable.options_alt);
 						if (isTransferAllowed) {
 							transfer.setVisibility(View.VISIBLE);
 						}
 						addCall.setVisibility(View.VISIBLE);
 						animation.setAnimationListener(null);
 					}
 				});
 				if (isTransferAllowed) {
 					transfer.startAnimation(animation);
 				}
 				addCall.startAnimation(animation);
 			}
 			transfer.setEnabled(LinphoneManager.getLc().getCurrentCall() != null);
 		}
 	}
 	
 	public void goBackToDialer() {
 		Intent intent = new Intent();
 		intent.putExtra("Transfer", false);
 		setResult(Activity.RESULT_FIRST_USER, intent);
 		finish();
 	}
 	
 	private void goBackToDialerAndDisplayTransferButton() {
 		Intent intent = new Intent();
 		intent.putExtra("Transfer", true);
 		setResult(Activity.RESULT_FIRST_USER, intent);
 		finish();
 	}
 
 	@Override
 	public void onCallStateChanged(LinphoneCall call, State state, String message) {
 		if (LinphoneManager.getLc().getCallsNb() == 0) {
 			finish();
 			return;
 		}
 		
 		if (state == State.StreamsRunning) {     
 			boolean isVideoEnabledInCall = call.getCurrentParamsCopy().getVideoEnabled();
 			if (isVideoEnabledInCall != isVideoEnabled) {
 				isVideoEnabled = isVideoEnabledInCall;
 				switchVideo(isVideoEnabled);
 			}
 			
 			isMicMuted = LinphoneManager.getLc().isMicMuted();
 			enableAndRefreshInCallActions();
 		}
 		
 		if (audioCallFragment != null) {
 			mHandler.post(new Runnable() {
 				@Override
 				public void run() {
 					audioCallFragment.refreshCallList(getResources());
 				}
 			});
 		}
 		
 		transfer.setEnabled(LinphoneManager.getLc().getCurrentCall() != null);
 	}
 
 	@Override
 	public void onCallEncryptionChanged(LinphoneCall call, boolean encrypted, String authenticationToken) {
 		if (status != null) {
 			status.refreshStatusItems();
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		LinphoneManager.addListener(this);
 		LinphoneManager.startProximitySensorForActivity(this);
 		
 		if (isVideoEnabled) {
 			displayVideoCallControlsIfHidden();
 		} else {
 			setCallControlsVisibleAndRemoveCallbacks();
 		}
 		
 		super.onResume();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		if (mControlsHandler != null && mControls != null) {
 			mControlsHandler.removeCallbacks(mControls);
 		}
 		mControls = null;
 		
 		LinphoneManager.stopProximitySensorForActivity(this);
 		LinphoneManager.removeListener(this);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if (mControlsHandler != null && mControls != null) {
 			mControlsHandler.removeCallbacks(mControls);
 		}
 		mControls = null;
 		mControlsHandler = null;
 		mHandler = null;
 		
 		unbindDrawables(findViewById(R.id.topLayout));
 		instance = null;
 		super.onDestroy();
 	    System.gc();
 	}
 	
 	private void unbindDrawables(View view) {
         if (view.getBackground() != null) {
         	view.getBackground().setCallback(null);
         }
         if (view instanceof ImageView) {
         	view.setOnClickListener(null);
         }
         if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
             for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
             	unbindDrawables(((ViewGroup) view).getChildAt(i));
             }
             ((ViewGroup) view).removeAllViews();
         }
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (LinphoneUtils.onKeyVolumeAdjust(keyCode)) return true;
 // 		if (LinphoneUtils.onKeyBackGoHome(this, keyCode, event)) return true;
  		return super.onKeyDown(keyCode, event);
  	}
 
 	public void bindAudioFragment(AudioCallFragment fragment) {
 		audioCallFragment = fragment;
 	}
 
 	public void bindVideoFragment(VideoCallFragment fragment) {
 		videoCallFragment = fragment;
 	}
 }
