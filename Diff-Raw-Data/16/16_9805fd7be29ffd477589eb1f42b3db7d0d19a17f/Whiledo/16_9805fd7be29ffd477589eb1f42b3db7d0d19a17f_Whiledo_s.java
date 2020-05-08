 package csheets.core.formula.lang;
 
 import csheets.core.*;
 import csheets.core.formula.*;
 
 public class Whiledo implements Function {
 
     public Whiledo() {
     }
 
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
	return new Value();
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
