 package se751.team13.quicksort;
 
import java.util.ArrayList;
 import java.util.List;
import java.util.concurrent.BrokenBarrierException;
 
 
 public class InsertionSorter<T extends Comparable<? super T>> implements Sorter<T> {
 
 	public List<T> sort(List<T> list) {
 		for (int i = 0;i<list.size();i++){
 			T valueToInsert = list.get(i);
 			int hole = i;
 			while (hole > 0 && valueToInsert.compareTo(list.get(hole - 1)) < 0){
 				list.set(hole, list.get(hole - 1));
 				hole--;
 			}
 			list.set(hole, valueToInsert);
 		}
 		return list;
 	}
 }
