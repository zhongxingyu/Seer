 /**
  * @author Aiman
  * Implements semantic checking and output code generation
  * of assignment statements
  * Example: x = y * d (without declaration)
  * 			int x = y + z (with declaration)
  * 
  */
 public class AssignmentOperatorNode extends ASTNode {
 
 	/** Indicates whether this assignment statement includes a declaration as well */
 	private boolean isDeclaration = false;
 	
 	/**
 	 * Instantiates AssignmentOperatorNode invoked by this grammar:
 	 * type ID '=' expression. Note this contains a declaration as well.
 	 * 
 	 *  Example:
 	 *  int x = 5
 	 *  
 	 *  @param declaration represents the left-side operand which is a declaration statement
 	 *  @param expr represents the right-hand side of the operator
 	 *  @param yyline the line where this operator was found in the source code
 	 *  @param yycolumn the column where this operator was found in the source code
 	 */
 	public AssignmentOperatorNode(DeclarationNode declaration, ASTNode expr, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(declaration);
 		this.addChild(expr);
 		this.isDeclaration = true;
 		
 	}
 	
 	
 	/**
 	 * Instantiates AssignmentOperatorNode invoked by this grammar:
 	 * ID '=' expression
 	 * 
 	 *  Example:
 	 *  x = 5
 	 *  
 	 *  @param idType represents the type of the identifier (e.g. int)
 	 *  @param id represents the identifier node 
 	 *  @param expr represents the right-hand side of the operator
 	 *  @param yyline the line where this operator was found in the source code
 	 *  @param yycolumn the column where this operator was found in the source code  
 	 */
	public AssignmentOperatorNode(IDNode id, ASTNode expr, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(id);
 		this.addChild(expr);
 	}
 	
 	/**
 	 * Assignment operator semantics:
 	 * Verifies that right side type matches the left hand side type
 	 * @throws Exception 
 	 * @see ASTNode#checkSemantics()
 	 */
 	@Override
 	public void checkSemantics() throws Exception {
 		this.getChildAt(0).checkSemantics();
 		this.getChildAt(1).checkSemantics();
 
 
 		// Now, verify the rhs and lhs types are equal
 		if (! this.getChildAt(0).getType().equals(this.getChildAt(1).getType()))
 		{
 			String varName = "";
 			if (this.isDeclaration)
 				varName = ((DeclarationNode)this.getChildAt(0)).getIdNode().getName();
 			else
 				varName = ((IDNode)this.getChildAt(0)).getName();
 			throw new Exception("Type mismatch: variable " + varName + " was declared "
 								+ this.getChildAt(0).getType() + ", but right-hand side value is "
 								+ this.getChildAt(1).getType() + ". Line " + this.getYyline() + ":" + this.getYycolumn());
 		}
 		
 		this.setType(this.getChildAt(0).getType());
 		
 			
 	}
 
 	
 
 	@Override
 	public String generateCode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 	public boolean isDeclaration() {
 		return isDeclaration;
 	}
 
 
 
 
 }
