 package smartest;
 
 /*
  * Implements Semantic checking and code output generation for
  * EqualityOperands
  * 
  * Example - a  LT 3
  * 			 b  LT 5
  */
 /**
  * The Class RelationalOperatorNode.
  *@author Parth
  */
 class RelationalOperatorNode extends ASTNode {
 	
 	/** The operator. */
 	private String operator;
 	/*
 	 * Instantiates EqualityOperator invoked by this grammar:
 	 *  equality_operand  LT relational_operand 
 	 *  equality_operand  GT relational_operand
 	 * 
 	 * 
 	 *  @param op specifies the type of operator
 	 *  @param lcNode,rcNode represents nodes of operand
 	 *  @param yyline,yycolumn represents nodes line number and column number 
 	 */
 	/**
 	 * Instantiates a new relational operator node.
 	 *
 	 * @param op the op
 	 * @param lcNode the lc node
 	 * @param rcNode the rc node
 	 * @param yyline the yyline
 	 * @param yycolumn the yycolumn
 	 */
 	public RelationalOperatorNode(String op, ASTNode lcNode, ASTNode rcNode,
 			int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(lcNode);
 		this.addChild(rcNode);
 		this.operator = op;
 	}
 
 	/*
 	 * symnatic check
 	 * 
 	 */
 	/* (non-Javadoc)
 	 * @see ASTNode#checkSemantics()
 	 */
 	@Override
 	public void checkSemantics() throws Exception{
 			//symantic check for children
 		   this.getChildAt(0).checkSemantics();
 		   this.getChildAt(1).checkSemantics();
 
 			if(!((this.getChildAt(0).getType().equals("float") || this.getChildAt(0).getType().equals("int"))
 					&& (this.getChildAt(1).getType().equals("float") || this.getChildAt(1).getType().equals("int"))))
 			{
 				throw new Exception("Cannot do Relational operation on "+this.getChildAt(0).getType()+ " & " + 
 						this.getChildAt(1).getType()+" on line "+ this.getYyline() + ":" + 
 						this.getYycolumn());
 			}
 
             		this.setType("boolean");
 		return;	
 	}
 
 	/* (non-Javadoc)
 	 * @see ASTNode#generateCode()
 	 */
 	@Override
 	public StringBuffer generateCode() {
 		// TODO Auto-generated method stub
 		StringBuffer output = new StringBuffer();
 		output.append(this.getChildAt(0).generateCode());
 		if ("LT".equalsIgnoreCase(operator))
 		{
 			output.append(" < ");
 		}
 		else if ("LE".equalsIgnoreCase(operator))
 		{
 			output.append(" <= ");
 		}
 		else if ("GT".equalsIgnoreCase(operator))
 		{
 			output.append(" > ");
 		}
 		else if ("GE".equalsIgnoreCase(operator))
 		{
			output.append(" <= ");
 
 		}
 		output.append(this.getChildAt(1).generateCode());
 		return output;
 	}
 
 }
