 // CompilationEngine.java
 
 package SyntaxAnalyzer;
 
 import java.io.*;
 import SyntaxAnalyzer.JackTokenizer.Token;
 
 public class CompilationEngine {
   public OutputStreamWriter outStream;
   public JackTokenizer tokenizer;
   public JackTokenizer.Token token_type;
 
   // Creates a new compilation engine
   public CompilationEngine(JackTokenizer token, OutputStreamWriter stream) throws IOException {
     outStream = stream;
 	tokenizer = token;
   }
 
   // Compiles a complete class
   public void CompileClass() throws IOException {
 	//print default <tokens>
 	outStream.write("<tokens>\n");
 	// Loop through tokens and handle each one
     while (tokenizer.hasMoreTokens()) {
 	  //assigns token's type
       token_type = tokenizer.tokenType();
 	//returns token's corresponding XML line
 		if(token_type == Token.KEYWORD) {
 			outStream.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
 		}
 		if(token_type == Token.SYMBOL) {
			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 		}
 		if(token_type == Token.IDENTIFIER) {
 			outStream.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
 		}
 		if(token_type == Token.INT_CONST) {	
 			outStream.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");
 		}
 		if(token_type == Token.STRING_CONST) {
 			outStream.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
 		}
 		
       tokenizer.advance();
     }
 
     // Handle last token
     token_type = tokenizer.tokenType();
 	
 	outStream.write("</tokens>\n");
   
   }
   
   // Compiles a static declaration or a field declaration
   public void CompileClassVarDec() {
   
   }
   
   // Compiles a complete method, function, or constructor
   public void CompileSubroutine() {
   
   }
   
   // Compiles a parameter list
   public void compileParameterList() {
   
   }
   
   // Compiles a var declaration
   public void compileVarDec() {
   
   }
   
   // Compiles a sentence of statements
   public void compileStatements() {
   
   }
   
   // Compiles a do statement
   public void compileDo() {
   
   }
   
   // Compiles a let statement
   public void compileLet() {
   
   }
   
   // Compiles a while statement
   public void compileWhile() {
   
   }
   
   // Compiles a return statement
   public void compileReturn() {
   
   }
   
   // Compiles an if statement
   public void compileIf() {
   
   }
   
   // Compiles an expression
   public void CompileExpression() {
   
   }
   
   // Compiles a term
   public void CompileTerm() {
   
   }
   
   // Compiles a comma-separated list of expressions
   public void CompileExpressionList() {
   
   }
 
 }
