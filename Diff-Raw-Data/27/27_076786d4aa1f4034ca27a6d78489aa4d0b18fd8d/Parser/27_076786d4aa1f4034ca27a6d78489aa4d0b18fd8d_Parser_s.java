 package parser;
 
 import java.util.Stack;
 
 /**
  * 
  */
 public final class Parser {
 
 	Tokenizer te;
 	Stack<String> tokenStack;
 
 	boolean DEBUG;
 	
 	// <rexp> -> <rexp1> <rexp>
 	void rexp() throws ParseException {	
 		rexp1();
 		rexpPrime();
 
 	}
 	
 	// <rexp1> -> <rexp2> <rexp1> | E
 	void rexp1() throws ParseException {		
 		rexp2();
 		rexp1Prime();
 		
 	}
 	
 	// rexpPrime -> UNION <rexp1> <rexp> | E
 	void rexpPrime() throws ParseException {
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.UNION) {				
 			if(DEBUG) {
 				System.out.println("UNION found. Not in char_class.");
 			}
 				
 			if(DEBUG)
 				System.out.println("|");
 			tokenStack.push("|");
 			te.getNextToken();
 			rexp1();
 			rexpPrime();
 			    		
 		}else {
 			return;
 		}
 	
 	}
 	
 	// <rexp1> -> <rexp2> <rexp1>  | E        
 	void rexp1Prime() throws ParseException {	
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.LPAREN || ahead.getType() == TokenType.DOT || ahead.getType() == TokenType.LBRACKET || ahead.getType() == TokenType.DEFINED) {
 			rexp2();
 			rexp1Prime();		
 		
 		} else if(ahead.getType() == TokenType.LITERAL) {
 			boolean flag = check_valid(ahead, RE_CHAR);
 			if(!flag){
 				System.out.println(ahead.getValue() + " was Not Valied RE_CHAR.");
 				return;
 			}
 			rexp2();
 			rexp1Prime();	
 		
 		} else {
 			return;
 		}
 		
 	}
 	
 	// <rexp2> -> (<rexp>) <rexp2-tail>  | RE_CHAR <rexp2-tail> | <rexp3>
 	void rexp2() throws ParseException {
 		Token ahead = te.peekNextToken();
 		if(ahead!=null) {
 		
 			if(ahead.getType() == TokenType.LPAREN) {
 				if(DEBUG) {
 					System.out.println("L_PAREN found. In rexp2.");
 				}
 
 				if(DEBUG)
 					System.out.println("(");
 				tokenStack.push("(");
 				te.getNextToken();
 
 			    rexp();
 			    
 			    if(DEBUG)
 			    	System.out.println(")");
 			    tokenStack.push(")");
 			    te.getNextToken();
 			    
 				if(DEBUG) {
 					System.out.println("PAREN successfully matched.");
 				}
 			    
 				rexp2_tail();
 			    
 			}else if(ahead.getType() ==  TokenType.LITERAL) {
 				if(DEBUG) {
 					System.out.println("LITERAL found. In rexp2.");
 				}
 				
 				boolean flag = check_valid(ahead, RE_CHAR);
 				if(!flag) {
 					throw new ParseException();
 				}
 				if(DEBUG)
 					System.out.println((String)ahead.getValue());
 				tokenStack.push((String)ahead.getValue());
 				te.getNextToken(); 
 
 				//System.out.println("Next Token: " + te.peekNextToken().getValue());
 				
 				rexp2_tail();
 				
 			} else {
 				rexp3();
 			}
 		
 		}
 
 	}
 	
 	// <rexp2-tail> -> * | + | E
 	void rexp2_tail() throws ParseException {		
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.KLEENE) {
 			if(DEBUG) {
 				System.out.println(" * Found. In rexp2_tail.");
 			}
 
			if(DEBUG)
				System.out.println("*");
 			tokenStack.push("*");
 			te.getNextToken(); 
 				
 		}else if(ahead.getType() == TokenType.PLUS) {
 			if(DEBUG) {
				System.out.println(" * Found. In rexp2_tail.");
 			}
 
			if(DEBUG)
				System.out.println("+");
 			tokenStack.push("+");
 			te.getNextToken(); 
 			
 		}else {
 				return;
 			
 		}
 	}
 	
 	// <rexp3> -> <char-class>  | E   
 	void rexp3() throws ParseException {
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.DOT || ahead.getType() == TokenType.LBRACKET || ahead.getType() == TokenType.DEFINED) {
 			char_class();
 		
 		} else {
 			return;
 			
 		}
 	}
 	
 	// <char-class> -> .  |  [ <char-class1>  | <defined-class>
 	void char_class() throws ParseException {
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.DOT) {
 			if(DEBUG)
 				System.out.println(".");
 			tokenStack.push(".");
 			te.getNextToken();
 		
 		}else if(ahead.getType() == TokenType.LBRACKET) {
 			if(DEBUG)
 				System.out.println("[");
 			tokenStack.push("[");
 			te.getNextToken();
 			char_class1();	
 
 		}else { // defined_class
 			if(DEBUG)
 				System.out.println((String) ahead.getValue());
 			tokenStack.push((String) ahead.getValue());
 			te.getNextToken();
 			defined_class(ahead, false);
 		}
 		
 		return;
 	}
 	
 	// <char-class1> -> <char-set-list> | <exclude-set>
 	void char_class1() throws ParseException {
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.CARET) {
 			exclude_set();		
 			
 		}else {
 			char_set_list();
 		}
 
 	}
 	
 	// <char-set-list> -> <char-set> <char-set-list> | ]
 	void char_set_list() throws ParseException {
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.LITERAL || ahead.getType() == TokenType.DOT) {			
 			if(!check_valid(ahead, CLS_CHAR))
 				return;
 			char_set();			
 			char_set_list();
 			
 		}else { 
 	        if(DEBUG)
 	        	System.out.println("]");
 			tokenStack.push("]");
 			return;
 		}
 
 	}
 	
 	// <char-set> -> CLS_CHAR <char-set-tail> 
 	void char_set() throws ParseException {	
 		Token ahead = te.peekNextToken();
 		if(DEBUG)
 			System.out.println((String)ahead.getValue());
 		tokenStack.push((String)ahead.getValue());
 		te.getNextToken();
 		
 		if(!check_valid(ahead, CLS_CHAR)) {
 			System.out.println("ahead was not CLS_CHAR: " + ahead.getValue());
 			throw new ParseException();
         }
                
         char_set_tail(ahead);
 	}
 	
 	// <char-set-tail> -> - CLS_CHAR | E
 	void char_set_tail(Token start) throws ParseException {	
 		Token ahead = te.peekNextToken();
 		if(ahead.getType() == TokenType.DASH) {
 			if(DEBUG)
 				System.out.println("-");
 			tokenStack.push("-");
             te.getNextToken();
             
             Token end = te.getNextToken();
             tokenStack.push((String)end.getValue());
             if(DEBUG)
             	System.out.println((String)end.getValue());
                   
             if(!check_valid(end, CLS_CHAR)) {
             	System.out.println("end was not CLS_CHAR: " + end.getValue());
             	throw new ParseException();
             }
    		
 		}
 	}
 	
 	// <exclude-set> -> ^ <char-set>] IN <exclude-set-tail>  
 	void exclude_set() throws ParseException {
 		 if(DEBUG)
 			 System.out.println("^");
 		 tokenStack.push("^");
          te.getNextToken();
          
         char_set();
          
          if(DEBUG)
         	 System.out.println("]");
          tokenStack.push("]");
          te.getNextToken();
          //tokenStack.push("SCOPE_OUT");        
          
          if(DEBUG)
         	 System.out.println(TokenType.IN);
          tokenStack.push("IN");
          te.getNextToken();
          exclude_set_tail();
 
 	}
 	
 	// <exclude-set-tail> -> [<char-set>]  | <defined-class>
 	 void exclude_set_tail() throws ParseException {
 		 Token ahead = te.peekNextToken();
 		 if(ahead.getType() == TokenType.LBRACKET) {
 			 if(DEBUG)
 				 System.out.println(TokenType.LBRACKET);
 			 tokenStack.push("[");
 			 te.getNextToken();
 			 
              char_set();
              
              if(DEBUG) 
             	 System.out.println("]");
              tokenStack.push("]");
              te.getNextToken();
 
          }else {
         	 if(DEBUG)
         		 System.out.println((String)ahead.getValue());
         	 tokenStack.push((String)ahead.getValue());
         	 te.getNextToken();
              defined_class(ahead, true);
 
          }
 
 	}
 	 
     void defined_class(Token token, boolean flag) throws ParseException {    
          return;         
      }
      
      boolean check_valid(Token token, String[] set) {
     	 for(int i = 0; i < set.length; i++) {
     		 if(set[i].equals(token.getValue())) {
     			 return true;
              }
          }
          return false;
      }
      
      private static final String[] RE_CHAR = {
                      "\\ ", "!", "\\\"", "#", "$", "%", "&", "\\\'", "\\(", "\\)", "\\*", "\\+", ",", "-", 
                      "\\.", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "\\?", 
                      "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", 
                      "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\\[", "\\\\", "\\]", "^", "_", 
                      "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", 
                      "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "\\|", "}", "~"             
      };
      
      private static final String[] CLS_CHAR = {
                      " ", "!", "\"", "#", "$", "%", "&", "\'", "(", ")", "*", "+", ",", "\\-", ".", "/",
                      "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", 
                      "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", 
                      "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\\[", "\\\\", "\\]", "\\^", "_", 
                      "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", 
                      "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "~"               
      };
 	
 	public Parser(Tokenizer te, boolean DEBUG) throws ParseException {
 		 this.te = te;
 		 this.DEBUG = DEBUG;
 		 tokenStack = new Stack<String>();
 		 
 		 rexp();
 	}
 	
 	public Stack<String> getTokenStack(){
 		return tokenStack;
 	}
 	
 }
