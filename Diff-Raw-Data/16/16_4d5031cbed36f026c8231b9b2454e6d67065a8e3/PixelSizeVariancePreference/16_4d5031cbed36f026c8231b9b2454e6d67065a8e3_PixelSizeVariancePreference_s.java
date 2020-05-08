 package steve4448.livetextbackground.preference;
 
 import steve4448.livetextbackground.R;
 import steve4448.livetextbackground.widget.MinMaxBar;
 import steve4448.livetextbackground.widget.listener.OnMinMaxBarChangeListener;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
import android.util.TypedValue;
 import android.view.View;
 import android.widget.TextView;
 
 public class PixelSizeVariancePreference extends DialogPreference {
 	public String keyMin = "minKey", keyMax = "maxKey";
 	private int min = 0, max = 0;
 	private boolean exists = false;
 	
 	public PixelSizeVariancePreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		setDialogLayoutResource(R.layout.pixelsizevariancepreference);
 		TypedArray extraAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.PixelSizeVariancePreference, 0, 0);
 		if(extraAttrs.hasValue(R.styleable.PixelSizeVariancePreference_minKey))
 			keyMin = extraAttrs.getString(R.styleable.PixelSizeVariancePreference_minKey);
 		if(extraAttrs.hasValue(R.styleable.PixelSizeVariancePreference_maxKey))
 			keyMax = extraAttrs.getString(R.styleable.PixelSizeVariancePreference_maxKey);
 		extraAttrs.recycle();
 	}
 	
 	@Override
 	public void onBindDialogView(View view) {
 		exists = getSharedPreferences().contains(keyMin);
 		if(exists) {
 			min = getSharedPreferences().getInt(keyMin, -1);
 			max = getSharedPreferences().getInt(keyMax, -1);
 		}
 		final TextView minTextView = (TextView)view.findViewById(R.id.numMin);
 		final TextView maxTextView = (TextView)view.findViewById(R.id.minFPS);
 		final TextView minPreviewTextView = (TextView)view.findViewById(R.id.minPreview);
 		final TextView maxPreviewTextView = (TextView)view.findViewById(R.id.maxPreview);
 		final MinMaxBar minMaxBar = (MinMaxBar)view.findViewById(R.id.fpsBar);
 		final OnMinMaxBarChangeListener minMaxBarChangeListener = new OnMinMaxBarChangeListener() {
 			@Override
 			public void onMinValueChanged(int newMin, boolean userCall) {
 				min = newMin;
 				minTextView.setText(min + "px");
 				minPreviewTextView.setTextSize(min);
 			}
 			
 			@Override
 			public void onMaxValueChanged(int newMax, boolean userCall) {
 				max = newMax;
 				maxTextView.setText(max + "px");
 				maxPreviewTextView.setTextSize(max);
 			}
 		};
 		minMaxBar.setOnMinMaxBarChangeListener(minMaxBarChangeListener);
 		if(exists) {
 			minMaxBar.setMinimum(min);
 			minMaxBar.setMaximum(max);
 		} else {
 			min = (int)minMaxBar.getMinimum();
 			max = (int)minMaxBar.getMaximum();
 		}
 		minTextView.setText(min + "px");
 		maxTextView.setText(max + "px");
		minPreviewTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, min);
		minPreviewTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, max);
 		float tmpScreenDenisty = getContext().getResources().getDisplayMetrics().density;
 		minPreviewTextView.setHeight((int)(tmpScreenDenisty * minMaxBar.getAbsoluteMaximum() + minMaxBar.getAbsoluteMaximum() * 0.2));
 		maxPreviewTextView.setHeight((int)(tmpScreenDenisty * minMaxBar.getAbsoluteMaximum() + minMaxBar.getAbsoluteMaximum() * 0.2));
 		super.onBindDialogView(view);
 	}
 	
 	@Override
 	public void onDialogClosed(boolean positiveResult) {
 		super.onDialogClosed(positiveResult);
 		if(positiveResult)
 			getSharedPreferences().edit().putInt(keyMin, min).putInt(keyMax, max).commit();
 	}
 }
