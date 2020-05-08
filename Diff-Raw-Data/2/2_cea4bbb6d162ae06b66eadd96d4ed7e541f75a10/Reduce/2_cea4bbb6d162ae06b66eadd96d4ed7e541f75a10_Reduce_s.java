 package ar.com.jolisper.enumerable.core;
 
 import java.util.List;
 
 public abstract class Reduce<ResultType, CollectionType> {
 	
	public Object reduce(ResultType initValue, List<? extends CollectionType> collection) {
 		
 		ResultType result = initValue;
 		
 		for ( CollectionType element : collection ) {
 			result = logic( result , element );
 		}
 		
 		return result;
 	}
 
 	protected abstract ResultType logic(ResultType result, CollectionType element);
 
 }
