 package com.github.dimedriller.listview;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.Adapter;
 import com.github.dimedriller.listview.diff.DeleteDiffAtom;
 import com.github.dimedriller.listview.diff.DiffAnalyser;
 import com.github.dimedriller.listview.diff.DiffAtom;
 import com.github.dimedriller.listview.diff.InsertDiffAtom;
 
 import java.util.ArrayList;
 
 /**
  ***********************************************************************************************************************
  * This class is designed to display horizontal list view of elements
  ***********************************************************************************************************************
  */
 public class HorizontalListView extends HorizontalAbsListView {
     private final DataSetObserver mDataSetObserver = new DataSetObserver() {
         @Override
         public void onChanged() {
             onAdapterDataChanged();
         }
 
         @Override
         public void onInvalidated() {
             // TODO: Implement this method
             super.onInvalidated();
         }
     };
     private Runnable mInsertDeleteAction;
     private Runnable mRearrangementAction;
 
     public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     public HorizontalListView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     public HorizontalListView(Context context) {
         super(context);
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
         Adapter oldAdapter = getAdapter();
         if (oldAdapter != null)
             oldAdapter.unregisterDataSetObserver(mDataSetObserver);
 
         super.setAdapter(adapter);
 
         if (adapter != null)
             adapter.registerDataSetObserver(mDataSetObserver);
     }
 
     private Object[] getVisibleItemsList() {
         ArrayList<ItemInfo> items = mItems;
         int itemsCount = items.size();
         Object[] visibleItems = new Object[itemsCount];
         for(int counterItem = 0; counterItem < itemsCount; counterItem++)
             visibleItems[counterItem] = ((ListItemInfo)items.get(counterItem)).getItem();
         return visibleItems;
     }
 
     private Animation getInsertAnimation() {
         AlphaAnimation animation = new AlphaAnimation(0, 1);
         animation.setDuration(300);
         return animation;
     }
 
     private Animation getDeleteAnimation() {
         AlphaAnimation animation = new AlphaAnimation(1, 0);
         animation.setDuration(300);
         return animation;
     }
 
     /**
      *******************************************************************************************************************
      * Starts insertion and deletion animations
      *******************************************************************************************************************
      */
     private void startListUpdate(int adapterOffset, DiffAtom[] changes) {
         for(DiffAtom change : changes)
             Log.d("HorizontalListView", change.toString());
 
         ItemInfoManager itemsManager = getItemsManager();
         ArrayList<ItemInfo> items = mItems;
 
         ArrayList<UpdateStep> updateSteps = new ArrayList<UpdateStep>();
         int itemsFullWidth = getDisplayedItemsFullWidth();
         int paddingLeft = getPaddingLeft();
         int paddingTop = getPaddingTop();
 
         for(DiffAtom diff : changes)
             if (diff instanceof InsertDiffAtom) {
                 InsertDiffAtom insertDiff = (InsertDiffAtom) diff;
                 ItemInfo itemInfo = itemsManager.createItemInfo(this, insertDiff.getAdapterPosition());
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
 
                 ((ListItemInfo)itemInfo).mView.startAnimation(getInsertAnimation());
                 updateSteps.add(new InsertStep(itemInfo));
 
             } else {
                 int deleteIndex = ((DeleteDiffAtom) diff).getListPosition();
                 ItemInfo itemInfo = items.get(deleteIndex);
                 ((ListItemInfo) itemInfo).mView.startAnimation(getDeleteAnimation());
 
                 updateSteps.add(new DeleteStep(itemInfo));
             }
 
         int viewWidth = getWidthWithoutPaddings();
         int adapterItemsCount = getItemsManager().getItemInfoCount();
         while (  viewWidth > itemsFullWidth
              && adapterOffset + items.size() < adapterItemsCount) {
            int adapterIndex = adapterOffset + items.size();
             ItemInfo insertedItem = itemsManager.createItemInfo(this, adapterIndex);
             insertedItem.measureViews(getWidthWithoutPaddings(), getHeightWithoutPaddings());
 
             itemsFullWidth += insertedItem.getWidth();
             int layoutLeft = items.get(items.size() - 1).getRight();
             items.add(insertedItem);
             insertedItem.layoutViews(layoutLeft, paddingLeft, paddingTop, 0);
 
             ((ListItemInfo)insertedItem).mView.startAnimation(getInsertAnimation());
 
             updateSteps.add(new InsertStep(insertedItem));
         }
 
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
         UpdateStep[] updateStepsArray = updateSteps.toArray(new UpdateStep[updateSteps.size()]);
         post(new InsertDeleteAction(updateStepsArray, 300));
     }
 
     private void onAdapterDataChanged() {
         stopScrolling();
 
         Object[] visibleItems = getVisibleItemsList();
         final DiffAnalyser diffAnalyser = new DiffAnalyser(visibleItems);
 
         Adapter adapter = getAdapter();
         diffAnalyser.findDiff(adapter);
 
         startListUpdate(diffAnalyser.getSubsetOffset(), diffAnalyser.getChanges());
     }
 
     protected static class ListItemInfo extends HorizontalAbsListView.ItemInfo {
         private View mView;
         private Object mItem;
 
         @Override
         public void createItemViews(HorizontalAbsListView parent,
                 int itemIndex,
                 Adapter adapter,
                 HorizontalAbsListView.ViewCache viewCache) {
             int viewType = adapter.getItemViewType(itemIndex);
             View cachedView = viewCache.poll(viewType);
             mView = adapter.getView(itemIndex, cachedView, parent);
 
             mItem = adapter.getItem(itemIndex);
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
             parent.removeViewInLayout(mView);
         }
 
         @Override
         public void recycleItemViews(int itemIndex, Adapter adapter, HorizontalAbsListView.ViewCache viewCache) {
             if (itemIndex != -1) {
                 int viewType = adapter.getItemViewType(itemIndex);
                 viewCache.offer(viewType, mView);
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
         public void finish();
     }
 
     private class InsertStep implements UpdateStep {
         private final ItemInfo mItem;
 
         private int mPreviousWidth;
 
         public InsertStep(ItemInfo item) {
             mItem = item;
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
 
             if (mItems.indexOf(item) != -1)
                 item.layoutViews(item.getLeft(), getPaddingLeft(), getPaddingTop(), currentWidth);
 
             int delta = currentWidth - mPreviousWidth;
             mPreviousWidth = currentWidth;
             ArrayList<ItemInfo> items = mItems;
             int itemIndex = items.indexOf(item);
             if (itemIndex != -1)
                 shiftItems(itemIndex + 1, items.size() - itemIndex - 1, delta);
 
             return delta;
         }
 
         @Override
         public void finish() {
             // No action
         }
     }
 
     private class DeleteStep implements UpdateStep {
         private final ItemInfo mItem;
 
         private int mPreviousWidth;
 
         private DeleteStep(ItemInfo item) {
             mItem = item;
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
 
             if (mItems.indexOf(item) != -1)
                 item.layoutViews(item.getLeft(), getPaddingLeft(), getPaddingTop(), currentWidth);
 
             int delta = currentWidth - mPreviousWidth;
             mPreviousWidth = currentWidth;
             ArrayList<ItemInfo> items = mItems;
             int itemIndex = items.indexOf(item);
             if (itemIndex != -1)
                 shiftItems(itemIndex + 1, items.size() - itemIndex - 1, delta);
 
             return delta;
         }
 
         @Override
         public void finish() {
             ArrayList<ItemInfo> items = mItems;
             int itemIndex = items.indexOf(mItem);
             if (itemIndex != -1) {
                 items.remove(itemIndex);
                 getItemsManager().recycleItemInfo(HorizontalListView.this, -1, mItem);
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
         public void finish() {
             // No action
         }
     }
 
     private class InsertDeleteAction implements Runnable {
         private final UpdateStep[] mUpdateSteps;
         private final long mStartTime;
         private final long mDuration;
         private final int mDeletionsCount;
 
         public InsertDeleteAction(UpdateStep[] updateSteps, long duration) {
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
 
         @Override
         public void run() {
             long currentTime = System.currentTimeMillis();
             float interpolatedTime = (float) (currentTime - mStartTime) / mDuration;
             interpolatedTime = Math.max(0.0f, interpolatedTime);
             interpolatedTime = Math.min(1.0f, interpolatedTime);
 
             UpdateStep[] updateSteps = mUpdateSteps;
 
             int updateDelta = 0;
             for(UpdateStep updateStep : updateSteps)
                 updateDelta += updateStep.makeStep(interpolatedTime);
 
             if (updateDelta > 0) // If total items width is increased than remove remove invisible items on right
                 removeItemsRight(-updateDelta);
             else if (updateDelta < 0){ // If total items width is decreased than add items on right if possible
                 mFirstGlobalItemIndex -= mDeletionsCount; // Items to be deleted was already removed from adapter but
                 int deltaDiff = addItemsRight(updateDelta) - updateDelta; // they are still visible and corresponding
                 mFirstGlobalItemIndex += mDeletionsCount;                 // ItemInfo objects are in mItems list. So
                                                       // correction for adapter is necessary when items are added right.
 
                 if (deltaDiff > 0) { // If visible part of list is at the end and if a visible item is removed
                     int newDeltaDiff = addItemsLeft(-deltaDiff); // than do left side correction to adjust visible
                     shiftItems(-newDeltaDiff);                   // items on right side
                 }
             }
 
             if (interpolatedTime == 1.0f)
                 for(UpdateStep updateStep : updateSteps)
                     updateStep.finish();
             else
                 post(this);
             invalidate();
         }
     }
 }
