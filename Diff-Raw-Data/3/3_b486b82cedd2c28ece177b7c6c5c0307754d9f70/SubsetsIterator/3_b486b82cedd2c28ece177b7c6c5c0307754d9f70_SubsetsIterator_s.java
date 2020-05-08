 package com.max.algs.permutation;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
import scala.actors.threadpool.Arrays;
 
 /**
  * General purpose subsets iterator.
  * Iterate over all subsets of current set.
  * All subsets appeared in a squashed order.
  * 
  * Number of subsets from a given set:  2^N, where N - original set size.  
  * 
  * @author max
  *
  * @param <T>
  */
 public final class SubsetsIterator<T> implements Iterator<List<T>> {
 
 	
 	private final T[] originalSet;
 	private final int maxValue;
 		
 	private int curValue = 0;
 	
 	@SuppressWarnings("unchecked")
 	public SubsetsIterator( T[] set ) {
 		super();
 		if( set == null || set.length == 0 ){
 			throw new IllegalArgumentException("NULL or EMPTY 'set' passed");
 		}	
 		this.originalSet = (T[])Arrays.copyOf(set, set.length);		
 		this.maxValue = 1 << originalSet.length;
 	}
 
 	@Override
 	public boolean hasNext() {
 		return curValue < maxValue;
 	}
 
 	@Override
 	public List<T> next() {
 		
 		if( !hasNext() ){
 			throw new NoSuchElementException();
 		}
 		
 		int value = curValue;
 		int elemIndex = 0;
 		
 		final List<T> res = new ArrayList<>();
 		
 		while( value > 0 ){
 			
 			if( (value & 1) == 1){
 				res.add( originalSet[elemIndex] );
 			}
 			
 			++elemIndex;
 			value >>>= 1;
 		}
 		
 		++curValue;
 
 		return res;
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException("'SubsetsIterator' is read only");		
 	}
 	
 
 
 
 }
