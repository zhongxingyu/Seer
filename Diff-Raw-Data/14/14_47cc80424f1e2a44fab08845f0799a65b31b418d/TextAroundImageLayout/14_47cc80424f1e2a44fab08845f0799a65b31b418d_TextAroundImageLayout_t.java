 package albin.oredev2012.ui;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.text.Layout;
 import android.text.SpannableString;
 import android.text.style.LeadingMarginSpan;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EViewGroup;
 
 /**
  * TextAroundImageLayout is a {@link FrameLayout} that expects an
  * {@link ImageView} and a {@link TextView} as children. You can set the image
  * and the text and this layout will try to wrap the text around the image. <br />
  * There are some limitations, however:
  * <ul>
  * <li>The image must have a fixed size set; <code>wrap_content</code> will not
  * work</li>
  * <li>Only the initial text can be wrapped around an image on the left side.</li>
  * </ul>
  * <br />
  * If there aren't exactly one {@link ImageView} and one {@link TextView} inside
  * this layout, the behaviour is undefined and may crash.
  * 
  * @author albintheander
  * 
  */
 @EViewGroup
 public class TextAroundImageLayout extends FrameLayout {
 
 	private ImageView imageView;
 
 	private TextView textView;
 
 	private CharSequence text;
 
 	public TextAroundImageLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	@AfterViews
 	protected void initViews() {
 		int childCount = getChildCount();
 		for (int i = 0; i < childCount; i++) {
 			View child = getChildAt(i);
 			if (child instanceof ImageView)
 				imageView = (ImageView) child;
 			if (child instanceof TextView)
 				textView = (TextView) child;
 		}
 	}
 
 	public void setText(CharSequence text) {
 		this.text = text;
 		wrapTextAroundImage();
 	}
 
 	public void setImageBitmap(Bitmap bitmap) {
 		imageView.setImageBitmap(bitmap);
 		wrapTextAroundImage();
 	}
 
 	private void wrapTextAroundImage() {
 		if (text == null)
 			return;
 
 		// Calculate how many lines in the textView that are next to the
 		// imageView
 		ViewGroup.MarginLayoutParams vlp = (MarginLayoutParams) imageView
 				.getLayoutParams();
 		int imageHeight = vlp.height + vlp.topMargin + vlp.bottomMargin;
		int textLineHeight = textView.getLineHeight();
		int lines = (int) FloatMath.ceil((float) imageHeight / textLineHeight);
 
 		// Create a spannable string that will be indented with the width of the
 		// image for the first lines
 		int imageWidth = vlp.width + vlp.leftMargin + vlp.rightMargin;
 		SpannableString ss = new SpannableString(text);
 		ss.setSpan(new FirstLinesIndent(lines, imageWidth), 0, ss.length(), 0);
 		textView.setText(ss);
 	}
 
 	class FirstLinesIndent implements LeadingMarginSpan.LeadingMarginSpan2 {
 		private int margin;
 		private int lines;
 
 		FirstLinesIndent(int lines, int margin) {
 			this.margin = margin;
 			this.lines = lines;
 		}
 
 		@Override
 		public int getLeadingMargin(boolean first) {
 			if (first) {
 				return margin;
 			} else {
 				return 0;
 			}
 		}
 
 		@Override
 		public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
 				int top, int baseline, int bottom, CharSequence text,
 				int start, int end, boolean first, Layout layout) {
 			// We just want to be transparent, so don't do anything!
 		}
 
 		@Override
 		public int getLeadingMarginLineCount() {
 			return lines;
 		}
 	};
 
 }
