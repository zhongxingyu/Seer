 package com.github.dimedriller.listview;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.database.DataSetObserver;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Adapter;
 import com.github.dimedriller.R;
 import com.github.dimedriller.listview.diff.DeleteDiffAtom;
 import com.github.dimedriller.listview.diff.DiffAnalyser;
 import com.github.dimedriller.listview.diff.DiffAtom;
 import com.github.dimedriller.listview.diff.InsertDiffAtom;
 import com.github.dimedriller.log.Log;
 
 import java.util.ArrayList;
 
 /**
  ***********************************************************************************************************************
  * This class is designed to display horizontal list view of elements
  ***********************************************************************************************************************
  */
 public class HorizontalListView extends HorizontalAbsListView {
     private static final int DEFAULT_EXPAND_COLLAPSE_DURATION = 300;
     private static final int DEFAULT_EXPAND_COLLAPSE_DELAY = 0;
 
     private final DataSetObserver mDataSetObserver = new DataSetObserver() {
         @Override
         public void onChanged() {
             if (mInsertDeleteAction == null) { // If there are no current updating action than start new updating action
                 onAdapterDataChanged();
                 return;
             }
 
             if (mPostponedDataChangedUpdate != null) // If one update already scheduled ignore another one
                 return;
 
            mPostponedDataChangedUpdate = new PostponedDataChangedUpdate(); // Start new update when current one is
             postDelayed(mPostponedDataChangedUpdate, mInsertDeleteAction.getRemainingTime() + 100); // finished + 100ms
         }
 
         @Override
         public void onInvalidated() {
             // TODO: Implement smooth version of invalidating
             requestLayout();
         }
     };
     private Animation mAddViewAnimation;
     private Animation mRemoveViewAnimation;
     private int mExpandCollapseDelay;
     private int mExpandCollapseDuration;
 
     private InsertDeleteAction mInsertDeleteAction;
     private Runnable mPostponedDataChangedUpdate;
     private Runnable mPostponedLayoutUpdate;
 
     @SuppressWarnings("UnusedDeclaration")
     public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         initViewParameters(context, attrs);
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public HorizontalListView(Context context, AttributeSet attrs) {
         super(context, attrs);
         initViewParameters(context, attrs);
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public HorizontalListView(Context context) {
         super(context);
         initViewParameters();
     }
 
     private static Animation extractAnimation(Context context, TypedArray rawParams, int paramID) {
         int animationID = rawParams.getResourceId(paramID, 0);
         if (animationID == 0)
             return null;
         else
             return AnimationUtils.loadAnimation(context, animationID);
     }
 
     private void initViewParameters(Context context, AttributeSet attrs) {
         TypedArray rawParams = context.obtainStyledAttributes(attrs, R.styleable.HorizontalListView);
         mAddViewAnimation = extractAnimation(context, rawParams, R.styleable.HorizontalListView_addViewAnimation);
         mRemoveViewAnimation = extractAnimation(context, rawParams, R.styleable.HorizontalListView_removeViewAnimation);
         mExpandCollapseDelay = rawParams.getInteger(R.styleable.HorizontalListView_expandCollapseDelay,
                 DEFAULT_EXPAND_COLLAPSE_DELAY);
         mExpandCollapseDuration = rawParams.getInteger(R.styleable.HorizontalListView_expandCollapseDuration,
                 DEFAULT_EXPAND_COLLAPSE_DURATION);
         rawParams.recycle();
     }
 
     private void initViewParameters() {
         mExpandCollapseDelay = DEFAULT_EXPAND_COLLAPSE_DELAY;
         mExpandCollapseDuration = DEFAULT_EXPAND_COLLAPSE_DURATION;
     }
 
     @Override
     public LayoutParams generateDefaultLayoutParams() {
         return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
     }
 
     @Override
     protected ItemInfoManager createItemInfoManager(Adapter adapter) {
         return new ListItemInfoManager(adapter);
     }
 
     @Override
     public void setAdapter(Adapter adapter) {
        Log.d(Integer.toHexString(hashCode()));

        if (mInsertDeleteAction != null) {
            Log.d(Integer.toHexString(hashCode()), "mInsertDeleteAction != null");
            removeCallbacks(mInsertDeleteAction);
            mInsertDeleteAction = null;
        }

         Adapter oldAdapter = getAdapter();
         if (oldAdapter != null)
             oldAdapter.unregisterDataSetObserver(mDataSetObserver);
 
         super.setAdapter(adapter);
 
         if (adapter != null)
             adapter.registerDataSetObserver(mDataSetObserver);
     }
 
     @Override
     protected int addItemsRight(int dX) {
         int firstItemOffset;
         InsertDeleteAction insertDeleteAction = mInsertDeleteAction;
         if (insertDeleteAction == null)
             firstItemOffset = 0;
        else // Items to be deleted was already removed from adapter but they are still visible and corresponding
            firstItemOffset = insertDeleteAction.getDeletionsCount(); // ItemInfo objects are in mItems list. So
        // correction for adapter is necessary when items are added right.
 
         mFirstGlobalItemIndex -= firstItemOffset;
         int newDX = super.addItemsRight(dX);
         mFirstGlobalItemIndex += firstItemOffset;
         return newDX;
     }
 
     @Override
     protected void removeItemsLeft(int dX) {
         super.removeItemsLeft(dX);
 
         InsertDeleteAction insertDeleteAction = mInsertDeleteAction;
         if (insertDeleteAction != null) {
             int oldItemsToDeleteCount = insertDeleteAction.getDeletionsCount();
             insertDeleteAction.cleanUpSteps();
             int newItemsToDeleteCount = insertDeleteAction.getDeletionsCount();
             mFirstGlobalItemIndex += newItemsToDeleteCount - oldItemsToDeleteCount;
         }
     }
 
     @Override
     protected void removeItemsRight(int dX) {
         super.removeItemsRight(dX);
 
         InsertDeleteAction insertDeleteAction = mInsertDeleteAction;
         if (insertDeleteAction != null)
             insertDeleteAction.cleanUpSteps();
     }
 
     @Override
     protected void onLayout(boolean isChanged, int l, int t, int r, int b) {
        if (mInsertDeleteAction == null) {
            Log.d(Integer.toHexString(hashCode()), isChanged, l, t, r, b);
            super.onLayout(isChanged, l, t, r, b);
        } else {
            Log.d(Integer.toHexString(hashCode()), "Postponed");
             if (mPostponedLayoutUpdate != null)
                 removeCallbacks(mPostponedLayoutUpdate);
             mPostponedLayoutUpdate = new PostponedLayoutUpdate(isChanged, l, t, r, b);
             postDelayed(mPostponedLayoutUpdate, mInsertDeleteAction.getRemainingTime());
         }
     }
 
     @Override
     protected boolean isTapItemAvailable() {
         return mInsertDeleteAction == null;
     }
 
     private Object[] getVisibleItemsList() {
         ArrayList<ItemInfo> items = mItems;
         int itemsCount = items.size();
         Object[] visibleItems = new Object[itemsCount];
         for(int counterItem = 0; counterItem < itemsCount; counterItem++)
             visibleItems[counterItem] = ((ListItemInfo)items.get(counterItem)).getItem();
         return visibleItems;
     }
 
     /**
      *******************************************************************************************************************
      * Starts insertion and deletion animations
      *******************************************************************************************************************
      */
     private void startListUpdate(int adapterOffset, DiffAtom[] changes) {
         for(DiffAtom change : changes)
             Log.d(change);
 
         ItemInfoManager itemsManager = getItemsManager();
         ArrayList<ItemInfo> items = mItems;
 
         ArrayList<UpdateStep> updateSteps = new ArrayList<UpdateStep>();
         int itemsFullWidth = getDisplayedItemsFullWidth();
         int itemsOldFullWidth = itemsFullWidth;
         int itemsToDeleteCount = 0;
         int paddingLeft = getPaddingLeft();
         int paddingTop = getPaddingTop();
 
         for(DiffAtom diff : changes)
             if (diff instanceof InsertDiffAtom) {
                 InsertDiffAtom insertDiff = (InsertDiffAtom) diff;
                 ListItemInfo itemInfo = (ListItemInfo) itemsManager.createItemInfo(this, insertDiff.getAdapterPosition());
                 itemInfo.measureViews(getWidthWithoutPaddings(), getHeightWithoutPaddings());
 
                 itemsFullWidth += itemInfo.getWidth();
 
                 int insertIndex = insertDiff.getListPosition();
                 int itemsCount = items.size();
                 int layoutLeft;
                 if (itemsCount <= insertIndex) {
                     layoutLeft = items.get(itemsCount - 1).getRight();
                     items.add(itemInfo);
                 } else {
                     layoutLeft = items.get(insertIndex).getLeft();
                     items.add(insertIndex, itemInfo);
                 }
                 itemInfo.layoutViews(layoutLeft, paddingLeft, paddingTop, 0);
 
                 itemInfo.startAnimation(mAddViewAnimation);
                 updateSteps.add(new InsertStep(itemInfo));
 
             } else {
                 int deleteIndex = ((DeleteDiffAtom) diff).getListPosition();
                 ListItemInfo itemInfo = (ListItemInfo) items.get(deleteIndex);
                 itemInfo.startAnimation(mRemoveViewAnimation);
 
                 itemsToDeleteCount++;
                 itemsFullWidth -= itemInfo.getWidth();
 
                 updateSteps.add(new DeleteStep(itemInfo));
             }
 
         int viewWidth = getWidthWithoutPaddings();
         int adapterItemsCount = getItemsManager().getItemInfoCount();
         int adapterIndex = adapterOffset + items.size() - itemsToDeleteCount;
         int layoutLeft;
         if (items.size() == 0)
             layoutLeft = 0;
         else
             layoutLeft = items.get(items.size() - 1).getRight();
         while (  itemsOldFullWidth < viewWidth
               && viewWidth > itemsFullWidth
               && adapterIndex < adapterItemsCount) {
             ListItemInfo insertedItem = (ListItemInfo) itemsManager.createItemInfo(this, adapterIndex);
             insertedItem.measureViews(getWidthWithoutPaddings(), getHeightWithoutPaddings());
 
             itemsFullWidth += insertedItem.getWidth();
             items.add(insertedItem);
             insertedItem.layoutViews(layoutLeft, paddingLeft, paddingTop, 0);
             layoutLeft = insertedItem.getRight();
 
             insertedItem.startAnimation(mAddViewAnimation);
 
             updateSteps.add(new InsertStep(insertedItem));
             adapterIndex++;
         }
 
         if (items.size() == 0)
             return;
 
         int layoutRight = items.get(0).getLeft();
         int leftItemsWidth = 0;
         while (  viewWidth > itemsFullWidth
               && adapterOffset > 0) {
             adapterOffset--;
             ItemInfo insertedItem = itemsManager.createItemInfo(this, adapterOffset);
             insertedItem.measureViews(getWidthWithoutPaddings(), getHeightWithoutPaddings());
 
             itemsFullWidth += insertedItem.getWidth();
             leftItemsWidth += insertedItem.getWidth();
             layoutRight -= insertedItem.getWidth();
             items.add(0, insertedItem);
             insertedItem.layoutViews(layoutRight, paddingLeft, getPaddingTop());
         }
 
         if (leftItemsWidth > 0) {
             int moveLeftX;
             if (itemsFullWidth > viewWidth)
                 moveLeftX = leftItemsWidth - itemsFullWidth + viewWidth;
             else
                 moveLeftX = leftItemsWidth;
             updateSteps.add(0, new MoveStep(moveLeftX));
         }
 
         mFirstGlobalItemIndex = adapterOffset;
         mInsertDeleteAction = new InsertDeleteAction(updateSteps, mExpandCollapseDuration);
         postDelayed(mInsertDeleteAction, mExpandCollapseDelay);
     }
 
     private void onAdapterDataChanged() {
         Object[] visibleItems = getVisibleItemsList();
         final DiffAnalyser diffAnalyser = new DiffAnalyser(visibleItems);
 
         Adapter adapter = getAdapter();
         diffAnalyser.findDiff(adapter);
 
         startListUpdate(diffAnalyser.getSubsetOffset(), diffAnalyser.getChanges());
     }
 
     protected static class ListItemInfo extends HorizontalAbsListView.ItemInfo {
         private View mView;
         private Object mItem;
         private int mViewTypeID;
 
         @Override
         public void createItemViews(HorizontalAbsListView parent,
                 int itemIndex,
                 Adapter adapter,
                 HorizontalAbsListView.ViewCache viewCache) {
             int viewType = adapter.getItemViewType(itemIndex);
             View cachedView = viewCache.poll(viewType);
             mView = adapter.getView(itemIndex, cachedView, parent);
 
             mItem = adapter.getItem(itemIndex);
             mViewTypeID = adapter.getItemViewType(itemIndex);
             setRecyclingAvailable(true);
         }
 
         @Override
         public void addItemViews(HorizontalAbsListView parent) {
             ViewGroup.LayoutParams params = mView.getLayoutParams();
             if (params == null) {
                 params = parent.generateDefaultLayoutParams();
                 mView.setLayoutParams(params);
             }
             parent.addViewInLayout(mView, -1, params, true);
         }
 
         @Override
         public void removeItemViews(HorizontalAbsListView parent) {
             mView.clearAnimation();
             parent.removeViewInLayout(mView);
         }
 
         @Override
         public void recycleItemViews(HorizontalAbsListView.ViewCache viewCache) {
             if (mViewTypeID != -1) {
                 viewCache.offer(mViewTypeID, mView);
                 mViewTypeID = -1;
             }
             mView = null;
             mItem = null;
         }
 
         @Override
         public void measureViewsBySpecs(int parentSpecWidth,
                 int paddingHorizontal,
                 int parentSpecHeight,
                 int paddingVertical) {
             LayoutParams params = mView.getLayoutParams();
             int childSpecWidth = getChildMeasureSpec(parentSpecWidth, paddingHorizontal, params.width);
             int childSpecHeight = getChildMeasureSpec(parentSpecHeight, paddingVertical, params.height);
 
             mView.measure(childSpecWidth, childSpecHeight);
             setWidth(mView.getMeasuredWidth());
             setHeight(mView.getMeasuredHeight());
         }
 
         @Override
         public void measureViews(int parentWidth, int parentHeight) {
             int parentSpecWidth = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.EXACTLY);
             int parentSpecHeight = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);
 
             measureViewsBySpecs(parentSpecWidth, 0, parentSpecHeight, 0);
         }
 
         @Override
         protected void onLayoutViews(int left, int top, int width) {
             mView.layout(left, top, left + width, top + getHeight());
         }
 
         @Override
         protected void onOffsetViews(int dX) {
             mView.offsetLeftAndRight(dX);
         }
 
         @Override
         public void showPressed(int touchX, int touchY) {
             mView.setPressed(true);
         }
 
         @Override
         public void hidePressed() {
             mView.setPressed(false);
         }
 
         @Override
         public int findAdapterItemIndex(int index, int x, int y) {
             return index;
         }
 
         @Override
         public View findAdapterViewItem(int x, int y) {
             return mView;
         }
 
         public Object getItem() {
             return mItem;
         }
 
         public void startAnimation(Animation animation) {
             if (animation == null)
                 return;
             mView.startAnimation(animation);
         }
 
         @Override
         public String toString() {
             return getClass().getSimpleName() + " item: " + mItem + " @ "
                     + Integer.toString(hashCode(), 16).toUpperCase();
         }
     }
 
     protected static class ListItemInfoManager extends ItemInfoManager {
         public ListItemInfoManager(Adapter adapter) {
             super(adapter);
         }
 
         @Override
         protected int onGetItemInfoCount(Adapter adapter) {
             return adapter.getCount();
         }
 
         @Override
         protected ItemInfo onCreateItemInfo() {
             return new ListItemInfo();
         }
     }
 
     private interface UpdateStep {
         public void start();
         public int makeStep(float interpolatedTime);
         public boolean isValid();
         public void finish();
     }
 
     private class InsertStep implements UpdateStep {
         private final ListItemInfo mItem;
 
         private int mPreviousWidth;
 
         public InsertStep(ListItemInfo item) {
             mItem = item;
             item.setRecyclingAvailable(false);
         }
 
         @Override
         public void start() {
             mPreviousWidth = 0;
         }
 
         @Override
         public int makeStep(float interpolatedTime) {
             ItemInfo item = mItem;
             int finalWidth = item.getWidth();
             int currentWidth = Math.round(finalWidth * interpolatedTime);
             currentWidth = Math.min(currentWidth, finalWidth);
 
             item.layoutViews(item.getLeft(), getPaddingLeft(), getPaddingTop(), currentWidth);
 
             int delta = currentWidth - mPreviousWidth;
             mPreviousWidth = currentWidth;
             ArrayList<ItemInfo> items = mItems;
             int itemIndex = items.indexOf(item);
             shiftItems(itemIndex + 1, items.size() - itemIndex - 1, delta);
 
             return delta;
         }
 
         @Override
         public boolean isValid() {
             return mItem.getItem() != null;
         }
 
         @Override
         public void finish() {
             // No action
         }
     }
 
     private class DeleteStep implements UpdateStep {
         private final ListItemInfo mItem;
 
         private int mPreviousWidth;
 
         private DeleteStep(ListItemInfo item) {
             mItem = item;
             item.setRecyclingAvailable(false);
         }
 
         @Override
         public void start() {
             mPreviousWidth = mItem.getWidth();
         }
 
         @Override
         public int makeStep(float interpolatedTime) {
             ItemInfo item = mItem;
             int startWidth = item.getWidth();
             int currentWidth = Math.round(startWidth * (1 - interpolatedTime));
             currentWidth = Math.max(0, currentWidth);
 
             item.layoutViews(item.getLeft(), getPaddingLeft(), getPaddingTop(), currentWidth);
 
             int delta = currentWidth - mPreviousWidth;
             mPreviousWidth = currentWidth;
             ArrayList<ItemInfo> items = mItems;
             int itemIndex = items.indexOf(item);
             shiftItems(itemIndex + 1, items.size() - itemIndex - 1, delta);
 
             return delta;
         }
 
         @Override
         public boolean isValid() {
             return mItem.getItem() != null;
         }
 
         @Override
         public void finish() {
             ArrayList<ItemInfo> items = mItems;
             int itemIndex = items.indexOf(mItem);
             if (itemIndex != -1) {
                 items.remove(itemIndex);
                 getItemsManager().recycleItemInfo(HorizontalListView.this, mItem);
             }
         }
     }
 
     private class MoveStep implements UpdateStep {
         private final int mTotalOffset;
 
         private int mPreviousOffset;
 
         private MoveStep(int offset) {
             mTotalOffset = offset;
         }
 
         @Override
         public void start() {
             mPreviousOffset = 0;
         }
 
         @Override
         public int makeStep(float interpolatedTime) {
             int currentOffset = Math.round(interpolatedTime * mTotalOffset);
             int delta = currentOffset - mPreviousOffset;
             mPreviousOffset = currentOffset;
 
             shiftItems(delta);
 
             return delta;
         }
 
         @Override
         public boolean isValid() {
             return true;
         }
 
         @Override
         public void finish() {
             // No action
         }
     }
 
     private class InsertDeleteAction implements Runnable {
         private final ArrayList<UpdateStep> mUpdateSteps;
         private final long mStartTime;
         private final long mDuration;
         private int mDeletionsCount;
 
         public InsertDeleteAction(ArrayList<UpdateStep> updateSteps, long duration) {
             mUpdateSteps = updateSteps;
             mStartTime = System.currentTimeMillis();
             mDuration = duration;
 
             int deletionsCount = 0;
             for(UpdateStep updateStep : updateSteps) {
                 updateStep.start();
                 if (updateStep instanceof DeleteStep)
                     deletionsCount++;
             }
             mDeletionsCount = deletionsCount;
         }
 
         public long getRemainingTime() {
             long remainingTime = mDuration - System.currentTimeMillis() + mStartTime;
             Math.max(0, remainingTime);
             return remainingTime;
         }
 
         public int getDeletionsCount() {
             return mDeletionsCount;
         }
 
         private int makeSteps(float interpolatedTime) {
             ArrayList<UpdateStep> steps = mUpdateSteps;
             int countSteps = steps.size();
             int delta = 0;
 
             // for each cycle is not used to avoid invocation of GC during animation
             //noinspection ForLoopReplaceableByForEach
             for(int counterStep = 0; counterStep < countSteps; counterStep++)
                 delta += steps.get(counterStep).makeStep(interpolatedTime);
             return delta;
         }
 
         public void cleanUpSteps() {
             ArrayList<UpdateStep> steps = mUpdateSteps;
             for(int counterStep = steps.size() - 1; counterStep >= 0; counterStep--) {
                 UpdateStep step = steps.get(counterStep);
                 if (step.isValid())
                     continue;
                 steps.remove(counterStep);
                 if (step instanceof DeleteStep)
                     mDeletionsCount--;
             }
         }
 
         private void finishSteps() {
             for(UpdateStep step : mUpdateSteps)
                 step.finish();
         }
 
         @Override
         public void run() {
             long currentTime = System.currentTimeMillis();
             float interpolatedTime = (float) (currentTime - mStartTime) / mDuration;
             interpolatedTime = Math.max(0.0f, interpolatedTime);
             interpolatedTime = Math.min(1.0f, interpolatedTime);
 
             int updateDelta = makeSteps(interpolatedTime);
 
             if (updateDelta > 0) // If total items width is increased than remove invisible items on right
                 removeItemsRight(0);
             else if (updateDelta < 0){ // If total items width is decreased than add items on right if possible
                 int deltaDiff = addItemsRight(updateDelta) - updateDelta;
 
                 if (deltaDiff > 0) { // If visible part of list is at the end and if a visible item is removed
                     int newDeltaDiff = addItemsLeft(-deltaDiff); // than do mLeft side correction to adjust visible
                    shiftItems(-newDeltaDiff); // items on right side
                 }
             }
 
             if (interpolatedTime == 1.0f) {
                 finishSteps();
                 mInsertDeleteAction = null;
             } else
                 post(this);
             invalidate();
         }
     }
 
     private class PostponedDataChangedUpdate implements Runnable {
         @Override
         public void run() {
             onAdapterDataChanged();
             mPostponedDataChangedUpdate = null;
         }
     }
 
     private class PostponedLayoutUpdate implements Runnable {
         private final boolean mIsChanged;
         private final int mLeft;
         private final int mTop;
         private final int mRight;
         private final int mBottom;
 
         private PostponedLayoutUpdate(boolean isChanged, int left, int top, int right, int bottom) {
             mIsChanged = isChanged;
             mLeft = left;
             mTop = top;
             mRight = right;
             mBottom = bottom;
         }
 
         @Override
         public void run() {
             onLayout(mIsChanged, mLeft, mTop, mRight, mBottom);
         }
     }
 }
