 package org.webik.storm.StormUnit.trident.mock;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import storm.trident.operation.TridentCollector;
 
 public class MockCollector implements TridentCollector{
 
 	protected LinkedList<List<Object>> tuplesList = new LinkedList<List<Object>>();
 	
 	public void emit( List<Object> values ) {
 		tuplesList.add( values );
 		
 	}
 
 	public void reportError(Throwable t) {
		// TODO Auto-generated method stub
		
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
 
 }
