 package com.luzi82.chitanda.game.ui;
 
import com.badlogic.gdx.utils.Logger;
 
 public class CameraTouchLogic {
 
 	// touch
 	public static final int TOUCH_MAX = 16;
 	private boolean mTouchCountChange;
 	private boolean mTouchChange;
 	private boolean[] mTouching;
 	private int[] mTouchSX;
 	private int[] mTouchSY;
 	private float mTouchStartBXAvg;
 	private float mTouchStartBYAvg;
 	private float mTouchStartDiff;
 	private float mTouchStartCameraZoom;
 
 	// mouse
 	private int mMouseOverSX;
 	private int mMouseOverSY;
 	private int mMouseScrolled;
 
 	public CameraLogic mCameraLogic;
 
 	public CameraTouchLogic(CameraLogic aCameraLogic) {
 		mCameraLogic = aCameraLogic;
 
 		mTouching = new boolean[TOUCH_MAX];
 		mTouchSX = new int[TOUCH_MAX];
 		mTouchSY = new int[TOUCH_MAX];
 	}
 
 	public void update(float aDelta) {
 		int i;
 
 		float reduce = (float) Math.pow(CameraLogic.SMOOTH_REDUCE, aDelta);
 		float intReduce = (reduce - 1) * CameraLogic.DIV_LN_SMOOTH_REDUCE;
 
 		float touchSXAvg = 0;
 		float touchSYAvg = 0;
 		float touchDiff = 0;
 		int touchCount = 0;
 
 		for (i = 0; i < TOUCH_MAX; ++i) {
 			if (!mTouching[i])
 				continue;
 			touchSXAvg += mTouchSX[i];
 			touchSYAvg += mTouchSY[i];
 			++touchCount;
 		}
 		if (touchCount > 0) {
 			touchSXAvg /= touchCount;
 			touchSYAvg /= touchCount;
 			if (touchCount > 1) {
 				for (i = 0; i < TOUCH_MAX; ++i) {
 					if (!mTouching[i])
 						continue;
 					float d = 0, dd = 0;
 					dd = mTouchSX[i] - touchSXAvg;
 					dd *= dd;
 					d += dd;
 					dd = mTouchSY[i] - touchSYAvg;
 					dd *= dd;
 					d += dd;
 					touchDiff += (float) Math.sqrt(d);
 				}
 			}
 			touchDiff /= touchCount;
 			if (mTouchCountChange) {
 				mTouchStartBXAvg = mCameraLogic.screenToBoardX(touchSXAvg);
 				mTouchStartBYAvg = mCameraLogic.screenToBoardY(touchSYAvg);
 				if (touchCount > 1) {
 					mTouchStartDiff = touchDiff;
 					mTouchStartCameraZoom = mCameraLogic.iCameraZoom;
 				} else {
 					mCameraLogic.smoothZoom(aDelta, reduce, intReduce);
 					float newCameraBX = mCameraLogic.screenBoardToCameraX(touchSXAvg, mTouchStartBXAvg);
 					float newCameraBY = mCameraLogic.screenBoardToCameraY(touchSYAvg, mTouchStartBYAvg);
 					mCameraLogic.xyMove(newCameraBX, newCameraBY, aDelta);
 				}
 				mTouchCountChange = false;
 			} else if (mTouchChange) {
 				if (touchCount > 1) {
 					float newZoom = mTouchStartCameraZoom * mTouchStartDiff / touchDiff;
 					mCameraLogic.zoomMove(newZoom, aDelta);
 				} else {
 					mCameraLogic.smoothZoom(aDelta, reduce, intReduce);
 				}
 				float newCameraBX = mCameraLogic.screenBoardToCameraX(touchSXAvg, mTouchStartBXAvg);
 				float newCameraBY = mCameraLogic.screenBoardToCameraY(touchSYAvg, mTouchStartBYAvg);
 				mCameraLogic.xyMove(newCameraBX, newCameraBY, aDelta);
 			} else if (touchCount == 1) {
 				mCameraLogic.smoothZoom(aDelta, reduce, intReduce);
 				float newCameraBX = mCameraLogic.screenBoardToCameraX(touchSXAvg, mTouchStartBXAvg);
 				float newCameraBY = mCameraLogic.screenBoardToCameraY(touchSYAvg, mTouchStartBYAvg);
 				mCameraLogic.xySet(newCameraBX, newCameraBY);
 			}
 		} else if (mMouseScrolled != 0) {
 			float mouseBX = mCameraLogic.screenToBoardX(mMouseOverSX);
 			float mouseBY = mCameraLogic.screenToBoardY(mMouseOverSY);
 
 			mCameraLogic.mCameraZoomD -= mMouseScrolled * CameraLogic.PHI;
 			mCameraLogic.smoothZoom(aDelta, reduce, intReduce);
 
 			float newCameraBX = mCameraLogic.screenBoardToCameraX(mMouseOverSX, mouseBX);
 			float newCameraBY = mCameraLogic.screenBoardToCameraY(mMouseOverSY, mouseBY);
 			// mCameraXD = (newX - iCameraX) / aDelta;
 			// mCameraYD = (newY - iCameraY) / aDelta;
 			// iCameraX = newX;
 			// iCameraY = newY;
 			mCameraLogic.xyMove(newCameraBX, newCameraBY, aDelta);
 			mMouseScrolled = 0;
 		} else {
 			mCameraLogic.smoothZoom(aDelta, reduce, intReduce);
 			mCameraLogic.smoothXY(aDelta, reduce, intReduce);
 		}
 		mTouchChange = false;
 	}
 
 	public void touchDown(int aSX, int aSY, int aPointer, int aButton) {
 		// iLogger.debug("touchDown");
 		mTouchCountChange = true;
 		mTouchChange = true;
 		mTouching[aPointer] = true;
 		mTouchSX[aPointer] = aSX;
 		mTouchSY[aPointer] = aSY;
 	}
 
 	public void touchUp(int aSX, int aSY, int aPointer, int aButton) {
 		// iLogger.debug("touchUp");
 		mTouchCountChange = true;
 		mTouchChange = true;
 		mTouching[aPointer] = false;
 	}
 
 	public void touchDragged(int aSX, int aSY, int aPointer) {
 		// iLogger.debug("touchDragged");
 		mTouching[aPointer] = true;
 		mTouchChange = ((mTouchSX[aPointer] != aSX) || (mTouchSY[aPointer] != aSY));
 		mTouchSX[aPointer] = aSX;
 		mTouchSY[aPointer] = aSY;
 	}
 
 	public void touchMoved(int aX, int aY) {
 		// iLogger.debug("touchMoved");
 		mMouseOverSX = aX;
 		mMouseOverSY = aY;
 	}
 
 	public void scrolled(int aAmount) {
 		mMouseScrolled += aAmount;
 	}
 
 }
