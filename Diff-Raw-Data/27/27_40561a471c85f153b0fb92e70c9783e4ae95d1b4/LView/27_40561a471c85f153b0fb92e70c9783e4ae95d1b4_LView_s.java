 package com.liongrid.gameengine.view;
 
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 
 import com.liongrid.gameengine.LBaseObject;
 import com.liongrid.gameengine.LGamePointers;
 import com.liongrid.gameengine.LInputDispatchInterface;
 import com.liongrid.gameengine.LRenderSystem;
 import com.liongrid.gameengine.LShape;
 import com.liongrid.gameengine.LShape.Square;
 import com.liongrid.gameengine.tools.LVector2;
 import com.liongrid.infectomancer.IMainMenuActivity;
 
 /**
  * @author Lastis
  * Because of the gesture detector we have implemented, it easier to dispatch spesific
  * gestures than the generic touch event. Therefore onTouch is not used as in android view.
  */
 public abstract class LView extends LBaseObject 
 		implements LShape.Square, LInputDispatchInterface {
 	
 	public interface OnClickListener{
 		void onClick(LView view);
 	}
 	public interface OnFocusChangeListener{
 		void onFocusChange(LView view, boolean hasFocus);
 	}
 	public interface OnKeyListener{
 		boolean onKey(LView hudElement, int keyCode, KeyEvent event);
 	}
 	public interface OnLongClickListener{
 		boolean onLongClick(LView view);
 	}
 	public interface OnScrollListener{
 		boolean onScroll(LView view, float distanceX, float distanceY);
 	}
 	public interface OnTouchListener{
 		boolean onTouch(LView view, MotionEvent event);
 	}
 	
 	public LVector2 mPos = new LVector2();
 	private int mWidth;
 	private int mHeight;
 	private boolean mClickable;
 	private boolean mLongClickable;
 	private boolean mFocusable;
 	private boolean mScrollable;
 	
 	protected int mMeasuredHeight;
 	protected int mMeasuredWidth;
 	
 	 /**
      * The minimum height of the view. We'll try our best to have the height
      * of this view to at least this amount.
      */
     private int mMinHeight;
     
     
 	
 	private boolean mPressed;
 	private boolean mSelected;
 	private boolean mFocused;
 	private boolean mVisible = true;
 	private boolean mEnabled = true;
 	
 	private boolean mOpaque;
 	private boolean mSoundEffectsEnabled;
 	protected LViewParent mParent;
 	
 	private OnClickListener mOnClickListener;
 	private OnLongClickListener mOnLongClickListener;
 	private OnKeyListener mOnKeyListener;
 	private OnFocusChangeListener mOnFocusChangeListener;
 	private OnScrollListener mOnScrollListener;
 	private OnTouchListener mOnTouchListener;
 	
 	private LView mNextFocusDown;
 	private LView mNextFocusUp;
 	private LView mNextFocusLeft;
 	private LView mNextFocusRight;
 	
 	public LView() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public boolean dispatchLongPress(MotionEvent e) {
 		boolean result = false;
 		if(!isEnabled()) return true;
 		if(isLongClickable()){
 			result = performLongPress();
 		}
 		return result;
 	}
 	
 	public boolean dispatchScroll(MotionEvent e1, MotionEvent e2,
 			float distanceX, float distanceY) {
 		return false;
 	}
 	
 	public boolean dispatchSingleTapUp(MotionEvent event) {
 		boolean result = false;
 		if(!isEnabled()) return true;
 		if(isClickable()){
 			result = performClick();
 		}
 		return result;
 	}
 
 	public boolean dispatchTouchDown(MotionEvent e) {
 		boolean result = false;
 		if(!isEnabled()) return true;
 		if((isClickable() || isLongClickable()) && !isPressed()){
 			setPressed(true);
 			result = performTouchDown();
 		}
 		return result;
 	}
 	
 	public boolean dispatchTouchEvent(MotionEvent event){
 		if(mOnTouchListener != null && 
 				(isEnabled() && mOnTouchListener.onTouch(this, event))){
 			return true;
 		}
 		return onTouchEvent(event);
 	}
 	
 	public boolean dispatchTouchUp(MotionEvent e) {
 		boolean result = false;
 		if(!isEnabled()) return true;
 		if(isPressed()){
 			setPressed(false);
 			result = performTouchUp();
 		}
 		return result;
 	}
 	/*
 	public LView focusSearch(int direction){
 		if(mParent != null){
 			return mParent.focusSearch(this, direction);
 		}
 		else{
 			return null;
 		}
 	}*/
 
 	public LVector2 getPos() {
 		return mPos;
 	}
 
 	public int getShape() {
 		return LShape.SQUARE;
 	}
 
 	protected void handleFocusGainInternal(int direction) {
 		if(!isFocused()){
 			setFocused(true);
 			if(mParent != null){
 				mParent.requestChildFocus(this, this);
 			}
 			onFocusChange(true, direction);
 		}
 	}
 
 	private boolean hasAncestorThatBlocksDescendantFocus() {
 		// Need to be tied to LViewGroup
 		return false;
 	}
 
 	public boolean isClickable() {
 		return mClickable;
 	}
 
 	public boolean isEnabled() {
 		return mEnabled;
 	}
 
 	public boolean isFocusable() {
 		return mFocusable;
 	}
 
 	public LView findFocus() {
 		return mFocused ? this : null;
 	}
 	public boolean isFocused() {
 		return mFocused;
     }
 
 
 	public boolean isLongClickable() {
 		return mLongClickable;
 	}
 
 	public boolean isOpaque() {
 		return mOpaque;
 	}
 
 	public boolean isPressed() {
 		return mPressed;
 	}
 
 	public boolean isScrollable() {
 		return mScrollable;
 	}
 
 	public boolean isSelected() {
 		return mSelected;
 	}
 
 	public boolean isSoundEffectsEnabled() {
 		return mSoundEffectsEnabled;
 	}
 
 	public boolean isVisible() {
 		return mVisible;
 	}
 
 	protected void onFocusChange(boolean gainFocus, int direction){
 		if(!gainFocus){
 			if(isPressed()){
 				setPressed(false);
 				onFocusLost();
 			}
 		}
 		if(mOnFocusChangeListener != null){
 			mOnFocusChangeListener.onFocusChange(this, gainFocus);
 		}
 	}
 	
 	protected void onFocusLost() {
 		
 	}
 	
 	protected boolean onTouchEvent(MotionEvent event) {
 		// Put code here if we need touch events to go faster
 		return false;
 	}
 	
 	public boolean performClick(){
 		if(mOnClickListener != null){
 			mOnClickListener.onClick(this);
 			return true;
 		}
 		return false;
 	}
 
 	private boolean performTouchUp() {
 		return false;
 	}
 	
 	public boolean performLongPress() {
 		if(mOnLongClickListener != null){
 			mOnLongClickListener.onLongClick(this);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean performScroll(float distanceX, float distanceY){
 		if(mOnScrollListener != null){
 			mOnScrollListener.onScroll(this, distanceX, distanceY);
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean performTouchDown() {
 		return false;
 	}
 	
 	public void playSoundEffect(){
 		
 	}
 	
 	public boolean requestFocus(){
 		return requestFocus(View.FOCUS_DOWN);
 	}
 	
 	public boolean requestFocus(int direction) {
 		if(!isFocusable() || !isVisible()){
 			return false;
 		}
 		if(hasAncestorThatBlocksDescendantFocus()){
 			return false;
 		}
 		handleFocusGainInternal(direction);
 		return true;
 	}
 
 	@Override
 	public void reset() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setClickable(boolean clickable) {
 		this.mClickable = clickable;
 	}
 	
 	public void setEnabled(boolean enabled) {
 		this.mEnabled = enabled;
 	}
 	
 	public void setFocusable(boolean focusable) {
 		this.mFocusable = focusable;
 	}
 	
 	public void setFocused(boolean focused) {
 		this.mFocused = focused;
 	}
 	
 	public void setLongClickable(boolean longClickable) {
 		this.mLongClickable = longClickable;
 	}
 	
 	public void setNextFocusDown(LView down){
 		mNextFocusDown = down;
 	}
 
 	public void setNextFocusLeft(LView left){
 		mNextFocusLeft = left;
 	}
 
 	public void setNextFocusRight(LView right){
 		mNextFocusRight = right;
 	}
 
 	public void setNextFocusUp(LView up){
 		mNextFocusUp = up;
 	}
 
 	public void setOnClickListener(OnClickListener l){
 		if(!isClickable()){
 			setClickable(true);
 		}
 		mOnClickListener = l;
 	}
 
 	public void setOnFocusChangeListener(OnFocusChangeListener l){
 		if(!isFocusable()){
 			setFocusable(true);
 		}
 		mOnFocusChangeListener = l;
 	}
 	
 	public void setOnKeyListener(OnKeyListener l){
 		mOnKeyListener = l;
 	}
 
 	public void setOnLongClickListener(OnLongClickListener l){
 		if(!isLongClickable()){
 			setLongClickable(true);
 		}
 		mOnLongClickListener = l;
 	}
 	
 	public void setOnScrollListener(OnScrollListener l){
 		if(!isScrollable()){
 			setScrollable(true);
 		}
 		mOnScrollListener = l;
 	}
 
 	public void setOpaque(boolean opaque) {
 		this.mOpaque = opaque;
 	}
 
 	public void setPressed(boolean pressed) {
 		this.mPressed = pressed;
 	}
 
 	public void setScrollable(boolean scrollable) {
 		this.mScrollable = scrollable;
 	}
 
 	public void setSelected(boolean selected) {
 		this.mSelected = selected;
 	}
 	
 	public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
 		this.mSoundEffectsEnabled = soundEffectsEnabled;
 	}
 	public void setVisible(boolean visible){
 		mVisible = visible;
 	}
 
 	@Override
 	public void update(float dt, LBaseObject parent) {
 		LRenderSystem rs = LGamePointers.renderSystem;
 		onDraw(rs);
 	}
 
 	protected abstract void onDraw(LRenderSystem rs);
 
 	public float getHeight() {
 		return mHeight;
 	}
 	
 	public float getWidth() {
 		return mWidth;
 	}
 
 	public void unFocus() {
 		if(mFocused){
 			mFocused = false;
 			onFocusChange(false, 0);
 		}
 	}
 	
 	/**
      * Gets the parent of this view. Note that the parent is a
      * ViewParent and not necessarily a View.
      *
      * @return Parent of this view.
      */
     public final LViewParent getParent() {
         return mParent;
     }
     
     /**
      * <p>
      * Measure the view and its content to determine the measured width and the
      * measured height. This method is invoked by {@link #measure(int, int)} and
      * should be overriden by subclasses to provide accurate and efficient
      * measurement of their contents.
      * </p>
      *
      * <p>
      * <strong>CONTRACT:</strong> When overriding this method, you
      * <em>must</em> call {@link #setMeasuredDimension(int, int)} to store the
      * measured width and height of this view. Failure to do so will trigger an
      * <code>IllegalStateException</code>, thrown by
      * {@link #measure(int, int)}. Calling the superclass'
      * {@link #onMeasure(int, int)} is a valid use.
      * </p>
      *
      * <p>
      * The base class implementation of measure defaults to the background size,
      * unless a larger size is allowed by the MeasureSpec. Subclasses should
      * override {@link #onMeasure(int, int)} to provide better measurements of
      * their content.
      * </p>
      *
      * <p>
      * If this method is overridden, it is the subclass's responsibility to make
      * sure the measured height and width are at least the view's minimum height
      * and width ({@link #getSuggestedMinimumHeight()} and
      * {@link #getSuggestedMinimumWidth()}).
      * </p>
      *
      * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
      *                         The requirements are encoded with
      *                         {@link android.view.View.MeasureSpec}.
      * @param heightMeasureSpec vertical space requirements as imposed by the parent.
      *                         The requirements are encoded with
      *                         {@link android.view.View.MeasureSpec}.
      *
      * @see #getMeasuredWidth()
      * @see #getMeasuredHeight()
      * @see #setMeasuredDimension(int, int)
      * @see #getSuggestedMinimumHeight()
      * @see #getSuggestedMinimumWidth()
      * @see android.view.View.MeasureSpec#getMode(int)
      * @see android.view.View.MeasureSpec#getSize(int)
      */
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(),
                 widthMeasureSpec), getDefaultSize(
                getSuggestedMinimumHeight(), heightMeasureSpec));
     }
     
     /**
      * Returns the suggested minimum width that the view should use. This
      * returns the maximum of the view's minimum width)
      * and the background's minimum width
      *  ({@link android.graphics.drawable.Drawable#getMinimumWidth()}).
      * <p>
      * When being used in {@link #onMeasure(int, int)}, the caller should still
      * ensure the returned width is within the requirements of the parent.
      *
      * @return The suggested minimum width of the view.
      */
    protected int getSuggestedMinimumWidth() {
         int suggestedMinWidth = mMinWidth;
 
         if (mBGDrawable != null) {
             final int bgMinWidth = mBGDrawable.getMinimumWidth();
             if (suggestedMinWidth < bgMinWidth) {
                 suggestedMinWidth = bgMinWidth;
             }
         }
 
         return suggestedMinWidth;
    }
     
     /**
      * Returns the suggested minimum height that the view should use. This
      * returns the maximum of the view's minimum height
      * and the background's minimum height
      * ({@link android.graphics.drawable.Drawable#getMinimumHeight()}).
      * <p>
      * When being used in {@link #onMeasure(int, int)}, the caller should still
      * ensure the returned height is within the requirements of the parent.
      *
      * @return The suggested minimum height of the view.
      */
     protected int getSuggestedMinimumHeight() {
         int suggestedMinHeight = mMinHeight;
 
        if (mBGDrawable != null) {
             final int bgMinHeight = mBGDrawable.getMinimumHeight();
             if (suggestedMinHeight < bgMinHeight) {
                 suggestedMinHeight = bgMinHeight;
             }
         }

         return suggestedMinHeight;
     }
 	
 }
