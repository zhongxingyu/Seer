 import java.util.*;
 
 public class Problem10_QuickSort{
 
 	public static void Sort(Integer[] array){
 		if (array == null || array.length ==1){
 			return ;
 		}
 		SortRec(array, 0, array.length - 1);
 	}
 	
 	private static void SortRec(Integer[] array, int p, int r){
 		if( p < r ){
 			int q = Partition(array, p, r);
 			SortRec(array, p, q-1);
 			SortRec(array, q+1, r);
 		}	
 		
 	}
 	private static int Partition(Integer[] array, int p, int r){
 		
 		int x = array[r];
 		int i = p-1;
 		for (int j = p ; j < r; j++){
 			if( array[j] < x){
 				i++;
				TestDataGenerator.Swap(array, i , j);
 			}
 		}
 		i++;
		TestDataGenerator.Swap(array, i , r);
 		return i;
 	}
 	
 	public static void main(String args[]){
 		Integer[] temp = TestDataGenerator.GetArray(10, true, 20);
 		Sort(temp);
 		TestDataGenerator.PrintArray(temp);
 	}
 
 }
