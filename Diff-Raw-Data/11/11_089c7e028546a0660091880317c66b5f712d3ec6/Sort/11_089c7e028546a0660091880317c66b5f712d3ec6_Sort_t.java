package algorithms;
 
 public class Sort {
     public static void main (String[] args) {
         System.out.println ("Test merge sort");
         mergeSort(new int[]{8,2,5,3,9,3,6,4,9,7,0,7,4,5,3,2,6,7,3,8,2,9});
         System.out.println ();
         
         System.out.println ("Test insertion sort");
         insertionSort(new int[]{8,2,5,3,9,3,6,4,9,7,0,7,4,5,3,2,6,7,3,8,2,9});
         System.out.println ();
         
         System.out.println ("Test quick sort");
         quickSort(new int[]{8,2,5,3,9,3,6,4,9,7,0,7,4,5,3,2,6,7,3,8,2,9});
         System.out.println ();
         
         System.out.println ("Test counting sort");
         countingSort(new int[]{8,2,5,3,9,3,6,4,9,7,0,7,4,5,3,2,6,7,3,8,2,9});
     }
     
     private static void printArray (int[] nums) {
         for (int i : nums)
             System.out.print (i + " ");
     }
     
     public static void mergeSort (int[] nums) {
         mergeSort (nums, 0, nums.length);
         printArray (nums);
     }
     
     private static void mergeSort (int[] nums, int start, int end) {
         if (end-start < 2)
             return;
         int mid = (start + end) / 2;
         mergeSort (nums, start, mid);
         mergeSort (nums, mid, end);
         int[] temp = new int[end-start];
         int p1 = start, p2 = mid, counter=0;
         while (p1 < mid && p2 < end)
             if (nums[p1] < nums[p2]) {
                 temp[counter++] = nums[p1];
                 ++p1;
             } else {
                 temp[counter++] = nums[p2];
                 ++p2;
             }
         if (p1<mid)
             for (; p1<mid; ++p1)
                 temp[counter++] = nums[p1];
         if (p2<end)
             for(; p2<end; ++p2)
                 temp[counter++] = nums[p2];
         for (int i=0; i<temp.length; ++i)
             nums[start+i] = temp[i];
     }
     
     public static void insertionSort (int[] nums) {
         for (int i=1; i<nums.length; ++i)
             for (int j=i; j>0 && nums[j-1]>nums[j]; --j) {
                 int t = nums[j];
                 nums[j] = nums[j-1];
                 nums[j-1] = t;
             }
         printArray (nums);
     }
     
     public static void quickSort (int[] nums) {
         quickSort (nums, 0, nums.length);
         printArray (nums);
     }
     
     private static void quickSort (int[] nums, int start, int end) {
         if (end-start < 2)
             return;
         int pivot = partition (nums, start, end);
         quickSort (nums, start, pivot);
         quickSort (nums, pivot+1, end);
     }
     
     private static int partition (int[] nums, int start, int end) {
         int randPivot = start + (int)(Math.random()*(end-start));
         int t = nums[end-1];
         nums[end-1] = nums[randPivot];
         nums[randPivot] = t;
         
         int s=start;
         for (int i=start; i<end-1; ++i)
             if (nums[i] < nums[end-1]) {
                 t = nums[s];
                 nums[s++] = nums[i];
                 nums[i] = t;
             }
         t = nums[s];
         nums[s] = nums[end-1];
         nums[end-1] = t;
         return s;
     }
     
     public static void countingSort (int[] nums) {
         int[] counts = new int[10];
         for (int num : nums) {
             counts[num]++;
         }
         int index=0;
         for (int i=0; i<counts.length; ++i)
             for (int j=0; j<counts[i]; ++j)
                 nums[index++] = i;
         printArray(nums);
     }
             
                 
 }
