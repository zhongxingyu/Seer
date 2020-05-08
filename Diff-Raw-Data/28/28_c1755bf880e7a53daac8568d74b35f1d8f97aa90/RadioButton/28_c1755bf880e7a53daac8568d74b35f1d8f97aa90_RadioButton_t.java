 package cs309.a1.shared;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 
 /**
  * A Custom RadioButton for this Application
  * 
  * On Production builds, it uses a custom font
  */
 public class RadioButton extends android.widget.RadioButton {
 
 	private Context mContext;
 
 	/**
 	 * Create a Button with the given Context and Attributes
 	 * 
 	 * @param context
 	 * @param attrs
 	 */
 	public RadioButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mContext = context;
 	}
 
	/* (non-Javadoc)
 	 * @see android.view.View#onFinishInflate()
 	 */
 	@Override
 	protected void onFinishInflate() {
 		super.onFinishInflate();
 
 		// If this is not a debug build, use the custom font
 		if (!Util.isDebugBuild()) {
			Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "helsinki.otf");
 			setTypeface(typeface);
 		}
 	}
 }
