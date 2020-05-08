 package com.quizz.core.widgets;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
 import com.actionbarsherlock.internal.nineoldandroids.animation.Animator.AnimatorListener;
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 import com.quizz.core.R;
 import com.quizz.core.utils.AnimatorUtils;
 
 public class QuizzActionBar extends RelativeLayout {
 
 	public static final int MOVE_DIRECT = 1;
 	public static final int MOVE_NORMAL = 250;
 
 	private ImageButton mBackButton;
 	private ViewGroup mCustomView;
 	private View mShadow;
 
 	private ObjectAnimator mAbAnimation = new ObjectAnimator();
 	private ObjectAnimator mShadowAnimation = new ObjectAnimator();
 
 	public QuizzActionBar(Context context) {
 		super(context);
 		init(context, null);
 	}
 
 	public QuizzActionBar(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		TypedArray style = context.obtainStyledAttributes(attrs,
 				R.styleable.widgets_QuizzActionBar);
 		init(context, style);
 	}
 
 	public QuizzActionBar(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		TypedArray style = context.obtainStyledAttributes(attrs,
 				R.styleable.widgets_QuizzActionBar, defStyle, 0);
 		init(context, style);
 	}
 
 	// @SuppressWarnings("deprecation")
 	private void init(Context context, TypedArray style) {
 		LayoutInflater.from(context).inflate(R.layout.layout_quizz_action_bar,
 				this, true);
 		mBackButton = (ImageButton) findViewById(R.id.ab_back_button);
 		mCustomView = (ViewGroup) findViewById(R.id.ab_custom_view_container);
 		/*
 		 * GetParent always null..
 		 * 
 		 * if (style != null) { if
 		 * (style.hasValue(R.styleable.widgets_QuizzActionBar_shadow)) { int
 		 * shadowInt =
 		 * style.getResourceId(R.styleable.widgets_QuizzActionBar_shadow, -1);
 		 * if (shadowInt > 0 && getParent() instanceof ViewGroup) { mShadow =
 		 * ((ViewGroup) getParent()).findViewById(shadowInt); } }
 		 * 
 		 * style.recycle(); }
 		 */
 
 		mBackButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (getContext() instanceof Activity) {
 					InputMethodManager imm = (InputMethodManager) ((Activity) getContext())
 							.getSystemService(Context.INPUT_METHOD_SERVICE);
 					if (imm.isAcceptingText()) {
						  imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 					}
 					((Activity) getContext()).onBackPressed();
 				}
 			}
 		});
 	}
 
 	public void setShadowView(View shadowView) {
 		mShadow = shadowView;
 	}
 
 	public void setShadowVisibility(int visibility) {
 		if (mShadow != null)
 			mShadow.setVisibility(visibility);
 	}
 
 	public ImageButton getBackButton() {
 		return mBackButton;
 	}
 
 	public View getCustomViewContainer() {
 		return mCustomView;
 	}
 
 	public void setCustomView(int layoutRes) {
 		mCustomView.removeAllViews();
 
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		inflater.inflate(layoutRes, mCustomView);
 	}
 
 	private void animate(float[] movements, int duration, boolean bounce,
 			AnimatorListener listener) {
 		if (mAbAnimation.isRunning()) {
 			mAbAnimation.cancel();
 		}
 
 		mAbAnimation = ObjectAnimator.ofFloat(this, "translationY", movements);
 		mAbAnimation.setDuration(duration);
 		if (listener != null) {
 			mAbAnimation.addListener(listener);
 		}
 
 		if (mShadow != null) {
 			if (mShadowAnimation.isRunning()) {
 				mShadowAnimation.cancel();
 			}
 
 			mShadowAnimation = ObjectAnimator.ofFloat(mShadow, "translationY",
 					movements);
 			mShadowAnimation.setDuration(duration);
 
 			if (bounce) {
 				AnimatorUtils.bounceAnimator(mShadowAnimation, movements, 5,
 						100);
 			} else {
 				mShadowAnimation.start();
 			}
 		}
 
 		if (bounce) {
 			AnimatorUtils.bounceAnimator(mAbAnimation, movements, 5, 100);
 		} else {
 			mAbAnimation.start();
 		}
 	}
 
 	private int getBarHeight() {
 		int height = getHeight();
 		if (height == 0) {
 			measure(0, 0);
 			height = getMeasuredHeight();
 		}
 		return height;
 	}
 
 	/**
 	 * Calls the show() method only if the visibility is set to GONE
 	 */
 	public void showIfNecessary(int duration) {
 		if (getVisibility() == View.GONE) {
 			show(duration);
 		}
 	}
 
 	public void show(int duration) {
 		animate(new float[] { -getBarHeight(), 0 }, duration,
 				(duration > MOVE_DIRECT) ? true : false,
 				mAbShowAnimatorListener);
 	}
 
 	public void hide(int duration) {
 		animate(new float[] { 0, -getBarHeight() }, duration, false,
 				mAbHideAnimatorListener);
 	}
 
 	// ===========================================================
 	// Listeners
 	// ===========================================================
 
 	AnimatorListener mAbShowAnimatorListener = new AnimatorListener() {
 
 		@Override
 		public void onAnimationStart(Animator animation) {
 			setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		public void onAnimationRepeat(Animator animation) {
 		}
 
 		@Override
 		public void onAnimationEnd(Animator animation) {
 		}
 
 		@Override
 		public void onAnimationCancel(Animator animation) {
 		}
 	};
 
 	AnimatorListener mAbHideAnimatorListener = new AnimatorListener() {
 
 		@Override
 		public void onAnimationStart(Animator animation) {
 		}
 
 		@Override
 		public void onAnimationRepeat(Animator animation) {
 		}
 
 		@Override
 		public void onAnimationEnd(Animator animation) {
 			setVisibility(View.GONE);
 		}
 
 		@Override
 		public void onAnimationCancel(Animator animation) {
 		}
 	};
 }
