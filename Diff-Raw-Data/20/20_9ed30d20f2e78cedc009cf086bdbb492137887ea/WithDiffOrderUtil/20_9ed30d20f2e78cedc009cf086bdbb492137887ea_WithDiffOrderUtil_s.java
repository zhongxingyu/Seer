 /* (c) Copyright by Man YUAN */
 package net.epsilony.tb.analysis;
 
 import gnu.trove.list.array.TDoubleArrayList;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class WithDiffOrderUtil {
 
     public static int outputLength(int dim, int diffOrder) {
         switch (dim) {
             case 1:
                return diffOrder;
             case 2:
                 return outputLength2D(diffOrder);
             default:
                 throw new IllegalArgumentException("The dim should be 1 or 2, not " + dim);
         }
     }
 
     public static int outputLength2D(int diffOrder) {
         return (diffOrder + 2) * (diffOrder + 1) / 2;
     }
 
     public static TDoubleArrayList[] initOutput(TDoubleArrayList[] toInit, int capacity, int dim, int diffOrder) {
         int size = outputLength(dim, diffOrder);
         TDoubleArrayList[] result;
         if (null == toInit) {
             result = new TDoubleArrayList[size];
             for (int i = 0; i < result.length; i++) {
                 if (capacity > 0) {
                     result[i] = new TDoubleArrayList(capacity);
                 }
             }
         } else {
             result = toInit;
             for (int i = 0; i < result.length; i++) {
                 result[i].resetQuick();
                 if (capacity > 0) {
                     result[i].ensureCapacity(capacity);
                 }
             }
         }
         return result;
     }
 }
