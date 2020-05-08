 package com.bean;
 
 import java.lang.reflect.Method;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.text.TextUtils;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.Transformation;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ExpandablePanelOld extends LinearLayout {
 
 	private TextView mTextView;
 	private FrameLayout mFrame;
 	private LinearLayout parentView;
 	private ImageView mExpandIndicator;
 	private int mCollapsedHeight;
 	private String mContent;
 	private int mTheme;
 	private boolean collapsible;
 
 	public static int THEME_DARK = 0;
 	public static int THEME_LIGHT = 1;
 
 	public ExpandablePanelOld(Context context) {
 		super(context);
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.main, this);
 		init();
 	}
 
 
 	public ExpandablePanelOld(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.main, this);
 		init();
 	}
 
 	@Override
 	protected void onFinishInflate() {
 		super.onFinishInflate();
 		// mTextView.setText(mContent);
 	}
 
 	private void init() {
 
 		mTheme = THEME_DARK;
 		mCollapsedHeight = (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
 						.getDisplayMetrics());
 
 		mContent = DIALOGUE;
 
 		mTextView = (TextView) findViewById(R.id.text);
 		mTextView.setText(mContent);
 		mTextView.setTextColor(Color.WHITE); // default
 		mTextView.setSelected(true);
 		mTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
 		mTextView.isVerticalFadingEdgeEnabled();
 		Log.e("fade", Boolean.toString(mTextView.isVerticalFadingEdgeEnabled()));
 		mTextView.setFadingEdgeLength(30);
 
 		mExpandIndicator = (ImageView) findViewById(R.id.indicator);
 		mExpandIndicator.setImageResource(R.drawable.arrow_light); // default
 
 		mFrame = (FrameLayout) findViewById(R.id.frame);
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, this.mCollapsedHeight);
 		mFrame.setLayoutParams(params);
 		parentView = (LinearLayout) findViewById(R.id.parent_layout);
 
 		parentView.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				expandOrCollapse(mFrame, mTextView, mCollapsedHeight);
 
 				if (mFrame.getHeight() == mCollapsedHeight) {
 					if (mTheme == THEME_DARK)
 						mExpandIndicator
 								.setImageResource(R.drawable.arrow_light_inverted);
 					else
 						mExpandIndicator
 								.setImageResource(R.drawable.arrow_dark_inverted);
 
 				} else {
 					if (mTheme == THEME_DARK)
 						mExpandIndicator
 								.setImageResource(R.drawable.arrow_light);
 					else
 						mExpandIndicator
 								.setImageResource(R.drawable.arrow_dark);
 				}
 				if (mTextView.getHeight() < mCollapsedHeight)
 					mExpandIndicator.setVisibility(View.GONE);
 
 			}
 
 		});
 	}
 
 	private static int measureViewHeight(View view2Expand, View view2Measure) {
 		try {
 			Method m = view2Measure.getClass().getDeclaredMethod("onMeasure",
 					int.class, int.class);
 			m.setAccessible(true);
 			m.invoke(view2Measure, MeasureSpec.makeMeasureSpec(
 					view2Expand.getWidth(), MeasureSpec.AT_MOST), MeasureSpec
 					.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
 		} catch (Exception e) {
 			return -1;
 		}
 
 		int measuredHeight = view2Measure.getMeasuredHeight();
 		return measuredHeight;
 	}
 
 	private boolean expandOrCollapse(View view2Expand, View view2Measure,
 			int collapsedHeight) {
 		if (view2Expand.getHeight() < collapsedHeight)
 			return false;
 
 		int measuredHeight = measureViewHeight(view2Expand, view2Measure);
 
 		if (measuredHeight < collapsedHeight)
 			measuredHeight = collapsedHeight;
 
 		final int startHeight = view2Expand.getHeight();
 		final int finishHeight = startHeight <= collapsedHeight ? measuredHeight
 				: collapsedHeight;
 
 		view2Expand.startAnimation(new ExpandAnimation(view2Expand,
 				startHeight, finishHeight));
 		return true;
 	}
 
 	public void setText(String mContent) {
 		this.mContent = mContent;
 		// initPost();
 		mTextView.setText(mContent);
 
 	}
 
 	private void initPost() {
 		mTextView.setText(mContent);
 		mFrame = (FrameLayout) findViewById(R.id.frame);
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, this.mCollapsedHeight);
 		mFrame.setLayoutParams(params);
 
 		if (mTextView.getMeasuredHeight() < mCollapsedHeight) {
 			mExpandIndicator.setVisibility(View.GONE);
 		} else {
 			mExpandIndicator.setVisibility(View.VISIBLE);
 
 		}
 
 	}
 
 	public void setTextColor(int mColor) {
 		mTextView.setTextColor(mColor);
 	}
 
 	public void setParams(String mContent, int mCollapsedHeight) {
 		this.mContent = mContent;
 		this.mCollapsedHeight = mCollapsedHeight;
 		initPost();
 
 	}
 
 	public void setTheme(int mTheme) {
 		this.mTheme = mTheme;
 
 		if (mTheme == THEME_DARK) {
 			mTextView.setTextColor(Color.WHITE);
 			mExpandIndicator.setImageResource(R.drawable.arrow_light);
 
 		} else if (mTheme == THEME_LIGHT) {
 			mTextView.setTextColor(Color.DKGRAY);
 			mExpandIndicator.setImageResource(R.drawable.arrow_dark);
 		} else {
 			// error..handle it...
 		}
 
 	}
 
 	public void setCollapsedHeight(int mCollapsedHeight) {
 		Log.e("CollapsedHeight", Integer.toString(mCollapsedHeight));
 		Log.e("CollapsedHeightOld", Integer.toString(this.mCollapsedHeight));
 		Log.e("TextViewHeight", Integer.toString(mTextView.getHeight()));
 
 		this.mCollapsedHeight = mCollapsedHeight;
 		initPost();
 
 	}
 
 	public class ExpandAnimation extends Animation {
 		private final View _view;
 		private final int _startHeight;
 		private final int _finishHeight;
 
 		public ExpandAnimation(View view, int startHeight, int finishHeight) {
 			_view = view;
 			_startHeight = startHeight;
 			_finishHeight = finishHeight;
 			setDuration(220);
 		}
 
 		@Override
 		protected void applyTransformation(float interpolatedTime,
 				Transformation t) {
 			final int newHeight = (int) ((_finishHeight - _startHeight)
 					* interpolatedTime + _startHeight);
 			_view.getLayoutParams().height = newHeight;
 			_view.requestLayout();
 		}
 
 		@Override
 		public void initialize(int width, int height, int parentWidth,
 				int parentHeight) {
 			super.initialize(width, height, parentWidth, parentHeight);
 		}
 
 		@Override
 		public boolean willChangeBounds() {
 			return true;
 		}
 	}
 
 	public static final String DIALOGUE = new String(
 			"Set the content using setText() method");
 
 }
