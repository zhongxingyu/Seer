 
 /**
  * Represents a question literal node as defined in this grammar:
  * question_literal : '$' expr1 ':' expr2 '[' answer_choices ']' '$'
  * Example: $"Math-Easy":"What is  2 * 4 / 2?" [ "4":5, "6":0 ]$
  * Semantics: expr1 and expr2 must be strings
  * @author Aiman
  */
 public class QuestionLiteralNode extends ASTNode {
 
 	private ASTNode questionCategory;
 	private ASTNode questionText;
 	private ASTNode answerChoices;
 	
 	/**
 	 * Instantiates a question node invoked by this production in  the grammar:
 	 * question_literal : '$' expr1 ':' expr2 '[' answer_choices ']' '$'
 	 * @param expr1 the question category
 	 * @param expr2 the question text
 	 * @param answerChoices a list of answer choices (AnswerChoicesNode)
 	 */
 	public QuestionLiteralNode(ASTNode expr1, ASTNode expr2, ASTNode answerChoices, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(expr1);
 		this.addChild(expr2);
 		this.addChild(answerChoices);
 		this.questionCategory = expr1;
 		this.questionText = expr2;
 		this.answerChoices = answerChoices;
 	}
 
 	/**
 	 * Verifies the semantics of the question literal:
 	 * Question category must be a string
 	 * Question text must be a string
 	 * @see ASTNode#checkSemantics()
 	 */
 	@Override
 	public void checkSemantics() throws Exception {
 		questionCategory.checkSemantics();		
 		questionText.checkSemantics();
 		answerChoices.checkSemantics();
 		
 		if (! questionCategory.getType().equals("string"))
 		{
 			throw new Exception("Line " + this.getYyline() + ":" + this.getYycolumn() + " "
 					+ " question category must be a string, found: " + questionCategory.getType());
 		}
 		
 		if (! questionText.getType().equals("string"))
 		{
 			throw new Exception("Line " + this.getYyline() + ":" + this.getYycolumn() + " "
 					+ " question text must be a string, found: " + questionText.getType());
 		}		
 
 		this.setType("question");
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see ASTNode#generateCode()
 	 */
 	@Override
 	public StringBuffer generateCode() {
 
 		StringBuffer output = new StringBuffer();
		output.append("new Question(");
 		output.append(this.questionCategory.generateCode());
 		output.append(", ");
 		output.append(this.questionText.generateCode());
 		output.append(", ");
 		output.append(this.answerChoices.generateCode());
 		output.append(")");
 		
 		return output;
 
 		
 	}
 	
 	/**
 	 * @return the questionCategory
 	 */
 	public ASTNode getQuestionCategory() {
 		return questionCategory;
 	}
 
 	/**
 	 * @param questionCategory the questionCategory to set
 	 */
 	public void setQuestionCategory(ASTNode questionCategory) {
 		this.questionCategory = questionCategory;
 	}
 
 	/**
 	 * @return the questionText
 	 */
 	public ASTNode getQuestionText() {
 		return questionText;
 	}
 
 	/**
 	 * @param questionText the questionText to set
 	 */
 	public void setQuestionText(ASTNode questionText) {
 		this.questionText = questionText;
 	}
 
 	/**
 	 * @return the answerChoices
 	 */
 	public ASTNode getAnswerChoices() {
 		return answerChoices;
 	}
 
 	/**
 	 * @param answerChoices the answerChoices to set
 	 */
 	public void setAnswerChoices(ASTNode answerChoices) {
 		this.answerChoices = answerChoices;
 	}
 
 }
