 package com.risch.evertsson.iclib.layout;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.RemoteViews.RemoteView;
 
 import com.risch.evertsson.iclib.R;
 
 /**
  * Created by johanrisch on 6/13/13.
  */
 public class ICGridLayout extends ViewGroup {
     private int mColumns = 4;
     private float mSpacing;
 
     public ICGridLayout(Context context) {
         super(context);
     }
 
     public ICGridLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
         init(attrs);
     }
 
     public ICGridLayout(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init(attrs);
     }
 
     private void init(AttributeSet attrs) {
         TypedArray a = getContext().obtainStyledAttributes(
                 attrs,
                 R.styleable.ICGridLayout);
         this.mColumns = a.getInt(R.styleable.ICGridLayout_columns, 3);
         this.mSpacing = a.getDimension(R.styleable.ICGridLayout_spacing, 0);
         a.recycle();
     }
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         int width = (int) (r - l);
        int side = width / mColumns;
         int children = getChildCount();
         View child = null;
 
         for (int i = 0; i < children; i++) {
             child = getChildAt(i);
             LayoutParams lp = (LayoutParams) child.getLayoutParams();
 
             int lSpacing = (int) (lp.lSpacing != -1 ? lp.lSpacing : mSpacing);
             int rSpacing = (int) (lp.rSpacing != -1 ? lp.rSpacing : mSpacing);
             int tSpacing = (int) (lp.tSpacing != -1 ? lp.tSpacing : mSpacing);
             int bSpacing = (int) (lp.bSpacing != -1 ? lp.bSpacing : mSpacing);
             int left = (lp.left * side + lSpacing);
             int right = r;
             int top = (lp.top * side + tSpacing);
             int bottom = b;
             if (!lp.fillHorizontal) {
                 right = (lp.right * side - rSpacing);
             }
             if (!lp.fillVertical) {
                 bottom = (lp.bottom * side - bSpacing);
             }
             child.layout(left, top, right, bottom);
         }
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         doMeasure(widthMeasureSpec, heightMeasureSpec);
 
     }
 
     private void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int widthMode = MeasureSpec.getMode(widthMeasureSpec);
         int width = 0;
         int height = MeasureSpec.getSize(heightMeasureSpec);
         // As of now we do not support horizontal scrolling. Thus we require a
         // set size for our layout width.
         if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY) {
             width = MeasureSpec.getSize(widthMeasureSpec);
         } else {
             throw new RuntimeException("widthMeasureSpec must be AT_MOST or " +
                     "EXACTLY not UNSPECIFIED when orientation == VERTICAL");
         }
 
         View child = null;
         int row = 0;
         int side = width / mColumns;
         int childCount = getChildCount();
         for (int i = 0; i < childCount; i++) {
             child = getChildAt(i);
             if (child.getVisibility() != View.GONE) {
                 LayoutParams lp = (LayoutParams) child.getLayoutParams();
 
                 if (lp.bottom > row) {
                     row = lp.bottom;
                 }
                 measureChild(child, lp, side, heightMeasureSpec, widthMeasureSpec, width, height - lp.top * side);
             }
         }
         height = row * side;
         setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                 resolveSize(height, heightMeasureSpec));
     }
 
     /**
      * Calculates the size of the childs area depending on the supplied
      * coordinates.
      *
      * @param child             the child to be measured
      * @param lp                the childs layoutparams.
      * @param side              the size of a 1x1 squares side
      * @param heightMeasureSpec the parents heightMeasureSpec
      * @param widthMeasureSpec  the parents widthMeasureSpec
      */
     protected void measureChild(View child, LayoutParams lp, int side, int heightMeasureSpec,
                                 int widthMeasureSpec, int parentWidth, int parentHeight) {
         int childHeight = parentHeight;
         int childWidth = parentWidth;
         if (!lp.fillVertical) {
             childHeight = (lp.bottom - lp.top) * side;
         }
         if (!lp.fillHorizontal) {
             childWidth = (lp.right - lp.left) * side;
         }
         int heightSpec = getChildMeasureSpec(heightMeasureSpec, 0, childHeight);
         int widthSpec = getChildMeasureSpec(widthMeasureSpec, 0, childWidth);
         child.measure(widthSpec, heightSpec);
     }
 
     @Override
     public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
         return new ICGridLayout.LayoutParams(getContext(), attrs);
     }
 
     @Override
     protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
         return p instanceof ICGridLayout.LayoutParams;
     }
 
     @Override
     protected ViewGroup.LayoutParams
     generateLayoutParams(ViewGroup.LayoutParams p) {
         return new ICGridLayout.LayoutParams(p);
     }
 
     protected ViewGroup.LayoutParams
     generateLayoutParams(ViewGroup.MarginLayoutParams p) {
         return new ICGridLayout.LayoutParams(p);
     }
 
     @Override
     protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
         return new LayoutParams();
     }
 
     public static class LayoutParams extends ViewGroup.LayoutParams {
         public int right = 1;
         public int bottom = 1;
         public int top = 0;
         public int left = 0;
         public int width = -1;
         public int height = -1;
         public int lSpacing = -1;
         public int rSpacing = -1;
         public int tSpacing = -1;
         public int bSpacing = -1;
         public boolean fillVertical = false;
         public boolean fillHorizontal = false;
 
         public LayoutParams() {
             super(MATCH_PARENT, MATCH_PARENT);
             top = 0;
             left = 1;
         }
 
         public LayoutParams(int width, int height) {
             super(width, height);
             top = 0;
             left = 1;
         }
 
         public LayoutParams(Context context, AttributeSet attrs) {
             super(context, attrs);
             TypedArray a = context.obtainStyledAttributes(
                     attrs,
                     R.styleable.ICGridLayout_Layout);
             fillHorizontal = a.getBoolean(R.styleable.ICGridLayout_Layout_layout_fill_horizontal, false);
             fillVertical = a.getBoolean(R.styleable.ICGridLayout_Layout_layout_fill_vertical, false);
             left = a.getInt(R.styleable.ICGridLayout_Layout_layout_left, 0);
             top = a.getInt(R.styleable.ICGridLayout_Layout_layout_top, 0);
             right = a.getInt(R.styleable.ICGridLayout_Layout_layout_right, left + 1);
             bottom = a.getInt(R.styleable.ICGridLayout_Layout_layout_bottom, top + 1);
             height = a.getInt(R.styleable.ICGridLayout_Layout_layout_row_span, -1);
             width = a.getInt(R.styleable.ICGridLayout_Layout_layout_col_span, -1);
             int spacing = a.getDimensionPixelSize(
                     R.styleable.ICGridLayout_Layout_layout_spacing, -1);
             if (spacing == -1) {
                 int hSpacing = a.getDimensionPixelSize(
                         R.styleable.ICGridLayout_Layout_layout_horizontal_spacing, -1);
 
                 int vSpacing = a.getDimensionPixelSize(
                         R.styleable.ICGridLayout_Layout_layout_vertical_spacing, -1);
                 if (hSpacing == -1) {
                     lSpacing = a.getDimensionPixelSize(
                             R.styleable.ICGridLayout_Layout_layout_left_spacing, -1);
                     rSpacing = a.getDimensionPixelSize(
                             R.styleable.ICGridLayout_Layout_layout_right_spacing, -1);
                 } else {
                     lSpacing = rSpacing = hSpacing;
                 }
                 if (vSpacing == -1) {
                     tSpacing = a.getDimensionPixelSize(
                             R.styleable.ICGridLayout_Layout_layout_top_spacing, -1);
                     bSpacing = a.getDimensionPixelSize(
                             R.styleable.ICGridLayout_Layout_layout_bottom_spacing, -1);
                 } else {
                     tSpacing = bSpacing = vSpacing;
                 }
             } else {
                 lSpacing = rSpacing = tSpacing = bSpacing = spacing;
             }
 
             if (height != -1) {
                 bottom = top + height;
             }
             if (width != -1) {
                 right = left + width;
             }
 
             a.recycle();
         }
 
         public LayoutParams(ViewGroup.LayoutParams params) {
             super(params);
         }
 
         public LayoutParams(ViewGroup.MarginLayoutParams params) {
             super(params);
         }
     }
 
 }
