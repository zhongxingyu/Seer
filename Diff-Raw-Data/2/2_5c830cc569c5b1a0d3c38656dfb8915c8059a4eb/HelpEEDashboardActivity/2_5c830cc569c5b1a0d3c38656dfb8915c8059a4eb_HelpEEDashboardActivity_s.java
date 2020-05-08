 package com.indago.helpme.gui.dashboard;
 
 import android.animation.Animator;
 import android.animation.AnimatorInflater;
 import android.animation.AnimatorListenerAdapter;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 
 import com.android.helpme.demo.interfaces.DrawManagerInterface;
 import com.android.helpme.demo.manager.HistoryManager;
 import com.android.helpme.demo.manager.MessageOrchestrator;
 import com.android.helpme.demo.manager.UserManager;
 import com.android.helpme.demo.utils.Task;
 import com.android.helpme.demo.utils.ThreadPool;
 import com.android.helpme.demo.utils.User;
 import com.indago.helpme.R;
 import com.indago.helpme.gui.ATemplateActivity;
 import com.indago.helpme.gui.dashboard.statemachine.HelpEEStateMachine;
 import com.indago.helpme.gui.dashboard.statemachine.STATES;
 import com.indago.helpme.gui.dashboard.views.HelpEEButtonView;
 import com.indago.helpme.gui.dashboard.views.HelpEEHintView;
 import com.indago.helpme.gui.dashboard.views.HelpEEProgressView;
 
 /**
  * 
  * @author martinmajewski
  * 
  */
 public class HelpEEDashboardActivity extends ATemplateActivity implements DrawManagerInterface {
 	private static final String LOGTAG = HelpEEDashboardActivity.class.getSimpleName();
 
 	private Handler mHandler;
 
 	private MessageOrchestrator orchestrator;
 	private ImageView mTopCover;
 	private ImageView mHelpMeLogo;
 	private Animator mFadeIn;
 	private Animator mFadeOut;
 	private HelpEEProgressView mProgressBars;
 	private HelpEEButtonView mButton;
 	private HelpEEHintView mHintViewer;
 	private HelpEEStateMachine mStateMachine;
 
 	private Vibrator mVibrator;
 	private ResetTimer mIdleTimer;
 	private MediaPlayerExitTimer mMP3Timer;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.d(LOGTAG, "onCreate()");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_help_ee_dashboard);
 
 		mHandler = new Handler();
 
 		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 
 		mTopCover = (ImageView) findViewById(R.id.iv_topcover);
 		mFadeIn = (Animator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.fade_in);
 		mFadeOut = (Animator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.fade_out);
 
 		mProgressBars = (HelpEEProgressView) findViewById(R.id.iv_help_ee_indicator);
 		mHintViewer = (HelpEEHintView) findViewById(R.id.tv_help_ee_infoarea);
 		mButton = (HelpEEButtonView) findViewById(R.id.btn_help_ee_button);
 		mHelpMeLogo = (ImageView) findViewById(R.id.iv_logo);
 
 		mStateMachine = HelpEEStateMachine.getInstance();
 		mStateMachine.addOne(mButton);
 		mStateMachine.addOne(mHintViewer);
 		mStateMachine.addOne(mProgressBars);
 		mStateMachine.setState(STATES.SHIELDED);
 
 		init();
 	}
 
 	@Override
 	protected void onResume() {
 		mStateMachine.setState(STATES.SHIELDED);
 		orchestrator.addDrawManager(DRAWMANAGER_TYPE.SEEKER, this);
 		super.onResume();
 	}
 
 	@Override
 	public void onBackPressed() {
 		if(mStateMachine.getState() == STATES.FINISHED || mStateMachine.getState() == STATES.SHIELDED) {
 			exit();
 		}
 	}
 
 	private void exit() {
 
 		if(mIdleTimer != null) {
 			mIdleTimer.dismiss();
 		}
 
 		if(mMP3Timer != null) {
 			mMP3Timer.dismiss();
 		}
 
 		orchestrator.removeDrawManager(DRAWMANAGER_TYPE.SEEKER);
 		orchestrator.removeDrawManager(DRAWMANAGER_TYPE.HELPERCOMMING);
 		ThreadPool.runTask(UserManager.getInstance().deleteUserChoice(getApplicationContext()));
 		finish();
 	}
 
 	private void init() {
 		final MediaPlayer player = MediaPlayer.create(this, R.raw.callcenter);
 
 		mHelpMeLogo.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(mStateMachine.getState() == STATES.HELP_INCOMMING) {
					mStateMachine.nextState();
 				}
 			}
 		});
 
 		mButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(mStateMachine.getState() == STATES.HELP_ARRIVED) {
 					mHandler.post(new Runnable() {
 
 						@Override
 						public void run() {
 							exit();
 						}
 					});
 				}
 			}
 		});
 
 		mButton.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if(v instanceof HelpEEButtonView) {
 					switch(event.getAction()) {
 						case MotionEvent.ACTION_DOWN:
 							if(mStateMachine.getState() != STATES.LOCKED &&
 									mStateMachine.getState() != STATES.HELP_ARRIVED &&
 									mStateMachine.getState() != STATES.FINISHED) {
 
 								mStateMachine.nextState();
 
 								switch((STATES) mStateMachine.getState()) {
 									case PART_SHIELDED:
 										if(mIdleTimer == null) {
 											mIdleTimer = new ResetTimer();
 											mIdleTimer.execute(6000L);
 										}
 										break;
 									case PRESSED:
 										if(mIdleTimer != null) {
 											mIdleTimer.dismiss();
 										}
 										break;
 									case CALLCENTER_PRESSED:
 										mMP3Timer = new MediaPlayerExitTimer();
 										mMP3Timer.execute(player);
 
 										mStateMachine.setState(STATES.FINISHED);
 
 										break;
 									default:
 										if(mIdleTimer != null) {
 											mIdleTimer.resetTime();
 										}
 										break;
 								}
 
 								mVibrator.vibrate(15);
 
 							}
 
 							break;
 						case MotionEvent.ACTION_UP:
 							if(mStateMachine.getState() == STATES.PRESSED) {
 								ButtonStateChangeDelay mBRTimer = new ButtonStateChangeDelay();
 								mBRTimer.execute(STATES.LOCKED);
 								HistoryManager.getInstance().startNewTask();
 							}
 							break;
 					}
 
 				}
 
 				return false;
 			}
 		});
 
 		orchestrator = MessageOrchestrator.getInstance();
 	}
 
 	private void reset() {
 		mTopCover.setImageResource(R.drawable.drawable_white);
 		mFadeIn.setTarget(mTopCover);
 		mFadeOut.setTarget(mTopCover);
 		mFadeOut.setStartDelay(100);
 		mFadeIn.addListener(new AnimatorListenerAdapter() {
 			@Override
 			public void onAnimationEnd(Animator animation) {
 
 				mStateMachine.setState(STATES.SHIELDED);
 
 				mFadeOut.start();
 				super.onAnimationEnd(animation);
 			}
 		});
 
 		long[] pattern = { 0, 25, 75, 25, 75, 25, 75, 25 };
 		mVibrator.vibrate(pattern, -1);
 		mFadeIn.start();
 	}
 
 	public void toHelpIncomming() {
 		if(mStateMachine.getState() == STATES.LOCKED) {
 			mTopCover.setImageResource(R.drawable.drawable_green);
 			mFadeIn.setTarget(mTopCover);
 			mFadeOut.setTarget(mTopCover);
 			mFadeOut.setStartDelay(100);
 			mFadeIn.addListener(new AnimatorListenerAdapter() {
 				@Override
 				public void onAnimationEnd(Animator animation) {
 
 					mStateMachine.setState(STATES.HELP_INCOMMING);
 
 					mFadeOut.start();
 					super.onAnimationEnd(animation);
 				}
 			});
 
 			orchestrator.removeDrawManager(DRAWMANAGER_TYPE.SEEKER);
 			orchestrator.addDrawManager(DRAWMANAGER_TYPE.HELPERCOMMING, this);
 
 			long[] pattern = { 0, 25, 75, 25, 75, 25, 75, 25 };
 			mVibrator.vibrate(pattern, -1);
 			mFadeIn.start();
 		}
 	}
 
 	public void toCallCenter() {
 		if(mStateMachine.getState() == STATES.LOCKED) {
 			mTopCover.setImageResource(R.drawable.drawable_yellow);
 			mFadeIn.setTarget(mTopCover);
 			mFadeOut.setTarget(mTopCover);
 			mFadeOut.setStartDelay(100);
 			mFadeIn.addListener(new AnimatorListenerAdapter() {
 				@Override
 				public void onAnimationEnd(Animator animation) {
 
 					mStateMachine.setState(STATES.CALLCENTER);
 
 					mFadeOut.start();
 					super.onAnimationEnd(animation);
 				}
 			});
 
 			long[] pattern = { 0, 25, 75, 25, 75, 25, 75, 25 };
 			mVibrator.vibrate(pattern, -1);
 			mFadeIn.start();
 		}
 	}
 
 	@Override
 	public void drawThis(Object object) {
 		if(object instanceof User) {
 			if(mStateMachine.getState() != STATES.HELP_INCOMMING) {
 				mHandler.post(new Runnable() {
 
 					@Override
 					public void run() {
 						toHelpIncomming();
 					}
 				});
 			}
 		}
 		if(object instanceof Task) {
 			Task task = (Task) object;
 			if(!task.isSuccsessfull()) {
 				mHandler.post(new Runnable() {
 
 					@Override
 					public void run() {
 						toCallCenter();
 					}
 				});
 			}
 			else {
 				orchestrator.removeDrawManager(DRAWMANAGER_TYPE.HELPERCOMMING);
 				mHandler.post(new Runnable() {
 
 					@Override
 					public void run() {
 						mStateMachine.setState(STATES.HELP_ARRIVED);
 //						exit();
 					}
 				});
 			}
 		}
 	}
 
 	class ResetTimer extends AsyncTask<Long, Void, Void> {
 
 		private volatile long idleTimeout = 10000;
 		private volatile boolean dismissed = false;
 
 		private long oldTime;
 
 		public ResetTimer() {}
 
 		synchronized public void resetTime() {
 			oldTime = System.currentTimeMillis();
 		}
 
 		synchronized public void dismiss() {
 			dismissed = true;
 			mIdleTimer = null;
 		}
 
 		@Override
 		protected Void doInBackground(Long... params) {
 			idleTimeout = params[0];
 			oldTime = System.currentTimeMillis();
 
 			while(!dismissed && (System.currentTimeMillis() - oldTime) <= idleTimeout) {
 				try {
 					Thread.sleep((long) (250));
 				} catch(InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if(!dismissed) {
 				reset();
 			}
 			super.onPostExecute(result);
 
 			mIdleTimer = null;
 		}
 	}
 
 	class SetStateTimer extends AsyncTask<STATES, Void, STATES> {
 
 		private volatile long idleTimeout = 10000;
 		private volatile boolean dismissed = false;
 
 		private long oldTime;
 
 		public SetStateTimer(long waitTime) {
 			idleTimeout = waitTime;
 		}
 
 		synchronized public void dismiss() {
 			dismissed = true;
 		}
 
 		@Override
 		protected STATES doInBackground(STATES... params) {
 			oldTime = System.currentTimeMillis();
 
 			while(!dismissed && (System.currentTimeMillis() - oldTime) <= idleTimeout) {
 				try {
 					Thread.sleep((long) (250));
 				} catch(InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 
 			return params[0];
 		}
 
 		@Override
 		protected void onPostExecute(STATES result) {
 			if(!dismissed && result != null) {
 				mStateMachine.setState(result);
 			}
 			super.onPostExecute(result);
 		}
 
 	}
 
 	class ButtonStateChangeDelay extends AsyncTask<STATES, Void, STATES> {
 
 		@Override
 		protected STATES doInBackground(STATES... params) {
 
 			try {
 				Thread.sleep((long) (500));
 			} catch(InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			return params[0];
 		}
 
 		@Override
 		protected void onPostExecute(STATES state) {
 			if(state != null) {
 				mStateMachine.setState(state);
 			}
 			super.onPostExecute(state);
 		}
 
 	}
 
 	class MediaPlayerExitTimer extends AsyncTask<MediaPlayer, Void, Void> {
 
 		private MediaPlayer player;
 		private volatile boolean dismissed = false;
 
 		synchronized public void dismiss() {
 			dismissed = true;
 		}
 
 		@Override
 		protected Void doInBackground(MediaPlayer... params) {
 			player = params[0];
 
 			try {
 				if(!dismissed && player != null) {
 					player.start();
 
 					while(!dismissed && player.isPlaying()) {
 						Thread.sleep(1000);
 					}
 				}
 			} catch(InterruptedException e) {
 				e.printStackTrace();
 			} catch(IllegalStateException e) {
 				Log.e(LOGTAG, "MediaPlayerExitTimer Thread - MediaPlayer throws IllegalStateException!");
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if(dismissed && player != null) {
 				player.seekTo(player.getDuration());
 				player.stop();
 				player.release();
 			}
 
 			super.onPostExecute(result);
 
 			exit();
 		}
 	}
 
 }
