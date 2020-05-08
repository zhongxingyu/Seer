 package smartest;
 
 /**
  * Implements Semantic checking and code output generation for addition,
  * subtraction, multiplication, division, and modulus.
  * 
  * Example -
  * <p>
  * a + 3 <br />
  * a - 3 <br />
  * a * 3 <br />
  * a / 3
  * </p>
  * 
  * @author Parth and Kshitij
  */
 public class ArithmeticOperatorNode extends ASTNode {
 
     /** The type of operation */
     private String arithType = "";
 
     /**
      * Instantiates an ArithmeticOperatorNode.
      * 
      * <p>
      * This node represents the grammar productions:
      * 
      * <pre>
      * relational_operand '+' term
      * relational_operand '-' term relational_operand '/' term
      * relational_operand '*' term relational_operand '%' term
      * </pre>
      * 
      * <p>
      * Examples:
      * 
      * <pre>
      * a + 3
      * a - 3
      * a * 3
      * a / 3
      * a % 3
      * </pre>
      * 
      * </p>
      * 
      * @param str
      *            specifies whether the expression was addition or subtraction
      * @param expr1
      *            represents the left side
      * @param expr2
      *            represents the right side
      */
     public ArithmeticOperatorNode(String str, ASTNode expr1, ASTNode expr2,
             int yyline, int yycolumn) {
         super(yyline, yycolumn);
         addChild(expr1);
         addChild(expr2);
         this.setArithType(str);
     }
 
     /**
      * 
      * Instantiates an ArithmeticOperatorNode for a unary operator.
      * 
      * <p>
      * Example: -a
      * </p>
      * 
      * @param str
      *            specifies whether the expression was addition or subtraction
      * 
      * @param expr
     *            the operand
      */
    public ArithmeticOperatorNode(String str, ASTNode expr, int yyline,
             int yycolumn) {
         super(yyline, yycolumn);
        this.addChild(expr);
         this.setArithType(str);
     }
 
     /**
      * Checks the semantics to make sure that all operands are numerical (float
      * or int). Throw an exception otherwise. (non-Javadoc)
      * 
      * @see ASTNode#checkSemantics()
      */
     @Override
     public void checkSemantics() throws Exception {
         this.getChildAt(0).checkSemantics();
 
         if (this.getChildCount() > 1)
             this.getChildAt(1).checkSemantics();
 
         // for unary minus
         if (this.getChildCount() == 1) {
             if (!(this.getChildAt(0).getType().equalsIgnoreCase("int") || this
                     .getChildAt(0).getType().equalsIgnoreCase("float"))) {
                 throw new Exception("Invalid operand on line "
                         + this.getYyline() + ":" + this.getYycolumn()
                         + " cannot compute unary minus on type "
                         + this.getChildAt(0).getType());
             }
             if (this.getChildAt(0).getType().equals("float"))
                 this.setType("float");
             else
                 this.setType("int");
         } else {
 
             if (!((this.getChildAt(0).getType().equals("float") || this
                     .getChildAt(0).getType().equals("int")) && (this
                     .getChildAt(1).getType().equals("float") || this
                     .getChildAt(1).getType().equals("int")))) {
                 throw new Exception("Cannot do Arithmetic operation on "
                         + this.getChildAt(0).getType() + " & "
                         + this.getChildAt(1).getType() + " on line "
                         + this.getYyline() + ":" + this.getYycolumn());
             }
             if (this.getChildAt(0).getType().equals("float")
                     || this.getChildAt(1).getType().equals("float"))
                 this.setType("float");
             else
                 this.setType("int");
         }
     }
 
     /**
      * Invokes code generation of children, and appends operator in its proper
      * place.
      * 
      * @see ASTNode#generateCode()
      */
     @Override
     public StringBuffer generateCode() {
         StringBuffer output = new StringBuffer();
         if (this.getChildCount() > 1) {
             output.append(this.getChildAt(0).generateCode());
             if ("multiplication".equalsIgnoreCase(getArithType())) {
                 output.append(" * ");
             } else if ("division".equalsIgnoreCase(getArithType())) {
                 output.append(" / ");
             } else if ("addition".equalsIgnoreCase(getArithType())) {
                 output.append(" + ");
             } else if ("subtraction".equalsIgnoreCase(getArithType())) {
                 output.append(" - ");
             } else if ("modulus".equalsIgnoreCase(getArithType())) {
                 output.append(" % ");
             }
             output.append(this.getChildAt(1).generateCode());
         } else {
             output.append(" - ");
             output.append(this.getChildAt(0).generateCode());
         }
         return output;
     }
 
     /**
      * Gets the type of operator.
      * 
      * @return the arithType
      */
     public String getArithType() {
         return arithType;
     }
 
     /**
      * Sets the type of operator.
      * 
      * @param arithType
      *            the arithType to set
      */
     public void setArithType(String arithType) {
         this.arithType = arithType;
     }
 
 }
