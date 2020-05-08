 package cz.cuni.mff.d3s.spl.core.impl.formula;
 
 import cz.cuni.mff.d3s.spl.core.Formula;
 import cz.cuni.mff.d3s.spl.core.Result;
 
 public class LogicOr extends LogicOp {
 
 	public LogicOr(Formula left, Formula right) {
 		super(left, right);
 	}
 
 	/*
 	 * We are using Kleene three-value logic.
 	 */
 	@Override
 	public Result evaluate() {
 		Result leftResult = left.evaluate();
 		
 		/*
 		 * If the left one is TRUE, we do not need
 		 * to evaluate the right one.
 		 */
 		if (leftResult == Result.TRUE) {
 			return Result.TRUE;
 		}
 		
 		Result rightResult = right.evaluate();
 		
 		/*
 		 * The same works other way round.
 		 */
 		if (rightResult == Result.TRUE) {
 			return Result.TRUE;
 		}
 		
 		/*
 		 * FALSE is returned only if both are false, otherwise
 		 * we return CANNOT_COMPUTE.
 		 */
		if ((rightResult == Result.FALSE) || (leftResult == Result.FALSE)) {
 			return Result.FALSE;
 		} else {
 			return Result.CANNOT_COMPUTE;
 		}
 	}
 
 }
