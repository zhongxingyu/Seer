 package edu.tum.lua.operator.relational;
 

 public class NotEqOperator {
/* The NotEqual operator is the exact opposite of the Equal operator */
	
	private EqOperator eqOperator;
	
	public boolean apply(Object o1, Object o2) throws NoSuchMethodException {
		
 		return !eqOperator.apply(o1, o2);
		
 	}

 }
