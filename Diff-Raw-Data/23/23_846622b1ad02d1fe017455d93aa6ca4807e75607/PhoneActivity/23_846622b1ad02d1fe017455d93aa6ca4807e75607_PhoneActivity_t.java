 package carnero.me.activity;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import carnero.me.Constants;
 import carnero.me.R;
 import carnero.me.Utils;
 import carnero.me.fragment.NetworksFragment;
 import carnero.me.fragment.TimelineFragment;
 import carnero.me.fragment.VcardFragment;
 import carnero.me.listener.BothEndsAnimatorListener;
 import carnero.me.listener.DisplayBeforeAnimatorListener;
 import carnero.me.listener.HideAfterAnimatorListener;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.GAServiceManager;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 import com.nineoldandroids.animation.Animator;
 import com.nineoldandroids.animation.AnimatorInflater;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 public class PhoneActivity extends SlidingFragmentActivity {
 
 	private SharedPreferences mPrefs;
 	private SlidingMenu mMenu;
 	private View mContainerVcard;
 	private View mVerticalLeft;
 	private View mVerticalRight;
 	private View mHintLeft;
 	private View mHintRight;
 	private Tracker mTracker;
 	private Animator mAnimVcardOut;
 	private Animator mAnimVerticalLeftIn;
 	private Animator mAnimVerticalRightIn;
 	private static final Handler sHandler = new Handler();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (Utils.isWide(this)) { // wide enough, switch to tablet layout
 			final Intent intent = new Intent();
 			intent.setClass(this, TabletActivity.class);
 			startActivity(intent);
 			finish();
 
 			return;
 		}
 
 		mPrefs = getPreferences(MODE_PRIVATE);
 
 		mMenu = getSlidingMenu();
 		mMenu.setMode(SlidingMenu.LEFT_RIGHT);
 		mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 		mMenu.setBehindOffset((int) getResources().getDimension(R.dimen.sliding_menu_offset));
 		mMenu.setFadeDegree(0.5f);
 		mMenu.setShadowWidthRes(R.dimen.sliding_menu_shadow);
 		mMenu.setShadowDrawable(R.drawable.shadow_left);
 		mMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);
 
 		// activity content
 		setContentView(R.layout.activity_phone);
 
 		mContainerVcard = findViewById(R.id.container_vcard);
 		mVerticalLeft = findViewById(R.id.vertical_left);
 		mVerticalRight = findViewById(R.id.vertical_right);
 		mHintLeft = findViewById(R.id.side_hint_left);
 		mHintRight = findViewById(R.id.side_hint_right);
 
 		setBehindContentView(R.layout.menu_primary);
 		mMenu.setSecondaryMenu(R.layout.menu_secondary);
 
 		if (savedInstanceState == null) {
 			// vcard
 			getSupportFragmentManager()
 					.beginTransaction()
 					.replace(R.id.container_vcard, new VcardFragment())
 					.commit();
 
 			/*
 			 *  displaying of layout waits until onCreate completes
 			 *  load another fragments little bit later to allow fast displaying of activity_phone one
 			 */
 			sHandler.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 
 					// networks
 					getSupportFragmentManager()
 							.beginTransaction()
 							.replace(R.id.container_networks, new NetworksFragment())
 							.commit();
 
 					// timeline
 					getSupportFragmentManager()
 							.beginTransaction()
 							.replace(R.id.container_timeline, new TimelineFragment())
 							.commit();
 
 				}
 			}, 250);
 		}
 
 		mMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
 			@Override
 			public void onOpened() {
 				if (mAnimVcardOut.isRunning()) {
 					mAnimVcardOut.end();
 				}
 				mAnimVcardOut.setTarget(mContainerVcard);
 				mAnimVcardOut.start();
 
 				if (mMenu.isSecondaryMenuShowing()) {
 					if (mAnimVerticalRightIn.isRunning()) {
 						mAnimVerticalRightIn.end();
 					}
 					mAnimVerticalRightIn.setTarget(mVerticalRight);
 					mAnimVerticalRightIn.start();
 				} else {
 					if (mAnimVerticalLeftIn.isRunning()) {
 						mAnimVerticalLeftIn.end();
 					}
 					mAnimVerticalLeftIn.setTarget(mVerticalLeft);
 					mAnimVerticalLeftIn.start();
 				}
 
 				final SharedPreferences.Editor edit = mPrefs.edit();
 				edit.putBoolean(Constants.PREF_SIDE_USED, true);
 				edit.commit();
 
 				if (mTracker != null) {
 					mTracker.sendEvent("activity_phone", "open", "slide_menu", 0l);
 
 					GAServiceManager.getInstance().dispatch();
 				}
 			}
 		});
 
 		mMenu.setOnClosingListener(new SlidingMenu.OnClosingListener() {
 			@Override
 			public void onClosing() {
 				if (mVerticalLeft.getVisibility() == View.VISIBLE) {
 					if (mAnimVerticalLeftIn.isRunning()) {
 						mAnimVerticalLeftIn.end();
 					}
 					mVerticalLeft.setVisibility(View.INVISIBLE);
 				} else if (mVerticalRight.getVisibility() == View.VISIBLE) {
 					if (mAnimVerticalRightIn.isRunning()) {
 						mAnimVerticalRightIn.end();
 					}
 					mVerticalRight.setVisibility(View.INVISIBLE);
 				}
 
 				if (mAnimVcardOut.isRunning()) {
 					mAnimVcardOut.end();
 				}
 				mContainerVcard.setAlpha(1.0f);
 				mContainerVcard.setVisibility(View.VISIBLE);
 			}
 		});
 	}
 
 	@Override
 	public void onPostCreate(Bundle state) {
 		super.onPostCreate(state);
 
		final boolean open;
 		final boolean secondary;
 		if (state != null) {
			open = state.getBoolean("SlidingActivityHelper.open");
 			secondary = state.getBoolean("SlidingActivityHelper.secondary");
 		} else {
			open = false;
 			secondary = false;
 		}
 
		if (open) {
			if (secondary) {
				mContainerVcard.setVisibility(View.INVISIBLE);
				mVerticalRight.setVisibility(View.VISIBLE);
			} else {
				mContainerVcard.setVisibility(View.INVISIBLE);
				mVerticalLeft.setVisibility(View.VISIBLE);
			}
 		}
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		EasyTracker.getInstance().activityStart(this);
 
 		final GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
 		mTracker = analytics.getTracker(getString(R.string.ga_trackingId));
 		mTracker.sendEvent("activity_phone", "start", "phone", 0l);
 
 		GAServiceManager.getInstance().dispatch();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		// load animations
 		mAnimVcardOut = AnimatorInflater.loadAnimator(this, R.animator.fade_out);
 		mAnimVerticalLeftIn = AnimatorInflater.loadAnimator(this, R.animator.fade_in);
 		mAnimVerticalRightIn = AnimatorInflater.loadAnimator(this, R.animator.fade_in);
 
 		mAnimVcardOut.addListener(new HideAfterAnimatorListener(mContainerVcard));
 		mAnimVerticalLeftIn.addListener(new DisplayBeforeAnimatorListener(mVerticalLeft));
 		mAnimVerticalRightIn.addListener(new DisplayBeforeAnimatorListener(mVerticalRight));
 
 		if (!mPrefs.getBoolean(Constants.PREF_SIDE_USED, false)) {
 			final Animator animatorHintLeft = AnimatorInflater.loadAnimator(this, R.animator.side_hint);
 			final Animator animatorHintRight = AnimatorInflater.loadAnimator(this, R.animator.side_hint);
 
 			animatorHintLeft.addListener(new BothEndsAnimatorListener(mHintLeft));
 			animatorHintRight.addListener(new BothEndsAnimatorListener(mHintRight));
 
 			animatorHintLeft.setTarget(mHintLeft);
 			animatorHintRight.setTarget(mHintRight);
 
 			animatorHintLeft.start();
 			animatorHintRight.start();
 		}
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 
 		EasyTracker.getInstance().activityStop(this);
 	}
 }
