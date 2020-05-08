 /**
  * 
  */
 
 /**
  * Represents the insert operator ( << )
  * As defined in this grammar:
  * ID INSERT expression ';'
  * Where INSERT token is "<<"
  * Expression is a question type
  * and ID is set type
  * Example:
  * s << $"Math":"1+1 is ?": [ "2":4, "5":0 ]
  * @author Aiman
  */
 public class InsertOperatorNode extends ASTNode {
 
 	private ASTNode set;
 	private ASTNode question;
 	
 	/**
 	 * Instantiates an insert operator node, invoked by this grammar production:
 	 * statement: ID INSERT expression ';'
 	 * @param id the set to be modified
 	 * @param expression the question to be inserted
 	 * @param yyline line where this operator was found in the source code
 	 * @param yycolumn column where this operator was found in the source code
 	 */
 	public InsertOperatorNode(ASTNode set, ASTNode question, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(set);
 		this.addChild(question);
 		this.setSet(set);
 		this.question = question;
 		
 	}
 
 	/**
 	 * Verifies the semantic of the set operator:
 	 * Example: s << q
 	 * s must bet a set
 	 * q must be a question
 	 * Insert operator << has a side effect, it returns set
 	 * @see ASTNode#checkSemantics()
 	 */
 	@Override
 	public void checkSemantics() throws Exception {
		getSet().checkSemantics();
		question.checkSemantics();
 		
 		if (! getSet().getType().equals("set"))
 		{
 			throw new Exception("Line " + this.getYyline() + ":" + this.getYycolumn() + " "
 					+ " insert operator << left operand must be a set, found: " + getSet().getType());
 		}

		if (! question.getType().equals("question"))
		{
			throw new Exception("Line " + this.getYyline() + ":" + this.getYycolumn() + " "
					+ " insert operator << right operand must be a question, found: " + question.getType());
 		}
		
 		this.setType("set");
 		
 
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ASTNode#generateCode()
 	 */
 	@Override
 	public StringBuffer generateCode() {
 		
 		StringBuffer output = new StringBuffer();
                 ASTNode setID = this.getChildAt(0);
                 output.append(setID.generateCode());
                 for (ASTNode questionE : this.getChildren())
                 {
                         if (questionE == setID)
                                 continue;
                         output.append(".addQuestion(");
                         output.append(questionE.generateCode());
                         output.append(")");
                 }
 		
 		
 		
 		return output;
 	}
 
 	/**
 	 * @return the question
 	 */
 	public ASTNode getQuestion() {
 		return question;
 	}
 
 	/**
 	 * @param question the question to set
 	 */
 	public void setQuestion(ASTNode question) {
 		this.question = question;
 	}
 
 	/**
 	 * @return the set
 	 */
 	public ASTNode getSet() {
 		return set;
 	}
 
 	/**
 	 * @param set the set to set
 	 */
 	public void setSet(ASTNode set) {
 		this.set = set;
 	}
 
 	
 }
