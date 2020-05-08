 package forrest.main;
 
 import java.util.List;
 
 import fire.ForrestFireException;
 
 /**
  * Class to check validity of expressions and sets the return types of their parents
  */
 public class ExpressionChecker {
 	
 	/**
 	 * Check whether the types of two expressions match and sets the return type
 	 	of the parent node to that type if they do
 	 * @param t - the parent tree node
 	 * @throws ForrestFireException if the expressions do not match
 	 */
 	public static void checkAssign(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		ForrestTree expr2 = (ForrestTree) t.getChild(1);
 		if (expr1.getReturnType() != expr2.getReturnType()) {
 			throw new ForrestFireException(expr2, "does not match type of " + expr1.getReturnType());
 		} else {
 			t.setReturnType(expr1.getReturnType());
 		}
 	}
 	
 	/**
 	 * Check whether an if-else-statement is correct
 	 * @param t - the parent tree node
 	 * @throws ForrestFireException if the expressions do not match
 	 */
 	public static void checkIfElse(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		ForrestTree expr2 = (ForrestTree) t.getChild(1);
 		ForrestTree expr3 = (ForrestTree) t.getChild(2);
 		if (expr1.getReturnType() != Type.BOOL) {
 			throw new ForrestFireException(expr1, "is not of type boolean");
 		} else {
 			if (expr2.getReturnType() == expr3.getReturnType()) {
 				t.setReturnType(expr2.getReturnType());
 			} else {
 				t.setReturnType(Type.VOID);
 			}
 		}
 	}
 	
 	/**
 	 * Check whether an if-statement is correct
 	 * @param t - the parent tree node
 	 * @throws ForrestFireException if the expressions do not match
 	 */
 	public static void checkIf(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		if (expr1.getReturnType() != Type.BOOL) {
 			throw new ForrestFireException(expr1, "is not of type boolean");
 		} else {
 			t.setReturnType(Type.VOID);
 		}
 	}
 	
 	/**
 	 * Check whether a binary boolean operation is correct
 	 * @param t - the parent node
 	 * @throws ForrestFireException if the operation is not correct
 	 */
 	public static void checkBinaryBoolean(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		ForrestTree expr2 = (ForrestTree) t.getChild(1);
 		if (expr1.getReturnType() != Type.BOOL){
 			throw new ForrestFireException(expr1, "is not of type boolean");
 		} else if (expr2.getReturnType() != Type.BOOL){
 			throw new ForrestFireException(expr2, "is not of type boolean");
 		} else {
 			t.setReturnType(Type.BOOL);
 		}
 	}
 	
 	/**
 	 * Check whether a comparison between integers is correct
 	 * @param t - the parent node
 	 * @throws ForrestFireException if the operation is not correct
 	 */
 	public static void checkComparison(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		ForrestTree expr2 = (ForrestTree) t.getChild(1);
 		if (expr1.getReturnType() != Type.INT){
 			throw new ForrestFireException(expr1, "is not of type integer");
 		} else if (expr2.getReturnType() != Type.INT){
 			throw new ForrestFireException(expr2, "is not of type integer");
 		} else {
 			t.setReturnType(Type.BOOL);
 		}
 	}
 	
 	/**
 	 * Check whether a comparison between two expressions is correct
 	 * @param t - the parent node
 	 * @throws ForrestFireException if the operation is not correct
 	 */
 	public static void checkEquals(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		ForrestTree expr2 = (ForrestTree) t.getChild(1);
 		if (expr1.getReturnType() != expr2.getReturnType()){
 			throw new ForrestFireException(expr2, "does not match the type of " + expr1.getReturnType());
 		} else {
 			t.setReturnType(Type.BOOL);
 		}
 	}
 	
 	/**
 	 * Check whether an operation with integers is correct
 	 * @param t - the parent node
 	 * @throws ForrestFireException if the operation is not correct
 	 */
 	public static void checkBinaryInteger(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		ForrestTree expr2 = (ForrestTree) t.getChild(1);
 		if (expr1.getReturnType() != Type.INT){
 			throw new ForrestFireException(expr1, "is not of type integer");
 		} else if (expr2.getReturnType() != Type.INT){
 			throw new ForrestFireException(expr2, "is not of type integer");
 		} else {
 			t.setReturnType(Type.INT);
 		}
 	}
 	
 	/**
 	 * Check whether an operation with an integer is correct
 	 * @param t - the parent node
 	 * @throws ForrestFireException if the operation is not correct
 	 */
 	public static void checkUnaryInteger(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		if (expr1.getReturnType() != Type.INT){
 			throw new ForrestFireException(expr1, "is not of type integer");
 		} else {
 			t.setReturnType(Type.INT);
 		}
 	}
 	
 	/**
 	 * Check whether an operation with a boolean is correct
 	 * @param t - the parent node
 	 * @throws ForrestFireException if the operation is not correct
 	 */
 	public static void checkUnaryBoolean(ForrestTree t) throws ForrestFireException {
 		ForrestTree expr1 = (ForrestTree) t.getChild(0);
 		if (expr1.getReturnType() != Type.BOOL){
 			throw new ForrestFireException(expr1, "is not of type boolean");
 		} else {
 			t.setReturnType(Type.BOOL);
 		}
 	}
 	
 	/**
 	 * Check whether a read operation is correct
 	 * @param t - the parent node
 	 */
 	public static void checkRead(ForrestTree t) {
 		List<ForrestTree> ids = (List<ForrestTree>) t.getChildren();
 		if (ids.size() == 1) {
 			t.setReturnType(ids.get(0).getReturnType());
 		} else {
 			t.setReturnType(Type.VOID);
 		}
 	}
 	
 	/**
 	 * Check whether a print operation is correct
 	 * @param t - the parent node
 	 * @throws ForrestFireException - if there is a problem in the arguments
 	 */
 	public static void checkPrint(ForrestTree t) throws ForrestFireException {
 		List<ForrestTree> exprs = (List<ForrestTree>) t.getChildren();
 		for(int i = 0; i < exprs.size(); i++) {
 			if (exprs.get(i).getReturnType() == Type.VOID) {
 				throw new ForrestFireException(exprs.get(i), "is of type void");
 			}
 		}
 		if (exprs.size() == 1) {
 			t.setReturnType(exprs.get(0).getReturnType());
 		} else {
 			t.setReturnType(Type.VOID);
 		}
 	}
     
     /**
      * Set the type for a ForrestTree node that is an integer
      * @param t - ForrestTree that needs its type set 
      * @throws ForrestFireException if the id has not been declared yet
      */
    public void setNumber(ForrestTree t) throws ForrestFireException {
 		t.setReturnType(Type.INT);
     }
     
     /**
      * Set the type for a ForrestTree node that is a boolean
      * @param t - ForrestTree that needs its type set 
      * @throws ForrestFireException if the id has not been declared yet
      */
    public void setBoolean(ForrestTree t) throws ForrestFireException {
 		t.setReturnType(Type.BOOL);
     }
     
     /**
      * Set the type for a ForrestTree node that is a character
      * @param t - ForrestTree that needs its type set 
      * @throws ForrestFireException if the id has not been declared yet
      */
    public void setCharacter(ForrestTree t) throws ForrestFireException {
 		t.setReturnType(Type.CHAR);
     }
     
     /**
      * Set the type for a ForrestTree node that is a closed compound
      * @param t - ForrestTree that needs its type set 
      * @throws ForrestFireException if the id has not been declared yet
      */
    public void setCompound(ForrestTree t) throws ForrestFireException {
     	ForrestTree last = (ForrestTree) t.getChild(t.getChildCount() - 1);
 		t.setReturnType(last.getReturnType());
     }
 
 }
