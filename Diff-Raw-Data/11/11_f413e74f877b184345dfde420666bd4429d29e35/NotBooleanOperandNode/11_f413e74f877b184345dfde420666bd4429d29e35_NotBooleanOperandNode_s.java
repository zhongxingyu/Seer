 package smartest;
 
 /**
  * Implements semantic checking and output code generation for the unary NOT
 * operation.<br />
  * Example:
  * 
  * <pre>
  * NOT true
  * </pre>
  * 
  * @author Kshitij
  */
 public class NotBooleanOperandNode extends ASTNode {
     /**
      * Instantiates NotBooleanOperandNode.
      * 
      * @param expr
      *            represents an expression of type boolean
      * @param yyline
      *            the corresponding line in the input file
      * @param yycolumn
      *            the corresponding column in the input file
      */
     public NotBooleanOperandNode(ASTNode expr, int yyline, int yycolumn) {
         super(yyline, yycolumn);
         this.addChild(expr);
     }
 
     /**
      * Semantic check to make sure the expression is of type boolean.
      * 
      * @throws Exception
      *             if the operand is of unsupported type
      * @see ASTNode#checkSemantics()
      */
     @Override
     public void checkSemantics() throws Exception {
         this.getChildAt(0).checkSemantics();
         if (!this.getChildAt(0).getType().equals("boolean")) {
             throw new Exception("Type mismatch: statement at Line "
                     + this.getYyline() + ":" + this.getYycolumn()
                     + "should be a booelean");
         }
         this.setType(this.getChildAt(0).getType());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see ASTNode#generateCode()
      */
     @Override
     public StringBuffer generateCode() {
         StringBuffer output = new StringBuffer();
         output.append("!");
         output.append(this.getChildAt(0).generateCode());
         output.append(" ");
         return output;
     }
 }
