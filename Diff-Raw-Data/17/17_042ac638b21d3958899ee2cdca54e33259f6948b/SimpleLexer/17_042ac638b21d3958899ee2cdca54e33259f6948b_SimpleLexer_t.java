 package oop1;
 
 public class SimpleLexer {
 	private int offset;
 	private String input;
 	private TokenReader[] readers;
 
 	public SimpleLexer(String input, TokenReader... readers) {
 		this.input = input;
 		this.readers = readers;
 		this.offset = 0;
 	}
 
 	public Token readNextToken() {
 		int maxLength = -1;
 		Token betterToken = null;
 		Token token;
 		
 		for (TokenReader reader : readers) {
 			String input = this.input.substring(offset);
 			token = reader.tryReadToken(input);
 			if (token != null && token.getText().length() > maxLength){
 				betterToken = token;
 				maxLength = token.getText().length();
 			}
 		}	
		
 		if (betterToken != null){
 			offset += maxLength; 
 		}
		
 		return betterToken;
 	}
 
 	public boolean hasNextTokens() {
 		return this.offset != this.input.length(); 
 	}
 }
