 package com.x3.tictactoe;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class PlaygroundView extends View implements Playground.DataListener {
 
 	public interface OnClickListener {
 		
 		void onClick(PlaygroundView view, int cellX, int cellY);
 		
 	}
 	
 	private Drawable mCellDrawable = null;
 	private Drawable mXDrawable = null;
 	private Drawable mODrawable = null;
 	private Playground mPlayground = null;
 	private OnClickListener mListener = null;
 	
 	public PlaygroundView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mCellDrawable = context.getResources().getDrawable(R.drawable.game_cell);
 		mXDrawable = context.getResources().getDrawable(R.drawable.x);
 		mODrawable = context.getResources().getDrawable(R.drawable.o);
 	}
 	
 	public Playground getPlayground() {
 		return mPlayground;
 	}
 	
 	public void setPlayground(Playground playground) {
 		if (mPlayground != null && mPlayground.getDataListener() == this) {
 			mPlayground.setDataListener(null);
 		}
 		
 		mPlayground = playground;
 		
 		if (mPlayground != null) {
 			mPlayground.setDataListener(this);
 		}
 		
 		reload();
 	}
 	
 	public OnClickListener getListener() {
 		return mListener;
 	}
 	
 	public void setListener(OnClickListener listener) {
 		mListener = listener;
 	}
 	
 	public void reload() {
 		invalidate();
 	}
 	
 	public void reload(int cellX, int cellY) {
 		if (mPlayground != null) {
 			final int playgroundSize = mPlayground.getSize();
 			final int cellWidth = getMeasuredWidth() / playgroundSize;
 			final int cellHeight = getMeasuredHeight() / playgroundSize;
 			final int left = cellX * cellWidth, top = cellY * cellHeight;
 			
 			invalidate(left, top, left + (cellX + 1) * cellWidth, top + (cellY + 1)
 					* cellHeight);
 		} else {
 			invalidate();
 		}
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 		
 		final float ICON_SIZE = 0.6f;
 		
 		if (mPlayground != null) {
 			final int playgroundSize = mPlayground.getSize();
 			final int cellWidth = getMeasuredWidth() / playgroundSize;
 			final int cellHeight = getMeasuredHeight() / playgroundSize;
 			final int iconWidth = (int)(cellWidth * ICON_SIZE);
 			final int iconHeight = (int)(cellHeight * ICON_SIZE);
 			final int iconOffsetX = (cellWidth - iconWidth) / 2;
 			final int iconOffsetY = (cellHeight - iconHeight) / 2;
 			
 			for (int i = 0; i < playgroundSize; i++) {
 				for (int j = 0; j < playgroundSize; j++) {
 					final int left = i * cellWidth, top = j * cellHeight;
 					mCellDrawable.setBounds(left, top, left + cellWidth, top
 							+ cellHeight);
 					mCellDrawable.draw(canvas);
 					
 					final int cellValue = mPlayground.get(i, j);
 					
 					Drawable iconDrawable = null;
 					if (cellValue == Playground.X) {
 						iconDrawable = mXDrawable;
 					} else  if (cellValue == Playground.O) {
 						iconDrawable = mODrawable;
 					}
 					
 					if (iconDrawable != null) {
 						final int iconLeft = left + iconOffsetX;
 						final int iconTop = top + iconOffsetY;
 						iconDrawable.setBounds(iconLeft, iconTop, iconLeft
 								+ iconWidth, iconTop + iconHeight);
 						iconDrawable.draw(canvas);
 					}
 				}
 			}
 		}
 	}
 		
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (mPlayground == null) {
 			return false;
 		}
 		
 		final int eventAction = event.getAction();
 		final int playgroundSize = mPlayground.getSize();
 		if (eventAction == MotionEvent.ACTION_CANCEL
 				|| eventAction == MotionEvent.ACTION_UP) {
			final float viewX = event.getX();
			final float viewY = event.getY();
 			
			if (viewX >= 0 && viewX <= getMeasuredWidth() && viewY >= 0
					&& viewY <= getMeasuredHeight()) {
				final int cellX = (int) (event.getX() * playgroundSize / getMeasuredWidth());
				final int cellY = (int) (event.getY() * playgroundSize / getMeasuredHeight());

				if (mListener != null) {
					mListener.onClick(this, cellX, cellY);
				}
 			}
 		}
 		
 		return true;
 	}
 
 	@Override
 	public void onReset() {
 		reload();
 		
 	}
 
 	@Override
 	public void onElementChanged(int x, int y) {
 		reload(x, y);
 	}
 	
 }
