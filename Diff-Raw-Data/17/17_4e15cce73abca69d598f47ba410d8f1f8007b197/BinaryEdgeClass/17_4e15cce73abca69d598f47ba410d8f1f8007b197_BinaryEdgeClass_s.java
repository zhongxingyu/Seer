 package de.uni_koblenz.jgralab.schema;
 
 
 public interface BinaryEdgeClass extends EdgeClass {
 	
 	public static final String DEFAULTBINARYEDGECLASS_NAME = "BinaryEdge";
 	
 	
 	public IncidenceClass getToIncidenceClass();
 	
 	public IncidenceClass getFromIncidenceClass();
 	
 
 }
