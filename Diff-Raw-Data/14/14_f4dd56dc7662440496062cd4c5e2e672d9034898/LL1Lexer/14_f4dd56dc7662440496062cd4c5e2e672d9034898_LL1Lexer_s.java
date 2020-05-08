 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Scanner;
 
 
 public class LL1Lexer {
 	private Scanner scanner;
 	private Queue<Token> tokenBuffer;
 	
 	public LL1Lexer(File f) {
 		try {
 			scanner = new Scanner(f);
 			tokenBuffer = new LinkedList<Token>();
 //			scanner.useDelimiter("( |,|;|%|\\+|-|:)");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean hasNext() {
 		return scanner.hasNext() || !tokenBuffer.isEmpty();
 	}
 	
 	public void fillTokenBuffer(String s) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			if (c == ' ' || c == ',' || c == ';' || c == '&' || c == '+' || c == '-' || c == '(' || c == ')') {
 				if (sb.toString().length() > 0) {
 					tokenBuffer.add(new Token(sb.toString()));
 				}
 				tokenBuffer.add(new Token(new Character(c).toString()));
 				sb.setLength(0);
 			} else if (c == ':' && i + 1 < s.length() && s.charAt(i + 1) == '=') {
 				if (sb.toString().length() > 0) {
 					tokenBuffer.add(new Token(sb.toString()));
 				}
 				tokenBuffer.add(new Token(":="));
 				i++;
 				sb.setLength(0);
 			} else {
 				sb.append(c);
 				
 				if (i == s.length() - 1) {
 					tokenBuffer.add(new Token(sb.toString()));
 				}
 			}
 		}
 	}
 	
 	public Token next() {
		if (!scanner.hasNext()) {
 			return null;
 		}
 		
 		while (tokenBuffer.isEmpty()) {
 			fillTokenBuffer(scanner.next());
 		}
 		
 		return tokenBuffer.remove();
 	}
 }
