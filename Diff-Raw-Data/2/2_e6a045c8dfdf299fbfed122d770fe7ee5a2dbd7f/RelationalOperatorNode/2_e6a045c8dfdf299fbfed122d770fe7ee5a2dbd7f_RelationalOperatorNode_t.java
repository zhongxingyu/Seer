 /*
  * Implements Semantic checking and code output generation for
  * EqualityOperands
  * 
  * Example - a  LT 3
  * 			 b  LT 5
  */
 class RelationalOperatorNode extends ASTNode {
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
 	public RelationalOperatorNode(String op, ASTNode lcNode, ASTNode rcNode,
 			int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(lcNode);
 		this.addChild(rcNode);
 	}
 
 	/*
 	 * symnatic check
 	 * 
 	 */
 	@Override
 	public void checkSemantics() throws Exception{
 			//symantic check for children
 		   this.getChildAt(0).checkSemantics();
 		   this.getChildAt(1).checkSemantics();
 		  
 		   //type check
 			if (!this.getChildAt(0).getType().equals(this.getChildAt(1).getType())) {
 				throw new Exception("Type Mismatch : Cannot Add" + this.getChildAt(0).getType()
 						+ " and " + this.getChildAt(1).getType()+ "-- at "
 						+ this.getChildAt(0).getYyline() + ":" + this.getChildAt(0).getYycolumn());
 			}
			this.setType("boolean");
 		return;	
 	}
 
 	@Override
 	public String generateCode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
