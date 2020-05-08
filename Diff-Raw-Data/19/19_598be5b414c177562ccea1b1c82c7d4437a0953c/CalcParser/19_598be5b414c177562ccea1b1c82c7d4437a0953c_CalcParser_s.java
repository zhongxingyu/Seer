 package calculator.parser;
 
 import calculator.common.Lexer;
 import calculator.common.Parser;
 import calculator.common.Token;
 import calculator.common.ValidationError;
 
 public class CalcParser extends Parser<TokenType> {
 
 	public CalcParser(Lexer<Token<TokenType>> input) {
 		super(input);
 	}
 
 	public AST expr() {
 		AST node;
 
 		if (lookahead.type == TokenType.MINUS) {
 			node = handleNegativeNumber();
 		} else {
 			node = multiExpr();
 		}
 		
 		if (lookahead.type == TokenType.INT) {
			throw new ValidationError("INT not allaowed here: " + lookahead.text);
 		}
 
 		while (lookahead.type == TokenType.PLUS || lookahead.type == TokenType.MINUS) {
 			AST parentNode = new AST(lookahead);
 			match(lookahead.type);
 			parentNode.setLeft(node);
 			parentNode.setRight(multiExpr());
 			node = parentNode;
 		}
 
 		return node;
 	}
 
 	private AST handleNegativeNumber() {
 		AST node;
 		node = new AST(lookahead);
 		match(TokenType.MINUS);
 		node.setLeft(new AST(new CalcToken(TokenType.INT, "0")));
 		node.setRight(multiExpr());
 		return node;
 	}
 
 	public AST multiExpr() {
 		AST node;
 
 		if (lookahead.type == TokenType.MULTIPLY || lookahead.type == TokenType.DIVIDE) {
 			node = new AST(lookahead);
 			match(lookahead.type);
 			node.setLeft(matchPrimary());
 		} else {
 			node = matchPrimary();
 		}
 
 		while (lookahead.type == TokenType.MULTIPLY || lookahead.type == TokenType.DIVIDE) {
 			AST parentNode = new AST(lookahead);
 			match(lookahead.type);
 			parentNode.setLeft(node);
 			parentNode.setRight(multiExpr());
 			node = parentNode;
 		}
 
 		return node;
 	}
 
 	private AST matchPrimary() {
 		AST node;
 		if (lookahead.type == TokenType.INT) {
 			node = new AST(lookahead);
 			match(TokenType.INT);
 		} else if (lookahead.type == TokenType.LPARENTHESIS) {
 			match(TokenType.LPARENTHESIS);
 			node = expr();
 			match(TokenType.RPARENTHESIS);
 		} else
 			throw new ValidationError("Expecting number or left parenthesis. Found " + lookahead);
 		return node;
 	}
 }
