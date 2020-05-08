 package steve4448.livetextbackground.preference;
 
 import steve4448.livetextbackground.R;
 import steve4448.livetextbackground.widget.MinMaxBar;
 import steve4448.livetextbackground.widget.listener.OnMinMaxBarChangeListener;
 import android.content.Context;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
 
 public class MinMaxBarPreference extends DialogPreference {
 	
 	public MinMaxBarPreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		actualInit(context, attrs);
 	}
 	
 	public MinMaxBarPreference(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		actualInit(context, attrs);
 	}
 	
 	public void actualInit(Context context, AttributeSet attrs) {
 		setDialogLayoutResource(R.layout.minmaxbarpreference);
 	}
 	
 	@Override
 	public void onBindDialogView(View view) {
 		super.onBindDialogView(view);
 		final TextView minTextView = (TextView)view.findViewById(R.id.numMin);
 		final TextView maxTextView = (TextView)view.findViewById(R.id.numMax);
 		final TextView minPreviewTextView = (TextView)view.findViewById(R.id.minPreview);
 		final TextView maxPreviewTextView = (TextView)view.findViewById(R.id.maxPreview);
 		final MinMaxBar minMaxBar = (MinMaxBar)view.findViewById(R.id.minMaxBar);
 		final OnMinMaxBarChangeListener minMaxBarChangeListener = new OnMinMaxBarChangeListener() {
 			@Override
 			public void onMinValueChanged(int newMin, boolean userCall) {
				minTextView.setText(newMin + "dp");
 				minPreviewTextView.setTextSize((int)minMaxBar.getMinimum());
 			}
 			
 			@Override
 			public void onMaxValueChanged(int newMax, boolean userCall) {
				maxTextView.setText(newMax + "dp");
 				maxPreviewTextView.setTextSize((int)minMaxBar.getMaximum());
 			}
 		};
 		minMaxBar.setOnMinMaxBarChangeListener(minMaxBarChangeListener);
		minTextView.setText((int)minMaxBar.getMinimum() + "dp");
		maxTextView.setText((int)minMaxBar.getMaximum() + "dp");
 		minPreviewTextView.setTextSize((int)minMaxBar.getMinimum());
 		maxPreviewTextView.setTextSize((int)minMaxBar.getMaximum());
 	}
 }
