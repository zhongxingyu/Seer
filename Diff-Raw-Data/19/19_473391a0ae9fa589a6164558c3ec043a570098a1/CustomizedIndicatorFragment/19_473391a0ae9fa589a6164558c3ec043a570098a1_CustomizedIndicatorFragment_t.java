 package com.andreaszeiser.jalousiesamples.fragment;
 
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.andreaszeiser.jalousie.IndicatedLinearLayoutJalousie;
 import com.andreaszeiser.jalousie.IndicatorElement;
 import com.andreaszeiser.jalousiesamples.R;
 
 public class CustomizedIndicatorFragment extends BaseExampleFragment {
 
 	private IndicatedLinearLayoutJalousie mIndicatedLinearLayoutJalousie;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		getActivity().setTitle(R.string.ex_indicator);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.indicator_customized, null);
 
 		mIndicatedLinearLayoutJalousie = (IndicatedLinearLayoutJalousie) view
 				.findViewById(R.id.indicated_jalousie);
 		mLinearLayoutJalousie = mIndicatedLinearLayoutJalousie
				.getLinearLayoutJalousie();
 
 		return view;
 	}
 
 	public static class IndicatorRectangle extends View implements
 			IndicatorElement {
 
 		private int mState = IndicatorElement.STATE_COLLAPSED;
 		private int mBackgroundColor;
 		private Paint mBackgroundPaint;
 
 		public IndicatorRectangle(Context context, AttributeSet attrs,
 				int defStyle) {
 
 			super(context, attrs, defStyle);
 
 			init();
 		}
 
 		public IndicatorRectangle(Context context, AttributeSet attrs) {
 
 			super(context, attrs);
 
 			init();
 		}
 
 		public IndicatorRectangle(Context context) {
 
 			super(context);
 
 			init();
 		}
 
 		private void init() {
 
 			mBackgroundColor = generateRandomColor();
 			mBackgroundPaint = new Paint();
 			mBackgroundPaint.setColor(mBackgroundColor);
 			mBackgroundPaint.setStyle(Style.FILL);
 		}
 
 		@Override
 		protected void onDraw(Canvas canvas) {
 			canvas.drawColor(mBackgroundColor);
 		}
 
 		@Override
 		public int getState() {
 
 			return mState;
 		}
 
 		@Override
 		public void setState(int indicatorState) {
 
 			switch (indicatorState) {
 			case IndicatorElement.STATE_COLLAPSED:
 			case IndicatorElement.STATE_EXPANDED:
 
 				mState = indicatorState;
 
 				mBackgroundColor = generateRandomColor();
 				invalidate();
 
 				break;
 
 			default:
 				break;
 			}
 		}
 
 		private int generateRandomColor() {
 
 			Random random = new Random();
 			return Color.rgb(random.nextInt(255), random.nextInt(255),
 					random.nextInt(255));
 		}
 
 	}
 }
