 import java.util.Arrays;
 
 /**
  * @author kchung
  */
 public class RankFind {
 	public static void main(String[] args) {
		int[] a = {10, 8, 7, 4, 5, 3, 2, 1, 6, 9};
 		int n = 4;
 		int[] rank = rankFindSmallest(a, n);
 		System.out.println(Arrays.toString(rank));
 	}
 
 	private static int[] rankFindSmallest(int[] a, int n) {
 		//copy to not mess up the original
 		int[] copy = Arrays.copyOf(a, a.length);
 
 		modifiedQuickSort(copy,0, a.length - 1, n);
 
 		//copy is now ranked
 		return Arrays.copyOf(copy, n);
 	}
 
 	private static void modifiedQuickSort(int[] a, int start, int end, int n){
 
 		if(start >= end){
 
 		} else{
 			int pivotIndex = choosePivot(a, start, end);
 
			partition(a, start, end, pivotIndex, n);
			modifiedQuickSort(a, start, pivotIndex - 1, n);
			modifiedQuickSort(a, end, pivotIndex + 1, n);
 		}
 	}
 
 	private static int choosePivot(int[] a, int start, int end) {
 		//simple pivot chosing logic - choose median
 		return (start + end) / 2;
 	}
 
	private static void partition(int[] a, int start, int end, int pivotIndex, int n) {
 		//swap pivot and end
 		swap(a, pivotIndex, end);
 
 		int firstHigh = start;
 		int pivot = a[end];
 		for (int i = start; i < end; i++){
			//can stop now since we've ranked upto n
			//just break since we need to swap pivot and first high b ack
			if(firstHigh >= n){
				break;
			}
 			if (a[i] < pivot){
 				swap(a, firstHigh, i);
 				firstHigh++;
 			}
 		}
 
 		//swap firstHigh and end
 		swap(a, firstHigh, end);
 	}
 
 	/**
 	 * Swap elements on index i and j
 	 * @param a
 	 * @param i
 	 * @param j
 	 */
 	private static void swap(int[] a, int i, int j) {
 		int temp = a[i];
 		a[i] = a[j];
 		a[j] = temp;
 	}
 }
