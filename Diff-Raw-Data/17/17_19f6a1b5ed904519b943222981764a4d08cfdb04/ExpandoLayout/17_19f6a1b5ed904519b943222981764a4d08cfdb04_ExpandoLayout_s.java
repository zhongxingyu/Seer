 package com.djpsoft.moreDroid;
 
 import com.djpsoft.moreDroid.R;
import com.djpsoft.moreDroid.R.drawable;
import com.djpsoft.moreDroid.R.styleable;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ExpandoLayout extends ViewGroup {
 
 	public static int DEFAULT_TEXT_SIZE = 17;
 	public static int DEFAULT_TOP_ROW_PADDING = 5;
 
     private boolean expanded = false;
     private String text = null;
     private int textSize = DEFAULT_TEXT_SIZE;
     private int topRowPadding = DEFAULT_TOP_ROW_PADDING;
 
     private Context context;
     private LinearLayout topRow;
     private ImageView icon;
 
     public ExpandoLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.context = context;
 
         TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandoLayout);
         try {
             text = a.getString(R.styleable.ExpandoLayout_text);
             textSize = a.getInt(R.styleable.ExpandoLayout_textSize, DEFAULT_TEXT_SIZE);
             topRowPadding = a.getInt(R.styleable.ExpandoLayout_topRowPadding, DEFAULT_TOP_ROW_PADDING);
             expanded = a.getBoolean(R.styleable.ExpandoLayout_expanded, false);
         } finally {
             a.recycle();
         }
     }
 
     public ExpandoLayout(Context context) {
         this(context, null);
     }
 
     @Override
     protected void onFinishInflate() {
         super.onFinishInflate();
         topRow = new LinearLayout(context);
         topRow.setOrientation(LinearLayout.HORIZONTAL);
         topRow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         topRow.setPadding(topRowPadding, topRowPadding, topRowPadding, topRowPadding);
         icon = new ImageView(context);
        icon.setImageResource(R.drawable.expander_ic_minimized);
         icon.setPadding(0, 0, 5, 0);
         topRow.addView(icon);
         TextView tv = new TextView(context);
         tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
         tv.setText(text);
         tv.setTextSize(textSize);
         tv.setGravity(Gravity.CENTER_VERTICAL);
         topRow.addView(tv);
         addView(topRow, 0);
         topRow.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 toggleExpand();
             }
         });
     }
 
     protected void toggleExpand() {
         expanded = !expanded;
        if (expanded)
             icon.setImageResource(R.drawable.expander_ic_maximized);
         else
             icon.setImageResource(R.drawable.expander_ic_minimized);
        requestLayout();
    }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int width = getPaddingLeft();
         int height = getPaddingTop();
 
         final int count = expanded ? getChildCount() : 1;
         for (int i = 0; i < count; i++) {
             View child = getChildAt(i);
             measureChild(child, widthMeasureSpec, heightMeasureSpec);
 
             LayoutParams lp = (LayoutParams) child.getLayoutParams();
             lp.x = getPaddingLeft();
             lp.y = height;
 
             width = Math.max(width, getPaddingLeft() + child.getMeasuredWidth());
             height += child.getMeasuredHeight();
         }
 
         width += getPaddingLeft() + getPaddingRight();
         height += getPaddingBottom();
 
         setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                 resolveSize(height, heightMeasureSpec));
     }
 
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         final int count = expanded ? getChildCount() : 1;
         for (int i = 0; i < count; i++) {
             View child = getChildAt(i);
             LayoutParams lp = (LayoutParams) child.getLayoutParams();
             child.layout(lp.x, lp.y,
                     lp.x + child.getMeasuredWidth(),
                     lp.y + child.getMeasuredHeight());
         }
     }
 
     @Override
     protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
         return super.drawChild(canvas, child, drawingTime);
     }
 
     @Override
     protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
         return p instanceof LayoutParams;
     }
 
     @Override
     protected LayoutParams generateDefaultLayoutParams() {
         return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
     }
 
     @Override
     public LayoutParams generateLayoutParams(AttributeSet attrs) {
         return new LayoutParams(getContext(), attrs);
     }
 
     @Override
     protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
         return new LayoutParams(p.width, p.height);
     }
 
     public static class LayoutParams extends ViewGroup.LayoutParams {
         public int x;
         public int y;
 
         public LayoutParams(Context context, AttributeSet attrs) {
             super(context, attrs);
         }
 
         public LayoutParams(int w, int h) {
             super(w, h);
         }
     }
 
 }
