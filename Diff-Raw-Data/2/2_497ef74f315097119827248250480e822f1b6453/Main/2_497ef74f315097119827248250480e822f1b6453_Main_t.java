 /**
  * 
  */
 package com.agois.javatest;
 
 import java.util.Arrays;
 
 /**
  * @author agois
  *
  */
 public class Main {
 
     public static  class Singleton{
         private static volatile Singleton _instance;
 
        private Singleton() {}
        
         public static Singleton getInstance(){
 
            if(_instance == null){
                     synchronized(Singleton.class){
                       if(_instance == null)
                       _instance = new Singleton();
                     }
 
            }
            return _instance;
 
         }
     }
     
     /**
      * @param args
      */
     public static void main(String[] args) {
         int[] arr = {1, 22, 44, -1, -3, -5, 0, 9, 8, 7, 50, 100, -1, 0, 0};
         
         findMaxContiguousRangeWithMaxProduct(arr);
         
         findThreeNumbersSumZero(arr);
         
         int[] arr2 = {99, 22, 44, 1, 45, 5, 76, 87, 33, 55, 99};
         findBestBuySellDays(arr2);
         
         findClosesPerfectSquare(170);
         
         findTripletInOrderofPositionAndValue(arr2);
     }
 
     private static void findTripletInOrderofPositionAndValue(int[] arr) {
         if (arr.length < 3) return;
         int[] min = new int[arr.length];
         min[0] = arr[0];
         for (int i = 1; i < arr.length; i++) {
             if (arr[i] < min[i-1]) {
                 min[i] = arr[i];
             } else {
                 min[i] = min[i-1];
             }
         }
         int[] max = new int[arr.length];
         max[arr.length-1] = arr[arr.length-1];
         for (int i = arr.length-2; i >= 0; i--) {
             if (arr[i] > max[i+1]) {
                 max[i] = arr[i];
             } else {
                 max[i] = max[i+1];
             }
         }
         for (int i = 0; i < arr.length; i++) {
             if ((min[i] < arr[i]) && (arr[i] < max[i])) {
                 System.out.println("Found a="+min[i]+" b="+arr[i]+" c="+max[i]);
             }
         }
     }
 
     private static void findClosesPerfectSquare(int i) {
         int prev = (int) Math.floor(Math.sqrt(i));
         int next = prev + 1;
         int sqrPrev = prev*prev;
         int sqrNext = next*next;
         if (i - sqrPrev > sqrNext - i) {
             System.out.println("square = "+sqrNext);
         } else {
             System.out.println("square = "+sqrPrev);
         }
     }
 
     // Traverse the array and keep track of the min so far and the difference
     // for each element. When find a new min, update idxMin, when find a new
     // best profit, update idxMax
     private static int findBestBuySellDays(int[] a) {
            if(a.length==0)return 0;
            int min=a[0];
            int best=0;
            int idxMin = 0;
            int idxMax = 0;
            for(int i=1;i<a.length;i++)
            {
                int tmp = a[i]-min;
                if (tmp > best) {
                    idxMax = i;
                }
                best = Math.max(best,tmp);
                if(a[i]<min) {
                    min=a[i];
                    idxMin = i;
                }
            }
         System.out.println("Best buying date is "+idxMin);
         System.out.println("Best selling date is "+idxMax);
         System.out.println("Best profit is "+best);
         return best;
     }
 
     private static void findThreeNumbersSumZero(int[] arr) {
         Arrays.sort(arr);
         int j = 1;
         int k = arr.length - 1;
         outer:
         for (int i = 0; i < arr.length - 2; i++) {
             loop2:
             for (j = i+1; j < k; j++) {
                 for (k =arr.length-1; k > j; --k) {
                     int sum = arr[i] + arr[j] + arr[k];
                     if (sum == 0) {
                         System.out.println("found indexes "+i+" "+j+" "+k);
                         System.out.println("found values "+arr[i]+" "+arr[j]+" "+arr[k]);
                         break outer;
                     } else if (sum < 0) {
                         break loop2;
                     }
                 }
             }
         }
     }
 
     private static void findMaxContiguousRangeWithMaxProduct(int[] arr) {
         int windowSize = arr.length;
         int product = 1;
         int initialPos = 0;
         while ((initialPos + windowSize) <= arr.length) {
             product = 1;
             for (int i = initialPos; i < (initialPos + windowSize); i++) {
                 product *= arr[i];
                 if (product == 0) {
                     break;
                 }
             }
             if (product <= 0) {
                 if ((initialPos + windowSize) < arr.length) {
                     initialPos++;
                 } else {
                     initialPos = 0;
                     windowSize--;
                 }
             } else {
                 break;
             }
         }
         System.out.println("max product = "+product);
         System.out.println("max range = ["+initialPos+","+(initialPos+windowSize)+"]");
     }
 
 }
