 package dev;
 
import java.io.IOException;

 import lexer.*;
 
 @SuppressWarnings("unused")
 public class ConsoleLexer {
 
 	/**
 	 * @param args
 	 */
	public static void main(String[] args) throws IOException {
 		Lexer lexer = new Lexer(System.in);
		Yytoken token = null;
 		do {
			try {
				token = lexer.yylex();
			} catch (IOException ioe) {
				throw new IOException("Error during lexer.yylex() call: ", ioe);
			}
 			System.out.println("token = " + token);
		} while (token != null);
 		
 		System.out.println("test face");
 
 	}
 
 }
