 package de.uni_koblenz.jgralab.schema;
 
import de.uni_koblenz.jgralab.BinaryEdge;

 
 public interface BinaryEdgeClass extends EdgeClass {
 	
 	public static final String DEFAULTBINARYEDGECLASS_NAME = "BinaryEdge";
 	
 	
 	public IncidenceClass getToIncidenceClass();
 	
 	public IncidenceClass getFromIncidenceClass();
 	
 
	

 }
