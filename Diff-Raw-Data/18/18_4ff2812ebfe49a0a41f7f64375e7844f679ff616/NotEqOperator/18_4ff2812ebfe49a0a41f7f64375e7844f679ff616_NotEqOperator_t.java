 package edu.tum.lua.operator.relational;
 
/**
 * The NotEqual operator is the exact opposite of the Equal operator
 */
 public class NotEqOperator {
	private final static EqOperator eqOperator = new EqOperator();

	public boolean apply(Object o1, Object o2) {
 		return !eqOperator.apply(o1, o2);
 	}
 }
