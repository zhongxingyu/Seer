 package org.webik.storm.StormUnit.trident.mock;
 
 import java.util.List;
 import java.util.Stack;
 
import clojure.stacktrace__init;
 import storm.trident.operation.TridentCollector;
 
 public class MockStackCollector implements TridentCollector {
 
 	protected Stack<List<Object>> tuplesStack = new Stack<List<Object>>();
 	
 	public void emit(List<Object> values) {
 		tuplesStack.push( values );
 	}
 
 	public void reportError(Throwable t) {
		// TODO Auto-generated method stub
 	}
 	
 	public boolean assertTuple( List<Object> expectedTuple ){
 		if( tuplesStack.isEmpty() ){
 			return false;
 		}
 		return tuplesStack.pop().equals( expectedTuple );
 	}
 	
 
 }
