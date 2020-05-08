 package de.uni_leipzig.simba.saim.core.metric;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /** Metric operations allow to combine metric values. They include the operators
  * MIN, MAX, ADD and MULT, e.g. as follows:
 <pre><code>MAX(trigrams(x.rdfs:label,y.dc:title),euclidean(x.lat|long,y.latitude|longitude))</code></pre>.
 Boolean operations allow to combine and filter the results of metric operations and include
 AND, OR, DIFF, e.g. as follows:
 <pre><code>AND(trigrams(x.rdfs:label,y.dc:title)|0.9,euclidean(x.lat|x.long,y.latitude|y.longitude)|0.7)</code></pre>.
 
  * An operator like add,and,or,min,max. A function from [0,1]^2 to [0,1]. Logical operators convert input values to booleans via mandatory thresholds.*/
 public class Operator extends Node
 {
 	public Operator(String id) {super(id);}
 	public static final Set<String> identifiers =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {"min","max","add","mult","and","or","diff","xor", "minus"})));
 	@Override public Set<String> identifiers()	{return identifiers;}	
 	@Override public byte getMaxChilds() {return 2;}
 	@SuppressWarnings("unchecked")
 	static public final Set<Class<? extends Node>> validChildClasses =
 	Collections.unmodifiableSet(new HashSet<Class<? extends Node>>(Arrays.asList((Class<? extends Node>[])new Class[] {Measure.class,Operator.class})));		
 	@Override public Set<Class<? extends Node>> validChildClasses() {return validChildClasses;}	
 }
