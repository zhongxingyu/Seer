 /**
  * 
  */
 package functions;
 
 import symboldiff.Expression;
 import symboldiff.Gradient;
 import symboldiff.Simplifier;
 import symboldiff.exceptions.ExpressionException;
 
 /**
  * @author nvpanov
  * This class creates Functions from Strings.
  * It parses the string and create Expression,
  * than apples simboldiff on it and gets first and
  * second derivatives as Expressions as well  
  */
 public class FunctionFactory {
 	private static FunctionNEW function = null;
 	public static FunctionNEW getTargetFunction() {
 		assert(function != null);
 		return function;
 	}
 	public static FunctionNEW newFunction(String formula) throws ExpressionException {
 		Expression func = null;
 		try {
 			func = new Expression(formula);
 		} catch (ExpressionException e) {
 			//e.printStackTrace();
 			throw e;
 		}
 		Simplifier.simplify(func);
 		Gradient g1 = new Gradient(func);
 		Expression[] df1 = new Expression[func.numOfVars()];
 		Expression[] df2 = new Expression[df1.length];
 		try {
 			for (int i = 0; i < df1.length; i++) {
 				df1[i] = g1.getPartialDerivative(i);
 				Simplifier.simplify(df1[i]);
 				assert(df1[i]!=null);
				df2[i] = new Gradient(g1.getPartialDerivative(i)).getPartialDerivative(0);
 				Simplifier.simplify(df2[i]);
 				assert(df2[i]!=null);
 			}
 		} catch (Exception e) {
 			assert(false); // no exceptions can be thrown during differentiation of test functions
 		}		
 		FunctionNEW f = new FunctionNEW();
 		f.init(func, df1, df2);
 		function=f;
 		return f;
 	}
 
 }
