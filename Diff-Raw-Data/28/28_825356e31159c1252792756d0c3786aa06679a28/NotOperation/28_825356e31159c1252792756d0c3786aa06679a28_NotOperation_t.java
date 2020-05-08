 /*
  *   NotOperation.java
  * 	 @Author Oleg Gorobets
  *   Created: 24.07.2007
  *   CVS-ID: $Id: 
  *************************************************************************/
 
 package org.swfparser.operation;
 
 import java.util.Stack;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.log4j.Logger;
 
 import org.swfparser.BooleanOperation;
 import org.swfparser.Operation;
 import org.swfparser.Priority;
 
 public class NotOperation extends UnaryOperation implements BooleanOperation {
 
 	private static Logger logger = Logger.getLogger(NotOperation.class);
 	
 	public NotOperation(Operation op) {
 		super(op);
 	}
 	
 //	public static Operation createNotOperation(Operation op) {
 //		if (op instanceof NotOperation) {
 //			return ((NotOperation)op).getOp();
 //		} else {
 //			return new NotOperation(op);
 //		}
 //	}
 	
 	
 	public NotOperation(Stack<Operation> stack) {
 		super(stack);
 		logger.debug("#Not "+op);
 	}
 	
 //	public static Operation createNotOperation(Stack<Operation> stack) {
 //		Operation op = stack.pop();
 //		if (op instanceof NotOperation) {
 //			return ((NotOperation)op).getOp();
 //		} else {
 //			return new NotOperation(op);
 //		}
 //	}
 
 	public int getArgsNumber() {
 		return 1;
 	}
 
 	public String getStringValue(int level) {
        if (op instanceof BooleanOperation) {
            // Don't make `!b && !c)` out of `!(b || c)`.
            // Don't use And/OrOperation for getInvertedOperation().
            if (op instanceof AndOperation || op instanceof OrOperation) {
                return getString(level);
            } else {
			    return ((BooleanOperation)op).getInvertedOperation().getStringValue(level);
            }
 		} else {
            return getString(level);
        }
 //		return (op instanceof NotOperation) ? 
 //				((NotOperation)op).getInvertedStringValue(level):
 //				"!"+op.getStringValue(level);
 	}
 
    private String getString(int level) {
        if (op.getPriority() > getPriority()) {
            return "!("+op.getStringValue(level)+")";
        } else {
            return "!"+op.getStringValue(level);
        }
    }

 //	public String getInvertedStringValue(int level) {
 //		
 //		if (op instanceof BooleanOperation) {
 //			return ((BooleanOperation)op).getStringValue(level);
 //		} else {
 //			return op.getStringValue(level);
 //		}
 //		
 //	}
 	
 	public Operation getInvertedOperation() {
 		return op;
 	}	
 	
 	
 	@Override
 	public String toString() {
 		return "NotOperation("+op+")";
 	}
 	
 	@Override
 	public int getPriority() {
 		return isDoubleNot()  
 				// return priority of underlying op
 				? getRealOperation().getPriority()
 				
 				: Priority.UNARY;
 	}
 
 	
 	@Override
 	public boolean equals(Object obj) {
 		logger.debug("Testing EQ: Not "+op+" vs. "+obj);
 		if (obj == this) {
 			return true;
 		}
 		
 		if ((obj instanceof NotOperation)) {
 			NotOperation otherOp = (NotOperation) obj;
 //			logger.debug("Eq "+this.op+" and "+op.op);
 			return new EqualsBuilder()
 				.append(this.op, otherOp.op)
 				.isEquals();
 		} else {
 			Operation myOp = op;
 			int count = 1;
 			while (myOp instanceof NotOperation) {
 				myOp = ((NotOperation)myOp).getOp();
 				count++;
 			}
 			logger.debug("Real operation behind Not(): "+myOp+". Not count = "+count);
 			return (obj.equals(myOp) && (count%2==0));
 		}
 		
 	}
 	
 	protected boolean isDoubleNot() {
 		Operation myOp = op;
 		int count = 1;
 		while (myOp instanceof NotOperation) {
 			myOp = ((NotOperation)myOp).getOp();
 			count++;
 		}
 		return count%2==0;
 	}
 	
 	protected Operation getRealOperation() {
 		Operation myOp = op;
 		int count = 1;
 		while (myOp instanceof NotOperation) {
 			myOp = ((NotOperation)myOp).getOp();
 			count++;
 		}
 		return myOp;
 	}
 	
 //	protected boolean eq {
 //		Operation myOp = op;
 //		int count = 1;
 //		while (myOp instanceof NotOperation) {
 //			myOp = ((NotOperation)myOp).getOp();
 //			count++;
 //		}
 //		if (count%2 == 0) {
 //			
 //		} else {
 //			
 //		}
 //	}
 
 }
