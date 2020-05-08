 package com.pwr.zpi.views;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 import com.pwr.zpi.R;
 
 public class GPSSignalDisplayer extends RelativeLayout {
 	
 	private final ImageView circle1;
 	private final ImageView circle2;
 	private final ImageView circle3;
 	View gpsSignalView;
 	
	private static final double FIRST_CIRCLE_THRESHOLD = 40;
	private static final double SECOND_CIRCLE_THRESHOLD = 20;
 	private static final double THIRD_CIRCLE_THRESHOLD = 10;
 	
 	public GPSSignalDisplayer(Context context) {
 		this(context, null);
 	}
 	
 	public GPSSignalDisplayer(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 	
 	public GPSSignalDisplayer(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		
 		LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		gpsSignalView = inflator.inflate(R.layout.gps_signal_strenght, null);
 		this.addView(gpsSignalView);
 		circle1 = (ImageView) gpsSignalView.findViewById(R.id.imageViewFirstCircle);
 		circle2 = (ImageView) gpsSignalView.findViewById(R.id.imageViewSecondCircle);
 		circle3 = (ImageView) gpsSignalView.findViewById(R.id.imageViewThirdCircle);
 		
 		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GPSSignalDisplayer, 0, 0);
 		boolean isWide = false;
 		try {
 			isWide = a.getInteger(R.styleable.GPSSignalDisplayer_displayType, 0) == 0;
 		}
 		finally {
 			a.recycle();
 		}
 		
 		if (isWide) {
 			setWideMode();
 		}
 		
 		setNoSignal();
 	}
 	
 	private void setWideMode() {
 		LinearLayout lay = (LinearLayout) gpsSignalView.findViewById(R.id.linearLayoutGPSSignal);
 		lay.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		circle1.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
 		circle2.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
 		circle3.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
 	}
 	
 	public void updateStrengthSignal(double accuracy) {
 		updateCircle(accuracy, FIRST_CIRCLE_THRESHOLD, circle1);
 		updateCircle(accuracy, SECOND_CIRCLE_THRESHOLD, circle2);
 		updateCircle(accuracy, THIRD_CIRCLE_THRESHOLD, circle3);
 	}
 	
 	public void setNoSignal() {
 		circle1.setImageResource(R.drawable.circle_small);
 		circle2.setImageResource(R.drawable.circle_small);
 		circle3.setImageResource(R.drawable.circle_small);
 	}
 	
 	private void updateCircle(double accuracy, double treshold, ImageView circle) {
 		if (accuracy < treshold) {
 			circle.setImageResource(R.drawable.circle_full_small);
 		}
 		else {
 			circle.setImageResource(R.drawable.circle_small);
 		}
 	}
 }
