 package calculator.parser;
 
 import calculator.common.Token;
 
 public class AST {
 	Token<TokenType> token;
 	AST left, right; // operands
 
 	public AST(Token<TokenType> token) {
 		this.token = token;
 	}
 
 	public void setLeft(AST t) {
 		left = t;
 	}
 
 	public void setRight(AST t) {
 		right = t;
 	}
 
 	public Token<TokenType> getToken() {
 		return token;
 	}
 
 	public long evaluate() {
		// System.out.println(getToken());
 		long result = 0;
 		switch (getToken().type) {
 		case INT:
 			result = Integer.parseInt(getToken().text);
 			break;
 		case MULTIPLY:
 			result = left.evaluate() * right.evaluate();
 			break;
 		case DIVIDE:
 			result = left.evaluate() / right.evaluate();
 			break;
 		case PLUS:
 			result = left.evaluate() + right.evaluate();
 			break;
 		case MINUS:
 			result = left.evaluate() - right.evaluate();
 			break;
 		default:
 			break;
 		}
		// System.out.println("END OF " + getToken());
 		return result;
 	}
 }
