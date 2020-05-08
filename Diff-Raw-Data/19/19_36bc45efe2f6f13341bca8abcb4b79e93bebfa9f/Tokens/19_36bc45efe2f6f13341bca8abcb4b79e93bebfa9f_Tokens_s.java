 package forth;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Tokens {
 	List<Token> tokens;
 
 	public Tokens(String program) {
 		tokens = new ArrayList<Token>();
 		int start = 0;
 		for (int i=0; i < program.length(); i++) {
 			char c = program.charAt(i);
 			switch (c) {
 			case ' ':
 			case '\n':
 			case '\r':
 			case '\t':
 				if (c == start) {
 					start += 1;
 					continue;
 				} else {
 					tokens.add(Token.create(program.substring(start, i)));
 					start = i+1;
 				}
 			case '#':
 				for (int j=i; j < program.length(); j++) {
 					char cj = program.charAt(i);
 					if (cj == '\n' || cj == '\r') {
 						i = cj;
 						start = i+1;
 						break;
 					}
 				}
 				continue;
 			default:
 				continue;
 			}
 		}
 		
 		/* do not forget the last token */
 		if (start < program.length()) {
 			tokens.add(Token.create(program.substring(start)));
 		}
 	}
 
 	public Token get(int position) {
 		return tokens.get(position);
 	}
 
 }
