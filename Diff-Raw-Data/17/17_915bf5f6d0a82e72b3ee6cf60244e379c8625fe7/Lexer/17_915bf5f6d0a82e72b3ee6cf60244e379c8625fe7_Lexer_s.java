 package se.c0la.calc;
 
 import java.util.*;
 
 public class Lexer
 {
 	public Lexer()
 	{
 	}
 	
 	public List<Lexeme> tokenize(String data)
 	throws UnknownTokenException
 	{
 		StringBuffer currentToken = new StringBuffer();
 		
 		List<Lexeme> lexemes = new ArrayList<Lexeme>();
 		Lexeme lexeme = null;
 		for (int i = 0; i < data.length(); i++) {
 			String cur = data.substring(i, i+1);
 			if (cur.matches("[0-9.a-zA-Z_]")) {
 				currentToken.append(cur);
 				continue;
 			}
 			else if (currentToken.length() > 0) {
 				String token = currentToken.toString();
				if (token.matches("[0-9.]+")) {
 					lexeme = new Lexeme(TokenType.NUMBER, currentToken.toString());
 					lexemes.add(lexeme);				
 				} else {
 					lexeme = new Lexeme(TokenType.VARIABLE, currentToken.toString());
 					lexemes.add(lexeme);
 				}
 				
 				currentToken = new StringBuffer();
 			}
 			
 			char chr = data.charAt(i);
 			switch (chr) {
 				case '+':
 					lexeme = new Lexeme(TokenType.PLUS);
 					break;
 				case '-':
 					lexeme = new Lexeme(TokenType.MINUS);
 					break;
 				case '*':
 					lexeme = new Lexeme(TokenType.ASTERIX);
 					break;
 				case '/':
 					lexeme = new Lexeme(TokenType.SLASH);
 					break;
 				case '(':
 					if (lexeme != null && lexeme.getType() == TokenType.VARIABLE) {
 						lexemes.remove(lexemes.size() - 1);
 						lexeme = new Lexeme(TokenType.FUNCTION, lexeme.getValue());
 					} else {
 						lexeme = new Lexeme(TokenType.OPEN_PARANTHESIS);
 					}
 					break;
 				case ')':
 					lexeme = new Lexeme(TokenType.CLOSE_PARANTHESIS);
 					break;
 				case '^':
 					lexeme = new Lexeme(TokenType.EXPONENT);
 					break;
 				case '=':
 					lexeme = new Lexeme(TokenType.EQUALSIGN);
 					break;
 				case ' ':
 					continue;
 				default:
 					throw new UnknownTokenException("Unknown token: " + cur);
 			}
 			
 			lexemes.add(lexeme);
 		}
 		
 		// Add any trailing tokens
 		if (currentToken.length() > 0) {
 			String token = currentToken.toString();
			if (token.matches("[0-9.]+")) {
 				lexeme = new Lexeme(TokenType.NUMBER, currentToken.toString());
 				lexemes.add(lexeme);				
 			} else {
 				lexeme = new Lexeme(TokenType.VARIABLE, currentToken.toString());
 				lexemes.add(lexeme);
 			}
 		}
 		
 		return lexemes;
 	}
 }
