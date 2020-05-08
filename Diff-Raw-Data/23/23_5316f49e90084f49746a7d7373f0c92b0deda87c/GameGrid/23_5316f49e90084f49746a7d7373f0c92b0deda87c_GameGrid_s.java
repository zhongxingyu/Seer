 package com.smfandroid.game100;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.support.v4.view.VelocityTrackerCompat;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.smfandroid.game100.C100GameCore.IllegalMoveException;
 
 public class GameGrid extends LinearLayout implements OnTouchListener {
 
 	protected class GridElement extends TextView {
 		protected int mValue;
 		
 		public int getValue() {
 			return mValue;
 		}
 		
 		public void setEmptyValue() {
 			mValue = -1;
 			updateValue();
 		}
 
 		public void setPossibleValue() {
 			mValue = -2;
 			updateValue();
 		}
 		
 		public void setValue(int value) {
 			if(value <=0)
 				throw new IllegalArgumentException("The value is a stricly positive integer");
 			
 			this.mValue = value;
 			updateValue();
 		}
 
 		private void updateValue() {
 			if(mValue > 0)
 				setText(Integer.toString(mValue));
 			else
 				setText(" ");
 			if(mValue == -2)
 				setBackgroundColor(Color.GREEN);
 			else
 				setBackgroundColor(Color.WHITE);
 		}
 		
 		public GridElement(Context context) {
 			super(context);
 			this.setBackgroundColor(Color.WHITE);
 			this.setWidth(0);
 			this.setGravity(Gravity.CENTER);
 			setEmptyValue();
 		}
 		
 	}
 	protected final String TAG = getClass().getSimpleName();
 
 	private int mNbRows;
 	private int mNbColumns;
 	private Random mRndGenerator = new Random();
 	private C100GameCore mGameCore;
 	
 	private VelocityTracker mVelocityTracker = null;
 
 	public void setSize(int nbRows, int nbColumns) {
 		if(nbRows <= 0 || nbRows <=0)
 			throw new IllegalArgumentException("No size value can be zero or less");
 
 		this.mNbRows = nbRows;
 		this.mNbColumns = nbColumns;
 
 		refreshGrid();
 	}
 
 	public int getNbRows() {
 		return mNbRows;
 	}
 
 	public int getNbColumns() {
 		return mNbColumns;
 	}
 
 	protected View CreateRow() {
 		LinearLayout row = new LinearLayout(getContext());
 		row.setOrientation(LinearLayout.HORIZONTAL);
 
 		LayoutParams layoutElement = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
 		layoutElement.setMargins(2, 2, 2, 2);
 
 		for(int j = 0; j < getNbColumns(); j++) {
 			row.addView(new GridElement(getContext()), layoutElement);
 		}		
 		row.setGravity(Gravity.CENTER);
 		return row;
 	}
 
 	public final int EMPTY_VALUE = -1;
 	public final int POSSIBLE_VALUE = -2;
 
 
 	public void reset() {
 		Point initPoint = new Point();
 		initPoint.x = mRndGenerator.nextInt(mNbColumns);
 		initPoint.y = mRndGenerator.nextInt(mNbRows);
 
 		mGameCore = new C100GameCore(new Point(mNbRows, mNbColumns), initPoint, 1);
 		redrawValues();
 	}
 	
 	protected void setValueAt(Point position, int value) {
 		LinearLayout l = (LinearLayout)getChildAt(position.y);
 		GridElement gridE = (GridElement)l.getChildAt(position.x);
 		if(value == EMPTY_VALUE)
 			gridE.setEmptyValue();
 		else if(value == POSSIBLE_VALUE)
 			gridE.setPossibleValue();
 		else			
 			gridE.setValue(value);
 	}
 	
 	private void redrawValues() {
 		for(int i = 0; i < getChildCount(); i++) {
 			LinearLayout l = (LinearLayout)getChildAt(i);
 			for(int j = 0; j< l.getChildCount(); j++) {
 				((GridElement)l.getChildAt(j)).setEmptyValue();
 			}
 		}
 		int val = 0;
 		for(Point p:mGameCore.GetState()) {
 			val += 1; 
 			setValueAt(p, val);
 		}
 		for(Point p:mGameCore.PossibleMoves()) {
 			setValueAt(p, POSSIBLE_VALUE);
 		}
 	}
 
 	public void popLastMove() {
 		mGameCore.PopMove();
 		redrawValues();
 	}
 	
 	public void nextPointSelected(Point p) {
 		Point oldPoint, newPoint;
 		LinkedList<Point> lMoves = mGameCore.GetState();
 		oldPoint = lMoves.getLast();
 		newPoint = new Point();
 		newPoint.x =  oldPoint.x + p.x;
 		newPoint.y += oldPoint.y + p.y;
 		try {
 			mGameCore.PushMove(newPoint);
 			redrawValues();
 		} catch (IllegalMoveException e) {
 			Toast.makeText(getContext(), R.string.msg_illegal_move, Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 
 	public void refreshGrid() {
 		this.removeAllViews();
 
 		this.setOrientation(LinearLayout.VERTICAL);
 		LayoutParams layoutRow = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
 
 		for(int i = 0; i< getNbRows(); i++) {
 			this.addView(CreateRow(), layoutRow);
 		}
 	}	
 
 	public GameGrid(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.setBackgroundColor(Color.BLACK);
 		setSize(10, 10);		
 		setOnTouchListener(this);
 		reset();
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent event) {
 		Log.i(TAG, "Touch detected");
 		int index = event.getActionIndex();
 		int action = event.getActionMasked();
 		int pointerId = event.getPointerId(index);
 
 		switch(action) {
 		case MotionEvent.ACTION_DOWN:
 			if(mVelocityTracker == null) {
 				// Retrieve a new VelocityTracker object to watch the velocity of a motion.
 				mVelocityTracker = VelocityTracker.obtain();
 			}
 			else {
 				// Reset the velocity tracker back to its initial state.
 				mVelocityTracker.clear();
 			}
 			// Add a user's movement to the tracker.
 			mVelocityTracker.addMovement(event);
 			break;
 		case MotionEvent.ACTION_MOVE:
 			mVelocityTracker.addMovement(event);
 			break;
 		case MotionEvent.ACTION_UP:
 			Point nextPoint = new Point();
 			// When you want to determine the velocity, call 
 			// computeCurrentVelocity(). Then call getXVelocity() 
 			// and getYVelocity() to retrieve the velocity for each pointer ID. 
 			mVelocityTracker.computeCurrentVelocity(1000);
 			// Log velocity of pixels per second
 			// Best practice to use VelocityTrackerCompat where possible.
 			float x = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
 			float y = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
 
 			// Clearly one direction is stronger than the other
 			if(Math.abs(x) > 2 * Math.abs(y)) {
 				if(x > 0)
					nextPoint = new Point(2,0);
 				else
					nextPoint = new Point(-2,0);
 			} else if(Math.abs(x) < 0.5 * Math.abs(y)) {
 				if(y > 0)
					nextPoint = new Point(0,2);
 				else
					nextPoint = new Point(0,-2);
 				// Diagonals
 			} else if(x > 0) { // X > 0
 			if(y > 0)
				nextPoint = new Point(1,1);
 			else
				nextPoint = new Point(1,-1);
 			} else             // X < 0
 				if (y > 0)
					nextPoint = new Point(-1,1);
 				else
					nextPoint = new Point(-1,-1);
 			nextPointSelected(nextPoint);
 		case MotionEvent.ACTION_CANCEL:
 			// Return a VelocityTracker object back to be re-used by others.
 			mVelocityTracker.recycle();
 			break;
 		}
 		return true;
 	}
 
 
 	
 }
