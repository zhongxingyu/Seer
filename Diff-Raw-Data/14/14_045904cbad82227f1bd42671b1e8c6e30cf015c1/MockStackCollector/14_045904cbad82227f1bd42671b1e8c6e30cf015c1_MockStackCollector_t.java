 package org.webik.storm.StormUnit.trident.mock;
 
 import java.util.List;
 import java.util.Stack;
 
 import storm.trident.operation.TridentCollector;
 
 public class MockStackCollector implements TridentCollector {
 
 	protected Stack<List<Object>> tuplesStack = new Stack<List<Object>>();
	private Throwable lastError;
 	
 	public void emit(List<Object> values) {
 		tuplesStack.push( values );
 	}
 
 	public void reportError(Throwable t) {
		this.lastError = t; 
 	}
 	
 	public boolean assertTuple( List<Object> expectedTuple ){
 		if( tuplesStack.isEmpty() ){
 			return false;
 		}
 		return tuplesStack.pop().equals( expectedTuple );
 	}
 	
	public boolean assertError(){
		return lastError != null;
	}
	
	public Throwable getLastError(){
		return lastError;
	}
 
 }
