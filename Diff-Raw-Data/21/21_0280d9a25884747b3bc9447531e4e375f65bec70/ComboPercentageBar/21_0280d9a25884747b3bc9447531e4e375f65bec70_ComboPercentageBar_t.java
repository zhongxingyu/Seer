 /*****************************************************************************
  *  PhraseCraze is a family friendly word game for mobile phones.
  *  Copyright (C) 2011 Siramix Team
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ****************************************************************************/
 package com.siramix.phrasecraze;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import android.widget.LinearLayout;
 
 /**
  * Custom view that represents up to 3 values in their proportion to the total.
  * 
  * @author The PhraseCraze Team
  * 
  */
 public class ComboPercentageBar extends LinearLayout {
 
   private Context mContext;
 
   /*
    * Elements contained in this Layout
    */
   private LinearLayout mBarLayout;
   private BarSegment[] mBarSegments;
   private TextView mTitle;
 
   /**
    * @param context
    */
   public ComboPercentageBar(Context context) {
     super(context);
     initializeMembers(context);
   }
 
   /**
    * @param context
    * @param attrs
    */
   public ComboPercentageBar(Context context, AttributeSet attrs) {
     super(context, attrs);
     initializeMembers(context);
   }
 
   // Initialize the member variables
   private void initializeMembers(Context context) {
     mContext = context;
     mBarLayout = new LinearLayout(mContext);
     mBarSegments = new BarSegment[3];
     for (int i = 0; i < mBarSegments.length; i++) {
       mBarSegments[i] = new BarSegment(mContext);
     }
     mTitle = new TextView(mContext);
   }
 
   @Override
   public void onFinishInflate() {
     super.onFinishInflate();
 
     final float DENSITY = this.getResources().getDisplayMetrics().density;
 
     // Setup initial paramters of the main layout
     this.setOrientation(LinearLayout.VERTICAL);
     this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
         LayoutParams.FILL_PARENT));
 
     // Setup the Title
     mTitle.setGravity(Gravity.CENTER);
     mTitle.setText("Title");
     mTitle.setTextSize(20);
     mTitle.setTextColor(this.getResources().getColor(R.color.white));
     if (!this.isInEditMode()) {
       Typeface francoisFont = Typeface.createFromAsset(mContext.getAssets(),
           "fonts/FrancoisOne.ttf");
       mTitle.setTypeface(francoisFont);
     }
     // Setup the Bar layout. This is for the group of bar pieces and labels
     mBarLayout.setOrientation(LinearLayout.HORIZONTAL);
     mBarLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
         LayoutParams.FILL_PARENT));
 
     // Setup each segment in the bar layout
     for (int i = 0; i < mBarSegments.length; i++) {
 
       // Setup the segment's group first
       mBarSegments[i].setOrientation(LinearLayout.VERTICAL);
       LinearLayout.LayoutParams barSegmentLayoutParams = new LinearLayout.LayoutParams(
           LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
       barSegmentLayoutParams.weight = 1.0f;
       mBarSegments[i].setLayoutParams(barSegmentLayoutParams);
 
       // Setup the frame that contains the bar and value
       FrameLayout frame = new FrameLayout(mContext);
       LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(
           LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
       frameLayoutParams.weight = 0.6f;
       int defaultPadding = (int) (DENSITY * 2 + 0.5f);
       // framePadding in parameter order: left, top, right, down
       int[] framepadding = {defaultPadding,defaultPadding,defaultPadding,defaultPadding};
       if(i == 0)
       {
         // First segment has right padding reduced
         framepadding[2] = (int) (DENSITY * 1 + 0.5f);
       }
      else if ( i < (mBarSegments.length-1))
       {
         // Middle segments have left and right padding both reduced
         framepadding[0] = (int) (DENSITY * 1 + 0.5f);
         framepadding[2] = (int) (DENSITY * 1 + 0.5f);
       }
      else if ( i == (mBarSegments.length-1))
       {
         // End segment has left padding reduced
         framepadding[0] = (int) (DENSITY * 1 + 0.5f);
       }
       frame.setPadding(framepadding[0], framepadding[1], framepadding[2], framepadding[3]);
       frame.setBackgroundColor(this.getResources().getColor(R.color.black));
       frame.setLayoutParams(frameLayoutParams);
 
       // Setup the colored section of the bar
       View foreground = new View(mContext);
       foreground.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
           LayoutParams.FILL_PARENT));
       foreground.setBackgroundColor(this.getResources().getColor(
           R.color.teamA_primary));
 
       // Setup the text that displays the numerical value for the bar
       TextView value = new TextView(mContext);
       value.setText("100");
       value.setGravity(Gravity.CENTER);
       value.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
           LayoutParams.FILL_PARENT));
       value.setTextColor(this.getResources().getColor(R.color.white));
       value.setIncludeFontPadding(false);
       value.setTextSize(18);
 
       // Construct the bar
       frame.addView(foreground);
       frame.addView(value);
 
       // Setup the label for this segment
       TextView label = new TextView(mContext);
       label.setGravity(Gravity.CENTER);
       label.setText("Label");
       LinearLayout.LayoutParams labelLayoutParams = new LinearLayout.LayoutParams(
           LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
       labelLayoutParams.weight = 1.0f;
       label.setLayoutParams(labelLayoutParams);
 
       // Set fonts - Wrap in isInEditMode so as not to break previewer
       if (!this.isInEditMode()) {
         Typeface francoisFont = Typeface.createFromAsset(mContext.getAssets(),
             "fonts/FrancoisOne.ttf");
         label.setTypeface(francoisFont);
         Typeface antonFont = Typeface.createFromAsset(mContext.getAssets(),
             "fonts/FrancoisOne.ttf");
         value.setTypeface(antonFont);
       }
 
       // Construct the segment
       mBarSegments[i].addView(frame);
       mBarSegments[i].addView(label);
       mBarLayout.addView(mBarSegments[i]);
     }
 
     // Construct the entire element
     this.addView(mTitle);
     this.addView(mBarLayout);
   }
  
   /*
    * Set the title of this percentage bar
    */
   public void setTitle(String title) {
     mTitle.setText(title);
   }
 
   /**
    * Set all the data that this segment of the ComboPercentageBar can represent.
    * 
    * @param segmentIndex
    *          the segment to change
    * @param value
    *          the value for the new segment
    * @param label
    *          the label for the segment
    * @param barColor
    *          the color resource for the bar
    * @param labelColor
    *          the color resource for the bar's label
    */
   public void setSegmentComponents(int segmentIndex, int value, String label,
       int barColor, int labelColor) {
     FrameLayout bar = (FrameLayout) mBarSegments[segmentIndex].getChildAt(0);
     TextView segmentLabel = (TextView) mBarSegments[segmentIndex].getChildAt(1);
     TextView segmentValue = (TextView) bar.getChildAt(1);
     View segmentForeground = (View) bar.getChildAt(0);
 
     segmentForeground.setBackgroundColor(barColor);
     segmentValue.setText(Integer.toString(value));
     segmentLabel.setText(label);
     segmentLabel.setTextColor(labelColor);
   }
 
   /*
    * Returns the value of a specified segment.
    */
   private int getSegmentValue(int segmentIndex) {
     FrameLayout frame = (FrameLayout) mBarSegments[segmentIndex].getChildAt(0);
     TextView valueView = (TextView) frame.getChildAt(1);
 
     return Integer.valueOf(valueView.getText().toString());
   }
 
   /*
    * Get the view to re-render itself with new weighting for each segment based
    * on the previously supplied segment values.
    */
   public void updateSegmentWeights() {
     final int NUM_SEGMENTS = mBarSegments.length;
     float[] values = new float[NUM_SEGMENTS];
     float totalValue = 0;
 
     // Find the values
     for (int i = 0; i < NUM_SEGMENTS; i++) {
       values[i] = getSegmentValue(i);
       totalValue += values[i];
     }
 
     // Calculate new weights based on proportion of the whole
     float[] newWeights = new float[NUM_SEGMENTS];
     for (int i = 0; i < NUM_SEGMENTS; i++) {
       // Weight specifies how much room to leave for the other views
       // so we subtract from 1
       newWeights[i] = 1.0f - (values[i] / totalValue);
     }
 
     // Assign the new weights
     for (int i = 0; i < NUM_SEGMENTS; i++) {
       LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBarSegments[i]
           .getLayoutParams();
       params.weight = newWeights[i];
       mBarSegments[i].setLayoutParams(params);
     }
 
     // Force the view to redraw itself
     this.invalidate();
   }
   
   /**
    * Bar Segment class lets us override the onSizeChange to hide or display
    * the label at the proper time.
    */
   public class BarSegment extends LinearLayout
   {
 
     public BarSegment(Context context) {
       super(context);
     }
 
     /*
      * Adjust values and labels when they don't fit in the view
      * (non-Javadoc)
      * @see android.view.View#onSizeChanged(int, int, int, int)
      */
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
       super.onSizeChanged(w, h, oldw, oldh);
       
       // Break out elements of the bar
       FrameLayout bar = (FrameLayout) this.getChildAt(0);
       TextView segmentValue = (TextView) bar.getChildAt(1);
       TextView segmentLabel = (TextView) this.getChildAt(1);
 
       // Get width of bar and text
      int barWidth = w - (bar.getPaddingLeft()+ bar.getPaddingRight()); 
       float valueTextWidth = segmentValue.getPaint().measureText(
           segmentValue.getText().toString());
       // Debugging Logs
       if (PhraseCrazeApplication.DEBUG) {
       Log.d("ComboBar",
           "__________ checkTextBounds() _____" + segmentLabel.getText());
       Log.d(
           "ComboBar",
           "w: " + Integer.toString(w) + " " + "h: " + Integer.toString(h) + " "
               + "oldw: " + Integer.toString(oldw) + " " + "oldh: "
               + Integer.toString(oldh));
       Log.d("ComboBar", "   Barwidth: " + Integer.toString(barWidth));
       Log.d("ComboBar", "   Value: " + segmentValue.getText() + " Width: "
           + Float.toString(valueTextWidth));
       }
 
       // If the value is bigger than the bar, hide it and the label
      // Also hide it if the value is 0
      if (valueTextWidth > barWidth || Integer.valueOf(segmentValue.getText().toString()) == 0) {
         segmentLabel.setVisibility(View.INVISIBLE);
         segmentValue.setVisibility(View.INVISIBLE);
       } else {
         segmentLabel.setVisibility(View.VISIBLE);
         segmentValue.setVisibility(View.VISIBLE);
       }
     } 
     
   }
 }
