 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.view;
 
 import static com.zapta.apps.maniana.util.Assertions.check;
 import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
 
 import javax.annotation.Nullable;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.PixelFormat;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 import com.zapta.apps.maniana.main.AppContext;
 import com.zapta.apps.maniana.quick_action.QuickActionItem;
 import com.zapta.apps.maniana.quick_action.QuickActionMenu;
 import com.zapta.apps.maniana.quick_action.QuickActionMenu.OnActionItemOutcomeListener;
 import com.zapta.apps.maniana.util.LogUtil;
 
 /**
  * A view for the item list portion of a page.
  * 
  * @author Tal Dayan
  */
 public class ItemListView extends ListView {
 
     /** Id for handler messages indicating long press timeout. */
     private static final int MESSAGE_DOWN_STABLE_TIMEOUT = 1;
 
     /** Id for handler messages used to provide ticks for scroll during drag. */
     private static final int MESSAGE_DRAG_SCROLL_TICK = 2;
 
     /**
      * The the width percentile of the left side color click area. Defines the border between color
      * area click and item text click.
      */
     private static final int COLOR_AREA_HORIZONTAL_PERCENTILE = 12;
 
     /**
      * Percent of text click area from the item view width. Defines the border between the text and
      * button areas for click classification purposes.
      */
     private static final int TEXT_AREA_HORIZONTAL_PERCENTILE = 80;
 
     /** Time interval for scrolling during drag. */
     private static final int DRAG_SCROLL_TICK_MILLIS = 100;
 
     /** Indicates that a drag scroll tick message is in flight. */
     private boolean mPedningDragScrollTick = false;
 
     /** Provided access to app components. */
     private AppContext mApp;
 
     /** Adapter to the underlying model page item list. */
     private ItemListViewAdapter mAdapter;
 
     /**
      * When > 0, indicates that an animation is running. Should ignore user input during this time.
      */
     private int mAnimationsInProgress = 0;
 
     /** Resource ID of drawable to use for background highlight. */
     private int mItemHighlightDrawableResourceId = 0;
 
     /** Outcomes of OnTouchEvent handler. */
     private static enum OnTouchEventOutcome {
         /**
          * Call super and return class. This allows the super class to track and handle the vertical
          * scrolling.
          */
         CALL_SUPER,
         /**
          * Return true without calling the super class. This hides this event from the super class
          * (event is not available for vertical scroll handling). Also, indicates to the super view
          * (not to be consumed with super class) that this even was consumed.
          */
         TRUE
     }
 
     /** Indicates areas of an item view */
     private static enum ItemArea {
         /** The color area on the left. */
         COLOR,
         /** The text area. */
         TEXT,
         /** The button area. */
         BUTTON;
     }
 
     /** This item list view is in one of these states. */
     private static enum State {
         /**
          * The idle state
          * <p>
          * Transitions:<br>
          * pressed_down on item -> DOWN_STABLE<br>
          * pressed_down not on an item -> DOWN_PASSIVE<br>
          */
         UP,
 
         /**
          * The do-nothing down state.
          * <p>
          * Transitions:<br>
          * pressed_up -> UP.<br>
          */
         DOWN_PASSIVE,
 
         /**
          * A down state with no movement and before long click min time.
          * <p>
          * Transitions:<br>
          * pressed_up -> UP (generate onClick)<br>
          * long press timeout -> DOWN_DRAG (start dragging item)<br>
          * movement -> DOWN_UNSTABLE (e.g. horizontal parent scroll)<br>
          */
         DOWN_STABLE,
 
         /**
          * Item pressed but touch moved before long click timeout. Becoming passive and letting the
          * parent to handle (e.g. horizontal parent scroll).
          * <p>
          * Transitions:<br>
          * up -> UP<br>
          */
         DOWN_UNSTABLE,
 
         /**
          * In this state an item is dragged up/down.
          * <p>
          * Transition:<br>
          * pressed_up -> UP
          */
         DOWN_DRAG
     }
 
     /**
      * The viewer state. All state change should be done via {@link #setState(State)}
      */
     private State mState = State.UP;
 
     /**
      * When down, indicates the item view area that was pressed. Is null IFF UP or DOWN_PASSIVE
      * states.
      */
     @Nullable
     private ItemArea mPressedItemArea = null;
 
     /**
      * Index of the item that was pressed. Not used when state is UP or DOWN_STABLE.
      */
     private int mPressDownItemIndex;
 
     // Initial x,y on screen of a the down event
     private int mPressPointInScreenX;
     private int mPressPointInScreenY;
 
     // The difference between screen coordinates and coordinates of this view
     private int mListViewOffsetInScreenX;
     private int mListViewOffsetInScreenY;
 
     /**
      * At what offset inside the item view did the user press.
      */
     private int mPressPointInItemViewX;
     private int mPressPointInItemViewY;
 
     /**
      * Layout params of the hover view being drags. Non null IFF in drag. Changes as the dragged
      * item image view is moved up/down to reposition the image.
      */
     private WindowManager.LayoutParams mDragedItemImageViewWindowParams;
 
     /**
      * During drag, the image with the dragged item snapshot that is hovered above the list.
      */
     private ImageView mDragedItemImageView;
 
     /**
      * The dragged item snapshot used in mDragView.
      */
     private Bitmap mDragBitmap;
 
     /**
      * Index in the model of the item currently hovering above during drag. The view of this item is
      * highlighted to provide feedback to the user.
      */
     private int mDragCurrentHighlightedItemIndex;
 
     /**
      * Time of SystemClock.uptimeMillis() when touched down. Valid in all but UP state. Zero in UP
      * state.
      */
     private long mDownSystemTimeMillies;
 
     /** Adapter to call instance methods upon arrival of time related messages. */
     private class MessageHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             switch (msg.what) {
                 case MESSAGE_DOWN_STABLE_TIMEOUT:
                     transitionDownStableToDrag();
                     break;
                 case MESSAGE_DRAG_SCROLL_TICK:
                     handleDragTick();
                     break;
                 default:
                     throw new RuntimeException("Unknown message type: " + msg.what);
             }
         }
     }
 
     private final MessageHandler mMessageHandler = new MessageHandler();
 
     /** Constructor. Called from page layout inflater. */
     public ItemListView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     /**
      * Setup app related context. Ideally would pass this to the constructor but the constructor is
      * called from the page layout XML inflater so we don't have access to its parameters.
      * 
      * @param app the app context.
      * @param adapter adapter to the underlying PageModel item list.
      */
     public final void setApp(AppContext app, ItemListViewAdapter adapter) {
         this.mApp = checkNotNull(app);
         this.mAdapter = checkNotNull(adapter);
         super.setAdapter(adapter);
     }
 
     @Override
     public final ItemListViewAdapter getAdapter() {
         return mAdapter;
     }
 
     /** Intrcept classify and handle events before dispatching to children views. */
     @Override
     public final boolean onInterceptTouchEvent(MotionEvent ev) {
         final int action = ev.getAction();
 
         if (LogUtil.DEBUG_LEVEL >= 3) {
             LogUtil.debug("onInterceptTouchEvent(): action = %s, state = %s (%d, %d)",
                     ViewUtil.actionDebugName(action), mState, (int) ev.getX(), (int) ev.getY());
         }
 
         // True when the parent is doing vertical scrolling. We don't interrupt
         // by stealing the touch events.
         final boolean doNotStealEvents = super.onInterceptTouchEvent(ev);
 
         // TODO(tal): if this does not get triggered by Jone 2012, remove the error message string
         // since it instanciates a var arg parameter array (performance)
         check(ev.getAction() == MotionEvent.ACTION_DOWN, "Expected action DOWN, found %d",
                 ev.getAction());
 
         // Crash report on the Android market:
         // @formatter:off
         // v1.01.15 Jan 16, 2012 1:06:45 PM
         // java.lang.RuntimeException: Assertion failed: Expected UP, found: DOWN_PASSIVE
         //    at com.zapta.apps.maniana.util.Assertions.check(Assertions.java:26)
         //    at com.zapta.apps.maniana.view.ItemListView.onInterceptTouchEvent(ItemListView.java:260)
         //    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:940)
         //    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:961)
         //    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:961)
         //    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:961)
         //    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:961)
         //    at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:961)
         //    at com.android.internal.policy.impl.PhoneWindow$DecorView.superDispatchTouchEvent(PhoneWindow.java:1711)
         //    at com.android.internal.policy.impl.PhoneWindow.superDispatchTouchEvent(PhoneWindow.java:1145)
         //    at android.app.Activity.dispatchTouchEvent(Activity.java:2096)
         //    at com.android.internal.policy.impl.PhoneWindow$DecorView.dispatchTouchEvent(PhoneWindow.java:1695)
         //    at android.view.ViewRoot.deliverPointerEvent(ViewRoot.java:2217)
         //    at android.view.ViewRoot.handleMessage(ViewRoot.java:1901)
         //    at android.os.Handler.dispatchMessage(Handler.java:99)
         //    at android.os.Looper.loop(Looper.java:130)
         //    at android.app.ActivityThread.main(ActivityThread.java:3701)
         //    at java.lang.reflect.Method.invokeNative(Native Method)
         //    at java.lang.reflect.Method.invoke(Method.java:507)
         //    at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:866)
         //    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:624)
         //    at dalvik.system.NativeStart.main(Native Method)
         // @formatter:on
         //
         // check(mState == State.UP, "Expected UP, found: " + mState);
         //
         // Hack to avoid forced close. Would be nice if we could log this event, for example
         // using the ACRA library but as of Jan 2012 Maniana does not ask for internet access
         // permission.
         //
         if (mState != State.UP) {
             LogUtil.error("Expected state UP but found %s, panic transition to UP", mState);
             transitionToUp();
         }
         check(mState == State.UP, "Expected UP state, found, %s", mState);
 
         // Event coordinate within this list view
         final int eventXInListView = (int) ev.getX();
         final int eventYInListView = (int) ev.getY();
 
         // INVALID_POSITION if not on an item
         final int itemIndex = pointToPosition(eventXInListView, eventYInListView);
 
         if (doNotStealEvents || itemIndex == AdapterView.INVALID_POSITION
                 || mAnimationsInProgress > 0) {
             transitionUpToDownPassive();
         } else {
             transitionUpToDownStable(ev, itemIndex, eventXInListView, eventYInListView);
         }
 
         if (LogUtil.DEBUG_LEVEL >= 3) {
             LogUtil.debug("super.onInterceptTouchEvent(ev) = %s (%s) -> %s", doNotStealEvents,
                     ViewUtil.actionDebugName(ev.getAction()), mState);
         }
 
         return doNotStealEvents;
     }
 
     /** A wrapper for the internal event handle. Manages the return behavior. */
     @Override
     public final boolean onTouchEvent(MotionEvent ev) {
         final OnTouchEventOutcome outcome = onTouchEventInternal(ev);
         final boolean result;
         switch (outcome) {
             case CALL_SUPER:
                 result = super.onTouchEvent(ev);
                 break;
             case TRUE:
                 result = true;
                 break;
             default:
                 throw new RuntimeException("Unknown outcome");
         }
         if (LogUtil.DEBUG_LEVEL >= 3) {
             LogUtil.debug("onTouchEvent() outcome = %s (%s)", outcome, result);
         }
         return result;
     }
 
     /** The main event handler. */
     public final OnTouchEventOutcome onTouchEventInternal(MotionEvent event) {
         checkConsistency();
         final int action = event.getAction();
 
         if (LogUtil.DEBUG_LEVEL >= 3) {
             LogUtil.debug("onTouchEvent(): action = %s, state = %s (%d, %d)",
                     ViewUtil.actionDebugName(action), mState, (int) event.getX(),
                     (int) event.getY());
         }
 
         switch (action) {
             case MotionEvent.ACTION_DOWN: {
                 check(mState == State.DOWN_PASSIVE || mState == State.DOWN_STABLE);
                 return OnTouchEventOutcome.CALL_SUPER;
             }
 
             case MotionEvent.ACTION_MOVE: {
                 if (LogUtil.DEBUG_LEVEL >= 5) {
                     LogUtil.debug("Handling ACTION_MOVE");
                 }
 
                 if (mState == State.DOWN_PASSIVE || mState == State.DOWN_UNSTABLE) {
                     // Do nothing. Be passive.
                     return OnTouchEventOutcome.CALL_SUPER;
                 }
 
                 if (mState == State.DOWN_STABLE) {
                     int totalMovementX = (int) Math.abs(event.getRawX() - mPressPointInScreenX);
                     int totalMovementY = (int) Math.abs(event.getRawY() - mPressPointInScreenY);
                     final int totalMovement = Math.max(totalMovementX, totalMovementY);
                     final int totalMovementLimit = movementLimitInDownStable();
                     if (LogUtil.DEBUG_LEVEL >= 3) {
                         LogUtil.debug("Movement = %s, limit = %s", totalMovement,
                                 totalMovementLimit);
                     }
                     if (totalMovement > totalMovementLimit) {
                         transitionDownStableToUnstable();
                         // Do not consume the event. Let parent to scroll list up/down.
                         return OnTouchEventOutcome.CALL_SUPER;
                     }
 
                     // Here when still in STABLE. We stay in this state. The change to DRAG is done
                     // on the long press timeout message.
                     check(mState == State.DOWN_STABLE);
                     return OnTouchEventOutcome.CALL_SUPER;
                 }
 
                 if (mState == State.DOWN_DRAG) {
                     final int eventYInListView = (int) event.getY();
 
                     mDragedItemImageViewWindowParams.x = 0;
                     mDragedItemImageViewWindowParams.y = eventYInListView - mPressPointInItemViewY
                             + mListViewOffsetInScreenY;
                     mApp.services()
                             .windowManager()
                             .updateViewLayout(mDragedItemImageView,
                                     mDragedItemImageViewWindowParams);
                     updateViewsDuringDrag();
 
                     if (!mPedningDragScrollTick && directionToScroll() != 0) {
                         scheduleNextDragScrollTick(true);
                     }
 
                     // Consume the event. Don't let parent to scroll vertically.
                     return OnTouchEventOutcome.TRUE;
                 }
 
                 // Non reachable
                 check(false, "Unexpected state: " + mState);
                 break;
             }
 
             case MotionEvent.ACTION_UP:
             case MotionEvent.ACTION_CANCEL: {
                 // TODO: should we do only a subset of this stuff for ACTION_CANCEL?
 
                 // Cache values, transitionToUp() clears them.
                 final State cachedLastState = mState;
                 final int cachedPressDownItemIndex = mPressDownItemIndex;
                 final ItemArea cachedPressedItemArea = mPressedItemArea;
                 final int cachedDragCurrentHighlightedItemIndex = mDragCurrentHighlightedItemIndex;
                 // final int cachedTimeMillisSinceDown = timeMillisSinceDown();
 
                 // NOTE: this clears the down related members. Use only cached values.
                 transitionToUp();
 
                 if (cachedLastState == State.DOWN_STABLE && action == MotionEvent.ACTION_UP) {
                     switch (cachedPressedItemArea) {
                         case COLOR:
                             mApp.controller().onItemColorClick(mAdapter.pageKind(),
                                     cachedPressDownItemIndex);
                             break;
                         case TEXT:
                             mApp.controller().onItemTextClick(mAdapter.pageKind(),
                                     cachedPressDownItemIndex);
                             break;
                         case BUTTON:
                             mApp.controller().onItemArrowClick(mAdapter.pageKind(),
                                     cachedPressDownItemIndex);
                             break;
                         default:
                             throw new RuntimeException("Unexpected pressed zone: "
                                     + mPressedItemArea);
                     }
                     return OnTouchEventOutcome.CALL_SUPER;
                 }
 
                 if (cachedLastState == State.DOWN_DRAG
                         && cachedDragCurrentHighlightedItemIndex >= 0
                         && cachedDragCurrentHighlightedItemIndex < getCount()
                         && cachedDragCurrentHighlightedItemIndex != cachedPressDownItemIndex) {
                     mApp.controller().onItemMoveInPage(mAdapter.pageKind(),
                             cachedPressDownItemIndex, cachedDragCurrentHighlightedItemIndex);
                     return OnTouchEventOutcome.CALL_SUPER;
                 }
             }
         }
 
         return OnTouchEventOutcome.CALL_SUPER;
     }
 
     /** Transition from DOWN_STABLE to DOWN_UNSTABLE state */
     private final void transitionDownStableToUnstable() {
         check(mState == State.DOWN_STABLE);
         mMessageHandler.removeMessages(MESSAGE_DOWN_STABLE_TIMEOUT);
         setState(State.DOWN_UNSTABLE);
     }
 
     /** Transition from UP to DOWN_PASSIVE state */
     private final void transitionUpToDownPassive() {
         check(mState == State.UP);
         setState(State.DOWN_PASSIVE);
         mDownSystemTimeMillies = SystemClock.uptimeMillis();
     }
 
     /** Start given animation on view of given item. View is assumed to be visible. */
     public final void startItemAnimation(int itemIndex,
             final AppView.ItemAnimationType animationType, int initialDelayMillis,
             @Nullable final Runnable callback) {
 
         @Nullable
         final ItemView itemView = getItemViewIfVisible(itemIndex);
         // NOTE: this should always be non null but handling gracefully to avoid a
         // force close.
         if (itemView == null) {
             if (callback != null) {
                 callback.run();
             }
             return;
         }
 
         mAnimationsInProgress++;
 
         itemView.startItemAnimation(itemIndex, animationType, initialDelayMillis, new Runnable() {
             @Override
             public void run() {
                 mAnimationsInProgress--;
                 if (mAnimationsInProgress != 0) {
                     // NOTE(tal): we expect to have one animation at a time though this is not an
                     // absolute requirement.
                     LogUtil.warning(
                             "mAnimationsInProgress is non zero after an animation: %s, type: %s",
                             mAnimationsInProgress, animationType);
                 }
                 check(mAnimationsInProgress >= 0);
                 if (callback != null) {
                     callback.run();
                 }
             }
         });
 
     }
 
     /** Transition from UP to DOWN_STABLE as a result of a DOWN event on an item */
     private final void transitionUpToDownStable(MotionEvent ev, int itemIndex,
             int eventXInListView, int eventYInListView) {
         check(mState == State.UP);
         check(itemIndex >= 0);
 
         final ViewGroup itemView = (ViewGroup) getChildAt(itemIndex - getFirstVisiblePosition());
 
         mPressPointInItemViewX = eventXInListView - itemView.getLeft();
         mPressPointInItemViewY = eventYInListView - itemView.getTop();
 
         mPressPointInScreenX = (int) ev.getRawX();
         mPressPointInScreenY = (int) ev.getRawY();
 
         mListViewOffsetInScreenX = mPressPointInScreenX - eventXInListView;
         mListViewOffsetInScreenY = mPressPointInScreenY - eventYInListView;
 
         itemView.setDrawingCacheEnabled(true);
         mDragBitmap = Bitmap.createBitmap(itemView.getDrawingCache());
 
         mDragedItemImageViewWindowParams = new WindowManager.LayoutParams();
         mDragedItemImageViewWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
         mDragedItemImageViewWindowParams.x = eventXInListView - mPressPointInItemViewX
                 + mListViewOffsetInScreenX;
         mDragedItemImageViewWindowParams.y = eventYInListView - mPressPointInItemViewY
                 + mListViewOffsetInScreenY;
 
         mDragedItemImageViewWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
         mDragedItemImageViewWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
         mDragedItemImageViewWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                 | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                 | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                 | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
         mDragedItemImageViewWindowParams.format = PixelFormat.TRANSLUCENT;
         mDragedItemImageViewWindowParams.windowAnimations = 0;
 
         mDragedItemImageView = new ImageView(getContext());
         mDragedItemImageView.setPadding(0, 0, 0, 0);
         mDragedItemImageView.setImageBitmap(mDragBitmap);
 
         mPressDownItemIndex = itemIndex;
 
         // Post a delayed message for the long press timeout period.
         final boolean ok = mMessageHandler.sendEmptyMessageDelayed(MESSAGE_DOWN_STABLE_TIMEOUT,
                 ViewConfiguration.getLongPressTimeout());
         // mApp.resources().getLongPressMinMillis());
         check(ok);
 
         // TODO: prepare and enable drag only when down on button.
         final int xPercentile = (eventXInListView * 100) / mDragBitmap.getWidth();
 
         mPressedItemArea = (xPercentile <= COLOR_AREA_HORIZONTAL_PERCENTILE) ? ItemArea.COLOR
                 : ((xPercentile <= TEXT_AREA_HORIZONTAL_PERCENTILE) ? ItemArea.TEXT
                         : ItemArea.BUTTON);
         if (LogUtil.DEBUG_LEVEL >= 5) {
             LogUtil.debug("Pressed on %s", mPressedItemArea);
         }
 
         setState(State.DOWN_STABLE);
         mDownSystemTimeMillies = SystemClock.uptimeMillis();
         checkConsistency();
     }
 
     /** Return time in millies in a down state */
     private final int timeMillisSinceDown() {
         check(mState != State.UP);
         return (int) (SystemClock.uptimeMillis() - mDownSystemTimeMillies);
     }
 
     /** Returns the max allowable distance in pixles to stay in DOWN_STABLE. */
     private final int movementLimitInDownStable() {
         // TODO: normalize to screen size or pixel density?
         return Math.min(30, 10 + (timeMillisSinceDown() * 20) / 1000);
     }
 
     /** Transition from DOWN_STABLE to DRAG. */
     private void transitionDownStableToDrag() {
         check(mState == State.DOWN_STABLE);
         check(mPressedItemArea != null);
 
         if (LogUtil.DEBUG_LEVEL >= 3) {
             LogUtil.debug("down to drag");
         }
         mApp.services().vibrateForLongPress();
         mApp.services().windowManager()
                 .addView(mDragedItemImageView, mDragedItemImageViewWindowParams);
         setState(State.DOWN_DRAG);
 
         // NOTE(tal): updated by updateViewsDuringDrag
         mDragCurrentHighlightedItemIndex = -1;
 
         // This highlights the initial item under drag.
         updateViewsDuringDrag();
 
         checkConsistency();
 
         if (directionToScroll() != 0) {
             scheduleNextDragScrollTick(true);
         }
     }
 
     /** Display item menu on top of given item */
     public void showItemMenu(final int itemIndex, QuickActionItem actions[],
             final int dismissActionId) {
         final QuickActionMenu quickActionMenu = new QuickActionMenu(mApp,
                 new OnActionItemOutcomeListener() {
                     @Override
                     public void onOutcome(QuickActionMenu source, QuickActionItem actionItem) {
                         mApp.popupsTracker().untrack(source);
                         final int actionId = (actionItem != null) ? actionItem.getActionId()
                                 : dismissActionId;
                         mApp.controller().onItemMenuSelection(mAdapter.pageKind(), itemIndex,
                                 actionId);
                     }
                 });
 
         for (QuickActionItem action : actions) {
             quickActionMenu.addActionItem(action);
         }
 
         @Nullable
         final ItemView itemView = getItemViewIfVisible(itemIndex);
         // NOTE: this should always be non null but handling gracefully to avoid a forced close.
         if (itemView != null) {
             mApp.popupsTracker().track(quickActionMenu);
             quickActionMenu.show(itemView);
         }
     }
 
     /**
      * Schedule a delayed message to trigger the next scroll during drag.
      * 
      * @param now is true, schedule the message with minimal delay, pratcially now.
      */
     private final void scheduleNextDragScrollTick(boolean now) {
         check(mState == State.DOWN_DRAG);
         check(!mPedningDragScrollTick);
         final long millis = now ? 1 : DRAG_SCROLL_TICK_MILLIS;
         final boolean ok = mMessageHandler
                 .sendEmptyMessageDelayed(MESSAGE_DRAG_SCROLL_TICK, millis);
         check(ok);
         mPedningDragScrollTick = true;
     }
 
     /** Transition from any state to UP */
     private final void transitionToUp() {
         checkConsistency();
 
         if (mState != State.UP) {
             if (mState != State.DOWN_PASSIVE) {
 
                 if (mState == State.DOWN_DRAG) {
                     mDragedItemImageView.setVisibility(GONE);
                     mApp.services().windowManager().removeView(mDragedItemImageView);
                     updateViewsHighlight(-1);
                 }
 
                 mDragedItemImageView.setImageDrawable(null);
                 mDragedItemImageView = null;
 
                 mDragBitmap.recycle();
                 mDragBitmap = null;
 
                 mDragedItemImageViewWindowParams = null;
 
                 mMessageHandler.removeMessages(MESSAGE_DOWN_STABLE_TIMEOUT);
                 mMessageHandler.removeMessages(MESSAGE_DRAG_SCROLL_TICK);
 
                 mPedningDragScrollTick = false;
 
                 mPressedItemArea = null;
             }
         }
 
         setState(State.UP);
         mDownSystemTimeMillies = 0;
 
         checkConsistency();
     }
 
     /** Common method to change state and print debug info. */
     private final void setState(State state) {
         if (LogUtil.DEBUG_LEVEL >= 3) {
             if (mState == State.UP) {
                 LogUtil.debug("%s -> %s", mState, state);
             } else {
                 LogUtil.debug("%s -> %s, down time = %sms", mState, state, timeMillisSinceDown());
             }
         }
 
         mState = state;
     }
 
     /** Given a y position during drag, return the index of the underlying item. */
     private final int getUnderlyingItemDuringDrag(int y) {
         check(mState == State.DOWN_DRAG);
         check(getHeaderViewsCount() == 0);
 
         final int firstVisibleItem = getFirstVisiblePosition();
         int bestMatch = firstVisibleItem;
 
         for (int i = 0;; i++) {
             final View view = getChildAt(i);
             if (view == null) {
                 break; // no more views
             }
 
             if (y >= view.getTop()) {
                 bestMatch = firstVisibleItem + i;
             }
         }
 
         return bestMatch;
     }
 
     /**
      * Get view of given item index. View is assumed to be visible. Returns null if could not find
      * the view.
      */
     @Nullable
     private final ItemView getItemViewIfVisible(int itemIndex) {
         final int firstVisibleItem = getFirstVisiblePosition();
         final int visibleIndex = itemIndex - firstVisibleItem;
         if (visibleIndex < 0) {
             LogUtil.error("Tried to access a view before the visible range: %s vs %s", itemIndex,
                     firstVisibleItem);
             return null;
         }
         // check(itemIndex >= firstVisibleItem, "Non visible");
         final ItemView result = (ItemView) getChildAt(visibleIndex);
 
         if (result == null) {
             LogUtil.error("Could not find visible(?) item, index: %s , first visible: %s",
                     itemIndex, firstVisibleItem);
         }
         return result;
     }
 
     /** Scroll to top of page. */
     public void scrollToTop() {
         setSelection(0);
     }
 
     /**
      * Update the underlying views during drag. This methods is responsible for highlighting the
      * current underlying item.
      */
     private final void updateViewsDuringDrag() {
         check(mState == State.DOWN_DRAG);
 
         // Mid y, relative to list view, of the hovering view.
         final int midY = mDragedItemImageViewWindowParams.y - mListViewOffsetInScreenY
                 + (mDragedItemImageView.getHeight() / 2);
 
         final int itemIndex = getUnderlyingItemDuringDrag(midY);
 
         // If no change nothing to do.
         if (itemIndex == mDragCurrentHighlightedItemIndex) {
             return;
         }
 
         mDragCurrentHighlightedItemIndex = itemIndex;
         updateViewsHighlight(mDragCurrentHighlightedItemIndex);
     }
 
     /**
      * Clear highlight of all views. Set view of item with given index to highlight.
      * 
      * @param the index of the model item whose view should be highlighted. Use out of range value
      *        (e.g. -1) to clear highlight of all items.
      */
     private final void updateViewsHighlight(int itemIndexToHighlight) {
         check(mState == State.DOWN_DRAG);
 
         final int viewIndexToHighlight = itemIndexToHighlight - getFirstVisiblePosition();
 
         // TODO: override the add headers methods to throw an exception if somebody tries
         // to set headers/footers.
         check(getHeaderViewsCount() == 0, "Headers not supported in this view");
 
         for (int i = 0;; i++) {
             final ItemView itemView = (ItemView) getChildAt(i);
             if (itemView == null) {
                 // no more views
                 break;
             }
             setItemViewHighlight(itemView, i == viewIndexToHighlight);
         }
     }
 
     /** Called periodically during drag to scroll the underlying list. */
     private final void handleDragTick() {
         check(mState == State.DOWN_DRAG);
         check(mPedningDragScrollTick);
 
         mPedningDragScrollTick = false;
 
         final int direction = directionToScroll();
         if (direction < 0) {
             smoothScrollToPosition(getFirstVisiblePosition() - 1);
             updateViewsDuringDrag();
         } else if (direction > 0) {
             smoothScrollToPosition(getLastVisiblePosition() + 1);
             updateViewsDuringDrag();
         } else {
             return;
         }
 
         if (directionToScroll() != 0) {
             scheduleNextDragScrollTick(false);
         }
     }
 
     /**
      * Determine during drag if a scroll of the underlying list is needed.
      * 
      * @return status. -1 = scroll toward low item indexes, +1 = scroll toward high item indexes, 0
      *         = scroll not needed.
      */
     private final int directionToScroll() {
         check(mState == State.DOWN_DRAG);
         if (mDragCurrentHighlightedItemIndex > 0
                 && mDragCurrentHighlightedItemIndex <= getFirstVisiblePosition() + 1) {
             return -1;
         }
         if (mDragCurrentHighlightedItemIndex < getCount() - 1
                 && mDragCurrentHighlightedItemIndex >= getLastVisiblePosition() - 1) {
             return 1;
         }
         return 0;
     }
 
     /** Updated view of given item to reflect ModelItem change. Assumes the view is visible. */
     public void updateSingleItemView(int itemIndex) {
         @Nullable
         final ItemView itemView = getItemViewIfVisible(itemIndex);
         // NOTE: this should always be non null but handling gracefully to avoid a
         // force close.
         if (itemView != null) {
             itemView.updateFromItem(mAdapter.getItem(itemIndex));
         }
     }
 
     /** Update all views to reflect change in item font preferences. */
     public final void onPageItemFontVariationPreferenceChange() {
         // This causes this list view to call the adapter.getView() for each of
         // its child item views. This will cause the item fonts to be updated to
         // the latest font preference.
         //
         // NOTE(tal): a simple iteration over the child views did not work well.
         // In some conditions, some views were not updated. See more info here
         // http://tinyurl.com/7ao2bga
         invalidateViews();
     }
 
     /** Does nothing if item is non visible. */
     public void setItemViewHighlight(int itemIndex, boolean isHighlight) {
         @Nullable
         final ItemView itemView = getItemViewIfVisible(itemIndex);
         if (itemView != null) {
             check(mItemHighlightDrawableResourceId != 0, "Not set yet");
             setItemViewHighlight(itemView, isHighlight);
         }
     }
 
     private final void setItemViewHighlight(ItemView itemView, boolean isHighlight) {
         if (isHighlight) {
             itemView.setHighlight(mItemHighlightDrawableResourceId);
         } else {
             itemView.clearHighlight();
         }
     }
 
     public void setItemHighlightDrawableResourceId(int drawableResourceId) {
         check(drawableResourceId != 0, "Zero resource id");
         mItemHighlightDrawableResourceId = drawableResourceId;
     }
 
     private final void checkConsistency() {
         final boolean upOrPassive = (mState == State.UP) || (mState == State.DOWN_PASSIVE);
         check(!upOrPassive == (mDragedItemImageView != null));
         check(!upOrPassive == (mDragBitmap != null));
 
         check(!upOrPassive == (mDragedItemImageViewWindowParams != null));
 
         check((mState != State.UP) == (mDownSystemTimeMillies > 0));
     }
 }
