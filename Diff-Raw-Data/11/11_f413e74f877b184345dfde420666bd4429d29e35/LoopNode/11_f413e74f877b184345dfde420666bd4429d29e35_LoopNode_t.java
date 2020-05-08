 package smartest;
 
 /**
  * Implements semantic checking and output code generation for a 'loop while'
 * statement.
 * <br />
  * Example:
  * 
  * <pre>
  * loop while(i<=4) {
  *      i=i+1;
  * }
  * </pre>
  * 
  * @author Kshitij
  */
 public class LoopNode extends ASTNode implements NoSemiColonStatement {
     /**
      * Instantiates a LoopNode. It represents the following grammar production:
      * 
      * <pre>
      * LOOP WHILE '(' expression ')' '{' statements '}'
      * </pre>
      * 
      * <br />
      * Example:
      * 
      * <pre>
      * loop while (i < 6) {
      *     i = i + 1;
      * }
      * </pre>
      * 
      * @param expr
      *            the loop condition
      * @param stmt
      *            represents statements to iterate over
      * @param yyline
      *            the corresponding line in the input file
      * @param yycolumn
      *            the corresponding column in the input file
      */
     LoopNode(ASTNode expr, ASTNode stmt, int yyline, int yycolumn) {
         super(yyline, yycolumn);
         addChild(expr);
         addChild(stmt);
     }
 
     /**
      * This verifies the semantics. The expression used here should of the type
      * boolean.
      * 
      * @throws Exception
      *             the exception
      * @see ASTNode#checkSemantics()
      */
     @Override
     public void checkSemantics() throws Exception {
         SymbolsTables.enterNewScope();
         this.getChildAt(0).checkSemantics();
         this.getChildAt(1).checkSemantics();
         /*
          * The grammar should look into that only boolean expressions or
          * relational expressions are executed.
          */
         if (!this.getChildAt(0).getType().equals("boolean")) {
             throw new Exception("Type mismatch: statement at Line "
                     + this.getYyline() + ":" + this.getYycolumn()
                     + " should be a boolean; found: "
                     + this.getChildAt(0).getType());
         }
         SymbolsTables.leaveCurrentScope();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see ASTNode#generateCode()
      */
     @Override
     public StringBuffer generateCode() {
         StringBuffer output = new StringBuffer();
         output.append("while (");
         output.append(this.getChildAt(0).generateCode());
         output.append(")");
         output.append("\n");
         output.append("{");
         output.append(this.getChildAt(1).generateCode());
         output.append("}");
         output.append("\n");
         return output;
     }
 }
