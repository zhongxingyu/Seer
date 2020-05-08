 package com.quizz.places.dialogs;
 
 import android.app.Activity;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.animation.LinearInterpolator;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.ValueAnimator;
 import com.actionbarsherlock.internal.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
 import com.quizz.core.managers.DataManager;
 import com.quizz.core.models.Level;
 import com.quizz.core.models.Section;
 import com.quizz.places.R;
 import com.quizz.places.widgets.RotatingSunEffect;
 
 public class LevelSuccessDialog extends Activity {
 	public static final int RESULT_CODE_CLOSE = 1;
 	public static final int RESULT_CODE_NEXT = 2;
 	public static final int RESULT_CODE_BACK = 3;
 	
 	public static final String EXTRA_LEVEL = "LevelSuccessDialog.EXTRA_LEVEL";
 	
 	private ValueAnimator mBackgroundRotationAnimator;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 
 		// To remove the background of the 'Dialog'
 		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
 
 		Level level = getIntent().getExtras().getParcelable(EXTRA_LEVEL);
 		
 		setContentView(R.layout.dialog_level_success);
 		Button closeButton = (Button) findViewById(R.id.level_success_close_button);
 		Button nextButton = (Button) findViewById(R.id.level_success_next_button);
 		Button backButton = (Button) findViewById(R.id.level_success_back_button);
 		TextView levelName = (TextView) findViewById(R.id.level_success_response);
 		closeButton.setOnClickListener(mOnCloseButtonClickListener);
 		levelName.setText(level.response);
		
 		Section section = DataManager.getSection(level.sectionId);
 		if (section == null || !section.isComplete()) {
 			nextButton.setOnClickListener(mOnNextButtonClickListener);
 		} else {
 			nextButton.setVisibility(View.GONE);
 			backButton.setVisibility(View.VISIBLE);
 			backButton.setOnClickListener(mOnBackButtonClickListener);
 		}
 		
 		final RotatingSunEffect background = (RotatingSunEffect) findViewById(R.id.level_success_background_effect);
 		
 		mBackgroundRotationAnimator = ValueAnimator.ofFloat(0, 360);
 		mBackgroundRotationAnimator.addUpdateListener(new AnimatorUpdateListener() {
             @Override
             public void onAnimationUpdate(ValueAnimator arg0) {
 
                 background.rotationDegrees = (Float) arg0.getAnimatedValue();
                 background.invalidate();
             }
         });
 		mBackgroundRotationAnimator.setInterpolator(new LinearInterpolator());
 		mBackgroundRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
 		mBackgroundRotationAnimator.setDuration(6500);
 		mBackgroundRotationAnimator.start();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if (mBackgroundRotationAnimator != null && mBackgroundRotationAnimator.isRunning()) {
 			mBackgroundRotationAnimator.end();
 		}
 		super.onDestroy();
 	}
 	
 	public void close(int result) {
 		setResult(result);
 		finish();
 	}
 	
 	// ===========================================================
 	// Listeners
 	// ===========================================================
 	
 	OnClickListener mOnCloseButtonClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			close(RESULT_CODE_CLOSE);
 		}
 	};
 
 	OnClickListener mOnNextButtonClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			close(RESULT_CODE_NEXT);
 		}
 	};
 	
 	OnClickListener mOnBackButtonClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			close(RESULT_CODE_BACK);
 		}
 	};
 }
