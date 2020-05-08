 package com.liongrid.gameengine.view;
 
 import com.liongrid.gameengine.LRenderSystem;
 
 public class LAbsoluteLayout extends LViewGroup {
 
 	@Override
 	protected void onDraw(LRenderSystem rs) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	
 	 @Override
      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int count = getChildCount();
 
          int maxHeight = 0;
          int maxWidth = 0;
 
          // Find out how big everyone wants to be
          measureChildren(widthMeasureSpec, heightMeasureSpec);
 
          // Find rightmost and bottom-most child
          for (int i = 0; i < count; i++) {
              View child = getChildAt(i);
              if (child.getVisibility() != GONE) {
                  int childRight;
                  int childBottom;
 
                  AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) child
                          .getLayoutParams();
 
                  childRight = lp.x + child.getMeasuredWidth();
                  childBottom = lp.y + child.getMeasuredHeight();
 
                  maxWidth = Math.max(maxWidth, childRight);
                  maxHeight = Math.max(maxHeight, childBottom);
              }
          }
 
          // Account for padding too
          maxWidth += mPaddingLeft + mPaddingRight;
          maxHeight += mPaddingTop + mPaddingBottom;
 
          // Check against minimum height and width
          maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
          maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
 
          setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                 resolveSize(maxHeight, heightMeasureSpec));
      }
 
 }
