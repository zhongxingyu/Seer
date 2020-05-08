 package com.aragaer.reminder;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class DrawDialogLayout extends ViewGroup {
 	boolean horizontal;
 	int padding_x = 0, padding_y = 0;
 
 	public DrawDialogLayout(Context context) {
 		this(context, null);
 	}
 
 	public DrawDialogLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		final int w = MeasureSpec.getSize(widthMeasureSpec);
 		final int h = MeasureSpec.getSize(heightMeasureSpec);
 
 		// We have exactly 3 children right now - variable size drawing area, checkbox and "OK" button
 		// Start with figuring out how much space button and checkbox will take
 		final View grid = getChildAt(0), chk = getChildAt(1), btn = getChildAt(2);
 		chk.measure(widthMeasureSpec, heightMeasureSpec);
 		btn.measure(widthMeasureSpec, heightMeasureSpec);
 
 		final int extra_w = Math.max(chk.getMeasuredWidth(), btn.getMeasuredWidth());
 		final int extra_h = chk.getMeasuredHeight() + btn.getMeasuredHeight();
 
 		int space_w = w - extra_w; // how much we can take for grid if we use horizontal layout
 		int space_h = h - extra_h; // same for vertical
 
 		// There's no point in having more space in one dimension than the whole other dimension
 		space_w = Math.min(space_w, h);
 		space_h = Math.min(space_h, w);
 
 		// We've got all the sizes now
 		// Still we'll take all the space provided, so it's time to find out how much spacing we have
 		horizontal = space_h < space_w;
 		int spec;
 		int my_w, my_h;
 		if (horizontal) {
 			spec = MeasureSpec.makeMeasureSpec(space_w, MeasureSpec.AT_MOST);
 			my_w = space_w + extra_w;
 			my_h = Math.max(space_w, extra_h);
 		} else {
 			spec = MeasureSpec.makeMeasureSpec(space_h, MeasureSpec.AT_MOST);
 			my_w = Math.max(space_h, extra_w);
 			my_h = space_h + extra_h;
 		}
 		padding_x = w - my_w;
 		padding_y = h - my_h;
 		grid.measure(spec, spec);
 		setMeasuredDimension(w, h);
 	}
 
 	protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
 		if (!changed)
 			return;
 		final View grid = getChildAt(0), chk = getChildAt(1), btn = getChildAt(2);
 		int w = right - left, h = bottom - top;
 		int chk_h = chk.getMeasuredHeight();
 		if (horizontal) {
 			int px = padding_x / 3, py = padding_y / 2;
 			left += px;
 			right -= px;
 			top += py;
 			bottom -= py;
 			grid.layout(left, top, left + h, bottom);
 			left += px;
 			chk.layout(left + h, top + py, right, top + chk_h);
 			btn.layout(left + h, top + chk_h, right, top + chk_h + btn.getMeasuredHeight());
 		} else {
 			int px = padding_x / 2, py = padding_y / 2;
 			left += px;
 			right -= px;
 			top += py;
 			grid.layout(left, top, right, top + w);
 			top += py;
 			chk.layout(left, top + w, right, top + w + chk_h);
 			btn.layout(left, top + w + chk_h, right, bottom);
 		}
 	}
 }
