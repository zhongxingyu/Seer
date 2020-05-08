 
 
 /*
  * Implements semantic checking and output code generation
  * for a 'loop while' statement
  * Example :
  * 			loop while(i<=4){i=i+1;}
  */
 public class LoopNode extends ASTNode{
 	
 	/*
 	 * Instantiates LoopNode invoked by this grammar:
 	 * LOOP WHILE '(' expression ')' '{' statements '}' 
 	 * 
 	 *  Example:
 	 *  loop while (i<6) {i=10;}
 	 *  
 	 *  @param expr represents an expression
 	 *  @param stmt represents statements 
 	 */
 	LoopNode(ASTNode expr, ASTNode stmt,int yyline, int yycolumn)
 	{
 		super(yyline, yycolumn);
 		addChild(expr);
 		addChild(stmt);
 	}
 	
 	/*
 	 * This verifies the semantics.
 	 * The expression used here should of the type boolean.
 	 * @see ASTNode#checkSemantics()
 	 */
 	@Override
 	public void checkSemantics() throws Exception
 	{
 		this.getChildAt(0).checkSemantics();
 		this.getChildAt(1).checkSemantics();
 		/*
 		 * The grammar should look into that only boolean expressions or relational expressions are executed.
 		 */
		if (! this.getChildAt(1).getType().equals("boolean"))
 		{
			throw new Exception("Type mismatch: statement at Line " + this.getYyline() + ":" + this.getYycolumn()+"should be a booelean");
 		}
 	}
 	
 	@Override
 	public String generateCode()
 	{
 		return null;
 		
 	}
 
 }
