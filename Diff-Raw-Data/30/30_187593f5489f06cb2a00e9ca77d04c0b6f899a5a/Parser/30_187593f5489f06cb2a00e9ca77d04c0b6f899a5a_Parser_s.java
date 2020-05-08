 package parser;
 
 import java.util.Arrays;
 
 
 /**
  * 
  */
 public final class Parser {
 
 	Tokenizer te;
 	transient Tree<String> tokenTree;
 	transient Token ahead;
 	
 	void match(String token) throws ParseException{
 	    if(token == null || ahead.getValue().equals(token)) {
 	    	try {
 	    		ahead = te.nextToken();
 	        } catch(TokenNotFoundException e) {
 	        	throw new ParseException(e);
 	        }
 	    } else {
 	    	throw new ParseException();
 	    }		
 	}
 
 	void rexp() throws ParseException{
 		System.out.println("rexp");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		rexp1();
 		rexpPrime();
 		System.out.println("rexp ENDS");
 	}
 	
 	void rexp1() throws ParseException{
 		System.out.println("rexp1");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		rexp2();
 		rexp1Prime();
 		System.out.println("rexp1 ENDS");
 	}
 	
 	void rexpPrime() throws ParseException{
 		System.out.println("rexpPrime");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		Token token;
 
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.TOKEN_UNION){
 				token = ahead;
	    		match((String)ahead.getValue());
 	    		rexp1();
 	    		// Add to Tree
 	    		System.out.println("Saved Here: " + token.getValue());
 	    		
 			}else{
 				break;
 			}
 		}
 		
 		match(null);
 		System.out.println("rexpPrime ENDS");
 	}
 	
 	void rexp1Prime() throws ParseException{
 		System.out.println("rexp1Prime");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		rexp2();
 		match(null);
 		System.out.println("rexp1Prime ENDS");
 	}
 	
 	void rexp2() throws ParseException{
 		System.out.println("rexp2");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		Token token;
 		
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.TOKEN_L_PAREN){
 				token = ahead;
 				match((String)ahead.getValue());
 	    		rexp();
	    		match((String)ahead.getValue());
 	    		rexp2_tail();
 	    		// Add to Tree
 	    		System.out.println("Saved Here: " + token.getValue());
 	    
 			}else if(ahead.getType() == RegexTokenType.RE_CHAR){
 				token = ahead;
 				match((String)ahead.getValue());
 				rexp2_tail();
 				// Add to Tree
 				System.out.println("Saved Here: " + token.getValue());
 				
 			}else{
 				rexp3();
 				break;
 			}
 		}		
 		
 		System.out.println("rexp2 ENDS");
 	}
 	
 	void rexp2_tail() throws ParseException{
 		Token token;
 		
 		System.out.println("rexp2_tail");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 
 		if(ahead==null) {
 		      throw new ParseException();
 		}   
 		
		if(ahead.getType() == RegexTokenType.TOKEN_ASTE || ahead.getType() == RegexTokenType.TOKEN_ASTE){
 			token = ahead;
 			match((String)ahead.getValue());
 			// Add to Tree
 			System.out.println("Saved Here: " + token.getValue());
 		}
 		
 		match(null);		
 		System.out.println("rexp2_tail ENDS");
 	}
 	
 	void rexp3() throws ParseException{
 		System.out.println("rexp3");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		char_class();
 		match(null);
 		System.out.println("rexp3 ENDS");
 	}
 	
 	void char_class() throws ParseException{
 		System.out.println("char_class");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		Token token;
 		
 		if(ahead.getType() == RegexTokenType.TOKEN_DOT){
 			// Add to Tree
 			System.out.println("Saved Here: " + ahead.getValue());
 			match(null);
 		}
 		
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.TOKEN_L_BRACKET){
 				token = ahead;
 				match((String)ahead.getValue());
 				char_class1();	
 				// Add to Tree
 				System.out.println("Saved Here: " + token.getValue());
 				
 			}else if(ahead.getType() == RegexTokenType.TOKEN_DEFINE){
 				token = ahead;
 				match((String) ahead.getValue());		
 				// Add to Tree
 				System.out.println("Saved Here: " + token.getValue());
 				break;
 				
 			}else{
 				break;
 			}
 		}
 		System.out.println("ahead: " + ahead.getValue());
 		System.out.println("char_class ENDS");
 	}
 		
 	void char_class1() throws ParseException{
 		System.out.println("char_class1");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		char_set_list();
 		exclude_set();		
 		System.out.println("char_class1 ENDS");
 	}
 	
 	void char_set_list() throws ParseException{
 		System.out.println("char_set_list");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		Token token;
 		
 		char_set();
 		
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.TOKEN_R_BRACKET){
 				token = ahead;
 				match((String)ahead.getValue());
 				// Add to Tree		
 				System.out.println("Saved Here: " + token.getValue());
 				
 			}else{
 				break;
 			}
 		}
 		
 		match(null);
 		System.out.println("char_set_list ENDS");
 	}
 	
 	void char_set() throws ParseException{
 		System.out.println("char_set");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		Token token;
 		
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.CLS_CHAR){
 				token = ahead;
 				char_set_tail();
 				// Add to Tree
 				System.out.println("Saved Here: " + token.getValue());
 				
 			}else{
 				break;
 			}
 		}
 		
 		System.out.println("char_set ENDS");
 	}
 	
 	void char_set_tail() throws ParseException{		
 		System.out.println("char_set_tail");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		Token token;
 		
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.CLS_CHAR){
 				// Add to Tree
 				System.out.println("Saved Here: " + ahead.getValue());
 				
 			}else{
 				break;
 			}
 			
 		}
 		
 		match(null);
 		System.out.println("char_set_tail ENDS");
 	}
 	
 	void exclude_set() throws ParseException{
 		System.out.println("exclude_set");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		Token token;
 		
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.TOKEN_UP){
 				token = ahead;
 				match((String)ahead.getValue());
 				char_set();
 				// Add to Tree
 				System.out.println("Saved Here: " + token.getValue());
 				
 				if(ahead.getType() == RegexTokenType.TOKEN_R_BRACKET){
 					token = ahead;
 					match((String)ahead.getValue());
 					// Add to Tree
 					System.out.println("Saved Here: " + token.getValue());
 					
 					if(ahead.getType() == RegexTokenType.TOKEN_IN){
 						token = ahead;
 						match((String)ahead.getValue());
 						exclude_set_tail();
 						// Add to Tree
 						System.out.println("Saved Here: " + token.getValue());
 						
 					}					
 				}
 			}
 			
 		}		
 		System.out.println("exclude_set ENDS");
 		
 	}
 	
 	void exclude_set_tail() throws ParseException{
 		System.out.println("exclude_set_tail");
 		if(ahead!=null){
 			System.out.println("ahead type: " + ahead.getType());
 			System.out.println("ahead value: " + ahead.getValue());
 		}
 		
 		Token token;
 		
 		while(ahead!=null){
 			if(ahead.getType() == RegexTokenType.LEX_L_BRACKET){		
 				token = ahead;
 				match((String)ahead.getValue());			
 				char_set();
 				// Add to Tree
 				System.out.println("Saved Here: " + token.getValue());
 				
 				match(RegexTokenType.LEX_R_BRACKET);
 				
 			}else if(ahead.getType() == RegexTokenType.TOKEN_DEFINE){
 				token = ahead;
 				match((String) ahead.getValue());
 				// Add to Tree
 				System.out.println("Saved Here: " + token.getValue());
 				break;	
 				
 			}else {
 				break;
 			}
 		}
 		match(null);
 		System.out.println("exclude_set_tail ENDS");
 	}
 
 	public Parser() {
 	    te = new Tokenizer(Arrays.asList((TokenType[])RegexTokenType.values()));
 	}
 
 	public Tree parse(String input) throws ParseException {
 	    te.setInput(input); 
 
 	    match(null);
 
 	    if(ahead == null) {
 	      throw new ParseException();
 	    }
 
 	    tokenTree = new Tree<String>((String)ahead.getValue());
 
 	    rexp();
 
 	    if(ahead != null) {
 	      throw new ParseException();
 	    }
 
 	    return tokenTree;
 	}
 	
 }
