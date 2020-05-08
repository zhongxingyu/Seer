 package csheets.core.formula.lang;
 
 import csheets.core.*;
 import csheets.core.formula.*;
 
/**
 * A function that implements a loop statement where the first condition was the
 * stop argument and the next arguments are the execution statements
 * 
 * @author Andre
 * 
 */
 public class Whiledo implements Function {
 
    /**
     * Create a new instance of function while do
     */
     public Whiledo() {
     }
 
    /** Parameters: condition, execution statements */
     public static final FunctionParameter[] parameters = new FunctionParameter[] { new FunctionParameter(
 	    Value.Type.UNDEFINED, "Iterations", false,
 	    "The iterators of expression.") };
 
     @Override
     public String getIdentifier() {
 	return "WHILEDO";
     }
 
     @Override
     public Value applyTo(Expression[] args) throws IllegalValueTypeException {
 	// arg[0] = conditionals
 	// arg[>0] = iterations
 	int i = 1;
 	while (args[0].evaluate().toBoolean()) {
 	    for (; i < (args.length - 1); i++) {
 		args[i].evaluate();
 	    }
 	    return args[i].evaluate();
 	}
	return args[i + 1].evaluate();
     }
 
     @Override
     public FunctionParameter[] getParameters() {
 	return parameters;
     }
 
     @Override
     public boolean isVarArg() {
 	return true;
     }
 
 }
