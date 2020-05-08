 /*
  * Copyright (C) 2013 by Ethan Hall
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package io.ehdev.android.drivingtime.view.entry;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class DisplayRecordRow extends RelativeLayout {
 
     private TextView rightText, leftText, centerText;
 
     public DisplayRecordRow(Context context, int left_p, int top_p, int right_p, int bottom_p ) {
         super(context);
         setPadding(left_p, top_p, right_p, bottom_p);
         setupView(context);
     }
 
     public DisplayRecordRow(Context context, AttributeSet attrs){
         super(context, attrs);
         setupView(context);
     }
 
     private void setupView(Context context) {
         setBackgroundResource(android.R.color.transparent);
 
         DisplayMetrics metrics = context.getResources().getDisplayMetrics();
         int maxSize = metrics.widthPixels / 3;
 
         createLeftText(context);
         createRightText(context);
         createCenterText(context);
 
         //Set up the relitive layout
        createLeftLayout((int)(metrics.widthPixels * .25));
        createRightLayout((int)(metrics.widthPixels * .25));
        createCenterLayout((int)(metrics.widthPixels * .50));
     }
 
     public void setTextAttributes(int fontSize, int typeFace){
         setViewOptions(rightText, fontSize, typeFace);
         setViewOptions(leftText, fontSize, typeFace);
         setViewOptions(centerText, fontSize, typeFace);
         invalidate();
     }
 
     private void setViewOptions(TextView view, int fontSize, int typeFace) {
         view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
         view.setTypeface(null, typeFace);
     }
 
     private void createLeftLayout(int width) {
         LayoutParams left =
                 new LayoutParams(width,
                         LayoutParams.WRAP_CONTENT);
 
         left.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         left.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
         addView(leftText, left);
     }
 
     private void createRightLayout(int width) {
         LayoutParams right =
                 new LayoutParams(width,
                         LayoutParams.WRAP_CONTENT);
 
         right.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         right.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
         addView(rightText, right);
     }
 
     private void createCenterLayout(int width) {
         LayoutParams right =
                 new LayoutParams(width,
                         LayoutParams.WRAP_CONTENT);
 
         right.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         right.addRule(RelativeLayout.CENTER_IN_PARENT);
         addView(centerText, right);
     }
 
     private void createRightText(Context context) {
         rightText = new TextView(context);
         rightText.setText("left");
         rightText.setTextSize(18);
         rightText.setTextColor(Color.BLACK);
         rightText.setId(1);
         rightText.setGravity(Gravity.CENTER);
     }
 
     private void createCenterText(Context context) {
         centerText = new TextView(context);
         centerText.setText("");
         centerText.setTextSize(18);
         centerText.setTextColor(Color.BLACK);
         centerText.setId(3);
        centerText.setGravity(Gravity.CENTER);
     }
 
     private void createLeftText(Context context) {
         leftText = new TextView(context);
         leftText.setText("right");
         leftText.setTextSize(18);
         leftText.setTextColor(Color.BLACK);
         leftText.setId(2);
         leftText.setGravity(Gravity.CENTER);
     }
 
     public void setRightText(String text){
         rightText.setText(text);
     }
 
     public void setLeftText(String text){
         leftText.setText(text);
     }
 
     public void setCenterText(String text){
         centerText.setText(text);
     }
 
     public DisplayRecordRow(Context context){
         this(context, 12, 3, 10, 3);
     }
 }
