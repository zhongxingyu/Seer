 // JackTokenizer.java
 
 package SyntaxAnalyzer;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import org.apache.commons.lang.*;
 
 public class JackTokenizer {
   public String entireFile = "";
   public ArrayList<String> tokens; 
   public int counter;
   public String currentToken;
   public int binaryCount;
   public int variableCount;
   public Boolean Debug = false;
   public Boolean inComment = false;
   public Boolean firstComment = false;
   public String regexIdentifier = "[a-zA-Z_]+[a-zA-Z0-9_]*";
   public String regexSymbols = "[\\{\\}\\(\\)\\[\\]\\.,;+\\*\\-/&\\|<>=~]";
   String[] keywords = {"class", "constructor", "function", "method", "field", "static", "var", "int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return"};
 
 
   public enum Token {
     KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST;
   }
   
   public enum Keyword {
 	CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS;
   }
 
   // Parses the input stream
   public JackTokenizer(BufferedReader stream) throws IOException {
     tokens = new ArrayList<String>();
 	
     // Read in file
     String line = stream.readLine();
     while (line != null){
       // Get rid of comments
       String[] split = line.split("//");
       line = split[0];
 	  if (!line.equals(null)){
         entireFile = entireFile + line;
 	  }
 	  
       // Read next line
       line = stream.readLine();
     }
 	
 	// Check that file was read in okay
 	if (Debug) System.out.println(entireFile);
 	
 	// Remove all the comments in original file
 	entireFile = entireFile.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");
 
 	// Extract string constants from the file
 	ArrayList<String> largeTokens = new ArrayList<String>();
 	int index1;
 	int index2;
 	
 	while (entireFile.indexOf('"') != -1){
 	  index1 = entireFile.indexOf('"');
 	  index2 = entireFile.indexOf('"', index1 + 1);
 	  
 	  if (index2 == -1) {
 	    System.err.println("Error finding end of quote.");
 		System.exit(1);
 	  } else {
 	    largeTokens.add(entireFile.substring(0, index1));
 		largeTokens.add(entireFile.substring(index1, index2));
 		entireFile = entireFile.substring(index2+1);
 	  }
 	}
 	largeTokens.add(entireFile);
 	
 	// Loop through all the larger chunks and separate them into tokens
 	for (int j=0; j < largeTokens.size(); j++){
 		
 		String nextToken = largeTokens.get(j);
 		
 		// Check if the token is a string constant, if so add directly to tokens list
 		if (nextToken.indexOf('"') != -1){
 		  Boolean check = tokens.add(nextToken);
 		  if (check == false){
 		    System.err.println("Error adding line to tokens list");
 		  }
 		} else {
 			// Separate file based on symbols
 			String delimiters = "{}()[].,;+-*/&|<>=~ ";
 			StringTokenizer tokenized = new StringTokenizer(nextToken, delimiters, true);
 			
 			// Loop through all the tokens
 			while (tokenized.hasMoreTokens()){
 			  String next = tokenized.nextToken();
 				
 			  // Get rid of all whitespace
 			  int i=0;
 			  while (i < next.length()){
 				try {
 				  while (Character.isWhitespace(next.charAt(i))){
 					next = next.substring(0, i) + next.substring(i+1);
 					if (next.length() == 0){
 					  break;
 					}
 				  }
 				} catch (IndexOutOfBoundsException e){
 				  System.err.println("Error trying to remove whitespace.");  
 				}
 				i++;
 			  }
 
 			  // Make sure that next is not empty or null
 			  if (!next.equals(null) && !next.equals("")){
 				// Add token to array
 				if (Debug) System.out.println(next);
 				Boolean check = tokens.add(next);
 				if (check == false){
 				  System.err.println("Error adding line to tokens list");
 				}
 			  }
 			}
 		}
 	}
 	  
 	  
     counter = 0;
     binaryCount = 0;
     variableCount = 16;
     currentToken = tokens.get(counter);
   }
 
   // Are there more comamnds in input?
   public Boolean hasMoreTokens() {
     if (counter < tokens.size()-1) {
       return true;
     } else {
       return false;
     }
   }
 
   // Reads next command and makes it current command
   public void advance() {
     counter++;
     currentToken = tokens.get(counter);
   }
 
   // Returns the type of the current VM command
   public Token tokenType() {
     // Checks token type
     if (currentToken.length() == 0)
       return null;
 	  
 	// Match regular expressions
	if (currentToken.matches(regexIdentifier)){
	  return Token.IDENTIFIER;
 	}
 	if (currentToken.matches(regexSymbols)){
 	  return Token.SYMBOL;
 	}
	if(Arrays.asList(keywords).contains(currentToken)) {
		return Token.KEYWORD;
	}
 	try {
 		Integer.parseInt(currentToken);
 		return Token.INT_CONST;
 	}catch(NumberFormatException x){
 		//do nothing
 	}
 	if(currentToken.indexOf('"') != -1) {
 		return Token.STRING_CONST;
 	}
 	return Token.KEYWORD;
   }
   
   // Returns the keyword which is the current token
   public String keyWord() {
 	if (tokenType() != Token.KEYWORD) {
 	  System.err.println("Error retrieving keyword");
 	  return null;
 	}
     return currentToken;
   }
 
   // Returns the character which is the current token
   public char symbol() {
 	if (tokenType() != Token.SYMBOL) {
 	  System.err.println("Error retrieving symbol");
 	  return '0';
 	}
 	
 	return currentToken.charAt(0);
   }
 
   // Returns the identifier which is the current token
   public String identifier() {
     if (tokenType() != Token.IDENTIFIER){
 	  System.err.println("Error retrieving identifier");
 	  return null;
 	}
 	
     return currentToken;
   }
   
   public Integer intVal() {
     if (tokenType() != Token.INT_CONST){
 	  System.err.println("Error retrieving integer constant");
 	  return null;
 	}
 	int num = Integer.parseInt( currentToken );
     return num;
   }
   
   public String stringVal() {
     if (tokenType() != Token.STRING_CONST){
 	  System.err.println("Error retrieving string constant");
 	  return null;
 	}
 	
 	// String constants have the first " character still, so return string without it
     return currentToken.substring(1);
   }
 }
