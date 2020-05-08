 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package android.view.cts;
 
 import java.util.ArrayList;
 
 import android.app.cts.CTSResult;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.XmlResourceParser;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.Region;
 import android.graphics.Bitmap.Config;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Parcelable;
 import android.os.SystemClock;
 import android.test.InstrumentationTestCase;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.SparseArray;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.BaseSavedState;
 import android.view.View.MeasureSpec;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewGroup.OnHierarchyChangeListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.LayoutAnimationController;
 import android.view.animation.RotateAnimation;
 import android.view.animation.Transformation;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.TextView;
 import android.widget.cts.ViewGroupStubActivity;
 
 import com.android.internal.util.XmlUtils;
 
 import dalvik.annotation.TestTargets;
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.ToBeFixed;
 
 @TestTargetClass(ViewGroup.class)
 public class ViewGroupTest extends InstrumentationTestCase implements CTSResult{
 
     private Context mContext;
     private MotionEvent mMotionEvent;
 
    private Object mSync = new Object();
    private boolean mHasNotify;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mContext = getInstrumentation().getTargetContext();
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test constructor",
             method = "ViewGroup",
             args = {android.content.Context.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test constructor",
             method = "ViewGroup",
             args = {android.content.Context.class, android.util.AttributeSet.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test constructor",
             method = "ViewGroup",
             args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
         )
     })
     public void testConstructor() {
         new MockViewGroup(mContext);
         new MockViewGroup(mContext, null);
         new MockViewGroup(mContext, null, 0);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addFocusables",
         method = "addFocusables",
         args = {java.util.ArrayList.class, int.class}
     )
     public void testAddFocusables() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setFocusable(true);
 
         ArrayList<View> list = new ArrayList<View>();
         TextView textView = new TextView(mContext);
         list.add(textView);
         vg.addView(textView);
         vg.addFocusables(list, 0);
 
         assertEquals(2, list.size());
 
         list = new ArrayList<View>();
         list.add(textView);
         vg.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         vg.setFocusable(false);
         vg.addFocusables(list, 0);
         assertEquals(1, list.size());
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test addStatesFromChildren",
             method = "addStatesFromChildren",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test addStatesFromChildren",
             method = "setAddStatesFromChildren",
             args = {boolean.class}
         )
     })
     public void testAddStatesFromChildren() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
         vg.addView(textView);
         assertFalse(vg.addStatesFromChildren());
 
         vg.setAddStatesFromChildren(true);
         textView.performClick();
         assertTrue(vg.addStatesFromChildren());
         assertTrue(vg.mIsDrawableStateChangedCalled);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test addTouchables function",
             method = "addTouchables",
             args = {java.util.ArrayList.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test addTouchables function",
             method = "getChildAt",
             args = {int.class}
         )
     })
     public void testAddTouchables() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setFocusable(true);
 
         ArrayList<View> list = new ArrayList<View>();
         TextView textView = new TextView(mContext);
         textView.setVisibility(View.VISIBLE);
         textView.setClickable(true);
         textView.setEnabled(true);
 
         list.add(textView);
         vg.addView(textView);
         vg.addTouchables(list);
 
         assertEquals(2, list.size());
 
         View v = vg.getChildAt(0);
         assertSame(textView, v);
 
         v = vg.getChildAt(-1);
         assertNull(v);
 
         v = vg.getChildAt(1);
         assertNull(v);
 
         v = vg.getChildAt(100);
         assertNull(v);
 
         v = vg.getChildAt(-100);
         assertNull(v);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addView function",
         method = "addView",
         args = {android.view.View.class}
     )
     public void testAddView() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         vg.addView(textView);
         assertEquals(1, vg.getChildCount());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addView function",
         method = "addView",
         args = {android.view.View.class, int.class}
     )
     public void testAddViewWithParaViewInt() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         vg.addView(textView, -1);
         assertEquals(1, vg.getChildCount());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addView function",
         method = "addView",
         args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class}
     )
     public void testAddViewWithParaViewLayoutPara() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         vg.addView(textView, new ViewGroup.LayoutParams(100, 200));
 
         assertEquals(1, vg.getChildCount());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addView function",
         method = "addView",
         args = {android.view.View.class, int.class, int.class}
     )
     public void testAddViewWithParaViewIntInt() {
         final int width = 100;
         final int height = 200;
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         vg.addView(textView, width, height);
         assertEquals(width, textView.getLayoutParams().width);
         assertEquals(height, textView.getLayoutParams().height);
 
         assertEquals(1, vg.getChildCount());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addView function",
         method = "addView",
         args = {android.view.View.class, int.class, android.view.ViewGroup.LayoutParams.class}
     )
     public void testAddViewWidthParaViewIntLayoutParam() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         vg.addView(textView, -1, new ViewGroup.LayoutParams(100, 200));
 
         assertEquals(1, vg.getChildCount());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addViewInLayout function",
         method = "addViewInLayout",
         args = {android.view.View.class, int.class, android.view.ViewGroup.LayoutParams.class}
     )
     public void testAddViewInLayout() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         assertTrue(vg.mIsRequestLayoutCalled);
         vg.mIsRequestLayoutCalled = false;
         assertTrue(vg.addViewInLayout(textView, -1, new ViewGroup.LayoutParams(100, 200)));
         assertEquals(1, vg.getChildCount());
         // check that calling addViewInLayout() does not trigger a
         // requestLayout() on this ViewGroup
         assertFalse(vg.mIsRequestLayoutCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test attachLayoutAnimationParameters function",
         method = "attachLayoutAnimationParameters",
        args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class, int.class,
                 int.class}
     )
     public void testAttachLayoutAnimationParameters() {
         MockViewGroup vg = new MockViewGroup(mContext);
         ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(10, 10);
 
         vg.attachLayoutAnimationParameters(null, param, 1, 2);
         assertEquals(2, param.layoutAnimationParameters.count);
         assertEquals(1, param.layoutAnimationParameters.index);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test attachViewToParent function",
         method = "attachViewToParent",
         args = {android.view.View.class, int.class, android.view.ViewGroup.LayoutParams.class}
     )
     public void testAttachViewToParent() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setFocusable(true);
         assertEquals(0, vg.getChildCount());
 
         ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(10, 10);
 
         TextView child = new TextView(mContext);
         child.setFocusable(true);
         vg.attachViewToParent(child, -1, param);
         assertSame(vg, child.getParent());
         assertEquals(1, vg.getChildCount());
         assertSame(child, vg.getChildAt(0));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test addViewInLayout function",
         method = "addViewInLayout",
        args = {android.view.View.class, int.class, android.view.ViewGroup.LayoutParams.class,
                 boolean.class}
     )
     public void testAddViewInLayoutWithParamViewIntLayB() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         assertTrue(vg.mIsRequestLayoutCalled);
         vg.mIsRequestLayoutCalled = false;
         assertTrue(vg.addViewInLayout(textView, -1, new ViewGroup.LayoutParams(100, 200), true));
 
         assertEquals(1, vg.getChildCount());
         // check that calling addViewInLayout() does not trigger a
         // requestLayout() on this ViewGroup
         assertFalse(vg.mIsRequestLayoutCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test bringChildToFront function",
         method = "bringChildToFront",
         args = {android.view.View.class}
     )
     public void testBringChildToFront() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView1 = new TextView(mContext);
         TextView textView2 = new TextView(mContext);
 
         assertEquals(0, vg.getChildCount());
 
         vg.addView(textView1);
         vg.addView(textView2);
         assertEquals(2, vg.getChildCount());
 
         vg.bringChildToFront(textView1);
         assertEquals(vg, textView1.getParent());
         assertEquals(2, vg.getChildCount());
         assertNotNull(vg.getChildAt(0));
         assertSame(textView2, vg.getChildAt(0));
 
         vg.bringChildToFront(textView2);
         assertEquals(vg, textView2.getParent());
         assertEquals(2, vg.getChildCount());
         assertNotNull(vg.getChildAt(0));
         assertSame(textView1, vg.getChildAt(0));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test canAnimate function",
         method = "canAnimate",
         args = {}
     )
     public void testCanAnimate() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertFalse(vg.canAnimate());
 
         RotateAnimation animation = new RotateAnimation(0.1f, 0.1f);
         LayoutAnimationController la = new LayoutAnimationController(animation);
         vg.setLayoutAnimation(la);
         assertTrue(vg.canAnimate());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test checkLayoutParams function",
         method = "checkLayoutParams",
         args = {android.view.ViewGroup.LayoutParams.class}
     )
     public void testCheckLayoutParams() {
         MockViewGroup view = new MockViewGroup(mContext);
         assertFalse(view.checkLayoutParams(null));
 
         assertTrue(view.checkLayoutParams(new ViewGroup.LayoutParams(100, 200)));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test childDrawableStateChanged function",
         method = "childDrawableStateChanged",
         args = {android.view.View.class}
     )
     public void testChildDrawableStateChanged() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setAddStatesFromChildren(true);
 
         vg.childDrawableStateChanged(null);
         assertTrue(vg.mIsRefreshDrawableStateCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test cleanupLayoutState function",
         method = "cleanupLayoutState",
         args = {android.view.View.class}
     )
     public void testCleanupLayoutState() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertTrue(textView.isLayoutRequested());
 
         vg.cleanupLayoutState(textView);
         assertFalse(textView.isLayoutRequested());
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test clearChildFocus and getFocusedChild function",
             method = "clearChildFocus",
             args = {android.view.View.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test clearChildFocus and getFocusedChild function",
             method = "getFocusedChild",
             args = {}
         )
     })
     public void testClearChildFocus() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         vg.addView(textView);
         vg.requestChildFocus(textView, null);
 
         View focusedView = vg.getFocusedChild();
         assertSame(textView, focusedView);
 
         vg.clearChildFocus(textView);
         assertNull(vg.getFocusedChild());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test clearDisappearingChildren function",
         method = "clearDisappearingChildren",
         args = {}
     )
     public void testClearDisappearingChildren() {
 
         Canvas canvas = new Canvas();
         MockViewGroup vg = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
         son.setAnimation(new MockAnimation());
         vg.addView(son);
         assertEquals(1, vg.getChildCount());
 
         assertNotNull(son.getAnimation());
         vg.dispatchDraw(canvas);
         assertEquals(1, vg.mDrawChildCalledTime);
 
         son.setAnimation(new MockAnimation());
         vg.removeAllViewsInLayout();
 
         vg.mDrawChildCalledTime = 0;
         vg.dispatchDraw(canvas);
         assertEquals(1, vg.mDrawChildCalledTime);
 
         son.setAnimation(new MockAnimation());
         vg.clearDisappearingChildren();
 
         vg.mDrawChildCalledTime = 0;
         vg.dispatchDraw(canvas);
         assertEquals(0, vg.mDrawChildCalledTime);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test clearFocus",
         method = "clearFocus",
         args = {}
     )
     public void testClearFocus() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
 
         vg.addView(textView);
         vg.requestChildFocus(textView, null);
         vg.clearFocus();
         assertTrue(textView.mIsClearFocusCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test detachAllViewsFromParent function",
         method = "detachAllViewsFromParent",
         args = {}
     )
     public void testDetachAllViewsFromParent() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         vg.addView(textView);
         assertEquals(1, vg.getChildCount());
         assertSame(vg, textView.getParent());
         vg.detachAllViewsFromParent();
         assertEquals(0, vg.getChildCount());
         assertNull(textView.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test detachViewFromParent function",
         method = "detachViewFromParent",
         args = {int.class}
     )
     public void testDetachViewFromParent() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         vg.addView(textView);
         assertEquals(1, vg.getChildCount());
 
         vg.detachViewFromParent(0);
 
         assertEquals(0, vg.getChildCount());
         assertNull(textView.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test detachViewFromParent function",
         method = "detachViewFromParent",
         args = {android.view.View.class}
     )
     public void testDetachViewFromParentWithParamView() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         vg.addView(textView);
         assertEquals(1, vg.getChildCount());
         assertSame(vg, textView.getParent());
 
         vg.detachViewFromParent(textView);
 
         assertEquals(0, vg.getChildCount());
         assertNull(vg.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test detachViewsFromParent function",
         method = "detachViewsFromParent",
         args = {int.class, int.class}
     )
     public void testDetachViewsFromParent() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView1 = new TextView(mContext);
         TextView textView2 = new TextView(mContext);
         TextView textView3 = new TextView(mContext);
 
         vg.addView(textView1);
         vg.addView(textView2);
         vg.addView(textView3);
         assertEquals(3, vg.getChildCount());
 
         vg.detachViewsFromParent(0, 2);
 
         assertEquals(1, vg.getChildCount());
         assertNull(textView1.getParent());
         assertNull(textView2.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchDraw function",
         method = "dispatchDraw",
         args = {android.graphics.Canvas.class}
     )
     public void testDispatchDraw() {
         MockViewGroup vg = new MockViewGroup(mContext);
         Canvas canvas = new Canvas();
 
         vg.draw(canvas);
         assertTrue(vg.mIsDispatchDrawCalled);
         assertSame(canvas, vg.mCanvas);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchFreezeSelfOnly function",
         method = "dispatchFreezeSelfOnly",
         args = {android.util.SparseArray.class}
     )
     @SuppressWarnings("unchecked")
     public void testDispatchFreezeSelfOnly() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setId(1);
         vg.setSaveEnabled(true);
 
         SparseArray container = new SparseArray();
         assertEquals(0, container.size());
         vg.dispatchFreezeSelfOnly(container);
         assertEquals(1, container.size());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchKeyEvent function",
         method = "dispatchKeyEvent",
         args = {android.view.KeyEvent.class}
     )
     public void testDispatchKeyEvent() {
         MockViewGroup vg = new MockViewGroup(mContext);
         KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
         assertFalse(vg.dispatchKeyEvent(event));
 
         MockTextView textView = new MockTextView(mContext);
         vg.addView(textView);
         vg.requestChildFocus(textView, null);
         textView.setFrame(1, 1, 100, 100);
 
         assertTrue(vg.dispatchKeyEvent(event));
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "dispatchRestoreInstanceState",
             args = {android.util.SparseArray.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "dispatchSaveInstanceState",
             args = {android.util.SparseArray.class}
         )
     })
     @SuppressWarnings("unchecked")
     public void testDispatchSaveInstanceState() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setId(2);
         vg.setSaveEnabled(true);
         MockTextView textView = new MockTextView(mContext);
         textView.setSaveEnabled(true);
         textView.setId(1);
         vg.addView(textView);
 
         SparseArray array = new SparseArray();
         vg.dispatchSaveInstanceState(array);
 
         assertTrue(array.size() > 0);
         assertNotNull(array.get(2));
 
         array = new SparseArray();
         vg.dispatchRestoreInstanceState(array);
         assertTrue(textView.mIsDispatchRestoreInstanceStateCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchSetPressed function",
         method = "dispatchSetPressed",
         args = {boolean.class}
     )
     public void testDispatchSetPressed() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         vg.addView(textView);
 
         vg.dispatchSetPressed(true);
         assertTrue(textView.isPressed());
 
         vg.dispatchSetPressed(false);
         assertFalse(textView.isPressed());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchSetSelected function",
         method = "dispatchSetSelected",
         args = {boolean.class}
     )
     public void testDispatchSetSelected() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         vg.addView(textView);
 
         vg.dispatchSetSelected(true);
         assertTrue(textView.isSelected());
 
         vg.dispatchSetSelected(false);
         assertFalse(textView.isSelected());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchSetSelected function",
         method = "dispatchThawSelfOnly",
         args = {android.util.SparseArray.class}
     )
     @SuppressWarnings("unchecked")
     public void testDispatchThawSelfOnly() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setId(1);
         SparseArray array = new SparseArray();
         array.put(1, BaseSavedState.EMPTY_STATE);
 
         vg.dispatchThawSelfOnly(array);
         assertTrue(vg.mIsOnRestoreInstanceStateCalled);
 
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchTouchEvent function",
         method = "dispatchTouchEvent",
         args = {android.view.MotionEvent.class}
     )
     public void testDispatchTouchEvent() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         DisplayMetrics metrics = new DisplayMetrics();
         WindowManager wm = (WindowManager) mContext.getSystemService(
                 Context.WINDOW_SERVICE);
         Display d = wm.getDefaultDisplay();
         d.getMetrics(metrics);
         int screenWidth = metrics.widthPixels;
         int screenHeight = metrics.heightPixels;
         vg.setFrame(0, 0, screenWidth, screenHeight);
         vg.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenHeight));
 
         MockTextView textView = new MockTextView(mContext);
         mMotionEvent = null;
         textView.setOnTouchListener(new OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 mMotionEvent = event;
                 return true;
             }
         });
 
         textView.setVisibility(View.VISIBLE);
         textView.setEnabled(true);
 
         vg.addView(textView, new LayoutParams(screenWidth, screenHeight));
 
         vg.requestDisallowInterceptTouchEvent(true);
         MotionEvent me = MotionEvent.obtain(SystemClock.uptimeMillis(),
                 SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
                 screenWidth / 2, screenHeight / 2, 0);
 
         assertFalse(vg.dispatchTouchEvent(me));
         assertTrue(mMotionEvent == null);
 
         textView.setFrame(0, 0, screenWidth, screenHeight);
         assertTrue(vg.dispatchTouchEvent(me));
         assertSame(me, mMotionEvent);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchTrackballEvent function",
         method = "dispatchTrackballEvent",
         args = {android.view.MotionEvent.class}
     )
     public void testDispatchTrackballEvent() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MotionEvent me = MotionEvent.obtain(SystemClock.uptimeMillis(),
                 SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 100, 100,
                 0);
         assertFalse(vg.dispatchTrackballEvent(me));
 
         MockTextView textView = new MockTextView(mContext);
         vg.addView(textView);
         textView.setFrame(1, 1, 100, 100);
         vg.requestChildFocus(textView, null);
         assertTrue(vg.dispatchTrackballEvent(me));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchUnhandledMove function",
         method = "dispatchUnhandledMove",
         args = {android.view.View.class, int.class}
     )
     public void testDispatchUnhandledMove() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         assertFalse(vg.dispatchUnhandledMove(textView, View.FOCUS_DOWN));
 
         vg.addView(textView);
         textView.setFrame(1, 1, 100, 100);
         vg.requestChildFocus(textView, null);
         assertTrue(vg.dispatchUnhandledMove(textView, View.FOCUS_DOWN));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchWindowFocusChanged function",
         method = "dispatchWindowFocusChanged",
         args = {boolean.class}
     )
     public void testDispatchWindowFocusChanged() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
 
         vg.addView(textView);
         textView.setPressed(true);
         assertTrue(textView.isPressed());
 
         vg.dispatchWindowFocusChanged(false);
         assertFalse(textView.isPressed());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test dispatchWindowVisibilityChanged function",
         method = "dispatchWindowVisibilityChanged",
         args = {int.class}
     )
     public void testDispatchWindowVisibilityChanged() {
         int expected = 10;
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
 
         vg.addView(textView);
         vg.dispatchWindowVisibilityChanged(expected);
         assertEquals(expected, textView.mVisibility);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test drawableStateChanged function",
         method = "drawableStateChanged",
         args = {}
     )
     public void testDrawableStateChanged() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         textView.setDuplicateParentStateEnabled(true);
 
         vg.addView(textView);
         vg.setAddStatesFromChildren(false);
         vg.drawableStateChanged();
         assertTrue(textView.mIsRefreshDrawableStateCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test drawChild function",
         method = "drawChild",
         args = {android.graphics.Canvas.class, android.view.View.class, long.class}
     )
     public void testDrawChild() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         vg.addView(textView);
 
         MockCanvas canvas = new MockCanvas();
         textView.setBackgroundDrawable(new BitmapDrawable(Bitmap.createBitmap(100, 100,
                 Config.ALPHA_8)));
         try {
             assertFalse(vg.drawChild(canvas, textView, 100));
             // test whether child's draw method is called.
             assertTrue(textView.mIsDrawCalled);
         } catch (Exception e) {
             fail(e.getMessage());
         }
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test findFocus function",
         method = "findFocus",
         args = {}
     )
     public void testFindFocus() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertNull(vg.findFocus());
         vg.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         vg.setFocusable(true);
         vg.setVisibility(View.VISIBLE);
         vg.setFocusableInTouchMode(true);
         assertTrue(vg.requestFocus(1, new Rect()));
 
         assertSame(vg, vg.findFocus());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test fitSystemWindows function",
         method = "fitSystemWindows",
         args = {android.graphics.Rect.class}
     )
     public void testFitSystemWindows() {
         Rect rect = new Rect(1, 1, 100, 100);
         MockViewGroup vg = new MockViewGroup(mContext);
         assertFalse(vg.fitSystemWindows(rect));
 
         vg = new MockViewGroup(mContext, null, 0);
         MockView mv = new MockView(mContext);
         vg.addView(mv);
         assertTrue(vg.fitSystemWindows(rect));
     }
 
     static class MockView extends ViewGroup {
 
         public int mWidthMeasureSpec;
         public int mHeightMeasureSpec;
 
         public MockView(Context context) {
             super(context);
         }
 
         @Override
         public void onLayout(boolean changed, int l, int t, int r, int b) {
         }
 
         @Override
         public boolean fitSystemWindows(Rect insets) {
             super.fitSystemWindows(insets);
             return true;
         }
 
         @Override
         public void onMeasure(int widthMeasureSpec,
                 int heightMeasureSpec) {
             mWidthMeasureSpec = widthMeasureSpec;
             mHeightMeasureSpec = heightMeasureSpec;
             super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         }
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test focusableViewAvailable function",
         method = "focusableViewAvailable",
         args = {android.view.View.class}
     )
     public void testFocusableViewAvailable() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockView son = new MockView(mContext);
         vg.addView(son);
 
         son.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
         son.focusableViewAvailable(vg);
 
         assertTrue(vg.mIsFocusableViewAvailable);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test focusSearch function",
         method = "focusSearch",
         args = {android.view.View.class, int.class}
     )
     public void testFocusSearch() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         MockView son = new MockView(mContext);
         vg.addView(son);
         son.addView(textView);
         assertNotNull(son.focusSearch(textView, 1));
         assertSame(textView, son.focusSearch(textView, 1));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test gatherTransparentRegion function",
         method = "gatherTransparentRegion",
         args = {android.graphics.Region.class}
     )
     @ToBeFixed(bug = "1391284", explanation = "Currently this function always return true")
     public void testGatherTransparentRegion() {
         Region region = new Region();
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         textView.setAnimation(new AlphaAnimation(mContext, null));
         textView.setVisibility(100);
         vg.addView(textView);
         assertEquals(1, vg.getChildCount());
 
         assertTrue(vg.gatherTransparentRegion(region));
         assertTrue(vg.gatherTransparentRegion(null));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test generateDefaultLayoutParams function",
         method = "generateDefaultLayoutParams",
         args = {}
     )
     public void testGenerateDefaultLayoutParams(){
         MockViewGroup vg = new MockViewGroup(mContext);
         LayoutParams lp = vg.generateDefaultLayoutParams();
 
         assertEquals(LayoutParams.WRAP_CONTENT, lp.width);
         assertEquals(LayoutParams.WRAP_CONTENT, lp.height);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test generateLayoutParams function",
         method = "generateLayoutParams",
         args = {android.util.AttributeSet.class}
     )
     public void testGenerateLayoutParamsWithParaAttributeSet() {
         MockViewGroup vg = new MockViewGroup(mContext);
         XmlResourceParser set = mContext.getResources().getLayout(
                 com.android.cts.stub.R.layout.abslistview_layout);
         try {
             XmlUtils.beginDocument(set, "ViewGroup_Layout");
         } catch (Exception e) {
         }
         LayoutParams lp = vg.generateLayoutParams(set);
         assertNotNull(lp);
         assertEquals(25, lp.height);
         assertEquals(25, lp.width);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test generateLayoutParams function",
         method = "generateLayoutParams",
         args = {android.view.ViewGroup.LayoutParams.class}
     )
     public void testGenerateLayoutParams() {
         MockViewGroup vg = new MockViewGroup(mContext);
         LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.FILL_PARENT);
         assertSame(p, vg.generateLayoutParams(p));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getChildDrawingOrder function",
         method = "getChildDrawingOrder",
         args = {int.class, int.class}
     )
     public void testGetChildDrawingOrder() {
         MockViewGroup vg = new MockViewGroup(mContext);
         assertEquals(1, vg.getChildDrawingOrder(0, 1));
         assertEquals(2, vg.getChildDrawingOrder(0, 2));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getChildMeasureSpec function",
         method = "getChildMeasureSpec",
         args = {int.class, int.class, int.class}
     )
     public void testGetChildMeasureSpec() {
         int spec = 1;
         int padding = 1;
         int childDimension = 1;
         assertEquals(MeasureSpec.makeMeasureSpec(childDimension, MeasureSpec.EXACTLY),
                 ViewGroup.getChildMeasureSpec(spec, padding, childDimension));
         spec = 4;
         padding = 6;
         childDimension = 9;
         assertEquals(MeasureSpec.makeMeasureSpec(childDimension, MeasureSpec.EXACTLY),
                 ViewGroup.getChildMeasureSpec(spec, padding, childDimension));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getChildStaticTransformation function",
         method = "getChildStaticTransformation",
         args = {android.view.View.class, android.view.animation.Transformation.class}
     )
     public void testGetChildStaticTransformation() {
         MockViewGroup vg = new MockViewGroup(mContext);
         assertFalse(vg.getChildStaticTransformation(null, null));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getChildVisibleRect function",
         method = "getChildVisibleRect",
         args = {android.view.View.class, android.graphics.Rect.class, android.graphics.Point.class}
     )
     public void testGetChildVisibleRect() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
 
         textView.setFrame(1, 1, 100, 100);
         Rect rect = new Rect(1, 1, 50, 50);
         Point p = new Point();
         assertFalse(vg.getChildVisibleRect(textView, rect, p));
 
         textView.setFrame(0, 0, 0, 0);
         vg.setFrame(20, 20, 60, 60);
         rect = new Rect(10, 10, 40, 40);
         p = new Point();
         assertTrue(vg.getChildVisibleRect(textView, rect, p));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getDescendantFocusability function",
         method = "getDescendantFocusability",
         args = {}
     )
     public void testGetDescendantFocusability() {
         MockViewGroup vg = new MockViewGroup(mContext);
         final int FLAG_MASK_FOCUSABILITY = 0x60000;
         assertFalse((vg.getDescendantFocusability() & FLAG_MASK_FOCUSABILITY) == 0);
 
         vg.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         assertFalse((vg.getDescendantFocusability() & FLAG_MASK_FOCUSABILITY) == 0);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getLayoutAnimation function",
         method = "getLayoutAnimation",
         args = {}
     )
     public void testGetLayoutAnimation() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertNull(vg.getLayoutAnimation());
         RotateAnimation animation = new RotateAnimation(0.1f, 0.1f);
         LayoutAnimationController la = new LayoutAnimationController(animation);
         vg.setLayoutAnimation(la);
         assertTrue(vg.canAnimate());
         assertSame(la, vg.getLayoutAnimation());
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test getLayoutAnimationListener and setLayoutAnimationListener",
             method = "getLayoutAnimationListener",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test getLayoutAnimationListener and setLayoutAnimationListener",
             method = "setLayoutAnimationListener",
             args = {android.view.animation.Animation.AnimationListener.class}
         )
     })
     public void testGetLayoutAnimationListener() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertNull(vg.getLayoutAnimationListener());
 
         AnimationListener al = new AnimationListener() {
 
             public void onAnimationEnd(Animation animation) {
             }
 
             public void onAnimationRepeat(Animation animation) {
             }
 
             public void onAnimationStart(Animation animation) {
             }
         };
         vg.setLayoutAnimationListener(al);
         assertSame(al, vg.getLayoutAnimationListener());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getPersistentDrawingCache function",
         method = "getPersistentDrawingCache",
         args = {}
     )
     public void testGetPersistentDrawingCache() {
         MockViewGroup vg = new MockViewGroup(mContext);
         final int mPersistentDrawingCache1 = 2;
         final int mPersistentDrawingCache2 = 3;
         assertEquals(mPersistentDrawingCache1, vg.getPersistentDrawingCache());
 
         vg.setPersistentDrawingCache(mPersistentDrawingCache2);
         assertEquals(mPersistentDrawingCache2, vg.getPersistentDrawingCache());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test hasFocus function",
         method = "hasFocus",
         args = {}
     )
     public void testHasFocus() {
         MockViewGroup vg = new MockViewGroup(mContext);
         assertFalse(vg.hasFocus());
 
         TextView textView = new TextView(mContext);
 
         vg.addView(textView);
         vg.requestChildFocus(textView, null);
 
         assertTrue(vg.hasFocus());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test hasFocusable function",
         method = "hasFocusable",
         args = {}
     )
     public void testHasFocusable() {
         MockViewGroup vg = new MockViewGroup(mContext);
         assertFalse(vg.hasFocusable());
 
         vg.setVisibility(View.VISIBLE);
         vg.setFocusable(true);
         assertTrue(vg.hasFocusable());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test indexOfChild function",
         method = "indexOfChild",
         args = {android.view.View.class}
     )
     public void testIndexOfChild() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         assertEquals(-1, vg.indexOfChild(textView));
 
         vg.addView(textView);
         assertEquals(0, vg.indexOfChild(textView));
     }
 
     private void setupActivity(String action) {
 
         Intent intent = new Intent(getInstrumentation().getTargetContext(),
                 ViewGroupStubActivity.class);
         intent.setAction(action);
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         getInstrumentation().getTargetContext().startActivity(intent);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test invalidateChild function",
             method = "invalidateChild",
             args = {android.view.View.class, android.graphics.Rect.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test invalidateChild function",
             method = "invalidateChildInParent",
             args = {int[].class, android.graphics.Rect.class}
         )
     })
     public void testInvalidateChild() {
         ViewGroupStubActivity.setResult(this);
         setupActivity(ViewGroupStubActivity.ACTION_INVALIDATE_CHILD);
         waitForResult();
 
     }
 
     private void waitForResult() {
         synchronized (mSync) {
            while (!mHasNotify) {
                 try {
                     mSync.wait();
                 } catch (InterruptedException e) {
                 }
             }
         }
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test isAlwaysDrawnWithCacheEnabled function",
             method = "isAlwaysDrawnWithCacheEnabled",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test isAlwaysDrawnWithCacheEnabled function",
             method = "setAlwaysDrawnWithCacheEnabled",
             args = {boolean.class}
         )
     })
     public void testIsAlwaysDrawnWithCacheEnabled() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertTrue(vg.isAlwaysDrawnWithCacheEnabled());
 
         vg.setAlwaysDrawnWithCacheEnabled(false);
         assertFalse(vg.isAlwaysDrawnWithCacheEnabled());
         vg.setAlwaysDrawnWithCacheEnabled(true);
         assertTrue(vg.isAlwaysDrawnWithCacheEnabled());
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test isAnimationCacheEnabled function",
             method = "isAnimationCacheEnabled",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test isAnimationCacheEnabled function",
             method = "setAnimationCacheEnabled",
             args = {boolean.class}
         )
     })
     public void testIsAnimationCacheEnabled() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertTrue(vg.isAnimationCacheEnabled());
 
         vg.setAnimationCacheEnabled(false);
         assertFalse(vg.isAnimationCacheEnabled());
         vg.setAnimationCacheEnabled(true);
         assertTrue(vg.isAnimationCacheEnabled());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test isChildrenDrawnWithCacheEnabled function",
         method = "isChildrenDrawnWithCacheEnabled",
         args = {}
     )
     public void testIsChildrenDrawnWithCacheEnabled() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertFalse(vg.isChildrenDrawnWithCacheEnabled());
 
         vg.setChildrenDrawnWithCacheEnabled(true);
         assertTrue(vg.isChildrenDrawnWithCacheEnabled());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test measureChild function",
         method = "measureChild",
         args = {android.view.View.class, int.class, int.class}
     )
     public void testMeasureChild() {
         final int width = 100;
         final int height = 200;
         MockViewGroup vg = new MockViewGroup(mContext);
         MockView son = new MockView(mContext);
         son.setLayoutParams(new LayoutParams(width, height));
         son.forceLayout();
         vg.addView(son);
 
         final int parentWidthMeasureSpec = 1;
         final int parentHeightMeasureSpec = 2;
         vg.measureChild(son, parentWidthMeasureSpec, parentHeightMeasureSpec);
         assertEquals(ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, 0, width),
                 son.mWidthMeasureSpec);
         assertEquals(ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec, 0, height),
                 son.mHeightMeasureSpec);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test measureChildren function",
         method = "measureChildren",
         args = {int.class, int.class}
     )
     public void testMeasureChildren() {
         final int widthMeasureSpec = 100;
         final int heightMeasureSpec = 200;
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView1 = new MockTextView(mContext);
 
         vg.addView(textView1);
         vg.mMeasureChildCalledTime = 0;
         vg.measureChildren(widthMeasureSpec, heightMeasureSpec);
         assertEquals(1, vg.mMeasureChildCalledTime);
 
         MockTextView textView2 = new MockTextView(mContext);
         textView2.setVisibility(View.GONE);
         vg.addView(textView2);
 
         vg.mMeasureChildCalledTime = 0;
         vg.measureChildren(widthMeasureSpec, heightMeasureSpec);
         assertEquals(1, vg.mMeasureChildCalledTime);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test measureChildWithMargins function",
         method = "measureChildWithMargins",
         args = {android.view.View.class, int.class, int.class, int.class, int.class}
     )
     public void testMeasureChildWithMargins() {
         final int width = 10;
         final int height = 20;
         final int parentWidthMeasureSpec = 1;
         final int widthUsed = 2;
         final int parentHeightMeasureSpec = 3;
         final int heightUsed = 4;
         MockViewGroup vg = new MockViewGroup(mContext);
         MockView son = new MockView(mContext);
 
         vg.addView(son);
         son.setLayoutParams(new ViewGroup.LayoutParams(width, height));
         try {
             vg.measureChildWithMargins(son, parentWidthMeasureSpec, widthUsed,
                     parentHeightMeasureSpec, heightUsed);
             fail("measureChildWithMargins should throw out class cast exception");
         } catch (RuntimeException e) {
         }
         son.setLayoutParams(new ViewGroup.MarginLayoutParams(width, height));
 
         vg.measureChildWithMargins(son, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec,
                 heightUsed);
         assertEquals(ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, parentHeightMeasureSpec,
                 width), son.mWidthMeasureSpec);
         assertEquals(ViewGroup.getChildMeasureSpec(widthUsed, heightUsed, height),
                 son.mHeightMeasureSpec);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test offsetDescendantRectToMyCoords function",
         method = "offsetDescendantRectToMyCoords",
         args = {android.view.View.class, android.graphics.Rect.class}
     )
     public void testOffsetDescendantRectToMyCoords() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
 
         try {
             vg.offsetDescendantRectToMyCoords(textView, new Rect());
             fail("offsetDescendantRectToMyCoords should throw out "
                     + "IllegalArgumentException");
         } catch (RuntimeException e) {
         }
         vg.addView(textView);
         textView.setFrame(1, 2, 3, 4);
         Rect rect = new Rect();
         vg.offsetDescendantRectToMyCoords(textView, rect);
         assertEquals(2, rect.bottom);
         assertEquals(2, rect.top);
         assertEquals(1, rect.left);
         assertEquals(1, rect.right);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test offsetRectIntoDescendantCoords function",
         method = "offsetRectIntoDescendantCoords",
         args = {android.view.View.class, android.graphics.Rect.class}
     )
     public void testOffsetRectIntoDescendantCoords() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setFrame(10, 20, 30, 40);
         MockTextView textView = new MockTextView(mContext);
 
         try {
             vg.offsetRectIntoDescendantCoords(textView, new Rect());
             fail("offsetRectIntoDescendantCoords should throw out "
                     + "IllegalArgumentException");
         } catch (RuntimeException e) {
         }
         textView.setFrame(1, 2, 3, 4);
         vg.addView(textView);
 
         Rect rect = new Rect(5, 6, 7, 8);
         vg.offsetRectIntoDescendantCoords(textView, rect);
         assertEquals(6, rect.bottom);
         assertEquals(4, rect.top);
         assertEquals(4, rect.left);
         assertEquals(6, rect.right);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test onAnimationStart function",
         method = "onAnimationEnd",
         args = {}
     )
     public void testOnAnimationEnd() {
         // this function is a call back function it should be tested in ViewGroup#drawChild.
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
         son.setAnimation(new MockAnimation());
         // this call will make mPrivateFlags |= ANIMATION_STARTED;
         son.onAnimationStart();
         father.addView(son);
 
         MockCanvas canvas = new MockCanvas();
         try {
             assertFalse(father.drawChild(canvas, son, 100));
             assertTrue(son.mIsOnAnimationEndCall);
         } catch (Exception e) {
         }
     }
 
     private class MockAnimation extends Animation {
         public MockAnimation() {
             super();
         }
 
         public MockAnimation(Context context, AttributeSet attrs) {
             super(context, attrs);
         }
 
         @Override
         public boolean getTransformation(long currentTime, Transformation outTransformation) {
            super.getTransformation(currentTime, outTransformation);
            return false;
         }
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test onAnimationStart function",
         method = "onAnimationStart",
         args = {}
     )
     public void testOnAnimationStart() {
         // this function is a call back function it should be tested in ViewGroup#drawChild.
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
 
         father.addView(son);
 
         MockCanvas canvas = new MockCanvas();
         try {
             assertFalse(father.drawChild(canvas, son, 100));
             assertFalse(son.mIsOnAnimationStartCall);
         } catch (Exception e) {
         }
 
         son.setAnimation(new MockAnimation());
         try {
             assertFalse(father.drawChild(canvas, son, 100));
             assertTrue(son.mIsOnAnimationStartCall);
         } catch (Exception e) {
         }
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test onCreateDrawableState function",
         method = "onCreateDrawableState",
         args = {int.class}
     )
     public void testOnCreateDrawableState() {
         MockViewGroup vg = new MockViewGroup(mContext);
         // Call back function. Called in View#getDrawableState()
         int[] data = vg.getDrawableState();
         assertTrue(vg.mIsOnCreateDrawableStateCalled);
         assertEquals(1, data.length);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test onInterceptTouchEvent function",
         method = "onInterceptTouchEvent",
         args = {android.view.MotionEvent.class}
     )
     public void testOnInterceptTouchEvent() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MotionEvent me = MotionEvent.obtain(SystemClock.uptimeMillis(),
                 SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 100, 100,
                 0);
 
         assertFalse(vg.dispatchTouchEvent(me));
         assertTrue(vg.mIsOnInterceptTouchEventCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test onLayout function",
         method = "onLayout",
         args = {boolean.class, int.class, int.class, int.class, int.class}
     )
     public void testOnLayout() {
         final int left = 1;
         final int top = 2;
         final int right = 100;
         final int bottom = 200;
         MockViewGroup mv = new MockViewGroup(mContext);
         mv.layout(left, top, right, bottom);
         assertEquals(left, mv.mLeft);
         assertEquals(top, mv.mTop);
         assertEquals(right, mv.mRight);
         assertEquals(bottom, mv.mBottom);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test onRequestFocusInDescendants function",
         method = "onRequestFocusInDescendants",
         args = {int.class, android.graphics.Rect.class}
     )
     public void testOnRequestFocusInDescendants() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         vg.requestFocus(View.FOCUS_DOWN, new Rect());
         assertTrue(vg.mIsOnRequestFocusInDescendantsCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test recomputeViewAttributes function",
         method = "recomputeViewAttributes",
         args = {android.view.View.class}
     )
     public void testRecomputeViewAttributes() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
         father.addView(son);
 
         son.recomputeViewAttributes(null);
         assertTrue(father.mIsRecomputeViewAttributesCalled);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test removeAllViews function",
             method = "removeAllViews",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test removeAllViews function",
             method = "getChildCount",
             args = {}
         )
     })
     public void testRemoveAllViews() {
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         assertEquals(0, vg.getChildCount());
 
         vg.addView(textView);
         assertEquals(1, vg.getChildCount());
 
         vg.removeAllViews();
         assertEquals(0, vg.getChildCount());
         assertNull(textView.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test removeAllViewsInLayout function",
         method = "removeAllViewsInLayout",
         args = {}
     )
     public void testRemoveAllViewsInLayout() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
 
         assertEquals(0, father.getChildCount());
 
         son.addView(textView);
         father.addView(son);
         assertEquals(1, father.getChildCount());
 
         father.removeAllViewsInLayout();
         assertEquals(0, father.getChildCount());
         assertEquals(1, son.getChildCount());
         assertNull(son.getParent());
         assertSame(son, textView.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test removeDetachedView function",
         method = "removeDetachedView",
         args = {android.view.View.class, boolean.class}
     )
     public void testRemoveDetachedView() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son1 = new MockViewGroup(mContext);
         MockViewGroup son2 = new MockViewGroup(mContext);
         MockOnHierarchyChangeListener listener = new MockOnHierarchyChangeListener();
         father.setOnHierarchyChangeListener(listener);
         father.addView(son1);
         father.addView(son2);
 
         father.removeDetachedView(son1, false);
         assertSame(father, listener.sParent);
         assertSame(son1, listener.sChild);
     }
 
     static class MockOnHierarchyChangeListener implements OnHierarchyChangeListener {
 
         public View sParent;
         public View sChild;
 
         public void onChildViewAdded(View parent, View child) {
         }
 
         public void onChildViewRemoved(View parent, View child) {
             sParent = parent;
             sChild = child;
         }
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test removeView function",
         method = "removeView",
         args = {android.view.View.class}
     )
     public void testRemoveView() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
 
         assertEquals(0, father.getChildCount());
 
         father.addView(son);
         assertEquals(1, father.getChildCount());
 
         father.removeView(son);
         assertEquals(0, father.getChildCount());
         assertNull(son.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test removeViewAt function",
         method = "removeViewAt",
         args = {int.class}
     )
     public void testRemoveViewAt() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
 
         assertEquals(0, father.getChildCount());
 
         father.addView(son);
         assertEquals(1, father.getChildCount());
 
         try {
             father.removeViewAt(2);
             fail("should throw out null pointer exception");
         } catch (RuntimeException e) {
         }
         assertEquals(1, father.getChildCount());
 
         father.removeViewAt(0);
         assertEquals(0, father.getChildCount());
         assertNull(son.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test removeViewInLayout function",
         method = "removeViewInLayout",
         args = {android.view.View.class}
     )
     public void testRemoveViewInLayout() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
 
         assertEquals(0, father.getChildCount());
 
         father.addView(son);
         assertEquals(1, father.getChildCount());
 
         father.removeViewInLayout(son);
         assertEquals(0, father.getChildCount());
         assertNull(son.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test removeViews function",
         method = "removeViews",
         args = {int.class, int.class}
     )
     public void testRemoveViews() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son1 = new MockViewGroup(mContext);
         MockViewGroup son2 = new MockViewGroup(mContext);
 
         assertEquals(0, father.getChildCount());
 
         father.addView(son1);
         father.addView(son2);
         assertEquals(2, father.getChildCount());
 
         father.removeViews(0, 1);
         assertEquals(1, father.getChildCount());
         assertNull(son1.getParent());
 
         father.removeViews(0, 1);
         assertEquals(0, father.getChildCount());
         assertNull(son2.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test removeViewsInLayout function",
         method = "removeViewsInLayout",
         args = {int.class, int.class}
     )
     public void testRemoveViewsInLayout() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son1 = new MockViewGroup(mContext);
         MockViewGroup son2 = new MockViewGroup(mContext);
 
         assertEquals(0, father.getChildCount());
 
         father.addView(son1);
         father.addView(son2);
         assertEquals(2, father.getChildCount());
 
         father.removeViewsInLayout(0, 1);
         assertEquals(1, father.getChildCount());
         assertNull(son1.getParent());
 
         father.removeViewsInLayout(0, 1);
         assertEquals(0, father.getChildCount());
         assertNull(son2.getParent());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test requestChildFocus function",
         method = "requestChildFocus",
         args = {android.view.View.class, android.view.View.class}
     )
     public void testRequestChildFocus() {
         MockViewGroup vg = new MockViewGroup(mContext);
         TextView textView = new TextView(mContext);
 
         vg.addView(textView);
         vg.requestChildFocus(textView, null);
 
         assertNotNull(vg.getFocusedChild());
 
         vg.clearChildFocus(textView);
         assertNull(vg.getFocusedChild());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test requestChildRectangleOnScreen function",
         method = "requestChildRectangleOnScreen",
         args = {android.view.View.class, android.graphics.Rect.class, boolean.class}
     )
     public void testRequestChildRectangleOnScreen() {
         MockViewGroup vg = new MockViewGroup(mContext);
         assertFalse(vg.requestChildRectangleOnScreen(null, null, false));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test requestDisallowInterceptTouchEvent function",
         method = "requestDisallowInterceptTouchEvent",
         args = {boolean.class}
     )
     public void testRequestDisallowInterceptTouchEvent() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockView son = new MockView(mContext);
 
         father.addView(son);
         son.requestDisallowInterceptTouchEvent(true);
         son.requestDisallowInterceptTouchEvent(false);
         assertTrue(father.mIsRequestDisallowInterceptTouchEventCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test requestFocus function",
         method = "requestFocus",
         args = {int.class, android.graphics.Rect.class}
     )
     public void testRequestFocus() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         vg.requestFocus(View.FOCUS_DOWN, new Rect());
         assertTrue(vg.mIsOnRequestFocusInDescendantsCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test requestTransparentRegion function",
         method = "requestTransparentRegion",
         args = {android.view.View.class}
     )
     public void testRequestTransparentRegion() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockView son1 = new MockView(mContext);
         MockView son2 = new MockView(mContext);
         son1.addView(son2);
         father.addView(son1);
         son1.requestTransparentRegion(son2);
         assertTrue(father.mIsRequestTransparentRegionCalled);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test scheduleLayoutAnimation function",
         method = "scheduleLayoutAnimation",
         args = {}
     )
     public void testScheduleLayoutAnimation() {
         MockViewGroup vg = new MockViewGroup(mContext);
         Animation animation = new AlphaAnimation(mContext, null);
 
         MockLayoutAnimationController al = new MockLayoutAnimationController(animation);
         vg.setLayoutAnimation(al);
         vg.scheduleLayoutAnimation();
         vg.dispatchDraw(new Canvas());
         assertTrue(al.mIsStartCalled);
     }
 
     static class MockLayoutAnimationController extends LayoutAnimationController {
 
         public boolean mIsStartCalled;
 
         public MockLayoutAnimationController(Animation animation) {
             super(animation);
         }
 
         @Override
         public void start() {
             mIsStartCalled = true;
             super.start();
         }
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setAddStatesFromChildren function",
         method = "setAddStatesFromChildren",
         args = {boolean.class}
     )
     public void testSetAddStatesFromChildren() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setAddStatesFromChildren(true);
         assertTrue(vg.addStatesFromChildren());
 
         vg.setAddStatesFromChildren(false);
         assertFalse(vg.addStatesFromChildren());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setChildrenDrawingCacheEnabled function",
         method = "setChildrenDrawingCacheEnabled",
         args = {boolean.class}
     )
     public void testSetChildrenDrawingCacheEnabled() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertTrue(vg.isAnimationCacheEnabled());
 
         vg.setAnimationCacheEnabled(false);
         assertFalse(vg.isAnimationCacheEnabled());
 
         vg.setAnimationCacheEnabled(true);
         assertTrue(vg.isAnimationCacheEnabled());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setChildrenDrawnWithCacheEnabled function",
         method = "setChildrenDrawnWithCacheEnabled",
         args = {boolean.class}
     )
     public void testSetChildrenDrawnWithCacheEnabled() {
         MockViewGroup vg = new MockViewGroup(mContext);
 
         assertFalse(vg.isChildrenDrawnWithCacheEnabled());
 
         vg.setChildrenDrawnWithCacheEnabled(true);
         assertTrue(vg.isChildrenDrawnWithCacheEnabled());
 
         vg.setChildrenDrawnWithCacheEnabled(false);
         assertFalse(vg.isChildrenDrawnWithCacheEnabled());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setClipChildren function",
         method = "setClipChildren",
         args = {boolean.class}
     )
     public void testSetClipChildren() {
         Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
 
         MockViewGroup vg = new MockViewGroup(mContext);
         MockTextView textView = new MockTextView(mContext);
         textView.setFrame(1, 2, 30, 40);
         vg.setFrame(1, 1, 100, 200);
         vg.setClipChildren(true);
 
         MockCanvas canvas = new MockCanvas(bitmap);
         vg.drawChild(canvas, textView, 100);
         Rect rect = canvas.getClipBounds();
         assertEquals(0, rect.top);
         assertEquals(100, rect.bottom);
         assertEquals(0, rect.left);
         assertEquals(100, rect.right);
     }
 
     class MockCanvas extends Canvas {
 
         public boolean mIsSaveCalled;
         public int mLeft;
         public int mTop;
         public int mRight;
         public int mBottom;
 
         public MockCanvas() {
         }
 
         public MockCanvas(Bitmap bitmap) {
             super(bitmap);
         }
 
         @Override
         public boolean quickReject(float left, float top, float right,
                 float bottom, EdgeType type) {
             super.quickReject(left, top, right, bottom, type);
             return false;
         }
 
         @Override
         public int save() {
             mIsSaveCalled = true;
             return super.save();
         }
 
         @Override
         public boolean clipRect(int left, int top, int right, int bottom) {
             mLeft = left;
             mTop = top;
             mRight = right;
             mBottom = bottom;
             return super.clipRect(left, top, right, bottom);
         }
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setClipToPadding function",
         method = "setClipToPadding",
         args = {boolean.class}
     )
     public void testSetClipToPadding() {
         final int frameLeft = 1;
         final int frameTop = 2;
         final int frameRight = 100;
         final int frameBottom = 200;
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setFrame(frameLeft, frameTop, frameRight, frameBottom);
 
         vg.setClipToPadding(true);
         MockCanvas canvas = new MockCanvas();
         final int paddingLeft = 10;
         final int paddingTop = 20;
         final int paddingRight = 100;
         final int paddingBottom = 200;
         vg.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
         vg.dispatchDraw(canvas);
         //check that the clip region does not contain the padding area
         assertTrue(canvas.mIsSaveCalled);
         assertEquals(10, canvas.mLeft);
         assertEquals(20, canvas.mTop);
         assertEquals(-frameLeft, canvas.mRight);
         assertEquals(-frameTop, canvas.mBottom);
 
         vg.setClipToPadding(false);
         canvas = new MockCanvas();
         vg.dispatchDraw(canvas);
         assertFalse(canvas.mIsSaveCalled);
         assertEquals(0, canvas.mLeft);
         assertEquals(0, canvas.mTop);
         assertEquals(0, canvas.mRight);
         assertEquals(0, canvas.mBottom);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setDescendantFocusability function",
         method = "setDescendantFocusability",
         args = {int.class}
     )
     public void testSetDescendantFocusability() {
         MockViewGroup vg = new MockViewGroup(mContext);
         final int FLAG_MASK_FOCUSABILITY = 0x60000;
         assertFalse((vg.getDescendantFocusability() & FLAG_MASK_FOCUSABILITY) == 0);
 
         vg.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         assertFalse((vg.getDescendantFocusability() & FLAG_MASK_FOCUSABILITY) == 0);
 
         vg.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
         assertFalse((vg.getDescendantFocusability() & FLAG_MASK_FOCUSABILITY) == 0);
         assertFalse((vg.getDescendantFocusability() &
                 ViewGroup.FOCUS_BEFORE_DESCENDANTS) == 0);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setOnHierarchyChangeListener function",
         method = "setOnHierarchyChangeListener",
         args = {android.view.ViewGroup.OnHierarchyChangeListener.class}
     )
     public void testSetOnHierarchyChangeListener() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
         MockOnHierarchyChangeListener listener = new MockOnHierarchyChangeListener();
         father.setOnHierarchyChangeListener(listener);
         father.addView(son);
 
         father.removeDetachedView(son, false);
         assertSame(father, listener.sParent);
         assertSame(son, listener.sChild);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setPadding function",
         method = "setPadding",
         args = {int.class, int.class, int.class, int.class}
     )
     public void testSetPadding() {
         final int left = 1;
         final int top = 2;
         final int right = 3;
         final int bottom = 4;
         MockViewGroup vg = new MockViewGroup(mContext);
         assertEquals(0, vg.getPaddingBottom());
         assertEquals(0, vg.getPaddingTop());
         assertEquals(0, vg.getPaddingLeft());
         assertEquals(0, vg.getPaddingRight());
 
         vg.setPadding(left, top, right, bottom);
         assertEquals(bottom, vg.getPaddingBottom());
         assertEquals(top, vg.getPaddingTop());
         assertEquals(left, vg.getPaddingLeft());
         assertEquals(right, vg.getPaddingRight());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test setPersistentDrawingCache function",
         method = "setPersistentDrawingCache",
         args = {int.class}
     )
     public void testSetPersistentDrawingCache() {
         MockViewGroup vg = new MockViewGroup(mContext);
         vg.setPersistentDrawingCache(1);
         assertEquals(1 & ViewGroup.PERSISTENT_ALL_CACHES, vg
                 .getPersistentDrawingCache());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test showContextMenuForChild function",
         method = "showContextMenuForChild",
         args = {android.view.View.class}
     )
     public void testShowContextMenuForChild() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
         father.addView(son);
 
         son.showContextMenuForChild(null);
         assertTrue(father.mIsShowContextMenuForChildCalled);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test startLayoutAnimation and setLayoutAnimation",
             method = "startLayoutAnimation",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test startLayoutAnimation and setLayoutAnimation",
             method = "setLayoutAnimation",
             args = {android.view.animation.LayoutAnimationController.class}
         )
     })
     public void testStartLayoutAnimation() {
         MockViewGroup vg = new MockViewGroup(mContext);
         RotateAnimation animation = new RotateAnimation(0.1f, 0.1f);
         LayoutAnimationController la = new LayoutAnimationController(animation);
         vg.setLayoutAnimation(la);
 
         vg.layout(1, 1, 100, 100);
         assertFalse(vg.isLayoutRequested());
         vg.startLayoutAnimation();
         assertTrue(vg.isLayoutRequested());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test updateViewLayout function",
         method = "updateViewLayout",
         args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class}
     )
     public void testUpdateViewLayout() {
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
 
         father.addView(son);
         LayoutParams param = new LayoutParams(100, 200);
         father.updateViewLayout(son, param);
         assertEquals(param.width, son.getLayoutParams().width);
         assertEquals(param.height, son.getLayoutParams().height);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test debug function",
         method = "debug",
         args = {int.class}
     )
     public void testDebug() {
         final int EXPECTED = 100;
         MockViewGroup father = new MockViewGroup(mContext);
         MockViewGroup son = new MockViewGroup(mContext);
         father.addView(son);
 
         father.debug(EXPECTED);
         assertEquals(EXPECTED + 1, son.mDebugDepth);
     }
 
     static class MockTextView extends TextView {
 
         public boolean mIsClearFocusCalled;
         public boolean mIsDispatchRestoreInstanceStateCalled;
         public int mVisibility;
         public boolean mIsRefreshDrawableStateCalled;
         public boolean mIsDrawCalled;
 
         public MockTextView(Context context) {
             super(context);
         }
 
         @Override
         public void draw(Canvas canvas) {
             super.draw(canvas);
             mIsDrawCalled = true;
         }
 
         @Override
         public void clearFocus() {
             mIsClearFocusCalled = true;
             super.clearFocus();
         }
 
         @Override
         public boolean dispatchKeyEvent(KeyEvent event) {
             return true;
         }
 
         @Override
         public boolean setFrame(int l, int t, int r, int b) {
             return super.setFrame(l, t, r, b);
         }
 
         @Override
         public void dispatchRestoreInstanceState(
                 SparseArray<Parcelable> container) {
             mIsDispatchRestoreInstanceStateCalled = true;
             super.dispatchRestoreInstanceState(container);
         }
 
         @Override
         public boolean onTrackballEvent(MotionEvent event) {
             return true;
         }
 
         @Override
         public boolean dispatchUnhandledMove(View focused, int direction) {
             return true;
         }
 
         @Override
         public void onWindowVisibilityChanged(int visibility) {
             mVisibility = visibility;
             super.onWindowVisibilityChanged(visibility);
         }
 
         @Override
         public void refreshDrawableState() {
             mIsRefreshDrawableStateCalled = true;
             super.refreshDrawableState();
         }
 
         @Override
         public boolean gatherTransparentRegion(Region region) {
             super.gatherTransparentRegion(region);
             return false;
         }
 
         @Override
         public boolean dispatchTouchEvent(MotionEvent event) {
             super.dispatchTouchEvent(event);
             return true;
         }
     }
 
     static class MockViewGroup extends ViewGroup {
 
         public boolean mIsRecomputeViewAttributesCalled;
         public boolean mIsShowContextMenuForChildCalled;
         public boolean mIsRefreshDrawableStateCalled;
         public boolean mIsOnRestoreInstanceStateCalled;
         public boolean mIsOnCreateDrawableStateCalled;
         public boolean mIsOnInterceptTouchEventCalled;
         public boolean mIsOnRequestFocusInDescendantsCalled;
         public boolean mIsFocusableViewAvailable;
         public boolean mIsDispatchDrawCalled;
         public boolean mIsRequestDisallowInterceptTouchEventCalled;
         public boolean mIsRequestTransparentRegionCalled;
         public int[] mLocation;
         public int mMeasureChildCalledTime;
         public boolean mIsOnAnimationEndCall;
         public boolean mIsOnAnimationStartCall;
         public int mDebugDepth;
         public int mDrawChildCalledTime;
         public Canvas mCanvas;
         public boolean mInvalidateChildInParentCalled;
         public boolean mIsDrawableStateChangedCalled;
         public boolean mIsRequestLayoutCalled;
         public boolean mIsOnLayoutCalled;
         public int mLeft;
         public int mTop;
         public int mRight;
         public int mBottom;
 
         public MockViewGroup(Context context, AttributeSet attrs, int defStyle) {
             super(context, attrs, defStyle);
         }
 
         public MockViewGroup(Context context, AttributeSet attrs) {
             super(context, attrs);
         }
 
         public MockViewGroup(Context context) {
             super(context);
         }
 
         @Override
         public void onLayout(boolean changed, int l, int t, int r, int b) {
             mIsOnLayoutCalled = true;
             mLeft = l;
             mTop = t;
             mRight = r;
             mBottom = b;
         }
 
         @Override
         public boolean addViewInLayout(View child, int index,
                 ViewGroup.LayoutParams params) {
             return super.addViewInLayout(child, index, params);
         }
 
         @Override
         public boolean addViewInLayout(View child, int index,
                 ViewGroup.LayoutParams params, boolean preventRequestLayout) {
             return super.addViewInLayout(child, index, params, preventRequestLayout);
         }
 
         @Override
         public void attachLayoutAnimationParameters(View child,
                 ViewGroup.LayoutParams params, int index, int count) {
             super.attachLayoutAnimationParameters(child, params, index, count);
         }
 
         @Override
         public void attachViewToParent(View child, int index,
                 LayoutParams params) {
             super.attachViewToParent(child, index, params);
         }
 
         @Override
         public boolean canAnimate() {
             return super.canAnimate();
         }
 
         @Override
         public boolean checkLayoutParams(LayoutParams p) {
             return super.checkLayoutParams(p);
         }
 
         @Override
         public void refreshDrawableState() {
             mIsRefreshDrawableStateCalled = true;
             super.refreshDrawableState();
         }
 
         @Override
         public void cleanupLayoutState(View child) {
             super.cleanupLayoutState(child);
         }
 
         @Override
         public void detachAllViewsFromParent() {
             super.detachAllViewsFromParent();
         }
 
         @Override
         public void detachViewFromParent(int index) {
             super.detachViewFromParent(index);
         }
 
         @Override
         public void detachViewFromParent(View child) {
             super.detachViewFromParent(child);
         }
         @Override
 
         public void detachViewsFromParent(int start, int count) {
             super.detachViewsFromParent(start, count);
         }
 
         @Override
         public void dispatchDraw(Canvas canvas) {
             mIsDispatchDrawCalled = true;
             super.dispatchDraw(canvas);
             mCanvas = canvas;
         }
 
         @Override
         public void dispatchFreezeSelfOnly(SparseArray<Parcelable> container) {
             super.dispatchFreezeSelfOnly(container);
         }
 
         @Override
         public void dispatchRestoreInstanceState(
                 SparseArray<Parcelable> container) {
             super.dispatchRestoreInstanceState(container);
         }
 
         @Override
         public void dispatchSaveInstanceState(
                 SparseArray<Parcelable> container) {
             super.dispatchSaveInstanceState(container);
         }
 
         @Override
         public void dispatchSetPressed(boolean pressed) {
             super.dispatchSetPressed(pressed);
         }
 
         @Override
         public void dispatchThawSelfOnly(SparseArray<Parcelable> container) {
             super.dispatchThawSelfOnly(container);
         }
 
         @Override
         public void onRestoreInstanceState(Parcelable state) {
             mIsOnRestoreInstanceStateCalled = true;
             super.onRestoreInstanceState(state);
         }
 
         @Override
         public void drawableStateChanged() {
             mIsDrawableStateChangedCalled = true;
             super.drawableStateChanged();
         }
 
         @Override
         public boolean drawChild(Canvas canvas, View child, long drawingTime) {
             mDrawChildCalledTime++;
             return super.drawChild(canvas, child, drawingTime);
         }
 
         @Override
         public boolean fitSystemWindows(Rect insets) {
             return super.fitSystemWindows(insets);
         }
 
         @Override
         public LayoutParams generateDefaultLayoutParams() {
             return super.generateDefaultLayoutParams();
         }
 
         @Override
         public LayoutParams generateLayoutParams(LayoutParams p) {
             return super.generateLayoutParams(p);
         }
 
         @Override
         public int getChildDrawingOrder(int childCount, int i) {
             return super.getChildDrawingOrder(childCount, i);
         }
 
         @Override
         public boolean getChildStaticTransformation(View child,
                 Transformation t) {
             return super.getChildStaticTransformation(child, t);
         }
 
         @Override
         public boolean setFrame(int left, int top, int right, int bottom) {
             return super.setFrame(left, top, right, bottom);
         }
 
         @Override
         public boolean isChildrenDrawnWithCacheEnabled() {
             return super.isChildrenDrawnWithCacheEnabled();
         }
 
         @Override
         public void setChildrenDrawnWithCacheEnabled(boolean enabled) {
             super.setChildrenDrawnWithCacheEnabled(enabled);
         }
 
         @Override
         public void measureChild(View child, int parentWidthMeasureSpec,
                 int parentHeightMeasureSpec) {
             mMeasureChildCalledTime++;
             super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
         }
 
         @Override
         public void measureChildren(int widthMeasureSpec,
                 int heightMeasureSpec) {
             super.measureChildren(widthMeasureSpec, heightMeasureSpec);
         }
 
         @Override
         public void measureChildWithMargins(View child,
                 int parentWidthMeasureSpec, int widthUsed,
                 int parentHeightMeasureSpec, int heightUsed) {
             super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed,
                     parentHeightMeasureSpec, heightUsed);
         }
 
         @Override
         public void onAnimationEnd() {
             mIsOnAnimationEndCall = true;
             super.onAnimationEnd();
         }
 
         @Override
         public void onAnimationStart() {
             super.onAnimationStart();
             mIsOnAnimationStartCall = true;
         }
 
         @Override
         public int[] onCreateDrawableState(int extraSpace) {
             mIsOnCreateDrawableStateCalled = true;
             return super.onCreateDrawableState(extraSpace);
         }
 
         @Override
         public boolean onInterceptTouchEvent(MotionEvent ev) {
             mIsOnInterceptTouchEventCalled = true;
             return super.onInterceptTouchEvent(ev);
         }
 
         @Override
         public boolean onRequestFocusInDescendants(int direction,
                 Rect previouslyFocusedRect) {
             mIsOnRequestFocusInDescendantsCalled = true;
             return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
         }
 
         @Override
         public void recomputeViewAttributes(View child) {
             mIsRecomputeViewAttributesCalled = true;
             super.recomputeViewAttributes(child);
         }
 
         @Override
         public void removeDetachedView(View child, boolean animate) {
             super.removeDetachedView(child, animate);
         }
 
         @Override
         public boolean showContextMenuForChild(View originalView) {
             mIsShowContextMenuForChildCalled = true;
             return super.showContextMenuForChild(originalView);
         }
 
         @Override
         public boolean isInTouchMode() {
             super.isInTouchMode();
             return false;
         }
 
         @Override
         public void focusableViewAvailable(View v) {
             mIsFocusableViewAvailable = true;
             super.focusableViewAvailable(v);
         }
 
         @Override
         public View focusSearch(View focused, int direction) {
             super.focusSearch(focused, direction);
             return focused;
         }
 
         @Override
         public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
             mIsRequestDisallowInterceptTouchEventCalled = true;
             super.requestDisallowInterceptTouchEvent(disallowIntercept);
         }
 
         @Override
         public void requestTransparentRegion(View child) {
             mIsRequestTransparentRegionCalled = true;
             super.requestTransparentRegion(child);
         }
 
         @Override
         public void debug(int depth) {
             mDebugDepth = depth;
             super.debug(depth);
         }
 
         @Override
         public void requestLayout() {
             mIsRequestLayoutCalled = true;
             super.requestLayout();
         }
 
     }
 
     public void setResult(int resultCode) {
         synchronized (mSync) {
            mHasNotify = true;
             mSync.notify();
             assertEquals(CTSResult.RESULT_OK, resultCode);
         }
     }
 }
