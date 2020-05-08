 package com.killoctal.andenginecontrols.detectors;
 
 import org.andengine.entity.scene.ITouchArea;
 import org.andengine.input.touch.TouchEvent;
 
 /**
  * @brief A pointer detector class for fix ACTION_UP and ACTION_MOVE problems
  * 
  * @note The "leaving" event requires the scene has setTouchAreaBindingOnActionDownEnabled(true) 
  */
 public class PointerDetector
 {
 	
 	public static interface IClickListener {
 		/// Executed when pressed and released inside
 		void onClick(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, long pPressDuration);
 		
 		/// Executed when pressed (~ACTION_DOWN)
 		void onPress(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY);
 		
 		/// Executed when released inside (~ACTION_UP) or leaved
 		void onRelease(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pInside);
 	}
 	
 	
 	public static interface IMoveListener {
 		/// Executed when pointer is moving
 		void onMove(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pInside);
 		
 		/// Executed when dragging inside
 		void onEnter(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pIsPressed);
 		
 		/// Executed when dragging out
 		void onLeave(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pIsPressed);
 	}
 	
 	
 	
 	
 	boolean mIsPressed;
 	boolean mIsPointerInside;
 	
 	
 	private int mPointerID;
 	
 	private boolean mIsEnabled;
 	
 	private boolean mIsModal;
 	private boolean mIsModalThisTime;
 	private boolean mIsModalTotal;
 	
 	private long mPressTime;
 	protected float mPressX, mPressY;
 	
 	
 	public ITouchArea mTouchArea;
 	
 	/**
 	 * @name Listeners
 	 * @{
 	 */
 	public IClickListener mClickListener;
 	public IMoveListener mMoveListener;
 	/**
 	 * @}
 	 */
 	
 	
 	
 	/**
 	 * @brief Constructor
 	 * @param pTouchArea A touchArea to link with
 	 */
 	public PointerDetector(ITouchArea pTouchArea)
 	{
 		mTouchArea = pTouchArea;
 		
 		// Creates the listeners lists
 		mClickListener = null;
 		mMoveListener = null;
 		
 		mIsEnabled = true;
 		mPointerID = TouchEvent.INVALID_POINTER_ID;
 		
 		mIsModalThisTime = false;
 		
 		mIsPressed = false;
 		mIsPointerInside = false;
 		
 		setModal(true);
 	}
 	
 	
 	
 	/**
 	 * @overload
 	 */
 	public PointerDetector()
 	{
 		this(null);
 	}
 	
 	
 	
 	/**
 	 * @brief Handle the touch event
 	 * 
 	 * The onTouchArea of the object must execute this method
 	 */
 	public boolean handleTouchEvent(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
 	{
 		boolean tmpInside = mTouchArea == null || mTouchArea.contains(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
 		
 		// Catch the pointer ID
 		if (mPointerID == TouchEvent.INVALID_POINTER_ID)
 		{
			// If item concerned, catch the ID and continue
			if (tmpInside)
			{
				mPointerID = pSceneTouchEvent.getPointerID();
			}
			else // If item not concerned just don't handle the event
			{
				return false;
			}
 		}
 		
 		// If the pointer is the right one and not an other
 		if (mPointerID == pSceneTouchEvent.getPointerID())
 		{
 			if (pSceneTouchEvent.isActionDown())
 			{
 				// Useless check but for the principe
 				if (tmpInside)
 				{
 					mPressTime = System.currentTimeMillis();
 					setPressed(true);
 
 					// Pointer press position
 					mPressX = pTouchAreaLocalX;
 					mPressY = pTouchAreaLocalY;
 
 					// Execute the listner
 					executeOnPressListeners(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
 				}
 			}
 			else if (pSceneTouchEvent.isActionUp())
 			{
 				// Execute the listner
 				executeOnReleaseListeners(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, tmpInside);
 
 				if (tmpInside)
 				{
 					// If pressed => click
 					if (mIsPressed)
 					{
 						setPressed(false);
 
 						// Execute the listner
 						executeOnClickListeners(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, System.currentTimeMillis() - mPressTime);
 					}
 				}
 
 				// Free the catched pointer
 				freePointer();
 			}
 			else if (pSceneTouchEvent.isActionMove())
 			{
 				if (tmpInside)
 				{
 					if (! mIsPointerInside)
 					{
 						mIsPointerInside = true;
 
 						// Execute the listners
 						executeOnEnterListeners(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, mIsPressed);
 					}
 				}
 				else
 				{
 					if (mIsPointerInside)
 					{
 						mIsPointerInside = false;
 
 						// Execute the listner
 						executeOnLeaveListeners(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, mIsPressed);
 
 						// If pressed, release the button
 						if (mIsPressed)
 						{
 							setPressed(false);
 
 							// Execute the listner
 							executeOnReleaseListeners(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, false);
 						}
 					}
 				}
 
 				// Execute the listner
 				executeOnMoveListeners(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, tmpInside);
 			}
 		}
 		
 		
 		// If modal once
 		if (mIsModalThisTime)
 		{
 			mIsModalThisTime = false;
 			return true;
 		}
 		
 		
 		// If always modal
 		if (mIsModal)
 		{
 			if (mIsModalTotal)
 			{
 				return true;
 			}
 			return tmpInside;
 		}
 		
 		// Not handled
 		return false;
 	}
 	
 	
 	
 	
 	
 	
 	
 	public void setEnabled(boolean pEnabled)
 	{
 		mIsEnabled = pEnabled;
 	}
 	
 	public boolean isEnabled()
 	{
 		return mIsEnabled;
 	}
 	
 	
 	final public boolean isPressed()
 	{
 		return mIsPressed;
 	}
 	
 	
 	
 	final public void freePointer()
 	{
 		mPointerID = TouchEvent.INVALID_POINTER_ID;
 	}
 	
 	
 	final public void setModal(boolean pModal, boolean pTotal)
 	{
 		mIsModal = pModal;
 		mIsModalTotal = pTotal;
 	}
 
 	final public void setModal(boolean pModal)
 	{
 		setModal(pModal, true);
 	}
 	
 	
 	
 	final public boolean isModal()
 	{
 		return mIsModal;
 	}
 	
 	final public void setModalThisTime()
 	{
 		mIsModalThisTime = true;
 	}
 	
 	
 	/**
 	 * 
 	 * @param pPressed
 	 * @note This does not execute any "on...()" method
 	 */
 	public void setPressed(boolean pPressed)
 	{
 		mIsPressed = pPressed;
 		mIsPointerInside = pPressed;
 	}
 	
 	
 	public float getPressX()
 	{
 		return mPressX;
 	}
 	public float getPressY()
 	{
 		return mPressY;
 	}
 	
 	
 	
 	
 	protected void executeOnClickListeners(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, long pPressDuration)
 	{
 		if (mClickListener != null)
 			mClickListener.onClick(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, pPressDuration);
 	}
 	
 	
 	protected void executeOnPressListeners(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
 	{
 		if (mClickListener != null)
 			mClickListener.onPress(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
 	}
 	
 	
 	protected void executeOnReleaseListeners(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pInside)
 	{
 		if (mClickListener != null)
 			mClickListener.onRelease(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, pInside);
 	}
 	
 	
 	protected void executeOnMoveListeners(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pInside)
 	{
 		if (mMoveListener != null)
 			mMoveListener.onMove(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, pInside);
 	}
 	
 	
 	protected void executeOnEnterListeners(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pIsPressed)
 	{
 		if (mMoveListener != null)
 			mMoveListener.onEnter(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, pIsPressed);
 	}
 	
 	
 	protected void executeOnLeaveListeners(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY, boolean pIsPressed)
 	{
 		if (mMoveListener != null)
 			mMoveListener.onLeave(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY, pIsPressed);
 	}
 
 	
 
 }
