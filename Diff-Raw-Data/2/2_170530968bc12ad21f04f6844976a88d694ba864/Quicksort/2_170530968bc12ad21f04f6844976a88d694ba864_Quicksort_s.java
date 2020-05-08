 package net.codehobby.quicksort;
 
 import java.util.List;
 
 /**
  * Quicksort is an implementation of the quicksort algorithm. 
  * It moves items in-place to save space. It picks the pivot point by taking the first, middle and last item and uses the median value.
  * 
  * @author Jeff Crone
  *
  */
 public class Quicksort {
 
 	/**
 	 * getMedianIndex takes the median item of the items at firstIndex, secondIndex and thirdIndex or the item at thirdIndex if it doesn't find the median value.
 	 * 
 	 * @author Jeff Crone
 	 * 
 	 * @param lst The list of items.
 	 * @param firstIndex The first index to consider.
 	 * @param secondIndex The second index to consider.
 	 * @param thirdIndex The third index to consider.
 	 * @return The index of the median item.
 	 */
 	private static <T extends Comparable<T>> int getMedianIndex( List<T> lst, int firstIndex, int secondIndex, int thirdIndex )
 	{//Returns the index of the median value of the values at the three indexes or thirdIndex.
 		if( (lst.get(firstIndex).compareTo(lst.get(secondIndex)) <= 0) && (lst.get(firstIndex).compareTo(lst.get(thirdIndex)) >= 0) )
 		{//The object at firstIndex is a median of the three because it's less than or equal to the object at secondIndex and greater than or equal to the object at thirdIndex. Since it counts as a median, return it.
 			return firstIndex;
 		}
 		else if( (lst.get(firstIndex).compareTo(lst.get(secondIndex)) >= 0) && (lst.get(firstIndex).compareTo(lst.get(thirdIndex)) <= 0) )
 		{//The object at firstIndex is a median of the three because it's greater than or equal to the object at secondIndex and less than or equal to the object at thirdIndex. Since it counts as a median, return it.
 			return firstIndex;
 		}
 		else if( (lst.get(secondIndex).compareTo(lst.get(firstIndex)) <= 0) && (lst.get(secondIndex).compareTo(lst.get(thirdIndex)) >= 0) )
 		{//The object at secondIndex is a median of the three because it's less than or equal to the object at firstIndex and greater than or equal to the object at thirdIndex. Since it counts as a median, return it.
 			return secondIndex;
 		}
 		else if( (lst.get(secondIndex).compareTo(lst.get(firstIndex)) >= 0) && (lst.get(secondIndex).compareTo(lst.get(thirdIndex)) <= 0) )
 		{//The object at secondIndex is a median of the three because it's greater than or equal to the object at firstIndex and less than or equal to the object at thirdIndex. Since it counts as a median, return it.
 			return secondIndex;
 		}
 		else
 		{//thirdIndex is probably the median if execution is here, but even if it's not the method needs to return something so might as well be thirdIndex.
 			return thirdIndex;
 		}
 	}
 	
 	/**
 	 * Swaps the items at firstIndex and secondIndex.
 	 * 
 	 * @author Jeff Crone
 	 * 
 	 * @param lst The list of items.
 	 * @param firstIndex The index of the first item to swap.
 	 * @param secondIndex The index of the second item to swap.
 	 */
 	private static <T extends Comparable<T>> void swap( List<T> lst, int firstIndex, int secondIndex )
 	{//Swap items at first and second index.
 		T tempVal = lst.get(firstIndex);
 		lst.set(firstIndex, lst.get(secondIndex) );
 		lst.set( secondIndex, tempVal );
 	}
 	
 	/**
 	 * partition does an in-place sorting into 2 partitions; items less than the partition and items equal to or greater than the partition.
 	 * 
 	 * @author Jeff Crone
 	 * 
 	 * @param lst The list of items to partition.
 	 * @param leftIndex The index on the left edge of the items to partition.
 	 * @param rightIndex The index on the right edge of the items to partition.
 	 * @param pivotIndex The index of the pivot point.
 	 * @return The new index for he pivot point.
 	 */
 	private static <T extends Comparable<T>> int partition( List<T> lst, int leftIndex, int rightIndex, int pivotIndex )
 	{//Sort from left to right around pivot.
 		int newIndex = leftIndex;
 		T pivVal = lst.get(pivotIndex);
 		Quicksort.swap(lst, pivotIndex, rightIndex );
 		
 		for( int i = leftIndex; i < rightIndex; i++ )
 		{
 			if( lst.get(i).compareTo(pivVal) < 0 )
 			{//If the element at index i is less than the pivot value, it should be before the pivot, so switch it.
 				Quicksort.swap( lst, i, newIndex );
 				newIndex++;
 			}
 		}
 		
 		//Move pivot from location right to the location it should end up.
 		Quicksort.swap( lst, newIndex, rightIndex );
 		
 		return newIndex;
 	}
 	
 	/**
 	 * The main quicksort method. 
 	 * It gets the pivot point and uses that to partition the items. It then does a recursive call to sort the partitions.
 	 * 
 	 * @author Jeff Crone
 	 * 
 	 * @param items The list of items to sort.
 	 * @param left The index of the leftmost item to sort.
 	 * @param right The index of the rightmost item to sort.
 	 */
 	public static <T extends Comparable<T>> void quicksort( List<T> items, int left, int right )
 	{//Main quicksort function. Call this to sort a list of items.
 		int pivotPoint, newPivotPoint;
 		if( left < right )
 		{//If the list has at least 2 more items, keep running quicksort.
 			pivotPoint = getMedianIndex( items, left, (left+right)/2, right );//Get the median of the left, middle and right.
 			newPivotPoint = partition( items, left, right, pivotPoint );
 			quicksort( items, left, newPivotPoint - 1 );
 			quicksort( items, newPivotPoint + 1, right );
 		}
 	}
 
 	/**
 	 * The main quicksort method without the left and right parameters. It just has the list parameter to provide an easier method to use for other code. 
 	 * 
 	 * @author Jeff Crone
 	 * 
 	 * @param items The list of items to sort.
 	 */
 	public static <T extends Comparable<T>> void quicksort( List<T> items )
 	{
 		//Just call the other quicksort method with programatically generated left and right parameters.
		return quicksort( items, 0, items,size()-1 );
 	}
 }
