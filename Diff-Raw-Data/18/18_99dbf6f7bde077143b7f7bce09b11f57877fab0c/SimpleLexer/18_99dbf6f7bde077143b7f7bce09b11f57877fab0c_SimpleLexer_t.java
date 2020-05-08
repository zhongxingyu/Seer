 package oop1;
 
 public class SimpleLexer {
 	private int offset;
 	private final String input;
 	private final TokenReader[] readers;
 
 	public SimpleLexer(String input, TokenReader... readers) {
 		this.input = input;
 		this.readers = readers;
 		this.offset = 0;
 	}
 
 	public Token readNextToken() throws Exception {
 		int maxLength = -1;
 		Token betterToken = null;
 		Token token;
 		
 		if (!this.hasNextTokens()){
 			throw new NullTokenException("End of string");
 		}
 
 //        String input = this.input.substring(offset);
 		for (TokenReader reader : readers) {
 			token = reader.tryReadToken(input, offset);
 			if (token != null && token.getText().length() > maxLength) {
 				betterToken = token;
 				maxLength = token.getText().length();
 			}
 		}
 
 		if (betterToken == null) {
 //            maxLength = 1;
 			throw new NullTokenException("Can't read token beginning from " + (offset + 1)
 					+ " symbol. That is letter '" + this.input.charAt(offset) + "'. Context: " + this.input.substring(offset - 3, offset + 3));
 		}
 		offset += maxLength;
 
 		return betterToken;
 	}
 
 	public boolean hasNextTokens() {
 		return this.offset != this.input.length();
 	}
 
//    public int getOffset() {
//        return this.offset;
//    }
 
 	public class NullTokenException extends Exception {
 		public NullTokenException(String message) {
 			super(message);
 		}
 	}
 }
