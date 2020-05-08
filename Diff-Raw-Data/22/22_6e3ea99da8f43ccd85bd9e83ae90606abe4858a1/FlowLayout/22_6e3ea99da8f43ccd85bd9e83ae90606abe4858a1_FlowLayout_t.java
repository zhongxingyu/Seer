 package fr.itinerennes.ui.views;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * A layout displaying childrend views inline but breaking lines when all children can't fit in a
  * single line.
  * 
  * <pre>
  * |AAAABB  |
  * |CCCDDDD |
  * |EE      |
  * </pre>
  * 
  * @author RAW
  * @see http://nishantvnair.wordpress.com/2010/09/28/flowlayout-in-android/
  */
 public final class FlowLayout extends ViewGroup {
 
     /** The height of each line. */
     private int[] lineHeight = null;
 
     /** The width of each line. */
     private int[] lineWidth = null;
 
     /** The line number where each child is positioned. */
     private int[] childLine = null;
 
     /**
      * The layout params you can set to a child view of a {@link FlowLayout}.
      * 
      * @author Jérémie Huchet
      */
     public static class LayoutParams extends ViewGroup.LayoutParams {
 
         /** The top margin in pixels. */
         private final int marginTop;
 
         /** The left margin in pixels. */
         private final int marginLeft;
 
         /** The bottom margin in pixels. */
         private final int marginBottom;
 
         /** The right margin in pixels. */
         private final int marginRight;
 
         /**
          * Creates a default {@link LayoutParams} without vertical or horizontal spacing.
          * 
          * @param params
          *            the {@link android.view.ViewGroup.LayoutParams}
          */
         public LayoutParams(final android.view.ViewGroup.LayoutParams params) {
 
             super(params);
             this.marginTop = 0;
             this.marginLeft = 0;
             this.marginBottom = 0;
             this.marginRight = 0;
         }
 
         /**
          * Creates a {@link LayoutParams}.
          * 
          * @param width
          *            the width, either FILL_PARENT, WRAP_CONTENT or a fixed size in pixels. See
          *            {@link android.view.ViewGroup.LayoutParams#width}
          * @param height
          *            the height, either FILL_PARENT, WRAP_CONTENT or a fixed size in pixels. See
          *            {@link android.view.ViewGroup.LayoutParams#height}
          * @param marginTop
          *            the top margin in pixels
          * @param marginLeft
          *            the left margin in pixels
          * @param marginBottom
          *            the bottom margin in pixels
          * @param marginRight
          *            the right margin in pixels
          */
         public LayoutParams(final int width, final int height, final int marginTop,
                 final int marginLeft, final int marginBottom, final int marginRight) {
 
             super(width, height);
             this.marginTop = 0;
             this.marginLeft = 0;
             this.marginBottom = 0;
             this.marginRight = 0;
         }
 
         /**
          * Gets the marginTop.
          * 
          * @return the marginTop
          */
         public final int getMarginTop() {
 
             return marginTop;
         }
 
         /**
          * Gets the marginLeft.
          * 
          * @return the marginLeft
          */
         public final int getMarginLeft() {
 
             return marginLeft;
         }
 
         /**
          * Gets the marginBottom.
          * 
          * @return the marginBottom
          */
         public final int getMarginBottom() {
 
             return marginBottom;
         }
 
         /**
          * Gets the marginRight.
          * 
          * @return the marginRight
          */
         public final int getMarginRight() {
 
             return marginRight;
         }
     }
 
     /**
      * Constructor.
      * 
      * @param context
      *            the context
      */
     public FlowLayout(final Context context) {
 
         super(context);
     }
 
     /**
      * Constructor.
      * 
      * @param context
      *            the context
      * @param attrs
      *            the attributes
      */
     public FlowLayout(final Context context, final AttributeSet attrs) {
 
         super(context, attrs);
     }
 
     /**
      * The default behavior for child views is
      * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} and no margin.
      * 
      * @return the default layout params
      * @see android.view.ViewGroup#generateDefaultLayoutParams()
      */
     @Override
     protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
 
         return new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                 android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
     }
 
     /**
      * Checks the given layout params is an instance of {@link LayoutParams}.
      * 
      * @return true if the given layout params is an instance of {@link LayoutParams}
      * @see android.view.ViewGroup#checkLayoutParams(android.view.ViewGroup.LayoutParams)
      */
     @Override
     protected boolean checkLayoutParams(final android.view.ViewGroup.LayoutParams p) {
 
         return p instanceof LayoutParams;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.view.View#onMeasure(int, int)
      */
     @Override
     protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
 
         final int availableWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
                 - getPaddingRight();
         final int availableHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()
                 - getPaddingBottom();
         final int count = getChildCount();
 
         childLine = new int[count];
         lineHeight = new int[] { 0 };
         lineWidth = new int[] { 0 };
 
         // this is the width measure spec to use to measure children
         final int childHeightMeasureSpec;
         if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
             childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(availableHeight,
                     MeasureSpec.AT_MOST);
         } else {
             childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
         }
 
         int currentLine = 0;
 
         for (int i = 0; i < count; i++) {
             final View child = getChildAt(i);
             if (child.getVisibility() != GONE) {
 
                 // rerieve child layout params
                 final LayoutParams lp;
                 if (child.getLayoutParams() instanceof LayoutParams) {
                     lp = (LayoutParams) child.getLayoutParams();
                 } else {
                     lp = new LayoutParams(child.getLayoutParams());
                 }
 
                 // this is the width measure spec to use to measure children
                 final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth,
                         MeasureSpec.AT_MOST);
 
                 child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
 
                 final int childWidth = lp.getMarginLeft() + child.getMeasuredWidth()
                         + lp.getMarginRight();
                 final int childHeight = lp.getMarginTop() + child.getMeasuredHeight()
                         + lp.getMarginBottom();
 
                 // if the child doesn't fit on the current line
                 if (getPaddingLeft() + lineWidth[currentLine] + childWidth + getPaddingRight() > availableWidth) {
                     // move cursor vertically (move cursor to the next line)
                     currentLine++;
 
                    // then increment lineWidth and lineHeight arrays
                    int[] copyTemp = new int[lineWidth.length + 1];
                    System.arraycopy(lineWidth, 0, copyTemp, 0, lineWidth.length);
                    copyTemp[copyTemp.length - 1] = 0;
                    lineWidth = copyTemp;

                    copyTemp = new int[lineHeight.length + 1];
                    System.arraycopy(lineHeight, 0, copyTemp, 0, lineHeight.length);
                    copyTemp[copyTemp.length - 1] = 0;
                    lineHeight = copyTemp;
                 }
 
                 lineWidth[currentLine] = lineWidth[currentLine] + childWidth;
                 lineHeight[currentLine] = Math.max(lineHeight[currentLine], childHeight);
 
                 // store the line number the child have to be drawn
                 childLine[i] = currentLine;
             }
         }
 
         // layout minimum width is the biggest line width we have
         final int layoutWidth;
         if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
             layoutWidth = findMaxValue(lineWidth);
         } else if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
             /*
              * TJHU Here is a bug : the layout must have attribute android:width="fill_parent" to be
              * displayed correctly. But we don't want it to fill the whole screen. so even if the
              * layout has the attribute width="fill_parent" we do not use the full available space.
              */
             // if (getLayoutParams().width >= 0) {
             // // layout has a fixed width
             // layoutWidth = getLayoutParams().width;
             // } else if (getLayoutParams().width == LayoutParams.FILL_PARENT) {
             // // layout has property fill_parent: use all available space
             // layoutWidth = availableWidth;
             // } else {
             // // layout should have property wrap content, use only needed space
             layoutWidth = findMaxValue(lineWidth);
             // }
         } else {
             // (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY)
             layoutWidth = availableWidth;
         }
 
         // layout minimum height is total of all line height
         final int layoutHeight;
         if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
             layoutHeight = computeTotal(lineHeight);
         } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
             if (getLayoutParams().height >= 0) {
                 // layout has a fixed height
                 layoutHeight = getLayoutParams().height;
             } else {
                 // layout has fill_parent or wrap_content property, the FlowLayout use the height it
                 // needs
                 layoutHeight = computeTotal(lineHeight);
             }
         } else {
             // (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)
             layoutHeight = availableHeight;
         }
 
         setMeasuredDimension(getPaddingLeft() + layoutWidth + getPaddingRight(), getPaddingTop()
                 + layoutHeight + getPaddingBottom());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
      */
     @Override
     protected void onLayout(final boolean changed, final int l, final int t, final int r,
             final int b) {
 
         final int availableWidth = r - l;
 
         int ypos = getPaddingTop();
 
         // for each line
         for (int lineNumber = 0; lineNumber < lineHeight.length; lineNumber++) {
             final int lineWidth = this.lineWidth[lineNumber];
             final int lineHeight = this.lineHeight[lineNumber];
             // get the views to display on the current line
             final View[] views = getViewsOnLine(lineNumber);
 
             final int linePaddingLeft = (availableWidth - lineWidth) / 2;
             // paddingRight = (availableWidth - lineWidth) / 2
             int xpos = getPaddingLeft() + linePaddingLeft;
 
             for (final View child : views) {
                 final int childw = child.getMeasuredWidth();
                 final int childh = child.getMeasuredHeight();
                 final LayoutParams lp;
                 if (child.getLayoutParams() instanceof LayoutParams) {
                     lp = (LayoutParams) child.getLayoutParams();
                 } else {
                     lp = new LayoutParams(child.getLayoutParams());
                 }
 
                 final int childRequiredHeight = lp.getMarginTop() + childh + lp.getMarginBottom();
                 final int childPaddingTop = (lineHeight - childRequiredHeight) / 2;
                 final int childypos = ypos + childPaddingTop;
 
                 // layout the child
                 child.layout(xpos, childypos, xpos + childw, childypos + childh);
 
                 // move horizontal cursor
                 xpos += lp.getMarginLeft() + childw + lp.getMarginRight();
             }
             // move cursor vertically between each line
             ypos += lineHeight;
         }
     }
 
     /**
      * Gets the list of the child views to display on the given line number.
      * 
      * @param lineNumber
      *            a 0-based line number (must be greater than 0 and lower than the amount of
      *            necessary lines)
      * @return the view to display on the given line number
      */
     private View[] getViewsOnLine(final int lineNumber) {
 
         int count = 0;
         // get the amount of views to display on the requested line
         // (childLine[i] <= lineNumber) this stops the loop when next lines will only be higher than
         // the requested one
         for (int i = 0; i < childLine.length && childLine[i] <= lineNumber; i++) {
             if (lineNumber == childLine[i]) {
                 count++;
             }
         }
 
         final View[] views = new View[count];
         count = 0;
         for (int i = 0; i < childLine.length; i++) {
             if (lineNumber == childLine[i]) {
                 views[count++] = getChildAt(i);
             }
         }
         return views;
     }
 
     /**
      * Finds the maximum value stored in the given array.
      * 
      * @param array
      *            an integer array, must not be null or contain null values
      * @return the maximum value
      */
     private int findMaxValue(final int[] array) {
 
         int max = Integer.MIN_VALUE;
         for (final int i : array) {
             max = Math.max(max, i);
         }
         return max;
     }
 
     /**
      * Additions each number of the given array.
      * 
      * @param array
      *            an integer array, must not be null or contain null values
      * @return the total value
      */
     private int computeTotal(final int[] array) {
 
         int total = 0;
         for (final int i : array) {
             total += i;
         }
         return total;
     }
 }
