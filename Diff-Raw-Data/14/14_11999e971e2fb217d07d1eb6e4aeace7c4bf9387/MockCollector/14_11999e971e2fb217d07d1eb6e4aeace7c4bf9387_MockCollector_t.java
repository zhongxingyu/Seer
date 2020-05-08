 package org.webik.storm.StormUnit.trident.mock;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import storm.trident.operation.TridentCollector;
 
 public class MockCollector implements TridentCollector{
 
 	protected LinkedList<List<Object>> tuplesList = new LinkedList<List<Object>>();
 	
	protected Throwable lastError = null;
	
 	public void emit( List<Object> values ) {
 		tuplesList.add( values );
 		
 	}
 
 	public void reportError(Throwable t) {
		lastError = t;
 	}
 	
 	public List<Object> getEmmitedTuple( int offset ){
 		return tuplesList.get( offset );
 	}
 	
 	public List<Object> getEmmitedTuple(){
 		return tuplesList.getLast();
 	}
 	
 	public boolean assertTuplesListSize( int expectedSize ){
 		return tuplesList.size() == expectedSize;
 	}
 	
 	public boolean assertTuple( List<Object> expectedTuple ){
 		return getEmmitedTuple().equals( expectedTuple );
 	}
 	
 	public boolean assertTuple( int offset, List<Object> expectedTuple ){
 		return getEmmitedTuple(offset).equals( expectedTuple );
 	}
 
	public boolean assertError(){
		return lastError != null;
	}
	
	public Throwable getLastError(){
		return lastError;
	}
 }
