 package com.jackmaney.IntroductionToAlgorithms.search;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 
 import com.jackmaney.IntroductionToAlgorithms.sort.MergeSort;
 
 public class SumSearch {
 
 	public static ArrayList<Integer> search(AbstractList<Integer> list,int x){
 		
 		if(list.isEmpty()){
 			throw new IllegalArgumentException();
 		}
 		
 		ArrayList<Integer> result = null;
 		
 		MergeSort.sort(list);
 		
 		for(int i = 0; i < list.size(); i++){
 			
 			int y = list.get(i);
 			int index = BinarySearch.search(list, new Integer(x - y));
 			
			if(index > 0 && index != i){
 				result = new ArrayList<>();
 				result.add(y);
 				result.add(list.get(index));
 				break;
 			}
 			
 		}
 		
 		return result;
 	}
 	
 }
