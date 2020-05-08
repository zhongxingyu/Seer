 package com.github.dimedriller.listview.diff;
 
 import android.widget.Adapter;
 
 import java.util.ArrayList;
 
 /**
  ***********************************************************************************************************************
  * This class was designed to search for differences between subset of items represented by static list and any other
  * items set
  ***********************************************************************************************************************
  */
 public class DiffAnalyser {
     private final Object[] mBaseList;
 
     private int mSubsetOffset;
     private final ArrayList<DiffAtom> mChanges;
 
     /**
      *******************************************************************************************************************
      * Creates instance of analyser for specific items subset
      * @param baseList - items subset
      *******************************************************************************************************************
      */
     public DiffAnalyser(Object[] baseList) {
         mBaseList = baseList;
         mSubsetOffset = -1;
         mChanges = new ArrayList<DiffAtom>();
     }
 
     /**
      *******************************************************************************************************************
      * @return offset in set analysed where subsequence is found. If -1 is returned than no subsequence was found
      *******************************************************************************************************************
      */
     public int getSubsetOffset() {
         return mSubsetOffset;
     }
 
     /**
      *******************************************************************************************************************
      * @return changes list withing subsequence
      *******************************************************************************************************************
      */
     public DiffAtom[] getChanges() {
         return mChanges.toArray(new DiffAtom[mChanges.size()]);
     }
 
     private int findBaseListItemIndex(int startIndex, Object item) {
         Object[] baseList = mBaseList;
         int itemsCount = baseList.length;
         for(int counterItem = startIndex; counterItem < itemsCount; counterItem++)
             if (baseList[counterItem].equals(item))
                 return counterItem;
         return -1;
     }
 
     /**
      *******************************************************************************************************************
      * Looks for subset in {@code adapter} set.
      * @param adapter - adapter where items are found
      * @return array of first adapter indices corresponding found subsets or empty list if no items from the subset were
      * found in {@code adapter}
      *******************************************************************************************************************
      */
     private int[] findBaseListPositionInAdapter(Adapter adapter) {
         ArrayList<Integer> adapterPositionsMax = new ArrayList<Integer>();
         int metricsMax = 0;
 
         int adapterItemsCount = adapter.getCount();
         int baseListItemsCount = mBaseList.length;
         for(int counterAdapterItem = 0; counterAdapterItem < adapterItemsCount; counterAdapterItem++) {
             Object adapterItem = adapter.getItem(counterAdapterItem);
             int baseListItemIndex = findBaseListItemIndex(0, adapterItem);
             if (baseListItemIndex == -1)
                 continue;
 
             int metricsCurrent = 1;
             int counterBaseListItem = baseListItemIndex + 1;
             int counterAdapterSubsetItem = counterAdapterItem + 1;
             while (  counterBaseListItem < baseListItemsCount
                   && counterAdapterSubsetItem - counterAdapterItem < baseListItemsCount
                   && counterAdapterSubsetItem < adapterItemsCount) {
                 adapterItem = adapter.getItem(counterAdapterSubsetItem);
                 baseListItemIndex = findBaseListItemIndex(counterBaseListItem, adapterItem);
 
                 if (baseListItemIndex != -1) {
                     counterBaseListItem = baseListItemIndex + 1;
                     metricsCurrent++;
                 }
 
                 counterAdapterSubsetItem++;
             }
 
             if (metricsCurrent > metricsMax) {
                 metricsMax = metricsCurrent;
                 adapterPositionsMax.clear();
                 adapterPositionsMax.add(counterAdapterItem);
             } else if(metricsCurrent == metricsMax)
                 adapterPositionsMax.add(counterAdapterItem);
         }
 
         int[] adapterPositionsMaxArray = new int[adapterPositionsMax.size()];
         for(int counterPosition = 0; counterPosition < adapterPositionsMaxArray.length; counterPosition++ )
             adapterPositionsMaxArray[counterPosition] = adapterPositionsMax.get(counterPosition);
         return adapterPositionsMaxArray;
     }
 
     /**
      *******************************************************************************************************************
      * Looks for differences between subset of items and corresponding item subset stored as part
      * of {@code adapter}
      * @param adapter - adapter where items are found
      *******************************************************************************************************************
      */
     public void findDiff(Adapter adapter) {
         ArrayList<DiffAtom> changesMin = mChanges;
         changesMin.clear();
         int metricsMin = Integer.MAX_VALUE;
 
         Object[] baseList = mBaseList;
         int baseListItemsCount = baseList.length;
 
         int adapterItemsCount = adapter.getCount();
         int[] adapterPositions = findBaseListPositionInAdapter(adapter);
 
         if (adapterPositions.length == 0) {
             mSubsetOffset = 0;
             for(int counterBaseList = 0; counterBaseList < baseListItemsCount; counterBaseList++)
                 changesMin.add(new DeleteDiffAtom(counterBaseList));
         } else
             for(int firstAdapterPosition : adapterPositions) {
                 ArrayList<DiffAtom> changes = new ArrayList<DiffAtom>();
 
                 Object adapterItem = adapter.getItem(firstAdapterPosition);
                 int baseListStartPivot = findBaseListItemIndex(0, adapterItem);
 
                 for(int counterBaseList = 0; counterBaseList < baseListStartPivot; counterBaseList++)
                     changes.add(new DeleteDiffAtom(counterBaseList));
 
                 int adapterStartPivot = firstAdapterPosition;
                 int adapterEndPivot = adapterStartPivot + 1;
                 int insertOffset = 0;
                 while (adapterEndPivot < adapterItemsCount) {
                     adapterItem = adapter.getItem(adapterEndPivot);
                     int baseListEndPivot = findBaseListItemIndex(baseListStartPivot + 1, adapterItem);
 
                     if (baseListEndPivot != -1) {
                         for(int counterBaseList = baseListStartPivot + 1; counterBaseList < baseListEndPivot; counterBaseList++)
                             changes.add(new DeleteDiffAtom(counterBaseList + insertOffset));
                         baseListStartPivot = baseListEndPivot;
 
                         for(int counterAdapter = adapterStartPivot + 1; counterAdapter < adapterEndPivot; counterAdapter++) {
                             int listInsertionPosition = baseListEndPivot + insertOffset;
                             changes.add(new InsertDiffAtom(listInsertionPosition, counterAdapter));
                             insertOffset++;
                         }
                         adapterStartPivot = adapterEndPivot;
                     }
 
                     adapterEndPivot++;
                 }
 
                 for(int counterBaseList = baseListStartPivot + 1; counterBaseList < baseListItemsCount; counterBaseList++)
                    changes.add(new DeleteDiffAtom(counterBaseList + insertOffset));
 
                 int changesCount = changes.size();
                 if (changesCount < metricsMin) {
                     mSubsetOffset = firstAdapterPosition;
                     metricsMin = changesCount;
                     changesMin.clear();
                     changesMin.addAll(changes);
                 }
             }
     }
 }
