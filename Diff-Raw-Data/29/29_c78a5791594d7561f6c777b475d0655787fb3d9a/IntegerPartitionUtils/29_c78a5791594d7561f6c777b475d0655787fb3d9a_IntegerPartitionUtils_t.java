 package com.max.algs.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
import java.util.ListIterator;
 
 public final class IntegerPartitionUtils {
 	
 	
 	private IntegerPartitionUtils(){
 		super();
 	}
 	
 	
 	/**
 	 * 
 	 * Generate partitions of integer value.
 	 * See: https://en.wikipedia.org/wiki/Partition_(number_theory)
 	 * 
 	 */
 	@SuppressWarnings("serial")
 	public static List<List<Integer>> generatePartitions( final int value ){
 		
 		List<List<Integer>> partitions = new ArrayList<>();
 		
 		partitions.add( new ArrayList<Integer>(){{ add(value); }});
 		
 		while( true ){
 			
 			List<Integer> lastPartition = partitions.get( partitions.size()-1 );
 			
 			if( allOnes(lastPartition) ){
 				break;
 			}
 			
 			List<Integer> newPartition = new ArrayList<>();
 			
 			int lastBigElemIndex = -1;
 			
 			for ( int i = lastPartition.size()-1; i >=0; i-- ){
 				
 				int elem = lastPartition.get(i);
 				
 				if( elem > 1 ){
 					lastBigElemIndex = i;
 					break;
 				}
 				
 				newPartition.add( elem );
 			}
 			
 			int elemToSplit = lastPartition.get(lastBigElemIndex);
 			
 			newPartition.add(1);
 			newPartition.add(elemToSplit-1);
 			
 			for( int i = lastBigElemIndex-1; i >=0; i-- ){
 				newPartition.add( lastPartition.get(i) );
 			}			
 
 			Collections.reverse( newPartition );
 			
 			if( elemToSplit-1 > 1 ){
				normalize(newPartition, elemToSplit-1);
 			}
 						
 //			System.out.println( "last = " + lastPartition + ", new = " + newPartition );
 			
 			partitions.add( newPartition );			
 			
 		}
 		
 		return partitions; 
 	}
 	
	private static void normalize( List<Integer> data, int baseValue ){
 		
 		int oneIndex = -1;
 		
 		for( int i = 0; i < data.size(); i++ ){
 			
 			if( data.get(i) == 1 ){				
 				oneIndex = i;
 				break;
 			}
 		}
 		
		int reduced = 0 ; 
		
//		ListIterator<Integer> it = data.li
		
		for( int i = oneIndex; i < data.size(); i++ ){
			
			if( reduced == baseValue ){
				data.set(i-1, reduced);
				reduced = 0;				
			}
			
			reduced += data.get(i);
 			data.set(i, 0);
 		}
 		
		if( reduced > 0 ){
			data.add(reduced);
		}		

 		Iterator<Integer> it = data.iterator();
 		
 		while( it.hasNext() ){
 			int elemVal = it.next();
 			
 			if( elemVal == 0 ){
 				it.remove();
 			}
 		}
		
 	}
 
 	private static boolean allOnes( List<Integer> elems ){
 		
 		for( int val : elems ){
 			if( val != 1 ){
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 
 }
