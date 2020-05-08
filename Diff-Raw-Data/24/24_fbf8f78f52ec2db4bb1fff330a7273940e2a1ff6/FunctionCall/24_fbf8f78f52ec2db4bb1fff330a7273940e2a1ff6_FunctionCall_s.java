 package ast;
 
 /**
  * This class symbolizes a function call.
  */
 public class FunctionCall extends Expression {
     /**
      * function parameters
      */
     private final Expression[] parameters;
     /**
      * identifier of the function to be called
      */
     private final Identifier functionIdentifier;
     /**
      * function to be called
      */
     private Function function;
 
     /**
      * Constructor.
      *
      * @param function function to be called
      * @param parameters array of function parameters
      * @param position indicates the position of this element
      *                 in the original source code
      */
     public FunctionCall(Identifier function, Expression[] parameters,
                            Position position) {
         super(position);
         this.parameters = parameters;
         this.functionIdentifier = function;
     }
 
     @Override
     public void accept(ASTVisitor visitor) {
         visitor.visit(this);
     }
 
     /**
      * Sets the reference to the correct function.
      * This is done during type checking.
      * @param function function to be called
      */
     public void setFunction(Function function) {
        if (this.function != null) {
            //function cannot be set again
            throw new IllegalStateException("Function may only be set once!");
        }
         this.function = function;
     }
 
     /**
      * Returns the parameters of this function call.
      * @return function parameters
      */
     public Expression[] getParameters() {
         return parameters;
     }
 
     /**
      * Returns the identifier of the function to be called.
      * @return identifier of the function to be called
      */
     public Identifier getFunctionIdentifier() {
         return functionIdentifier;
     }
 
     /**
      * Returns the function to be called.
      * @return function to be called
      */
     public Function getFunction() {
         return function;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("(");
         sb.append(functionIdentifier).append('(');
         if (parameters.length != 0) {
             sb.append(parameters[0]);
         }
         for (int i = 1; i < parameters.length; i++) {
             sb.append(", ").append(parameters[i]);
         }
         sb.append("))");
         return sb.toString();
     }
 }
