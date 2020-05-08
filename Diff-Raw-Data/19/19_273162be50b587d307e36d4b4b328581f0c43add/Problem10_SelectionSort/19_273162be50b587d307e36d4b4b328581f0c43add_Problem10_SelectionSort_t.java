 
 public class Problem10_SelectionSort{
 
 	public static void Sort(Integer[] array){
 		if (array == null || array.length ==1){
 			return;
 		}
 		
 		for(int i = 0; i < array.length-1; i++){
 			int minIndex = i;
 			for(int j = i+1; j< array.length; j++){
 				if(array[j] < array[minIndex]){
 					minIndex = j;
 				}
 			}
			TestDataGenerator.Swap(array, i, minIndex);
 		}
 	
 	}
 	
 	public static void main(String args[]){
 		Integer[] temp = TestDataGenerator.GetArray( 10, true, 20);
 		TestDataGenerator.PrintArray(temp);
 		Sort(temp);
 		TestDataGenerator.PrintArray(temp);
 	}
 
 }
