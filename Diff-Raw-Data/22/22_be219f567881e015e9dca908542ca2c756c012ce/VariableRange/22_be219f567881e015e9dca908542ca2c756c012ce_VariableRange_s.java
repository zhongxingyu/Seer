 package org.simplesql.data;
 
 /**
  * 
  * Defines the Range of data required for a Variable
  * 
  */
 public class VariableRange {
 
 	final boolean numeric;
 	Object upper = Integer.MAX_VALUE, lower = Integer.MIN_VALUE;
 	final String variable;
 
 	public VariableRange(String variable, boolean numeric) {
 		this.variable = variable;
 		this.numeric = numeric;
 	}
 
 	public VariableRange(String variable, boolean numeric, Object upper,
 			Object lower) {
 		super();
 		this.variable = variable;
 		this.numeric = numeric;
 		this.upper = upper;
 		this.lower = lower;
 	}
 
 	public void merge(VariableRange range) {
 		if (numeric && range.isNumeric()) {
 
 			Object rUpper = range.getUpper();
 			Object rLower = range.getLower();
 
 			addBiggerEq(rLower);
 			addSmallerEq(rUpper);
 
 		} else {
 			upper = range.getUpper();
 			lower = range.getLower();
 		}
 
 	}
 
 	public String getVariable() {
 		return variable;
 	}
 
 	public void setUpper(Object upper) {
 		this.upper = upper;
 	}
 
 	public void setLower(Object lower) {
 		this.lower = lower;
 	}
 
 	public boolean isNumeric() {
 		return numeric;
 	}
 
 	public Object getUpper() {
 		return upper;
 	}
 
 	public Object getLower() {
 		return lower;
 	}
 
 	public void addSmallerEq(Object val) {
 
 		if (upper instanceof Double)
 			upper = Math.min((Double) upper, ((Number) val).doubleValue());
 		else if (upper instanceof Integer)
 			upper = Math.min((Integer) upper, ((Number) val).intValue());
 		else if (upper instanceof Long)
 			upper = Math.min((Long) upper, ((Number) val).longValue());
 		else
 			// if(upper instanceof Long || val instanceof Long)
 			lower = Math.min((Long) lower, ((Number) val).longValue());
 
 	}
 
 	public void addBiggerEq(Object val) {
 
 		if (lower instanceof Double)
 			lower = Math.max((Double) lower, ((Number) val).doubleValue());
 		else if (lower instanceof Integer)
 			lower = Math.max((Integer) lower, ((Number) val).intValue());
 		else
 			lower = Math.max((Long) lower, ((Number) val).longValue());
 
 	}
 
 	public void addEq(Object val) {
 
 		if (val instanceof Number) {
 			addSmallerEq(val);
 			addBiggerEq(val);
 		} else {
 			lower = val;
 			upper = val;
 		}
 
 	}
 
 }
