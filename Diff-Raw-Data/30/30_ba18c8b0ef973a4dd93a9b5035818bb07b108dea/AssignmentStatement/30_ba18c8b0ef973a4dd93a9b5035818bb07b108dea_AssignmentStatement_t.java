 package edu.lmu.cs.xlg.koan.entities;
 
 import edu.lmu.cs.xlg.util.Log;
 
 /**
  * A Koan assignment statement.
  */
 public class AssignmentStatement extends Statement {
 
     private VariableReference left;
     private Expression right;
 
     /**
      * Creates an assignment statement.
      */
     public AssignmentStatement(VariableReference left, Expression right) {
         this.left = left;
         this.right = right;
     }
 
     /**
      * Returns the destination of the assignment.
      */
     public VariableReference getLeft() {
         return left;
     }
 
     /**
      * Returns the source of the assignment.
      */
     public Expression getRight() {
         return right;
     }
 
     /**
      * Analyzes the statement, ensuring that the target variable is writable and the right hand side
      * is type compatible with the type of the variable.
      */
//    @Override
//    public void analyze(Log log, SymbolTable table, Function function, boolean inLoop) {
//        left.analyze(log, table, function, inLoop);
//        right.analyze(log, table, function, inLoop);
//        left.assertWritable(log);
//        right.assertAssignableTo(left.type, log, "assignment.type.error");
//    }

	public Expression getVariableReference() {
		// TODO Auto-generated method stub
		return null;
	}
 }
