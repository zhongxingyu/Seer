 package com.quizz.places.activities;
 
 import java.util.List;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.animation.LinearInterpolator;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 import com.quizz.core.activities.BaseQuizzActivity;
 import com.quizz.core.models.Section;
 import com.quizz.core.utils.NavigationUtils;
 import com.quizz.core.widgets.QuizzActionBar;
 import com.quizz.places.R;
 import com.quizz.places.db.GameDataLoading;
 import com.quizz.places.db.GameDataLoading.GameDataLoadingListener;
 import com.quizz.places.fragments.MenuFragment;
 
 public class QuizzActivity extends BaseQuizzActivity implements GameDataLoadingListener {
 
 	public static final String DEBUG_TAG = QuizzActivity.class.getSimpleName();
 	
 	private ObjectAnimator mBgAnimation;
 	private GameDataLoadingListener mGameDataLoadingListener;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (savedInstanceState == null) {
 			// First launch of the activity, not a rotation change
 			getQuizzActionBar().hide(QuizzActionBar.MOVE_DIRECT);
 			Log.e("ASYNC", "initAsyncGameLoading: "+System.currentTimeMillis());
 			initAsyncGameLoading();
 		}
 
 		getQuizzActionBar().getBackButton().setImageResource(
 				R.drawable.back_but_5);
 		getQuizzActionBar().setCustomView(R.layout.ab_view_sections);
 		getQuizzActionBar().setBackgroundResource(R.drawable.bg_actionbar);
 
 		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
 		NavigationUtils.directNavigationTo(MenuFragment.class,
 				getSupportFragmentManager(), this, false, transaction);
 		
 		initBackground();
 	}
 
 	private void initAsyncGameLoading() {
 		GameDataLoading gdl = new GameDataLoading(this);
		gdl.executeAsyncGameLoading(gdl.isFirstLaunch());
		
 //			gdl.initPreferences();
		/*} else if (gdl.isDbUpgradeNeeded()) {
 			Log.d(DEBUG_TAG, "upgrade is needed");
 			// Handle upgrade stuff here
 			gdl.upgradeVersionInPreferences();
 		} else {
 			Log.d(DEBUG_TAG, "upgrade is NOT needed");
 			
		}*/
 	}
 
 	public void setGameDataLoadingListener(GameDataLoadingListener listener) {
 		mGameDataLoadingListener = listener;
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if (mBgAnimation != null && mBgAnimation.isRunning()) {
 			mBgAnimation.end();
 		}
 		super.onDestroy();
 	}
 
 	private void initBackground() {
 		getBackgroundAnimatedImage().setBackgroundResource(R.drawable.clouds);
 
 		mBgAnimation = ObjectAnimator.ofFloat(getBackgroundAnimatedImage(),
 				"translationX", 500, -1700);
 		mBgAnimation.setDuration(30000);
 		mBgAnimation.setInterpolator(new LinearInterpolator());
 		mBgAnimation.setRepeatCount(ObjectAnimator.INFINITE);
 		mBgAnimation.start();
 	}
 
 	@Override
 	public void onGameLoadingStart() {
 		if (mGameDataLoadingListener != null) {
 			mGameDataLoadingListener.onGameLoadingStart();
 		}
 	}
 	
 	@Override
 	public void onGameLoadingProgress(int progress) {
 		if (mGameDataLoadingListener != null) {
 			mGameDataLoadingListener.onGameLoadingProgress(progress);
 		}
 	}
 
 	@Override
 	public void onGameLoadingSuccess(List<Section> sections) {
 		Log.e("ASYNC", "onGameLoadingSuccess: "+System.currentTimeMillis());
 		if (mGameDataLoadingListener != null) {
 			Log.e("ASYNC", "onGameLoadingSuccess mGameDataLoadingListener: "+System.currentTimeMillis());
 			mGameDataLoadingListener.onGameLoadingSuccess(sections);
 		}
 	}
 
 	@Override
 	public void onGameLoadingFailure(Exception e) {
 		if (mGameDataLoadingListener != null) {
 			mGameDataLoadingListener.onGameLoadingFailure(e);
 		}		
 	}
 }
