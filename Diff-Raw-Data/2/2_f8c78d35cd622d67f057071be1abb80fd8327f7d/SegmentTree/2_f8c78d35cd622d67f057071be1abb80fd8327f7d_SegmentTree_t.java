 package iensen.DataStructures;
 
 import java.util.Arrays;
 
 /**
  * Created with IntelliJ IDEA.
  * User: iensen
  * Date: 12/27/12
  * Time: 10:15 AM
  * To change this template use File | Settings | File Templates.
  */
 
 public class SegmentTree
 {
 
     SegmentTreeOperation op;
     int array[];
     final int initVal;
 
     public SegmentTree(int[] initArray, SegmentTreeOperation op)
     {
         switch (op)
         {
             case SUM:
                 initVal = 0;
                 break;
             case MIN:
                 initVal = Integer.MAX_VALUE;
                 break;
             case MAX:
                 initVal = Integer.MIN_VALUE;
                 break;
             default:
                 initVal = 0;
         }
         this.op = op;
         int togetCap = initArray.length * 2;
         int curLength = 1;
         while (curLength < togetCap)
         {
             curLength *= 2;
         }
         array = new int[curLength];
         Arrays.fill(array, initVal);
         build(initArray, 1, 0, curLength / 2 - 1);
 
     }
 
     private void build(int initArray[], int segmentTreeIdx, int initArrayLIdx, int initArrayRIdx)
     {
 
             //FILL leaves:
             if (initArrayLIdx == initArrayRIdx)
             {
                 array[segmentTreeIdx] = (initArrayLIdx < initArray.length) ? initArray[initArrayLIdx] : initVal;
             } else
             {
                 int initArrayMIdx = (initArrayLIdx + initArrayRIdx) / 2;
                 //build subtrees:
                 build(initArray, segmentTreeIdx * 2, initArrayLIdx, initArrayMIdx);
                 build(initArray, segmentTreeIdx * 2 + 1, initArrayMIdx + 1, initArrayRIdx);
                 //assign value to current vertex
                 switch (op)
                 {
                     case MIN:
                         array[segmentTreeIdx] = Math.min(array[segmentTreeIdx * 2],
                                 array[segmentTreeIdx * 2 + 1]);
                         break;
                     case SUM:
                         array[segmentTreeIdx] = array[segmentTreeIdx * 2] +
                                 array[segmentTreeIdx * 2 + 1];
                         break;
                     case MAX:
                         array[segmentTreeIdx] = Math.max(array[segmentTreeIdx * 2],
                                 array[segmentTreeIdx * 2 + 1]);
                         break;
                 }
 
             }
     }
 
 
     public int query(int left, int right)
     {
         return query(1, 0, array.length / 2 - 1, left, right);
     }
 
     private int query(int segmentTreeIdx, int coverLIdx, int coverRIdx, int queryLIdx, int queryRIdx)
     {
 
         if (queryLIdx > queryRIdx)
             return initVal;
         if (coverLIdx == queryLIdx && coverRIdx == queryRIdx)
             return array[segmentTreeIdx];
         int coverMIdx = (coverLIdx + coverRIdx) / 2;
         switch (op)
         {
 
             case MIN:
                 return Math.min(query(segmentTreeIdx * 2, coverLIdx, coverMIdx, queryLIdx, Math.min(queryRIdx, coverMIdx))
                         , query(segmentTreeIdx * 2 + 1, coverMIdx + 1, coverRIdx, Math.max(queryLIdx, coverMIdx + 1), queryRIdx));
 
             case MAX:
                 return Math.max(query(segmentTreeIdx * 2, coverLIdx, coverMIdx, queryLIdx, Math.min(queryRIdx, coverMIdx))
                         , query(segmentTreeIdx * 2 + 1, coverMIdx + 1, coverRIdx, Math.max(queryLIdx, coverMIdx + 1), queryRIdx));
 
             case SUM:
                 return query(segmentTreeIdx * 2, coverLIdx, coverMIdx, queryLIdx, Math.min(queryRIdx, coverMIdx)) +
                         query(segmentTreeIdx * 2 + 1, coverMIdx + 1, coverRIdx, Math.max(queryLIdx, coverMIdx + 1), queryRIdx);
 
             default:
                 return 0;
         }
 
 
     }
 
     public void update(int index, int newvalue)
     {
         update(1, 0, array.length / 2 - 1, index, newvalue);
     }
 
     public void update(int segmentTreeIdx, int coverLIdx, int coverRIdx, int index, int new_val)
     {
         if (coverLIdx == coverRIdx)
             array[segmentTreeIdx] = new_val;
         else
         {
             int middleIndex = (coverLIdx + coverRIdx) / 2;
             if (index <= middleIndex)
                 //update left child
                update(segmentTreeIdx * 2, coverLIdx, middleIndex, index, new_val);
             else//update right child
                 update(segmentTreeIdx * 2 + 1, middleIndex + 1, coverRIdx, index, new_val);
             //update parent after updating left and right children
 
             switch (op)
             {
 
                 case MIN:
                     Math.min(array[segmentTreeIdx * 2], array[segmentTreeIdx * 2 + 1]);
                     break;
                 case MAX:
                     array[segmentTreeIdx] = Math.max(array[segmentTreeIdx * 2], array[segmentTreeIdx * 2 + 1]);
                     break;
                 case SUM:
                     array[segmentTreeIdx] = array[segmentTreeIdx * 2] + array[segmentTreeIdx * 2 + 1];
                     break;
             }
 
         }
     }
 }
