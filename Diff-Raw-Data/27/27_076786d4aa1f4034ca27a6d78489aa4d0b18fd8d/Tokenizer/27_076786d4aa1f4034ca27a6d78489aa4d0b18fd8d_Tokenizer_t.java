 package parser;
 
 import java.util.Scanner;
 
 public class Tokenizer {
 
     private FileToInput input_stream;
     private boolean peek;
     private Token current;
     
     /**
      * @param scanner
      */
     public Tokenizer(Scanner scanner) {
     	this.input_stream = new FileToInput(scanner);
         this.peek = false;
         this.current = null;
     }
 
     /**m
      * @return 
      */
     public Token getNextToken() {
     	if(peek) {
     		peek = false;
             return current;
         }
         else {
             current = makeNewToken();
             return current;
         }
     }
     
     /**
      * @return
      */
     public Token peekNextToken() {
     	if(peek) {
     		return current;
         }
         else {
         	current = getNextToken();
             peek = true;
             return current;
         }
     }
     
     public Token makeNewToken() {
         char t = input_stream.getNext();
             
         Token result = null;
             
             switch(t) {
                     //ignore comment lines
                     case '%':
                             if(input_stream.peekNext() == '%') {
                                     input_stream.gotoNextLine();
                                     return makeNewToken();
                             }
                             else {
                                     result = new Token(TokenType.LITERAL, "%");
                             }
                             break;
                     //ignore whitespace
                     case '\t':
                     case ' ':
                             return makeNewToken();
                     //handle possible line returns
                     case '\n':
                             result = new Token(TokenType.EOL, "\n");
                             break;
                     //defined name
                     case '$':
                             String name = new String();
                             while(validDefinedCharacters()) {
 
                                     name += input_stream.getNext();
                             }
                            //System.out.println(input_stream.peekNext());
                             result = new Token(TokenType.DEFINED, name);
                             break;
                     //alternation
                     case '|':
                             result = new Token(TokenType.UNION, "|");
                             break;
                     //repetition >= 0
                     case '*':
                             result = new Token(TokenType.KLEENE, "*");
                             break;
                     //repetition > 0
                     case '+':
                    		//System.out.println("PLUS");
                             result = new Token(TokenType.PLUS, "+");
                             break;
                     //dash 
                     case '-':
                             result = new Token(TokenType.DASH, "-");
                             break;
                     //caret 
                     case '^':
                             result = new Token(TokenType.CARET, "^");
                             break;
                     //dot
                     case '.':
                             result = new Token(TokenType.DOT, ".");
                             break;
                     //left bracket
                     case '[':
                             result = new Token(TokenType.LBRACKET, "[");
                             break;
                     //right bracket
                     case ']':
                             result = new Token(TokenType.RBRACKET, "]");
                             break;
                     //left parentheses 
                     case '(':
                             result = new Token(TokenType.LPAREN, "(");
                             break;
                     //right parentheses 
                     case ')':
                             result = new Token(TokenType.RPAREN, ")");
                             break;
                     //IN & I
                     case 'I':                
                     		FileToInput replace = new FileToInput(input_stream.getInput(), input_stream.getBuffer(), input_stream.getPos(), input_stream.getPeek());
                     		input_stream.getNext();                    		
                     	
                     		if(input_stream.peekNext() == ' ') {
                             	result = new Token(TokenType.IN, "IN");
                             	replace.getNext();
                         
                     		}else if(input_stream.peekNext() != ' ') {
                     			result = new Token(TokenType.LITERAL, new String() + 'I');
                                
             				}else {
                                 result = new Token(TokenType.LITERAL, "L");
                             }
                     		input_stream = replace;
                             break;
                     //escaped characters
                     case '\\':
                             String escaped = "\\" + input_stream.getNext();
                             result = new Token(TokenType.LITERAL, escaped);
                             break;
                     //character literals
                     default:
                             result = new Token(TokenType.LITERAL, new String() + t);
             }
             
             return result;
     }
     
     public boolean gotoNextLine() {
     	peek = false;
         return input_stream.gotoNextLine();
     }
     
     public boolean validDefinedCharacters() {
         int t = ((int)input_stream.peekNext());
         if((t >= 48 && t <= 57) || (t >= 65 && t <= 90) || (t >= 97 && t <= 122) || (t == 45) || (t == 95)) {
         	return true;
         }
         else {
             return false;
         }
     }
 }
