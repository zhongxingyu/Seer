 package alex.algorithms.arrays;
 
 import java.util.Arrays;
 
 public class FourNumberWithSum {
 	//O(n^2*log(n))
 	public static void findFourElements(int array[], int sum) {
 		int n = array.length;
 		Pair T[] = new Pair[(n * (n - 1)) / 2];
 		int k = 0;
 		for (int i = 0; i < n; i++) {
 			for (int j = i + 1; j < n; j++) {
 				T[k++] = new Pair(i, j, array[i] + array[j]);
 			}
 		}
 		Arrays.sort(T);
 		int i = 0;
 		int j = T.length - 1;
 		while (i < T.length && j >= 0) {
 			if (sum == (T[i].sum + T[j].sum) && T[i].noCommonElement(T[j])) {
 				System.out.printf("%d, %d, %d, %d\n", array[T[i].first],
 						array[T[i].second], array[T[j].first],
 						array[T[j].second]);
 				return;
 			}
 			else if( sum < T[i].sum + T[j].sum){
 				j--;
 			}else{
 				i++;
 			}
 		}
 
 	}
	//O(n^3)
 	public static void find4Numbers(int [] array, int X){
 		int n = array.length;
 		Arrays.sort(array);
 		for(int i=0; i < n -3; i++ ){
 			for(int j=i+1; j < n-2; j++){
 				int left = j+1;
 				int right = n-1;
 				while(left < right){
 					int sum = array[i] + array[j] + array[left] + array[right];
 					if(X == sum){
 						System.out.printf("%d, %d, %d, %d\n",array[i], array[j], array[left], array[right]);
 						return ;
 					}else if( X < sum){
 						right--;
 					}else{
 						left++;
 					}
 				}
 			}
 		}
 	}
 	public static void main(String[] args) {
 		int array[] = {10, 20, 30, 40, 1, 2};
 		findFourElements(array, 91);
 		find4Numbers(array, 91);
 	}
 
 }
 
 class Pair implements Comparable<Pair> {
 	int first;
 	int second;
 	int sum;
 
 	public Pair(int first, int second, int sum) {
 		super();
 		this.first = first;
 		this.second = second;
 		this.sum = sum;
 	}
 
 	@Override
 	public int compareTo(Pair o) {
 		return this.sum - o.sum;
 	}
 
 	public boolean noCommonElement(Pair b) {
 		if (first == b.first || first == b.second || second == b.first
 				|| second == b.second)
 			return false;
 		return true;
 	}
 }
