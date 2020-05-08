 
 package scalr.expression;
 
 import scalr.Exceptions.TypeError;
 import scalr.variable.SymbolTable;
 import scalr.variable.Variable;
 
 public class CreationOperator implements Expression
 {
 	Expression	   rval;
 	String	       var;
 	private String	func;
 	
 	public CreationOperator(String name)
 	{
 		var = name;
 	}
 	
 	public void addFunc(String name)
 	{
 		func = name;
 	}
 	
 	public void addOperand(Expression expr)
 	{
 		rval = expr;
		if (func != null)
			SymbolTable.addTypeReference(func, var, expr.getType());
		else
			SymbolTable.addTypeReference(SymbolTable.currentFunctionScope, var, expr.getType());
 	}
 	
 	@Override
 	public Expression getValue(Expression... expressions)
 	{
 		// Compute the rval
 		Variable inter = (Variable) rval.getValue(expressions);
 		Expression result = inter.getCopy();
 		try {
 			SymbolTable.addReference(func, var, result);
 		}
 		catch (TypeError e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		return result;
 	}
 	
 	@Override
 	public ExpressionType getType()
 	{
 		return rval.getType();
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "(" + func + "." + var + " = Type: " + rval.getType() + " | Value: "
 		        + rval.toString() + ")";
 	}
 	
 }
