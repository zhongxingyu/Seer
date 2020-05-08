 /**
  * Extends arrays by adding a reduce() member function
  * 
  */
 package org.litnak.coldfunctional.reduce;
 
 import railo.runtime.PageContext;
 import railo.runtime.exp.ExpressionException;
 import railo.runtime.exp.PageException;
 import railo.runtime.functions.BIF;
 import railo.runtime.op.Caster;
 import railo.runtime.type.Array;
 import railo.runtime.type.UDF;
 
 
 
 public class ArrayReduce extends BIF{
 
 	private static final long serialVersionUID = -3536069174087530428L;
 
 	public static Object call(PageContext pc , Array arr, UDF reducer) throws PageException {
		if (arr.size() == 0) return new ExpressionException("array provided to ArrayReduce must not be empty"); 
 		if (arr.size() == 1) return arr.getE(1);
 		Object lastValue = arr.getE(1);
 		for(int i = 2; i<= arr.size(); i++) {
 			Object nextValue = arr.getE(i);
 			lastValue = reducer.call(pc,new Object[]{lastValue,nextValue},true);
 		}
 		return lastValue;
 	}
 	
 	@Override
 	public Object invoke(PageContext pc, Object[] args) throws PageException {
 		return call(pc,Caster.toArray(args[0]),Caster.toFunction(args[1]));
 	}
 }
